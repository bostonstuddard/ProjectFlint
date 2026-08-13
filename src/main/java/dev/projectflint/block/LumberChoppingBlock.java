package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.LumberChoppingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class LumberChoppingBlock extends BaseEntityBlock {
    public static final MapCodec<LumberChoppingBlock> CODEC = simpleCodec(LumberChoppingBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public LumberChoppingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LumberChoppingBlockEntity(pos, state); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof LumberChoppingBlockEntity controller) {
            if (!level.isClientSide()) {
                controller.cycleOutput();
                player.displayClientMessage(Component.literal(controller.outputMode() == 0
                    ? "Lumber Processor: 2 Wooden Boards"
                    : "Lumber Processor: 1 Wooden Beam"), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, dev.projectflint.registry.ModBlockEntities.LUMBER_CHOPPING_BLOCK,
            (world, pos, blockState, blockEntity) -> blockEntity.serverTick((ServerLevel) world, pos, blockState));
    }
}
