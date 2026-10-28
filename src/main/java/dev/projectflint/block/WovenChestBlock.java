package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.WoodenChestBlockEntity;
import dev.projectflint.block.entity.WovenChestBlockEntity;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Nine-slot woven storage upgrade. */
public final class WovenChestBlock extends BaseEntityBlock {
	public static final MapCodec<WovenChestBlock> CODEC = simpleCodec(WovenChestBlock::new);
	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public WovenChestBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
			.setValue(OPEN, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
			.setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite())
			.setValue(OPEN, false);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return open(level, pos, player);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.is(ModItems.WOODEN_CHEST_FRAME)) {
			if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WovenChestBlockEntity oldChest) {
				ItemStack[] kept = new ItemStack[oldChest.getContainerSize()];
				for (int i = 0; i < kept.length; i++) kept[i] = oldChest.getItem(i).copy();
				oldChest.clearContent();
				oldChest.setChanged();
				Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
				BlockState next = ModBlocks.WOODEN_CHEST.defaultBlockState()
					.setValue(ChestBlock.FACING, facing)
					.setValue(ChestBlock.TYPE, ChestType.SINGLE)
					.setValue(ChestBlock.WATERLOGGED, false);
				level.setBlock(pos, next, 3);
				if (level.getBlockEntity(pos) instanceof WoodenChestBlockEntity chest) {
					for (int i = 0; i < kept.length; i++) chest.setItem(i, kept[i]);
					chest.setChanged();
				}
				if (!player.isCreative()) stack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		return open(level, pos, player);
	}

	private static InteractionResult open(Level level, BlockPos pos, Player player) {
		if (ChestBlock.isChestBlockedAt(level, pos)) return InteractionResult.SUCCESS;
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WovenChestBlockEntity chest) {
			player.openMenu(chest);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WovenChestBlockEntity chest) {
			Containers.dropContents(level, pos, chest);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING, OPEN);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WovenChestBlockEntity(pos, state);
	}
}
