package dev.projectflint.item;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Eight pieces of Woven Wool are placed together as one supported two-block Sleeping Bag. */
public final class WovenWoolItem extends Item {
	public WovenWoolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPlaceContext placeContext = new BlockPlaceContext(context);
		BlockPos clickedPos = context.getClickedPos();
		BlockState clickedState = level.getBlockState(clickedPos);
		BlockPos placePos = clickedState.canBeReplaced(placeContext) ? clickedPos : clickedPos.relative(context.getClickedFace());
		if (!level.getBlockState(placePos).canBeReplaced(placeContext)) return InteractionResult.PASS;

		Direction facing = context.getPlayer() == null ? Direction.NORTH : context.getPlayer().getDirection().getOpposite();

		ItemStack stack = context.getItemInHand();
		if (stack.getCount() < GameplayTuning.WOVEN_WOOL_PER_SLEEPING_BAG) return InteractionResult.FAIL;
		if (level.isClientSide()) {
			BlockPos headPos = placePos.relative(facing);
			if (!level.getBlockState(headPos).canBeReplaced(placeContext)) return InteractionResult.FAIL;
			return InteractionResult.SUCCESS;
		}
		if (!ModBlocks.SLEEPING_BAG.placePair(level, placePos, facing, net.minecraft.world.item.DyeColor.GREEN, placeContext)) {
			return InteractionResult.FAIL;
		}
		if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
			stack.shrink(GameplayTuning.WOVEN_WOOL_PER_SLEEPING_BAG);
		}
		level.playSound(null, placePos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.72F, 0.88F);
		if (context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			ModAdvancements.award(serverPlayer, "sleeping_bag", "placed_sleeping_bag");
		}
		return InteractionResult.SUCCESS;
	}
}
