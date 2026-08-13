package dev.projectflint.block;

import dev.projectflint.block.entity.AdvancedKilnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Front-bottom-left controller block for the 2x2x2 Advanced Kiln. */
public final class AdvancedKilnControllerBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public AdvancedKilnControllerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
		return simpleCodec(AdvancedKilnControllerBlock::new);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return open(level, pos, player);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return open(level, pos, player);
	}

	private static InteractionResult open(Level level, BlockPos pos, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AdvancedKilnBlockEntity kiln) {
			player.openMenu(kiln);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) AdvancedKilnMultiblock.dismantle(level, pos, state, player);
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AdvancedKilnBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (lvl, pos, st, be) -> {
			if (be instanceof AdvancedKilnBlockEntity kiln) kiln.serverTick(lvl, pos, st);
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, ACTIVE);
	}
}
