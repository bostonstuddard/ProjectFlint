package dev.projectflint.mixin;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
	@Inject(method = "init", at = @At("TAIL"))
	private void projectflint$applyPrimitiveWorldCreationDefaults(CallbackInfo ci) {
		CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
		WorldCreationUiState state = screen.getUiState();

		if (state.getGameMode() != WorldCreationUiState.SelectedGameMode.CREATIVE
			&& state.getGameMode() != WorldCreationUiState.SelectedGameMode.HARDCORE) {
			state.setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
		}
		if (state.getDifficulty() != Difficulty.HARD) {
			state.setDifficulty(Difficulty.HARD);
		}
	}
}
