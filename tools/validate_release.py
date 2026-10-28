from pathlib import Path
import json
import re
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
EXPECTED_VERSION = "0.31.48-b"
errors: list[str] = []


def fail(message: str) -> None:
	errors.append(message)


def read(path: str) -> str:
	p = ROOT / path
	if not p.exists():
		fail(f"missing required file: {path}")
		return ""
	return p.read_text(encoding="utf-8")


props = read("gradle.properties")
version = next((line.split("=", 1)[1].strip() for line in props.splitlines() if line.startswith("mod_version=")), None)
if version != EXPECTED_VERSION:
	fail(f"expected mod_version={EXPECTED_VERSION}, got {version!r}")

release = ROOT / "docs/releases" / f"{EXPECTED_VERSION}.md"
if not release.exists():
	fail(f"missing release document: {release.relative_to(ROOT)}")
else:
	release_text = release.read_text(encoding="utf-8")
	for section in (
		"## Intent", "## Player-facing behavior", "## Architecture", "## Design reasoning",
		"## Tuning snapshot", "## Assets and licenses", "## Advancement coverage",
		"## Known limitations", "## Validation", "## Next release pressure",
	):
		if section not in release_text:
			fail(f"release document missing section: {section}")

# Resource syntax.
for path in RESOURCES.rglob("*.json"):
	try:
		json.loads(path.read_text(encoding="utf-8"))
	except Exception as exc:
		fail(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")

# Mixin cleanup / current client hooks.
mixins_text = read("src/main/resources/projectflint.mixins.json")
try:
	mixins = json.loads(mixins_text) if mixins_text else {}
except json.JSONDecodeError:
	mixins = {}
active_mixins = set(mixins.get("mixins", [])) | set(mixins.get("client", [])) | set(mixins.get("server", []))
for obsolete in (
	"MobEffectFogEnvironmentMixin", "BlindnessFogEnvironmentMixin",
	"FogRendererMixin", "ItemEntityMixin", "ServerPlayerMixin",
):
	if obsolete in active_mixins:
		fail(f"obsolete mixin must not be active: {obsolete}")

for obsolete_path in (
	"src/main/java/dev/projectflint/client/ProjectFlintHardcoreDarkness.java",
	"src/main/java/dev/projectflint/mixin/MobEffectFogEnvironmentMixin.java",
	"src/main/java/dev/projectflint/mixin/BlindnessFogEnvironmentMixin.java",
	"src/main/java/dev/projectflint/client/ProjectFlintNightFogEnvironment.java",
	"src/main/java/dev/projectflint/mixin/FogRendererMixin.java",
	"src/main/java/dev/projectflint/mixin/ItemEntityMixin.java",
	"src/main/java/dev/projectflint/mixin/ServerPlayerMixin.java",
	"src/main/java/dev/projectflint/system/PrimitiveCooking.java",
):
	if (ROOT / obsolete_path).exists():
		fail(f"obsolete source/resource still present: {obsolete_path}")

# Darkness: lightmap-only. Do not clip distant navigation lights with artificial fog.
shader = read("src/main/resources/assets/minecraft/shaders/core/lightmap.fsh")
for expected in ("hardened_sky_factor", "NightVisionFactor", "BrightnessFactor", "fragColor"):
	if expected not in shader:
		fail(f"Project Flint lightmap shader missing: {expected}")
for forbidden in ("ProjectFlintNightFogEnvironment", "FogRendererMixin"):
	if forbidden in mixins_text:
		fail(f"artificial night fog must not be active: {forbidden}")

# Actual world-generation Loose Rock must stay separate from held Rock/stockpile art.
loose_model_path = ROOT / "src/main/resources/assets/projectflint/models/block/loose_rock.json"
try:
	loose_model = json.loads(loose_model_path.read_text(encoding="utf-8"))
	if len(loose_model.get("elements", [])) < 4:
		fail("worldgen loose_rock model must contain an irregular multi-element rock shape")
	textures = loose_model.get("textures", {})
	if textures.get("rock") != "minecraft:block/stone" or textures.get("particle") != "minecraft:block/stone":
		fail("worldgen loose_rock must use its block-surface texture path, not held Rock art")
	if "projectflint:block/placed/rock" in loose_model_path.read_text(encoding="utf-8"):
		fail("worldgen loose_rock must not reference the placed/held Rock mirror")
except Exception as exc:
	fail(f"could not validate worldgen loose_rock model: {exc}")
held_loose = read("src/main/resources/assets/projectflint/items/loose_rock.json")
if '"model": "projectflint:item/rock"' not in held_loose:
	fail("held Loose Rock item must remain on the separate projectflint:item/rock path")

# Wetness / Drenched behavior.
effects = read("src/main/java/dev/projectflint/registry/ModEffects.java")
environment = read("src/main/java/dev/projectflint/system/PrimitiveEnvironmentEffectSystem.java")
food = read("src/main/java/dev/projectflint/mixin/FoodDataMixin.java")
for expected in ("DRENCHED", "DrenchedEffect", "entity.tickCount % 25"):
	if expected not in effects:
		fail(f"Drenched effect implementation missing: {expected}")
if "DRENCHED_AFTER_TICKS = 20 * 90" not in environment:
	fail("Drenched threshold must be 90 seconds")
if "player.hasEffect(ModEffects.DRENCHED)" not in food:
	fail("Drenched natural-healing suppression missing")

# Rain-sensitive primitive fires.
twig = read("src/main/java/dev/projectflint/block/TwigPileBlock.java")
campfire = read("src/main/java/dev/projectflint/block/PrimitiveCampfireBlock.java")
for name, body in (("TwigPileBlock", twig), ("PrimitiveCampfireBlock", campfire)):
	for expected in ("isRainingAt(pos)", "handlePrecipitation", "FIRE_EXTINGUISH"):
		if expected not in body:
			fail(f"{name} rain handling missing: {expected}")

# Processor timing.
tuning = read("src/main/java/dev/projectflint/config/GameplayTuning.java")
processing = read("src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java")
processor_be = read("src/main/java/dev/projectflint/block/entity/AbstractPrimitiveProcessorBlockEntity.java")
mortar_be = read("src/main/java/dev/projectflint/block/entity/BakedClayKilnBlockEntity.java")
campfire_be = read("src/main/java/dev/projectflint/block/entity/PrimitiveCampfireBlockEntity.java")
for expected in ("PLANT_FIBER_COOK_TICKS = KILN_MEAT_COOK_TICKS * 5 / 4", "MORTAR_KILN_COOK_SPEED = 1.25F", "CAMPFIRE_COOK_SPEED = 0.90F"):
	if expected not in tuning:
		fail(f"processing tuning missing: {expected}")
if "GameplayTuning.PLANT_FIBER_COOK_TICKS" not in processing:
	fail("Plant Fiber processing recipe does not use the 1.25x duration")
if "recipeTicks / speed" not in processor_be:
	fail("shared primitive processor speed multiplier calculation missing")
if "MORTAR_KILN_COOK_SPEED" not in mortar_be:
	fail("Mortar Kiln 1.25x speed override missing")
if "CAMPFIRE_COOK_SPEED" not in campfire_be:
	fail("Campfire 0.90x speed override missing")

# Loose Soil and bear dev utilities/combat corrections.
items = read("src/main/java/dev/projectflint/registry/ModItems.java")
tab = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
mining = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
interaction = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
for expected in ("LOOSE_SOIL", "BLACK_BEAR_SPAWN_EGG", "BROWN_BEAR_SPAWN_EGG"):
	if expected not in items or expected not in tab:
		fail(f"creative item registration missing: {expected}")
if "Blocks.DIRT.defaultBlockState().getDestroyProgress" in mining:
	fail("Loose Soil still uses the obsolete recursive Dirt destroy-progress special case")
shared_earth = "state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.LOOSE_SOIL) || state.is(Blocks.GRAVEL)"
if shared_earth not in mining:
	fail("Loose Soil is not in the shared multitool earth destroy-progress branch")
if interaction.count("state.is(ModBlocks.LOOSE_SOIL)") < 2:
	fail("Loose Soil is missing from one or more primitive multitool authority gates")
for path in (
	"src/main/resources/assets/projectflint/items/loose_soil.json",
	"src/main/resources/assets/projectflint/items/black_bear_spawn_egg.json",
	"src/main/resources/assets/projectflint/items/brown_bear_spawn_egg.json",
):
	if not (ROOT / path).exists():
		fail(f"missing item definition: {path}")

# Bears use their separate source-authored geometry/UV layouts.
black_model = read("src/main/java/dev/projectflint/client/BlackBearModel.java")
brown_model = read("src/main/java/dev/projectflint/client/BrownBearModel.java")
black_renderer = read("src/main/java/dev/projectflint/client/BlackBearRenderer.java")
brown_renderer = read("src/main/java/dev/projectflint/client/BrownBearRenderer.java")
client = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
entities = read("src/main/java/dev/projectflint/registry/ModEntities.java")
for name, body, required in (
	("BlackBearModel", black_model, ("texOffs(1, 0)", "texOffs(1, 44)", "texOffs(4, 21)", "LayerDefinition.create(mesh, 128, 64)")),
	("BrownBearModel", brown_model, ("texOffs(0, 0)", "texOffs(0, 56)", "texOffs(0, 30)", "LayerDefinition.create(mesh, 128, 64)")),
):
	for expected in required:
		if expected not in body:
			fail(f"{name} missing source-authored model detail: {expected}")
if "ModelLayers.POLAR_BEAR" in black_renderer or "ModelLayers.POLAR_BEAR" in brown_renderer:
	fail("bear renderers must not reuse vanilla Polar Bear layers")
for expected in (
	"BlackBearModel.BODY_LAYER", "BlackBearModel.BODY_LAYER_BABY",
	"BrownBearModel.BODY_LAYER", "BrownBearModel.BODY_LAYER_BABY",
):
	if expected not in client:
		fail(f"ProjectFlintClient missing bear model-layer registration: {expected}")
if "BLACK_BEAR,\n\t\t\t6," not in entities:
	fail("Black Bear spawn weight must be 6")
if "BROWN_BEAR,\n\t\t\t4," not in entities:
	fail("Brown Bear spawn weight must be 4")
if ".sized(1.8F, 1.8F)" not in entities:
	fail("Brown Bear entity dimensions must match its 1.8x1.8 source design")
if not (ROOT / "LICENSES/Bear-Minimum-Apache-2.0.txt").exists():
	fail("missing Bear Minimum Apache-2.0 license copy")

# Forage / Loose Soil / Leaf Litter correction.
tool_caps = read("src/main/java/dev/projectflint/system/ToolCapabilities.java")
server_game_mode = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
if "ModTags.FORAGE_BUSHES" in tool_caps:
	fail("forage bushes must not be direct-harvestable by stronger multitools")
for expected in (
	"level.getRandom().nextFloat() < 0.33F",
	"adjacentState.is(Blocks.DIRT) || adjacentState.is(Blocks.GRASS_BLOCK)",
	"projectflint$loosenAdjacentDirt(pos)",
	"state.getValue(SegmentableBlock.AMOUNT)",
	"(fullCount * layers + 3) / 4",
	"twigCount = 8",
	"twigCount = 6",
):
	if expected not in server_game_mode:
		fail(f"forage/earth correction missing: {expected}")
shared_spread_branch = "state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.LOOSE_SOIL)"
if shared_spread_branch not in server_game_mode:
	fail("Dirt/Grass/Loose Soil must share the same multitool spread branch")

# Sweet Berry progression and future item stubs.
items = read("src/main/java/dev/projectflint/registry/ModItems.java")
interaction = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
mining = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
food_balance = read("src/main/java/dev/projectflint/system/FoodBalanceSystem.java")
seed_item = read("src/main/java/dev/projectflint/item/SweetBerrySeedsItem.java")
for expected in ("SWEET_BERRY_SEEDS", "SWEET_BERRY_JAM", "SWEET_BERRY_PIE", "isPlantCutterLike", "canHarvestSweetBerrySeeds"):
	if expected not in items:
		fail(f"Sweet Berry item/tool helper missing: {expected}")
for expected in ("SweetBerryBushBlock.AGE", "1 + level.getRandom().nextInt(2)", "SWEET_BERRY_BUSH_PICK_BERRIES", "ModItems.isPlantCutterLike(held)"):
	if expected not in interaction:
		fail(f"Sweet Berry right-click harvesting missing: {expected}")
if "1.0F / 130.0F" not in mining or "canHarvestSweetBerrySeeds" not in mining:
	fail("Sweet Berry Bush seed extraction must take 130 ticks and be Flint-cutter gated")
for expected in ("Blocks.GRASS_BLOCK", "Blocks.SWEET_BERRY_BUSH.defaultBlockState()", "seeds.shrink(1)"):
	if expected not in seed_item:
		fail(f"Sweet Berry seed placement missing: {expected}")
for expected in ("Items.SWEET_BERRIES", "nutrition(1)", "saturationModifier(0.15F)"):
	if expected not in food_balance:
		fail(f"Sweet Berry food rebalance missing: {expected}")
asset_hashes = {
	"sweet_berry_seeds.png": "3e30e26e854ccd4081bce268977f3f4484f6c4ea6e4dac9fc52ded3411c08349",
	"sweet_berry_jam.png": "31da30a9b6cf807385ce35ecadac84197884dda80caba0b2251985f045031028",
	"sweet_berry_pie.png": "249288d5942c739bcd0ceabc565123fb08a7bae724c2be1e63ca53e79d41aceb",
}
import hashlib
for filename, expected_hash in asset_hashes.items():
	asset = ROOT / "src/main/resources/assets/projectflint/textures/item" / filename
	if not asset.exists():
		fail(f"missing owner-provided Sweet Berry asset: {filename}")
	elif hashlib.sha256(asset.read_bytes()).hexdigest() != expected_hash:
		fail(f"Sweet Berry asset pixels changed: {filename}")
	stem = filename[:-4]
	for path in (
		ROOT / "src/main/resources/assets/projectflint/items" / f"{stem}.json",
		ROOT / "src/main/resources/assets/projectflint/models/item" / f"{stem}.json",
	):
		if not path.exists():
			fail(f"missing Sweet Berry item resource: {path.relative_to(ROOT)}")

# Grinder selection persistence from the underlying 0.28.7-a stability repair must survive.
for name, path in (
	("ClayGrinderMenu", "src/main/java/dev/projectflint/menu/ClayGrinderMenu.java"),
	("WoodenGrinderMenu", "src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java"),
):
	body = read(path)
	for expected in ("preferredRecipeId", "nextSelection", ".id().equals(preferredRecipeId)"):
		if expected not in body:
			fail(f"{name} recipe-selection persistence missing: {expected}")

# Build / cleanup protocol.
build = read("build.bat")
if "APPLY_PROJECTFLINT_0_31_48_B.bat" not in build:
	fail("build.bat does not apply the current release cleanup")
if 'explorer.exe /select,' not in build:
	fail("build.bat no longer opens Explorer with the successful JAR selected")
if not (ROOT / "APPLY_PROJECTFLINT_0_31_48_B.bat").exists():
	fail("missing current release cleanup BAT")

for root_junk in (
	"APPLY_0_26_A_TEXTURE_DEDUP_REMOVALS_FIXED.bat",
	"APPLY_PROJECTFLINT_0_26_6_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_1_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_2_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_3_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_4_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_5_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_6_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_7_A.bat",
	"APPLY_PROJECTFLINT_0_28_7_8_A.bat",
	"DELETE_THESE_FILES.txt",
	"PATCH_MANIFEST_0.25.1.md",
	"MEAT_IMPORT_VALIDATION_v0.28.0-a.txt",
	"TEXTURE_IMPORT_VERIFICATION.txt",
	"TEXTURE_RENDER_FIX_VERIFICATION_v0.27.2-a.txt",
	"TEXTURE_SYNC_VERIFICATION.txt",
	"VALIDATION_0.27.0-a.txt",
	"VALIDATION_0.27.4-a.txt",
	"VALIDATION_0.27.5-a.txt",
	"VALIDATION_v0.28.1-a.txt",
):
	if (ROOT / root_junk).exists():
		fail(f"obsolete root artifact still present: {root_junk}")



# v0.28.7.5-a stack-safe knapping and crude plant torch.
rock_item = read("src/main/java/dev/projectflint/item/RockItem.java")
if "heldItemTransformedTo" in rock_item:
	fail("Rock knapping must not transform/clear the entire held stack")
for expected in ("mainHand.shrink(1)", "StoneWorkingResult.SUCCESS", "new ItemStack(ModItems.REFINED_ROCK)"):
	if expected not in rock_item:
		fail(f"stack-safe Rock knapping missing: {expected}")

front_anim = read("src/main/resources/assets/projectflint/textures/block/advanced_kiln_front_active.png.mcmeta")
try:
	front_anim_json = json.loads(front_anim)
	if front_anim_json.get("animation", {}).get("frametime") != 4:
		fail("active Advanced/Mortar Kiln front animation must use frametime 4")
except Exception as exc:
	fail(f"could not parse active kiln front animation metadata: {exc}")

shader = read("src/main/resources/assets/minecraft/shaders/core/lightmap.fsh")
if "max(hardened_sky_factor, 0.06)" in shader:
	fail("full-night shader must not preserve the temporary 0.06 moonlight floor")
if "hardened_sky_factor *= hardened_sky_factor" not in shader:
	fail("hardcore-night sky suppression curve is missing")
environment = read("src/main/java/dev/projectflint/system/PrimitiveEnvironmentEffectSystem.java")
if "TERRIFIED_DAMAGE_DELAY = 20 * 45" not in environment:
	fail("darkness damage grace must be 45 seconds")
for expected in ("hasCarriedCrudePlantTorch(player)", "Math.max(effectiveBrightness, 6)", "ModItems.CRUDE_PLANT_TORCH"):
	if expected not in environment:
		fail(f"carried Crude Plant Torch darkness protection missing: {expected}")

torch_item = read("src/main/java/dev/projectflint/item/CrudePlantTorchItem.java")
torch_block = read("src/main/java/dev/projectflint/block/CrudePlantTorchBlock.java")
torch_be = read("src/main/java/dev/projectflint/block/entity/CrudePlantTorchBlockEntity.java")
blocks = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
block_entities = read("src/main/java/dev/projectflint/registry/ModBlockEntities.java")
items = read("src/main/java/dev/projectflint/registry/ModItems.java")
client = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
twig = read("src/main/java/dev/projectflint/block/TwigPileBlock.java")
campfire = read("src/main/java/dev/projectflint/block/PrimitiveCampfireBlock.java")
for expected in (
	"LIFETIME_TICKS = 20 * 60 * 5", "INVENTORY_BURN_INTERVAL = 50", "isInWaterOrRain()",
	"isRainingAt(player.blockPosition())", "damageSources().inFire()",
	"stack.setDamageValue(nextDamage)", "lightFromCampfire",
):
	if expected not in torch_item:
		fail(f"Crude Plant Torch carried behavior missing: {expected}")

if "canSeeSky(player.blockPosition().above())" in torch_item:
	fail("carried Crude Plant Torch must not require direct sky access")

for expected in (
	"remainingTicks--", "isRainingAt(pos)", "canSeeSky(pos.above())", "BaseFireBlock.getState",
	"BlockTags.LOGS_THAT_BURN", "ModTags.FORAGE_BUSHES", "Blocks.SWEET_BERRY_BUSH",
):
	if expected not in torch_be:
		fail(f"Crude Plant Torch placed behavior missing: {expected}")
for expected in ("CRUDE_PLANT_TORCH", "lightLevel(state -> 6)", "noLootTable()", "strength(-1.0F, 3600000.0F)"):
	if expected not in blocks:
		fail(f"Crude Plant Torch block registration missing: {expected}")
if "CRUDE_PLANT_TORCH" not in block_entities:
	fail("Crude Plant Torch block entity is not registered")
if 'register("crude_plant_torch", CrudePlantTorchItem::new)' not in items:
	fail("Crude Plant Torch item is not registered")
if "BlockRenderLayerMap.putBlock(ModBlocks.CRUDE_PLANT_TORCH, ChunkSectionLayer.CUTOUT)" not in client:
	fail("Crude Plant Torch CUTOUT render layer missing")
for name, body in (("TwigPileBlock", twig), ("PrimitiveCampfireBlock", campfire)):
	if "CrudePlantTorchItem.lightFromCampfire" not in body:
		fail(f"{name} does not light Crude Plant Torches from Lightly Burnt Twigs")
for path in (
	"src/main/resources/assets/projectflint/blockstates/crude_plant_torch.json",
	"src/main/resources/assets/projectflint/models/block/crude_plant_torch.json",
	"src/main/resources/assets/projectflint/models/item/crude_plant_torch.json",
	"src/main/resources/assets/projectflint/items/crude_plant_torch.json",
):
	if not (ROOT / path).exists():
		fail(f"missing Crude Plant Torch resource: {path}")
if "projectflint:block/crude_plant_torch" not in read("src/main/resources/assets/projectflint/models/block/crude_plant_torch.json"):
	fail("Crude Plant Torch block model must use the owner-supplied Project Flint torch artwork")

# v0.31.6-b integration regression guards.
stove_entity = read("src/main/java/dev/projectflint/block/entity/PrimitiveStoveBlockEntity.java")
stove_recipes = read("src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java")
cabinet_entity = read("src/main/java/dev/projectflint/block/entity/WoodenCabinetBlockEntity.java")
cabinet_menu = read("src/main/java/dev/projectflint/menu/WoodenCabinetMenu.java")
primitive_316 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
blocks_316 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
for expected, haystack in (
    ("PRIMITIVE_STOVE_COOK_SPEED = 1.45F", tuning),
    ("BlockRenderLayerMap.putBlock(ModBlocks.WOODEN_CABINET, ChunkSectionLayer.CUTOUT)", client),
    ("state.is(ModBlocks.WOODEN_CABINET)", primitive_316),
    ("state.is(ModBlocks.PRIMITIVE_STOVE)", primitive_316),
    ("if (isLimestoneFamily(state)) return held.is(ModItems.ADVANCED_FLINT_MULTITOOL)", primitive_316),
    ("noLootTable().noOcclusion().strength(1.4F)", blocks_316),
):
    if expected not in haystack:
        fail(f"v0.31.6-b integration fix missing: {expected}")
for expected in ("STRIPPED_OAK_WOOD", "STRIPPED_PALE_OAK_WOOD", "STRIPPED_CRIMSON_HYPHAE", "STRIPPED_WARPED_HYPHAE"):
    if expected not in stove_entity:
        fail(f"Primitive Stove stripped-wood fuel coverage missing: {expected}")
if "GameplayTuning.MORTAR_KILN_COOK_SPEED *" in stove_entity:
    fail("Primitive Stove must not compound the Mortar Kiln multiplier")
for expected in ("result.get(DataComponents.FOOD) != null", "PrimitiveProcessingRegistry.find(PrimitiveProcessor.KILN, input)"):
    if expected not in stove_recipes:
        fail(f"Primitive Stove direct-food bridge missing: {expected}")
for body, label in ((cabinet_entity, "cabinet entity"), (cabinet_menu, "cabinet menu")):
    if "stack.is(ModItems.RECIPE_PARCHMENT)" not in body:
        fail(f"{label} must remain parchment-only")
    if "RecipeParchmentItem.isBound(stack)" in body:
        fail(f"{label} must accept blank Recipe Parchment in v0.31.10-b")
lang = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json") or "{}")
for name in ("limestone", "cobbled_limestone", "smooth_limestone", "limestone_bricks", "chiseled_limestone", "polished_limestone", "checkered_tile", "glazed_checkered_tile", "wooden_cabinet", "primitive_stove"):
    if f"item.projectflint.{name}" not in lang:
        fail(f"missing v0.31.6-b item translation: {name}")

build_bat = read("build.bat")
cleanup_bat = read("APPLY_PROJECTFLINT_0_31_48_B.bat")
for expected in ("0.31.48-b", "APPLY_PROJECTFLINT_0_31_48_B.bat"):
	if expected not in build_bat:
		fail(f"build.bat version/apply wiring missing: {expected}")
if "APPLY_PROJECTFLINT_0_28_28_A.bat" not in cleanup_bat:
	fail("v0.28.30-a cleanup must retire the previous v0.28.28-a apply BAT")

# v0.28.7.8-a gourd and food rules.
primitive = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
block_state_mixin = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
game_mode_mixin = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")

# v0.31.1-b carried regression guard: Flint Plant Cutters must use exactly one Sugar Cane completion path.
plant_cutter_cane_condition = "state.is(Blocks.SUGAR_CANE) && held.is(ModItems.FLINT_PLANT_CUTTERS)"
if game_mode_mixin.count(plant_cutter_cane_condition) != 1:
	fail("v0.31.1-b requires exactly one Flint Plant Cutter Sugar Cane handler")
for expected in (
	"int harvested = projectflint$removeSugarCaneColumn(pos);",
	"new ItemStack(ModItems.SUGARCANE_ROLL, harvested)",
	"held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND)",
):
	if expected not in game_mode_mixin:
		fail(f"v0.31.1-b Flint Plant Cutter Sugar Cane regression: missing {expected}")
if "new ItemStack(ModItems.REED_ROLL)" in game_mode_mixin:
	fail("v0.31.1-b stale one-Reed-Roll Sugar Cane handler is still present")
food_balance = read("src/main/java/dev/projectflint/system/FoodBalanceSystem.java")
items_source = read("src/main/java/dev/projectflint/registry/ModItems.java")
for expected in (
	"Blocks.PUMPKIN", "Blocks.MELON", "ModItems.canHarvestGourdFlesh(held)",
	"wouldPlantVanillaSweetBerryBush(level, hit, clicked)", "held.is(Items.SWEET_BERRIES)",
):
	if expected not in primitive:
		fail(f"v0.28.7.8-a primitive interaction rule missing: {expected}")
if "Blocks.PUMPKIN" not in block_state_mixin or "Blocks.MELON" not in block_state_mixin or "1.0F / 65.0F" not in block_state_mixin:
	fail("Flint gourd destroy progress is missing")
for expected in (
	"new ItemStack(Items.PUMPKIN_SEEDS)", "new ItemStack(Items.MELON_SEEDS)",
	"new ItemStack(ModItems.PUMPKIN_SLICE)", "new ItemStack(Items.MELON_SLICE)",
):
	if expected not in game_mode_mixin:
		fail(f"gourd completion/drop rule missing: {expected}")
if "PUMPKIN_SEEDS, 4" in game_mode_mixin:
	fail("pumpkins must drop exactly one pumpkin seed")
for expected in (
	"Items.MELON_SLICE", "food(1, 0.22F)", "Items.COOKED_CHICKEN", "food(2, 0.54F)",
	"Items.COOKED_MUTTON", "food(4, 0.72F)", "Items.COOKED_RABBIT", "food(2, 0.81F)",
):
	if expected not in food_balance:
		fail(f"food balance rule missing: {expected}")
for expected in (
	'PUMPKIN_SLICE = register("pumpkin_slice"',
	'COOKED_HORSE_MEAT = register("cooked_horse_meat", properties -> new Item(properties.stacksTo(16).food(new FoodProperties.Builder().nutrition(5)',
):
	if expected not in items_source:
		fail(f"Project Flint food item balance missing: {expected}")
for path in (
	"src/main/resources/assets/projectflint/textures/item/pumpkin_slice.png",
	"src/main/resources/assets/projectflint/items/pumpkin_slice.json",
	"src/main/resources/assets/projectflint/models/item/pumpkin_slice.json",
):
	if not (ROOT / path).exists():
		fail(f"missing Pumpkin Slice resource: {path}")


# v0.28.18-a versioning and wattle/daub construction.
import re
if not re.fullmatch(r"\d+\.\d+\.\d+-(?:a|b|r)", EXPECTED_VERSION):
	fail("current release version violates MAJOR.RELEASE.DEV-a/b/r")
versioning = read("docs/VERSIONING.md")
for expected in ("MAJOR.RELEASE.DEV-CHANNEL", "Never introduce a fourth numeric component", "0.31.32-b"):
	if expected not in versioning:
		fail(f"versioning documentation missing: {expected}")

wattle_block = read("src/main/java/dev/projectflint/block/WattledDaubBricksBlock.java")
for expected in (
	"MAX_COLUMN_HEIGHT = 4", "MAX_CEILING_REACH = 3", "isLoadBearingColumnTop",
	"FallingBlockEntity.fall", "ModItems.CRUDE_REED", "ModItems.TWIG, 2",
):
	if expected not in wattle_block:
		fail(f"Wattled Daub structural rule missing: {expected}")

items_source = read("src/main/java/dev/projectflint/registry/ModItems.java")
for expected in (
	'WATTLE = register("wattle"', 'TIED_WATTLE = register("tied_wattle"',
	'MUD_PILE = register("mud_pile"', 'DAUB_PILE = register("daub_pile"',
	'WET_DAUB_PILE = register("wet_daub_pile"', 'UNFIRED_WATTLE_AND_DAUB = register("unfired_wattle_and_daub"',
	'WATTLED_DAUB_BRICKS = registerBlockItem("wattled_daub_bricks"',
):
	if expected not in items_source:
		fail(f"Wattle/Daub item registration missing: {expected}")

primitive = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
for expected in (
	"main.getCount() >= 12 && off.is(ModItems.TWIG) && off.getCount() >= 12",
	"main.is(ModItems.WATTLE) && off.is(ModItems.CRUDE_PLANT_STRING)",
	"main.getCount() >= 4 && off.is(ModItems.WET_DAUB_PILE) && off.getCount() >= 2",
	"main.shrink(12)", "off.shrink(12)", "new ItemStack(ModItems.WATTLE, 4)",
	"new ItemStack(ModItems.UNFIRED_WATTLE_AND_DAUB, 2)",
	"main.is(ModItems.DIRT_PILE) || main.is(ModItems.DAUB_PILE)",
	"held.is(ModItems.MUD_PILE) && clicked.is(BlockTags.LEAVES)",
	"instanceof BlockItem", "WATTLED_DAUB_BRICKS",
):
	if expected not in primitive:
		fail(f"Wattle/Daub interaction rule missing: {expected}")

processing = read("src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java")
if 'ModItems.UNFIRED_WATTLE_AND_DAUB, new ItemStack(ModItems.WATTLED_DAUB_BRICKS, 3)' not in processing:
	fail("permanent campfire/baked/mortar cooking must output 3 Wattled Daub Bricks")
advanced = read("src/main/java/dev/projectflint/block/entity/AdvancedKilnBlockEntity.java")
if "UNFIRED_WATTLE_AND_DAUB" in advanced or "WATTLED_DAUB_BRICKS, 3" in advanced:
	fail("Advanced Kiln must not process Wattled Daub Bricks in v0.28.26-a")

game_mode = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
for expected in (
	"state.is(ModBlocks.WATTLED_DAUB_BRICKS)",
	"held.is(ModItems.FLINT_MULTITOOL) || held.is(ModItems.ADVANCED_FLINT_MULTITOOL)",
	"ModItems.WATTLED_DAUB_BRICKS", "new ItemStack(ModItems.CRUDE_REED)", "new ItemStack(ModItems.TWIG, 2)",
):
	if expected not in game_mode:
		fail(f"Wattled Daub break/recovery rule missing: {expected}")

# v0.28.18-a finalized owner-supplied texture hookup.
for rel in (
	"src/main/resources/assets/projectflint/textures/item/wattle.png",
	"src/main/resources/assets/projectflint/textures/item/tied_wattle.png",
	"src/main/resources/assets/projectflint/textures/item/mud_pile.png",
	"src/main/resources/assets/projectflint/textures/item/daub_pile.png",
	"src/main/resources/assets/projectflint/textures/item/wet_daub_pile.png",
	"src/main/resources/assets/projectflint/textures/item/unfired_wattle_and_daub.png",
	"src/main/resources/assets/projectflint/textures/block/wattled_daub_bricks.png",
	"src/main/resources/assets/projectflint/textures/item/crude_plant_torch.png",
	"src/main/resources/assets/projectflint/textures/block/crude_plant_torch.png",
	"src/main/resources/assets/projectflint/textures/block/advanced_kiln_front_active.png",
	"src/main/resources/assets/projectflint/items/wattle.json",
	"src/main/resources/assets/projectflint/items/tied_wattle.json",
	"src/main/resources/assets/projectflint/items/mud_pile.json",
	"src/main/resources/assets/projectflint/items/daub_pile.json",
	"src/main/resources/assets/projectflint/items/wet_daub_pile.json",
	"src/main/resources/assets/projectflint/items/unfired_wattle_and_daub.json",
	"src/main/resources/assets/projectflint/items/wattled_daub_bricks.json",
	"src/main/resources/assets/projectflint/models/item/wattle.json",
	"src/main/resources/assets/projectflint/models/item/tied_wattle.json",
	"src/main/resources/assets/projectflint/models/item/mud_pile.json",
	"src/main/resources/assets/projectflint/models/item/daub_pile.json",
	"src/main/resources/assets/projectflint/models/item/wet_daub_pile.json",
	"src/main/resources/assets/projectflint/models/item/unfired_wattle_and_daub.json",
	"src/main/resources/assets/projectflint/models/item/wattled_daub_bricks.json",
	"src/main/resources/assets/projectflint/models/block/wattled_daub_bricks.json",
	"src/main/resources/assets/projectflint/blockstates/wattled_daub_bricks.json",
):
	if not (ROOT / rel).exists():
		fail(f"v0.28.18-a missing finalized asset/resource hookup: {rel}")

if '"frametime": 4' not in read("src/main/resources/assets/projectflint/textures/block/advanced_kiln_front_active.png.mcmeta"):
	fail("v0.28.18-a active kiln front animation must preserve frametime 4")

for rel in (
	"src/main/resources/assets/projectflint/models/item/crude_plant_torch.json",
	"src/main/resources/assets/projectflint/models/block/crude_plant_torch.json",
):
	if "crude_plant_torch" not in read(rel):
		fail(f"Crude Plant Torch model is not wired to owner texture: {rel}")

# v0.28.19-a habitable Wattle/Daub expansion and placement fixes.
blocks_source = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
items_source = read("src/main/java/dev/projectflint/registry/ModItems.java")
client_source = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
primitive = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
wattle_block = read("src/main/java/dev/projectflint/block/WattledDaubBricksBlock.java")
torch_block = read("src/main/java/dev/projectflint/block/CrudePlantTorchBlock.java")
wooden_menu = read("src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java")

for expected in ("WATTLED_DAUB_FLOOR_TILE", "WATTLED_DAUB_WINDOW", "WATTLED_DAUB_DOOR"):
	if expected not in blocks_source or expected not in items_source:
		fail(f"v0.28.19-a block/item registration missing: {expected}")
if "DoubleHighBlockItem(ModBlocks.WATTLED_DAUB_DOOR" not in items_source:
	fail("Wattled Daub Door must use DoubleHighBlockItem placement")
for expected in (
	"BlockRenderLayerMap.putBlock(ModBlocks.WATTLED_DAUB_WINDOW, ChunkSectionLayer.CUTOUT)",
	"BlockRenderLayerMap.putBlock(ModBlocks.WATTLED_DAUB_DOOR, ChunkSectionLayer.CUTOUT)",
):
	if expected not in client_source:
		fail(f"transparent Wattle/Daub render layer missing: {expected}")
for expected in (
	"isProjectFlintBlockItem(held)", "level.getFluidState(target).is(FluidTags.WATER)",
	"processDaubWaterInteraction", "below.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)",
	"held.is(ModItems.WATTLED_DAUB_DOOR)",
):
	if expected not in primitive:
		fail(f"v0.28.19-a placement/water rule missing: {expected}")
for expected in (
	"state.is(ModBlocks.WATTLED_DAUB_BRICKS)",
	"state.is(ModBlocks.WATTLED_DAUB_WINDOW)",
	"state.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)",
	"state.is(ModBlocks.WATTLED_DAUB_DOOR)",
):
	if expected not in primitive:
		fail(f"v0.28.19-a break-authority rule missing: {expected}")
for expected in (
	"isWattledDaubWall", "state.is(ModBlocks.WATTLED_DAUB_WINDOW)",
	"state.is(ModBlocks.WATTLED_DAUB_DOOR)", "foundation.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)",
	"FallingBlockEntity falling = FallingBlockEntity.fall", "falling.disableDrop()",
):
	if expected not in wattle_block:
		fail(f"v0.28.19-a structural/falling rule missing: {expected}")
for expected in ("ParticleTypes.SMOKE", "ParticleTypes.FLAME", "animateTick"):
	if expected not in torch_block:
		fail(f"Crude Plant Torch vanilla-style ambient particle behavior missing: {expected}")
wooden_recipe_registry = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	"Ingredient.of(ModItems.WATTLED_DAUB_BRICKS)", "Ingredient.of(ModItems.ADVANCED_KILN_GLASS)",
	"new ItemStack(ModItems.WATTLED_DAUB_WINDOW)",
):
	if expected not in wooden_recipe_registry:
		fail(f"Wattled Daub Window Wooden Grinder recipe missing: {expected}")

