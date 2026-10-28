package dev.projectflint.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

/** Project Flint progression now lives in Flint Quests, so no advancement data is loaded. */
@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {
	@ModifyVariable(method = "apply", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private Map<Identifier, Advancement> projectflint$removeAllAdvancements(Map<Identifier, Advancement> advancements) {
		return Map.of();
	}
}
