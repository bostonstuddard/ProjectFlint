from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
EXPECTED_VERSION = "0.28.41-a"
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
if "APPLY_PROJECTFLINT_0_28_41_A.bat" not in build:
	fail("build.bat does not apply the v0.28.41-a cleanup")
if 'explorer.exe /select,' not in build:
	fail("build.bat no longer opens Explorer with the successful JAR selected")
if not (ROOT / "APPLY_PROJECTFLINT_0_28_41_A.bat").exists():
	fail("missing v0.28.41-a cleanup BAT")

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

build_bat = read("build.bat")
cleanup_bat = read("APPLY_PROJECTFLINT_0_28_41_A.bat")
for expected in ("v0.28.41-a", "APPLY_PROJECTFLINT_0_28_41_A.bat"):
	if expected not in build_bat:
		fail(f"build.bat version/apply wiring missing: {expected}")
if "APPLY_PROJECTFLINT_0_28_28_A.bat" not in cleanup_bat:
	fail("v0.28.30-a cleanup must retire the previous v0.28.28-a apply BAT")

# v0.28.7.8-a gourd and food rules.
primitive = read("src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java")
block_state_mixin = read("src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java")
game_mode_mixin = read("src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java")
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
	"Items.MELON_SLICE", "food(1, 0.25F)", "Items.COOKED_CHICKEN", "food(3, 0.60F)",
	"Items.COOKED_MUTTON", "food(5, 0.80F)", "Items.COOKED_RABBIT", "food(2, 0.90F)",
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
for expected in ("MAJOR.RELEASE.DEV-CHANNEL", "Never introduce a fourth numeric component", "0.28.41-a"):
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
	'Ingredient.of(ModItems.SUPER_HEATED_CLAY), 24',
	'Ingredient.of(ModItems.MUD_PILE), 16',
	'new ItemStack(ModItems.TORCH_HOLDER)',
	'Ingredient.of(Items.FLINT), 16',
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
if "WoodenGrinderRecipeRegistry.find(slot0, slot1)" not in wooden_menu:
	fail("Wooden Grinder gameplay matching is not using the shared dual-recipe registry")
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
	"if (lightmapInfo.NightVisionFactor > 0.0)",
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
	"findBelow(ServerLevel", "footprint(AquiferHit", "SCARCE(0.25F",
	"NORMAL(0.60F", "GOOD(0.85F", "ABUNDANT(1.20F",
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
if '"item.projectflint.aquifer_finder": "Aquifer Dowsing Rod"' not in read("src/main/resources/assets/projectflint/lang/en_us.json"):
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
if "Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D)" not in lumber_workpiece:
	fail("Lumber Processor finished workpiece must be a half-slab shape")
for forbidden in ("STRIPPED_CRIMSON", "STRIPPED_WARPED"):
	if forbidden in lumber_system:
		fail(f"Lumber Processor must reject Nether stems: {forbidden}")
for adv in ("sanding_station.json", "sanding_station_loaded.json", "water_bearing_pot.json", "water_bearing_pot_filled.json", "water_pot.json", "water_pot_filled.json", "bound_wood_processor.json", "lumber_processor.json", "lumber_washing.json", "wooden_board.json", "wooden_beam.json"):
	if not (adv_root / adv).exists():
		fail(f"Wood -> Water progression advancement missing: {adv}")
cleanup_2837 = read("APPLY_PROJECTFLINT_0_28_41_A.bat")
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

# v0.28.41-a owner-supplied Wood -> Water textures.
for tex in (
    "src/main/resources/assets/projectflint/textures/block/sanding_station_top.png",
    "src/main/resources/assets/projectflint/textures/block/lumber_processor_top.png",
    "src/main/resources/assets/projectflint/textures/block/lumber_processor_side.png",
    "src/main/resources/assets/projectflint/textures/block/lumber_processor_bottom.png",
    "src/main/resources/assets/projectflint/textures/block/water_pot_top.png",
    "src/main/resources/assets/projectflint/textures/block/water_pot_side.png",
    "src/main/resources/assets/projectflint/textures/block/water_pot_front.png",
):
    if not (ROOT / tex).exists():
        fail(f"v0.28.41-a texture missing: {tex}")
water_pot_block_2837 = read("src/main/java/dev/projectflint/block/WaterPotBlock.java")
for expected in ("EnumProperty<Direction> FACING", "getStateForPlacement", "builder.add(FACING)"):
    if expected not in water_pot_block_2837:
        fail(f"v0.28.41-a Water Pot facing support missing: {expected}")
for model_path, expected_texture in (
    ("src/main/resources/assets/projectflint/models/block/sanding_station.json", "sanding_station_top"),
    ("src/main/resources/assets/projectflint/models/block/lumber_chopping_block.json", "lumber_processor_top"),
    ("src/main/resources/assets/projectflint/models/block/water_pot.json", "water_pot_front"),
):
    if expected_texture not in read(model_path):
        fail(f"v0.28.41-a model wiring missing: {model_path} -> {expected_texture}")


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

if errors:
	print("Project Flint release validation FAILED:")
	for message in errors:
		print(f" - {message}")
	sys.exit(1)
print(f"Project Flint {EXPECTED_VERSION} release validation passed.")

