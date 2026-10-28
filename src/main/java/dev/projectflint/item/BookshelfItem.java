package dev.projectflint.item;

import dev.projectflint.block.entity.BookshelfBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public final class BookshelfItem extends BlockItem {
	public static final String KEY="ProjectFlintBooks";
	public BookshelfItem(Block block, Properties properties){super(block,properties);}
	public static ItemStack withBooks(net.minecraft.world.item.Item item,int books){ItemStack stack=new ItemStack(item);CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt(KEY,books));return stack;}
	@Override public InteractionResult place(BlockPlaceContext context){
		int books=context.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr(KEY,0);
		InteractionResult result=super.place(context);
		if(result.consumesAction()&&!context.getLevel().isClientSide()){
			net.minecraft.core.BlockPos pos=context.getClickedPos();
			if(context.getLevel().getBlockEntity(pos) instanceof BookshelfBlockEntity be) be.setBooksStored(books);
		}
		return result;
	}
}
