#!/usr/bin/env python3
"""Targeted source regression tests for ProjectFlint v0.31.68-b."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
reads = lambda rel: (ROOT / rel).read_text(encoding='utf-8')
checks = 0

def require(value, label):
    global checks
    assert value, label
    checks += 1

blast = reads('src/main/java/dev/projectflint/block/entity/DynamiteChargeBlockEntity.java')
ore = reads('src/main/java/dev/projectflint/system/OreMiningRules.java')
basin = reads('src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java')
field_guide = reads('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')
recipe_guide = reads('src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java')
recipe = json.loads(reads('src/main/resources/data/projectflint/recipe/hardwood_dynamite.json'))

for key, expected in {'ORE_RADIUS = 1.75': 1, 'ROCK_RADIUS = 1.25': 1,
                      'MAX_ORE_BLOCKS = 2': 1, 'MAX_ROCK_BLOCKS = 1': 1,
                      'ROCK_BREAK_CHANCE = 0.50': 1, 'BASIN_YIELD_MULTIPLIER = 1.20F': 1,
                      'ENTITY_RADIUS = 1.75': 1}.items():
    require(blast.count(key) == expected, key)
require('RADIUS = 2.75' not in blast and '1.5F' not in blast, 'old dynamite radius/odds')
require('Collections.swap(positions' in blast, 'random selection ordering')
require('oresBroken >= MAX_ORE_BLOCKS' in blast, 'ore break cap')
require('rocksBroken >= MAX_ROCK_BLOCKS' in blast, 'rock break cap')
require('server.getRandom().nextDouble() >= ROCK_BREAK_CHANCE' in blast, 'stone break roll')
require('processWithMultiplier(' in blast, 'Basin material rolls shared')
for block in ['STONE', 'COBBLESTONE', 'ANDESITE', 'DIORITE', 'GRANITE', 'IRON_ORE', 'COPPER_ORE', 'COAL_ORE']:
    require('state.is(Blocks.'+block+')' in blast, 'blast whitelist '+block)
require('state.is(Blocks.DEEPSLATE)' not in blast, 'deepslate not blastable')
require('BasinMaterialProcessingSystem.processWithMultiplier' in blast, 'centralized source of loot')
require('Math.floor(expected)' in basin and 'expected - guaranteed' in basin, 'fractional overroll maintained')
require('processWithMultiplier(input, random, 1.0F)' in basin, 'normal Basin odds unchanged')
require('1.2x' in basin, 'Basin comment updated')

require(recipe['type'] == 'projectflint:hardwood_shaped', 'Hardwood-only crafting')
require(recipe['pattern'] == ['PFP', 'PRP', 'CCC'], 'copper entire bottom')
require(recipe['key'] == {'P':'projectflint:black_powder', 'F':'projectflint:fuse',
                         'R':'minecraft:red_dye', 'C':'projectflint:copper_dust'}, 'recipe assignments')
require(recipe['result'] == {'id':'projectflint:dynamite','count':1}, 'result remains 1')
require('projectflint:copper_dust#3' in recipe_guide, 'K-key recipe guide three copper')
require('PFP/PRP/CCC' in recipe_guide, 'K-key guide correct pattern')
require('three Copper Dust' in field_guide, 'G-key guide correct recipe')
require('20% higher' in field_guide, 'G-key guide blast odds')

for block in ['POINTED_DRIPSTONE','DRIPSTONE_BLOCK', 'TUFF', 'CALCITE',
              'BASALT','BLACKSTONE','AMETHYST_BLOCK','BUDDING_AMETHYST']:
    require('state.is(Blocks.'+block+')' in ore, 'advanced geology '+block)
require('|| isAdvancedGeology(state)' in ore, 'central restrictions')
require('if (isAdvancedGeology(state)) return canMineAdvancedRock' in ore, 'drop protection')
require('if (isAdvancedGeology(state))\n' in ore, 'mining progress protection')
require('isUpgradedPickaxe(held) && isValidVanillaPickaxe' in ore, 'later-tier mining')
require('canPrimitiveStoneWork(state, held)' in ore, 'Crude Stone Hammer exemption')
require('isForbiddenDeepslate(state)) return 0.0F;' in ore, 'deepslate remains locked')
require('mod_version=0.31.68-b' in reads('gradle.properties'), 'version')
require('APPLY_PROJECTFLINT_0_31_68_B.bat' in reads('build.bat'), 'build cleanup callback')
require('APPLY_PROJECTFLINT_0_31_67_B.bat' in reads('APPLY_PROJECTFLINT_0_31_68_B.bat'), 'remove previous apply script')

for path in (ROOT/'src/main/resources').rglob('*.json'):
    try: json.loads(path.read_text(encoding='utf-8'))
    except Exception as err: raise AssertionError(f'Invalid JSON: {path}: {err}') from err
    checks += 1
print(f'PASS: {checks} v0.31.68-b targeted checks (including resource JSON).')
