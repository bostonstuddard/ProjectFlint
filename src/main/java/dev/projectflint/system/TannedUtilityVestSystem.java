package dev.projectflint.system;

import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Explicit hand-clearing privilege; intentionally excludes structural blocks, wood and ores. */
public final class TannedUtilityVestSystem {
	private TannedUtilityVestSystem() {}
	/** Well frame parts have their own recovery path (including Beam screws and Pulley inventory). */
	public static boolean canDismantleWellFrame(Player player, BlockState state) {
		return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TANNED_UTILITY_VEST)
			&& (ModBlocks.isWoodenBeam(state) || state.is(ModBlocks.WELL_PULLEY));
	}

	public static boolean canHandClear(Player player, BlockState state) {
		return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TANNED_UTILITY_VEST)
			&& (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS)
				|| state.is(ModTags.BREAKABLE_BY_HAND)
				|| state.is(ModTags.FORAGE_BUSHES)
				|| state.is(Blocks.SWEET_BERRY_BUSH) || state.is(ModBlocks.STRAWBERRY_BUSH)
				|| state.is(BlockTags.SAPLINGS)
				|| state.is(BlockTags.LEAVES)
				|| state.is(BlockTags.FLOWERS)
				|| state.is(BlockTags.SMALL_FLOWERS)
				|| state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
				|| state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)
				|| state.is(Blocks.LEAF_LITTER) || state.is(Blocks.PINK_PETALS)
				|| state.is(Blocks.WILDFLOWERS) || state.is(Blocks.LILY_PAD)
				|| state.is(Blocks.SUGAR_CANE) || state.is(Blocks.VINE)
				|| state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.DEAD_BUSH)
				|| state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)
				|| state.is(ModBlocks.SLEEPING_BAG));
	}
}
