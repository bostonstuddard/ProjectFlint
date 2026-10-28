package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.menu.HardwoodCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Mobile 3x3 worktable; recipes require three seconds before pickup. */
public final class HardwoodCraftingTableBlock extends CraftingTableBlock {
    public static final MapCodec<HardwoodCraftingTableBlock> CODEC = simpleCodec(HardwoodCraftingTableBlock::new);
    public HardwoodCraftingTableBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override public MapCodec<? extends CraftingTableBlock> codec() { return CODEC; }
    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
            new HardwoodCraftingMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
            Component.translatable("container.projectflint.hardwood_crafting_table"));
    }
}
