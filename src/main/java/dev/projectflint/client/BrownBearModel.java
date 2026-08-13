package dev.projectflint.client;

import dev.projectflint.ProjectFlint;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;

import java.util.Set;

/**
 * Project Flint adaptation of Bear Minimum's Apache-2.0 BrownBearModel.
 * Brown Bear intentionally has its own larger geometry and UV offsets; it must not share Black/Polar Bear layout.
 */
public final class BrownBearModel extends QuadrupedModel<PolarBearRenderState> {
	public static final ModelLayerLocation BODY_LAYER = new ModelLayerLocation(ProjectFlint.id("brown_bear"), "main");
	public static final ModelLayerLocation BODY_LAYER_BABY = new ModelLayerLocation(ProjectFlint.id("brown_bear_baby"), "main");
	private static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0F, 4.0F, 2.25F, 2.0F, 24.0F, Set.of("head"));

	public BrownBearModel(ModelPart root) { super(root); }

	public static LayerDefinition createBodyLayer(boolean isBaby) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-4.5F, -5.0F, -7.0F, 9.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
			.texOffs(0, 56).addBox(-2.5F, -2.0F, -11.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
			.texOffs(21, 61).addBox(3.5F, -6.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(21, 61).addBox(-5.5F, -6.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 11.0F, -13.0F));
		root.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 30).addBox(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F, new CubeDeformation(0.0F))
			.texOffs(37, 0).addBox(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.0F, 9.0F, 12.0F, 1.5708F, 0.0F, 0.0F));
		root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(50, 30)
			.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 14.0F, 6.0F));
		root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(50, 30)
			.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, 14.0F, 6.0F));
		root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(50, 48)
			.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 14.0F, -8.0F));
		root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(50, 48)
			.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 14.0F, -8.0F));

		return LayerDefinition.create(mesh, 128, 64)
			.apply(isBaby ? BABY_TRANSFORMER : MeshTransformer.IDENTITY)
			.apply(MeshTransformer.scaling(1.2F));
	}

	@Override
	public void setupAnim(PolarBearRenderState state) {
		super.setupAnim(state);
		float stand = state.standScale * state.standScale;
		float ageScale = state.ageScale;
		float headScale = state.isBaby ? 0.44444445F : 1.0F;
		body.xRot -= stand * (float)Math.PI * 0.35F;
		body.y += stand * ageScale * 2.0F;
		rightFrontLeg.y -= stand * ageScale * 20.0F;
		rightFrontLeg.z += stand * ageScale * 4.0F;
		rightFrontLeg.xRot -= stand * (float)Math.PI * 0.45F;
		leftFrontLeg.y = rightFrontLeg.y;
		leftFrontLeg.z = rightFrontLeg.z;
		leftFrontLeg.xRot -= stand * (float)Math.PI * 0.45F;
		head.y -= stand * headScale * 24.0F;
		head.z += stand * headScale * 11.0F;
		head.xRot += stand * (float)Math.PI * 0.15F;
	}
}
