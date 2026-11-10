package io.github.thebusybiscuit.slimefun5.core.services.localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.ReflectionCompat;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedItemFlag;

/**
 * Renders an item's enchantments as lore lines placed with the composed blocks (under the Type category)
 * instead of the vanilla client's default position above the lore. To do that the vanilla enchantment
 * tooltip must be hidden ({@link VersionedItemFlag#HIDE_ENCHANTS}); on Minecraft versions where that flag
 * no longer exists (1.21.5+, where hiding moved to a data component the universal jar does not touch), we
 * canNOT reliably hide it, so we DON'T re-render either - leaving vanilla's default rendering untouched
 * rather than risk showing the enchantments twice.
 */
public final class EnchantDisplay {

    private EnchantDisplay() {}

    /** Whether we can hide the vanilla enchant tooltip on this version (and thus safely re-render it). */
    public static boolean canReposition() {
        return VersionedItemFlag.HIDE_ENCHANTS != null;
    }

    /** Hides the vanilla enchantment tooltip so the re-rendered lore lines are the only ones shown. No-op when unsupported. */
    public static void hide(@Nonnull ItemMeta meta) {
        if (canReposition()) {
            VersionedItemFlag.addFlags(meta, VersionedItemFlag.HIDE_ENCHANTS);
        }
    }

    /** Reveals the vanilla enchantment tooltip when enchantments are removed or repositioning is no longer needed. */
    public static void reveal(@Nonnull ItemMeta meta) {
        if (canReposition()) {
            VersionedItemFlag.removeFlags(meta, VersionedItemFlag.HIDE_ENCHANTS);
        }
    }

    /**
     * Removes any lore lines representing the given enchantment from the item's lore.
     */
    public static void removeEnchantmentLore(@Nonnull ItemMeta meta, @Nonnull Enchantment enchantment, int level) {
        if (!meta.hasLore()) {
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }

        Set<String> targets = new HashSet<>();
        try {
            if (Slimefun.getLocalization() != null && Slimefun.getLocalization().getLanguages() != null) {
                for (Language lang : Slimefun.getLocalization().getLanguages()) {
                    targets.add(ChatColor.stripColor(prettyName(enchantment, lang.getId()) + " " + roman(level)).trim().toLowerCase(Locale.ROOT));
                }
            }
        } catch (Exception | LinkageError ignored) {
            // fall back to default languages below
        }

        targets.add(ChatColor.stripColor(prettyName(enchantment, "pt-BR") + " " + roman(level)).trim().toLowerCase(Locale.ROOT));
        targets.add(ChatColor.stripColor(prettyName(enchantment, "pt") + " " + roman(level)).trim().toLowerCase(Locale.ROOT));
        targets.add(ChatColor.stripColor(prettyName(enchantment, "en") + " " + roman(level)).trim().toLowerCase(Locale.ROOT));
        targets.add(ChatColor.stripColor(prettyName(enchantment, "es") + " " + roman(level)).trim().toLowerCase(Locale.ROOT));

        List<String> newLore = new ArrayList<>(lore.size());
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line).trim().toLowerCase(Locale.ROOT);
            if (!targets.contains(stripped)) {
                newLore.add(line);
            }
        }

        newLore = LoreComposer.normalizeBlankLines(newLore);
        meta.setLore(newLore.isEmpty() ? null : newLore);
    }

    /**
     * Grey enchantment lines (e.g. {@code Knockback II}) for the given enchantments, to sit
     * under the Type block. Empty when enchantments is null/empty or repositioning is unsupported.
     */
    @Nonnull
    public static List<String> linesForEnchantments(@Nullable Map<Enchantment, Integer> enchantments, @Nullable String languageId) {
        if (!canReposition() || enchantments == null || enchantments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map.Entry<Enchantment, Integer>> entries = new ArrayList<>(enchantments.entrySet());
        entries.sort((a, b) -> {
            String nameA = prettyName(a.getKey(), languageId);
            String nameB = prettyName(b.getKey(), languageId);
            int cmp = nameA.compareTo(nameB);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(a.getValue(), b.getValue());
        });

        List<String> out = new ArrayList<>(entries.size());

        for (Map.Entry<Enchantment, Integer> entry : entries) {
            out.add(ChatColor.GRAY + prettyName(entry.getKey(), languageId) + " " + roman(entry.getValue()));
        }

        return out;
    }

    /**
     * Grey enchantment lines (e.g. {@code Knockback II}) for the item's template enchantments, to sit
     * under the Type block. Empty when the item has no enchantments or the vanilla tooltip cannot be
     * hidden on this version (so we never double-render).
     */
    @Nonnull
    public static List<String> lines(@Nonnull SlimefunItem item, @Nullable String languageId) {
        if (!canReposition()) {
            return Collections.emptyList();
        }

        // A glow-only enchant (author set HIDE_ENCHANTS purely for the glint) is not a real, meaningful
        // enchantment - so we neither render it as a lore line nor reveal it; only the glint remains.
        if (Slimefun.getItemTranslationService().wasAuthorEnchantHidden(item.getId())) {
            return Collections.emptyList();
        }

        Map<Enchantment, Integer> enchantments;

        try {
            enchantments = item.getItem().getEnchantments();
        } catch (Exception | LinkageError e) {
            return Collections.emptyList();
        }

        return linesForEnchantments(enchantments, languageId);
    }

    /** A human-readable enchantment name: the data-driven translation, else version-safe title-casing of the raw key. */
    @Nonnull
    private static String prettyName(@Nonnull Enchantment enchantment, @Nullable String languageId) {
        String raw = null;

        try {
            Object key = ReflectionCompat.invoke(enchantment, "getKey"); // NamespacedKey on 1.13+
            if (key != null) {
                Object keyName = ReflectionCompat.invoke(key, "getKey");
                if (keyName != null) {
                    raw = String.valueOf(keyName);
                }
            }
        } catch (Exception | LinkageError ignored) {
            // fall through to the legacy name
        }

        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            try {
                raw = enchantment.getName(); // deprecated legacy name (1.8+)
            } catch (Exception | LinkageError e) {
                raw = "enchantment";
            }
        }

        String resolved = Slimefun.getEnchantTranslationService().name(languageId, raw);
        return resolved != null ? resolved : titleCase(raw);
    }

    @Nonnull
    private static String titleCase(@Nonnull String raw) {
        String[] parts = raw.toLowerCase(java.util.Locale.ROOT).split("[_\\s]+");
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(' ');
            }

            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        return sb.length() > 0 ? sb.toString() : raw;
    }

    /** Roman numeral for enchantment levels 1..10; falls back to the plain number beyond that. */
    @Nonnull
    private static String roman(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(level);
        }
    }
}
