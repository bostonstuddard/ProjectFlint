package dev.projectflint.entity;

import dev.projectflint.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class BlackBear extends PolarBear {
	public BlackBear(EntityType<? extends PolarBear> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createBlackBearAttributes() {
		return PolarBear.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.5D)
			.add(Attributes.ATTACK_DAMAGE, 6.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.27D)
			.add(Attributes.FOLLOW_RANGE, 8.0D);
	}

	@Override
	protected AABB getAttackBoundingBox(double attackRange) {
		// Bears are large-bodied attackers. Give their melee goal enough horizontal
		// reach to connect before the player can safely sit just outside the model.
		return super.getAttackBoundingBox(attackRange + 1.25D);
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
		return ModEntities.BLACK_BEAR.create(level, EntitySpawnReason.BREEDING);
	}
}
