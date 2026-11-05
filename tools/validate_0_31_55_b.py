#!/usr/bin/env python3
"""Targeted portable Project Flint v0.31.55-b source/resource regression audit."""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC = ROOT / 'src/main'
JAVA = SRC / 'java/dev/projectflint'
RES = SRC / 'resources'
errors=[]
checks=0

def chk(condition, message):
    global checks
    checks+=1
    if not condition: errors.append(message)

def code(path): return (JAVA/path).read_text(encoding='utf8')

def resource(path): return (RES/path).read_text(encoding='utf8')

# Version, cleanup, and release state.
chk('mod_version=0.31.55-b' in (ROOT/'gradle.properties').read_text(), 'version mismatch')
chk('APPLY_PROJECTFLINT_0_31_55_B.bat' in (ROOT/'build.bat').read_text(), 'build must use new cleanup')
chk((ROOT/'APPLY_PROJECTFLINT_0_31_55_B.bat').is_file(), 'new cleanup script missing')
chk((ROOT/'docs/releases/0.31.55-b.md').is_file(), 'release notes missing')

# Parse every JSON resource and check full data presence.
resources=list(RES.rglob('*.json'))
for f in resources:
    try: json.loads(f.read_text(encoding='utf8'))
    except Exception as exc: errors.append(f'invalid JSON {f.relative_to(ROOT)}: {exc}')
chk(len(resources)>1500, f'unexpected resource count {len(resources)}')

# All six tools must receive hardwood stick ingredient.
for tool in ('axe','pickaxe','shovel','hoe','sword','hammer'):
    x=json.loads(resource(f'data/projectflint/recipe/hardwood_{tool}.json'))
    chk('projectflint:hardwood_sticks' in x.get('key',{}).values(),f'{tool} still uses vanilla stick')

# Hardwood slab pattern horizontal must not clash with stick vertical.
for name in ('oak','spruce','birch','jungle','acacia','cherry','dark_oak','mangrove','pale_oak','bamboo','crimson','warped'):
    x=json.loads(resource(f'data/projectflint/recipe/hardwood_{name}_slabs.json'))
    chk(x.get('pattern') == ['PP'], f'{name} slab pattern collides with stick')

for id, expected in {
    'axle':(['SSS','SRS','SSS'],'projectflint:axle'),
    'basin_alternative':(['S S','SSS','CCC'],'projectflint:basin'),
}.items():
    x=json.loads(resource(f'data/projectflint/recipe/{id}.json'))
    chk(x.get('pattern')==expected[0] and x.get('result',{}).get('id')==expected[1], f'{id} recipe wrong')
for name in ('cobblestone','andesite','diorite','granite'):
    for item in ('slabs','stairs'):
        p=RES/f'data/projectflint/recipe/hardwood_{name}_{item}.json'
        chk(p.is_file() and json.loads(p.read_text()).get('type')=='projectflint:hardwood_shaped',f'{name} {item} recipe missing')
chk(len(list(RES.glob('data/projectflint/recipe/*door_hinged.json')))>=9,'prior hinged doors missing')
chk('path.endsWith("_door_hinged")' not in code('mixin/RecipeManagerMixin.java'),'hinged doors filtered out')

# Registry/asset parity for all new outputs.
items=code('registry/ModItems.java')
creative=code('registry/ModCreativeTabs.java')
for name in ('IRON_DUST','COPPER_DUST','NICKEL_DUST','SILVER_DUST','STONE_DUST','QUARTZ_DUST','POTASSIUM_DUST','MICA_DUST','GOLD_DUST','SILICON_DUST','SLAG','FERTILIZER','HARDWOOD_STICKS','AXLE'):
    chk(name in items and 'ModItems.'+name in creative, f'{name} missing item registration/creative tab')
    item_id=name.lower()
    chk((RES/f'assets/projectflint/items/{item_id}.json').is_file(), f'{item_id} missing item definition')
    chk((RES/f'assets/projectflint/models/item/{item_id}.json').is_file(), f'{item_id} missing model')
    if name!='AXLE': chk((RES/f'assets/projectflint/textures/item/{item_id}.png').is_file(), f'{item_id} missing sprite')
for p in ('blockstates/axle.json','models/block/axle.json','models/item/axle.json'):
    chk((RES/f'assets/projectflint/{p}').is_file(), 'axle missing '+p)
