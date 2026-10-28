package dev.projectflint.recipe;

import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ProjectFlintBrushSystem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Single source of truth for Wooden Grinder two-input recipes. */
public final class WoodenGrinderRecipeRegistry {
	private WoodenGrinderRecipeRegistry() {}

	private static final Ingredient WOODEN_BEAMS = Ingredient.of(
		ModItems.WOODEN_BEAM, ModItems.ACACIA_WOODEN_BEAM, ModItems.BIRCH_WOODEN_BEAM,
		ModItems.CHERRY_WOODEN_BEAM, ModItems.CRIMSON_WOODEN_BEAM, ModItems.DARK_OAK_WOODEN_BEAM,
		ModItems.JUNGLE_WOODEN_BEAM, ModItems.MANGROVE_WOODEN_BEAM, ModItems.PALE_OAK_WOODEN_BEAM,
		ModItems.SPRUCE_WOODEN_BEAM, ModItems.WARPED_WOODEN_BEAM
	);

	private static final Ingredient STRIPPED_WOOD = Ingredient.of(
		Items.STRIPPED_OAK_LOG, Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_JUNGLE_WOOD,
		Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_MANGROVE_LOG, Items.STRIPPED_MANGROVE_WOOD, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_CHERRY_WOOD,
		Items.STRIPPED_PALE_OAK_LOG, Items.STRIPPED_PALE_OAK_WOOD, Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_CRIMSON_HYPHAE, Items.STRIPPED_WARPED_STEM, Items.STRIPPED_WARPED_HYPHAE
	);

