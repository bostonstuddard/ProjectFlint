from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
checks=0
def ck(test,label):
 global checks
 if not test: raise AssertionError(label)
 checks+=1

def read(p): return (root/p).read_text(encoding='utf-8')
blocks=read('src/main/java/dev/projectflint/registry/ModBlocks.java')
ck('import net.minecraft.world.level.block.TransparentBlock;' in blocks, 'mapped glass class import')
ck('new TransparentBlock(' in blocks and 'Properties.ofFullCopy(Blocks.GLASS)' in blocks, 'glass implementation/properties')
ck('import net.minecraft.world.level.block.GlassBlock;' not in blocks, 'old invalid import removed')
ck('new TintedGlassBlock(' not in blocks, 'not tinted/light blocking')
ck(not (root/'src/main/resources/data/projectflint/recipe/personal_pmi_sheets.json').exists(), 'obsolete personal recipe absent')
recipe=json.loads(read('src/main/resources/data/projectflint/recipe/hardwood_pmi_sheets.json'))
ck(recipe['type']=='projectflint:hardwood_durable','Hardwood-only recipe type')
ck(recipe['ingredients']==['projectflint:pure_mica_insulated_glass'],'Glass input')
ck(recipe['tool']=='projectflint:bone_knife','durable Bone Knife input')
ck(recipe['result']=={'id':'projectflint:pmi_sheet','count':2},'2 sheets per glass')
ck('personal_pmi_sheets' not in read('src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java'), 'personal guide removed')
ck('hardwood_pmi_sheets' in read('src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java'), 'hardwood guide retained')
field=read('src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java')
ck('At the Hardwood Crafting Table, combine one glass block with a Bone Knife' in field,'field guide corrected')
ck('personal crafting or the Hardwood Table' not in field,'no old guide text')
ck('mod_version=0.31.61-b' in read('gradle.properties'),'version')
bat=read('build.bat')
ck('APPLY_PROJECTFLINT_0_31_61_B.bat' in bat and '0.31.61-b' in bat,'build version/cleanup')
ck(r'src\main\resources\data\projectflint\recipe\personal_pmi_sheets.json' in read('APPLY_PROJECTFLINT_0_31_61_B.bat'),'cleanup deletes legacy recipe')
ck('dist' in bat and '--no-daemon' in bat,'build delivery preserved')
ck('0020-hardwood-only-pmi-sheets-and-mojang-glass-mappings.md' in ' '.join(p.name for p in (root/'docs/decisions').iterdir()),'ADR present')
ck((root/'docs/releases/0.31.61-b.md').exists(),'release notes')
for p in (root/'src/main/resources').rglob('*.json'):
 json.loads(p.read_text(encoding='utf-8'))
 checks+=1
print('PASS:',checks,'source and resource checks')
