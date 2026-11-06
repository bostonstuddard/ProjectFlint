#!/usr/bin/env python3
"""Project Flint v0.31.60-b targeted source + resource checks.

A static validator does not replace building or client/multiplayer testing.
"""
import json
import re
from pathlib import Path
import struct
import zlib

root = Path(__file__).resolve().parents[1]
checks = 0

def check(assertion, label):
    global checks
    checks += 1
    if not assertion:
        raise AssertionError(label)

def read(relative):
    return (root / relative).read_text(encoding='utf-8')

def jsonfile(relative):
    return json.loads(read(relative))

check('mod_version=0.31.60-b' in read('gradle.properties'), 'version bumped')
check('org.gradle.daemon=false' in read('gradle.properties'), 'nonpersistent default')
script = read('build.bat')
check('APPLY_PROJECTFLINT_0_31_60_B.bat' in script, 'cleanup called')
check((root / 'APPLY_PROJECTFLINT_0_31_60_B.bat').exists(), 'cleanup exists')
check('APPLY_PROJECTFLINT_0_31_59_B.bat' in read('APPLY_PROJECTFLINT_0_31_60_B.bat'), 'cleanup retires previous script')
check('call gradlew.bat clean build --no-daemon --stacktrace' in script, 'wrapper runs without daemon')
check('call "%GRADLE_HOME%\\bin\\gradle.bat" clean build --no-daemon --stacktrace' in script, 'bootstrap without daemon')
check('Downloading Gradle' in script and ':run_bootstrap' in script and ':check_build' in script, 'bootstrap not truncated')
check('if errorlevel 1 goto :build_failed' in script and 'if not exist "build\\libs\\%EXPECTED_JAR%"' in script, 'failed builds blocked')
check('jar tf "build\\libs\\%EXPECTED_JAR%"' in script and 'PF-EMI-1.21.11.jar' in script, 'nested EMI gate retained')
check('copy /Y "build\\libs\\%EXPECTED_JAR%" "dist\\%EXPECTED_JAR%"' in script, 'publish JAR in dist')
check('for %%J in ("dist\\%ARCHIVES_BASE_NAME%-*.jar")' in script, 'remove older Project Flint JARs')
check('explorer.exe /select,"%CD%\\dist\\%EXPECTED_JAR%"' in script, 'open dist output')
check('dist/' in read('.gitignore') and '.gradle-bootstrap/' in read('.gitignore'), 'exclude generated output from git')

blocks = read('src/main/java/dev/projectflint/registry/ModBlocks.java')
check('import net.minecraft.world.level.block.GlassBlock;' in blocks, 'GlassBlock import')
check('new GlassBlock(' in blocks and 'Properties.ofFullCopy(Blocks.GLASS)' in blocks, 'light-passing glass properties')
check('new TintedGlassBlock(' not in blocks, 'no tinted glass block')
check('ChunkSectionLayer.TRANSLUCENT' in read('src/main/java/dev/projectflint/client/ProjectFlintClient.java'), 'translucent layer')
check('state.is(ModBlocks.PURE_MICA_INSULATED_GLASS)' in read('src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java'), 'glass recovery permission still present')
check('PURE_MICA_INSULATED_GLASS' in read('src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java'), 'glass recovery on server')

