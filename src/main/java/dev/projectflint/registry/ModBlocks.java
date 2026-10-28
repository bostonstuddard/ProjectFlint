package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.block.BindingAssemblyBlock;
import dev.projectflint.block.MortarBricksBlock;
import dev.projectflint.block.AdvancedToolAssemblyBlock;
import dev.projectflint.block.AdvancedKilnCasingBlock;
import dev.projectflint.block.AdvancedKilnControllerBlock;
import dev.projectflint.block.BakedClayKilnBlock;
import dev.projectflint.block.BakedKilnTopBlock;
import dev.projectflint.block.ClayFiringPileBlock;
import dev.projectflint.block.ClayGrinderBlock;
import dev.projectflint.block.ClayGrindingBaseBlock;
import dev.projectflint.block.CrudeClayKilnBlock;
import dev.projectflint.block.CrushedStoneBlock;
import dev.projectflint.block.FlaxCropBlock;
import dev.projectflint.block.CrudePlantTorchBlock;
import dev.projectflint.block.FlintTorchBlock;
import dev.projectflint.block.FatBurningTorchBlock;
import dev.projectflint.block.FiredClayPileBlock;
import dev.projectflint.block.FallingSlabBlock;
import dev.projectflint.block.FlintToolAssemblyBlock;
import dev.projectflint.block.KilnTopFormBlock;
import dev.projectflint.block.LooseRockBlock;
import dev.projectflint.block.WovenBowlAssemblyBlock;
import dev.projectflint.block.DirtPileBlock;
import dev.projectflint.block.FiberWorkPileBlock;
import dev.projectflint.block.HammerAssemblyBlock;
import dev.projectflint.block.HarvestingToolAssemblyBlock;
import dev.projectflint.block.MultitoolAssemblyBlock;
import dev.projectflint.block.MortarChestBlock;
import dev.projectflint.block.MortarKilnBlock;
import dev.projectflint.block.WoodenGrinderBlock;
import dev.projectflint.block.WovenChestBlock;
import dev.projectflint.block.PrimitiveCampfireBlock;
import dev.projectflint.block.RockPileBlock;
import dev.projectflint.block.SleepingBagBlock;
import dev.projectflint.block.StrawberryBushBlock;
import dev.projectflint.block.TwigPileBlock;
import dev.projectflint.block.VeryRefinedStonePileBlock;
import dev.projectflint.block.WovenGearAssemblyBlock;
import dev.projectflint.block.WattledDaubBricksBlock;
import dev.projectflint.block.WattledDaubWindowBlock;
import dev.projectflint.block.WattledDaubFloorTileBlock;
import dev.projectflint.block.WattledDaubDoorBlock;
import dev.projectflint.block.LumberWorkpieceBlock;
import dev.projectflint.block.LumberChoppingBlock;
import dev.projectflint.block.WaterPotBlock;
import dev.projectflint.block.SandingStationBlock;
import dev.projectflint.block.WoodenChestBlock;
import dev.projectflint.block.BookshelfBlock;
import dev.projectflint.block.WoodenCabinetBlock;
import dev.projectflint.block.PrimitiveStoveBlock;
import dev.projectflint.block.BrickFurnaceBlock;
import dev.projectflint.block.BrickBlastFurnaceBlock;
import dev.projectflint.block.RopeBlock;
import dev.projectflint.block.NestBlock;
import dev.projectflint.block.WellPulleyBlock;
import dev.projectflint.block.PrototypeStationBlock;
import dev.projectflint.block.BasinBlock;
import dev.projectflint.block.HardwoodCraftingTableBlock;
import dev.projectflint.block.FireclayForgeControllerBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public final class ModBlocks {
	public static final Block LOOSE_ROCK = registerLooseRock("loose_rock");
	public static final TwigPileBlock TWIG_PILE = registerTwigPile("twig_pile");
	public static final CrudePlantTorchBlock CRUDE_PLANT_TORCH = registerCrudePlantTorch("crude_plant_torch");
	public static final FlintTorchBlock FLINT_TORCH = registerFlintTorch("flint_torch");
	public static final FatBurningTorchBlock FAT_BURNING_TORCH = registerFatTorch("fat_burning_torch");
	public static final Block HARDWOOD_CRAFTING_TABLE = registerHardwoodTable("hardwood_crafting_table");
	public static final FiberWorkPileBlock FIBER_WORK_PILE = registerFiberWorkPile("fiber_work_pile");
	public static final HammerAssemblyBlock HAMMER_ASSEMBLY = registerHammerAssembly("hammer_assembly");
	public static final BindingAssemblyBlock BINDING_ASSEMBLY = registerBindingAssembly("binding_assembly");
	public static final HarvestingToolAssemblyBlock HARVESTING_TOOL_ASSEMBLY = registerHarvestingToolAssembly("harvesting_tool_assembly");
	public static final RockPileBlock ROCK_PILE = registerRockPile("rock_pile");
	public static final VeryRefinedStonePileBlock VERY_REFINED_STONE_PILE = registerVeryRefinedStonePile("very_refined_stone_pile");
	public static final PrimitiveCampfireBlock PRIMITIVE_CAMPFIRE = registerPrimitiveCampfire("primitive_campfire");
	public static final MultitoolAssemblyBlock MULTITOOL_ASSEMBLY = registerMultitoolAssembly("multitool_assembly");
	public static final DirtPileBlock DIRT_PILE = registerDirtPile("dirt_pile");
	public static final Block LOOSE_DIRT = registerLooseDirt("loose_dirt");
	public static final Block LOOSE_SOIL = registerLooseSoil("loose_soil");
	public static final SlabBlock LOOSE_DIRT_SLAB = registerLooseDirtSlab("loose_dirt_slab");
	public static final FlintToolAssemblyBlock FLINT_TOOL_ASSEMBLY = registerFlintToolAssembly("flint_tool_assembly");
	public static final WovenBowlAssemblyBlock WOVEN_BOWL_ASSEMBLY = registerWovenBowlAssembly("woven_bowl_assembly");
	public static final ClayFiringPileBlock CLAY_FIRING_PILE = registerClayFiringPile("clay_firing_pile");
	public static final FiredClayPileBlock FIRED_CLAY_PILE = registerFiredClayPile("fired_clay_pile");
	public static final Block CRUDE_CLAY_BLOCK = registerCrudeClayBlock("crude_clay_block");
	public static final CrudeClayKilnBlock CRUDE_CLAY_KILN = registerCrudeClayKiln("crude_clay_kiln");
	public static final ClayGrindingBaseBlock CLAY_GRINDING_BASE = registerClayGrindingBase("clay_grinding_base");
	public static final ClayGrinderBlock CLAY_GRINDER = registerClayGrinder("clay_grinder");
	public static final BakedClayKilnBlock BAKED_CLAY_KILN = registerBakedClayKiln("baked_clay_kiln");
	public static final KilnTopFormBlock KILN_TOP_FORM = registerKilnTopForm("kiln_top_form");
	public static final BakedKilnTopBlock BAKED_KILN_TOP = registerBakedKilnTop("baked_kiln_top");
	public static final CrushedStoneBlock CRUSHED_STONE = registerCrushedStone("crushed_stone");
	public static final SlabBlock CRUSHED_STONE_SLAB = registerCrushedStoneSlab("crushed_stone_slab");
	public static final SleepingBagBlock SLEEPING_BAG = registerSleepingBag("sleeping_bag");
	public static final MortarBricksBlock MORTAR_BRICKS = registerMortarBricks("mortar_bricks");
	public static final SlabBlock MORTAR_BRICK_SLAB = registerMortarBrickSlab("mortar_brick_slab");
	public static final SlabBlock CRUDE_CLAY_SLAB = registerCrudeClaySlab("crude_clay_slab");
	public static final SlabBlock SANDED_WOOD_SLAB = registerProcessedWoodSlab("sanded_wood_slab");
	public static final SlabBlock GRINDED_WOOD_SLAB = registerProcessedWoodSlab("grinded_wood_slab");
	public static final SlabBlock WOVEN_WOOD_SLAB = registerProcessedWoodSlab("woven_wood_slab");
	public static final Block WOVEN_WOOD = registerProcessedWoodBlock("woven_wood");
	public static final Block BURNT_BRANCH_BLOCK = registerBurntBranchBlock("burnt_branch_block");
	public static final SlabBlock BURNT_BRANCH_SLAB = registerBurntBranchSlab("burnt_branch_slab");
	public static final Block MORTAR_KILN_BASE = registerMortarKilnBase("mortar_kiln_base");
	public static final MortarKilnBlock MORTAR_KILN = registerMortarKiln("mortar_kiln");
	public static final Block WOODEN_GRINDER_BASE = registerWoodenGrinderBase("wooden_grinder_base");
	public static final WoodenGrinderBlock WOODEN_GRINDER = registerWoodenGrinder("wooden_grinder");
	public static final SandingStationBlock SANDING_STATION = registerSandingStation("sanding_station");
	public static final WaterPotBlock WATER_POT = registerWaterPot("water_pot");
	public static final LumberChoppingBlock LUMBER_CHOPPING_BLOCK = registerLumberChoppingBlock("lumber_chopping_block");
	public static final LumberWorkpieceBlock LUMBER_WORKPIECE = registerLumberWorkpiece("lumber_workpiece");
	public static final WovenGearAssemblyBlock WOVEN_GEAR_ASSEMBLY = registerWovenGearAssembly("woven_gear_assembly");
	public static final MortarChestBlock MORTAR_CHEST = registerMortarChest("mortar_chest");
	public static final WovenChestBlock WOVEN_CHEST = registerWovenChest("woven_chest");
	public static final AdvancedKilnControllerBlock ADVANCED_KILN_CONTROLLER = registerAdvancedKilnController("advanced_kiln_controller");
	public static final AdvancedKilnCasingBlock ADVANCED_KILN_CASING = registerAdvancedKilnCasing("advanced_kiln_casing");
	public static final AdvancedToolAssemblyBlock ADVANCED_TOOL_ASSEMBLY = registerAdvancedToolAssembly("advanced_tool_assembly");
	public static final WattledDaubBricksBlock WATTLED_DAUB_BRICKS = registerWattledDaubBricks("wattled_daub_bricks");
	public static final WattledDaubWindowBlock WATTLED_DAUB_WINDOW = registerWattledDaubWindow("wattled_daub_window");
	public static final WattledDaubFloorTileBlock WATTLED_DAUB_FLOOR_TILE = registerWattledDaubFloorTile("wattled_daub_floor_tile");
	public static final WattledDaubDoorBlock WATTLED_DAUB_DOOR = registerWattledDaubDoor("wattled_daub_door");
	public static final Block WOODEN_BEAM = registerBeam("wooden_beam");
	public static final Block ACACIA_WOODEN_BEAM = registerBeam("acacia_wooden_beam");
	public static final Block BIRCH_WOODEN_BEAM = registerBeam("birch_wooden_beam");
	public static final Block CHERRY_WOODEN_BEAM = registerBeam("cherry_wooden_beam");
	public static final Block CRIMSON_WOODEN_BEAM = registerBeam("crimson_wooden_beam");
	public static final Block DARK_OAK_WOODEN_BEAM = registerBeam("dark_oak_wooden_beam");
	public static final Block JUNGLE_WOODEN_BEAM = registerBeam("jungle_wooden_beam");
	public static final Block MANGROVE_WOODEN_BEAM = registerBeam("mangrove_wooden_beam");
	public static final Block PALE_OAK_WOODEN_BEAM = registerBeam("pale_oak_wooden_beam");
	public static final Block SPRUCE_WOODEN_BEAM = registerBeam("spruce_wooden_beam");
	public static final Block WARPED_WOODEN_BEAM = registerBeam("warped_wooden_beam");
	public static final Block SAWMILL = registerSawmill("sawmill");
	public static final WoodenChestBlock WOODEN_CHEST = registerWoodenChest("wooden_chest");
	public static final BookshelfBlock BOOKSHELF = registerBookshelf("bookshelf");
	public static final Block LIMESTONE = registerLimestoneBlock("limestone");
	public static final Block COBBLED_LIMESTONE = registerLimestoneFallingBlock("cobbled_limestone", 0xB9B08FFF);
	public static final Block SMOOTH_LIMESTONE = registerLimestoneFallingBlock("smooth_limestone", 0xD4C9A8FF);
	public static final Block LIMESTONE_BRICKS = registerLimestoneBlock("limestone_bricks");
	public static final Block CHISELED_LIMESTONE = registerLimestoneBlock("chiseled_limestone");
	public static final Block POLISHED_LIMESTONE = registerLimestoneBlock("polished_limestone");
	public static final Block CHECKERED_TILE = registerLimestoneFallingBlock("checkered_tile", 0xA8A39BFF);
	public static final Block GLAZED_CHECKERED_TILE = registerLimestoneFallingBlock("glazed_checkered_tile", 0xB5B0A7FF);
	public static final WoodenCabinetBlock WOODEN_CABINET = registerWoodenCabinet("wooden_cabinet");
	public static final PrimitiveStoveBlock PRIMITIVE_STOVE = registerPrimitiveStove("primitive_stove");
	public static final BrickFurnaceBlock BRICK_FURNACE = registerBrickFurnace("brick_furnace");
	public static final BrickBlastFurnaceBlock BRICK_BLAST_FURNACE = registerBrickBlastFurnace("brick_blast_furnace");
	public static final Block LOOSE_MUD_BRICKS = registerLooseMudBricks("loose_mud_bricks");
	public static final SlabBlock LOOSE_MUD_BRICK_SLAB = registerLooseMudBrickSlab("loose_mud_brick_slab");
	public static final Block BASIN = registerPrototypeStation("basin");
	public static final Block DRYING_RACK = registerPrototypeStation("drying_rack");
	public static final Block MILLSTONE = registerPrototypeStation("millstone");
	public static final Block FIRECLAY_BRICK_BLOCK = registerFireclayBlock("fireclay_brick_block");
	public static final FireclayForgeControllerBlock FIRECLAY_FORGE_CONTROLLER = registerForge("fireclay_forge_controller");
	public static final SlabBlock FIRECLAY_BRICK_SLAB = registerFireclaySlab("fireclay_brick_slab");
	public static final StairBlock FIRECLAY_BRICK_STAIRS = registerFireclayStairs("fireclay_brick_stairs");
	public static final FlaxCropBlock FLAX_CROP = registerFlaxCrop("flax_crop");
	public static final StrawberryBushBlock STRAWBERRY_BUSH = registerStrawberryBush("strawberry_bush");
	public static final RopeBlock ROPE = registerRope("rope");
	public static final NestBlock NEST = registerNest("nest");
	public static final SlabBlock BRICK_SHINGLES = registerBrickShingles("brick_shingles");
	public static final WellPulleyBlock WELL_PULLEY = registerWellPulley("well_pulley");


	/**
	 * Active alpha.8 forage ecology. Hot/arid, shore, snowy, and swamp biomes are
	 * intentionally excluded from primitive woody forage generation.
	 */
	public static final List<ForageBushDefinition> FORAGE_BUSHES = List.of(
		bush("plains", Biomes.PLAINS),
		bush("sunflower_plains", Biomes.SUNFLOWER_PLAINS),
		bush("forest", Biomes.FOREST),
		bush("flower_forest", Biomes.FLOWER_FOREST),
		bush("birch_forest", Biomes.BIRCH_FOREST),
		bush("dark_forest", Biomes.DARK_FOREST),
		bush("pale_garden", Biomes.PALE_GARDEN),
		bush("old_growth_birch_forest", Biomes.OLD_GROWTH_BIRCH_FOREST),
		bush("old_growth_pine_taiga", Biomes.OLD_GROWTH_PINE_TAIGA),
		bush("old_growth_spruce_taiga", Biomes.OLD_GROWTH_SPRUCE_TAIGA),
		bush("taiga", Biomes.TAIGA),
		bush("windswept_hills", Biomes.WINDSWEPT_HILLS),
		bush("windswept_gravelly_hills", Biomes.WINDSWEPT_GRAVELLY_HILLS),
		bush("windswept_forest", Biomes.WINDSWEPT_FOREST),
		bush("jungle", Biomes.JUNGLE),
		bush("sparse_jungle", Biomes.SPARSE_JUNGLE),
		bush("bamboo_jungle", Biomes.BAMBOO_JUNGLE),
		bush("meadow", Biomes.MEADOW),
		bush("cherry_grove", Biomes.CHERRY_GROVE),
		bush("stony_peaks", Biomes.STONY_PEAKS),
		bush("mushroom_fields", Biomes.MUSHROOM_FIELDS)
	);

	/** Registry compatibility for forage bushes from older test worlds. */
	public static final List<ForageBushDefinition> LEGACY_INACTIVE_BUSHES = List.of(
		legacyBush("beach", Biomes.BEACH),
		legacyBush("snowy_beach", Biomes.SNOWY_BEACH),
		legacyBush("stony_shore", Biomes.STONY_SHORE),
		legacyBush("desert", Biomes.DESERT),
		legacyBush("savanna", Biomes.SAVANNA),
		legacyBush("savanna_plateau", Biomes.SAVANNA_PLATEAU),
		legacyBush("windswept_savanna", Biomes.WINDSWEPT_SAVANNA),
		legacyBush("badlands", Biomes.BADLANDS),
		legacyBush("eroded_badlands", Biomes.ERODED_BADLANDS),
		legacyBush("wooded_badlands", Biomes.WOODED_BADLANDS),
		legacyBush("snowy_plains", Biomes.SNOWY_PLAINS),
		legacyBush("ice_spikes", Biomes.ICE_SPIKES),
		legacyBush("swamp", Biomes.SWAMP),
		legacyBush("mangrove_swamp", Biomes.MANGROVE_SWAMP),
		legacyBush("snowy_taiga", Biomes.SNOWY_TAIGA),
		legacyBush("grove", Biomes.GROVE),
		legacyBush("snowy_slopes", Biomes.SNOWY_SLOPES),
		legacyBush("frozen_peaks", Biomes.FROZEN_PEAKS),
		legacyBush("jagged_peaks", Biomes.JAGGED_PEAKS)
	);

	private ModBlocks() {}

	private static ForageBushDefinition bush(String biomePath, ResourceKey<Biome> biome) {
		String blockPath = biomePath + "_bush";
		return new ForageBushDefinition(blockPath, biome, registerBush(blockPath));
	}

	private static ForageBushDefinition legacyBush(String biomePath, ResourceKey<Biome> biome) {
		String blockPath = biomePath + "_bush";
		return new ForageBushDefinition(blockPath, biome, registerBush(blockPath));
	}

	private static TwigPileBlock registerTwigPile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		TwigPileBlock block = new TwigPileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.1F).sound(SoundType.WOOD).lightLevel(state -> state.getValue(TwigPileBlock.LIT) ? 10 : 0));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static CrudePlantTorchBlock registerCrudePlantTorch(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new CrudePlantTorchBlock(
			BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable()
				.strength(-1.0F, 3600000.0F).sound(SoundType.WOOD).lightLevel(state -> 6)
		));
	}

	private static FlintTorchBlock registerFlintTorch(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FlintTorchBlock(
			BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable()
				.strength(0.15F).sound(SoundType.STONE)
				.lightLevel(state -> state.getValue(FlintTorchBlock.LIT) ? 12 : 0)
		));
	}

	private static FatBurningTorchBlock registerFatTorch(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FatBurningTorchBlock(
			BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion()
				.strength(0.15F).sound(SoundType.WOOD)
				.lightLevel(state -> state.getValue(FatBurningTorchBlock.LIT) ? 12 : 0)
		));
	}

	private static FiberWorkPileBlock registerFiberWorkPile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FiberWorkPileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.05F).sound(SoundType.GRASS)));
	}

	private static HammerAssemblyBlock registerHammerAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new HammerAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.05F).sound(SoundType.WOOD)));
	}

	private static BindingAssemblyBlock registerBindingAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new BindingAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.05F).sound(SoundType.GRASS)));
	}

	private static HarvestingToolAssemblyBlock registerHarvestingToolAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new HarvestingToolAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.05F).sound(SoundType.WOOD)));
	}

	private static RockPileBlock registerRockPile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new RockPileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.08F).sound(SoundType.STONE)));
	}

	private static VeryRefinedStonePileBlock registerVeryRefinedStonePile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new VeryRefinedStonePileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.08F).sound(SoundType.STONE)));
	}

	private static PrimitiveCampfireBlock registerPrimitiveCampfire(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new PrimitiveCampfireBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().noLootTable().strength(0.35F).sound(SoundType.STONE).lightLevel(state -> state.getValue(PrimitiveCampfireBlock.LIT) ? 12 : 0)));
	}

	private static MultitoolAssemblyBlock registerMultitoolAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new MultitoolAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.06F).sound(SoundType.WOOD)));
	}

	private static DirtPileBlock registerDirtPile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new DirtPileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.05F).sound(SoundType.GRAVEL)));
	}

	private static Block registerLooseDirt(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		Block block = new ColoredFallingBlock(new ColorRGBA(0x8A5A35FF), BlockBehaviour.Properties.of().setId(key).noLootTable().strength(0.5F).sound(SoundType.GRAVEL));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static SlabBlock registerLooseDirtSlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FallingSlabBlock(BlockBehaviour.Properties.of().setId(key).strength(0.5F).sound(SoundType.GRAVEL)));
	}

	private static Block registerLooseSoil(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ColoredFallingBlock(
			new ColorRGBA(0x8A5A35FF),
			BlockBehaviour.Properties.of().setId(key).strength(0.5F).sound(SoundType.GRAVEL)
		));
	}

	private static FlintToolAssemblyBlock registerFlintToolAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FlintToolAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.06F).sound(SoundType.WOOD)));
	}

	private static WovenBowlAssemblyBlock registerWovenBowlAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WovenBowlAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.08F).sound(SoundType.ROOTED_DIRT)));
	}

	private static ClayFiringPileBlock registerClayFiringPile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ClayFiringPileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.10F).sound(SoundType.GRAVEL).lightLevel(state -> state.getValue(ClayFiringPileBlock.LIT) ? 10 : 0)));
	}

	private static FiredClayPileBlock registerFiredClayPile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FiredClayPileBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.12F).sound(SoundType.DECORATED_POT)));
	}

	private static Block registerCrudeClayBlock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ColoredFallingBlock(new ColorRGBA(0xA86444FF), BlockBehaviour.Properties.of().setId(key).strength(0.65F).sound(SoundType.GRAVEL)));
	}


	private static ClayGrindingBaseBlock registerClayGrindingBase(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ClayGrindingBaseBlock(BlockBehaviour.Properties.of().setId(key).strength(0.7F).sound(SoundType.DECORATED_POT)));
	}

	private static ClayGrinderBlock registerClayGrinder(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ClayGrinderBlock(BlockBehaviour.Properties.of().setId(key).strength(0.9F).sound(SoundType.STONE)));
	}

	private static CrudeClayKilnBlock registerCrudeClayKiln(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new CrudeClayKilnBlock(BlockBehaviour.Properties.of().setId(key).strength(0.75F).sound(SoundType.GRAVEL).lightLevel(state -> state.getValue(CrudeClayKilnBlock.LIT) ? 12 : 0)));
	}

	private static BakedClayKilnBlock registerBakedClayKiln(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new BakedClayKilnBlock(BlockBehaviour.Properties.of().setId(key).strength(1.2F).sound(SoundType.DECORATED_POT).lightLevel(state -> state.getValue(BakedClayKilnBlock.LIT) ? 13 : 0)));
	}

	private static KilnTopFormBlock registerKilnTopForm(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new KilnTopFormBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(-1.0F, 3600000.0F).sound(SoundType.GRAVEL)));
	}

	private static BakedKilnTopBlock registerBakedKilnTop(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new BakedKilnTopBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(-1.0F, 3600000.0F).sound(SoundType.DECORATED_POT)));
	}

	private static CrushedStoneBlock registerCrushedStone(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new CrushedStoneBlock(
			new ColorRGBA(0x777777FF),
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.5F).sound(SoundType.STONE)
		));
	}

	private static SlabBlock registerCrushedStoneSlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FallingSlabBlock(
			BlockBehaviour.Properties.of().setId(key).strength(1.25F).sound(SoundType.STONE)
		));
	}

	private static SleepingBagBlock registerSleepingBag(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new SleepingBagBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().noLootTable().strength(0.2F).sound(SoundType.WOOL)));
	}

	private static MortarBricksBlock registerMortarBricks(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new MortarBricksBlock(BlockBehaviour.Properties.of().setId(key).strength(1.2F).sound(SoundType.DECORATED_POT)));
	}


	private static SlabBlock registerMortarBrickSlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new SlabBlock(BlockBehaviour.Properties.of().setId(key).strength(1.2F).sound(SoundType.DECORATED_POT)));
	}

	private static SlabBlock registerCrudeClaySlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FallingSlabBlock(BlockBehaviour.Properties.of().setId(key).strength(0.65F).sound(SoundType.GRAVEL)));
	}

	private static SlabBlock registerProcessedWoodSlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new SlabBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(0.8F).sound(SoundType.WOOD)));
	}

	private static Block registerProcessedWoodBlock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.0F).sound(SoundType.WOOD)));
	}

	private static Block registerBurntBranchBlock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ColoredFallingBlock(new ColorRGBA(0x4B2D1DFF), BlockBehaviour.Properties.of().setId(key).strength(0.9F).sound(SoundType.WOOD)));
	}

	private static SlabBlock registerBurntBranchSlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FallingSlabBlock(BlockBehaviour.Properties.of().setId(key).strength(0.75F).sound(SoundType.WOOD)));
	}

	private static Block registerMortarKilnBase(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).strength(1.4F).sound(SoundType.DECORATED_POT)));
	}

	private static MortarKilnBlock registerMortarKiln(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new MortarKilnBlock(BlockBehaviour.Properties.of().setId(key).strength(1.6F).sound(SoundType.DECORATED_POT).lightLevel(state -> state.getValue(MortarKilnBlock.LIT) ? 13 : 0)));
	}

	private static Block registerWoodenGrinderBase(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).strength(1.1F).sound(SoundType.WOOD)));
	}

	private static WoodenGrinderBlock registerWoodenGrinder(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WoodenGrinderBlock(BlockBehaviour.Properties.of().setId(key).strength(1.2F).sound(SoundType.WOOD)));
	}

	private static SandingStationBlock registerSandingStation(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new SandingStationBlock(BlockBehaviour.Properties.of().setId(key).strength(1.0F).sound(SoundType.WOOD)));
	}

	private static WaterPotBlock registerWaterPot(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WaterPotBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(0.65F).sound(SoundType.DECORATED_POT)));
	}

	private static LumberChoppingBlock registerLumberChoppingBlock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new LumberChoppingBlock(BlockBehaviour.Properties.of().setId(key).strength(1.15F).sound(SoundType.WOOD)));
	}

	private static LumberWorkpieceBlock registerLumberWorkpiece(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new LumberWorkpieceBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(0.7F).sound(SoundType.WOOD)));
	}

	private static WovenGearAssemblyBlock registerWovenGearAssembly(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WovenGearAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.08F).sound(SoundType.GRASS)));
	}

	private static MortarChestBlock registerMortarChest(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new MortarChestBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.2F).sound(SoundType.DECORATED_POT)));
	}

	private static WovenChestBlock registerWovenChest(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WovenChestBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.1F).sound(SoundType.WOOD)));
	}


	private static AdvancedKilnControllerBlock registerAdvancedKilnController(String name) {
		ResourceKey<Block> key=ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK,key,new AdvancedKilnControllerBlock(BlockBehaviour.Properties.of().setId(key).strength(1.6F).sound(SoundType.DECORATED_POT).lightLevel(state -> state.getValue(AdvancedKilnControllerBlock.ACTIVE) ? 10 : 0)));
	}

	private static AdvancedKilnCasingBlock registerAdvancedKilnCasing(String name) {
		ResourceKey<Block> key=ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK,key,new AdvancedKilnCasingBlock(BlockBehaviour.Properties.of().setId(key).strength(1.6F).sound(SoundType.DECORATED_POT).lightLevel(state -> state.getValue(AdvancedKilnCasingBlock.ACTIVE) ? 10 : 0)));
	}

	private static AdvancedToolAssemblyBlock registerAdvancedToolAssembly(String name) {
		ResourceKey<Block> key=ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK,key,new AdvancedToolAssemblyBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().strength(0.08F).sound(SoundType.WOOD)));
	}


	private static WattledDaubBricksBlock registerWattledDaubBricks(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WattledDaubBricksBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(0.45F).sound(SoundType.GRAVEL)
		));
	}

	private static WattledDaubWindowBlock registerWattledDaubWindow(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WattledDaubWindowBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().noOcclusion().strength(0.45F).sound(SoundType.GRAVEL)
		));
	}

	private static WattledDaubFloorTileBlock registerWattledDaubFloorTile(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WattledDaubFloorTileBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(0.45F).sound(SoundType.GRAVEL)
		));
	}

	private static WattledDaubDoorBlock registerWattledDaubDoor(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WattledDaubDoorBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().noOcclusion().strength(0.45F).sound(SoundType.WOOD)
		));
	}


	private static WoodenChestBlock registerWoodenChest(String name) { ResourceKey<Block> key=ResourceKey.create(Registries.BLOCK,ProjectFlint.id(name)); return Registry.register(BuiltInRegistries.BLOCK,key,new WoodenChestBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.5F).sound(SoundType.WOOD))); }
	private static BookshelfBlock registerBookshelf(String name) { ResourceKey<Block> key=ResourceKey.create(Registries.BLOCK,ProjectFlint.id(name)); return Registry.register(BuiltInRegistries.BLOCK,key,new BookshelfBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.3F).sound(SoundType.WOOD))); }

	private static Block registerLimestoneBlock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.7F).sound(SoundType.STONE)));
	}

	private static Block registerLimestoneFallingBlock(String name, int dustColor) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ColoredFallingBlock(new ColorRGBA(dustColor), BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.35F).sound(SoundType.STONE)));
	}

	private static WoodenCabinetBlock registerWoodenCabinet(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WoodenCabinetBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().noOcclusion().strength(1.4F).sound(SoundType.WOOD)));
	}

	private static PrimitiveStoveBlock registerPrimitiveStove(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new PrimitiveStoveBlock(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.8F).sound(SoundType.STONE).lightLevel(state -> state.getValue(PrimitiveStoveBlock.LIT) ? 13 : 0)));
	}

	private static BrickFurnaceBlock registerBrickFurnace(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new BrickFurnaceBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(2.0F).sound(SoundType.STONE)
				.lightLevel(state -> state.getValue(BrickFurnaceBlock.LIT) ? 13 : 0)
		));
	}

	private static BrickBlastFurnaceBlock registerBrickBlastFurnace(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new BrickBlastFurnaceBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(2.2F).sound(SoundType.STONE)
				.lightLevel(state -> state.getValue(BrickBlastFurnaceBlock.LIT) ? 13 : 0)
		));
	}

	private static Block registerLooseMudBricks(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new ColoredFallingBlock(
			new ColorRGBA(0x8D6A4FFF),
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.1F).sound(SoundType.MUD_BRICKS)
		));
	}

	private static SlabBlock registerLooseMudBrickSlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FallingSlabBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.0F).sound(SoundType.MUD_BRICKS)
		));
	}

	private static FlaxCropBlock registerFlaxCrop(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FlaxCropBlock(
			BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable().randomTicks().instabreak().sound(SoundType.CROP)
		));
	}

	private static RopeBlock registerRope(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new RopeBlock(
			BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().strength(0.25F).sound(SoundType.WOOL)
		));
	}

	private static Block registerBeam(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.5F).sound(SoundType.WOOD)));
	}

	private static Block registerSawmill(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.5F).sound(SoundType.WOOD)));
	}

	private static Block registerLooseRock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new LooseRockBlock(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().strength(0.4F).sound(SoundType.STONE)));
	}

	private static Block registerStone(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().strength(0.4F).sound(SoundType.STONE)));
	}

	private static Block registerBush(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().strength(0.2F).sound(SoundType.GRASS)));
	}

	private static FireclayForgeControllerBlock registerForge(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new FireclayForgeControllerBlock(
			BlockBehaviour.Properties.of().setId(key).strength(2.0F).sound(SoundType.DECORATED_POT)
				.lightLevel(state -> state.getValue(FireclayForgeControllerBlock.LIT) ? 11 : 0)
		));
	}

	private static Block registerHardwoodTable(String name) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, new HardwoodCraftingTableBlock(
            BlockBehaviour.Properties.of().setId(key).strength(2.0F).sound(SoundType.WOOD)));
    }

	private static Block registerPrototypeStation(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, name.equals("basin") ? new BasinBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.5F).sound(SoundType.WOOD)) : new PrototypeStationBlock(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.5F).sound(SoundType.WOOD)));
	}

	private static Block registerFireclayBlock(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.of().setId(key).strength(1.8F).sound(SoundType.DECORATED_POT)));
	}

	private static SlabBlock registerFireclaySlab(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new SlabBlock(BlockBehaviour.Properties.of().setId(key).strength(1.8F).sound(SoundType.DECORATED_POT)));
	}

	private static StairBlock registerFireclayStairs(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new StairBlock(FIRECLAY_BRICK_BLOCK.defaultBlockState(), BlockBehaviour.Properties.of().setId(key).strength(1.8F).sound(SoundType.DECORATED_POT)));
	}

	private static StrawberryBushBlock registerStrawberryBush(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new StrawberryBushBlock(
			BlockBehaviour.Properties.of().setId(key).noCollision().noOcclusion().noLootTable()
				.randomTicks().strength(0.2F).sound(SoundType.SWEET_BERRY_BUSH)
		));
	}

	private static SlabBlock registerBrickShingles(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new SlabBlock(
			BlockBehaviour.Properties.of().setId(key).strength(1.1F).sound(SoundType.DECORATED_POT)
		));
	}

	private static WellPulleyBlock registerWellPulley(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new WellPulleyBlock(
			BlockBehaviour.Properties.of().setId(key).noLootTable().strength(1.5F).sound(SoundType.WOOD)
		));
	}

	public static boolean isWoodenBeam(net.minecraft.world.level.block.state.BlockState state) {
		return state.is(WOODEN_BEAM)
			|| state.is(ACACIA_WOODEN_BEAM)
			|| state.is(BIRCH_WOODEN_BEAM)
			|| state.is(CHERRY_WOODEN_BEAM)
			|| state.is(CRIMSON_WOODEN_BEAM)
			|| state.is(DARK_OAK_WOODEN_BEAM)
			|| state.is(JUNGLE_WOODEN_BEAM)
			|| state.is(MANGROVE_WOODEN_BEAM)
			|| state.is(PALE_OAK_WOODEN_BEAM)
			|| state.is(SPRUCE_WOODEN_BEAM)
			|| state.is(WARPED_WOODEN_BEAM);
	}

	private static NestBlock registerNest(String name) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new NestBlock(
			BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(0.15F).sound(SoundType.GRASS)
		));
	}

	public static void initialize() {}

	public record ForageBushDefinition(String blockPath, ResourceKey<Biome> biome, Block block) {}
}