floor_recipe = read("src/main/resources/data/projectflint/recipe/wattled_daub_floor_tile.json")
for expected in ('"type": "projectflint:clay_grinding"', '"ingredient": "projectflint:wattled_daub_bricks"', '"count": 2'):
	if expected not in floor_recipe:
		fail(f"Wattled Daub Floor Tile 1:2 grinder recipe missing: {expected}")
if (ROOT / "src/main/resources/data/projectflint/recipe/wattled_daub_bricks_from_floor_tile.json").exists():
	fail("Wattled Daub Floor Tile must not have a reverse grinder recipe")

asset_hashes_2819 = {
	"src/main/resources/assets/projectflint/textures/block/wattled_daub_floor_tile.png": "ed8f79a4cc10c69f06d0c118073339678fed2cbe5b2059e27fe17a81e36375ee",
	"src/main/resources/assets/projectflint/textures/block/wattled_daub_window.png": "cf3c4f7a011175e548b022e9d4d51bad5e2aee9486131e6df2cc6cca0ceaa616",
	"src/main/resources/assets/projectflint/textures/block/wattled_daub_door_bottom.png": "6c2dba1b9b1d7d83b51f47decf60d49327c39393a12af5e7979eca1af253b2c8",
	"src/main/resources/assets/projectflint/textures/block/wattled_daub_door_top.png": "36d41dbc6ffd8ea935565204b30af219793b851a72cf0230b4023d7de651c3f5",
	"src/main/resources/assets/projectflint/textures/item/flower_petals.png": "c6812a577450d4a79768df4f6cb9d4dd5bb074869d679b15a82915532554ff96",
}
for rel, expected_hash in asset_hashes_2819.items():
	asset = ROOT / rel
	if not asset.exists():
		fail(f"missing v0.28.19-a owner asset: {rel}")
	elif hashlib.sha256(asset.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.28.19-a owner asset pixels changed: {rel}")

for rel in (
	"src/main/resources/assets/projectflint/blockstates/wattled_daub_floor_tile.json",
	"src/main/resources/assets/projectflint/blockstates/wattled_daub_window.json",
	"src/main/resources/assets/projectflint/blockstates/wattled_daub_door.json",
	"src/main/resources/assets/projectflint/items/wattled_daub_floor_tile.json",
	"src/main/resources/assets/projectflint/items/wattled_daub_window.json",
	"src/main/resources/assets/projectflint/items/wattled_daub_door.json",
	"src/main/resources/data/projectflint/advancement/wattled_daub_floor_tile.json",
	"src/main/resources/data/projectflint/advancement/wattled_daub_window.json",
):
	if not (ROOT / rel).exists():
		fail(f"missing v0.28.19-a resource: {rel}")



# v0.28.20-a shelter renovation, Sugar Cane column processing, and grinder expansion.
wattle_block_2820 = read("src/main/java/dev/projectflint/block/WattledDaubBricksBlock.java")
window_block_2820 = read("src/main/java/dev/projectflint/block/WattledDaubWindowBlock.java")
game_mode_2820 = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
wooden_menu_2820 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	"MAX_CEILING_REACH = 3",
	"STRUCTURAL_RECHECK_DELAY_TICKS = 10",
	"verticalStructuralLayer",
	"state.canSurvive(level, pos)",
	"falling.disableDrop()",
):
	if expected not in wattle_block_2820:
		fail(f"v0.28.20-a Wattled Daub structural rule missing: {expected}")
for expected in (
	"level.getBlockState(pos.below()).is(ModBlocks.WATTLED_DAUB_BRICKS)",
	"layer == 2 || layer == 3",
	"adjacentState.is(ModBlocks.WATTLED_DAUB_WINDOW)",
	"skipRendering",
):
	if expected not in window_block_2820:
		fail(f"v0.28.20-a Window restriction/render rule missing: {expected}")
for expected in (
	"state.is(Blocks.SUGAR_CANE) && held.is(ModItems.FLINT_PLANT_CUTTERS)",
	"new ItemStack(ModItems.SUGARCANE_ROLL, harvested)",
	"projectflint$removeSugarCaneColumn(pos)",
):
	if expected not in game_mode_2820:
		fail(f"current Flint Plant Cutter Sugar Cane rule missing: {expected}")
for expected in (
	"ModItems.SUGARCANE_ROLL", "ModItems.PLANT_TOOL_BINDING", "new ItemStack(ModItems.WOVEN_FAN, 2)",
	"ModItems.WOVEN_FAN", "ModItems.WATTLE", "new ItemStack(ModItems.TIED_WATTLE, 3)",
	"ModItems.FLINT_CHIP", "ModItems.MUD_PILE", "new ItemStack(ModItems.DAUB_PILE, 3)",
	"ModItems.TIED_WATTLE", "ModItems.WET_DAUB_PILE", "new ItemStack(ModItems.UNFIRED_WATTLE_AND_DAUB, 4)",
):
	if expected not in wooden_menu_2820:
		fail(f"v0.28.20-a Wooden Grinder recipe missing: {expected}")

for rel, expected_tokens in (
	("src/main/resources/data/projectflint/recipe/flint_from_flint_chips.json", ('"ingredient": "projectflint:flint_chip"', '"input_count": 4', '"id": "minecraft:flint"')),
	("src/main/resources/data/projectflint/recipe/flint_chips_from_flint.json", ('"ingredient": "minecraft:flint"', '"id": "projectflint:flint_chip"', '"count": 4')),
):
	body = read(rel)
	for expected in expected_tokens:
		if expected not in body:
			fail(f"v0.28.20-a Flint grinder conversion missing in {rel}: {expected}")

door_item_texture = ROOT / "src/main/resources/assets/projectflint/textures/item/wattled_daub_door.png"
if not door_item_texture.exists():
	fail("missing v0.28.20-a Wattled Daub Door item texture")
elif hashlib.sha256(door_item_texture.read_bytes()).hexdigest() != "51533d53dbc5b061c6436f987ab1a0880e1efb94b693c662340ce74e1251b92f":
	fail("v0.28.20-a Wattled Daub Door item texture pixels changed")
if '"layer0": "projectflint:item/wattled_daub_door"' not in read("src/main/resources/assets/projectflint/models/item/wattled_daub_door.json"):
	fail("Wattled Daub Door inventory model is not wired to the dedicated item sprite")



# v0.28.21-a reusable Flint Torch Holder.
flint_torch_block = read("src/main/java/dev/projectflint/block/FlintTorchBlock.java")
flint_torch_be = read("src/main/java/dev/projectflint/block/entity/FlintTorchBlockEntity.java")
blocks_2821 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
block_entities_2821 = read("src/main/java/dev/projectflint/registry/ModBlockEntities.java")
items_2821 = read("src/main/java/dev/projectflint/registry/ModItems.java")
wooden_2821 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
client_2821 = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
primitive_2821 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
mining_2821 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
adv_2821 = read("src/main/java/dev/projectflint/system/PrimitiveItemAdvancementSystem.java")

for expected in (
	'TORCH_HOLDER = register("torch_holder", properties -> new Item(properties.stacksTo(1)))',
	'UNLIT_FLINT_TORCH = register("unlit_flint_torch", properties -> new Item(properties.stacksTo(1)))',
	'FLINT_TORCH_HOLDER = registerBlockItem("flint_torch_holder", ModBlocks.FLINT_TORCH, 1)',
):
	if expected not in items_2821:
		fail(f"v0.28.21-a stack-one Flint Torch item registration missing: {expected}")

for expected in (
	'Ingredient.of(ModItems.MORTAR_BRICKS), 8',
	'Ingredient.of(ModItems.MUD_PILE), 16',
	'new ItemStack(ModItems.TORCH_HOLDER)',
	'Ingredient.of(Items.FLINT), 12',
	'Ingredient.of(ModItems.WOVEN_WOOD), 1',
	'new ItemStack(ModItems.UNLIT_FLINT_TORCH)',
	'Ingredient.of(ModItems.TORCH_HOLDER), 1',
	'Ingredient.of(ModItems.UNLIT_FLINT_TORCH), 1',
	'new ItemStack(ModItems.FLINT_TORCH_HOLDER)',
):
	if expected not in wooden_2821:
		fail(f"v0.28.21-a Wooden Grinder Flint Torch recipe missing: {expected}")

for expected in (
	'support.is(ModBlocks.WATTLED_DAUB_BRICKS)',
	'supportLayer != 2 && supportLayer != 3',
	'for (int dx = -1; dx <= 1; dx++)',
	'for (int dz = -1; dz <= 1; dz++)',
	'state.isAir() || state.is(ModBlocks.WATTLED_DAUB_BRICKS) || state.is(ModBlocks.WATTLED_DAUB_WINDOW)',
	'for (int rise = 1; rise <= 2; rise++)',
	'overhead.is(ModBlocks.WATTLED_DAUB_BRICKS) && !overhead.is(ModBlocks.WATTLED_DAUB_WINDOW)',
	'stack.is(Items.FLINT)',
	'stack.is(Items.TORCH)',
	'stack.is(ModItems.CRUDE_PLANT_TORCH)',
	'stack.is(Items.FLINT_AND_STEEL)',
	'ToolCapabilities.Capability.FIRESTARTER',
):
	if expected not in flint_torch_block:
		fail(f"v0.28.21-a Flint Torch placement/lighting rule missing: {expected}")
if 'support.is(ModBlocks.WATTLED_DAUB_WINDOW)' in flint_torch_block:
	fail("Flint Torch Holder must never mount directly to a Wattled Daub Window")

for expected in (
	'BURN_TICKS = 40 * 60 * 20',
	'FlintTorchBlock.isValidFixture(level, pos, state)',
	'level.removeBlock(pos, false)',
	'hasFlint = false',
	'state.setValue(FlintTorchBlock.LIT, false)',
	'output.putBoolean("HasFlint", hasFlint)',
	'output.putInt("RemainingTicks", remainingTicks)',
):
	if expected not in flint_torch_be:
		fail(f"v0.28.21-a Flint Torch persistent burn rule missing: {expected}")

for expected in (
	'FLINT_TORCH = registerFlintTorch("flint_torch")',
	'noLootTable()',
	'lightLevel(state -> state.getValue(FlintTorchBlock.LIT) ? 12 : 0)',
):
	if expected not in blocks_2821:
		fail(f"v0.28.21-a Flint Torch block registration missing: {expected}")
if 'BlockEntityType<FlintTorchBlockEntity> FLINT_TORCH' not in block_entities_2821:
	fail("v0.28.21-a Flint Torch block entity is not registered")
if 'BlockRenderLayerMap.putBlock(ModBlocks.FLINT_TORCH, ChunkSectionLayer.CUTOUT)' not in client_2821:
	fail("v0.28.21-a Flint Torch CUTOUT render layer missing")
if primitive_2821.count('state.is(ModBlocks.FLINT_TORCH)') < 2:
	fail("v0.28.21-a Flint Torch is missing from one or more break-authority gates")
if 'state.is(ModBlocks.FLINT_TORCH)' not in mining_2821 or '0.08F' not in mining_2821:
	fail("v0.28.21-a Flint Torch hand-break destroy progress missing")
for expected in ('ModItems.TORCH_HOLDER, "torch_holder"', 'ModItems.UNLIT_FLINT_TORCH, "unlit_flint_torch"', 'ModItems.FLINT_TORCH_HOLDER, "flint_torch_holder"'):
	if expected not in adv_2821:
		fail(f"v0.28.21-a Flint Torch advancement fallback missing: {expected}")

asset_hashes_2821 = {
	"src/main/resources/assets/projectflint/textures/item/torch_holder.png": "f1ad1f167f6f3b6273ce0ea3cbd50559cd010edd179b3eb5c6f15e85a627ddcf",
	"src/main/resources/assets/projectflint/textures/item/unlit_flint_torch.png": "b2637ad1979d0654ab66f05497975d721511bdaa85257b47ee1e1e8cc8aff62d",
	"src/main/resources/assets/projectflint/textures/block/flint_torch_unlit.png": "b2637ad1979d0654ab66f05497975d721511bdaa85257b47ee1e1e8cc8aff62d",
	"src/main/resources/assets/projectflint/textures/item/flint_torch_holder.png": "274e90db80f9c41ba85a4c1acba285766d46ff39c8c7937e9bb949ca7088f874",
	"src/main/resources/assets/projectflint/textures/item/lit_flint_torch.png": "74c9ad7d0401001ccb92bf22d7b72c9665b303c81f52646051dc1bb7f68e48c2",
	"src/main/resources/assets/projectflint/textures/block/flint_torch_lit.png": "74c9ad7d0401001ccb92bf22d7b72c9665b303c81f52646051dc1bb7f68e48c2",
}
for rel, expected_hash in asset_hashes_2821.items():
	asset = ROOT / rel
	if not asset.exists():
		fail(f"missing v0.28.21-a owner Flint Torch asset: {rel}")
	elif hashlib.sha256(asset.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.28.21-a owner Flint Torch asset pixels changed: {rel}")

for rel in (
	"src/main/resources/assets/projectflint/blockstates/flint_torch.json",
	"src/main/resources/assets/projectflint/items/torch_holder.json",
	"src/main/resources/assets/projectflint/items/unlit_flint_torch.json",
	"src/main/resources/assets/projectflint/items/flint_torch_holder.json",
	"src/main/resources/assets/projectflint/models/item/torch_holder.json",
	"src/main/resources/assets/projectflint/models/item/unlit_flint_torch.json",
	"src/main/resources/assets/projectflint/models/item/flint_torch_holder.json",
	"src/main/resources/assets/projectflint/models/block/flint_torch_unlit_wall.json",
	"src/main/resources/assets/projectflint/models/block/flint_torch_lit_wall.json",
	"src/main/resources/data/projectflint/advancement/torch_holder.json",
	"src/main/resources/data/projectflint/advancement/unlit_flint_torch.json",
	"src/main/resources/data/projectflint/advancement/flint_torch_holder.json",
):
	if not (ROOT / rel).exists():
		fail(f"missing v0.28.21-a Flint Torch resource: {rel}")



# v0.28.41-a unified detailed advancement guide.
adv_root = RESOURCES / "data/projectflint/advancement"
root_adv = adv_root / "root.json"
if not root_adv.exists():
	fail("unified Project Flint advancement root is missing")
else:
	try:
		data = json.loads(root_adv.read_text(encoding="utf-8"))
		display = data.get("display", {})
		if display.get("title", {}).get("text") != "ProjectFlint":
			fail("unified advancement root title must remain ProjectFlint")
		if display.get("background") != "minecraft:gui/advancements/backgrounds/stone":
			fail("unified advancement root should use the stone progression background")
	except Exception as exc:
		fail(f"could not validate unified advancement root: {exc}")

for retired_root in ("00_tool_era", "01_clay_era", "02_wood_era", "tool_era", "clay_era", "wood_era"):
	if (adv_root / retired_root).exists():
		fail(f"retired split advancement tab still exists: {retired_root}")

advancement_files = list(adv_root.rglob("*.json"))
if len(advancement_files) < 135:
	fail(f"detailed advancement guide was over-pruned again: only {len(advancement_files)} advancements remain")

# Every Project Flint parent must resolve inside the one progression tree.
for path in advancement_files:
	try:
		data = json.loads(path.read_text(encoding="utf-8"))
	except Exception:
		continue
	parent = data.get("parent")
	if parent and parent.startswith("projectflint:"):
		parent_path = adv_root / (parent.split(":", 1)[1] + ".json")
		if not parent_path.exists():
			fail(f"advancement has missing Project Flint parent: {path.relative_to(adv_root)} -> {parent}")

# Quest-heavy guidance must retain the important individual steps.
for restored in (
	"stone_in_hand.json", "disturbed_earth.json", "refined_rock.json", "first_twigs.json",
	"kindling.json", "first_fire.json", "charred_twig.json", "campfire_stone.json",
	"primitive_campfire.json", "crude_hammer.json", "crude_harvesting_tool.json",
	"direct_plant_string.json", "leaf_litter_fiber.json", "crude_multitool.json",
	"flint_multitool.json", "clay_pot.json", "bone_club.json", "bone_brush.json",
	"flint_plant_cutters.json", "advanced_flint_multitool.json", "clay_grinder.json",
	"wooden_grinder.json", "sanding_station.json", "water_bearing_pot.json",
	"water_pot.json", "bound_wood_processor.json", "lumber_processor.json",
	"lumber_washing.json", "wooden_board.json", "wooden_beam.json",
):
	if not (adv_root / restored).exists():
		fail(f"detailed progression milestone missing: {restored}")

for rel, token in (
	("disturbed_earth.json", "58%"),
	("refined_rock.json", "3 failures"),
	("first_fire.json", "65%"),
	("leaf_litter_fiber.json", "Crude Stone Cutters and Crude Multitool give up to 2"),
	("flint_plant_cutters.json", "Plant Tool Bindings"),
):
	if token not in read(f"src/main/resources/data/projectflint/advancement/{rel}"):
		fail(f"updated detailed advancement guidance missing from {rel}: {token}")

recipe_guide = read("src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java")
recipe_button = read("src/main/java/dev/projectflint/client/screen/RecipeGuideButton.java")
clay_guide_registry = read("src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java")
wooden_registry = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
wooden_menu = read("src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java")
primitive_registry = read("src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java")
inventory_screen = read("src/main/java/dev/projectflint/mixin/InventoryScreenMixin.java")
client_bootstrap = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
for expected in (
	"Baked Kiln", "Mortar Kiln", "Clay Grinder", "Wooden Grinder", "Advanced Kiln",
	"ClayGrinderRecipeGuideRegistry.recipes()",
	"WoodenGrinderRecipeRegistry.recipes()", "PrimitiveProcessingRegistry.recipesFor",
	"setTooltipForNextFrame(font, hovered",
):
	if expected not in recipe_guide:
		fail(f"Project Flint recipe guide missing: {expected}")
if "Permanent Campfire" in recipe_guide or "CAMPFIRE(" in recipe_guide:
	fail("Permanent Campfire must not be a recipe-guide station")
if "WoodenGrinderRecipeRegistry.findAll(slot0, slot1)" not in wooden_menu:
	fail("Wooden Grinder gameplay matching is not using the shared multi-match dual-recipe registry")
if "public static List<Definition> recipes()" not in wooden_registry:
	fail("Wooden Grinder shared recipe registry does not expose guide entries")
if "public static List<PrimitiveProcessingRecipe> recipesFor" not in primitive_registry:
	fail("primitive processing registry does not expose station recipes to the guide")
for expected in ("getRecipeBookButtonPosition", "new ScreenPosition(-1000, -1000)", "projectflint$disableRecipeBook"):
	if expected not in inventory_screen:
		fail(f"survival inventory recipe-book hiding/disable behavior missing: {expected}")
if "ProjectFlintRecipeGuideScreen" in inventory_screen:
	fail("survival inventory must not expose the Project Flint recipe guide")
if "public boolean isInGameUi()" not in recipe_guide or "return true;" not in recipe_guide:
	fail("Project Flint recipe guide must render as in-game UI to avoid the one-blur-per-frame crash")
if 'GLFW.GLFW_KEY_K' not in client_bootstrap or 'key.projectflint.recipe_guide' not in client_bootstrap or 'ProjectFlintRecipeGuideScreen.openGlobal' not in client_bootstrap:
	fail("default K recipe-guide keybind is missing")
if "BOOK_ICON" not in recipe_button or "Items.BOOK" not in recipe_button or 'Component.literal("Recipes")' not in recipe_button:
	fail("custom workstation recipe-book icon button is missing")
for screen_path in (
	"src/main/java/dev/projectflint/client/screen/ClayGrinderScreen.java",
	"src/main/java/dev/projectflint/client/screen/WoodenGrinderScreen.java",
	"src/main/java/dev/projectflint/client/screen/AdvancedKilnScreen.java",
):
	body = read(screen_path)
	if "new RecipeGuideButton" not in body or "ProjectFlintRecipeGuideScreen.open" not in body:
		fail(f"station screen missing integrated custom recipe-guide button: {screen_path}")

advanced_kiln = read("src/main/java/dev/projectflint/block/entity/AdvancedKilnBlockEntity.java")
if "UNFIRED_WATTLE_AND_DAUB" in advanced_kiln or "WATTLED_DAUB_BRICKS" in advanced_kiln:
	fail("Advanced Kiln must not advertise or process the Wattled Daub brick firing recipe")
if "UNFIRED_WATTLE_AND_DAUB" in recipe_guide and "advancedKilnRecipes" in recipe_guide:
	fail("Advanced Kiln recipe guide still contains the removed Wattled Daub recipe")
woven_screen = read("src/main/java/dev/projectflint/client/screen/WovenChestScreen.java")
woven_menu = read("src/main/java/dev/projectflint/menu/WovenChestMenu.java")
if "this.imageWidth = 184;" not in woven_screen or "addStandardInventorySlots(inventory, 11, 84);" not in woven_menu:
	fail("Woven Chest GUI width/slot alignment fix is missing")

# Minecraft 1.21.11 does not expose arbitrary custom recipes through ClientRecipeContainer,
# so the Clay Grinder guide keeps a validated client-safe mirror of the data recipes.
clay_data_entries = set()
for path in (RESOURCES / "data/projectflint/recipe").glob("*.json"):
	try:
		data = json.loads(path.read_text(encoding="utf-8"))
	except Exception:
		continue
	if data.get("type") != "projectflint:clay_grinding":
		continue
	result = data.get("result", {})
	clay_data_entries.add((
		str(data.get("ingredient")),
		int(data.get("input_count", 1)),
		str(result.get("id")),
		int(result.get("count", 1)),
	))
clay_guide_entries = {
	(a, int(b), c, int(d))
	for a, b, c, d in re.findall(r'new Definition\("([^"]+)", (\d+), "([^"]+)", (\d+)\)', clay_guide_registry)
}
if clay_data_entries != clay_guide_entries:
	missing = clay_data_entries - clay_guide_entries
	extra = clay_guide_entries - clay_data_entries
	fail(f"Clay Grinder guide mirror is out of sync (missing={len(missing)}, extra={len(extra)})")

# The single root and detailed charred-twig step are intentionally restored in v0.28.41-a.
if not (adv_root / "root.json").exists():
	fail("unified advancement root was removed")
if not (adv_root / "charred_twig.json").exists():
	fail("detailed Lightly Burnt Twig/charred-twig guidance was not restored")
for retired_root in ("00_tool_era", "01_clay_era", "02_wood_era", "tool_era", "clay_era", "wood_era"):
	if (adv_root / retired_root).exists():
		fail(f"retired split advancement root should be removed: {retired_root}")

# v0.28.26-a horse resources, visibility bypass, guidance restoration, Nether-wood cleanup, and aquifers.
animal_drops_2826 = read("src/main/java/dev/projectflint/system/PrimitiveAnimalDropSystem.java")
for expected in (
	"if (entity instanceof Horse)",
	"drops.removeIf(stack -> stack.is(Items.LEATHER))",
	"entity.getRandom().nextBoolean()) drops.add(new ItemStack(ModItems.RAW_LEATHER))",
	"if (entity instanceof Horse) return entity.getRandom().nextBoolean() ? 1 : 0",
):
	if expected not in animal_drops_2826:
		fail(f"v0.28.26-a horse resource rule missing: {expected}")

light_mixin_2826 = read("src/main/java/dev/projectflint/mixin/LightTextureMixin.java")
shader_2826 = read("src/main/resources/assets/minecraft/shaders/core/lightmap.fsh")
if "LightTextureMixin" not in active_mixins:
	fail("v0.28.26-a Creative/Night Vision lightmap adapter mixin is not active")
for expected in (
	"player.isCreative() || player.hasEffect(effect)",
	"player.isCreative()",
	"GameRenderer.getNightVisionScale(entity, partialTick)",
):
	if expected not in light_mixin_2826:
		fail(f"v0.28.26-a Creative/Night Vision bypass mixin missing: {expected}")
for expected in (
	"NightVisionFactor > 0.0",
	"vanillaColor(block_brightness, raw_sky_brightness)",
	"hardened_sky_factor *= hardened_sky_factor",
	"return;",
):
	if expected not in shader_2826:
		fail(f"v0.28.26-a lightmap bypass branch missing: {expected}")

# Unified detailed advancement policy supersedes the former three era backgrounds.
try:
	root_data = json.loads((adv_root / "root.json").read_text(encoding="utf-8"))
	if root_data.get("display", {}).get("background") != "minecraft:gui/advancements/backgrounds/stone":
		fail("unified advancement page background must remain stone")
except Exception as exc:
	fail(f"could not validate unified advancement background: {exc}")
for rel in (
	"direct_plant_string.json", "filed_bone.json", "bone_club.json", "bone_firestarter.json",
	"woven_wool.json", "sleeping_bag.json", "bone_brush.json", "unfired_clay_pot_mold.json",
	"fired_clay_pot_mold.json", "clay_pot.json", "flint_cutter_piece.json",
	"refined_flint_multitool_piece.json",
):
	if not (adv_root / rel).exists():
		fail(f"restored progression advancement missing: {rel}")

# Nether woods must not be processor recipes in either data or the guide mirror.
for rel in (
	"src/main/resources/data/projectflint/recipe/crimson_slab_from_stripped_log.json",
	"src/main/resources/data/projectflint/recipe/crimson_stripped_log_from_slabs.json",
	"src/main/resources/data/projectflint/recipe/warped_slab_from_stripped_log.json",
	"src/main/resources/data/projectflint/recipe/warped_stripped_log_from_slabs.json",
):
	if (ROOT / rel).exists():
		fail(f"Nether wood grinder recipe must be removed: {rel}")
for forbidden in ("crimson_stem", "crimson_hyphae", "warped_stem", "warped_hyphae", "crimson_slab", "warped_slab"):
	if forbidden in clay_guide_registry:
		fail(f"Clay Grinder guide still exposes Nether wood processing: {forbidden}")

# v0.28.28-a finite surface-water rule.
finite_water = read("src/main/java/dev/projectflint/system/FiniteWaterSystem.java")
project_bootstrap_2828 = read("src/main/java/dev/projectflint/ProjectFlint.java")
for expected in (
	"ServerLifecycleEvents.SERVER_STARTED.register",
	"GameRules.WATER_SOURCE_CONVERSION",
	"rules.set(GameRules.WATER_SOURCE_CONVERSION, false, server)",
):
	if expected not in finite_water:
		fail(f"finite-water system missing: {expected}")
if "FiniteWaterSystem.initialize();" not in project_bootstrap_2828:
	fail("FiniteWaterSystem is not initialized from ProjectFlint")
if "APPLY_PROJECTFLINT_0_28_28_A.bat" not in cleanup_bat:
	fail("v0.28.30-a cleanup must retire the v0.28.28-a apply BAT")

# v0.28.27-a seed-deterministic virtual groundwater.
aquifer_system_2827 = read("src/main/java/dev/projectflint/system/AquiferSystem.java")
aquifer_finder_2827 = read("src/main/java/dev/projectflint/item/AquiferFinderItem.java")
worldgen_2827 = read("src/main/java/dev/projectflint/world/ModWorldgen.java")
project_bootstrap_2827 = read("src/main/java/dev/projectflint/ProjectFlint.java")
item_model_2827 = read("src/main/resources/assets/projectflint/items/aquifer_finder.json")
for expected in (
	"CELL_SIZE = 32", "EXISTENCE_DENOMINATOR = 7", "level.getSeed()",
	"MIN_DEPTH = 20", "DEPTH_VARIANCE = 7", "findNearby(ServerLevel",
	"findBelow(ServerLevel", "footprint(AquiferHit", "SCARCE(0.13F",
	"NORMAL(0.68F", "GOOD(0.94F", "ABUNDANT(1.30F",
):
	if expected not in aquifer_system_2827:
		fail(f"virtual aquifer field missing: {expected}")
for forbidden in ("Blocks.WATER", "Blocks.CLAY", "setBlock(", "SavedData", "Attachment"):
	if forbidden in aquifer_system_2827:
		fail(f"virtual aquifer system must not depend on physical/stored aquifer state: {forbidden}")
for expected in ("player.isCreative()", "SEARCH_RADIUS = 96", "AquiferSystem.findNearby(serverLevel", "AquiferSystem.footprint(hit)"):
	if expected not in aquifer_finder_2827:
		fail(f"Aquifer Dowsing Rod behavior missing: {expected}")
for forbidden in ('placed("aquifer_humid")', 'placed("aquifer_temperate")', 'placed("aquifer_arid")'):
	if forbidden in worldgen_2827:
		fail(f"physical aquifer worldgen wiring must be retired: {forbidden}")
if "ModFeatures.initialize()" in project_bootstrap_2827 or "import dev.projectflint.registry.ModFeatures;" in project_bootstrap_2827:
	fail("obsolete aquifer Feature registry is still initialized")
for rel in (
	"src/main/java/dev/projectflint/world/feature/AquiferFeature.java",
	"src/main/java/dev/projectflint/registry/ModFeatures.java",
	"src/main/resources/data/projectflint/worldgen/configured_feature/aquifer.json",
	"src/main/resources/data/projectflint/worldgen/placed_feature/aquifer_humid.json",
	"src/main/resources/data/projectflint/worldgen/placed_feature/aquifer_temperate.json",
	"src/main/resources/data/projectflint/worldgen/placed_feature/aquifer_arid.json",
):
	if (ROOT / rel).exists():
		fail(f"obsolete physical aquifer artifact still present: {rel}")
	if rel.replace("/", "\\") not in cleanup_bat:
		fail(f"cleanup BAT does not retire physical aquifer artifact: {rel}")
if '"model": "projectflint:item/aquifer_finder"' not in item_model_2827:
	fail("Aquifer Dowsing Rod item definition is not using the Project Flint model")
for rel in (
	"src/main/resources/assets/projectflint/models/item/aquifer_finder.json",
	"src/main/resources/assets/projectflint/textures/item/aquifer_finder.png",
):
	if not (ROOT / rel).exists():
		fail(f"Aquifer Dowsing Rod resource missing: {rel}")
if '"item.projectflint.aquifer_finder": "Dowsing Rod"' not in read("src/main/resources/assets/projectflint/lang/en_us.json"):
	fail("Aquifer Dowsing Rod display name missing")

if "APPLY_PROJECTFLINT_0_28_28_A.bat" not in cleanup_bat:
	fail("v0.28.30-a cleanup does not retire the v0.28.28-a apply BAT")
for stale_recipe in ("crimson_slab_from_stripped_log.json", "crimson_stripped_log_from_slabs.json", "warped_slab_from_stripped_log.json", "warped_stripped_log_from_slabs.json"):
	if stale_recipe not in cleanup_bat:
		fail(f"cleanup BAT does not remove stale Nether wood recipe: {stale_recipe}")

# v0.28.30-a Wood -> Water Age part 1.
sanding_registry = read("src/main/java/dev/projectflint/recipe/SandingStationRecipeRegistry.java")
sanding_be = read("src/main/java/dev/projectflint/block/entity/SandingStationBlockEntity.java")
sanding_menu = read("src/main/java/dev/projectflint/menu/SandingStationMenu.java")
water_carrier = read("src/main/java/dev/projectflint/item/WaterBearingPotItem.java")
water_pot_be = read("src/main/java/dev/projectflint/block/entity/WaterPotBlockEntity.java")
lumber_system = read("src/main/java/dev/projectflint/system/LumberProcessorSystem.java")
lumber_be = read("src/main/java/dev/projectflint/block/entity/LumberChoppingBlockEntity.java")
lumber_workpiece = read("src/main/java/dev/projectflint/block/LumberWorkpieceBlock.java")
wooden_registry_2830 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in ("DURABILITY_PER_PAPER = 32", "MAX_DURABILITY = 256", "consumeDurability"):
	if expected not in sanding_be:
		fail(f"Sanding Station durability rule missing: {expected}")
for expected in ("PLANT_TOOL_BINDING", "WOVEN_FAN, 3", "GLASS_SHARD", "ADVANCED_KILN_GLASS", "RAW_LEATHER", "Items.LEATHER, 2", "ANIMAL_BONES", "FILED_BONE, 3", "OVERWORLD_WOOD_SLABS", "SANDED_WOOD_SLAB, 3", "ModItems.WOOL", "WOVEN_WOOL, 2"):
	if expected not in sanding_registry:
		fail(f"Sanding Station recipe missing: {expected}")
for forbidden in ("CRIMSON", "WARPED"):
	if forbidden in sanding_registry:
		fail(f"Sanding Station must reject Nether wood slabs: {forbidden}")
if "station.consumeDurability()" not in sanding_menu:
	fail("Sanding Station crafts do not spend one sanding durability")
for expected in ("WOVEN_WOOD), 16", "WATER_BEARING_POT", "Items.LEATHER), 4", "WATER_POT", "FLINT_AXE_PIECES", "BOUND_WOOD_PROCESSOR", "SANDED_WOOD_SLAB), 8", "Items.LEATHER), 5", "SANDING_STATION"):
	if expected not in wooden_registry_2830:
		fail(f"Wooden Grinder Wood -> Water recipe missing: {expected}")
for expected in ("MAX_UNITS = 2", "consumeFullBucket", "linkedController"):
	if expected not in water_pot_be:
		fail(f"Water Pot reservoir behavior missing: {expected}")
for expected in ("WATER_BEARING_POT_FILLED", "pot.addHalfBucket()", "Blocks.WATER"):
	if expected not in water_carrier:
		fail(f"Water Bearing Pot behavior missing: {expected}")
if "level.getBlockState(pos).is(Blocks.WATER)" not in water_carrier:
	fail("Water Bearing Pot must not delete waterlogged blocks while collecting water")
for expected in ("WOVEN_WOOD", "WATER_POT", "CRUSHED_STONE", "LUMBER_CHOPPING_BLOCK", "ADVANCED_FLINT_TIER", "FLINT_PLANT_CUTTERS", "1.0F / 150.0F", "WASH_TICKS = 10 * 20", "WOODEN_BOARD, 2", "WOODEN_BEAM"):
	if expected not in (lumber_system + lumber_be + block_state_mixin):
		fail(f"Lumber Processor rule missing: {expected}")
if "getShape" in lumber_workpiece or "getCollisionShape" in lumber_workpiece or "HALF_SLAB" in lumber_workpiece:
	fail("Lumber Processor washed workpiece must use the default full-block/chopping-block shape")
lumber_workpiece_model = read("src/main/resources/assets/projectflint/models/block/lumber_workpiece.json")
for expected in ("minecraft:block/cube_bottom_top", "projectflint:block/wooden_beam", "projectflint:block/wooden_beam_top"):
	if expected not in lumber_workpiece_model:
		fail(f"v0.31.3-b washed workpiece Wooden Beam model missing: {expected}")
for forbidden in ("STRIPPED_CRIMSON", "STRIPPED_WARPED"):
	if forbidden in lumber_system:
		fail(f"Lumber Processor must reject Nether stems: {forbidden}")
# Minecraft/Project Flint advancements were intentionally retired in v0.31.20-b in favor of Flint Quests.
if adv_root.exists():
	fail("retired Project Flint advancement directory has reappeared")
cleanup_2837 = read("APPLY_PROJECTFLINT_0_31_48_B.bat")
if "APPLY_PROJECTFLINT_0_28_36_A.bat" not in cleanup_2837:
	fail("v0.28.41-a cleanup must retire the previous v0.28.36-a apply BAT")

# v0.28.31-a Lumber Processor staged-log interaction hotfix.
lumber_system_2831 = read("src/main/java/dev/projectflint/system/LumberProcessorSystem.java")
for expected in (
    "isSupportedStrippedLog(level.getBlockState(controller.above()))",
    "!be.washing() && !be.outputReady()",
    "!be.armInput() || !be.startWash()",
):
    if expected not in lumber_system_2831:
        fail(f"v0.28.31-a Lumber Processor staged-log fix missing: {expected}")

lumber_entity_2831 = read("src/main/java/dev/projectflint/block/entity/LumberChoppingBlockEntity.java")
if "if (washing() || outputReady) return false;" not in lumber_entity_2831 or "public boolean armInput()" not in lumber_entity_2831:
    fail("v0.28.31-a Lumber Processor output-selection/input-arm behavior missing")

primitive_2831 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
for expected in (
    "Use Flint Plant Cutters to process this log.",
    "The Lumber Processor is incomplete or unsupported.",
    "Fill the Water Pot to 1.0 bucket first.",
):
    if expected not in primitive_2831:
        fail(f"v0.28.31-a Lumber Processor readiness feedback missing: {expected}")

# v0.28.32-a client/server Lumber Processor mining prediction hotfix.
if "if (level.isClientSide()) return InteractionResult.PASS;" not in primitive_2831:
    fail("v0.28.32-a Lumber Processor client attack path still validates unsynchronized block entities")
if "!actualLevel.isClientSide()" not in block_state_mixin:
    fail("v0.28.32-a staged-log destroy progress still requires server-authoritative machine state on the client")
if "held.is(ModItems.FLINT_PLANT_CUTTERS)" not in block_state_mixin or "1.0F / 150.0F" not in block_state_mixin:
    fail("v0.28.32-a staged-log Flint Plant Cutter 7.5-second prediction missing")

# Flint Plant Cutters: Advanced Flint Multitool plant tier without unrelated generic Cutter authority.
tool_caps = read("src/main/java/dev/projectflint/system/ToolCapabilities.java")
primitive_interactions = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
break_speed = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
server_breaks = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
if "case CUTTER ->" in tool_caps:
	cut_line = next((line for line in tool_caps.splitlines() if "case CUTTER ->" in line), "")
	if "FLINT_PLANT_CUTTERS" in cut_line:
		fail("Flint Plant Cutters must not inherit unrelated generic CUTTER capability")
for expected in (
	"isAdvancedPlantTier",
	"stack.is(ModItems.FLINT_PLANT_CUTTERS) || stack.is(ModItems.ADVANCED_FLINT_MULTITOOL)",
	"ADVANCED_FLINT_MULTITOOL_SPEED_MULTIPLIER",
	"canDirectHarvestPlant",
	"canHarvestLeaves",
):
	if expected not in tool_caps:
		fail(f"Flint Plant Cutters advanced plant-tier capability missing: {expected}")
for body_name, body in (("PrimitiveInteractionSystem", primitive_interactions), ("BlockStateBaseMixin", break_speed), ("ServerPlayerGameModeMixin", server_breaks)):
	if "isPlantCutterLike(held)" not in body:
		fail(f"{body_name} must use plant-cutter-aware foliage gates")
for expected in (
	"ToolCapabilities.isAdvancedPlantTier(held) ? 3",
	"ToolCapabilities.isAdvancedPlantTier(held) ? 6",
	"if (ToolCapabilities.isAdvancedPlantTier(held))",
	"twigCount = 8",
):
	if expected not in server_breaks:
		fail(f"Flint Plant Cutters advanced plant-tier yield missing: {expected}")
if "held.is(ModItems.FLINT_PLANT_CUTTERS) && state.is(Blocks.SHORT_GRASS)" not in server_breaks or "new ItemStack(ModItems.PLANT_TOOL_BINDING)" not in server_breaks:
	fail("Flint Plant Cutters must preserve Short Grass -> Plant Tool Binding")


# v0.28.35-a Sanding Station UI rebuild.
sanding_screen = read("src/main/java/dev/projectflint/client/screen/SandingStationScreen.java")
sanding_menu = read("src/main/java/dev/projectflint/menu/SandingStationMenu.java")
for expected in (
    "imageHeight = 182",
    "inventoryLabelY = 88",
    "Sanding Reserve",
    "RESERVE_WIDTH = 152",
    "MAX_DURABILITY / 32",
    "1 Sanding Paper = 32 reserve",
):
    if expected not in sanding_screen:
        fail(f"v0.28.35-a Sanding Station UI rule missing: {expected}")
for expected in (
    "new Slot(input, 0, 44, 35)",
    "new Slot(result, 0, 116, 35)",
    "addStandardInventorySlots(inventory, 8, 100)",
):
    if expected not in sanding_menu:
        fail(f"v0.28.35-a Sanding Station menu alignment missing: {expected}")
if 'graphics.drawString(font, "Sanding " + durability + "/256"' in sanding_screen:
    fail("legacy Sanding durability text-over-bar rendering returned")

# v0.28.36-a Sugar Cane output specialization.
cane_breaks = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
roll_gate = 'state.is(Blocks.SUGAR_CANE) && held.is(ModItems.FLINT_PLANT_CUTTERS)'
if roll_gate not in cane_breaks:
    fail("v0.28.36-a Sugarcane Roll branch must be exclusive to Flint Plant Cutters")
for forbidden in (
    'held.is(ModItems.FLINT_PLANT_CUTTERS) || held.is(ModItems.ADVANCED_FLINT_MULTITOOL)',
    'held.is(ModItems.ADVANCED_FLINT_MULTITOOL) || held.is(ModItems.FLINT_PLANT_CUTTERS)',
):
    if forbidden in cane_breaks:
        fail("Advanced Flint Multitool must not share the Sugarcane Roll output branch")
if 'state.is(Blocks.SUGAR_CANE) && ModItems.isPlantCutterLike(held)' not in cane_breaks or 'new ItemStack(Items.SUGAR_CANE, harvested)' not in cane_breaks:
    fail("Advanced Flint Multitool/general plant cutters must retain normal Sugar Cane harvesting")

# Wood -> Water texture baseline plus v0.31.0-b non-directional Water Pot revision.
for tex in (
    "src/main/resources/assets/projectflint/textures/block/sanding_station_top.png",
    "src/main/resources/assets/projectflint/textures/block/lumber_processor_top.png",
    "src/main/resources/assets/projectflint/textures/block/lumber_processor_side.png",
    "src/main/resources/assets/projectflint/textures/block/lumber_processor_bottom.png",
    "src/main/resources/assets/projectflint/textures/block/water_pot_top.png",
    "src/main/resources/assets/projectflint/textures/block/water_pot_side.png",
    "src/main/resources/assets/projectflint/textures/block/water_pot_bottom.png",
):
    if not (ROOT / tex).exists():
        fail(f"Wood/Water texture missing: {tex}")
water_pot_block_2837 = read("src/main/java/dev/projectflint/block/WaterPotBlock.java")
for forbidden in ("EnumProperty<Direction> FACING", "builder.add(FACING)", "getStateForPlacement"):
    if forbidden in water_pot_block_2837:
        fail(f"v0.31.0-b Water Pot must be non-directional: {forbidden}")
if (ROOT / "src/main/resources/assets/projectflint/textures/block/water_pot_front.png").exists():
    fail("v0.31.0-b obsolete Water Pot front texture must be removed")
for model_path, expected_texture in (
    ("src/main/resources/assets/projectflint/models/block/sanding_station.json", "sanding_station_top"),
    ("src/main/resources/assets/projectflint/models/block/lumber_chopping_block.json", "lumber_processor_top"),
    ("src/main/resources/assets/projectflint/models/block/water_pot.json", "water_pot_bottom"),
):
    if expected_texture not in read(model_path):
        fail(f"model wiring missing: {model_path} -> {expected_texture}")


# v0.28.41-a opening pacing and detailed-guide regression checks.
tuning_2839 = read("src/main/java/dev/projectflint/config/GameplayTuning.java")
for expected in (
	"GRASS_STRIP_PROGRESS_PER_TICK = 1.0F / 140.0F",
	"GRASS_FIBER_CHANCE = 0.58F",
	"REGULAR_ROCK_FAILURES_BEFORE_GUARANTEE = 3",
	"TWIG_IGNITION_SUCCESS_CHANCE = 0.65F",
):
	if expected not in tuning_2839:
		fail(f"v0.28.41-a opening tuning missing: {expected}")
stone_2839 = read("src/main/java/dev/projectflint/system/StoneWorkingSystem.java")
for expected in ("REGULAR_ROCK_FAILURES", "pitySuccess", "REGULAR_ROCK_FAILURES_BEFORE_GUARANTEE"):
	if expected not in stone_2839:
		fail(f"v0.28.41-a Rock-knapping pity logic missing: {expected}")
env_2839 = read("src/main/java/dev/projectflint/system/PrimitiveEnvironmentEffectSystem.java")
for expected in ("TERRIFIED_EFFECT_DELAY = 20 * 10", "elapsed >= TERRIFIED_EFFECT_DELAY", "TERRIFIED_DAMAGE_DELAY = 20 * 45"):
	if expected not in env_2839:
		fail(f"v0.28.41-a darkness grace missing: {expected}")
if "smoothstep(0.28, 0.88, raw_sky_factor)" not in shader:
	fail("v0.28.41-a later true-black night curve is missing")
if "held.is(ModItems.FLINT_MULTITOOL) ? 4 : 2" not in game_mode_mixin:
	fail("Crude Stone Cutters and Crude Multitool must both retain the 2 Plant Fiber full Leaf Litter yield")


# v0.28.41-a correction: cutter cost is one binding, with legacy extra-binding refund.
harvesting_assembly_2840 = read("src/main/java/dev/projectflint/block/HarvestingToolAssemblyBlock.java")
for expected in (
	"The starter binding is now the only binding required",
	"state.getValue(BINDINGS) < 1",
	"state.getValue(BINDINGS) > 1",
	"new ItemStack(ModItems.PLANT_TOOL_BINDING)",
):
	if expected not in harvesting_assembly_2840:
		fail(f"v0.28.41-a Crude Stone Cutters one-binding cost/refund logic missing: {expected}")
if "stack.consume(1, player)" in harvesting_assembly_2840[harvesting_assembly_2840.find("if (stack.is(ModItems.PLANT_TOOL_BINDING))"):harvesting_assembly_2840.find("if (stack.is(ModItems.LIGHTLY_BURNT_TWIG))")]:
	fail("Crude Stone Cutters assembly must not consume a second Plant Tool Binding")

# v0.28.41-a progression-critical Crude Multitool foliage restoration.
tool_caps_2841 = read("src/main/java/dev/projectflint/system/ToolCapabilities.java")
leaf_method_start = tool_caps_2841.find("public static boolean canHarvestLeaves")
leaf_method_end = tool_caps_2841.find("\n\t}", leaf_method_start)
leaf_method = tool_caps_2841[leaf_method_start:leaf_method_end]
if "ModItems.CRUDE_MULTITOOL" not in leaf_method:
	fail("v0.28.41-a Crude Multitool must remain authorized to harvest leaves")
for expected in ("GameplayTuning.MULTITOOL_LEAF_TWIGS", "held.is(ModItems.FLINT_MULTITOOL) ? 2 : 1"):
	if expected not in game_mode_mixin:
		fail(f"v0.28.41-a Crude Multitool leaf yield path missing: {expected}")


# v0.31.0-b Wood/Water expansion and regression coverage.
# Loose Dirt is a valid vanilla Sugar Cane substrate through the Minecraft dirt tag.
try:
	dirt_tag = json.loads(read("src/main/resources/data/minecraft/tags/block/dirt.json"))
	if "projectflint:loose_dirt" not in dirt_tag.get("values", []):
		fail("v0.31.0-b Loose Dirt must be included in minecraft:dirt for Sugar Cane placement")
except Exception as exc:
	fail(f"could not validate Loose Dirt tag: {exc}")

primitive_310 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
if "Ordinary blocks may replace water again" not in primitive_310:
	fail("v0.31.0-b ordinary block-in-water placement regression fix is missing")
if "held.is(ModItems.DIRT_PILE) || held.is(ModItems.DAUB_PILE)" not in primitive_310:
	fail("Dirt/Daub pile special water-processing route must remain while ordinary block placement is restored")
for expected in ("pot.isFull()", "held.is(ModItems.DIRT_PILE)", "new ItemStack(ModItems.MUD_PILE)", "held.is(ModItems.REED_ROLL)", "new ItemStack(ModItems.SUGARCANE_ROLL)"):
	if expected not in primitive_310:
		fail(f"full Water Pot washing path missing: {expected}")

lumber_310 = read("src/main/java/dev/projectflint/system/LumberProcessorSystem.java")
if "context.getHorizontalDirection().getClockWise()" not in lumber_310:
	fail("recovered Lumber Processor must place its footprint horizontally across the player")
for expected in ("isProcessorPart", "WATER_POT", "CRUSHED_STONE"):
	if expected not in lumber_310 + primitive_310:
		fail(f"formed Lumber Processor part protection missing: {expected}")

# Leaf pacing and dedicated Plant Cutter loot specialization.
leaf_caps_310 = read("src/main/java/dev/projectflint/system/ToolCapabilities.java")
for expected in ("1.0F / 44.0F", "1.0F / 37.5F", "1.0F / 40.5F", "1.0F / 50.0F"):
	if expected not in leaf_caps_310:
		fail(f"v0.31.3-b leaf pacing constant missing: {expected}")
for expected in ("state.is(BlockTags.LEAVES) && held.is(ModItems.FLINT_PLANT_CUTTERS)", "< 0.25F", "< 0.10F", "projectflint$saplingForLeaves(state)"):
	if expected not in server_game_mode:
		fail(f"v0.31.3-b Flint Plant Cutter leaf specialization missing: {expected}")

# Shared Clay/Wood Grinder single-input recipes.
for recipe_path, ingredient, count, result_id, result_count in (
	("src/main/resources/data/projectflint/recipe/fired_clay_from_crude_clay_block.json", "projectflint:crude_clay_block", 1, "projectflint:fired_clay", 4),
	("src/main/resources/data/projectflint/recipe/twigs_from_branch.json", "projectflint:branch", 1, "projectflint:twig", 2),
):
	try:
		d = json.loads(read(recipe_path))
		if d.get("type") != "projectflint:clay_grinding" or d.get("ingredient") != ingredient or int(d.get("input_count", 1)) != count:
			fail(f"shared grinder recipe input mismatch: {recipe_path}")
		r = d.get("result", {})
		if r.get("id") != result_id or int(r.get("count", 1)) != result_count:
			fail(f"shared grinder recipe output mismatch: {recipe_path}")
	except Exception as exc:
		fail(f"could not validate shared grinder recipe {recipe_path}: {exc}")

advanced_310 = read("src/main/java/dev/projectflint/block/entity/AdvancedKilnBlockEntity.java")
for expected in ("CRUDE_CLAY_BLOCK_PROCESS_TICKS = 20 * 20", "new ItemStack(ModItems.CRUDE_CLAY_BLOCK, 3)", "ModItems.UNFIRED_CLAY_BALL, 8", "new ProcessDefinition(2", "true, false"):
	if expected not in advanced_310:
		fail(f"20-second Advanced Kiln Crude Clay Block process missing: {expected}")

bound_model_310 = read("src/main/resources/assets/projectflint/models/item/bound_wood_processor.json")
if "projectflint:block/lumber_processor_top" not in bound_model_310:
	fail("Bound Wood Processor must use the Lumber Processor top texture")

# Sanding Station geometry + recipes.
try:
	sanding_model = json.loads(read("src/main/resources/assets/projectflint/models/block/sanding_station.json"))
	elements = sanding_model.get("elements", [])
	if len(elements) != 1 or elements[0].get("to", [None, None, None])[1] != 4:
		fail("Sanding Station must be quarter-block tall (4/16)")
	textures = sanding_model.get("textures", {})
	if textures.get("side") != "projectflint:block/sanded_wood" or textures.get("bottom") != "projectflint:block/sanded_wood":
		fail("Sanding Station sides/bottom must use Sanded Wood texture")
except Exception as exc:
	fail(f"could not validate Sanding Station model: {exc}")
sanding_recipes_310 = read("src/main/java/dev/projectflint/recipe/SandingStationRecipeRegistry.java")
for expected in ("Ingredient.of(ModItems.GLASS_SHARD), 3", "Ingredient.of(ModItems.SAND_FLAKE), 3", "new ItemStack(ModItems.SAND_BALL)"):
	if expected not in sanding_recipes_310:
		fail(f"Sanding Station recipe update missing: {expected}")

wood_recipes_310 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	"Ingredient.of(Items.LEATHER), 3, Ingredient.of(ModItems.WOVEN_WOOD), 6",
	"Ingredient.of(ModItems.SUGARCANE_ROLL), 3, Ingredient.of(ModItems.PLANT_TOOL_BINDING), 1, new ItemStack(ModItems.SANDING_PAPER, 2)",
	"Ingredient.of(ModItems.WOODEN_BOARD), 2, Ingredient.of(ModItems.UNLIT_FLINT_TORCH), 1, new ItemStack(ModItems.WOOD_FIRESTARTER)",
	"Ingredient.of(Items.STICK), 2, Ingredient.of(ModItems.FILED_BONE), 3, new ItemStack(ModItems.BONE_HOE)",
	"Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.WOVEN_WOOL), 4, maxDamageStack(Items.LEATHER_BOOTS, 512)",
	"Ingredient.of(Items.LEATHER), 5, Ingredient.of(ModItems.WOVEN_WOOL), 5, maxDamageStack(Items.LEATHER_HELMET, 512)",
	"Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(ModItems.BOOKSHELF)",
	"fullDurabilityRecipe(Ingredient.of(ModItems.WOODEN_SCREW), 2, Ingredient.of(ModItems.BONE_BRUSH), 1, ProjectFlintBrushSystem.createBrush())",
):
	if expected not in wood_recipes_310:
		fail(f"Wooden Grinder v0.31.0-b recipe missing: {expected}")

