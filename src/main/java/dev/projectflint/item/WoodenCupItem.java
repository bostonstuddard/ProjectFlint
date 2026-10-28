package dev.projectflint.item;

import dev.projectflint.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;

/** Empty wooden cup. Right-clicking a cow fills one cup with raw milk. */
public final class WoodenCupItem extends Item {
	public WoodenCupItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Cow)) return InteractionResult.PASS;
		if (player.level().isClientSide()) return InteractionResult.SUCCESS;

		ItemStack filled = new ItemStack(ModItems.WOODEN_CUP_RAW_MILK);
		player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
		player.level().playSound(null, target.blockPosition(), SoundEvents.COW_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}
}
