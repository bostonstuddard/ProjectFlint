package dev.projectflint.client.screen;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;

/** Current, player-facing survival instructions only; no release or developer commentary. */
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
        add(result, "GETTING STARTED", "Welcome", Items.BOOK,
            "Project Flint is a survival adventure built around gathering, crafting by hand, and building workstations. You begin without the tools to mine ordinary blocks. Progress through rockwork, primitive fire, ceramics, wood processing, and the Fireclay Forge.",
            "Choose a chapter from the contents on the left, or search for an item or activity. Turn pages with Prev and Next. The Recipes button opens the separate workstation recipe reference. You can reopen this book with G at any time.");
        add(result, "GETTING STARTED", "First Materials", ModItems.PLANT_FIBER,
            "Look for loose Rocks on the ground. Scrape Grass Blocks for Plant Fiber. Knapping Rocks creates the Refined Rock needed for early plant harvesting.",
            "Use Refined Rock to collect Twigs from bushes. Bare hands cannot collect their early-game resources. Gather enough Twigs to make a first fire, then prepare more tool materials.");
        add(result, "STONEWORK", "Knapping", ModItems.REFINED_ROCK,
            "Knapping turns Rocks into more useful worked stone. The basic attempt succeeds 68% of the time. After three failures in a row, the next attempt succeeds.",
            "The Crude Stone Hammer, Very Refined Stone and Plant Tool Bindings are essential ingredients for more advanced primitive tools. Craft and assemble these as your supplies allow.");
        add(result, "STONEWORK", "Bushes and Twigs", ModItems.TWIG,
            "Forage bushes provide Twigs when worked with Refined Rock or a suitable cutter. A Tanned Utility Vest permits hand-clearing of vegetation, but does not grant early Twig harvesting.",
            "Twigs are essential for primitive fire and several tools. Make sure you have spare Twigs before trying to ignite a fire.");
        add(result, "STONEWORK", "Fiber and Cutters", ModItems.CRUDE_PLANT_STRING,
            "Plant Fiber can be processed into early string and bindings. Cutters harvest vegetation more effectively than a bare hand. Crude, Flint, and Advanced Flint tools become more capable as you progress.",
            "Try specialized Plant Cutters when gathering flowers, leaves and sugar cane. Harvest results depend on the particular tool and vegetation.");
        add(result, "FIRE", "First Fire", ModItems.CAMPFIRE_STONE,
            "Stack 12 Twigs into a primitive Twig Pile. Ignite it with the early Twig and Refined Rock method. A successful fire burns briefly and creates burnt material needed by later recipes.",
            "Collect Campfire Stones and Burnt Twigs to build a permanent campfire. Permanent campfires can be relit for repeated processing.");
        add(result, "STONEWORK", "Primitive Tools", ModItems.CRUDE_STONE_HAMMER,
            "Use a Crude Stone Hammer for early stoneworking. The Knife, Cutters, Crude Multitool and Flint tools unlock more demanding tasks.",
            "Ordinary stone, cobblestone, granite, andesite and diorite need a Wooden Pickaxe, Hardwood Pickaxe or Hardwood Hammer. Bare hands and unrelated items cannot mine rock, even with a Utility Vest.");
        add(result, "CLAYWORK", "Collecting Clay", ModItems.UNFIRED_CLAY_BALL,
            "Clay starts with simple gathering and forming. Clay bowls, unfired items, the firing pile and kilns gradually unlock fired ceramics.",
            "A clay vessel must be fired before it serves as a durable container. Follow the Clay Grinder and kiln sections of the Recipes guide for their supported inputs.");
        add(result, "CLAYWORK", "Kilns", ModItems.CRUDE_CLAY_KILN,
            "Build and heat a primitive clay kiln to fire pottery and prepare sturdier materials. More advanced kilns process additional components.",
            "Keep fuel prepared before starting a firing task. Some kilns form multiblock structures and require particular placement and accessible working faces.");
        add(result, "CLAYWORK", "Brick and Mortar", ModItems.MORTAR_BRICKS,
            "Bricks, Brick Shingles, mortar, and Wattle & Daub provide more durable construction. Some structural blocks must be worked with a capable tool to recover them.",
            "Prepare Fireclay materials as you expand into high-temperature processing. The Recipes guide lists the kiln and grinder inputs for each product.");
        add(result, "SURVIVAL", "Darkness and Shelter", ModItems.FLINT_FIRESTARTER,
            "Nights are extremely dark. Light, a roof and a safe sleeping place are important for surviving extended exploration. Spend too long in darkness and you may become Terrified.",
            "Below Y=0, Speluncaphobia grows in strength and raises hunger use while slowing recovery. Below Y=-40, Claustrophobia also slows movement and attack damage. Direct sunlight or strong artificial light (level 13+) clears both. Sleeping Bags allow you to rest through the night. Permanent campfires and torches offer more dependable light than the primitive Twig Pile.");
        add(result, "SURVIVAL", "Food and Wildlife", Items.COOKED_BEEF,
            "Hunting can be dangerous because even normally passive animals can retaliate. Fish are vulnerable to the Hardwood Hammer and other eligible sources of damage.",
            "Animal Bones, Fat, wool and leather feed crafting and survival. Store fish in appropriate buckets; fish buckets are for transport, not eating.");
        add(result, "SURVIVAL", "Storage and Clothing", ModItems.REED_BACKPACK,
            "Backpacks, chests and the Tanned Utility Vest expand storage options. The vest also enables clearing some vegetation without a cutting tool.",
            "The Tanned Utility Vest helps you clear plants, leaves and bushes by hand. It does not mine stone or dismantle machines. Small torches and Chicken Nests can be picked up by hand. Mortar and Woven Chests help maintain workshop supplies.");
        add(result, "FIRE", "Fat-Burning Torch", ModItems.FAT_BURNING_TORCH,
            "Craft a Fat-Burning Torch with a Stick, String and Charcoal. Place it on the ground or against a suitable wall. A newly placed torch is unlit and contains no Fat.",
            "Use Fat on the placed torch to load a charge, then use a Firestarter to ignite it. Each ignition consumes one stored Fat charge. Refuel with additional Fat when needed. Rain can extinguish exposed torches.");
        add(result, "WOODWORK", "First Wood Tools", ModItems.WOODEN_BOARD,
            "Wood processing provides Boards, Beams, Slabs, Sheets and the materials for better stations. Use the Wooden Grinder and Sanding Station for their listed recipes.",
            "A Sanding Station consumes Sand Paper durability while working. The Wooden Pickaxe and Axe become practical tools for wood- and stone-age construction.");
        add(result, "WOODWORK", "Lumber Processor", ModItems.LUMBER_PROCESSOR,
            "Build a three-block Lumber Processor from a Chopping Block, Water Pot and Crushed Stone, all supported from underneath. Leave the space above each station clear.",
            "Place a suitable Stripped Log on the Chopping Block. Fill the Water Pot using the Water Bearing Pot, select Boards or Beams, and use Flint Plant Cutters to start processing.");
        add(result, "WOODWORK", "Basin and Wood Dust", ModItems.WOOD_DUST,
            "Place Planks into the Basin to see them resting inside it. Jump and land on the Basin twice to crush each Plank into 1 Wood Dust. Stripped Oak/Birch Logs take 3 stomps for 2 Tannin.",
            "Other materials also appear inside the Basin while being worked. Each successful stomp costs 1 hunger point. Different inputs need 2 or 3 stomps at most. Remove unfinished items by hand to recover them; removing one resets progress.");
        add(result, "MATERIALS", "Ore Washing", ModItems.IRON_DUST,
            "At the Basin, 1 Raw Iron produces 1 Iron Dust, with a 75% chance for a second Iron Dust and 50% for Slag. Raw Copper produces 1 Copper Dust, then has independent 83% and 24% chances for two more, plus 10% Nickel Dust and 2% Silver Dust.",
            "Iron, Copper, Stone, Cobblestone, Andesite and Granite each need 3 stomps. Every stomp costs 1 hunger point. Iron may also yield 3% Gold Dust; Copper has 5% Slag. Andesite and Granite yield 1 or 2 Fertilizer with 33% Stone Dust.");
        add(result, "MATERIALS", "Dust and Slag", ModItems.SLAG,
            "Stone and Cobblestone give independent chances of 66%, 44%, and 13.33% for up to 3 Stone Dust. Stone Dust may yield Slag (50%). Gravel always gives Flint and may give Quartz (20%) or Iron Dust (1%). Sand gives Potassium Dust, with 15% Tin, 5% Mica, and 1% Gold Dust.",
            "Gravel, Stone Dust and Sand take 2 stomps; Slag takes 3. Slag gives independent 33% chances each for Iron, Copper and Silicon Dust. Clay Blocks take 3 stomps, with 5% Mica, 3.33% Rare Earth, 0.33% Silver and 2% Gold. Rare Earth Dust takes 3 stomps and can yield Lithium (22.3%), Iron (15%), Aluminum (4.8%), or Gallium (0.85%). These rolls are independent.");
        add(result, "MATERIALS", "Brick Furnace and Stone", Items.COBBLESTONE,
            "The Brick Furnace can smelt Cobblestone into Stone using Charcoal. Ordinary Stone, Cobblestone, Andesite, Granite and Diorite can be made into slabs or stairs at the Hardwood Crafting Table.",
            "Deepslate and all Deepslate Ore varieties cannot be mined with the tools available in the current progression. Keep other ores for a future mining tier.");
        add(result, "WOODWORK", "Axles and Building", ModItems.AXLE,
            "Craft an Axle with 1 Rope surrounded by 8 Hardwood Sticks. Place it on a suitable surface; its shaft aligns with the placement axis. Axles will connect to later mechanical devices.",
            "Hardwood Sticks use 2 Hardwood for 2 Sticks. Hardwood tools use Hardwood Sticks instead of ordinary Sticks. The older Wooden Door Hinges are still useful for making early doors.");
        add(result, "WOODWORK", "Leather and Rope", ModItems.WOVEN_WOOL,
            "Leather, Leather Straps, plant fibers, wool and Rope are used in clothing, tools and workstations. Prepare hides with the available drying and processing methods.",
            "The Sanding Station processes leather and other materials. See its Recipes page for the correct amounts and outputs.");
        add(result, "FIRECLAY", "Fireclay Forge", ModItems.FIRECLAY_FORGE_CONTROLLER,
            "Build the Fireclay Forge from its required multiblock components. Use a suitable Firestarter to ignite a fuel charge. Four Logs burn for one minute and produce four Charcoal over time.",
            "To produce Hardwood Alloy, place 1 Hardwood in the material slot, 1 Charcoal in the fuel slot, and a usable Firestarter in its slot. Ignite and let the Forge burn for the full five minutes. Collect the Hardwood Alloy from the finished-item output.");
        add(result, "HARDWOOD", "Making Hardwood", ModItems.HARDWOOD,
            "Obtain Wood Dust by processing Planks in the Basin. Collect Tannin through the Basin as well. Combine 2 Wood Dust, 1 Tannin and 1 filled Clay Water Bucket to craft 1 Hardwood.",
            "After crafting, the Clay Bucket is returned empty. Put 1 Hardwood into the Fireclay Forge and burn 1 Charcoal for five minutes to make 1 Hardwood Alloy.");
        add(result, "HARDWOOD", "Crafting Table", ModItems.HARDWOOD_CRAFTING_TABLE,
            "In your personal 2x2 crafting grid, combine 2 Hardwood Alloy, 1 Cobblestone and 1 Bound Wood Processor to make the Hardwood Crafting Table. You do not need a Hardwood Table to make your first one.",
            "The Hardwood Crafting Table makes Hardwood Sticks (2 Hardwood for 2 Sticks) and Axles (8 Hardwood Sticks around Rope), and provides a 3x3 crafting grid. Taking a crafting result requires three seconds. Pick the table up with a suitable axe if you need to move it.");
        add(result, "HARDWOOD", "Pickaxe and Axe", ModItems.HARDWOOD_PICKAXE,
            "The Wooden Pickaxe and Hardwood Pickaxe both mine ordinary Stone in about 13.5 seconds. Hardwood tools use Hardwood Sticks, with Leather Straps in their recipes except the Shovel.",
            "A Hardwood Axe strips Logs by right-clicking. Either a Wooden Axe or Hardwood Axe may be used to make two Planks from a suitable Log in the crafting grid. The Axe loses durability but is not consumed.");
        add(result, "HARDWOOD", "Hardwood Hammer", ModItems.HARDWOOD_HAMMER,
            "Craft a Hardwood Hammer using 6 Hardwood Alloy in the top two rows of the Hardwood Crafting Table, then a Leather Strap, Hardwood Stick and Rope from left to right along the bottom row.",
            "The Hardwood Hammer mines Stone and related rock in about 12 seconds. It mines Coal Ore for 2 Coal, and ordinary Iron Ore or Copper Ore for 1 Raw Ore. It can also hurt fish. Deepslate ores and Deepslate itself cannot be mined at this stage.");
        add(result, "MATERIALS", "Glass and Rare Earth", ModItems.RARE_EARTH_DUST,
            "Clay Blocks in the Basin can yield Rare Earth Dust. The Basin can process Rare Earth Dust into Lithium, Iron, Aluminum and Gallium Dust, each with its own separate chance. Eight Rare Earth Dust surrounding a Clay Water Bucket yields 3 Aluminum Dust in the Hardwood Table.",
            "Use a Bone Knife and Flint File on Glass to prepare Glass Dust. Combine 2 Silicon Dust, 1 Aluminum Dust and a filled Clay Water Bucket for Borosilicate Dust; the empty Clay Bucket returns.");
        add(result, "MATERIALS", "Insulated Glass", ModItems.MBSSIG_DUST,
            "Surround 1 Borosilicate Dust with 8 Glass Dust to make 4 BSIG Dust. Mix 4 BSIG Dust, 3 Iron Dust and 2 Charcoal to make 4 BSSIG Dust. Combine 4 BSSIG Dust with 1 Mica Dust to create MBSSIG Dust.",
            "Place MBSSIG Dust into the Fireclay Forge and burn 1 Charcoal for an uninterrupted five minutes to obtain Pure Mica Insulated Glass. It is a new material for later progression.");
        add(result, "HARDWOOD", "Tool Recipes", ModItems.FLINT_FILE,
            "The Hardwood Crafting Table makes Bone Clubs, Bone Hoes, Flint Axes, Flint Plant Cutters, Advanced Flint Multitools and Flint Torches with shaped 3x3 patterns. See the Hardwood Table tab under Recipes (K) for ingredient lists and patterns.",
            "Bone Knives and Flint Files lose durability rather than being consumed when cutting Wood Sheets, Boards, Leather Sheets, Fans and Glass Dust. Keep spare tools ready for larger batches.");
        add(result, "REFERENCE", "Finding Recipes", Items.CRAFTING_TABLE,
            "Use the Recipes button or press K to browse processing and crafting recipes. The Hardwood Table now has its own Recipes tab, alongside the Basin and Fireclay Forge.",
            "Workstation recipes may require timed processing, fuel, water, tools or other conditions. Refer to the relevant chapter for the steps to operate a workstation.");
        add(result, "REFERENCE", "Item Atlas", Items.BOOK,
            "Browse Project Flint items by name and icon. Select the Item Atlas and turn pages to explore the available materials and tools.",
            "Use the search box to find items or chapters. Hover over an icon to see its usual in-game item information.");
        return List.copyOf(result);
    }
    private static void add(List<Chapter> dest, String section, String title, Item icon, String... pages) {
        dest.add(new Chapter(section, title, icon, List.of(pages)));
    }
}
