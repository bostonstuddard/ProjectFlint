package dev.projectflint.system;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Set;

public record PrimitiveProcessingRecipe(
	String id,
	Ingredient input,
	ItemStack output,
	int processingTicks,
	Set<PrimitiveProcessor> processors
) {
	public boolean matches(PrimitiveProcessor processor, ItemStack stack) {
		return processors.contains(processor) && input.test(stack);
	}

	public ItemStack resultCopy() {
		return output.copy();
	}
}
