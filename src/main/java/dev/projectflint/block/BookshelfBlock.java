package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.BookshelfBlockEntity;
import dev.projectflint.item.BookshelfItem;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BookshelfBlock extends BaseEntityBlock {
	public static final MapCodec<BookshelfBlock> CODEC=simpleCodec(BookshelfBlock::new); public static final BooleanProperty FILLED=BooleanProperty.create("filled");
	public BookshelfBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,net.minecraft.core.Direction.NORTH).setValue(FILLED,false));}
	@Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
	@Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
	@Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,context.getHorizontalDirection().getOpposite()).setValue(FILLED,false);}
	@Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(HorizontalDirectionalBlock.FACING,rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));}
	@Override protected BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));}
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b){super.createBlockStateDefinition(b);b.add(HorizontalDirectionalBlock.FACING,FILLED);}
	@Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
		if(stack.is(Items.BOOK)&&level.getBlockEntity(pos) instanceof BookshelfBlockEntity be&&be.getBooksStored()<4){if(!level.isClientSide()){if(!player.isCreative())stack.shrink(1);be.setBooksStored(be.getBooksStored()+1);if(be.getBooksStored()>=4&&player instanceof ServerPlayer sp)ModAdvancements.awardAll(sp,"filled_bookshelf");}return InteractionResult.SUCCESS;}
		return open(level,pos,player);
	}
	@Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player pl,BlockHitResult h){return open(l,p,pl);}
	private static InteractionResult open(Level level,BlockPos pos,Player player){if(!level.isClientSide()&&level.getBlockEntity(pos) instanceof BookshelfBlockEntity be)player.openMenu(be);return InteractionResult.SUCCESS;}
	@Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){if(!level.isClientSide()&&!player.isCreative()&&level.getBlockEntity(pos) instanceof BookshelfBlockEntity be){Containers.dropContents(level,pos,be);net.minecraft.world.level.block.Block.popResource(level,pos,BookshelfItem.withBooks(ModItems.BOOKSHELF,be.getBooksStored()));}return super.playerWillDestroy(level,pos,state,player);}
	@Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new BookshelfBlockEntity(pos,state);}
}
