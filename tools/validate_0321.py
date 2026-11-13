#!/usr/bin/env python3
"""Focused v0.32.1 regression checks. These are static assertions, not a Minecraft playtest."""
from pathlib import Path
import json,re,sys
ROOT=Path(__file__).resolve().parents[1]
S=ROOT/'src/main'
fail=[];count=0

def check(ok,label):
 global count
 count+=1
 if not ok:fail.append(label)

def text(r):return (ROOT/r).read_text(encoding='utf-8')
def x(r,substring):check(substring in text(r),f'{r}: {substring}')
def jsonr(name):return json.loads((ROOT/name).read_text(encoding='utf-8'))
# all actual asset and data JSON
for path in S.rglob('*.json'):
 try:json.loads(path.read_text(encoding='utf-8'));check(True,str(path))
 except Exception as ex:check(False,f'{path} invalid JSON: {ex}')
check('mod_version=0.32.1' in text('gradle.properties'),'correct release version')
x('build.bat','APPLY_PROJECTFLINT_0_32_1.bat')
x('build.bat','dist')
# Issue-specific checks
x('src/main/java/dev/projectflint/block/WaterPotBlock.java','pot.waterUnits() != 0')
x('src/main/java/dev/projectflint/block/WaterPotBlock.java','pot.setWaterUnits(WaterPotBlockEntity.MAX_UNITS)')
x('src/main/java/dev/projectflint/block/WaterPotBlock.java','new ItemStack(ModItems.CLAY_BUCKET)')
x('src/main/java/dev/projectflint/mixin/BlockItemPlacedLedgerMixin.java','protectWaterFromSugarCane')
x('src/main/java/dev/projectflint/mixin/BlockItemPlacedLedgerMixin.java','FluidTags.WATER')
x('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java','projectflint$isTallFlower')
x('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java','UPDATE_SUPPRESS_DROPS')
x('src/main/java/dev/projectflint/system/PlayerPlacedBlockLedger.java','Blocks.TRIPWIRE')
x('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java','state.is(Blocks.TRIPWIRE) ? new ItemStack(Items.STRING)')
check('level.removeBlock(pos, false);\n                cir.setReturnValue(false);' not in text('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java'),'no forbidden unqualified ore removal')
x('src/main/java/dev/projectflint/system/OreMiningRules.java','return isValidVanillaPickaxe(state, held) ? vanilla : 0.0F;')
for n,sprite in [('axe_piece','flint_axe_pieces'),('cutter_piece','flint_cutter_piece'),('multitool_piece','refined_flint_multitool_piece')]:
 model=jsonr(f'src/main/resources/assets/projectflint/models/block/advanced_tool_assembly_{n}.json')
 check(model['textures']['piece']==f'projectflint:item/{sprite}',f'piece art {n}')
 check(model['textures']['particle']==f'projectflint:item/{sprite}',f'particle art {n}')
for n in ('acacia','bamboo','birch','cherry','dark_oak','jungle','mangrove','oak','pale_oak','spruce'):
 check(not (S/f'resources/data/projectflint/recipe/{n}_stripped_log_from_slabs.json').exists(),f'no slab-to-log {n}')
check('stripped_' not in '\n'.join(line for line in text('src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java').splitlines() if 'new Definition("minecraft:' in line and '_slab",' in line and ', "minecraft:stripped_' in line),'no guide reverse recipes')
check('toolRecipe(Ingredient.of(Items.CLAY_BALL), 3, Ingredient.of(ModItems.BONE_KNIFE)' not in text('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java'),'Clay Bucket removed from Grinder')
check(jsonr('src/main/resources/data/projectflint/recipe/hardwood_unfired_clay_bucket_knife.json')['type']=='projectflint:hardwood_durable','Knife recipe Hardwood durable')
x('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java','canChop ? 1.0F / 90.0F')
x('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java','if (held.is(ModItems.FLINT_AXE) && (projectflint$isSupportedFlintAxeLog(state)')
x('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java','1.0F / 125.0F')
x('src/main/java/dev/projectflint/block/entity/AdvancedKilnBlockEntity.java','new ProcessDefinition(3, result, 8, 6')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','new ItemStack(ModItems.WATER_BEARING_POT)')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','Ingredient.of(ModItems.WOVEN_WOOD), 6, Ingredient.of(ModItems.CLAY_POT)')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','new ItemStack(ModItems.WATTLED_DAUB_WINDOW, 3)')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','Ingredient.of(Items.SUGAR_CANE), 3, Ingredient.of(ModItems.BRANCH), 2, new ItemStack(ModItems.WATTLE, 4)')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','Ingredient.of(Items.LEATHER), 2, Ingredient.of(ModItems.WOVEN_WOOD), 4, new ItemStack(ModItems.WOVEN_CHEST_FRAME)')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','new ItemStack(ModItems.GRINDED_WOOD_SLAB, 2)')
x('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java','new ItemStack(ModItems.WOVEN_WOOD, 2)')
x('src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java','new ItemStack(ModItems.SWEET_BERRY_JAM, 4)')
x('src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java','new ItemStack(ModItems.STRAWBERRY_JAM, 4)')
x('src/main/java/dev/projectflint/recipe/PrimitiveStoveRecipeRegistry.java','new ItemStack(ModItems.MUTTON_PIE, 3)')
x('src/main/java/dev/projectflint/recipe/BookshelfRecipeRegistry.java','Ingredient.of(Items.LEATHER), 1, new ItemStack(Items.BOOK)')
x('src/main/java/dev/projectflint/system/ProjectFlintBrushSystem.java','BRUSH_TICKS = 35')
x('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java','new ItemStack(ModItems.BRANCH, 5)')
check(jsonr('src/main/resources/data/projectflint/recipe/mortar_kiln_base.json')['input_count']==24,'Mortar Kiln Base 24')
for n,output in [('hardwood_wooden_cabinet',4),('hardwood_hardwood_from_wood_dust',3)]:
 check(jsonr(f'src/main/resources/data/projectflint/recipe/{n}.json')['result']['count']==output,f'{n} output {output}')
x('src/main/java/dev/projectflint/system/PrimitiveAnimalDropSystem.java','eligibleAdultDeath && entity instanceof Cow')
x('src/main/java/dev/projectflint/system/PrimitiveAnimalDropSystem.java','eligibleAdultDeath && (entity instanceof BlackBear || entity instanceof BrownBear)')
for n in ('0.32.1-cooking-balance.md','0.32.1-animal-byproducts.md'):
 x(f'docs/proposals/{n}','proposal')
x('docs/releases/0.32.1.md','#25')
x('docs/releases/0.32.1.md','#17')
if fail:
 print(f'FAILED {len(fail)}/{count} checks:')
 for m in fail:print(' -',m)
 sys.exit(1)
print(f'PASS {count} static checks. Minecraft build/playtest must be run separately.')
