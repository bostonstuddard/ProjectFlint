package dev.projectflint.mixin;

import dev.projectflint.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/** Adds consistent survival-stat lore to all foods and backpack utility items. */
@Mixin(Item.class)
public abstract class ItemTooltipMixin {
	@Inject(method = "appendHoverText", at = @At("TAIL"))
	private void projectflint$appendSurvivalStats(
		ItemStack stack,
		Item.TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> textConsumer,
		TooltipFlag flag,
		CallbackInfo ci
	) {
		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food != null && !(stack.getItem() instanceof net.minecraft.world.item.MobBucketItem)) {
			textConsumer.accept(Component.literal(
				"Hunger: " + formatHalf(food.nutrition() / 2.0F)
			).withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal(
				"Saturation: " + formatHalf(food.saturation() / 2.0F)
			).withStyle(ChatFormatting.GRAY));
		}

		if (stack.is(ModItems.REED_BACKPACK)) {
			textConsumer.accept(Component.literal("Unlocks 9 inventory slots.").withStyle(ChatFormatting.GRAY));
		}
		if (stack.is(ModItems.LEATHER_BACKPACK)) {
			textConsumer.accept(Component.literal("Unlocks 13 inventory slots.").withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal("Hunger Drain: 1.05x while worn").withStyle(ChatFormatting.GRAY));
		}
		if (stack.is(ModItems.LEATHER_BACKPACK_WITH_CHEST)) {
			textConsumer.accept(Component.literal("Unlocks 18 inventory slots.").withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal("Hunger Drain: 1.10x while worn").withStyle(ChatFormatting.GRAY));
		}
		if (stack.is(ModItems.TANNED_UTILITY_VEST)) {
			textConsumer.accept(Component.literal("Unlocks 22 inventory slots.").withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal("Clear vegetation by hand.").withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal("Unlocks personal crafting and recipe book.").withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal("Hunger Drain: 1.10x while worn").withStyle(ChatFormatting.GRAY));
		}

	}

	private static String formatHalf(float value) {
		float rounded = Math.round(value * 10.0F) / 10.0F;
		if (Math.abs(rounded - Math.round(rounded)) < 0.001F) {
			return Integer.toString(Math.round(rounded));
		}
		return Float.toString(rounded);
	}
}
