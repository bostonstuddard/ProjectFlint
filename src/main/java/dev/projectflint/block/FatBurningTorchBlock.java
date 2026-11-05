package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.FatBurningTorchBlockEntity;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Freestanding 30-minute fat torch; an overhead block protects it from rain. */
public final class FatBurningTorchBlock extends Block implements EntityBlock {
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	/** UP is standing; horizontal values point away from the supporting wall. */
	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, facing -> facing != Direction.DOWN);
	public static final MapCodec<FatBurningTorchBlock> CODEC = simpleCodec(FatBurningTorchBlock::new);
	private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 11, 10);
	

	public FatBurningTorchBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(FACING, Direction.UP));
	}

	@Override protected MapCodec<? extends Block> codec() { return CODEC; }
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT, FACING); }
	@Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction mount = state.getValue(FACING);
        if (mount == Direction.UP) return SHAPE;
        // Match the vanilla wall-torch selection outline in all four directions.
        return WallTorchBlock.getShape(Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, mount));
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clicked = context.getClickedFace();
		BlockState result = defaultBlockState().setValue(FACING, clicked == Direction.DOWN ? Direction.UP : clicked);
		if (result.canSurvive(context.getLevel(), context.getClickedPos())) return result;
		BlockState standing = defaultBlockState().setValue(FACING, Direction.UP);
		return standing.canSurvive(context.getLevel(), context.getClickedPos()) ? standing : null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction mount = state.getValue(FACING);
		return mount == Direction.UP ? Block.canSupportCenter(level, pos.below(), Direction.UP)
			: Block.canSupportCenter(level, pos.relative(mount.getOpposite()), mount);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTicks, BlockPos pos,
		Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) scheduledTicks.scheduleTick(pos, this, 1);
		return super.updateShape(state, level, scheduledTicks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
		if (!state.canSurvive(level, pos)) {
			level.scheduleTick(pos, this, 1);
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			Block.popResource(level, pos, new ItemStack(dev.projectflint.registry.ModItems.FAT_BURNING_TORCH));
			level.removeBlock(pos, false);
			return;
		}
		super.tick(state, level, pos, random);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hit) {
		boolean fat = stack.is(ModItems.FAT);
		boolean starter = stack.is(Items.FLINT_AND_STEEL) || ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER);
		if (!fat && !starter) return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof FatBurningTorchBlockEntity torch)) return InteractionResult.FAIL;
		if (fat) {
			if (!torch.addFat()) return InteractionResult.FAIL;
			if (!player.isCreative()) stack.shrink(1);
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.4F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		if (state.getValue(LIT) || !torch.ignite()) return InteractionResult.FAIL;
		if (!player.isCreative() && stack.isDamageableItem()) stack.hurtAndBreak(1, player, hand);
		level.setBlockAndUpdate(pos, state.setValue(LIT, true));
		level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.6F, 1.0F);
		return InteractionResult.SUCCESS;
	}

	@Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction facing = state.getValue(FACING);
        // Wall-mounted flames must follow the rotated torch tip, not remain in the block center.
        double x = pos.getX() + 0.5D - (facing == Direction.UP ? 0.0D : 0.27D * facing.getStepX());
        double y = pos.getY() + (facing == Direction.UP ? 0.75D : 0.92D);
        double z = pos.getZ() + 0.5D - (facing == Direction.UP ? 0.0D : 0.27D * facing.getStepZ());
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.SMOKE, x, y + (facing == Direction.UP ? 0.08D : 0.0D), z, 0, 0.015D, 0);
        if (random.nextInt(2) == 0) level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }

	@Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FatBurningTorchBlockEntity(pos, state); }
	@Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (lvl, pos, st, be) -> {
			if (lvl instanceof ServerLevel server && be instanceof FatBurningTorchBlockEntity torch) torch.serverTick(server, pos, st);
		};
	}
}
