package dev.projectflint.block;

import dev.projectflint.block.entity.AdvancedKilnBlockEntity;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Shared structure lookup/dismantling for the 2x2x2 Advanced Kiln. */
public final class AdvancedKilnMultiblock {
	private AdvancedKilnMultiblock() {}

	public static BlockPos controllerPos(BlockPos pos, BlockState state) {
		if (state.is(ModBlocks.ADVANCED_KILN_CONTROLLER)) return pos;
		if (state.is(ModBlocks.ADVANCED_KILN_CASING)
			&& state.hasProperty(AdvancedKilnCasingBlock.FACING)
			&& state.hasProperty(AdvancedKilnCasingBlock.PART)) {
			return state.getValue(AdvancedKilnCasingBlock.PART)
				.controllerFrom(pos, state.getValue(AdvancedKilnCasingBlock.FACING));
		}
		return pos;
	}

	public static void dismantle(Level level, BlockPos brokenPos, BlockState brokenState, Player player) {
		BlockPos controllerPos = controllerPos(brokenPos, brokenState);
		BlockState controllerState = level.getBlockState(controllerPos);

		// Compatibility fallback for structures created before casing-position state existed.
		if (!controllerState.is(ModBlocks.ADVANCED_KILN_CONTROLLER)) {
			controllerPos = findNearbyController(level, brokenPos);
			if (controllerPos == null) return;
			controllerState = level.getBlockState(controllerPos);
		}

		Direction facing = controllerState.getValue(AdvancedKilnControllerBlock.FACING);
		Direction across = facing.getClockWise();
		Direction back = facing.getOpposite();

		if (level.getBlockEntity(controllerPos) instanceof AdvancedKilnBlockEntity kiln) {
			Containers.dropContents(level, controllerPos, kiln);
		}

		for (int y = 0; y < 2; y++) {
			for (int a = 0; a < 2; a++) {
				for (int d = 0; d < 2; d++) {
					BlockPos at = controllerPos.above(y).relative(across, a).relative(back, d);
					BlockState atState = level.getBlockState(at);
					if (atState.is(ModBlocks.ADVANCED_KILN_CONTROLLER) || atState.is(ModBlocks.ADVANCED_KILN_CASING)) {
						level.removeBlock(at, false);
					}
				}
			}
		}

		if (!player.isCreative()) {
			Block.popResource(level, controllerPos, new ItemStack(ModItems.MORTAR_BRICKS, 8));
			Block.popResource(level, controllerPos, new ItemStack(ModItems.ADVANCED_KILN_GLASS));
		}
	}

	public static void setActive(Level level, BlockPos controllerPos, BlockState controllerState, boolean active) {
		if (!controllerState.is(ModBlocks.ADVANCED_KILN_CONTROLLER)) return;
		Direction facing = controllerState.getValue(AdvancedKilnControllerBlock.FACING);
		Direction across = facing.getClockWise();
		Direction back = facing.getOpposite();

		for (int y = 0; y < 2; y++) {
			for (int a = 0; a < 2; a++) {
				for (int d = 0; d < 2; d++) {
					if (y == 0 && a == 0 && d == 0) continue;
					BlockPos at = controllerPos.above(y).relative(across, a).relative(back, d);
					BlockState state = level.getBlockState(at);
					if (state.is(ModBlocks.ADVANCED_KILN_CASING) && state.hasProperty(AdvancedKilnCasingBlock.ACTIVE)
						&& state.getValue(AdvancedKilnCasingBlock.ACTIVE) != active) {
						level.setBlockAndUpdate(at, state.setValue(AdvancedKilnCasingBlock.ACTIVE, active));
					}
				}
			}
		}
	}

	private static BlockPos findNearbyController(Level level, BlockPos origin) {
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos candidate = origin.offset(x, y, z);
					if (level.getBlockState(candidate).is(ModBlocks.ADVANCED_KILN_CONTROLLER)) return candidate;
				}
			}
		}
		return null;
	}
}