sleep_block_310 = read("src/main/java/dev/projectflint/block/SleepingBagBlock.java")
sleep_item_310 = read("src/main/java/dev/projectflint/item/WovenWoolItem.java")
if "DyeColor.GREEN" not in sleep_block_310 or "DyeColor.GREEN" not in sleep_item_310:
	fail("new Sleeping Bags must default to green")

brush_system_310 = read("src/main/java/dev/projectflint/system/ProjectFlintBrushSystem.java")
brush_mixin_310 = read("src/main/java/dev/projectflint/mixin/BrushItemMixin.java")
for expected in ("DURABILITY = 512", "BRUSH_TICKS = 50", "4 + sheep.getRandom().nextInt(2)"):
	if expected not in brush_system_310:
		fail(f"Project Flint Brush tuning missing: {expected}")
for expected in ("new ItemStack(ModItems.SAND_FLAKE, 2)", "new ItemStack(Items.FLINT)", "new ItemStack(ModItems.FLINT_CHIP, 2)", "Blocks.CLAY.defaultBlockState()"):
	if expected not in brush_mixin_310:
		fail(f"Project Flint Brush block output missing: {expected}")
if "BrushItemMixin" not in active_mixins:
	fail("BrushItemMixin must be active for the 512-durability Project Flint Brush")

water_item_310 = read("src/main/java/dev/projectflint/item/WaterPotItem.java")
water_block_310 = read("src/main/java/dev/projectflint/block/WaterPotBlock.java")
water_break_310 = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
for expected in ("ProjectFlintWaterUnits", "DataComponents.CUSTOM_DATA", "pot.setWaterUnits"):
	if expected not in water_item_310:
		fail(f"Water Pot retained-water item data missing: {expected}")
if "Deliberately non-directional" not in water_block_310:
	fail("Water Pot non-directional block implementation marker missing")
if "WaterPotItem.withWater(new ItemStack(ModItems.WATER_POT), units)" not in water_break_310:
	fail("Water Pot Survival break must preserve water units on the dropped item")

# Wooden Chest uses vanilla chest combination behavior with reduced capacity.
wood_chest_block_310 = read("src/main/java/dev/projectflint/block/WoodenChestBlock.java")
wood_chest_be_310 = read("src/main/java/dev/projectflint/block/entity/WoodenChestBlockEntity.java")
wood_chest_menu_310 = read("src/main/java/dev/projectflint/menu/WoodenChestMenu.java")
woven_chest_310 = read("src/main/java/dev/projectflint/block/WovenChestBlock.java")
mortar_chest_310 = read("src/main/java/dev/projectflint/block/MortarChestBlock.java")
for expected in ("extends ChestBlock", "ChestBlock.getContainer", "rows == 2 ?"):
	if expected not in wood_chest_block_310:
		fail(f"Wooden Chest vanilla chest integration missing: {expected}")
if "SLOT_COUNT = 9" not in wood_chest_be_310 or "extends ChestMenu" not in wood_chest_menu_310 or "new SimpleContainer(18)" not in wood_chest_menu_310 or "getRowCount()" not in wood_chest_menu_310:
	fail("Wooden Chest must use vanilla ChestMenu lifecycle with 9 slots single / 18 slots double")
for expected in ("stack.is(ModItems.WOODEN_CHEST_FRAME)", "stack.shrink(1)", "ModBlocks.WOODEN_CHEST"):
	if expected not in woven_chest_310:
		fail(f"Woven Chest -> Wooden Chest frame conversion missing: {expected}")
if "stack.is(ModItems.WOODEN_BOARD)" in mortar_chest_310 and "ModBlocks.WOODEN_CHEST" in mortar_chest_310:
	fail("Wooden Chest upgrade must not originate from the Mortar Chest")
if "SheetsMixin" not in active_mixins:
	fail("Wooden Chest custom vanilla-style chest renderer material hook is missing")

# The Sawmill remains staged Creative-only. Beams become survival placeable in v0.31.24-b.
items_310 = read("src/main/java/dev/projectflint/registry/ModItems.java")
if "new CreativeOnlyBlockItem(ModBlocks.SAWMILL" not in items_310:
	fail("creative-only Sawmill registration missing")

# Bookshelf knowledge station foundation.
bookshelf_block_310 = read("src/main/java/dev/projectflint/block/BookshelfBlock.java")
bookshelf_be_310 = read("src/main/java/dev/projectflint/block/entity/BookshelfBlockEntity.java")
bookshelf_menu_310 = read("src/main/java/dev/projectflint/menu/BookshelfMenu.java")
bookshelf_item_310 = read("src/main/java/dev/projectflint/item/BookshelfItem.java")
for expected in ("getBooksStored()<4", "ModAdvancements.awardAll(sp,\"filled_bookshelf\")", "HorizontalDirectionalBlock.FACING"):
	if expected not in bookshelf_block_310:
		fail(f"Bookshelf block behavior missing: {expected}")
for expected in ("Math.min(4,books)", "BooksStored", "BookshelfBlock.FILLED"):
	if expected not in bookshelf_be_310:
		fail(f"Bookshelf persistent fill state missing: {expected}")
bookshelf_recipes_312 = read("src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java")
for expected in ("Ingredient.of(ModItems.SUGARCANE_ROLL), 8", "Ingredient.of(Items.LEATHER), 2", "new ItemStack(Items.BOOK)"):
	if expected not in bookshelf_recipes_312:
		fail(f"Bookshelf Book recipe missing: {expected}")
if "BookshelfRecipeRegistry.find" not in bookshelf_menu_310:
	fail("Bookshelf menu must consume the shared Bookshelf recipe registry")
if "ProjectFlintBooks" not in bookshelf_item_310:
	fail("Bookshelf item must retain inserted-book state when moved")

# Firestarter, Bone armor, and Bone Hoe.
if "BONE_FIRESTARTER_DURABILITY * 3 / 2" not in items_310:
	fail("Wood Firestarter must have 1.5x Bone Firestarter durability")
fire_caps_310 = read("src/main/java/dev/projectflint/system/ToolCapabilities.java")
if "ModItems.WOOD_FIRESTARTER" not in fire_caps_310:
	fail("Wood Firestarter must participate in the firestarter capability")
for item_name in ("BONE_HELMET", "BONE_CHESTPLATE", "BONE_LEGGINGS", "BONE_BOOTS"):
	if item_name not in items_310 or ".durability(128)" not in items_310:
		fail(f"Bone armor registration/durability missing: {item_name}")
for recipe_name, count in (("bone_helmet.json",5),("bone_chestplate.json",8),("bone_leggings.json",7),("bone_boots.json",4)):
	try:
		d = json.loads(read("src/main/resources/data/projectflint/recipe/"+recipe_name))
		if d.get("ingredient") != "projectflint:filed_bone" or int(d.get("input_count",0)) != count:
			fail(f"Bone armor Filed Bone count mismatch: {recipe_name}")
	except Exception as exc:
		fail(f"could not validate Bone armor recipe {recipe_name}: {exc}")
bone_hoe_310 = read("src/main/java/dev/projectflint/item/BoneHoeItem.java")
for expected in ("Blocks.GRASS_BLOCK", "FluidTags.WATER", "< 0.10F", "Blocks.FARMLAND.defaultBlockState()"):
	if expected not in bone_hoe_310:
		fail(f"Bone Hoe constrained tilling behavior missing: {expected}")
if 'BONE_HOE = register("bone_hoe", properties -> new BoneHoeItem(properties.durability(24)' not in items_310:
	fail("Bone Hoe must have 24 durability")

# Advanced tool assembly ground art must use the actual item PNG bytes.
import hashlib
for name in ("flint_axe_pieces", "flint_cutter_piece", "refined_flint_multitool_piece"):
	item_png = ROOT / f"src/main/resources/assets/projectflint/textures/item/{name}.png"
	placed_png = ROOT / f"src/main/resources/assets/projectflint/textures/block/placed/{name}.png"
	if not item_png.exists() or not placed_png.exists() or hashlib.sha256(item_png.read_bytes()).digest() != hashlib.sha256(placed_png.read_bytes()).digest():
		fail(f"placed advanced Flint piece must byte-match its item PNG: {name}")

# Detailed single-page advancement guide remains intact and covers the new systems.
advancement_files_310 = list((RESOURCES / "data/projectflint/advancement").rglob("*.json"))
if len(advancement_files_310) != 183:
	fail(f"v0.31.10-b expected 183 detailed advancements after Rope/researched-meal progression, got {len(advancement_files_310)}")
for adv in ("wooden_chest.json", "bookshelf.json", "bookshelf_book.json", "filled_bookshelf.json", "wood_firestarter.json", "bone_hoe.json", "flax_seeds.json", "bone_helmet.json", "bone_chestplate.json", "bone_leggings.json", "bone_boots.json", "fine_brush.json"):
	if not (RESOURCES / "data/projectflint/advancement" / adv).exists():
		fail(f"v0.31.0-b advancement missing: {adv}")

# Direct owner texture hashes from new update stuff(4).zip.
expected_texture_hashes = {
	"src/main/resources/assets/projectflint/textures/item/wooden_board.png": "98ab1e1e34194f9ef1aaaf8ab12a8a547d416749f4cdbe120c27f07016946079",
	"src/main/resources/assets/projectflint/textures/item/wood_firestarter.png": "504830a8b9f46b6297567acf5d0761848de84e26d8205ee6e7bd17f3dbfead24",
	"src/main/resources/assets/projectflint/textures/item/bone_hoe.png": "dad6e93779a6e8c21f69ffb23e4d6906dc9287d22c7f83514501f3882a28a63e",
	"src/main/resources/assets/projectflint/textures/block/water_pot_top.png": "6ba80d1b094f6bdf4427987d6c9b878e88bee46d9f650606be63e1c801aae9a5",
	"src/main/resources/assets/projectflint/textures/block/water_pot_side.png": "b13f6213fa079fb737ac50404af800073da666de8bf012cabe76670c4d50fc82",
	"src/main/resources/assets/projectflint/textures/block/water_pot_bottom.png": "7012e7f2dafacc35f5b57655eade88d733aa93daec24755b89a56b74d0aad0b7",
	"src/main/resources/assets/projectflint/textures/entity/chest/wooden_chest.png": "6e31cfa9cc702f1d812de6d0f7cbf2327d122a4256ab81cd5bec3c0d9d03e868",
	"src/main/resources/assets/projectflint/textures/block/bookshelf_empty.png": "22514060ef0e25c7a82fd6efa073c98ce03a8f23300907f511d7a82b28f6fbbd",
	"src/main/resources/assets/projectflint/textures/block/bookshelf_filled.png": "deec7cff6434f7ad30a392d9726efbe0b05a7f24d9a4534b311e8ccf5fda87e8",
	"src/main/resources/assets/projectflint/textures/block/sawmill_front.png": "cfd8625f3c7cc10eca640173c8bfe05e758c44b929c8d7e5385abbe6e27b033e",
	"src/main/resources/assets/projectflint/textures/block/wooden_beam.png": "fda68ab101c4a7afddd1cf3bf52d8e5375a19f601f36ff560efcef96826874cb",
	"src/main/resources/assets/projectflint/textures/entity/equipment/humanoid/bone.png": "a2c6fbf61e3f0b26531e9fb1e3bc357a0d19c18b64a6ae05269c43e0c2252d06",
}
for rel, expected_hash in expected_texture_hashes.items():
	path = ROOT / rel
	if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest() != expected_hash:
		fail(f"owner-supplied v0.31.0-b texture hash mismatch: {rel}")

if "water_pot_front.png" not in cleanup_bat or "APPLY_PROJECTFLINT_0_28_41_A.bat" not in cleanup_bat:
	fail("v0.31.0-b cleanup must remove the obsolete Water Pot front texture and retire v0.28.41-a APPLY BAT")


# v0.31.2-b Wood/Water workstation, asset, recipe, and dismantling repairs.
wood_chest_screen_312 = read("src/main/java/dev/projectflint/client/screen/WoodenChestScreen.java")
if "inventoryLabelY=20+menu.rows()*18" not in wood_chest_screen_312.replace(" ", ""):
	fail("Wooden Chest Inventory label must sit below the chest rows without overlap")
if "extends ChestMenu" not in wood_chest_menu_310:
	fail("Wooden Chest menu must extend vanilla ChestMenu so chest opener/lid tracking closes correctly")

wood_recipes_312 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	"Ingredient.of(ModItems.MORTAR_BRICKS), 8, Ingredient.of(ModItems.MUD_PILE), 16, new ItemStack(ModItems.TORCH_HOLDER)",
	"Ingredient.of(Items.FLINT), 12, Ingredient.of(ModItems.WOVEN_WOOD), 1, new ItemStack(ModItems.UNLIT_FLINT_TORCH)",
	"Ingredient.of(Items.LEATHER), 4, Ingredient.of(ModItems.WOVEN_WOOL), 4, maxDamageStack(Items.LEATHER_BOOTS, 512)",
):
	if expected not in wood_recipes_312:
		fail(f"v0.31.2-b Wooden Grinder recipe correction missing: {expected}")

bookshelf_screen_312 = read("src/main/java/dev/projectflint/client/screen/BookshelfScreen.java")
recipe_guide_312 = read("src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java")
for expected in ("RECIPE_PREVIEW_COUNT = 6", "addStandardInventorySlots(inventory, 8, 100)", "BookshelfRecipeRegistry.find"):
	if expected not in bookshelf_menu_310:
		fail(f"Bookshelf Wooden-Grinder-style menu integration missing: {expected}")
for expected in ("Station.BOOKSHELF", "x + 35", "x + 53", "x + 116", "y + 64"):
	if expected not in bookshelf_screen_312:
		fail(f"Bookshelf Wooden-Grinder-style screen missing: {expected}")
for expected in ("case BOOKSHELF -> bookshelfRecipes()", "BookshelfRecipeRegistry.recipes()", "BOOKSHELF(Component.literal(\"Bookshelf\"))"):
	if expected not in recipe_guide_312:
		fail(f"Bookshelf recipe-guide page missing: {expected}")

lang_312 = read("src/main/resources/assets/projectflint/lang/en_us.json")
try:
	lang_json_312 = json.loads(lang_312)
except Exception:
	lang_json_312 = {}
required_names_312 = {
	"item.projectflint.water_bearing_pot_filled": "Water Bearing Pot (Filled)",
	"item.projectflint.wooden_chest": "Wooden Chest",
	"item.projectflint.bookshelf": "Bookshelf",
	"item.projectflint.sawmill": "Sawmill",
	"item.projectflint.acacia_wooden_beam": "Acacia Wooden Beam",
	"item.projectflint.birch_wooden_beam": "Birch Wooden Beam",
	"item.projectflint.cherry_wooden_beam": "Cherry Wooden Beam",
	"item.projectflint.crimson_wooden_beam": "Crimson Wooden Beam",
	"item.projectflint.dark_oak_wooden_beam": "Dark Oak Wooden Beam",
	"item.projectflint.jungle_wooden_beam": "Jungle Wooden Beam",
	"item.projectflint.mangrove_wooden_beam": "Mangrove Wooden Beam",
	"item.projectflint.pale_oak_wooden_beam": "Pale Oak Wooden Beam",
	"item.projectflint.spruce_wooden_beam": "Spruce Wooden Beam",
	"item.projectflint.warped_wooden_beam": "Warped Wooden Beam",
}
for key, value in required_names_312.items():
	if lang_json_312.get(key) != value:
		fail(f"missing/incorrect v0.31.2-b item translation: {key}")

