package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Final processed rope. Earlier rope stages are items only; only this tier can be placed. */
public final class RopeBlock extends Block {
	public static final MapCodec<RopeBlock> CODEC = simpleCodec(RopeBlock::new);
	private static final VoxelShape SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);

	public RopeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override protected MapCodec<? extends Block> codec() { return CODEC; }
	@Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
