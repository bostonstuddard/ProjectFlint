package dev.projectflint.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Vanilla-backed 9-slot single / 18-slot double wooden chest menu.
 *
 * Extending ChestMenu is important: ChestBlockEntity's vanilla opener tracking
 * explicitly recognizes ChestMenu instances when it rechecks who still has a
 * chest open. A parallel AbstractContainerMenu implementation leaves the lid
 * counter out of sync and can strand the rendered lid in the wrong state.
 */
public final class WoodenChestMenu extends ChestMenu {
	public static WoodenChestMenu single(int id, Inventory inventory) {
		return new WoodenChestMenu(
			dev.projectflint.registry.ModMenus.WOODEN_CHEST,
			id,
			inventory,
			new SimpleContainer(9),
			1
		);
	}

	public static WoodenChestMenu doubleChest(int id, Inventory inventory) {
		return new WoodenChestMenu(
			dev.projectflint.registry.ModMenus.WOODEN_DOUBLE_CHEST,
			id,
			inventory,
			new SimpleContainer(18),
			2
		);
	}

	public WoodenChestMenu(MenuType<?> type, int id, Inventory inventory, Container container, int rows) {
		super(type, id, inventory, container, rows);
	}

	public int rows() {
		return getRowCount();
	}
}
