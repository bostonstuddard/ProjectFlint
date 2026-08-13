package dev.projectflint.recipe;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/** Single source of truth for Sanding Station recipes. Every craft costs one sanding durability. */
public final class SandingStationRecipeRegistry {
    private SandingStationRecipeRegistry() {}

    private static final Ingredient OVERWORLD_WOOD_SLABS = Ingredient.of(
        Items.OAK_SLAB, Items.SPRUCE_SLAB, Items.BIRCH_SLAB, Items.JUNGLE_SLAB,
        Items.ACACIA_SLAB, Items.DARK_OAK_SLAB, Items.MANGROVE_SLAB, Items.CHERRY_SLAB,
        Items.PALE_OAK_SLAB, Items.BAMBOO_SLAB, Items.BAMBOO_MOSAIC_SLAB
    );

    private static final List<Definition> RECIPES = List.of(
        recipe(Ingredient.of(ModItems.PLANT_TOOL_BINDING), 2, new ItemStack(ModItems.WOVEN_FAN, 3)),
        recipe(Ingredient.of(ModItems.GLASS_SHARD), 4, new ItemStack(ModItems.ADVANCED_KILN_GLASS)),
        recipe(Ingredient.of(ModItems.RAW_LEATHER), 3, new ItemStack(Items.LEATHER, 2)),
        recipe(Ingredient.of(ModItems.ANIMAL_BONES), 2, new ItemStack(ModItems.FILED_BONE, 3)),
        recipe(OVERWORLD_WOOD_SLABS, 2, new ItemStack(ModItems.SANDED_WOOD_SLAB, 3)),
        recipe(Ingredient.of(ModItems.WOOL), 3, new ItemStack(ModItems.WOVEN_WOOL, 2))
    );

    private static Definition recipe(Ingredient input, int count, ItemStack result) {
        return new Definition(input, count, result);
    }

    public static List<Definition> recipes() {
        return RECIPES;
    }

    public static Optional<Definition> find(ItemStack input) {
        return RECIPES.stream().filter(recipe -> recipe.matches(input)).findFirst();
    }

    public record Definition(Ingredient input, int count, ItemStack result) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && stack.getCount() >= count && input.test(stack);
        }
    }
}
