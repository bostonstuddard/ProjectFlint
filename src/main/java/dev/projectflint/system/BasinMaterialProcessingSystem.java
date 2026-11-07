package dev.projectflint.system;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import java.util.ArrayList;
import java.util.List;

/** Independently rolls each material outcome. Dynamite separately boosts every roll by 1.2x. */
public final class BasinMaterialProcessingSystem {
    private BasinMaterialProcessingSystem() {}

    public static boolean supports(ItemStack input) {
        return input.is(Items.RAW_IRON) || input.is(Items.RAW_COPPER)
            || input.is(Items.ANDESITE) || input.is(Items.GRANITE)
            || input.is(Items.STONE) || input.is(Items.COBBLESTONE) || input.is(Items.DIORITE)
            || input.is(ModItems.STONE_DUST) || input.is(Items.GRAVEL) || input.is(Items.SAND)
            || input.is(Items.CLAY) || input.is(ModItems.RARE_EARTH_DUST) || input.is(ModItems.SLAG)
            || input.is(Items.COAL) || input.is(Items.CHARCOAL);
    }

    public static int requiredStomps(ItemStack input) {
        if (input.is(Items.RAW_IRON) || input.is(Items.RAW_COPPER) || input.is(Items.CLAY)
            || input.is(Items.ANDESITE) || input.is(Items.GRANITE) || input.is(Items.STONE)
            || input.is(Items.COBBLESTONE) || input.is(Items.DIORITE) || input.is(ModItems.SLAG)
            || input.is(ModItems.RARE_EARTH_DUST) || input.is(Items.COAL)) return 3;
        return 2;
    }

    /**
     * A 100% chance yields one item. At 120%, one is guaranteed and a second
     * item has a separate 20% roll. Independent inputs never share a single roll.
     */
    private static void roll(List<ItemStack> drops, Item item, double chance, double multiplier, RandomSource random) {
        double expected = Math.max(0, chance * multiplier);
        int guaranteed = (int) Math.floor(expected);
        if (guaranteed > 0) drops.add(new ItemStack(item, guaranteed));
        if (random.nextDouble() < expected - guaranteed) drops.add(new ItemStack(item));
    }
    public static List<ItemStack> process(ItemStack input, RandomSource random) {
        return processWithMultiplier(input, random, 1.0F);
    }
    public static List<ItemStack> processWithMultiplier(ItemStack input, RandomSource random, float multiplier) {
        List<ItemStack> drops = new ArrayList<>();
        if (input.is(Items.RAW_IRON)) {
            roll(drops, ModItems.IRON_DUST, 1, multiplier, random);
            roll(drops, ModItems.IRON_DUST, .75, multiplier, random);
            roll(drops, ModItems.SLAG, .50, multiplier, random);
            roll(drops, ModItems.GOLD_DUST, .03, multiplier, random);
        } else if (input.is(Items.RAW_COPPER)) {
            roll(drops, ModItems.COPPER_DUST, 1, multiplier, random);
            roll(drops, ModItems.COPPER_DUST, .83, multiplier, random);
            roll(drops, ModItems.COPPER_DUST, .24, multiplier, random);
            roll(drops, ModItems.NICKEL_DUST, .10, multiplier, random);
            roll(drops, ModItems.SILVER_DUST, .02, multiplier, random);
            roll(drops, ModItems.SLAG, .125, multiplier, random);
        } else if (input.is(Items.ANDESITE) || input.is(Items.GRANITE)) {
            roll(drops, ModItems.FERTILIZER, 1, multiplier, random);
            roll(drops, ModItems.FERTILIZER, .5, multiplier, random);
            roll(drops, ModItems.STONE_DUST, .33, multiplier, random);
        } else if (input.is(Items.STONE) || input.is(Items.COBBLESTONE)) {
            roll(drops, ModItems.STONE_DUST, .66, multiplier, random);
            roll(drops, ModItems.STONE_DUST, .44, multiplier, random);
            roll(drops, ModItems.STONE_DUST, .1333, multiplier, random);
        } else if (input.is(Items.DIORITE)) {
            roll(drops, ModItems.STONE_DUST, 1, multiplier, random);
            roll(drops, ModItems.STONE_DUST, .5, multiplier, random);
        } else if (input.is(ModItems.STONE_DUST)) {
            roll(drops, ModItems.SLAG, .50, multiplier, random);
        } else if (input.is(Items.GRAVEL)) {
            roll(drops, Items.FLINT, 1, multiplier, random);
            roll(drops, ModItems.QUARTZ_DUST, .20, multiplier, random);
            roll(drops, ModItems.IRON_DUST, .01, multiplier, random);
            roll(drops, ModItems.RARE_EARTH_DUST, .04, multiplier, random);
        } else if (input.is(Items.SAND)) {
            roll(drops, ModItems.POTASSIUM_DUST, 1, multiplier, random);
            roll(drops, ModItems.MICA_DUST, .05, multiplier, random);
            roll(drops, ModItems.GOLD_DUST, .01, multiplier, random);
            roll(drops, ModItems.TIN_DUST, .15, multiplier, random);
            roll(drops, ModItems.RARE_EARTH_DUST, .04, multiplier, random);
        } else if (input.is(Items.CLAY)) {
            roll(drops, ModItems.MICA_DUST, .05, multiplier, random);
            roll(drops, ModItems.RARE_EARTH_DUST, .04, multiplier, random);
            roll(drops, ModItems.SILVER_DUST, .0033, multiplier, random);
            roll(drops, ModItems.GOLD_DUST, .02, multiplier, random);
        } else if (input.is(ModItems.RARE_EARTH_DUST)) {
            roll(drops, ModItems.LITHIUM_DUST, .223, multiplier, random);
            roll(drops, ModItems.IRON_DUST, .15, multiplier, random);
            roll(drops, ModItems.ALUMINUM_DUST, .048, multiplier, random);
            roll(drops, ModItems.GALLIUM_DUST, .0085, multiplier, random);
        } else if (input.is(ModItems.SLAG)) {
            roll(drops, ModItems.IRON_DUST, .33, multiplier, random);
            roll(drops, ModItems.COPPER_DUST, .33, multiplier, random);
            roll(drops, ModItems.SILICON_DUST, .33, multiplier, random);
            roll(drops, ModItems.RARE_EARTH_DUST, .018, multiplier, random);
        } else if (input.is(Items.CHARCOAL)) {
            roll(drops, ModItems.SULFUR_DUST, .20, multiplier, random);
            roll(drops, ModItems.WOOD_DUST, .15, multiplier, random);
        } else if (input.is(Items.COAL)) {
            roll(drops, ModItems.SULFUR_DUST, .45, multiplier, random);
            roll(drops, ModItems.STONE_DUST, .43, multiplier, random);
            roll(drops, ModItems.STONE_DUST, .23, multiplier, random);
            roll(drops, ModItems.CARBON_DUST, .024, multiplier, random);
        }
        return drops;
    }
}
