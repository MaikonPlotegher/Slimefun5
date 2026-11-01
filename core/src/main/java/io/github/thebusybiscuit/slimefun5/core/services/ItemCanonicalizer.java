package io.github.thebusybiscuit.slimefun5.core.services;

import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.slimefun5.api.items.LiveLore;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun5.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun5.core.services.localization.RenamedItems;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.PdcCompat;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.ReflectionCompat;

/**
 * Normalizes plain, stackable Slimefun items (resources, dusts, ingots) to the canonical server-side
 * representation (display name = id, lore = null) so that items from addons, menus, and client echoes
 * share identical ItemMeta and stack cleanly across all inventory actions, ground merges, and containers.
 * <p>
 * Strictly guarded to never touch distinct items, player renames, rechargeable items, tools,
 * backpacks, or items carrying dynamic live lore / addon storage data (e.g. Networks).
 */
public final class ItemCanonicalizer {

    private ItemCanonicalizer() {}

    /**
     * Canonicalizes {@code stack} if it is a plain, stackable Slimefun resource whose metadata differs
     * from the canonical server-side template.
     *
     * @param stack The item to check and potentially normalize
     * @return {@code true} if the stack was modified, {@code false} otherwise
     */
    public static boolean canonicalize(@Nullable ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) {
            return false;
        }

        // 1. Must be a stackable item (maxStackSize > 1).
        // This safely excludes weapons, tools, armor, backpacks, SlimeTinker parts, etc.
        if (stack.getMaxStackSize() <= 1) {
            return false;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }

        // 2. Must not be player-renamed with an anvil
        if (RenamedItems.isRenamed(meta)) {
            return false;
        }

        // 3. Must not have enchantments
        if (meta.hasEnchants()) {
            return false;
        }

        // 4. Must not have dynamic live lore (used by Networks, displays, etc.)
        if (!LiveLore.get(meta).isEmpty()) {
            return false;
        }

        // 5. Must be a registered Slimefun item
        SlimefunItem sfItem = SlimefunItem.getByItem(stack);
        if (sfItem == null) {
            return false;
        }

        // 6. Must NOT be DistinctiveItem or Rechargeable
        if (sfItem instanceof DistinctiveItem || sfItem instanceof Rechargeable) {
            return false;
        }

        // 7. Must not be internal GUI chrome (_UI_*)
        String id = sfItem.getId();
        if (id.startsWith("_")) {
            return false;
        }

        // 8. Must not carry custom addon PDC keys (e.g. Networks quantum storage keys)
        if (hasUnknownPdcKeys(meta)) {
            return false;
        }

        // 9. ONLY migrate if the item's current display name is the raw ID (legacy NMS format):
        String currentName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        if (!id.equals(currentName)) {
            return false;
        }

        ItemMeta templateMeta = sfItem.getItem().getItemMeta();
        if (templateMeta == null) {
            return false;
        }

        String targetName = templateMeta.hasDisplayName() ? templateMeta.getDisplayName() : null;
        java.util.List<String> targetLore = templateMeta.hasLore() ? templateMeta.getLore() : null;

        // Apply updated name and lore from template
        meta.setDisplayName(targetName);
        meta.setLore(targetLore);
        if (Slimefun.getRegistry() != null && Slimefun.getRegistry().getShownOriginalKey() != null) {
            PdcCompat.remove(meta, Slimefun.getRegistry().getShownOriginalKey());
        }
        stack.setItemMeta(meta);
        return true;
    }

    private static boolean hasUnknownPdcKeys(@Nonnull ItemMeta meta) {
        if (meta instanceof org.bukkit.persistence.PersistentDataHolder) {
            org.bukkit.persistence.PersistentDataContainer container = ((org.bukkit.persistence.PersistentDataHolder) meta).getPersistentDataContainer();
            for (org.bukkit.NamespacedKey key : container.getKeys()) {
                String str = key.toString();
                if (!str.equals("slimefun:slimefun_item") && !str.equals("slimefun:shown_original")) {
                    return true;
                }
            }
        }
        return false;
    }
}

