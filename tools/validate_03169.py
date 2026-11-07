#!/usr/bin/env python3
"""Focused regression assertions for 0.31.69-b, independent of Minecraft runtime."""
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
REC=ROOT/'src/main/resources/data/projectflint/recipe'
for filename,bucket in [('hardwood_hardwood_from_wood_dust.json','projectflint:clay_water_bucket'),('hardwood_hardwood_from_wood_dust_vanilla_bucket.json','minecraft:water_bucket')]:
 r=json.loads((REC/filename).read_text())
 assert r['type']=='projectflint:hardwood_shaped' and r['pattern']==['DDD','DWD','TTT']
 assert r['key']=={'D':'projectflint:wood_dust','W':bucket,'T':'projectflint:tannin'}
 assert r['result']=={'id':'projectflint:hardwood','count':1}
old=json.loads((REC/'hardwood.json').read_text());assert old['type']=='minecraft:crafting_shapeless'
items=(ROOT/'src/main/java/dev/projectflint/registry/ModItems.java').read_text()
assert 'craftRemainder(CLAY_BUCKET)' in items
s=(ROOT/'src/main/java/dev/projectflint/block/entity/DynamiteChargeBlockEntity.java').read_text()
for token in ['ORE_RADIUS = 3.0','ROCK_RADIUS = 2.25','ENTITY_RADIUS = 1.75','MAX_ORE_BLOCKS = 4','MAX_ROCK_BLOCKS = 2','ROCK_BREAK_CHANCE = 0.65','BASIN_YIELD_MULTIPLIER = 1.25F','FUSE_TICKS = 60','blastableOre(original)','blastableRock(original)']:
 assert token in s,token
assert 'DEEPSLATE' not in s
assert 'ENTITY_RADIUS = 1.75' in s
assert 'new AABB(pos).inflate(ENTITY_RADIUS)' in s
ref=(ROOT/'src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java').read_text()
assert ref.count('add(all, "hardwood_hardwood_from_wood_dust"')==1
f=(ROOT/'src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java').read_text()
assert 'five Wood Dust' in f and 'up to 3 blocks' in f
assert 'mod_version=0.31.69-b' in (ROOT/'gradle.properties').read_text()
assert 'APPLY_PROJECTFLINT_0_31_69_B.bat' in (ROOT/'build.bat').read_text()
assert (ROOT/'docs/decisions/0028-hardwood-table-wood-dust-recipe-and-controlled-dynamite-radius.md').exists()
# The client, attack event, server pre-break and completed-break handlers
# must all share the Fireclay Brick tool gate for vanilla mud blocks.
for f in ['system/OreMiningRules.java','system/PrimitiveInteractionSystem.java',
          'mixin/ServerPlayerGameModeMixin.java','system/WoodenToolTargets.java']:
    source=(ROOT/'src/main/java/dev/projectflint'/f).read_text()
    assert 'mudMasonry' in source, f
rules=(ROOT/'src/main/java/dev/projectflint/system/OreMiningRules.java').read_text()
assert 'ToolCapabilities.canMineBrick(held)' in rules
assert '0.025F' in rules
assert 'state.is(Blocks.PACKED_MUD)' in (ROOT/'src/main/java/dev/projectflint/system/WoodenToolTargets.java').read_text()
count=0
for p in (ROOT/'src/main/resources').rglob('*.json'):
 json.loads(p.read_text());count+=1
print('PASS: v0.31.69-b recipe, blast and guide assertions; validated',count,'resource JSON files')
