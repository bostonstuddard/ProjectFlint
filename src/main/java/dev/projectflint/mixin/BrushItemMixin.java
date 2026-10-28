package dev.projectflint.mixin;

import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInteractionSystem;
import dev.projectflint.system.ProjectFlintBrushSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrushItem.class)
public abstract class BrushItemMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void projectflint$startCustomBrush(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (!ProjectFlintBrushSystem.isProjectFlintBrush(context.getItemInHand())) return;
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (!projectflint$isTarget(state) || context.getPlayer() == null) return;
		context.getPlayer().startUsingItem(context.getHand());
		cir.setReturnValue(InteractionResult.CONSUME);
	}

	@Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
	private void projectflint$duration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		if (ProjectFlintBrushSystem.isProjectFlintBrush(stack)) cir.setReturnValue(ProjectFlintBrushSystem.BRUSH_TICKS);
	}

	@Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true)
	private void projectflint$customBrushTick(Level level, LivingEntity entity, ItemStack stack, int remaining, CallbackInfo ci) {
		if (!ProjectFlintBrushSystem.isProjectFlintBrush(stack)) return;
		ci.cancel();
		if (!(entity instanceof Player player)) return;
		HitResult result = player.pick(player.blockInteractionRange(), 0.0F, false);
		if (!(result instanceof BlockHitResult hit) || result.getType() != HitResult.Type.BLOCK) { player.releaseUsingItem(); return; }
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!projectflint$isTarget(state)) { player.releaseUsingItem(); return; }
		if (remaining % 10 == 0) level.playSound(player, pos, state.is(Blocks.GRAVEL) ? SoundEvents.BRUSH_GRAVEL : SoundEvents.BRUSH_SAND, SoundSource.BLOCKS, 0.55F, 0.92F);
		if (remaining > 1 || level.isClientSide()) return;
		if (state.is(Blocks.GRAVEL)) {
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(Items.FLINT));
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.FLINT_CHIP, 2));
			level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL_COMPLETED, SoundSource.BLOCKS, 0.8F, 1.0F);
			if (player instanceof ServerPlayer sp) ModAdvancements.award(sp, "vanilla_flint", "knapped_flint");
		} else {
			level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.SAND_FLAKE, 2));
			level.playSound(null, pos, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.BLOCKS, 0.8F, 1.0F);
			if (player instanceof ServerPlayer sp) ModAdvancements.award(sp, "sand_flake", "brushed_sand");
		}
		InteractionHand used = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		stack.hurtAndBreak(1, player, used);
		player.releaseUsingItem();
	}

	private static boolean projectflint$isTarget(BlockState state) {
		return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL);
	}
}
