package dev.projectflint.mixin;

import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin {
	@Inject(method = "isHardcore", at = @At("HEAD"), cancellable = true)
	private void projectflint$forceHardcore(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}
}
