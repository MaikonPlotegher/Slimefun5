package io.github.thebusybiscuit.slimefun5.implementation.resources;

import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

import io.github.thebusybiscuit.slimefun5.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun5.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun5.utils.biomes.BiomeMap;

/**
 * A {@link GEOResource} which consists of small chunks of Uranium.
 * 
 * @author TheBusyBiscuit
 *
 */
class UraniumResource extends AbstractResource {

    private static final int DEFAULT_OVERWORLD_VALUE = 4;

    private final BiomeMap<Integer> biomes;

    UraniumResource() {
        super("uranium", "Small Chunks of Uranium", SlimefunItems.SMALL_URANIUM.item(), 2, true);
        biomes = getBiomeMap(this, "/biome-maps/uranium_v1.18.json");
    }

    @Override
    public int getDefaultSupply(Environment environment, Biome biome) {
        if (environment != Environment.NORMAL) {
            return 0;
        } else {
            return biomes.getOrDefault(biome, DEFAULT_OVERWORLD_VALUE);
        }
    }

}

