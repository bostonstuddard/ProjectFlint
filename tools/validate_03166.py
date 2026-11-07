#!/usr/bin/env python3
"""0.31.66-b static, asset, progression and mapping-sensitive regression checks.
A passing result does not establish that Gradle builds or the game launches.
"""
from pathlib import Path
import json, re, sys
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
checks=0

def assert_ok(test, message):
    global checks
    checks+=1
    if not test: raise AssertionError(message)

def txt(rel): return (ROOT/rel).read_text(encoding='utf-8')
def data(rel): return json.loads(txt(rel))
def java(rel): return txt('src/main/java/dev/projectflint/'+rel)
def recipe(name): return data('src/main/resources/data/projectflint/recipe/'+name+'.json')
def contains(rel,*terms):
    s=txt(rel)
    for term in terms: assert_ok(term in s,rel+': '+term)

assert_ok('mod_version=0.31.66-b' in txt('gradle.properties'),'version')
contains('build.bat','0.31.66-b','--no-daemon','dist')
assert_ok((ROOT/'APPLY_PROJECTFLINT_0_31_66_B.bat').exists(),'cleanup bat')
assert_ok('getSpeedFactor' not in java('mixin/BlockStateBaseMixin.java'),'invalid Packed Mud Mixin')
contains('src/main/resources/projectflint.mixins.json','PackedMudMovementMixin')
contains('src/main/java/dev/projectflint/mixin/PackedMudMovementMixin.java','@Mixin(Entity.class)','getBlockSpeedFactor','Blocks.PACKED_MUD','1.10F')

axes=data('src/main/resources/data/projectflint/tags/item/plank_cutting_axes.json')['values']
assert_ok(set(axes)=={'minecraft:wooden_axe','projectflint:hardwood_axe'},'flint axe must not produce planks')
personal=java('recipe/PersonalCraftingGuideRegistry.java')
assert_ok('planks_from_log_flint_axe' not in personal and 'planks_from_stripped_log_flint_axe' not in personal,'stale guide plank recipes')
bridge=recipe('primitive_wooden_sheet_from_stripped_log')
assert_ok(bridge['type']=='projectflint:durable_crafting','bridge recipe type')
assert_ok(bridge['tool']=='projectflint:bone_knife','bridge knife wear')
assert_ok(bridge['ingredients']==['#projectflint:primitive_stripped_logs_for_sheets'],'bridge stripped tag')
assert_ok(bridge['result']=={'id':'projectflint:wooden_sheet','count':1},'bridge output')
assert_ok(len(data('src/main/resources/data/projectflint/tags/item/primitive_stripped_logs_for_sheets.json')['values'])>=9,'bridge wood varieties')
assert_ok('primitive_wooden_sheet_from_stripped_log' in personal,'bridge guide')

blast=java('block/entity/DynamiteChargeBlockEntity.java')
for fragment in ['FUSE_TICKS = 90','RADIUS = 2.75','Blocks.STONE','Blocks.COBBLESTONE','Blocks.ANDESITE','Blocks.DIORITE','Blocks.GRANITE','Blocks.IRON_ORE','Blocks.COPPER_ORE','Blocks.COAL_ORE','processWithMultiplier','1.5F','damage = 12.0F','server.setBlock','hurtServer']:
    assert_ok(fragment in blast,'dynamite '+fragment)
for banned in ['Blocks.DEEPSLATE','Blocks.DEEPSLATE_IRON_ORE','Blocks.DEEPSLATE_COPPER_ORE','Blocks.DEEPSLATE_COAL_ORE','Blocks.CRAFTING_TABLE','Blocks.FURNACE']:
    assert_ok(banned not in blast,'blast must not affect '+banned)
item=java('item/DynamiteItem.java')
for term in ['InteractionHand.MAIN_HAND','InteractionHand.OFF_HAND','Items.FLINT_AND_STEEL','Capability.FIRESTARTER','super.useOn','hurtAndBreak']:
    assert_ok(term in item,'placement check '+term)
