package dev.projectflint.mixin;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiConsumer;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Shadow @Final private Entity source;

	/**
	 * Creepers still damage entities and destroy terrain, but their block-destruction
	 * callback receives a no-op loot collector. This avoids the old redirect into
	 * ServerExplosion's private loot-aggregation helper, which proved unstable during
	 * live creeper explosions in alpha.14.
	 */
	@Redirect(
		method = "interactWithBlocks",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"
		)
	)
	private void projectflint$preventCreeperBlockDrops(
		BlockState state,
		ServerLevel level,
		BlockPos pos,
		Explosion explosion,
		BiConsumer<ItemStack, BlockPos> dropConsumer
	) {
		if (this.source instanceof Creeper) {
			if (state.is(ModBlocks.BAKED_CLAY_KILN) || state.is(ModBlocks.CLAY_GRINDING_BASE) || state.is(ModBlocks.CLAY_GRINDER)) {
				state.onExplosionHit(level, pos, explosion, dropConsumer);
			} else {
				state.onExplosionHit(level, pos, explosion, (stack, dropPos) -> {
					// Intentionally discard other creeper-generated block loot.
				});
			}
			return;
		}

		state.onExplosionHit(level, pos, explosion, dropConsumer);
	}
}
