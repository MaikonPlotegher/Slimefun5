package io.github.thebusybiscuit.slimefun5.implementation.items.electric.machines.accelerators;

import io.github.thebusybiscuit.slimefun5.utils.compatibility.EntityCompat;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.ParticleCompat;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.implementation.items.misc.OrganicFood;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedParticle;

import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

public class AnimalGrowthAccelerator extends AbstractGrowthAccelerator {

    private static final int ENERGY_CONSUMPTION = 14;
    private static final double RADIUS = 3.0;

    public AnimalGrowthAccelerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int getCapacity() {
        return 1024;
    }

    @Override
    protected void tick(Block b) {
        BlockMenu inv = BlockStorage.getInventory(b);
        if (inv == null) {
            return;
        }

        if (getCharge(b.getLocation()) < ENERGY_CONSUMPTION) {
            return;
        }

        Location center = b.getLocation().add(0.5, 0.5, 0.5);
        for (Entity n : EntityCompat.getNearbyEntities(b.getWorld(), center, RADIUS, RADIUS, RADIUS, this::isReadyToGrow)) {
            for (int slot : getInputSlots()) {
                if (OrganicFood.isOrganicFood(inv.getItemInSlot(slot))) {
                    Ageable ageable = (Ageable) n;
                    removeCharge(b.getLocation(), ENERGY_CONSUMPTION);
                    inv.consumeItem(slot);
                    ageable.setAge(ageable.getAge() + 2000);

                    if (ageable.getAge() > 0) {
                        ageable.setAge(0);
                    }

                    ParticleCompat.spawn(n.getWorld(), VersionedParticle.HAPPY_VILLAGER, ((LivingEntity) n).getEyeLocation(), 8, 0.2F, 0.2F, 0.2F);
                    return;
                }
            }
        }
    }

    private boolean isReadyToGrow(Entity n) {
        return n instanceof Ageable && n.isValid() && !((Ageable) n).isAdult() && !((Ageable) n).getAgeLock();
    }

}

