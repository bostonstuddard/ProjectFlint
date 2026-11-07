from pathlib import Path

root = Path(__file__).resolve().parents[1]
assert not (root / 'src/main/resources/data/projectflint/recipe/primitive_wooden_sheet_from_stripped_log.json').exists()
assert not (root / 'src/main/resources/data/projectflint/tags/item/primitive_stripped_logs_for_sheets.json').exists()
personal = (root / 'src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java').read_text()
field = (root / 'src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java').read_text()
assert 'primitive_wooden_sheet_from_stripped_log' not in personal
assert 'Cut a stripped Log with a Bone Knife to get one Wooden Sheet' not in field
assert 'Wooden Grinder with one Wooden Beam and a Flint File' in field
grinder = root / 'src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java'
if grinder.exists():
    s = grinder.read_text()
    assert 'Ingredient.of(ModItems.WOODEN_BEAM)' in s
    assert 'new ItemStack(ModItems.WOODEN_SHEET, 4)' in s
    assert 'Ingredient.of(ModItems.FLINT_FILE)' in s
print('v0.31.67-b wooden-sheet progression checks passed')
