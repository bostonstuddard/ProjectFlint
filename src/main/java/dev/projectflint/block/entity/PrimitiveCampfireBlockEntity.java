package dev.projectflint.block.entity;

import dev.projectflint.block.PrimitiveCampfireBlock;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class PrimitiveCampfireBlockEntity extends AbstractPrimitiveProcessorBlockEntity {
	public PrimitiveCampfireBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlockEntities.PRIMITIVE_CAMPFIRE, pos, blockState);
	}

	@Override
	protected PrimitiveProcessor processor() {
		return PrimitiveProcessor.CAMPFIRE;
	}

	@Override
	protected float processingSpeedMultiplier(BlockState state) {
		return GameplayTuning.CAMPFIRE_COOK_SPEED;
	}

	@Override
	protected boolean isActive(BlockState state) {
		return state.hasProperty(PrimitiveCampfireBlock.LIT) && state.getValue(PrimitiveCampfireBlock.LIT);
	}

	@Override
	protected void ejectFinishedOutput(Level level, BlockPos pos, BlockState state, ItemStack output) {
		ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D, output);
		entity.setDefaultPickUpDelay();
		level.addFreshEntity(entity);
	}
}
