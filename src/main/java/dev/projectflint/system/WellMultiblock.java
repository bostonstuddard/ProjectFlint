package dev.projectflint.system;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Structural validator for the 3x3 Project Flint aquifer Well. */
public final class WellMultiblock {
	private WellMultiblock() {}

	public static Optional<AquiferSystem.AquiferHit> validate(ServerLevel level, BlockPos controllerPos) {
		int baseY = controllerPos.getY() - 6;
		BlockPos center = new BlockPos(controllerPos.getX(), baseY, controllerPos.getZ());

		// Solid 3x3 brick floor.
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (!level.getBlockState(center.offset(dx, 0, dz)).is(Blocks.BRICKS)) return Optional.empty();
			}
		}

		// Three-block-deep hollow brick shaft. Only the actual brick shell is structural.
		// The center column may contain water or replaceable world clutter such as grass.
		for (int dy = 1; dy <= 3; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockState state = level.getBlockState(center.offset(dx, dy, dz));
					if (dx == 0 && dz == 0) {
						if (!isShaftInteriorAllowed(state)) return Optional.empty();
					} else if (!state.is(Blocks.BRICKS)) {
						return Optional.empty();
					}
				}
			}
		}

		// New compact upper frame. It may face east/west or north/south.
		// Y4: S.S / BRB / S.S
		// Y5: ... / BRB / ...
		// Y6: ... / BPB / ...
		// Y7: ... / SSS / ...
		if (!matchesUpperFrame(level, center, controllerPos, true)
			&& !matchesUpperFrame(level, center, controllerPos, false)) {
			return Optional.empty();
		}

		return AquiferSystem.findCenteredBelow(level, center.above(4));
	}

	private static boolean matchesUpperFrame(ServerLevel level, BlockPos center, BlockPos controllerPos, boolean alongX) {
		for (int dy = 4; dy <= 7; dy++) {
			for (int u = -1; u <= 1; u++) {
				for (int v = -1; v <= 1; v++) {
					int dx = alongX ? u : v;
					int dz = alongX ? v : u;
					BlockPos check = center.offset(dx, dy, dz);
					BlockState state = level.getBlockState(check);

					if (dy == 4) {
						if (v == 0 && Math.abs(u) == 1) {
							if (!ModBlocks.isWoodenBeam(state)) return false;
						} else if (u == 0 && v == 0) {
							if (!state.is(ModBlocks.ROPE)) return false;
						} else if (Math.abs(u) == 1 && Math.abs(v) == 1) {
							if (!state.is(ModBlocks.BRICK_SHINGLES)) return false;
						}
						continue;
					}

					if (dy == 5) {
						if (v == 0 && Math.abs(u) == 1) {
							if (!ModBlocks.isWoodenBeam(state)) return false;
						} else if (u == 0 && v == 0) {
							if (!state.is(ModBlocks.ROPE)) return false;
						}
						continue;
					}

					if (dy == 6) {
						if (v == 0 && Math.abs(u) == 1) {
							if (!ModBlocks.isWoodenBeam(state)) return false;
						} else if (u == 0 && v == 0) {
							if (!check.equals(controllerPos) || !state.is(ModBlocks.WELL_PULLEY)) return false;
						}
						continue;
					}

					if (v == 0 && !state.is(ModBlocks.BRICK_SHINGLES)) return false;
				}
			}
		}
		return true;
	}

	private static boolean isShaftInteriorAllowed(BlockState state) {
		return state.isAir() || state.is(Blocks.WATER) || state.canBeReplaced();
	}

	public static void refillWater(ServerLevel level, BlockPos controllerPos) {
		BlockPos center = controllerPos.below(6);
		for (int dy = 1; dy <= 3; dy++) {
			BlockPos waterPos = center.above(dy);
			BlockState state = level.getBlockState(waterPos);
			if (isShaftInteriorAllowed(state)) {
				level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
			}
		}
	}
}
