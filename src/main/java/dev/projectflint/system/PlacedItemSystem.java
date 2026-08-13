package dev.projectflint.system;

import com.mojang.math.Transformation;
import dev.projectflint.network.PlaceInWorldPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Server-owned flat item displays placed with the configurable Place in World key. */
public final class PlacedItemSystem {
	private static final String HOLDER_TAG = "projectflint.placed_item";
	private static final String VISUAL_TAG = "projectflint.placed_item_visual";

	private PlacedItemSystem() {}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!isPlacedItem(entity) || player.isSpectator()) return InteractionResult.PASS;
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) reclaim(serverPlayer, entity);
			return InteractionResult.SUCCESS;
		});
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> isPlacedItem(entity) ? InteractionResult.FAIL : InteractionResult.PASS);
		ServerTickEvents.END_WORLD_TICK.register(PlacedItemSystem::checkSupports);
	}

	public static void placeSelected(ServerPlayer player, PlaceInWorldPayload payload) {
		if (player.isSpectator()) return;
		ItemStack selected = player.getMainHandItem();
		if (selected.isEmpty()) return;

		if (!(player.pick(player.blockInteractionRange(), 0.0F, false) instanceof BlockHitResult hit)) return;
		if (hit.getDirection() != Direction.UP) return;

		ServerLevel level = player.level();
		BlockPos supportPos = hit.getBlockPos();
		BlockPos placePos = supportPos.above();
		if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) return;
		if (!level.getBlockState(placePos).isAir() || !level.getFluidState(placePos).isEmpty()) return;
		if (!level.getEntitiesOfClass(Interaction.class, new AABB(placePos).inflate(0.05D), PlacedItemSystem::isPlacedItem).isEmpty()) return;

		Interaction holder = new Interaction(EntityType.INTERACTION, level);
		holder.setPos(placePos.getX() + 0.5D, placePos.getY() + 0.005D, placePos.getZ() + 0.5D);
		holder.setWidth(0.82F);
		holder.setHeight(0.16F);
		holder.setResponse(false);
		holder.setNoGravity(true);
		holder.addTag(HOLDER_TAG);

		Display.ItemDisplay visual = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
		visual.setPos(holder.position());
		visual.setYRot(player.getYRot());
		visual.setItemStack(selected.copyWithCount(1));
		visual.setItemTransform(ItemDisplayContext.GROUND);
		visual.setTransformation(new Transformation(
			new Vector3f(0.0F, -0.15F, 0.0F),
			new Quaternionf().rotationX((float) (Math.PI / 2.0D)),
			new Vector3f(0.65F),
			new Quaternionf()
		));
		visual.setShadowRadius(0.15F);
		visual.setShadowStrength(0.45F);
		visual.addTag(VISUAL_TAG);

		if (!level.addFreshEntity(holder)) return;
		if (!level.addFreshEntity(visual)) {
			holder.discard();
			return;
		}
		visual.startRiding(holder);
		if (!player.isCreative()) selected.shrink(1);
		level.playSound(null, placePos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.55F, 1.08F);
		ModAdvancements.award(player, "placed_item", "placed_item_in_world");
	}

	private static void reclaim(ServerPlayer player, Entity holder) {
		Display.ItemDisplay visual = visual(holder);
		if (visual == null || visual.getItemStack().isEmpty()) {
			discardPlacedItem(holder);
			return;
		}
		PrimitiveInteractionSystem.giveOrDrop(player, visual.getItemStack().copy());
		player.level().playSound(null, holder.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 0.55F, 1.08F);
		discardPlacedItem(holder);
	}

	private static void checkSupports(ServerLevel level) {
		if (level.getGameTime() % 10L != 0L) return;
		List<Entity> unsupported = new ArrayList<>();
		for (Entity entity : level.getAllEntities()) {
			if (!isPlacedItem(entity)) continue;
			BlockPos pos = entity.blockPosition();
			BlockPos below = pos.below();
			if (visual(entity) == null || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
				unsupported.add(entity);
			}
		}
		unsupported.forEach(entity -> dropAndDiscard(level, entity));
	}

	private static void dropAndDiscard(ServerLevel level, Entity holder) {
		Display.ItemDisplay visual = visual(holder);
		if (visual != null && !visual.getItemStack().isEmpty()) {
			ItemEntity drop = new ItemEntity(level, holder.getX(), holder.getY() + 0.12D, holder.getZ(), visual.getItemStack().copy());
			drop.setDefaultPickUpDelay();
			level.addFreshEntity(drop);
		}
		discardPlacedItem(holder);
	}

	private static Display.ItemDisplay visual(Entity holder) {
		for (Entity passenger : holder.getPassengers()) {
			if (passenger instanceof Display.ItemDisplay itemDisplay && passenger.getTags().contains(VISUAL_TAG)) return itemDisplay;
		}
		return null;
	}

	private static void discardPlacedItem(Entity holder) {
		holder.getPassengers().forEach(Entity::discard);
		holder.discard();
	}

	private static boolean isPlacedItem(Entity entity) {
		return entity instanceof Interaction && entity.getTags().contains(HOLDER_TAG);
	}
}
