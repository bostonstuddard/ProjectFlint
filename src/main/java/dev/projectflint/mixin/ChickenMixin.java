package dev.projectflint.mixin;

import dev.projectflint.system.ChickenNestSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public abstract class ChickenMixin {
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void projectflint$requireNestForEggs(CallbackInfo ci) {
		Chicken chicken = (Chicken) (Object) this;
		if (!(chicken.level() instanceof ServerLevel level)) return;
		if (!chicken.isAlive() || chicken.isBaby() || chicken.isChickenJockey() || chicken.eggTime > 1) return;

		if (ChickenNestSystem.tryLayEgg(level, chicken)) {
			chicken.playSound(SoundEvents.CHICKEN_EGG, 1.0F,
				(chicken.getRandom().nextFloat() - chicken.getRandom().nextFloat()) * 0.2F + 1.0F);
			chicken.gameEvent(GameEvent.ENTITY_PLACE);
			chicken.eggTime = chicken.getRandom().nextInt(6000) + 6000;
		} else {
			// No valid nest: suppress vanilla egg spawning and retry in ten seconds.
			chicken.eggTime = 200;
		}
	}
}
