package dev.projectflint.item;

import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInteractionSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** A sustained-use primitive brush that turns sand into Sand Flakes and gravel into vanilla Flint. */
public final class BoneBrushItem extends Item {
	private static final int BRUSH_TICKS = 20 * 7 / 2;

	public BoneBrushItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Sheep sheep) || !sheep.readyForShearing()) {
			return InteractionResult.PASS;
		}

		Level level = player.level();
		if (level instanceof ServerLevel serverLevel) {
			sheep.setSheared(true);
			int woolCount = 3 + sheep.getRandom().nextInt(2);
			sheep.spawnAtLocation(serverLevel, new ItemStack(ModItems.WOOL, woolCount));
			stack.hurtAndBreak(1, player, hand);
			level.playSound(null, sheep.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !isBrushTarget(context.getLevel().getBlockState(context.getClickedPos()))) {
			return InteractionResult.PASS;
		}
		player.startUsingItem(context.getHand());
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BRUSH;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return BRUSH_TICKS;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (!(entity instanceof Player player)) return;
		HitResult result = player.pick(player.blockInteractionRange(), 0.0F, false);
		if (!(result instanceof BlockHitResult hit) || result.getType() != HitResult.Type.BLOCK) {
			player.releaseUsingItem();
			return;
		}

		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!isBrushTarget(state)) {
			player.releaseUsingItem();
			return;
		}

		if (remainingUseDuration % 10 == 0) {
			level.playSound(player, pos, state.is(Blocks.GRAVEL) ? SoundEvents.BRUSH_GRAVEL : SoundEvents.BRUSH_SAND, SoundSource.BLOCKS, 0.55F, 0.92F);
		}
		if (remainingUseDuration > 1 || level.isClientSide()) return;

		if (state.is(Blocks.GRAVEL)) {
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(Items.FLINT));
			level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL_COMPLETED, SoundSource.BLOCKS, 0.8F, 1.0F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "vanilla_flint", "knapped_flint");
			}
		} else {
			level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.SAND_FLAKE));
			level.playSound(null, pos, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.BLOCKS, 0.8F, 1.0F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "sand_flake", "brushed_sand");
			}
		}
		InteractionHand usedHand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		stack.hurtAndBreak(1, player, usedHand);
		player.releaseUsingItem();
	}

	private static boolean isBrushTarget(BlockState state) {
		return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL);
	}
}
