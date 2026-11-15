#!/usr/bin/env python3
"""Project Flint v0.32.2 recipe retirement and anti-duplication checks.

Runs against patched source *after* APPLY_PROJECTFLINT_0_32_2.bat cleanup.
This is not a Minecraft launch test.
"""
from __future__ import annotations
import collections
import json
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main"
REC = SRC / "resources/data/projectflint/recipe"
JAVA = SRC / "java/dev/projectflint"
failures: list[str] = []
checks = 0

def check(value: bool, msg: str) -> None:
    global checks
    checks += 1
    if not value:
        failures.append(msg)

def source(p: str) -> str:
    return (ROOT / p).read_text(encoding="utf-8")

retired_wood = [
    f"{wood}_stripped_log_from_slabs" for wood in
    ("acacia", "bamboo", "birch", "cherry", "dark_oak", "jungle", "mangrove", "oak", "pale_oak", "spruce")
]
retired_others = [
    "loose_dirt_from_slabs", "loose_mud_bricks_from_slabs",
    "sanded_wood_slabs_from_grinded", "primitive_wooden_sheet_from_stripped_log", "personal_pmi_sheets"
]
retired = retired_wood + retired_others

for path in SRC.rglob("*.json"):
    try:
        json.loads(path.read_text(encoding="utf-8"))
        check(True, "valid JSON: " + str(path.relative_to(ROOT)))
    except Exception as exc:
        check(False, f"invalid JSON {path}: {exc}")

check("mod_version=0.32.2" in source("gradle.properties"), "version 0.32.2")
check('APPLY_PROJECTFLINT_0_32_2.bat' in source('build.bat'), 'build runs v0.32.2 cleanup')
check('clean build --no-daemon' in source('build.bat'), 'clean build required')
check('dist' in source('build.bat'), 'dist output')
for name in retired:
    check(not (REC / (name + ".json")).exists(), f"retired recipe JSON absent: {name}")
    bat = source("APPLY_PROJECTFLINT_0_32_2.bat")
    if name.endswith('_stripped_log_from_slabs'):
        check('_stripped_log_from_slabs.json' in bat, 'cleanup covers old slab-log recipes')
    else:
        check(name in bat, f'cleanup covers {name}')

policy=source('src/main/java/dev/projectflint/recipe/RetiredRecipePolicy.java')
for name in retired_others:
    check('"'+name+'"' in policy, f'runtime denies {name}')
check('path.endsWith("_stripped_log_from_slabs")' in policy, 'runtime denies ten slab-log IDs')
check('"projectflint".equals(namespace)' in policy, 'runtime policy scoped to projectflint')
for loc in (
    'src/main/java/dev/projectflint/mixin/RecipeManagerMixin.java',
    'src/main/java/dev/projectflint/menu/ClayGrinderMenu.java',
    'src/main/java/dev/projectflint/menu/WoodenGrinderMenu.java',
):
    body=source(loc)
    check('RetiredRecipePolicy.isRetired(' in body, f'{loc} blocks deprecated recipe IDs')

mirror=source('src/main/java/dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java')
for text in ('new Definition("projectflint:loose_dirt_slab", 2, "projectflint:loose_dirt"',
             'new Definition("projectflint:loose_mud_brick_slab", 2, "projectflint:loose_mud_bricks"',
             'new Definition("projectflint:grinded_wood_slab", 1, "projectflint:sanded_wood_slab"'):
    check(text not in mirror, f'guide does not advertise retired reverse recipe: {text[:55]}')
for text in ('new Definition("projectflint:fireclay_brick_slab", 2, "projectflint:fireclay_brick_block"',
             'new Definition("minecraft:stripped_oak_log", 1, "minecraft:oak_slab"'):
    check(text in mirror, 'preserve safe forward/balanced recipes: '+text[:55])

# Check production recipes: Hardwood Table 2 -> 6 slab, plus the retired
# reverse 2 -> 1, is a 50%-gain cycle. The forward crafts are kept.
for name,expected_in,expected_out in (
    ('hardwood_oak_slabs', 2, 6),
    ('hardwood_loose_dirt_slabs', 2, 6),
    ('hardwood_loose_mud_brick_slabs', 2, 6),
):
    item=json.loads((REC / (name + '.json')).read_text(encoding='utf-8'))
    check(item['type']=='projectflint:hardwood_shaped', name+' correct recipe type')
    check(sum(len(s) for s in item['pattern'])==expected_in and item['result']['count']==expected_out,
          name+' forward output unchanged')

