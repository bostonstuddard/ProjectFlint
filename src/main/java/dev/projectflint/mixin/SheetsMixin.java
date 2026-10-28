package dev.projectflint.mixin;
import dev.projectflint.client.WoodenChestMaterials;
import dev.projectflint.client.WoodenChestRenderContext;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Sheets.class) public abstract class SheetsMixin { @Inject(method="chooseMaterial",at=@At("HEAD"),cancellable=true) private static void projectflint$woodenChest(ChestRenderState.ChestMaterialType material, ChestType type, CallbackInfoReturnable<Material> cir){ if(WoodenChestRenderContext.active()) cir.setReturnValue(WoodenChestMaterials.choose(type)); } }
