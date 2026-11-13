package dev.projectflint.system;

import com.mojang.serialization.Codec;
import dev.projectflint.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/** Per-dimension, persistent evidence that a block was placed by a player, not generated naturally. */
public final class PlayerPlacedBlockLedger extends SavedData {
	private static final Codec<PlayerPlacedBlockLedger> CODEC = CompoundTag.CODEC.xmap(PlayerPlacedBlockLedger::read, PlayerPlacedBlockLedger::write);
	private static final SavedDataType<PlayerPlacedBlockLedger> TYPE = new SavedDataType<>(
		"projectflint_player_placed_blocks", PlayerPlacedBlockLedger::new, CODEC, DataFixTypes.LEVEL);
	private final Map<Long, String> placed = new HashMap<>();

	private static PlayerPlacedBlockLedger read(CompoundTag tag) {
		PlayerPlacedBlockLedger result = new PlayerPlacedBlockLedger();
		CompoundTag entries = tag.getCompoundOrEmpty("Blocks");
		for (String pos : entries.keySet()) {
			try { result.placed.put(Long.parseLong(pos), entries.getStringOr(pos, "")); }
			catch (NumberFormatException ignored) { }
		}
		return result;
	}
	private CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		CompoundTag entries = new CompoundTag();
		placed.forEach((pos, id) -> entries.putString(Long.toString(pos), id));
		tag.put("Blocks", entries);
		return tag;
	}
	private static PlayerPlacedBlockLedger of(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }

	public static void record(ServerLevel level, BlockPos pos, BlockState state) {
		if (!potential(level, pos, state)) return;
		PlayerPlacedBlockLedger ledger = of(level);
		ledger.placed.put(pos.asLong(), BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
		ledger.setDirty();
	}

	public static void clear(ServerLevel level, BlockPos pos) {
		PlayerPlacedBlockLedger ledger = of(level);
		if (ledger.placed.remove(pos.asLong()) != null) ledger.setDirty();
	}

	public static boolean recoverable(Player player, ServerLevel level, BlockPos pos, BlockState state) {
		if (!potential(level, pos, state)) return false;
		PlayerPlacedBlockLedger ledger = of(level);
		String registered = ledger.placed.get(pos.asLong());
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(registered);
	}

	/** Explicitly limited to small, safely recoverable decorations. No structural salvage shortcut. */
    public static boolean isHandRecoverableDecoration(BlockState state) {
        return state.is(Blocks.TRIPWIRE) || state.is(ModBlocks.AXLE) || state.is(ModBlocks.FAT_BURNING_TORCH) || state.is(ModBlocks.FLINT_TORCH)
            || state.is(ModBlocks.NEST) || state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)
            || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)
            || state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH);
    }
    public static boolean potential(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state) {
        return isHandRecoverableDecoration(state);
    }
}
