package dev.projectflint.item;

import dev.projectflint.registry.ModBlocks;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;

/** Main-hand charge + offhand firestarter places a live 4.5-second fuse. */
public final class DynamiteItem extends BlockItem {
    public DynamiteItem(Properties properties) { super(ModBlocks.DYNAMITE_CHARGE, properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;
        ItemStack igniter = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!(igniter.is(Items.FLINT_AND_STEEL)
                || ToolCapabilities.has(igniter, ToolCapabilities.Capability.FIRESTARTER))) return InteractionResult.FAIL;
        if (igniter.isDamageableItem() && igniter.getDamageValue() >= igniter.getMaxDamage()) return InteractionResult.FAIL;
        InteractionResult result = super.useOn(context);
        if (result.consumesAction() && !context.getLevel().isClientSide() && !player.isCreative()
                && igniter.isDamageableItem()) {
            igniter.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
        }
        return result;
    }
}
