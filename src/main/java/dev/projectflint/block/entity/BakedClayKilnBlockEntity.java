package dev.projectflint.block.entity;

import dev.projectflint.block.BakedClayKilnBlock;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.system.PrimitiveProcessingRecipe;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BakedClayKilnBlockEntity extends AbstractPrimitiveProcessorBlockEntity {
	public BakedClayKilnBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlockEntities.BAKED_CLAY_KILN, pos, blockState);
	}

	@Override
	protected PrimitiveProcessor processor() {
		return PrimitiveProcessor.KILN;
	}

	@Override
	protected float processingSpeedMultiplier(BlockState state) {
		return state.is(ModBlocks.MORTAR_KILN) ? GameplayTuning.MORTAR_KILN_COOK_SPEED : 1.0F;
	}

	@Override
	protected boolean isActive(BlockState state) {
		return state.hasProperty(BakedClayKilnBlock.LIT) && state.getValue(BakedClayKilnBlock.LIT);
	}

	@Override
	protected void ejectFinishedOutput(Level level, BlockPos pos, BlockState state, ItemStack output) {
		Direction facing = state.hasProperty(BakedClayKilnBlock.FACING) ? state.getValue(BakedClayKilnBlock.FACING) : Direction.NORTH;
		double x = pos.getX() + 0.5D + facing.getStepX() * 0.85D;
		double y = pos.getY() + 0.58D;
		double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.85D;
		ItemEntity entity = new ItemEntity(level, x, y, z, output);
		entity.setDeltaMovement(facing.getStepX() * 0.08D, 0.04D, facing.getStepZ() * 0.08D);
		entity.setDefaultPickUpDelay();
		level.addFreshEntity(entity);
	}

	public static ItemStack cookResult(ItemStack stack) {
		return PrimitiveProcessingRegistry.find(PrimitiveProcessor.KILN, stack)
			.map(PrimitiveProcessingRecipe::resultCopy)
			.orElse(ItemStack.EMPTY);
	}
}