for rel, expected_texture in (
	("src/main/resources/assets/projectflint/models/item/wooden_board.json", "projectflint:item/wooden_board"),
	("src/main/resources/assets/projectflint/models/item/black_bear_spawn_egg.json", "projectflint:item/black_bear_spawn_egg"),
	("src/main/resources/assets/projectflint/models/item/brown_bear_spawn_egg.json", "projectflint:item/brown_bear_spawn_egg"),
):
	try:
		model = json.loads(read(rel))
		if model.get("parent") != "minecraft:item/generated" or model.get("textures", {}).get("layer0") != expected_texture:
			fail(f"item texture model is not wired correctly: {rel}")
	except Exception as exc:
		fail(f"could not validate v0.31.2-b item model {rel}: {exc}")
board_png_312 = ROOT / "src/main/resources/assets/projectflint/textures/item/wooden_board.png"
if not board_png_312.exists() or hashlib.sha256(board_png_312.read_bytes()).hexdigest() != "98ab1e1e34194f9ef1aaaf8ab12a8a547d416749f4cdbe120c27f07016946079":
	fail("Wooden Board must retain the owner-supplied texture bytes")

blocks_312 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
if "registerSawmill" not in blocks_312 or "new Block(BlockBehaviour.Properties.of().setId(key).noOcclusion().strength(1.5F).sound(SoundType.WOOD))" not in blocks_312:
	fail("Sawmill partial model must be registered non-occluding")

primitive_312 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
mining_312 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
server_312 = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
for body_name, body in (("PrimitiveInteractionSystem", primitive_312), ("BlockStateBaseMixin", mining_312), ("ServerPlayerGameModeMixin", server_312)):
	for station in ("SANDING_STATION", "BOOKSHELF", "WOODEN_GRINDER"):
		if station not in body or "FLINT_AXE" not in body:
			fail(f"{body_name} missing Flint Axe-only workstation gate for {station}")
if "LumberProcessorSystem.isProcessorPart(level, pos)" not in primitive_312 or "held.is(ModItems.FLINT_AXE)" not in primitive_312:
	fail("formed Lumber Processor interaction gate must require Flint Axe")
if "LumberProcessorSystem.isProcessorPart(level, pos)" not in server_312 or "LumberProcessorSystem.recover(level, pos, player)" not in server_312:
	fail("formed Lumber Processor must recover from linked parts, including the Water Pot")

sanding_item_312 = read("src/main/java/dev/projectflint/item/SandingStationItem.java")
sanding_be_312 = read("src/main/java/dev/projectflint/block/entity/SandingStationBlockEntity.java")
items_312 = read("src/main/java/dev/projectflint/registry/ModItems.java")
for expected in ("ProjectFlintSandingDurability", "DataComponents.CUSTOM_DATA", "station.setDurability", "withDurability"):
	if expected not in sanding_item_312:
		fail(f"Sanding Station reserve item persistence missing: {expected}")
if "public void setDurability" not in sanding_be_312:
	fail("Sanding Station block entity must restore reserve from its item")
if "new SandingStationItem(ModBlocks.SANDING_STATION" not in items_312:
	fail("Sanding Station must register its reserve-preserving BlockItem")
if "SandingStationItem.withDurability(new ItemStack(ModItems.SANDING_STATION), reserve)" not in server_312:
	fail("Sanding Station Flint Axe recovery must write stored reserve to the dropped item")

