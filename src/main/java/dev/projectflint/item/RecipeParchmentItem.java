package dev.projectflint.item;

import dev.projectflint.recipe.PrimitiveStoveRecipeRegistry;
import dev.projectflint.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Immutable single-stack recipe token used by Bookshelf/Oven progression. */
public final class RecipeParchmentItem extends Item {
	private static final String RECIPE_KEY = "ProjectFlintRecipe";
	public static final String UNFINISHED_BACON_RECIPE = "projectflint:bacon_and_eggs_unfinished";

	public RecipeParchmentItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	public static Optional<String> recipeId(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) return Optional.empty();
		String id = data.copyTag().getStringOr(RECIPE_KEY, "");
		return id.isBlank() ? Optional.empty() : Optional.of(id);
	}

	public static boolean isBound(ItemStack stack) {
		return recipeId(stack).isPresent();
	}

	/** Returns false if this parchment was already assigned to any recipe. */
	public static boolean bindRecipe(ItemStack stack, String recipeId) {
		if (recipeId == null || recipeId.isBlank() || isBound(stack)) return false;
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(RECIPE_KEY, recipeId));
		if (UNFINISHED_BACON_RECIPE.equals(recipeId)) {
			stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("incomplete"), List.of()));
		} else {
			stack.remove(DataComponents.CUSTOM_MODEL_DATA);
		}
		return true;
	}

	public static ItemStack boundStack(String recipeId) {
		ItemStack stack = new ItemStack(ModItems.RECIPE_PARCHMENT);
		bindRecipe(stack, recipeId);
		return stack;
	}

	@Override
	public Component getName(ItemStack stack) {
		Optional<String> id = recipeId(stack);
		if (id.isPresent() && UNFINISHED_BACON_RECIPE.equals(id.get())) {
			return Component.literal("Recipe Parchment: Unfinished Bacon Recipe");
		}

		Optional<PrimitiveStoveRecipeRegistry.Definition> boundRecipe = id.flatMap(PrimitiveStoveRecipeRegistry::findBound);
		if (boundRecipe.isEmpty()) return super.getName(stack);

		return Component.literal("Recipe Parchment: ")
			.append(boundRecipe.get().result().getHoverName());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> text, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, text, flag);
		Optional<String> id = recipeId(stack);
		if (id.isEmpty()) {
			text.accept(Component.literal("Blank recipe parchment").withStyle(ChatFormatting.GRAY));
			return;
		}

		if (UNFINISHED_BACON_RECIPE.equals(id.get())) {
			text.accept(Component.literal("Research step:").withStyle(ChatFormatting.GOLD));
			text.accept(Component.literal("  8x Scrambled Egg").withStyle(ChatFormatting.GRAY));
			text.accept(Component.literal("Finish at a 4/4 Bookshelf").withStyle(ChatFormatting.DARK_GRAY));
			return;
		}

		Optional<PrimitiveStoveRecipeRegistry.Definition> found = PrimitiveStoveRecipeRegistry.findBound(id.get());
		if (found.isEmpty()) {
			text.accept(Component.literal("Recipe: " + id.get()).withStyle(ChatFormatting.GRAY));
			return;
		}

		PrimitiveStoveRecipeRegistry.Definition recipe = found.get();
		text.accept(Component.literal("Makes: ").withStyle(ChatFormatting.GRAY).append(recipe.result().getHoverName()));
		text.accept(Component.literal("Ingredients:").withStyle(ChatFormatting.GOLD));
		for (PrimitiveStoveRecipeRegistry.Requirement requirement : recipe.requirements()) {
			if ("projectflint:fat".equals(recipe.id())) {
				text.accept(Component.literal("  any 3 raw meat").withStyle(ChatFormatting.GRAY));
				continue;
			}
			String names = requirement.ingredient().items()
				.map(holder -> new ItemStack(holder.value()).getHoverName().getString())
				.distinct()
				.collect(Collectors.joining(" or "));
			if (names.isBlank()) names = "Unknown ingredient";
			text.accept(Component.literal("  " + requirement.count() + "x " + names).withStyle(ChatFormatting.GRAY));
		}
	}
}
