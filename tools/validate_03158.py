from pathlib import Path
import json, re
import struct
R=Path(__file__).resolve().parents[1];
def read(f):return (R/f).read_text()
def rec(name):return json.loads(read(f'src/main/resources/data/projectflint/recipe/{name}.json'))
checks=[]
def ck(name,condition):
 checks.append((name,bool(condition)))
 if not condition:print('FAIL:',name)
# JSON integrity
json_files=list((R/'src/main/resources').rglob('*.json'))
for f in json_files:
 try:json.loads(f.read_text())
 except Exception as e:print('BAD JSON',f,e);raise
ck('JSON resources all parse',len(json_files)>1500)
# forge
forge=read('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
ck('forge no automatic ignition',not re.search(r'ignite\(\)\s*;\s*// A new fuel',forge) and forge.count('ignite();') == 0)
ck('forge charcoal requires input and output', 'processingKind() == 0 || !canOutputProcessed(processingKind())' in forge)
# torch
T=read('src/main/java/dev/projectflint/block/entity/FatBurningTorchBlockEntity.java')
ck('torch 50 minutes','BURN_TIME = 50 * 60 * 20' in T)
ck('torch denies below Y0','getBlockPos().getY() < 0' in T)
# independent odds: check branch and chance call order
b=read('src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java')
for n,pat in [('copper slag 12.5','new ItemStack(ModItems.SLAG), .125F'),('clay rare earth 4','new ItemStack(ModItems.RARE_EARTH_DUST), .04F'),('slag rare earth 1.8','new ItemStack(ModItems.RARE_EARTH_DUST), .018F')]:ck(n,pat in b)
ck('3 independent rare-earth 4% rolls',b.count('new ItemStack(ModItems.RARE_EARTH_DUST), .04F')==3)
ck('clay output independent','} else if (input.is(Items.CLAY)) {' in b and b[b.index('} else if (input.is(Items.CLAY)) {'):b.index('} else if (input.is(ModItems.RARE_EARTH_DUST)) {')].count('addChance(')==4)
ck('max 3 stomps', 'return 3;' in b and 'return 2;' in b)
# recipes
c=rec('personal_wooden_cups');ck('personal cups 3 durable',c['type']=='projectflint:durable_crafting' and c['result']['count']==3 and c['tool']=='projectflint:bone_knife')
c=rec('hardwood_wooden_cups');ck('hardwood cups 4 durable',c['type']=='projectflint:hardwood_durable' and c['result']['count']==4 and c['tool']=='projectflint:bone_knife')
for prefix,yield_,ctype in [('personal',5,'minecraft:crafting_shapeless'),('hardwood',6,'projectflint:hardwood_shapeless')]:
 c=rec(prefix+'_wooden_cups_raw_milk');ck(prefix+' milk cups',c['type']==ctype and c['result']['count']==yield_ and c['ingredients'].count('projectflint:wooden_cup')==2 and 'projectflint:clay_milk_bucket' in c['ingredients'])
ck('milk bucket remainder','properties.stacksTo(1).craftRemainder(CLAY_BUCKET)' in read('src/main/java/dev/projectflint/registry/ModItems.java'))
c=rec('hardwood_flax_seeds_to_wheat');ck('flax 8 seeds water -> 6',c['pattern']==['FFF','FWF','FFF'] and c['result']['count']==6)
c=rec('hardwood_potassium_rich_fertilizer');ck('potassium fertilizer 8 + 1 -> 12',c['pattern']==['FFF','FPF','FFF'] and c['result']['count']==12)
c=rec('hardwood_flint_plant_cutters');ck('cutters reversed flint',c['pattern']==['SF ','S F','SF '])
# assets, lang, registration
mc='src/main/java/dev/projectflint/registry/ModBlocks.java';mi='src/main/java/dev/projectflint/registry/ModItems.java'
ck('tinted block registered','new TintedGlassBlock(' in read(mc) and 'PURE_MICA_INSULATED_GLASS' in read(mc))
ck('glass blockitem','registerBlockItem("pure_mica_insulated_glass", ModBlocks.PURE_MICA_INSULATED_GLASS' in read(mi))
ck('translucent layer','BlockRenderLayerMap.putBlock(ModBlocks.PURE_MICA_INSULATED_GLASS, ChunkSectionLayer.TRANSLUCENT)' in read('src/main/java/dev/projectflint/client/ProjectFlintClient.java'))
for p in ('assets/projectflint/blockstates/pure_mica_insulated_glass.json','assets/projectflint/models/block/pure_mica_insulated_glass.json','assets/projectflint/items/potassium_rich_fertilizer.json','data/projectflint/loot_table/blocks/pure_mica_insulated_glass.json'):
 ck('resource '+p,(R/'src/main/resources'/p).is_file())
png=(R/'src/main/resources/assets/projectflint/textures/block/pure_mica_insulated_glass.png').read_bytes()
ck('glass sprite valid 16x16 PNG',png.startswith(b'\x89PNG\r\n\x1a\n') and struct.unpack('>II',png[16:24])==(16,16))
ck('fertilizer item can bonemeal', 'extends BoneMealItem' in read('src/main/java/dev/projectflint/item/PotassiumRichFertilizerItem.java'))
ck('fertilizer tree growth restricted','BlockTags.SAPLINGS' in read('src/main/java/dev/projectflint/item/PotassiumRichFertilizerItem.java'))
ck('material in creative', 'output.accept(ModItems.POTASSIUM_RICH_FERTILIZER)' in read('src/main/java/dev/projectflint/registry/ModCreativeTabs.java'))
# guide parity and docs
for fname in ('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java','src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java'):
 text=read(fname)
 ck('Guide updated '+fname,'12.5%' in text and '4%' in text)
ck('user docs current','1.8%' in read('docs/BASIN_MATERIAL_YIELDS.md'))
ck('version','mod_version=0.31.58-b' in read('gradle.properties'))
ck('release file',(R/'docs/releases/0.31.58-b.md').exists())
ck('ADR',(R/'docs/decisions/0017-manual-forge-cycles-tinted-glass-and-independent-basin-odds.md').exists())
ck('updated BAT',(R/'APPLY_PROJECTFLINT_0_31_58_B.bat').exists())
print(f'Passed {sum(ok for _,ok in checks)} / {len(checks)} checks, {len(json_files)} JSON resources')
assert all(ok for _,ok in checks)
