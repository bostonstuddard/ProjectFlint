package dev.projectflint.recipe;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/** Single source of truth for Wooden Grinder two-input recipes. */
public final class WoodenGrinderRecipeRegistry {
	private WoodenGrinderRecipeRegistry() {}

	private static final List<Definition> RECIPES = List.of(
		recipe(Ingredient.of(ModItems.RAW_LEATHER, Items.LEATHER), 2, Ingredient.of(ModItems.SANDING_PAPER), 1, new ItemStack(ModItems.GROUND_LEATHER)),
		recipe(Ingredient.of(Items.LEATHER), 8, Ingredient.of(ModItems.WOVEN_WOOD), 8, new ItemStack(ModItems.WOVEN_CHEST_FRAME)),
		recipe(Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.REED_BACKPACK), 1, new ItemStack(ModItems.LEATHER_BACKPACK)),
		recipe(Ingredient.of(ModItems.WATTLED_DAUB_BRICKS), 2, Ingredient.of(ModItems.ADVANCED_KILN_GLASS), 1, new ItemStack(ModItems.WATTLED_DAUB_WINDOW)),
		recipe(Ingredient.of(ModItems.SUGARCANE_ROLL), 1, Ingredient.of(ModItems.PLANT_TOOL_BINDING), 1, new ItemStack(ModItems.WOVEN_FAN, 2)),
		recipe(Ingredient.of(ModItems.WOVEN_FAN), 1, Ingredient.of(ModItems.WATTLE), 1, new ItemStack(ModItems.TIED_WATTLE, 3)),
		recipe(Ingredient.of(ModItems.FLINT_CHIP), 1, Ingredient.of(ModItems.MUD_PILE), 1, new ItemStack(ModItems.DAUB_PILE, 3)),
		recipe(Ingredient.of(ModItems.TIED_WATTLE), 4, Ingredient.of(ModItems.WET_DAUB_PILE), 2, new ItemStack(ModItems.UNFIRED_WATTLE_AND_DAUB, 4)),
		recipe(Ingredient.of(ModItems.SUPER_HEATED_CLAY), 24, Ingredient.of(ModItems.MUD_PILE), 16, new ItemStack(ModItems.TORCH_HOLDER)),
		recipe(Ingredient.of(Items.FLINT), 16, Ingredient.of(ModItems.WOVEN_WOOD), 1, new ItemStack(ModItems.UNLIT_FLINT_TORCH)),
		recipe(Ingredient.of(ModItems.TORCH_HOLDER), 1, Ingredient.of(ModItems.UNLIT_FLINT_TORCH), 1, new ItemStack(ModItems.FLINT_TORCH_HOLDER)),
		recipe(Ingredient.of(ModItems.SANDED_WOOD_SLAB), 8, Ingredient.of(Items.LEATHER), 5, new ItemStack(ModItems.SANDING_STATION)),
		recipe(Ingredient.of(ModItems.WOVEN_WOOD), 16, Ingredient.of(ModItems.CLAY_POT), 1, new ItemStack(ModItems.WATER_BEARING_POT)),
		recipe(Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.CLAY_POT), 1, new ItemStack(ModItems.WATER_POT)),
		recipe(Ingredient.of(ModItems.FLINT_AXE_PIECES), 1, Ingredient.of(Items.LEATHER), 2, new ItemStack(ModItems.BOUND_WOOD_PROCESSOR))
	);

	private static Definition recipe(Ingredient inputA, int countA, Ingredient inputB, int countB, ItemStack result) {
		return new Definition(inputA, countA, inputB, countB, result);
	}

	public static List<Definition> recipes() {
		return RECIPES;
	}

	public static Optional<Match> find(ItemStack slot0, ItemStack slot1) {
		for (Definition recipe : RECIPES) {
			if (recipe.matches(slot0, slot1)) {
				return Optional.of(new Match(recipe.countA(), recipe.countB(), recipe.result().copy()));
			}
			if (recipe.matches(slot1, slot0)) {
				return Optional.of(new Match(recipe.countB(), recipe.countA(), recipe.result().copy()));
			}
		}
		return Optional.empty();
	}

	public record Definition(Ingredient inputA, int countA, Ingredient inputB, int countB, ItemStack result) {
		private boolean matches(ItemStack a, ItemStack b) {
			return !a.isEmpty() && !b.isEmpty()
				&& a.getCount() >= countA && b.getCount() >= countB
				&& inputA.test(a) && inputB.test(b);
		}
	}

	public record Match(int countA, int countB, ItemStack result) {}
}
