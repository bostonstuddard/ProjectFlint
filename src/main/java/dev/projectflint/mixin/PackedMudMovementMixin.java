package dev.projectflint.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Entity.getBlockSpeedFactor exists in 1.21.11; BlockStateBase.getSpeedFactor does not. */
@Mixin(Entity.class)
public abstract class PackedMudMovementMixin {
    @Shadow protected abstract BlockPos getBlockPosBelowThatAffectsMyMovement();
    @Shadow public abstract Level level();

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    private void projectflint$packedMudFooting(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player
            && level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).is(Blocks.PACKED_MUD)) {
            cir.setReturnValue(1.10F);
        }
    }
}
