package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;

public final class PrimitiveExhaustionContext {
	private static final ThreadLocal<Mode> ACTIVE_MODE = ThreadLocal.withInitial(() -> Mode.DEFAULT);

	private PrimitiveExhaustionContext() {
	}

	public static void beginSwimmingMovement() {
		ACTIVE_MODE.set(Mode.SWIMMING);
	}

	public static void beginSprintMovement() {
		ACTIVE_MODE.set(Mode.SPRINT);
	}

	public static void beginSprintJump() {
		ACTIVE_MODE.set(Mode.SPRINT_JUMP);
	}

	public static void clear() {
		ACTIVE_MODE.remove();
	}

	public static float multiplier() {
		return switch (ACTIVE_MODE.get()) {
			case SWIMMING -> GameplayTuning.SWIMMING_EXHAUSTION_MULTIPLIER;
			case SPRINT -> GameplayTuning.SPRINT_EXHAUSTION_VANILLA_MULTIPLIER;
			case SPRINT_JUMP -> GameplayTuning.SPRINT_JUMP_EXHAUSTION_VANILLA_MULTIPLIER;
			case DEFAULT -> GameplayTuning.HUNGER_EXHAUSTION_MULTIPLIER;
		};
	}

	private enum Mode {
		DEFAULT,
		SWIMMING,
		SPRINT,
		SPRINT_JUMP
	}
}
