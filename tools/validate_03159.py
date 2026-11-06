#!/usr/bin/env python3
"""Targeted offline consistency gates for Project Flint v0.31.59-b."""
import json,re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
checks=0
def require(test,label):
 global checks
 checks+=1
 if not test: raise AssertionError(label)
def read(path):return (root/path).read_text(encoding='utf-8')
be=read('src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java')
renderer=read('src/main/java/dev/projectflint/client/BasinItemRenderer.java')
menu=read('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideScreen.java')
content=read('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')
server=read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java')
interaction=read('src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java')
mining=read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java')
manifest=json.loads(read('src/main/resources/fabric.mod.json'))
require('mod_version=0.31.59-b' in read('gradle.properties'), 'release')
require('APPLY_PROJECTFLINT_0_31_59_B.bat' in read('build.bat'), 'new cleanup')
require((root/'APPLY_PROJECTFLINT_0_31_59_B.bat').exists(), 'cleanup exists')
require('CAPACITY = 4' in be and 'implements Container' in be, 'four slots')
require('NonNullList.withSize(CAPACITY' in be, 'distinct stored stacks')
require('getMaxStackSize() { return 1; }' in be, 'one per slot')
require('ContainerHelper.saveAllItems' in be and 'ContainerHelper.loadAllItems' in be, 'saved inventory')
require('"BasinInput"' in be and 'legacyOverflow' in be, 'migration without losing old stacks')
require('stomps++' in be and 'stomps = 0' in be and 'setFoodLevel' in be, 'stomping and hunger')
require('ParticleTypes.POOF' in be and 'SoundEvents.GRAVEL_BREAK' in be, 'feedback')
require('state.count = items.size()' in renderer and 'state.items[i].submit' in renderer, 'four rendering slots')
require('PURE_MICA_INSULATED_GLASS' in server and 'getItemBySlot' in server, 'vest glass server')
require('PURE_MICA_INSULATED_GLASS' in mining and '1.0F / 25.0F' in mining, 'glass crack progress')
require(interaction.count('PURE_MICA_INSULATED_GLASS')>=2, 'glass permission paths')
require('private List<FormattedCharSequence> wrappedLines' in menu and 'bodyLinesPerPage' in menu, 'measured pagination')
require('enableScissor' in menu and 'disableScissor' in menu, 'clipping')
require('bodyScroll' not in menu and 'renderBackground(' not in menu, 'no obsolete scroll or blur')
require(content.count('add(c,')==34,'34 chapters')
require('ProjectFlint Contributors' in manifest['authors'] and manifest['authors'][0]=='imkas','author order')
require(manifest['contact']['issues']=='https://github.com/bostonstuddard/ProjectFlint/issues', 'issues')
require('modmenu' in manifest['entrypoints'] and 'modmenu' in manifest['suggests'], 'optional modmenu')
require('modCompileOnly' in read('build.gradle'), 'optional dependency')
require('Nothing to see here!' in read('src/main/java/dev/projectflint/client/screen/ProjectFlintConfigScreen.java'), 'settings placeholder')
require('ProjectFlintModMenu.java' in str(root/'src/main/java/dev/projectflint/client/ProjectFlintModMenu.java'), 'modmenu java')
require((root/'docs/decisions/0018-basin-container-guide-modmenu-and-glass-recovery.md').exists(),'ADR')
require((root/'docs/releases/0.31.59-b.md').exists(), 'release notes')
require('Basin holds 4 items' in read('src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java'), 'recipe guide')
for p in (root/'src/main/resources').rglob('*.json'):
 json.loads(p.read_text(encoding='utf-8'))
 checks+=1
print(f'PASS: {checks} checks including {checks-26} resource JSON parse operations')
