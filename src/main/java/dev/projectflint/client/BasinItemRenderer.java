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
        final ItemStackRenderState[] items = {new ItemStackRenderState(), new ItemStackRenderState(),
            new ItemStackRenderState(), new ItemStackRenderState()};
        int count;
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BasinBlockEntity basin, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(basin, state, partialTick, camera, overlay);
        java.util.List<net.minecraft.world.item.ItemStack> items = basin.visibleContents();
        state.count = items.size();
        for (int i = 0; i < state.items.length; i++) {
            net.minecraft.world.item.ItemStack item = i < items.size() ? items.get(i) : net.minecraft.world.item.ItemStack.EMPTY;
            itemModelResolver.updateForTopItem(state.items[i], item,
                ItemDisplayContext.FIXED, basin.getLevel(), null, basin.getBlockPos().hashCode() + i);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.count == 0) return;
        for (int i = 0; i < state.count; i++) {
            if (state.items[i].isEmpty()) continue;
            poseStack.pushPose();
            // Four separate objects fit into the Basin at once. No bobbing or overlap.
            float x = (i % 2 == 0) ? 0.32F : 0.68F;
            float z = (i / 2 == 0) ? 0.32F : 0.68F;
            poseStack.translate(x, 0.18F, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 37.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.29F, 0.29F, 0.29F);
            state.items[i].submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
