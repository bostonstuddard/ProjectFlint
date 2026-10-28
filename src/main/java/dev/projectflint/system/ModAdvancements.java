package dev.projectflint.system;

import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility no-op retained while old call sites are migrated to Flint Quests.
 * Project Flint no longer loads or awards Minecraft advancements.
 */
public final class ModAdvancements {
	private ModAdvancements() {
	}

	public static void awardAll(ServerPlayer player, String advancementPath) {
		// Intentionally disabled.
	}

	public static void award(ServerPlayer player, String advancementPath, String criterion) {
		// Intentionally disabled.
	}
}
