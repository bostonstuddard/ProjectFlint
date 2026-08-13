package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class LumberWorkpieceBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape HALF_SLAB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    public static final MapCodec<LumberWorkpieceBlock> CODEC = simpleCodec(LumberWorkpieceBlock::new);
    public static final IntegerProperty OUTPUT = IntegerProperty.create("output", 0, 1); // 0 boards, 1 beam

    public LumberWorkpieceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OUTPUT, 0));
    }

    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return HALF_SLAB; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return HALF_SLAB; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, OUTPUT);
    }
}
