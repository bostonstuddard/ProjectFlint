package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.menu.AdvancedKilnMenu;
import dev.projectflint.menu.ClayGrinderMenu;
import dev.projectflint.menu.MortarChestMenu;
import dev.projectflint.menu.SandingStationMenu;
import dev.projectflint.menu.WovenChestMenu;
import dev.projectflint.menu.WoodenGrinderMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	public static final MenuType<ClayGrinderMenu> CLAY_GRINDER = Registry.register(
		BuiltInRegistries.MENU,
		ProjectFlint.id("clay_grinder"),
		new MenuType<>((syncId, inventory) -> new ClayGrinderMenu(syncId, inventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS)
	);

	public static final MenuType<MortarChestMenu> MORTAR_CHEST = Registry.register(
		BuiltInRegistries.MENU,
		ProjectFlint.id("mortar_chest"),
		new MenuType<>(MortarChestMenu::new, FeatureFlags.DEFAULT_FLAGS)
	);

	public static final MenuType<AdvancedKilnMenu> ADVANCED_KILN = Registry.register(
		BuiltInRegistries.MENU,
		ProjectFlint.id("advanced_kiln"),
		new MenuType<>(AdvancedKilnMenu::new, FeatureFlags.DEFAULT_FLAGS)
	);

	public static final MenuType<SandingStationMenu> SANDING_STATION = Registry.register(
		BuiltInRegistries.MENU,
		ProjectFlint.id("sanding_station"),
		new MenuType<>(SandingStationMenu::new, FeatureFlags.DEFAULT_FLAGS)
	);

	public static final MenuType<WoodenGrinderMenu> WOODEN_GRINDER = Registry.register(
		BuiltInRegistries.MENU,
		ProjectFlint.id("wooden_grinder"),
		new MenuType<>((syncId, inventory) -> new WoodenGrinderMenu(syncId, inventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS)
	);

	public static final MenuType<WovenChestMenu> WOVEN_CHEST = Registry.register(
		BuiltInRegistries.MENU,
		ProjectFlint.id("woven_chest"),
		new MenuType<>(WovenChestMenu::new, FeatureFlags.DEFAULT_FLAGS)
	);

	private ModMenus() {}

	public static void initialize() {
		// Class loading performs registration.
	}
}
