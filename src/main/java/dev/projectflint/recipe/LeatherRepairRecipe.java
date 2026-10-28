package dev.projectflint.recipe;

import com.mojang.serialization.MapCodec;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** One repair kit restores 32 durability to leather or tanned leather armor. */
public final class LeatherRepairRecipe extends CustomRecipe {
	public LeatherRepairRecipe() { super(CraftingBookCategory.EQUIPMENT); }

	private static boolean isLeatherArmor(ItemStack stack) {
		return stack.is(Items.LEATHER_HELMET) || stack.is(Items.LEATHER_CHESTPLATE)
			|| stack.is(Items.LEATHER_LEGGINGS) || stack.is(Items.LEATHER_BOOTS)
			|| stack.is(ModItems.TANNED_LEATHER_HELMET) || stack.is(ModItems.TANNED_LEATHER_CHESTPLATE)
			|| stack.is(ModItems.TANNED_LEATHER_LEGGINGS) || stack.is(ModItems.TANNED_LEATHER_BOOTS);
	}

	private static ItemStack target(CraftingInput input) {
		ItemStack armor = ItemStack.EMPTY;
		boolean kit = false;
		for (int index = 0; index < input.size(); index++) {
			ItemStack stack = input.getItem(index);
			if (stack.isEmpty()) continue;
			if (stack.is(ModItems.REPAIR_KIT) && !kit) { kit = true; continue; }
			if (isLeatherArmor(stack) && armor.isEmpty() && stack.isDamaged()) { armor = stack; continue; }
			return ItemStack.EMPTY;
		}
		return kit ? armor : ItemStack.EMPTY;
	}

	@Override public boolean matches(CraftingInput input, Level level) { return !target(input).isEmpty(); }
	@Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		ItemStack armor = target(input);
		if (armor.isEmpty()) return ItemStack.EMPTY;
		ItemStack repaired = armor.copyWithCount(1);
		repaired.setDamageValue(Math.max(0, repaired.getDamageValue() - 32));
		return repaired;
	}
	@Override public RecipeSerializer<LeatherRepairRecipe> getSerializer() { return ModRecipes.LEATHER_REPAIR_SERIALIZER; }
	public static final class Serializer implements RecipeSerializer<LeatherRepairRecipe> {
		private static final MapCodec<LeatherRepairRecipe> CODEC = MapCodec.unit(LeatherRepairRecipe::new);
		private static final StreamCodec<RegistryFriendlyByteBuf, LeatherRepairRecipe> STREAM_CODEC = StreamCodec.unit(new LeatherRepairRecipe());
		@Override public MapCodec<LeatherRepairRecipe> codec() { return CODEC; }
		@Override public StreamCodec<RegistryFriendlyByteBuf, LeatherRepairRecipe> streamCodec() { return STREAM_CODEC; }
	}
}
