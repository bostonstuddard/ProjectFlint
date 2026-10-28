package dev.projectflint.system;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Applies the intended early-game combat values to ordinary vanilla Wooden Axes. */
public final class WoodenAxeBalanceSystem {
	private static final ItemAttributeModifiers ATTRIBUTES = ItemAttributeModifiers.builder()
		.add(Attributes.ATTACK_DAMAGE,
			new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.5D, AttributeModifier.Operation.ADD_VALUE),
			EquipmentSlotGroup.MAINHAND)
		.add(Attributes.ATTACK_SPEED,
			new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2D, AttributeModifier.Operation.ADD_VALUE),
			EquipmentSlotGroup.MAINHAND)
		.build();

	private WoodenAxeBalanceSystem() {}

	public static void normalize(ItemStack stack) {
		// An explicitly customized item keeps its own attributes; only the vanilla default is replaced.
		if (stack.is(Items.WOODEN_AXE) && !stack.hasNonDefault(DataComponents.ATTRIBUTE_MODIFIERS))
			stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ATTRIBUTES);
	}
}
