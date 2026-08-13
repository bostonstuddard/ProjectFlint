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
		if (food != null) {
			textConsumer.accept(Component.literal(
				"Hunger: " + formatHalf(food.nutrition() / 2.0F)
			).withStyle(ChatFormatting.GRAY));
			textConsumer.accept(Component.literal(
				"Saturation: " + formatHalf(food.saturation() / 2.0F)
			).withStyle(ChatFormatting.GRAY));
		}

		if (stack.is(ModItems.LEATHER_BACKPACK)) {
			textConsumer.accept(Component.literal("Hunger Drain: 1.05x while worn").withStyle(ChatFormatting.GRAY));
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
