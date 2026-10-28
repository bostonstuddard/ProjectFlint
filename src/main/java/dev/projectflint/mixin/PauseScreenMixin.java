package dev.projectflint.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
	@Inject(method = "openScreenButton", at = @At("RETURN"), cancellable = true)
	private void projectflint$replaceRetiredAdvancements(
		Component label,
		Supplier<Screen> screenSupplier,
		CallbackInfoReturnable<Button> cir
	) {
		Button original = cir.getReturnValue();
		PauseScreen parent = (PauseScreen) (Object) this;
		boolean flintQuestsLoaded = FabricLoader.getInstance().isModLoaded("flintquests");

		if (label.equals(Component.translatable("gui.advancements"))) {
			if (flintQuestsLoaded) {
				cir.setReturnValue(Button.builder(Component.literal("Quests"), button -> projectflint$openFlintQuests(parent))
					.width(original.getWidth())
					.build());
			} else {
				original.visible = false;
				original.active = false;
			}
			return;
		}

		if (!label.equals(Component.translatable("menu.shareToLan"))) return;

		Minecraft client = Minecraft.getInstance();
		IntegratedServer server = client.getSingleplayerServer();
		if (server == null || server.getWorldData().isAllowCommands()) return;
		original.active = false;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void projectflint$expandStatisticsWithoutQuestMod(CallbackInfo ci) {
		if (FabricLoader.getInstance().isModLoaded("flintquests")) return;

		PauseScreen screen = (PauseScreen) (Object) this;
		for (GuiEventListener child : screen.children()) {
			if (!(child instanceof Button button)) continue;
			if (!button.getMessage().equals(Component.translatable("gui.stats"))) continue;
			button.setX(screen.width / 2 - 102);
			button.setWidth(204);
			break;
		}
	}

	private static void projectflint$openFlintQuests(Screen parent) {
		Minecraft client = Minecraft.getInstance();
		try {
			Class<?> screenClass = Class.forName("com.projectflint.flintquests.client.QuestBookScreen");
			Constructor<?> constructor = screenClass.getConstructor(Screen.class);
			Object screen = constructor.newInstance(parent);
			if (screen instanceof Screen questScreen) client.setScreen(questScreen);
		} catch (ReflectiveOperationException ignored) {
			// Keep the pause menu usable if a development Flint Quests build changes its client screen constructor.
		}
	}
}
