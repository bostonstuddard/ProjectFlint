package dev.projectflint.block.entity;

import dev.projectflint.block.FlintTorchBlock;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Stores the loaded-flint flag and 40-minute burn timer for a wall Flint Torch. */
public final class FlintTorchBlockEntity extends BlockEntity {
    public static final int BURN_TICKS = 40 * 60 * 20;

    private boolean hasFlint;
    private int remainingTicks;

    public FlintTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLINT_TORCH, pos, state);
    }

    public boolean hasFlint() {
        return hasFlint;
    }

    public void loadFlint() {
        hasFlint = true;
        setChanged();
    }

    public void light() {
        if (!hasFlint) return;
        remainingTicks = BURN_TICKS;
        setChanged();
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        // The fixture must obey its construction rules for its entire lifetime, not
        // only during placement. This catches diagonal/canopy changes as well.
        if (!FlintTorchBlock.isValidFixture(level, pos, state)) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.65F, 0.9F);
            return;
        }

        if (!state.getValue(FlintTorchBlock.LIT)) return;
        if (remainingTicks > 0) remainingTicks--;
        if (remainingTicks > 0) {
            if (remainingTicks % 20 == 0) setChanged();
            return;
        }

        // One loaded Flint powers one complete burn. Burnout returns to the reusable
        // unloaded fixture instead of destroying it.
        hasFlint = false;
        remainingTicks = 0;
        level.setBlockAndUpdate(pos, state.setValue(FlintTorchBlock.LIT, false));
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
            5, 0.08D, 0.08D, 0.08D, 0.01D);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.0F);
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("HasFlint", hasFlint);
        output.putInt("RemainingTicks", remainingTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hasFlint = input.getBooleanOr("HasFlint", false);
        remainingTicks = input.getIntOr("RemainingTicks", 0);
    }
}
