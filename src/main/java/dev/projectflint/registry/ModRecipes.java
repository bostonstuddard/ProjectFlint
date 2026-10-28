package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.recipe.ClayGrinderRecipe;
import dev.projectflint.recipe.DurableCraftingRecipe;
import dev.projectflint.recipe.LeatherRepairRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
	public static final RecipeType<ClayGrinderRecipe> CLAY_GRINDING = Registry.register(
		BuiltInRegistries.RECIPE_TYPE,
		ProjectFlint.id("clay_grinding"),
		new RecipeType<ClayGrinderRecipe>() {
			@Override
			public String toString() {
				return ProjectFlint.MOD_ID + ":clay_grinding";
			}
		}
	);

	public static final RecipeSerializer<ClayGrinderRecipe> CLAY_GRINDING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER,
		ProjectFlint.id("clay_grinding"),
		new ClayGrinderRecipe.Serializer()
	);

	public static final RecipeSerializer<DurableCraftingRecipe> DURABLE_CRAFTING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER, ProjectFlint.id("durable_crafting"), new DurableCraftingRecipe.Serializer());
	public static final RecipeSerializer<LeatherRepairRecipe> LEATHER_REPAIR_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER, ProjectFlint.id("leather_repair"), new LeatherRepairRecipe.Serializer());

	private ModRecipes() {}

	public static void initialize() {
		// Class loading performs registration.
	}
}
