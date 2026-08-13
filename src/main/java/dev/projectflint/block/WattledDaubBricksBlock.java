package dev.projectflint.block;

import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Early structural masonry made from fired wattle-and-daub.
 *
 * A vertical structure may be at most four cells tall. A four-high load-bearing
 * column can support same-height Wattled Daub ceiling pieces up to three blocks
 * outward in each cardinal direction. Windows participate as structural cells,
 * but their own placement rules restrict them to wall layers two and three.
 * A two-high Wattled Daub Door counts as two structural cells and is allowed to
 * use a Wattled Daub Floor Tile as its foundation.
 */
public class WattledDaubBricksBlock extends Block implements Fallable {
	public static final int MAX_COLUMN_HEIGHT = 4;
	public static final int MAX_CEILING_REACH = 3;
	/** Enough grace to actually swap a wall brick for a window before the roof reacts. */
	public static final int STRUCTURAL_RECHECK_DELAY_TICKS = 10;

	public WattledDaubBricksBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		return isStructurallySupported(context.getLevel(), pos) ? defaultBlockState() : null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return isStructurallySupported(level, pos);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		level.scheduleTick(pos, this, STRUCTURAL_RECHECK_DELAY_TICKS);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTicks, BlockPos pos,
		Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		scheduledTicks.scheduleTick(pos, this, STRUCTURAL_RECHECK_DELAY_TICKS);
		return super.updateShape(state, level, scheduledTicks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.canSurvive(level, pos)) return;

		if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinY()) {
			// Disable the FallingBlockEntity's ordinary block-item drop immediately.
			// Unsupported Wattled Daub must crumble into primitive materials only.
			FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
			falling.disableDrop();
			return;
		}

		crumble(level, pos);
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
		fallingBlock.disableDrop();
		Block.popResource(level, pos, new ItemStack(ModItems.CRUDE_REED));
		Block.popResource(level, pos, new ItemStack(ModItems.TWIG, 2));
	}

	public static boolean isWattledDaubWall(BlockState state) {
		return state.is(ModBlocks.WATTLED_DAUB_BRICKS) || state.is(ModBlocks.WATTLED_DAUB_WINDOW);
	}

	public static boolean isStructuralCell(BlockState state) {
		return isWattledDaubWall(state) || state.is(ModBlocks.WATTLED_DAUB_DOOR);
	}

	/**
	 * Returns the candidate cell's 1-based layer in a contiguous structural column,
	 * or -1 when the column does not terminate on an allowed foundation.
	 */
	public static int verticalStructuralLayer(LevelReader level, BlockPos pos) {
		int height = 1;
		boolean sawDoor = false;
		BlockPos cursor = pos.below();
		while (isStructuralCell(level.getBlockState(cursor))) {
			BlockState cell = level.getBlockState(cursor);
			if (cell.is(ModBlocks.WATTLED_DAUB_DOOR)) sawDoor = true;
			height++;
			if (height > MAX_COLUMN_HEIGHT) return -1;
			cursor = cursor.below();
		}

		BlockState foundation = level.getBlockState(cursor);
		if (foundation.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)) {
			// Floor tiles are intentionally NOT wall foundations. The creative-only
			// door is the one exception, allowing a proper threshold with wall above.
			return sawDoor ? height : -1;
		}
		return foundation.isFaceSturdy(level, cursor, Direction.UP) ? height : -1;
	}

	public static boolean isStructurallySupported(LevelReader level, BlockPos pos) {
		if (verticalStructuralLayer(level, pos) > 0) return true;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			for (int distance = 1; distance <= MAX_CEILING_REACH; distance++) {
				BlockPos anchor = pos.relative(direction, distance);
				if (!isLoadBearingColumnTop(level, anchor)) continue;

				boolean connected = true;
				for (int step = 1; step < distance; step++) {
					if (!isWattledDaubWall(level.getBlockState(pos.relative(direction, step)))) {
						connected = false;
						break;
					}
				}
				if (connected) return true;
			}
		}
		return false;
	}

	private static boolean isLoadBearingColumnTop(LevelReader level, BlockPos top) {
		boolean sawDoor = false;
		for (int offset = 0; offset < MAX_COLUMN_HEIGHT; offset++) {
			BlockState cell = level.getBlockState(top.below(offset));
			if (!isStructuralCell(cell)) return false;
			if (cell.is(ModBlocks.WATTLED_DAUB_DOOR)) sawDoor = true;
		}
		BlockPos foundationPos = top.below(MAX_COLUMN_HEIGHT);
		BlockState foundation = level.getBlockState(foundationPos);
		if (foundation.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)) return sawDoor;
		return !isStructuralCell(foundation) && foundation.isFaceSturdy(level, foundationPos, Direction.UP);
	}

	private static void crumble(ServerLevel level, BlockPos pos) {
		if (!isWattledDaubWall(level.getBlockState(pos))) return;
		level.removeBlock(pos, false);
		Block.popResource(level, pos, new ItemStack(ModItems.CRUDE_REED));
		Block.popResource(level, pos, new ItemStack(ModItems.TWIG, 2));
		level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.65F, 0.78F);
	}
}
