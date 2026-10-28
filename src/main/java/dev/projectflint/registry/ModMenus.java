package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.menu.AdvancedKilnMenu;
import dev.projectflint.menu.ClayGrinderMenu;
import dev.projectflint.menu.MortarChestMenu;
import dev.projectflint.menu.SandingStationMenu;
import dev.projectflint.menu.WovenChestMenu;
import dev.projectflint.menu.WoodenGrinderMenu;
import dev.projectflint.menu.WoodenChestMenu;
import dev.projectflint.menu.BookshelfMenu;
import dev.projectflint.menu.WoodenCabinetMenu;
import dev.projectflint.menu.PrimitiveStoveMenu;
import dev.projectflint.menu.BrickFurnaceMenu;
import dev.projectflint.menu.WellPulleyMenu;
import dev.projectflint.menu.NestMenu;
import dev.projectflint.menu.FireclayForgeMenu;
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

	public static final MenuType<WoodenChestMenu> WOODEN_CHEST = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("wooden_chest"), new MenuType<>(WoodenChestMenu::single, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<WoodenChestMenu> WOODEN_DOUBLE_CHEST = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("wooden_double_chest"), new MenuType<>(WoodenChestMenu::doubleChest, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<BookshelfMenu> BOOKSHELF = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("bookshelf"), new MenuType<>(BookshelfMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<WoodenCabinetMenu> WOODEN_CABINET = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("wooden_cabinet"), new MenuType<>(WoodenCabinetMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<PrimitiveStoveMenu> PRIMITIVE_STOVE = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("primitive_stove"), new MenuType<>(PrimitiveStoveMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<BrickFurnaceMenu> BRICK_FURNACE = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("brick_furnace"), new MenuType<>(BrickFurnaceMenu::furnace, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<BrickFurnaceMenu> BRICK_BLAST_FURNACE = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("brick_blast_furnace"), new MenuType<>(BrickFurnaceMenu::blastFurnace, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<WellPulleyMenu> WELL_PULLEY = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("well_pulley"), new MenuType<>(WellPulleyMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<NestMenu> NEST = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("nest"), new MenuType<>(NestMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final MenuType<FireclayForgeMenu> FIRECLAY_FORGE = Registry.register(BuiltInRegistries.MENU, ProjectFlint.id("fireclay_forge"), new MenuType<>(FireclayForgeMenu::new, FeatureFlags.DEFAULT_FLAGS));

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
