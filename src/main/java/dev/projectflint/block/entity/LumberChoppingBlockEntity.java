package dev.projectflint.block.entity;

import dev.projectflint.block.LumberChoppingBlock;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.LumberProcessorSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class LumberChoppingBlockEntity extends BlockEntity {
    public static final int WASH_TICKS = 10 * 20;

    private int outputMode; // 0 = 2 boards, 1 = 1 beam
    private int washingTicks;
    private boolean inputArmed;
    private boolean outputReady;
    private boolean initialized;

    public LumberChoppingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUMBER_CHOPPING_BLOCK, pos, state);
    }

    public int outputMode() { return outputMode; }
    public boolean washing() { return washingTicks > 0; }
    public boolean inputArmed() { return inputArmed; }
    public boolean outputReady() { return outputReady; }

    public boolean cycleOutput() {
        // The output is intentionally selectable after a stripped log has been placed.
        // Only an active wash or an awaiting finished workpiece locks the choice.
        if (washing() || outputReady) return false;
        outputMode = outputMode == 0 ? 1 : 0;
        setChanged();
        return true;
    }

    public boolean armInput() {
        if (washing() || outputReady) return false;
        if (!inputArmed) {
            inputArmed = true;
            setChanged();
        }
        return true;
    }

    public void initializeFormed() { initialized = true; setChanged(); }

    public boolean startWash() {
        if (!inputArmed || washing() || outputReady) return false;
        inputArmed = false;
        washingTicks = WASH_TICKS;
        setChanged();
        return true;
    }

    public void markOutputTaken() { outputReady = false; setChanged(); }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!initialized) initialized = true;
        if (!LumberProcessorSystem.isValidProcessor(level, pos, state.getValue(LumberChoppingBlock.FACING))) {
            LumberProcessorSystem.collapse(level, pos, false);
            return;
        }

        BlockState above = level.getBlockState(pos.above());
        BlockPos middleTop = pos.relative(state.getValue(LumberChoppingBlock.FACING)).above();
        BlockPos endTop = pos.relative(state.getValue(LumberChoppingBlock.FACING), 2).above();
        if (!level.getBlockState(middleTop).isAir()) {
            LumberProcessorSystem.collapse(level, pos, false);
            return;
        }
        if (!outputReady && !level.getBlockState(endTop).isAir()) {
            LumberProcessorSystem.collapse(level, pos, false);
            return;
        }
        BlockState aboveEnd = level.getBlockState(endTop);
        if (outputReady && !aboveEnd.is(dev.projectflint.registry.ModBlocks.LUMBER_WORKPIECE)) {
            LumberProcessorSystem.collapse(level, pos, false);
            return;
        }
        boolean strippedLog = LumberProcessorSystem.isSupportedStrippedLog(above);
        if ((washing() || outputReady) && !above.isAir()) {
            LumberProcessorSystem.collapse(level, pos, false);
            return;
        }
        if (!washing() && !outputReady) {
            if (strippedLog) {
                if (!inputArmed) { inputArmed = true; setChanged(); }
            } else if (!above.isAir() || inputArmed) {
                LumberProcessorSystem.collapse(level, pos, false);
                return;
            }
        }

        if (washingTicks > 0) {
            if (!LumberProcessorSystem.hasFullWater(level, pos, state.getValue(LumberChoppingBlock.FACING))) {
                LumberProcessorSystem.collapse(level, pos, false);
                return;
            }
            washingTicks--;
            if (washingTicks == 0) {
                if (LumberProcessorSystem.completeWash(level, pos, state.getValue(LumberChoppingBlock.FACING), outputMode)) {
                    outputReady = true;
                    setChanged();
                } else {
                    LumberProcessorSystem.collapse(level, pos, false);
                }
            } else if (washingTicks % 20 == 0) setChanged();
        } else if (outputReady && !LumberProcessorSystem.hasWorkpiece(level, pos, state.getValue(LumberChoppingBlock.FACING))) {
            LumberProcessorSystem.collapse(level, pos, false);
        }
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("OutputMode", outputMode);
        output.putInt("WashingTicks", washingTicks);
        output.putBoolean("InputArmed", inputArmed);
        output.putBoolean("OutputReady", outputReady);
        output.putBoolean("Initialized", initialized);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        outputMode = Math.max(0, Math.min(1, input.getIntOr("OutputMode", 0)));
        washingTicks = Math.max(0, input.getIntOr("WashingTicks", 0));
        inputArmed = input.getBooleanOr("InputArmed", false);
        outputReady = input.getBooleanOr("OutputReady", false);
        initialized = input.getBooleanOr("Initialized", true);
    }
}
