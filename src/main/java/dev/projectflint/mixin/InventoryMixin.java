package dev.projectflint.mixin;

import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps vanilla ItemEntity pickup/drop behavior intact while teaching vanilla
 * Inventory.add where survival Project Flint players are actually allowed to
 * store picked-up items. This avoids replacing ItemEntity.playerTouch itself.
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
	@Shadow @Final public Player player;
	@Shadow public abstract ItemStack getItem(int slot);

	@Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
	private void projectflint$limitFreePickupSlot(CallbackInfoReturnable<Integer> cir) {
		if (player.isCreative() || player.isSpectator()) {
			return;
		}
		for (int slot = 0; slot < PrimitiveInventorySystem.allowedHotbarSlots(player); slot++) {
			if (getItem(slot).isEmpty()) {
				cir.setReturnValue(slot);
				return;
			}
		}
		cir.setReturnValue(-1);
	}

	@Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
	private void projectflint$limitPickupMergeSlot(ItemStack incoming, CallbackInfoReturnable<Integer> cir) {
		if (player.isCreative() || player.isSpectator()) {
			return;
		}

		for (int slot = 0; slot < PrimitiveInventorySystem.allowedHotbarSlots(player); slot++) {
			if (projectflint$canMerge(getItem(slot), incoming)) {
				cir.setReturnValue(slot);
				return;
			}
		}

		// Offhand remains a legal primitive carrying slot and vanilla can merge into it.
		if (projectflint$canMerge(getItem(Inventory.SLOT_OFFHAND), incoming)) {
			cir.setReturnValue(Inventory.SLOT_OFFHAND);
			return;
		}
		cir.setReturnValue(-1);
	}

	private static boolean projectflint$canMerge(ItemStack existing, ItemStack incoming) {
		return !existing.isEmpty()
			&& ItemStack.isSameItemSameComponents(existing, incoming)
			&& existing.getCount() < existing.getMaxStackSize();
	}
}
