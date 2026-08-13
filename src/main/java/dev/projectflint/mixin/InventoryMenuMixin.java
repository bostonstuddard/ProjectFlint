package dev.projectflint.mixin;

import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void projectflint$disablePersonalCrafting(CallbackInfo ci) {
		InventoryMenu menu = (InventoryMenu) (Object) this;
		PrimitiveInventorySystem.markPersonalCraftingSlot(menu.getResultSlot());
		for (Slot slot : menu.getInputGridSlots()) {
			PrimitiveInventorySystem.markPersonalCraftingSlot(slot);
		}
	}
}
