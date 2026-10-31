package io.github.thebusybiscuit.slimefun5.integrations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import io.github.bakedlibs.dough.protection.Interaction;
import io.github.bakedlibs.dough.protection.ProtectionModule;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;

/**
 * A {@link ProtectionModule} for GriefDefender.
 *
 * <p>
 * GriefDefender ships its own Slimefun hook, but that hook is compiled against the
 * {@code slimefun4} package and cannot work with this fork. Without it, Slimefun machines with a
 * virtual menu (which are not vanilla containers, so GriefDefender never sees them as such) were not
 * protected. This module answers Slimefun's permission queries from the claim and trust data of the
 * GriefDefender API.
 *
 * <p>
 * Everything is reached through reflection so the GriefDefender API is not needed at compile time. If the
 * API ever changes shape, the module logs once and stops restricting instead of breaking machines.
 */
public class GriefDefenderProtectionModule implements ProtectionModule {

    private static final String API = "com.griefdefender.api.";

    private final Plugin plugin;

    private Object core;
    private Method getClaimAt;
    private Method getUser;
    private Method isWilderness;
    private Method getOwnerUniqueId;
    private Method isUserTrusted;
    private Method canIgnoreClaim;

    private Object accessor;
    private Object container;
    private Object builder;
    private Object manager;

    private boolean warned;

    public GriefDefenderProtectionModule(@Nonnull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void load() {
        try {
            ClassLoader loader = plugin.getClass().getClassLoader();

            Class<?> griefDefender = Class.forName(API + "GriefDefender", true, loader);
            Class<?> coreClass = Class.forName(API + "Core", true, loader);
            Class<?> claimClass = Class.forName(API + "claim.Claim", true, loader);
            Class<?> trustType = Class.forName(API + "claim.TrustType", true, loader);
            Class<?> trustTypes = Class.forName(API + "claim.TrustTypes", true, loader);
            Class<?> userClass = Class.forName(API + "User", true, loader);

            core = griefDefender.getMethod("getCore").invoke(null);
            getClaimAt = coreClass.getMethod("getClaimAt", Object.class);
            getUser = coreClass.getMethod("getUser", UUID.class);
            isWilderness = claimClass.getMethod("isWilderness");
            getOwnerUniqueId = claimClass.getMethod("getOwnerUniqueId");
            isUserTrusted = claimClass.getMethod("isUserTrusted", UUID.class, trustType);
            canIgnoreClaim = userClass.getMethod("canIgnoreClaim", claimClass);

            accessor = trust(trustTypes, "ACCESSOR");
            container = trust(trustTypes, "CONTAINER");
            builder = trust(trustTypes, "BUILDER");
            manager = trust(trustTypes, "MANAGER");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            // Let the dough ProtectionManager log and skip this module
            throw new IllegalStateException("The GriefDefender API is not compatible: " + e, e);
        }
    }

    private static Object trust(Class<?> trustTypes, String name) throws ReflectiveOperationException {
        Field field = trustTypes.getField(name);
        return field.get(null);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean hasPermission(@Nullable OfflinePlayer player, @Nullable Location location, @Nonnull Interaction interaction) {
        if (player == null || location == null || location.getWorld() == null) {
            return true;
        }

        Object[] accepted = acceptedTrusts(interaction);

        if (accepted == null) {
            // PvP and entity damage are handled by GriefDefender's own listeners
            return true;
        }

        try {
            Object claim = getClaimAt.invoke(core, location);

            if (claim == null || (Boolean) isWilderness.invoke(claim)) {
                return true;
            }

            UUID uuid = player.getUniqueId();

            if (uuid.equals(getOwnerUniqueId.invoke(claim))) {
                return true;
            }

            Object user = getUser.invoke(core, uuid);

            if (user != null && (Boolean) canIgnoreClaim.invoke(user, claim)) {
                return true;
            }

            for (Object trust : accepted) {
                if ((Boolean) isUserTrusted.invoke(claim, uuid, trust)) {
                    return true;
                }
            }

            return false;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            if (!warned) {
                warned = true;
                Slimefun.logger().log(Level.WARNING, e, () -> "GriefDefender claim check failed, Slimefun will not restrict machines in claims until this is fixed.");
            }

            return true;
        }
    }

    /**
     * The trust levels that allow {@code interaction}. A higher trust always includes the lower ones, so a
     * builder can also open a machine. {@code null} means the interaction is left to GriefDefender itself.
     */
    private @Nullable Object[] acceptedTrusts(@Nonnull Interaction interaction) {
        switch (interaction) {
            case INTERACT_BLOCK:
                return new Object[] { container, builder, manager };
            case BREAK_BLOCK:
            case PLACE_BLOCK:
                return new Object[] { builder, manager };
            case INTERACT_ENTITY:
                return new Object[] { accessor, container, builder, manager };
            default:
                return null;
        }
    }

}

