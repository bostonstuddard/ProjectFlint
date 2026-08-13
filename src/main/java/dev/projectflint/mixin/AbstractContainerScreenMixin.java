package dev.projectflint.mixin;

import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws primitive locked-slot art as part of the slot's own background pass.
 *
 * Injecting at the head of renderSlot guarantees Minecraft draws the slot item
 * (and later the carried/cursor item) after this art. The lock therefore reads
 * as the disabled slot background instead of a translucent pane sitting on top
 * of item sprites.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	@Inject(method = "renderSlot", at = @At("HEAD"))
	private void projectflint$renderPrimitiveLockedSlotBackground(
		GuiGraphics graphics,
		Slot slot,
		int mouseX,
		int mouseY,
		CallbackInfo ci
	) {
		if (!PrimitiveInventorySystem.isLockedPlayerInventorySlot(slot)) {
			return;
		}

		// renderSlot is invoked with the GUI pose already translated to the
		// container origin. Slot coordinates are therefore already local to the
		// inventory texture; adding leftPos/topPos again double-offsets the art.
		int x = slot.x;
		int y = slot.y;

		// Lighter disabled-slot treatment requested in alpha.6, now rendered behind
		// item sprites rather than over them.
		graphics.fill(x, y, x + 16, y + 16, 0xE0787878);
		graphics.fill(x + 2, y + 2, x + 14, y + 14, 0xE08A8A8A);
		graphics.fill(x + 3, y + 7, x + 13, y + 9, 0xD0B0B0B0);
	}
}
