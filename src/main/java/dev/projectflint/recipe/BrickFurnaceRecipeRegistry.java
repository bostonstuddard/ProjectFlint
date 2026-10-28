package dev.projectflint.recipe;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.PrimitiveProcessingRecipe;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Set;

/** Exclusive Brick Furnace material conversion; not part of primitive kiln/campfire pool. */
public final class BrickFurnaceRecipeRegistry {
	private BrickFurnaceRecipeRegistry() {}

	public static final PrimitiveProcessingRecipe TANNED_LEATHER = new PrimitiveProcessingRecipe(
		"tanned_leather", Ingredient.of(Items.LEATHER), new ItemStack(ModItems.TANNED_LEATHER),
		GameplayTuning.KILN_MEAT_COOK_TICKS, Set.of(PrimitiveProcessor.KILN)
	);
	public static final PrimitiveProcessingRecipe GLASS = new PrimitiveProcessingRecipe(
		"glass", Ingredient.of(Items.SAND), new ItemStack(Items.GLASS),
		GameplayTuning.KILN_MEAT_COOK_TICKS * 2, Set.of(PrimitiveProcessor.KILN)
	);
}
