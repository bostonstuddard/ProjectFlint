package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

public final class ModPotions {
	public static final ResourceKey<Potion> SOAKED_KEY = ResourceKey.create(Registries.POTION, ProjectFlint.id("soaked"));
	public static final Holder<Potion> SOAKED = Registry.registerForHolder(
		BuiltInRegistries.POTION,
		SOAKED_KEY,
		new Potion(
			"soaked",
			new MobEffectInstance(ModEffects.SOAKED, 20 * 30, 0),
			new MobEffectInstance(MobEffects.SLOWNESS, 20 * 30, 0),
			new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0)
		)
	);

	private ModPotions() {}

	public static void initialize() {}
}
