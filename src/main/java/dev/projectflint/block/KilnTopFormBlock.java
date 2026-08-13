package dev.projectflint.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Cut loose-dirt shell waiting for the baked base to fire it into the permanent kiln top. */
public final class KilnTopFormBlock extends Block {
	public KilnTopFormBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.5D, 0.0D, 1.5D, 14.5D, 15.0D, 14.5D);
	}
}
