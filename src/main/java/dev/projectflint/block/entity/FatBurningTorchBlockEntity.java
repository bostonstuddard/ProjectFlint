package dev.projectflint.block.entity;

import dev.projectflint.block.FatBurningTorchBlock;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Burning time is saved in block-entity NBT and never serialized as a menu short. */
public final class FatBurningTorchBlockEntity extends BlockEntity {
	public static final int BURN_TIME = 30 * 60 * 20;
	private int burnRemaining;
	private int fatCharges = 1; // The fat used to craft the torch supplies its first charge.

	public FatBurningTorchBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.FAT_BURNING_TORCH, pos, state); }
	public boolean addFat() {
		if (fatCharges >= 8) return false;
		fatCharges++;
		setChanged();
		return true;
	}
	public boolean ignite() {
		if (burnRemaining > 0 || fatCharges <= 0) return false;
		fatCharges--;
		burnRemaining = BURN_TIME;
		setChanged();
		return true;
	}
	public void serverTick(ServerLevel world, BlockPos pos, BlockState state) {
		if (burnRemaining <= 0) return;
		// isRainingAt includes direct sky exposure. A roof immediately above protects the flame.
		if (world.isRainingAt(pos.above())) burnRemaining = 0;
		else burnRemaining--;
		if (burnRemaining == 0) world.setBlockAndUpdate(pos, state.setValue(FatBurningTorchBlock.LIT, false));
		if (burnRemaining % 20 == 0) setChanged();
	}
	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		burnRemaining = Math.max(0, input.getIntOr("BurnRemaining", 0));
		fatCharges = Math.max(0, input.getIntOr("FatCharges", input.getIntOr("CharcoalCharges", 1) > 0 ? 1 : 0));
	}
	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("BurnRemaining", burnRemaining);
		output.putInt("FatCharges", fatCharges);
	}
}