items = read('src/main/java/dev/projectflint/registry/ModItems.java')
check('PMI_SHEET = register("pmi_sheet", properties -> new Item(properties.stacksTo(64)))' in items, 'nonplaceable item registered')
check('output.accept(ModItems.PMI_SHEET);' in read('src/main/java/dev/projectflint/registry/ModCreativeTabs.java'), 'creative menu entry')
check(jsonfile('src/main/resources/assets/projectflint/lang/en_us.json')['item.projectflint.pmi_sheet']=='PMI Sheet', 'English item name')
check(jsonfile('src/main/resources/assets/projectflint/models/item/pmi_sheet.json')['parent']=='minecraft:item/generated', 'flat item model')
check(jsonfile('src/main/resources/assets/projectflint/items/pmi_sheet.json')['model']['model']=='projectflint:item/pmi_sheet', 'item definition')
def png_alpha_values(path):
    data=path.read_bytes()
    check(data[:8]==b'\x89PNG\r\n\x1a\n','PNG magic: '+path.name)
    width,height,depth,color,compression,filtering,interlace=struct.unpack('>IIBBBBB',data[16:29])
    check((width,height,depth,color,interlace)==(16,16,8,6,0),'16x16 noninterlaced RGBA: '+path.name)
    pieces=[]; pos=8
    while pos<len(data):
        length=struct.unpack('>I',data[pos:pos+4])[0]
        kind=data[pos+4:pos+8]
        if kind==b'IDAT':pieces.append(data[pos+8:pos+8+length])
        pos+=12+length
    raw=zlib.decompress(b''.join(pieces))
    stride=width*4
    previous=bytearray(stride)
    alphas=[]
    for row in range(height):
        kind=raw[row*(stride+1)]
        filtered=raw[row*(stride+1)+1:(row+1)*(stride+1)]
        unfiltered=bytearray(stride)
        for i,v in enumerate(filtered):
            left=unfiltered[i-4] if i>=4 else 0
            up=previous[i]
            ul=previous[i-4] if i>=4 else 0
            if kind==0: predictor=0
            elif kind==1: predictor=left
            elif kind==2: predictor=up
            elif kind==3: predictor=(left+up)//2
            elif kind==4:
                p=left+up-ul
                a,b,c=abs(p-left),abs(p-up),abs(p-ul)
                predictor=left if a<=b and a<=c else up if b<=c else ul
            else:raise ValueError('PNG filter')
            unfiltered[i]=(v+predictor)&255
        alphas.extend(unfiltered[3::4])
        previous=unfiltered
    return alphas
for rel in ('src/main/resources/assets/projectflint/textures/item/pmi_sheet.png','src/main/resources/assets/projectflint/textures/block/pure_mica_insulated_glass.png'):
    alpha=png_alpha_values(root/rel)
    check(max(alpha)>0,'visible pixels '+rel)
check(min(png_alpha_values(root/'src/main/resources/assets/projectflint/textures/block/pure_mica_insulated_glass.png'))<60,'transparent glass core alpha')

for recipe_id,typ in [('personal_pmi_sheets','projectflint:durable_crafting'),('hardwood_pmi_sheets','projectflint:hardwood_durable')]:
    r=jsonfile('src/main/resources/data/projectflint/recipe/'+recipe_id+'.json')
    check(r['type']==typ, recipe_id+' recipe type')
    check(r['ingredients']==['projectflint:pure_mica_insulated_glass'],recipe_id+' ingredient')
    check(r['tool']=='projectflint:bone_knife',recipe_id+' knife wears')
    check(r['result']=={'id':'projectflint:pmi_sheet','count':2},recipe_id+' sheet output')
check('personal_pmi_sheets' in read('src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java'), 'G/K personal guide record')
check('hardwood_pmi_sheets' in read('src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java'), 'K hardwood guide record')
text=read('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')
check('Combine one block with a Bone Knife' in text and 'sunlight' in text, 'player-facing G content')
for rel in ('docs/releases/0.31.60-b.md','docs/decisions/0019-light-passing-pmi-glass-sheets-and-nonpersistent-gradle-builds.md','README.md','CHANGELOG.md','docs/CURRENT_SYSTEMS.md','docs/RELEASE_PROCESS.md','docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md','docs/TEXTURE_PROVENANCE.md'):
    check('0.31.60-b' in read(rel), 'docs updated '+rel)
for p in (root/'src/main/resources').rglob('*.json'):
    json.loads(p.read_text(encoding='utf-8'))
    checks += 1
print(f'PASS: {checks} targeted checks including source, assets, recipes and all resource JSONs')