for term in ['getTicker','DynamiteChargeBlockEntity::tick','RenderShape.MODEL']:
    assert_ok(term in java('block/DynamiteChargeBlock.java'),'charge block '+term)
assert_ok('DYNAMITE_CHARGE' in java('registry/ModBlockEntities.java'),'block entity registration')
assert_ok('DYNAMITE_CHARGE' in java('registry/ModBlocks.java'),'block registration')
for name in ['dynamite','fuse','fuse_ready_dust','fine_fuse_powder','black_powder','sulfur_dust','carbon_dust','pure_carbon_dust']:
    for path in ['src/main/resources/assets/projectflint/models/item/'+name+'.json','src/main/resources/assets/projectflint/items/'+name+'.json','src/main/resources/assets/projectflint/textures/item/'+name+'.png']:
        assert_ok((ROOT/path).is_file(),'missing item art '+path)
    assert_ok(name.upper() in java('registry/ModItems.java'),'item registration '+name)
for name in ['hardwood_fuse_ready_dust','hardwood_fuse','hardwood_black_powder_potassium','hardwood_black_powder_gunpowder','hardwood_dynamite']:
    obj=recipe(name)
    assert_ok(obj['type'].startswith('projectflint:hardwood_'),'wrong recipe type '+name)
assert_ok(recipe('hardwood_dynamite')['pattern']==[' F ','PRP','PCP'],'dynamite shape')
assert_ok(recipe('hardwood_black_powder_potassium')['result']['count']==3,'potassium powder yield')
assert_ok(recipe('hardwood_black_powder_gunpowder')['result']['count']==4,'gunpowder yield')
assert_ok(recipe('hardwood_fuse')['result']['id']=='projectflint:fuse','fuse result')
mat=java('system/BasinMaterialProcessingSystem.java')
for term in ['processWithMultiplier','expected - guaranteed','random.nextDouble()','input.is(Items.CHARCOAL)','input.is(Items.COAL)','ModItems.SULFUR_DUST, .20','ModItems.SULFUR_DUST, .45','ModItems.CARBON_DUST, .024','ModItems.STONE_DUST, .43','ModItems.STONE_DUST, .23','ModItems.WOOD_DUST, .15']:
    assert_ok(term in mat,'Basin roll '+term)
for term in ['FINE_FUSE_POWDER','200 * 20','FUSE_READY_DUST']:
    assert_ok(term in java('recipe/BrickFurnaceRecipeRegistry.java'),'Brick Furnace '+term)
for term in ['200 * 20','FINE_FUSE_POWDER']:
    assert_ok(term in java('block/entity/BrickFurnaceBlockEntity.java'),'Brick Furnace timer '+term)
for term in ['ModItems.CARBON_DUST','ModItems.PURE_CARBON_DUST','400 * 20','processInputKind == 3']:
    assert_ok(term in java('block/entity/FireclayForgeBlockEntity.java'),'Forge carbon '+term)
assert_ok('Coal and Sulfur' in java('client/screen/ProjectFlintFieldGuideContent.java'),'Field Guide update')
assert_ok('FINE_FUSE_POWDER' in java('client/screen/ProjectFlintRecipeGuideScreen.java'),'K guide update')
assert_ok('hardwood_dynamite' in java('recipe/HardwoodCraftingGuideRegistry.java'),'Hardwood guide recipe')
assert_ok((ROOT/'docs/decisions/0025-packed-mud-mixin-and-selective-dynamite-processing.md').is_file(),'ADR')
assert_ok((ROOT/'docs/releases/0.31.66-b.md').is_file(),'release notes')

for path in (ROOT/'src/main/resources').rglob('*.json'):
    json.loads(path.read_text(encoding='utf8'))
    checks+=1
for path in (ROOT/'src/main/resources/assets/projectflint/textures/item').glob('*.png'):
    with Image.open(path) as im: assert_ok(im.width>0 and im.height>0,'bad PNG '+str(path))
print(f'PASS: {checks} targeted/static resource checks. Build & game launch not verified.')
