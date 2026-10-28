package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Single source of truth for timed primitive heat-processing.
 *
 * Both the permanent campfire and baked kiln use this table. Station differences
 * are explicit per recipe instead of being duplicated as block-specific if-chains.
 */
public final class PrimitiveProcessingRegistry {
	private PrimitiveProcessingRegistry() {}

	private static List<PrimitiveProcessingRecipe> createRecipes() {
		Set<PrimitiveProcessor> both = Set.of(PrimitiveProcessor.CAMPFIRE, PrimitiveProcessor.KILN);
		Set<PrimitiveProcessor> kilnOnly = Set.of(PrimitiveProcessor.KILN);
		return List.of(
			recipe("advanced_kiln_glass", ModItems.UNFIRED_KILN_GLASS, new ItemStack(ModItems.ADVANCED_KILN_GLASS), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("unfired_clay_mold_base", ModItems.CRUDE_CLAY_BLOCK, new ItemStack(ModItems.UNFIRED_CLAY_MOLD_BASE), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("fired_clay_axe_mold", ModItems.UNFIRED_CLAY_AXE_MOLD, new ItemStack(ModItems.FIRED_CLAY_AXE_MOLD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("fired_clay_brick_mold", ModItems.UNFIRED_CLAY_BRICK_MOLD, new ItemStack(ModItems.FIRED_CLAY_BRICK_MOLD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("fired_clay_cutters_mold", ModItems.UNFIRED_CLAY_CUTTERS_MOLD, new ItemStack(ModItems.FIRED_CLAY_CUTTERS_MOLD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("fired_clay_multitool_mold", ModItems.UNFIRED_CLAY_MULTITOOL_MOLD, new ItemStack(ModItems.FIRED_CLAY_MULTITOOL_MOLD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("fired_clay_pot_mold", ModItems.UNFIRED_CLAY_POT_MOLD, new ItemStack(ModItems.FIRED_CLAY_POT_MOLD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("clay_bucket", ModItems.UNFIRED_CLAY_BUCKET, new ItemStack(ModItems.CLAY_BUCKET), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("fireclay_brick", ModItems.UNFIRED_FIRECLAY_BRICK, new ItemStack(ModItems.FIRECLAY_BRICK), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("brick", ModItems.UNFIRED_BRICK, new ItemStack(Items.BRICK), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("fired_clay_block_mold", ModItems.UNFIRED_CLAY_BLOCK_MOLD, new ItemStack(ModItems.FIRED_CLAY_BLOCK_MOLD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("super_heated_clay", ModItems.UNFIRED_CLAY_BALL, new ItemStack(ModItems.SUPER_HEATED_CLAY), GameplayTuning.KILN_SUPER_HEATED_CLAY_TICKS, kilnOnly),
			recipe("scrambled_egg", Items.EGG, new ItemStack(ModItems.SCRAMBLED_EGG, 2), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("plant_seeds", ModItems.PLANT_FIBER, new ItemStack(ModItems.PLANT_SEEDS), GameplayTuning.PLANT_FIBER_COOK_TICKS, both),
			recipe("fired_clay_bowl", ModItems.UNFIRED_WOVEN_BOWL, new ItemStack(ModItems.FIRED_CLAY_BOWL), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("wattled_daub_bricks", ModItems.UNFIRED_WATTLE_AND_DAUB, new ItemStack(ModItems.WATTLED_DAUB_BRICKS, 3), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("burnt_twig", ModItems.TWIG, new ItemStack(ModItems.BURNT_TWIG), 0, both),
			recipe("burnt_branch", ModItems.BRANCH, new ItemStack(ModItems.BURNT_BRANCH), 0, both),
			recipe("mortar_bricks", ModItems.UNFIRED_MORTAR_BRICK, new ItemStack(ModItems.MORTAR_BRICKS), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("limestone", ModItems.SMOOTH_LIMESTONE, new ItemStack(ModItems.LIMESTONE), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("cooked_calamari", ModItems.RAW_CALAMARI, new ItemStack(ModItems.COOKED_CALAMARI), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_turtle_meat", ModItems.RAW_TURTLE_MEAT, new ItemStack(ModItems.COOKED_TURTLE_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_fox_meat", ModItems.RAW_FOX_MEAT, new ItemStack(ModItems.COOKED_FOX_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_frog_meat", ModItems.RAW_FROG_MEAT, new ItemStack(ModItems.COOKED_FROG_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_goat_meat", ModItems.RAW_GOAT_MEAT, new ItemStack(ModItems.COOKED_GOAT_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_horse_meat", ModItems.RAW_HORSE_MEAT, new ItemStack(ModItems.COOKED_HORSE_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_llama_meat", ModItems.RAW_LLAMA_MEAT, new ItemStack(ModItems.COOKED_LLAMA_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_dolphin_meat", ModItems.RAW_DOLPHIN_MEAT, new ItemStack(ModItems.COOKED_DOLPHIN_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_bear_meat", ModItems.RAW_BEAR_MEAT, new ItemStack(ModItems.COOKED_BEAR_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_camel_meat", ModItems.RAW_CAMEL_MEAT, new ItemStack(ModItems.COOKED_CAMEL_MEAT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("glass_shard", ModItems.SAND_BALL, new ItemStack(ModItems.GLASS_SHARD), GameplayTuning.KILN_MEAT_COOK_TICKS, kilnOnly),
			recipe("cooked_beef", Items.BEEF, new ItemStack(Items.COOKED_BEEF), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_porkchop", Items.PORKCHOP, new ItemStack(Items.COOKED_PORKCHOP), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_chicken", Items.CHICKEN, new ItemStack(Items.COOKED_CHICKEN), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_mutton", Items.MUTTON, new ItemStack(Items.COOKED_MUTTON), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_rabbit", Items.RABBIT, new ItemStack(Items.COOKED_RABBIT), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_cod", Items.COD, new ItemStack(Items.COOKED_COD), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("cooked_salmon", Items.SALMON, new ItemStack(Items.COOKED_SALMON), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("baked_potato", Items.POTATO, new ItemStack(Items.BAKED_POTATO), GameplayTuning.KILN_MEAT_COOK_TICKS, both),
			recipe("dried_kelp", Items.KELP, new ItemStack(Items.DRIED_KELP), GameplayTuning.KILN_MEAT_COOK_TICKS, both)
		);
	}


	private static PrimitiveProcessingRecipe recipe(String id, Ingredient input, ItemStack output, int ticks, Set<PrimitiveProcessor> processors) {
		return new PrimitiveProcessingRecipe(id, input, output, ticks, processors);
	}

	private static PrimitiveProcessingRecipe recipe(String id, net.minecraft.world.level.ItemLike input, ItemStack output, int ticks, Set<PrimitiveProcessor> processors) {
		return new PrimitiveProcessingRecipe(id, Ingredient.of(input), output, ticks, processors);
	}

	private static final class Holder {
		private static final List<PrimitiveProcessingRecipe> RECIPES = createRecipes();
	}

	public static List<PrimitiveProcessingRecipe> recipesFor(PrimitiveProcessor processor) {
		return Holder.RECIPES.stream().filter(recipe -> recipe.processors().contains(processor)).toList();
	}

	public static Optional<PrimitiveProcessingRecipe> find(PrimitiveProcessor processor, ItemStack stack) {
		if (stack.isEmpty()) return Optional.empty();
		return Holder.RECIPES.stream().filter(recipe -> recipe.matches(processor, stack)).findFirst();
	}

	public static boolean isProcessable(PrimitiveProcessor processor, ItemStack stack) {
		return find(processor, stack).isPresent();
	}
}
