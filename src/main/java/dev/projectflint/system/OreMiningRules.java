package dev.projectflint.system;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Single source of truth for the early mining tiers (client progress + server drops). */
public final class OreMiningRules {
    private OreMiningRules() {}
    public static final float WOODEN_PROGRESS = 1.0F / 270.0F;
    public static final float UNQUALIFIED_PROGRESS = 1.0F / 900.0F;

    public static boolean isStone(BlockState state) { return state.is(Blocks.STONE); }
    public static boolean isCoal(BlockState state) {
        return state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE);
    }
    public static boolean isCopperOrIronStoneOre(BlockState state) {
        return state.is(Blocks.COPPER_ORE) || state.is(Blocks.IRON_ORE);
    }
    public static boolean isOre(BlockState state) {
        return isCoal(state) || isCopperOrIronStoneOre(state)
            || state.is(Blocks.DEEPSLATE_COPPER_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
            || state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
            || state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE)
            || state.is(Blocks.ANCIENT_DEBRIS);
    }
    public static boolean isVanillaPickaxe(ItemStack stack) {
        return stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.STONE_PICKAXE)
            || stack.is(Items.IRON_PICKAXE) || stack.is(Items.GOLDEN_PICKAXE)
            || stack.is(Items.DIAMOND_PICKAXE) || stack.is(Items.NETHERITE_PICKAXE);
    }
    public static boolean isWoodenTier(ItemStack stack) {
        return stack.is(Items.WOODEN_PICKAXE) || stack.is(ModItems.HARDWOOD_PICKAXE)
            || stack.is(ModItems.HARDWOOD_HAMMER);
    }
    public static boolean canGetDrops(BlockState state, ItemStack stack) {
        if (isStone(state) || isCoal(state)) return isWoodenTier(stack) || isVanillaPickaxe(stack);
        if (isCopperOrIronStoneOre(state)) {
            return stack.is(ModItems.HARDWOOD_HAMMER)
                || stack.is(Items.STONE_PICKAXE) || stack.is(Items.IRON_PICKAXE)
                || stack.is(Items.DIAMOND_PICKAXE) || stack.is(Items.NETHERITE_PICKAXE);
        }
        // Everything later belongs to genuine vanilla tool tiers, not Flint/Hardwood implements.
        return isVanillaPickaxe(stack) && !stack.is(Items.WOODEN_PICKAXE);
    }
    public static float progress(BlockState state, ItemStack held, float vanilla) {
        if (isStone(state) || isCoal(state) || isCopperOrIronStoneOre(state)) {
            if (isWoodenTier(held) && (isStone(state) || isCoal(state) || held.is(ModItems.HARDWOOD_HAMMER)))
                return WOODEN_PROGRESS;
        }
        return isVanillaPickaxe(held) ? vanilla : UNQUALIFIED_PROGRESS;
    }
}
