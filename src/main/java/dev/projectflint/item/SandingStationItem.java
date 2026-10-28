package dev.projectflint.item;

import dev.projectflint.block.entity.SandingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Block item that preserves a Sanding Station's stored sanding reserve. */
public final class SandingStationItem extends BlockItem {
	private static final String DURABILITY_KEY = "ProjectFlintSandingDurability";

	public SandingStationItem(Block block, Properties properties) {
		super(block, properties.stacksTo(1));
	}

	public static ItemStack withDurability(ItemStack stack, int durability) {
		int clamped = Math.max(0, Math.min(SandingStationBlockEntity.MAX_DURABILITY, durability));
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(DURABILITY_KEY, clamped));
		return stack;
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
		boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data != null && level.getBlockEntity(pos) instanceof SandingStationBlockEntity station) {
			station.setDurability(data.copyTag().getIntOr(DURABILITY_KEY, 0));
			return true;
		}
		return changed;
	}
}
