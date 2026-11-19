package io.github.thebusybiscuit.slimefun5.api.events;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a {@link Player} is about to gain radiation exposure from radioactive items in their inventory.
 * If this event is cancelled, exposure is set to 0, or max exposure is set to 0,
 * radiation will not be added to the player.
 * You can also set {@link #setMaxExposure(int)} to cap radiation gain (e.g. 75 for 25% protection).
 * Note that this does NOT grant immunity to existing radiation symptoms or damage if the player already has exposure.
 *
 * @author MaikonPlotegher
 */
public class PlayerRadiationExposureEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ItemStack[] armor;
    private final int currentExposure;
    private int exposure;
    private int maxExposure = 100;
    private boolean cancelled;

    public PlayerRadiationExposureEvent(@Nonnull Player who, int exposure, @Nonnull ItemStack[] armor) {
        this(who, exposure, 0, armor);
    }

    public PlayerRadiationExposureEvent(@Nonnull Player who, int exposure, int currentExposure, @Nonnull ItemStack[] armor) {
        super(true);
        this.player = who;
        this.exposure = exposure;
        this.currentExposure = currentExposure;
        this.armor = armor;
    }

    @Nonnull
    public Player getPlayer() {
        return player;
    }

    public int getExposure() {
        return exposure;
    }

    public void setExposure(int exposure) {
        this.exposure = Math.max(0, exposure);
    }

    public int getCurrentExposure() {
        return currentExposure;
    }

    public int getMaxExposure() {
        return maxExposure;
    }

    public void setMaxExposure(int maxExposure) {
        this.maxExposure = Math.max(0, Math.min(100, maxExposure));
    }

    @Nonnull
    public ItemStack[] getArmor() {
        return armor;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @Nonnull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @Nonnull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
