package dev.projectflint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.projectflint.block.entity.WoodenChestBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

public final class WoodenChestRenderer extends ChestRenderer<WoodenChestBlockEntity> {
	public WoodenChestRenderer(BlockEntityRendererProvider.Context context){super(context);}
	@Override public void submit(ChestRenderState state, PoseStack poses, SubmitNodeCollector nodes, CameraRenderState camera){ WoodenChestRenderContext.push(); try{super.submit(state,poses,nodes,camera);}finally{WoodenChestRenderContext.pop();} }
}
