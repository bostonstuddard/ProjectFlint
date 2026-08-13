package dev.projectflint.item;

import dev.projectflint.block.entity.WaterPotBlockEntity;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/** Half-bucket clay water carrier. The filled form is an internal state item with the same display name. */
public final class WaterBearingPotItem extends Item {
    private final boolean filled;

    public WaterBearingPotItem(Properties properties, boolean filled) {
        super(properties.stacksTo(1).overrideDescription("item.projectflint.water_bearing_pot"));
        this.filled = filled;
    }

    public boolean isFilled() { return filled; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (filled) return InteractionResult.PASS;
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos pos = hit.getBlockPos();
        if (!level.getBlockState(pos).is(Blocks.WATER) || !level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos).isSource()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (!player.isCreative()) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            player.setItemInHand(hand, new ItemStack(ModItems.WATER_BEARING_POT_FILLED));
            if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "water_bearing_pot_filled");
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.7F, 1.1F);
            player.displayClientMessage(Component.literal("Water Bearing Pot: 0.5 bucket"), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!filled) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(ModBlocks.WATER_POT)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof WaterPotBlockEntity pot) || pot.waterUnits() >= WaterPotBlockEntity.MAX_UNITS) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            pot.addHalfBucket();
            Player player = context.getPlayer();
            if (pot.isFull() && player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "water_pot_filled");
            if (player != null && !player.isCreative()) player.setItemInHand(context.getHand(), new ItemStack(ModItems.WATER_BEARING_POT));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.7F, 1.1F);
            if (player != null) player.displayClientMessage(Component.literal("Water Pot: " + pot.waterUnits() * 0.5F + " / 1.0 bucket"), true);
        }
        return InteractionResult.SUCCESS;
    }
}
