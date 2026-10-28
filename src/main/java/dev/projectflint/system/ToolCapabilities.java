package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Authoritative capability and speed table for Project Flint primitive tools.
 *
 * Gameplay systems should query this class instead of independently reconstructing
 * Hammer/Cutters/Multitool inheritance rules.
 */
public final class ToolCapabilities {
	public enum Capability {
		HAMMER,
		CUTTER,
		MULTITOOL,
		WORKSTATION_DISMANTLE,
		FLINT_TIER,
		ADVANCED_FLINT_TIER,
		FIRESTARTER
	}

	private ToolCapabilities() {}

	public static boolean has(ItemStack stack, Capability capability) {
		if (stack.isEmpty()) return false;
		return switch (capability) {
			case HAMMER -> stack.is(ModItems.HARDWOOD_HAMMER) || stack.is(ModItems.CRUDE_STONE_HAMMER) || stack.is(ModItems.CRUDE_MULTITOOL) || stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case CUTTER -> stack.is(ModItems.CRUDE_HARVESTING_TOOL) || stack.is(ModItems.CRUDE_MULTITOOL) || stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case MULTITOOL -> stack.is(ModItems.CRUDE_MULTITOOL) || stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case WORKSTATION_DISMANTLE, FLINT_TIER -> stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case ADVANCED_FLINT_TIER -> stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case FIRESTARTER -> stack.is(ModItems.FLINT_FIRESTARTER) || stack.is(ModItems.BONE_FIRESTARTER) || stack.is(ModItems.WOOD_FIRESTARTER);
		};
	}

	/** Masonry can be dismantled with the Advanced Flint Multitool or the later Wooden Pickaxe. */
	public static boolean canMineBrick(ItemStack stack) {
		return stack.is(Items.WOODEN_PICKAXE) || stack.is(ModItems.HARDWOOD_PICKAXE) || stack.is(ModItems.HARDWOOD_HAMMER) || has(stack, Capability.ADVANCED_FLINT_TIER);
	}

	/** Flint tools unlocked around the Advanced Kiln woodworking tier. */
	public static boolean isAdvancedKilnFlintTool(ItemStack stack) {
		return stack.is(ModItems.FLINT_AXE)
			|| stack.is(ModItems.FLINT_PLANT_CUTTERS)
			|| stack.is(ModItems.ADVANCED_FLINT_MULTITOOL)
			|| stack.is(ModItems.FLINT_FILE);
	}

	/** Well frame wood is intentionally dismantled with dedicated Flint Axe/Plant Cutters only. */
	public static boolean isWellWoodTool(ItemStack stack) {
		return stack.is(ModItems.FLINT_AXE) || stack.is(ModItems.FLINT_PLANT_CUTTERS);
	}

	public static float hammerSpeedMultiplier(ItemStack stack) {
		if (stack.is(ModItems.ADVANCED_FLINT_MULTITOOL)) return GameplayTuning.ADVANCED_FLINT_MULTITOOL_SPEED_MULTIPLIER;
		if (stack.is(ModItems.FLINT_MULTITOOL)) return GameplayTuning.FLINT_MULTITOOL_SPEED_MULTIPLIER;
		if (stack.is(ModItems.CRUDE_MULTITOOL)) return GameplayTuning.CRUDE_MULTITOOL_SPEED_MULTIPLIER;
		return 1.0F;
	}

	/**
	 * Plant Cutters are a dedicated progression upgrade: for vegetation they perform at
	 * Advanced Flint Multitool tier, without inheriting its non-plant capabilities.
	 */
	public static boolean isAdvancedPlantTier(ItemStack stack) {
		return stack.is(ModItems.FLINT_PLANT_CUTTERS) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
	}

	public static float cutterSpeedMultiplier(ItemStack stack) {
		if (isAdvancedPlantTier(stack)) return GameplayTuning.ADVANCED_FLINT_MULTITOOL_SPEED_MULTIPLIER;
		if (stack.is(ModItems.FLINT_MULTITOOL)) return GameplayTuning.FLINT_MULTITOOL_SPEED_MULTIPLIER;
		if (stack.is(ModItems.CRUDE_MULTITOOL)) return GameplayTuning.CRUDE_MULTITOOL_SPEED_MULTIPLIER;
		return 1.0F;
	}

	/** Flint-tier foliage doubles its normal cutter speed; Plant Cutters use the advanced tier. */
	public static float foliageCutterSpeedMultiplier(ItemStack stack) {
		float multiplier = cutterSpeedMultiplier(stack);
		return (isAdvancedPlantTier(stack) || stack.is(ModItems.FLINT_MULTITOOL)) ? multiplier * 2.0F : multiplier;
	}

	/** Dedicated leaf timing keeps leaves grindy even as general cutter speed improves. */
	public static float leafProgressPerTick(ItemStack stack) {
		// v0.31.3-b: preserve tier ordering while making leaf processing less instantaneous.
		// Flint Multitool: 2.0s -> 2.2s (+10%, exactly +0.2s).
		// Advanced Flint Multitool: 1.5s -> 1.875s (+25%, smaller than +0.45s).
		// Flint Plant Cutters: 1.5s -> 2.025s (+35%, smaller than +0.75s).
		if (stack.is(ModItems.FLINT_PLANT_CUTTERS)) return 1.0F / 40.5F;
		if (stack.is(ModItems.ADVANCED_FLINT_MULTITOOL)) return 1.0F / 37.5F;
		if (stack.is(ModItems.FLINT_MULTITOOL)) return 1.0F / 44.0F;
		if (stack.is(ModItems.CRUDE_MULTITOOL)) return 1.0F / 50.0F;
		return 0.0F;
	}

	public static float earthProgressMultiplier(ItemStack stack) {
		if (stack.is(ModItems.ADVANCED_FLINT_MULTITOOL)) return GameplayTuning.ADVANCED_FLINT_MULTITOOL_EARTH_PROGRESS_MULTIPLIER;
		if (stack.is(ModItems.FLINT_MULTITOOL)) return GameplayTuning.FLINT_MULTITOOL_EARTH_PROGRESS_MULTIPLIER;
		if (stack.is(ModItems.CRUDE_MULTITOOL)) return GameplayTuning.CRUDE_MULTITOOL_EARTH_PROGRESS_MULTIPLIER;
		return 0.0F;
	}

	/** Plants that the Flint Multitool cleanly uproots instead of processing into primitive materials. */
	public static boolean canDirectHarvestPlant(ItemStack stack, BlockState state) {
		return (stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.FLINT_PLANT_CUTTERS) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL))
			&& (state.is(BlockTags.FLOWERS)
				|| state.is(Blocks.DEAD_BUSH)
				|| state.is(Blocks.BUSH)
				|| state.is(Blocks.FIREFLY_BUSH));
	}

	/** Plant-cutting tools that inherit Flint Multitool foliage harvesting without gaining mining/earth capabilities. */
	public static boolean canHarvestLeaves(ItemStack stack) {
		return stack.is(ModItems.CRUDE_MULTITOOL)
			|| stack.is(ModItems.FLINT_MULTITOOL)
			|| stack.is(ModItems.FLINT_PLANT_CUTTERS)
			|| stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
	}
}
