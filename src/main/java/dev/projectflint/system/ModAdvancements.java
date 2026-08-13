package dev.projectflint.system;

import dev.projectflint.ProjectFlint;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Small server-side bridge for milestones that are defined by Project Flint
 * processes rather than vanilla's generic inventory/location criteria.
 */
public final class ModAdvancements {
	private ModAdvancements() {
	}

	public static void awardAll(ServerPlayer player, String advancementPath) {
		MinecraftServer server = player.level().getServer();
		AdvancementHolder advancement = server.getAdvancements().get(ProjectFlint.id(advancementPath));
		if (advancement == null) return;
		for (String criterion : advancement.value().criteria().keySet()) {
			player.getAdvancements().award(advancement, criterion);
		}
	}

	public static void award(ServerPlayer player, String advancementPath, String criterion) {
		MinecraftServer server = player.level().getServer();
		AdvancementHolder advancement = server.getAdvancements().get(ProjectFlint.id(advancementPath));
		if (advancement != null) {
			player.getAdvancements().award(advancement, criterion);
		}
	}
}
