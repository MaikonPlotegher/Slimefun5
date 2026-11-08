package io.github.thebusybiscuit.slimefun5.implementation.items.magical.runes;

import io.github.thebusybiscuit.slimefun5.utils.compatibility.EntityCompat;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCategory;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.attributes.Soulbound;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemDropHandler;
import io.github.thebusybiscuit.slimefun5.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun5.implementation.items.magical.RuneAnvil;
import io.github.thebusybiscuit.slimefun5.implementation.items.magical.SoulboundItem;
import io.github.thebusybiscuit.slimefun5.utils.SlimefunUtils;

/**
 * This {@link SlimefunItem} allows you to convert any {@link ItemStack} into a
 * {@link SoulboundItem} either via dropping the rune onto an item on the ground or in a {@link RuneAnvil}.
 *
 * @author Linox
 * @author Walshy
 * @author TheBusyBiscuit
 *
 * @see ItemDropHandler
 * @see RuneAnvil
 * @see Soulbound
 */
public class SoulboundRune extends SimpleSlimefunItem<ItemDropHandler> {

    private static final double RANGE = 1.5;

    @ParametersAreNonnullByDefault
    public SoulboundRune(ItemGroup itemGroup, SlimefunItemStack item, RecipeType type, ItemStack[] recipe) {
        super(itemGroup, item, type, recipe);
    }

    @Override
    public @Nonnull ItemDropHandler getItemHandler() {
        return (e, p, item) -> {
            if (isItem(item.getItemStack())) {
                if (!canUse(p, true)) {
                    return true;
                }

                Slimefun.runSync(() -> activate(p, item), 20L);
                return true;
            }
            return false;
        };
    }

    private void activate(@Nonnull Player p, @Nonnull Item rune) {
        if (!rune.isValid()) {
            return;
        }

        Location l = rune.getLocation();
        Collection<Entity> entities = EntityCompat.getNearbyEntities(l.getWorld(), l, RANGE, RANGE, RANGE, this::findCompatibleItem);
        Optional<Entity> optional = entities.stream().findFirst();

        if (optional.isPresent()) {
            Item item = (Item) optional.get();
            ItemStack itemStack = item.getItemStack();

            if (itemStack.getAmount() == 1) {
                l.getWorld().strikeLightningEffect(l);

                Slimefun.runSync(() -> {
                    if (rune.isValid() && item.isValid() && itemStack.getAmount() == 1) {
                        l.getWorld().createExplosion(l, 0);
                        SoundEffect.SOULBOUND_RUNE_RITUAL_SOUND.playAt(l, SoundCategory.PLAYERS);

                        item.remove();
                        if (rune.getItemStack().getAmount() > 1) {
                            ItemStack remaining = rune.getItemStack().clone();
                            remaining.setAmount(remaining.getAmount() - 1);
                            rune.setItemStack(remaining);
                        } else {
                            rune.remove();
                        }

                        SlimefunUtils.setSoulbound(itemStack, true);
                        l.getWorld().dropItemNaturally(l, itemStack);

                        Slimefun.getLocalization().sendMessage(p, "messages.soulbound-rune.success", true);
                    } else {
                        Slimefun.getLocalization().sendMessage(p, "messages.soulbound-rune.fail", true);
                    }
                }, 10L);
            } else {
                Slimefun.getLocalization().sendMessage(p, "messages.soulbound-rune.fail", true);
            }
        }
    }

    private boolean findCompatibleItem(@Nonnull Entity entity) {
        if (entity instanceof Item) {
            Item item = (Item) entity;
            return item.getPickupDelay() <= 0 && !SlimefunUtils.isSoulbound(item.getItemStack()) && !isItem(item.getItemStack());
        }

        return false;
    }
}
