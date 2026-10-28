package dev.projectflint.system;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
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
			// Fish buckets are containers, not food. A null value removes the component in Minecraft 1.21.11.
			for (var fishBucket : new net.minecraft.world.item.Item[] {Items.COD_BUCKET, Items.SALMON_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET})
				context.modify(fishBucket, builder -> builder.set(DataComponents.FOOD, null));

			// Oven-progression foods. Project Flint's tooltip displays nutrition/2 and saturation/2.
			context.modify(Items.SUGAR, builder -> builder
				.set(DataComponents.MAX_STACK_SIZE, 16)
				.set(DataComponents.FOOD, food(1, 0.80F))
				.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD));
			context.modify(Items.PUMPKIN_PIE, builder -> builder
				.set(DataComponents.MAX_STACK_SIZE, 16)
				.set(DataComponents.FOOD, food(6, 3.2F / 6.0F)));

			// Common vanilla foods yield slightly less than researched multicomponent Oven meals.
			context.modify(Items.APPLE, builder -> builder.set(DataComponents.FOOD, food(3, 0.22F)));
			context.modify(Items.CARROT, builder -> builder.set(DataComponents.FOOD, food(3, 0.40F)));
			context.modify(Items.POTATO, builder -> builder.set(DataComponents.FOOD, food(1, 0.20F)));
			context.modify(Items.BEETROOT, builder -> builder.set(DataComponents.FOOD, food(1, 0.35F)));
			context.modify(Items.GLOW_BERRIES, builder -> builder.set(DataComponents.FOOD, food(2, 0.05F)));
			context.modify(Items.COOKED_COD, builder -> builder.set(DataComponents.FOOD, food(4, 0.52F)));
			context.modify(Items.COOKED_SALMON, builder -> builder.set(DataComponents.FOOD, food(5, 0.70F)));
			context.modify(Items.BAKED_POTATO, builder -> builder.set(DataComponents.FOOD, food(4, 0.52F)));
			context.modify(Items.DRIED_KELP, builder -> builder.set(DataComponents.FOOD, food(1, 0.45F)));
			context.modify(Items.COOKED_BEEF, builder -> builder.set(
				DataComponents.FOOD,
				new FoodProperties.Builder().nutrition(5).saturationModifier(0.72F).build()
			));

			context.modify(Items.COOKED_PORKCHOP, builder -> builder.set(
				DataComponents.FOOD,
				new FoodProperties.Builder().nutrition(4).saturationModifier(0.72F).build()
			));

			context.modify(Items.SWEET_BERRIES, builder -> builder.set(
				DataComponents.FOOD,
				new FoodProperties.Builder().nutrition(1).saturationModifier(0.15F).build()
			));

			// Raw meats: approximately half their vanilla hunger and exactly half their prior saturation.
			context.modify(Items.BEEF, builder -> builder.set(DataComponents.FOOD, food(2, 0.20F)));
			context.modify(Items.PORKCHOP, builder -> builder.set(DataComponents.FOOD, food(2, 0.20F)));
			context.modify(Items.CHICKEN, builder -> builder.set(DataComponents.FOOD, food(1, 0.27F)));
			context.modify(Items.MUTTON, builder -> builder.set(DataComponents.FOOD, food(1, 0.27F)));
			context.modify(Items.RABBIT, builder -> builder.set(DataComponents.FOOD, food(2, 0.20F)));
			context.modify(Items.COD, builder -> builder.set(DataComponents.FOOD, food(1, 0.09F)));
			context.modify(Items.SALMON, builder -> builder.set(DataComponents.FOOD, food(1, 0.09F)));
			context.modify(Items.TROPICAL_FISH, builder -> builder.set(DataComponents.FOOD, food(1, 0.045F)));
			context.modify(Items.PUFFERFISH, builder -> builder.set(DataComponents.FOOD, food(1, 0.045F)));

			context.modify(Items.MELON_SLICE, builder -> builder.set(DataComponents.FOOD, food(1, 0.22F)));
			context.modify(Items.COOKED_CHICKEN, builder -> builder.set(DataComponents.FOOD, food(2, 0.54F)));
			context.modify(Items.COOKED_MUTTON, builder -> builder.set(DataComponents.FOOD, food(4, 0.72F)));
			context.modify(Items.COOKED_RABBIT, builder -> builder.set(DataComponents.FOOD, food(2, 0.81F)));
		});
	}

	private static FoodProperties food(int nutrition, float saturationModifier) {
		return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build();
	}
}
