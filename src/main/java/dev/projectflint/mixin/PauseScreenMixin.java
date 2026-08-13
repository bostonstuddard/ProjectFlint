package dev.projectflint.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
	@Inject(method = "openScreenButton", at = @At("RETURN"))
	private void projectflint$disableOpenToLanWithoutCheats(
		Component label,
		Supplier<Screen> screenSupplier,
		CallbackInfoReturnable<Button> cir
	) {
		if (!label.equals(Component.translatable("menu.shareToLan"))) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		IntegratedServer server = client.getSingleplayerServer();
		if (server == null || server.getWorldData().isAllowCommands()) {
			return;
		}

		cir.getReturnValue().active = false;
	}
}
