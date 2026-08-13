package dev.projectflint.block;

import dev.projectflint.block.entity.AdvancedKilnBlockEntity;
import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Non-controller pieces of the 2x2x2 Advanced Kiln.
 * Each part remembers its facing and exact position relative to the controller so
 * the whole structure can render and dismantle as one multiblock.
 */
public final class AdvancedKilnCasingBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public AdvancedKilnCasingBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(PART, Part.BACK_BOTTOM_LEFT)
			.setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART, ACTIVE);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return openKiln(level, pos, state, player);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return openKiln(level, pos, state, player);
	}

	private static InteractionResult openKiln(Level level, BlockPos pos, BlockState state, Player player) {
		BlockPos controllerPos = AdvancedKilnMultiblock.controllerPos(pos, state);
		if (!level.isClientSide() && level.getBlockState(controllerPos).is(ModBlocks.ADVANCED_KILN_CONTROLLER)
			&& level.getBlockEntity(controllerPos) instanceof AdvancedKilnBlockEntity kiln) {
			player.openMenu(kiln);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) AdvancedKilnMultiblock.dismantle(level, pos, state, player);
		return super.playerWillDestroy(level, pos, state, player);
	}

	public enum Part implements StringRepresentable {
		FRONT_BOTTOM_RIGHT("front_bottom_right", 1, 0, 0),
		FRONT_TOP_LEFT("front_top_left", 0, 1, 0),
		FRONT_TOP_RIGHT("front_top_right", 1, 1, 0),
		BACK_BOTTOM_LEFT("back_bottom_left", 0, 0, 1),
		BACK_BOTTOM_RIGHT("back_bottom_right", 1, 0, 1),
		BACK_TOP_LEFT("back_top_left", 0, 1, 1),
		BACK_TOP_RIGHT("back_top_right", 1, 1, 1);

		private final String serializedName;
		private final int across;
		private final int up;
		private final int depth;

		Part(String serializedName, int across, int up, int depth) {
			this.serializedName = serializedName;
			this.across = across;
			this.up = up;
			this.depth = depth;
		}

		@Override
		public String getSerializedName() {
			return serializedName;
		}

		public BlockPos controllerFrom(BlockPos partPos, Direction facing) {
			Direction acrossDirection = facing.getClockWise();
			Direction backDirection = facing.getOpposite();
			return partPos
				.relative(acrossDirection, -across)
				.below(up)
				.relative(backDirection, -depth);
		}

		public static Part of(int across, int up, int depth) {
			for (Part part : values()) {
				if (part.across == across && part.up == up && part.depth == depth) return part;
			}
			throw new IllegalArgumentException("Controller occupies front_bottom_left; no casing part for " + across + "," + up + "," + depth);
		}
	}
}
