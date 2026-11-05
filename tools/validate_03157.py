#!/usr/bin/env python3
"""v0.31.57-b source/material/recipe/asset release gates (offline, no Gradle)."""
from pathlib import Path
import json,re,sys,struct
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
REC=RES/'data/projectflint/recipe'
JAVA=ROOT/'src/main/java/dev/projectflint'
checks=0
def check(c,msg):
 global checks
 checks+=1
 if not c:raise AssertionError(msg)
def read(rel):return (ROOT/rel).read_text()
def recipe(name):
 p=REC/(name+'.json');check(p.exists(),f'recipe missing: {name}');return json.loads(p.read_text())
for p in RES.rglob('*.json'):
 try:json.loads(p.read_text())
 except Exception as exc:raise AssertionError(f'{p}: {exc}')
 checks+=1
src=read('src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java')
check('player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 1);' in src,'no hunger decrement')
check(src.index('getFoodLevel() - 1') < src.index('if (stomps + 1 < required)'), 'hunger charged only on final stomp')
check('return Math.min(3,' in src, 'stomp cap missing')
check('!player.isCreative() && player.getFoodData().getFoodLevel() < 1' in src,'empty-hunger protection')
check('output.putInt("Stomps", stomps)' in src,'saved Basin progress')
model=json.loads(read('src/main/resources/assets/projectflint/models/item/basin.json'))
check('display' in model and all(x in model['display'] for x in ('gui','ground','firstperson_righthand','thirdperson_righthand')), 'Basin hand display transforms')
basin=read('src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java')
for expected in ('Items.COBBLESTONE','Items.CLAY','RARE_EARTH_DUST','LITHIUM_DUST','GALLIUM_DUST','TIN_DUST','.1333F','.44F','.66F','.223F','.15F','.048F','.0085F'):
 check(expected in basin, 'Basin output missing '+expected)
check('new ItemStack(ModItems.STONE_DUST), .66F' in basin and 'new ItemStack(ModItems.STONE_DUST), .44F' in basin and 'new ItemStack(ModItems.STONE_DUST), .1333F' in basin,'independent Stone rolls')
check(recipe('hardwood_crafting_table')['type']=='minecraft:crafting_shapeless','Hardwood Table not craftable in personal 2x2')
check(len(recipe('hardwood_crafting_table')['ingredients'])==4,'personal table recipe not 2x2')
check(recipe('leather_straps_from_sheet')['result']['count']==3,'personal leather straps')
check(recipe('leather_repair_kit')['result']['count']==2 and len(recipe('leather_repair_kit')['ingredients'])==3,'personal repair kit')
customs=[json.loads(f.read_text()) for f in REC.glob('hardwood_*.json') if json.loads(f.read_text())['type'].startswith('projectflint:hardwood_')]
check(len(customs)>=150,'hardwood custom recipes lost')
for name,cnt in [('hardwood_wattle_daub_windows',8),('hardwood_leather_sheets',2),('hardwood_leather_straps',3),('hardwood_leather_repair_kits',2),('hardwood_woven_fans',6),('hardwood_fat_burning_torches',2),('hardwood_bsig_dust',4),('hardwood_bssig_dust',4),('hardwood_aluminum_dust_from_rare_earth',3)]:
 check(recipe(name)['result']['count']==cnt,name+' count')
for name in ('hardwood_sheets_any_planks','hardwood_boards_any_planks'):
 d=recipe(name);check('#minecraft:planks' in d['ingredients'],name+' does not accept all planks')
for name in ('hardwood_glass_dust','hardwood_leather_sheets','hardwood_leather_straps','hardwood_sheets_any_planks','hardwood_boards_any_planks','hardwood_woven_fans'):
 check(recipe(name)['type']=='projectflint:hardwood_durable',name+' would consume knife/file')
forge=read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
for expected in ('MBSSIG_DUST','PURE_MICA_INSULATED_GLASS','CHARCOAL_BURN_TICKS','processingKind()','canOutputProcessed','ForgeProcessInputKind'):
 check(expected in forge,'missing forge behavior '+expected)
check('"0.31.57-b"' in read('src/main/resources/fabric.mod.json') or '"version": "${version}"' in read('src/main/resources/fabric.mod.json'), 'fabric mod version check')
check('mod_version=0.31.57-b' in read('gradle.properties'),'version not bumped')
check('v0.31.57-b' in read('build.bat'),'build script not bumped')
check((ROOT/'APPLY_PROJECTFLINT_0_31_57_B.bat').exists(),'cleanup script missing')
lang=json.loads(read('src/main/resources/assets/projectflint/lang/en_us.json'))
items=read('src/main/java/dev/projectflint/registry/ModItems.java')
creative=read('src/main/java/dev/projectflint/registry/ModCreativeTabs.java')
new=['tin_dust','rare_earth_dust','lithium_dust','aluminum_dust','gallium_dust','glass_dust','borosilicate_dust','bsig_dust','bssig_dust','mbssig_dust','pure_mica_insulated_glass']
for n in new:
 check('"'+n+'"' in items,'not registered '+n)
 check('ModItems.'+n.upper() in creative,'not creative '+n)
 check('item.projectflint.'+n in lang,'no translation '+n)
 for path in [f'assets/projectflint/items/{n}.json',f'assets/projectflint/models/item/{n}.json']:
  check((RES/path).exists(),n+' missing asset '+path)
 if n!='pure_mica_insulated_glass':
  pic=RES/f'assets/projectflint/textures/item/{n}.png';check(pic.exists(),n+' no PNG')
  data=pic.read_bytes()
  check(data[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',data[16:24])==(225,225), n+' not recolored from existing wood dust')
ref=read('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java')
check('case HARDWOOD_CRAFTING -> hardwoodCraftingRecipes();' in ref,'K guide lacks hardwood tab')
check('ModItems.PURE_MICA_INSULATED_GLASS' in ref,'K guide lacks forged glass')
check('new ItemStack(Items.COBBLESTONE)' in ref,'K guide lacks cobblestone')
book=read('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')
check('Glass and Rare Earth' in book and 'Insulated Glass' in book,'G handbook lacks new chapters')
reg=read('src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java')
check(reg.count('        add(all,')==len(customs),'K hardwood guide not generated from all source recipes')
for d in (ROOT/'docs/BASIN_MATERIAL_YIELDS.md',ROOT/'docs/decisions/0016-basin-stomp-economy-and-mica-glass-chain.md',ROOT/'docs/releases/0.31.57-b.md'):
 check(d.exists(),f'doc missing: {d}')
print(f'PASS: {checks} offline validation assertions/resources; {len(customs)} hardwood-only recipes verified.')
