package dev.projectflint.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Client-safe Clay Grinder recipe index used by the Project Flint guide.
 *
 * Kept as a compact mirror of the data-driven clay_grinding recipes because
 * Minecraft 1.21.11 does not synchronize arbitrary custom recipe types into
 * ClientRecipeContainer. Gameplay remains authoritative in the recipe JSONs;
 * tools/validate_release.py verifies this guide mirror stays in sync.
 */
public final class ClayGrinderRecipeGuideRegistry {
	private static final List<Definition> RECIPES = List.of(
		new Definition("minecraft:stripped_acacia_log", 1, "minecraft:acacia_slab", 2),
		new Definition("minecraft:acacia_slab", 2, "minecraft:stripped_acacia_log", 1),
		new Definition("minecraft:stripped_bamboo_block", 1, "minecraft:bamboo_slab", 2),
		new Definition("minecraft:bamboo_slab", 2, "minecraft:stripped_bamboo_block", 1),
		new Definition("minecraft:stripped_birch_log", 1, "minecraft:birch_slab", 2),
		new Definition("minecraft:birch_slab", 2, "minecraft:stripped_birch_log", 1),
		new Definition("projectflint:filed_bone", 4, "projectflint:bone_club", 1),
		new Definition("projectflint:burnt_branch_slab", 2, "projectflint:burnt_branch_block", 1),
		new Definition("projectflint:burnt_branch_block", 1, "projectflint:burnt_branch_slab", 2),
		new Definition("projectflint:burnt_branch_slab", 1, "projectflint:burnt_branch", 8),
		new Definition("minecraft:stripped_cherry_log", 1, "minecraft:cherry_slab", 2),
		new Definition("minecraft:cherry_slab", 2, "minecraft:stripped_cherry_log", 1),
		new Definition("projectflint:super_heated_clay", 9, "projectflint:clay_mortar", 1),
		new Definition("projectflint:crude_clay_slab", 2, "projectflint:crude_clay_block", 1),
		new Definition("projectflint:crude_clay_block", 1, "projectflint:crude_clay_slab", 2),
		new Definition("projectflint:crushed_stone_slab", 2, "projectflint:crushed_stone", 1),
		new Definition("projectflint:very_refined_stone", 8, "projectflint:crushed_stone", 1),
		new Definition("projectflint:crushed_stone", 1, "projectflint:crushed_stone_slab", 2),
		new Definition("minecraft:stripped_dark_oak_log", 1, "minecraft:dark_oak_slab", 2),
		new Definition("minecraft:dark_oak_slab", 2, "minecraft:stripped_dark_oak_log", 1),
		new Definition("minecraft:flint", 1, "projectflint:flint_chip", 4),
		new Definition("projectflint:flint_chip", 4, "minecraft:flint", 1),
		new Definition("projectflint:sanded_wood_slab", 2, "projectflint:grinded_wood_slab", 1),
		new Definition("minecraft:stripped_jungle_log", 1, "minecraft:jungle_slab", 2),
		new Definition("minecraft:jungle_slab", 2, "minecraft:stripped_jungle_log", 1),
		new Definition("projectflint:loose_dirt_slab", 2, "projectflint:loose_dirt", 1),
		new Definition("minecraft:stripped_mangrove_log", 1, "minecraft:mangrove_slab", 2),
		new Definition("minecraft:mangrove_slab", 2, "minecraft:stripped_mangrove_log", 1),
		new Definition("projectflint:mortar_bricks", 1, "projectflint:mortar_brick_slab", 2),
		new Definition("projectflint:mortar_brick_slab", 2, "projectflint:mortar_bricks", 1),
		new Definition("projectflint:mortar_bricks", 32, "projectflint:mortar_kiln_base", 1),
		new Definition("minecraft:stripped_oak_log", 1, "minecraft:oak_slab", 2),
		new Definition("minecraft:oak_slab", 2, "minecraft:stripped_oak_log", 1),
		new Definition("minecraft:stripped_pale_oak_log", 1, "minecraft:pale_oak_slab", 2),
		new Definition("minecraft:pale_oak_slab", 2, "minecraft:stripped_pale_oak_log", 1),
		new Definition("projectflint:grinded_wood_slab", 1, "projectflint:sanded_wood_slab", 2),
		new Definition("projectflint:sugarcane_roll", 4, "projectflint:sanding_paper", 1),
		new Definition("minecraft:stripped_spruce_log", 1, "minecraft:spruce_slab", 2),
		new Definition("minecraft:spruce_slab", 2, "minecraft:stripped_spruce_log", 1),
		new Definition("projectflint:crushed_stone", 8, "projectflint:stone_grindsaw", 1),
		new Definition("projectflint:twig", 2, "projectflint:branch", 1),
		new Definition("projectflint:unfired_clay_mold_base", 1, "projectflint:unfired_clay_axe_mold", 1),
		new Definition("projectflint:unfired_clay_mold_base", 1, "projectflint:unfired_clay_block_mold", 1),
		new Definition("projectflint:unfired_clay_mold_base", 1, "projectflint:unfired_clay_brick_mold", 1),
		new Definition("projectflint:unfired_clay_mold_base", 1, "projectflint:unfired_clay_cutters_mold", 1),
		new Definition("projectflint:unfired_clay_mold_base", 1, "projectflint:unfired_clay_multitool_mold", 1),
		new Definition("projectflint:unfired_clay_mold_base", 1, "projectflint:unfired_clay_pot_mold", 1),
		new Definition("projectflint:glass_shard", 4, "projectflint:unfired_kiln_glass", 1),
		new Definition("projectflint:wattled_daub_bricks", 1, "projectflint:wattled_daub_floor_tile", 2),
		new Definition("projectflint:woven_wood", 16, "projectflint:wooden_grinder_base", 1),
		new Definition("projectflint:woven_wood_slab", 2, "projectflint:woven_wood", 1),
		new Definition("projectflint:woven_wood", 1, "projectflint:woven_wood_slab", 2)
	);

	private ClayGrinderRecipeGuideRegistry() {}

	public static List<Definition> recipes() {
		return RECIPES;
	}

	private static ItemStack stack(String itemId, int count) {
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
		if (item == null) return ItemStack.EMPTY;
		return new ItemStack(item, count);
	}

	public record Definition(String inputId, int inputCount, String outputId, int outputCount) {
		public ItemStack inputStack() { return stack(inputId, inputCount); }
		public ItemStack outputStack() { return stack(outputId, outputCount); }
	}
}
