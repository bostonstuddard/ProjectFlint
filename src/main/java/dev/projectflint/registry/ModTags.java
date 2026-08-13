package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
	public static final TagKey<Block> FORAGE_BUSHES = TagKey.create(Registries.BLOCK, ProjectFlint.id("forage_bushes"));
	public static final TagKey<Block> BREAKABLE_BY_HAND = TagKey.create(Registries.BLOCK, ProjectFlint.id("breakable_by_hand"));
	public static final TagKey<Block> GRASS_SCRAPE_TARGETS = TagKey.create(Registries.BLOCK, ProjectFlint.id("grass_scrape_targets"));
	public static final TagKey<Block> SLOW_HAND_CLEAR_TARGETS = TagKey.create(Registries.BLOCK, ProjectFlint.id("slow_hand_clear_targets"));
	public static final TagKey<Item> RAW_MEATS = TagKey.create(Registries.ITEM, ProjectFlint.id("raw_meats"));

	private ModTags() {
	}
}
