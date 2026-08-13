package dev.projectflint.system;

import dev.projectflint.block.LumberChoppingBlock;
import dev.projectflint.block.LumberWorkpieceBlock;
import dev.projectflint.block.WaterPotBlock;
import dev.projectflint.block.entity.LumberChoppingBlockEntity;
import dev.projectflint.block.entity.WaterPotBlockEntity;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Formation, validation, processing, recovery, and fail-safe teardown for the 3x1x2 Lumber Processor. */
public final class LumberProcessorSystem {
    private LumberProcessorSystem() {}

    public static InteractionResult tryForm(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos left = context.getClickedPos();
        if (!level.getBlockState(left).is(ModBlocks.WOVEN_WOOD)) return InteractionResult.PASS;

        Direction found = null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (canForm(level, left, direction, false)) {
                found = direction;
                break;
            }
        }
        if (found == null) {
            Player player = context.getPlayer();
            if (player != null && !level.isClientSide()) {
                player.displayClientMessage(Component.literal("Need Woven Wood → Water Pot → Crushed Stone, all supported with one air block above."), true);
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            form(level, left, found);
            Player player = context.getPlayer();
            if (player != null && !player.isCreative()) context.getItemInHand().shrink(1);
            if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "lumber_processor");
            level.playSound(null, left, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult placeRecovered(UseOnContext context) {
        BlockPlaceContext placement = new BlockPlaceContext(context);
        Level level = context.getLevel();
        BlockPos left = placement.getClickedPos();
        Direction direction = context.getHorizontalDirection();
        if (!direction.getAxis().isHorizontal()) direction = Direction.NORTH;
        if (!canForm(level, left, direction, true)) {
            if (context.getPlayer() != null && !level.isClientSide()) {
                context.getPlayer().displayClientMessage(Component.literal("The Lumber Processor needs three supported blocks in a row and clear air above."), true);
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(left, ModBlocks.LUMBER_CHOPPING_BLOCK.defaultBlockState().setValue(LumberChoppingBlock.FACING, direction));
            level.setBlockAndUpdate(left.relative(direction), ModBlocks.WATER_POT.defaultBlockState().setValue(WaterPotBlock.FACING, direction.getOpposite()));
            level.setBlockAndUpdate(left.relative(direction, 2), ModBlocks.CRUSHED_STONE.defaultBlockState());
            link(level, left, direction);
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
            level.playSound(null, left, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean canForm(LevelReader level, BlockPos left, Direction direction, boolean emptyBase) {
        BlockPos middle = left.relative(direction);
        BlockPos end = left.relative(direction, 2);
        if (emptyBase) {
            if (!level.getBlockState(left).isAir() || !level.getBlockState(middle).isAir() || !level.getBlockState(end).isAir()) return false;
        } else {
            if (!level.getBlockState(left).is(ModBlocks.WOVEN_WOOD)
                || !level.getBlockState(middle).is(ModBlocks.WATER_POT)
                || !level.getBlockState(end).is(ModBlocks.CRUSHED_STONE)) return false;
        }
        for (BlockPos base : new BlockPos[] {left, middle, end}) {
            BlockPos below = base.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return false;
            if (!level.getBlockState(base.above()).isAir()) return false;
        }
        return true;
    }

    private static void form(Level level, BlockPos left, Direction direction) {
        level.setBlockAndUpdate(left, ModBlocks.LUMBER_CHOPPING_BLOCK.defaultBlockState().setValue(LumberChoppingBlock.FACING, direction));
        link(level, left, direction);
    }

    private static void link(Level level, BlockPos left, Direction direction) {
        if (level.getBlockEntity(left) instanceof LumberChoppingBlockEntity controller) controller.initializeFormed();
        BlockPos middle = left.relative(direction);
        if (level.getBlockEntity(middle) instanceof WaterPotBlockEntity pot) pot.link(left);
    }

    public static boolean isController(BlockGetter level, BlockPos pos) {
        return pos != null && level.getBlockState(pos).is(ModBlocks.LUMBER_CHOPPING_BLOCK);
    }

    public static boolean isValidProcessor(LevelReader level, BlockPos controller, Direction direction) {
        if (controller == null) return false;
        BlockPos middle = controller.relative(direction);
        BlockPos end = controller.relative(direction, 2);
        BlockState controllerState = level.getBlockState(controller);
        if (!controllerState.is(ModBlocks.LUMBER_CHOPPING_BLOCK)
            || controllerState.getValue(LumberChoppingBlock.FACING) != direction
            || !level.getBlockState(middle).is(ModBlocks.WATER_POT)
            || !level.getBlockState(end).is(ModBlocks.CRUSHED_STONE)) return false;
        for (BlockPos base : new BlockPos[] {controller, middle, end}) {
            BlockPos below = base.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return false;
        }
        if (level.getBlockEntity(middle) instanceof WaterPotBlockEntity pot) {
            BlockPos linked = pot.linkedController();
            return linked != null && linked.equals(controller);
        }
        return false;
    }

    public static boolean hasFullWater(Level level, BlockPos controller, Direction direction) {
        BlockPos middle = controller.relative(direction);
        return level.getBlockEntity(middle) instanceof WaterPotBlockEntity pot && pot.isFull();
    }

    public static boolean canChop(Level level, BlockPos controller) {
        BlockState state = level.getBlockState(controller);
        if (!state.is(ModBlocks.LUMBER_CHOPPING_BLOCK)) return false;
        Direction direction = state.getValue(LumberChoppingBlock.FACING);
        if (!isValidProcessor(level, controller, direction) || !hasFullWater(level, controller, direction)) return false;
        // Treat the actual block above the controller as authoritative. The old implementation
        // depended on inputArmed being set by a later block-entity tick, so starting to mine
        // immediately after placing the log could be rejected even though the machine was valid.
        if (!isSupportedStrippedLog(level.getBlockState(controller.above()))) return false;
        return level.getBlockEntity(controller) instanceof LumberChoppingBlockEntity be
            && !be.washing() && !be.outputReady();
    }

    public static boolean startWash(ServerLevel level, BlockPos controller) {
        BlockState state = level.getBlockState(controller);
        if (!state.is(ModBlocks.LUMBER_CHOPPING_BLOCK) || !canChop(level, controller)) return false;
        if (!(level.getBlockEntity(controller) instanceof LumberChoppingBlockEntity be)
            || !be.armInput() || !be.startWash()) return false;
        level.removeBlock(controller.above(), false);
        level.playSound(null, controller, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.75F, 0.85F);
        return true;
    }

    public static boolean completeWash(ServerLevel level, BlockPos controller, Direction direction, int outputMode) {
        if (!isValidProcessor(level, controller, direction)) return false;
        BlockPos middle = controller.relative(direction);
        BlockPos endTop = controller.relative(direction, 2).above();
        if (!level.getBlockState(endTop).isAir()) return false;
        if (!(level.getBlockEntity(middle) instanceof WaterPotBlockEntity pot) || !pot.consumeFullBucket()) return false;
        level.setBlockAndUpdate(endTop, ModBlocks.LUMBER_WORKPIECE.defaultBlockState()
            .setValue(LumberWorkpieceBlock.FACING, direction)
            .setValue(LumberWorkpieceBlock.OUTPUT, outputMode));
        level.playSound(null, middle, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.65F, 0.85F);
        return true;
    }

    public static boolean hasWorkpiece(LevelReader level, BlockPos controller, Direction direction) {
        BlockPos pos = controller.relative(direction, 2).above();
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.LUMBER_WORKPIECE) && state.getValue(LumberWorkpieceBlock.FACING) == direction;
    }

    public static boolean harvestWorkpiece(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.LUMBER_WORKPIECE)) return false;
        Direction direction = state.getValue(LumberWorkpieceBlock.FACING);
        BlockPos controllerPos = pos.below().relative(direction.getOpposite(), 2);
        if (!(level.getBlockEntity(controllerPos) instanceof LumberChoppingBlockEntity controller) || !controller.outputReady()) return false;
        controller.markOutputTaken();
        level.removeBlock(pos, false);
        ItemStack result = state.getValue(LumberWorkpieceBlock.OUTPUT) == 0
            ? new ItemStack(ModItems.WOODEN_BOARD, 2)
            : new ItemStack(ModItems.WOODEN_BEAM);
        Block.popResource(level, pos, result);
        level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.75F, 0.9F);
        return true;
    }

    public static boolean isSupportedStrippedLog(BlockState state) {
        return state.is(Blocks.STRIPPED_OAK_LOG) || state.is(Blocks.STRIPPED_SPRUCE_LOG)
            || state.is(Blocks.STRIPPED_BIRCH_LOG) || state.is(Blocks.STRIPPED_JUNGLE_LOG)
            || state.is(Blocks.STRIPPED_ACACIA_LOG) || state.is(Blocks.STRIPPED_DARK_OAK_LOG)
            || state.is(Blocks.STRIPPED_MANGROVE_LOG) || state.is(Blocks.STRIPPED_CHERRY_LOG)
            || state.is(Blocks.STRIPPED_PALE_OAK_LOG);
    }

    public static boolean isProcessorPart(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.LUMBER_CHOPPING_BLOCK)) return true;
        if (state.is(ModBlocks.WATER_POT) && level.getBlockEntity(pos) instanceof WaterPotBlockEntity pot && pot.isLinked()) return true;
        if (state.is(ModBlocks.CRUSHED_STONE)) return findControllerForPart(level, pos) != null;
        return false;
    }

