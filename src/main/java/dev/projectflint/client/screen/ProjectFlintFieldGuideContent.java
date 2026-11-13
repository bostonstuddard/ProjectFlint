package dev.projectflint.client.screen;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Survival handbook only. Technical and release documentation belongs in docs/, not here. */
public final class ProjectFlintFieldGuideContent {
    public record Chapter(String section, String title, Item icon, List<String> pages) {
        public boolean matches(String query) {
            if (query.isBlank()) return true;
            if ((section + " " + title).toLowerCase(Locale.ROOT).contains(query)) return true;
            for (String text : pages) if (text.toLowerCase(Locale.ROOT).contains(query)) return true;
            return false;
        }
    }
    private ProjectFlintFieldGuideContent() {}

    public static List<Chapter> chapters() {
        List<Chapter> c = new ArrayList<>();
        add(c, "START HERE", "Your First Day", Items.BOOK,
            "Begin by collecting loose Rocks. Scrape Grass Blocks for Plant Fiber, and knap Rocks into Refined Rock.",
            "Use Refined Rock on bushes for Twigs. Gather extra materials before night; darkness becomes dangerous quickly. Press K whenever you need an exact recipe.");
        add(c, "START HERE", "Rocks and Knapping", ModItems.REFINED_ROCK,
            "Knap ordinary Rocks to make Refined Rock. Each attempt has a chance to fail, but repeated failures eventually guarantee a success.",
            "Save worked stone for your first tools and fire. You cannot mine ordinary stone with bare hands, food or a vest.");
        add(c, "START HERE", "Gathering Fiber", ModItems.PLANT_FIBER,
            "Scrape Grass Blocks for Plant Fiber. Turn the fibers into early bindings and string as soon as the required tools are available.",
            "More capable cutters produce better results from leaves, flowers and other plants. Check the recipe guide for each tool.");
        add(c, "START HERE", "Bushes and Twigs", ModItems.TWIG,
            "Use Refined Rock or an appropriate cutter to obtain Twigs from bushes. Bare hands cannot bypass this first step.",
            "The Tanned Utility Vest helps clear vegetation by hand later, but does not let you skip Twig harvesting.");
        add(c, "FIRST FIRE", "Twig Pile", ModItems.BURNT_TWIG,
            "Place twelve Twigs into a pile and try to ignite it with the early Twig and Refined Rock method.",
            "A successful fire burns briefly. Gather the burnt materials it produces before the flame goes out.");
        add(c, "FIRST FIRE", "Permanent Campfire", ModItems.CAMPFIRE_STONE,
            "Combine Campfire Stones with the required burnt materials to build a permanent campfire.",
            "Permanent campfires can be relit. Keep spare fuel and ignition supplies nearby for cooking and early processing.");
        add(c, "FIRST TOOLS", "Stone Tools", ModItems.CRUDE_STONE_HAMMER,
            "Use a Crude Stone Hammer on natural Stone to make Crushed Stone and collect four Rocks. This also counts as breaking Stone for Flint Quests.",
            "Early Multitools can also crush Stone. They cannot mine Cobblestone or ore; later Pickaxes and the Hardwood Hammer are needed for those.");
        add(c, "FIRST TOOLS", "Flint Tools", ModItems.FLINT_PLANT_CUTTERS,
            "Work toward the Flint Axe, Flint Plant Cutters and Advanced Flint Multitool to harvest more materials and shape wood. Flint Plant Cutters yield five Branches from leaves, plus other leaf drops.",
            "Different tools have different uses. Press K and look under the relevant workstations for exact ingredients.");
        add(c, "SURVIVAL", "Food and Wildlife", Items.COOKED_BEEF,
            "Keep food ready before working. The Basin uses one hunger point for each successful stomp.",
            "Some animals fight back. Fish can be killed with the Hardwood Hammer. Fish buckets transport fish; they are not food.");
        add(c, "SURVIVAL", "Cooking and Preserves", ModItems.SWEET_BERRY_JAM,
            "Use the Primitive Stove to prepare food using the recipe parchments. Sweet Berry Jam and Strawberry Jam now make four servings per batch.",
            "Mutton Pie uses four Cooked Mutton and two Sugar to make three pies. Look under the Stove tab for exact ingredients and fuel needs.");
        add(c, "SURVIVAL", "Darkness and Shelter", ModItems.FLINT_FIRESTARTER,
            "Nights are exceptionally dark. Carry light, set up shelter and use a Sleeping Bag to rest.",
            "Spending too long in deep darkness can cause Terrified. Torches and permanent fires help keep the surroundings safe.");
        add(c, "SURVIVAL", "Underground Fear", Items.COBBLESTONE,
            "Below Y=0, Speluncaphobia makes you hungrier and slows natural recovery. Below Y=-40, Claustrophobia also slows movement and weakens attacks.",
            "Direct daylight from the sky or strong light (level 13 or higher) temporarily clears these effects. Neither effect directly kills you.");
        add(c, "SURVIVAL", "Storage and Clothing", ModItems.REED_BACKPACK,
            "Mortar and Woven Chests provide early storage. Backpacks and other clothing extend what you can carry.",
            "The Tanned Utility Vest helps clear vegetation. It does not allow you to mine stone or dismantle masonry. It does allow you to recover Pure Mica Insulated Glass intact.");
        add(c, "CLAY AGE", "Clay and Pottery", ModItems.UNFIRED_CLAY_BALL,
            "Collect clay, shape unfired vessels, and prepare them for firing. The Hardwood Table can shape three Clay Balls into an Unfired Clay Bucket using a Bone Knife; the Knife loses durability.",
            "Pour one full Clay Water Bucket into an empty Water Pot to fill it completely and recover your empty Clay Bucket. A partly filled pot cannot be topped off this way.");
        add(c, "CLAY AGE", "Kilns", ModItems.CRUDE_CLAY_KILN,
            "Use a Clay Kiln to fire pottery and other early clay objects. Standard Advanced Kiln mold processing uses six Flint. Larger kilns require correctly assembled blocks.",
            "Most firing jobs need fuel. Check the kiln pages in Recipes for the required input and output.");
        add(c, "CLAY AGE", "Bricks and Shelter", ModItems.MORTAR_BRICKS,
            "Fire bricks for walls, ovens and more durable workstations. Brick Shingles and Wattle & Daub offer additional building materials.",
            "Brick structures need the appropriate tools to recover. A vest is not a substitute for a masonry tool.");
        add(c, "WOOD AGE", "Early Woodworking", ModItems.WOODEN_BOARD,
            "Use the Wooden Grinder with one Wooden Beam and a Flint File to make four Wooden Sheets. The File loses durability.",
            "Use a Wooden Axe or Hardwood Axe with Logs in personal crafting to make Planks. A Flint Axe cannot cut Planks.",
            "Use a Bone Knife on Oak Planks to make three Wooden Sheets, or a Flint File to make one Wooden Board. Both tools lose durability.");
        add(c, "WOOD AGE", "Improved Wood Processing", ModItems.WOVEN_WOOD,
            "In the Wooden Grinder, three Sanded Wood Slabs and a Flint File make two Grinded Wood Slabs. The File loses one durability.",
            "Combine three Grinded Wood Slabs and three Woven Fans in the same Grinder to make two Woven Wood. Three Sugar Cane and two Branches also make four Wattle there.");
        add(c, "WOOD AGE", "Wooden Tools", Items.WOODEN_AXE,
            "Craft Wooden Axes, Pickaxes and Shovels in your personal 2x2 crafting grid. You can also make Wooden Swords and Hoes there.",
            "Wooden Axes work Logs into Planks, and Wooden Pickaxes mine ordinary Stone and Coal. Press K for the exact materials.");
        add(c, "WOOD AGE", "Books and Research", Items.BOOK,
            "At the Bookshelf, combine eight Sugarcane Rolls and one Leather to make a Book.",
            "A Flint File and Bone Knife help with other bookwork at the Hardwood Table. See the Recipes tab for each station.");
        add(c, "WOOD AGE", "Lumber Processor", ModItems.LUMBER_PROCESSOR,
            "Build the Chopping Block, Water Pot and Crushed Stone parts together with support underneath each one.",
            "Place a supported Stripped Log, fill the Water Pot, choose Boards or Beams, then chop it with Flint Plant Cutters for 4.5 seconds. Washing still takes ten seconds.");
        add(c, "WOOD AGE", "Basin", ModItems.BASIN,
            "Put up to four materials in the Basin. All four appear inside it. Jump and land on the Basin to crush the first material.",
            "Each stomp costs one hunger point. Materials take two or three stomps. Empty-hand interaction returns an unfinished item. Breaking the Basin returns its contents.");
        add(c, "WOOD AGE", "Basin: Wood and Stone", ModItems.WOOD_DUST,
            "Two stomps turn a Plank into Wood Dust. Stripped Oak and Birch need three stomps to produce Tannin.",
            "Stone and Cobblestone may produce Stone Dust. Andesite and Granite produce Fertilizer, with a chance of Stone Dust. Other materials have their own outputs in Recipes.");
        add(c, "WOOD AGE", "Basin: Ores", ModItems.IRON_DUST,
            "Raw Iron and Raw Copper can be crushed in the Basin. Each always produces one matching dust, with chances for bonus dust and other minerals.",
            "Sand, Gravel, Clay and Slag can also produce useful dusts. Each possible bonus is rolled separately, so more than one may appear from the same input.");
        add(c, "WOOD AGE", "Leather and Rope", ModItems.WOVEN_WOOL,
            "Process hides into Leather and Tanned Leather. The Wooden Grinder can make a Woven Chest Frame from two Leather and four Woven Wood. Prepare straps, Wool and Rope for stronger tools.",
            "Use the Sanding Station and Hardwood Table for more advanced leather and rope recipes.");
        add(c, "WOOD AGE", "Torches and Fuel", ModItems.FAT_BURNING_TORCH,
            "Place a Fat-Burning Torch on the floor or a wall. New torches contain no Fat. Add Fat, then ignite the torch with a Firestarter.",
            "One Fat charge burns for about fifty minutes. Fat-Burning Torches cannot be lit below Y=0.");
        add(c, "WOOD AGE", "Wooden Pickaxe", Items.WOODEN_PICKAXE,
            "The Wooden Pickaxe can mine Stone, Coal Ore and ordinary rock. The Hardwood Pickaxe is stronger but the stone-mining speed remains controlled.",
            "Deepslate and Deepslate Ores are unavailable for mining. Ordinary Iron and Copper Ore require the Hardwood Hammer at this stage.");
        add(c, "FIRECLAY", "Fireclay Forge", ModItems.FIRECLAY_FORGE_CONTROLLER,
            "Build the Forge and place its fuel, material and Firestarter in the correct slots. Ignite a fuel charge when you're ready to work.",
            "Four Logs can be burned into Charcoal. Hardwood and MBSSIG Dust each need a five-minute charcoal-fueled Forge cycle. With input, Charcoal and a working Firestarter, the Forge starts its next cycle automatically.");
        add(c, "HARDWOOD", "Making Hardwood", ModItems.HARDWOOD,
            "Combine two Wood Dust, Tannin and a filled Clay Water Bucket to create Hardwood. You get the empty Clay Bucket back.",
            "At the Hardwood Table, five Wood Dust, one Clay Water Bucket and three Tannin make three Hardwood. The empty bucket is returned. Heat Hardwood with Charcoal in the Fireclay Forge for five minutes to make Hardwood Alloy.");
        add(c, "HARDWOOD", "Crafting Table", ModItems.HARDWOOD_CRAFTING_TABLE,
            "Make the first Hardwood Crafting Table in your personal 2x2 grid from two Hardwood Alloy, Cobblestone and a Bound Wood Processor.",
            "Use its 3x3 grid for more complicated recipes. Each completed craft takes three seconds. Recipes are listed under Hardwood Table in Recipes (K).");
        add(c, "HARDWOOD", "Mud and Earth Building", Items.PACKED_MUD,
            "At the Hardwood Crafting Table, surround a Water Bucket with eight Dirt to make eight Mud. The bucket is returned. One Mud makes four Mud Piles.",
            "Combine Mud with Wheat for Packed Mud. Walking on Packed Mud makes you 10% faster. Four Packed Mud make four Mud Bricks. Mud Bricks can be made into Loose Mud Bricks, but not the other way around.",
            "Use the table for Dirt, Loose Dirt and Loose Mud Brick Slabs, Dirt Stairs, and Mud Brick stairs, slabs and walls. Packed Mud and Mud Bricks are recovered with the same tools used for Fireclay Bricks, not bare hands. A Fireclay Forge with four Logs, a Firestarter and room for Charcoal can light the next wood batch automatically.");
        add(c, "HARDWOOD", "Hardwood Tools", ModItems.HARDWOOD_PICKAXE,
            "Hardwood tools use Hardwood Sticks. Most also require Leather Straps. Make Hardwood Sticks from two Hardwood at the Hardwood Table.",
            "The Hardwood Axe strips Logs by right-clicking. An appropriate axe can turn Logs into Planks in the crafting grid.");
        add(c, "HARDWOOD", "Hardwood Hammer", ModItems.HARDWOOD_HAMMER,
            "Fill the top two rows with six Hardwood Alloy. Put a Leather Strap, Hardwood Stick and Rope across the bottom row.",
            "The Hammer mines ordinary Iron and Copper Ore for one raw ore, and Coal Ore for two Coal. It breaks normal stone and can hurt fish. Dripstone, Tuff, Calcite and Deepslate are not mineable with early tools.");
        add(c, "HARDWOOD", "Axles and Doors", ModItems.AXLE,
            "Surround one Rope with eight Hardwood Sticks to make an Axle. Place it along the direction you need.",
            "Early door hinges are still useful. The Hardwood Table also makes wood components, screws, doors, furniture and tools.");
        add(c, "GLASS AGE", "Glass and Dust", ModItems.RARE_EARTH_DUST,
            "Crush Clay Blocks, Sand, Gravel and other materials in the Basin for mineral dust. Rare Earth Dust can produce Lithium, Iron, Aluminum and Gallium Dust.",
            "Make Glass Dust with Glass, a Bone Knife and a Flint File. Tools lose durability rather than being consumed.");
        add(c, "GLASS AGE", "Insulated Glass", ModItems.MBSSIG_DUST,
            "Make Borosilicate Dust using Silicon Dust, Aluminum Dust and a Clay Water Bucket. Surround it with eight Glass Dust to make BSIG Dust.",
            "Combine BSIG with Iron Dust and Charcoal for BSSIG Dust. Add Mica Dust to make MBSSIG Dust. Heat MBSSIG Dust for five minutes in the Fireclay Forge to obtain Pure Mica Insulated Glass.");
        add(c, "GLASS AGE", "Pure Mica Glass", ModItems.PURE_MICA_INSULATED_GLASS,
            "Pure Mica Insulated Glass is transparent blue-gray glass. It lets sunlight through like ordinary Glass.",
            "Wear a Tanned Utility Vest to collect it intact without Silk Touch. At the Hardwood Crafting Table, combine one glass block with a Bone Knife to make two PMI Sheets. The knife loses durability.");
        add(c, "BLASTING", "Coal and Sulfur", ModItems.SULFUR_DUST,
            "Stomp Charcoal in the Basin for a 20% Sulfur Dust chance or 15% Wood Dust. Coal has a 45% Sulfur Dust chance, plus separate 43% and 23% Stone Dust rolls and a 2.4% Carbon Dust chance.",
            "Heat Carbon Dust with Charcoal for 400 seconds in the Fireclay Forge to obtain Pure Carbon Dust.");
        add(c, "BLASTING", "Making Dynamite", ModItems.FUSE,
            "At the Hardwood Table, combine Silicon, Aluminum, Tin and Silver Dust to make Fuse-Ready Dust. Heat it for 200 seconds in the Brick Furnace to make Fine Fuse Powder, then combine it with Rope for a Fuse.",
            "Black Powder comes from Potassium Dust, Sulfur and Charcoal, or Gunpowder, Sulfur and Charcoal. Put Black Powder in the top corners and beside Red Dye, Fuse at the top center, and three Copper Dust across the bottom.");
        add(c, "BLASTING", "Using Dynamite", ModItems.DYNAMITE,
            "Hold Dynamite in your main hand and a Firestarter or Flint and Steel in your offhand. Right-click a block to place a lit charge against it. The fuse burns for three seconds.",
            "Weak Dynamite reaches up to 3 blocks for ordinary Coal, Iron or Copper Ores (four at most) and 2.25 blocks for Stone, Cobblestone, Andesite and Granite (two at most, with a 65% chance to break each). It cannot damage Deepslate or machinery. Each Basin drop chance is 25% higher (50% becomes 62.5%); chances above 100% can grant extra drops.");
        add(c, "REFERENCE", "Find Recipes", Items.CRAFTING_TABLE,
            "Press K or use Recipes below to see workstation recipes. Look for the workstation tab that matches what you're trying to make.",
            "Some recipes need fuel, water, a reusable tool or time to complete. Check the recipe's ingredients and any tool requirements.");
        add(c, "REFERENCE", "Item Atlas", Items.BOOK,
            "Browse the items currently available in Project Flint. Use the search box to locate an item by name.",
            "Hover over a listed item for its normal tooltip. Use the Field Guide chapters for the steps needed to obtain it.");
        return List.copyOf(c);
    }
    private static void add(List<Chapter> chapters, String section, String title, Item icon, String... paragraphs) {
        chapters.add(new Chapter(section, title, icon, List.of(paragraphs)));
    }
}
