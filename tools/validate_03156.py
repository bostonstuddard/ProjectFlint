#!/usr/bin/env python3
"""Source and resource regression checks for the v0.31.56-b patch.

These are static checks, not a substitute for a Minecraft Gradle build or in-game validation.
"""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parent.parent

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def check(ok, detail):
    if not ok:
        raise AssertionError(detail)

basin = read('src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java')
processing = read('src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java')
renderer = read('src/main/java/dev/projectflint/client/BasinItemRenderer.java')
menu = read('src/main/java/dev/projectflint/menu/HardwoodCraftingMenu.java')
durable = read('src/main/java/dev/projectflint/recipe/DurableCraftingRecipe.java')
client = read('src/main/java/dev/projectflint/client/ProjectFlintClient.java')
guide = read('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java')
models = json.loads(read('src/main/resources/assets/projectflint/blockstates/basin.json'))

check('BlockEntityRenderers.register(ModBlockEntities.BASIN, BasinItemRenderer::new)' in client, 'renderer not registered')
check('itemModelResolver.updateForTopItem' in renderer and 'basin.stored()' in renderer, 'renderer not using stored item')
check(len(models['variants']) == 28 and all(x['model'] == 'projectflint:block/basin' for x in models['variants'].values()), 'stale static wood model')
check('getUpdatePacket()' in basin and 'getUpdateTag(' in basin and 'sendBlockUpdated' in basin, 'client item synchronization incomplete')
check('output.putInt("Stomps", stomps)' in basin and 'getIntOr("Stomps", 0)' in basin, 'stomp progress not persisted')
check('stomps = 0;' in basin and 'stomps++' in basin, 'stomp lifecycle incomplete')
check('stomps + 1 < required' in basin, 'one-stomp processing regression')
check(basin.index('stomps + 1 < required') < basin.index('player.getFoodData().setFoodLevel'), 'hunger debited before final stomp')
check('requiredStompsFor' in basin and 'withStomps(count)' in guide, 'recipe guide stomp counts not wired')
for material, value in [('RAW_IRON',5),('RAW_COPPER',4),('ANDESITE',4),('SLAG',3),('GRAVEL',2),('SAND',2)]:
    check(re.search(r'input\.is\('+('ModItems' if material in ('SLAG','STONE_DUST') else 'Items')+r'\.'+material+r'\).*?return '+str(value)+r';', processing, re.S) is not None, f'{material} stomp requirement changed')
check('return 2;' in basin and 'return 3;' in basin, 'wood stump thresholds missing')
check('public void onTake' in menu and 'consumeHardwoodInputs' in menu, 'hardwood result slot transaction missing')
check('getRemainingItems(input)' in menu and 'RecipeType.CRAFTING' in menu, 'recipe-specific remainders not used')
check('consumingRecipe = true' in menu and 'consumingRecipe = false' in menu and '!consumingRecipe' in menu, 'recipe refresh not guarded')
check('craftSlots.removeItem(slotIndex, 1)' in menu and 'craftSlots.setItem(slotIndex, remainder)' in menu, 'tool reinsert after ingredient consumption missing')
check('if (!player.getInventory().add(remainder))' in menu and 'player.drop(remainder, false)' in menu, 'full-inventory tool handling missing')
check('Math.clamp(progress.get(), 0, CRAFT_TICKS)' in menu and 'CRAFT_TICKS = 60' in menu, 'server timed crafting changed')
check('worn.setDamageValue(worn.getDamageValue() + 1)' in durable and 'worn.getDamageValue() >= worn.getMaxDamage()' in durable, 'durable tool wear/break missing')

for rel in ROOT.rglob('src/main/resources/**/*.json'):
    json.loads(rel.read_text(encoding='utf-8'))
recipe_folder = ROOT / 'src/main/resources/data/projectflint/recipe'
all_durable = sorted(recipe_folder.glob('hardwood_*.json'))
n = 0
for path in all_durable:
    data = json.loads(path.read_text())
    if data.get('type') == 'projectflint:hardwood_durable':
        check(data.get('tool') in {'projectflint:bone_knife','projectflint:flint_file','projectflint:flint_plant_cutters','projectflint:unfired_brick_mold'}, f'bad durable tool in {path.name}')
        n += 1
check(n >= 8, f'expected hardwood durable recipes, found {n}')
check('mod_version=0.31.56-b' in read('gradle.properties'), 'version not incremented')
check('APPLY_PROJECTFLINT_0_31_56_B.bat' in read('build.bat'), 'new apply script not called')
check((ROOT / 'docs/releases/0.31.56-b.md').is_file(), 'missing release notes')
check((ROOT / 'docs/decisions/0015-physical-basin-and-durable-remainders.md').is_file(), 'missing ADR')
print('PASS: v0.31.56-b basin visual/stomp, durability, recipes, guides, version, and JSON static checks')
print('NOTE: full Gradle build and in-game behavior remain unverified.')
