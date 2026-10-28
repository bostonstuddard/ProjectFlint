package dev.projectflint.block;

import com.mojang.serialization.MapCodec;
import dev.projectflint.block.entity.WoodenChestBlockEntity;
import dev.projectflint.menu.WoodenChestMenu;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Vanilla-style 9-slot wooden chest. Two adjacent chests combine into 18 slots. */
public final class WoodenChestBlock extends ChestBlock {
	public static final MapCodec<WoodenChestBlock> CODEC = simpleCodec(WoodenChestBlock::new);

	public WoodenChestBlock(BlockBehaviour.Properties properties) {
		super(() -> ModBlockEntities.WOODEN_CHEST, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
	}

	@Override public MapCodec<? extends WoodenChestBlock> codec() { return CODEC; }

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		Container container = ChestBlock.getContainer(this, state, level, pos, false);
		if (container == null) return null;
		int rows = container.getContainerSize() >= 18 ? 2 : 1;
		return new SimpleMenuProvider((syncId, inventory, player) -> new WoodenChestMenu(
			rows == 2 ? dev.projectflint.registry.ModMenus.WOODEN_DOUBLE_CHEST : dev.projectflint.registry.ModMenus.WOODEN_CHEST,
			syncId, inventory, container, rows
		), Component.translatable("container.projectflint.wooden_chest"));
	}

	@Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WoodenChestBlockEntity(pos, state); }
}