# Source check for the approved 3 sanded + Flint File -> 2 grinded conversion.
grinder=source('src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java')
check(bool(re.search(r'toolRecipe\(Ingredient\.of\(ModItems\.SANDED_WOOD_SLAB\),\s*3,\s*Ingredient\.of\(ModItems\.FLINT_FILE\),\s*new ItemStack\(ModItems\.GRINDED_WOOD_SLAB,\s*2\)', grinder)),
      'approved sanded-to-grinded Wooden Grinder recipe kept')

# Automatically detect profitable 2-edge conversions among the JSON recipes.
edges: list[tuple[str,str,float,str]]=[]
for path in REC.glob('*.json'):
    obj=json.loads(path.read_text(encoding='utf-8'))
    result=obj.get('result')
    if not isinstance(result,dict) or 'id' not in result:
        continue
    out=result['id'];n=result.get('count',1)
    typ=obj.get('type','')
    if typ=='projectflint:clay_grinding':
        inp=obj.get('ingredient')
        if isinstance(inp,str):
            edges.append((inp,out,n/obj.get('input_count',1),path.name))
    elif typ=='projectflint:hardwood_shaped':
        symbols=''.join(obj.get('pattern',[])).replace(' ','')
        ingredients=[obj.get('key',{}).get(c) for c in symbols]
        if ingredients and isinstance(ingredients[0],str) and all(x==ingredients[0] for x in ingredients):
            edges.append((ingredients[0],out,n/len(ingredients),path.name))

lookup=collections.defaultdict(list)
for a,b,mult,name in edges:
    lookup[(a,b)].append((mult,name))
checked=set()
for (a,b),links in lookup.items():
    if (b,a) not in lookup or (b,a) in checked: continue
    checked.add((a,b))
    for mult1,name1 in links:
        for mult2,name2 in lookup[(b,a)]:
            check(mult1*mult2 <= 1.000001,
                f'profitable two-way single-input recipe loop: {a} -> {b} -> {a}: {mult1*mult2:.3f}x ({name1}, {name2})')

# Compile and execute the actual Java rule on JDK21, not a copy in Python.
if shutil.which('javac') and shutil.which('java'):
    with tempfile.TemporaryDirectory(prefix='pf0322_retired_java_') as t:
        f=Path(t)/'Probe.java'
        probes=[('projectflint',x,True) for x in retired]
        probes += [('minecraft','oak_stripped_log_from_slabs',False),
                   ('projectflint','oak_slab_from_stripped_log',False),
                   ('projectflint','fireclay_block_from_slabs',False),
                   ('projectflint','hardwood_oak_slabs',False),
                   ('projectflint','grinded_wood_slab',False),
                   ('projectflint','hardwood_loose_dirt_slabs',False),
                   ('projectflint','hardwood_loose_mud_brick_slabs',False)]
        code=['public class Probe { public static void main(String[] args) {']
        for namespace,ident,want in probes:
            code.append('if (dev.projectflint.recipe.RetiredRecipePolicy.isRetired("%s", "%s") != %s) throw new AssertionError("%s:%s");' % (namespace,ident,str(want).lower(),namespace,ident))
        code.append('System.out.println("PASS: %d Java recipe-ID decisions");' % len(probes))
        code.append('}}')
        f.write_text('\n'.join(code),encoding='utf-8')
        r=subprocess.run(['javac','-d',t,str(JAVA/'recipe/RetiredRecipePolicy.java'),str(f)],capture_output=True,text=True)
        check(r.returncode==0, 'Java 21 policy compiles: '+r.stderr[:350])
        if r.returncode==0:
            r=subprocess.run(['java','-cp',t,'Probe'],capture_output=True,text=True)
            check(r.returncode==0, 'executed Java policy decisions: '+r.stdout+r.stderr[:350])
            if r.returncode==0:print(r.stdout.strip())
else:
    print('NOTE: javac/java unavailable, Java behavior check skipped (static checks still run)')

if failures:
    print(f'FAIL: {len(failures)}/{checks} checks failed')
    for failure in failures:print(' -',failure)
    sys.exit(1)
print(f'PASS: {checks} checks; no profitable 2-way JSON recipe loops detected. Full Gradle build/in-game playtest not performed.')
