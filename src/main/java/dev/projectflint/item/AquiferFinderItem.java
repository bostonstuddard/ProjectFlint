package dev.projectflint.item;

import dev.projectflint.system.AquiferSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/** Survival dowsing rod for locating the virtual aquifer field. */
public final class AquiferFinderItem extends Item {
	private static final int SEARCH_RADIUS = 96;
	private static final int MAX_HIGHLIGHTS = 8;

	public AquiferFinderItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
		if (!player.isCreative()) player.getItemInHand(hand).hurtAndBreak(1, player, hand);

		List<AquiferSystem.AquiferHit> hits = AquiferSystem.findNearby(serverLevel, player.blockPosition(), SEARCH_RADIUS, MAX_HIGHLIGHTS);
		if (hits.isEmpty()) {
			player.displayClientMessage(Component.literal("No virtual aquifers found within 96 blocks."), true);
			return InteractionResult.SUCCESS;
		}

		for (AquiferSystem.AquiferHit hit : hits) {
			for (BlockPos footprint : AquiferSystem.footprint(hit)) {
				int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, footprint.getX(), footprint.getZ());
				serverLevel.sendParticles(
					ParticleTypes.END_ROD,
					footprint.getX() + 0.5D,
					surfaceY + 1.05D,
					footprint.getZ() + 0.5D,
					3,
					0.08D,
					0.10D,
					0.08D,
					0.0D
				);
			}
		}

		AquiferSystem.AquiferHit nearest = hits.getFirst();
		double dx = nearest.center().getX() - player.getX();
		double dz = nearest.center().getZ() - player.getZ();
		long distance = Math.round(Math.sqrt(dx * dx + dz * dz));
		player.displayClientMessage(Component.literal(
			"Aquifers: " + hits.size() + " found • nearest " + distance + " blocks • " + nearest.yield().label() + " yield"
		), true);
		return InteractionResult.SUCCESS;
	}
}
