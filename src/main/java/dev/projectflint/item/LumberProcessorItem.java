package dev.projectflint.item;

import dev.projectflint.system.LumberProcessorSystem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class LumberProcessorItem extends Item {
    public LumberProcessorItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public InteractionResult useOn(UseOnContext context) { return LumberProcessorSystem.placeRecovered(context); }
}
