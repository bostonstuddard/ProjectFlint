package dev.projectflint.item;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class SuperHeatedClayItem extends Item {
	public SuperHeatedClayItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPlaceContext placeContext = new BlockPlaceContext(context);
		BlockPos clickedPos = context.getClickedPos();
		BlockState clickedState = level.getBlockState(clickedPos);
		BlockPos placePos = clickedState.canBeReplaced(placeContext) ? clickedPos : clickedPos.relative(context.getClickedFace());
		BlockState placeState = level.getBlockState(placePos);
		BlockState grindingBase = ModBlocks.CLAY_GRINDING_BASE.defaultBlockState();
		if (!placeState.canBeReplaced(placeContext)) return InteractionResult.PASS;
		if (!grindingBase.canSurvive(level, placePos)) return InteractionResult.FAIL;
		ItemStack stack = context.getItemInHand();
		if (stack.getCount() < GameplayTuning.SUPER_HEATED_CLAY_PER_BASE) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			level.setBlockAndUpdate(placePos, grindingBase);
			if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
				stack.shrink(GameplayTuning.SUPER_HEATED_CLAY_PER_BASE);
			}
			if (context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "clay_grinding_base", "placed_clay_grinding_base");
			}
		}
		return InteractionResult.SUCCESS;
	}
}
