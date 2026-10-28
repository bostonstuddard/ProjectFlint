package dev.projectflint.system;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * The controller occupies the front-centre of Y1. All coordinates below are
 * relative to the centre of the 3x3 floor; the shell is never claimed or
 * rewritten, so neighbouring forges may share ordinary fireclay wall cells.
 *
 * Y0: solid 3x3 fireclay brick blocks
 * Y1: hollow 3x3 brick ring, with the front-centre controller
 * Y2: corner stairs, cardinal brick blocks, empty centre
 * Y3/Y4: cardinal brick blocks only; only the centre chimney cell must be air
 * Y5: four single fireclay slabs at the cardinal positions; only the centre must be air
 * Exterior cells not occupied by required structure blocks are ignored, allowing nearby blocks/wall sharing.
 */
public final class FireclayForgeMultiblock {
	private FireclayForgeMultiblock() {}

	public static BlockPos centreBase(BlockPos controller, Direction facing) {
		return controller.below().relative(facing.getOpposite());
	}

	public static BlockPos chimneyTop(BlockPos controller, Direction facing) {
		return centreBase(controller, facing).above(6);
	}

	private static boolean innerAir(Level level, BlockPos pos) {
		return level.getBlockState(pos).isAir();
	}

	public static boolean validate(Level level, BlockPos controller, Direction facing) {
		if (!facing.getAxis().isHorizontal()) return false;
		BlockPos centre = centreBase(controller, facing);
		Direction side = facing.getClockWise();
		Direction back = facing.getOpposite();
		if (!level.getBlockState(controller).is(ModBlocks.FIRECLAY_FORGE_CONTROLLER)) return false;
		if (level.getBlockState(controller).getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING) != facing) return false;

		for (int y = 0; y <= 5; y++) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos cell = centre.relative(side, x).relative(back, z).above(y);
					if (y == 1 && x == 0 && z == -1) {
						if (!cell.equals(controller)) return false;
						continue;
					}

					boolean centreCell = x == 0 && z == 0;
					boolean corner = x != 0 && z != 0;
					boolean cardinal = (x == 0) != (z == 0);
					BlockState state = level.getBlockState(cell);

					if (y == 0 || (y == 1 && (corner || cardinal)) || (y >= 2 && y <= 4 && cardinal)) {
						if (!state.is(ModBlocks.FIRECLAY_BRICK_BLOCK)) return false;
					} else if (y == 2 && corner) {
						if (!state.is(ModBlocks.FIRECLAY_BRICK_STAIRS)) return false;
					} else if (y == 5 && cardinal) {
						if (!state.is(ModBlocks.FIRECLAY_BRICK_SLAB) || state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) return false;
					} else if (centreCell && y >= 1 && !innerAir(level, cell)) {
						// Only the actual inner chimney/funnel must stay open. Exterior gaps are ignored.
						return false;
					}
				}
			}
		}
		return innerAir(level, centre.above(6));
	}
}
