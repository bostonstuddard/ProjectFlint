package dev.projectflint.system;

import dev.projectflint.entity.BlackBear;
import dev.projectflint.entity.BrownBear;
import dev.projectflint.registry.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class PrimitiveAnimalDropSystem {
	private PrimitiveAnimalDropSystem() {}

	public static void initialize() {
		LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
			Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
			if (entity == null) return;

			if (entity instanceof Cow) {
				for (int i = 0; i < drops.size(); i++) {
					ItemStack stack = drops.get(i);
					if (stack.is(Items.LEATHER)) drops.set(i, new ItemStack(ModItems.RAW_LEATHER, stack.getCount()));
				}
			}

			if (entity instanceof Horse) {
				// Horses no longer leak vanilla Leather into Project Flint progression.
				drops.removeIf(stack -> stack.is(Items.LEATHER));
				if (entity.getRandom().nextBoolean()) drops.add(new ItemStack(ModItems.RAW_LEATHER));
			}

			if (entity instanceof Sheep) {
				for (int i = 0; i < drops.size(); i++) {
					ItemStack stack = drops.get(i);
					if (stack.is(ItemTags.WOOL)) {
						drops.set(i, new ItemStack(ModItems.WOOL, 1 + entity.getRandom().nextInt(2)));
					}
				}
			}

			boolean defaultLootTable = entity.getType().getDefaultLootTable().isPresent()
				&& entry.is(entity.getType().getDefaultLootTable().get());
			if (defaultLootTable && entity instanceof Squid) {
				drops.add(new ItemStack(ModItems.RAW_CALAMARI));
			}

			if (!context.hasParameter(LootContextParams.DAMAGE_SOURCE) || !defaultLootTable) return;

			if (!(entity instanceof AgeableMob ageable) || !ageable.isBaby()) {
				addNewMeatDrop(entity, drops);
			}

			int bones = animalBoneCount(entity);
			if (bones > 0) drops.add(new ItemStack(ModItems.ANIMAL_BONES, bones));
		});
	}

	private static void addNewMeatDrop(Entity entity, java.util.List<ItemStack> drops) {
		if (entity instanceof Fox) {
			addMeat(entity, drops, ModItems.RAW_FOX_MEAT, ModItems.COOKED_FOX_MEAT, 1, 1);
		} else if (entity instanceof Frog) {
			addMeat(entity, drops, ModItems.RAW_FROG_MEAT, ModItems.COOKED_FROG_MEAT, 1, 1);
		} else if (entity instanceof Goat) {
			addMeat(entity, drops, ModItems.RAW_GOAT_MEAT, ModItems.COOKED_GOAT_MEAT, 1, 2);
		} else if (entity instanceof Horse) {
			addMeat(entity, drops, ModItems.RAW_HORSE_MEAT, ModItems.COOKED_HORSE_MEAT, 2, 3);
		} else if (entity instanceof Llama) {
			addMeat(entity, drops, ModItems.RAW_LLAMA_MEAT, ModItems.COOKED_LLAMA_MEAT, 2, 3);
		} else if (entity instanceof Dolphin) {
			addMeat(entity, drops, ModItems.RAW_DOLPHIN_MEAT, ModItems.COOKED_DOLPHIN_MEAT, 1, 2);
		} else if (entity instanceof BlackBear || entity instanceof BrownBear) {
			addMeat(entity, drops, ModItems.RAW_BEAR_MEAT, ModItems.COOKED_BEAR_MEAT, 1, 2);
		} else if (entity instanceof PolarBear) {
			addMeat(entity, drops, ModItems.RAW_BEAR_MEAT, ModItems.COOKED_BEAR_MEAT, 2, 4);
		} else if (entity instanceof Camel) {
			addMeat(entity, drops, ModItems.RAW_CAMEL_MEAT, ModItems.COOKED_CAMEL_MEAT, 3, 4);
		}
	}

	private static void addMeat(Entity entity, java.util.List<ItemStack> drops, Item raw, Item cooked, int min, int max) {
		int count = min + entity.getRandom().nextInt(max - min + 1);
		drops.add(new ItemStack(entity.isOnFire() ? cooked : raw, count));
	}

	private static int animalBoneCount(Entity entity) {
		if (entity instanceof Chicken) return 1;
		if (entity instanceof Pig) return 1 + entity.getRandom().nextInt(2);
		if (entity instanceof Sheep) return 1 + entity.getRandom().nextInt(3);
		if (entity instanceof Cow) return 2 + entity.getRandom().nextInt(2);
		if (entity instanceof Squid) return 1 + entity.getRandom().nextInt(3);
		if (entity instanceof Horse) return entity.getRandom().nextBoolean() ? 1 : 0;
		if (entity instanceof BlackBear || entity instanceof BrownBear) return 2 + entity.getRandom().nextInt(2);
		return 0;
	}
}
