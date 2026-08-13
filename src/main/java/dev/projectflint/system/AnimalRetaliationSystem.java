package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class AnimalRetaliationSystem {
	private static final Map<UUID, Retaliation> RETALIATIONS = new HashMap<>();

	private AnimalRetaliationSystem() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (!(entity instanceof Animal animal) || !(entity.level() instanceof ServerLevel level)) {
				return;
			}
			Entity attacker = source.getEntity();
			if (!(attacker instanceof Player player) || player.isCreative() || player.isSpectator()) {
				return;
			}

			softenPlayerDamageAgainstWateredLandAnimals(animal, damageTaken);
			aggravateNearbySameSpecies(level, animal, player);
		});

		ServerTickEvents.END_WORLD_TICK.register(AnimalRetaliationSystem::tickWorld);
	}

	private static void softenPlayerDamageAgainstWateredLandAnimals(Animal animal, float damageTaken) {
		if (damageTaken <= 0.0F || !animal.isAlive() || !animal.isInWater()) {
			return;
		}
		float healAmount = (float) (damageTaken * (1.0D - GameplayTuning.LAND_MOB_DAMAGE_IN_WATER_MULTIPLIER));
		if (healAmount > 0.0F) {
			animal.heal(healAmount);
		}
	}

	private static void aggravateNearbySameSpecies(ServerLevel level, Animal struckAnimal, Player player) {
		long now = level.getGameTime();
		for (Animal animal : level.getEntitiesOfClass(
			Animal.class,
			struckAnimal.getBoundingBox().inflate(GameplayTuning.ANIMAL_GROUP_AGGRO_RANGE),
			candidate -> candidate.isAlive()
				&& candidate.getType() == struckAnimal.getType()
				&& canRetaliate(candidate)
		)) {
			animal.getNavigation().stop();
			RETALIATIONS.put(animal.getUUID(), new Retaliation(
				player.getUUID(),
				now + GameplayTuning.ANIMAL_RETALIATION_TICKS,
				now
			));
		}
	}

	private static boolean canRetaliate(Animal animal) {
		if (animal instanceof Horse || animal instanceof PolarBear) return false;
		return !animal.isBaby() || animal instanceof Wolf;
	}

	private static float retaliationDamage(Animal animal) {
		return animal instanceof Chicken
			? GameplayTuning.CHICKEN_RETALIATION_DAMAGE
			: GameplayTuning.ANIMAL_RETALIATION_DAMAGE;
	}

	private static void tickWorld(ServerLevel level) {
		long now = level.getGameTime();
		Iterator<Map.Entry<UUID, Retaliation>> iterator = RETALIATIONS.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, Retaliation> entry = iterator.next();
			Entity rawAnimal = level.getEntityInAnyDimension(entry.getKey());
			Retaliation retaliation = entry.getValue();

			if (!(rawAnimal instanceof Animal animal)) {
				iterator.remove();
				continue;
			}
			if (animal.level() != level) {
				continue;
			}
			if (!canRetaliate(animal)) {
				iterator.remove();
				continue;
			}

			Entity rawTarget = level.getEntityInAnyDimension(retaliation.playerId());
			if (!(rawTarget instanceof ServerPlayer player) || player.level() != level) {
				iterator.remove();
				continue;
			}

			if (!animal.isAlive() || !player.isAlive() || player.isCreative() || player.isSpectator() || now > retaliation.expiresAt()) {
				iterator.remove();
				continue;
			}

			double maxRangeSq = GameplayTuning.ANIMAL_PURSUIT_RANGE * GameplayTuning.ANIMAL_PURSUIT_RANGE;
			if (animal.distanceToSqr(player) > maxRangeSq) {
				iterator.remove();
				continue;
			}

			animal.getLookControl().setLookAt(player, 30.0F, 30.0F);
			animal.getNavigation().moveTo(player, GameplayTuning.ANIMAL_MOVE_SPEED);

			double attackRangeSq = GameplayTuning.ANIMAL_ATTACK_RANGE * GameplayTuning.ANIMAL_ATTACK_RANGE;
			if (animal.distanceToSqr(player) <= attackRangeSq && now >= retaliation.nextAttackAt()) {
				player.hurtServer(level, level.damageSources().mobAttack(animal), retaliationDamage(animal));
				entry.setValue(new Retaliation(
					retaliation.playerId(),
					retaliation.expiresAt(),
					now + GameplayTuning.ANIMAL_ATTACK_COOLDOWN_TICKS
				));
			}
		}
	}

	private record Retaliation(UUID playerId, long expiresAt, long nextAttackAt) {
	}
}
