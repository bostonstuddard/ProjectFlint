package dev.projectflint.client;

import dev.projectflint.client.screen.AdvancedKilnScreen;
import dev.projectflint.client.screen.ClayGrinderScreen;
import dev.projectflint.client.screen.MortarChestScreen;
import dev.projectflint.client.screen.ProjectFlintRecipeGuideScreen;
import dev.projectflint.client.screen.ProjectFlintFieldGuideScreen;
import dev.projectflint.client.screen.SandingStationScreen;
import dev.projectflint.client.screen.WoodenGrinderScreen;
import dev.projectflint.client.screen.WovenChestScreen;
import dev.projectflint.client.screen.WoodenChestScreen;
import dev.projectflint.client.screen.BookshelfScreen;
import dev.projectflint.client.screen.WoodenCabinetScreen;
import dev.projectflint.client.screen.PrimitiveStoveScreen;
import dev.projectflint.client.screen.BrickFurnaceScreen;
import dev.projectflint.client.screen.WellPulleyScreen;
import dev.projectflint.client.screen.NestScreen;
import dev.projectflint.client.screen.FireclayForgeScreen;
import dev.projectflint.client.screen.HardwoodCraftingScreen;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import dev.projectflint.network.PrimitiveProgressPayload;
import dev.projectflint.network.PlaceInWorldPayload;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModEntities;
import dev.projectflint.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.FoliageColor;
import org.lwjgl.glfw.GLFW;

