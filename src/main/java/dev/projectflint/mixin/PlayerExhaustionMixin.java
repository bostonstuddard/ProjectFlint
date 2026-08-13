package dev.projectflint.mixin;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Leather Backpack adds a small 5% movement/food exhaustion tax while equipped. */
@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {
	@ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
	private float projectflint$leatherBackpackExhaustion(float exhaustion) {
		Player player = (Player) (Object) this;
		return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.LEATHER_BACKPACK) ? exhaustion * 1.05F : exhaustion;
	}
}
