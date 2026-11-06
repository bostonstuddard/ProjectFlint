#!/usr/bin/env python3
"""Static regression gates for Project Flint v0.31.62-b opening Stone work."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
def src(p):return (root/p).read_text(encoding='utf-8')
def ck(x,msg):
    global checks
    if not x:raise AssertionError(msg)
    checks+=1
checks=0
ore=src('src/main/java/dev/projectflint/system/OreMiningRules.java')
progress=src('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java')
primitive=src('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
server=src('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
field=src('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')
# Positive whitelist and exact block target (no broad tier escalation)
operation=ore.split('public static boolean canPrimitiveStoneWork(',1)[1].split('public static boolean isVanillaPickaxe(',1)[0]
ck('state.is(Blocks.STONE)' in operation, 'natural Stone selected')
for key in ('CRUDE_STONE_HAMMER','CRUDE_MULTITOOL','FLINT_MULTITOOL','ADVANCED_FLINT_MULTITOOL'):
    ck('held.is(ModItems.'+key+')' in operation,key+' Stone conversion')
for key in ('HARDWOOD_HAMMER','HARDWOOD_PICKAXE','WOODEN_PICKAXE'):
    ck('held.is(ModItems.'+key+')' not in operation,key+' excluded from primitive conversion')
ck('return isWoodenTier(held);' not in operation, 'no blanket wooden mining permission')
ck('canPrimitiveStoneWork(state, held)\n            || isEarlyRockTool' in ore, 'Stone permission before regular mining tiers')
progressBranch=ore.split('public static float progress(',1)[1]
ck(progressBranch.index('canPrimitiveStoneWork(state, held)')<progressBranch.index('isEarlyRockTool(state, held)'), 'early hammer progress precedes pickaxe progress')
ck('GameplayTuning.CRUDE_HAMMER_STONE_PROGRESS_PER_TICK' in progressBranch, 'hammer original speed')
ck('ToolCapabilities.hammerSpeedMultiplier(held)' in progressBranch,'multitool speed preserved')
ck(progress.index('OreMiningRules.isRestricted(state)')<progress.index('if (state.is(Blocks.STONE)') if 'if (state.is(Blocks.STONE)' in progress else True, 'client applies shared mining rule first')
ck('OreMiningRules.progress(state, held, vanillaProgress)' in progress,'client mining progress uses shared rule')
attack=primitive.split('private static InteractionResult onAttackBlock(',1)[1].split('private static InteractionResult onUseBlock(',1)[0]
before=primitive.split('private static boolean beforeBlockBreak(',1)[1].split('private static boolean isLimestoneFamily(',1)[0]
ck(attack.index('canPrimitiveStoneWork(state, held)')<attack.index('return !OreMiningRules.isStoneFamily'), 'attack permission before denial')
ck(before.index('canPrimitiveStoneWork(state, held)')<before.index('return !OreMiningRules.isStoneFamily'), 'pre-break permission before denial')
block=server.split('private void projectflint$completePrimitiveOperation(',1)[1].split('if (PlayerPlacedBlockLedger.isHandRecoverableDecoration',1)[0]
ck(block.index('if (OreMiningRules.canPrimitiveStoneWork(state, held))') < block.index('if (!OreMiningRules.canGetDrops(state, held))'), 'completion before mining deny')
ck(block.index('if (OreMiningRules.canPrimitiveStoneWork(state, held))') < block.index('if (OreMiningRules.isWoodenTier(held))'), 'completion before pickaxe result')
ck('level.setBlockAndUpdate(pos, ModBlocks.CRUSHED_STONE.defaultBlockState())' in block,'Stone replaced in world')
ck('for (int i = 0; i < 4; i++) projectflint$dropAtBlock(pos, new ItemStack(ModItems.ROCK))' in block,'four Rocks returned')
ck('held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND)' in block,'tool durability consumed')
ck('ModAdvancements.award(player, "crushed_stone", "crushed_stone")' in block,'original milestone kept')
ck(server.count('level.setBlockAndUpdate(pos, ModBlocks.CRUSHED_STONE.defaultBlockState())')==1,'no duplicate Stone conversion handlers')
ck('OreMiningRules.isForbiddenDeepslate(state)' in ore or 'isForbiddenDeepslate(state)' in ore,'deepslate block preserved')
ck('if (isForbiddenDeepslate(state)) return false;' in ore,'deepslate drops blocked')
ck('if (isForbiddenDeepslate(state)) return 0.0F;' in ore,'deepslate progress blocked')
ck('canPrimitiveStoneWork(state, held)' not in ore.split('public static boolean isWoodenTier(',1)[1].split('private static boolean isValidVanillaPickaxe',1)[0], 'no hammer added to wooden mining')
ck('The Tanned Utility Vest helps clear vegetation' in field,'vest player guide unchanged')
ck('Use a Crude Stone Hammer on natural Stone' in field,'guide describes repaired tool')
ck('four Rocks' in field,'guide describes material output')
ck('mod_version=0.31.62-b' in src('gradle.properties'),'version number')
ck('APPLY_PROJECTFLINT_0_31_62_B.bat' in src('build.bat'),'build invokes cleanup')
ck('--no-daemon' in src('build.bat') and 'dist' in src('build.bat'),'distribution preserved')
ck((root/'docs/decisions/0021-primitive-stone-working-exception-before-mining-gates.md').exists(),'ADR exists')
ck((root/'docs/releases/0.31.62-b.md').exists(),'release document exists')
for jsonfile in (root/'src/main/resources').rglob('*.json'):
    json.loads(jsonfile.read_text(encoding='utf-8'))
    checks+=1
print('PASS:',checks,'static checks (source, resources, release files)')
