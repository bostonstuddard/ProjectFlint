package dev.projectflint.recipe;

import dev.projectflint.item.RecipeParchmentItem;
import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Single source of truth for Bookshelf workstation recipes and four-book research gating. */
public final class BookshelfRecipeRegistry {
	private BookshelfRecipeRegistry() {}

	private static final List<Definition> RECIPES = List.of(
		recipe(Ingredient.of(ModItems.SUGARCANE_ROLL), 8, Ingredient.of(Items.LEATHER), 2, new ItemStack(Items.BOOK), 0),
		recipe(Ingredient.of(Items.BOOK), 1, Ingredient.of(Items.BLACK_DYE), 2, new ItemStack(Items.WRITABLE_BOOK), 4),
		parchment(Ingredient.of(Items.SUGAR_CANE), 16, "projectflint:sugar"),
		parchment(Ingredient.of(Items.SWEET_BERRIES), 16, "projectflint:sweet_berry_jam"),
		parchment(Ingredient.of(Items.SWEET_BERRIES), 16, "projectflint:sweet_berry_pie"),
		parchment(Ingredient.of(ModItems.STRAWBERRY), 16, "projectflint:strawberry_jam"),
		parchment(Ingredient.of(ModItems.STRAWBERRY), 16, "projectflint:strawberry_pie"),
		parchment(Ingredient.of(Items.MELON_SLICE), 16, "projectflint:melon_pie"),
		parchment(Ingredient.of(Items.COOKED_MUTTON), 16, "projectflint:mutton_pie"),
		parchment(Ingredient.of(ModItems.PUMPKIN_SLICE), 16, "projectflint:pumpkin_cake"),
		parchment(Ingredient.of(ModItems.PUMPKIN_SLICE), 16, "projectflint:pumpkin_pie"),
		parchment(Ingredient.of(Items.APPLE), 16, "projectflint:apples_n_pie"),
		parchment(Ingredient.of(ModItems.FAT), 8, "projectflint:fat"),
		parchment(Ingredient.of(ModItems.FLOUR), 8, "projectflint:dough"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:sweet_dough"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:bread"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:rustic_bread"),
		parchment(Ingredient.of(ModItems.SWEET_DOUGH), 8, "projectflint:sweet_bread"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:meat_bread"),
		parchment(Ingredient.of(ModItems.SWEET_DOUGH), 8, "projectflint:pumpkin_bread"),
		parchment(Ingredient.of(ModItems.SWEET_DOUGH), 8, "projectflint:apple_bread"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:cheese_bread"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:egg_bread"),
		parchment(Ingredient.of(ModItems.SWEET_DOUGH), 8, "projectflint:breakfast_bread"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:flatbread"),
		parchment(Ingredient.of(ModItems.DOUGH), 8, "projectflint:french_bread"),
		parchment(Ingredient.of(ModItems.BACON_AND_EGGS), 1, "projectflint:scrambled_egg_oven"),
		parchment(Ingredient.of(ModItems.RAW_CALAMARI), 8, "projectflint:sushi_roll"),
		parchment(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 4, "projectflint:cheese"),
		parchment(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 4, "projectflint:butter"),
		parchment(Ingredient.of(Items.COOKED_BEEF), 8, "projectflint:beef_stew"),
		parchment(Ingredient.of(Items.COOKED_PORKCHOP), 8, RecipeParchmentItem.UNFINISHED_BACON_RECIPE),
		stagedParchment(Ingredient.of(ModItems.SCRAMBLED_EGG), 8, RecipeParchmentItem.UNFINISHED_BACON_RECIPE, "projectflint:bacon_and_eggs"),
		parchment(Ingredient.of(ModItems.FILED_BONE), 6, "projectflint:bone_broth")
	);

	private static Definition recipe(Ingredient a, int ca, Ingredient b, int cb, ItemStack result, int books) {
		return new Definition(a, ca, null, b, cb, null, result, books);
	}

	/** A null parchment constraint means normal ingredient; empty means blank parchment; non-empty means that exact bound recipe id. */
	private static Definition parchment(Ingredient material, int count, String recipeId) {
		return new Definition(material, count, null, Ingredient.of(ModItems.RECIPE_PARCHMENT), 1, "", RecipeParchmentItem.boundStack(recipeId), 4);
	}

	private static Definition stagedParchment(Ingredient material, int count, String requiredRecipeId, String resultRecipeId) {
		return new Definition(material, count, null, Ingredient.of(ModItems.RECIPE_PARCHMENT), 1, requiredRecipeId, RecipeParchmentItem.boundStack(resultRecipeId), 4);
	}

	public static List<Definition> recipes() {
		return RECIPES;
	}

	public static List<Match> findAll(ItemStack slot0, ItemStack slot1, int booksStored) {
		List<Match> matches = new ArrayList<>();
		for (Definition recipe : RECIPES) {
			if (!recipe.unlocked(booksStored)) continue;
			if (recipe.matches(slot0, slot1)) {
				matches.add(new Match(recipe.countA(), recipe.countB(), recipe.result().copy(), recipe.requiredBooks()));
			} else if (recipe.swappedMatches(slot0, slot1)) {
				matches.add(new Match(recipe.countB(), recipe.countA(), recipe.result().copy(), recipe.requiredBooks()));
			}
		}
		return matches;
	}

	public static Optional<Match> find(ItemStack slot0, ItemStack slot1, int booksStored) {
		List<Match> matches = findAll(slot0, slot1, booksStored);
		return matches.isEmpty() ? Optional.empty() : Optional.of(matches.getFirst());
	}

	public record Definition(
		Ingredient inputA, int countA, String parchmentRecipeA,
		Ingredient inputB, int countB, String parchmentRecipeB,
		ItemStack result, int requiredBooks
	) {
		public boolean unlocked(int booksStored) { return booksStored >= requiredBooks; }

		private static boolean inputMatches(Ingredient ingredient, int count, String parchmentRecipe, ItemStack stack) {
			if (stack.isEmpty() || stack.getCount() < count || !ingredient.test(stack)) return false;
			if (parchmentRecipe == null) return true;
			Optional<String> bound = RecipeParchmentItem.recipeId(stack);
			return parchmentRecipe.isEmpty() ? bound.isEmpty() : bound.filter(parchmentRecipe::equals).isPresent();
		}

		private boolean matches(ItemStack a, ItemStack b) {
			return inputMatches(inputA, countA, parchmentRecipeA, a) && inputMatches(inputB, countB, parchmentRecipeB, b);
		}

		private boolean swappedMatches(ItemStack a, ItemStack b) {
			return inputMatches(inputB, countB, parchmentRecipeB, a) && inputMatches(inputA, countA, parchmentRecipeA, b);
		}
	}

	public record Match(int countA, int countB, ItemStack result, int requiredBooks) {}
}
