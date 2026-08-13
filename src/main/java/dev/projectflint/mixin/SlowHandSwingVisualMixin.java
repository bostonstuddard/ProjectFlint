package dev.projectflint.mixin;

import dev.projectflint.registry.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SlowHandSwingVisualMixin {
	@Inject(method = "getAttackAnim", at = @At("HEAD"), cancellable = true)
	private void projectflint$slowInadequateHandSwing(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (!((Object) this instanceof LocalPlayer player)) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.level == null || !client.options.keyAttack.isDown() || !player.getMainHandItem().isEmpty()) {
			return;
		}

		if (!(client.hitResult instanceof BlockHitResult hit)) {
			return;
		}

		if (!client.level.getBlockState(hit.getBlockPos()).is(ModTags.SLOW_HAND_CLEAR_TARGETS)) {
			return;
		}

		float cycle = ((player.tickCount % 28) + partialTick) / 28.0F;
		cir.setReturnValue(cycle);
	}
}
