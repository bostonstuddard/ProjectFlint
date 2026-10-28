package dev.projectflint.block.entity;

import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class SandingStationBlockEntity extends BlockEntity {
    public static final int DURABILITY_PER_PAPER = 32;
    public static final int MAX_DURABILITY = 256;

    private int sandingDurability;

    public SandingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SANDING_STATION, pos, state);
    }

    public int durability() {
        return sandingDurability;
    }

    public void setDurability(int durability) {
        sandingDurability = Math.max(0, Math.min(MAX_DURABILITY, durability));
        setChanged();
    }

    public boolean addPaper() {
        if (sandingDurability >= MAX_DURABILITY) return false;
        sandingDurability = Math.min(MAX_DURABILITY, sandingDurability + DURABILITY_PER_PAPER);
        setChanged();
        return true;
    }

    public boolean consumeDurability() {
        if (sandingDurability <= 0) return false;
        sandingDurability--;
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("SandingDurability", sandingDurability);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        sandingDurability = Math.max(0, Math.min(MAX_DURABILITY, input.getIntOr("SandingDurability", 0)));
    }
}
