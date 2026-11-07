#!/usr/bin/env python3
"""0.31.64-b regression guards for early-game crafting and guarded Forge batches."""
from pathlib import Path
import json,re,collections
r=Path(__file__).resolve().parents[1]
checks=0

def source(f): return (r/f).read_text(encoding='utf-8')
def ck(cond,msg):
    global checks
    assert cond,msg
    checks+=1

def recipe(name): return json.loads(source(f'src/main/resources/data/projectflint/recipe/{name}.json'))

mixin=source('src/main/java/dev/projectflint/mixin/RecipeManagerMixin.java')
ck('CraftingRecipe' in mixin and 'getNamespace().equals("minecraft")' in mixin,'vanilla crafting disabled')
ck('path.equals("wooden_axe")' not in mixin,'personal wooden axe not filtered')
ck('path.equals("wooden_pickaxe")' not in mixin,'personal pickaxe not filtered')
ck('path.startsWith("wooden_shovel_")' not in mixin,'personal shovel variants not filtered')
for n,item in {'wooden_axe':'minecraft:wooden_axe','wooden_pickaxe':'minecraft:wooden_pickaxe','wooden_shovel':'minecraft:wooden_shovel','wooden_sword_personal':'minecraft:wooden_sword','wooden_hoe_personal':'minecraft:wooden_hoe'}.items():
    j=recipe(n)
    ck(j['type']=='minecraft:crafting_shapeless' and j['result']['id']==item,(n,'2x2 recipe'))
    ck(len(j['ingredients']) <= 4,(n,'fits 2x2 grid'))
for wood in ('spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','pale_oak'):
    j=recipe(f'wooden_shovel_{wood}')
    ck(j['type']=='minecraft:crafting_shapeless' and j['result']['id']=='minecraft:wooden_shovel',(wood,'shovel'))
    ck(len(j['ingredients'])<=4,(wood,'fits personal grid'))
for name,kind,count in [('oak_sheet_from_plank','durable_crafting',3),('oak_board_from_plank','durable_crafting',1),('hardwood_sheets_any_planks','hardwood_durable',4),('hardwood_boards_any_planks','hardwood_durable',2)]:
    j=recipe(name)
    ck(j['type']=='projectflint:'+kind and j['result']['count']==count,(name,'proper recipe station and yield'))
ck(recipe('oak_sheet_from_plank')['tool']=='projectflint:bone_knife','personal sheet knife remainder')
ck(recipe('oak_board_from_plank')['tool']=='projectflint:flint_file','personal board file remainder')
ck(recipe('hardwood_sheets_any_planks')['ingredients']==['#minecraft:planks'],'all planks hardwood sheet recipe')
ck(recipe('hardwood_boards_any_planks')['ingredients']==['#minecraft:planks'],'all planks hardwood board recipe')
# Reachability gate: first Planks must not require the Wooden Axe that itself requires Sheets.
axe_tag=json.loads(source('src/main/resources/data/projectflint/tags/item/plank_cutting_axes.json'))
ck('projectflint:flint_axe' in axe_tag['values'],'Flint Axe grants first Planks')
ck('minecraft:wooden_axe' in axe_tag['values'],'Wooden Axe keeps its plank crafting')
ck(recipe('oak_planks_from_log')['type']=='projectflint:durable_crafting','early Log-to-Planks craft stays in personal grid')
ck(recipe('oak_planks_from_log')['tool']=='#projectflint:plank_cutting_axes','Log processing uses validated axe tag')
ck('minecraft:oak_planks' in recipe('oak_sheet_from_plank')['ingredients'],'Wooden Axe sheet input now obtainable from Flint Axe')
for wood in ('oak','spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','pale_oak'):
    for variant in ('log','stripped_log'):
        ck(f'"{wood}_planks_from_{variant}_flint_axe"' in source('src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java'),(wood,variant,'Flint Axe guide'))

# Duplicate within Hardwood station: identical ingredient/tool/match type should not be in two definitions.
seen={}
for p in (r/'src/main/resources/data/projectflint/recipe').glob('*.json'):
    j=json.loads(p.read_text())
    if j.get('type')!='projectflint:hardwood_durable':continue
    key=(tuple(sorted(j.get('ingredients',[]))),j.get('tool'))
    ck(key not in seen,f'duplicate Hardwood durable recipe: {seen.get(key)} / {p.name}')
    seen[key]=p.name

guide=source('src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java')
for n in ('wooden_axe','wooden_pickaxe','wooden_shovel','wooden_sword_personal','wooden_hoe_personal','oak_sheet_from_plank','oak_board_from_plank'):
    ck(f'"{n}"' in guide,f'personal K-key guide: {n}')
# Explicit machine-safe relight from idle only, with preflight fuel, ignition and input validation.
forge=source('src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java')
for token in ('if (burnTicks <= 0)', 'items.get(FUEL_SLOT).is(Items.CHARCOAL)', 'nextKind != 0', 'canOutputProcessed(nextKind)', 'isFirestarter(items.get(IGNITER_SLOT))', 'ignite();'):
    ck(token in forge,f'Forge guard {token}')
ck('if (!nextWood && (processingKind() == 0 || !canOutputProcessed(processingKind()))) return false;' in forge,'no idle charcoal consumption')
ck('WOOD_BYPRODUCT_TICKS' in forge and 'if (woodFuel &&' in forge,'log charcoal processing retained')
vest=source('src/main/java/dev/projectflint/system/TannedUtilityVestSystem.java')
for block in ('BUSH','FIREFLY_BUSH','SHORT_DRY_GRASS','TALL_DRY_GRASS'):
    ck('Blocks.'+block in vest,'Vest vanilla vegetation '+block)
ck('!OreMiningRules.isRestricted(state)' in vest,'vest cannot mine ore')
ck('!state.is(BlockTags.MINEABLE_WITH_PICKAXE)' in vest,'vest cannot mine stone')
primitive=source('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
ck('if (TannedUtilityVestSystem.canHandClear(player, state)) return InteractionResult.PASS;' in primitive,'vest attack permission')
ck('if (TannedUtilityVestSystem.canHandClear(player, state)) return true;' in primitive,'vest server permission')
server=source('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
ck('TannedUtilityVestSystem.canHandClear(player, state)' in server,'vest server loot path')
ck('PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak' in server,'FlintQuests converted block compatibility preserved')
ck('mod_version=0.31.64-b' in source('gradle.properties'),'version')
ck('APPLY_PROJECTFLINT_0_31_64_B.bat' in source('build.bat'),'cleanup wiring')
ck('--no-daemon' in source('build.bat') and 'dist' in source('build.bat'),'distribution unchanged')
ck((r/'docs/decisions/0023-restore-personal-wood-tools-avoid-duplicate-hardwood-recipes-and-guard-forge.md').is_file(),'ADR')
ck((r/'docs/releases/0.31.64-b.md').is_file(),'release notes')
for f in (r/'src/main/resources').rglob('*.json'):
    json.loads(f.read_text())
    checks+=1
print(f'PASS {checks} checks: personal tool progression, woodworking ownership, vest flora, Forge guards, prior mining, resource JSON')
