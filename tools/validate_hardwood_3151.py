from pathlib import Path
import json,re,sys
P=Path(__file__).resolve().parents[1]
r=P/'src/main/resources/data/projectflint/recipe'
items=(P/'src/main/java/dev/projectflint/registry/ModItems.java').read_text()
registered=set(re.findall(r'\bregister(?:BlockItem)?\("([a-z0-9_]+)"', items))
errors=[]
for file in r.glob('hardwood_*.json'):
 try:
  recipe=json.loads(file.read_text());typ=recipe['type'];
  if typ not in ('projectflint:hardwood_shaped','projectflint:hardwood_shapeless','projectflint:hardwood_durable'):errors.append(f'{file.name} wrong workstation type {typ}')
  pattern=recipe.get('pattern',[])
  if len(pattern)>3 or any(len(row)>3 for row in pattern):errors.append(f'{file.name} illegal shape')
  if any(c!=' ' and c not in recipe.get('key',{}) for line in pattern for c in line):errors.append(f'{file.name} missing key')
  for ingredient in recipe.get('ingredients',[])+list(recipe.get('key',{}).values())+[recipe.get('tool',''),recipe['result']['id']]:
   if isinstance(ingredient,str) and ingredient.startswith('projectflint:') and ingredient[13:] not in registered:errors.append(f'{file.name} unknown item {ingredient}')
 except Exception as e:errors.append(f'{file.name}: {e}')
if len(list(r.glob('hardwood_*.json')))<120:errors.append('missing Hardwood recipes')
for species in ('oak','spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','pale_oak','bamboo','crimson','warped'):
 recipe=json.loads((r/f'hardwood_{species}_slabs.json').read_text())
 if recipe['result'] != {'id':f'minecraft:{species}_slab','count':6}:errors.append(species+' slab direction reversed')
if not (r/'hardwood_bamboo_door.json').exists():errors.append('missing bamboo door')
for filename,count in [('hardwood_hammer',1),('hardwood_sticks',2),('sticks_from_planks',3),('hardwood_rope_bulk',5)]:
 j=json.loads((r/f'{filename}.json').read_text())
 if j['result']['count']!=count:errors.append(filename+' output count')
ore=(P/'src/main/java/dev/projectflint/system/OreMiningRules.java').read_text()
for key in ('UNQUALIFIED_PROGRESS','WOODEN_PROGRESS','DEEPSLATE_IRON_ORE','COPPER_ORE','COAL_ORE'):
 if key not in ore:errors.append('missing mining gate '+key)
if 'OreMiningRules.canGetDrops(state, held)' not in (P/'src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java').read_text():errors.append('ore server enforcement missing')
if errors: print('\n'.join(errors));sys.exit(1)
print(f'v0.31.51-b: {len(list(r.glob("hardwood_*.json")))} Hardwood recipe JSONs valid; ore gates present.')
