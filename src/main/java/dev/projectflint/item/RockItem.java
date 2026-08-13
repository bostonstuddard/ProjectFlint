package dev.projectflint.item;

import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInteractionSystem;
import dev.projectflint.system.StoneWorkingSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Loose Rock item. Rocks are no longer directly placeable; physical placement
 * was removed after alpha.14 playtesting. The item still owns stone working and
 * the Rock-on-Crushed-Stone refinement interaction.
 */
public final class RockItem extends Item {
	public RockItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!level.getBlockState(context.getClickedPos()).is(ModBlocks.CRUSHED_STONE)) {
			return InteractionResult.PASS;
		}

		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ItemStack held = context.getItemInHand();
		if (!held.is(ModItems.ROCK)) {
			return InteractionResult.PASS;
		}

		// Use an explicit one-item transaction so larger Rock stacks remain intact.
		// This is reliable even with Project Flint's custom five-slot inventory path.
		if (!player.isCreative()) {
			held.shrink(1);
		}
		PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.VERY_REFINED_STONE));
		level.playSound(null, context.getClickedPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.65F, 1.35F);

		if (player instanceof ServerPlayer serverPlayer) {
			ModAdvancements.award(serverPlayer, "very_refined_stone", "made_very_refined_stone");
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}

		ItemStack mainHand = player.getMainHandItem();
		ItemStack offHand = player.getOffhandItem();
		if (!mainHand.is(ModItems.ROCK) || !offHand.is(ModItems.ROCK)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		StoneWorkingSystem.StoneWorkingResult result = StoneWorkingSystem.strike(level, player);
		if (result == StoneWorkingSystem.StoneWorkingResult.IN_PROGRESS) {
			return InteractionResult.SUCCESS;
		}

		// Never transform the whole held stack. One knapping attempt consumes exactly
		// one Rock from the main hand whether it succeeds or fails.
		if (!player.isCreative()) {
			mainHand.shrink(1);
		}
		if (result == StoneWorkingSystem.StoneWorkingResult.SUCCESS) {
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.REFINED_ROCK));
		}
		return InteractionResult.SUCCESS;
	}
}
