package dev.projectflint.system;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import java.util.ArrayList;
import java.util.List;

/** One independent roll per potential byproduct; all successes can occur together. */
public final class BasinMaterialProcessingSystem {
    private BasinMaterialProcessingSystem() {}
    public static boolean supports(ItemStack input) {
        return input.is(Items.RAW_IRON) || input.is(Items.RAW_COPPER)
            || input.is(Items.ANDESITE) || input.is(Items.GRANITE)
            || input.is(Items.STONE) || input.is(Items.COBBLESTONE) || input.is(Items.DIORITE)
            || input.is(ModItems.STONE_DUST) || input.is(Items.GRAVEL)
            || input.is(Items.SAND) || input.is(Items.CLAY) || input.is(ModItems.RARE_EARTH_DUST) || input.is(ModItems.SLAG);
    }
    /** Every process takes 2 or 3 valid landings, never more. */
    public static int requiredStomps(ItemStack input) {
        if (input.is(Items.RAW_IRON) || input.is(Items.RAW_COPPER) || input.is(Items.CLAY)
            || input.is(Items.ANDESITE) || input.is(Items.GRANITE) || input.is(Items.STONE)
            || input.is(Items.COBBLESTONE) || input.is(Items.DIORITE) || input.is(ModItems.SLAG)
            || input.is(ModItems.RARE_EARTH_DUST)) return 3;
        return 2;
    }
    private static void addChance(List<ItemStack> drops, ItemStack stack, float chance, RandomSource random) {
        if (random.nextFloat() < chance) drops.add(stack);
    }
    public static List<ItemStack> process(ItemStack input, RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        if (input.is(Items.RAW_IRON)) {
            drops.add(new ItemStack(ModItems.IRON_DUST));
            addChance(drops, new ItemStack(ModItems.IRON_DUST), .75F, random);
            addChance(drops, new ItemStack(ModItems.SLAG), .50F, random);
            addChance(drops, new ItemStack(ModItems.GOLD_DUST), .03F, random);
        } else if (input.is(Items.RAW_COPPER)) {
            drops.add(new ItemStack(ModItems.COPPER_DUST));
            addChance(drops, new ItemStack(ModItems.COPPER_DUST), .83F, random);
            addChance(drops, new ItemStack(ModItems.COPPER_DUST), .24F, random);
            addChance(drops, new ItemStack(ModItems.NICKEL_DUST), .10F, random);
            addChance(drops, new ItemStack(ModItems.SILVER_DUST), .02F, random);
            addChance(drops, new ItemStack(ModItems.SLAG), .125F, random);
        } else if (input.is(Items.ANDESITE) || input.is(Items.GRANITE)) {
            drops.add(new ItemStack(ModItems.FERTILIZER, random.nextBoolean() ? 2 : 1));
            addChance(drops, new ItemStack(ModItems.STONE_DUST), .33F, random);
        } else if (input.is(Items.STONE) || input.is(Items.COBBLESTONE)) {
            addChance(drops, new ItemStack(ModItems.STONE_DUST), .66F, random);
            addChance(drops, new ItemStack(ModItems.STONE_DUST), .44F, random);
            addChance(drops, new ItemStack(ModItems.STONE_DUST), .1333F, random);
        } else if (input.is(Items.DIORITE)) {
            drops.add(new ItemStack(ModItems.STONE_DUST, random.nextBoolean() ? 2 : 1));
        } else if (input.is(ModItems.STONE_DUST)) {
            addChance(drops, new ItemStack(ModItems.SLAG), .50F, random);
        } else if (input.is(Items.GRAVEL)) {
            drops.add(new ItemStack(Items.FLINT));
            addChance(drops, new ItemStack(ModItems.QUARTZ_DUST), .20F, random);
            addChance(drops, new ItemStack(ModItems.IRON_DUST), .01F, random);
            addChance(drops, new ItemStack(ModItems.RARE_EARTH_DUST), .04F, random);
        } else if (input.is(Items.SAND)) {
            drops.add(new ItemStack(ModItems.POTASSIUM_DUST));
            addChance(drops, new ItemStack(ModItems.MICA_DUST), .05F, random);
            addChance(drops, new ItemStack(ModItems.GOLD_DUST), .01F, random);
            addChance(drops, new ItemStack(ModItems.TIN_DUST), .15F, random);
            addChance(drops, new ItemStack(ModItems.RARE_EARTH_DUST), .04F, random);
        } else if (input.is(Items.CLAY)) {
            addChance(drops, new ItemStack(ModItems.MICA_DUST), .05F, random);
            addChance(drops, new ItemStack(ModItems.RARE_EARTH_DUST), .04F, random);
            addChance(drops, new ItemStack(ModItems.SILVER_DUST), .0033F, random);
            addChance(drops, new ItemStack(ModItems.GOLD_DUST), .02F, random);
        } else if (input.is(ModItems.RARE_EARTH_DUST)) {
            addChance(drops, new ItemStack(ModItems.LITHIUM_DUST), .223F, random);
            addChance(drops, new ItemStack(ModItems.IRON_DUST), .15F, random);
            addChance(drops, new ItemStack(ModItems.ALUMINUM_DUST), .048F, random);
            addChance(drops, new ItemStack(ModItems.GALLIUM_DUST), .0085F, random);
        } else if (input.is(ModItems.SLAG)) {
            addChance(drops, new ItemStack(ModItems.IRON_DUST), .33F, random);
            addChance(drops, new ItemStack(ModItems.COPPER_DUST), .33F, random);
            addChance(drops, new ItemStack(ModItems.SILICON_DUST), .33F, random);
            addChance(drops, new ItemStack(ModItems.RARE_EARTH_DUST), .018F, random);
        }
        return drops;
    }
}