    public static BlockPos findControllerForPart(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.LUMBER_CHOPPING_BLOCK)) return pos;
        if (state.is(ModBlocks.WATER_POT) && level.getBlockEntity(pos) instanceof WaterPotBlockEntity pot) return pot.linkedController();
        if (state.is(ModBlocks.CRUSHED_STONE)) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = pos.relative(direction, 2);
                BlockState candidateState = level.getBlockState(candidate);
                if (!candidateState.is(ModBlocks.LUMBER_CHOPPING_BLOCK)) continue;
                Direction facing = candidateState.getValue(LumberChoppingBlock.FACING);
                if (candidate.relative(facing, 2).equals(pos)) return candidate;
            }
        }
        return null;
    }

    public static void recover(ServerLevel level, BlockPos anyPart, Player player) {
        BlockPos controller = findControllerForPart(level, anyPart);
        if (controller == null) return;
        removeAssembly(level, controller, true);
        Block.popResource(level, anyPart, new ItemStack(ModItems.LUMBER_PROCESSOR));
        level.playSound(null, anyPart, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 0.82F);
    }

    public static void collapse(ServerLevel level, BlockPos controller, boolean preserveAsItem) {
        if (controller == null) return;
        BlockState controllerState = level.getBlockState(controller);
        Direction direction = controllerState.is(ModBlocks.LUMBER_CHOPPING_BLOCK)
            ? controllerState.getValue(LumberChoppingBlock.FACING)
            : findDirectionFromLinkedPot(level, controller);
        if (direction == null) return;
        if (preserveAsItem) {
            removeAssembly(level, controller, true);
            Block.popResource(level, controller, new ItemStack(ModItems.LUMBER_PROCESSOR));
            return;
        }

        BlockPos middle = controller.relative(direction);
        BlockPos end = controller.relative(direction, 2);
        dropTopIfRecoverable(level, controller.above());
        dropTopIfRecoverable(level, middle.above());
        level.removeBlock(end.above(), false); // unfinished/finished workpiece is lost on structural failure

        if (level.getBlockState(controller).is(ModBlocks.LUMBER_CHOPPING_BLOCK)) Block.popResource(level, controller, new ItemStack(ModItems.WOVEN_WOOD));
        if (level.getBlockState(middle).is(ModBlocks.WATER_POT)) Block.popResource(level, middle, new ItemStack(ModItems.WATER_POT));
        if (level.getBlockState(end).is(ModBlocks.CRUSHED_STONE)) Block.popResource(level, end, new ItemStack(ModItems.CRUSHED_STONE_BLOCK_ITEM));
        level.removeBlock(controller, false);
        level.removeBlock(middle, false);
        level.removeBlock(end, false);
        level.playSound(null, controller, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.85F, 0.7F);
    }

    private static Direction findDirectionFromLinkedPot(BlockGetter level, BlockPos controller) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos middle = controller.relative(direction);
            if (level.getBlockEntity(middle) instanceof WaterPotBlockEntity pot) {
                BlockPos linked = pot.linkedController();
                if (linked != null && linked.equals(controller)) return direction;
            }
        }
        return null;
    }

    private static void removeAssembly(ServerLevel level, BlockPos controller, boolean removeTop) {
        BlockState state = level.getBlockState(controller);
        if (!state.is(ModBlocks.LUMBER_CHOPPING_BLOCK)) return;
        Direction direction = state.getValue(LumberChoppingBlock.FACING);
        BlockPos middle = controller.relative(direction);
        BlockPos end = controller.relative(direction, 2);
        if (removeTop) {
            level.removeBlock(controller.above(), false);
            level.removeBlock(middle.above(), false);
            level.removeBlock(end.above(), false);
        }
        level.removeBlock(controller, false);
        level.removeBlock(middle, false);
        level.removeBlock(end, false);
    }

    private static void dropTopIfRecoverable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(ModBlocks.LUMBER_WORKPIECE)) {
            level.removeBlock(pos, false);
            return;
        }
        if (isSupportedStrippedLog(state) && state.getBlock().asItem() != net.minecraft.world.item.Items.AIR) {
            Block.popResource(level, pos, new ItemStack(state.getBlock().asItem()));
        }
        level.removeBlock(pos, false);
    }
}
