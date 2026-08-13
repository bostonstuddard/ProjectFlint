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
 * Project Flint adaptation of Bear Minimum's Apache-2.0 BlackBearModel.
 * Namespace/mappings were changed; geometry and UV layout intentionally follow the source model.
 */
public final class BlackBearModel extends QuadrupedModel<PolarBearRenderState> {
	public static final ModelLayerLocation BODY_LAYER = new ModelLayerLocation(ProjectFlint.id("black_bear"), "main");
	public static final ModelLayerLocation BODY_LAYER_BABY = new ModelLayerLocation(ProjectFlint.id("black_bear_baby"), "main");
	private static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0F, 4.0F, 2.25F, 2.0F, 24.0F, Set.of("head"));

	public BlackBearModel(ModelPart root) { super(root); }

	public static LayerDefinition createBodyLayer(boolean isBaby) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(1, 0).addBox(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
			.texOffs(1, 44).addBox(-1.5F, 0.0F, -9.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
			.texOffs(26, 0).addBox(2.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(26, 0).addBox(-4.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 12.0F, -11.0F));
		root.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(4, 21).addBox(-3.5F, -12.0F, -7.0F, 11.0F, 10.0F, 9.0F, new CubeDeformation(0.0F))
			.texOffs(40, 0).addBox(-3.5F, -23.0F, -7.0F, 11.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.0F, 10.0F, 12.0F, 1.5708F, 0.0F, 0.0F));
		root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(52, 24)
			.addBox(-3.0F, 0.0F, -2.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 16.0F, 5.0F));
		root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(52, 24)
			.addBox(-1.0F, 0.0F, -2.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 16.0F, 5.0F));
		root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(51, 41)
			.addBox(-2.5F, 0.0F, -1.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 16.0F, -7.0F));
		root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(51, 41)
			.addBox(-1.5F, 0.0F, -1.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 16.0F, -7.0F));

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
		body.y += stand * ageScale * 5.0F;
		rightFrontLeg.y -= stand * ageScale * 17.0F;
		rightFrontLeg.z += stand * ageScale * 4.0F;
		rightFrontLeg.xRot -= stand * (float)Math.PI * 0.45F;
		leftFrontLeg.y = rightFrontLeg.y;
		leftFrontLeg.z = rightFrontLeg.z;
		leftFrontLeg.xRot -= stand * (float)Math.PI * 0.45F;
		head.y -= stand * headScale * 18.0F;
		head.z += stand * headScale * 9.0F;
		head.xRot += stand * (float)Math.PI * 0.15F;
	}
}