if "APPLY_PROJECTFLINT_0_31_1_B.bat" not in cleanup_bat:
	fail("v0.31.2-b cleanup lineage must still retire the v0.31.1-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_2_B.bat" not in cleanup_bat:
	fail("v0.31.3-b cleanup must retire the previous v0.31.2-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_3_B.bat" not in cleanup_bat:
	fail("v0.31.4-b cleanup must retire the previous v0.31.3-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_4_B.bat" not in cleanup_bat:
	fail("v0.31.6-b cleanup must retire the previous v0.31.4-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_5_B.bat" not in cleanup_bat:
	fail("v0.31.6-b cleanup must retire the previous v0.31.5-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_6_B.bat" not in cleanup_bat:
	fail("v0.31.7-b cleanup must retire the previous v0.31.6-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_7_B.bat" not in cleanup_bat:
	fail("v0.31.8-b cleanup lineage must still retire the previous v0.31.7-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_8_B.bat" not in cleanup_bat:
	fail("v0.31.9-b cleanup lineage must still retire the previous v0.31.8-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_9_B.bat" not in cleanup_bat:
	fail("v0.31.10-b cleanup must retire the previous v0.31.9-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_10_B.bat" not in cleanup_bat:
	fail("v0.31.11-b cleanup must retire the previous v0.31.10-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_11_B.bat" not in cleanup_bat:
	fail("v0.31.16-b cleanup must retire the previous v0.31.11-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_16_B.bat" not in cleanup_bat:
	fail("v0.31.17-b cleanup must retire the previous v0.31.16-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_18_B.bat" not in cleanup_bat:
	fail("v0.31.19-b cleanup must retire the previous v0.31.18-b apply BAT")
if "APPLY_PROJECTFLINT_0_31_19_B.bat" not in cleanup_bat:
	fail("v0.31.21-b cleanup must retire the previous v0.31.20-b apply BAT")

# v0.31.3-b Flint File woodworking, leaf tuning, armor, Bookshelf, and asset coverage.
bookshelf_menu_313 = read("src/main/java/dev/projectflint/menu/BookshelfMenu.java")
bookshelf_screen_313 = read("src/main/java/dev/projectflint/client/screen/BookshelfScreen.java")
for expected in ("previews.clearContent()", "BookshelfRecipeRegistry.findAll", "previews.setItem(i, activeRecipes.get(i).result().copy())"):
	if expected not in bookshelf_menu_313:
		fail(f"Bookshelf selectable recipe-preview state missing: {expected}")
if "for (int i = 0; i < BookshelfMenu.RECIPE_PREVIEW_COUNT; i++)" not in bookshelf_screen_313:
	fail("v0.31.4-b Bookshelf screen must keep the lower recipe-prompt wells visible")
if "if (menu.getSlot(BookshelfMenu.RESULT_SLOT).hasItem())" in bookshelf_screen_313:
	fail("v0.31.4-b Bookshelf must not hide the entire recipe-prompt strip when inputs are empty")

items_313 = read("src/main/java/dev/projectflint/registry/ModItems.java")
for expected in (
	'FLINT_FILE_HEAD = register("flint_file_head", properties -> new Item(properties.stacksTo(1)))',
	'FLINT_FILE = register("flint_file", properties -> new Item(properties.durability(128).stacksTo(1)))',
	'WOODEN_SHEET = register("wooden_sheet", properties -> new Item(properties.stacksTo(64)))',
	'WOODEN_SCREW = register("wooden_screw", properties -> new Item(properties.stacksTo(32)))',
	'WOODEN_DOOR_HINGE = register("wooden_door_hinge", properties -> new Item(properties.stacksTo(8)))',
	'projectflint$boneArmor(properties, ArmorType.CHESTPLATE, 1.5D)',
	'projectflint$boneArmor(properties, ArmorType.LEGGINGS, 1.5D)',
	'AttributeModifier.Operation.ADD_VALUE',
):
	if expected not in items_313:
		fail(f"v0.31.3-b item/armor registration missing: {expected}")

sanding_recipes_313 = read("src/main/java/dev/projectflint/recipe/SandingStationRecipeRegistry.java")
if "Ingredient.of(ModItems.FLINT_CUTTER_PIECE), 1, new ItemStack(ModItems.FLINT_FILE_HEAD)" not in sanding_recipes_313:
	fail("v0.31.3-b Sanding Station Flint File Head recipe missing")

wood_recipes_313 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	"Ingredient.of(ModItems.FLINT_FILE_HEAD), 1, Ingredient.of(Items.STICK), 1, new ItemStack(ModItems.FLINT_FILE)",
	"toolRecipe(Ingredient.of(ModItems.WOODEN_BEAM), 1, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.WOODEN_SHEET, 4))",
	"toolRecipe(Ingredient.of(ModItems.WOODEN_BOARD), 1, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.WOODEN_SCREW, 2))",
	"Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.WOODEN_SCREW), 4, new ItemStack(ModItems.WOODEN_DOOR_HINGE)",
	"Ingredient.of(ModItems.WATTLED_DAUB_BRICKS), 16, Ingredient.of(ModItems.WOODEN_DOOR_HINGE), 1, new ItemStack(ModItems.WATTLED_DAUB_DOOR)",
	"Ingredient.of(Items.LEATHER), 8, Ingredient.of(ModItems.WOVEN_WOOL), 8, maxDamageStack(Items.LEATHER_CHESTPLATE, 512)",
	"Ingredient.of(Items.LEATHER), 7, Ingredient.of(ModItems.WOVEN_WOOL), 7, maxDamageStack(Items.LEATHER_LEGGINGS, 512)",
	"true, false, 0, 1",
):
	if expected not in wood_recipes_313:
		fail(f"v0.31.3-b Wooden Grinder progression/catalyst recipe missing: {expected}")

wood_menu_313 = read("src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java")
for expected in ("dual.consumeA()", "dual.damageA()", "projectflint$applyDualInputUse", "tool.setDamageValue(nextDamage)"):
	if expected not in wood_menu_313:
		fail(f"v0.31.3-b Wooden Grinder retained-tool durability handling missing: {expected}")

creative_313 = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
for expected in ("output.accept(ModItems.FLINT_FILE_HEAD)", "output.accept(ModItems.FLINT_FILE)", "output.accept(ModItems.WOODEN_SHEET)", "output.accept(ModItems.WOODEN_SCREW)", "output.accept(ModItems.WOODEN_DOOR_HINGE)", "output.accept(ProjectFlintBrushSystem.createBrush())"):
	if expected not in creative_313:
		fail(f"v0.31.3-b creative inventory exposure missing: {expected}")

lang_313 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json"))
for key, value in {
	"item.projectflint.flint_file_head": "Flint File Head",
	"item.projectflint.flint_file": "Flint File",
	"item.projectflint.wooden_sheet": "Wooden Sheet",
	"item.projectflint.wooden_screw": "Wooden Screw",
	"item.projectflint.wooden_door_hinge": "Wooden Door Hinge",
}.items():
	if lang_313.get(key) != value:
		fail(f"v0.31.3-b translation missing/incorrect: {key}")

owner_assets_313 = {
	"flint_file_head.png": "d321b2bb0418b15338d80cdb9dcf3e0e67178faae3ef2f23059069a26fa57039",
	"flint_file.png": "8d1e2851155eafcfb9a75f48d0a7ccaa4e463f54bcf520d64c2b51dfb4797a84",
	"screw.png": "fbd54bbcacde1e7b4cf9c560c1881e114dcf48a3b47d733a0c5d91c4a5d4303f",
	"screw_secondary.png": "8d0ab3fc57dd666bde3403cf0b353313f06a7fd4d02d1b3f381222d45d69277a",
	"wooden_sheet.png": "c17e79215b44f8a00272c41d27732335532da6db3ae4ab78b326f56d12ff4815",
	"wooden_door_hinge.png": "cea3097bb82473793f722241981c336ec67223760c186ae89a319e8c78691bd1",
}
for filename, expected_hash in owner_assets_313.items():
	path = ROOT / "src/main/resources/assets/projectflint/textures/item" / filename
	if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.31.3-b owner-supplied texture hash mismatch: {filename}")
if (ROOT / "src/main/resources/assets/projectflint/textures/item/file_bottom for future use do not include.png").exists():
	fail("v0.31.3-b explicitly excluded future file-bottom texture must not be imported")

screw_model_313 = json.loads(read("src/main/resources/assets/projectflint/models/item/screw.json"))
if screw_model_313.get("textures", {}).get("layer0") != "projectflint:item/screw" or screw_model_313.get("textures", {}).get("layer1") != "projectflint:item/screw_secondary":
	fail("v0.31.3-b reusable screw model must retain the supplied primary/secondary layers")
wooden_screw_def_313 = json.loads(read("src/main/resources/assets/projectflint/items/wooden_screw.json"))
model_def_313 = wooden_screw_def_313.get("model", {})
if model_def_313.get("model") != "projectflint:item/screw" or model_def_313.get("tints") != [{"type": "minecraft:constant", "value": -10865377}]:
	fail("v0.31.3-b Wooden Screw must use the reusable screw model with the dark-brown constant material tint")

for adv in ("flint_file_head.json", "flint_file.json", "wooden_sheet.json", "wooden_screw.json", "wooden_door_hinge.json", "wattled_daub_door.json"):
	if not (RESOURCES / "data/projectflint/advancement" / adv).exists():
		fail(f"v0.31.3-b woodworking advancement missing: {adv}")

# v0.31.4-b Bookshelf recovery, component recipes, chest framing, and Sanding Station dye coverage.
bookshelf_registry_314 = read("src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java")
bookshelf_menu_314 = read("src/main/java/dev/projectflint/menu/BookshelfMenu.java")
bookshelf_screen_314 = read("src/main/java/dev/projectflint/client/screen/BookshelfScreen.java")
for expected in (
	"new ItemStack(Items.BOOK),",
	"new ItemStack(Items.WRITABLE_BOOK),",
	"Ingredient.of(ModItems.SUGARCANE_ROLL), 8",
	"Ingredient.of(Items.LEATHER), 2",
	"Ingredient.of(Items.BLACK_DYE), 2",
	"int requiredBooks",
	"booksStored >= requiredBooks",
):
	if expected not in bookshelf_registry_314:
		fail(f"v0.31.4-b Bookshelf recipe tier missing: {expected}")
if "new ItemStack(Items.WRITABLE_BOOK), 4" not in bookshelf_registry_314:
	fail("Book and Quill must require a full four-book Bookshelf")
for expected in (
	"private final SimpleContainer inputs = new SimpleContainer(2)",
	"if (!suppressRefresh) BookshelfMenu.this.slotsChanged(this)",
	"BookshelfRecipeRegistry.findAll(inputs.getItem(INPUT_A_SLOT), inputs.getItem(INPUT_B_SLOT), books.get())",
	"bookshelf_book_and_quill",
):
	if expected not in bookshelf_menu_314:
		fail(f"v0.31.4-b responsive Bookshelf menu missing: {expected}")
if "for (int i = 0; i < BookshelfMenu.RECIPE_PREVIEW_COUNT; i++)" not in bookshelf_screen_314:
	fail("v0.31.4-b Bookshelf recipe prompt row is not always drawn")
if "menu.getSelectedRecipeIndex() == i && menu.getSlot(BookshelfMenu.RECIPE_PREVIEW_START + i).hasItem()" not in bookshelf_screen_314:
	fail("Bookshelf prompt selection must remain conditional on a real selected preview item")

items_314 = read("src/main/java/dev/projectflint/registry/ModItems.java")
creative_314 = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
woven_chest_314 = read("src/main/java/dev/projectflint/block/WovenChestBlock.java")
wood_recipes_314 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	'WOODEN_CHEST_FRAME = register("wooden_chest_frame", properties -> new Item(properties.stacksTo(1)))',
):
	if expected not in items_314:
		fail(f"v0.31.4-b Wooden Chest Frame registration missing: {expected}")
if "output.accept(ModItems.WOODEN_CHEST_FRAME)" not in creative_314:
	fail("v0.31.4-b Wooden Chest Frame must be in the Project Flint creative tab")
for expected in ("stack.is(ModItems.WOODEN_CHEST_FRAME)", "stack.shrink(1)"):
	if expected not in woven_chest_314:
		fail(f"v0.31.4-b Woven Chest upgrade catalyst missing: {expected}")
if "stack.getCount() >= 16" in woven_chest_314 or "stack.is(ModItems.WOODEN_BOARD)" in woven_chest_314:
	fail("v0.31.4-b obsolete 16-Wooden-Board chest upgrade path is still active")
for expected in (
	"Ingredient.of(ModItems.WOODEN_SHEET), 4, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(ModItems.WOODEN_CHEST_FRAME)",
	"Ingredient.of(Items.STICK), 2, Ingredient.of(ModItems.FILED_BONE), 3, new ItemStack(ModItems.BONE_HOE)",
	"Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(ModItems.BOOKSHELF)",
	"fullDurabilityRecipe(Ingredient.of(ModItems.WOODEN_SCREW), 2, Ingredient.of(ModItems.BONE_BRUSH), 1, ProjectFlintBrushSystem.createBrush())",
):
	if expected not in wood_recipes_314:
		fail(f"v0.31.4-b Wooden Grinder retuning missing: {expected}")

try:
	wooden_grinder_base_314 = json.loads(read("src/main/resources/data/projectflint/recipe/wooden_grinder_base.json"))
	if int(wooden_grinder_base_314.get("input_count", 0)) != 12:
		fail("v0.31.4-b Wooden Grinder Base must cost 12 Woven Wood")
except Exception as exc:
	fail(f"could not validate v0.31.4-b Wooden Grinder Base recipe: {exc}")
if 'new Definition("projectflint:woven_wood", 12, "projectflint:wooden_grinder_base", 1)' not in read("src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java"):
	fail("v0.31.4-b Wooden Grinder Base recipe-guide mirror must also use 12 Woven Wood")

sanding_314 = read("src/main/java/dev/projectflint/recipe/SandingStationRecipeRegistry.java")
for expected in (
	"Ingredient.of(ModItems.BRANCH), 1, new ItemStack(ModItems.TWIG, 4)",
	"dyeRecipe(Items.INK_SAC, Items.BLACK_DYE, 2)",
	"dyeRecipe(Items.SUNFLOWER, Items.YELLOW_DYE, 4)",
	"dyeRecipe(Items.ROSE_BUSH, Items.RED_DYE, 4)",
	"dyeRecipe(Items.LILAC, Items.MAGENTA_DYE, 4)",
	"dyeRecipe(Items.PEONY, Items.PINK_DYE, 4)",
	"dyeRecipe(Items.PITCHER_PLANT, Items.CYAN_DYE, 4)",
):
	if expected not in sanding_314:
		fail(f"v0.31.4-b Sanding Station dye/twig recipe missing: {expected}")
for flower in (
	"DANDELION", "WILDFLOWERS", "SUNFLOWER", "POPPY", "RED_TULIP", "ROSE_BUSH", "BLUE_ORCHID", "ALLIUM", "LILAC",
	"AZURE_BLUET", "WHITE_TULIP", "OXEYE_DAISY", "ORANGE_TULIP", "TORCHFLOWER", "OPEN_EYEBLOSSOM", "PINK_TULIP",
	"PINK_PETALS", "CACTUS_FLOWER", "PEONY", "CORNFLOWER", "LILY_OF_THE_VALLEY", "WITHER_ROSE", "CLOSED_EYEBLOSSOM", "PITCHER_PLANT",
):
	if f"dyeRecipe(Items.{flower}" not in sanding_314:
		fail(f"v0.31.4-b missing Sanding Station flower dye mapping: {flower}")

for rel in (
	"src/main/resources/assets/projectflint/items/wooden_chest_frame.json",
	"src/main/resources/assets/projectflint/models/item/wooden_chest_frame.json",
	"src/main/resources/data/projectflint/advancement/wooden_chest_frame.json",
	"src/main/resources/data/projectflint/advancement/bookshelf_book_and_quill.json",
):
	if not (ROOT / rel).exists():
		fail(f"v0.31.4-b missing resource/progression file: {rel}")
lang_314 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json"))
if lang_314.get("item.projectflint.wooden_chest_frame") != "Wooden Chest Frame":
	fail("v0.31.4-b Wooden Chest Frame translation missing")


# v0.31.6-b Limestone, parchment storage, Water Pot tile washing, and Primitive Stove.
blocks_315 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
items_315 = read("src/main/java/dev/projectflint/registry/ModItems.java")
creative_315 = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
sanding_315 = read("src/main/java/dev/projectflint/recipe/SandingStationRecipeRegistry.java")
wood_315 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
processing_315 = read("src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java")
block_progress_315 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
server_break_315 = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
water_pot_315 = read("src/main/java/dev/projectflint/block/entity/WaterPotBlockEntity.java")
parchment_315 = read("src/main/java/dev/projectflint/item/RecipeParchmentItem.java")
cabinet_be_315 = read("src/main/java/dev/projectflint/block/entity/WoodenCabinetBlockEntity.java")
stove_be_315 = read("src/main/java/dev/projectflint/block/entity/PrimitiveStoveBlockEntity.java")
stove_recipes_315 = read("src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java")
worldgen_315 = read("src/main/java/dev/projectflint/world/ModWorldgen.java")

for expected in (
    'LIMESTONE = registerLimestoneBlock("limestone")',
    'COBBLED_LIMESTONE = registerLimestoneFallingBlock',
    'SMOOTH_LIMESTONE = registerLimestoneFallingBlock',
    'LIMESTONE_BRICKS = registerLimestoneBlock',
    'CHISELED_LIMESTONE = registerLimestoneBlock',
    'POLISHED_LIMESTONE = registerLimestoneBlock',
    'CHECKERED_TILE = registerLimestoneFallingBlock',
    'GLAZED_CHECKERED_TILE = registerLimestoneFallingBlock',
    'WOODEN_CABINET = registerWoodenCabinet',
    'PRIMITIVE_STOVE = registerPrimitiveStove',
):
    if expected not in blocks_315:
        fail(f"v0.31.6-b block registration missing: {expected}")
for expected in (
    'RECIPE_PARCHMENT = register("recipe_parchment", RecipeParchmentItem::new)',
    'CHECKERED_TILE = registerBlockItem("checkered_tile"',
    'WOODEN_CABINET = registerBlockItem("wooden_cabinet"',
    'PRIMITIVE_STOVE = registerBlockItem("primitive_stove"',
):
    if expected not in items_315:
        fail(f"v0.31.6-b item registration missing: {expected}")
for expected in ('output.accept(ModItems.RECIPE_PARCHMENT)', 'output.accept(ModItems.WOODEN_CABINET)', 'output.accept(ModItems.PRIMITIVE_STOVE)'):
    if expected not in creative_315:
        fail(f"v0.31.6-b creative entry missing: {expected}")

for expected in (
    'Ingredient.of(ModItems.COBBLED_LIMESTONE), 8, new ItemStack(ModItems.SMOOTH_LIMESTONE)',
    'Ingredient.of(ModItems.LIMESTONE_BRICKS), 1, new ItemStack(ModItems.POLISHED_LIMESTONE)',
    'Ingredient.of(ModItems.CHECKERED_TILE), 1, new ItemStack(ModItems.GLAZED_CHECKERED_TILE)',
    'Ingredient.of(ModItems.GLAZED_CHECKERED_TILE), 1, new ItemStack(ModItems.CHECKERED_TILE)',
    'Ingredient.of(Items.WRITABLE_BOOK), 1, new ItemStack(ModItems.RECIPE_PARCHMENT)',
):
    if expected not in sanding_315:
        fail(f"v0.31.6-b Sanding Station recipe missing: {expected}")
for expected in (
    'toolRecipe(Ingredient.of(ModItems.LIMESTONE), 4, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.LIMESTONE_BRICKS, 4))',
    'toolRecipe(Ingredient.of(ModItems.LIMESTONE_BRICKS), 4, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.CHISELED_LIMESTONE, 4))',
    'recipe(WOODEN_BEAMS, 1, Ingredient.of(ModItems.CRUSHED_STONE_BLOCK_ITEM), 1, new ItemStack(ModItems.WOODEN_CABINET))',
):
    if expected not in wood_315:
        fail(f"v0.31.6-b Wooden Grinder recipe missing: {expected}")
if 'recipe("limestone", ModItems.SMOOTH_LIMESTONE, new ItemStack(ModItems.LIMESTONE)' not in processing_315:
    fail("v0.31.6-b Smooth Limestone kiln firing recipe missing")

for expected in ('held.is(ModItems.ADVANCED_FLINT_MULTITOOL)', 'projectflint$isLimestoneFamily(state)'):
    if expected not in block_progress_315 or expected not in server_break_315:
        fail(f"v0.31.6-b Advanced Flint limestone mining gate missing: {expected}")
if 'if (state.is(ModBlocks.LIMESTONE)) return ModItems.COBBLED_LIMESTONE;' not in server_break_315:
    fail("v0.31.6-b natural Limestone must drop Cobbled Limestone")

for expected in (
    'count(items, ModItems.LIMESTONE_BRICKS) < 16',
    'count(items, Items.WHITE_DYE) < 1',
    'count(items, Items.BLACK_DYE) < 1',
    'consumeFullBucket()',
    'new ItemStack(ModItems.CHECKERED_TILE, 16)',
):
    if expected not in water_pot_315:
        fail(f"v0.31.6-b Water Pot tile ritual missing: {expected}")

for expected in ('properties.stacksTo(1)', 'ProjectFlintRecipe', 'if (recipeId == null || recipeId.isBlank() || isBound(stack)) return false'):
    if expected not in parchment_315:
        fail(f"v0.31.6-b immutable Recipe Parchment contract missing: {expected}")
for expected in ('SLOT_COUNT = 27', 'stack.is(ModItems.RECIPE_PARCHMENT)'):
    if expected not in cabinet_be_315:
        fail(f"v0.31.6-b parchment-only Wooden Cabinet missing: {expected}")
for expected in ('FUEL_LOGS_PER_IGNITION = 4', 'BURN_TICKS = 20 * 60 * 30', 'GameplayTuning.PRIMITIVE_STOVE_COOK_SPEED', 'RecipeParchmentItem.recipeId(parchment)'):
    if expected not in stove_be_315:
        fail(f"v0.31.6-b Primitive Stove tuning/recipe behavior missing: {expected}")
for expected in ('PrimitiveProcessingRegistry.recipesFor(PrimitiveProcessor.KILN)', 'findDirectFood'):
    if expected not in stove_recipes_315:
        fail(f"v0.31.6-b Primitive Stove recipe bridge missing: {expected}")

for expected in ('Biomes.RIVER', 'Biomes.FROZEN_RIVER', 'Biomes.BEACH', 'Biomes.SNOWY_BEACH', 'Biomes.STONY_SHORE', 'placed("limestone_disk")'):
    if expected not in worldgen_315:
        fail(f"v0.31.6-b Limestone biome scheduling missing: {expected}")
try:
    configured_315 = json.loads(read("src/main/resources/data/projectflint/worldgen/configured_feature/limestone_disk.json"))
    placed_315 = json.loads(read("src/main/resources/data/projectflint/worldgen/placed_feature/limestone_disk.json"))
    if configured_315.get("type") != "minecraft:disk": fail("v0.31.6-b Limestone configured feature must be a disk")
    if configured_315.get("config", {}).get("state_provider", {}).get("fallback", {}).get("state", {}).get("Name") != "projectflint:limestone": fail("v0.31.6-b Limestone disk state is wrong")
    placements = placed_315.get("placement", [])
    if not any(x.get("type") == "minecraft:rarity_filter" and x.get("chance") == 4 for x in placements): fail("v0.31.6-b Limestone disk rarity must be 1-in-4")
    if not any(x.get("type") == "minecraft:block_predicate_filter" for x in placements): fail("v0.31.6-b Limestone disk must require submerged placement")
except Exception as exc:
    fail(f"could not validate v0.31.6-b Limestone worldgen: {exc}")

for rel in (
    "src/main/resources/assets/projectflint/textures/block/limestone.png",
    "src/main/resources/assets/projectflint/textures/block/cobbled_limestone.png",
    "src/main/resources/assets/projectflint/textures/block/smooth_limestone.png",
    "src/main/resources/assets/projectflint/textures/block/limestone_bricks.png",
    "src/main/resources/assets/projectflint/textures/block/chiseled_limestone.png",
    "src/main/resources/assets/projectflint/textures/block/polished_limestone.png",
    "src/main/resources/assets/projectflint/textures/block/checkered_tile.png",
    "src/main/resources/assets/projectflint/textures/block/glazed_checkered_tile.png",
    "src/main/resources/assets/projectflint/textures/item/recipe_parchment.png",
    "src/main/resources/assets/projectflint/textures/block/primitive_stove_front.png",
    "src/main/resources/assets/projectflint/textures/block/primitive_stove_front_on.png",
    "src/main/resources/assets/projectflint/textures/block/primitive_stove_front_on.png.mcmeta",
    "src/main/resources/assets/projectflint/blockstates/wooden_cabinet.json",
    "src/main/resources/assets/projectflint/models/block/wooden_cabinet.json",
    "src/main/resources/assets/projectflint/models/block/wooden_cabinet_open.json",
):
    if not (ROOT / rel).exists(): fail(f"v0.31.6-b missing requested asset/resource: {rel}")

lang_315 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json"))
for key, value in {
    "item.projectflint.recipe_parchment": "Recipe Parchment",
    "block.projectflint.limestone": "Limestone",
    "block.projectflint.wooden_cabinet": "Wooden Cabinet",
    "block.projectflint.primitive_stove": "Primitive Oven",
}.items():
    if lang_315.get(key) != value: fail(f"v0.31.6-b translation missing/wrong: {key}")

for adv in ("limestone.json", "smooth_limestone.json", "fired_limestone.json", "limestone_bricks.json", "chiseled_limestone.json", "polished_limestone.json", "checkered_tile.json", "glazed_checkered_tile.json", "recipe_parchment.json", "wooden_cabinet.json"):
    if not (RESOURCES / "data/projectflint/advancement" / adv).exists(): fail(f"v0.31.6-b progression advancement missing: {adv}")


# v0.31.7-b persistent Crushed Stone, Flax/String, recipe research, and Primitive Oven food progression.
crushed_317 = read("src/main/java/dev/projectflint/block/CrushedStoneBlock.java")
blocks_317 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
items_317 = read("src/main/java/dev/projectflint/registry/ModItems.java")
primitive_317 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
progress_317 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
break_317 = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
parchment_317 = read("src/main/java/dev/projectflint/item/RecipeParchmentItem.java")
stove_registry_317 = read("src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java")
bookshelf_registry_317 = read("src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java")
bookshelf_menu_317 = read("src/main/java/dev/projectflint/menu/BookshelfMenu.java")
wood_317 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
flax_317 = read("src/main/java/dev/projectflint/block/FlaxCropBlock.java")
animal_317 = read("src/main/java/dev/projectflint/system/PrimitiveAnimalDropSystem.java")
creative_317 = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
guide_screen_317 = read("src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java")
stove_screen_317 = read("src/main/java/dev/projectflint/client/screen/PrimitiveStoveScreen.java")
food_317 = read("src/main/java/dev/projectflint/system/FoodBalanceSystem.java")

for expected in (
    'BooleanProperty.create("player_placed")',
    'setValue(PLAYER_PLACED, false)',
    'setValue(PLAYER_PLACED, true)',
    'builder.add(PLAYER_PLACED)',
):
    if expected not in crushed_317:
        fail(f"v0.31.7-b Crushed Stone placement-state contract missing: {expected}")
for body, label in ((primitive_317, "interaction gate"), (progress_317, "destroy progress"), (break_317, "server break")):
    for expected in ('CrushedStoneBlock.isPlayerPlaced(state)', 'ModItems.ADVANCED_FLINT_MULTITOOL'):
        if expected not in body:
            fail(f"v0.31.7-b player-placed Crushed Stone {label} missing: {expected}")
if 'new ItemStack(ModItems.CRUSHED_STONE_BLOCK_ITEM)' not in break_317:
    fail("v0.31.7-b placed Crushed Stone must recover its block item")
try:
    crushed_state = json.loads(read("src/main/resources/assets/projectflint/blockstates/crushed_stone.json"))
    variants = crushed_state.get("variants", {})
    if "player_placed=false" not in variants or "player_placed=true" not in variants:
        fail("v0.31.7-b Crushed Stone blockstate must cover both placement-data values")
except Exception as exc:
    fail(f"could not validate v0.31.7-b Crushed Stone blockstate: {exc}")

if "PRIMITIVE_STOVE_COOK_SPEED = 1.45F" not in tuning:
    fail("v0.31.7-b Primitive Oven must run at 1.45x the shared baseline")
if "GameplayTuning.MORTAR_KILN_COOK_SPEED *" in stove_entity:
    fail("v0.31.7-b Primitive Oven speed must not compound the Mortar multiplier")

for expected in ('Component.literal("Ingredients:")', 'PrimitiveStoveRecipeRegistry.findBound', 'requirement.ingredient().items()', 'Collectors.joining(" or ")'):
    if expected not in parchment_317:
        fail(f"v0.31.7-b data-driven parchment lore missing: {expected}")
for recipe_id in (
    'projectflint:sugar', 'projectflint:sweet_berry_jam', 'projectflint:sweet_berry_pie',
    'projectflint:pumpkin_pie', 'projectflint:melon_pie', 'projectflint:mutton_pie',
    'projectflint:butter', 'projectflint:pumpkin_cake', 'projectflint:apples_n_pie',
):
    if recipe_id not in stove_registry_317:
        fail(f"v0.31.7-b Primitive Oven recipe missing: {recipe_id}")
for expected in (
    'Ingredient.of(Items.SUGAR_CANE), 4',
    'Ingredient.of(Items.SWEET_BERRIES), 8',
    'Ingredient.of(ModItems.WOODEN_CUP), 2',
    'Ingredient.of(ModItems.PUMPKIN_SLICE), 8',
    'Ingredient.of(Items.MELON_SLICE), 8',
    'Ingredient.of(Items.COOKED_MUTTON), 8',
    'Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 4',
    'Ingredient.of(Items.PUMPKIN_PIE), 4',
    'Ingredient.of(ModItems.MELON_PIE, Items.PUMPKIN_PIE)',
):
    if expected not in stove_registry_317:
        fail(f"v0.31.7-b Primitive Oven requirement missing: {expected}")

for expected in (
    'parchment(Ingredient.of(Items.SUGAR_CANE), 16, "projectflint:sugar")',
    'parchment(Ingredient.of(Items.SWEET_BERRIES), 16, "projectflint:sweet_berry_jam")',
    'parchment(Ingredient.of(Items.SWEET_BERRIES), 16, "projectflint:sweet_berry_pie")',
    'parchment(Ingredient.of(ModItems.PUMPKIN_SLICE), 16, "projectflint:pumpkin_cake")',
    'parchment(Ingredient.of(ModItems.PUMPKIN_SLICE), 16, "projectflint:pumpkin_pie")',
    'parchment(Ingredient.of(ModItems.WOODEN_CUP_RAW_MILK), 4, "projectflint:butter")',
    'RecipeParchmentItem.boundStack(recipeId), 4',
    'List<Match> findAll',
):
    if expected not in bookshelf_registry_317:
        fail(f"v0.31.7-b Bookshelf research definition missing: {expected}")
for expected in ('selectedRecipe', 'activeRecipes', 'BookshelfRecipeRegistry.findAll', 'RECIPE_PREVIEW_COUNT'):
    if expected not in bookshelf_menu_317:
        fail(f"v0.31.7-b selectable research UI missing: {expected}")

for expected in (
    'ModItems.FIRED_CLAY), 4, Ingredient.of(ModItems.MUD_PILE), 16, new ItemStack(ModItems.LOOSE_MUD_BRICKS)',
    'ModItems.LOOSE_MUD_BRICKS), 4, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(Items.BRICKS)',
    'ModItems.SMOOTH_LIMESTONE), 1, Ingredient.of(Items.BRICKS), 2, new ItemStack(ModItems.PRIMITIVE_STOVE)',
    'ModItems.FILED_BONE), 2, Ingredient.of(Items.STICK), 1, new ItemStack(ModItems.BONE_KNIFE)',
    'toolRecipe(Ingredient.of(ModItems.WOODEN_SHEET), 1, Ingredient.of(ModItems.BONE_KNIFE), new ItemStack(ModItems.WOODEN_CUP, 2))',
    'toolRecipe(Ingredient.of(ModItems.PUMPKIN_CAKE), 1, Ingredient.of(ModItems.BONE_KNIFE), new ItemStack(ModItems.PUMPKIN_CAKE_SLICE, 4))',
):
    if expected not in wood_317:
        fail(f"v0.31.7-b Wooden Grinder progression recipe missing: {expected}")
for rel in (
    "src/main/resources/data/projectflint/recipe/loose_mud_brick_slab.json",
    "src/main/resources/data/projectflint/recipe/loose_mud_bricks_from_slabs.json",
):
    if not (ROOT / rel).exists():
        fail(f"v0.31.7-b loose mud slab loop missing: {rel}")

for expected in ('isMaxAge(state)', 'new ItemStack(Items.STRING)', '1 + (level.random.nextFloat() < 0.10F ? 1 : 0)'):
    if expected not in flax_317:
        fail(f"v0.31.7-b Flax drop behavior missing: {expected}")
if 'drops.removeIf(stack -> stack.is(Items.STRING))' not in animal_317:
    fail("v0.31.7-b String must be removed from loot-table sources")
if 'BlockRenderLayerMap.putBlock(ModBlocks.FLAX_CROP, ChunkSectionLayer.CUTOUT)' not in client:
    fail("v0.31.7-b Flax crop must use CUTOUT rendering")
for age in range(8):
    if not (ROOT / f"src/main/resources/assets/projectflint/textures/block/flax/{age}.png").exists():
        fail(f"v0.31.7-b missing supplied Flax stage {age}")

for expected in (
    'BONE_KNIFE = register("bone_knife"', '.durability(64)',
    'WOODEN_CUP = register("wooden_cup"', '.stacksTo(16)',
    'WOODEN_CUP_RAW_MILK = register("wooden_cup_raw_milk"', 'Consumables.DEFAULT_DRINK',
    'MELON_PIE = register("melon_pie"', 'MUTTON_PIE = register("mutton_pie"',
    'BUTTER = register("butter"', 'PUMPKIN_CAKE = register("pumpkin_cake"',
    'PUMPKIN_CAKE_SLICE = register("pumpkin_cake_slice"', 'APPLES_N_PIE = register("apples_n_pie"',
):
    if expected not in items_317:
        fail(f"v0.31.7-b item registration/property missing: {expected}")
raw_tag_317 = json.loads(read("src/main/resources/data/projectflint/tags/item/raw_meats.json"))
if "projectflint:wooden_cup_raw_milk" not in raw_tag_317.get("values", []):
    fail("v0.31.7-b raw milk cup must use the shared raw-food sickness profile")

for rel in (
    "src/main/resources/data/minecraft/recipe/pumpkin_pie.json",
    "src/main/resources/data/minecraft/recipe/sugar_from_sugar_cane.json",
    "src/main/resources/data/minecraft/recipe/sugar_from_honey_bottle.json",
):
    try:
        disabled = json.loads(read(rel))
        ingredients = disabled.get("ingredients", [])
        first = ingredients[0] if ingredients else None
        barrier = first == "minecraft:barrier" or (isinstance(first, dict) and first.get("item") == "minecraft:barrier")
        if not barrier:
            fail(f"v0.31.7-b vanilla recipe bypass not disabled: {rel}")
    except Exception as exc:
        fail(f"could not validate disabled vanilla recipe {rel}: {exc}")

for expected in ('case PRIMITIVE_OVEN -> primitiveOvenRecipes()', 'PrimitiveStoveRecipeRegistry.parchmentRecipes()', 'RecipeParchmentItem.boundStack(recipe.id())'):
    if expected not in guide_screen_317:
        fail(f"v0.31.7-b Primitive Oven recipe guide missing: {expected}")
if 'Station.PRIMITIVE_OVEN' not in stove_screen_317:
    fail("v0.31.7-b Primitive Oven screen must expose its recipe guide button")

lang_317 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json"))
for key, value in {
    "block.projectflint.primitive_stove": "Primitive Oven",
    "item.projectflint.primitive_stove": "Primitive Oven",
    "item.projectflint.bone_knife": "Bone Knife",
    "item.projectflint.wooden_cup": "Wooden Cup",
    "item.projectflint.wooden_cup_raw_milk": "Wooden Cup with Raw Milk",
    "item.projectflint.melon_pie": "Melon Pie",
    "item.projectflint.mutton_pie": "Mutton Pie",
    "item.projectflint.butter": "Butter",
    "item.projectflint.pumpkin_cake": "Pumpkin Cake",
    "item.projectflint.pumpkin_cake_slice": "Pumpkin Cake Slice",
    "item.projectflint.apples_n_pie": "Apples n Pie",
}.items():
    if lang_317.get(key) != value:
        fail(f"v0.31.7-b translation missing/wrong: {key}")

owner_assets_317 = {
    "bone_knife.png": "a78a31f2c4d0e078032ddd84626193231ec32232420166a986fc66854a53dead",
    "wooden_cup.png": "9e185cfe4cdfe73a2d4f2245796b32ca6f92a1fc8886b17b99e5b03f69b67d78",
    "wooden_cup_raw_milk.png": "a2bad3f8ef046700626152e4bceb69caff02f7a703463f0ee08efeef124a6b34",
    "butter.png": "ba8f9a71db59ec9299a076092784fafa6733b055887370464b7a306cfe1a6590",
    "melon_pie.png": "fc398092832ff5195f2063a9fd22bc3e085bfc767bab2ea0f630aa7b77eb7952",
    "mutton_pie.png": "d168c92f47d4b0d68dc3d272d043a896bc0c3eb67d9ef8d00152503f6e6f2b2b",
    "pumpkin_cake.png": "399667f5cc324ee0f218b6a5be628f20639bcb9c489791b7e1a687186ed8cc9f",
    "pumpkin_cake_slice.png": "cc42ae084c35eb8883316fc364702784c1b22c8bfc87949c28f4521018e2fd75",
    "apples_n_pie.png": "58a324b06e98bb951d2736631a392a06a28ef09d841eacf26b0c547d5a5bf3f5",
}
for filename, expected_hash in owner_assets_317.items():
    path = ROOT / "src/main/resources/assets/projectflint/textures/item" / filename
    if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest() != expected_hash:
        fail(f"v0.31.7-b owner-supplied item texture changed/missing: {filename}")
flax_hashes_317 = [
    "b1df6ce75540e91704c8724b96b33cbc863e2c57bf4d01d6d41350a39c93580b",
    "e0bae99683af00da2b0e09b21268a22525fbf9f3f257b3440d22f6b7cfc05a38",
    "bbf8ae007d32687450736fb7f52c2fa65b2f900f8f5bf01434a181fec8ae976b",
    "6b8e0f4081ebefc8b2916301cda7f378b2146e3e586728e32ea93e7879d120d5",
    "c181dace72acb0d94442535dd9871054cb6e460f878baba0621297cf8d2888fd",
    "23b092fe9e1c0f2bafb7ba6b15b2fb2401fc89870036e2ee19d89c63066c0ce2",
    "ef32ada55415423f29489603759feff8ce9fe60600be5fd899bb0f52549522e2",
    "10a2ad958fbb43872df7080ccc7a3d1decd2843e25540d741547cfa2466a3bab",
]
for age, expected_hash in enumerate(flax_hashes_317):
    path = ROOT / f"src/main/resources/assets/projectflint/textures/block/flax/{age}.png"
    if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest() != expected_hash:
        fail(f"v0.31.7-b owner-supplied Flax texture changed/missing: stage {age}")

for adv in ("flax_string.json", "loose_mud_bricks.json", "bone_knife.json", "wooden_cup.json", "raw_milk_cup.json", "primitive_oven.json", "recipe_research.json"):
    if not (RESOURCES / "data/projectflint/advancement" / adv).exists():
        fail(f"v0.31.7-b progression advancement missing: {adv}")


# v0.31.8-b Recipe Parchment Component/MutableComponent compile hotfix.
parchment_318 = read("src/main/java/dev/projectflint/item/RecipeParchmentItem.java")
for expected in (
	"Optional<PrimitiveStoveRecipeRegistry.Definition> boundRecipe",
	"if (boundRecipe.isEmpty()) return super.getName(stack);",
	".append(boundRecipe.get().result().getHoverName())",
):
	if expected not in parchment_318:
		fail(f"v0.31.8-b Recipe Parchment compile hotfix missing: {expected}")
if ".orElseGet(() -> super.getName(stack))" in parchment_318:
	fail("v0.31.8-b must not reintroduce the Optional<MutableComponent>/Component fallback compile error")


# v0.31.9-b hotfixes: last-cup milk exchange, ambiguous dual recipes, and Bookshelf restore.
wooden_cup = read("src/main/java/dev/projectflint/item/WoodenCupItem.java")
if "ItemUtils.createFilledResult(stack, player, filled)" not in wooden_cup:
	fail("Wooden Cup cow milking must use vanilla filled-container exchange so a one-cup stack returns Raw Milk")
if "stack.shrink(1)" in wooden_cup:
	fail("Wooden Cup cow milking must not manually shrink before replacing the final cup")

grinder_registry = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
grinder_menu = read("src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java")
for expected in ("List<Match> findAll", "return List.copyOf(matches)"):
	if expected not in grinder_registry:
		fail(f"Wooden Grinder multi-match registry missing: {expected}")
for expected in ("dualRecipes.addAll(findDualRecipes(a, b))", "setupDualResult()", "ItemStack.isSameItemSameComponents"):
	if expected not in grinder_menu:
		fail(f"Wooden Grinder ambiguous-recipe selection missing: {expected}")

bookshelf_item = read("src/main/java/dev/projectflint/item/BookshelfItem.java")
if "net.minecraft.core.BlockPos pos=context.getClickedPos();" not in bookshelf_item:
	fail("Bookshelf item must restore saved BooksStored data at the resolved placement position")
if "pos=pos.relative(context.getClickedFace())" in bookshelf_item:
	fail("Bookshelf placement must not double-offset the resolved BlockPlaceContext position")

# v0.31.10-b: blank/filled parchment storage, staged Rope, and researched meals.
cabinet_entity_3110 = read("src/main/java/dev/projectflint/block/entity/WoodenCabinetBlockEntity.java")
cabinet_menu_3110 = read("src/main/java/dev/projectflint/menu/WoodenCabinetMenu.java")
for body, label in ((cabinet_entity_3110, "cabinet entity"), (cabinet_menu_3110, "cabinet menu")):
	if "stack.is(ModItems.RECIPE_PARCHMENT)" not in body:
		fail(f"v0.31.10-b {label} lost parchment-only storage")
	if "RecipeParchmentItem.isBound(stack)" in body:
		fail(f"v0.31.10-b {label} must accept blank parchment")

parchment_model_3110 = json.loads(read("src/main/resources/assets/projectflint/items/recipe_parchment.json") or "{}")
model_node_3110 = parchment_model_3110.get("model", {})
# v0.31.24-b adds an outer custom-model-data select for staged/incomplete parchments;
# the fallback must preserve the v0.31.10-b custom_data bound-vs-blank behavior.
bound_node_3110 = model_node_3110.get("fallback", model_node_3110)
if bound_node_3110.get("type") != "minecraft:condition" or bound_node_3110.get("property") != "minecraft:has_component" or bound_node_3110.get("component") != "minecraft:custom_data":
	fail("v0.31.10-b Recipe Parchment must select filled art from persistent custom_data")
if bound_node_3110.get("on_true", {}).get("model") != "projectflint:item/recipe_parchment_filled":
	fail("v0.31.10-b bound Recipe Parchment must use filled parchment model")

bookshelf_3110 = read("src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java")
for expected in (
	'Ingredient.of(Items.COOKED_BEEF), 8, "projectflint:beef_stew"',
	'Ingredient.of(Items.COOKED_PORKCHOP), 8, RecipeParchmentItem.UNFINISHED_BACON_RECIPE',
	'Ingredient.of(ModItems.SCRAMBLED_EGG), 8, RecipeParchmentItem.UNFINISHED_BACON_RECIPE, "projectflint:bacon_and_eggs"',
	'Ingredient.of(ModItems.FILED_BONE), 6, "projectflint:bone_broth"',
	'bound.filter(parchmentRecipe::equals).isPresent()',
):
	if expected not in bookshelf_3110:
		fail(f"v0.31.10-b Bookshelf research staging missing: {expected}")

stove_recipes_3110 = read("src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java")
for expected in (
	'recipe("projectflint:beef_stew", new ItemStack(ModItems.BEEF_STEW, 3)',
	'req(Ingredient.of(Items.COOKED_BEEF), 2)', 'req(Ingredient.of(ModItems.BUTTER), 1)', 'req(Ingredient.of(Items.SUGAR), 1)', 'req(Ingredient.of(ModItems.WOODEN_CUP), 3)',
	'recipe("projectflint:bacon_and_eggs", new ItemStack(ModItems.BACON_AND_EGGS, 3)', 'req(Ingredient.of(Items.COOKED_PORKCHOP), 1)', 'req(Ingredient.of(ModItems.SCRAMBLED_EGG), 3)',
	'recipe("projectflint:bone_broth", new ItemStack(ModItems.BONE_BROTH, 4)', 'req(Ingredient.of(ModItems.FILED_BONE), 4)', 'req(Ingredient.of(ModItems.WOODEN_CUP), 2)',
):
	if expected not in stove_recipes_3110:
		fail(f"v0.31.10-b Primitive Oven recipe missing: {expected}")

sanding_3110 = read("src/main/java/dev/projectflint/recipe/SandingStationRecipeRegistry.java")
grinder_3110 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
if 'Ingredient.of(Items.STRING), 16, new ItemStack(ModItems.COBWEB)' not in sanding_3110:
	fail("v0.31.10-b 16 String -> Project Flint Cobweb Sanding Station recipe missing")
for expected in (
	'recipe(Ingredient.of(ModItems.COBWEB), 1, Ingredient.of(ModItems.PLANT_TOOL_BINDING), 4, new ItemStack(ModItems.ROPE_STAGE_1))',
	'recipe(Ingredient.of(ModItems.ROPE_STAGE_1), 1, Ingredient.of(ModItems.WOODEN_SCREW), 4, new ItemStack(ModItems.ROPE_STAGE_2))',
	'recipe(Ingredient.of(ModItems.ROPE_STAGE_2), 1, Ingredient.of(ModItems.WOODEN_SHEET), 2, new ItemStack(ModItems.ROPE_STAGE_3))',
	'toolRecipe(Ingredient.of(ModItems.ROPE_STAGE_3), 1, Ingredient.of(ModItems.FLINT_FILE), new ItemStack(ModItems.ROPE))',
):
	if expected not in grinder_3110:
		fail(f"v0.31.10-b staged Rope recipe missing: {expected}")

items_3110 = read("src/main/java/dev/projectflint/registry/ModItems.java")
for expected in (
	'COBWEB = register("cobweb"', 'ROPE_STAGE_1 = register("rope_stage_1"', 'ROPE_STAGE_2 = register("rope_stage_2"', 'ROPE_STAGE_3 = register("rope_stage_3"',
	'ROPE = registerBlockItem("rope", ModBlocks.ROPE, 16)', 'BEEF_STEW = register("beef_stew"', 'BACON_AND_EGGS = register("bacon_and_eggs"', 'BONE_BROTH = register("bone_broth"',
):
	if expected not in items_3110:
		fail(f"v0.31.10-b item registration missing: {expected}")
for stage in ("rope_stage_1", "rope_stage_2", "rope_stage_3"):
	if f'new BlockItem(ModBlocks.ROPE' in items_3110:
		fail("v0.31.10-b intermediate Rope stages must remain item-only")

rope_block_3110 = read("src/main/java/dev/projectflint/block/RopeBlock.java")
mod_blocks_3110 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
if 'public static final RopeBlock ROPE = registerRope("rope")' not in mod_blocks_3110 or 'noCollision().noOcclusion()' not in mod_blocks_3110:
	fail("v0.31.10-b final Rope block registration/geometry contract missing")
if 'private static final VoxelShape SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)' not in rope_block_3110:
	fail("v0.31.10-b Rope outline must remain a thin central hanging line")

lang_3110 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json") or "{}")
for key, value in {
	"item.projectflint.cobweb": "Cobweb", "item.projectflint.rope_stage_1": "Rope (1/3)", "item.projectflint.rope_stage_2": "Rope (2/3)", "item.projectflint.rope_stage_3": "Rope (3/3)",
	"block.projectflint.rope": "Rope", "item.projectflint.beef_stew": "Beef Stew", "item.projectflint.bacon_and_eggs": "Bacon and Eggs", "item.projectflint.bone_broth": "Bone Broth",
}.items():
	if lang_3110.get(key) != value:
		fail(f"v0.31.10-b translation missing/wrong: {key}")

owner_assets_3110 = {'src/main/resources/assets/projectflint/textures/item/recipe_parchment_filled.png': '2c80e6493349d899ee6c2e97d76376fa08374d39e92a4f1e90140de633571600', 'src/main/resources/assets/projectflint/textures/item/rope.png': '0def0530afe986602be08072fa6fd67278c587fe95f587be645e1c7cdedba99a', 'src/main/resources/assets/projectflint/textures/block/rope.png': '2e93e9afc8d07d7a21a847c0bc08ae16235c9ab65c7b3c3520ab161838505223', 'src/main/resources/assets/projectflint/textures/block/rope_top.png': '6e5334869c95ab452b7c9fe7c528fcaabdb3cca2f41a629fd1550d939e70a61d', 'src/main/resources/assets/projectflint/textures/item/beef_stew.png': 'dad7965af720a358699f339f899bc302cc576c818432be59c0ef0a83f0536aac', 'src/main/resources/assets/projectflint/textures/item/bacon_and_eggs.png': '663c7439206e3a8bc87c7d86c7fe5f1371ab0f40d53ad9d12b2c16616f6046f4', 'src/main/resources/assets/projectflint/textures/item/bone_broth.png': '441d0677330c73e1dfd61bbd050769ec18e5a8a747f6387f8f2e78f420a671f7'}
for path, expected_hash in owner_assets_3110.items():
	p = ROOT / path
	if not p.exists() or hashlib.sha256(p.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.31.10-b owner-supplied texture changed/missing: {path}")

for adv in ("projectflint_cobweb.json", "rope_stage_1.json", "rope_stage_2.json", "rope_stage_3.json", "rope.json", "beef_stew.json", "bacon_and_eggs.json", "bone_broth.json"):
	if not (RESOURCES / "data/projectflint/advancement" / adv).exists():
		fail(f"v0.31.10-b progression advancement missing: {adv}")

# v0.31.11-b: Crude Stone Cutters one-binding assembly completion hotfix.
interaction_3111 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
if "state.getValue(HarvestingToolAssemblyBlock.BINDINGS) == 2" in interaction_3111:
	fail("v0.31.11-b stale Crude Stone Cutters gate still requires exactly 2 Plant Tool Bindings")
if "state.getValue(HarvestingToolAssemblyBlock.BINDINGS) >= 1" not in interaction_3111:
	fail("v0.31.11-b Crude Stone Cutters hammer gate must accept the intended single Plant Tool Binding")


# v0.31.16-b: remove Realms without breaking Mod Menu title-screen initialization.
mixins_3116 = set(mixins.get("client", []))
for expected in ("RealmsAvailabilityMixin", "TitleScreenMixin"):
	if expected not in mixins_3116:
		fail(f"v0.31.16-b Realms-removal mixin missing: {expected}")
if "MinecraftRealmsMixin" in mixins_3116:
	fail("v0.31.16-b must not hard-disable Minecraft#allowsRealms before Mod Menu finishes title-screen initialization")
realms_availability_mixin = read("src/main/java/dev/projectflint/mixin/RealmsAvailabilityMixin.java")
for expected in ("RealmsAvailability.Type.INCOMPATIBLE_CLIENT", "CompletableFuture.completedFuture"):
	if expected not in realms_availability_mixin:
		fail(f"v0.31.16-b Realms availability short-circuit missing: {expected}")
realms_title_mixin = read("src/main/java/dev/projectflint/mixin/TitleScreenMixin.java")
for expected in ('method = "render"', '"menu.online"', "removeWidget(realmsButton)", "widget.setY(widget.getY() - 24)", "realmsNotificationsScreen = null", "projectflint$realmsLayoutProcessed"):
	if expected not in realms_title_mixin:
		fail(f"v0.31.16-b deferred title-screen Realms removal/reflow missing: {expected}")
if 'projectflint$stripRealmsFromTitleScreen' in realms_title_mixin:
	fail("v0.31.16-b must not remove the Realms row during TitleScreen.init() before Mod Menu compatibility hooks finish")



# v0.31.28-b: PF-EMI exclusively owns EMI presentation/discovery.
# Project Flint retains only the external runtime dependency and no EMI API/plugin source.
emi_plugin_path = ROOT / "src/main/java/dev/projectflint/compat/emi/ProjectFlintEmiPlugin.java"
if emi_plugin_path.exists():
	fail("v0.31.28-b ProjectFlintEmiPlugin.java must be deleted")

emi_import_files = []
for java_path in (ROOT / "src/main/java").rglob("*.java"):
	try:
		body = java_path.read_text(encoding="utf-8")
	except Exception as exc:
		fail(f"v0.31.28-b could not read Java source {java_path.relative_to(ROOT)}: {exc}")
		continue
	if "dev.emi.emi" in body:
		emi_import_files.append(str(java_path.relative_to(ROOT)))
if emi_import_files:
	fail("v0.31.28-b Project Flint Java source still imports dev.emi.emi: " + ", ".join(emi_import_files))

for obsolete_local in ("libs/PF-EMI-1.21.11.jar", "libs/PF-EMI-1.21.11.sha256"):
	if (ROOT / obsolete_local).exists():
		fail(f"v0.31.28-b obsolete local PF-EMI build input still present: {obsolete_local}")

build_gradle_3128 = read("build.gradle")
for forbidden in (
	'flatDir { dirs "libs" }',
	'modCompileOnly name: "PF-EMI-1.21.11"',
	'include name: "PF-EMI-1.21.11"',
	'include files("libs/PF-EMI-1.21.11.jar")',
	'modImplementation name: "PF-EMI-1.21.11"',
	'modImplementation files("libs/PF-EMI-1.21.11.jar")',
):
	if forbidden in build_gradle_3128:
		fail(f"v0.31.28-b obsolete PF-EMI Gradle bridge/packaging remains: {forbidden}")
if "PF-EMI" in build_gradle_3128:
	fail("v0.31.28-b build.gradle must not reference PF-EMI")

fabric_mod_3128 = json.loads(read("src/main/resources/fabric.mod.json") or "{}")
entrypoints_3128 = fabric_mod_3128.get("entrypoints", {})
if "emi" in entrypoints_3128:
	fail("v0.31.28-b Project Flint must not declare an emi entrypoint")
if set(entrypoints_3128) != {"main", "client"}:
	fail("v0.31.28-b Project Flint entrypoints must contain only main and client")
if fabric_mod_3128.get("depends", {}).get("emi") != "*":
	fail("v0.31.28-b Project Flint must retain the external emi runtime dependency")
if fabric_mod_3128.get("jars"):
	fail("v0.31.28-b source Fabric metadata must not declare nested jars")

build_bat_3128 = read("build.bat")
for forbidden in (
	'if not exist "libs\\PF-EMI-1.21.11.jar"',
	'Compile-time PF-EMI API jar',
	'compile Project Flint\'s EMI plugin',
):
	if forbidden in build_bat_3128:
		fail(f"v0.31.28-b build.bat still requires the local PF-EMI compile bridge: {forbidden}")
for expected in ('jar tf "build\\libs\\%EXPECTED_JAR%"', 'findstr /i /c:"PF-EMI-1.21.11.jar"', 'PF-EMI must be installed as a separate jar'):
	if expected not in build_bat_3128:
		fail(f"v0.31.28-b built-jar nested PF-EMI guard missing: {expected}")

lang_3128 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json") or "{}")
for obsolete_key in (
	"emi.category.projectflint.clay_grinder",
	"emi.category.projectflint.wooden_grinder",
	"emi.category.projectflint.sanding_station",
	"emi.category.projectflint.bookshelf",
	"emi.category.projectflint.primitive_heat",
	"emi.category.projectflint.advanced_kiln",
):
	if obsolete_key in lang_3128:
		fail(f"v0.31.28-b obsolete Project Flint EMI translation remains: {obsolete_key}")

# If a release jar already exists, validate the ownership boundary there as well.
built_jar_3128 = ROOT / "build/libs" / f"projectflint-{EXPECTED_VERSION}.jar"
if built_jar_3128.exists():
	try:
		with zipfile.ZipFile(built_jar_3128) as built_zip:
			names = built_zip.namelist()
			if any("PF-EMI" in name or name.startswith("META-INF/jars/") and "emi" in name.lower() for name in names):
				fail("v0.31.28-b produced Project Flint jar contains nested PF-EMI/EMI payload")
			if "dev/projectflint/compat/emi/ProjectFlintEmiPlugin.class" in names:
				fail("v0.31.28-b produced Project Flint jar still contains ProjectFlintEmiPlugin.class")
	except Exception as exc:
		fail(f"v0.31.28-b could not inspect produced Project Flint jar: {exc}")

quest_bridge_3117 = read("src/main/java/dev/projectflint/compat/flintquests/FlintQuestsBridge.java")
for expected in (
	"com.projectflint.flintquests.api.FlintQuestHooks",
	'hooks.getMethod("trigger", ServerPlayer.class, String.class, int.class)',
	'"projectflint:workstation/any"', '"projectflint:workstation/" + workstationPath',
):
	if expected not in quest_bridge_3117:
		fail(f"v0.31.17-b Flint Quests public API bridge missing: {expected}")

for path, station in (
	("src/main/java/dev/projectflint/menu/ClayGrinderMenu.java", "clay_grinder"),
	("src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java", "wooden_grinder"),
	("src/main/java/dev/projectflint/menu/SandingStationMenu.java", "sanding_station"),
	("src/main/java/dev/projectflint/menu/BookshelfMenu.java", "bookshelf"),
):
	body = read(path)
	if f'FlintQuestsBridge.onCraft(serverPlayer, "{station}", stack)' not in body:
		fail(f"v0.31.17-b workstation quest hook missing: {station}")

for path, station in (
	("src/main/java/dev/projectflint/menu/PrimitiveStoveMenu.java", 'FlintQuestsBridge.onCraft(serverPlayer, "primitive_oven", stack)'),
	("src/main/java/dev/projectflint/menu/AdvancedKilnMenu.java", 'FlintQuestsBridge.onCraft(serverPlayer, "advanced_kiln", stack)'),
	("src/main/java/dev/projectflint/system/LumberProcessorSystem.java", 'FlintQuestsBridge.onCraft(player, "lumber_processor", result)'),
):
	if station not in read(path):
		fail(f"v0.31.17-b timed/bespoke workstation quest hook missing in {path}")

for path, event_id in (
	("src/main/java/dev/projectflint/block/PrimitiveCampfireBlock.java", 'FlintQuestsBridge.onWorkstationUse(serverPlayer, "primitive_campfire")'),
	("src/main/java/dev/projectflint/block/BakedClayKilnBlock.java", 'FlintQuestsBridge.onWorkstationUse(serverPlayer, "baked_kiln")'),
	("src/main/java/dev/projectflint/block/MortarKilnBlock.java", 'FlintQuestsBridge.onWorkstationUse(serverPlayer, "mortar_kiln")'),
):
	if event_id not in read(path):
		fail(f"v0.31.17-b primitive heat workstation quest hook missing: {event_id}")


bone_hoe_3117 = read("src/main/java/dev/projectflint/item/BoneHoeItem.java")
for expected in ("soil.is(ModBlocks.LOOSE_DIRT)", "soil.is(ModBlocks.LOOSE_SOIL)", "Blocks.FARMLAND.defaultBlockState()"):
	if expected not in bone_hoe_3117:
		fail(f"v0.31.17-b Loose Dirt/Loose Soil tilling missing: {expected}")



# v0.31.20-b intentionally retires the entire Minecraft advancement layer.
# Ignore historical advancement-preservation guards from older releases, then enforce
# the new zero-advancement contract below.
errors = [message for message in errors if not (
	"advancement" in message.lower()
	or "PrimitiveItemAdvancementSystem.java" in message
	or "detailed progression milestone" in message.lower()
	or "charred-twig guidance" in message.lower()
)]

# v0.31.20-b Grinder balance.
try:
	twig_branch = json.loads(read("src/main/resources/data/projectflint/recipe/twig_to_branch.json") or "{}")
	branch_twig = json.loads(read("src/main/resources/data/projectflint/recipe/twigs_from_branch.json") or "{}")
	if twig_branch.get("input_count") != 4 or twig_branch.get("result", {}).get("id") != "projectflint:branch" or twig_branch.get("result", {}).get("count") != 1:
		fail("v0.31.20-b Clay Grinder must convert exactly 4 Twigs -> 1 Branch")
	if branch_twig.get("input_count") != 1 or branch_twig.get("result", {}).get("id") != "projectflint:twig" or branch_twig.get("result", {}).get("count") != 2:
		fail("v0.31.20-b Clay Grinder must convert exactly 1 Branch -> 2 Twigs")
except Exception as exc:
	fail(f"v0.31.20-b could not validate twig/branch grinder recipes: {exc}")
clay_guide_3120 = read("src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java")
for expected in (
	'new Definition("projectflint:twig", 4, "projectflint:branch", 1)',
	'new Definition("projectflint:branch", 1, "projectflint:twig", 2)',
):
	if expected not in clay_guide_3120:
		fail(f"v0.31.20-b Clay Grinder guide/EMI mirror missing: {expected}")

# v0.31.20-b advancement retirement.
adv_root_3120 = RESOURCES / "data/projectflint/advancement"
if adv_root_3120.exists() and any(adv_root_3120.rglob("*.json")):
	fail("v0.31.20-b zero-progression-registry policy violated: Project Flint advancement JSONs still exist")
if (ROOT / "src/main/java/dev/projectflint/system/PrimitiveItemAdvancementSystem.java").exists():
	fail("v0.31.20-b retired inventory milestone polling class still exists")
project_flint_3120 = read("src/main/java/dev/projectflint/ProjectFlint.java")
if "PrimitiveItemAdvancementSystem" in project_flint_3120:
	fail("v0.31.20-b bootstrap still initializes retired milestone polling")
server_adv_3120 = read("src/main/java/dev/projectflint/mixin/ServerAdvancementManagerMixin.java")
if "return Map.of();" not in server_adv_3120 or "removeAllAdvancements" not in server_adv_3120:
	fail("v0.31.20-b server progression registry is not globally emptied")
mod_adv_3120 = read("src/main/java/dev/projectflint/system/ModAdvancements.java")
for forbidden in ("getAdvancements()", ".award("):
	if forbidden in mod_adv_3120:
		fail(f"v0.31.20-b legacy milestone bridge must be a no-op: {forbidden}")
pause_3120 = read("src/main/java/dev/projectflint/mixin/PauseScreenMixin.java")
for expected in ('Component.translatable("gui.advancements")', 'visible = false', 'active = false'):
	if expected not in pause_3120:
		fail(f"v0.31.20-b pause-menu progression button suppression missing: {expected}")

# v0.31.20-b chest-backed Leather Backpack progression.
items_3120 = read("src/main/java/dev/projectflint/registry/ModItems.java")
for expected in (
	'LEATHER_BACKPACK_WITH_CHEST = register("leather_backpack_with_chest", ModItems::leatherBackpackWithChest)',
	'ProjectFlint.id("leather_backpack_with_chest_armor"), 4.0D',
	'.setAsset(LEATHER_BACKPACK_EQUIPMENT_ASSET)',
):
	if expected not in items_3120:
		fail(f"v0.31.20-b Leather Backpack with a Chest registration/armor missing: {expected}")
wooden_3120 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	'Ingredient.of(ModItems.LEATHER_BACKPACK), 1',
	'Ingredient.of(ModItems.WOODEN_CHEST), 2',
	'new ItemStack(ModItems.LEATHER_BACKPACK_WITH_CHEST)',
):
	if expected not in wooden_3120:
		fail(f"v0.31.20-b chest-backed backpack Wooden Grinder recipe missing: {expected}")
inventory_3120 = read("src/main/java/dev/projectflint/system/PrimitiveInventorySystem.java")
for expected in (
	'LEATHER_BACKPACK_WITH_CHEST_ALLOWED_SLOTS = 18',
	'chest.is(dev.projectflint.registry.ModItems.LEATHER_BACKPACK_WITH_CHEST)',
	'return LEATHER_BACKPACK_WITH_CHEST_ALLOWED_SLOTS',
):
	if expected not in inventory_3120:
		fail(f"v0.31.20-b full top-row inventory unlock missing: {expected}")
exhaustion_3120 = read("src/main/java/dev/projectflint/mixin/PlayerExhaustionMixin.java")
if 'chest.is(ModItems.LEATHER_BACKPACK_WITH_CHEST)' not in exhaustion_3120 or 'exhaustion * 1.10F' not in exhaustion_3120:
	fail("v0.31.20-b chest-backed backpack 1.10x hunger/exhaustion tax missing")
layer_3120 = read("src/main/java/dev/projectflint/client/ReedBackpackLayer.java")
if 'state.chestEquipment.is(ModItems.LEATHER_BACKPACK_WITH_CHEST)' not in layer_3120 or '"leather_backpack_worn"' not in layer_3120:
	fail("v0.31.20-b chest-backed backpack does not reuse the Leather Backpack worn model")
backpack_model_3120 = read("src/main/resources/assets/projectflint/items/leather_backpack_with_chest.json")
if '"model": "projectflint:item/leather_backpack"' not in backpack_model_3120:
	fail("v0.31.20-b chest-backed backpack must reuse the exact Leather Backpack item model")
lang_3120 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json") or "{}")
if lang_3120.get("item.projectflint.leather_backpack_with_chest") != "Leather Backpack with a Chest":
	fail("v0.31.20-b chest-backed backpack translation missing")

# v0.31.20-b Crude Plant Torch finite dropped-item behavior.
placed_3120 = read("src/main/java/dev/projectflint/system/PlacedItemSystem.java")
for expected in (
	'selected.is(ModItems.CRUDE_PLANT_TORCH)',
	'itemEntity.getItem().is(ModItems.CRUDE_PLANT_TORCH)',
	'itemEntity.onGround()',
	'level.getBlockState(pos).canBeReplaced()',
	'ModBlocks.CRUDE_PLANT_TORCH.defaultBlockState()',
	'torch.initializeFromStack(torchStack)',
	'burnOutDroppedTorch',
):
	if expected not in placed_3120:
		fail(f"v0.31.20-b dropped Crude Plant Torch settling/burnout rule missing: {expected}")

cleanup_3120 = read("APPLY_PROJECTFLINT_0_31_48_B.bat")
for expected in (
	'APPLY_PROJECTFLINT_0_31_19_B.bat',
	'src\\main\\resources\\data\\projectflint\\advancement',
	'PrimitiveItemAdvancementSystem.java',
):
	if expected not in cleanup_3120:
		fail(f"v0.31.21-b cleanup policy missing: {expected}")

# v0.31.21-b Java 21 compile hotfix: advancement shim must stay a valid, no-op class.
adv_shim = read("src/main/java/dev/projectflint/system/ModAdvancements.java")
for expected in (
	"public final class ModAdvancements",
	"public static void awardAll(ServerPlayer player, String advancementPath)",
	"public static void award(ServerPlayer player, String advancementPath, String criterion)",
):
	if expected not in adv_shim:
		fail(f"v0.31.21-b advancement compatibility shim missing: {expected}")
if adv_shim.count("public final class ModAdvancements") != 1:
	fail("v0.31.21-b advancement compatibility shim class declaration is malformed")
if "}\t// Intentionally disabled.\n\t}" in adv_shim:
	fail("v0.31.21-b advancement compatibility shim still contains the v0.31.20-b extra-brace compile bug")


# v0.31.22-b Advanced Kiln Crude Clay Block input repair.
advanced_menu = read("src/main/java/dev/projectflint/menu/AdvancedKilnMenu.java")
for expected in (
	"AdvancedKilnBlockEntity.FIRED_CLAY_SLOT, 86, 33, 8",
	"stack.is(ModItems.FIRED_CLAY) || stack.is(ModItems.UNFIRED_CLAY_BALL)",
):
	if expected not in advanced_menu:
		fail(f"v0.31.22-b Advanced Kiln material-slot repair missing: {expected}")

# v0.31.22-b nest-gated chicken eggs.
nest_block = read("src/main/java/dev/projectflint/block/NestBlock.java")
nest_be = read("src/main/java/dev/projectflint/block/entity/NestBlockEntity.java")
nest_system = read("src/main/java/dev/projectflint/system/ChickenNestSystem.java")
chicken_mixin = read("src/main/java/dev/projectflint/mixin/ChickenMixin.java")
blocks_3122 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
items_3122 = read("src/main/java/dev/projectflint/registry/ModItems.java")
block_entities_3122 = read("src/main/java/dev/projectflint/registry/ModBlockEntities.java")
for expected in ("MAX_EGGS = 4", "UUID owner", "addEgg(UUID chickenId)", "owner.equals(chickenId)"):
	if expected not in nest_be:
		fail(f"v0.31.22-b Chicken Nest storage/ownership missing: {expected}")
for expected in ("SEARCH_RADIUS = 10", "distance > SEARCH_RADIUS * SEARCH_RADIUS", "!nest.hasOwner()", "nest.isOwnedBy(chickenId)"):
	if expected not in nest_system:
		fail(f"v0.31.22-b Chicken Nest lookup missing: {expected}")
for expected in ("@Mixin(Chicken.class)", "chicken.eggTime > 1", "ChickenNestSystem.tryLayEgg", "chicken.eggTime = 200"):
	if expected not in chicken_mixin:
		fail(f"v0.31.22-b chicken egg interception missing: {expected}")
for expected, body in (
	('public static final NestBlock NEST = registerNest("nest")', blocks_3122),
	('public static final Item NEST = registerBlockItem("nest", ModBlocks.NEST, 1)', items_3122),
	('BlockEntityType<NestBlockEntity> NEST', block_entities_3122),
):
	if expected not in body:
		fail(f"v0.31.22-b Chicken Nest registration missing: {expected}")
for path in (
	"src/main/resources/assets/projectflint/blockstates/nest.json",
	"src/main/resources/assets/projectflint/models/block/nest.json",
	"src/main/resources/assets/projectflint/models/item/nest.json",
	"src/main/resources/assets/projectflint/items/nest.json",
	"src/main/resources/data/projectflint/loot_table/blocks/nest.json",
):
	if not (ROOT / path).exists():
		fail(f"v0.31.22-b Chicken Nest resource missing: {path}")
if "Containers.dropContents(level, pos, nest)" not in nest_block:
	fail("v0.31.22-b breaking a Chicken Nest must return stored eggs")

# v0.31.22-b pause-menu cleanup / optional Flint Quests button.
pause_3122 = read("src/main/java/dev/projectflint/mixin/PauseScreenMixin.java")
for expected in (
	'isModLoaded("flintquests")',
	'Component.literal("Quests")',
	'com.projectflint.flintquests.client.QuestBookScreen',
	'button.setX(screen.width / 2 - 102)',
	'button.setWidth(204)',
):
	if expected not in pause_3122:
		fail(f"v0.31.22-b pause-menu cleanup missing: {expected}")

# v0.31.24-b: Well / beam / dowsing / parchment progression.
items = read("src/main/java/dev/projectflint/registry/ModItems.java")
blocks = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
grinder = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
aquifer = read("src/main/java/dev/projectflint/system/AquiferSystem.java")
well_multiblock = read("src/main/java/dev/projectflint/system/WellMultiblock.java")
well_entity = read("src/main/java/dev/projectflint/block/entity/WellPulleyBlockEntity.java")
beam_item = read("src/main/java/dev/projectflint/item/ScrewedBeamBlockItem.java")
parchment_item = read("src/main/java/dev/projectflint/item/RecipeParchmentItem.java")
parchment_model = read("src/main/resources/assets/projectflint/items/recipe_parchment.json")
if 'CRUDE_CLAY_BLOCK, properties.stacksTo(3)' not in items and 'CRUDE_CLAY_BLOCK, properties.stacksTo(3)' not in items.replace('ModBlocks.', ''):
    if 'properties.stacksTo(3)' not in items[items.find('CRUDE_CLAY_BLOCK'):items.find('CRUDE_CLAY_BLOCK')+220]:
        fail("Crude Clay Block must stack to 3")
for expected in ('properties.durability(16)', 'ScrewedBeamBlockItem', 'BRICK_SHINGLES', 'WELL_PULLEY'):
    if expected not in items:
        fail(f"v0.31.24-b item registration missing: {expected}")
for expected in ('registerBrickShingles', 'registerWellPulley', 'isWoodenBeam'):
    if expected not in blocks:
        fail(f"v0.31.24-b block registration missing: {expected}")
for expected in ('Ingredient.of(Items.STICK), 2', 'new ItemStack(ModItems.AQUIFER_FINDER)', 'Ingredient.of(Items.BRICKS), 1', 'new ItemStack(ModItems.BRICK_SHINGLES, 3)'):
    if expected not in grinder:
        fail(f"Wooden Grinder Well-age recipe missing: {expected}")
for expected in ('getOffhandItem()', 'WOODEN_SCREW', 'screw.shrink(1)'):
    if expected not in beam_item:
        fail(f"Survival screwed-beam placement missing: {expected}")
for expected in ('findCenteredBelow', 'hit.center().getX() != wellCenter.getX()'):
    if expected not in aquifer:
        fail(f"exact aquifer-center Well hook missing: {expected}")
for expected in ('Three-block-deep hollow brick shaft', 'ModBlocks.ROPE', 'ModBlocks.BRICK_SHINGLES', 'findCenteredBelow'):
    if expected not in well_multiblock:
        fail(f"Well multiblock rule missing: {expected}")
for expected in ('OUTPUT_SLOTS = 8', 'ropeQualityBonus(ropeCount())', 'BASE_CYCLE_TICKS = 20 * 60', 'Items.CLAY_BALL', 'pendingFlintUnits >= 4', 'Items.FLINT', 'FLINT_CHIP'):
    if expected not in well_entity:
        fail(f"Well production/buffer rule missing: {expected}")
for expected in ('CustomModelData', 'UNFINISHED_BACON_RECIPE.equals(recipeId)', '"incomplete"'):
    if expected not in parchment_item:
        fail(f"incomplete parchment model marker missing: {expected}")
for expected in ('minecraft:custom_model_data', 'recipe_parchment_incomplete', 'recipe_parchment_filled'):
    if expected not in parchment_model:
        fail(f"Recipe Parchment item model missing state: {expected}")
import hashlib
for path, expected_hash in (
    ("src/main/resources/assets/projectflint/textures/block/brick_shingles.png", "af205973306a894bfea7f37b6bf3393543b634c00cd0e429de25c0544b17b21a"),
    ("src/main/resources/assets/projectflint/textures/item/recipe_parchment_incomplete.png", "da6a81ef7f321b17524244f6b7aa22d7b77da2217c47e23ae959e56d3e8c7add"),
):
    asset = ROOT / path
    if not asset.exists():
        fail(f"v0.31.24-b owner asset missing: {path}")
    elif hashlib.sha256(asset.read_bytes()).hexdigest() != expected_hash:
        fail(f"v0.31.24-b owner asset pixels changed: {path}")

for path in (
    "src/main/resources/assets/projectflint/textures/block/brick_shingles.png",
    "src/main/resources/assets/projectflint/textures/item/recipe_parchment_incomplete.png",
    "src/main/resources/assets/projectflint/blockstates/brick_shingles.json",
    "src/main/resources/assets/projectflint/blockstates/well_pulley.json",
    "src/main/resources/data/projectflint/loot_table/blocks/brick_shingles.json",
    "src/main/java/dev/projectflint/menu/WellPulleyMenu.java",
    "src/main/java/dev/projectflint/client/screen/WellPulleyScreen.java",
):
    if not (ROOT / path).exists():
        fail(f"v0.31.24-b file missing: {path}")

# v0.31.25-b: mortar-smoker Advanced Kiln + Brick Furnace family.
brick_furnace_block = read("src/main/java/dev/projectflint/block/BrickFurnaceBlock.java")
brick_blast_block = read("src/main/java/dev/projectflint/block/BrickBlastFurnaceBlock.java")
brick_furnace_entity = read("src/main/java/dev/projectflint/block/entity/BrickFurnaceBlockEntity.java")
brick_furnace_menu = read("src/main/java/dev/projectflint/menu/BrickFurnaceMenu.java")
brick_furnace_screen = read("src/main/java/dev/projectflint/client/screen/BrickFurnaceScreen.java")
blocks_3125 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
items_3125 = read("src/main/java/dev/projectflint/registry/ModItems.java")
entities_3125 = read("src/main/java/dev/projectflint/registry/ModBlockEntities.java")
menus_3125 = read("src/main/java/dev/projectflint/registry/ModMenus.java")
tuning_3125 = read("src/main/java/dev/projectflint/config/GameplayTuning.java")
for expected in (
	'BRICK_FURNACE = registerBrickFurnace("brick_furnace")',
	'BRICK_BLAST_FURNACE = registerBrickBlastFurnace("brick_blast_furnace")',
):
	if expected not in blocks_3125:
		fail(f"v0.31.25-b block registration missing: {expected}")
for expected in (
	'BRICK_FURNACE = registerBlockItem("brick_furnace", ModBlocks.BRICK_FURNACE, 1)',
	'BRICK_BLAST_FURNACE = registerBlockItem("brick_blast_furnace", ModBlocks.BRICK_BLAST_FURNACE, 1)',
):
	if expected not in items_3125:
		fail(f"v0.31.25-b item registration missing: {expected}")
for expected in (
	'FabricBlockEntityTypeBuilder.create(BrickFurnaceBlockEntity::new, ModBlocks.BRICK_FURNACE, ModBlocks.BRICK_BLAST_FURNACE)',
):
	if expected not in entities_3125:
		fail(f"v0.31.25-b Brick Furnace block entity wiring missing: {expected}")
for expected in ('BrickFurnaceMenu::furnace', 'BrickFurnaceMenu::blastFurnace'):
	if expected not in menus_3125:
		fail(f"v0.31.25-b Brick Furnace menu wiring missing: {expected}")
for expected in (
	'OUTPUT_START = 2', 'OUTPUT_END = 6', 'SLOT_COUNT = 6',
	'PrimitiveProcessingRegistry.find(PrimitiveProcessor.KILN',
	'FUEL_CHARCOAL_PER_IGNITION = 1',
	'BURN_TICKS = 10 * 60 * 20',
	'insertOutput(recipe.get().resultCopy())',
):
	if expected not in brick_furnace_entity:
		fail(f"v0.31.25-b Brick Furnace batch processing missing: {expected}")
if 'BRICK_FURNACE_COOK_SPEED = 1.50F' not in tuning_3125:
	fail("v0.31.25-b Brick Furnace must run at 1.50x baseline")
if 'BRICK_BLAST_FURNACE_COOK_SPEED = BRICK_FURNACE_COOK_SPEED' not in tuning_3125:
	fail("v0.31.25-b Blast Furnace placeholder must share Brick Furnace tuning")
for expected in ('brick_furnace', 'brick_blast_furnace'):
	if expected not in brick_furnace_menu:
		fail(f"v0.31.25-b Flint Quests workstation handler missing: {expected}")
if not brick_furnace_screen:
	fail("v0.31.25-b Brick Furnace screen missing")
for path in (
	"src/main/resources/assets/projectflint/blockstates/brick_furnace.json",
	"src/main/resources/assets/projectflint/blockstates/brick_blast_furnace.json",
	"src/main/resources/assets/projectflint/models/block/brick_furnace.json",
	"src/main/resources/assets/projectflint/models/block/brick_furnace_lit.json",
	"src/main/resources/assets/projectflint/models/block/brick_blast_furnace.json",
	"src/main/resources/assets/projectflint/models/block/brick_blast_furnace_lit.json",
	"src/main/resources/assets/projectflint/textures/block/brick_furnace_front.png",
	"src/main/resources/assets/projectflint/textures/block/brick_furnace_front_on.png",
	"src/main/resources/assets/projectflint/textures/block/brick_blast_furnace_front.png",
	"src/main/resources/assets/projectflint/textures/block/brick_blast_furnace_front_on.png",
	"src/main/resources/assets/projectflint/textures/block/advanced_kiln_front.png",
	"src/main/resources/assets/projectflint/textures/block/advanced_kiln_front_active.png",
):
	if not (ROOT / path).exists():
		fail(f"v0.31.25-b workstation/Advanced Kiln resource missing: {path}")
for forbidden in (
	"src/main/resources/data/projectflint/recipe/brick_furnace.json",
	"src/main/resources/data/projectflint/recipe/brick_blast_furnace.json",
):
	if (ROOT / forbidden).exists():
		fail(f"v0.31.25-b must not add Survival crafting recipe yet: {forbidden}")
# Owner-supplied Furnace/Blast Furnace pixels remain byte-for-byte.
import hashlib as _hashlib_3125
for path, expected_hash in (
	("src/main/resources/assets/projectflint/textures/block/brick_furnace_front.png", "51404a10e5b07491471cae2f918a3f34de7908fd0245d0a37f7db1873ee5d266"),
	("src/main/resources/assets/projectflint/textures/block/brick_furnace_front_on.png", "fe1abee2ec8a9028e6e5e19d9fa9f567fd78341c0b8d224e3c94a6bbc4328294"),
	("src/main/resources/assets/projectflint/textures/block/brick_blast_furnace_front.png", "4fc0fa561a527c52513049aaea8fe1323785fdf00fc843b0619dcf4be0edced5"),
	("src/main/resources/assets/projectflint/textures/block/brick_blast_furnace_front_on.png", "7ff82d086f4005bdcb0f0b795ae1c600b5f119e7239b78becab1b025a0b123fc"),
):
	asset = ROOT / path
	if asset.exists() and _hashlib_3125.sha256(asset.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.31.25-b supplied texture pixels changed: {path}")

# v0.31.26-b: Well layout/UI/progress/dismantling hotfix.
well_multi_3126 = read("src/main/java/dev/projectflint/system/WellMultiblock.java")
well_entity_3126 = read("src/main/java/dev/projectflint/block/entity/WellPulleyBlockEntity.java")
well_menu_3126 = read("src/main/java/dev/projectflint/menu/WellPulleyMenu.java")
well_screen_3126 = read("src/main/java/dev/projectflint/client/screen/WellPulleyScreen.java")
primitive_3126 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
progress_3126 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
tools_3126 = read("src/main/java/dev/projectflint/system/ToolCapabilities.java")
for expected in (
	'controllerPos.getY() - 6',
	'Y4: S.S / BRB / S.S',
	'Y5: ... / BRB / ...',
	'Y6: ... / BPB / ...',
	'Y7: ... / SSS / ...',
	'matchesUpperFrame(level, center, controllerPos, true)',
	'matchesUpperFrame(level, center, controllerPos, false)',
	'controllerPos.below(6)',
):
	if expected not in well_multi_3126:
		fail(f"v0.31.26-b Well upper-frame rule missing: {expected}")
for expected in ('CYCLE_WORK = BASE_CYCLE_TICKS * 100', 'progressMax = CYCLE_WORK', 'progress + qualityPoints'):
	if expected not in well_entity_3126:
		fail(f"v0.31.26-b stable Well progress missing: {expected}")
if 'Math.ceil(BASE_CYCLE_TICKS * 100.0D / qualityPoints)' in well_entity_3126:
	fail("v0.31.26-b Well progress target must not resize when Rope changes")
for expected in ('WellPulleyBlockEntity.ROPE_SLOT, 17, 44', '96 + col * 18'):
	if expected not in well_menu_3126:
		fail(f"v0.31.26-b Well slot reflow missing: {expected}")
for expected in ('menu.yieldLabel()', '"Quality " + menu.qualityPoints() + "%"', 'x + 96 + col * 18', 'menu.progressPixels(40)'):
	if expected not in well_screen_3126:
		fail(f"v0.31.26-b Well screen reflow missing: {expected}")
if 'isWellWoodTool' not in tools_3126 or 'FLINT_AXE' not in tools_3126 or 'FLINT_PLANT_CUTTERS' not in tools_3126:
	fail("v0.31.26-b dedicated Well wood dismantling helper missing")
for expected in ('state.is(ModBlocks.ROPE)', 'state.is(ModBlocks.WELL_PULLEY)', 'state.is(Blocks.BRICKS)', 'state.is(ModBlocks.BRICK_SHINGLES)'):
	if expected not in primitive_3126 or expected not in progress_3126:
		fail(f"v0.31.26-b Well dismantling gate missing: {expected}")

# v0.31.27-b: relaxed Well interiors, GUI-safe progress sync, and Creative lighting bypass fixes.
well_multi_3127 = well_multi_3126
well_entity_3127 = well_entity_3126
slow_swing_3127 = read("src/main/java/dev/projectflint/mixin/SlowHandSwingVisualMixin.java")
light_mixin_3127 = read("src/main/java/dev/projectflint/mixin/LightTextureMixin.java")
shader_3127 = shader
for expected in ('isShaftInteriorAllowed(state)', 'state.canBeReplaced()', 'if (v == 0 && !state.is(ModBlocks.BRICK_SHINGLES)) return false;'):
	if expected not in well_multi_3127:
		fail(f"v0.31.27-b relaxed Well validation missing: {expected}")
for forbidden in ('else if (!state.isAir()) {',):
	if forbidden in well_multi_3127:
		fail("v0.31.27-b Well upper frame still requires literal air in non-structure cells")
for expected in ('GUI_PROGRESS_MAX = 10_000', 'progress * GUI_PROGRESS_MAX / (float) progressMax', 'case 1 -> progressMax <= 0 ? 0 : GUI_PROGRESS_MAX'):
	if expected not in well_entity_3127:
		fail(f"v0.31.27-b GUI-safe Well progress sync missing: {expected}")
if 'player.isCreative() || player.isSpectator()' not in slow_swing_3127:
	fail("v0.31.27-b Creative/Spectator slow-hand visual bypass missing")
for expected in ('@Shadow private boolean updateLightTexture;', 'lightingMode != projectflint$lastLightingMode', 'updateLightTexture = true;', 'return -1.0F;', '!player.hasEffect(MobEffects.NIGHT_VISION)'):
	if expected not in light_mixin_3127:
		fail(f"v0.31.27-b Creative darkness refresh/vanilla-lighting hook missing: {expected}")
for expected in ('bool creative_bypass = lightmapInfo.NightVisionFactor < 0.0;', 'if (creative_bypass || lightmapInfo.NightVisionFactor > 0.0)', 'if (!creative_bypass)'):
	if expected not in shader_3127:
		fail(f"v0.31.27-b Creative vanilla-lightmap shader branch missing: {expected}")


# v0.31.29-b: Chicken Nest inventory/occupant UI and fired Clay Bucket water vessel.
nest_menu_3129 = read("src/main/java/dev/projectflint/menu/NestMenu.java")
nest_screen_3129 = read("src/main/java/dev/projectflint/client/screen/NestScreen.java")
nest_be_3129 = read("src/main/java/dev/projectflint/block/entity/NestBlockEntity.java")
nest_block_3129 = read("src/main/java/dev/projectflint/block/NestBlock.java")
menus_3129 = read("src/main/java/dev/projectflint/registry/ModMenus.java")
client_3129 = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
for expected in (
	"extends RandomizableContainerBlockEntity",
	"MAX_EGGS = 4",
	"NonNullList.withSize(MAX_EGGS, ItemStack.EMPTY)",
	"public int getMaxStackSize()",
	"return stack.is(Items.EGG);",
	"UUID owner",
):
	if expected not in nest_be_3129:
		fail(f"v0.31.29-b Chicken Nest container/ownership implementation missing: {expected}")
for expected in (
	"NEST_SLOTS = NestBlockEntity.MAX_EGGS",
	"new ContainerData()",
	"nest.hasOwner() ? 1 : 0",
	"return stack.is(Items.EGG);",
	"public int getMaxStackSize()",
	"addStandardInventorySlots(inventory, 8, 77)",
	"public boolean hasOccupant()",
):
	if expected not in nest_menu_3129:
		fail(f"v0.31.29-b Chicken Nest menu missing: {expected}")
for expected in (
	'"gui.projectflint.nest.occupied"',
	'"gui.projectflint.nest.unoccupied"',
	"menu.hasOccupant()",
):
	if expected not in nest_screen_3129:
		fail(f"v0.31.29-b Chicken Nest occupant display missing: {expected}")
for expected in ("player.openMenu(nest)", "Containers.dropContents(level, pos, nest)"):
	if expected not in nest_block_3129:
		fail(f"v0.31.29-b Chicken Nest interaction/drop behavior missing: {expected}")
if 'MenuType<NestMenu> NEST' not in menus_3129:
	fail("v0.31.29-b Chicken Nest menu type is not registered")
if 'MenuScreens.register(ModMenus.NEST, NestScreen::new)' not in client_3129:
	fail("v0.31.29-b Chicken Nest screen is not registered")

try:
	nest_model_3129 = json.loads(read("src/main/resources/assets/projectflint/models/block/nest.json") or "{}")
	floor_elements = [
		e for e in nest_model_3129.get("elements", [])
		if e.get("from") == [4, 0, 4] and e.get("to") == [12, 1, 12]
	]
	if not floor_elements:
		fail("v0.31.29-b Chicken Nest model is missing the one-pixel center hay floor")
	elif floor_elements[0].get("faces", {}).get("up", {}).get("texture") != "#top":
		fail("v0.31.29-b Chicken Nest center floor does not use the hay top texture")
except Exception as exc:
	fail(f"v0.31.29-b could not validate Chicken Nest model floor: {exc}")

items_3129 = read("src/main/java/dev/projectflint/registry/ModItems.java")
creative_3129 = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
wooden_3129 = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
processing_3129 = read("src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java")
clay_bucket_3129 = read("src/main/java/dev/projectflint/item/ClayBucketItem.java")
clay_water_bucket_3129 = read("src/main/java/dev/projectflint/item/ClayWaterBucketItem.java")
for expected in (
	'UNFIRED_CLAY_BUCKET = register("unfired_clay_bucket"',
	'CLAY_BUCKET = register("clay_bucket"',
	'CLAY_WATER_BUCKET = register("clay_water_bucket"',
):
	if expected not in items_3129:
		fail(f"v0.31.29-b Clay Bucket item registration missing: {expected}")
for expected in ("ModItems.UNFIRED_CLAY_BUCKET", "ModItems.CLAY_BUCKET", "ModItems.CLAY_WATER_BUCKET"):
	if expected not in creative_3129:
		fail(f"v0.31.29-b Clay Bucket creative inventory entry missing: {expected}")
grinder_bucket_recipe_3129 = 'recipe(Ingredient.of(Items.CLAY_BALL), 3, Ingredient.of(ModItems.WOVEN_CHEST_FRAME), 1, new ItemStack(ModItems.UNFIRED_CLAY_BUCKET))'
if grinder_bucket_recipe_3129 not in wooden_3129:
	fail("v0.31.29-b Wooden Grinder must make 1 Unfired Clay Bucket from 3 Clay Balls + 1 Woven Chest Frame")
for expected in (
	'recipe("clay_bucket", ModItems.UNFIRED_CLAY_BUCKET, new ItemStack(ModItems.CLAY_BUCKET)',
	"GameplayTuning.KILN_MEAT_COOK_TICKS",
	"kilnOnly",
):
	if expected not in processing_3129:
		fail(f"v0.31.29-b Clay Bucket kiln firing path missing: {expected}")
for expected in (
	"super(Fluids.EMPTY, properties)",
	"ClipContext.Fluid.SOURCE_ONLY",
	"fluid.isSource()",
	"fluid.is(FluidTags.WATER)",
	"ModItems.CLAY_WATER_BUCKET",
):
	if expected not in clay_bucket_3129:
		fail(f"v0.31.29-b source-water-only Clay Bucket behavior missing: {expected}")
if "Fluids.LAVA" in clay_bucket_3129 or "FluidTags.LAVA" in clay_bucket_3129:
	fail("v0.31.29-b empty Clay Bucket must not accept lava")
for expected in ("super(Fluids.WATER, properties)", "super.use(level, player, hand)", "ModItems.CLAY_BUCKET"):
	if expected not in clay_water_bucket_3129:
		fail(f"v0.31.29-b filled Clay Bucket placement/return behavior missing: {expected}")
if "Fluids.LAVA" in clay_water_bucket_3129:
	fail("v0.31.29-b filled Clay Bucket must remain water-only")

for stem in ("unfired_clay_bucket", "clay_bucket", "clay_water_bucket"):
	for rel in (
		f"src/main/resources/assets/projectflint/items/{stem}.json",
		f"src/main/resources/assets/projectflint/models/item/{stem}.json",
		f"src/main/resources/assets/projectflint/textures/item/{stem}.png",
	):
		if not (ROOT / rel).exists():
			fail(f"v0.31.29-b Clay Bucket resource missing: {rel}")
lang_3129 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json") or "{}")
for key, value in (
	("item.projectflint.unfired_clay_bucket", "Unfired Clay Bucket"),
	("item.projectflint.clay_bucket", "Clay Bucket"),
	("item.projectflint.clay_water_bucket", "Clay Bucket (Water)"),
	("container.projectflint.nest", "Chicken Nest"),
	("gui.projectflint.nest.occupied", "Occupant: Chicken"),
	("gui.projectflint.nest.unoccupied", "Occupant: None"),
):
	if lang_3129.get(key) != value:
		fail(f"v0.31.29-b translation missing/changed: {key}")

for rel, expected_hash in (
	("src/main/resources/assets/projectflint/textures/item/unfired_clay_bucket.png", "595eb57c43b47d73edb2d9c39a0ca1a811898dc42d44db228f439bc8c51748ab"),
	("src/main/resources/assets/projectflint/textures/item/clay_bucket.png", "757f5130f989026fc236a2ea563ac8f9a95809f2f7f1d1372052408b3de6fb79"),
	("src/main/resources/assets/projectflint/textures/item/clay_water_bucket.png", "df6027f7dd9209e26403ed4c01b1cb47e2765d7420f637948e052e326925c6b0"),
):
	asset = ROOT / rel
	if asset.exists() and hashlib.sha256(asset.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.31.29-b Clay Bucket texture pixels changed: {rel}")

cleanup_3129 = read("APPLY_PROJECTFLINT_0_31_48_B.bat")
for retired in ("APPLY_PROJECTFLINT_0_31_29_B.bat", "APPLY_PROJECTFLINT_0_31_28_B.bat"):
	if retired not in cleanup_3129:
		fail(f"v0.31.31-b cleanup must retire previous cleanup BAT: {retired}")


# v0.31.30-b: owner-supplied Strawberries with berry-bush-style growth, cutter harvesting, and temperate worldgen.
strawberry_block = read("src/main/java/dev/projectflint/block/StrawberryBushBlock.java")
blocks_3130 = read("src/main/java/dev/projectflint/registry/ModBlocks.java")
items_3130 = read("src/main/java/dev/projectflint/registry/ModItems.java")
creative_3130 = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
client_3130 = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
worldgen_3130 = read("src/main/java/dev/projectflint/world/ModWorldgen.java")
primitive_3130 = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
gamemode_3130 = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
progress_3130 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")

for expected in (
	"extends SweetBerryBushBlock",
	"return new ItemStack(ModItems.STRAWBERRY_SEEDS)",
	"protected void entityInside",
	"protected InteractionResult useWithoutItem",
):
	if expected not in strawberry_block:
		fail(f"v0.31.30-b Strawberry Plant implementation missing: {expected}")
if "super.entityInside" in strawberry_block:
	fail("v0.31.30-b Strawberry Plant must not inherit Sweet Berry thorn slowdown/damage")
for expected in (
	'STRAWBERRY_BUSH = registerStrawberryBush("strawberry_bush")',
	"noCollision().noOcclusion().noLootTable().randomTicks()",
	"SoundType.SWEET_BERRY_BUSH",
):
	if expected not in blocks_3130:
		fail(f"v0.31.30-b Strawberry block registration missing: {expected}")
for expected in (
	'STRAWBERRY = register("strawberry"',
	'STRAWBERRY_SEEDS = register("strawberry_seeds"',
	"new StrawberrySeedsItem(properties.stacksTo(16))",
	"new Item(properties.stacksTo(16).food(",
	"nutrition(1)",
	"saturationModifier(0.05F)",
):
	if expected not in items_3130:
		fail(f"v0.31.30-b Strawberry item/food registration missing: {expected}")
if "output.accept(ModItems.STRAWBERRY);" not in creative_3130:
	fail("v0.31.30-b Strawberry missing from Project Flint creative tab")
if "BlockRenderLayerMap.putBlock(ModBlocks.STRAWBERRY_BUSH, ChunkSectionLayer.CUTOUT);" not in client_3130:
	fail("v0.31.30-b Strawberry Plant is not registered on the cutout render layer")

for expected in (
	"clicked.is(ModBlocks.STRAWBERRY_BUSH)",
	"ModItems.isPlantCutterLike(held)",
	"1 + level.getRandom().nextInt(2)",
	"new ItemStack(ModItems.STRAWBERRY, strawberries)",
	"clicked.setValue(SweetBerryBushBlock.AGE, 1)",
	"held.hurtAndBreak(1, player, hand)",
):
	if expected not in primitive_3130:
		fail(f"v0.31.30-b Strawberry survival harvest behavior missing: {expected}")
for expected in (
	"state.is(ModBlocks.STRAWBERRY_BUSH)",
	"ModItems.canHarvestSweetBerrySeeds(held)",
	"new ItemStack(ModItems.STRAWBERRY_SEEDS)",
	"held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND)",
):
	if expected not in gamemode_3130:
		fail(f"v0.31.30-b Strawberry whole-plant recovery missing: {expected}")
if "state.is(ModBlocks.STRAWBERRY_BUSH)" not in progress_3130 or "1.0F / 130.0F" not in progress_3130:
	fail("v0.31.30-b Strawberry Plant destroy-progress gate is missing")

for biome in ("Biomes.BIRCH_FOREST", "Biomes.OLD_GROWTH_BIRCH_FOREST", "Biomes.FLOWER_FOREST", "Biomes.FOREST", "Biomes.MEADOW"):
	if biome not in worldgen_3130:
		fail(f"v0.31.30-b Strawberry worldgen biome missing: {biome}")
if 'placed("strawberry_patch")' not in worldgen_3130:
	fail("v0.31.30-b Strawberry placed feature is not attached to biomes")

try:
	configured_3130 = json.loads(read("src/main/resources/data/projectflint/worldgen/configured_feature/strawberry_patch.json") or "{}")
	cfg = configured_3130.get("config", {})
	placed_state = cfg.get("feature", {}).get("feature", {}).get("config", {}).get("to_place", {}).get("state", {})
	if configured_3130.get("type") != "minecraft:random_patch":
		fail("v0.31.30-b Strawberry configured feature must use minecraft:random_patch")
	for key, value in (("tries", 96), ("xz_spread", 7), ("y_spread", 3)):
		if cfg.get(key) != value:
			fail(f"v0.31.30-b Strawberry patch {key} must be {value}")
	if placed_state.get("Name") != "projectflint:strawberry_bush" or placed_state.get("Properties", {}).get("age") != "3":
		fail("v0.31.30-b natural Strawberry patches must place mature age-3 plants")
	configured_text = json.dumps(configured_3130, sort_keys=True)
	if "minecraft:grass_block" not in configured_text or "minecraft:air" not in configured_text:
		fail("v0.31.30-b Strawberry patch must require air over Grass Block")
except Exception as exc:
	fail(f"v0.31.30-b Strawberry configured feature validation failed: {exc}")

try:
	placed_3130 = json.loads(read("src/main/resources/data/projectflint/worldgen/placed_feature/strawberry_patch.json") or "{}")
	if placed_3130.get("feature") != "projectflint:strawberry_patch":
		fail("v0.31.30-b Strawberry placed feature points at the wrong configured feature")
	placements = placed_3130.get("placement", [])
	rarity = next((entry for entry in placements if entry.get("type") == "minecraft:rarity_filter"), None)
	if rarity is None or rarity.get("chance") != 32:
		fail("v0.31.30-b Strawberry patch rarity must remain 1 in 32")
	placement_types = {entry.get("type") for entry in placements}
	for required in ("minecraft:in_square", "minecraft:heightmap", "minecraft:biome"):
		if required not in placement_types:
			fail(f"v0.31.30-b Strawberry placed feature missing placement modifier: {required}")
except Exception as exc:
	fail(f"v0.31.30-b Strawberry placed feature validation failed: {exc}")

for stage in range(4):
	for rel in (
		f"src/main/resources/assets/projectflint/models/block/strawberry_bush_stage{stage}.json",
		f"src/main/resources/assets/projectflint/textures/block/strawberry_bush_stage{stage}.png",
	):
		if not (ROOT / rel).exists():
			fail(f"v0.31.30-b Strawberry growth-stage resource missing: {rel}")
for rel in (
	"src/main/resources/assets/projectflint/blockstates/strawberry_bush.json",
	"src/main/resources/assets/projectflint/items/strawberry.json",
	"src/main/resources/assets/projectflint/models/item/strawberry.json",
	"src/main/resources/assets/projectflint/textures/item/strawberry.png",
):
	if not (ROOT / rel).exists():
		fail(f"v0.31.30-b Strawberry resource missing: {rel}")

lang_3130 = json.loads(read("src/main/resources/assets/projectflint/lang/en_us.json") or "{}")
for key, value in (("block.projectflint.strawberry_bush", "Strawberry Plant"), ("item.projectflint.strawberry", "Strawberry")):
	if lang_3130.get(key) != value:
		fail(f"v0.31.30-b Strawberry translation missing/changed: {key}")

strawberry_asset_hashes = {
	"src/main/resources/assets/projectflint/textures/item/strawberry.png": "2af63b1970e9a80c20885befa2e6dd370b028996a18cafed5c7f2bec7df668cd",
	"src/main/resources/assets/projectflint/textures/block/strawberry_bush_stage0.png": "481ff5c8edd2d284c46f5d3b4b253090c19a2dba25b21279ca1de170e39527e2",
	"src/main/resources/assets/projectflint/textures/block/strawberry_bush_stage1.png": "ef315253b0e8497b538219561a06ca64b847300cd61f378eed632b623764876a",
	"src/main/resources/assets/projectflint/textures/block/strawberry_bush_stage2.png": "5f829277abc3be08fee85d386e499e6e2af75c74bb805d758cd3f1b6590679d1",
	"src/main/resources/assets/projectflint/textures/block/strawberry_bush_stage3.png": "e2bb913b5e82ba66f6cb3813a92d50f2d1da6628f1b02d38946d00cd726470b4",
}
for rel, expected_hash in strawberry_asset_hashes.items():
	asset = ROOT / rel
	if asset.exists() and hashlib.sha256(asset.read_bytes()).hexdigest() != expected_hash:
		fail(f"v0.31.30-b owner-supplied Strawberry texture pixels changed: {rel}")

cleanup_3130 = read("APPLY_PROJECTFLINT_0_31_48_B.bat")
if "APPLY_PROJECTFLINT_0_31_29_B.bat" not in cleanup_3130:
	fail("v0.31.31-b cleanup must retire the previous v0.31.29-b cleanup BAT")


# v0.31.31-b: string pickup, separated seeds/foods, reusable brick form, heat, nonlinear aquifer tuning.
from hashlib import sha256
item_java = read("src/main/java/dev/projectflint/registry/ModItems.java")
item_tab = read("src/main/java/dev/projectflint/registry/ModCreativeTabs.java")
seed_java = read("src/main/java/dev/projectflint/item/StrawberrySeedsItem.java")
for symbol in ("STRAWBERRY_SEEDS", "STRAWBERRY_JAM", "STRAWBERRY_PIE", "UNFIRED_BRICK_MOLD", "UNFIRED_BRICK"):
	if f'public static final Item {symbol} =' not in item_java or f'output.accept(ModItems.{symbol});' not in item_tab:
		fail(f"v31 missing registered item or creative tab entry: {symbol}")
for expected in ("class StrawberrySeedsItem extends Item", "ModBlocks.STRAWBERRY_BUSH.defaultBlockState()", "seeds.shrink(1)"):
	if expected not in seed_java: fail(f"v31 Strawberry seed behavior missing: {expected}")
if 'new BlockItem(ModBlocks.STRAWBERRY_BUSH' in item_java: fail("v31 Strawberry fruit must not plant the bush")
if 'STRAWBERRY_SEEDS' not in strawberry_block or 'STRAWBERRY_SEEDS' not in gamemode_3130:
	fail("v31 strawberry clone/uproot must provide seeds")
wood = read("src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java")
for expected in (
	"Ingredient.of(Items.CLAY_BALL), 3, Ingredient.of(ModItems.BONE_KNIFE)",
	"Ingredient.of(ModItems.WOODEN_SHEET), 8, Ingredient.of(ModItems.BONE_KNIFE)",
	"reusableRecipe(Ingredient.of(Items.CLAY_BALL), 8, Ingredient.of(ModItems.UNFIRED_BRICK_MOLD)",
	"new ItemStack(ModItems.UNFIRED_BRICK, 8)",
	"new ItemStack(ModItems.WATTLED_DAUB_BRICKS, 12)",
	"recipe(Ingredient.of(Items.BRICK), 2, Ingredient.of(Items.BRICK), 2, new ItemStack(Items.BRICKS))",
):
	if expected not in wood: fail(f"v31 Wooden Grinder missing: {expected}")
if wood.index('recipe(Ingredient.of(Items.BRICK), 2, Ingredient.of(Items.BRICK), 2') > wood.index('recipe(Ingredient.of(Items.BRICK), 1, Ingredient.of(Items.BRICK), 1'):
	fail('v31 4-brick grinder recipe must precede the overlapping 2-brick recipe')
stove = read("src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java")
books = read("src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java")
for expected in ('projectflint:strawberry_jam', 'projectflint:strawberry_pie'):
	if expected not in stove or expected not in books: fail(f"v31 oven/book research missing {expected}")
if 'req(Ingredient.of(ModItems.STRAWBERRY), 12)' not in stove: fail('v31 strawberry jam should cost 12 berries, vs sweet berry jam 8')
if 'req(Ingredient.of(ModItems.STRAWBERRY), 2)' not in stove: fail('v31 strawberry pie should cost two extra berries')
processing = read("src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java")
for expected in ('recipe("brick", ModItems.UNFIRED_BRICK, new ItemStack(Items.BRICK)', 'recipe("clay_bucket", ModItems.UNFIRED_CLAY_BUCKET', 'GameplayTuning.KILN_MEAT_COOK_TICKS, both'):
	if expected not in processing: fail(f"v31 all-primitive-heat processing missing: {expected}")
if 'input.is(ModItems.UNFIRED_CLAY_BUCKET) || input.is(ModItems.UNFIRED_BRICK)' not in stove:
	fail('v31 direct Primitive Oven mode should support firing clay bucket and bricks')
for item,output in (('unfired_brick','minecraft:brick'), ('unfired_clay_bucket','projectflint:clay_bucket')):
	for cook in ('smelting','blasting','smoking','campfire_cooking'):
		obj=json.loads(read(f'src/main/resources/data/projectflint/recipe/{item}_{cook}.json') or '{}')
		if obj.get('type') != f'minecraft:{cook}' or obj.get('result',{}).get('id') != output or obj.get('ingredient') != f'projectflint:{item}':
			fail(f'v31 vanilla heat recipe mismatch: {item}_{cook}')
for recipe_path,item,output,num in (
	('unfired_clay_from_brick.json','projectflint:unfired_brick','projectflint:unfired_clay_ball',2),
	('unfired_clay_from_unfired_brick.json','projectflint:unfired_brick','projectflint:unfired_clay_ball',2),
	('wattled_daub_bricks_from_brick.json','minecraft:brick','projectflint:wattled_daub_bricks',12),
):
	obj=json.loads(read('src/main/resources/data/projectflint/recipe/'+recipe_path) or '{}')
	if obj.get('result',{}).get('id') != output or obj.get('result',{}).get('count') != num or item not in json.dumps(obj):
		fail(f'v31 recycling/block conversion wrong: {recipe_path}')
if 'new Definition("projectflint:unfired_brick", 1, "projectflint:unfired_clay_ball", 2)' not in read('src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java'):
	fail('v31 new brick-to-clay conversion missing from Clay Grinder guide')
aquifer=read('src/main/java/dev/projectflint/system/AquiferSystem.java')
well=read('src/main/java/dev/projectflint/block/entity/WellPulleyBlockEntity.java')
for expected in ('SCARCE(0.13F','NORMAL(0.68F','GOOD(0.94F','ABUNDANT(1.30F'):
	if expected not in aquifer: fail(f'v31 aquifer quality rebalancing missing: {expected}')
for expected in ('rope * 3 + rope * rope / 4', 'ropeQualityBonus(ropeCount())', 'SoundEvents.MINECART_RIDING', '0.12F, 1.18F', 'serverLevel.getGameTime() % 70L'):
	if expected not in well: fail(f'v31 Well rope/sound tuning missing: {expected}')
drops=read('src/main/java/dev/projectflint/system/PrimitiveAnimalDropSystem.java')
placed=read('src/main/java/dev/projectflint/system/PlacedItemSystem.java')
if 'blocks/tripwire' not in drops or 'if (!entry.is(tripwireLoot)) drops.removeIf(stack -> stack.is(Items.STRING));' not in drops:
	fail('v31 placed String/tripwire must drop without restoring unrelated string loot')
if 'holder.setResponse(true);' not in placed or 'if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) reclaim(serverPlayer, entity);' not in placed:
	fail('v31 Place-in-World item pickup regression')
assets31={
	"unfired_brick_mold.png": "71c3bfd41b7d8ec99ae44bc35fc5e2720aa607b34515584d59b84c8319d078bd",
	"unfired_brick.png": "1a96516c5bf3fc43bab1eeb034f3c673ee65d651ec6d8050c2dea3be4dab4b07",
	"strawberry_seeds.png": "486881309cc28bfb45667c02cf10ba69dcd00af6f06e82e0aa658eb2791fa28b",
	"strawberry_jam.png": "102143b39d14a6c5a991fd292a3c96249689ada59fc8132f1e4569adc9dab90b",
	"strawberry_pie.png": "399d585b37606c7ac8cb7b8944b68bdea79635ae8e309fa454ee1c2f17b162ad",
}
for name,checksum in assets31.items():
	root='src/main/resources/assets/projectflint/'
	for required in (root+'textures/item/'+name, root+'items/'+name[:-4]+'.json', root+'models/item/'+name[:-4]+'.json'):
		if not (ROOT/required).exists():fail(f'v31 missing sprite/item/model: {required}')
	tex=ROOT/root/'textures/item'/name
	if tex.exists() and sha256(tex.read_bytes()).hexdigest()!=checksum:fail(f'v31 altered user texture: {name}')
for key in ('strawberry_seeds','strawberry_jam','strawberry_pie','unfired_brick_mold','unfired_brick'):
	if f'item.projectflint.{key}' not in lang_3130:fail(f'v31 translation missing: {key}')
for critical in ('src/main/java/dev/projectflint/compat/emi/ProjectFlintEmiPlugin.java','libs/PF-EMI-1.21.11.jar'):
	if (ROOT/critical).exists():fail('v31 must not restore external PF-EMI compile bridge')
if (ROOT/'APPLY_PROJECTFLINT_0_31_30_B.bat').exists():fail('v31 cleanup must retire v30 cleanup BAT')
if 'APPLY_PROJECTFLINT_0_31_30_B.bat' not in cleanup_bat:fail('v31 cleanup must delete the old v30 cleanup BAT')
if (ROOT/'APPLY_PROJECTFLINT_0_31_31_B.bat').exists():fail('v32 must retire the old v31 cleanup BAT')
if 'APPLY_PROJECTFLINT_0_31_31_B.bat' not in cleanup_bat:fail('v32 cleanup must delete the old v31 cleanup BAT')



# v0.31.32-b: wheat/dairy/fat/food, turtle meat, fireclay, Brick Furnace tanning, UI hints and night packs.
items32=read('src/main/java/dev/projectflint/registry/ModItems.java')
tabs32=read('src/main/java/dev/projectflint/registry/ModCreativeTabs.java')
for key in ('FLOUR','FAT','SUSHI_ROLL','CHEESE','RAW_TURTLE_MEAT','COOKED_TURTLE_MEAT','UNFIRED_FIRECLAY_BRICK','FIRECLAY_BRICK','FIRECLAY_BRICK_BLOCK','FIRECLAY_BRICK_SLAB','FIRECLAY_BRICK_STAIRS','TANNED_LEATHER','CLAY_MILK_BUCKET'):
	if f'public static final Item {key} =' not in items32 or f'output.accept(ModItems.{key});' not in tabs32:
		fail(f'v32 missing registered item or creative tab entry: {key}')
blocks32=read('src/main/java/dev/projectflint/registry/ModBlocks.java')
for key in ('FIRECLAY_BRICK_BLOCK','FIRECLAY_BRICK_SLAB','FIRECLAY_BRICK_STAIRS'):
	if f'public static final ' not in blocks32 or f' {key} =' not in blocks32:
		fail(f'v32 missing fireclay block: {key}')
wood32=read('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java')
for expected in ('Ingredient.of(ModItems.FLAX_SEEDS), 9, Ingredient.of(ModItems.CLAY_WATER_BUCKET)', 'new ItemStack(Items.WHEAT_SEEDS, 3)', 'Ingredient.of(Items.WHEAT), 9, Ingredient.of(ModItems.CLAY_WATER_BUCKET)', 'new ItemStack(ModItems.FLOUR, 9)', 'recipe(rawMeat(), 1, Ingredient.of(ModItems.CLAY_WATER_BUCKET)', 'Ingredient.of(ModItems.WOODEN_CUP), 2, Ingredient.of(ModItems.CLAY_MILK_BUCKET)', 'new ItemStack(ModItems.WOODEN_CUP_RAW_MILK, 4)', 'Ingredient.of(ModItems.PRIMITIVE_STOVE), 1, Ingredient.of(ModItems.FIRECLAY_BRICK_BLOCK), 8', 'Ingredient.of(ModItems.UNFIRED_BRICK), 2, Ingredient.of(ModItems.SAND_FLAKE), 1'):
	if expected not in wood32: fail('v32 Wooden Grinder recipe missing: '+expected)
if 'WOODEN_CUP_RAW_MILK' in wood32.split('public static Ingredient rawMeat()',1)[1].split('}',1)[0]:
	fail('v32 raw meat ingredient must exclude milk')
grinder32=read('src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java')
for expected in ('isFilledClayBucket','ModItems.CLAY_WATER_BUCKET','ModItems.CLAY_MILK_BUCKET','new ItemStack(ModItems.CLAY_BUCKET)','player.getInventory().add(empty)'):
	if expected not in grinder32: fail('v32 spent clay vessel return missing: '+expected)
if 'target instanceof Cow cow' not in read('src/main/java/dev/projectflint/item/ClayBucketItem.java') or 'ModItems.CLAY_MILK_BUCKET' not in read('src/main/java/dev/projectflint/item/ClayBucketItem.java'):
	fail('v32 clay bucket must milk adult cows')
books32=read('src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java')
stove32=read('src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java')
for key in ('fat','bread','sushi_roll','cheese'):
	for source in (books32,stove32):
		if f'projectflint:{key}' not in source: fail('v32 parchment/oven recipe missing: '+key)
for expected in ('new ItemStack(ModItems.FAT, 5)','req(WoodenGrinderRecipeRegistry.rawMeat(), 3)','new ItemStack(Items.BREAD)','req(Ingredient.of(ModItems.DOUGH), 2)','new ItemStack(ModItems.SUSHI_ROLL, 5)','req(Ingredient.of(ModItems.FAT), 1)'):
	if expected not in stove32:fail('v32 food recipe count missing: '+expected)
processing32=read('src/main/java/dev/projectflint/system/PrimitiveProcessingRegistry.java')
for expected in ('"fireclay_brick", ModItems.UNFIRED_FIRECLAY_BRICK','"cooked_turtle_meat", ModItems.RAW_TURTLE_MEAT'):
	if expected not in processing32: fail('v32 primitive heat recipe missing: '+expected)
exclusive32=read('src/main/java/dev/projectflint/recipe/BrickFurnaceRecipeRegistry.java')
brick_entity32=read('src/main/java/dev/projectflint/block/entity/BrickFurnaceBlockEntity.java')
for expected in ('Ingredient.of(Items.LEATHER)','ModItems.TANNED_LEATHER'):
	if expected not in exclusive32:fail('v32 furnace-exclusive tanning recipe missing: '+expected)
if 'if (!isBlastFurnace() && input.is(Items.LEATHER))' not in brick_entity32:
	fail('v32 tanning must exclude blast furnace')
raw32=json.loads(read('src/main/resources/data/projectflint/tags/item/raw_meats.json') or '{}').get('values',[])
if 'projectflint:raw_turtle_meat' not in raw32 or 'projectflint:wooden_cup_raw_milk' not in raw32:fail('v32 turtle raw-meat tag or raw-milk sickness regression')
if 'entity instanceof Turtle' not in read('src/main/java/dev/projectflint/system/PrimitiveAnimalDropSystem.java'):fail('v32 turtle meat drop missing')
for name in ('fireclay_block_from_bricks','fireclay_slab_from_block','fireclay_stairs_from_block','fireclay_block_from_slabs'):
	if not (ROOT/'src/main/resources/data/projectflint/recipe'/f'{name}.json').exists():fail('v32 fireclay clay-grinder recipe missing: '+name)
for name in ('unfired_fireclay_brick','raw_turtle_meat'):
	for mode in ('smelting','blasting','smoking','campfire_cooking'):
		path=ROOT/'src/main/resources/data/projectflint/recipe'/f'{name}_{mode}.json'
		if not path.exists():fail(f'v32 cooking recipe missing: {name}_{mode}')
		else:
			r=json.loads(path.read_text())
			if r.get('type')!='minecraft:'+mode:fail(f'v32 cooking recipe wrong serializer: {name}_{mode}')
for name in ('fireclay_brick_block','fireclay_brick_slab','fireclay_brick_stairs'):
	for folder in ('assets/projectflint/blockstates','assets/projectflint/models/item','assets/projectflint/items','data/projectflint/loot_table/blocks'):
		if not (RESOURCES/folder/(name+'.json')).exists():fail('v32 block resource missing: '+folder+'/'+name)
for name in ('flour','fat','raw_turtle_meat','cooked_turtle_meat','sushi_roll','cheese','unfired_fireclay_brick','fireclay_brick','tanned_leather','clay_milk_bucket'):
	for folder in ('assets/projectflint/textures/item','assets/projectflint/models/item','assets/projectflint/items'):
		ext='.png' if 'textures' in folder else '.json'
		if not (RESOURCES/folder/(name+ext)).exists():fail('v32 item resource missing: '+name+ext)
for png in ('flour','fat','turtle','turtle_cooked','sushi_roll','fireclay_brick','unfired_fireclay_brick','fireclay_brick_block','cheese'):
	typ='block' if png=='fireclay_brick_block' else 'item';p=RESOURCES/'assets/projectflint/textures'/typ/(png+'.png')
	# Turtle uploads are used under raw/cooked item names, rather than as unused sprite aliases.
	if png in ('turtle','turtle_cooked'):
		p=RESOURCES/'assets/projectflint/textures/item'/('raw_turtle_meat.png' if png=='turtle' else 'cooked_turtle_meat.png')
	for record in json.loads(read('docs/texture_provenance.json') or '[]'):
		if record.get('source_path')==png+'.png' and record.get('source_archive')=='Direct user upload: v0.31.32-b':
			if p.exists() and hashlib.sha256(p.read_bytes()).hexdigest()!=record['sha256']:fail('v32 supplied sprite altered: '+png)
			break
	else:fail('v32 new sprite provenance missing: '+png)
world32=read('src/main/java/dev/projectflint/system/PrimitiveWorldRulesSystem.java')
for expected in ('EntityType.ZOMBIE_HORSE','entity.discard()'):
	if expected not in world32:fail('v32 surface zombie horse cleanup missing: '+expected)
night32=read('src/main/java/dev/projectflint/system/NightWolfPackSystem.java')
for expected in ('day < 5L','NIGHTLY_CHANCE = 0.05F','3 + level.random.nextInt(3)','30 + level.random.nextInt(16)','EntityType.WOLF.create','LAST_NIGHT_CHECKED.put(uuid, day)'):
	if expected not in night32:fail('v32 wolf pack behavior missing: '+expected)
if 'NightWolfPackSystem.initialize();' not in read('src/main/java/dev/projectflint/ProjectFlint.java'):fail('v32 wolf system not registered')
for screen,key in (('PrimitiveStoveScreen.java','fuel_hint'),('WellPulleyScreen.java','rope_hint')):
	body=read('src/main/java/dev/projectflint/client/screen/'+screen)
	if f'gui.projectflint.{key}' not in body or 'setTooltipForNextFrame' not in body:fail('v32 missing GUI hint: '+screen)
brick_fuel_hint=read('src/main/java/dev/projectflint/client/screen/BrickFurnaceScreen.java')
if 'gui.projectflint.brick_furnace.fuel_time' not in brick_fuel_hint or 'setComponentTooltipForNextFrame' not in brick_fuel_hint:fail('v37 Brick Furnace charcoal-only fuel tooltip missing')
meta32=json.loads(read('src/main/resources/fabric.mod.json') or '{}')
if 'emi' in meta32.get('entrypoints',{}) or meta32.get('depends',{}).get('emi')!='*':fail('v32 PF-EMI external boundary regression')

# v0.31.33-b: parchment-backed dough, bread family, revised protein ratios, three prototype stations.
for id in ('dough','sweet_dough','rustic_bread','sweet_bread','meat_bread','pumpkin_bread','apple_bread','cheese_bread','egg_bread','breakfast_bread','flatbread','french_bread'):
 for folder in ('assets/projectflint/items','assets/projectflint/models/item','assets/projectflint/textures/item'):
  ext='.png' if 'textures' in folder else '.json'
  if not (RESOURCES/folder/(id+ext)).exists():fail('v33 item asset missing: '+id+' '+folder)
 if '"projectflint:'+id+'"' not in stove32 and id not in ('dough','sweet_dough'):
  fail('v33 oven recipe missing: '+id)
 if 'projectflint:'+id not in books32:fail('v33 parchment unlock missing: '+id)
for id in ('basin','drying_rack','millstone'):
 for folder in ('assets/projectflint/items','assets/projectflint/blockstates','assets/projectflint/models/block','assets/projectflint/models/item','data/projectflint/loot_table/blocks'):
  if not (RESOURCES/folder/(id+'.json')).exists():fail('v33 workstation asset missing: '+id+' '+folder)
 if f'registerPrototypeStation("{id}")' not in blocks32:fail('v33 prototype station not registered: '+id)
 if f'output.accept(ModItems.{id.upper()});' not in tabs32:fail('v33 prototype missing creative tab: '+id)
for snippet in ('new ItemStack(ModItems.SUSHI_ROLL, 5)','req(Ingredient.of(Items.SUGAR), 1)','req(WoodenGrinderRecipeRegistry.rawMeat(), 3)','new ItemStack(ModItems.SCRAMBLED_EGG, 3)'):
 if snippet not in stove32:fail('v33 oven balancing regression: '+snippet)
if 'any 3 raw meat' not in read('src/main/java/dev/projectflint/item/RecipeParchmentItem.java'):fail('v33 condensed fat parchment missing')
# The Bacon and Eggs dish pays for the research once; each Oven batch then requires only one Egg.
if 'parchment(Ingredient.of(ModItems.BACON_AND_EGGS), 1, "projectflint:scrambled_egg_oven")' not in books32:
 fail('v33 efficient Egg parchment research cost regression')
if 'recipe("projectflint:scrambled_egg_oven", new ItemStack(ModItems.SCRAMBLED_EGG, 3),\n\t\t\treq(Ingredient.of(Items.EGG), 1))' not in stove32:
 fail('v33 direct Oven Egg ratio regression')
# Compare all newly supplied texture bytes to their recorded source checksums.
for record in json.loads(read('docs/texture_provenance.json') or '[]'):
 if record.get('source_archive') not in ('Direct user upload: v0.31.33-b','new workstations.zip (user upload)'):continue
 target=RESOURCES/record['target']
 if not target.exists(): fail('v33 supplied texture missing: '+record['target'])
 elif hashlib.sha256(target.read_bytes()).hexdigest()!=record['sha256']:
  fail('v33 supplied texture changed: '+record['target'])

guide33=read('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java')
for snippet in ('case BRICK_FURNACE -> brickFurnaceRecipes()','BrickFurnaceRecipeRegistry.TANNED_LEATHER','PrimitiveStoveRecipeRegistry.parchmentRecipes()'):
 if snippet not in guide33:fail('v33 guide missing: '+snippet)
for rel in ('src/main/java/dev/projectflint/compat/emi/ProjectFlintEmiPlugin.java','libs/PF-EMI-1.21.11.jar'):
 if (ROOT/rel).exists():fail('v33 PF-EMI integration must remain external: '+rel)


# v0.31.34-b: user-requested Nest/Bucket/Pulley and Creative-only forge progression.
wood34=read('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java')
for check in (
 'Ingredient.of(ModItems.PLANT_FIBER), 16, Ingredient.of(Items.STRING), 2, new ItemStack(ModItems.NEST)',
 'recipe(STRIPPED_WOOD, 16, Ingredient.of(ModItems.ROPE), 1, new ItemStack(ModItems.WELL_PULLEY))',
 'Items.STRIPPED_PALE_OAK_WOOD', 'Items.STRIPPED_CRIMSON_STEM',
):
 if check not in wood34:fail('v34 missing nest/pulley recipe: '+check)
items34=read('src/main/java/dev/projectflint/registry/ModItems.java')
if 'CLAY_MILK_BUCKET = register("clay_milk_bucket", properties -> new Item(properties.stacksTo(1)))' not in items34:
 fail('v34 Clay Milk Bucket must no longer have food properties or a food-use conversion')
lang34=json.loads(read('src/main/resources/assets/projectflint/lang/en_us.json') or '{}')
for key,value in {'item.projectflint.clay_milk_bucket':'Clay Bucket (Milk)','item.projectflint.clay_water_bucket':'Clay Bucket (Water)'}.items():
 if lang34.get(key)!=value:fail('v34 inconsistent clay-bucket label: '+key)
for id in ('basin','drying_rack','millstone'):
 path='src/main/resources/assets/projectflint/models/block/'+id+'.json'
 shape=json.loads(read(path) or '{}')
 if not shape.get('elements') or shape.get('parent')=='minecraft:block/cube_all':fail('v34 workstation must use element geometry: '+id)
 states=json.loads(read('src/main/resources/assets/projectflint/blockstates/'+id+'.json') or '{}')
 if len(states.get('variants',{})) != (24 if id=='basin' else 4):fail('v34 workstation facing/content variants missing: '+id)
station=read('src/main/java/dev/projectflint/block/PrototypeStationBlock.java')
for check in ('extends HorizontalDirectionalBlock','FACING','getStateForPlacement','codec()'):
 if check not in station:fail('v34 station orientation/codec regression: '+check)
# Preserve all three original user-supplied atlas textures exactly via the v33 provenance hashes.
provenance34=json.loads(read('docs/texture_provenance.json') or '[]')
for name in ('basin_stitched.png','drying_rack_stitched.png','millstone_stitched.png'):
 key='assets/projectflint/textures/block/'+name
 matches=[row for row in provenance34 if row.get('target')==key]
 tex=RESOURCES/key
 if not matches or not tex.is_file() or hashlib.sha256(tex.read_bytes()).hexdigest()!=matches[0]['sha256']:
  fail('v34 original user workstation atlas altered: '+name)
for name in ('well_pulley_top','well_pulley_side','well_pulley_bottom','fireclay_forge_controller_off','fireclay_forge_controller_ready','fireclay_forge_controller_lit'):
 path=RESOURCES/'assets/projectflint/textures/block'/(name+'.png')
 key='assets/projectflint/textures/block/'+name+'.png'
 matches=[row for row in provenance34 if row.get('target')==key and row.get('byte_identical') is False]
 if not matches or not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest()!=matches[0].get('sha256'):
  fail('v34 composed pulley/forge sprite missing or undocumented: '+name)
for name in ('well_pulley','fireclay_forge_controller'):
 for stem in ('models/block','models/item','items','blockstates'):
  if not (RESOURCES/'assets/projectflint'/stem/(name+'.json')).exists() and not (name=='fireclay_forge_controller' and stem=='models/block'):
   fail('v34 pulley or controller model registration missing: '+stem+'/'+name)
for suffix in ('off','ready','lit'):
 if not (RESOURCES/'assets/projectflint/models/block'/('fireclay_forge_controller_'+suffix+'.json')).exists():fail('v34 missing controller state model: '+suffix)
block34=read('src/main/java/dev/projectflint/block/FireclayForgeControllerBlock.java')
entity34=read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
multiblock34=read('src/main/java/dev/projectflint/system/FireclayForgeMultiblock.java')
menu34=read('src/main/java/dev/projectflint/menu/FireclayForgeMenu.java')
screen34=read('src/main/java/dev/projectflint/client/screen/FireclayForgeScreen.java')
for needle in ('FORMED','LIT','getTicker','animateTick','ParticleTypes.CAMPFIRE_COSY_SMOKE'):
 if needle not in block34:fail('v34 controller formation/effects hook missing: '+needle)
for needle in ('FireclayForgeMultiblock.validate','FUEL_SLOT','WOOD_BURN_TICKS','CHARCOAL_BURN_TICKS','WOOD_BYPRODUCT_TICKS','ItemTags.LOGS_THAT_BURN','isFirestarter','setDamageValue','ContainerHelper.saveAllItems','ContainerHelper.loadAllItems','level.playSound','FURNACE_FIRE_CRACKLE','ignite();'):
 if needle not in entity34:fail('v34 forge persistence/reignite/outputs missing: '+needle)
for needle in ('y == 0', 'FIRECLAY_BRICK_STAIRS', 'FIRECLAY_BRICK_SLAB', 'SlabType.DOUBLE', 'innerAir', 'FIRECLAY_FORGE_CONTROLLER'):
 if needle not in multiblock34:fail('v34 shareable shell validator missing: '+needle)
if 'clickMenuButton' not in menu34 or 'handleInventoryButtonClick' not in screen34:fail('v34 firestarter ignition button not wired through authoritative menu')
for path, needle in (
 ('src/main/java/dev/projectflint/registry/ModBlocks.java','FIRECLAY_FORGE_CONTROLLER = registerForge'),
 ('src/main/java/dev/projectflint/registry/ModBlockEntities.java','FireclayForgeBlockEntity::new'),
 ('src/main/java/dev/projectflint/registry/ModMenus.java','FireclayForgeMenu::new'),
 ('src/main/java/dev/projectflint/registry/ModItems.java','registerBlockItem("fireclay_forge_controller"'),
 ('src/main/java/dev/projectflint/client/ProjectFlintClient.java','MenuScreens.register(ModMenus.FIRECLAY_FORGE, FireclayForgeScreen::new)'),
 ('src/main/java/dev/projectflint/registry/ModCreativeTabs.java','output.accept(ModItems.FIRECLAY_FORGE_CONTROLLER)'),
):
 if needle not in read(path):fail('v34 forge registry/screen missing: '+path)
if any('fireclay_forge_controller' in path.name for path in (RESOURCES/'data/projectflint/recipe').glob('*.json')):
 fail('v34 forge controller remains wooden-grinder crafted, not a vanilla crafting-table recipe')
if (ROOT/'APPLY_PROJECTFLINT_0_31_34_B.bat').exists() or 'APPLY_PROJECTFLINT_0_31_34_B.bat' not in cleanup_bat:
 fail('v35 cleanup must retire old v34 cleanup script')
if json.loads(read('src/main/resources/fabric.mod.json') or '{}').get('depends',{}).get('emi')!='*':
 fail('v34 external PF-EMI dependency must remain unchanged')
if (ROOT/'src/main/java/dev/projectflint/compat/emi/ProjectFlintEmiPlugin.java').exists():fail('v34 do not restore a ProjectFlint EMI plugin')


# v0.31.35-b acceptance: exact visible forge geometry, reusable fuel/recipe separation,
# short workstation hitboxes, marquee clipping, and mandatory guide integration.
forge35 = read('src/main/java/dev/projectflint/system/FireclayForgeMultiblock.java')
for expected in ('y <= 5', 'y == 2 && corner', 'y >= 2 && y <= 4 && cardinal', 'y == 5 && cardinal', 'SlabType.DOUBLE', 'FIRECLAY_BRICK_STAIRS', 'FIRECLAY_BRICK_SLAB', 'centre.above(6)'):
 if expected not in forge35:fail('v35 structural layer check missing: '+expected)
for outdated in ('centre.relative(back).above(3)', 'single rear-centre brick'):
 if outdated in forge35:fail('v35 obsolete chimney rule: '+outdated)
entity35=read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
for expected in ('INPUT_SLOT = 0','FUEL_SLOT = 1','IGNITER_SLOT = 2','OUTPUT_SLOT = 3','LEGACY_FUEL_SLOT = 4','WOOD_BURN_TICKS = 60 * 20','CHARCOAL_BURN_TICKS = 5 * 60 * 20','WOOD_BYPRODUCT_TICKS = 15 * 20','woodByproductTicks','canOutputCharcoal()', 'burnTicks--', 'fuel.shrink(nextWood ? WOOD_FUEL_COST : 1)', 'ignite();','ForgeLayoutVersion', 'ContainerHelper.saveAllItems', 'ContainerHelper.loadAllItems'):
 if expected not in entity35:fail('v35 fuel/byproduct/persistence missing: '+expected)
if 'PENDING_INPUT_SLOT' in entity35 or 'INPUT_PER_BATCH' in entity35:fail('v35 obsolete exclusive charcoal input/process remains')
menu35=read('src/main/java/dev/projectflint/menu/FireclayForgeMenu.java')
for expected in ('VISIBLE_SLOTS = 5','SimpleContainerData(5)','FUEL_SLOT','IGNITER_SLOT','OUTPUT_SLOT','isFuel(stack)','burnPixels'):
 if expected not in menu35:fail('v35 forge menu incomplete: '+expected)
screen35=read('src/main/java/dev/projectflint/client/screen/FireclayForgeScreen.java')
for expected in ('RecipeGuideButton', 'Station.FIRECLAY_FORGE','gui.projectflint.forge.fuel_wood','gui.projectflint.forge.materials','burnPixels'):
 if expected not in screen35:fail('v35 forge guide/fuel screen missing: '+expected)
if '4 overworld logs or wood per batch' in screen35 or '"W"' in screen35:fail('v35 obsolete wood input hint remains')
station35=read('src/main/java/dev/projectflint/block/PrototypeStationBlock.java')
for expected in ('BASIN_SHAPE', 'RACK_SHAPE', 'MILLSTONE_SHAPE', 'getShape(', 'getCollisionShape(', 'Block.box(2, 0, 2, 14, 8, 14)', 'Block.box(1, 0, 1, 15, 8, 15)', 'Block.box(2, 0, 2, 14, 10, 14)'):
 if expected not in station35:fail('v35 low-station shape incomplete: '+expected)
guide35=read('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java')
for expected in ('FIRECLAY_FORGE','case FIRECLAY_FORGE -> fireclayForgeRecipes()','fireclayForgeRecipes()', 'graphics.enableScissor(', 'graphics.disableScissor()', 'System.currentTimeMillis()', 'font.width(name)', 'if (inside(mouseX, mouseY, labelX'):
 if expected not in guide35:fail('v35 forge-guide/marquee missing: '+expected)
for station_name in ('BAKED_KILN','MORTAR_KILN','CLAY_GRINDER','WOODEN_GRINDER','BOOKSHELF','SANDING_STATION','ADVANCED_KILN','PRIMITIVE_OVEN','BRICK_FURNACE','FIRECLAY_FORGE'):
 if station_name+'(Component.literal' not in guide35:fail('v35 a gameplay station is missing in guide: '+station_name)
if not (ROOT/'docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md').exists():fail('v35 permanent recipe-guide release checklist missing')
if 'APPLY_PROJECTFLINT_0_31_34_B.bat' not in cleanup_bat or (ROOT/'APPLY_PROJECTFLINT_0_31_34_B.bat').exists():
 fail('v35 cleanup must retire v34 apply BAT')
if not (ROOT/'docs/releases/0.31.35-b.md').exists():fail('v35 release notes missing')


# v0.31.36-b: tanned progression and vest-gated 2x2, retained external PF-EMI.
items36=read('src/main/java/dev/projectflint/registry/ModItems.java')
inv36=read('src/main/java/dev/projectflint/system/PrimitiveInventorySystem.java')
vest36=read('src/main/java/dev/projectflint/system/TannedUtilityVestSystem.java')
wood36=read('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java')
clay36=read('src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java')
stove36=read('src/main/java/dev/projectflint/block/entity/BrickFurnaceBlockEntity.java')
for name in ('TANNED_LEATHER_HELMET','TANNED_LEATHER_CHESTPLATE','TANNED_LEATHER_LEGGINGS','TANNED_LEATHER_BOOTS','TANNED_UTILITY_VEST'):
 if name not in items36:fail('v36 missing item: '+name)
for needle in ('getMaxDamage() + 96', ' + 0.5D', 'TANNED_ARMOR_ASSET','TANNED_UTILITY_ASSET','4.5D','No durability component'):
 if needle not in items36:fail('v36 armor/vest component missing: '+needle)
for kind,n in (('helmet',5),('chestplate',8),('leggings',7),('boots',4)):
 name='tanned_leather_'+kind
 try:
  o=json.loads(read('src/main/resources/data/projectflint/recipe/'+name+'.json'))
  if o.get('type')!='projectflint:clay_grinding' or o.get('input_count')!=n or o.get('result',{}).get('id')!='projectflint:'+name:fail('v36 incorrect Clay Grinder armor recipe: '+name)
 except (ValueError,AttributeError):fail('v36 invalid armor recipe: '+name)
 if '"projectflint:tanned_leather", '+str(n)+', "projectflint:'+name+'"' not in clay36:fail('v36 guide omits armor recipe: '+name)
 for folder in ('assets/projectflint/items','assets/projectflint/models/item','assets/projectflint/textures/item'):
  ext='.png' if 'textures' in folder else '.json'
  if not (RESOURCES/folder/(name+ext)).exists():fail('v36 missing armor asset: '+name+' '+folder)
for f in ('tanned_leather','tanned_utility_vest'):
 if not (RESOURCES/'assets/projectflint/equipment'/(f+'.json')).exists():fail('v36 equipment profile missing: '+f)
 for folder in ('humanoid','humanoid_leggings'):
  if not (RESOURCES/'assets/projectflint/textures/entity/equipment'/folder/(f+'.png')).exists():fail('v36 worn equipment sprite missing: '+f+' '+folder)
if 'TANNED_LEATHER_CHESTPLATE' not in wood36 or 'LEATHER_BACKPACK_WITH_CHEST' not in wood36 or 'TANNED_UTILITY_VEST' not in wood36:fail('v36 Wooden Grinder vest assembly missing')
if 'WOODEN_BEAM' not in wood36 or 'FIRECLAY_BRICK' not in wood36 or 'FIRECLAY_FORGE_CONTROLLER' not in wood36:fail('v36 Forge controller not Survival-craftable')
try:
 vestrecipe=json.loads(read('src/main/resources/data/projectflint/recipe/tanned_utility_vest.json'))
 if vestrecipe.get('type')!='minecraft:crafting_shapeless' or len(vestrecipe.get('ingredients',[]))!=2:fail('v36 vest 2x2 crafting recipe incorrect')
except (ValueError,AttributeError):fail('v36 invalid vest crafting JSON')
for needle in ('TANNED_UTILITY_VEST_ALLOWED_SLOTS = 22','canUsePersonalCrafting(player)','awardRecipes(', 'WeakReference<Player>', 'isPersonalCraftingLocked'):
 if needle not in inv36:fail('v36 inventory/recipe-book milestone missing: '+needle)
for needle in ('BlockTags.LEAVES','Blocks.SUGAR_CANE','Blocks.LILY_PAD','ModBlocks.SLEEPING_BAG'):
 if needle not in vest36:fail('v36 vegetation privilege missing: '+needle)
if 'recoverIntact || tool.is(ModItems.ADVANCED_FLINT_MULTITOOL)' not in read('src/main/java/dev/projectflint/block/SleepingBagBlock.java'):fail('v36 intact bedroll recovery missing')
if 'TannedUtilityVestSystem.canHandClear(player, state)' not in read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java'):fail('v36 vest hand speed missing')
recipe_filter36=read('src/main/java/dev/projectflint/mixin/RecipeManagerMixin.java')
if 'instanceof CraftingRecipe' not in recipe_filter36 or 'getNamespace().equals("minecraft")' not in recipe_filter36 or 'RecipeMap.create' not in recipe_filter36 or 'RecipeManagerMixin' not in active_mixins:fail('v36 vanilla crafting-only filter missing')
for needle in ('FUEL_CHARCOAL_PER_IGNITION = 1','BURN_TICKS = 10 * 60 * 20','fuel.is(Items.CHARCOAL)','Math.min(BURN_TICKS'):
 if needle not in stove36:fail('v36 charcoal-only 10-min fuel missing: '+needle)
if 'Items.CHARCOAL' not in read('src/main/java/dev/projectflint/menu/BrickFurnaceMenu.java'):fail('v36 furnace menu still accepts other fuel')
if not (ROOT/'APPLY_PROJECTFLINT_0_31_48_B.bat').exists():fail('v44 cleanup BAT lineage missing')
if 'APPLY_PROJECTFLINT_0_31_36_B.bat' not in cleanup_bat:fail('v37 must retire prior apply BAT')
if (ROOT/'src/main/java/dev/projectflint/compat/emi/ProjectFlintEmiPlugin.java').exists():fail('v36 must not restore PF-EMI integration in core')

# v36 follow-up acceptance: real guide entries only, two independent outputs, correct menu sync.
forge36=read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
for expected in ('PRIMARY_OUTPUT_SLOT = 5', 'SLOT_COUNT = 6', 'ForgeLayoutVersion", 3', 'items.set(OUTPUT_SLOT, old.get(2))'):
 if expected not in forge36:fail('v36 independent forge output/save migration missing: '+expected)
for expected in ('PRIMARY_OUTPUT_SLOT', 'OUTPUT_SLOT, 158, 34', 'VISIBLE_SLOTS = 5'):
 if expected not in menu35:fail('v36 independent output GUI slot missing: '+expected)
for expected in ('gui.projectflint.forge.output', 'gui.projectflint.forge.charcoal_output', 'remainingSeconds()'):
 if expected not in screen35:fail('v36 forge display/output tooltip missing: '+expected)
for expected in ('BURN_SYNC_DIVISOR = 2', '(burnTicks + BURN_SYNC_DIVISOR - 1) / BURN_SYNC_DIVISOR'):
 if expected not in read('src/main/java/dev/projectflint/block/entity/PrimitiveStoveBlockEntity.java'):fail('v36 oven timer still overflows signed short: '+expected)
for file_name in ('PrimitiveStoveMenu.java','BrickFurnaceMenu.java'):
 if 'remainingFuelSeconds()' not in read('src/main/java/dev/projectflint/menu/'+file_name):fail('v36 missing fuel countdown: '+file_name)
for file_name in ('PrimitiveStoveScreen.java','BrickFurnaceScreen.java'):
 if 'remainingFuelSeconds()' not in read('src/main/java/dev/projectflint/client/screen/'+file_name):fail('v36 fuel timer not displayed: '+file_name)
if 'new GuideRecipe(List.of(new ItemStack(Items.CHARCOAL)), new ItemStack(ModItems.BRICK_FURNACE))' in guide35:fail('v36 fake charcoal-to-furnace recipe remains')
if 'new ItemStack(Items.OAK_LOG), new ItemStack(ModItems.FLINT_FIRESTARTER)' in guide35:fail('v36 old fuel pseudo-recipe remains')


# v0.31.37-b: four logs per 60-second ignition, four charcoal output, player-facing multiline hints.
forge37=read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
for expected in ('WOOD_FUEL_COST = 4', 'fuel.getCount() < WOOD_FUEL_COST', 'fuel.shrink(nextWood ? WOOD_FUEL_COST : 1)', 'WOOD_BURN_TICKS = 60 * 20', 'WOOD_BYPRODUCT_TICKS = 15 * 20', 'fuel.shrink(nextWood ? WOOD_FUEL_COST : 1)'):
 if expected not in forge37:fail('v37 correct 4-log wood fuel consumption missing: '+expected)
if 'fuel.shrink(1);' in forge37:fail('v37 old single-log fuel shrink remains')
for forbidden in ('future forge recipes','Recipe input (future', 'Recipe output (separate', 'Charcoal fuel byproduct', 'Fuel: wood/log = 60s'):
 if forbidden in screen35:fail('v37 developer-facing forge tooltip remains: '+forbidden)
for expected in ('setComponentTooltipForNextFrame', 'List.of(', 'gui.projectflint.forge.fuel_wood','gui.projectflint.forge.output'):
 if expected not in screen35:fail('v37 multilined translated forge hint missing: '+expected)
furnace37=read('src/main/java/dev/projectflint/client/screen/BrickFurnaceScreen.java')
if 'setComponentTooltipForNextFrame' not in furnace37 or 'gui.projectflint.brick_furnace.fuel_time' not in furnace37:fail('v37 brick furnace two-line hint missing')
locale37=json.loads(read('src/main/resources/assets/projectflint/lang/en_us.json'))
for key in ('gui.projectflint.forge.materials', 'gui.projectflint.forge.fuel', 'gui.projectflint.forge.fuel_wood', 'gui.projectflint.forge.fuel_charcoal', 'gui.projectflint.forge.firestarter', 'gui.projectflint.forge.firestarter_use','gui.projectflint.forge.output', 'gui.projectflint.forge.charcoal_output', 'gui.projectflint.brick_furnace.fuel_time'):
 if not locale37.get(key):fail('v37 missing player-facing translation: '+key)


# v0.31.37-b build hotfix: Minecraft 1.21.11 ResourceKey uses identifier(),
# and Tanned Leather references require an explicit vanilla Items import.
recipe_key_mixin = read("src/main/java/dev/projectflint/mixin/RecipeManagerMixin.java")
personal_crafting = read("src/main/java/dev/projectflint/system/PrimitiveInventorySystem.java")
armor_items = read("src/main/java/dev/projectflint/registry/ModItems.java")
for name, source in (("RecipeManagerMixin", recipe_key_mixin), ("PrimitiveInventorySystem", personal_crafting)):
	if "holder.id().identifier().getNamespace()" not in source or "holder.id().location()" in source:
		fail(f"{name} must use ResourceKey.identifier() in Minecraft 1.21.11")
if "import net.minecraft.world.item.Items;" not in armor_items:
	fail("Tanned Leather registration requires the net.minecraft.world.item.Items import")


# v0.31.38-b: woodworking, leather crafts, new torch and guide coverage.
for needed in ('leather_sheet_from_tanned','leather_straps_from_sheet','leather_repair_kit','wooden_pickaxe','wooden_axe','wooden_shovel','basin_crafting','fat_burning_torch','dirt_piles_from_loose_soil'):
 if not (RESOURCES/'data/projectflint/recipe'/(needed+'.json')).exists():fail('v38 missing gameplay craft: '+needed)
for needed in ('tool_belt','leather_sheet','leather_strap','repair_kit','tannin'):
 for directory in ('textures/item','models/item','items'):
  extension='.png' if directory=='textures/item' else '.json'
  if not (RESOURCES/'assets/projectflint'/directory/(needed+extension)).exists():fail('v38 item asset missing: '+directory+'/'+needed)
if (RESOURCES/'data/projectflint/recipe/sand_to_glass_brick_furnace.json').exists():fail('v38 glass must be Brick Furnace exclusive, not vanilla smelting')
for file, needle in (
 ('src/main/java/dev/projectflint/recipe/BrickFurnaceRecipeRegistry.java','GLASS'),
 ('src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java','repair_leather_armor'),
 ('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java','PERSONAL_CRAFTING'),
 ('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java','case BASIN'),
 ('src/main/java/dev/projectflint/block/FatBurningTorchBlock.java','class FatBurningTorchBlock'),
 ('src/main/java/dev/projectflint/block/entity/FatBurningTorchBlockEntity.java','BURN_TIME = 30 * 60 * 20'),
 ('src/main/java/dev/projectflint/recipe/DurableCraftingRecipe.java','getRemainingItems'),
 ('src/main/java/dev/projectflint/recipe/LeatherRepairRecipe.java','getSerializer'),
 ('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java','projectflint$loosenAdjacentStone'),
 ('src/main/java/dev/projectflint/registry/ModItems.java','TOOL_BELT'),
 ('src/main/java/dev/projectflint/mixin/ItemTooltipMixin.java','Unlocks 22 inventory slots.'),
):
 if needle not in read(file):fail('v38 expected source integration missing: '+file+' '+needle)
if any('dev.emi.emi' in x.read_text() for x in (ROOT/'src/main/java').rglob('*.java')):fail('v38 must keep PF-EMI external')

# v0.31.39-b: guide alternatives, persisted Basin, wood tools, Vest and masonry.
guide39 = read('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java')
for forbidden in ('Fuel: 1 Charcoal burns for 10 minutes.', 'Wood: 60s, 4 charcoal.', 'Charcoal: 5m, no output.', 'Each ignition consumes 1 Firestarter durability.'):
 if forbidden in guide39:fail('v39 guide contains non-recipe instructional text: '+forbidden)
for needed in ('condense(switch (station)', 'variants', 'Accepted inputs', 'System.currentTimeMillis()', 'setComponentTooltipForNextFrame', 'case BASIN -> basinRecipes()', 'acceptedForgeWood()'):
 if needed not in guide39:fail('v39 guide alternatives or station coverage missing: '+needed)
grinder39=read('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java')
if 'Ingredient.of(ModItems.WOODEN_SHEET), 32, Ingredient.of(ModItems.FIRECLAY_BRICK_BLOCK), 32, new ItemStack(ModItems.FIRECLAY_FORGE_CONTROLLER)' not in grinder39:fail('v39 forge controller recipe must require 32 wooden sheets and 32 fireclay blocks')
basin39=read('src/main/java/dev/projectflint/block/BasinBlock.java')
entity39=read('src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java')
for needed in ('fallOn(', 'CONTENTS','useItemOn(', 'useWithoutItem(', 'getCollisionShape('):
 if needed not in basin39:fail('v39 Basin interaction/visual missing: '+needed)
for needed in ('inputData.read("BasinInput"', 'output.store("BasinInput"', 'setFoodLevel(player.getFoodData().getFoodLevel() - 1)', 'new ItemStack(ModItems.TANNIN, 2)', 'refreshVisual()'):
 if needed not in entity39:fail('v39 Basin save/process missing: '+needed)
for suffix in ('','_oak_log','_oak_wood','_birch_log','_birch_wood'):
 if not (RESOURCES/'assets/projectflint/models/block'/('basin'+suffix+'.json')).is_file():fail('v39 Basin visible content model missing: '+suffix)
basebasin=json.loads(read('src/main/resources/assets/projectflint/models/block/basin.json') or '{}')
if basebasin.get('textures',{}).get('wood')!='minecraft:block/oak_planks' or basebasin.get('textures',{}).get('stone')!='minecraft:block/cobblestone':fail('v39 Basin must use Oak Planks + Cobblestone')
if len(json.loads(read('src/main/resources/assets/projectflint/blockstates/basin.json') or '{}').get('variants',{}))!=24:fail('v39 Basin must display all four wood contents for each facing, plus empty')
if 'new BasinBlock(' not in read('src/main/java/dev/projectflint/registry/ModBlocks.java') or 'BasinBlockEntity::new' not in read('src/main/java/dev/projectflint/registry/ModBlockEntities.java'):fail('v39 Basin entity/block registration incomplete')
if 'BlockItemPlacedLedgerMixin' not in active_mixins:fail('v39 player placements must be recorded persistently')
ledger39=read('src/main/java/dev/projectflint/system/PlayerPlacedBlockLedger.java')
if 'SavedDataType' not in ledger39 or 'ledger.setDirty()' not in ledger39 or 'recoverable(' not in ledger39:fail('v39 placement ledger is not persistent')
for sourcefile in ('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java','src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java','src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java'):
 if 'WoodenToolTargets' not in read(sourcefile):fail('v39 wooden tool mining permissions not unified: '+sourcefile)
server39=read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
for needed in ('Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, ItemStack.EMPTY)', 'level.random.nextFloat() < 0.02F', 'PlayerPlacedBlockLedger.recoverable', 'ModBlocks.FIRECLAY_FORGE_CONTROLLER'):
 if needed not in server39:fail('v39 vest vegetation/recovery or masonry incomplete: '+needed)
axe39=read('src/main/java/dev/projectflint/system/WoodenAxeBalanceSystem.java')
if '3.5D' not in axe39 or '-3.2D' not in axe39 or 'WoodenAxeBalanceSystem.normalize' not in read('src/main/java/dev/projectflint/system/PrimitiveInventorySystem.java'):fail('v39 wooden axe damage modifier not applied')
if '0.31.39-b' not in read('docs/releases/0.31.39-b.md'):fail('v39 release notes missing')

# v0.31.40-b: masonry, wooden Stone timing, and recoverable forage bushes.
wood40=read('src/main/java/dev/projectflint/system/WoodenToolTargets.java')
tool40=read('src/main/java/dev/projectflint/system/ToolCapabilities.java')
progress40=read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java')
attack40=read('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
server40=read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
if 'canMineBrick(ItemStack stack)' not in tool40 or 'stack.is(Items.WOODEN_PICKAXE)' not in tool40: fail('v40 wooden pickaxe masonry capability missing')
for piece in ('Blocks.BRICKS','BRICK_SHINGLES','FIRECLAY_BRICK_BLOCK','FIRECLAY_BRICK_SLAB','FIRECLAY_BRICK_STAIRS','MORTAR_BRICKS','MORTAR_BRICK_SLAB','WATTLED_DAUB_BRICKS','LIMESTONE_BRICKS'):
 if piece not in wood40: fail('v40 wooden pickaxe masonry target missing: '+piece)
for name,source in [('progress',progress40),('permissions',attack40),('server drops',server40)]:
 if 'WoodenToolTargets.brickMasonry(state)' not in source: fail('v40 brick harvesting not wired into '+name)
if progress40.index('WoodenToolTargets.pickaxe(state)')>progress40.index('PlayerPlacedBlockLedger.potential(level, pos, state)'):
 fail('v40 wooden Stone slow timing must precede Vest potential preview')
if '1.0F / 270.0F' not in progress40 or '1.0F / 120.0F' not in progress40:
 fail('v40 stone/masonry explicit slow mining progress missing')
if 'if (state.is(ModTags.FORAGE_BUSHES))' not in progress40 or 'if (state.is(ModTags.FORAGE_BUSHES))' not in server40:
 fail('v40 forage bush fallback missing')
for name,source in [('attack',attack40),('server',server40)]:
 if 'new ItemStack(Items.STICK)' not in server40 or 'if (state.is(ModTags.FORAGE_BUSHES))' not in source:
  fail('v40 forage bush hand pickup not fully wired: '+name)
for name in ('beach_bush','jagged_peaks_bush'):
 if 'projectflint:'+name not in read('src/main/resources/data/projectflint/tags/block/forage_bushes.json'):
  fail('v40 legacy bushes must remain harvestable in existing saves: '+name)
if '0.31.40-b' not in read('docs/releases/0.31.40-b.md'): fail('v40 release notes missing')


# v0.31.42-b: safe Well inventory access and Vest frame salvage.
well_block42 = read('src/main/java/dev/projectflint/block/WellPulleyBlock.java')
well_be42 = read('src/main/java/dev/projectflint/block/entity/WellPulleyBlockEntity.java')
well_ui42 = read('src/main/java/dev/projectflint/client/screen/WellPulleyScreen.java')
vest42 = read('src/main/java/dev/projectflint/system/TannedUtilityVestSystem.java')
ledger42 = read('src/main/java/dev/projectflint/system/PlayerPlacedBlockLedger.java')
permissions42 = read('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
progress42 = read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java')
drops42 = read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
for needle in ('pulley.refreshStructure(serverLevel);', 'player.openMenu(pulley);'):
 if needle not in well_block42: fail('v42 cannot open incomplete Pulley or refresh its status: '+needle)
if 'WellMultiblock.validate(serverLevel, pos).isEmpty()' in well_block42:fail('v42 unfinished Well still blocks GUI access')
for needle in ('public void refreshStructure(ServerLevel level)', 'if (cachedAquifer == null) return;', 'if (pendingClay >= 0 || pendingFlintUnits >= 0)'):
 if needle not in well_be42:fail('v42 well should remain idle when invalid and preserve pending output behavior: '+needle)
if 'Rope boosts' in well_ui42:fail('v42 obsolete Rope boosts caption still shown')
for needle in ('y + 45', 'y + 46', 'y + 51'):
 if needle not in well_ui42:fail('v42 progress meter not moved: '+needle)
if 'canDismantleWellFrame' not in vest42 or 'ModBlocks.isWoodenBeam(state)' not in vest42 or 'ModBlocks.WELL_PULLEY' not in vest42:fail('v42 Vest well-frame permissions missing')
if '!ModBlocks.isWoodenBeam(state)' not in ledger42:fail('v42 beams would lose Wooden Screw via generic Vest loot')
for source,label in ((permissions42,'break permissions'),(progress42,'progress display'),(drops42,'server loot')):
 if 'TannedUtilityVestSystem.canDismantleWellFrame(player, state)' not in source:fail('v42 vest frame rule missing from '+label)
for needle in ('if (state.is(ModBlocks.WELL_PULLEY)) {', 'Containers.dropContents(level, pos, pulley);', 'new ItemStack(ModItems.WELL_PULLEY)', 'new ItemStack(ModItems.WOODEN_SCREW)', 'PlayerPlacedBlockLedger.clear(level, pos);'):
 if needle not in drops42:fail('v42 live Pulley/Beam safe dismantle missing: '+needle)
if drops42.index('if (state.is(ModBlocks.WELL_PULLEY)) {') > drops42.index('if (state.is(ModBlocks.CRUSHED_STONE)) {'):fail('v42 pulley handler must precede generic custom mining')
if '0.31.42-b' not in read('docs/releases/0.31.42-b.md'):fail('v42 release notes missing')





# v0.31.41-b: a retired Creative-only item must not leak into Survival or accelerate mining.
if (RESOURCES/'data/projectflint/recipe/tool_belt.json').exists():fail('v41 Tool Belt crafting recipe must be retired')
if '"tool_belt"' in read('src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java'):fail('v41 Tool Belt must not appear in personal crafting guide')
if 'output.accept(ModItems.TOOL_BELT)' not in read('src/main/java/dev/projectflint/registry/ModCreativeTabs.java'):fail('v41 preserve creative Tool Belt availability')
belt_items = read('src/main/java/dev/projectflint/registry/ModItems.java')
if 'private static Item toolBelt(Item.Properties properties)' not in belt_items or 'return new Item(properties.stacksTo(1));' not in belt_items:fail('v41 Tool Belt must be an inert item')
for forbidden in ('tool_belt_break_speed', 'tool_belt_armor'):
 if forbidden in belt_items:fail('v41 removed Tool Belt attribute remains: '+forbidden)
if 'TOOL_BELT' in read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java'):fail('v41 old belt mining bonus remains')
if 'Mining Speed: +10% while worn.' in read('src/main/java/dev/projectflint/mixin/ItemTooltipMixin.java'):fail('v41 retired Tool Belt tooltip remains')
progress41 = read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java')
attack41 = read('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
drop41 = read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
if 'held.is(net.minecraft.world.item.Items.WOODEN_AXE) || held.is(ModItems.HARDWOOD_AXE)) && state.is(BlockTags.LOGS)' not in progress41:fail('v41 Wooden Axe progress missing')
for needle in ('held.is(Items.WOODEN_AXE) || held.is(ModItems.HARDWOOD_AXE)) && state.is(BlockTags.LOGS)) return InteractionResult.PASS;', 'held.is(Items.WOODEN_AXE) || held.is(ModItems.HARDWOOD_AXE)) && state.is(BlockTags.LOGS)) return true;'):
 if needle not in attack41:fail('v41 Wooden Axe attack/break permission missing')
if 'state.is(ModTags.GRASS_SCRAPE_TARGETS) && held.isEmpty()' not in progress41 or 'state.is(ModTags.GRASS_SCRAPE_TARGETS) && held.isEmpty()' not in drop41:fail('v41 grass scraping must require empty main hand')
if 'state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH) ? new ItemStack(Items.DIRT)' not in drop41:fail('v41 shovel path must return Dirt')
if '1.0F / 40.0F' not in progress41 or 'state.is(BlockTags.LEAVES)' not in progress41:fail('v41 non-instant Vest leaf timing missing')
if '0.31.41-b' not in read('docs/releases/0.31.41-b.md'):fail('v41 release notes missing')


# v0.31.43-b: vegetation recovery, forge-only internal air, darker armor, and staged hardwood assets.
forge43 = read('src/main/java/dev/projectflint/system/FireclayForgeMultiblock.java')
vest43 = read('src/main/java/dev/projectflint/system/TannedUtilityVestSystem.java')
items43 = read('src/main/java/dev/projectflint/registry/ModItems.java')
tab43 = read('src/main/java/dev/projectflint/registry/ModCreativeTabs.java')
if 'centreCell && y >= 1 && !innerAir(level, cell)' not in forge43 or 'return innerAir(level, centre.above(6));' not in forge43:
	fail('v43 forge must require air only through the internal funnel/chimney')
if 'else if (!innerAir(level, cell))' in forge43:
	fail('v43 forge must not require exterior non-structure cells to be air')
for needle in ('state.is(ModTags.FORAGE_BUSHES)', 'state.is(Blocks.SWEET_BERRY_BUSH)', 'state.is(ModBlocks.STRAWBERRY_BUSH)', 'state.is(BlockTags.SAPLINGS)'):
	if needle not in vest43: fail('v43 Utility Vest vegetation coverage missing: '+needle)
for name in ('WOOD_DUST','HARDWOOD','HARDWOOD_ALLOY','HARDWOOD_AXE','HARDWOOD_HAMMER','HARDWOOD_HOE','HARDWOOD_PICKAXE','HARDWOOD_SHOVEL','HARDWOOD_SWORD'):
	if name not in items43 or f'output.accept(ModItems.{name})' not in tab43: fail('v43 Creative-only staged item missing: '+name)
for path in ('wood_dust','hardwood','hardwood_alloy','hardwood_axe','hardwood_hammer','hardwood_hoe','hardwood_pickaxe','hardwood_shovel','hardwood_sword'):
	for rel in (f'assets/projectflint/items/{path}.json', f'assets/projectflint/models/item/{path}.json', f'assets/projectflint/textures/item/{path}.png'):
		if not (RESOURCES/rel).exists(): fail('v43 missing staged asset: '+rel)
	# v45 legitimately activates the stage-43 Creative-only placeholders.
if '0.31.43-b' not in read('docs/releases/0.31.43-b.md'): fail('v43 release notes missing')


# v0.31.44-b: Wooden Pickaxe tier gate, supported Fat-Burning Torch, and Wooden Sheet restoration.
progress44 = read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java')
permissions44 = read('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
drops44 = read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
torch44 = read('src/main/java/dev/projectflint/block/FatBurningTorchBlock.java')
for source,label in ((progress44,'mining progress'),(permissions44,'break permissions'),(drops44,'authoritative destroy')):
	for tier in ('BlockTags.NEEDS_STONE_TOOL','BlockTags.NEEDS_IRON_TOOL','BlockTags.NEEDS_DIAMOND_TOOL'):
		if tier not in source: fail('v44 Wooden Pickaxe tier gate missing from '+label+': '+tier)
if 'Block.canSupportCenter(level, pos.below(), Direction.UP)' not in torch44:
	fail('v44 Fat-Burning Torch must require standing-torch support')
for needle in ('scheduledTicks.scheduleTick(pos, this, 1)', 'level.scheduleTick(pos, this, 1)', 'level.removeBlock(pos, false)'):
	if needle not in torch44: fail('v44 Fat-Burning Torch support-loss handling missing: '+needle)
wood_sheet44 = RESOURCES/'assets/projectflint/textures/item/wooden_sheet.png'
if not wood_sheet44.exists() or hashlib.sha256(wood_sheet44.read_bytes()).hexdigest() != 'c17e79215b44f8a00272c41d27732335532da6db3ae4ab78b326f56d12ff4815':
	fail('v44 original Wooden Sheet sprite was not restored')
if '0.31.44-b' not in read('docs/releases/0.31.44-b.md'): fail('v44 release notes missing')

# v0.31.45-b: hardcore mining, fat torch, fish, kiln and the activated Hardwood chain.
world45 = read('src/main/java/dev/projectflint/system/PrimitiveWorldRulesSystem.java')
food45 = read('src/main/java/dev/projectflint/system/FoodBalanceSystem.java')
tips45 = read('src/main/java/dev/projectflint/mixin/ItemTooltipMixin.java')
if 'ServerLivingEntityEvents.ALLOW_DAMAGE' in world45 and 'AbstractFish' in world45:
    fail('v45 fish damage still canceled globally')
if 'instanceof net.minecraft.world.item.MobBucketItem' not in tips45 or 'DataComponents.FOOD' not in food45:
    fail('v45 fish buckets must have no food tooltip or food component')
if 'state.getDestroyProgress(player, level, pos) <= 0.0F' not in drops44:
    fail('v45 authoritative mining completion is not guarded')
if 'else cir.setReturnValue(0.0F); // Bare hands/irrelevant items cannot harvest a progression bush.' not in progress44:
    fail('v45 forage bushes still admit unarmed stage skips')
if 'Blocks.COAL_ORE' not in read('src/main/java/dev/projectflint/system/WoodenToolTargets.java'):
    fail('v45 coal ore must be wooden-pickaxe accessible')
if 'state.is(Blocks.COAL_ORE) ? new ItemStack(Items.COAL)' not in drops44:
    fail('v45 coal ore must explicitly drop Coal, not stone')
if 'EnumProperty<Direction> FACING' not in torch44 or 'stack.is(ModItems.FAT)' not in torch44:
    fail('v45 fat torch must be wall-placeable and fueled by Fat')
if 'fatCharges' not in read('src/main/java/dev/projectflint/block/entity/FatBurningTorchBlockEntity.java'):
    fail('v45 torch fuel charge persistence missing')
for entry in ('baked_clay_kiln_body','baked_kiln_top'):
    if 'projectflint:block/brick_shingles' not in read('src/main/resources/assets/projectflint/models/block/'+entry+'.json'):
        fail('v45 kiln texture not swapped to Brick Shingles: '+entry)
for recipe in ('sticks_from_planks','hardwood','hardwood_crafting_table','hardwood_hammer'):
    if not (RESOURCES/'data/projectflint/recipe'/f'{recipe}.json').is_file():
        fail('v45 missing progression recipe: '+recipe)
for material in ('hardwood_axe','hardwood_hoe','hardwood_pickaxe','hardwood_shovel','hardwood_sword'):
    for variant in (material, 'vanilla_wooden_'+material.removeprefix('hardwood_')):
        if not (RESOURCES/'data/projectflint/recipe'/f'{variant}.json').is_file():
            fail('v45 missing 3x3 tool recipe: '+variant)
if 'case INPUT_SLOT -> stack.is(ModItems.HARDWOOD)' not in read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java'):
    fail('v45 forge cannot receive Hardwood')
if 'alloyTicks >= CHARCOAL_BURN_TICKS' not in read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java'):
    fail('v45 alloy must require entire charcoal burn')
if '60L' not in read('src/main/java/dev/projectflint/menu/HardwoodCraftingMenu.java'):
    fail('v45 hardwood crafting table must enforce three seconds')
if 'HARDWOOD_HAMMER_MATERIAL' not in items43:
    fail('v45 heavyweight Hammer was not activated')
if 'ItemTags.PLANKS' not in read('src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java'):
    fail('v45 Basin cannot turn planks into Wood Dust')
if 'craftRemainder(CLAY_BUCKET)' not in items43:
    fail('v45 reusable empty clay bucket not returned after Hardwood crafting')


# v0.31.47-b: limit Hammer stone speed and ship a navigable client field guide.
client47 = read("src/main/java/dev/projectflint/client/ProjectFlintClient.java")
manual47 = read("src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideScreen.java")
content47 = read("src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java")
mining47 = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
item47 = read("src/main/java/dev/projectflint/registry/ModItems.java")
tips47 = read("src/main/java/dev/projectflint/mixin/ItemTooltipMixin.java")
if 'cir.setReturnValue(1.0F / 420.0F)' not in mining47 or 'Math.max(1.0F / 95.0F, vanillaProgress)' in mining47:
    fail('v47 Hammer bypass still outruns pickaxes')
if 'held.is(ModItems.HARDWOOD_PICKAXE) ? 1.0F / 180.0F : 1.0F / 270.0F' not in mining47:
    fail('v47 Hardwood pickaxe missing meaningfully faster mining')
if 'new net.minecraft.world.item.AxeItem(HARDWOOD_MATERIAL' not in item47:
    fail('v47 Hardwood Axe must implement vanilla AxeItem stripping')
if 'properties.pickaxe(HARDWOOD_HAMMER_MATERIAL, 2.0F, -3.2F)' not in item47:
    fail('v47 Hardwood Hammer melee attack was not rebalanced')
if 'Can injure and kill fish.' not in tips47:
    fail('v47 Hammer tooltip missing its fish-killing capability')
if 'GLFW.GLFW_KEY_G' not in client47 or 'GLFW.GLFW_KEY_K' not in client47:
    fail('v47 Field Guide and old Recipe Guide hotkeys must both be registered')
for needle in ('ProjectFlintFieldGuideContent.chapters()', 'renderAtlas', 'matchingItems', 'mouseScrolled', 'ProjectFlintRecipeGuideScreen.openGlobal(this)'):
    if needle not in manual47: fail('v47 Field Guide missing: '+needle)
if content47.count('add(result,') < 25:
    fail('v47 must contain at least 25 complete reference chapters')
if read('src/main/resources/assets/projectflint/lang/en_us.json').count('key.projectflint.field_guide') != 1:
    fail('v47 guide hotkey translation missing')
if '0.31.47-b' not in read('docs/releases/0.31.47-b.md'):
    fail('v47 release notes missing')
# v0.31.48-b: regression guard for the Field Guide crash in 1.21.11.
if 'renderBackground(g, mx, my, partialTick);' in manual47:
    fail('v48 Field Guide render still calls Screen.renderBackground: duplicate blur crash')
if 'g.fill(0, 0, width, height, 0xA0000000);' not in manual47:
    fail('v48 Field Guide missing its blur-free translucent background')
if 'super.render(g, mx, my, partialTick);' not in manual47:
    fail('v48 Field Guide buttons and text controls no longer render normally')
if 'APPLY_PROJECTFLINT_0_31_47_B.bat' not in read('APPLY_PROJECTFLINT_0_31_48_B.bat'):
    fail('v48 cleanup script does not retire previous release BAT')
if '0.31.48-b' not in read('docs/releases/0.31.48-b.md'):
    fail('v48 release notes missing')
if errors:
    print('Project Flint v0.31.48-b validation FAILED:')
    for message in errors: print(' -', message)
    sys.exit(1)
print('Project Flint v0.31.48-b Field Guide crash hotfix checks passed.')
