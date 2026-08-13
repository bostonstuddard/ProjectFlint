package dev.projectflint.system;

import dev.projectflint.block.BindingAssemblyBlock;
import dev.projectflint.block.ClayFiringPileBlock;
import dev.projectflint.block.CrudeClayKilnBlock;
import dev.projectflint.block.DirtPileBlock;
import dev.projectflint.block.FiberWorkPileBlock;
import dev.projectflint.block.FiredClayPileBlock;
import dev.projectflint.block.HammerAssemblyBlock;
import dev.projectflint.block.HarvestingToolAssemblyBlock;
import dev.projectflint.block.PrimitiveCampfireBlock;
import dev.projectflint.block.TwigPileBlock;
import dev.projectflint.block.entity.MortarChestBlockEntity;
import dev.projectflint.block.entity.WovenChestBlockEntity;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

public final class PrimitiveInteractionSystem {
	private PrimitiveInteractionSystem() {}

	public static void initialize() {
		AttackBlockCallback.EVENT.register(PrimitiveInteractionSystem::onAttackBlock);
		UseBlockCallback.EVENT.register(PrimitiveInteractionSystem::onUseBlock);
		UseItemCallback.EVENT.register(PrimitiveInteractionSystem::onUseItem);
		PlayerBlockBreakEvents.BEFORE.register(PrimitiveInteractionSystem::beforeBlockBreak);
	}

