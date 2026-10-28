package dev.projectflint.mixin;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.block.CrushedStoneBlock;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModTags;
import dev.projectflint.system.ToolCapabilities;
import dev.projectflint.system.LumberProcessorSystem;
import dev.projectflint.system.TannedUtilityVestSystem;
import dev.projectflint.system.PlayerPlacedBlockLedger;
import dev.projectflint.system.WoodenToolTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void projectflint$primitiveDestroyProgress(Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (player.isCreative()) return;

		float vanillaProgress = cir.getReturnValue();
		BlockState state = (BlockState) (Object) this;
		ItemStack held = player.getMainHandItem();
		// Leaves should never pop instantly from unrelated held tools or a retired belt.
		// The Vest grants hand-harvesting permission, not an instantaneous leaf harvest.
		if (state.is(BlockTags.LEAVES)) {
			if (ToolCapabilities.canHarvestLeaves(held)) {
				cir.setReturnValue(ToolCapabilities.leafProgressPerTick(held));
			} else if (TannedUtilityVestSystem.canHandClear(player, state)) {
				cir.setReturnValue(1.0F / 40.0F);
			} else {
				cir.setReturnValue(0.0F);
			}
			return;
		}
		if (TannedUtilityVestSystem.canHandClear(player, state)) {
			cir.setReturnValue(1.0F);
			return;
		}
		if (state.is(ModBlocks.HARDWOOD_CRAFTING_TABLE)) { cir.setReturnValue(held.is(ModItems.HARDWOOD_AXE) || held.is(net.minecraft.world.item.Items.WOODEN_AXE) || held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.03F) : 0.0F); return; }
		if (state.is(ModBlocks.BASIN) || state.is(ModBlocks.FAT_BURNING_TORCH)) {
			cir.setReturnValue(Math.max(vanillaProgress, state.is(ModBlocks.FAT_BURNING_TORCH) ? 1.0F : 0.06F));
			return;
		}
		if (held.is(ModItems.HARDWOOD_HAMMER) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
			&& !state.is(BlockTags.NEEDS_IRON_TOOL) && !state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
			// The Hammer is for rock-working and stone-tier ores, NOT a faster strip-mining pickaxe.
			// Never take max(vanillaProgress, customProgress): that reopens the fast-mining bypass.
			cir.setReturnValue(1.0F / 420.0F); // 21s for natural rock and pickaxe-mineable ores.
			return;
		}
		// Tool Belt is disabled; wooden tool timings do not depend on equipped leggings.
		// Transitional wooden tools remain slower than vanilla but can harvest basic materials.
		if ((held.is(net.minecraft.world.item.Items.WOODEN_PICKAXE) || held.is(ModItems.HARDWOOD_PICKAXE))
			&& WoodenToolTargets.pickaxe(state)) {
			// Hardwood is an actual mining upgrade, while stone continues to resist strip-mining.
			cir.setReturnValue(held.is(ModItems.HARDWOOD_PICKAXE) ? 1.0F / 180.0F : 1.0F / 270.0F);
			return;
		}
		if ((held.is(net.minecraft.world.item.Items.WOODEN_PICKAXE) || held.is(ModItems.HARDWOOD_PICKAXE)) && WoodenToolTargets.brickMasonry(state)) {
			cir.setReturnValue(held.is(ModItems.HARDWOOD_PICKAXE) ? 1.0F / 90.0F : 1.0F / 120.0F);
			return;
		}
		// The transitional Wooden Pickaxe must not bypass Minecraft's higher mining tiers.
		// This check runs before the Utility Vest's player-placed preview so ores such as
		// iron remain completely unbreakable with the Wooden Pickaxe.
		if ((held.is(net.minecraft.world.item.Items.WOODEN_PICKAXE) || held.is(ModItems.HARDWOOD_PICKAXE))
			&& (state.is(BlockTags.NEEDS_STONE_TOOL) || state.is(BlockTags.NEEDS_IRON_TOOL) || state.is(BlockTags.NEEDS_DIAMOND_TOOL))) {
			cir.setReturnValue(0.0F);
			return;
		}
		if ((held.is(net.minecraft.world.item.Items.WOODEN_AXE) || held.is(ModItems.HARDWOOD_AXE)) && state.is(BlockTags.LOGS)) {
			cir.setReturnValue(Math.max(vanillaProgress * 0.65F, 1.0F / 105.0F));
			return;
		}
		if ((held.is(net.minecraft.world.item.Items.WOODEN_SHOVEL) || held.is(ModItems.HARDWOOD_SHOVEL)) && WoodenToolTargets.shovel(state)) {
			cir.setReturnValue(Math.max(vanillaProgress * 0.55F, 1.0F / 100.0F));
			return;
		}
		// Grass scraping is a bare-hand action; tools must use an allowed harvesting path.
		if (state.is(ModTags.GRASS_SCRAPE_TARGETS) && !held.isEmpty() && !ModItems.isMultitool(held)) {
			cir.setReturnValue(0.0F);
			return;
		}
		// A candidate for Vest recovery is not proof that natural stone was player-placed.
		// Apply the wooden-tool mining timing before the optimistic client-side Vest preview.
		if (player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(ModItems.TANNED_UTILITY_VEST)
			&& PlayerPlacedBlockLedger.potential(level, pos, state)) {
			cir.setReturnValue(Math.max(vanillaProgress, 0.15F));
			return;
		}
		if (ModBlocks.isWoodenBeam(state)) {
			cir.setReturnValue((ToolCapabilities.isWellWoodTool(held) || TannedUtilityVestSystem.canDismantleWellFrame(player, state)) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.ROPE)) {
			cir.setReturnValue(Math.max(vanillaProgress, 0.08F));
			return;
		}
		if (state.is(ModBlocks.WELL_PULLEY)) {
			cir.setReturnValue((ToolCapabilities.isWellWoodTool(held) || TannedUtilityVestSystem.canDismantleWellFrame(player, state)) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(Blocks.BRICKS) || state.is(ModBlocks.BRICK_SHINGLES)) {
			boolean canBreak = ToolCapabilities.canMineBrick(held);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.CRUSHED_STONE)) {
			boolean canBreak = CrushedStoneBlock.isPlayerPlaced(state) && held.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.FLAX_CROP)) {
			cir.setReturnValue(Math.max(vanillaProgress, 1.0F));
			return;
		}
		if (state.is(ModBlocks.LOOSE_MUD_BRICKS) || state.is(ModBlocks.LOOSE_MUD_BRICK_SLAB)) {
			boolean canBreak = ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (projectflint$isLimestoneFamily(state)) {
			boolean canBreak = held.is(ModItems.ADVANCED_FLINT_MULTITOOL);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.BOOKSHELF)) {
			boolean canBreak = held.is(ModItems.FLINT_AXE);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.WOODEN_CHEST)) {
			cir.setReturnValue(held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.WOODEN_CABINET)) {
			cir.setReturnValue(held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.FIRECLAY_BRICK_BLOCK) || state.is(ModBlocks.FIRECLAY_BRICK_SLAB) || state.is(ModBlocks.FIRECLAY_BRICK_STAIRS)
			|| state.is(ModBlocks.BRICK_FURNACE) || state.is(ModBlocks.BRICK_BLAST_FURNACE) || state.is(ModBlocks.FIRECLAY_FORGE_CONTROLLER)) {
			boolean canBreak = ToolCapabilities.canMineBrick(held);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.PRIMITIVE_STOVE)) {
			cir.setReturnValue(held.is(ModItems.ADVANCED_FLINT_MULTITOOL) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.SLEEPING_BAG)) {
			cir.setReturnValue(vanillaProgress);
			return;
		}
		if (state.is(ModBlocks.WATTLED_DAUB_BRICKS) || state.is(ModBlocks.WATTLED_DAUB_WINDOW) || state.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)) {
			cir.setReturnValue(Math.max(vanillaProgress, 0.025F));
			return;
		}
		if (state.is(ModBlocks.WATTLED_DAUB_DOOR)) {
			cir.setReturnValue(Math.max(vanillaProgress, 0.025F));
			return;
		}
		if (state.is(ModBlocks.FLINT_TORCH)) {
			cir.setReturnValue(Math.max(vanillaProgress, 0.08F));
			return;
		}
		if (state.is(ModBlocks.SANDING_STATION)) {
			cir.setReturnValue(held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.LUMBER_WORKPIECE)) {
			cir.setReturnValue(1.0F / 150.0F);
			return;
		}
		if (LumberProcessorSystem.isProcessorPart(level, pos)) {
			cir.setReturnValue(held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.WATER_POT)) {
			cir.setReturnValue(Math.max(vanillaProgress, 0.10F));
			return;
		}
		if (LumberProcessorSystem.isSupportedStrippedLog(state) && level.getBlockState(pos.below()).is(ModBlocks.LUMBER_CHOPPING_BLOCK)) {
			// Mining progress is predicted on the client, while the Water Pot/controller block-entity
			// state is authoritative on the server and is not guaranteed to be synchronized every tick.
			// Requiring canChop() here made a full, valid processor look permanently unmineable client-side.
			boolean canChop = held.is(ModItems.FLINT_PLANT_CUTTERS);
			if (canChop && level instanceof net.minecraft.world.level.Level actualLevel && !actualLevel.isClientSide()) {
				canChop = LumberProcessorSystem.canChop(actualLevel, pos.below());
			}
			cir.setReturnValue(canChop ? 1.0F / 150.0F : 0.0F);
			return;
		}
		if (state.is(ModBlocks.MORTAR_BRICKS)) {
			cir.setReturnValue(ModItems.isHammerLike(held) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.MORTAR_BRICK_SLAB) || state.is(ModBlocks.CRUDE_CLAY_SLAB) || state.is(ModBlocks.CRUSHED_STONE_SLAB)) {
			cir.setReturnValue(ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.MORTAR_CHEST)) {
			cir.setReturnValue(ModItems.isFlintTier(held) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.WOVEN_CHEST)) {
			cir.setReturnValue(held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.MORTAR_KILN)) {
			cir.setReturnValue(ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.WOODEN_GRINDER)) {
			cir.setReturnValue(held.is(ModItems.FLINT_AXE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.MORTAR_KILN_BASE) || state.is(ModBlocks.WOODEN_GRINDER_BASE)) {
			cir.setReturnValue(ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.BURNT_BRANCH_BLOCK) || state.is(ModBlocks.BURNT_BRANCH_SLAB)) {
			boolean canBreak = ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) || held.is(ModItems.FLINT_AXE);
			cir.setReturnValue(canBreak ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.ADVANCED_KILN_CONTROLLER) || state.is(ModBlocks.ADVANCED_KILN_CASING)) {
			cir.setReturnValue(ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}

		if ((held.is(ModItems.FLINT_AXE) || ModItems.canHarvestGourdFlesh(held))
			&& (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON))) {
			cir.setReturnValue(Math.max(vanillaProgress, 1.0F / 65.0F));
			return;
		}
		if (held.is(ModItems.FLINT_AXE) && (projectflint$isSupportedFlintAxeLog(state) || projectflint$isStrippedFlintAxeLog(state))) {
			cir.setReturnValue(1.0F / 130.0F);
			return;
		}
		if (held.is(ModItems.FLINT_AXE) && (projectflint$isVanillaPlankSlab(state) || projectflint$isProcessedWood(state))) {
			cir.setReturnValue(Math.max(vanillaProgress, 1.0F / 100.0F));
			return;
		}
		if (held.is(ModItems.FLINT_PLANT_CUTTERS) && (state.is(Blocks.SUGAR_CANE) || state.is(Blocks.SHORT_GRASS))) {
			cir.setReturnValue(1.0F);
			return;
		}
		if (state.is(Blocks.SWEET_BERRY_BUSH)) {
			cir.setReturnValue(ModItems.canHarvestSweetBerrySeeds(held) ? 1.0F / 130.0F : 0.0F);
			return;
		}
		if (state.is(ModBlocks.STRAWBERRY_BUSH)) {
			cir.setReturnValue(ModItems.canHarvestSweetBerrySeeds(held) ? 1.0F / 130.0F : 0.0F);
			return;
		}
		if (state.is(ModBlocks.CRUDE_CLAY_BLOCK) || state.is(ModBlocks.CRUDE_CLAY_KILN)) {
			cir.setReturnValue(vanillaProgress);
			return;
		}
		if (state.is(ModBlocks.BAKED_CLAY_KILN) || state.is(ModBlocks.BAKED_KILN_TOP) || state.is(ModBlocks.CLAY_GRINDING_BASE) || state.is(ModBlocks.CLAY_GRINDER)) {
			cir.setReturnValue(ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE) ? Math.max(vanillaProgress, 0.025F) : 0.0F);
			return;
		}
		if (state.is(ModBlocks.KILN_TOP_FORM)) {
			cir.setReturnValue(0.0F);
			return;
		}
		if (state.is(ModBlocks.LOOSE_DIRT) && level.getBlockState(pos.below()).is(ModBlocks.BAKED_CLAY_KILN) && ModItems.isCutterLike(held)) {
			cir.setReturnValue(projectflint$cutterProgress(held, GameplayTuning.HAND_CLEAR_FOLIAGE_PROGRESS_PER_TICK));
			return;
		}
		if (state.is(ModBlocks.LOOSE_DIRT) && ModItems.isMultitool(held)) {
			cir.setReturnValue(vanillaProgress * ToolCapabilities.earthProgressMultiplier(held));
			return;
		}
		if (state.is(ModBlocks.LOOSE_DIRT_SLAB) && ModItems.isMultitool(held)) {
			cir.setReturnValue(vanillaProgress * ToolCapabilities.earthProgressMultiplier(held));
			return;
		}
		if (state.is(ModTags.GRASS_SCRAPE_TARGETS) && held.isEmpty()) {
			cir.setReturnValue(GameplayTuning.GRASS_STRIP_PROGRESS_PER_TICK);
			return;
		}
		if (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) && held.isEmpty()) {
			cir.setReturnValue(GameplayTuning.HAND_CLEAR_FOLIAGE_PROGRESS_PER_TICK);
			return;
		}
		if (ToolCapabilities.canDirectHarvestPlant(held, state)) {
			cir.setReturnValue(1.0F);
			return;
		}
		if (state.is(ModTags.FORAGE_BUSHES)) {
			float base = GameplayTuning.BUSH_CUT_PROGRESS_PER_TICK;
			if (held.is(ModItems.REFINED_ROCK)) cir.setReturnValue(base);
			else if (ModItems.isPlantCutterLike(held)) cir.setReturnValue(projectflint$cutterProgress(held, base));
			else cir.setReturnValue(0.0F); // Bare hands/irrelevant items cannot harvest a progression bush.
			return;
		}
		if (state.is(Blocks.LEAF_LITTER) && ModItems.isPlantCutterLike(held)) {
			float progress = GameplayTuning.HAND_CLEAR_FOLIAGE_PROGRESS_PER_TICK * ToolCapabilities.foliageCutterSpeedMultiplier(held);
			cir.setReturnValue(Math.min(1.0F, progress));
			return;
		}
		if (state.is(Blocks.STONE) && ModItems.isHammerLike(held)) {
			cir.setReturnValue(projectflint$hammerProgress(held, GameplayTuning.CRUDE_HAMMER_STONE_PROGRESS_PER_TICK));
			return;
		}
		if (held.is(ModItems.CRUDE_STONE_HAMMER) && state.is(Blocks.SUGAR_CANE)) {
			cir.setReturnValue(GameplayTuning.CRUDE_HAMMER_REED_PROGRESS_PER_TICK);
			return;
		}
		if (ModItems.isPlantCutterLike(held) && (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) || state.is(Blocks.TALL_GRASS) || state.is(BlockTags.FLOWERS))) {
			float progress = GameplayTuning.HAND_CLEAR_FOLIAGE_PROGRESS_PER_TICK * ToolCapabilities.foliageCutterSpeedMultiplier(held);
			cir.setReturnValue(Math.min(1.0F, progress));
			return;
		}
		if (ModItems.isPlantCutterLike(held) && state.is(Blocks.SUGAR_CANE)) {
			cir.setReturnValue(Math.min(1.0F, vanillaProgress * ToolCapabilities.cutterSpeedMultiplier(held)));
			return;
		}
		// Dirt and Loose Soil intentionally share this exact progress path.
		if (ModItems.isMultitool(held) && (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.LOOSE_SOIL) || state.is(Blocks.GRAVEL))) {
			cir.setReturnValue(vanillaProgress * ToolCapabilities.earthProgressMultiplier(held));
			return;
		}

		cir.setReturnValue(0.0F);
	}

	private static boolean projectflint$isLimestoneFamily(BlockState state) {
		return state.is(ModBlocks.LIMESTONE)
			|| state.is(ModBlocks.COBBLED_LIMESTONE)
			|| state.is(ModBlocks.SMOOTH_LIMESTONE)
			|| state.is(ModBlocks.LIMESTONE_BRICKS)
			|| state.is(ModBlocks.CHISELED_LIMESTONE)
			|| state.is(ModBlocks.POLISHED_LIMESTONE)
			|| state.is(ModBlocks.CHECKERED_TILE)
			|| state.is(ModBlocks.GLAZED_CHECKERED_TILE);
	}

	private static float projectflint$cutterProgress(ItemStack stack, float base) {
		return base * ToolCapabilities.cutterSpeedMultiplier(stack);
	}

	private static float projectflint$hammerProgress(ItemStack stack, float base) {
		return base * ToolCapabilities.hammerSpeedMultiplier(stack);
	}

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void projectflint$requireGroundSupport(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState state = (BlockState) (Object) this;
		if (!projectflint$isGroundPrimitive(state)) return;
		BlockPos belowPos = pos.below();
		BlockState below = level.getBlockState(belowPos);
		if (!below.isFaceSturdy(level, belowPos, Direction.UP)) cir.setReturnValue(false);
	}

	private static boolean projectflint$isSupportedFlintAxeLog(BlockState state) {
		return state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG) || state.is(Blocks.BIRCH_LOG)
			|| state.is(Blocks.JUNGLE_LOG) || state.is(Blocks.ACACIA_LOG) || state.is(Blocks.DARK_OAK_LOG)
			|| state.is(Blocks.MANGROVE_LOG) || state.is(Blocks.CHERRY_LOG) || state.is(Blocks.PALE_OAK_LOG)
			|| state.is(Blocks.CRIMSON_STEM) || state.is(Blocks.WARPED_STEM) || state.is(Blocks.BAMBOO_BLOCK);
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

	private static boolean projectflint$isStrippedFlintAxeLog(BlockState state) {
		return state.is(Blocks.STRIPPED_OAK_LOG) || state.is(Blocks.STRIPPED_SPRUCE_LOG) || state.is(Blocks.STRIPPED_BIRCH_LOG)
			|| state.is(Blocks.STRIPPED_JUNGLE_LOG) || state.is(Blocks.STRIPPED_ACACIA_LOG) || state.is(Blocks.STRIPPED_DARK_OAK_LOG)
			|| state.is(Blocks.STRIPPED_MANGROVE_LOG) || state.is(Blocks.STRIPPED_CHERRY_LOG) || state.is(Blocks.STRIPPED_PALE_OAK_LOG)
			|| state.is(Blocks.STRIPPED_CRIMSON_STEM) || state.is(Blocks.STRIPPED_WARPED_STEM) || state.is(Blocks.STRIPPED_BAMBOO_BLOCK);
	}

	private static boolean projectflint$isGroundPrimitive(BlockState state) {
		return state.is(ModBlocks.LOOSE_ROCK)
			|| state.is(ModBlocks.TWIG_PILE)
			|| state.is(ModBlocks.FIBER_WORK_PILE)
			|| state.is(ModBlocks.HAMMER_ASSEMBLY)
			|| state.is(ModBlocks.BINDING_ASSEMBLY)
			|| state.is(ModBlocks.HARVESTING_TOOL_ASSEMBLY)
			|| state.is(ModBlocks.MULTITOOL_ASSEMBLY)
			|| state.is(ModBlocks.FLINT_TOOL_ASSEMBLY)
			|| state.is(ModBlocks.WOVEN_BOWL_ASSEMBLY)
			|| state.is(ModBlocks.PRIMITIVE_CAMPFIRE)
			|| state.is(ModBlocks.DIRT_PILE)
			|| state.is(ModBlocks.CLAY_FIRING_PILE)
			|| state.is(ModBlocks.FIRED_CLAY_PILE)
			|| state.is(ModBlocks.ROCK_PILE)
			|| state.is(ModBlocks.VERY_REFINED_STONE_PILE)
			|| state.is(ModBlocks.WOVEN_GEAR_ASSEMBLY)
			|| state.is(ModBlocks.ADVANCED_TOOL_ASSEMBLY);
	}
}
