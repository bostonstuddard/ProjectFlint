package dev.projectflint.recipe;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.PrimitiveProcessingRecipe;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/** Single source of truth for parchment-bound Primitive Oven recipes. */
public final class PrimitiveStoveRecipeRegistry {
	private PrimitiveStoveRecipeRegistry() {}

	private static final int FOOD_TICKS = GameplayTuning.KILN_MEAT_COOK_TICKS;

	private static final List<Definition> PARCHMENT_RECIPES = List.of(
		recipe("projectflint:sugar", new ItemStack(Items.SUGAR), req(Ingredient.of(Items.SUGAR_CANE), 4)),
		recipe("projectflint:sweet_berry_jam", new ItemStack(ModItems.SWEET_BERRY_JAM, 2),
			req(Ingredient.of(Items.SWEET_BERRIES), 8), req(Ingredient.of(Items.SUGAR), 1), req(Ingredient.of(ModItems.WOODEN_CUP), 2)),
		recipe("projectflint:sweet_berry_pie", new ItemStack(ModItems.SWEET_BERRY_PIE, 2),
			req(Ingredient.of(ModItems.SWEET_BERRY_JAM), 2), req(Ingredient.of(Items.SUGAR), 1), req(Ingredient.of(ModItems.BUTTER), 1)),
		recipe("projectflint:strawberry_jam", new ItemStack(ModItems.STRAWBERRY_JAM, 2),
			req(Ingredient.of(ModItems.STRAWBERRY), 12), req(Ingredient.of(Items.SUGAR), 1), req(Ingredient.of(ModItems.WOODEN_CUP), 2)),
		recipe("projectflint:strawberry_pie", new ItemStack(ModItems.STRAWBERRY_PIE, 2),
			req(Ingredient.of(ModItems.STRAWBERRY_JAM), 2), req(Ingredient.of(ModItems.STRAWBERRY), 2), req(Ingredient.of(Items.SUGAR), 1), req(Ingredient.of(ModItems.BUTTER), 1)),
		recipe("projectflint:fat", new ItemStack(ModItems.FAT, 5),
			req(WoodenGrinderRecipeRegistry.rawMeat(), 3)),
		// Mixing/baking is a parchment-researched Oven process, not a new vanilla crafting-table recipe.
		recipe("projectflint:dough", new ItemStack(ModItems.DOUGH),
			req(Ingredient.of(ModItems.FLOUR), 2), req(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 1)),
		recipe("projectflint:sweet_dough", new ItemStack(ModItems.SWEET_DOUGH),
			req(Ingredient.of(ModItems.DOUGH), 1), req(Ingredient.of(Items.SUGAR), 1)),
		recipe("projectflint:bread", new ItemStack(Items.BREAD),
			req(Ingredient.of(ModItems.DOUGH), 2), req(Ingredient.of(Items.SUGAR), 1)),
		recipe("projectflint:rustic_bread", new ItemStack(ModItems.RUSTIC_BREAD),
			req(Ingredient.of(ModItems.DOUGH), 1)),
		recipe("projectflint:sweet_bread", new ItemStack(ModItems.SWEET_BREAD),
			req(Ingredient.of(ModItems.SWEET_DOUGH), 1)),
		recipe("projectflint:flatbread", new ItemStack(ModItems.FLATBREAD, 2),
			req(Ingredient.of(ModItems.DOUGH), 2), req(Ingredient.of(ModItems.FAT), 1)),
		recipe("projectflint:french_bread", new ItemStack(ModItems.FRENCH_BREAD, 2),
			req(Ingredient.of(ModItems.DOUGH), 3), req(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 1)),
		recipe("projectflint:meat_bread", new ItemStack(ModItems.MEAT_BREAD, 2),
			req(Ingredient.of(ModItems.DOUGH), 2), req(WoodenGrinderRecipeRegistry.cookedMeat(), 2), req(Ingredient.of(ModItems.FAT), 1)),
		recipe("projectflint:cheese_bread", new ItemStack(ModItems.CHEESE_BREAD, 2),
			req(Ingredient.of(ModItems.DOUGH), 2), req(Ingredient.of(ModItems.CHEESE), 1)),
		recipe("projectflint:egg_bread", new ItemStack(ModItems.EGG_BREAD, 2),
			req(Ingredient.of(ModItems.DOUGH), 2), req(Ingredient.of(ModItems.SCRAMBLED_EGG), 2)),
		recipe("projectflint:breakfast_bread", new ItemStack(ModItems.BREAKFAST_BREAD, 2),
			req(Ingredient.of(ModItems.SWEET_DOUGH), 2), req(Ingredient.of(ModItems.SCRAMBLED_EGG), 2), req(Ingredient.of(ModItems.BUTTER), 1)),
		recipe("projectflint:apple_bread", new ItemStack(ModItems.APPLE_BREAD, 2),
			req(Ingredient.of(ModItems.SWEET_DOUGH), 2), req(Ingredient.of(Items.APPLE), 2)),
		recipe("projectflint:pumpkin_bread", new ItemStack(ModItems.PUMPKIN_BREAD, 2),
			req(Ingredient.of(ModItems.SWEET_DOUGH), 2), req(Ingredient.of(ModItems.PUMPKIN_SLICE), 4)),
		recipe("projectflint:scrambled_egg_oven", new ItemStack(ModItems.SCRAMBLED_EGG, 3),
			req(Ingredient.of(Items.EGG), 1)),
		recipe("projectflint:sushi_roll", new ItemStack(ModItems.SUSHI_ROLL, 5),
			req(Ingredient.of(ModItems.RAW_CALAMARI), 2), req(Ingredient.of(ModItems.FLOUR), 2), req(Ingredient.of(Items.SUGAR), 1)),
		recipe("projectflint:cheese", new ItemStack(ModItems.CHEESE),
			req(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 2), req(Ingredient.of(ModItems.FAT), 1)),
		recipe("projectflint:pumpkin_pie", new ItemStack(Items.PUMPKIN_PIE, 2),
			req(Ingredient.of(ModItems.PUMPKIN_SLICE), 8), req(Ingredient.of(Items.SUGAR), 2)),
		recipe("projectflint:melon_pie", new ItemStack(ModItems.MELON_PIE, 2),
			req(Ingredient.of(Items.MELON_SLICE), 8), req(Ingredient.of(Items.SUGAR), 2)),
		recipe("projectflint:mutton_pie", new ItemStack(ModItems.MUTTON_PIE, 2),
			req(Ingredient.of(Items.COOKED_MUTTON), 8), req(Ingredient.of(Items.SUGAR), 2)),
		recipe("projectflint:butter", new ItemStack(ModItems.BUTTER),
			req(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 4), req(Ingredient.of(Items.SUGAR), 1)),
		recipe("projectflint:pumpkin_cake", new ItemStack(ModItems.PUMPKIN_CAKE),
			req(Ingredient.of(Items.PUMPKIN_PIE), 4), req(Ingredient.of(ModItems.BUTTER), 2), req(Ingredient.of(Items.SUGAR), 1)),
		recipe("projectflint:apples_n_pie", new ItemStack(ModItems.APPLES_N_PIE, 3),
			req(Ingredient.of(ModItems.MELON_PIE, Items.PUMPKIN_PIE), 1), req(Ingredient.of(Items.APPLE), 4), req(Ingredient.of(ModItems.WOODEN_CUP), 1)),
		recipe("projectflint:beef_stew", new ItemStack(ModItems.BEEF_STEW, 3),
			req(Ingredient.of(Items.COOKED_BEEF), 2), req(Ingredient.of(ModItems.BUTTER), 1), req(Ingredient.of(Items.SUGAR), 1), req(Ingredient.of(ModItems.WOODEN_CUP), 3)),
		recipe("projectflint:bacon_and_eggs", new ItemStack(ModItems.BACON_AND_EGGS, 3),
			req(Ingredient.of(Items.COOKED_PORKCHOP), 1), req(Ingredient.of(ModItems.SCRAMBLED_EGG), 3), req(Ingredient.of(ModItems.WOODEN_CUP), 1)),
		recipe("projectflint:bone_broth", new ItemStack(ModItems.BONE_BROTH, 4),
			req(Ingredient.of(ModItems.FILED_BONE), 4), req(Ingredient.of(ModItems.BUTTER), 1), req(Ingredient.of(ModItems.WOODEN_CUP), 2))
	);

