package dev.projectflint.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Player-facing 2x2 crafting reference; entries generated from ProjectFlint recipe JSONs.
 * When adding a crafting-grid recipe, update this reference via tools/generate_personal_crafting_guide.py.
 * Keep gameplay recipes in JSON; PF-EMI independently owns its recipe presentation.
 */
public final class PersonalCraftingGuideRegistry {
	public record Definition(String id, List<ItemStack> inputs, ItemStack result) {}
	private PersonalCraftingGuideRegistry() {}
	private static ItemStack item(String id, int count) {
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
		return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
	}
	private static void add(List<Definition> recipes, String id, String output, int count, String... ingredients) {
		List<ItemStack> stacks = new ArrayList<>();
		for (String entry : ingredients) {
			String[] pair = entry.split("#");
			ItemStack stack = item(pair[0], pair.length > 1 ? Integer.parseInt(pair[1]) : 1);
			if (!stack.isEmpty()) stacks.add(stack);
		}
		ItemStack result = item(output, count);
		if (!result.isEmpty()) recipes.add(new Definition(id, stacks, result));
	}
	public static List<Definition> recipes() {
		List<Definition> recipes = new ArrayList<>();
		add(recipes, "leather_sheet_from_tanned", "projectflint:leather_sheet", 1, "projectflint:tanned_leather", "projectflint:bone_knife");
		add(recipes, "leather_straps_from_sheet", "projectflint:leather_strap", 2, "projectflint:leather_sheet", "projectflint:bone_knife");
		add(recipes, "leather_repair_kit", "projectflint:repair_kit", 1, "projectflint:leather_strap", "minecraft:string");
		add(recipes, "wooden_pickaxe", "minecraft:wooden_pickaxe", 1, "minecraft:stick", "minecraft:string", "projectflint:leather_strap", "projectflint:wooden_sheet");
		add(recipes, "wooden_axe", "minecraft:wooden_axe", 1, "projectflint:tannin", "projectflint:leather_sheet", "minecraft:stick", "projectflint:wooden_sheet");
		add(recipes, "wooden_shovel", "minecraft:wooden_shovel", 1, "minecraft:oak_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_spruce", "minecraft:wooden_shovel", 1, "minecraft:spruce_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_birch", "minecraft:wooden_shovel", 1, "minecraft:birch_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_jungle", "minecraft:wooden_shovel", 1, "minecraft:jungle_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_acacia", "minecraft:wooden_shovel", 1, "minecraft:acacia_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_dark_oak", "minecraft:wooden_shovel", 1, "minecraft:dark_oak_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_mangrove", "minecraft:wooden_shovel", 1, "minecraft:mangrove_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_cherry", "minecraft:wooden_shovel", 1, "minecraft:cherry_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "wooden_shovel_pale_oak", "minecraft:wooden_shovel", 1, "minecraft:pale_oak_planks", "minecraft:stick", "projectflint:leather_strap");
		add(recipes, "crushed_stone_from_cobblestone", "projectflint:crushed_stone", 1, "minecraft:cobblestone");
		add(recipes, "basin_crafting", "projectflint:basin", 1, "projectflint:leather_sheet", "projectflint:wooden_sheet", "minecraft:cobblestone#2");
		add(recipes, "oak_planks_from_log", "minecraft:oak_planks", 2, "minecraft:oak_log", "minecraft:wooden_axe");
		add(recipes, "oak_planks_from_log_hardwood_axe", "minecraft:oak_planks", 2, "minecraft:oak_log", "projectflint:hardwood_axe");
		add(recipes, "oak_planks_from_stripped_log", "minecraft:oak_planks", 2, "minecraft:stripped_oak_log", "minecraft:wooden_axe");
		add(recipes, "oak_planks_from_stripped_log_hardwood_axe", "minecraft:oak_planks", 2, "minecraft:stripped_oak_log", "projectflint:hardwood_axe");
		add(recipes, "oak_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:oak_planks", "projectflint:bone_knife");
		add(recipes, "oak_board_from_plank", "projectflint:wooden_board", 1, "minecraft:oak_planks", "projectflint:flint_file");
		add(recipes, "oak_door_hinged", "minecraft:oak_door", 1, "projectflint:wooden_door_hinge", "minecraft:oak_planks#3");
		add(recipes, "spruce_planks_from_log", "minecraft:spruce_planks", 2, "minecraft:spruce_log", "minecraft:wooden_axe");
		add(recipes, "spruce_planks_from_log_hardwood_axe", "minecraft:spruce_planks", 2, "minecraft:spruce_log", "projectflint:hardwood_axe");
		add(recipes, "spruce_planks_from_stripped_log", "minecraft:spruce_planks", 2, "minecraft:stripped_spruce_log", "minecraft:wooden_axe");
		add(recipes, "spruce_planks_from_stripped_log_hardwood_axe", "minecraft:spruce_planks", 2, "minecraft:stripped_spruce_log", "projectflint:hardwood_axe");
		add(recipes, "spruce_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:spruce_planks", "projectflint:bone_knife");
		add(recipes, "spruce_board_from_plank", "projectflint:wooden_board", 1, "minecraft:spruce_planks", "projectflint:flint_file");
		add(recipes, "spruce_door_hinged", "minecraft:spruce_door", 1, "projectflint:wooden_door_hinge", "minecraft:spruce_planks#3");
		add(recipes, "birch_planks_from_log", "minecraft:birch_planks", 2, "minecraft:birch_log", "minecraft:wooden_axe");
		add(recipes, "birch_planks_from_log_hardwood_axe", "minecraft:birch_planks", 2, "minecraft:birch_log", "projectflint:hardwood_axe");
		add(recipes, "birch_planks_from_stripped_log", "minecraft:birch_planks", 2, "minecraft:stripped_birch_log", "minecraft:wooden_axe");
		add(recipes, "birch_planks_from_stripped_log_hardwood_axe", "minecraft:birch_planks", 2, "minecraft:stripped_birch_log", "projectflint:hardwood_axe");
		add(recipes, "birch_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:birch_planks", "projectflint:bone_knife");
		add(recipes, "birch_board_from_plank", "projectflint:wooden_board", 1, "minecraft:birch_planks", "projectflint:flint_file");
		add(recipes, "birch_door_hinged", "minecraft:birch_door", 1, "projectflint:wooden_door_hinge", "minecraft:birch_planks#3");
		add(recipes, "jungle_planks_from_log", "minecraft:jungle_planks", 2, "minecraft:jungle_log", "minecraft:wooden_axe");
		add(recipes, "jungle_planks_from_log_hardwood_axe", "minecraft:jungle_planks", 2, "minecraft:jungle_log", "projectflint:hardwood_axe");
		add(recipes, "jungle_planks_from_stripped_log", "minecraft:jungle_planks", 2, "minecraft:stripped_jungle_log", "minecraft:wooden_axe");
		add(recipes, "jungle_planks_from_stripped_log_hardwood_axe", "minecraft:jungle_planks", 2, "minecraft:stripped_jungle_log", "projectflint:hardwood_axe");
		add(recipes, "jungle_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:jungle_planks", "projectflint:bone_knife");
		add(recipes, "jungle_board_from_plank", "projectflint:wooden_board", 1, "minecraft:jungle_planks", "projectflint:flint_file");
		add(recipes, "jungle_door_hinged", "minecraft:jungle_door", 1, "projectflint:wooden_door_hinge", "minecraft:jungle_planks#3");
		add(recipes, "acacia_planks_from_log", "minecraft:acacia_planks", 2, "minecraft:acacia_log", "minecraft:wooden_axe");
		add(recipes, "acacia_planks_from_log_hardwood_axe", "minecraft:acacia_planks", 2, "minecraft:acacia_log", "projectflint:hardwood_axe");
		add(recipes, "acacia_planks_from_stripped_log", "minecraft:acacia_planks", 2, "minecraft:stripped_acacia_log", "minecraft:wooden_axe");
		add(recipes, "acacia_planks_from_stripped_log_hardwood_axe", "minecraft:acacia_planks", 2, "minecraft:stripped_acacia_log", "projectflint:hardwood_axe");
		add(recipes, "acacia_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:acacia_planks", "projectflint:bone_knife");
		add(recipes, "acacia_board_from_plank", "projectflint:wooden_board", 1, "minecraft:acacia_planks", "projectflint:flint_file");
		add(recipes, "acacia_door_hinged", "minecraft:acacia_door", 1, "projectflint:wooden_door_hinge", "minecraft:acacia_planks#3");
		add(recipes, "dark_oak_planks_from_log", "minecraft:dark_oak_planks", 2, "minecraft:dark_oak_log", "minecraft:wooden_axe");
		add(recipes, "dark_oak_planks_from_log_hardwood_axe", "minecraft:dark_oak_planks", 2, "minecraft:dark_oak_log", "projectflint:hardwood_axe");
		add(recipes, "dark_oak_planks_from_stripped_log", "minecraft:dark_oak_planks", 2, "minecraft:stripped_dark_oak_log", "minecraft:wooden_axe");
		add(recipes, "dark_oak_planks_from_stripped_log_hardwood_axe", "minecraft:dark_oak_planks", 2, "minecraft:stripped_dark_oak_log", "projectflint:hardwood_axe");
		add(recipes, "dark_oak_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:dark_oak_planks", "projectflint:bone_knife");
		add(recipes, "dark_oak_board_from_plank", "projectflint:wooden_board", 1, "minecraft:dark_oak_planks", "projectflint:flint_file");
		add(recipes, "dark_oak_door_hinged", "minecraft:dark_oak_door", 1, "projectflint:wooden_door_hinge", "minecraft:dark_oak_planks#3");
		add(recipes, "mangrove_planks_from_log", "minecraft:mangrove_planks", 2, "minecraft:mangrove_log", "minecraft:wooden_axe");
		add(recipes, "mangrove_planks_from_log_hardwood_axe", "minecraft:mangrove_planks", 2, "minecraft:mangrove_log", "projectflint:hardwood_axe");
		add(recipes, "mangrove_planks_from_stripped_log", "minecraft:mangrove_planks", 2, "minecraft:stripped_mangrove_log", "minecraft:wooden_axe");
		add(recipes, "mangrove_planks_from_stripped_log_hardwood_axe", "minecraft:mangrove_planks", 2, "minecraft:stripped_mangrove_log", "projectflint:hardwood_axe");
		add(recipes, "mangrove_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:mangrove_planks", "projectflint:bone_knife");
		add(recipes, "mangrove_board_from_plank", "projectflint:wooden_board", 1, "minecraft:mangrove_planks", "projectflint:flint_file");
		add(recipes, "mangrove_door_hinged", "minecraft:mangrove_door", 1, "projectflint:wooden_door_hinge", "minecraft:mangrove_planks#3");
		add(recipes, "cherry_planks_from_log", "minecraft:cherry_planks", 2, "minecraft:cherry_log", "minecraft:wooden_axe");
		add(recipes, "cherry_planks_from_log_hardwood_axe", "minecraft:cherry_planks", 2, "minecraft:cherry_log", "projectflint:hardwood_axe");
		add(recipes, "cherry_planks_from_stripped_log", "minecraft:cherry_planks", 2, "minecraft:stripped_cherry_log", "minecraft:wooden_axe");
		add(recipes, "cherry_planks_from_stripped_log_hardwood_axe", "minecraft:cherry_planks", 2, "minecraft:stripped_cherry_log", "projectflint:hardwood_axe");
		add(recipes, "cherry_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:cherry_planks", "projectflint:bone_knife");
		add(recipes, "cherry_board_from_plank", "projectflint:wooden_board", 1, "minecraft:cherry_planks", "projectflint:flint_file");
		add(recipes, "cherry_door_hinged", "minecraft:cherry_door", 1, "projectflint:wooden_door_hinge", "minecraft:cherry_planks#3");
		add(recipes, "pale_oak_planks_from_log", "minecraft:pale_oak_planks", 2, "minecraft:pale_oak_log", "minecraft:wooden_axe");
		add(recipes, "pale_oak_planks_from_log_hardwood_axe", "minecraft:pale_oak_planks", 2, "minecraft:pale_oak_log", "projectflint:hardwood_axe");
		add(recipes, "pale_oak_planks_from_stripped_log", "minecraft:pale_oak_planks", 2, "minecraft:stripped_pale_oak_log", "minecraft:wooden_axe");
		add(recipes, "pale_oak_planks_from_stripped_log_hardwood_axe", "minecraft:pale_oak_planks", 2, "minecraft:stripped_pale_oak_log", "projectflint:hardwood_axe");
		add(recipes, "pale_oak_sheet_from_plank", "projectflint:wooden_sheet", 3, "minecraft:pale_oak_planks", "projectflint:bone_knife");
		add(recipes, "pale_oak_board_from_plank", "projectflint:wooden_board", 1, "minecraft:pale_oak_planks", "projectflint:flint_file");
		add(recipes, "pale_oak_door_hinged", "minecraft:pale_oak_door", 1, "projectflint:wooden_door_hinge", "minecraft:pale_oak_planks#3");
		add(recipes, "sticks_from_sheet", "minecraft:stick", 4, "projectflint:wooden_sheet");
		add(recipes, "sand_from_flakes", "minecraft:sand", 1, "projectflint:sand_flake#4");
		add(recipes, "clay_block_from_clay_balls", "minecraft:clay", 1, "minecraft:clay_ball#4");
		add(recipes, "dirt_from_loose_soil", "minecraft:dirt", 1, "projectflint:loose_soil");
		add(recipes, "loose_soil_from_dirt", "projectflint:loose_soil", 1, "minecraft:dirt");
		add(recipes, "dirt_piles_from_loose_soil", "projectflint:dirt_pile", 4, "projectflint:loose_soil", "minecraft:wooden_shovel");
		add(recipes, "fat_burning_torch", "projectflint:fat_burning_torch", 1, "minecraft:stick", "minecraft:string", "minecraft:charcoal");
		// Special repair recipe: any damaged leather/tanned leather armor with one Repair Kit.
		add(recipes, "repair_leather_armor", "minecraft:leather_chestplate", 1, "projectflint:repair_kit", "minecraft:leather_chestplate");
		return Collections.unmodifiableList(recipes);
	}
}
