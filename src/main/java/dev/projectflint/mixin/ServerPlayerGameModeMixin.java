package dev.projectflint.mixin;

import dev.projectflint.block.AdvancedKilnMultiblock;
import dev.projectflint.block.ClayGrindingBaseBlock;
import dev.projectflint.block.CrudeClayKilnBlock;
import dev.projectflint.block.MortarKilnBlock;
import dev.projectflint.block.entity.BakedClayKilnBlockEntity;
import dev.projectflint.block.entity.WovenChestBlockEntity;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModTags;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.ToolCapabilities;
import dev.projectflint.system.LumberProcessorSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
	@Shadow protected ServerLevel level;
	@Shadow @Final protected ServerPlayer player;

	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	private void projectflint$completePrimitiveOperation(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (player.isCreative()) return;

		BlockState state = level.getBlockState(pos);
		ItemStack held = player.getMainHandItem();

		if (state.is(ModBlocks.LUMBER_WORKPIECE)) {
			LumberProcessorSystem.harvestWorkpiece(level, pos);
			cir.setReturnValue(false);
			return;
		}

		if (LumberProcessorSystem.isSupportedStrippedLog(state) && level.getBlockState(pos.below()).is(ModBlocks.LUMBER_CHOPPING_BLOCK)) {
			if (held.is(ModItems.FLINT_PLANT_CUTTERS) && LumberProcessorSystem.startWash(level, pos.below())) {
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				ModAdvancements.awardAll(player, "lumber_washing");
			}
			cir.setReturnValue(false);
			return;
		}

		if (LumberProcessorSystem.isProcessorPart(level, pos)) {
			if (ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER)) {
				LumberProcessorSystem.recover(level, pos, player);
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			}
			cir.setReturnValue(false);
			return;
		}

		if (ModItems.canHarvestGourdFlesh(held) && (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON))) {
			level.removeBlock(pos, false);
			ItemStack flesh = state.is(Blocks.PUMPKIN)
				? new ItemStack(ModItems.PUMPKIN_SLICE)
				: new ItemStack(Items.MELON_SLICE);
			projectflint$dropAtBlock(pos, flesh);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 0.72F, 0.96F);
			cir.setReturnValue(false);
			return;
		}

		if (held.is(ModItems.FLINT_AXE) && (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON))) {
			level.removeBlock(pos, false);
			ItemStack seed = state.is(Blocks.PUMPKIN)
				? new ItemStack(Items.PUMPKIN_SEEDS)
				: new ItemStack(Items.MELON_SEEDS);
			projectflint$dropAtBlock(pos, seed);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.72F, 0.92F);
			cir.setReturnValue(false);
			return;
		}

		if (held.is(ModItems.FLINT_AXE)) {
			ItemStack stripped = projectflint$strippedLog(state);
			if (!stripped.isEmpty()) {
				level.removeBlock(pos, false);
				projectflint$dropAtBlock(pos, stripped);
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				ModAdvancements.awardAll(player, "flint_axe_log");
				level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.75F, 0.8F);
				cir.setReturnValue(false);
				return;
			}
		}

		if (held.is(ModItems.FLINT_AXE) && (projectflint$isStrippedLog(state) || projectflint$isVanillaPlankSlab(state) || projectflint$isProcessedWood(state))) {
			int count = state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			ItemStack recovered = new ItemStack(state.getBlock().asItem(), count);
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, recovered);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.75F, 0.86F);
			cir.setReturnValue(false);
			return;
		}

		if (held.is(ModItems.FLINT_PLANT_CUTTERS) && state.is(Blocks.SUGAR_CANE)) {
			if (projectflint$removeSugarCaneColumn(pos) > 0) {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.REED_ROLL));
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				ModAdvancements.awardAll(player, "flint_plant_cutters");
			}
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.72F, 1.1F);
			cir.setReturnValue(false);
			return;
		}

		// Dedicated Plant Cutter upgrade: Short Grass yields a ready-made Plant Tool Binding
		// instead of falling back to the Advanced Multitool's Crude Plant String output.
		if (held.is(ModItems.FLINT_PLANT_CUTTERS) && state.is(Blocks.SHORT_GRASS)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.PLANT_TOOL_BINDING));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.awardAll(player, "flint_plant_cutters");
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.72F, 1.1F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(Blocks.SWEET_BERRY_BUSH)) {
			if (ModItems.canHarvestSweetBerrySeeds(held)) {
				level.removeBlock(pos, false);
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.SWEET_BERRY_SEEDS));
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.BLOCKS, 0.75F, 0.92F);
			}
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.SLEEPING_BAG)) {
			ModBlocks.SLEEPING_BAG.removePairWithSalvage(level, pos, state, held);
			level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.WOVEN_CHEST) && held.is(ModItems.FLINT_AXE)) {
			if (level.getBlockEntity(pos) instanceof WovenChestBlockEntity chest) Containers.dropContents(level, pos, chest);
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.WOVEN_CHEST));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.78F, 0.82F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.MORTAR_KILN) && ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER)) {
			int branches = state.getValue(MortarKilnBlock.BRANCHES);
			ItemStack stored = level.getBlockEntity(pos) instanceof BakedClayKilnBlockEntity kiln ? kiln.takeStoredInput() : ItemStack.EMPTY;
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.MORTAR_KILN));
			if (branches > 0) projectflint$dropAtBlock(pos, new ItemStack(ModItems.BRANCH, branches));
			if (!stored.isEmpty()) projectflint$dropAtBlock(pos, stored);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.78F, 0.76F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.WOODEN_GRINDER) && ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.WOODEN_GRINDER));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.78F, 0.8F);
			cir.setReturnValue(false);
			return;
		}

		if ((state.is(ModBlocks.MORTAR_KILN_BASE) || state.is(ModBlocks.WOODEN_GRINDER_BASE))
			&& ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER)) {
			ItemStack recovered = new ItemStack(state.is(ModBlocks.MORTAR_KILN_BASE) ? ModItems.MORTAR_KILN_BASE : ModItems.WOODEN_GRINDER_BASE);
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, recovered);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, state.is(ModBlocks.MORTAR_KILN_BASE) ? SoundEvents.DECORATED_POT_BREAK : SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.74F, 0.82F);
			cir.setReturnValue(false);
			return;
		}

		if ((state.is(ModBlocks.BURNT_BRANCH_BLOCK) || state.is(ModBlocks.BURNT_BRANCH_SLAB))
			&& (ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) || held.is(ModItems.FLINT_AXE))) {
			int count = state.is(ModBlocks.BURNT_BRANCH_SLAB) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			ItemStack recovered = new ItemStack(state.is(ModBlocks.BURNT_BRANCH_BLOCK) ? ModItems.BURNT_BRANCH_BLOCK : ModItems.BURNT_BRANCH_SLAB, count);
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, recovered);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.74F, 0.78F);
			cir.setReturnValue(false);
			return;
		}

		if ((state.is(ModBlocks.ADVANCED_KILN_CONTROLLER) || state.is(ModBlocks.ADVANCED_KILN_CASING))
			&& ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE)) {
			AdvancedKilnMultiblock.dismantle(level, pos, state, player);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.85F, 0.72F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.WATTLED_DAUB_BRICKS) || state.is(ModBlocks.WATTLED_DAUB_WINDOW)) {
			boolean window = state.is(ModBlocks.WATTLED_DAUB_WINDOW);
			level.removeBlock(pos, false);
			if (held.is(ModItems.FLINT_MULTITOOL) || held.is(ModItems.ADVANCED_FLINT_MULTITOOL)) {
				projectflint$dropAtBlock(pos, new ItemStack(window ? ModItems.WATTLED_DAUB_WINDOW : ModItems.WATTLED_DAUB_BRICKS));
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			} else {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUDE_REED));
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.TWIG, 2));
			}
			level.playSound(null, pos, window ? SoundEvents.GLASS_BREAK : SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.72F, 0.78F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.WATTLED_DAUB_FLOOR_TILE));
			level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.72F, 0.82F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.MORTAR_BRICKS) && ModItems.isHammerLike(held)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.MORTAR_BRICKS));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.72F, 0.78F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.MORTAR_BRICK_SLAB) && ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER)) {
			int slabs = state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.MORTAR_BRICK_SLAB, slabs));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.72F, 0.78F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.CRUDE_CLAY_BLOCK)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUDE_CLAY_BLOCK));
			level.playSound(null, pos, SoundEvents.MUD_BREAK, SoundSource.BLOCKS, 0.7F, 0.82F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.CRUSHED_STONE_SLAB) && ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER)) {
			int slabs = state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUSHED_STONE_SLAB, slabs));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.72F, 0.78F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.CRUDE_CLAY_SLAB) && ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER)) {
			int slabs = state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUDE_CLAY_SLAB, slabs));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.MUD_BREAK, SoundSource.BLOCKS, 0.7F, 0.82F);
			cir.setReturnValue(false);
			return;
		}
		if (state.is(ModBlocks.CRUDE_CLAY_KILN) && !state.getValue(CrudeClayKilnBlock.LIT)) {
			int branches = state.getValue(CrudeClayKilnBlock.BRANCHES);
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUDE_CLAY_KILN));
			if (branches > 0) projectflint$dropAtBlock(pos, new ItemStack(ModItems.BRANCH, branches));
			level.playSound(null, pos, SoundEvents.MUD_BREAK, SoundSource.BLOCKS, 0.7F, 0.72F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.CLAY_GRINDING_BASE) && ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE)) {
			int stones = state.getValue(ClayGrindingBaseBlock.STONES);
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.SUPER_HEATED_CLAY, 4));
			if (stones > 0) projectflint$dropAtBlock(pos, new ItemStack(ModItems.VERY_REFINED_STONE, stones));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.7F, 0.82F);
			cir.setReturnValue(false);
			return;
		}

		if (ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE) && (state.is(ModBlocks.BAKED_CLAY_KILN) || state.is(ModBlocks.BAKED_KILN_TOP))) {
			BlockPos basePos = state.is(ModBlocks.BAKED_KILN_TOP) ? pos.below() : pos;
			BlockPos topPos = basePos.above();
			if (level.getBlockState(basePos).is(ModBlocks.BAKED_CLAY_KILN)) {
				boolean hasBakedTop = level.getBlockState(topPos).is(ModBlocks.BAKED_KILN_TOP);
				level.removeBlock(topPos, false);
				level.removeBlock(basePos, false);
				if (held.is(ModItems.ADVANCED_FLINT_MULTITOOL)) {
					projectflint$dropAtBlock(basePos, new ItemStack(ModItems.BAKED_CLAY_KILN));
					if (hasBakedTop) projectflint$dropAtBlock(basePos, new ItemStack(ModItems.BAKED_KILN_TOP));
				} else {
					projectflint$dropAtBlock(basePos, new ItemStack(ModItems.FIRED_CLAY, 2));
				}
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				level.playSound(null, basePos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.78F, 0.76F);
				cir.setReturnValue(false);
				return;
			}
		}

		if (ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE) && state.is(ModBlocks.CLAY_GRINDER)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, held.is(ModItems.ADVANCED_FLINT_MULTITOOL)
				? new ItemStack(ModItems.CLAY_GRINDER)
				: new ItemStack(ModItems.SUPER_HEATED_CLAY, 4));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.78F, 0.78F);
			cir.setReturnValue(false);
			return;
		}


		if (state.is(ModBlocks.LOOSE_DIRT)
			&& level.getBlockState(pos.below()).is(ModBlocks.BAKED_CLAY_KILN)
			&& ModItems.isCutterLike(held)) {
			level.setBlockAndUpdate(pos, ModBlocks.KILN_TOP_FORM.defaultBlockState());
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "kiln_top_form", "cut_kiln_top");
			level.playSound(null, pos, SoundEvents.MUD_BREAK, SoundSource.BLOCKS, 0.72F, 0.82F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.LOOSE_DIRT) && ModItems.isMultitool(held)) {
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.LOOSE_DIRT));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.7F, 0.86F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModBlocks.LOOSE_DIRT_SLAB) && ModItems.isMultitool(held)) {
			int slabs = state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			level.removeBlock(pos, false);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.LOOSE_DIRT_SLAB, slabs));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.7F, 0.86F);
			cir.setReturnValue(false);
			return;
		}

		// Flint Plant Cutters are the dedicated Sugarcane Roll tool.
		// The Advanced Flint Multitool keeps advanced plant-tier speed/yields, but
		// harvesting cane with it returns normal Sugar Cane instead of rolls.
		if (state.is(Blocks.SUGAR_CANE) && held.is(ModItems.FLINT_PLANT_CUTTERS)) {
			int harvested = projectflint$removeSugarCaneColumn(pos);
			if (harvested > 0) {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.SUGARCANE_ROLL, harvested));
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				ModAdvancements.award(player, "sugarcane_roll", "has_sugarcane_roll");
				level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.72F, 1.02F);
			}
			cir.setReturnValue(false);
			return;
		}

		if (state.is(Blocks.SUGAR_CANE) && ModItems.isPlantCutterLike(held)) {
			int harvested = projectflint$removeSugarCaneColumn(pos);
			if (harvested > 0) {
				projectflint$dropAtBlock(pos, new ItemStack(Items.SUGAR_CANE, harvested));
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
				ModAdvancements.award(player, "sugar_cane_harvest", "harvested_sugar_cane");
				level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.72F, 1.02F);
			}
			cir.setReturnValue(false);
			return;
		}

		// Bootstrap route retained so the first Plant Tool Bindings do not require Cutters.
		if (state.is(Blocks.SUGAR_CANE) && held.is(ModItems.CRUDE_STONE_HAMMER)) {
			int harvested = projectflint$removeSugarCaneColumn(pos);
			if (harvested > 0) {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUDE_REED, harvested));
				held.hurtAndBreak(harvested, player, InteractionHand.MAIN_HAND);
				ModAdvancements.award(player, "crude_reeds", "harvested_crude_reeds");
				level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.72F, 0.86F);
			}
			cir.setReturnValue(false);
			return;
		}

		if (state.is(Blocks.STONE) && ModItems.isHammerLike(held)) {
			level.setBlockAndUpdate(pos, ModBlocks.CRUSHED_STONE.defaultBlockState());
			for (int i = 0; i < 4; i++) projectflint$dropAtBlock(pos, new ItemStack(ModItems.ROCK));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "crushed_stone", "crushed_stone");
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.9F, 0.7F);
			cir.setReturnValue(false);
			return;
		}

		if ((state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) && ModItems.isPlantCutterLike(held)) {
			boolean tall = state.is(Blocks.TALL_GRASS);
			projectflint$removeFlowerWithoutDrops(pos, state);
			int count = ToolCapabilities.isAdvancedPlantTier(held) ? 3 : held.is(ModItems.FLINT_MULTITOOL) ? 2 : 1;
			if (tall) count *= 2;
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.CRUDE_PLANT_STRING, count));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "direct_plant_string", "harvested_direct_plant_string");
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.65F, 1.12F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(Blocks.LEAF_LITTER) && ModItems.isPlantCutterLike(held)) {
			int layers = state.hasProperty(SegmentableBlock.AMOUNT) ? state.getValue(SegmentableBlock.AMOUNT) : 4;
			level.removeBlock(pos, false);
			int fullCount = ToolCapabilities.isAdvancedPlantTier(held) ? 6 : held.is(ModItems.FLINT_MULTITOOL) ? 4 : 2;
			int count = Math.max(1, (fullCount * layers + 3) / 4);
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.PLANT_FIBER, count));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "leaf_litter_fiber", "harvested_leaf_litter_fiber");
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.65F, 0.92F);
			cir.setReturnValue(false);
			return;
		}

		if (ToolCapabilities.canDirectHarvestPlant(held, state)) {
			projectflint$removeFlowerWithoutDrops(pos, state);
			ItemStack harvested = new ItemStack(state.getBlock().asItem());
			if (!harvested.isEmpty()) projectflint$dropAtBlock(pos, harvested);
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.65F, 1.08F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(BlockTags.FLOWERS) && ModItems.isPlantCutterLike(held)) {
			boolean tall = state.hasProperty(DoublePlantBlock.HALF);
			projectflint$removeFlowerWithoutDrops(pos, state);
			int petals = ToolCapabilities.isAdvancedPlantTier(held)
				? GameplayTuning.ADVANCED_FLINT_MULTITOOL_FLOWER_PETALS
				: held.is(ModItems.FLINT_MULTITOOL)
					? GameplayTuning.FLINT_MULTITOOL_FLOWER_PETALS
				: held.is(ModItems.CRUDE_MULTITOOL)
					? GameplayTuning.CRUDE_MULTITOOL_FLOWER_PETALS
					: GameplayTuning.BASE_FLOWER_PETALS;
			if (tall) petals *= GameplayTuning.TALL_FLOWER_MULTIPLIER;
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.FLOWER_PETALS, petals));
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "flower_petals", "harvested_flower_petals");
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.65F, 1.22F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(BlockTags.LEAVES) && ToolCapabilities.canHarvestLeaves(held)) {
			level.removeBlock(pos, false);
			if (ToolCapabilities.isAdvancedPlantTier(held)) {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.BRANCH, 3));
				if (level.getRandom().nextFloat() < 0.05F) projectflint$dropAtBlock(pos, new ItemStack(Items.APPLE));
			} else {
				int branchCount = held.is(ModItems.FLINT_MULTITOOL) ? 2 : 1;
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.TWIG, GameplayTuning.MULTITOOL_LEAF_TWIGS));
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.BRANCH, branchCount));
			}
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "branch", "harvested_branch");
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.78F, 0.74F);
			cir.setReturnValue(false);
			return;
		}

		if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.LOOSE_SOIL)) && ModItems.isMultitool(held)) {
			projectflint$loosenAdjacentDirt(pos);
			level.removeBlock(pos, false);
			if (held.is(ModItems.ADVANCED_FLINT_MULTITOOL)) {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.DIRT_PILE, 3));
			} else {
				int count = held.is(ModItems.FLINT_MULTITOOL) ? GameplayTuning.FLINT_MULTITOOL_EARTH_YIELD : 1;
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.DIRT_PILE, count));
			}
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "dirt_pile", "dug_dirt_pile");
			level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.7F, 0.84F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(Blocks.GRAVEL) && ModItems.isMultitool(held)) {
			level.removeBlock(pos, false);
			if (held.is(ModItems.ADVANCED_FLINT_MULTITOOL)) {
				projectflint$dropAtBlock(pos, new ItemStack(Items.FLINT));
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.FLINT_CHIP));
			} else {
				int count = held.is(ModItems.FLINT_MULTITOOL) ? GameplayTuning.FLINT_MULTITOOL_GRAVEL_YIELD : 1;
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.FLINT_CHIP, count));
			}
			held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			ModAdvancements.award(player, "flint_chip", "harvested_flint_chip");
			level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.8F, 0.74F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModTags.GRASS_SCRAPE_TARGETS)) {
			level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
			if (level.getRandom().nextFloat() < GameplayTuning.GRASS_FIBER_CHANCE) {
				projectflint$dropAtBlock(pos, new ItemStack(ModItems.PLANT_FIBER));
			}
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8F, 0.72F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) && held.isEmpty()) {
			level.removeBlock(pos, false);
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.45F, 0.62F);
			cir.setReturnValue(false);
			return;
		}

		if (state.is(ModTags.FORAGE_BUSHES) && (held.is(ModItems.REFINED_ROCK) || ModItems.isPlantCutterLike(held))) {
			int twigCount;
			if (ToolCapabilities.isAdvancedPlantTier(held)) {
				twigCount = 8;
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			} else if (held.is(ModItems.FLINT_MULTITOOL)) {
				twigCount = 6;
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			} else if (ModItems.isPlantCutterLike(held)) {
				twigCount = GameplayTuning.CUTTERS_BUSH_TWIGS;
				held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			} else {
				int range = GameplayTuning.BUSH_MAX_TWIGS - GameplayTuning.BUSH_MIN_TWIGS + 1;
				twigCount = GameplayTuning.BUSH_MIN_TWIGS + level.getRandom().nextInt(range);
			}
			projectflint$dropAtBlock(pos, new ItemStack(ModItems.TWIG, twigCount));
			level.removeBlock(pos, false);
			level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.9F, 0.92F);
			cir.setReturnValue(false);
		}
	}


	private static boolean projectflint$isVanillaPlankSlab(BlockState state) {
		return state.is(Blocks.OAK_SLAB) || state.is(Blocks.SPRUCE_SLAB) || state.is(Blocks.BIRCH_SLAB)
			|| state.is(Blocks.JUNGLE_SLAB) || state.is(Blocks.ACACIA_SLAB) || state.is(Blocks.DARK_OAK_SLAB)
			|| state.is(Blocks.MANGROVE_SLAB) || state.is(Blocks.CHERRY_SLAB) || state.is(Blocks.PALE_OAK_SLAB)
			|| state.is(Blocks.BAMBOO_SLAB) || state.is(Blocks.BAMBOO_MOSAIC_SLAB)
			|| state.is(Blocks.CRIMSON_SLAB) || state.is(Blocks.WARPED_SLAB);
	}

	private static boolean projectflint$isProcessedWood(BlockState state) {
		return state.is(ModBlocks.SANDED_WOOD_SLAB) || state.is(ModBlocks.GRINDED_WOOD_SLAB)
			|| state.is(ModBlocks.WOVEN_WOOD_SLAB) || state.is(ModBlocks.WOVEN_WOOD);
	}

	private static ItemStack projectflint$strippedLog(BlockState state) {
		if (state.is(Blocks.OAK_LOG)) return new ItemStack(Items.STRIPPED_OAK_LOG);
		if (state.is(Blocks.SPRUCE_LOG)) return new ItemStack(Items.STRIPPED_SPRUCE_LOG);
		if (state.is(Blocks.BIRCH_LOG)) return new ItemStack(Items.STRIPPED_BIRCH_LOG);
		if (state.is(Blocks.JUNGLE_LOG)) return new ItemStack(Items.STRIPPED_JUNGLE_LOG);
		if (state.is(Blocks.ACACIA_LOG)) return new ItemStack(Items.STRIPPED_ACACIA_LOG);
		if (state.is(Blocks.DARK_OAK_LOG)) return new ItemStack(Items.STRIPPED_DARK_OAK_LOG);
		if (state.is(Blocks.MANGROVE_LOG)) return new ItemStack(Items.STRIPPED_MANGROVE_LOG);
		if (state.is(Blocks.CHERRY_LOG)) return new ItemStack(Items.STRIPPED_CHERRY_LOG);
		if (state.is(Blocks.PALE_OAK_LOG)) return new ItemStack(Items.STRIPPED_PALE_OAK_LOG);
		if (state.is(Blocks.CRIMSON_STEM)) return new ItemStack(Items.STRIPPED_CRIMSON_STEM);
		if (state.is(Blocks.WARPED_STEM)) return new ItemStack(Items.STRIPPED_WARPED_STEM);
		if (state.is(Blocks.BAMBOO_BLOCK)) return new ItemStack(Items.STRIPPED_BAMBOO_BLOCK);
		return ItemStack.EMPTY;
	}


	private static boolean projectflint$isStrippedLog(BlockState state) {
		return state.is(Blocks.STRIPPED_OAK_LOG) || state.is(Blocks.STRIPPED_SPRUCE_LOG) || state.is(Blocks.STRIPPED_BIRCH_LOG)
			|| state.is(Blocks.STRIPPED_JUNGLE_LOG) || state.is(Blocks.STRIPPED_ACACIA_LOG) || state.is(Blocks.STRIPPED_DARK_OAK_LOG)
			|| state.is(Blocks.STRIPPED_MANGROVE_LOG) || state.is(Blocks.STRIPPED_CHERRY_LOG) || state.is(Blocks.STRIPPED_PALE_OAK_LOG)
			|| state.is(Blocks.STRIPPED_CRIMSON_STEM) || state.is(Blocks.STRIPPED_WARPED_STEM) || state.is(Blocks.STRIPPED_BAMBOO_BLOCK);
	}

	private void projectflint$loosenAdjacentDirt(BlockPos origin) {
		for (Direction direction : Direction.values()) {
			BlockPos adjacent = origin.relative(direction);
			BlockState adjacentState = level.getBlockState(adjacent);
			if ((adjacentState.is(Blocks.DIRT) || adjacentState.is(Blocks.GRASS_BLOCK))
				&& level.getRandom().nextFloat() < 0.33F) {
				level.setBlockAndUpdate(adjacent, ModBlocks.LOOSE_SOIL.defaultBlockState());
			}
		}
	}

	private void projectflint$dropAtBlock(BlockPos pos, ItemStack stack) {
		if (stack.isEmpty()) return;
		Block.popResource(level, pos, stack);
	}

	private int projectflint$removeSugarCaneColumn(BlockPos start) {
		BlockPos top = start;
		while (level.getBlockState(top.above()).is(Blocks.SUGAR_CANE)) top = top.above();
		int harvested = 0;
		for (BlockPos cursor = top; cursor.getY() >= start.getY(); cursor = cursor.below()) {
			if (!level.getBlockState(cursor).is(Blocks.SUGAR_CANE)) continue;
			level.removeBlock(cursor, false);
			harvested++;
		}
		return harvested;
	}

	private void projectflint$removeFlowerWithoutDrops(BlockPos pos, BlockState state) {
		if (state.hasProperty(DoublePlantBlock.HALF)) {
			DoubleBlockHalf half = state.getValue(DoublePlantBlock.HALF);
			BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
			BlockState otherState = level.getBlockState(otherPos);
			if (otherState.is(state.getBlock()) && otherState.hasProperty(DoublePlantBlock.HALF)) level.removeBlock(otherPos, false);
		}
		level.removeBlock(pos, false);
	}
}
