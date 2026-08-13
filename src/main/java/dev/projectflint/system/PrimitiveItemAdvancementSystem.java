package dev.projectflint.system;

import dev.projectflint.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

/** Fallback inventory sync so item milestones cannot remain stuck after the item is already obtained. */
public final class PrimitiveItemAdvancementSystem {
	private static final Map<Item, String> ITEM_ADVANCEMENTS = createTargets();

	private PrimitiveItemAdvancementSystem() {}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 10 != 0) return;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) awardOwnedItemAdvancements(player);
		});
	}

	private static void awardOwnedItemAdvancements(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			award(player, player.getInventory().getItem(slot));
		}
	}

	private static void award(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) return;
		String path = ITEM_ADVANCEMENTS.get(stack.getItem());
		if (path != null) ModAdvancements.awardAll(player, path);
	}

	private static Map<Item, String> createTargets() {
		Map<Item, String> map = new LinkedHashMap<>();
		map.put(ModItems.ROCK, "stone_in_hand");
		map.put(ModItems.REFINED_ROCK, "refined_rock");
		map.put(ModItems.VERY_REFINED_STONE, "very_refined_stone");
		map.put(ModItems.TWIG, "first_twigs");
		map.put(ModItems.LIGHTLY_BURNT_TWIG, "lightly_burnt_twig");
		map.put(ModItems.TORCH_HOLDER, "torch_holder");
		map.put(ModItems.UNLIT_FLINT_TORCH, "unlit_flint_torch");
		map.put(ModItems.FLINT_TORCH_HOLDER, "flint_torch_holder");
		map.put(ModItems.PLANT_FIBER, "leaf_litter_fiber");
		map.put(ModItems.CRUDE_PLANT_STRING, "crude_string");
		map.put(ModItems.CRUDE_STONE_HAMMER, "crude_hammer");
		map.put(ModItems.CRUDE_REED, "crude_reeds");
		map.put(ModItems.PLANT_TOOL_BINDING, "plant_tool_binding");
		map.put(ModItems.CRUDE_HARVESTING_TOOL, "crude_harvesting_tool");
		map.put(ModItems.CAMPFIRE_STONE, "campfire_stone");
		map.put(ModItems.BURNT_TWIG, "burnt_twig");
		map.put(ModItems.BRANCH, "branch");
		map.put(ModItems.BURNT_BRANCH, "burnt_branch");
		map.put(ModItems.CRUDE_MULTITOOL, "crude_multitool");
		map.put(ModItems.FLINT_FIRESTARTER, "flint_firestarter");
		map.put(ModItems.FLINT_MULTITOOL, "flint_multitool");
		map.put(ModItems.DIRT_PILE, "dirt_pile");
		map.put(ModItems.WATTLE, "wattle");
		map.put(ModItems.TIED_WATTLE, "tied_wattle");
		map.put(ModItems.MUD_PILE, "mud_pile");
		map.put(ModItems.DAUB_PILE, "daub_pile");
		map.put(ModItems.WET_DAUB_PILE, "wet_daub_pile");
		map.put(ModItems.UNFIRED_WATTLE_AND_DAUB, "unfired_wattle_and_daub");
		map.put(ModItems.WATTLED_DAUB_BRICKS, "wattled_daub_bricks");
		map.put(ModItems.WATTLED_DAUB_FLOOR_TILE, "wattled_daub_floor_tile");
		map.put(ModItems.WATTLED_DAUB_WINDOW, "wattled_daub_window");
		map.put(ModItems.FLINT_CHIP, "flint_chip");
		map.put(Items.FLINT, "vanilla_flint");
		map.put(ModItems.CRUDELY_WOVEN_BOWL, "crudely_woven_bowl");
		map.put(ModItems.UNFIRED_WOVEN_BOWL, "unfired_woven_bowl");
		map.put(ModItems.FIRED_CLAY_BOWL, "fired_clay_bowl");
		map.put(ModItems.UNFIRED_CLAY_BALL, "unfired_clay_ball");
		map.put(ModItems.SUPER_HEATED_CLAY, "super_heated_clay");
		map.put(ModItems.FIRED_CLAY, "fired_clay");
		map.put(ModItems.CRUDE_CLAY_BLOCK, "crude_clay_block");
		map.put(ModItems.CRUDE_CLAY_KILN, "crude_clay_kiln");
		map.put(ModItems.BAKED_CLAY_KILN, "baked_clay_kiln");
		map.put(ModItems.CLAY_GRINDING_BASE, "clay_grinding_base");
		map.put(ModItems.CLAY_GRINDER, "clay_grinder");
		map.put(ModItems.REED_ROLL, "reed_roll");
		map.put(ModItems.SANDING_PAPER, "sanding_paper");
		map.put(ModItems.CLAY_MORTAR, "clay_mortar");
		map.put(ModItems.UNFIRED_MORTAR_BRICK, "unfired_mortar_bricks");
		map.put(ModItems.MORTAR_BRICKS, "mortar_bricks");
		map.put(ModItems.SANDED_WOOD_SLAB, "sanded_wood_slab");
		map.put(ModItems.GRINDED_WOOD_SLAB, "grinded_wood_slab");
		map.put(ModItems.WOVEN_WOOD_SLAB, "woven_wood_slab");
		map.put(ModItems.WOVEN_WOOD, "woven_wood");
		map.put(ModItems.MORTAR_KILN_BASE, "mortar_kiln_base");
		map.put(ModItems.MORTAR_KILN, "mortar_kiln");
		map.put(ModItems.WOODEN_GRINDER_BASE, "wooden_grinder_base");
		map.put(ModItems.STONE_GRINDSAW, "stone_grindsaw");
		map.put(ModItems.WOODEN_GRINDER, "wooden_grinder");
		map.put(ModItems.WOVEN_FAN, "woven_fan");
		map.put(ModItems.REED_BACKPACK, "reed_backpack");
		map.put(ModItems.LEATHER_BACKPACK, "leather_backpack");
		map.put(ModItems.MORTAR_CHEST, "mortar_chest");
		map.put(ModItems.WOVEN_CHEST_FRAME, "woven_chest_frame");
		map.put(ModItems.WOVEN_CHEST, "woven_chest");
		map.put(ModItems.GROUND_LEATHER, "ground_leather");
		map.put(ModItems.FILED_BONE, "filed_bone");
		map.put(ModItems.BONE_CLUB, "bone_club");
		map.put(ModItems.BONE_FIRESTARTER, "bone_firestarter");
		map.put(ModItems.WOVEN_WOOL, "woven_wool");
		map.put(ModItems.SLEEPING_BAG, "sleeping_bag");
		map.put(ModItems.BONE_BRUSH, "bone_brush");
		map.put(ModItems.SAND_FLAKE, "sand_flake");
		map.put(ModItems.SAND_BALL, "sand_ball");
		map.put(ModItems.GLASS_SHARD, "glass_shard");
		map.put(ModItems.UNFIRED_KILN_GLASS, "unfired_kiln_glass");
		map.put(ModItems.ADVANCED_KILN_GLASS, "advanced_kiln_glass");
		map.put(ModItems.UNFIRED_CLAY_MOLD_BASE, "unfired_clay_mold_base");
		map.put(ModItems.UNFIRED_CLAY_POT_MOLD, "unfired_clay_pot_mold");
		map.put(ModItems.FIRED_CLAY_POT_MOLD, "fired_clay_pot_mold");
		map.put(ModItems.CLAY_POT, "clay_pot");
		map.put(ModItems.FLINT_CUTTER_PIECE, "flint_cutter_piece");
		map.put(ModItems.REFINED_FLINT_MULTITOOL_PIECE, "refined_flint_multitool_piece");
		map.put(ModItems.FLINT_AXE_PIECES, "flint_axe_pieces");
		map.put(ModItems.FLINT_AXE, "flint_axe");
		map.put(ModItems.FLINT_PLANT_CUTTERS, "flint_plant_cutters");
		map.put(ModItems.ADVANCED_FLINT_MULTITOOL, "advanced_flint_multitool");
		return Map.copyOf(map);
	}
}
