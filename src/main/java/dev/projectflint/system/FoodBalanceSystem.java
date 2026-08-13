package dev.projectflint.system;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

/** Small vanilla food-balance adjustments for Project Flint's survival pacing. */
public final class FoodBalanceSystem {
	private FoodBalanceSystem() {
	}

	public static void initialize() {
		DefaultItemComponentEvents.MODIFY.register(context -> {
			context.modify(
				item -> {
					var id = BuiltInRegistries.ITEM.getKey(item);
					var stack = item.getDefaultInstance();
					return id.getNamespace().equals("minecraft")
						&& stack.has(DataComponents.FOOD)
						&& stack.getMaxStackSize() == 64;
				},
				(builder, item) -> builder.set(DataComponents.MAX_STACK_SIZE, 16)
			);
			context.modify(Items.LEATHER, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 16));

			context.modify(Items.COOKED_BEEF, builder -> builder.set(
				DataComponents.FOOD,
				new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build()
			));

			context.modify(Items.COOKED_PORKCHOP, builder -> builder.set(
				DataComponents.FOOD,
				new FoodProperties.Builder().nutrition(5).saturationModifier(0.8F).build()
			));

			context.modify(Items.SWEET_BERRIES, builder -> builder.set(
				DataComponents.FOOD,
				new FoodProperties.Builder().nutrition(1).saturationModifier(0.15F).build()
			));

			// Raw meats: approximately half their vanilla hunger and exactly half their prior saturation.
			context.modify(Items.BEEF, builder -> builder.set(DataComponents.FOOD, food(2, 0.225F)));
			context.modify(Items.PORKCHOP, builder -> builder.set(DataComponents.FOOD, food(2, 0.225F)));
			context.modify(Items.CHICKEN, builder -> builder.set(DataComponents.FOOD, food(1, 0.30F)));
			context.modify(Items.MUTTON, builder -> builder.set(DataComponents.FOOD, food(1, 0.30F)));
			context.modify(Items.RABBIT, builder -> builder.set(DataComponents.FOOD, food(2, 0.225F)));
			context.modify(Items.COD, builder -> builder.set(DataComponents.FOOD, food(1, 0.10F)));
			context.modify(Items.SALMON, builder -> builder.set(DataComponents.FOOD, food(1, 0.10F)));
			context.modify(Items.TROPICAL_FISH, builder -> builder.set(DataComponents.FOOD, food(1, 0.05F)));
			context.modify(Items.PUFFERFISH, builder -> builder.set(DataComponents.FOOD, food(1, 0.05F)));

			context.modify(Items.MELON_SLICE, builder -> builder.set(DataComponents.FOOD, food(1, 0.25F)));
			context.modify(Items.COOKED_CHICKEN, builder -> builder.set(DataComponents.FOOD, food(3, 0.60F)));
			context.modify(Items.COOKED_MUTTON, builder -> builder.set(DataComponents.FOOD, food(5, 0.80F)));
			context.modify(Items.COOKED_RABBIT, builder -> builder.set(DataComponents.FOOD, food(2, 0.90F)));
		});
	}

	private static FoodProperties food(int nutrition, float saturationModifier) {
		return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build();
	}
}
