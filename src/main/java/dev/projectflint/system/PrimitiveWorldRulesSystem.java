package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PrimitiveWorldRulesSystem {
	private PrimitiveWorldRulesSystem() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(entity instanceof AbstractFish));
		ServerEntityEvents.ENTITY_LOAD.register(PrimitiveWorldRulesSystem::onEntityLoad);
	}

	private static void onEntityLoad(Entity entity, ServerLevel level) {
		if (!level.dimension().equals(Level.OVERWORLD)) {
			return;
		}

		if (entity.getType() == EntityType.WANDERING_TRADER
			|| entity.getType() == EntityType.TRADER_LLAMA
			|| entity.getType() == EntityType.PILLAGER) {
			entity.discard();
			return;
		}

		if (entity instanceof Drowned drowned) {
			if (drowned.getMainHandItem().is(Items.TRIDENT)) {
				drowned.setItemSlot(EquipmentSlot.MAINHAND, net.minecraft.world.item.ItemStack.EMPTY);
			}
			if (drowned.getOffhandItem().is(Items.TRIDENT)) {
				drowned.setItemSlot(EquipmentSlot.OFFHAND, net.minecraft.world.item.ItemStack.EMPTY);
			}
			applyFollowRange(drowned, GameplayTuning.DROWNED_FOLLOW_RANGE);
		} else if (entity instanceof Wolf wolf && !wolf.isTame()) {
			applyFollowRange(wolf, GameplayTuning.WILD_WOLF_AGGRO_RANGE);
		}

		if (!(entity instanceof Monster)) {
			return;
		}

		BlockPos pos = entity.blockPosition();
		int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
		if (level.canSeeSky(pos) || pos.getY() >= surfaceY - 3) {
			entity.discard();
		}
	}

	private static void applyFollowRange(LivingEntity living, double range) {
		AttributeInstance followRange = living.getAttribute(Attributes.FOLLOW_RANGE);
		if (followRange != null) {
			followRange.setBaseValue(range);
		}
	}
}
