package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.BasinBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Input appears as a small wood piece on the basin floor. Land on it to process one piece. */
public final class BasinBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final MapCodec<BasinBlock> CODEC = simpleCodec(BasinBlock::new);
	public static final EnumProperty<Contents> CONTENTS = EnumProperty.create("contents", Contents.class);
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 8, 14);

	public enum Contents implements StringRepresentable {
		EMPTY("empty"), OAK_LOG("oak_log"), OAK_WOOD("oak_wood"), BIRCH_LOG("birch_log"), BIRCH_WOOD("birch_wood"), PLANKS("planks");
		private final String name;
		Contents(String name) { this.name = name; }
		@Override public String getSerializedName() { return name; }
	}

	public BasinBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CONTENTS, Contents.EMPTY));
	}
	@Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BasinBlockEntity(pos, state); }
	@Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
	@Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
	@Override public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, CONTENTS); }
	@Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
	@Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }

	@Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!BasinBlockEntity.supports(stack)) return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BasinBlockEntity basin && basin.insert(stack, player)) {
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.9F);
			return InteractionResult.SUCCESS_SERVER;
		}
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}
	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BasinBlockEntity basin) {
			ItemStack removed = basin.takeOne();
			if (!removed.isEmpty() && !player.getInventory().add(removed)) Block.popResource(level, pos.above(), removed);
		}
		return InteractionResult.SUCCESS;
	}
	@Override public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double distance) {
		super.fallOn(level, state, pos, entity, distance);
		if (distance < 0.4D || level.isClientSide() || !(entity instanceof Player player)) return;
		if (level.getBlockEntity(pos) instanceof BasinBlockEntity basin) basin.jumpProcess(level, player);
	}
}
