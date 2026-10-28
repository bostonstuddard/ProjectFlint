package dev.projectflint.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftRealmsMixin {
	@Inject(method = "allowsRealms", at = @At("HEAD"), cancellable = true)
	private void projectflint$disableRealmsPermission(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
