package io.github.thebusybiscuit.slimefun5.api.items.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun5.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun5.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun5.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun5.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun5.utils.ChestMenuUtils;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;

/**
 * An {@link ItemGroup} that holds nested child item groups, supporting multi-level hierarchies
 * (Parent -> Child -> Grandchild -> Leaf SubItemGroup) in both Survival and Cheat guides.
 *
 * @author TheBusyBiscuit
 * @author MaikonPlotegher
 */
public class NestedItemGroup extends FlexItemGroup {

    private static final int GROUP_SIZE = 36;
    private final NestedItemGroup parentItemGroup;
    private final List<ItemGroup> subGroups = new ArrayList<>();

    @ParametersAreNonnullByDefault
    public NestedItemGroup(NamespacedKey key, ItemStack item) {
        this(key, null, item, 3);
    }

    @ParametersAreNonnullByDefault
    public NestedItemGroup(NamespacedKey key, ItemStack item, int tier) {
        this(key, null, item, tier);
    }

    @ParametersAreNonnullByDefault
    public NestedItemGroup(NamespacedKey key, @Nullable NestedItemGroup parent, ItemStack item) {
        this(key, parent, item, 3);
    }

    @ParametersAreNonnullByDefault
    public NestedItemGroup(NamespacedKey key, @Nullable NestedItemGroup parent, ItemStack item, int tier) {
        super(key, item, tier);

        this.parentItemGroup = parent;

        if (parent != null) {
            parent.addSubGroup(this);
        }
    }

    /**
     * Returns the parent {@link NestedItemGroup} if this group is nested within another, or null if top-level.
     *
     * @return The parent group or null
     */
    @Nullable
    public NestedItemGroup getParent() {
        return parentItemGroup;
    }

    /**
     * Adds an {@link ItemGroup} (such as another {@link NestedItemGroup} or a {@link SubItemGroup}) to this group.
     *
     * @param group
     *            The {@link ItemGroup} to add.
     */
    public void addSubGroup(@Nonnull ItemGroup group) {
        Validate.notNull(group, "The item group cannot be null!");

        if (!subGroups.contains(group)) {
            subGroups.add(group);
        }
    }

    /**
     * Overload for backward compatibility with existing addons.
     *
     * @param group
     *            The {@link SubItemGroup} to add.
     */
    public void addSubGroup(@Nonnull SubItemGroup group) {
        addSubGroup((ItemGroup) group);
    }

    /**
     * Removes the given {@link ItemGroup} from this {@link NestedItemGroup} (if present).
     *
     * @param group
     *            The {@link ItemGroup} to remove.
     */
    public void removeSubGroup(@Nonnull ItemGroup group) {
        Validate.notNull(group, "The item group cannot be null!");

        subGroups.remove(group);
    }

    /**
     * Overload for backward compatibility with existing addons.
     *
     * @param group
     *            The {@link SubItemGroup} to remove.
     */
    public void removeSubGroup(@Nonnull SubItemGroup group) {
        removeSubGroup((ItemGroup) group);
    }

    @Nonnull
    public List<ItemGroup> getSubGroups() {
        return Collections.unmodifiableList(subGroups);
    }

    /**
     * Checks whether this nested group has any visible sub-groups or items for the player.
     *
     * @param p
     *            The player
     * @return Whether this group should be displayed
     */
    public boolean isVisibleInNested(@Nonnull Player p) {
        for (ItemGroup child : subGroups) {
            if (child instanceof SubItemGroup) {
                if (((SubItemGroup) child).isVisibleInNested(p)) {
                    return true;
                }
            } else if (child instanceof NestedItemGroup) {
                if (((NestedItemGroup) child).isVisibleInNested(p)) {
                    return true;
                }
            } else if (child.isVisible(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        // If this group is nested inside a parent, it must not appear directly on the root main menu.
        if (parentItemGroup != null) {
            return false;
        }

        return isVisibleInNested(p);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        // A nested group with a single sub-group opens it directly, skipping the one-item menu.
        if (subGroups.size() == 1) {
            ItemGroup single = subGroups.get(0);
            if (single instanceof FlexItemGroup) {
                ((FlexItemGroup) single).open(p, profile, mode);
            } else {
                SlimefunGuide.openItemGroup(profile, single, mode, 1);
            }
            return;
        }

        openGuide(p, profile, mode, 1);
    }

    @ParametersAreNonnullByDefault
    private void openGuide(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        GuideHistory history = profile.getGuideHistory();
        history.add(this, page);

        ChestMenu menu = new ChestMenu(Slimefun.getLocalization().getMessage(p, "guide.title.main"));
        SurvivalSlimefunGuide guide = (SurvivalSlimefunGuide) Slimefun.getRegistry().getSlimefunGuide(mode);

        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(SoundEffect.GUIDE_BUTTON_CLICK_SOUND::playFor);
        guide.createHeader(p, profile, menu);

        menu.addItem(1, ChestMenuUtils.getBackButton(p, "", ChatColor.GRAY + Slimefun.getLocalization().getMessage(p, "guide.back.guide")));
        menu.addMenuClickHandler(1, (pl, s, is, action) -> {
            if (history.size() > 1) {
                history.goBack(guide);
            } else if (parentItemGroup != null) {
                parentItemGroup.open(pl, profile, mode);
            } else {
                SlimefunGuide.openMainMenu(profile, mode, history.getMainMenuPage());
            }
            return false;
        });

        int index = 9;
        int target = (GROUP_SIZE * (page - 1)) - 1;

        while (target < (subGroups.size() - 1) && index < GROUP_SIZE + 9) {
            target++;

            ItemGroup itemGroup = subGroups.get(target);
            if (itemGroup instanceof SubItemGroup) {
                if (!((SubItemGroup) itemGroup).isVisibleInNested(p)) {
                    continue;
                }
            } else if (itemGroup instanceof NestedItemGroup) {
                if (!((NestedItemGroup) itemGroup).isVisibleInNested(p)) {
                    continue;
                }
            } else if (!itemGroup.isVisible(p)) {
                continue;
            }

            menu.addItem(index, itemGroup.getItem(p));
            menu.addMenuClickHandler(index, (pl, slot, item, action) -> {
                if (itemGroup instanceof FlexItemGroup) {
                    ((FlexItemGroup) itemGroup).open(p, profile, mode);
                } else {
                    SlimefunGuide.openItemGroup(profile, itemGroup, mode, 1);
                }
                return false;
            });

            index++;
        }

        int pages = target == subGroups.size() - 1 ? page : (subGroups.size() - 1) / GROUP_SIZE + 1;

        menu.addItem(46, ChestMenuUtils.getPreviousButton(p, page, pages));
        menu.addMenuClickHandler(46, (pl, slot, item, action) -> {
            int next = page - 1;

            if (next != page && next > 0) {
                openGuide(p, profile, mode, next);
            }

            return false;
        });

        menu.addItem(52, ChestMenuUtils.getNextButton(p, page, pages));
        menu.addMenuClickHandler(52, (pl, slot, item, action) -> {
            int next = page + 1;

            if (next != page && next <= pages) {
                openGuide(p, profile, mode, next);
            }

            return false;
        });

        menu.open(p);
    }

    @Override
    public void register(@Nonnull SlimefunAddon addon) {
        super.register(addon);

        if (parentItemGroup != null && !parentItemGroup.isRegistered()) {
            parentItemGroup.register(addon);
        }
    }
}
