package dev.projectflint.mixin;

import com.mojang.realmsclient.RealmsAvailability;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RealmsAvailability.class)
public abstract class RealmsAvailabilityMixin {
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private static void projectflint$disableRealmsAvailability(
		CallbackInfoReturnable<CompletableFuture<RealmsAvailability.Result>> cir
	) {
		cir.setReturnValue(CompletableFuture.completedFuture(
			new RealmsAvailability.Result(RealmsAvailability.Type.INCOMPATIBLE_CLIENT)
		));
	}
}
