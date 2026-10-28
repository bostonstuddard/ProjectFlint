package dev.projectflint.compat.flintquests;

import dev.projectflint.ProjectFlint;
import dev.projectflint.api.quest.WorkstationQuestEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Optional bridge to the public Flint Quests custom-event API.
 *
 * Project Flint deliberately does not compile against Flint Quests. When the quest
 * mod is present, its stable FlintQuestHooks.trigger(ServerPlayer, String, int)
 * entry point is resolved once and used for bespoke workstation events. Generic
 * Flint Quests item/block tasks remain handled by Flint Quests itself.
 */
public final class FlintQuestsBridge {
	private static final String OBJECT_SHARE_KEY = "projectflint:workstation_craft_event";
	private static final String HOOK_CLASS = "com.projectflint.flintquests.api.FlintQuestHooks";
	private static Method triggerMethod;
	private static boolean initialized;

	private FlintQuestsBridge() {}

	public static void initialize() {
		if (initialized) return;
		initialized = true;

		// Stable Project Flint-side event for any quest/integration mod that wants it.
		FabricLoader.getInstance().getObjectShare().put(OBJECT_SHARE_KEY, WorkstationQuestEvents.CRAFT);

		if (!FabricLoader.getInstance().isModLoaded("flintquests")) return;
		try {
			Class<?> hooks = Class.forName(HOOK_CLASS, false, FlintQuestsBridge.class.getClassLoader());
			triggerMethod = hooks.getMethod("trigger", ServerPlayer.class, String.class, int.class);
			ProjectFlint.LOGGER.info("Flint Quests API compatibility active via {}#trigger", HOOK_CLASS);
		} catch (ClassNotFoundException | NoSuchMethodException exception) {
			ProjectFlint.LOGGER.warn("Flint Quests is loaded, but its public FlintQuestHooks API was not found; custom Project Flint quest events are disabled", exception);
		}
	}

	/** Called exactly once for each successfully claimed/completed workstation operation. */
	public static void onCraft(ServerPlayer player, String workstationPath, ItemStack output) {
		if (player == null || output == null || output.isEmpty()) return;
		Identifier workstationId = ProjectFlint.id(workstationPath);
		ItemStack snapshot = output.copy();
		WorkstationQuestEvents.CRAFT.invoker().onCraft(player, workstationId, snapshot);

		// CUSTOM_EVENT IDs intentionally stay human-readable for the Flint Quests editor.
		trigger(player, "projectflint:workstation/any", 1);
		trigger(player, "projectflint:workstation/" + workstationPath, 1);

		Identifier outputId = BuiltInRegistries.ITEM.getKey(snapshot.getItem());
		if (outputId != null) {
			trigger(player, "projectflint:workstation/" + workstationPath + "/output/" + outputId.getNamespace() + "/" + outputId.getPath(), Math.max(1, snapshot.getCount()));
		}
	}

	/** Reports a successful non-GUI workstation operation where no result slot exists. */
	public static void onWorkstationUse(ServerPlayer player, String workstationPath) {
		if (player == null || workstationPath == null || workstationPath.isBlank()) return;
		trigger(player, "projectflint:workstation/any", 1);
		trigger(player, "projectflint:workstation/" + workstationPath, 1);
	}

	/** Bespoke non-item workstation milestones such as forming a multiblock. */
	public static void trigger(ServerPlayer player, String eventId) {
		trigger(player, eventId, 1);
	}

	public static void trigger(ServerPlayer player, String eventId, int amount) {
		if (player == null || eventId == null || eventId.isBlank() || triggerMethod == null) return;
		try {
			triggerMethod.invoke(null, player, eventId, Math.max(1, amount));
		} catch (IllegalAccessException | InvocationTargetException | RuntimeException exception) {
			ProjectFlint.LOGGER.warn("Flint Quests custom event '{}' failed", eventId, exception);
		}
	}
}
