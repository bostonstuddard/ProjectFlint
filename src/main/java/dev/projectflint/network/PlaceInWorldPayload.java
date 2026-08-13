package dev.projectflint.network;

import dev.projectflint.ProjectFlint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlaceInWorldPayload() implements CustomPacketPayload {
	public static final PlaceInWorldPayload INSTANCE = new PlaceInWorldPayload();
	public static final Type<PlaceInWorldPayload> TYPE = new Type<>(ProjectFlint.id("place_in_world"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PlaceInWorldPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
