package dev.projectflint.block.entity;

import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.LumberProcessorSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WaterPotBlockEntity extends BlockEntity {
    public static final int MAX_UNITS = 2; // two half-bucket fills

    private int waterUnits;
    private long linkedController = Long.MIN_VALUE;

    public WaterPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_POT, pos, state);
    }

    public int waterUnits() { return waterUnits; }
    public void setWaterUnits(int units) { waterUnits = Math.max(0, Math.min(MAX_UNITS, units)); setChanged(); }
    public boolean isFull() { return waterUnits >= MAX_UNITS; }
    public void addHalfBucket() { waterUnits = Math.min(MAX_UNITS, waterUnits + 1); setChanged(); }
    public boolean consumeFullBucket() {
        if (waterUnits < MAX_UNITS) return false;
        waterUnits -= MAX_UNITS;
        setChanged();
        return true;
    }

    public void link(BlockPos controller) { linkedController = controller.asLong(); setChanged(); }
    public void unlink() { linkedController = Long.MIN_VALUE; setChanged(); }
    public boolean isLinked() { return linkedController != Long.MIN_VALUE; }
    public BlockPos linkedController() { return isLinked() ? BlockPos.of(linkedController) : null; }

    public void serverTick(ServerLevel level) {
        if (!isLinked()) {
            tryCheckeredTileRitual(level);
            return;
        }
        BlockPos controller = linkedController();
        if (controller == null || !LumberProcessorSystem.isController(level, controller)) {
            LumberProcessorSystem.collapse(level, controller, false);
        }
    }

    /**
     * Water-pot floor-tile wash: 16 Limestone Bricks + White Dye + Black Dye.
     * The ritual is intentionally disabled while the pot belongs to a formed Lumber Processor.
     */
    private void tryCheckeredTileRitual(ServerLevel level) {
        if (!isFull()) return;
        AABB area = new AABB(worldPosition.getX(), worldPosition.getY() + 0.55D, worldPosition.getZ(),
            worldPosition.getX() + 1.0D, worldPosition.getY() + 2.0D, worldPosition.getZ() + 1.0D);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, entity -> entity.isAlive());
        if (count(items, ModItems.LIMESTONE_BRICKS) < 16 || count(items, Items.WHITE_DYE) < 1 || count(items, Items.BLACK_DYE) < 1) return;

        consume(items, ModItems.LIMESTONE_BRICKS, 16);
        consume(items, Items.WHITE_DYE, 1);
        consume(items, Items.BLACK_DYE, 1);
        consumeFullBucket();
        Block.popResource(level, worldPosition.above(), new ItemStack(ModItems.CHECKERED_TILE, 16));
    }

    private static int count(List<ItemEntity> entities, net.minecraft.world.level.ItemLike item) {
        int total = 0;
        for (ItemEntity entity : entities) if (entity.getItem().is(item.asItem())) total += entity.getItem().getCount();
        return total;
    }

    private static void consume(List<ItemEntity> entities, net.minecraft.world.level.ItemLike item, int count) {
        int remaining = count;
        for (ItemEntity entity : entities) {
            if (remaining <= 0) break;
            ItemStack stack = entity.getItem();
            if (!stack.is(item.asItem())) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
            if (stack.isEmpty()) entity.discard(); else entity.setItem(stack);
        }
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("WaterUnits", waterUnits);
        output.putLong("LinkedController", linkedController);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        waterUnits = Math.max(0, Math.min(MAX_UNITS, input.getIntOr("WaterUnits", 0)));
        linkedController = input.getLongOr("LinkedController", Long.MIN_VALUE);
    }
}
