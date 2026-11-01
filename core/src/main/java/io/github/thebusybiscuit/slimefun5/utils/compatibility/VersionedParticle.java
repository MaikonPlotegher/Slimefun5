package io.github.thebusybiscuit.slimefun5.utils.compatibility;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Particle;

import io.github.thebusybiscuit.slimefun5.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;

/**
 * @implNote Reference for the 1.20.5 particle-constant renames handled below (CraftBukkit {@code FieldRename}):
 *           {@code https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/legacy/FieldRename.java?until=2a6207fe150b6165722fce94c83cc1f206620ab5&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2Fcraftbukkit%2Flegacy%2FFieldRename.java#281-318}
 */
public class VersionedParticle {
    
    public static final Particle DUST;
    public static final Particle SMOKE;
    public static final Particle HAPPY_VILLAGER;
    public static final Particle ENCHANTED_HIT;
    public static final Particle EXPLOSION;
    public static final Particle WITCH;
    public static final Particle FIREWORK;
    public static final Particle ENCHANT;

    static {
        DUST = Particle.DUST;
        SMOKE = Particle.SMOKE;
        HAPPY_VILLAGER = Particle.HAPPY_VILLAGER;
        ENCHANTED_HIT = Particle.ENCHANTED_HIT;
        EXPLOSION = Particle.EXPLOSION;
        WITCH = Particle.WITCH;
        FIREWORK = Particle.FIREWORK;
        ENCHANT = Particle.ENCHANT;
    }

    private static boolean classExists(@Nonnull String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    private static Particle getKey(@Nonnull String key) {
        try {
            Field field = Particle.class.getDeclaredField(key);
            return (Particle) field.get(null);
        } catch(Exception e) {
            return null;
        }
    }
}

