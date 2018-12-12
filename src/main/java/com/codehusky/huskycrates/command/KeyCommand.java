package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.util.Optional;

public class KeyCommand implements CommandExecutor {
    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        // possible crate
        Optional<Crate> crate = args.getOne(Text.of("crate"));
        // possible key
        Optional<Key> key = args.getOne(Text.of("key"));
        // possible amount
        Optional<Integer> pamount = args.getOne(Text.of("amount"));
        // is virtual
        boolean isVirtual = args.getOne(Text.of("virtual")).isPresent();

        // possible player
        Optional<Player> player = args.getOne(Text.of("player"));
        // possible all
        Optional<String> all = args.getOne(Text.of("all"));

        // key
        Key workingKey = null;

        // check if user has permission to give virtual keys
        if (isVirtual && !src.hasPermission("huskycrates.key.virtual")) {
            throw new CommandPermissionException(Text.of(TextColors.RED, "You do not have permission to give out virtual keys."));
        }

        // check if crate is present
        if (crate.isPresent()) {
            // check if crate has key
            if (crate.get().hasLocalKey()) {
                // get key
                workingKey = crate.get().getLocalKey();
            }
        }
        // is key is present
        else if (key.isPresent()) {
            // get key
            workingKey = key.get();
        }

        // if no working key
        if (workingKey == null) {
            throw new CommandException(HuskyCrates.keyCommandMessages.getCrateNoLocalKey());
        }

        // if no virtual key and not virtually given
        if (workingKey.isVirtual() && !isVirtual) {
            // throw exception
            throw new CommandException(HuskyCrates.keyCommandMessages.getCrateKeyVirtual());
        }
        // get amount if present, else 1
        int amount = pamount.orElse(1);
        // get name of key
        String keyName = crate.isPresent() ? crate.get().getName() : key.get().getName();

