package dev.projectflint.client;

import dev.projectflint.ProjectFlint;
import dev.projectflint.network.PrimitiveProgressPayload;
import dev.projectflint.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PrimitiveHud {
	private static final ItemStack ROCK_ICON = new ItemStack(ModItems.ROCK);
	private static final ItemStack VERY_REFINED_STONE_ICON = new ItemStack(ModItems.VERY_REFINED_STONE);
	private static final ItemStack CAMPFIRE_STONE_ICON = new ItemStack(ModItems.CAMPFIRE_STONE);
	private static final ItemStack SUGAR_CANE_ICON = new ItemStack(Items.SUGAR_CANE);
	private static final ItemStack BURNT_TWIG_ICON = new ItemStack(ModItems.BURNT_TWIG);
	private static final ItemStack FLINT_CHIP_ICON = new ItemStack(ModItems.FLINT_CHIP);
	private static final ItemStack CRUDE_REED_ICON = new ItemStack(ModItems.CRUDE_REED);
	private static final ItemStack SANDING_PAPER_ICON = new ItemStack(ModItems.SANDING_PAPER);
	private static final ItemStack FILED_BONE_ICON = new ItemStack(ModItems.FILED_BONE);
	private static final ItemStack FLINT_ICON = new ItemStack(Items.FLINT);
	private static final ItemStack WOOL_ICON = new ItemStack(ModItems.WOOL);
	private static final ItemStack INK_SAC_ICON = new ItemStack(Items.INK_SAC);
	private static final ItemStack PLANT_BINDING_ICON = new ItemStack(ModItems.PLANT_TOOL_BINDING);
	private static final ItemStack SAND_FLAKE_ICON = new ItemStack(ModItems.SAND_FLAKE);
	private static final ItemStack WATTLE_ICON = new ItemStack(ModItems.WATTLE);
	private static final ItemStack TIED_WATTLE_ICON = new ItemStack(ModItems.TIED_WATTLE);
	private static final ItemStack WET_DAUB_ICON = new ItemStack(ModItems.WET_DAUB_PILE);

	private static int workKind;
	private static int workCurrent;
	private static int workTotal;
	private static int workTicksRemaining;

	private PrimitiveHud() {}

	public static void initialize() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ProjectFlint.id("primitive_context"), PrimitiveHud::render);
	}

	public static void acceptProgress(PrimitiveProgressPayload payload) {
		workKind = payload.kind();
		workCurrent = payload.current();
		workTotal = Math.max(1, payload.total());
		workTicksRemaining = payload.ttlTicks();
	}

	public static void tick() {
		if (workTicksRemaining > 0) workTicksRemaining--;
	}

	private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;

		int centerX = graphics.guiWidth() / 2;
		int centerY = graphics.guiHeight() / 2;
		if (!client.player.isCreative() && !client.player.isSpectator()) {
			drawPrimitiveHotbarLocks(graphics, centerX, graphics.guiHeight());
			renderConditionStatus(graphics, client, centerX, graphics.guiHeight());
		}

		if (workTicksRemaining > 0) {
			renderHandworkingMeter(graphics, client, centerX, centerY);
		}
	}

	private static boolean renderHandworkingMeter(GuiGraphics graphics, Minecraft client, int centerX, int centerY) {
		ItemStack main = client.player.getMainHandItem();
		ItemStack off = client.player.getOffhandItem();
		ItemStack left;
		ItemStack right;
		switch (workKind) {
			case PrimitiveProgressPayload.KIND_STONE_WORKING -> {
				if (!main.is(ModItems.ROCK) || !off.is(ModItems.ROCK)) return false;
				left = ROCK_ICON;
				right = ROCK_ICON;
			}
			case PrimitiveProgressPayload.KIND_CAMPFIRE_STONE -> {
				if (!main.is(ModItems.VERY_REFINED_STONE) || !off.is(ModItems.VERY_REFINED_STONE)) return false;
				left = VERY_REFINED_STONE_ICON;
				right = CAMPFIRE_STONE_ICON;
			}
			case PrimitiveProgressPayload.KIND_REED_CUTTING -> {
				if (!main.is(Items.SUGAR_CANE) || !ModItems.isCutterLike(off)) return false;
				left = SUGAR_CANE_ICON;
				right = off.copy();
			}
			case PrimitiveProgressPayload.KIND_BURNT_TWIG_SPLITTING -> {
				if (!main.is(ModItems.BURNT_TWIG) || !ModItems.isHammerLike(off)) return false;
				left = BURNT_TWIG_ICON;
				right = off.copy();
			}
			case PrimitiveProgressPayload.KIND_FLINT_KNAPPING -> {
				if (!main.is(ModItems.FLINT_CHIP) || !ModItems.isHammerLike(off)) return false;
				left = FLINT_CHIP_ICON;
				right = off.copy();
			}
			case PrimitiveProgressPayload.KIND_REED_ROLL -> {
				if (!main.is(ModItems.CRUDE_REED) || main.getCount() < 4 || !ModItems.isMultitool(off)) return false;
				left = CRUDE_REED_ICON;
				right = off.copy();
			}
			case PrimitiveProgressPayload.KIND_FLOWER_KNAPPING -> {
				if (!main.is(net.minecraft.tags.ItemTags.FLOWERS) || !ModItems.isMultitool(off)) return false;
				left = main.copyWithCount(1);
				right = off.copy();
			}
			case PrimitiveProgressPayload.KIND_FLOWER_SANDING -> {
				if (!main.is(net.minecraft.tags.ItemTags.FLOWERS) || !off.is(ModItems.SANDING_PAPER)) return false;
				left = main.copyWithCount(1);
				right = SANDING_PAPER_ICON;
			}
			case PrimitiveProgressPayload.KIND_BONE_SANDING -> {
				if ((!main.is(ModItems.ANIMAL_BONES) && !main.is(Items.BONE)) || !off.is(ModItems.SANDING_PAPER)) return false;
				left = main.copyWithCount(1);
				right = SANDING_PAPER_ICON;
			}
			case PrimitiveProgressPayload.KIND_BONE_FIRESTARTER -> {
				if (!main.is(ModItems.FILED_BONE) || !off.is(Items.FLINT)) return false;
				left = FILED_BONE_ICON;
				right = FLINT_ICON;
			}
			case PrimitiveProgressPayload.KIND_WOOL_SANDING -> {
				if (!main.is(ModItems.WOOL) || main.getCount() < 2 || !off.is(ModItems.SANDING_PAPER)) return false;
				left = WOOL_ICON;
				right = SANDING_PAPER_ICON;
			}
			case PrimitiveProgressPayload.KIND_INK_SANDING -> {
				if (!main.is(Items.INK_SAC) || !off.is(ModItems.SANDING_PAPER)) return false;
				left = INK_SAC_ICON;
				right = SANDING_PAPER_ICON;
			}
			case PrimitiveProgressPayload.KIND_WOVEN_FAN_SANDING -> {
				if (!main.is(ModItems.PLANT_TOOL_BINDING) || !off.is(ModItems.SANDING_PAPER)) return false;
				left = PLANT_BINDING_ICON;
				right = SANDING_PAPER_ICON;
			}
			case PrimitiveProgressPayload.KIND_SAND_BALL -> {
				if (!main.is(ModItems.SAND_FLAKE) || main.getCount() < 4 || !ModItems.isMultitool(off)) return false;
				left = SAND_FLAKE_ICON;
				right = off.copy();
			}
			case PrimitiveProgressPayload.KIND_WATTLE_WEAVING -> {
				if (!main.is(ModItems.CRUDE_REED) || main.getCount() < 12 || !off.is(ModItems.TWIG) || off.getCount() < 12) return false;
				left = CRUDE_REED_ICON;
				right = new ItemStack(ModItems.TWIG);
			}
			case PrimitiveProgressPayload.KIND_WATTLE_TYING -> {
				if (!main.is(ModItems.WATTLE) || !off.is(ModItems.CRUDE_PLANT_STRING)) return false;
				left = WATTLE_ICON;
				right = new ItemStack(ModItems.CRUDE_PLANT_STRING);
			}
			case PrimitiveProgressPayload.KIND_WATTLE_DAUB_ASSEMBLY -> {
				if (!main.is(ModItems.TIED_WATTLE) || main.getCount() < 4 || !off.is(ModItems.WET_DAUB_PILE) || off.getCount() < 2) return false;
				left = TIED_WATTLE_ICON;
				right = WET_DAUB_ICON;
			}
			default -> {
				return false;
			}
		}
		drawMeter(graphics, centerX, centerY + 22, workCurrent, workTotal, 0xFFB9B3A7);
		drawItemRequirement(graphics, left, centerX - 18, centerY + 30);
		drawItemRequirement(graphics, right, centerX + 2, centerY + 30);
		return true;
	}

	private static void renderConditionStatus(GuiGraphics graphics, Minecraft client, int centerX, int guiHeight) {
		boolean dying = client.player.getHealth() < dev.projectflint.config.GameplayTuning.DYING_HEALTH_THRESHOLD;
		boolean weakened = !dying && client.player.getHealth() < dev.projectflint.config.GameplayTuning.WEAKENED_HEALTH_THRESHOLD;
		boolean hungry = client.player.getFoodData().getFoodLevel() < dev.projectflint.config.GameplayTuning.HUNGRY_FOOD_THRESHOLD;
		if (!dying && !weakened && !hungry) return;

		Component health = dying
			? Component.translatable("hud.projectflint.dying")
			: weakened ? Component.translatable("hud.projectflint.weakened") : Component.empty();
		Component text = hungry && (dying || weakened)
			? Component.translatable("hud.projectflint.condition_join", health, Component.translatable("hud.projectflint.hungry"))
			: hungry ? Component.translatable("hud.projectflint.hungry") : health;
		int color = dying ? 0xFF6B6B : hungry ? 0xE6C66A : 0xE8A76E;
		graphics.drawCenteredString(client.font, text, centerX + 50, guiHeight - 52, color);
	}

	private static void drawPrimitiveHotbarLocks(GuiGraphics graphics, int centerX, int guiHeight) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || dev.projectflint.system.PrimitiveInventorySystem.allowedHotbarSlots(client.player) >= 9) return;
		int hotbarLeft = centerX - 91;
		int slotInteriorTop = guiHeight - 19;
		for (int slot = 5; slot < 9; slot++) {
			int x = hotbarLeft + 3 + slot * 20;
			graphics.fill(x, slotInteriorTop, x + 16, slotInteriorTop + 16, 0xB8232323);
			graphics.fill(x + 3, slotInteriorTop + 7, x + 13, slotInteriorTop + 9, 0x986A6A6A);
		}
	}

	private static void drawItemRequirement(GuiGraphics graphics, ItemStack stack, int x, int y) {
		graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x9A111111);
		graphics.renderItem(stack, x, y);
	}

	private static void drawMeter(GuiGraphics graphics, int centerX, int y, int current, int total, int fillColor) {
		int width = 72;
		int height = 4;
		int left = centerX - width / 2;
		int clampedCurrent = Math.max(0, Math.min(current, total));
		int filled = Math.round(width * (clampedCurrent / (float) Math.max(1, total)));
		graphics.fill(left - 1, y - 1, left + width + 1, y + height + 1, 0xA0000000);
		graphics.fill(left, y, left + width, y + height, 0xAA3A3A3A);
		if (filled > 0) graphics.fill(left, y, left + filled, y + height, fillColor);
	}
}
