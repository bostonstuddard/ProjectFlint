package dev.projectflint.recipe;

import com.mojang.serialization.MapCodec;
import dev.projectflint.registry.ModRecipes;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Crafting recipes whose recipe type belongs exclusively to the Hardwood Crafting Table.
 * They are intentionally not vanilla crafting-table recipes or normal furnace recipes.
 */
public final class HardwoodCraftingRecipe implements CraftingRecipe {
    private final CraftingRecipe delegate;
    private final RecipeSerializer<HardwoodCraftingRecipe> serializer;

    public HardwoodCraftingRecipe(CraftingRecipe delegate) {
        this.delegate = delegate;
        this.serializer = delegate instanceof net.minecraft.world.item.crafting.ShapedRecipe
            ? ModRecipes.HARDWOOD_SHAPED_SERIALIZER
            : delegate instanceof net.minecraft.world.item.crafting.ShapelessRecipe
                ? ModRecipes.HARDWOOD_SHAPELESS_SERIALIZER
                : ModRecipes.HARDWOOD_DURABLE_SERIALIZER;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return delegate.matches(input, level); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider access) { return delegate.assemble(input, access); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input) { return delegate.getRemainingItems(input); }
    @Override public String group() { return delegate.group(); }
    @Override public CraftingBookCategory category() { return delegate.category(); }
    @Override public RecipeBookCategory recipeBookCategory() { return delegate.recipeBookCategory(); }
    @Override public PlacementInfo placementInfo() { return delegate.placementInfo(); }
    @Override public List<RecipeDisplay> display() { return delegate.display(); }
    @Override public RecipeType<CraftingRecipe> getType() { return ModRecipes.HARDWOOD_CRAFTING; }
    @Override public RecipeSerializer<HardwoodCraftingRecipe> getSerializer() { return serializer; }

    public static final class Serializer<T extends CraftingRecipe> implements RecipeSerializer<HardwoodCraftingRecipe> {
        private final MapCodec<HardwoodCraftingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, HardwoodCraftingRecipe> streamCodec;
        @SuppressWarnings("unchecked")
        public Serializer(RecipeSerializer<T> delegate) {
            this.codec = delegate.codec().xmap(HardwoodCraftingRecipe::new, r -> (T) r.delegate);
            this.streamCodec = delegate.streamCodec().map(HardwoodCraftingRecipe::new, r -> (T) r.delegate);
        }
        @Override public MapCodec<HardwoodCraftingRecipe> codec() { return codec; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, HardwoodCraftingRecipe> streamCodec() { return streamCodec; }
    }
}
