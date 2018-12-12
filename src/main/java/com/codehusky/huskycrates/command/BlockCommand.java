package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Crate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.util.Optional;

public class BlockCommand implements CommandExecutor {
    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        Optional<Crate> crate = args.getOne(Text.of("crate"));
        Optional<BlockType> block = args.getOne(Text.of("block"));
        Optional<Player> otherPlayer = args.getOne(Text.of("player"));
        int damage = (int) args.getOne(Text.of("damage")).orElse(0);

        Player playerToGive = null;
        if (src instanceof Player)
            playerToGive = (Player) src;

        if (otherPlayer.isPresent() && !src.hasPermission("huskycrates.block.others"))
            throw new CommandPermissionException(Text.of(TextColors.RED, "You do not have permission to give others crate placement blocks."));


        if (otherPlayer.isPresent()) playerToGive = otherPlayer.get();

        if (playerToGive == null)
            throw new CommandException(HuskyCrates.blockCommandMessages.getNoPlayersFound());

        if (!crate.isPresent()) throw new CommandException(Text.of(TextColors.RED, "No crate exsits with that name."));


        ItemStack stack;
        if (block.isPresent()) {
            Optional<ItemType> itP = block.get().getItem();
            if (!itP.isPresent()) {
                src.sendMessage(HuskyCrates.blockCommandMessages.getItemOnlyFailure());
                return CommandResult.success();
            }
            stack = crate.get().getCratePlacementBlock(itP.get(), damage);
        } else {
            stack = crate.get().getCratePlacementBlock(damage);
        }
        Util.getHotbarFirst(playerToGive.getInventory()).offer(stack);
        return CommandResult.success();
    }

    public static class Messages {
        private final String noPlayersFound;
        private final String itemOnlyFailure;

        public Messages(ConfigurationNode node) {
            this.noPlayersFound = node.getNode("noPlayersFound").getString("&cNo valid players could be found to give a crate placement block to.");
            this.itemOnlyFailure = node.getNode("itemOnlyFailure").getString("&cThe block you supplied could not be converted to an item. Please try again with a different block.");
        }

        public Text getNoPlayersFound() {
            return TextSerializers.FORMATTING_CODE.deserialize(noPlayersFound);
        }

        public Text getItemOnlyFailure() {
            return TextSerializers.FORMATTING_CODE.deserialize(itemOnlyFailure);
        }
    }
}