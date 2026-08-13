package dev.projectflint.block;

import dev.projectflint.block.entity.FlintTorchBlockEntity;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Permanent-ish wall light for the Wattle & Daub construction tier.
 *
 * The fixture is deliberately picky: it must be attached to a normal Wattled
 * Daub Brick on structural layer 2 or 3, have a full 3x3 Wattled-Daub canopy
 * one or two blocks above the torch, and have no unsafe same-level neighbors.
 */
public final class FlintTorchBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FlintTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null;
        BlockState state = defaultBlockState().setValue(FACING, face).setValue(LIT, false);
        return isValidFixture(context.getLevel(), context.getClickedPos(), state) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidFixture(level, pos, state);
    }

    public static boolean isValidFixture(LevelReader level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);

        // Holder itself must be fixed to an ordinary Wattled Daub Brick, never a window,
        // and only to the middle two structural layers.
        if (!support.is(ModBlocks.WATTLED_DAUB_BRICKS)) return false;
        int supportLayer = WattledDaubBricksBlock.verticalStructuralLayer(level, supportPos);
        if (supportLayer != 2 && supportLayer != 3) return false;

        // Every touching block in the horizontal 3x3x1 safety plane must be air or
        // nonflammable Wattled Daub wall material. The torch itself is skipped.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (!isSafeNeighbor(level.getBlockState(pos.offset(dx, 0, dz)))) return false;
            }
        }

        // A full 3x3 Wattled-Daub canopy must exist overhead. Because holders may be
        // mounted on layer 2 or 3 of a four-high wall, the roof can be either one or
        // two blocks above the fixture. This keeps both allowed wall layers usable.
        for (int rise = 1; rise <= 2; rise++) {
            BlockPos overheadCenter = pos.above(rise);
            BlockState center = level.getBlockState(overheadCenter);
            if (!center.is(ModBlocks.WATTLED_DAUB_BRICKS) && !center.is(ModBlocks.WATTLED_DAUB_WINDOW)) continue;

            boolean complete = true;
            for (int dx = -1; dx <= 1 && complete; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockState overhead = level.getBlockState(overheadCenter.offset(dx, 0, dz));
                    if (!overhead.is(ModBlocks.WATTLED_DAUB_BRICKS) && !overhead.is(ModBlocks.WATTLED_DAUB_WINDOW)) {
                        complete = false;
                        break;
                    }
                }
            }
            if (complete) return true;
        }
        return false;
    }

    private static boolean isSafeNeighbor(BlockState state) {
        return state.isAir() || state.is(ModBlocks.WATTLED_DAUB_BRICKS) || state.is(ModBlocks.WATTLED_DAUB_WINDOW);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return WallTorchBlock.getShape(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(LIT)) return InteractionResult.FAIL;

        // Client block entities do not need to mirror the loaded-Flint flag just to
        // decide whether an interaction packet should be sent. The server remains
        // authoritative for whether loading/ignition is actually valid.
        if (level.isClientSide()) {
            return stack.is(Items.FLINT) || isIgnitionSource(stack) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        if (!(level.getBlockEntity(pos) instanceof FlintTorchBlockEntity torch)) return InteractionResult.FAIL;

        if (stack.is(Items.FLINT)) {
            if (torch.hasFlint()) return InteractionResult.FAIL;
            if (!player.isCreative()) stack.shrink(1);
            torch.loadFlint();
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.65F, 1.12F);
            return InteractionResult.SUCCESS;
        }

        if (!torch.hasFlint() || !isIgnitionSource(stack)) return InteractionResult.FAIL;
        if (stack.is(Items.FLINT_AND_STEEL) || ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER)) {
            stack.hurtAndBreak(1, player, hand);
        }
        torch.light();
        level.setBlockAndUpdate(pos, state.setValue(LIT, true));
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.85F, 0.95F);
        return InteractionResult.SUCCESS;
    }

    private static boolean isIgnitionSource(ItemStack stack) {
        return stack.is(Items.TORCH)
            || stack.is(ModItems.CRUDE_PLANT_TORCH)
            || stack.is(Items.FLINT_AND_STEEL)
            || ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5D - 0.27D * facing.getStepX();
        double y = pos.getY() + 0.72D;
        double z = pos.getZ() + 0.5D - 0.27D * facing.getStepZ();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlintTorchBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel serverLevel && be instanceof FlintTorchBlockEntity torch) {
                torch.serverTick(serverLevel, pos, st);
            }
        };
    }
}
