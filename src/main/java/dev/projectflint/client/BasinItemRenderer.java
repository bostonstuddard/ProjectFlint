package dev.projectflint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.projectflint.block.entity.BasinBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** Render the real stored item on the basin floor (not a fixed wood-only blockstate). */
public final class BasinItemRenderer implements BlockEntityRenderer<BasinBlockEntity, BasinItemRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public BasinItemRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BasinBlockEntity basin, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(basin, state, partialTick, camera, overlay);
        itemModelResolver.updateForTopItem(state.item, basin.stored().copyWithCount(1),
            ItemDisplayContext.FIXED, basin.getLevel(), null, basin.getBlockPos().hashCode());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.27F, 0.5F);
        // A flat, tangible object at the bottom of the basin. Never float/rotate like a dropped item.
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.48F, 0.48F, 0.48F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
