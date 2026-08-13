package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public final class ModCreativeTabs {
	private static final ResourceKey<CreativeModeTab> PROJECT_FLINT_KEY = ResourceKey.create(
		Registries.CREATIVE_MODE_TAB,
		ProjectFlint.id("project_flint")
	);

	public static final CreativeModeTab PROJECT_FLINT = FabricItemGroup.builder()
		.title(Component.translatable("creative_tab.projectflint"))
		.icon(() -> new ItemStack(ModItems.REFINED_ROCK))
		.displayItems((parameters, output) -> {
			output.accept(ModItems.ROCK);
			output.accept(ModItems.LOOSE_ROCK_BLOCK_ITEM);
			output.accept(ModItems.REFINED_ROCK);
			output.accept(ModItems.VERY_REFINED_STONE);
			output.accept(ModItems.TWIG);
			output.accept(ModItems.LIGHTLY_BURNT_TWIG);
			output.accept(ModItems.CRUDE_PLANT_TORCH);
			output.accept(ModItems.TORCH_HOLDER);
			output.accept(ModItems.UNLIT_FLINT_TORCH);
			output.accept(ModItems.FLINT_TORCH_HOLDER);
			output.accept(ModItems.AQUIFER_FINDER);
			output.accept(ModItems.PLANT_FIBER);
			output.accept(ModItems.CRUDE_PLANT_STRING);
			output.accept(ModItems.CRUDE_STONE_HAMMER);
			output.accept(ModItems.CRUDE_REED);
			output.accept(ModItems.PLANT_TOOL_BINDING);
			output.accept(ModItems.CRUDE_HARVESTING_TOOL);
			output.accept(ModItems.FLOWER_PETALS);
			output.accept(ModItems.CAMPFIRE_STONE);
			output.accept(ModItems.BURNT_TWIG);
			output.accept(ModItems.BRANCH);
			output.accept(ModItems.BURNT_BRANCH);
			output.accept(ModItems.CRUDE_MULTITOOL);
			output.accept(ModItems.FLINT_FIRESTARTER);
			output.accept(ModItems.FLINT_MULTITOOL);
			output.accept(ModItems.DIRT_PILE);
			output.accept(ModItems.WATTLE);
			output.accept(ModItems.TIED_WATTLE);
			output.accept(ModItems.MUD_PILE);
			output.accept(ModItems.DAUB_PILE);
			output.accept(ModItems.WET_DAUB_PILE);
			output.accept(ModItems.UNFIRED_WATTLE_AND_DAUB);
			output.accept(ModItems.WATTLED_DAUB_BRICKS);
			output.accept(ModItems.WATTLED_DAUB_FLOOR_TILE);
			output.accept(ModItems.WATTLED_DAUB_WINDOW);
			output.accept(ModItems.WATTLED_DAUB_DOOR);
			output.accept(ModItems.LOOSE_DIRT);
			output.accept(ModItems.LOOSE_SOIL);
			output.accept(ModItems.LOOSE_DIRT_SLAB);
			output.accept(ModItems.FLINT_CHIP);
			output.accept(ModItems.CRUDELY_WOVEN_BOWL);
			output.accept(ModItems.UNFIRED_WOVEN_BOWL);
			output.accept(ModItems.FIRED_CLAY_BOWL);
			output.accept(ModItems.UNFIRED_CLAY_BALL);
			output.accept(ModItems.SUPER_HEATED_CLAY);
			output.accept(ModItems.FIRED_CLAY);
			output.accept(ModItems.CRUDE_CLAY_BLOCK);
			output.accept(ModItems.CRUDE_CLAY_KILN);
			output.accept(ModItems.BAKED_CLAY_KILN);
			output.accept(ModItems.BAKED_KILN_TOP);
			output.accept(ModItems.CLAY_GRINDING_BASE);
			output.accept(ModItems.CLAY_GRINDER);
			output.accept(ModItems.SCRAMBLED_EGG);
			output.accept(ModItems.RAW_CALAMARI);
			output.accept(ModItems.COOKED_CALAMARI);
			output.accept(ModItems.RAW_FOX_MEAT);
			output.accept(ModItems.COOKED_FOX_MEAT);
			output.accept(ModItems.RAW_FROG_MEAT);
			output.accept(ModItems.COOKED_FROG_MEAT);
			output.accept(ModItems.RAW_GOAT_MEAT);
			output.accept(ModItems.COOKED_GOAT_MEAT);
			output.accept(ModItems.RAW_HORSE_MEAT);
			output.accept(ModItems.COOKED_HORSE_MEAT);
			output.accept(ModItems.RAW_LLAMA_MEAT);
			output.accept(ModItems.COOKED_LLAMA_MEAT);
			output.accept(ModItems.RAW_DOLPHIN_MEAT);
			output.accept(ModItems.COOKED_DOLPHIN_MEAT);
			output.accept(ModItems.RAW_BEAR_MEAT);
			output.accept(ModItems.COOKED_BEAR_MEAT);
			output.accept(ModItems.BLACK_BEAR_SPAWN_EGG);
			output.accept(ModItems.BROWN_BEAR_SPAWN_EGG);
			output.accept(ModItems.RAW_CAMEL_MEAT);
			output.accept(ModItems.COOKED_CAMEL_MEAT);
			output.accept(ModItems.PLANT_SEEDS);
			output.accept(ModItems.SWEET_BERRY_SEEDS);
			output.accept(ModItems.PUMPKIN_SLICE);
			output.accept(ModItems.SWEET_BERRY_JAM);
			output.accept(ModItems.SWEET_BERRY_PIE);
			output.accept(ModItems.REED_ROLL);
			output.accept(ModItems.SUGARCANE_ROLL);
			output.accept(ModItems.SANDING_PAPER);
			output.accept(ModItems.WOOL);
			output.accept(ModItems.ANIMAL_BONES);
			output.accept(ModItems.FILED_BONE);
			output.accept(ModItems.BONE_FIRESTARTER);
			output.accept(ModItems.BONE_CLUB);
			output.accept(ModItems.WOVEN_WOOL);
			output.accept(ModItems.SLEEPING_BAG);
			output.accept(ModItems.CLAY_MORTAR);
			output.accept(ModItems.UNFIRED_MORTAR_BRICK);
			output.accept(ModItems.MORTAR_BRICKS);
			output.accept(ModItems.MORTAR_BRICK_SLAB);
			output.accept(ModItems.CRUDE_CLAY_SLAB);
			output.accept(ModItems.SANDED_WOOD_SLAB);
			output.accept(ModItems.GRINDED_WOOD_SLAB);
			output.accept(ModItems.WOVEN_WOOD_SLAB);
			output.accept(ModItems.WOVEN_WOOD);
			output.accept(ModItems.BURNT_BRANCH_BLOCK);
			output.accept(ModItems.BURNT_BRANCH_SLAB);
			output.accept(ModItems.MORTAR_KILN_BASE);
			output.accept(ModItems.MORTAR_KILN);
			output.accept(ModItems.WOODEN_GRINDER_BASE);
			output.accept(ModItems.STONE_GRINDSAW);
			output.accept(ModItems.WOODEN_GRINDER);
			output.accept(ModItems.SANDING_STATION);
			output.accept(ModItems.WATER_BEARING_POT);
			output.accept(ModItems.WATER_POT);
			output.accept(ModItems.BOUND_WOOD_PROCESSOR);
			output.accept(ModItems.LUMBER_PROCESSOR);
			output.accept(ModItems.WOODEN_BOARD);
			output.accept(ModItems.WOODEN_BEAM);
			output.accept(ModItems.WOVEN_FAN);
			output.accept(ModItems.REED_BACKPACK);
			output.accept(ModItems.LEATHER_BACKPACK);
			output.accept(ModItems.BONE_BRUSH);
			output.accept(ModItems.MORTAR_CHEST);
			output.accept(ModItems.WOVEN_CHEST_FRAME);
			output.accept(ModItems.WOVEN_CHEST);
			output.accept(ModItems.GROUND_LEATHER);
			output.accept(ModItems.SAND_FLAKE);
			output.accept(ModItems.SAND_BALL);
			output.accept(ModItems.GLASS_SHARD);
			output.accept(ModItems.RAW_LEATHER);
			output.accept(ModItems.UNFIRED_KILN_GLASS);
			output.accept(ModItems.ADVANCED_KILN_GLASS);
			output.accept(ModItems.UNFIRED_CLAY_MOLD_BASE);
			output.accept(ModItems.UNFIRED_CLAY_AXE_MOLD);
			output.accept(ModItems.UNFIRED_CLAY_BRICK_MOLD);
			output.accept(ModItems.UNFIRED_CLAY_CUTTERS_MOLD);
			output.accept(ModItems.UNFIRED_CLAY_MULTITOOL_MOLD);
			output.accept(ModItems.UNFIRED_CLAY_POT_MOLD);
			output.accept(ModItems.UNFIRED_CLAY_BLOCK_MOLD);
			output.accept(ModItems.FIRED_CLAY_AXE_MOLD);
			output.accept(ModItems.FIRED_CLAY_BRICK_MOLD);
			output.accept(ModItems.FIRED_CLAY_CUTTERS_MOLD);
			output.accept(ModItems.FIRED_CLAY_MULTITOOL_MOLD);
			output.accept(ModItems.FIRED_CLAY_POT_MOLD);
			output.accept(ModItems.FIRED_CLAY_BLOCK_MOLD);
			output.accept(ModItems.CLAY_POT);
			output.accept(ModItems.FLINT_AXE_PIECES);
			output.accept(ModItems.FLINT_CUTTER_PIECE);
			output.accept(ModItems.REFINED_FLINT_MULTITOOL_PIECE);
			output.accept(ModItems.FLINT_AXE);
			output.accept(ModItems.FLINT_PLANT_CUTTERS);
			output.accept(ModItems.ADVANCED_FLINT_MULTITOOL);
			output.accept(ModItems.CRUSHED_STONE_BLOCK_ITEM);
			output.accept(ModItems.CRUSHED_STONE_SLAB);
			output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.SOAKED));

			for (ModBlocks.ForageBushDefinition bush : ModBlocks.FORAGE_BUSHES) {
				output.accept(ModItems.bushItem(bush.blockPath()));
			}
		})
		.build();

	private ModCreativeTabs() {
	}

	public static void initialize() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, PROJECT_FLINT_KEY, PROJECT_FLINT);
	}
}
