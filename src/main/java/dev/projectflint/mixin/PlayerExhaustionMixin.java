package dev.projectflint.mixin;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Backpack upgrades trade carrying capacity for increased food exhaustion while equipped. */
@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {
	@ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
	private float projectflint$leatherBackpackExhaustion(float exhaustion) {
		Player player = (Player) (Object) this;
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (chest.is(ModItems.LEATHER_BACKPACK_WITH_CHEST) || chest.is(ModItems.TANNED_UTILITY_VEST)) return exhaustion * 1.10F;
		if (chest.is(ModItems.LEATHER_BACKPACK)) return exhaustion * 1.05F;
		return exhaustion;
	}
}
