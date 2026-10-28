package dev.projectflint.system;

import dev.projectflint.registry.ModItems;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Project Flint behavior attached only to the special 512-durability vanilla Brush. */
public final class ProjectFlintBrushSystem {
	public static final int DURABILITY = 512;
	public static final int BRUSH_TICKS = 50; // 2.5 seconds: one second faster than Bone Brush.

	private ProjectFlintBrushSystem() {}

	public static boolean isProjectFlintBrush(ItemStack stack) {
		return stack.is(Items.BRUSH) && stack.getOrDefault(DataComponents.MAX_DAMAGE, 0) == DURABILITY;
	}

	public static ItemStack createBrush() {
		ItemStack stack = new ItemStack(Items.BRUSH);
		stack.set(DataComponents.MAX_DAMAGE, DURABILITY);
		stack.set(DataComponents.DAMAGE, 0);
		return stack;
	}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!isProjectFlintBrush(stack) || !(entity instanceof Sheep sheep) || !sheep.readyForShearing()) return InteractionResult.PASS;
			if (level instanceof ServerLevel serverLevel) {
				sheep.setSheared(true);
				int woolCount = 4 + sheep.getRandom().nextInt(2); // +1 over Bone Brush's 3-4.
				sheep.spawnAtLocation(serverLevel, new ItemStack(ModItems.WOOL, woolCount));
				stack.hurtAndBreak(1, player, hand);
				level.playSound(null, sheep.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		});
	}
}
