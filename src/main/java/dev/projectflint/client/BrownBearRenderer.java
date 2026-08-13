package dev.projectflint.client;

import dev.projectflint.ProjectFlint;
import dev.projectflint.entity.BrownBear;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;
import net.minecraft.resources.Identifier;

public final class BrownBearRenderer extends AgeableMobRenderer<BrownBear, PolarBearRenderState, BrownBearModel> {
	private static final Identifier TEXTURE = ProjectFlint.id("textures/entity/brown_bear.png");

	public BrownBearRenderer(EntityRendererProvider.Context context) {
		super(context, new BrownBearModel(context.bakeLayer(BrownBearModel.BODY_LAYER)),
			new BrownBearModel(context.bakeLayer(BrownBearModel.BODY_LAYER_BABY)), 0.9F);
	}

	@Override public Identifier getTextureLocation(PolarBearRenderState state) { return TEXTURE; }
	@Override public PolarBearRenderState createRenderState() { return new PolarBearRenderState(); }
	@Override public void extractRenderState(BrownBear bear, PolarBearRenderState state, float partialTick) {
		super.extractRenderState(bear, state, partialTick);
		state.standScale = bear.getStandingAnimationScale(partialTick);
	}
}
