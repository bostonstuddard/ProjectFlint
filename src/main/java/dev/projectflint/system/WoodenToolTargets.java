package dev.projectflint.system;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** One predicate used by attack permission, mining progress and completed drops. */
public final class WoodenToolTargets {
	private WoodenToolTargets() {}
	public static boolean pickaxe(BlockState state) {
		return state.is(Blocks.STONE) || state.is(ModBlocks.CRUSHED_STONE) || state.is(Blocks.COAL_ORE);
	}
	/** Ordinary masonry recoverable with the later Wooden Pickaxe. Not a blanket machine-dismantle permission. */
	public static boolean brickMasonry(BlockState state) {
		return state.is(Blocks.BRICKS)
			|| state.is(ModBlocks.BRICK_SHINGLES)
			|| state.is(ModBlocks.FIRECLAY_BRICK_BLOCK)
			|| state.is(ModBlocks.FIRECLAY_BRICK_SLAB)
			|| state.is(ModBlocks.FIRECLAY_BRICK_STAIRS)
			|| state.is(ModBlocks.MORTAR_BRICKS)
			|| state.is(ModBlocks.MORTAR_BRICK_SLAB)
			|| state.is(ModBlocks.LOOSE_MUD_BRICKS)
			|| state.is(ModBlocks.LOOSE_MUD_BRICK_SLAB)
			|| state.is(ModBlocks.WATTLED_DAUB_BRICKS)
			|| state.is(ModBlocks.WATTLED_DAUB_WINDOW)
			|| state.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)
			|| state.is(ModBlocks.LIMESTONE_BRICKS);
	}

	public static boolean shovel(BlockState state) {
		return state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)
			|| state.is(Blocks.GRAVEL) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.CLAY)
			|| state.is(ModBlocks.LOOSE_SOIL) || state.is(ModBlocks.LOOSE_DIRT);
	}
}
