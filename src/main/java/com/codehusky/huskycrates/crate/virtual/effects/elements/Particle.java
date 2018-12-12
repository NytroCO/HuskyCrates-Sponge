package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.exception.ConfigParseException;
import com.codehusky.huskycrates.exception.InjectionDataException;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.*;
import java.util.ArrayList;
import java.util.Optional;

public class Particle {
    private ParticleEffect particle;
    private String animationCode;
    private boolean animateColor = false;
    private CompiledScript compiled;
    private int amount;
    private int quantity;
    private Vector3d offset;
    private Vector3d position;
    private String preset;
    private String colorPreset;

    private final ArrayList<ParticleEffect> palette = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> art = new ArrayList<>();

    public Particle(@Nonnull ParticleEffect particle, int amount, int quantity, Vector3d offset, Vector3d position, String animationPreset, @Nullable String colorPreset, @Nullable Boolean animateColor, String animationCode) throws InjectionDataException {
        this.particle = particle;
        this.amount = amount;
        this.quantity = quantity;
        this.offset = offset;
        this.position = (position == null) ? new Vector3d(0.5, 0.5, 0.5) : position;
        this.preset = animationPreset;
        this.colorPreset = colorPreset;
        this.animateColor = animateColor;
        this.animationCode = animationCode;

        if (this.animationCode == null && this.preset == null) {
            throw new InjectionDataException("Must provide animation instructions for a Particle.");
        }

    }

