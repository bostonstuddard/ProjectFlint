package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class ModEffects {
	public static final Holder<MobEffect> SOAKED = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ProjectFlint.id("soaked"),
		new SoakedEffect(MobEffectCategory.HARMFUL, 0x3D78B8)
			.addAttributeModifier(
				Attributes.MOVEMENT_SPEED,
				ProjectFlint.id("effect.soaked_movement_speed"),
				-0.15000000596046448D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
			)
	);

	public static final Holder<MobEffect> DRENCHED = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ProjectFlint.id("drenched"),
		new DrenchedEffect(MobEffectCategory.HARMFUL, 0x24537D)
			.addAttributeModifier(
				Attributes.MOVEMENT_SPEED,
				ProjectFlint.id("effect.drenched_movement_speed"),
				-0.15000000596046448D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
			)
	);

	public static final Holder<MobEffect> TERRIFIED = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ProjectFlint.id("terrified"),
		new TerrifiedEffect(MobEffectCategory.HARMFUL, 0x33294A)
			.addAttributeModifier(
				Attributes.ATTACK_DAMAGE,
				ProjectFlint.id("effect.terrified_weakness"),
				-4.0D,
				AttributeModifier.Operation.ADD_VALUE
			)
			.addAttributeModifier(
				Attributes.ATTACK_SPEED,
				ProjectFlint.id("effect.terrified_attack_speed"),
				-0.10000000149011612D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
			)
			.addAttributeModifier(
				Attributes.BLOCK_BREAK_SPEED,
				ProjectFlint.id("effect.terrified_block_break_speed"),
				-0.70D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
			)
	);

	private ModEffects() {}

	public static void initialize() {}

	private static class SoakedEffect extends MobEffect {
		private SoakedEffect(MobEffectCategory category, int color) {
			super(category, color);
		}

		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
			if (entity instanceof Player player) {
				player.causeFoodExhaustion(0.005F * (amplifier + 1));
			}
			return true;
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
			return true;
		}
	}

	private static final class DrenchedEffect extends SoakedEffect {
		private DrenchedEffect(MobEffectCategory category, int color) {
			super(category, color);
		}

		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
			super.applyEffectTick(level, entity, amplifier);
			if (entity.tickCount % 25 == 0 && entity.getHealth() > 1.0F) {
				float poisonDamage = Math.min(1.0F, entity.getHealth() - 1.0F);
				if (poisonDamage > 0.0F) {
					entity.hurtServer(level, level.damageSources().magic(), poisonDamage);
				}
			}
			return true;
		}
	}

	private static final class TerrifiedEffect extends MobEffect {
		private TerrifiedEffect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}
}
