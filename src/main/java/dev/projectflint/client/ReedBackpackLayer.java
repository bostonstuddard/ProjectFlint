package dev.projectflint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.projectflint.ProjectFlint;
import dev.projectflint.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders a dedicated 3D model for the Reed Backpack while it is equipped. */
public final class ReedBackpackLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private final ItemStackRenderState backpackState = new ItemStackRenderState();

	public ReedBackpackLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AvatarRenderState state, float yRot, float xRot) {
		boolean reed = state.chestEquipment.is(ModItems.REED_BACKPACK);
		boolean leather = state.chestEquipment.is(ModItems.LEATHER_BACKPACK) || state.chestEquipment.is(ModItems.LEATHER_BACKPACK_WITH_CHEST);
		if (!reed && !leather) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		ItemStack wornModel = state.chestEquipment.copy();
		// Both backpacks use the exact same proven custom-render path. The override is
		// applied only to this copied render stack, so held/inventory item models remain untouched.
		wornModel.set(
			DataComponents.ITEM_MODEL,
			ProjectFlint.id(leather ? "leather_backpack_worn" : "reed_backpack_worn")
		);
		client.getItemModelResolver().updateForLiving(backpackState, wornModel, ItemDisplayContext.FIXED, client.player);

		poseStack.pushPose();
		getParentModel().body.translateAndRotate(poseStack);
		// Shared worn transform for both Reed and Leather Backpacks.
		// 0.40 Y is ~3.4 model pixels lower than the previous 0.19 placement.
		poseStack.translate(0.0F, 0.40F, 0.28F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.scale(0.85F, 0.85F, 0.85F);
		backpackState.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
