package dev.projectflint.mixin;

import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void projectflint$lockPrimitiveSlots(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Slot slot = (Slot) (Object) this;
		if (PrimitiveInventorySystem.isPersonalCraftingSlot(slot) || PrimitiveInventorySystem.isLockedPlayerInventorySlot(slot)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void projectflint$preventPrimitivePickup(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (player.isCreative()) {
			return;
		}
		Slot slot = (Slot) (Object) this;
		if (PrimitiveInventorySystem.isPersonalCraftingSlot(slot) || PrimitiveInventorySystem.isLockedPlayerInventorySlot(slot)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private void projectflint$hidePersonalCraftingSlot(CallbackInfoReturnable<Boolean> cir) {
		if (PrimitiveInventorySystem.isPersonalCraftingSlot((Slot) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isHighlightable", at = @At("HEAD"), cancellable = true)
	private void projectflint$hidePersonalCraftingHighlight(CallbackInfoReturnable<Boolean> cir) {
		if (PrimitiveInventorySystem.isPersonalCraftingSlot((Slot) (Object) this)) {
			cir.setReturnValue(false);
		}
	}
}
