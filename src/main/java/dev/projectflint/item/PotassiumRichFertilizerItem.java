package dev.projectflint.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Uses vanilla bonemeal behavior on plants, but cannot force saplings or tree fungi to grow. */
public final class PotassiumRichFertilizerItem extends BoneMealItem {
    public PotassiumRichFertilizerItem(Item.Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.is(BlockTags.SAPLINGS) || state.is(Blocks.CRIMSON_FUNGUS)
            || state.is(Blocks.WARPED_FUNGUS)) return InteractionResult.PASS;
        return super.useOn(context);
    }
}
