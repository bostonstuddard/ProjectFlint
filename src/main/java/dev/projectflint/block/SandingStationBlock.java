package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.SandingStationBlockEntity;
import dev.projectflint.menu.SandingStationMenu;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class SandingStationBlock extends BaseEntityBlock {
    public static final MapCodec<SandingStationBlock> CODEC = simpleCodec(SandingStationBlock::new);
    private static final Component TITLE = Component.translatable("container.projectflint.sanding_station");

    public SandingStationBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Block.box(0, 0, 0, 16, 4, 16); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModItems.SANDING_PAPER)) {
            if (level.getBlockEntity(pos) instanceof SandingStationBlockEntity station && station.durability() < SandingStationBlockEntity.MAX_DURABILITY) {
                if (!level.isClientSide()) {
                    if (station.addPaper()) {
                        if (!player.isCreative()) stack.shrink(1);
                        if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "sanding_station_loaded");
                    }
                    level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.7F, 0.9F);
                    player.displayClientMessage(Component.literal("Sanding: " + station.durability() + " / " + SandingStationBlockEntity.MAX_DURABILITY), true);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return open(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return open(level, pos, player);
    }

    private InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SandingStationBlockEntity station) {
            MenuProvider provider = new SimpleMenuProvider(
                (syncId, inventory, p) -> new SandingStationMenu(syncId, inventory, ContainerLevelAccess.create(level, pos), station),
                TITLE
            );
            player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SandingStationBlockEntity(pos, state); }
}
