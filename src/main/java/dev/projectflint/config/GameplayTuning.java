package dev.projectflint.config;

public final class GameplayTuning {
	private GameplayTuning() {
	}

	// Restored to the alpha.3 rate after alpha.4 over-corrected it.
	public static final float GRASS_STRIP_PROGRESS_PER_TICK = 1.0F / 140.0F;
	public static final float HAND_CLEAR_FOLIAGE_PROGRESS_PER_TICK = 1.0F / 150.0F;
	// 1.75x slower than alpha.4's 34-tick bush cut.
	public static final float BUSH_CUT_PROGRESS_PER_TICK = 1.0F / 60.0F;
	public static final float CRUDE_HAMMER_REED_PROGRESS_PER_TICK = GRASS_STRIP_PROGRESS_PER_TICK;
	public static final float CRUDE_HAMMER_STONE_PROGRESS_PER_TICK = 1.0F / 234.0F;
	public static final float GRASS_FIBER_CHANCE = 0.58F;

	public static final int STONE_WORKING_STRIKES_PER_ATTEMPT = 8;
	public static final int STONE_WORKING_RESET_TICKS = 100;
	public static final float STONE_WORKING_SUCCESS_CHANCE = 0.68F;
	public static final int REGULAR_ROCK_FAILURES_BEFORE_GUARANTEE = 3;

	public static final int BUSH_MIN_TWIGS = 3;
	public static final int BUSH_MAX_TWIGS = 4;
	public static final int CUTTERS_BUSH_TWIGS = 4;

	public static final int TWIG_PILE_CAPACITY = 12;
	public static final int ROCK_PILE_CAPACITY = 12;
	public static final int VERY_REFINED_STONE_PILE_CAPACITY = 8;
	public static final int TWIG_IGNITION_STACK_SIZE = 4;
	public static final float TWIG_IGNITION_SUCCESS_CHANCE = 0.65F;
	public static final int TWIG_FIRE_DURATION_TICKS = 20 * 10;

	public static final int CAMPFIRE_STONE_KNAPPING_STRIKES = STONE_WORKING_STRIKES_PER_ATTEMPT * 2;
	public static final int CAMPFIRE_STONE_CAPACITY = 12;
	public static final int CAMPFIRE_BURNT_TWIG_CAPACITY = 12;
	public static final int CAMPFIRE_IGNITION_PETALS = 8;
	public static final int CAMPFIRE_IGNITION_OFFHAND_TWIGS = 4;
	public static final int CAMPFIRE_FIRE_DURATION_TICKS = 20 * 30;
	public static final int MULTITOOL_DURABILITY = 128;
	public static final int FLINT_MULTITOOL_DURABILITY = 256;
	public static final int FLINT_FIRESTARTER_DURABILITY = 32;
	public static final int BONE_FIRESTARTER_DURABILITY = 96;
	public static final int MULTITOOL_LEAF_TWIGS = 4;
	public static final int DIRT_PILE_COMPACTION_COUNT = 4;
	public static final int FLINT_CHIPS_PER_FLINT = 4;
	public static final int BASE_FLOWER_PETALS = 2;
	public static final int CRUDE_MULTITOOL_FLOWER_PETALS = 3;
	public static final int FLINT_MULTITOOL_FLOWER_PETALS = 4;
	public static final int TALL_FLOWER_MULTIPLIER = 2;
	public static final int CUTTERS_REEDS_PER_CANE = 3;
	public static final int CRUDE_MULTITOOL_REEDS_PER_CANE = 4;
	public static final int FLINT_MULTITOOL_REEDS_PER_CANE = 6;
	public static final float CRUDE_MULTITOOL_SPEED_MULTIPLIER = 1.25F;
	public static final float FLINT_MULTITOOL_SPEED_MULTIPLIER = 2.0F;
	public static final float ADVANCED_FLINT_MULTITOOL_SPEED_MULTIPLIER = 3.0F;
	public static final float ADVANCED_FLINT_MULTITOOL_EARTH_PROGRESS_MULTIPLIER = 0.80F;
	public static final int ADVANCED_FLINT_MULTITOOL_FLOWER_PETALS = 6;
	public static final int ADVANCED_FLINT_MULTITOOL_REEDS_PER_CANE = 8;
	public static final float CRUDE_MULTITOOL_EARTH_PROGRESS_MULTIPLIER = 0.30F;
	public static final float FLINT_MULTITOOL_EARTH_PROGRESS_MULTIPLIER = 0.50F;
	public static final int FLINT_MULTITOOL_EARTH_YIELD = 2;
	public static final int FLINT_MULTITOOL_GRAVEL_YIELD = 2;
	public static final int WOVEN_BOWL_BURNT_TWIGS = 12;
	public static final int WOVEN_BOWL_DIRT_PILES = 32;
	public static final int FIRED_BOWL_CLAY_PER_SCOOP = 2;
	public static final int FIRED_BOWL_CLAY_CAPACITY = 8;
	public static final int CLAY_BALL_FIRING_TWIGS = 4;
	public static final int CLAY_BALL_FIRING_TICKS = 20 * 30;
	public static final int FIRED_CLAY_PER_BLOCK = 4;
	public static final int SUPER_HEATED_CLAY_PER_BASE = 8;
	public static final int CLAY_GRINDER_STONE_REQUIREMENT = 12;
	public static final int WOVEN_WOOL_PER_SLEEPING_BAG = 8;
	public static final int SLEEPING_BAG_SALVAGE_WOOL = 2;
	public static final int KILN_SUPER_HEATED_CLAY_TICKS = 20 * 4;
	public static final int KILN_MEAT_COOK_TICKS = 20 * 15;
	public static final int PLANT_FIBER_COOK_TICKS = KILN_MEAT_COOK_TICKS * 5 / 4;
	public static final float MORTAR_KILN_COOK_SPEED = 1.25F;
	// Primitive Oven is +45% versus the shared 1.0 kiln baseline (not compounded with Mortar).
	public static final float PRIMITIVE_STOVE_COOK_SPEED = 1.45F;
	/** Brick Furnace is 20% faster than the 1.25x Mortar Kiln baseline. */
	public static final float BRICK_FURNACE_COOK_SPEED = 1.50F;
	/** Dedicated blast recipes/tuning are intentionally deferred; shares the Brick Furnace baseline for now. */
	public static final float BRICK_BLAST_FURNACE_COOK_SPEED = BRICK_FURNACE_COOK_SPEED;
	public static final float CAMPFIRE_COOK_SPEED = 0.90F;
	public static final int KILN_BRANCH_CAPACITY = 8;
	public static final int KILN_BAKE_TICKS = 20 * 30;
	public static final int KILN_BURN_TICKS = 20 * 60 * 5;

