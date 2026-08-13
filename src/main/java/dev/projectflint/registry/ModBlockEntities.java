package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.block.entity.BakedClayKilnBlockEntity;
import dev.projectflint.block.entity.AdvancedKilnBlockEntity;
import dev.projectflint.block.entity.PrimitiveCampfireBlockEntity;
import dev.projectflint.block.entity.CrudePlantTorchBlockEntity;
import dev.projectflint.block.entity.FlintTorchBlockEntity;
import dev.projectflint.block.entity.MortarChestBlockEntity;
import dev.projectflint.block.entity.WovenChestBlockEntity;
import dev.projectflint.block.entity.LumberChoppingBlockEntity;
import dev.projectflint.block.entity.WaterPotBlockEntity;
import dev.projectflint.block.entity.SandingStationBlockEntity;
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
