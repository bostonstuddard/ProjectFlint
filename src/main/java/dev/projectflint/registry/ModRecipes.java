package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.recipe.ClayGrinderRecipe;
import dev.projectflint.recipe.DurableCraftingRecipe;
import dev.projectflint.recipe.HardwoodCraftingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
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

	/** Separate recipe type prevents hardwood crafts appearing as vanilla crafting recipes in EMI. */
    public static final RecipeType<CraftingRecipe> HARDWOOD_CRAFTING = Registry.register(
        BuiltInRegistries.RECIPE_TYPE, ProjectFlint.id("hardwood_crafting"),
        new RecipeType<CraftingRecipe>() {
            @Override public String toString() { return "projectflint:hardwood_crafting"; }
        });
    public static final RecipeSerializer<HardwoodCraftingRecipe> HARDWOOD_SHAPED_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER, ProjectFlint.id("hardwood_shaped"),
        new HardwoodCraftingRecipe.Serializer<>(RecipeSerializer.SHAPED_RECIPE));
    public static final RecipeSerializer<HardwoodCraftingRecipe> HARDWOOD_SHAPELESS_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER, ProjectFlint.id("hardwood_shapeless"),
        new HardwoodCraftingRecipe.Serializer<>(RecipeSerializer.SHAPELESS_RECIPE));
    public static final RecipeSerializer<HardwoodCraftingRecipe> HARDWOOD_DURABLE_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER, ProjectFlint.id("hardwood_durable"),
        new HardwoodCraftingRecipe.Serializer<>(DURABLE_CRAFTING_SERIALIZER));

	private ModRecipes() {}

	public static void initialize() {
		// Class loading performs registration.
	}
}
