package dev.projectflint.block.entity;

import dev.projectflint.block.BookshelfBlock;
import dev.projectflint.menu.BookshelfMenu;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BookshelfBlockEntity extends BaseContainerBlockEntity {
	private NonNullList<ItemStack> items=NonNullList.withSize(2,ItemStack.EMPTY); private int booksStored;
	public BookshelfBlockEntity(BlockPos p,BlockState s){super(ModBlockEntities.BOOKSHELF,p,s);}
	public int getBooksStored(){return booksStored;}
	public void setBooksStored(int books){booksStored=Math.max(0,Math.min(4,books));setChanged();if(level!=null&&!level.isClientSide()){BlockState s=getBlockState();if(s.hasProperty(BookshelfBlock.FILLED)&&s.getValue(BookshelfBlock.FILLED)!=(booksStored>=4))level.setBlock(getBlockPos(),s.setValue(BookshelfBlock.FILLED,booksStored>=4),3);}}
	@Override protected void saveAdditional(ValueOutput o){super.saveAdditional(o);ContainerHelper.saveAllItems(o,items);o.putInt("BooksStored",booksStored);}
	@Override protected void loadAdditional(ValueInput i){super.loadAdditional(i);items=NonNullList.withSize(2,ItemStack.EMPTY);ContainerHelper.loadAllItems(i,items);booksStored=i.getIntOr("BooksStored",0);}
	@Override public int getContainerSize(){return 2;} @Override protected NonNullList<ItemStack> getItems(){return items;} @Override protected void setItems(NonNullList<ItemStack> i){items=i;}
	@Override protected Component getDefaultName(){return Component.translatable("container.projectflint.bookshelf");}
	@Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new BookshelfMenu(id,inv,this);}
}
