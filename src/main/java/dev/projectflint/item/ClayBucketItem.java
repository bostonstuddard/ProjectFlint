package dev.projectflint.item;

import dev.projectflint.registry.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Fired clay bucket that can collect water source blocks, but never lava or other bucket fluids. */
public final class ClayBucketItem extends BucketItem {
	public ClayBucketItem(Properties properties) {
		super(Fluids.EMPTY, properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Cow cow) || cow.isBaby()) return InteractionResult.PASS;
		if (player.level().isClientSide()) return InteractionResult.SUCCESS;
		player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ModItems.CLAY_MILK_BUCKET)));
		player.level().playSound(null, target.blockPosition(), SoundEvents.COW_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hit.getType() == HitResult.Type.MISS) return InteractionResult.PASS;
		if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;

		BlockPos pos = hit.getBlockPos();
		Direction direction = hit.getDirection();
		BlockPos adjacent = pos.relative(direction);
		if (!level.mayInteract(player, pos) || !player.mayUseItemAt(adjacent, direction, held)) {
			return InteractionResult.FAIL;
		}

		FluidState fluid = level.getFluidState(pos);
		if (!fluid.isSource() || !fluid.is(FluidTags.WATER)) {
			return InteractionResult.FAIL;
		}

		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof BucketPickup pickup)) {
			return InteractionResult.FAIL;
		}

		ItemStack pickedUp = pickup.pickupBlock(player, level, pos, state);
		if (pickedUp.isEmpty()) return InteractionResult.FAIL;

		player.awardStat(Stats.ITEM_USED.get(this));
		pickup.getPickupSound().ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
		level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

		ItemStack filled = ItemUtils.createFilledResult(held, player, new ItemStack(ModItems.CLAY_WATER_BUCKET));
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, new ItemStack(ModItems.CLAY_WATER_BUCKET));
		}
		return InteractionResult.SUCCESS.heldItemTransformedTo(filled);
	}
}
