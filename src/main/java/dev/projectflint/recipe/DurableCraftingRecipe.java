package dev.projectflint.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.projectflint.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

/** A normal recipe-book-visible craft that wears, rather than consumes, its one tool. */
public final class DurableCraftingRecipe implements CraftingRecipe {
	private final ShapelessRecipe delegate;
	private final String group;
	private final CraftingBookCategory category;
	private final ItemStack resultStack;
	private final List<Ingredient> materials;
	private final Ingredient tool;

	public DurableCraftingRecipe(String group, CraftingBookCategory category, ItemStack result, List<Ingredient> materials, Ingredient tool) {
		this.delegate = new ShapelessRecipe(group, category, result, withTool(materials, tool));
		this.group = group;
		this.category = category;
		this.resultStack = result.copy();
		this.materials = List.copyOf(materials);
		this.tool = tool;
	}

	private static List<Ingredient> withTool(List<Ingredient> materials, Ingredient tool) {
		List<Ingredient> all = new ArrayList<>(materials);
		all.add(tool);
		return all;
	}

	@Override public String group() { return group; }
	@Override public CraftingBookCategory category() { return category; }
	@Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
	@Override public PlacementInfo placementInfo() { return delegate.placementInfo(); }
	@Override public List<RecipeDisplay> display() { return delegate.display(); }
	@Override public boolean matches(CraftingInput input, Level level) { return delegate.matches(input, level); }
	@Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return resultStack.copy(); }
	public ItemStack result() { return resultStack.copy(); }
	public List<Ingredient> materials() { return materials; }
	public Ingredient tool() { return tool; }

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remains = CraftingRecipe.defaultCraftingReminder(input);
		for (int index = 0; index < input.size(); index++) {
			ItemStack stack = input.getItem(index);
			if (!stack.isEmpty() && tool.test(stack) && stack.isDamageableItem()) {
				ItemStack worn = stack.copyWithCount(1);
				worn.setDamageValue(worn.getDamageValue() + 1);
				remains.set(index, worn.getDamageValue() >= worn.getMaxDamage() ? ItemStack.EMPTY : worn);
				break;
			}
		}
		return remains;
	}

	@Override public RecipeSerializer<DurableCraftingRecipe> getSerializer() { return ModRecipes.DURABLE_CRAFTING_SERIALIZER; }

	public static final class Serializer implements RecipeSerializer<DurableCraftingRecipe> {
		private static final MapCodec<DurableCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.optionalFieldOf("group", "").forGetter(DurableCraftingRecipe::group),
			CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(DurableCraftingRecipe::category),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DurableCraftingRecipe::result),
			Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(DurableCraftingRecipe::materials),
			Ingredient.CODEC.fieldOf("tool").forGetter(DurableCraftingRecipe::tool)
		).apply(instance, DurableCraftingRecipe::new));
		private static final StreamCodec<RegistryFriendlyByteBuf, DurableCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, DurableCraftingRecipe::group,
			CraftingBookCategory.STREAM_CODEC, DurableCraftingRecipe::category,
			ItemStack.STREAM_CODEC, DurableCraftingRecipe::result,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), DurableCraftingRecipe::materials,
			Ingredient.CONTENTS_STREAM_CODEC, DurableCraftingRecipe::tool,
			DurableCraftingRecipe::new
		);
		@Override public MapCodec<DurableCraftingRecipe> codec() { return CODEC; }
		@Override public StreamCodec<RegistryFriendlyByteBuf, DurableCraftingRecipe> streamCodec() { return STREAM_CODEC; }
	}
}
