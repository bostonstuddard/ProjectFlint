package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.item.CrudeHarvestingToolItem;
import dev.projectflint.item.CrudeMultitoolItem;
import dev.projectflint.item.CrudeStoneHammerItem;
import dev.projectflint.item.CrudePlantTorchItem;
import dev.projectflint.item.BoneBrushItem;
import dev.projectflint.item.FlintFirestarterItem;
import dev.projectflint.item.FlintMultitoolItem;
import dev.projectflint.item.AdvancedFlintMultitoolItem;
import dev.projectflint.item.AquiferFinderItem;
import dev.projectflint.item.FlintPlantCuttersItem;
import dev.projectflint.item.FlintAxeItem;
import dev.projectflint.item.RockItem;
import dev.projectflint.item.SuperHeatedClayItem;
import dev.projectflint.item.SweetBerrySeedsItem;
import dev.projectflint.item.WovenWoolItem;
import dev.projectflint.item.LumberProcessorItem;
import dev.projectflint.item.BoundWoodProcessorItem;
import dev.projectflint.item.WaterBearingPotItem;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ModItems {
	private static final ResourceKey<EquipmentAsset> REED_BACKPACK_EQUIPMENT_ASSET = ResourceKey.create(
		EquipmentAssets.ROOT_ID, ProjectFlint.id("reed_backpack")
	);
	private static final ResourceKey<EquipmentAsset> LEATHER_BACKPACK_EQUIPMENT_ASSET = ResourceKey.create(
		EquipmentAssets.ROOT_ID, ProjectFlint.id("leather_backpack")
	);
	public static final Item ROCK = register("rock", properties -> new RockItem(properties.stacksTo(8)));
	public static final Item REFINED_ROCK = register("refined_rock", properties -> new Item(properties.stacksTo(8)));
	public static final Item VERY_REFINED_STONE = register("very_refined_stone", properties -> new BlockItem(ModBlocks.VERY_REFINED_STONE_PILE, properties.stacksTo(8)));
	public static final Item TWIG = register("twig", properties -> new BlockItem(ModBlocks.TWIG_PILE, properties.stacksTo(12)));
	public static final Item LIGHTLY_BURNT_TWIG = register("lightly_burnt_twig", properties -> new BlockItem(ModBlocks.HAMMER_ASSEMBLY, properties.stacksTo(12)));
	public static final Item CRUDE_PLANT_TORCH = register("crude_plant_torch", CrudePlantTorchItem::new);
	public static final Item TORCH_HOLDER = register("torch_holder", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNLIT_FLINT_TORCH = register("unlit_flint_torch", properties -> new Item(properties.stacksTo(1)));
	public static final Item FLINT_TORCH_HOLDER = registerBlockItem("flint_torch_holder", ModBlocks.FLINT_TORCH, 1);
	public static final Item AQUIFER_FINDER = register("aquifer_finder", AquiferFinderItem::new);
	public static final Item PLANT_FIBER = register("plant_fiber", properties -> new BlockItem(ModBlocks.FIBER_WORK_PILE, properties.stacksTo(16)));

	public static final Item CRUDE_PLANT_STRING = register("crude_plant_string", properties -> new Item(properties.stacksTo(8)));
	public static final Item CRUDE_STONE_HAMMER = register("crude_stone_hammer", properties -> new CrudeStoneHammerItem(
		primitiveWeapon(properties.durability(24), 1.0D, -3.2D)
	));
	public static final Item CRUDE_REED = register("crude_reed", properties -> new BlockItem(ModBlocks.BINDING_ASSEMBLY, properties.stacksTo(12)));
	public static final Item PLANT_TOOL_BINDING = register("plant_tool_binding", properties -> new BlockItem(ModBlocks.HARVESTING_TOOL_ASSEMBLY, properties.stacksTo(16)));
	public static final Item CRUDE_HARVESTING_TOOL = register("crude_harvesting_tool", properties -> new CrudeHarvestingToolItem(
		primitiveWeapon(properties.durability(48), 0.0D, -2.8D)
	));
	public static final Item FLOWER_PETALS = register("flower_petals", properties -> new Item(properties.stacksTo(16)));
	public static final Item CAMPFIRE_STONE = register("campfire_stone", properties -> new BlockItem(ModBlocks.PRIMITIVE_CAMPFIRE, properties.stacksTo(GameplayTuning.CAMPFIRE_STONE_CAPACITY)));
	public static final Item BURNT_TWIG = register("burnt_twig", properties -> new Item(properties.stacksTo(12)));
	public static final Item BRANCH = register("branch", properties -> new Item(properties.stacksTo(16)));
	public static final Item BURNT_BRANCH = register("burnt_branch", properties -> new Item(properties.stacksTo(16)));
	public static final Item CRUDE_MULTITOOL = register("crude_multitool", properties -> new CrudeMultitoolItem(
		primitiveWeapon(properties.durability(GameplayTuning.MULTITOOL_DURABILITY), 1.0D, -2.8D)
	));
	public static final Item FLINT_FIRESTARTER = register("flint_firestarter", properties -> new FlintFirestarterItem(properties.durability(GameplayTuning.FLINT_FIRESTARTER_DURABILITY).stacksTo(1)));
	public static final Item FLINT_MULTITOOL = register("flint_multitool", properties -> new FlintMultitoolItem(
		primitiveWeapon(properties.durability(GameplayTuning.FLINT_MULTITOOL_DURABILITY), 2.5D, -2.8D)
	));
	public static final Item DIRT_PILE = register("dirt_pile", properties -> new BlockItem(ModBlocks.DIRT_PILE, properties.stacksTo(32)));
	public static final Item WATTLE = register("wattle", properties -> new Item(properties.stacksTo(16)));
	public static final Item TIED_WATTLE = register("tied_wattle", properties -> new Item(properties.stacksTo(16)));
	public static final Item MUD_PILE = register("mud_pile", properties -> new Item(properties.stacksTo(16)));
	public static final Item DAUB_PILE = register("daub_pile", properties -> new Item(properties.stacksTo(16)));
	public static final Item WET_DAUB_PILE = register("wet_daub_pile", properties -> new Item(properties.stacksTo(16)));
	public static final Item UNFIRED_WATTLE_AND_DAUB = register("unfired_wattle_and_daub", properties -> new Item(properties.stacksTo(16)));
	public static final Item WATTLED_DAUB_BRICKS = registerBlockItem("wattled_daub_bricks", ModBlocks.WATTLED_DAUB_BRICKS, 16);
	public static final Item WATTLED_DAUB_FLOOR_TILE = registerBlockItem("wattled_daub_floor_tile", ModBlocks.WATTLED_DAUB_FLOOR_TILE, 16);
	public static final Item WATTLED_DAUB_WINDOW = registerBlockItem("wattled_daub_window", ModBlocks.WATTLED_DAUB_WINDOW, 16);
	public static final Item WATTLED_DAUB_DOOR = register("wattled_daub_door", properties -> new DoubleHighBlockItem(ModBlocks.WATTLED_DAUB_DOOR, properties.stacksTo(1)));
	public static final Item LOOSE_DIRT = register("loose_dirt", properties -> new BlockItem(ModBlocks.LOOSE_DIRT, properties.stacksTo(8)));
	public static final Item LOOSE_SOIL = register("loose_soil", properties -> new BlockItem(ModBlocks.LOOSE_SOIL, properties.stacksTo(8)));
	public static final Item LOOSE_DIRT_SLAB = register("loose_dirt_slab", properties -> new BlockItem(ModBlocks.LOOSE_DIRT_SLAB, properties.stacksTo(8)));
	public static final Item FLINT_CHIP = register("flint_chip", properties -> new Item(properties.stacksTo(32)));
	public static final Item CRUDELY_WOVEN_BOWL = register("crudely_woven_bowl", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_WOVEN_BOWL = register("unfired_woven_bowl", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_BOWL = register("fired_clay_bowl", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_BOWL_2 = register("fired_clay_bowl_2", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_BOWL_4 = register("fired_clay_bowl_4", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_BOWL_6 = register("fired_clay_bowl_6", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_BALL = register("unfired_clay_ball", properties -> new BlockItem(ModBlocks.CLAY_FIRING_PILE, properties.stacksTo(24)));
	public static final Item SUPER_HEATED_CLAY = register("super_heated_clay", properties -> new SuperHeatedClayItem(properties.stacksTo(24)));
	public static final Item FIRED_CLAY = register("fired_clay", properties -> new BlockItem(ModBlocks.FIRED_CLAY_PILE, properties.stacksTo(24)));
	public static final Item CRUDE_CLAY_BLOCK = register("crude_clay_block", properties -> new BlockItem(ModBlocks.CRUDE_CLAY_BLOCK, properties.stacksTo(1)));
	public static final Item CRUDE_CLAY_KILN = register("crude_clay_kiln", properties -> new BlockItem(ModBlocks.CRUDE_CLAY_KILN, properties.stacksTo(1)));
	public static final Item BAKED_CLAY_KILN = register("baked_clay_kiln", properties -> new BlockItem(
		ModBlocks.BAKED_CLAY_KILN,
		properties.stacksTo(1).overrideDescription("item.projectflint.baked_clay_kiln")
	));
	public static final Item BAKED_KILN_TOP = register("baked_kiln_top", properties -> new BlockItem(
		ModBlocks.BAKED_KILN_TOP,
		properties.stacksTo(1).overrideDescription("item.projectflint.baked_kiln_top")
	));
	public static final Item CLAY_GRINDING_BASE = register("clay_grinding_base", properties -> new BlockItem(ModBlocks.CLAY_GRINDING_BASE, properties.stacksTo(1)));
	public static final Item CLAY_GRINDER = register("clay_grinder", properties -> new BlockItem(ModBlocks.CLAY_GRINDER, properties.stacksTo(1)));
	public static final Item SCRAMBLED_EGG = register("scrambled_egg", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.625F).build())));
	public static final Item RAW_CALAMARI = register("raw_calamari", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.10F).build())));
	public static final Item COOKED_CALAMARI = register("cooked_calamari", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
	public static final Item RAW_FOX_MEAT = register("raw_fox_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.15F).build())));
	public static final Item COOKED_FOX_MEAT = register("cooked_fox_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.55F).build())));
	public static final Item RAW_FROG_MEAT = register("raw_frog_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.05F).build())));
	public static final Item COOKED_FROG_MEAT = register("cooked_frog_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.45F).build())));
	public static final Item RAW_GOAT_MEAT = register("raw_goat_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.15F).build())));
	public static final Item COOKED_GOAT_MEAT = register("cooked_goat_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.70F).build())));
	public static final Item RAW_HORSE_MEAT = register("raw_horse_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.15F).build())));
	public static final Item COOKED_HORSE_MEAT = register("cooked_horse_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.75F).build())));
	public static final Item RAW_LLAMA_MEAT = register("raw_llama_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.15F).build())));
	public static final Item COOKED_LLAMA_MEAT = register("cooked_llama_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.65F).build())));
	public static final Item RAW_DOLPHIN_MEAT = register("raw_dolphin_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.15F).build())));
	public static final Item COOKED_DOLPHIN_MEAT = register("cooked_dolphin_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.65F).build())));
	public static final Item RAW_BEAR_MEAT = register("raw_bear_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.25F).build())));
	public static final Item COOKED_BEAR_MEAT = register("cooked_bear_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.80F).build())));
	public static final Item BLACK_BEAR_SPAWN_EGG = register("black_bear_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.BLACK_BEAR)));
	public static final Item BROWN_BEAR_SPAWN_EGG = register("brown_bear_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.BROWN_BEAR)));
	public static final Item RAW_CAMEL_MEAT = register("raw_camel_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.25F).build())));
	public static final Item COOKED_CAMEL_MEAT = register("cooked_camel_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.75F).build())));
	public static final Item PLANT_SEEDS = register("plant_seeds", properties -> new Item(properties.stacksTo(24).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.75F).build())));
	public static final Item SWEET_BERRY_SEEDS = register("sweet_berry_seeds", properties -> new SweetBerrySeedsItem(properties.stacksTo(16)));
	public static final Item PUMPKIN_SLICE = register("pumpkin_slice", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.25F).build())));
	public static final Item SWEET_BERRY_JAM = register("sweet_berry_jam", properties -> new Item(properties.stacksTo(16)));
	public static final Item SWEET_BERRY_PIE = register("sweet_berry_pie", properties -> new Item(properties.stacksTo(16)));
	public static final Item REED_ROLL = register("reed_roll", properties -> new Item(properties.stacksTo(12)));
	public static final Item SUGARCANE_ROLL = register("sugarcane_roll", properties -> new Item(properties.stacksTo(12)));
	public static final Item SANDING_PAPER = register("sanding_paper", properties -> new Item(properties.stacksTo(12)));
	public static final Item WOOL = register("wool", properties -> new Item(properties.stacksTo(32)));
	public static final Item ANIMAL_BONES = register("animal_bones", properties -> new Item(properties.stacksTo(32)));
	public static final Item FILED_BONE = register("filed_bone", properties -> new Item(properties.stacksTo(32)));
	public static final Item BONE_FIRESTARTER = register("bone_firestarter", properties -> new FlintFirestarterItem(properties.durability(GameplayTuning.BONE_FIRESTARTER_DURABILITY).stacksTo(1)));
	public static final Item BONE_CLUB = register("bone_club", properties -> new Item(primitiveWeapon(properties.durability(160), 3.2D, -3.0D)));
	public static final Item WOVEN_WOOL = register("woven_wool", properties -> new WovenWoolItem(properties.stacksTo(24)));
	public static final Item SLEEPING_BAG = register("sleeping_bag", properties -> new BedItem(
		ModBlocks.SLEEPING_BAG,
		properties.stacksTo(1).overrideDescription("item.projectflint.sleeping_bag")
	));
	public static final Item CLAY_MORTAR = register("clay_mortar", properties -> new Item(properties.stacksTo(32)));
	public static final Item UNFIRED_MORTAR_BRICK = register("unfired_mortar_brick", properties -> new Item(properties.stacksTo(24)));
	public static final Item MORTAR_BRICKS = registerBlockItem("mortar_bricks", ModBlocks.MORTAR_BRICKS, 32);
	public static final Item MORTAR_BRICK_SLAB = registerBlockItem("mortar_brick_slab", ModBlocks.MORTAR_BRICK_SLAB, 8);
	public static final Item CRUDE_CLAY_SLAB = registerBlockItem("crude_clay_slab", ModBlocks.CRUDE_CLAY_SLAB);
	public static final Item SANDED_WOOD_SLAB = registerBlockItem("sanded_wood_slab", ModBlocks.SANDED_WOOD_SLAB);
	public static final Item GRINDED_WOOD_SLAB = registerBlockItem("grinded_wood_slab", ModBlocks.GRINDED_WOOD_SLAB);
	public static final Item WOVEN_WOOD_SLAB = registerBlockItem("woven_wood_slab", ModBlocks.WOVEN_WOOD_SLAB);
	public static final Item WOVEN_WOOD = registerBlockItem("woven_wood", ModBlocks.WOVEN_WOOD);
	public static final Item BURNT_BRANCH_BLOCK = registerBlockItem("burnt_branch_block", ModBlocks.BURNT_BRANCH_BLOCK);
	public static final Item BURNT_BRANCH_SLAB = registerBlockItem("burnt_branch_slab", ModBlocks.BURNT_BRANCH_SLAB);
	public static final Item MORTAR_KILN_BASE = registerBlockItem("mortar_kiln_base", ModBlocks.MORTAR_KILN_BASE, 1);
	public static final Item MORTAR_KILN = registerBlockItem("mortar_kiln", ModBlocks.MORTAR_KILN, 1);
	public static final Item WOODEN_GRINDER_BASE = registerBlockItem("wooden_grinder_base", ModBlocks.WOODEN_GRINDER_BASE, 1);
	public static final Item STONE_GRINDSAW = register("stone_grindsaw", properties -> new Item(properties.stacksTo(1)));
	public static final Item WOODEN_GRINDER = registerBlockItem("wooden_grinder", ModBlocks.WOODEN_GRINDER, 1);
	public static final Item SANDING_STATION = registerBlockItem("sanding_station", ModBlocks.SANDING_STATION, 1);
	public static final Item WATER_BEARING_POT = register("water_bearing_pot", properties -> new WaterBearingPotItem(properties, false));
	public static final Item WATER_BEARING_POT_FILLED = register("water_bearing_pot_filled", properties -> new WaterBearingPotItem(properties, true));
	public static final Item WATER_POT = registerBlockItem("water_pot", ModBlocks.WATER_POT, 1);
	public static final Item BOUND_WOOD_PROCESSOR = register("bound_wood_processor", BoundWoodProcessorItem::new);
	public static final Item LUMBER_PROCESSOR = register("lumber_processor", LumberProcessorItem::new);
	public static final Item WOODEN_BOARD = register("wooden_board", properties -> new Item(properties.stacksTo(16)));
	public static final Item WOODEN_BEAM = register("wooden_beam", properties -> new Item(properties.stacksTo(8)));
	public static final Item WOVEN_FAN = register("woven_fan", properties -> new Item(properties.stacksTo(8)));
	public static final Item REED_BACKPACK = register("reed_backpack", ModItems::reedBackpack);
	public static final Item LEATHER_BACKPACK = register("leather_backpack", ModItems::leatherBackpack);
	public static final Item BONE_BRUSH = register("bone_brush", properties -> new BoneBrushItem(properties.durability(160).stacksTo(1)));
	public static final Item MORTAR_CHEST = registerBlockItem("mortar_chest", ModBlocks.MORTAR_CHEST, 2);
	public static final Item WOVEN_CHEST_FRAME = register("woven_chest_frame", properties -> new Item(properties.stacksTo(1)));
	public static final Item WOVEN_CHEST = registerBlockItem("woven_chest", ModBlocks.WOVEN_CHEST, 2);
	public static final Item SAND_FLAKE = register("sand_flake", properties -> new Item(properties.stacksTo(32)));
	public static final Item SAND_BALL = register("sand_ball", properties -> new Item(properties.stacksTo(32)));
	public static final Item GLASS_SHARD = register("glass_shard", properties -> new Item(properties.stacksTo(8)));
	public static final Item RAW_LEATHER = register("raw_leather", properties -> new Item(properties.stacksTo(16)));
	public static final Item GROUND_LEATHER = register("ground_leather", properties -> new Item(properties.stacksTo(16)));
	public static final Item UNFIRED_KILN_GLASS = register("unfired_kiln_glass", properties -> new Item(properties.stacksTo(1)));
	public static final Item ADVANCED_KILN_GLASS = register("advanced_kiln_glass", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_MOLD_BASE = register("unfired_clay_mold_base", properties -> new Item(properties.stacksTo(4)));
	public static final Item UNFIRED_CLAY_AXE_MOLD = register("unfired_clay_axe_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_BRICK_MOLD = register("unfired_clay_brick_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_CUTTERS_MOLD = register("unfired_clay_cutters_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_MULTITOOL_MOLD = register("unfired_clay_multitool_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_POT_MOLD = register("unfired_clay_pot_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item UNFIRED_CLAY_BLOCK_MOLD = register("unfired_clay_block_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_AXE_MOLD = register("fired_clay_axe_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_BRICK_MOLD = register("fired_clay_brick_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_CUTTERS_MOLD = register("fired_clay_cutters_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_MULTITOOL_MOLD = register("fired_clay_multitool_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_POT_MOLD = register("fired_clay_pot_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item FIRED_CLAY_BLOCK_MOLD = register("fired_clay_block_mold", properties -> new Item(properties.stacksTo(1)));
	public static final Item CLAY_POT = register("clay_pot", properties -> new Item(properties.stacksTo(1)));
	public static final Item CLAY_POT_2 = register("clay_pot_2", properties -> new Item(properties.stacksTo(1)));
	public static final Item FLINT_AXE_PIECES = register("flint_axe_pieces", properties -> new Item(properties.stacksTo(1)));
	public static final Item FLINT_CUTTER_PIECE = register("flint_cutter_piece", properties -> new Item(properties.stacksTo(1)));
	public static final Item REFINED_FLINT_MULTITOOL_PIECE = register("refined_flint_multitool_piece", properties -> new Item(properties.stacksTo(1)));
	public static final Item FLINT_AXE = register("flint_axe", properties -> new FlintAxeItem(
		primitiveWeapon(primitiveUtilityTool(properties, 256, flintAxeTool()), 4.75D, -3.4D)
	));
	public static final Item FLINT_PLANT_CUTTERS = register("flint_plant_cutters", properties -> new FlintPlantCuttersItem(
		primitiveWeapon(primitiveUtilityTool(properties, 320, flintPlantCuttersTool()), 3.25D, -2.6D)
	));
	public static final Item ADVANCED_FLINT_MULTITOOL = register("advanced_flint_multitool", properties -> new AdvancedFlintMultitoolItem(
		primitiveWeapon(primitiveUtilityTool(properties, 512, advancedFlintMultitoolTool()), 3.85D, -2.75D)
	));

	public static final Item LOOSE_ROCK_BLOCK_ITEM = registerBlockItem("loose_rock", ModBlocks.LOOSE_ROCK, 8);
	public static final Item CRUSHED_STONE_BLOCK_ITEM = register("crushed_stone", properties -> new BlockItem(
		ModBlocks.CRUSHED_STONE,
		properties.stacksTo(8).overrideDescription("item.projectflint.crushed_stone")
	));
	public static final Item CRUSHED_STONE_SLAB = registerBlockItem("crushed_stone_slab", ModBlocks.CRUSHED_STONE_SLAB, 8);
	private static final Map<String, Item> BUSH_ITEMS = registerBushItems();
	@SuppressWarnings("unused")
	private static final Map<String, Item> LEGACY_BUSH_ITEMS = registerLegacyBushItems();

	private ModItems() {
	}

	private static Item.Properties primitiveUtilityTool(Item.Properties properties, int maxDamage, Tool tool) {
		return properties
			.durability(maxDamage)
			.component(DataComponents.TOOL, tool);
	}

	private static Tool flintAxeTool() {
		HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return new Tool(
			List.of(Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.LOGS), 1.0F)),
			1.0F,
			1,
			true
		);
	}

	private static Tool flintPlantCuttersTool() {
		HolderSet<Block> vegetation = HolderSet.direct(
			Blocks.SUGAR_CANE.builtInRegistryHolder(),
			Blocks.SHORT_GRASS.builtInRegistryHolder()
		);
		return new Tool(
			List.of(Tool.Rule.overrideSpeed(vegetation, 1.0F)),
			1.0F,
			1,
			true
		);
	}

	private static Tool advancedFlintMultitoolTool() {
		HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		HolderSet<Block> directTargets = HolderSet.direct(
			Blocks.STONE.builtInRegistryHolder(),
			Blocks.GRAVEL.builtInRegistryHolder(),
			Blocks.SUGAR_CANE.builtInRegistryHolder(),
			Blocks.SHORT_GRASS.builtInRegistryHolder(),
			Blocks.TALL_GRASS.builtInRegistryHolder(),
			Blocks.LEAF_LITTER.builtInRegistryHolder()
		);
		return new Tool(
			List.of(
				Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.LEAVES), 1.0F),
				Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.DIRT), 1.0F),
				Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.FLOWERS), 1.0F),
				Tool.Rule.overrideSpeed(directTargets, 1.0F)
			),
			1.0F,
			1,
			true
		);
	}

	private static Item.Properties primitiveWeapon(Item.Properties properties, double attackDamageModifier, double attackSpeedModifier) {
		ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamageModifier, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeedModifier, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();

		return properties
			.attributes(attributes)
			.component(DataComponents.WEAPON, new Weapon(1));
	}

	private static Item reedBackpack(Item.Properties properties) {
		ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
			.add(
				Attributes.ARMOR,
				new AttributeModifier(ProjectFlint.id("reed_backpack_armor"), 0.5D, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.CHEST
			)
			.build();
		Equippable equippable = Equippable.builder(EquipmentSlot.CHEST)
			.setAsset(REED_BACKPACK_EQUIPMENT_ASSET)
			.build();
		return new Item(properties.stacksTo(1)
			.component(DataComponents.EQUIPPABLE, equippable)
			.attributes(attributes));
	}

	private static Item leatherBackpack(Item.Properties properties) {
		ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
			.add(
				Attributes.ARMOR,
				new AttributeModifier(ProjectFlint.id("leather_backpack_armor"), 2.0D, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.CHEST
			)
			.build();
		// Minecraft 1.21.11 only copies chest equipment into HumanoidRenderState when
		// the Equippable component has an equipment asset id. ReedBackpackLayer reads
		// that render-state stack, so Leather must have an asset id too. The matching
		// equipment JSON intentionally has no vanilla layers; it exists only to make
		// the stack renderable to the custom backpack layer.
		Equippable equippable = Equippable.builder(EquipmentSlot.CHEST)
			.setAsset(LEATHER_BACKPACK_EQUIPMENT_ASSET)
			.build();
		return new Item(properties.stacksTo(1)
			.component(DataComponents.EQUIPPABLE, equippable)
			.attributes(attributes));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ProjectFlint.id(name));
		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	private static Item registerBlockItem(String name, net.minecraft.world.level.block.Block block) {
		return register(name, properties -> new BlockItem(block, properties));
	}

	private static Item registerBlockItem(String name, net.minecraft.world.level.block.Block block, int maxStackSize) {
		return register(name, properties -> new BlockItem(block, properties.stacksTo(maxStackSize)));
	}

	private static Map<String, Item> registerBushItems() {
		Map<String, Item> items = new LinkedHashMap<>();
		for (ModBlocks.ForageBushDefinition bush : ModBlocks.FORAGE_BUSHES) {
			items.put(bush.blockPath(), registerBlockItem(bush.blockPath(), bush.block()));
		}
		return Map.copyOf(items);
	}

	private static Map<String, Item> registerLegacyBushItems() {
		Map<String, Item> items = new LinkedHashMap<>();
		for (ModBlocks.ForageBushDefinition bush : ModBlocks.LEGACY_INACTIVE_BUSHES) {
			items.put(bush.blockPath(), registerBlockItem(bush.blockPath(), bush.block()));
		}
		return Map.copyOf(items);
	}

	public static Item bushItem(String blockPath) {
		Item item = BUSH_ITEMS.get(blockPath);
		if (item == null) {
			throw new IllegalArgumentException("No active forage bush item registered for " + blockPath);
		}
		return item;
	}

	public static boolean isHammerLike(ItemStack stack) {
		return ToolCapabilities.has(stack, ToolCapabilities.Capability.HAMMER);
	}

	public static boolean isCutterLike(ItemStack stack) {
		return ToolCapabilities.has(stack, ToolCapabilities.Capability.CUTTER);
	}

	public static boolean isMultitool(ItemStack stack) {
		return ToolCapabilities.has(stack, ToolCapabilities.Capability.MULTITOOL);
	}

	public static boolean isPlantCutterLike(ItemStack stack) {
		return isCutterLike(stack) || stack.is(FLINT_PLANT_CUTTERS);
	}

	public static boolean canHarvestSweetBerrySeeds(ItemStack stack) {
		return stack.is(FLINT_PLANT_CUTTERS) || stack.is(ADVANCED_FLINT_MULTITOOL);
	}

	public static boolean canHarvestGourdFlesh(ItemStack stack) {
		return stack.is(FLINT_PLANT_CUTTERS) || stack.is(ADVANCED_FLINT_MULTITOOL);
	}

	public static boolean isFlintTier(ItemStack stack) {
		return ToolCapabilities.has(stack, ToolCapabilities.Capability.FLINT_TIER);
	}

	public static boolean isFirestarter(ItemStack stack) {
		return ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER);
	}

	public static void initialize() {
		// Class loading performs registry registration.
	}
}
