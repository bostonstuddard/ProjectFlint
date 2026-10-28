package dev.projectflint.item;
import dev.projectflint.block.entity.WaterPotBlockEntity;
import net.minecraft.core.BlockPos; import net.minecraft.core.component.DataComponents; import net.minecraft.world.entity.player.Player; import net.minecraft.world.item.BlockItem; import net.minecraft.world.item.ItemStack; import net.minecraft.world.item.component.CustomData; import net.minecraft.world.level.Level; import net.minecraft.world.level.block.Block; import net.minecraft.world.level.block.state.BlockState;
public final class WaterPotItem extends BlockItem {
 private static final String KEY="ProjectFlintWaterUnits"; public WaterPotItem(Block b, Properties p){super(b,p);}
 public static ItemStack withWater(ItemStack s,int units){ CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt(KEY,Math.max(0,Math.min(2,units)))); return s; }
 @Override protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state){ boolean changed=super.updateCustomBlockEntityTag(pos,level,player,stack,state); CustomData d=stack.get(DataComponents.CUSTOM_DATA); if(d!=null && level.getBlockEntity(pos) instanceof WaterPotBlockEntity pot){ pot.setWaterUnits(Math.max(0,Math.min(2,d.copyTag().getIntOr(KEY,0)))); return true;} return changed;}
}
