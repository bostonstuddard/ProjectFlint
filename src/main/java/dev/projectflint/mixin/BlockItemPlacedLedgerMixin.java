package dev.projectflint.mixin;

import dev.projectflint.system.PlayerPlacedBlockLedger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Records only successful player placements; natural terrain is never flagged. */
@Mixin(BlockItem.class)
public abstract class BlockItemPlacedLedgerMixin {
	@Inject(method = "place", at = @At("RETURN"))
	private void projectflint$recordPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (!cir.getReturnValue().consumesAction() || !(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return;
		BlockState state = level.getBlockState(context.getClickedPos());
		if (state.getBlock() == ((BlockItem)(Object)this).getBlock())
			PlayerPlacedBlockLedger.record(level, context.getClickedPos(), state);
	}
}
