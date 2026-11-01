package io.github.thebusybiscuit.slimefun5.core.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.bakedlibs.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.ItemState;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun5.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun5.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun5.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun5.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun5.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun5.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun5.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun5.utils.SlimefunUtils;
import io.github.thebusybiscuit.slimefun5.utils.itemstack.ItemStackWrapper;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;

/**
 * Modern, high-performance base class for electric crafting machines in Slimefun 5.
 * <p>
 * Features native integration with {@link MachineStatus}, viewer-guarded UI updates,
 * configurable progress/status slots, fast-path checks for idle states, and full
 * backward compatibility for {@link me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer}.
 *
 * @author MaikonPlotegher
 */
public abstract class AbstractMachine extends SlimefunItem implements InventoryBlock, EnergyNetComponent, MachineProcessHolder<CraftingOperation>, RecipeDisplayItem {

    public static final int[] DEFAULT_BORDER = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 31, 36, 37, 38, 39, 40, 41, 42, 43, 44 };
    public static final int[] DEFAULT_BORDER_IN = { 9, 10, 11, 12, 18, 21, 27, 28, 29, 30 };
    public static final int[] DEFAULT_BORDER_OUT = { 14, 15, 16, 17, 23, 26, 32, 33, 34, 35 };

    protected final List<MachineRecipe> recipes = new ArrayList<>();
    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this);

    private int energyConsumedPerTick = -1;
    private int energyCapacity = -1;
    private int processingSpeed = -1;

    @ParametersAreNonnullByDefault
    protected AbstractMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        processor.setProgressBar(getProgressBar());
        createHeaderlessPreset(this, getInventoryTitle(), this::constructMenu);

        addItemHandler(onBlockBreak());
    }

    @ParametersAreNonnullByDefault
    protected AbstractMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        this(itemGroup, item, recipeType, recipe);
        this.recipeOutput = recipeOutput;
    }

    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(Block b) {
                BlockMenu inv = BlockStorage.getInventory(b);

                if (inv != null) {
                    inv.dropItems(b.getLocation(), getInputSlots());
                    inv.dropItems(b.getLocation(), getOutputSlots());
                }

                processor.endOperation(b);
            }

        };
    }

    @Override
    public MachineProcessor<CraftingOperation> getMachineProcessor() {
        return processor;
    }

    /**
     * Slot used for displaying the machine status or progress bar.
     * Default is slot 22. Subclasses may override this to use a custom GUI layout.
     *
     * @return The status slot index
     */
    public int getStatusSlot() {
        return 22;
    }

    public int[] getBorderSlots() {
        return DEFAULT_BORDER;
    }

    public int[] getInputBorderSlots() {
        return DEFAULT_BORDER_IN;
    }

    public int[] getOutputBorderSlots() {
        return DEFAULT_BORDER_OUT;
    }

    protected void constructMenu(BlockMenuPreset preset) {
        for (int i : getBorderSlots()) {
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : getInputBorderSlots()) {
            preset.addItem(i, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : getOutputBorderSlots()) {
            preset.addItem(i, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(getStatusSlot(), MachineStatus.IDLE.getItem(), ChestMenuUtils.getEmptyClickHandler());

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, ChestMenuUtils.getDefaultOutputHandler());
        }
    }

    @Nonnull
    public String getInventoryTitle() {
        return getItemName();
    }

    public abstract ItemStack getProgressBar();

    @Override
    public int getCapacity() {
        return energyCapacity;
    }

    public int getEnergyConsumption() {
        return energyConsumedPerTick;
    }

    public int getSpeed() {
        return processingSpeed;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractMachine> T setCapacity(int capacity) {
        Validate.isTrue(capacity > 0, "The capacity must be greater than zero!");

        if (getState() == ItemState.UNREGISTERED) {
            this.energyCapacity = capacity;
            return (T) this;
        } else {
            throw new IllegalStateException("You cannot modify the capacity after the Item was registered.");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractMachine> T setProcessingSpeed(int speed) {
        Validate.isTrue(speed > 0, "The speed must be greater than zero!");

        this.processingSpeed = speed;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractMachine> T setEnergyConsumption(int energyConsumption) {
        Validate.isTrue(energyConsumption > 0, "The energy consumption must be greater than zero!");
        Validate.isTrue(energyCapacity > 0, "You must specify the capacity before you can set the consumption amount.");
        Validate.isTrue(energyConsumption <= energyCapacity, "The energy consumption cannot be higher than the capacity (" + energyCapacity + ')');

        this.energyConsumedPerTick = energyConsumption;
        return (T) this;
    }

    @Override
    public void register(@Nonnull SlimefunAddon addon) {
        this.addon = addon;

        if (getCapacity() <= 0) {
            warn("The capacity has not been configured correctly. The Item was disabled.");
            warn("Make sure to call '" + getClass().getSimpleName() + "#setCapacity(...)' before registering!");
        }

        if (getEnergyConsumption() <= 0) {
            warn("The energy consumption has not been configured correctly. The Item was disabled.");
            warn("Make sure to call '" + getClass().getSimpleName() + "#setEnergyConsumption(...)' before registering!");
        }

        if (getSpeed() <= 0) {
            warn("The processing speed has not been configured correctly. The Item was disabled.");
            warn("Make sure to call '" + getClass().getSimpleName() + "#setProcessingSpeed(...)' before registering!");
        }

        if (getCapacity() > 0 && getEnergyConsumption() > 0 && getSpeed() > 0) {
            super.register(addon);
        }

        registerDefaultRecipes();
    }

    @Nonnull
    public abstract String getMachineIdentifier();

    protected void registerDefaultRecipes() {
        // Subclasses override this to register default recipes
    }

    public List<MachineRecipe> getMachineRecipes() {
        return recipes;
    }

    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> displayRecipes = new ArrayList<>(recipes.size() * 2);

        for (MachineRecipe recipe : recipes) {
            if (recipe.getInput().length != 1) {
                continue;
            }

            displayRecipes.add(recipe.getInput()[0]);
            displayRecipes.add(recipe.getOutput()[0]);
        }

        return displayRecipes;
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 19, 20 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { 24, 25 };
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    public void registerRecipe(MachineRecipe recipe) {
        recipe.setTicks(recipe.getTicks() / getSpeed());
        recipes.add(recipe);
    }

    public void registerRecipe(int seconds, ItemStack[] input, ItemStack[] output) {
        registerRecipe(new MachineRecipe(seconds, input, output));
    }

    public void registerRecipe(int seconds, ItemStack input, ItemStack output) {
        registerRecipe(new MachineRecipe(seconds, new ItemStack[] { input }, new ItemStack[] { output }));
    }

    public void registerRecipe(int seconds, SlimefunItemStack input, SlimefunItemStack output) {
        registerRecipe(seconds, input.item(), output.item());
    }

    public void registerRecipe(int seconds, SlimefunItemStack[] input, SlimefunItemStack[] output) {
        ItemStack[] inputAsItemStack = Arrays.stream(input)
                .map(SlimefunItemStack::item)
                .toArray(ItemStack[]::new);

        ItemStack[] outputAsItemStack = Arrays.stream(output)
                .map(SlimefunItemStack::item)
                .toArray(ItemStack[]::new);

        registerRecipe(seconds, inputAsItemStack, outputAsItemStack);
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, Config data) {
                AbstractMachine.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    protected void tick(Block b) {
        BlockMenu inv = BlockStorage.getInventory(b);
        CraftingOperation currentOperation = processor.getOperation(b);
        int statusSlot = getStatusSlot();

        if (currentOperation != null) {
            if (takeCharge(b.getLocation())) {

                if (!currentOperation.isFinished()) {
                    processor.updateProgressBar(inv, statusSlot, currentOperation);
                    currentOperation.addProgress(1);
                } else {
                    ChestMenuUtils.updateStatus(inv, statusSlot, MachineStatus.IDLE.getItem());

                    for (ItemStack output : currentOperation.getResults()) {
                        inv.pushItem(output.clone(), getOutputSlots());
                    }

                    processor.endOperation(b);
                }
            } else {
                ChestMenuUtils.updateStatus(inv, statusSlot, ChestMenuUtils.getNotEnoughEnergyStatus(getCharge(b.getLocation()), getEnergyConsumption()));
            }
        } else {
            MachineRecipe next = findNextRecipe(inv);

            if (next != null) {
                currentOperation = new CraftingOperation(next);
                processor.startOperation(b, currentOperation);

                processor.updateProgressBar(inv, statusSlot, currentOperation);
            } else if (isInputEmpty(inv)) {
                ChestMenuUtils.updateStatus(inv, statusSlot, MachineStatus.IDLE.getItem());
            }
        }
    }

    protected boolean isInputEmpty(@Nonnull BlockMenu inv) {
        for (int slot : getInputSlots()) {
            if (inv.getItemInSlot(slot) != null) {
                return false;
            }
        }
        return true;
    }

    protected boolean takeCharge(@Nonnull Location l) {
        Validate.notNull(l, "Can't attempt to take charge from a null location!");

        if (isChargeable()) {
            int charge = getCharge(l);

            if (charge < getEnergyConsumption()) {
                return false;
            }

            setCharge(l, charge - getEnergyConsumption());
            return true;
        } else {
            return true;
        }
    }

    protected MachineRecipe findNextRecipe(BlockMenu inv) {
        Map<Integer, ItemStack> inventory = new HashMap<>();

        for (int slot : getInputSlots()) {
            ItemStack item = inv.getItemInSlot(slot);

            if (item != null) {
                inventory.put(slot, ItemStackWrapper.wrap(item));
            }
        }

        Map<Integer, Integer> found = new HashMap<>();

        for (MachineRecipe recipe : recipes) {
            for (ItemStack input : recipe.getInput()) {
                for (int slot : getInputSlots()) {
                    if (SlimefunUtils.isItemSimilar(inventory.get(slot), input, true)) {
                        found.put(slot, input.getAmount());
                        break;
                    }
                }
            }

            if (found.size() == recipe.getInput().length) {
                if (!InvUtils.fitAll(inv.toInventory(), recipe.getOutput(), getOutputSlots())) {
                    ChestMenuUtils.updateStatus(inv, getStatusSlot(), MachineStatus.OUTPUT_FULL.getItem());
                    return null;
                }

                for (Map.Entry<Integer, Integer> entry : found.entrySet()) {
                    inv.consumeItem(entry.getKey(), entry.getValue());
                }

                return recipe;
            } else {
                found.clear();
            }
        }

        return null;
    }
}
