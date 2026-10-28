package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.PrimitiveInteractionSystem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Low-profile stations; Basin processes stripped oak/birch while the others remain prototypes. */
public final class PrototypeStationBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<PrototypeStationBlock> CODEC = simpleCodec(PrototypeStationBlock::new);
	private static final VoxelShape BASIN_SHAPE = Block.box(2, 0, 2, 14, 8, 14);
	private static final VoxelShape RACK_SHAPE = Block.box(1, 0, 1, 15, 8, 15);
	private static final VoxelShape MILLSTONE_SHAPE = Block.box(2, 0, 2, 14, 10, 14);

	@Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
	public PrototypeStationBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	private VoxelShape shape() {
		return switch (BuiltInRegistries.BLOCK.getKey(this).getPath()) {
			case "drying_rack" -> RACK_SHAPE;
			case "millstone" -> MILLSTONE_SHAPE;
			default -> BASIN_SHAPE;
		};
	}
	@Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return shape(); }
	@Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return shape(); }
	@Override public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	@Override protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}
	@Override protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
}
