package dev.projectflint.block.entity;

import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.LumberProcessorSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        if (!isLinked()) return;
        BlockPos controller = linkedController();
        if (controller == null || !LumberProcessorSystem.isController(level, controller)) {
            LumberProcessorSystem.collapse(level, controller, false);
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
