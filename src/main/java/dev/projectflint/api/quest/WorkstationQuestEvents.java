package dev.projectflint.api.quest;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Public Project Flint hook used by quest mods to observe successful workstation crafts. */
public final class WorkstationQuestEvents {
	private WorkstationQuestEvents() {}

	@FunctionalInterface
	public interface CraftCallback {
		void onCraft(ServerPlayer player, Identifier workstationId, ItemStack output);
	}

	public static final Event<CraftCallback> CRAFT = EventFactory.createArrayBacked(
		CraftCallback.class,
		listeners -> (player, workstationId, output) -> {
			for (CraftCallback listener : listeners) listener.onCraft(player, workstationId, output);
		}
	);
}