	private static InteractionResult onAttackBlock(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		if (player.isCreative() || player.isSpectator()) return InteractionResult.PASS;

		BlockState state = level.getBlockState(pos);
		ItemStack held = player.getItemInHand(hand);
		if (state.is(ModBlocks.SLEEPING_BAG)) return InteractionResult.PASS;
		if (state.is(ModBlocks.SANDING_STATION)) return InteractionResult.PASS;
		if (state.is(ModBlocks.LUMBER_WORKPIECE)) return InteractionResult.PASS;
		if (LumberProcessorSystem.isSupportedStrippedLog(state) && level.getBlockState(pos.below()).is(ModBlocks.LUMBER_CHOPPING_BLOCK)) {
			BlockPos controller = pos.below();
			if (!held.is(ModItems.FLINT_PLANT_CUTTERS)) {
				if (!level.isClientSide()) player.displayClientMessage(net.minecraft.network.chat.Component.literal("Use Flint Plant Cutters to process this log."), true);
				return InteractionResult.FAIL;
			}

			// Do not validate Water Pot/controller block entities on the client. Their persistent data is
			// server-authoritative and can be absent/stale client-side, which previously cancelled the
			// attack before Minecraft could even begin the 7.5-second breaking animation. The server
			// performs the full validation below and again when destroyBlock reaches startWash().
			if (level.isClientSide()) return InteractionResult.PASS;

			BlockState controllerState = level.getBlockState(controller);
			if (controllerState.is(ModBlocks.LUMBER_CHOPPING_BLOCK)) {
				Direction machineDirection = controllerState.getValue(dev.projectflint.block.LumberChoppingBlock.FACING);
				if (!LumberProcessorSystem.isValidProcessor(level, controller, machineDirection)) {
					player.displayClientMessage(net.minecraft.network.chat.Component.literal("The Lumber Processor is incomplete or unsupported."), true);
					return InteractionResult.FAIL;
				}
				if (!LumberProcessorSystem.hasFullWater(level, controller, machineDirection)) {
					player.displayClientMessage(net.minecraft.network.chat.Component.literal("Fill the Water Pot to 1.0 bucket first."), true);
					return InteractionResult.FAIL;
				}
			}
			return LumberProcessorSystem.canChop(level, controller) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		if (state.is(ModBlocks.LUMBER_CHOPPING_BLOCK)
			|| (state.is(ModBlocks.WATER_POT) && LumberProcessorSystem.isProcessorPart(level, pos))
			|| (state.is(ModBlocks.CRUSHED_STONE) && LumberProcessorSystem.isProcessorPart(level, pos))) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		if (state.is(ModBlocks.WATER_POT)) return InteractionResult.PASS;
		if (state.is(ModBlocks.WATTLED_DAUB_BRICKS) || state.is(ModBlocks.WATTLED_DAUB_WINDOW)
			|| state.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE) || state.is(ModBlocks.WATTLED_DAUB_DOOR)
			|| state.is(ModBlocks.FLINT_TORCH)) return InteractionResult.PASS;
		if (state.is(ModBlocks.MORTAR_BRICKS) && ModItems.isHammerLike(held)) return InteractionResult.PASS;
		if ((state.is(ModBlocks.MORTAR_BRICK_SLAB) || state.is(ModBlocks.CRUDE_CLAY_SLAB))
			&& ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER)) return InteractionResult.PASS;
		if (state.is(ModBlocks.MORTAR_CHEST)) return ModItems.isFlintTier(held) ? InteractionResult.PASS : InteractionResult.FAIL;
		if (state.is(ModBlocks.WOVEN_CHEST)) return held.is(ModItems.FLINT_AXE) ? InteractionResult.PASS : InteractionResult.FAIL;
		if (state.is(ModBlocks.MORTAR_KILN) || state.is(ModBlocks.WOODEN_GRINDER)) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		if (state.is(ModBlocks.MORTAR_KILN_BASE) || state.is(ModBlocks.WOODEN_GRINDER_BASE)) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		if (state.is(ModBlocks.BURNT_BRANCH_BLOCK) || state.is(ModBlocks.BURNT_BRANCH_SLAB)) {
			return (ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) || held.is(ModItems.FLINT_AXE)) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		if (state.is(ModBlocks.ADVANCED_KILN_CONTROLLER) || state.is(ModBlocks.ADVANCED_KILN_CASING)) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE) ? InteractionResult.PASS : InteractionResult.FAIL;
		}

		if (state.is(ModBlocks.FIBER_WORK_PILE)) {
			if (held.is(ModItems.REFINED_ROCK) && state.getValue(FiberWorkPileBlock.COUNT) == 2) {
				return ModBlocks.FIBER_WORK_PILE.hitWithRefinedRock(state, level, pos, player, hand);
			}
			return ModBlocks.FIBER_WORK_PILE.takeOneComponent(state, level, pos, player);
		}
		if (state.is(ModBlocks.HAMMER_ASSEMBLY)) {
			if (held.is(ModItems.REFINED_ROCK)
				&& state.getValue(HammerAssemblyBlock.TWIGS) == 2
				&& state.getValue(HammerAssemblyBlock.HAS_ROCK)) {
				return ModBlocks.HAMMER_ASSEMBLY.hitWithRefinedRock(state, level, pos, player, hand);
			}
			return ModBlocks.HAMMER_ASSEMBLY.takeOneComponent(state, level, pos, player);
		}
		if (state.is(ModBlocks.BINDING_ASSEMBLY)) {
			if (ModItems.isHammerLike(held)
				&& state.getValue(BindingAssemblyBlock.REEDS) == 4
				&& state.getValue(BindingAssemblyBlock.STRINGS) == 2) {
				return ModBlocks.BINDING_ASSEMBLY.hitWithHammer(state, level, pos, player, hand);
			}
			return ModBlocks.BINDING_ASSEMBLY.takeOneComponent(state, level, pos, player);
		}
		if (state.is(ModBlocks.HARVESTING_TOOL_ASSEMBLY)) {
			if (ModItems.isHammerLike(held)
				&& state.getValue(HarvestingToolAssemblyBlock.STONES) == 2
				&& state.getValue(HarvestingToolAssemblyBlock.BINDINGS) == 2
				&& state.getValue(HarvestingToolAssemblyBlock.HAS_TWIG)) {
				return ModBlocks.HARVESTING_TOOL_ASSEMBLY.hitWithHammer(state, level, pos, player, hand);
			}
			return ModBlocks.HARVESTING_TOOL_ASSEMBLY.takeOneComponent(state, level, pos, player);
		}
		if (state.is(ModBlocks.MULTITOOL_ASSEMBLY)) return ModBlocks.MULTITOOL_ASSEMBLY.takeOneComponent(state, level, pos, player);
		if (state.is(ModBlocks.FLINT_TOOL_ASSEMBLY)) return ModBlocks.FLINT_TOOL_ASSEMBLY.takeOneComponent(state, level, pos, player);
		if (state.is(ModBlocks.WOVEN_BOWL_ASSEMBLY)) return ModBlocks.WOVEN_BOWL_ASSEMBLY.takeOneComponent(state, level, pos, player);
		if (state.is(ModBlocks.WOVEN_GEAR_ASSEMBLY)) return ModBlocks.WOVEN_GEAR_ASSEMBLY.takeOneComponent(state, level, pos, player);
		if (state.is(ModBlocks.ADVANCED_TOOL_ASSEMBLY)) return ModBlocks.ADVANCED_TOOL_ASSEMBLY.takeOne(state, level, pos, player);

		if (state.is(ModBlocks.DIRT_PILE)) {
			if (ModItems.isHammerLike(held) && state.getValue(DirtPileBlock.COUNT) == GameplayTuning.DIRT_PILE_COMPACTION_COUNT) {
				return ModBlocks.DIRT_PILE.hitWithHammer(state, level, pos, player, hand);
			}
			return ModBlocks.DIRT_PILE.takeOne(state, level, pos, player);
		}
		if (state.is(ModBlocks.FIRED_CLAY_PILE)) {
			if (ModItems.isHammerLike(held) && state.getValue(FiredClayPileBlock.COUNT) == GameplayTuning.FIRED_CLAY_PER_BLOCK) {
				return ModBlocks.FIRED_CLAY_PILE.hitWithHammer(state, level, pos, player, hand);
			}
			return ModBlocks.FIRED_CLAY_PILE.takeOne(state, level, pos, player);
		}
		if (state.is(ModBlocks.CLAY_FIRING_PILE)) {
			return ModBlocks.CLAY_FIRING_PILE.takeOneComponent(state, level, pos, player);
		}

		if (state.is(ModBlocks.PRIMITIVE_CAMPFIRE)) {
			if (state.getValue(PrimitiveCampfireBlock.LIT)) return InteractionResult.FAIL;
			return ModBlocks.PRIMITIVE_CAMPFIRE.takeOneComponent(state, level, pos, player);
		}
		if (state.is(ModBlocks.CRUDE_CLAY_KILN)) {
			if (state.getValue(CrudeClayKilnBlock.LIT)) return InteractionResult.FAIL;
			if (state.getValue(CrudeClayKilnBlock.BRANCHES) > 0) {
				return ModBlocks.CRUDE_CLAY_KILN.takeOneBranch(state, level, pos, player);
			}
			return InteractionResult.PASS;
		}
		if (state.is(ModBlocks.BAKED_CLAY_KILN) || state.is(ModBlocks.BAKED_KILN_TOP) || state.is(ModBlocks.CLAY_GRINDING_BASE) || state.is(ModBlocks.CLAY_GRINDER)) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		if (state.is(ModBlocks.KILN_TOP_FORM)) {
			return InteractionResult.FAIL;
		}

		if (state.is(ModBlocks.ROCK_PILE)) return ModBlocks.ROCK_PILE.takeOne(state, level, pos, player);
		if (state.is(ModBlocks.VERY_REFINED_STONE_PILE)) return ModBlocks.VERY_REFINED_STONE_PILE.takeOne(state, level, pos, player);
		if (state.is(ModBlocks.LOOSE_ROCK)) return gatherLooseRock(level, player, pos);
		if (state.is(ModBlocks.TWIG_PILE) && !state.getValue(TwigPileBlock.LIT)) return ModBlocks.TWIG_PILE.takeOneTwig(state, level, pos, player);

		if (state.is(Blocks.SWEET_BERRY_BUSH)) return ModItems.canHarvestSweetBerrySeeds(held) ? InteractionResult.PASS : InteractionResult.FAIL;
		if ((held.is(ModItems.FLINT_AXE) || ModItems.canHarvestGourdFlesh(held)) && (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON))) return InteractionResult.PASS;
		if (held.is(ModItems.FLINT_AXE) && (state.is(BlockTags.LOGS) || isVanillaPlankSlab(state) || isProcessedWood(state))) return InteractionResult.PASS;
		if (held.is(ModItems.FLINT_PLANT_CUTTERS) && (state.is(Blocks.SUGAR_CANE) || state.is(Blocks.SHORT_GRASS))) return InteractionResult.PASS;
		if (state.is(ModBlocks.CRUDE_CLAY_BLOCK) || state.is(ModBlocks.CRUDE_CLAY_KILN)) return InteractionResult.PASS;
		if (state.is(ModBlocks.LOOSE_DIRT)) {
			if (level.getBlockState(pos.below()).is(ModBlocks.BAKED_CLAY_KILN) && ModItems.isCutterLike(held)) {
				return InteractionResult.PASS;
			}
			if (ModItems.isMultitool(held)) {
				return InteractionResult.PASS;
			}
		}
		if (state.is(ModBlocks.LOOSE_DIRT_SLAB) && ModItems.isMultitool(held)) return InteractionResult.PASS;
		if (state.is(ModTags.GRASS_SCRAPE_TARGETS)) return InteractionResult.PASS;
		if (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) && held.isEmpty()) return InteractionResult.PASS;
		if (ToolCapabilities.canDirectHarvestPlant(held, state)) return InteractionResult.PASS;
		if (state.is(ModTags.FORAGE_BUSHES)
			&& (held.is(ModItems.REFINED_ROCK) || ModItems.isPlantCutterLike(held))) return InteractionResult.PASS;
		if (state.is(Blocks.LEAF_LITTER) && ModItems.isPlantCutterLike(held)) return InteractionResult.PASS;
		if (held.is(ModItems.CRUDE_STONE_HAMMER) && (state.is(Blocks.STONE) || state.is(Blocks.SUGAR_CANE))) return InteractionResult.PASS;
		if (ModItems.isPlantCutterLike(held)
			&& (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) || state.is(Blocks.TALL_GRASS) || state.is(BlockTags.FLOWERS) || state.is(Blocks.SUGAR_CANE))) return InteractionResult.PASS;
		if (state.is(BlockTags.LEAVES) && ToolCapabilities.canHarvestLeaves(held)) return InteractionResult.PASS;
		if (ModItems.isMultitool(held)
			&& (state.is(Blocks.STONE) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.LOOSE_SOIL) || state.is(Blocks.GRAVEL) || state.is(ModBlocks.LOOSE_DIRT))) {
			return InteractionResult.PASS;
		}

		return InteractionResult.FAIL;
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

		ItemStack held = player.getMainHandItem();
		BlockPos clickedPos = hit.getBlockPos();
		BlockState clicked = level.getBlockState(clickedPos);

		if (held.getItem() instanceof BlockItem) {
			BlockPlaceContext placement = new BlockPlaceContext(player, hand, held, hit);
			BlockPos target = placement.getClickedPos();
			BlockState below = level.getBlockState(target.below());

			// Project Flint placeables are never allowed to replace water. Dirt/Daub
			// piles retain their intentional water-processing interaction instead.
			if (isProjectFlintBlockItem(held) && level.getFluidState(target).is(FluidTags.WATER)) {
				if (held.is(ModItems.DIRT_PILE) || held.is(ModItems.DAUB_PILE)) {
					return processDaubWaterInteraction(player, level, held, target);
				}
				return InteractionResult.FAIL;
			}

			boolean heldWall = held.is(ModItems.WATTLED_DAUB_BRICKS) || held.is(ModItems.WATTLED_DAUB_WINDOW);
			boolean heldFloor = held.is(ModItems.WATTLED_DAUB_FLOOR_TILE);
			boolean heldDoor = held.is(ModItems.WATTLED_DAUB_DOOR);

			// Floor tiles are usable flooring for beds/workstations/etc., but structural
			// wall pieces and more floor tiles cannot be stacked on them. The door is
			// the explicit exception and MUST use a floor tile as its threshold.
			if (below.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE) && (heldWall || heldFloor)) return InteractionResult.FAIL;
			if (heldDoor && !below.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)) return InteractionResult.FAIL;

			// Bricks/windows remain structural-only surfaces: only another wall piece
			// can be stacked directly on top. A door's upper half similarly supports
			// Wattled Daub wall pieces, but not arbitrary spacer blocks.
			if ((below.is(ModBlocks.WATTLED_DAUB_BRICKS) || below.is(ModBlocks.WATTLED_DAUB_WINDOW)) && !heldWall) {
				return InteractionResult.FAIL;
			}
			if (below.is(ModBlocks.WATTLED_DAUB_DOOR) && !heldWall) return InteractionResult.FAIL;
		}

		if (held.is(ModItems.MUD_PILE) && clicked.is(BlockTags.LEAVES)) {
			if (!level.isClientSide()) {
				level.removeBlock(clickedPos, false);
				if (!player.isCreative()) held.shrink(1);
				giveOrDrop(player, new ItemStack(ModItems.DAUB_PILE));
				level.playSound(null, clickedPos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.65F, 0.82F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "daub_pile");
			}
			return InteractionResult.SUCCESS;
		}

		// Vanilla Sweet Berries are food only in Project Flint. They must never act as
		// a Sweet Berry Bush BlockItem; only projectflint:sweet_berry_seeds can establish bushes.
		if (held.is(Items.SWEET_BERRIES) && wouldPlantVanillaSweetBerryBush(level, hit, clicked)) {
			// Redirect the would-be BlockItem placement into normal item use so berries remain
			// edible even while the crosshair is on ground where vanilla would plant a bush.
			return held.use(level, player, hand);
		}

		if (held.is(ModItems.SANDING_PAPER) && isVanillaPlankSlab(clicked)) {
			int paperCost = clicked.hasProperty(SlabBlock.TYPE) && clicked.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			if (!player.isCreative() && held.getCount() < paperCost) return InteractionResult.FAIL;
			if (!level.isClientSide()) {
				BlockState sanded = ModBlocks.SANDED_WOOD_SLAB.defaultBlockState();
				if (clicked.hasProperty(SlabBlock.TYPE)) sanded = sanded.setValue(SlabBlock.TYPE, clicked.getValue(SlabBlock.TYPE));
				level.setBlockAndUpdate(clickedPos, sanded);
				if (!player.isCreative()) held.shrink(paperCost);
				level.playSound(null, clickedPos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.55F, 0.88F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "sanded_wood_slab");
			}
			return InteractionResult.SUCCESS;
		}

		if (held.is(ModItems.WOVEN_FAN) && clicked.is(ModBlocks.GRINDED_WOOD_SLAB)) {
			int fanCost = clicked.hasProperty(SlabBlock.TYPE) && clicked.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1;
			if (!player.isCreative() && held.getCount() < fanCost) return InteractionResult.FAIL;
			if (!level.isClientSide()) {
				BlockState woven = ModBlocks.WOVEN_WOOD_SLAB.defaultBlockState();
				if (clicked.hasProperty(SlabBlock.TYPE)) woven = woven.setValue(SlabBlock.TYPE, clicked.getValue(SlabBlock.TYPE));
				level.setBlockAndUpdate(clickedPos, woven);
				if (!player.isCreative()) held.shrink(fanCost);
				level.playSound(null, clickedPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.58F, 0.94F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "woven_wood_slab");
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(ModBlocks.MORTAR_KILN_BASE) && held.is(ModItems.ADVANCED_KILN_GLASS)) {
			if (!level.isClientSide()) {
				BlockState kiln = ModBlocks.MORTAR_KILN.defaultBlockState()
					.setValue(dev.projectflint.block.MortarKilnBlock.FACING, player.getDirection().getOpposite());
				level.setBlockAndUpdate(clickedPos, kiln);
				if (!player.isCreative()) held.shrink(1);
				level.playSound(null, clickedPos, SoundEvents.DECORATED_POT_PLACE, SoundSource.BLOCKS, 0.75F, 0.75F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "mortar_kiln");
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(ModBlocks.WOODEN_GRINDER_BASE) && held.is(ModItems.STONE_GRINDSAW)) {
			if (!level.isClientSide()) {
				level.setBlockAndUpdate(clickedPos, ModBlocks.WOODEN_GRINDER.defaultBlockState()
					.setValue(HorizontalDirectionalBlock.FACING, player.getDirection().getOpposite()));
				if (!player.isCreative()) held.shrink(1);
				level.playSound(null, clickedPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.72F, 0.78F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "wooden_grinder");
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(ModBlocks.MORTAR_CHEST) && held.is(ModItems.WOVEN_CHEST_FRAME)) {
			if (!level.isClientSide()) {
				ItemStack[] saved = new ItemStack[MortarChestBlockEntity.SLOT_COUNT];
				for (int i = 0; i < saved.length; i++) saved[i] = ItemStack.EMPTY;
				if (level.getBlockEntity(clickedPos) instanceof MortarChestBlockEntity chest) {
					for (int i = 0; i < saved.length; i++) saved[i] = chest.getItem(i).copy();
					// The upgrade replaces the block entity in-place. Empty the old container first so
					// replacement/removal hooks cannot also spill the same stacks into the world.
					chest.clearContent();
					chest.setChanged();
				}
				Direction facing = clicked.hasProperty(HorizontalDirectionalBlock.FACING)
					? clicked.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
				level.setBlockAndUpdate(clickedPos, ModBlocks.WOVEN_CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing));
				if (level.getBlockEntity(clickedPos) instanceof WovenChestBlockEntity woven) {
					for (int i = 0; i < saved.length; i++) woven.setItem(i, saved[i]);
					woven.setChanged();
				}
				if (!player.isCreative()) held.shrink(1);
				level.playSound(null, clickedPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.72F, 0.9F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "woven_chest");
			}
			return InteractionResult.SUCCESS;
		}

		if (player.isCreative()) return InteractionResult.PASS;

		if (clicked.is(ModBlocks.LOOSE_DIRT) && ToolCapabilities.has(held, ToolCapabilities.Capability.MULTITOOL)) {
			if (!level.isClientSide()) {
				level.setBlockAndUpdate(clickedPos, ModBlocks.LOOSE_DIRT_SLAB.defaultBlockState());
				Block.popResource(level, clickedPos, new ItemStack(ModItems.LOOSE_DIRT_SLAB));
				held.hurtAndBreak(1, player, hand);
				level.playSound(null, clickedPos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.72F, 0.86F);
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(Blocks.SWEET_BERRY_BUSH)) {
			if (!ModItems.isPlantCutterLike(held)) return InteractionResult.FAIL;
			int age = clicked.getValue(SweetBerryBushBlock.AGE);
			if (age <= 1) return InteractionResult.FAIL;
			if (!level.isClientSide()) {
				int berries = 1 + level.getRandom().nextInt(2);
				level.setBlockAndUpdate(clickedPos, clicked.setValue(SweetBerryBushBlock.AGE, 1));
				Block.popResource(level, clickedPos, new ItemStack(Items.SWEET_BERRIES, berries));
				held.hurtAndBreak(1, player, hand);
				level.playSound(null, clickedPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 0.8F, 0.95F);
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(Blocks.CLAY) && held.is(ModItems.CRUDELY_WOVEN_BOWL)) {
			if (!level.isClientSide()) {
				level.removeBlock(clickedPos, false);
				held.shrink(1);
				giveOrDrop(player, new ItemStack(ModItems.UNFIRED_WOVEN_BOWL));
				level.playSound(null, clickedPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.65F, 0.72F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "unfired_woven_bowl", "clay_lined_bowl");
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(ModBlocks.MORTAR_BRICKS) && held.is(ModItems.WOVEN_FAN)) {
			if (!level.isClientSide()) {
				level.setBlockAndUpdate(clickedPos, ModBlocks.MORTAR_CHEST.defaultBlockState());
				held.consume(1, player);
				level.playSound(null, clickedPos, SoundEvents.DECORATED_POT_PLACE, SoundSource.BLOCKS, 0.75F, 0.82F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "mortar_chest", "formed_mortar_chest");
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(Blocks.CLAY) && isClayBowl(held)) {
			if (!level.isClientSide()) {
				level.removeBlock(clickedPos, false);
				Item next = nextClayBowl(held);
				held.shrink(1);
				if (next == null) {
					giveOrDrop(player, new ItemStack(ModItems.FIRED_CLAY_BOWL));
					giveOrDrop(player, new ItemStack(ModItems.UNFIRED_CLAY_BALL));
					if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "unfired_clay_ball", "filled_clay_bowl");
				} else {
					giveOrDrop(player, new ItemStack(next));
				}
				level.playSound(null, clickedPos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.55F, 0.82F);
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(Blocks.CLAY) && isClayPot(held)) {
			if (!level.isClientSide()) {
				level.removeBlock(clickedPos, false);
				boolean completesPot = held.is(ModItems.CLAY_POT_2);
				held.shrink(1);
				giveOrDrop(player, new ItemStack(completesPot ? ModItems.CLAY_POT : ModItems.CLAY_POT_2));
				if (completesPot) {
					giveOrDrop(player, new ItemStack(ModItems.UNFIRED_CLAY_BALL));
					if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "clay_pot");
				}
				level.playSound(null, clickedPos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.6F, 0.78F);
			}
			return InteractionResult.SUCCESS;
		}

		if (clicked.is(ModBlocks.CRUDE_CLAY_BLOCK) && ModItems.isCutterLike(held)) {
			if (!level.isClientSide()) {
				held.hurtAndBreak(1, player, hand);
				level.setBlockAndUpdate(clickedPos, ModBlocks.CRUDE_CLAY_KILN.defaultBlockState()
					.setValue(CrudeClayKilnBlock.FACING, player.getDirection().getOpposite()));
				level.playSound(null, clickedPos, SoundEvents.DECORATED_POT_PLACE, SoundSource.BLOCKS, 0.7F, 0.72F);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "crude_clay_kiln", "cut_crude_kiln");
			}
			return InteractionResult.SUCCESS;
		}

		if (!player.isShiftKeyDown()) return InteractionResult.PASS;
		if (hit.getDirection() != Direction.UP) return InteractionResult.PASS;

		boolean startsCrudeMultitool = held.is(ModItems.BURNT_TWIG) || held.is(ModItems.PLANT_TOOL_BINDING) || held.is(ModItems.VERY_REFINED_STONE);
		boolean startsFlintAssembly = held.is(ModItems.BRANCH) || held.is(ModItems.BURNT_BRANCH) || held.is(Items.FLINT);
		boolean startsBowl = held.is(ModItems.DIRT_PILE);
		boolean startsAdvancedTool = held.is(ModItems.FLINT_AXE_PIECES) || held.is(ModItems.FLINT_CUTTER_PIECE) || held.is(ModItems.REFINED_FLINT_MULTITOOL_PIECE);
		// A binding already starts the crude-multitool assembly. Woven gear starts
		// with a visually distinctive fan or filed bone, then accepts bindings.
		boolean startsWovenGear = held.is(ModItems.WOVEN_FAN) || held.is(ModItems.FILED_BONE);
		if (!startsCrudeMultitool && !startsFlintAssembly && !startsBowl && !startsWovenGear && !startsAdvancedTool) return InteractionResult.PASS;

		BlockPos supportPos = clickedPos;
		BlockState support = clicked;
		if (isPrimitiveGroundObject(support) || !support.isFaceSturdy(level, supportPos, Direction.UP)) return InteractionResult.PASS;
		BlockPos placePos = supportPos.above();
		if (!level.getBlockState(placePos).isAir()) return InteractionResult.PASS;

		if (!level.isClientSide()) {
			BlockState placed;
			if (startsFlintAssembly) {
				placed = ModBlocks.FLINT_TOOL_ASSEMBLY.stateForFirstIngredient(held);
			} else if (startsAdvancedTool) {
				placed = ModBlocks.ADVANCED_TOOL_ASSEMBLY.stateForPiece(held);
			} else if (startsBowl) {
				placed = ModBlocks.WOVEN_BOWL_ASSEMBLY.stateForFirstDirtPile();
			} else if (startsWovenGear) {
				placed = ModBlocks.WOVEN_GEAR_ASSEMBLY.stateForFirstIngredient(held);
			} else {
				placed = ModBlocks.MULTITOOL_ASSEMBLY.stateForFirstIngredient(held);
			}
			level.setBlockAndUpdate(placePos, placed);
			held.consume(1, player);
			level.playSound(null, placePos, startsBowl ? SoundEvents.ROOTED_DIRT_PLACE : SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.86F);
		}
		return InteractionResult.SUCCESS;
	}

	private static boolean isProjectFlintBlockItem(ItemStack stack) {
		return stack.getItem() instanceof BlockItem
			&& "projectflint".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
	}

	private static InteractionResult processDaubWaterInteraction(Player player, Level level, ItemStack stack, BlockPos waterPos) {
		if (!stack.is(ModItems.DIRT_PILE) && !stack.is(ModItems.DAUB_PILE)) return InteractionResult.PASS;
		if (!level.isClientSide()) {
			ItemStack result = stack.is(ModItems.DIRT_PILE) ? new ItemStack(ModItems.MUD_PILE) : new ItemStack(ModItems.WET_DAUB_PILE);
			if (!player.isCreative()) stack.shrink(1);
			giveOrDrop(player, result);
			level.playSound(null, waterPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.58F, 0.82F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.awardAll(serverPlayer, result.is(ModItems.MUD_PILE) ? "mud_pile" : "wet_daub_pile");
			}
		}
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();

		if (main.is(ModItems.DIRT_PILE) || main.is(ModItems.DAUB_PILE)) {
			var waterHit = player.pick(5.0D, 0.0F, true);
			if (waterHit instanceof BlockHitResult waterBlockHit
				&& level.getFluidState(waterBlockHit.getBlockPos()).is(FluidTags.WATER)) {
				if (!level.isClientSide()) {
					ItemStack result = main.is(ModItems.DIRT_PILE)
						? new ItemStack(ModItems.MUD_PILE)
						: new ItemStack(ModItems.WET_DAUB_PILE);
					if (!player.isCreative()) main.shrink(1);
					giveOrDrop(player, result);
					level.playSound(null, waterBlockHit.getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.58F, 0.82F);
					if (player instanceof ServerPlayer serverPlayer) {
						ModAdvancements.awardAll(serverPlayer, result.is(ModItems.MUD_PILE) ? "mud_pile" : "wet_daub_pile");
					}
				}
				return InteractionResult.SUCCESS;
			}
		}

		if (main.is(ModItems.GROUND_LEATHER)) {
			var waterHit = player.pick(5.0D, 0.0F, true);
			if (waterHit instanceof BlockHitResult waterBlockHit
				&& level.getFluidState(waterBlockHit.getBlockPos()).is(FluidTags.WATER)) {
				if (!level.isClientSide()) {
					if (!player.isCreative()) main.shrink(1);
					giveOrDrop(player, new ItemStack(Items.LEATHER));
					level.playSound(null, waterBlockHit.getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.55F, 0.9F);
					if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "ground_leather");
				}
				return InteractionResult.SUCCESS;
			}
		}

		if (main.is(ModItems.REED_ROLL)) {
			var waterHit = player.pick(5.0D, 0.0F, true);
			if (waterHit instanceof BlockHitResult waterBlockHit
				&& level.getFluidState(waterBlockHit.getBlockPos()).is(FluidTags.WATER)) {
				if (!level.isClientSide()) {
					if (!player.isCreative()) main.shrink(1);
					giveOrDrop(player, new ItemStack(ModItems.SUGARCANE_ROLL));
					level.playSound(null, waterBlockHit.getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.55F, 1.12F);
				}
				return InteractionResult.SUCCESS;
			}
		}

		if (main.is(ModItems.CLAY_MORTAR)) {
			var waterHit = player.pick(5.0D, 0.0F, true);
			if (waterHit instanceof BlockHitResult waterBlockHit
				&& level.getFluidState(waterBlockHit.getBlockPos()).is(FluidTags.WATER)) {
				if (!level.isClientSide()) {
					if (!player.isCreative()) main.shrink(1);
					giveOrDrop(player, new ItemStack(ModItems.UNFIRED_MORTAR_BRICK, 4));
					level.playSound(null, waterBlockHit.getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.55F, 0.92F);
					if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "unfired_mortar_bricks", "mixed_unfired_mortar_bricks");
				}
				return InteractionResult.SUCCESS;
			}
		}

		StoneWorkingSystem.Process process;
		if (main.is(ModItems.CRUDE_REED) && main.getCount() >= 12 && off.is(ModItems.TWIG) && off.getCount() >= 12) {
			process = StoneWorkingSystem.Process.WATTLE_WEAVING;
		} else if (main.is(ModItems.WATTLE) && off.is(ModItems.CRUDE_PLANT_STRING)) {
			process = StoneWorkingSystem.Process.WATTLE_TYING;
		} else if (main.is(ModItems.TIED_WATTLE) && main.getCount() >= 4 && off.is(ModItems.WET_DAUB_PILE) && off.getCount() >= 2) {
			process = StoneWorkingSystem.Process.WATTLE_DAUB_ASSEMBLY;
		} else if (main.is(ModItems.VERY_REFINED_STONE) && off.is(ModItems.VERY_REFINED_STONE)) {
			process = StoneWorkingSystem.Process.CAMPFIRE_STONE;
		} else if (main.is(Items.SUGAR_CANE) && ModItems.isCutterLike(off)) {
			process = StoneWorkingSystem.Process.REED_CUTTING;
		} else if (main.is(ModItems.BURNT_TWIG) && ModItems.isHammerLike(off)) {
			process = StoneWorkingSystem.Process.BURNT_TWIG_SPLITTING;
		} else if (main.is(ModItems.FLINT_CHIP) && main.getCount() >= GameplayTuning.FLINT_CHIPS_PER_FLINT && ModItems.isHammerLike(off)) {
			process = StoneWorkingSystem.Process.FLINT_KNAPPING;
		} else if (main.is(ModItems.CRUDE_REED) && main.getCount() >= 4 && ModItems.isMultitool(off)) {
			process = StoneWorkingSystem.Process.REED_ROLL;
		} else if (main.is(ItemTags.FLOWERS) && off.is(ModItems.SANDING_PAPER) && !flowerDye(main).isEmpty()) {
			process = StoneWorkingSystem.Process.FLOWER_SANDING;
		} else if ((main.is(ModItems.ANIMAL_BONES) || main.is(Items.BONE)) && off.is(ModItems.SANDING_PAPER)) {
			process = StoneWorkingSystem.Process.BONE_SANDING;
		} else if (main.is(ModItems.FILED_BONE) && off.is(Items.FLINT)) {
			process = StoneWorkingSystem.Process.BONE_FIRESTARTER;
		} else if (main.is(ModItems.WOOL) && main.getCount() >= 2 && off.is(ModItems.SANDING_PAPER)) {
			process = StoneWorkingSystem.Process.WOOL_SANDING;
		} else if (main.is(Items.INK_SAC) && off.is(ModItems.SANDING_PAPER)) {
			process = StoneWorkingSystem.Process.INK_SANDING;
		} else if (main.is(ModItems.PLANT_TOOL_BINDING) && off.is(ModItems.SANDING_PAPER)) {
			process = StoneWorkingSystem.Process.WOVEN_FAN_SANDING;
		} else if (main.is(ModItems.SAND_FLAKE) && main.getCount() >= 4 && ModItems.isMultitool(off)) {
			process = StoneWorkingSystem.Process.SAND_BALL;
		} else if (main.is(ItemTags.FLOWERS) && ModItems.isMultitool(off)) {
			process = StoneWorkingSystem.Process.FLOWER_KNAPPING;
		} else {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) return InteractionResult.SUCCESS;
		StoneWorkingSystem.StoneWorkingResult result = StoneWorkingSystem.strike(level, player, process);
		if (result != StoneWorkingSystem.StoneWorkingResult.SUCCESS) return InteractionResult.SUCCESS;

		switch (process) {
			case WATTLE_WEAVING -> {
				if (!player.isCreative()) {
					main.shrink(12);
					off.shrink(12);
				}
				giveOrDrop(player, new ItemStack(ModItems.WATTLE, 4));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "wattle");
			}
			case WATTLE_TYING -> {
				if (!player.isCreative()) {
					main.shrink(1);
					off.shrink(1);
				}
				giveOrDrop(player, new ItemStack(ModItems.TIED_WATTLE));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "tied_wattle");
			}
			case WATTLE_DAUB_ASSEMBLY -> {
				if (!player.isCreative()) {
					main.shrink(4);
					off.shrink(2);
				}
				giveOrDrop(player, new ItemStack(ModItems.UNFIRED_WATTLE_AND_DAUB, 2));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.awardAll(serverPlayer, "unfired_wattle_and_daub");
			}
			case CAMPFIRE_STONE -> {
				if (!player.isCreative()) main.shrink(1);
				giveOrDrop(player, new ItemStack(ModItems.CAMPFIRE_STONE));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "campfire_stone", "knapped_campfire_stone");
			}
			case REED_CUTTING -> {
				int reeds = off.is(ModItems.ADVANCED_FLINT_MULTITOOL)
					? GameplayTuning.ADVANCED_FLINT_MULTITOOL_REEDS_PER_CANE
					: off.is(ModItems.FLINT_MULTITOOL)
						? GameplayTuning.FLINT_MULTITOOL_REEDS_PER_CANE
					: off.is(ModItems.CRUDE_MULTITOOL)
						? GameplayTuning.CRUDE_MULTITOOL_REEDS_PER_CANE
						: GameplayTuning.CUTTERS_REEDS_PER_CANE;
				if (!player.isCreative()) {
					main.shrink(1);
					off.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
				}
				giveOrDrop(player, new ItemStack(ModItems.CRUDE_REED, reeds));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "crude_reeds", "harvested_crude_reeds");
			}
			case BURNT_TWIG_SPLITTING -> {
				if (!player.isCreative()) {
					main.shrink(1);
					off.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
				}
				giveOrDrop(player, new ItemStack(ModItems.LIGHTLY_BURNT_TWIG, 2));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "lightly_burnt_twig", "split_burnt_twig");
			}
			case FLINT_KNAPPING -> {
				if (!player.isCreative()) {
					main.shrink(GameplayTuning.FLINT_CHIPS_PER_FLINT);
					off.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
				}
				giveOrDrop(player, new ItemStack(Items.FLINT));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "vanilla_flint", "knapped_flint");
			}
			case REED_ROLL -> {
				if (!player.isCreative()) {
					main.shrink(4);
					off.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
				}
				giveOrDrop(player, new ItemStack(ModItems.REED_ROLL));
			}
			case FLOWER_KNAPPING -> {
				int petals = off.is(ModItems.ADVANCED_FLINT_MULTITOOL)
					? GameplayTuning.ADVANCED_FLINT_MULTITOOL_FLOWER_PETALS
					: off.is(ModItems.FLINT_MULTITOOL)
						? GameplayTuning.FLINT_MULTITOOL_FLOWER_PETALS
						: GameplayTuning.CRUDE_MULTITOOL_FLOWER_PETALS;
				if (isTallFlowerItem(main)) petals *= GameplayTuning.TALL_FLOWER_MULTIPLIER;
				if (!player.isCreative()) {
					main.shrink(1);
					off.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
				}
				giveOrDrop(player, new ItemStack(ModItems.FLOWER_PETALS, petals));
			}
			case FLOWER_SANDING -> {
				ItemStack dye = flowerDye(main);
				if (!player.isCreative()) main.shrink(1);
				giveOrDrop(player, dye);
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "flower_dyes", "filed_flower_dye");
			}
			case BONE_SANDING -> {
				if (!player.isCreative()) main.shrink(1);
				giveOrDrop(player, new ItemStack(ModItems.FILED_BONE));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "filed_bone", "filed_bone");
			}
			case BONE_FIRESTARTER -> {
				if (!player.isCreative()) {
					main.shrink(1);
					off.shrink(1);
				}
				giveOrDrop(player, new ItemStack(ModItems.BONE_FIRESTARTER));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "bone_firestarter", "assembled_bone_firestarter");
			}
			case WOOL_SANDING -> {
				if (!player.isCreative()) main.shrink(2);
				giveOrDrop(player, new ItemStack(ModItems.WOVEN_WOOL));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "woven_wool", "woven_wool");
			}
			case INK_SANDING -> {
				if (!player.isCreative()) main.shrink(1);
				giveOrDrop(player, new ItemStack(Items.BLACK_DYE));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "black_dye", "filed_ink_sac");
			}
			case WOVEN_FAN_SANDING -> {
				if (!player.isCreative()) main.shrink(1);
				giveOrDrop(player, new ItemStack(ModItems.WOVEN_FAN));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "woven_fan", "woven_fan");
			}
			case SAND_BALL -> {
				if (!player.isCreative()) {
					main.shrink(4);
					off.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
				}
				giveOrDrop(player, new ItemStack(ModItems.SAND_BALL));
				if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "sand_ball", "formed_sand_ball");
			}
			default -> {}
		}
		return InteractionResult.SUCCESS;
	}



	private static boolean beforeBlockBreak(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
		if (player.isCreative()) return true;
		if (player.isSpectator()) return false;

		ItemStack held = player.getMainHandItem();
		if (state.is(ModBlocks.SLEEPING_BAG)) return true;
		if (state.is(ModBlocks.WATTLED_DAUB_BRICKS)
			|| state.is(ModBlocks.WATTLED_DAUB_WINDOW)
			|| state.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)
			|| state.is(ModBlocks.WATTLED_DAUB_DOOR)
			|| state.is(ModBlocks.FLINT_TORCH)) return true;
		if ((held.is(ModItems.FLINT_AXE) || ModItems.canHarvestGourdFlesh(held)) && (state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON))) return true;
		if (held.is(ModItems.FLINT_AXE) && (state.is(BlockTags.LOGS) || isVanillaPlankSlab(state) || isProcessedWood(state))) return true;
		if (held.is(ModItems.FLINT_PLANT_CUTTERS) && (state.is(Blocks.SUGAR_CANE) || state.is(Blocks.SHORT_GRASS))) return true;
		if (state.is(ModBlocks.MORTAR_BRICKS) && ModItems.isHammerLike(held)) return true;
		if ((state.is(ModBlocks.MORTAR_BRICK_SLAB) || state.is(ModBlocks.CRUDE_CLAY_SLAB))
			&& ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER)) return true;
		if (state.is(ModBlocks.MORTAR_CHEST)) return ModItems.isFlintTier(held);
		if (state.is(ModBlocks.WOVEN_CHEST)) return held.is(ModItems.FLINT_AXE);
		if (state.is(ModBlocks.MORTAR_KILN) || state.is(ModBlocks.WOODEN_GRINDER)) return ToolCapabilities.has(held, ToolCapabilities.Capability.ADVANCED_FLINT_TIER);
		if (state.is(ModBlocks.MORTAR_KILN_BASE) || state.is(ModBlocks.WOODEN_GRINDER_BASE)) return ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER);
		if (state.is(ModBlocks.BURNT_BRANCH_BLOCK) || state.is(ModBlocks.BURNT_BRANCH_SLAB)) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.FLINT_TIER) || held.is(ModItems.FLINT_AXE);
		}
		if (state.is(ModBlocks.ADVANCED_KILN_CONTROLLER) || state.is(ModBlocks.ADVANCED_KILN_CASING)) {
			return ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE);
		}
		if (state.is(Blocks.SWEET_BERRY_BUSH)) return ModItems.canHarvestSweetBerrySeeds(held);
		if (state.is(ModBlocks.CRUDE_CLAY_BLOCK) || state.is(ModBlocks.CRUDE_CLAY_KILN)) return true;
		if ((state.is(ModBlocks.BAKED_CLAY_KILN) || state.is(ModBlocks.BAKED_KILN_TOP) || state.is(ModBlocks.CLAY_GRINDING_BASE) || state.is(ModBlocks.CLAY_GRINDER))
			&& ToolCapabilities.has(held, ToolCapabilities.Capability.WORKSTATION_DISMANTLE)) return true;
		if (state.is(ModBlocks.LOOSE_DIRT)) {
			if (level.getBlockState(pos.below()).is(ModBlocks.BAKED_CLAY_KILN) && ModItems.isCutterLike(held)) return true;
			if (ModItems.isMultitool(held)) return true;
		}
		if (state.is(ModTags.GRASS_SCRAPE_TARGETS)) return true;
		if (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) && held.isEmpty()) return true;
		if (ToolCapabilities.canDirectHarvestPlant(held, state)) return true;
		if (state.is(ModTags.FORAGE_BUSHES) && (held.is(ModItems.REFINED_ROCK) || ModItems.isPlantCutterLike(held))) return true;
		if (state.is(Blocks.LEAF_LITTER) && ModItems.isPlantCutterLike(held)) return true;
		if (held.is(ModItems.CRUDE_STONE_HAMMER) && (state.is(Blocks.STONE) || state.is(Blocks.SUGAR_CANE))) return true;
		if (ModItems.isPlantCutterLike(held)
			&& (state.is(ModTags.SLOW_HAND_CLEAR_TARGETS) || state.is(Blocks.TALL_GRASS) || state.is(BlockTags.FLOWERS) || state.is(Blocks.SUGAR_CANE))) return true;
		if (state.is(BlockTags.LEAVES) && ToolCapabilities.canHarvestLeaves(held)) return true;
		return ModItems.isMultitool(held)
			&& (state.is(Blocks.STONE) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.LOOSE_SOIL) || state.is(Blocks.GRAVEL) || state.is(ModBlocks.LOOSE_DIRT));
	}

	private static boolean wouldPlantVanillaSweetBerryBush(Level level, BlockHitResult hit, BlockState clicked) {
		BlockPos clickedPos = hit.getBlockPos();
		BlockPos plantPos = clicked.canBeReplaced() ? clickedPos : clickedPos.relative(hit.getDirection());
		BlockState plantState = level.getBlockState(plantPos);
		BlockState bush = Blocks.SWEET_BERRY_BUSH.defaultBlockState();
		return plantState.canBeReplaced() && bush.canSurvive(level, plantPos);
	}

	private static boolean isVanillaPlankSlab(BlockState state) {
		return state.is(Blocks.OAK_SLAB) || state.is(Blocks.SPRUCE_SLAB) || state.is(Blocks.BIRCH_SLAB)
			|| state.is(Blocks.JUNGLE_SLAB) || state.is(Blocks.ACACIA_SLAB) || state.is(Blocks.DARK_OAK_SLAB)
			|| state.is(Blocks.MANGROVE_SLAB) || state.is(Blocks.CHERRY_SLAB) || state.is(Blocks.PALE_OAK_SLAB)
			|| state.is(Blocks.BAMBOO_SLAB) || state.is(Blocks.BAMBOO_MOSAIC_SLAB)
			|| state.is(Blocks.CRIMSON_SLAB) || state.is(Blocks.WARPED_SLAB);
	}

	private static boolean isProcessedWood(BlockState state) {
		return state.is(ModBlocks.SANDED_WOOD_SLAB) || state.is(ModBlocks.GRINDED_WOOD_SLAB)
			|| state.is(ModBlocks.WOVEN_WOOD_SLAB) || state.is(ModBlocks.WOVEN_WOOD);
	}

	private static boolean isTallFlowerItem(ItemStack stack) {
		return stack.is(Items.SUNFLOWER) || stack.is(Items.LILAC) || stack.is(Items.ROSE_BUSH) || stack.is(Items.PEONY);
	}

	private static ItemStack flowerDye(ItemStack flower) {
		if (flower.is(Items.DANDELION) || flower.is(Items.WILDFLOWERS)) return new ItemStack(Items.YELLOW_DYE);
		if (flower.is(Items.SUNFLOWER)) return new ItemStack(Items.YELLOW_DYE, 2);
		if (flower.is(Items.POPPY) || flower.is(Items.RED_TULIP)) return new ItemStack(Items.RED_DYE);
		if (flower.is(Items.ROSE_BUSH)) return new ItemStack(Items.RED_DYE, 2);
		if (flower.is(Items.BLUE_ORCHID)) return new ItemStack(Items.LIGHT_BLUE_DYE);
		if (flower.is(Items.ALLIUM)) return new ItemStack(Items.MAGENTA_DYE);
		if (flower.is(Items.LILAC)) return new ItemStack(Items.MAGENTA_DYE, 2);
		if (flower.is(Items.AZURE_BLUET) || flower.is(Items.WHITE_TULIP) || flower.is(Items.OXEYE_DAISY)) return new ItemStack(Items.LIGHT_GRAY_DYE);
		if (flower.is(Items.ORANGE_TULIP) || flower.is(Items.TORCHFLOWER) || flower.is(Items.OPEN_EYEBLOSSOM)) return new ItemStack(Items.ORANGE_DYE);
		if (flower.is(Items.PINK_TULIP) || flower.is(Items.PINK_PETALS) || flower.is(Items.CACTUS_FLOWER)) return new ItemStack(Items.PINK_DYE);
		if (flower.is(Items.PEONY)) return new ItemStack(Items.PINK_DYE, 2);
		if (flower.is(Items.CORNFLOWER)) return new ItemStack(Items.BLUE_DYE);
		if (flower.is(Items.LILY_OF_THE_VALLEY)) return new ItemStack(Items.WHITE_DYE);
		if (flower.is(Items.WITHER_ROSE)) return new ItemStack(Items.BLACK_DYE);
		if (flower.is(Items.CLOSED_EYEBLOSSOM)) return new ItemStack(Items.GRAY_DYE);
		if (flower.is(Items.PITCHER_PLANT)) return new ItemStack(Items.CYAN_DYE, 2);
		return ItemStack.EMPTY;
	}

	private static boolean isClayBowl(ItemStack stack) {
		return stack.is(ModItems.FIRED_CLAY_BOWL) || stack.is(ModItems.FIRED_CLAY_BOWL_2)
			|| stack.is(ModItems.FIRED_CLAY_BOWL_4) || stack.is(ModItems.FIRED_CLAY_BOWL_6);
	}

	private static boolean isClayPot(ItemStack stack) {
		return stack.is(ModItems.CLAY_POT) || stack.is(ModItems.CLAY_POT_2);
	}

	private static Item nextClayBowl(ItemStack stack) {
		if (stack.is(ModItems.FIRED_CLAY_BOWL)) return ModItems.FIRED_CLAY_BOWL_2;
		if (stack.is(ModItems.FIRED_CLAY_BOWL_2)) return ModItems.FIRED_CLAY_BOWL_4;
		if (stack.is(ModItems.FIRED_CLAY_BOWL_4)) return ModItems.FIRED_CLAY_BOWL_6;
		return null;
	}

	private static InteractionResult gatherLooseRock(Level level, Player player, BlockPos pos) {
		if (!level.isClientSide()) {
			ItemStack rock = new ItemStack(ModItems.ROCK);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, rock)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, rock);
			level.removeBlock(pos, false);
			level.playSound(null, pos, SoundEvents.STONE_STEP, SoundSource.BLOCKS, 0.75F, 1.18F);
		}
		return InteractionResult.SUCCESS;
	}

	private static boolean isPrimitiveGroundObject(BlockState state) {
		return state.is(ModBlocks.TWIG_PILE)
			|| state.is(ModBlocks.FIBER_WORK_PILE)
			|| state.is(ModBlocks.HAMMER_ASSEMBLY)
			|| state.is(ModBlocks.BINDING_ASSEMBLY)
			|| state.is(ModBlocks.HARVESTING_TOOL_ASSEMBLY)
			|| state.is(ModBlocks.MULTITOOL_ASSEMBLY)
			|| state.is(ModBlocks.FLINT_TOOL_ASSEMBLY)
			|| state.is(ModBlocks.WOVEN_BOWL_ASSEMBLY)
			|| state.is(ModBlocks.WOVEN_GEAR_ASSEMBLY)
			|| state.is(ModBlocks.ADVANCED_TOOL_ASSEMBLY)
			|| state.is(ModBlocks.PRIMITIVE_CAMPFIRE)
			|| state.is(ModBlocks.DIRT_PILE)
			|| state.is(ModBlocks.CLAY_FIRING_PILE)
			|| state.is(ModBlocks.FIRED_CLAY_PILE)
			|| state.is(ModBlocks.VERY_REFINED_STONE_PILE);
	}

	public static void giveOrDrop(Player player, ItemStack stack) {
		if (player.isCreative()) {
			if (!player.getInventory().add(stack)) player.drop(stack, false);
			return;
		}
		if (!PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, stack)) player.drop(stack, false);
	}
}