chk((RES/'data/projectflint/loot_table/blocks/axle.json').is_file(),'axle must drop on break')

# Source contract for random independent Basin rolls.
basin=code('system/BasinMaterialProcessingSystem.java')
for threshold in ('.75F','.50F','.83F','.24F','.10F','.02F','.20F','.01F','.05F','.33F'):
    chk(threshold in basin, f'Basin chance {threshold} missing')
for item in ('RAW_IRON','RAW_COPPER','ANDESITE','GRANITE','STONE','DIORITE','GRAVEL','SAND','STONE_DUST','SLAG'):
    chk(item in basin,f'Basin input {item} missing')
chk('random.nextBoolean() ? 2 : 1' in basin, 'equal 1/2 Basin split missing')
chk('for (ItemStack drop : dev.projectflint.system.BasinMaterialProcessingSystem.process' in code('block/entity/BasinBlockEntity.java'), 'Basin processing loop missing')

ore=code('system/OreMiningRules.java')
chk('isForbiddenDeepslate' in ore and 'if (isForbiddenDeepslate(state)) return 0.0F' in ore,'Deepslate progress gate missing')
chk('if (isForbiddenDeepslate(state)) return false' in ore,'Deepslate drop gate missing')
sv=code('mixin/ServerPlayerGameModeMixin.java')
for marker in ('new ItemStack(Items.RAW_IRON)','new ItemStack(Items.RAW_COPPER)','new ItemStack(Items.COAL, held.is(ModItems.HARDWOOD_HAMMER) ? 2 : 1)'):
    chk(marker in sv,'ore yield wrong: '+marker)
for source in ('mixin/BlockStateBaseMixin.java','system/PrimitiveInteractionSystem.java','mixin/ServerPlayerGameModeMixin.java'):
    chk('PlayerPlacedBlockLedger.isHandRecoverableDecoration(state)' in code(source),'decor exemption missing '+source)
chk('state.is(ModBlocks.NEST)' in sv and 'Containers.dropContents' in sv,'chicken nest content-drop path missing')
chk('canHandClear' not in ore,'Utility Vest must not override rock gates')

fx=code('system/PrimitiveEnvironmentEffectSystem.java')
for marker in ('player.getBlockY() < -40','player.getBlockY() < 0','>= 13','level.canSeeSky','isBrightOutside','SPELUNCA_STACK_TICKS','MAX_SPELUNCA_AMPLIFIER','player.removeEffect(ModEffects.CLAUSTROPHOBIA)','player.removeEffect(ModEffects.SPELUNCAPHOBIA)'):
    chk(marker in fx,'underground effect condition missing: '+marker)
chk('getFoodLevel() > 6' in fx,'nonfatal hunger floor missing')
chk('ModEffects.SPELUNCAPHOBIA' in code('mixin/FoodDataMixin.java'), 'Speluncaphobia healing adjustment missing')
chk('stone_from_cobblestone' in code('recipe/BrickFurnaceRecipeRegistry.java'),'Brick Furnace stone recipe missing')

# Documentation and guides must be revised together.
for d in ('docs/BASIN_MATERIAL_YIELDS.md','docs/CURRENT_SYSTEMS.md','docs/ARCHITECTURE.md','docs/PRIMITIVE_PROGRESSION_MATRIX.md','docs/RELEASE_PROCESS.md','docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md','docs/decisions/0012-small-decoration-recovery-without-vest-mining-bypass.md','docs/decisions/0013-stone-ore-gates-and-probabilistic-basin.md','docs/decisions/0014-depth-fear-light-exemptions.md'):
    chk((ROOT/d).is_file(), f'documentation missing: {d}')
chk('ModItems.QUARTZ_DUST' in code('client/screen/ProjectFlintRecipeGuideScreen.java'),'K guide missing Basin outputs')
chk('Axles and Building' in code('client/screen/ProjectFlintFieldGuideContent.java'),'G guide missing axle')

if errors:
    print(f'Project Flint v0.31.55-b targeted audit FAILED ({checks} checks, {len(resources)} JSONs):')
    for issue in errors: print(' -',issue)
    sys.exit(1)
print(f'Project Flint v0.31.55-b targeted audit PASSED ({checks} checks, {len(resources)} JSONs parsed).')
