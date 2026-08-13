package dev.projectflint.block.entity;

import dev.projectflint.item.CrudePlantTorchItem;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CrudePlantTorchBlockEntity extends BlockEntity {
	private int remainingTicks = CrudePlantTorchItem.LIFETIME_TICKS;

	public CrudePlantTorchBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CRUDE_PLANT_TORCH, pos, state);
	}

	public void initializeFromStack(ItemStack stack) {
		remainingTicks = Math.max(1, stack.getMaxDamage() - stack.getDamageValue());
		setChanged();
	}

	public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		if (level.isRainingAt(pos)
			|| !level.getFluidState(pos).isEmpty()
			|| !level.canSeeSky(pos.above())) {
			extinguish(level, pos);
			return;
		}

		remainingTicks--;
		if (remainingTicks <= 0) {
			extinguish(level, pos);
			return;
		}

		if (remainingTicks % 20 == 0) {
			igniteAdjacentFlammables(level, pos);
			setChanged();
		}
	}

	private static void extinguish(ServerLevel level, BlockPos pos) {
		level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D, 4, 0.12D, 0.12D, 0.12D, 0.01D);
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.1F);
		level.removeBlock(pos, false);
	}

	private static void igniteAdjacentFlammables(ServerLevel level, BlockPos torchPos) {
		for (Direction direction : Direction.values()) {
			BlockPos fuelPos = torchPos.relative(direction);
			BlockState fuel = level.getBlockState(fuelPos);
			if (!isCrudeTorchFlammable(fuel)) continue;

			for (Direction fireDirection : Direction.values()) {
				BlockPos firePos = fuelPos.relative(fireDirection);
				if (firePos.equals(torchPos) || !level.getBlockState(firePos).isAir()) continue;
				BlockState fire = BaseFireBlock.getState(level, firePos);
				if (fire.canSurvive(level, firePos)) {
					level.setBlockAndUpdate(firePos, fire);
					return;
				}
			}
		}
	}

	private static boolean isCrudeTorchFlammable(BlockState state) {
		return state.is(BlockTags.LOGS_THAT_BURN)
			|| state.is(BlockTags.LEAVES)
			|| state.is(BlockTags.SAPLINGS)
			|| state.is(BlockTags.FLOWERS)
			|| state.is(BlockTags.WOOL)
			|| state.is(ModTags.FORAGE_BUSHES)
			|| state.is(Blocks.SWEET_BERRY_BUSH)
			|| state.is(Blocks.SHORT_GRASS)
			|| state.is(Blocks.TALL_GRASS)
			|| state.is(Blocks.FERN)
			|| state.is(Blocks.LARGE_FERN)
			|| state.is(Blocks.DEAD_BUSH)
			|| state.is(Blocks.BAMBOO)
			|| state.is(Blocks.BAMBOO_SAPLING);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("RemainingTicks", remainingTicks);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		remainingTicks = input.getIntOr("RemainingTicks", CrudePlantTorchItem.LIFETIME_TICKS);
	}
}