	private static final List<Definition> RECIPES = List.of(
		recipe(Ingredient.of(ModItems.RAW_LEATHER, Items.LEATHER), 2, Ingredient.of(ModItems.SANDING_PAPER), 1, new ItemStack(ModItems.GROUND_LEATHER)),
		recipe(Ingredient.of(Items.LEATHER), 3, Ingredient.of(ModItems.WOVEN_WOOD), 6, new ItemStack(ModItems.WOVEN_CHEST_FRAME)),
		recipe(Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.REED_BACKPACK), 1, new ItemStack(ModItems.LEATHER_BACKPACK)),
		recipe(Ingredient.of(ModItems.LEATHER_BACKPACK), 1, Ingredient.of(ModItems.WOODEN_CHEST), 2, new ItemStack(ModItems.LEATHER_BACKPACK_WITH_CHEST)),
		recipe(Ingredient.of(ModItems.TANNED_LEATHER_CHESTPLATE), 1, Ingredient.of(ModItems.LEATHER_BACKPACK_WITH_CHEST), 1, new ItemStack(ModItems.TANNED_UTILITY_VEST)),
		recipe(Ingredient.of(ModItems.WATTLED_DAUB_BRICKS), 2, Ingredient.of(ModItems.ADVANCED_KILN_GLASS), 1, new ItemStack(ModItems.WATTLED_DAUB_WINDOW)),
		recipe(Ingredient.of(ModItems.SUGARCANE_ROLL), 1, Ingredient.of(ModItems.PLANT_TOOL_BINDING), 1, new ItemStack(ModItems.WOVEN_FAN, 2)),
		recipe(Ingredient.of(ModItems.SUGARCANE_ROLL), 3, Ingredient.of(ModItems.PLANT_TOOL_BINDING), 1, new ItemStack(ModItems.SANDING_PAPER, 2)),
		recipe(Ingredient.of(ModItems.WOVEN_FAN), 1, Ingredient.of(ModItems.WATTLE), 1, new ItemStack(ModItems.TIED_WATTLE, 3)),
		recipe(Ingredient.of(ModItems.FLINT_CHIP), 1, Ingredient.of(ModItems.MUD_PILE), 1, new ItemStack(ModItems.DAUB_PILE, 3)),
		recipe(Ingredient.of(ModItems.TIED_WATTLE), 4, Ingredient.of(ModItems.WET_DAUB_PILE), 2, new ItemStack(ModItems.UNFIRED_WATTLE_AND_DAUB, 4)),
		recipe(Ingredient.of(ModItems.MORTAR_BRICKS), 8, Ingredient.of(ModItems.MUD_PILE), 16, new ItemStack(ModItems.TORCH_HOLDER)),
		recipe(Ingredient.of(Items.FLINT), 12, Ingredient.of(ModItems.WOVEN_WOOD), 1, new ItemStack(ModItems.UNLIT_FLINT_TORCH)),
		recipe(Ingredient.of(ModItems.TORCH_HOLDER), 1, Ingredient.of(ModItems.UNLIT_FLINT_TORCH), 1, new ItemStack(ModItems.FLINT_TORCH_HOLDER)),
		recipe(Ingredient.of(ModItems.SANDED_WOOD_SLAB), 8, Ingredient.of(Items.LEATHER), 5, new ItemStack(ModItems.SANDING_STATION)),
		recipe(Ingredient.of(ModItems.WOVEN_WOOD), 16, Ingredient.of(ModItems.CLAY_POT), 1, new ItemStack(ModItems.WATER_BEARING_POT)),
		recipe(Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.CLAY_POT), 1, new ItemStack(ModItems.WATER_POT)),
		recipe(Ingredient.of(ModItems.PLANT_FIBER), 16, Ingredient.of(Items.STRING), 2, new ItemStack(ModItems.NEST)),
		recipe(STRIPPED_WOOD, 16, Ingredient.of(ModItems.ROPE), 1, new ItemStack(ModItems.WELL_PULLEY)),
		recipe(Ingredient.of(Items.CLAY_BALL), 3, Ingredient.of(ModItems.WOVEN_CHEST_FRAME), 1, new ItemStack(ModItems.UNFIRED_CLAY_BUCKET)),
		toolRecipe(Ingredient.of(Items.CLAY_BALL), 3, Ingredient.of(ModItems.BONE_KNIFE), new ItemStack(ModItems.UNFIRED_CLAY_BUCKET)),
		// Hydration steps use the water, not the clay vessel. WoodenGrinderMenu returns the emptied bucket.
		recipe(Ingredient.of(ModItems.FLAX_SEEDS), 9, Ingredient.of(ModItems.CLAY_WATER_BUCKET), 1, new ItemStack(Items.WHEAT_SEEDS, 3)),
		recipe(Ingredient.of(Items.WHEAT), 9, Ingredient.of(ModItems.CLAY_WATER_BUCKET), 1, new ItemStack(ModItems.FLOUR, 9)),
		recipe(rawMeat(), 1, Ingredient.of(ModItems.CLAY_WATER_BUCKET), 1, new ItemStack(ModItems.FAT)),
		recipe(Ingredient.of(ModItems.WOODEN_CUP), 2, Ingredient.of(ModItems.CLAY_MILK_BUCKET), 1, new ItemStack(ModItems.WOODEN_CUP_RAW_MILK, 4)),
		recipe(Ingredient.of(ModItems.FLINT_AXE_PIECES), 1, Ingredient.of(Items.LEATHER), 2, new ItemStack(ModItems.BOUND_WOOD_PROCESSOR)),
		recipe(Ingredient.of(ModItems.WOODEN_BOARD), 2, Ingredient.of(ModItems.UNLIT_FLINT_TORCH), 1, new ItemStack(ModItems.WOOD_FIRESTARTER)),
		recipe(Ingredient.of(Items.STICK), 2, Ingredient.of(ModItems.FILED_BONE), 3, new ItemStack(ModItems.BONE_HOE)),
		recipe(Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.WOVEN_WOOL), 4, maxDamageStack(Items.LEATHER_BOOTS, 512)),
		recipe(Ingredient.of(Items.LEATHER), 5, Ingredient.of(ModItems.WOVEN_WOOL), 5, maxDamageStack(Items.LEATHER_HELMET, 512)),
		recipe(Ingredient.of(Items.LEATHER), 8, Ingredient.of(ModItems.WOVEN_WOOL), 8, maxDamageStack(Items.LEATHER_CHESTPLATE, 512)),
		recipe(Ingredient.of(Items.LEATHER), 7, Ingredient.of(ModItems.WOVEN_WOOL), 7, maxDamageStack(Items.LEATHER_LEGGINGS, 512)),
		recipe(Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(ModItems.BOOKSHELF)),
		recipe(Ingredient.of(ModItems.FLINT_FILE_HEAD), 1, Ingredient.of(Items.STICK), 1, new ItemStack(ModItems.FLINT_FILE)),
		toolRecipe(Ingredient.of(Items.STICK), 2, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.AQUIFER_FINDER)),
		recipe(Ingredient.of(Items.BRICKS), 1, Ingredient.of(ModItems.WOODEN_BOARD), 1, new ItemStack(ModItems.BRICK_SHINGLES, 3)),
		toolRecipe(Ingredient.of(ModItems.WOODEN_BEAM), 1, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.WOODEN_SHEET, 4)),
		toolRecipe(Ingredient.of(ModItems.WOODEN_BOARD), 1, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.WOODEN_SCREW, 2)),
		toolRecipe(Ingredient.of(ModItems.LIMESTONE), 4, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.LIMESTONE_BRICKS, 4)),
		toolRecipe(Ingredient.of(ModItems.LIMESTONE_BRICKS), 4, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.CHISELED_LIMESTONE, 4)),
		recipe(WOODEN_BEAMS, 1, Ingredient.of(ModItems.CRUSHED_STONE_BLOCK_ITEM), 1, new ItemStack(ModItems.WOODEN_CABINET)),
		recipe(Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.WOODEN_SCREW), 4, new ItemStack(ModItems.WOODEN_DOOR_HINGE)),
		recipe(Ingredient.of(ModItems.WOODEN_SHEET), 4, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(ModItems.WOODEN_CHEST_FRAME)),
		recipe(Ingredient.of(ModItems.WATTLED_DAUB_BRICKS), 16, Ingredient.of(ModItems.WOODEN_DOOR_HINGE), 1, new ItemStack(ModItems.WATTLED_DAUB_DOOR)),
		recipe(Ingredient.of(ModItems.FIRED_CLAY), 4, Ingredient.of(ModItems.MUD_PILE), 16, new ItemStack(ModItems.LOOSE_MUD_BRICKS)),
		recipe(Ingredient.of(ModItems.LOOSE_MUD_BRICKS), 4, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(Items.BRICKS)),
		recipe(Ingredient.of(ModItems.SMOOTH_LIMESTONE), 1, Ingredient.of(Items.BRICKS), 2, new ItemStack(ModItems.PRIMITIVE_STOVE)),
		recipe(Ingredient.of(ModItems.PRIMITIVE_STOVE), 1, Ingredient.of(ModItems.FIRECLAY_BRICK_BLOCK), 8, new ItemStack(ModItems.BRICK_FURNACE)),
		recipe(Ingredient.of(ModItems.WOODEN_SHEET), 32, Ingredient.of(ModItems.FIRECLAY_BRICK_BLOCK), 32, new ItemStack(ModItems.FIRECLAY_FORGE_CONTROLLER)),
		recipe(Ingredient.of(ModItems.UNFIRED_BRICK), 2, Ingredient.of(ModItems.SAND_FLAKE), 1, new ItemStack(ModItems.UNFIRED_FIRECLAY_BRICK, 2)),
		recipe(Ingredient.of(ModItems.FILED_BONE), 2, Ingredient.of(Items.STICK), 1, new ItemStack(ModItems.BONE_KNIFE)),
		toolRecipe(Ingredient.of(ModItems.WOODEN_SHEET), 1, Ingredient.of(ModItems.BONE_KNIFE), new ItemStack(ModItems.WOODEN_CUP, 2)),
		toolRecipe(Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.BONE_KNIFE), new ItemStack(ModItems.UNFIRED_BRICK_MOLD)),
		reusableRecipe(Ingredient.of(Items.CLAY_BALL), 8, Ingredient.of(ModItems.UNFIRED_BRICK_MOLD), new ItemStack(ModItems.UNFIRED_BRICK, 8)),
		// The 4-brick result must take priority when both inputs contain 2 bricks.
		recipe(Ingredient.of(Items.BRICK), 2, Ingredient.of(Items.BRICK), 2, new ItemStack(Items.BRICKS)),
		recipe(Ingredient.of(Items.BRICK), 1, Ingredient.of(Items.BRICK), 1, new ItemStack(ModItems.WATTLED_DAUB_BRICKS, 12)),
		toolRecipe(Ingredient.of(ModItems.PUMPKIN_CAKE), 1, Ingredient.of(ModItems.BONE_KNIFE), new ItemStack(ModItems.PUMPKIN_CAKE_SLICE, 4)),
		recipe(Ingredient.of(ModItems.COBWEB), 1, Ingredient.of(ModItems.PLANT_TOOL_BINDING), 4, new ItemStack(ModItems.ROPE_STAGE_1)),
		recipe(Ingredient.of(ModItems.ROPE_STAGE_1), 1, Ingredient.of(ModItems.WOODEN_SCREW), 4, new ItemStack(ModItems.ROPE_STAGE_2)),
		recipe(Ingredient.of(ModItems.ROPE_STAGE_2), 1, Ingredient.of(ModItems.WOODEN_SHEET), 2, new ItemStack(ModItems.ROPE_STAGE_3)),
		toolRecipe(Ingredient.of(ModItems.ROPE_STAGE_3), 1, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.ROPE)),
		fullDurabilityRecipe(Ingredient.of(ModItems.WOODEN_SCREW), 2, Ingredient.of(ModItems.BONE_BRUSH), 1, ProjectFlintBrushSystem.createBrush())
	);

	/** All genuine raw meats, including the new turtle drop; raw milk is deliberately excluded. */
	public static Ingredient rawMeat() {
		return Ingredient.of(
			Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT,
			Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH,
			ModItems.RAW_CALAMARI, ModItems.RAW_TURTLE_MEAT, ModItems.RAW_FOX_MEAT,
			ModItems.RAW_FROG_MEAT, ModItems.RAW_GOAT_MEAT, ModItems.RAW_HORSE_MEAT,
			ModItems.RAW_LLAMA_MEAT, ModItems.RAW_DOLPHIN_MEAT,
			ModItems.RAW_BEAR_MEAT, ModItems.RAW_CAMEL_MEAT
		);
	}

	/** Existing cooked meats may be used for researched Meat Bread. */
	public static Ingredient cookedMeat() {
		return Ingredient.of(
			Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN,
			Items.COOKED_MUTTON, Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON,
			ModItems.COOKED_CALAMARI, ModItems.COOKED_TURTLE_MEAT,
			ModItems.COOKED_FOX_MEAT, ModItems.COOKED_FROG_MEAT, ModItems.COOKED_GOAT_MEAT,
			ModItems.COOKED_HORSE_MEAT, ModItems.COOKED_LLAMA_MEAT,
			ModItems.COOKED_DOLPHIN_MEAT, ModItems.COOKED_BEAR_MEAT, ModItems.COOKED_CAMEL_MEAT
		);
	}

	private static ItemStack maxDamageStack(net.minecraft.world.item.Item item, int damage) {
		ItemStack stack = new ItemStack(item); stack.set(DataComponents.MAX_DAMAGE, damage); stack.set(DataComponents.DAMAGE, 0); return stack;
	}

	private static Definition recipe(Ingredient a, int ca, Ingredient b, int cb, ItemStack r) {
		return new Definition(a, ca, b, cb, r, false, false, true, true, 0, 0);
	}

	private static Definition fullDurabilityRecipe(Ingredient a, int ca, Ingredient b, int cb, ItemStack r) {
		return new Definition(a, ca, b, cb, r, false, true, true, true, 0, 0);
	}

	/** Material consumed; a reusable form is retained without damage. */
	private static Definition reusableRecipe(Ingredient material, int count, Ingredient form, ItemStack result) {
		return new Definition(material, count, form, 1, result, false, false, true, false, 0, 0);
	}

	/** Material is consumed; the tool is retained and loses one durability. */
	private static Definition toolRecipe(Ingredient material, int count, Ingredient tool, ItemStack result) {
		return new Definition(material, count, tool, 1, result, false, false, true, false, 0, 1);
	}

	public static List<Definition> recipes() { return RECIPES; }

	public static List<Match> findAll(ItemStack slot0, ItemStack slot1) {
		List<Match> matches = new ArrayList<>();
		for (Definition recipe : RECIPES) {
			if (recipe.matches(slot0, slot1)) {
				matches.add(new Match(recipe.countA(), recipe.countB(), recipe.result().copy(), recipe.consumeA(), recipe.consumeB(), recipe.damageA(), recipe.damageB()));
				continue;
			}
			if (recipe.swappedMatches(slot0, slot1)) {
				matches.add(new Match(recipe.countB(), recipe.countA(), recipe.result().copy(), recipe.consumeB(), recipe.consumeA(), recipe.damageB(), recipe.damageA()));
			}
		}
		return List.copyOf(matches);
	}

	public static Optional<Match> find(ItemStack slot0, ItemStack slot1) {
		return findAll(slot0, slot1).stream().findFirst();
	}

	public record Definition(
		Ingredient inputA, int countA, Ingredient inputB, int countB, ItemStack result,
		boolean fullA, boolean fullB, boolean consumeA, boolean consumeB, int damageA, int damageB
	) {
		private boolean base(ItemStack a, ItemStack b) {
			return !a.isEmpty() && !b.isEmpty() && a.getCount() >= countA && b.getCount() >= countB && inputA.test(a) && inputB.test(b);
		}
		private static boolean full(ItemStack s) { return s.isDamageableItem() && s.getDamageValue() == 0; }
		private boolean matches(ItemStack a, ItemStack b) { return base(a,b) && (!fullA || full(a)) && (!fullB || full(b)); }
		private boolean swappedMatches(ItemStack a, ItemStack b) {
			return !a.isEmpty() && !b.isEmpty() && a.getCount() >= countB && b.getCount() >= countA && inputB.test(a) && inputA.test(b)
				&& (!fullB || full(a)) && (!fullA || full(b));
		}
	}

	public record Match(
		int countA, int countB, ItemStack result,
		boolean consumeA, boolean consumeB, int damageA, int damageB
	) {}
}
