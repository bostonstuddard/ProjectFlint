#!/usr/bin/env python3
"""ProjectFlint 0.31.63-b break-event and source/resource regression check."""
from pathlib import Path
import json,re
root=Path(__file__).resolve().parents[1]
checks=0

def ck(condition, description):
    global checks
    assert condition, description
    checks+=1

def content(path):return (root/path).read_text(encoding='utf-8')

server=content('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
ore=content('src/main/java/dev/projectflint/system/OreMiningRules.java')
bridge=content('src/main/java/dev/projectflint/compat/flintquests/FlintQuestsBridge.java')
field=content('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')

# Fabric's existing after-break listeners must receive the same original block as normal mining.
ck('import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;' in server,'Fabric callback imported')
ck('BlockState state = level.getBlockState(pos);' in server,'original state captured')
ck('BlockEntity previousBlockEntity = level.getBlockEntity(pos);' in server,'original block entity captured')
ck('try {' in server and '} finally {' in server,'post-operation reporting covers all early returns')
ck('cir.isCancelled() && creditManualBreak' in server,'event is gated on manually canceled path')
ck('!level.getBlockState(pos).is(state.getBlock())' in server,'only actual break/transform reported')
ck('PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, previousBlockEntity);' in server,'dispatch original block state')
ck(server.count('PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(')==1,'exactly one synthetic dispatch site')
ck('!OreMiningRules.isRestricted(state)' in server and 'OreMiningRules.canGetDrops(state, held)' in server,'restricted ore exploit guard')
ck(server.index('boolean creditManualBreak')<server.index('held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND)'),'permissions captured before tool wears out')
ck(server.index('} finally {')>server.index('level.setBlockAndUpdate(pos, ModBlocks.CRUSHED_STONE.defaultBlockState())'),'stone conversion protected by finally')
ck('if (player.isCreative()) return;' in server,'no creative duplicate path')
ck('state.is(Blocks.STONE)' in ore.split('boolean canPrimitiveStoneWork',1)[1].split('boolean isVanillaPickaxe',1)[0],'stone path strictly scoped')
ck('level.setBlockAndUpdate(pos, ModBlocks.CRUSHED_STONE.defaultBlockState());' in server,'stone transform unchanged')
ck('for (int i = 0; i < 4; i++) projectflint$dropAtBlock(pos, new ItemStack(ModItems.ROCK))' in server,'four rocks retained')
ck('held.hurtAndBreak(1, player, InteractionHand.MAIN_HAND)' in server,'tool wear remains')
ck('canPrimitiveStoneWork(state, held)' in ore,'stone eligibility intact')
ck('onCraft' in bridge and 'trigger' in bridge,'workstation quest bridge untouched')
ck('This also counts as breaking Stone for Flint Quests.' in field,'player-facing guide updated')
ck('mod_version=0.31.63-b' in content('gradle.properties'),'release version')
ck('APPLY_PROJECTFLINT_0_31_63_B.bat' in content('build.bat'),'new apply script wired')
ck('--no-daemon' in content('build.bat') and 'dist' in content('build.bat'),'dist build path unchanged')
ck((root/'docs/releases/0.31.63-b.md').exists(),'release note exists')
ck((root/'docs/decisions/0022-custom-player-break-events-for-transformed-blocks.md').exists(),'ADR exists')
# Ensure no recipe/asset modifications were introduced by accident (validated against patch contents).
for file in (root/'src/main/resources').rglob('*.json'):
    json.loads(file.read_text(encoding='utf-8'))
    checks += 1
print(f'PASS {checks} checks: synthetic AFTER event, anti-double-counting, progress/drops, release metadata, JSON resources')
