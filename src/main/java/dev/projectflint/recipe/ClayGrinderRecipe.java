package dev.projectflint.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.projectflint.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** Project Flint-owned Clay Grinder recipe with explicit input-count support. */
public final class ClayGrinderRecipe extends SingleItemRecipe {
	private final int inputCount;

	public ClayGrinderRecipe(String group, Ingredient input, int inputCount, ItemStack result) {
		super(group, input, result);
		this.inputCount = Math.max(1, inputCount);
	}

	public int inputCount() {
		return inputCount;
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return input.item().getCount() >= inputCount && super.matches(input, level);
	}

	@Override
	public RecipeType<ClayGrinderRecipe> getType() {
		return ModRecipes.CLAY_GRINDING;
	}

	@Override
	public RecipeSerializer<ClayGrinderRecipe> getSerializer() {
		return ModRecipes.CLAY_GRINDING_SERIALIZER;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	public static final class Serializer implements RecipeSerializer<ClayGrinderRecipe> {
		private static final MapCodec<ClayGrinderRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.optionalFieldOf("group", "").forGetter(ClayGrinderRecipe::group),
			Ingredient.CODEC.fieldOf("ingredient").forGetter(ClayGrinderRecipe::input),
			Codec.INT.optionalFieldOf("input_count", 1).forGetter(ClayGrinderRecipe::inputCount),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result().copy())
		).apply(instance, ClayGrinderRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, ClayGrinderRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, ClayGrinderRecipe::group,
			Ingredient.CONTENTS_STREAM_CODEC, ClayGrinderRecipe::input,
			ByteBufCodecs.VAR_INT, ClayGrinderRecipe::inputCount,
			ItemStack.STREAM_CODEC, recipe -> recipe.result().copy(),
			ClayGrinderRecipe::new
		);

		@Override
		public MapCodec<ClayGrinderRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ClayGrinderRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
