package io.github.thebusybiscuit.slimefun5.implementation.items.misc;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun5.implementation.items.electric.machines.FoodComposter;
import io.github.thebusybiscuit.slimefun5.implementation.items.electric.machines.accelerators.CropGrowthAccelerator;
import io.github.thebusybiscuit.slimefun5.implementation.items.electric.machines.accelerators.TreeGrowthAccelerator;
import io.github.thebusybiscuit.slimefun5.utils.SlimefunUtils;

/**
 * {@link OrganicFertilizer} is used to fuel a {@link CropGrowthAccelerator}
 * or {@link TreeGrowthAccelerator}. And can be crafted using a {@link FoodComposter}.
 * 
 * @author TheBusyBiscuit
 * 
 * @see CropGrowthAccelerator
 * @see TreeGrowthAccelerator
 *
 */
public class OrganicFertilizer extends SlimefunItem {

    public static final int OUTPUT = 2;

    @ParametersAreNonnullByDefault
    public OrganicFertilizer(ItemGroup itemGroup, SlimefunItemStack item, SlimefunItemStack ingredient) {
        super(itemGroup, item, RecipeType.FOOD_COMPOSTER, new ItemStack[] { ingredient.item(), null, null, null, null, null, null, null, null }, new SlimefunItemStack(item, OUTPUT).item());
    }

    public static boolean isOrganicFertilizer(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem instanceof OrganicFertilizer) {
            return true;
        }

        if (sfItem != null && (sfItem.getId().startsWith("FERTILIZER") || sfItem.getId().endsWith("_FERTILIZER"))) {
            return true;
        }

        return SlimefunUtils.isItemSimilar(item, SlimefunItems.FERTILIZER.item(), false, false);
    }

}

