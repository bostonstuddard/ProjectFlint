package dev.projectflint.client.screen;

import dev.projectflint.menu.WoodenChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact vanilla-shaped 9/18-slot chest GUI. */
public final class WoodenChestScreen extends AbstractContainerScreen<WoodenChestMenu> {
	private static final int FRAME=0xFF2B2823, PANEL=0xFFC7A87A, SLOT_BORDER=0xFFE2CF9F, SLOT_FILL=0xFF8C765A;
	public WoodenChestScreen(WoodenChestMenu menu, Inventory inv, Component title) {
		super(menu,inv,title); imageWidth=176; imageHeight=114+menu.rows()*18; titleLabelX=8; titleLabelY=6; inventoryLabelX=8; inventoryLabelY=20+menu.rows()*18;
	}
	@Override protected void renderBg(GuiGraphics g,float partial,int mx,int my) {
		int x=leftPos,y=topPos; g.fill(x,y,x+imageWidth,y+imageHeight,FRAME); g.fill(x+3,y+3,x+imageWidth-3,y+imageHeight-3,PANEL);
		for(int r=0;r<menu.rows();r++) for(int c=0;c<9;c++) well(g,x+8+c*18,y+18+r*18);
		int iy=31+menu.rows()*18; for(int r=0;r<3;r++) for(int c=0;c<9;c++) well(g,x+8+c*18,y+iy+r*18); for(int c=0;c<9;c++) well(g,x+8+c*18,y+iy+58);
	}
	private static void well(GuiGraphics g,int x,int y){g.fill(x-1,y-1,x+17,y+17,SLOT_BORDER);g.fill(x,y,x+16,y+16,SLOT_FILL);}
	@Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
