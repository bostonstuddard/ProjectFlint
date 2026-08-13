package dev.projectflint.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {
	@ModifyVariable(method = "apply", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private Map<Identifier, Advancement> projectflint$removeVanillaAdvancements(Map<Identifier, Advancement> advancements) {
		Map<Identifier, Advancement> filtered = new LinkedHashMap<>(advancements);
		filtered.entrySet().removeIf(entry -> entry.getKey().getNamespace().equals("minecraft"));
		return filtered;
	}
}
