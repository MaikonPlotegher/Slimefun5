package me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.machines.AbstractMachine;

/**
 * Legacy container class retained for backward compatibility with Slimefun 4 addons.
 * All core machine logic, status indicators, and optimizations are now provided by
 * {@link AbstractMachine}.
 *
 * @deprecated Use {@link AbstractMachine} instead for modern addons.
 */
@Deprecated
public abstract class AContainer extends AbstractMachine {

    public static final int[] BORDER = DEFAULT_BORDER;
    public static final int[] BORDER_IN = DEFAULT_BORDER_IN;
    public static final int[] BORDER_OUT = DEFAULT_BORDER_OUT;

    @ParametersAreNonnullByDefault
    protected AContainer(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @ParametersAreNonnullByDefault
    protected AContainer(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
    }

    @Override
    public final AContainer setCapacity(int capacity) {
        return super.setCapacity(capacity);
    }

    @Override
    public final AContainer setProcessingSpeed(int speed) {
        return super.setProcessingSpeed(speed);
    }

    @Override
    public final AContainer setEnergyConsumption(int energyConsumption) {
        return super.setEnergyConsumption(energyConsumption);
    }
}