	// Food is scarce in the primitive opening, so vanilla exhaustion stays softened.
	public static final float HUNGER_EXHAUSTION_MULTIPLIER = 0.72F;
	public static final float SPRINT_EXHAUSTION_VANILLA_MULTIPLIER = 0.50F;
	public static final float SWIMMING_EXHAUSTION_MULTIPLIER = 3.0F;
	public static final float SPRINT_JUMP_EXHAUSTION_VANILLA_MULTIPLIER = 2.0F;
	public static final float WEAKENED_HEALTH_THRESHOLD = 14.0F;
	public static final float DYING_HEALTH_THRESHOLD = 8.0F;
	public static final int HUNGRY_FOOD_THRESHOLD = 8;
	public static final int CONDITION_EFFECT_REFRESH_TICKS = 30;
	public static final int MORNING_REGENERATION_TICKS = 20 * 4;
	public static final double CHICKEN_PASSIVE_HEALTH_BONUS = 0.60D;

	// Generic alpha retaliation; species-specific behavior will replace this later.
	public static final int ANIMAL_RETALIATION_TICKS = 20 * 30;
	public static final int ANIMAL_ATTACK_COOLDOWN_TICKS = 14;
	public static final double ANIMAL_PURSUIT_RANGE = 30.0D;
	public static final double ANIMAL_ATTACK_RANGE = 2.30D;
	public static final double ANIMAL_GROUP_AGGRO_RANGE = 16.0D;
	public static final double ANIMAL_MOVE_SPEED = 1.58D;
	public static final float ANIMAL_RETALIATION_DAMAGE = 2.5F;
	public static final float CHICKEN_RETALIATION_DAMAGE = 2.0F;

	public static final int WILD_WOLF_SCAN_INTERVAL_TICKS = 8;
	public static final double WILD_WOLF_AGGRO_RANGE = 8.0D;
	public static final double WILD_WOLF_MOVE_SPEED = 1.35D;
	public static final double DROWNED_FOLLOW_RANGE = 8.0D;
	public static final double LAND_MOB_DAMAGE_IN_WATER_MULTIPLIER = 1.0D / 3.0D;

	public static final double SQUID_AGGRO_RANGE = 12.0D;
	public static final double SQUID_CHASE_SPEED = 0.30D;
	public static final double SQUID_ATTACK_RANGE = 1.65D;
	public static final int SQUID_ATTACK_COOLDOWN_TICKS = 20;
	public static final float SQUID_ATTACK_DAMAGE = 2.0F;
}
