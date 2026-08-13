package dev.projectflint.system;

import dev.projectflint.ProjectFlint;
import dev.projectflint.config.GameplayTuning;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;

public final class PassiveMobTuningSystem {
	private static final Identifier HEALTH_MODIFIER_ID = ProjectFlint.id("primitive_passive_health");

	private PassiveMobTuningSystem() {
	}

	public static void initialize() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(entity instanceof LivingEntity living) || !isPrimitivePassiveCategory(entity.getType().getCategory())) {
				return;
			}

			AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
			if (maxHealth == null || maxHealth.hasModifier(HEALTH_MODIFIER_ID)) {
				return;
			}

			float oldMax = living.getMaxHealth();
			float oldHealth = living.getHealth();
			double healthBonus = living instanceof Chicken ? GameplayTuning.CHICKEN_PASSIVE_HEALTH_BONUS : 1.0D;
			maxHealth.addPermanentModifier(new AttributeModifier(
				HEALTH_MODIFIER_ID,
				healthBonus,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
			));

			if (oldMax > 0.0F) {
				float fraction = oldHealth / oldMax;
				living.setHealth(Math.min(living.getMaxHealth(), living.getMaxHealth() * fraction));
			}
		});
	}

	private static boolean isPrimitivePassiveCategory(MobCategory category) {
		return switch (category) {
			case CREATURE, AMBIENT, AXOLOTLS, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, WATER_AMBIENT -> true;
			case MONSTER, MISC -> false;
		};
	}
}
