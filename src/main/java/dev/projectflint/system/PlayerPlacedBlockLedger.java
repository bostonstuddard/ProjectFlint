package dev.projectflint.system;

import com.mojang.serialization.Codec;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EquipmentSlot;
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
		if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TANNED_UTILITY_VEST) || !potential(level, pos, state)) return false;
		PlayerPlacedBlockLedger ledger = of(level);
		String registered = ledger.placed.get(pos.asLong());
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(registered);
	}

	/** Structural machines and inventories are never bypassed by the Utility Vest. */
	public static boolean potential(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state) {
		float hardness = state.getDestroySpeed(level, pos);
		return hardness >= 0.0F && hardness <= 4.0F && !state.hasBlockEntity()
			&& !ModBlocks.isWoodenBeam(state) // Always use the Beam + Wooden Screw salvage path.
			&& !state.is(ModBlocks.FIRECLAY_BRICK_BLOCK) && !state.is(ModBlocks.FIRECLAY_BRICK_SLAB)
			&& !state.is(ModBlocks.FIRECLAY_BRICK_STAIRS) && !state.is(Blocks.BRICKS)
			&& !state.is(ModBlocks.BRICK_SHINGLES) && !state.is(ModBlocks.CRUSHED_STONE);
	}
}
