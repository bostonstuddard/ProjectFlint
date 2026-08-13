package dev.projectflint.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {
	@ModifyVariable(method = "setGameMode", at = @At("HEAD"), argsOnly = true)
	private WorldCreationUiState.SelectedGameMode projectflint$restrictCreationModes(WorldCreationUiState.SelectedGameMode mode) {
		return switch (mode) {
			case CREATIVE, HARDCORE -> mode;
			case SURVIVAL, DEBUG -> WorldCreationUiState.SelectedGameMode.HARDCORE;
		};
	}

	@ModifyVariable(method = "setDifficulty", at = @At("HEAD"), argsOnly = true)
	private Difficulty projectflint$forceCreationDifficulty(Difficulty difficulty) {
		return Difficulty.HARD;
	}
}
