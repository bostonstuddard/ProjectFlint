package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.WaterPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import dev.projectflint.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** One-bucket movable water vessel. Deliberately non-directional. */
public final class WaterPotBlock extends BaseEntityBlock {
    public static final MapCodec<WaterPotBlock> CODEC = simpleCodec(WaterPotBlock::new);
    public WaterPotBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WaterPotBlockEntity(pos, state); }
    /** One filled Clay Water Bucket fills only an entirely empty Water Pot.
     * An already partially filled pot is deliberately not topped off this way.
     */
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                     Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.CLAY_WATER_BUCKET)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof WaterPotBlockEntity pot) || pot.waterUnits() != 0)
            return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            pot.setWaterUnits(WaterPotBlockEntity.MAX_UNITS);
            level.sendBlockUpdated(pos, state, state, 3);
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
                ItemStack empty = new ItemStack(ModItems.CLAY_BUCKET);
                if (stack.isEmpty()) player.setItemInHand(hand, empty);
                else if (!player.getInventory().add(empty)) player.drop(empty, false);
            }
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof WaterPotBlockEntity pot) player.displayClientMessage(Component.literal("Water Pot: " + pot.waterUnits() * 0.5F + " / 1.0 bucket"), true);
        return InteractionResult.SUCCESS;
    }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, dev.projectflint.registry.ModBlockEntities.WATER_POT, (world, pos, blockState, blockEntity) -> blockEntity.serverTick((ServerLevel) world));
    }
}