    public Particle(ConfigurationNode node) {
        Optional<ParticleType> pPT = Sponge.getRegistry().getType(ParticleType.class, node.getNode("type").getString(""));
        if (!pPT.isPresent()) {
            throw new ConfigParseException("Invalid particle type!", node.getNode("type").getPath());
        }

        this.amount = node.getNode("amount").getInt(1);

        this.quantity = node.getNode("quantity").getInt(1);

        if (!node.getNode("offset").isVirtual() && node.getNode("offset").hasListChildren()) {
            this.offset = new Vector3d(node.getNode("offset", 0).getDouble(0), node.getNode("offset", 1).getDouble(0), node.getNode("offset", 2).getDouble(0));
        }

        if (!node.getNode("position").isVirtual() && node.getNode("position").hasListChildren()) {
            this.position = new Vector3d(node.getNode("position", 0).getDouble(0), node.getNode("position", 1).getDouble(0), node.getNode("position", 2).getDouble(0));
        } else {
            this.position = new Vector3d(0.5, 0.5, 0.5);
        }

        if (!node.getNode("palette").isVirtual()) {
            for (ConfigurationNode colorNode : node.getNode("palette").getChildrenList()) {
                ParticleEffect.Builder builder = ParticleEffect.builder();
                builder.type(pPT.get());
                builder.quantity(quantity);
                if (offset != null) {
                    builder.offset(offset);
                }
                Color clr = Color.ofRgb(colorNode.getNode(0).getInt(0), colorNode.getNode(1).getInt(0), colorNode.getNode(2).getInt(0));
                builder.option(ParticleOptions.COLOR, clr);
                this.palette.add(builder.build());
            }
            if (node.getNode("art").isVirtual()) {
                throw new ConfigParseException("Palette is defined but there is no art to go along with it.", node.getNode("art").getPath());
            } else {

                ArrayList potentialArt;
                try {
                    potentialArt = node.getNode("art").getValue(TypeToken.of(ArrayList.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ConfigParseException("Art is in invalid format.", node.getNode("art").getPath());
                }

                this.art = potentialArt;

            }
        } else {
            ParticleEffect.Builder builder = ParticleEffect.builder();
            builder.type(pPT.get());
            if (!node.getNode("color").isVirtual() && node.getNode("color").hasListChildren()) {
                builder.option(ParticleOptions.COLOR, Color.ofRgb(node.getNode("color").getNode(0).getInt(0), node.getNode("color").getNode(1).getInt(0), node.getNode("color").getNode(2).getInt(0)));
            }
            builder.quantity(quantity);
            if (offset != null) {
                builder.offset(offset);
            }
            this.particle = builder.build();
        }
        if (node.getNode("animationPreset").isVirtual()) {
            this.animationCode = node.getNode("animationCode").getString("x=Math.sin(time/5)*0.7;z=Math.cos(time/5)*0.7;");
            this.animateColor = node.getNode("animateColor").getBoolean(false);
        } else {
            preset = node.getNode("animationPreset").getString();
            colorPreset = node.getNode("colorAnimationPreset").getString();
            if (colorPreset != null) this.animateColor = true;
            //TODO: check if these are valid! if not, throw error!!!
            /*switch (node.getNode("animationPreset").getString("").toLowerCase()){
                case "orbit":
                    this.animationCode = "x=Math.sin(time/4)*0.7; y=Math.sin((time/4)) * 0.2 ; z=Math.cos(time/4)*0.7;";
                    break;
                case "counterorbit":
                    this.animationCode = "x=Math.cos(time/4)*0.7; y=Math.sin((time/4)+ 10) * 0.2 - 0.10; z=Math.sin(time/4)*0.7;";
                    break;
                default:
                    throw new ConfigParseException("Invalid animation preset specified!",node.getNode("animationPreset").getPath());
            }
            switch (node.getNode("colorAnimationPreset").getString("").toLowerCase()){
                case "rainbow":
                    this.animateColor = true;
                    this.animationCode += "; h=((time*5)%255) + 1; s=255; v=255;";
                    break;
                default:
                    this.animateColor=false;
                    break;
            }*/
        }


        if (preset != null) {
            try {
                this.compiled = ((Compilable) HuskyCrates.jsengine).compile("function HSVtoRGB(h,s,v){var r,g,b,i,f,p,q,t;if(arguments.length===1){s=h.s,v=h.v,h=h.h}i=Math.floor(h*6);f=h*6-i;p=v*(1-s);q=v*(1-f*s);t=v*(1-(1-f)*s);switch(i%6){case 0:r=v,g=t,b=p;break;case 1:r=q,g=v,b=p;break;case 2:r=p,g=v,b=t;break;case 3:r=p,g=q,b=v;break;case 4:r=t,g=p,b=v;break;case 5:r=v,g=p,b=q;break}return{r:Math.round(r*255),g:Math.round(g*255),b:Math.round(b*255)}} (function(time, num){var x = 0.0; var y = 0.0; var z = 0.0; var h; var s; var v; var r = 0; var g = 0; var b = 0; " + animationCode + "; if(h&&s&&v){var hsv = HSVtoRGB(h/255,s/255,v/255); r=hsv.r;g=hsv.g;b=hsv.b;} var result = {x:x,y:y,z:z,r:Math.round(r),g:Math.round(g),b:Math.round(b)}; return result;})(time, num);");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public Particle(ParticleEffect effect, Vector3d position) {
        this.particle = effect;
        this.amount = 1;
        this.position = position;
    }

    public void run(long tick, Location<World> location, Player player) {
        try {
            for (int i = 0; i < amount; i++) {
                Vector3d particlePos = location.getPosition().clone().add(position);
                ParticleEffect ourParticle = particle;
                if (animationCode != null || preset != null) {
                    double x = 0d;
                    double y = 0d;
                    double z = 0d;

                    int r = 0;
                    int g = 0;
                    int b = 0;
                    if (preset == null) {
                        SimpleScriptContext sc = new SimpleScriptContext();
                        sc.setBindings(HuskyCrates.jsengine.createBindings(), ScriptContext.GLOBAL_SCOPE);
                        Bindings bindings = sc.getBindings(ScriptContext.GLOBAL_SCOPE);
                        bindings.put("time", tick);
                        bindings.put("num", i + 1);
                        sc.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                        ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) compiled.eval(sc);

                        x = Double.parseDouble("" + scriptObjectMirror.get("x"));
                        y = Double.parseDouble("" + scriptObjectMirror.get("y"));
                        z = Double.parseDouble("" + scriptObjectMirror.get("z"));

                        r = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("r")).intValue()));
                        g = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("g")).intValue()));
                        b = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("b")).intValue()));
                    } else {
                        switch (preset.toLowerCase()) {
                            case "orbit":
                                //this.animationCode = "x=Math.sin(time/4)*0.7; y=Math.sin((time/4)) * 0.2 ; z=Math.cos(time/4)*0.7;";
                                x = Math.sin(tick / 4.0) * 0.7;
                                y = Math.sin(tick / 4.0) * 0.2;
                                z = Math.cos(tick / 4.0) * 0.7;
                                break;
                            case "counterorbit":
                                //this.animationCode = "x=Math.cos(time/4)*0.7; y=Math.sin((time/4)+ 10) * 0.2 - 0.10; z=Math.sin(time/4)*0.7;";
                                x = Math.cos(tick / 4.0) * 0.7;
                                y = Math.sin((tick / 4.0) + 10) * 0.2 - 0.10;
                                z = Math.sin(tick / 4.0) * 0.7;
                                break;
                        }
                        if (colorPreset != null) {
                            if ("rainbow".equals(colorPreset.toLowerCase())) {
                                this.animateColor = true;
                                //this.animationCode += "; h=((time*5)%255) + 1; s=255; v=255;";
                                java.awt.Color clr = java.awt.Color.getHSBColor((((tick * 5.0f) % 255.0f) + 1.0f) / 255f, 1.0f, 1.0f);
                                r = clr.getRed();
                                g = clr.getGreen();
                                b = clr.getBlue();
                            } else {
                                this.animateColor = false;
                            }
                        }
                    }


                    if (this.animateColor) {
                        ourParticle = ParticleEffect.builder()
                                .from(ourParticle)
                                .option(ParticleOptions.COLOR, Color.ofRgb(r, g, b))
                                .build();
                    }
                    particlePos = location.getPosition().clone().add(position).add(x, y, z);
                }
                if (player != null) {
                    player.spawnParticles(ourParticle, particlePos);
                } else {
                    location.getExtent().spawnParticles(ourParticle, particlePos);
                }
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
