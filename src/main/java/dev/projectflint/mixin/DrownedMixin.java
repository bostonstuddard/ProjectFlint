package dev.projectflint.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removes tridents at the exact point vanilla assigns Drowned spawn equipment. */
@Mixin(Drowned.class)
public abstract class DrownedMixin {
	@Inject(method = "populateDefaultEquipmentSlots", at = @At("RETURN"))
	private void projectflint$removeSpawnTrident(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
		Drowned drowned = (Drowned) (Object) this;
		if (drowned.getMainHandItem().is(Items.TRIDENT)) {
			drowned.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
		if (drowned.getOffhandItem().is(Items.TRIDENT)) {
			drowned.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}
	}
}
