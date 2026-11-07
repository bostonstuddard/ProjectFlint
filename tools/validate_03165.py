#!/usr/bin/env python3
"""v0.31.65-b: static gameplay, recipe, resource and release regression audit.
Run from the project root. This is not a live Minecraft game test.
"""
from pathlib import Path
import json
import re
ROOT = Path(__file__).resolve().parents[1]
REC = ROOT / 'src/main/resources/data/projectflint/recipe'
SRC = ROOT / 'src/main/java/dev/projectflint'
AST = ROOT / 'src/main/resources/assets/projectflint'
N = 0

def check(expr, msg):
    global N
    assert expr, msg
    N += 1

def recipe(name):
    return json.loads((REC / (name + '.json')).read_text())

def result(name, id, amount, station='projectflint:hardwood'):
    r=recipe(name)
    check(r['type'].startswith(station), name+': incorrect station '+r['type'])
    check(r['result']['id']==id, name+': wrong result')
    check(r['result'].get('count',1)==amount, name+': wrong output count')
    return r

alljson=list((ROOT / 'src/main/resources').rglob('*.json'))
for f in alljson:
    try: json.loads(f.read_text())
    except Exception as exc: raise AssertionError(f'Invalid JSON: {f} {exc}')
    N+=1

forge=(SRC/'block/entity/FireclayForgeBlockEntity.java').read_text()
check('isFuel(items.get(FUEL_SLOT)) || isFuel(items.get(LEGACY_FUEL_SLOT))' in forge,'Forge relight must work for Logs, Charcoal and migrated wood')
check('if (nextWood && fuel.getCount() < WOOD_FUEL_COST)' in forge,'Require 4 Logs per batch')
check('if (nextWood && !canOutputCharcoal())' in forge,'Never waste wood with full output')
check('if (!nextWood && (processingKind() == 0 || !canOutputProcessed(processingKind())))' in forge,'Charcoal requires valid input/output')
check('if (!formed || burnTicks > 0 || !isFirestarter(items.get(IGNITER_SLOT)))' in forge,'Forge preflight')
check('public static final int WOOD_FUEL_COST = 4' in forge,'Log batch size')
check('public static final int WOOD_BYPRODUCT_TICKS = 15 * 20' in forge,'Charcoal yields')

wood=(SRC/'recipe/WoodenGrinderRecipeRegistry.java').read_text()
check('Ingredient.of(ModItems.MUD_PILE), 8, new ItemStack(ModItems.LOOSE_MUD_BRICKS)' in wood,'8 Mud Piles required')
check('Ingredient.of(ModItems.LOOSE_MUD_BRICKS), 2, Ingredient.of(ModItems.WOODEN_SCREW), 2, new ItemStack(Items.BRICKS)' in wood,'2 loose bricks required')

personal=recipe('leather_straps_from_sheet'); table=recipe('hardwood_leather_straps')
check(personal['result']['count']==2 and table['result']['count']==3,'Personal vs Hardwood straps')
personal=recipe('leather_repair_kit'); table=recipe('hardwood_leather_repair_kits')
check(personal['result']['count']==1 and table['result']['count']==2,'Personal vs Hardwood repair kits')
check(len(recipe('clay_block_from_clay_balls')['ingredients'])==4,'Personal clay blocks cost 4')
check(len(recipe('hardwood_clay_block')['ingredients'])==3,'Hardwood clay blocks cost 3')