public final class ProjectFlintClient implements ClientModInitializer {
	private static final KeyMapping PLACE_IN_WORLD = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.projectflint.place_in_world",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_V,
		KeyMapping.Category.GAMEPLAY
	));

	private static final KeyMapping OPEN_RECIPE_GUIDE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.projectflint.recipe_guide",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_K,
		KeyMapping.Category.GAMEPLAY
	));

	private static final KeyMapping OPEN_FIELD_GUIDE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.projectflint.field_guide", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyMapping.Category.GAMEPLAY
	));

	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.CLAY_GRINDER, ClayGrinderScreen::new);
		MenuScreens.register(ModMenus.MORTAR_CHEST, MortarChestScreen::new);
		MenuScreens.register(ModMenus.ADVANCED_KILN, AdvancedKilnScreen::new);
		MenuScreens.register(ModMenus.WOODEN_GRINDER, WoodenGrinderScreen::new);
		MenuScreens.register(ModMenus.SANDING_STATION, SandingStationScreen::new);
		MenuScreens.register(ModMenus.WOVEN_CHEST, WovenChestScreen::new);
		MenuScreens.register(ModMenus.WOODEN_CHEST, WoodenChestScreen::new);
		MenuScreens.register(ModMenus.WOODEN_DOUBLE_CHEST, WoodenChestScreen::new);
		MenuScreens.register(ModMenus.BOOKSHELF, BookshelfScreen::new);
		MenuScreens.register(ModMenus.WOODEN_CABINET, WoodenCabinetScreen::new);
		MenuScreens.register(ModMenus.PRIMITIVE_STOVE, PrimitiveStoveScreen::new);
		MenuScreens.register(ModMenus.BRICK_FURNACE, BrickFurnaceScreen::new);
		MenuScreens.register(ModMenus.BRICK_BLAST_FURNACE, BrickFurnaceScreen::new);
		MenuScreens.register(ModMenus.WELL_PULLEY, WellPulleyScreen::new);
		MenuScreens.register(ModMenus.NEST, NestScreen::new);
		MenuScreens.register(ModMenus.FIRECLAY_FORGE, FireclayForgeScreen::new);
		MenuScreens.register(ModMenus.HARDWOOD_CRAFTING, HardwoodCraftingScreen::new);
		BlockEntityRenderers.register(ModBlockEntities.WOODEN_CHEST, WoodenChestRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(BlackBearModel.BODY_LAYER, () -> BlackBearModel.createBodyLayer(false));
		EntityModelLayerRegistry.registerModelLayer(BlackBearModel.BODY_LAYER_BABY, () -> BlackBearModel.createBodyLayer(true));
		EntityModelLayerRegistry.registerModelLayer(BrownBearModel.BODY_LAYER, () -> BrownBearModel.createBodyLayer(false));
		EntityModelLayerRegistry.registerModelLayer(BrownBearModel.BODY_LAYER_BABY, () -> BrownBearModel.createBodyLayer(true));
		EntityRendererRegistry.register(ModEntities.BLACK_BEAR, BlackBearRenderer::new);
		EntityRendererRegistry.register(ModEntities.BROWN_BEAR, BrownBearRenderer::new);
		registerBushRenderLayers(ModBlocks.FORAGE_BUSHES);
		registerBushRenderLayers(ModBlocks.LEGACY_INACTIVE_BUSHES);
		BlockRenderLayerMap.putBlock(ModBlocks.TWIG_PILE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CRUDE_PLANT_TORCH, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.FLINT_TORCH, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.FIBER_WORK_PILE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.HAMMER_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.BINDING_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.ROCK_PILE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.VERY_REFINED_STONE_PILE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.HARVESTING_TOOL_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.MULTITOOL_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.PRIMITIVE_CAMPFIRE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.FLINT_TOOL_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WOVEN_BOWL_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CLAY_FIRING_PILE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.FIRED_CLAY_PILE, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WOVEN_GEAR_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.ADVANCED_TOOL_ASSEMBLY, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CLAY_GRINDER, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WOODEN_GRINDER, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WATTLED_DAUB_WINDOW, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WATTLED_DAUB_DOOR, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.SAWMILL, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WOODEN_CABINET, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.FLAX_CROP, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.STRAWBERRY_BUSH, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.ROPE, ChunkSectionLayer.CUTOUT);
		registerBushColors(ModBlocks.FORAGE_BUSHES);
		registerBushColors(ModBlocks.LEGACY_INACTIVE_BUSHES);


		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new ReedBackpackLayer(avatarRenderer));
			}
		});

		PrimitiveHud.initialize();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PrimitiveHud.tick();
			while (PLACE_IN_WORLD.consumeClick()) {
				if (client.player != null && ClientPlayNetworking.canSend(PlaceInWorldPayload.TYPE)) {
					ClientPlayNetworking.send(PlaceInWorldPayload.INSTANCE);
				}
			}
			while (OPEN_FIELD_GUIDE.consumeClick()) {
				if (client.player != null && client.screen == null) {
					client.setScreen(new ProjectFlintFieldGuideScreen(null));
				}
			}
			while (OPEN_RECIPE_GUIDE.consumeClick()) {
				if (client.player != null && client.screen == null) {
					ProjectFlintRecipeGuideScreen.openGlobal(null);
				}
			}
			if (client.player != null && !client.player.isCreative()) {
				int allowedSlots = dev.projectflint.system.PrimitiveInventorySystem.allowedHotbarSlots(client.player);
				if (client.player.getInventory().getSelectedSlot() >= allowedSlots) {
					client.player.getInventory().setSelectedSlot(allowedSlots - 1);
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(PrimitiveProgressPayload.TYPE, (payload, context) -> PrimitiveHud.acceptProgress(payload));
	}

	private static void registerBushRenderLayers(Iterable<ModBlocks.ForageBushDefinition> bushes) {
		for (ModBlocks.ForageBushDefinition bush : bushes) {
			BlockRenderLayerMap.putBlock(bush.block(), ChunkSectionLayer.CUTOUT);
		}
	}

	private static void registerBushColors(Iterable<ModBlocks.ForageBushDefinition> bushes) {
		for (ModBlocks.ForageBushDefinition bush : bushes) {
			ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
				if (view == null || pos == null) {
					return FoliageColor.FOLIAGE_DEFAULT;
				}
				return BiomeColors.getAverageFoliageColor(view, pos);
			}, bush.block());
		}
	}
}
