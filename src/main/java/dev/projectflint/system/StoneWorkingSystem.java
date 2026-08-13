package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.network.PrimitiveProgressPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative repeated handworking/knapping progress. */
public final class StoneWorkingSystem {
	private static final Map<UUID, StoneWorkingProgress> PROGRESS = new HashMap<>();
	private static final Map<UUID, Integer> REGULAR_ROCK_FAILURES = new HashMap<>();

	private StoneWorkingSystem() {}

	public static StoneWorkingResult strike(Level level, Player player) {
		return strike(level, player, Process.REGULAR_ROCK);
	}

	public static StoneWorkingResult strike(Level level, Player player, Process process) {
		long gameTime = level.getGameTime();
		StoneWorkingProgress progress = PROGRESS.compute(player.getUUID(), (uuid, existing) -> {
			if (existing == null
				|| existing.process() != process
				|| gameTime - existing.lastStrikeTick() > GameplayTuning.STONE_WORKING_RESET_TICKS) {
				return new StoneWorkingProgress(process, 1, gameTime);
			}
			return new StoneWorkingProgress(process, existing.strikes() + 1, gameTime);
		});

		level.playSound(null, player.blockPosition(), SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.72F, 0.88F + level.getRandom().nextFloat() * 0.18F);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.POOF, player.getX(), player.getEyeY() - 0.35D, player.getZ(), 2, 0.08D, 0.04D, 0.08D, 0.015D);
		}

		if (player instanceof ServerPlayer serverPlayer) {
			ServerPlayNetworking.send(serverPlayer, new PrimitiveProgressPayload(
				process.payloadKind,
				Math.min(progress.strikes(), process.strikesRequired),
				process.strikesRequired,
				45
			));
		}

		if (progress.strikes() < process.strikesRequired) {
			return StoneWorkingResult.IN_PROGRESS;
		}

		PROGRESS.remove(player.getUUID());
		UUID playerId = player.getUUID();
		int regularFailures = process == Process.REGULAR_ROCK ? REGULAR_ROCK_FAILURES.getOrDefault(playerId, 0) : 0;
		boolean pitySuccess = process == Process.REGULAR_ROCK
			&& regularFailures >= GameplayTuning.REGULAR_ROCK_FAILURES_BEFORE_GUARANTEE;
		boolean success = pitySuccess || process.successChance >= 1.0F || level.getRandom().nextFloat() < process.successChance;
		if (!success) {
			if (process == Process.REGULAR_ROCK) {
				REGULAR_ROCK_FAILURES.put(playerId, regularFailures + 1);
			}
			if (player instanceof ServerPlayer serverPlayer) {
				ServerPlayNetworking.send(serverPlayer, new PrimitiveProgressPayload(process.payloadKind, 0, process.strikesRequired, 28));
			}
			level.playSound(null, player.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.55F, 0.72F);
			return StoneWorkingResult.FAILURE;
		}

		if (process == Process.REGULAR_ROCK) {
			REGULAR_ROCK_FAILURES.remove(playerId);
		}
		level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 0.72F);
		return StoneWorkingResult.SUCCESS;
	}

	public enum Process {
		REGULAR_ROCK(PrimitiveProgressPayload.KIND_STONE_WORKING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, GameplayTuning.STONE_WORKING_SUCCESS_CHANCE),
		CAMPFIRE_STONE(PrimitiveProgressPayload.KIND_CAMPFIRE_STONE, GameplayTuning.CAMPFIRE_STONE_KNAPPING_STRIKES, 1.0F),
		REED_CUTTING(PrimitiveProgressPayload.KIND_REED_CUTTING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		BURNT_TWIG_SPLITTING(PrimitiveProgressPayload.KIND_BURNT_TWIG_SPLITTING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		FLINT_KNAPPING(PrimitiveProgressPayload.KIND_FLINT_KNAPPING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		REED_ROLL(PrimitiveProgressPayload.KIND_REED_ROLL, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		FLOWER_KNAPPING(PrimitiveProgressPayload.KIND_FLOWER_KNAPPING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		FLOWER_SANDING(PrimitiveProgressPayload.KIND_FLOWER_SANDING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		BONE_SANDING(PrimitiveProgressPayload.KIND_BONE_SANDING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		BONE_FIRESTARTER(PrimitiveProgressPayload.KIND_BONE_FIRESTARTER, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		WOOL_SANDING(PrimitiveProgressPayload.KIND_WOOL_SANDING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		INK_SANDING(PrimitiveProgressPayload.KIND_INK_SANDING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		WOVEN_FAN_SANDING(PrimitiveProgressPayload.KIND_WOVEN_FAN_SANDING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		SAND_BALL(PrimitiveProgressPayload.KIND_SAND_BALL, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		WATTLE_WEAVING(PrimitiveProgressPayload.KIND_WATTLE_WEAVING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		WATTLE_TYING(PrimitiveProgressPayload.KIND_WATTLE_TYING, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F),
		WATTLE_DAUB_ASSEMBLY(PrimitiveProgressPayload.KIND_WATTLE_DAUB_ASSEMBLY, GameplayTuning.STONE_WORKING_STRIKES_PER_ATTEMPT, 1.0F);

		private final int payloadKind;
		private final int strikesRequired;
		private final float successChance;

		Process(int payloadKind, int strikesRequired, float successChance) {
			this.payloadKind = payloadKind;
			this.strikesRequired = strikesRequired;
			this.successChance = successChance;
		}
	}

	public enum StoneWorkingResult {
		IN_PROGRESS,
		SUCCESS,
		FAILURE
	}

	private record StoneWorkingProgress(Process process, int strikes, long lastStrikeTick) {}
}
