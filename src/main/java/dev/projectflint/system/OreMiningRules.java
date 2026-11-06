package dev.projectflint.system;

import dev.projectflint.registry.ModItems;
import dev.projectflint.config.GameplayTuning;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Shared early mining tiers for visual break progress, attack permission, and server drops. */
public final class OreMiningRules {
    private OreMiningRules() {}

    /** 13.5 seconds at 20 TPS; avoid vanilla's much faster inappropriate-tool progress. */
    public static final float WOODEN_PROGRESS = 1.0F / 270.0F;
    /** The Hardwood Hammer is slightly quicker on rock: 12 seconds instead of 13.5. */
    public static final float HAMMER_STONE_PROGRESS = 1.0F / 240.0F;
    /** Inaccessible ores are slowly removable without loot, never instantly cracked. */
    public static final float UNQUALIFIED_ORE_PROGRESS = 1.0F / 900.0F;

    public static boolean isStone(BlockState state) { return state.is(Blocks.STONE); }
    public static boolean isCoal(BlockState state) {
        return state.is(Blocks.COAL_ORE);
    }
    /** Deepslate and all its ore varieties are reserved for later progression. */
    public static boolean isForbiddenDeepslate(BlockState state) {
        return state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
            || state.is(Blocks.POLISHED_DEEPSLATE) || state.is(Blocks.DEEPSLATE_BRICKS)
            || state.is(Blocks.DEEPSLATE_TILES) || state.is(Blocks.CHISELED_DEEPSLATE)
            || state.is(Blocks.CRACKED_DEEPSLATE_BRICKS) || state.is(Blocks.CRACKED_DEEPSLATE_TILES)
            || state.is(Blocks.DEEPSLATE_COAL_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
            || state.is(Blocks.COBBLED_DEEPSLATE_SLAB) || state.is(Blocks.COBBLED_DEEPSLATE_STAIRS)
            || state.is(Blocks.COBBLED_DEEPSLATE_WALL) || state.is(Blocks.POLISHED_DEEPSLATE_SLAB)
            || state.is(Blocks.POLISHED_DEEPSLATE_STAIRS) || state.is(Blocks.POLISHED_DEEPSLATE_WALL)
            || state.is(Blocks.DEEPSLATE_BRICK_SLAB) || state.is(Blocks.DEEPSLATE_BRICK_STAIRS)
            || state.is(Blocks.DEEPSLATE_BRICK_WALL) || state.is(Blocks.DEEPSLATE_TILE_SLAB)
            || state.is(Blocks.DEEPSLATE_TILE_STAIRS) || state.is(Blocks.DEEPSLATE_TILE_WALL)
            || state.is(Blocks.INFESTED_DEEPSLATE);
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
    /**
     * Apply rock rules to every VANILLA pickaxe-minable block, not just Stone.
     * Includes Cobblestone, Andesite, Diorite, Granite, Tuff, Deepslate, stonework,
     * and vanilla masonry. Custom Flint stations retain their specialized paths.
     */
    public static boolean isStoneFamily(BlockState state) {
        return !isOre(state)
            && !state.is(Blocks.BRICKS) // Retain the explicit vanilla-Bricks salvage/capability path.
            && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
            && "minecraft".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }
    public static boolean isRestricted(BlockState state) { return isForbiddenDeepslate(state) || isOre(state) || isStoneFamily(state); }

    /**
     * The first stone-working operation is NOT pickaxe mining: primitive hammers
     * turn natural Stone into Crushed Stone and release four Rocks. Keep this
     * exemption specific to Stone and these early hammer tools, never all rock,
     * Cobblestone, ores, or deepslate. Hardwood Hammer mines as a pickaxe-tier tool.
     *
     * This must be checked consistently by the client mining progress, attack
     * callback, pre-break guard and the server's completed-break handler.
     */
    public static boolean canPrimitiveStoneWork(BlockState state, ItemStack held) {
        return state.is(Blocks.STONE) && (
            held.is(ModItems.CRUDE_STONE_HAMMER)
            || held.is(ModItems.CRUDE_MULTITOOL)
            || held.is(ModItems.FLINT_MULTITOOL)
            || held.is(ModItems.ADVANCED_FLINT_MULTITOOL)
        );
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
    /** Unlike an armor slot, a held tool is the only thing that grants mining permission. */
    private static boolean isValidVanillaPickaxe(BlockState state, ItemStack held) {
        if (!isVanillaPickaxe(held)) return false;
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL))
            return held.is(Items.DIAMOND_PICKAXE) || held.is(Items.NETHERITE_PICKAXE);
        if (state.is(BlockTags.NEEDS_IRON_TOOL))
            return held.is(Items.IRON_PICKAXE) || held.is(Items.DIAMOND_PICKAXE) || held.is(Items.NETHERITE_PICKAXE);
        if (state.is(BlockTags.NEEDS_STONE_TOOL))
            return held.is(Items.STONE_PICKAXE) || held.is(Items.IRON_PICKAXE)
                || held.is(Items.DIAMOND_PICKAXE) || held.is(Items.NETHERITE_PICKAXE);
        return true;
    }
    private static boolean isEarlyRockTool(BlockState state, ItemStack held) {
        if (state.is(BlockTags.NEEDS_STONE_TOOL) || state.is(BlockTags.NEEDS_IRON_TOOL)
            || state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return false;
        return isWoodenTier(held);
    }
    public static boolean canGetDrops(BlockState state, ItemStack held) {
        if (isForbiddenDeepslate(state)) return false;
        if (isStoneFamily(state)) return canPrimitiveStoneWork(state, held)
            || isEarlyRockTool(state, held) || isValidVanillaPickaxe(state, held);
        if (isCoal(state)) return isWoodenTier(held) || isValidVanillaPickaxe(state, held);
        if (isCopperOrIronStoneOre(state))
            return held.is(ModItems.HARDWOOD_HAMMER) || isValidVanillaPickaxe(state, held);
        // Later ores still require their actual vanilla tier; Hardwood doesn't shortcut it.
        return isValidVanillaPickaxe(state, held);
    }
    public static float progress(BlockState state, ItemStack held, float vanilla) {
        if (isForbiddenDeepslate(state)) return 0.0F;
        if (isStoneFamily(state)) {
            // This path must precede pickaxe-tier checks: a Crude Stone Hammer
            // cannot mine rock, but it *can* process natural Stone into Rocks.
            if (canPrimitiveStoneWork(state, held))
                return GameplayTuning.CRUDE_HAMMER_STONE_PROGRESS_PER_TICK
                    * ToolCapabilities.hammerSpeedMultiplier(held);
            if (isEarlyRockTool(state, held))
                return held.is(ModItems.HARDWOOD_HAMMER) ? HAMMER_STONE_PROGRESS : WOODEN_PROGRESS;
            if (isValidVanillaPickaxe(state, held)) return vanilla;
            // A hand, vest, food or unrelated item cannot start cracking rock.
            return 0.0F;
        }
        if (isCoal(state)) {
            if (isWoodenTier(held)) return held.is(ModItems.HARDWOOD_HAMMER) ? HAMMER_STONE_PROGRESS : WOODEN_PROGRESS;
            return isValidVanillaPickaxe(state, held) ? vanilla : UNQUALIFIED_ORE_PROGRESS;
        }
        if (isCopperOrIronStoneOre(state) && held.is(ModItems.HARDWOOD_HAMMER)) return WOODEN_PROGRESS;
        return isValidVanillaPickaxe(state, held) ? vanilla : UNQUALIFIED_ORE_PROGRESS;
    }
}
