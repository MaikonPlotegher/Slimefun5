package io.github.thebusybiscuit.slimefun5.implementation.items.magical.staves;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun5.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.implementation.items.SimpleSlimefunItem;

/**
 * The {@link WindStaff} is a powerful staff which launches the {@link Player} forward when clicked.
 * 
 * @author TheBusyBiscuit
 *
 */
public class WindStaff extends SimpleSlimefunItem<ItemUseHandler> implements Listener {

    private final ItemSetting<Integer> multiplier = new IntRangeSetting(this, "power", 1, 4, Integer.MAX_VALUE);

    @ParametersAreNonnullByDefault
    public WindStaff(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemSetting(multiplier);
    }

    @Override
    public void postRegister() {
        Bukkit.getPluginManager().registerEvents(this, Slimefun.instance());
    }

    @Override
    public @Nonnull ItemUseHandler getItemHandler() {
        return e -> use(e.getPlayer(), e.getItem());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (item != null && isItem(item)) {
                if (canUse(e.getPlayer(), true)) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        e.setCancelled(true);
                    }
                    use(e.getPlayer(), item);
                }
            }
        }
    }

    public void use(@Nonnull Player p, @Nonnull ItemStack item) {
        if (p.getFoodLevel() >= 2) {
            // The isItem() check is here to prevent the MultiTool from consuming hunger
            if (isItem(item) && p.getGameMode() != GameMode.CREATIVE) {
                FoodLevelChangeEvent event = new FoodLevelChangeEvent(p, p.getFoodLevel() - 2);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    p.setFoodLevel(event.getFoodLevel());
                }
            }

            p.setVelocity(p.getEyeLocation().getDirection().multiply(multiplier.getValue()));
            SoundEffect.WIND_STAFF_USE_SOUND.playFor(p);
            p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 1);
            p.setFallDistance(0F);
        } else {
            Slimefun.getLocalization().sendMessage(p, "messages.hungry", true);
        }
    }
}
