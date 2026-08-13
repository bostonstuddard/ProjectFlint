package dev.projectflint.client;

import dev.projectflint.ProjectFlint;
import dev.projectflint.entity.BlackBear;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;
import net.minecraft.resources.Identifier;

public final class BlackBearRenderer extends AgeableMobRenderer<BlackBear, PolarBearRenderState, BlackBearModel> {
	private static final Identifier TEXTURE = ProjectFlint.id("textures/entity/black_bear.png");

	public BlackBearRenderer(EntityRendererProvider.Context context) {
		super(context, new BlackBearModel(context.bakeLayer(BlackBearModel.BODY_LAYER)),
			new BlackBearModel(context.bakeLayer(BlackBearModel.BODY_LAYER_BABY)), 0.9F);
	}

	@Override public Identifier getTextureLocation(PolarBearRenderState state) { return TEXTURE; }
	@Override public PolarBearRenderState createRenderState() { return new PolarBearRenderState(); }
	@Override public void extractRenderState(BlackBear bear, PolarBearRenderState state, float partialTick) {
		super.extractRenderState(bear, state, partialTick);
		state.standScale = bear.getStandingAnimationScale(partialTick);
	}
}
