package dev.projectflint.system;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Primitive carrying capacity for the opening game.
 *
 * Inventory indices 0-4 (hotbar slots 1-5) are usable by default. Wearing a
 * Reed Backpack expands that allowance to the full hotbar. Leather progression
 * then opens part of the first inventory row, and the chest-backed upgrade opens
 * that entire row.
 * Locked-slot enforcement is server-side; the overlay only explains the rule.
 */
public final class PrimitiveInventorySystem {
	public static final int BASE_ALLOWED_HOTBAR_SLOTS = 5;
	public static final int BACKPACK_ALLOWED_HOTBAR_SLOTS = 9;
	public static final int LEATHER_BACKPACK_ALLOWED_SLOTS = 13;
	public static final int LEATHER_BACKPACK_WITH_CHEST_ALLOWED_SLOTS = 18;
	public static final int TANNED_UTILITY_VEST_ALLOWED_SLOTS = 22;
	private static final int LAST_MAIN_SLOT = 35;

	private static final Map<Slot, WeakReference<Player>> PERSONAL_CRAFTING_SLOTS = Collections.synchronizedMap(new WeakHashMap<>());

	private PrimitiveInventorySystem() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(PrimitiveInventorySystem::tickServer);
	}

	private static void tickServer(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			// Keep vanilla Wooden Axe attack damage at 4.5, including freshly crafted axes.
			Inventory axeInventory = player.getInventory();
			for (int index = 0; index < axeInventory.getContainerSize(); index++)
				WoodenAxeBalanceSystem.normalize(axeInventory.getItem(index));
			WoodenAxeBalanceSystem.normalize(player.getOffhandItem());
			// Existing users and newly joined players can discover ProjectFlint's own personal recipes.
			// Grant just our crafting recipes; never reveal/restore disabled vanilla crafting.
			if (canUsePersonalCrafting(player) && player.tickCount % 100 == 1) {
				player.awardRecipes(server.getRecipeManager().getRecipes().stream()
					.filter(holder -> holder.id().identifier().getNamespace().equals("projectflint")
						&& holder.value() instanceof net.minecraft.world.item.crafting.CraftingRecipe
						&& !player.getRecipeBook().contains(holder.id()))
					.toList());
			}

			if (player.isCreative() || player.isSpectator()) {
				continue;
			}

			Inventory inventory = player.getInventory();
			int allowedSlots = allowedHotbarSlots(player);
			if (inventory.getSelectedSlot() >= allowedSlots) {
				inventory.setSelectedSlot(allowedSlots - 1);
			}

			for (int slot = allowedSlots; slot <= LAST_MAIN_SLOT; slot++) {
				ItemStack stack = inventory.getItem(slot);
				if (stack.isEmpty()) {
					continue;
				}

				inventory.setItem(slot, ItemStack.EMPTY);
				if (!insertIntoPrimitiveInventory(player, stack)) {
					player.drop(stack, false);
				}
			}
		}
	}

	/**
	 * Returns true only when the entire supplied stack can fit inside the currently
	 * unlocked primitive inventory slots. This remains useful
	 * for physical world interactions that should be atomic, such as picking a
	 * component back out of an assembly. Dropped ItemEntities use partial insertion.
	 */
	public static boolean canFullyInsertIntoPrimitiveInventory(Player player, ItemStack stack) {
		if (stack.isEmpty() || player.isCreative()) {
			return true;
		}

		Inventory inventory = player.getInventory();
		int remaining = stack.getCount();

		int allowedSlots = allowedHotbarSlots(player);
		for (int slot = 0; slot < allowedSlots && remaining > 0; slot++) {
			ItemStack existing = inventory.getItem(slot);
			if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
				continue;
			}

			remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
		}

		for (int slot = 0; slot < allowedSlots && remaining > 0; slot++) {
			if (!inventory.getItem(slot).isEmpty()) {
				continue;
			}

			remaining -= stack.getMaxStackSize();
		}

		return remaining <= 0;
	}

	/**
	 * Inserts as much as possible into the currently usable primitive slots. The supplied
	 * stack is mutated down to its remainder, matching vanilla insertion conventions.
	 */
	public static boolean insertIntoPrimitiveInventory(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}
		if (player.isCreative()) {
			return player.getInventory().add(stack);
		}

		Inventory inventory = player.getInventory();
		int allowedSlots = allowedHotbarSlots(player);

		// Merge first so a partially filled allowed stack is useful even when all
		// visible slots are occupied.
		for (int slot = 0; slot < allowedSlots && !stack.isEmpty(); slot++) {
			ItemStack existing = inventory.getItem(slot);
			if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
				continue;
			}

			int room = existing.getMaxStackSize() - existing.getCount();
			if (room <= 0) {
				continue;
			}

			int moved = Math.min(room, stack.getCount());
			existing.grow(moved);
			stack.shrink(moved);
		}

		// Then fill empty primitive slots.
		for (int slot = 0; slot < allowedSlots && !stack.isEmpty(); slot++) {
			if (!inventory.getItem(slot).isEmpty()) {
				continue;
			}

			int moved = Math.min(stack.getCount(), stack.getMaxStackSize());
			ItemStack inserted = stack.copy();
			inserted.setCount(moved);
			inventory.setItem(slot, inserted);
			stack.shrink(moved);
		}

		return stack.isEmpty();
	}

	public static int allowedHotbarSlots(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (chest.is(dev.projectflint.registry.ModItems.TANNED_UTILITY_VEST)) return TANNED_UTILITY_VEST_ALLOWED_SLOTS;
		if (chest.is(dev.projectflint.registry.ModItems.LEATHER_BACKPACK_WITH_CHEST)) return LEATHER_BACKPACK_WITH_CHEST_ALLOWED_SLOTS;
		if (chest.is(dev.projectflint.registry.ModItems.LEATHER_BACKPACK)) return LEATHER_BACKPACK_ALLOWED_SLOTS;
		if (chest.is(dev.projectflint.registry.ModItems.REED_BACKPACK)) return BACKPACK_ALLOWED_HOTBAR_SLOTS;
		return BASE_ALLOWED_HOTBAR_SLOTS;
	}

	public static boolean isLockedInventoryIndex(Player player, int inventoryIndex) {
		return inventoryIndex >= allowedHotbarSlots(player) && inventoryIndex <= LAST_MAIN_SLOT;
	}

	public static boolean canUsePersonalCrafting(Player player) {
		return player.isCreative() || player.getItemBySlot(EquipmentSlot.CHEST).is(dev.projectflint.registry.ModItems.TANNED_UTILITY_VEST);
	}

	public static void markPersonalCraftingSlot(Slot slot, Player owner) {
		PERSONAL_CRAFTING_SLOTS.put(slot, new WeakReference<>(owner));
	}

	public static boolean isPersonalCraftingSlot(Slot slot) {
		return PERSONAL_CRAFTING_SLOTS.containsKey(slot);
	}

	public static boolean isPersonalCraftingLocked(Slot slot) {
		WeakReference<Player> reference = PERSONAL_CRAFTING_SLOTS.get(slot);
		Player owner = reference == null ? null : reference.get();
		return owner != null && !canUsePersonalCrafting(owner);
	}

	public static boolean isLockedPlayerInventorySlot(Slot slot) {
		Container container = slot.container;
		if (!(container instanceof Inventory inventory)) {
			return false;
		}
		return isLockedInventoryIndex(inventory.player, slot.getContainerSlot()) && !inventory.player.isCreative();
	}
}
