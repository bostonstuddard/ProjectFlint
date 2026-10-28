package dev.projectflint.client.screen;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;

/** The in-game manual is independent of EMI, quests, saves and server-side features. */
public final class ProjectFlintFieldGuideContent {
    public record Chapter(String section, String title, Item icon, List<String> pages) {
        public boolean matches(String query) {
            if (query.isBlank()) return true;
            if ((section + " " + title).toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            for (String page : pages) if (page.toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            return false;
        }
    }

    private ProjectFlintFieldGuideContent() {}

    public static List<Chapter> chapters() {
        List<Chapter> result = new ArrayList<>();
        add(result, "START HERE", "Welcome to Flint", Items.BOOK,
            "Project Flint is a survival overhaul. Start with rock, vegetation and fire, then move through clay, wood, water, metallurgy and hardwood machinery. Vanilla shortcuts are intentionally restricted.\n\nUse the index on the left to choose a subject. Turn the pages with Next and Prev. Search the whole handbook from the search box.",
            "This handbook explains progression and mechanics, not just shaped recipes. Press K to open the existing workstation Recipe Guide for exact ingredient displays. The Flint Quests mod can provide tracked objectives; the field guide works without it.\n\nPress G to open this manual again at any time. Both hotkeys are customizable in Minecraft Controls.");
        add(result, "START HERE", "Surviving Day One", ModItems.PLANT_FIBER,
            "Begin in a suitable biome and gather surface Rocks. Scrape ground cover with empty hands for Plant Fiber. The chance is 58%, and the hand action is intentionally slow.\n\nOrdinary dirt and stone cannot be mined with bread, bare hands or unrelated tools.",
            "Work surface rocks into Refined Rock, then use it to gather Twigs from forage bushes. Bushes are protected from empty-hand harvesting until you earn appropriate utility gear.\n\nMake a primitive fire before relying on advanced tools or night exploration.");
        add(result, "STONE AGE", "Rock Working", ModItems.REFINED_ROCK,
            "Surface Rocks are the first material. Knapping a Rock uses repeated strikes and a 68% success roll. After three consecutive failures, the following attempt is guaranteed. Successful knapping resets the streak.\n\nRefined Rock is useful for harvesting bushes and making your first tool assemblies.",
            "Stone progression uses physical world interactions and assembly blocks, rather than a vanilla crafting table. Look for Rock, Very Refined Stone, stockpiles, and a Crude Stone Hammer. Different rocks serve different recipes and stages.");
        add(result, "STONE AGE", "Twigs and Bushes", ModItems.TWIG,
            "Forage bushes provide early Twigs when cut with Refined Rock or appropriate Stone/Flint Cutters. Bare hands and irrelevant held items cannot harvest them for a free progression shortcut.\n\nWith a Tanned Utility Vest, you can clear vegetation by hand; the fallback bush drop is a vanilla Stick, not a free early Twig harvest.",
            "Carry Twigs for fire, fuel and early tool assembly. Stack them in the world to build a Twig Pile. A pile takes 12 Twigs. Its ignition can fail; keep spare materials and plan accordingly.");
        add(result, "STONE AGE", "Plant Fiber and Binding", ModItems.CRUDE_PLANT_STRING,
            "Hand-scrape suitable grass blocks to obtain Plant Fiber. Work Fiber into plant string and tool bindings using the early-world assembly actions.\n\nPlant Tool Bindings, Twigs and worked stones form the basis of Crude Cutters, the Knife and the Crude Multitool.",
            "Cutters handle vegetation more efficiently than bare hands. Flint and Advanced Flint tiers progressively improve yields. Flowers, reeds, sugar cane and leaves often have special processing outputs rather than vanilla drops.");
        add(result, "STONE AGE", "Fire and Campfires", ModItems.CAMPFIRE_STONE,
            "Collect 12 Twigs for the primitive Twig Pile. Light it using the early Refined Rock/Twig method; a successful ignition provides a short 10-second burn and useful Lightly Burnt Twigs.\n\nA permanent campfire requires 12 Campfire Stones and 12 Burnt Twigs, with dedicated ignition ingredients.",
            "Permanent campfires may be relit for 30-second cycles. They are useful for early processing and progression into clayworking. Later firestarters, torches and furnaces make light and fuel more dependable.");
        add(result, "STONE AGE", "Primitive Tool Paths", ModItems.CRUDE_STONE_HAMMER,
            "The Crude Stone Hammer opens many early assemblies and processing interactions. Crude Stone Cutters harvest plant materials, while the Crude Multitool combines limited stone and vegetation operations.\n\nTools are not interchangeable: harvest permissions and yields depend on their actual capability.",
            "The Flint Multitool, Flint Plant Cutters and Advanced Flint Multitool extend your options. The dedicated Plant Cutters are intentionally stronger at foliage collection than basic stone tools.\n\nDo not assume a hammer is a pickaxe: natural stone remains a slow undertaking.");
        add(result, "CLAY AGE", "Gathering Clay", ModItems.UNFIRED_CLAY_BALL,
            "Move from a woven bowl into clay gathering and firing. Clay bowls hold processed clay; the clay firing pile and the primitive kiln unlock more durable materials.\n\nRead each clay workstation's recipe guide for exact ingredients and outputs.",
            "Drying, firing and processing clay take time. The kiln uses physical world building and fuel, not an instant furnace recipe. Check workstations carefully before committing limited fuel.");
        add(result, "CLAY AGE", "Kiln and Ceramics", ModItems.CRUDE_CLAY_KILN,
            "Build the primitive clay kiln and bake it into a functional kiln. Fired clay, ceramic molds and other fired materials lead to advanced kilns and masonry.\n\nA fully baked clay kiln displays Brick Shingles rather than the original raw-clay exterior.",
            "Later Advanced and Mortar Kilns handle more complex processing. Built-up workstations can require multiple blocks, specific facing or supports. Use K to see their processing recipes once the relevant station is selected.");
        add(result, "CLAY AGE", "Bricks and Masonry", ModItems.MORTAR_BRICKS,
            "Brick Shingles, mortar, fired brick, and Wattle & Daub open permanent shelter. Some masonry must be worked with a capable Flint Multitool or pickaxe to dismantle it safely.\n\nDifferent brick blocks and slabs obey specialized salvage rules.",
            "Fireclay unlocks a higher-heat Forge. Prepare proper bricks and mold components, and note that breaking a structure with the wrong tool can waste progress. Workstation dismantling is not the same permission as general mining.");
        add(result, "SURVIVAL", "Night and Exposure", ModItems.FLINT_FIRESTARTER,
            "Darkness is dangerous in Project Flint. Light sources, prepared shelter and campfires matter. The Terrified condition needs 10 continuous seconds in qualifying darkness; nighttime darkness deepens over time.\n\nKeep fire-making supplies on hand before long excursions.",
            "Nighttime environmental damage and hunger effects are designed to make travel and unprepared mining expensive. A sleeping bag lets you skip the night and grants brief regeneration on waking.");
        add(result, "SURVIVAL", "Food and Animals", Items.COOKED_BEEF,
            "Project Flint changes hunger, food balance, animal durability and retaliation. Even passive animals can fight back. Fish are damageable and can be killed, including with the Hardwood Hammer.\n\nFish buckets are transport items: they are not edible and should not show hunger/saturation stats.",
            "Animal Bones, wool, leather, fat and cooked food feed later crafting paths. Use brushes, cutters and suitable weapons according to the target animal. Hunting without proper equipment is a real risk.");
        add(result, "SURVIVAL", "Shelter and Storage", ModItems.REED_BACKPACK,
            "Inventory capacity is highly limited early on. Reed and Leather Backpacks open additional slots; the Tanned Utility Vest expands capacity and improves clearing ability. Some upgrades increase hunger drain.\n\nMortar and Woven Chests provide permanent storage.",
            "Shelter materials include Wattle & Daub, mortar bricks and wooden construction. Workstations often become placeable, recoverable objects as the appropriate tool tier is unlocked.");
        add(result, "SURVIVAL", "Torches and Fuel", ModItems.FAT_BURNING_TORCH,
            "A Fat-Burning Torch is fueled with Fat. It starts with one charge, and each relight requires another charge. Ignition uses a Firestarter; shelter it from rain. It can mount on walls or floors.\n\nA fat-fueled torch lasts far longer than the first Twig Pile.",
            "Flint Torches and other lights have distinct materials and progression roles. Coal Ore can be harvested with a Wooden Pickaxe, but regular Coal does not yet unlock ordinary coal-powered torches or workstations.");
        add(result, "WOOD AGE", "Woodworking", ModItems.WOODEN_BOARD,
            "Use advanced Flint tools, mills and wood-processing workstations to move from logs to boards, beams, slabs and sheets. The Wooden Grinder and Sanding Station each unlock different processing routes.\n\nWooden Pickaxes and other primitive wood tools become craftable later than vanilla.",
            "At the Sanding Station, Sand Paper provides 32 durability up to a stored maximum of 256; each operation costs one point. Sanding unlocks prepared slabs, leather, filed bone, wool processing and other intermediates.");
        add(result, "WOOD AGE", "Lumber Processor", ModItems.LUMBER_PROCESSOR,
            "The Lumber Processor is a three-block multiblock. Its line uses a Chopping Block, a Water Pot, and a Crushed Stone station, with clear space and solid support underneath.\n\nThe leftmost part is the controller and determines placement orientation.",
            "Place a supported Stripped Log above the Chopping Block. Select Boards or Beams, fill the Water Pot to one bucket using two half-bucket trips with a Water Bearing Pot, then process the log with Flint Plant Cutters.\n\nDismantle and recover the machine only with the intended tool.");
        add(result, "WOOD AGE", "Water and the Basin", ModItems.WATER_POT,
            "The Water Bearing Pot holds half a bucket. The Water Pot is used in the Lumber Processor. Other water infrastructure opens distinct processing and collection paths.\n\nWater collection and station filling are deliberately hands-on.",
            "The Basin takes Stripped Logs for Tannin and ordinary Planks for Wood Dust. Its processing costs hunger, so bring food. These two materials are crucial for the Hardwood crafting chain.");
        add(result, "WOOD AGE", "Leather and Textile", ModItems.WOVEN_WOOL,
            "Drying, milling and preparing leather and wool opens durable clothing and storage. The Bone Brush, Sanding Station and related processing systems each serve a different step.\n\nThe Tanned Utility Vest is a major milestone for vegetation handling and inventory space.",
            "Flax, crude plant string, rope and other fibers become increasingly important in machines and tool handles. The earliest fiber crafts remain relevant well into the Wood Age.");
        add(result, "FIRECLAY AGE", "Fireclay Forge", ModItems.FIRECLAY_FORGE_CONTROLLER,
            "Fireclay Bricks and the Forge Controller create a later-stage fuelled workstation. The Forge uses its own input, charcoal fuel and output handling.\n\nHot processing unlocks Hardwood Alloy and a path toward better hand tools.",
            "One Hardwood and one Charcoal produce one Hardwood Alloy after a complete, uninterrupted 5-minute Forge burn. A non-charcoal fuel must not bypass the Alloy recipe. Leave output room before firing.");
        add(result, "HARDWOOD AGE", "Making Hardwood", ModItems.HARDWOOD,
            "First, process Planks in the Basin into Wood Dust. Combine 2 Wood Dust with Tannin and one filled Clay Water Bucket to make Hardwood.\n\nThe recipe empties the Clay Water Bucket and returns the Clay Bucket itself: only the water is consumed.",
            "Move Hardwood to the Fireclay Forge and burn it with Charcoal for five minutes. The output is Hardwood Alloy. Set up enough fuel and preparation before beginning long smelts.");
        add(result, "HARDWOOD AGE", "Crafting Table", ModItems.HARDWOOD_CRAFTING_TABLE,
            "Craft the Hardwood Crafting Table using 2 Hardwood Alloy, 1 Cobblestone and 1 Lumber Processor. It is a portable 3x3 workbench with a dark-wood appearance.\n\nUnlike a vanilla crafting table, taking each crafted result requires a three-second wait.",
            "Ordinary wooden tools use Planks and Sticks in their familiar vanilla shapes. Hardwood tools use Hardwood and Sticks in the same shapes. Two Planks make two vanilla Sticks.\n\nUse K for the ingredient reference for other processing stations.");
        add(result, "HARDWOOD AGE", "Pickaxe and Axe", ModItems.HARDWOOD_PICKAXE,
            "The Wooden Pickaxe breaks eligible Stone in 13.5 seconds. A Hardwood Pickaxe improves this to 9 seconds, with higher durability and regular wooden mining tier restrictions still intact.\n\nThe Hardwood Axe can right-click Strippable Logs/Wood just like a vanilla axe.",
            "Hardwood tool heads use ordinary Hardwood. They provide a meaningful but measured upgrade over primitive wooden counterparts. They are not a shortcut past stone-tier, iron-tier or diamond-tier mining gates.");
        add(result, "HARDWOOD AGE", "Hardwood Hammer", ModItems.HARDWOOD_HAMMER,
            "The Hardwood Hammer is a heavy stone-tier implement, not a strip-mining tool. Natural stone takes 21 seconds per block with the Hammer, slower than Wooden or Hardwood Pickaxes.\n\nIt can mine eligible stone-tier and lower ores, and can damage and kill fish.",
            "Hammer recipe: six Hardwood Alloy across the top two rows of the Hardwood Table. Bottom row: Leather, Stick, Rope.\n\nIt has 196 durability. Its attack damage is intentionally reduced versus its original overpowered implementation.");
        add(result, "REFERENCE", "Recipes and Help", Items.CRAFTING_TABLE,
            "This book is the progression and content compendium. It is not the station recipe browser. Press K from the world to open the Recipe Guide, then use its workstation and page arrows to inspect input/output recipes.\n\nPress G for this Field Guide. Search by mechanic, item name or progression age.",
            "Need a specific item? Open the Item Atlas for a paginated list of every registered Project Flint item and block-item, showing icons and registry identifiers. Hover over an icon for the in-game item tooltip.\n\nNo ProjectFlint-owned EMI plugin is installed by this manual.");
        add(result, "REFERENCE", "Item Atlas", Items.BOOK,
            "Every Project Flint item is catalogued here. Page through the entries or type an item name into the search field to narrow the list. Hover icons for full item tooltips.\n\nItems are taken from the active item registry, so new content appears here automatically.");
        return List.copyOf(result);
    }

    private static void add(List<Chapter> dest, String section, String title, Item icon, String... pages) {
        dest.add(new Chapter(section, title, icon, List.of(pages)));
    }
}
