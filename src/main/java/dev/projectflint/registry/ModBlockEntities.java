package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.block.entity.BakedClayKilnBlockEntity;
import dev.projectflint.block.entity.AdvancedKilnBlockEntity;
import dev.projectflint.block.entity.PrimitiveCampfireBlockEntity;
import dev.projectflint.block.entity.CrudePlantTorchBlockEntity;
import dev.projectflint.block.entity.FlintTorchBlockEntity;
import dev.projectflint.block.entity.FatBurningTorchBlockEntity;
import dev.projectflint.block.entity.MortarChestBlockEntity;
import dev.projectflint.block.entity.WovenChestBlockEntity;
import dev.projectflint.block.entity.LumberChoppingBlockEntity;
import dev.projectflint.block.entity.WaterPotBlockEntity;
import dev.projectflint.block.entity.SandingStationBlockEntity;
import dev.projectflint.block.entity.WoodenChestBlockEntity;
import dev.projectflint.block.entity.BookshelfBlockEntity;
import dev.projectflint.block.entity.WoodenCabinetBlockEntity;
import dev.projectflint.block.entity.PrimitiveStoveBlockEntity;
import dev.projectflint.block.entity.BrickFurnaceBlockEntity;
import dev.projectflint.block.entity.NestBlockEntity;
import dev.projectflint.block.entity.WellPulleyBlockEntity;
import dev.projectflint.block.entity.FireclayForgeBlockEntity;
import dev.projectflint.block.entity.BasinBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static final BlockEntityType<BakedClayKilnBlockEntity> BAKED_CLAY_KILN = register(
		"baked_clay_kiln",
		FabricBlockEntityTypeBuilder.create(BakedClayKilnBlockEntity::new, ModBlocks.BAKED_CLAY_KILN, ModBlocks.MORTAR_KILN).build()
	);

	public static final BlockEntityType<CrudePlantTorchBlockEntity> CRUDE_PLANT_TORCH = register(
		"crude_plant_torch",
		FabricBlockEntityTypeBuilder.create(CrudePlantTorchBlockEntity::new, ModBlocks.CRUDE_PLANT_TORCH).build()
	);

	public static final BlockEntityType<FlintTorchBlockEntity> FLINT_TORCH = register(
		"flint_torch",
		FabricBlockEntityTypeBuilder.create(FlintTorchBlockEntity::new, ModBlocks.FLINT_TORCH).build()
	);

	public static final BlockEntityType<FatBurningTorchBlockEntity> FAT_BURNING_TORCH = register(
		"fat_burning_torch", FabricBlockEntityTypeBuilder.create(FatBurningTorchBlockEntity::new, ModBlocks.FAT_BURNING_TORCH).build());

	public static final BlockEntityType<PrimitiveCampfireBlockEntity> PRIMITIVE_CAMPFIRE = register(
		"primitive_campfire",
		FabricBlockEntityTypeBuilder.create(PrimitiveCampfireBlockEntity::new, ModBlocks.PRIMITIVE_CAMPFIRE).build()
	);

	public static final BlockEntityType<AdvancedKilnBlockEntity> ADVANCED_KILN = register(
		"advanced_kiln",
		FabricBlockEntityTypeBuilder.create(AdvancedKilnBlockEntity::new, ModBlocks.ADVANCED_KILN_CONTROLLER).build()
	);

	public static final BlockEntityType<MortarChestBlockEntity> MORTAR_CHEST = register(
		"mortar_chest",
		FabricBlockEntityTypeBuilder.create(MortarChestBlockEntity::new, ModBlocks.MORTAR_CHEST).build()
	);

	public static final BlockEntityType<SandingStationBlockEntity> SANDING_STATION = register(
		"sanding_station",
		FabricBlockEntityTypeBuilder.create(SandingStationBlockEntity::new, ModBlocks.SANDING_STATION).build()
	);

	public static final BlockEntityType<WaterPotBlockEntity> WATER_POT = register(
		"water_pot",
		FabricBlockEntityTypeBuilder.create(WaterPotBlockEntity::new, ModBlocks.WATER_POT).build()
	);

	public static final BlockEntityType<LumberChoppingBlockEntity> LUMBER_CHOPPING_BLOCK = register(
		"lumber_chopping_block",
		FabricBlockEntityTypeBuilder.create(LumberChoppingBlockEntity::new, ModBlocks.LUMBER_CHOPPING_BLOCK).build()
	);

	public static final BlockEntityType<WoodenChestBlockEntity> WOODEN_CHEST = register("wooden_chest", FabricBlockEntityTypeBuilder.create(WoodenChestBlockEntity::new, ModBlocks.WOODEN_CHEST).build());
	public static final BlockEntityType<BookshelfBlockEntity> BOOKSHELF = register("bookshelf", FabricBlockEntityTypeBuilder.create(BookshelfBlockEntity::new, ModBlocks.BOOKSHELF).build());
	public static final BlockEntityType<WoodenCabinetBlockEntity> WOODEN_CABINET = register("wooden_cabinet", FabricBlockEntityTypeBuilder.create(WoodenCabinetBlockEntity::new, ModBlocks.WOODEN_CABINET).build());
	public static final BlockEntityType<PrimitiveStoveBlockEntity> PRIMITIVE_STOVE = register("primitive_stove", FabricBlockEntityTypeBuilder.create(PrimitiveStoveBlockEntity::new, ModBlocks.PRIMITIVE_STOVE).build());
	public static final BlockEntityType<BrickFurnaceBlockEntity> BRICK_FURNACE = register("brick_furnace", FabricBlockEntityTypeBuilder.create(BrickFurnaceBlockEntity::new, ModBlocks.BRICK_FURNACE, ModBlocks.BRICK_BLAST_FURNACE).build());
	public static final BlockEntityType<NestBlockEntity> NEST = register("nest", FabricBlockEntityTypeBuilder.create(NestBlockEntity::new, ModBlocks.NEST).build());
	public static final BlockEntityType<WellPulleyBlockEntity> WELL_PULLEY = register("well_pulley", FabricBlockEntityTypeBuilder.create(WellPulleyBlockEntity::new, ModBlocks.WELL_PULLEY).build());
	public static final BlockEntityType<BasinBlockEntity> BASIN = register("basin", FabricBlockEntityTypeBuilder.create(BasinBlockEntity::new, ModBlocks.BASIN).build());
	public static final BlockEntityType<FireclayForgeBlockEntity> FIRECLAY_FORGE = register("fireclay_forge", FabricBlockEntityTypeBuilder.create(FireclayForgeBlockEntity::new, ModBlocks.FIRECLAY_FORGE_CONTROLLER).build());

	public static final BlockEntityType<WovenChestBlockEntity> WOVEN_CHEST = register(
		"woven_chest",
		FabricBlockEntityTypeBuilder.create(WovenChestBlockEntity::new, ModBlocks.WOVEN_CHEST).build()
	);

	private ModBlockEntities() {}

	private static <T extends BlockEntityType<?>> T register(String name, T type) {
		ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
	}

	public static void initialize() {}
}
