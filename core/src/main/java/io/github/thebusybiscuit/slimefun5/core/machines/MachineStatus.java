package io.github.thebusybiscuit.slimefun5.core.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import io.github.bakedlibs.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun5.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun5.utils.NumberUtils;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.MaterialCompat;

/**
 * Standard machine status representations for Slimefun and addon GUIs.
 * Provides unified, beautifully formatted status indicators (idle, energy shortage,
 * running/processing with dynamic progress bar and time left, output full, etc.).
 *
 * @author MaikonPlotegher
 */
public enum MachineStatus {

    /** Machine is idle / waiting for input ingredients or recipe match. */
    IDLE("&6Aguardando Ingredientes...", XMaterial.YELLOW_STAINED_GLASS_PANE),

    /** Machine is currently processing / running. */
    PROCESSING("&aProcessando...", XMaterial.LIME_STAINED_GLASS_PANE),

    /** Machine lacks sufficient energy to operate. */
    NOT_ENOUGH_ENERGY("&cEnergia Insuficiente!", XMaterial.RED_STAINED_GLASS_PANE),

    /** Output inventory or buffer is full. */
    OUTPUT_FULL("&cSaída Obstruída / Cheia!", XMaterial.ORANGE_STAINED_GLASS_PANE),

    /** Generator lacks fuel. */
    NO_FUEL("&cSem Combustível!", XMaterial.RED_STAINED_GLASS_PANE),

    /** Machine is missing a fluid or water requirement. */
    NO_WATER("&cSem Água / Fluido!", XMaterial.BLUE_STAINED_GLASS_PANE),

    /** Machine has been disabled or powered off. */
    DISABLED("&8Máquina Desativada", XMaterial.GRAY_STAINED_GLASS_PANE);

    private final String defaultTitle;
    private final XMaterial material;

    MachineStatus(@Nonnull String defaultTitle, @Nonnull XMaterial material) {
        this.defaultTitle = defaultTitle;
        this.material = material;
    }

    @Nonnull
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Nonnull
    public XMaterial getMaterial() {
        return material;
    }

    @Nonnull
    public ItemStack getItem() {
        return withLore();
    }

    @Nonnull
    public ItemStack withLore(@Nullable String... lore) {
        return withTitleAndLore(defaultTitle, lore);
    }

    @Nonnull
    public ItemStack withTitleAndLore(@Nonnull String title, @Nullable String... lore) {
        ItemStack stack = MaterialCompat.stack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColors.color(title));
            if (lore != null && lore.length > 0) {
                List<String> list = new ArrayList<>();
                for (String l : lore) {
                    list.add(ChatColors.color(l));
                }
                meta.setLore(list);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Creates an idle status item with custom lore lines.
     */
    @Nonnull
    public static ItemStack idle(@Nullable String... lore) {
        return IDLE.withLore(lore);
    }

    /**
     * Creates an output full status item with custom lore lines.
     */
    @Nonnull
    public static ItemStack outputFull(@Nullable String... lore) {
        return OUTPUT_FULL.withLore(lore);
    }

    /**
     * Creates an energy shortage status item displaying current and required charge.
     */
    @Nonnull
    public static ItemStack notEnoughEnergy(int currentEnergy, int requiredEnergy) {
        return NOT_ENOUGH_ENERGY.withTitleAndLore(
            "&cEnergia Insuficiente!",
            "",
            "&7Carga: &e" + currentEnergy + " &7/ &e" + requiredEnergy + " J",
            "&cConecte à rede de energia!"
        );
    }

    /**
     * Creates a processing status item with an animated progress bar and remaining time in seconds.
     *
     * @param remainingTicks Slimefun ticks remaining in the operation
     * @param totalTicks Total Slimefun ticks for the operation
     */
    @Nonnull
    public static ItemStack processing(int remainingTicks, int totalTicks) {
        return processing(null, remainingTicks, totalTicks);
    }

    /**
     * Creates a processing status item with an animated progress bar, custom indicator, and remaining time.
     *
     * @param indicator The ItemStack to use as icon (null for default lime stained glass)
     * @param remainingTicks Slimefun ticks remaining
     * @param totalTicks Total Slimefun ticks
     */
    @Nonnull
    public static ItemStack processing(@Nullable ItemStack indicator, int remainingTicks, int totalTicks) {
        int total = Math.max(1, totalTicks);
        int remaining = Math.max(0, Math.min(total, remainingTicks));

        ItemStack stack = indicator != null ? indicator.clone() : MaterialCompat.stack(XMaterial.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColors.color("&aProcessando..."));
            meta.setLore(Arrays.asList(
                ChestMenuUtils.getProgressBar(remaining, total),
                "",
                ChatColors.color("&7Tempo restante: &e" + NumberUtils.getTimeLeft(remaining / 2))
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Creates a processing status item based on a direct completion percentage (0.0% to 100.0%).
     * Perfect for addons that calculate progress as a percentage or fraction.
     */
    @Nonnull
    public static ItemStack processing(float percentage) {
        return processing(null, percentage);
    }

    /**
     * Creates a processing status item based on a direct completion percentage and custom indicator.
     */
    @Nonnull
    public static ItemStack processing(@Nullable ItemStack indicator, float percentage) {
        float pct = Math.max(0.0F, Math.min(100.0F, percentage));
        int totalUnits = 20;
        int filledUnits = Math.round((pct / 100.0F) * totalUnits);

        StringBuilder bar = new StringBuilder();
        bar.append(NumberUtils.getColorFromPercentage(pct));
        for (int i = 0; i < filledUnits; i++) {
            bar.append(':');
        }
        bar.append("&7");
        for (int i = filledUnits; i < totalUnits; i++) {
            bar.append(':');
        }
        bar.append(" - ").append(Math.round(pct * 10.0F) / 10.0F).append('%');

        ItemStack stack = indicator != null ? indicator.clone() : MaterialCompat.stack(XMaterial.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColors.color("&aProcessando..."));
            meta.setLore(Arrays.asList(
                ChatColors.color(bar.toString()),
                "",
                ChatColors.color("&7Progresso: &e" + Math.round(pct) + "%")
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
