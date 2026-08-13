package dev.projectflint.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import dev.projectflint.system.PlacedItemSystem;

public final class ModNetworking {
	private ModNetworking() {
	}

	public static void initialize() {
		PayloadTypeRegistry.playS2C().register(PrimitiveProgressPayload.TYPE, PrimitiveProgressPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(PlaceInWorldPayload.TYPE, PlaceInWorldPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PlaceInWorldPayload.TYPE, (payload, context) -> PlacedItemSystem.placeSelected(context.player(), payload));
	}
}
