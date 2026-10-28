package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.FireclayForgeBlockEntity;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.FireclayForgeMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Wooden-Grinder craftable forge controller; the shell remains ordinary shareable fireclay. */
public final class FireclayForgeControllerBlock extends BaseEntityBlock {
	public static final MapCodec<FireclayForgeControllerBlock> CODEC = simpleCodec(FireclayForgeControllerBlock::new);
	public static final BooleanProperty FORMED = BooleanProperty.create("formed");
	public static final BooleanProperty LIT = BooleanProperty.create("lit");

	public FireclayForgeControllerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
			.setValue(FORMED, false).setValue(LIT, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

	@Override
	protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING, FORMED, LIT);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FireclayForgeBlockEntity forge) player.openMenu(forge);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		return useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FireclayForgeBlockEntity forge) {
			Containers.dropContents(level, pos, forge); // Includes consumed-but-unprocessed wood if interrupted.
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT) || random.nextInt(3) != 0) return;
		Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
		BlockPos chimney = FireclayForgeMultiblock.chimneyTop(pos, facing);
		level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, chimney.getX() + 0.5D,
			chimney.getY() + 0.1D, chimney.getZ() + 0.5D, 0.0D, 0.045D, 0.0D);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FireclayForgeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return createTickerHelper(type, ModBlockEntities.FIRECLAY_FORGE,
			(world, pos, blockState, blockEntity) -> blockEntity.serverTick(world, pos, blockState));
	}
}