	public static List<Definition> parchmentRecipes() {
		return PARCHMENT_RECIPES;
	}

	public static Optional<Definition> findBound(String id) {
		if (id == null || id.isBlank()) return Optional.empty();
		String fullId = id.contains(":") ? id : "projectflint:" + id;
		for (Definition recipe : PARCHMENT_RECIPES) {
			if (recipe.id().equals(fullId)) return Optional.of(recipe);
		}

		String normalized = fullId.startsWith("projectflint:") ? fullId.substring("projectflint:".length()) : fullId;
		return PrimitiveProcessingRegistry.recipesFor(PrimitiveProcessor.KILN).stream()
			.filter(recipe -> recipe.id().equals(normalized))
			.findFirst()
			.map(PrimitiveStoveRecipeRegistry::fromPrimitive);
	}

	/** Direct no-parchment mode is intentionally limited to ordinary food/meat kiln recipes. */
	public static Optional<Definition> findDirectFood(ItemStack input) {
		if (input.isEmpty()) return Optional.empty();
		Optional<PrimitiveProcessingRecipe> recipe = PrimitiveProcessingRegistry.find(PrimitiveProcessor.KILN, input);
		if (recipe.isEmpty()) return Optional.empty();
		ItemStack result = recipe.get().resultCopy();
		boolean foodLike = input.get(DataComponents.FOOD) != null || result.get(DataComponents.FOOD) != null || input.is(Items.EGG)
			|| input.is(ModItems.UNFIRED_CLAY_BUCKET) || input.is(ModItems.UNFIRED_BRICK) || input.is(ModItems.UNFIRED_FIRECLAY_BRICK);
		return foodLike ? recipe.map(PrimitiveStoveRecipeRegistry::fromPrimitive) : Optional.empty();
	}

	private static Requirement req(Ingredient ingredient, int count) {
		return new Requirement(ingredient, count);
	}

	private static Definition recipe(String id, ItemStack result, Requirement... requirements) {
		return new Definition(id, List.of(requirements), result, FOOD_TICKS);
	}

	private static Definition fromPrimitive(PrimitiveProcessingRecipe recipe) {
		return new Definition(
			"projectflint:" + recipe.id(),
			List.of(new Requirement(recipe.input(), 1)),
			recipe.resultCopy(),
			Math.max(1, recipe.processingTicks())
		);
	}

	public record Requirement(Ingredient ingredient, int count) {}
	public record Definition(String id, List<Requirement> requirements, ItemStack result, int processingTicks) {}
}
