package dev.projectflint.network;

import dev.projectflint.ProjectFlint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PrimitiveProgressPayload(int kind, int current, int total, int ttlTicks) implements CustomPacketPayload {
	public static final int KIND_STONE_WORKING = 1;
	public static final int KIND_CAMPFIRE_STONE = 2;
	public static final int KIND_REED_CUTTING = 3;
	public static final int KIND_BURNT_TWIG_SPLITTING = 4;
	public static final int KIND_FLINT_KNAPPING = 5;
	public static final int KIND_REED_ROLL = 6;
	public static final int KIND_FLOWER_KNAPPING = 7;
	public static final int KIND_FLOWER_SANDING = 8;
	public static final int KIND_BONE_SANDING = 9;
	public static final int KIND_BONE_FIRESTARTER = 10;
	public static final int KIND_WOOL_SANDING = 11;
	public static final int KIND_INK_SANDING = 12;
	public static final int KIND_WOVEN_FAN_SANDING = 13;
	public static final int KIND_SAND_BALL = 14;
	public static final int KIND_WATTLE_WEAVING = 15;
	public static final int KIND_WATTLE_TYING = 16;
	public static final int KIND_WATTLE_DAUB_ASSEMBLY = 17;

	public static final Type<PrimitiveProgressPayload> TYPE = new Type<>(ProjectFlint.id("primitive_progress"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PrimitiveProgressPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, PrimitiveProgressPayload::kind,
		ByteBufCodecs.VAR_INT, PrimitiveProgressPayload::current,
		ByteBufCodecs.VAR_INT, PrimitiveProgressPayload::total,
		ByteBufCodecs.VAR_INT, PrimitiveProgressPayload::ttlTicks,
		PrimitiveProgressPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
