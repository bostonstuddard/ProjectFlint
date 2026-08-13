package dev.projectflint.block;

import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class MortarBricksBlock extends Block {
	public MortarBricksBlock(BlockBehaviour.Properties properties) { super(properties); }

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		Direction front = hit.getDirection();
		if (!stack.is(ModItems.ADVANCED_KILN_GLASS) || !front.getAxis().isHorizontal()) return InteractionResult.PASS;
		BlockPos base = findCube(level, pos, front);
		if (base == null) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			Direction across = front.getClockWise();
			Direction back = front.getOpposite();
			for (int y = 0; y < 2; y++) {
				for (int a = 0; a < 2; a++) {
					for (int d = 0; d < 2; d++) {
						BlockPos at = base.above(y).relative(across, a).relative(back, d);
						if (y == 0 && a == 0 && d == 0) {
							level.setBlockAndUpdate(at, ModBlocks.ADVANCED_KILN_CONTROLLER.defaultBlockState()
								.setValue(AdvancedKilnControllerBlock.FACING, front));
						} else {
							AdvancedKilnCasingBlock.Part part = AdvancedKilnCasingBlock.Part.of(a, y, d);
							level.setBlockAndUpdate(at, ModBlocks.ADVANCED_KILN_CASING.defaultBlockState()
								.setValue(AdvancedKilnCasingBlock.FACING, front)
								.setValue(AdvancedKilnCasingBlock.PART, part));
						}
					}
				}
			}
			if (!player.isCreative()) stack.shrink(1);
			level.playSound(null, base, SoundEvents.DECORATED_POT_PLACE, SoundSource.BLOCKS, 0.9F, 0.62F);
			if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "advanced_kiln");
		}
		return InteractionResult.SUCCESS;
	}

	private static BlockPos findCube(Level level, BlockPos clicked, Direction front) {
		Direction across = front.getClockWise();
		Direction back = front.getOpposite();
		for (int down = 0; down <= 1; down++) {
			for (int acrossOffset = 0; acrossOffset <= 1; acrossOffset++) {
				BlockPos base = clicked.below(down).relative(across, -acrossOffset);
				boolean valid = true;
				for (int y = 0; y < 2 && valid; y++) {
					for (int a = 0; a < 2 && valid; a++) {
						for (int d = 0; d < 2; d++) {
							if (!level.getBlockState(base.above(y).relative(across, a).relative(back, d)).is(ModBlocks.MORTAR_BRICKS)) {
								valid = false;
								break;
							}
						}
					}
				}
				if (valid) return base;
			}
		}
		return null;
	}
}
