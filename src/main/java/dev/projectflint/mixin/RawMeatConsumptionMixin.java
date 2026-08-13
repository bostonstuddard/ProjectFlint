package dev.projectflint.mixin;

import dev.projectflint.registry.ModTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies Project Flint's shared raw-meat sickness profile after tagged raw meat is eaten. */
@Mixin(LivingEntity.class)
public abstract class RawMeatConsumptionMixin {
	@Inject(method = "completeUsingItem", at = @At("HEAD"))
	private void projectflint$applyRawMeatSickness(CallbackInfo ci) {
		LivingEntity living = (LivingEntity) (Object) this;
		ItemStack consumed = living.getUseItem();
		if (!(living instanceof ServerPlayer player) || !consumed.is(ModTags.RAW_MEATS)) return;

		player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 12, 2));
		player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 8, 0));
		player.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 5, 0));
		player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5, 0));
	}
}
