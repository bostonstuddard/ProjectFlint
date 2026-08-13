package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
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
			case HAMMER -> stack.is(ModItems.CRUDE_STONE_HAMMER) || stack.is(ModItems.CRUDE_MULTITOOL) || stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case CUTTER -> stack.is(ModItems.CRUDE_HARVESTING_TOOL) || stack.is(ModItems.CRUDE_MULTITOOL) || stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case MULTITOOL -> stack.is(ModItems.CRUDE_MULTITOOL) || stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case WORKSTATION_DISMANTLE, FLINT_TIER -> stack.is(ModItems.FLINT_MULTITOOL) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case ADVANCED_FLINT_TIER -> stack.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			case FIRESTARTER -> stack.is(ModItems.FLINT_FIRESTARTER) || stack.is(ModItems.BONE_FIRESTARTER);
		};
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
