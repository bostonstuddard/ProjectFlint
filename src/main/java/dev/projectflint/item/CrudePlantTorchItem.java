package dev.projectflint.item;

import dev.projectflint.block.CrudePlantTorchBlock;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.PrimitiveInteractionSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * Short-lived improvised light source. The damage value is its burn timer:
 * 0 is freshly lit and max damage is fully burned out.
 */
public final class CrudePlantTorchItem extends BlockItem {
	public static final int LIFETIME_TICKS = 20 * 60 * 5;
	private static final int INVENTORY_BURN_INTERVAL = 50;

	public CrudePlantTorchItem(Properties properties) {
		super(ModBlocks.CRUDE_PLANT_TORCH, properties.durability(LIFETIME_TICKS).stacksTo(1));
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		if (!(entity instanceof ServerPlayer player) || stack.isEmpty()) return;

		// Carried torches only extinguish from direct water/rain exposure.
		// Sky access is a placed-torch restriction, not an inventory restriction.
		if (player.isInWaterOrRain()
			|| level.isRainingAt(player.blockPosition())) {
			extinguish(stack, level, player.blockPosition());
			return;
		}

		int nextDamage = stack.getDamageValue() + 1;
		if (nextDamage >= stack.getMaxDamage()) {
			extinguish(stack, level, player.blockPosition());
			return;
		}
		stack.setDamageValue(nextDamage);

		if (!player.isCreative() && nextDamage % INVENTORY_BURN_INTERVAL == 0) {
			player.hurtServer(level, level.damageSources().inFire(), 1.0F);
		}
	}

	private static void extinguish(ItemStack stack, ServerLevel level, BlockPos pos) {
		stack.shrink(1);
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.55F, 1.15F);
	}

	/** Lights one held Lightly Burnt Twig from an already-lit primitive fire. */
	public static InteractionResult lightFromCampfire(ServerLevel level, BlockPos pos, Player player, ItemStack twig) {
		if (!player.isCreative()) twig.shrink(1);
		PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDE_PLANT_TORCH));
		level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.65F, 1.25F);
		return InteractionResult.SUCCESS;
	}
}
