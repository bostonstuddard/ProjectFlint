package dev.projectflint.recipe;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/** Single source of truth for Sanding Station recipes. Every craft costs one sanding durability. */
public final class SandingStationRecipeRegistry {
	private SandingStationRecipeRegistry() {}

	private static final Ingredient OVERWORLD_WOOD_SLABS = Ingredient.of(
		Items.OAK_SLAB, Items.SPRUCE_SLAB, Items.BIRCH_SLAB, Items.JUNGLE_SLAB,
		Items.ACACIA_SLAB, Items.DARK_OAK_SLAB, Items.MANGROVE_SLAB, Items.CHERRY_SLAB,
		Items.PALE_OAK_SLAB, Items.BAMBOO_SLAB, Items.BAMBOO_MOSAIC_SLAB
	);

	private static final List<Definition> RECIPES = List.of(
		recipe(Ingredient.of(ModItems.PLANT_TOOL_BINDING), 2, new ItemStack(ModItems.WOVEN_FAN, 3)),
		recipe(Ingredient.of(ModItems.GLASS_SHARD), 3, new ItemStack(ModItems.ADVANCED_KILN_GLASS)),
		recipe(Ingredient.of(ModItems.RAW_LEATHER), 3, new ItemStack(Items.LEATHER, 2)),
		recipe(Ingredient.of(ModItems.ANIMAL_BONES), 2, new ItemStack(ModItems.FILED_BONE, 3)),
		recipe(OVERWORLD_WOOD_SLABS, 2, new ItemStack(ModItems.SANDED_WOOD_SLAB, 3)),
		recipe(Ingredient.of(ModItems.WOOL), 3, new ItemStack(ModItems.WOVEN_WOOL, 2)),
		recipe(Ingredient.of(ModItems.SAND_FLAKE), 3, new ItemStack(ModItems.SAND_BALL)),
		recipe(Ingredient.of(ModItems.FLINT_CUTTER_PIECE), 1, new ItemStack(ModItems.FLINT_FILE_HEAD)),
		recipe(Ingredient.of(ModItems.BRANCH), 1, new ItemStack(ModItems.TWIG, 4)),
		recipe(Ingredient.of(Items.STRING), 16, new ItemStack(ModItems.COBWEB)),
		recipe(Ingredient.of(ModItems.COBBLED_LIMESTONE), 8, new ItemStack(ModItems.SMOOTH_LIMESTONE)),
		recipe(Ingredient.of(ModItems.LIMESTONE_BRICKS), 1, new ItemStack(ModItems.POLISHED_LIMESTONE)),
		recipe(Ingredient.of(ModItems.CHECKERED_TILE), 1, new ItemStack(ModItems.GLAZED_CHECKERED_TILE)),
		recipe(Ingredient.of(ModItems.GLAZED_CHECKERED_TILE), 1, new ItemStack(ModItems.CHECKERED_TILE)),
		recipe(Ingredient.of(Items.WRITABLE_BOOK), 1, new ItemStack(ModItems.RECIPE_PARCHMENT)),
		dyeRecipe(Items.DANDELION, Items.YELLOW_DYE, 2),
		dyeRecipe(Items.WILDFLOWERS, Items.YELLOW_DYE, 2),
		dyeRecipe(Items.SUNFLOWER, Items.YELLOW_DYE, 4),
		dyeRecipe(Items.POPPY, Items.RED_DYE, 2),
		dyeRecipe(Items.RED_TULIP, Items.RED_DYE, 2),
		dyeRecipe(Items.ROSE_BUSH, Items.RED_DYE, 4),
		dyeRecipe(Items.BLUE_ORCHID, Items.LIGHT_BLUE_DYE, 2),
		dyeRecipe(Items.ALLIUM, Items.MAGENTA_DYE, 2),
		dyeRecipe(Items.LILAC, Items.MAGENTA_DYE, 4),
		dyeRecipe(Items.AZURE_BLUET, Items.LIGHT_GRAY_DYE, 2),
		dyeRecipe(Items.WHITE_TULIP, Items.LIGHT_GRAY_DYE, 2),
		dyeRecipe(Items.OXEYE_DAISY, Items.LIGHT_GRAY_DYE, 2),
		dyeRecipe(Items.ORANGE_TULIP, Items.ORANGE_DYE, 2),
		dyeRecipe(Items.TORCHFLOWER, Items.ORANGE_DYE, 2),
		dyeRecipe(Items.OPEN_EYEBLOSSOM, Items.ORANGE_DYE, 2),
		dyeRecipe(Items.PINK_TULIP, Items.PINK_DYE, 2),
		dyeRecipe(Items.PINK_PETALS, Items.PINK_DYE, 2),
		dyeRecipe(Items.CACTUS_FLOWER, Items.PINK_DYE, 2),
		dyeRecipe(Items.PEONY, Items.PINK_DYE, 4),
		dyeRecipe(Items.CORNFLOWER, Items.BLUE_DYE, 2),
		dyeRecipe(Items.LILY_OF_THE_VALLEY, Items.WHITE_DYE, 2),
		dyeRecipe(Items.WITHER_ROSE, Items.BLACK_DYE, 2),
		dyeRecipe(Items.CLOSED_EYEBLOSSOM, Items.GRAY_DYE, 2),
		dyeRecipe(Items.PITCHER_PLANT, Items.CYAN_DYE, 4),
		dyeRecipe(Items.INK_SAC, Items.BLACK_DYE, 2)
	);

	private static Definition recipe(Ingredient input, int count, ItemStack result) {
		return new Definition(input, count, result);
	}

	private static Definition dyeRecipe(Item input, Item dye, int count) {
		return recipe(Ingredient.of(input), 1, new ItemStack(dye, count));
	}

	public static List<Definition> recipes() {
		return RECIPES;
	}

	public static Optional<Definition> find(ItemStack input) {
		return RECIPES.stream().filter(recipe -> recipe.matches(input)).findFirst();
	}

	public record Definition(Ingredient input, int count, ItemStack result) {
		public boolean matches(ItemStack stack) {
			return !stack.isEmpty() && stack.getCount() >= count && input.test(stack);
		}
	}
}