for name, output, count in [
 ('hardwood_mud_from_dirt','minecraft:mud',8),('hardwood_mud_from_dirt_vanilla_bucket','minecraft:mud',8),
 ('hardwood_mud_piles_from_mud','projectflint:mud_pile',4),('hardwood_packed_mud','minecraft:packed_mud',1),
 ('hardwood_mud_bricks','minecraft:mud_bricks',4),('hardwood_loose_mud_bricks','projectflint:loose_mud_bricks',1),
 ('hardwood_mud_brick_slabs','minecraft:mud_brick_slab',6),('hardwood_mud_brick_stairs','minecraft:mud_brick_stairs',4),
 ('hardwood_mud_brick_walls','minecraft:mud_brick_wall',6),('hardwood_loose_mud_brick_slabs','projectflint:loose_mud_brick_slab',6),
 ('hardwood_loose_dirt_slabs','projectflint:loose_dirt_slab',6),('hardwood_dirt_slabs','projectflint:dirt_slab',6),
 ('hardwood_dirt_stairs','projectflint:dirt_stairs',4),('hardwood_loose_soil_from_dirt','projectflint:loose_soil',1),
 ('hardwood_dirt_from_loose_soil','minecraft:dirt',1),
]:
    result(name,output,count)
    check(name in (SRC/'recipe/HardwoodCraftingGuideRegistry.java').read_text(),f'{name} absent from K guide')

for name in ('hardwood_mud_from_dirt','hardwood_mud_from_dirt_vanilla_bucket'):
    recipe_json=recipe(name)
    check(recipe_json['pattern']==['DDD','DWD','DDD'],name+' must surround bucket with 8 dirt')
    check(recipe_json['key']['D']=='minecraft:dirt',name+': must use dirt')
check('craftRemainder(CLAY_BUCKET)' in (SRC/'registry/ModItems.java').read_text(),'Clay Water Bucket needs empty remainder')
check('"minecraft:mud_bricks"' not in recipe('hardwood_loose_mud_bricks').get('result',{}).get('id',''), 'reversible loose conversion')
check('hardwood_mud_bricks' not in recipe('hardwood_loose_mud_bricks').get('ingredients',[]),'wrong reverse conversion')

for name in ('dirt_slab','dirt_stairs'):
    for p in [AST/'blockstates'/f'{name}.json',AST/'models/block'/f'{name}.json',AST/'models/item'/f'{name}.json',AST/'items'/f'{name}.json',ROOT/f'src/main/resources/data/projectflint/loot_table/blocks/{name}.json']:
        check(p.is_file(),str(p))
    check(f'DIRT_{name.split("_")[-1].upper()}' in (SRC/'registry/ModBlocks.java').read_text(),'block registry missing '+name)
    check(f'"{name}"' in (SRC/'registry/ModItems.java').read_text(),'item registry missing '+name)
    check(f'ModItems.{name.upper()}' in (SRC/'registry/ModCreativeTabs.java').read_text(),'creative item missing '+name)
    check(f'block.projectflint.{name}' in json.loads((AST/'lang/en_us.json').read_text()),'translation missing '+name)

mixin=(SRC/'mixin/BlockStateBaseMixin.java').read_text()
check('projectflint$packedMudFooting' in mixin and 'cir.setReturnValue(1.10F)' in mixin,'Packed Mud speed +10%')
check('stack.is(Items.PACKED_MUD)' in (SRC/'mixin/ItemTooltipMixin.java').read_text(),'Packed Mud tooltip')
for f in ['mixin/BlockStateBaseMixin.java','system/PrimitiveInteractionSystem.java']:
    txt=(SRC/f).read_text()
    check('state.is(ModBlocks.DIRT_SLAB) || state.is(ModBlocks.DIRT_STAIRS)' in txt,'Dirt shape mining permissions missing '+f)

field=(SRC/'client/screen/ProjectFlintFieldGuideContent.java').read_text()
check('Mud and Earth Building' in field,'G guide chapter missing')
check('Mud + Wheat' in field or 'Mud with Wheat' in field,'G guide packed mud hint missing')
check('walking on Packed Mud' in field or 'Walking on Packed Mud' in field,'G guide speed missing')
check('mod_version=0.31.65-b' in (ROOT/'gradle.properties').read_text(),'version')
check('APPLY_PROJECTFLINT_0_31_65_B.bat' in (ROOT/'build.bat').read_text(),'build cleanup')
check((ROOT/'docs/decisions/0024-forge-wood-autorelight-and-mud-crafting-economy.md').exists(),'ADR')
check((ROOT/'docs/releases/0.31.65-b.md').exists(),'release notes')
print(f'PASS: {N} static checks ({len(alljson)} JSON resources)')