        // if all is present
        if (all.isPresent()) {
            /* Deliver keys to all players */
            if (!src.hasPermission("huskycrates.key.all")) {
                throw new CommandPermissionException(Text.of(TextColors.RED, "You do not have permission to give everyone keys."));
            }
            // init counter
            int deliveredTo = 0;
            // for each player
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                // offer key
                boolean result = offerKey(isVirtual, workingKey, amount, p);

                // if key is not successfully added
                if (!result) {
                    // send message to command src saying it could not be added
                    src.sendMessage(HuskyCrates.keyCommandMessages.getKeyDeliveryFail(p.getName(), amount));
                } else {
                    // otherwise, send message to player that they got a key
                    p.sendMessage(HuskyCrates.keyCommandMessages.getReceivedKey(keyName, amount));
                    deliveredTo++;
                }
            }
            // send mass delivered message
            src.sendMessage(HuskyCrates.keyCommandMessages.getMassKeyDeliverySuccess(deliveredTo, amount));

        } else if (player.isPresent()) {
            // Deliver keys to a player
            if (!src.hasPermission("huskycrates.key.others")) {
                src.sendMessage(Text.of(TextColors.RED, "You do not have permission to give others keys."));
                return CommandResult.success();
            }
            boolean result = offerKey(isVirtual, workingKey, amount, player.get());

            if (!result) {
                src.sendMessage(HuskyCrates.keyCommandMessages.getKeyDeliveryFail(player.get().getName(), amount));
            } else {
                player.get().sendMessage(HuskyCrates.keyCommandMessages.getReceivedKey(keyName, amount));
                src.sendMessage(HuskyCrates.keyCommandMessages.getKeyDeliverySuccess(player.get().getName(), amount));
            }

        } else if (src instanceof Player) {
            // Deliver keys to self
            if (!src.hasPermission("huskycrates.key.self")) {
                throw new CommandPermissionException(Text.of(TextColors.RED, "You do not have permission to give yourself keys."));
            }

            // offer key
            boolean result = offerKey(isVirtual, workingKey, amount, (Player) src);

            // if result failed
            if (!result) {
                src.sendMessage(HuskyCrates.keyCommandMessages.getSelfKeyDeliveryFail());
            } else {
                // if success
                src.sendMessage(HuskyCrates.keyCommandMessages.getSelfKeyDeliverySuccess(amount));
            }

        } else {
            // No valid subject...
            src.sendMessage(HuskyCrates.keyCommandMessages.getNoPlayersFound());

        }
        return CommandResult.success();
    }

    private boolean offerKey(boolean isVirtual, Key workingWith, int amount, Player psrc) {
        return !isVirtual ?
                Util.getHotbarFirst(psrc.getInventory()).offer(workingWith.getKeyItemStack(amount)).getType() == InventoryTransactionResult.Type.SUCCESS :
                HuskyCrates.registry.addVirtualKeys(psrc.getUniqueId(), workingWith.getId(), amount);
    }

    public static class Messages {
        private final String crateNoLocalKey;
        private final String crateKeyVirtual;
        private final String receivedKey;
        private final String keyDeliveryFail;
        private final String massKeyDeliverySuccess;
        private final String keyDeliverySuccess;
        private final String selfKeyDeliveryFail;
        private final String selfKeyDeliverySuccess;
        private final String noPlayersFound;

        public Messages(ConfigurationNode node) {
            this.crateNoLocalKey = node.getNode("crateNoLocalKey")
                    .getString("&cThe supplied crate did not have a local key.");
            this.crateKeyVirtual = node.getNode("crateKeyVirtual")
                    .getString("&cThe resolved key is virtual only. Please supply a key that can be a physical item, or use the \"v\" flag.");
            this.receivedKey = node.getNode("receivedKey")
                    .getString("&aYou received {amount} {key}{amount.plural}&r!");
            this.keyDeliveryFail = node.getNode("keyDeliveryFail")
                    .getString("&c{player} failed to receive their {amount} key{amount.plural}!");
            this.massKeyDeliverySuccess = node.getNode("massKeyDeliverySuccess")
                    .getString("&a{playerAmount} player{playerAmount.plural} received {amount} key{amount.plural}.");
            this.keyDeliverySuccess = node.getNode("keyDeliverySuccess")
                    .getString("&a{player} received {amount} key{amount.plural}.");
            this.selfKeyDeliveryFail = node.getNode("selfKeyDeliveryFail")
                    .getString("&cFailed to give you keys!");
            this.selfKeyDeliverySuccess = node.getNode("selfKeyDeliverySuccess")
                    .getString("&aYou were given {amount} key{amount.plural}.");
            this.noPlayersFound = node.getNode("noPlayersFound")
                    .getString("No valid players could be found to deliver keys to.");
        }

        public Text getCrateKeyVirtual() {
            return TextSerializers.FORMATTING_CODE.deserialize(crateKeyVirtual);
        }

        public Text getCrateNoLocalKey() {
            return TextSerializers.FORMATTING_CODE.deserialize(crateNoLocalKey);
        }

        public Text getKeyDeliveryFail(String playerName, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(keyDeliveryFail
                    .replace("{player}", playerName)
                    .replace("{amount}", amount.toString())
                    .replace("{amount.plural}", (amount != 1) ? "s" : ""));
        }

        public Text getKeyDeliverySuccess(String playerName, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(keyDeliverySuccess
                    .replace("{player}", playerName)
                    .replace("{amount}", amount.toString())
                    .replace("{amount.plural}", (amount != 1) ? "s" : ""));
        }

        public Text getMassKeyDeliverySuccess(Integer playerAmount, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(massKeyDeliverySuccess
                    .replace("{playerAmount}", playerAmount.toString())
                    .replace("{playerAmount.plural}", (playerAmount != 1) ? "s" : "")
                    .replace("{amount}", amount.toString())
                    .replace("{amount.plural}", (amount != 1) ? "s" : ""));
        }

        public Text getNoPlayersFound() {
            return TextSerializers.FORMATTING_CODE.deserialize(noPlayersFound);
        }

        public Text getReceivedKey(String keyName, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(receivedKey
                    .replace("{key}", keyName)
                    .replace("{amount}", amount.toString())
                    .replace("{amount.plural}", (amount != 1) ? "s" : ""));
        }

        public Text getSelfKeyDeliveryFail() {
            return TextSerializers.FORMATTING_CODE.deserialize(selfKeyDeliveryFail);
        }

        public Text getSelfKeyDeliverySuccess(Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(selfKeyDeliverySuccess
                    .replace("{amount}", amount.toString())
                    .replace("{amount.plural}", (amount != 1) ? "s" : ""));
        }
    }
}
