package dev.projectflint.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;

/** Placeable three-axis hardwood shaft. Mechanical rotation transfer is a later feature. */
public final class AxleBlock extends RotatedPillarBlock {
    private static final VoxelShape X = Block.box(0, 6, 6, 16, 10, 10);
    private static final VoxelShape Y = Block.box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape Z = Block.box(6, 6, 0, 10, 10, 16);
    public AxleBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        return axis == Direction.Axis.X ? X : axis == Direction.Axis.Y ? Y : Z;
    }
}
