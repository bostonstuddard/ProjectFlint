package dev.projectflint;

import dev.projectflint.network.ModNetworking;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModEffects;
import dev.projectflint.registry.ModEntities;
import dev.projectflint.registry.ModPotions;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModCreativeTabs;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModMenus;
import dev.projectflint.registry.ModRecipes;
import dev.projectflint.system.AnimalRetaliationSystem;
import dev.projectflint.system.BearBehaviorSystem;
import dev.projectflint.system.FoodBalanceSystem;
import dev.projectflint.system.FiniteWaterSystem;
import dev.projectflint.system.PrimitiveInteractionSystem;
import dev.projectflint.system.PrimitiveEnvironmentEffectSystem;
import dev.projectflint.system.PrimitiveAnimalDropSystem;
import dev.projectflint.system.PrimitiveInventorySystem;
import dev.projectflint.system.PassiveMobTuningSystem;
import dev.projectflint.system.PrimitiveWorldRulesSystem;
import dev.projectflint.system.PrimitiveWorldSetupSystem;
import dev.projectflint.system.PrimitiveConditionSystem;
import dev.projectflint.system.PlacedItemSystem;
import dev.projectflint.system.PrimitiveItemAdvancementSystem;
import dev.projectflint.system.WildWolfAggressionSystem;
import dev.projectflint.world.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectFlint implements ModInitializer {
	public static final String MOD_ID = "projectflint";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModEffects.initialize();
		ModPotions.initialize();
		ModEntities.initialize();
		ModItems.initialize();
		ModRecipes.initialize();
		ModMenus.initialize();
		ModCreativeTabs.initialize();
		ModNetworking.initialize();
		FoodBalanceSystem.initialize();
		FiniteWaterSystem.initialize();
		PrimitiveInventorySystem.initialize();
		PrimitiveInteractionSystem.initialize();
		PassiveMobTuningSystem.initialize();
		PrimitiveWorldRulesSystem.initialize();
		PrimitiveWorldSetupSystem.initialize();
		PrimitiveConditionSystem.initialize();
		PlacedItemSystem.initialize();
		PrimitiveItemAdvancementSystem.initialize();
		AnimalRetaliationSystem.initialize();
		BearBehaviorSystem.initialize();
		PrimitiveEnvironmentEffectSystem.initialize();
		PrimitiveAnimalDropSystem.initialize();
		WildWolfAggressionSystem.initialize();
		ModWorldgen.initialize();

		LOGGER.info("Project Flint initialized");
	}
}
