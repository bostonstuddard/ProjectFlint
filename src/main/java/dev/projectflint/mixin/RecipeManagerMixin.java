package dev.projectflint.mixin;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Retire vanilla crafting only, before recipe-book synchronization; keep smelting/other station recipes. */
@Mixin(net.minecraft.world.item.crafting.RecipeManager.class)
public abstract class RecipeManagerMixin {
	@Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;", at = @At("RETURN"), cancellable = true)
	private void projectflint$disableVanillaCrafting(ResourceManager resources, ProfilerFiller profiler, CallbackInfoReturnable<RecipeMap> cir) {
		RecipeMap recipes = cir.getReturnValue();
		cir.setReturnValue(RecipeMap.create(recipes.values().stream()
            .filter(holder -> !(holder.value() instanceof CraftingRecipe && holder.id().identifier().getNamespace().equals("minecraft")))
            // The clay bucket / unfired bricks belong to kilns/forges, not a vanilla furnace.
            // Preserve Project Flint personal Wooden Tool and hinged-door recipes.
            // Their 2x2 unlocks must never depend on reaching the Hardwood Table.
            .filter(holder -> {
                var id = holder.id().identifier();
                if (!id.getNamespace().equals("projectflint")) return true;
                String path = id.getPath();
                if (path.startsWith("unfired_") && (path.endsWith("_smelting") || path.endsWith("_blasting")
                    || path.endsWith("_smoking") || path.endsWith("_campfire_cooking"))) return false;
                return true;
            })
			.toList()));
	}
}
