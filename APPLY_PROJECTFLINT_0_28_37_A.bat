@ECHO OFF
SETLOCAL EnableExtensions
CD /D "%~dp0"

IF /I NOT "%~1"=="/quiet" ECHO Applying Project Flint v0.28.37-a source cleanup...

FOR %%F IN (
  "APPLY_PROJECTFLINT_0_28_36_A.bat"
  "APPLY_PROJECTFLINT_0_28_35_A.bat"
  "APPLY_PROJECTFLINT_0_28_34_A.bat"
  "APPLY_PROJECTFLINT_0_28_33_A.bat"
  "APPLY_PROJECTFLINT_0_28_32_A.bat"
  "APPLY_PROJECTFLINT_0_28_31_A.bat"
  "APPLY_PROJECTFLINT_0_28_30_A.bat"
  "APPLY_PROJECTFLINT_0_28_29_A.bat"
  "APPLY_PROJECTFLINT_0_28_28_A.bat"
  "src\main\java\dev\projectflint\registry\ModFeatures.java"
  "src\main\java\dev\projectflint\world\feature\AquiferFeature.java"
  "src\main\resources\data\projectflint\worldgen\configured_feature\aquifer.json"
  "src\main\resources\data\projectflint\worldgen\placed_feature\aquifer_humid.json"
  "src\main\resources\data\projectflint\worldgen\placed_feature\aquifer_temperate.json"
  "src\main\resources\data\projectflint\worldgen\placed_feature\aquifer_arid.json"
  "src\main\java\dev\projectflint\client\ProjectFlintHardcoreDarkness.java"
  "src\main\java\dev\projectflint\client\ProjectFlintNightFogEnvironment.java"
  "src\main\java\dev\projectflint\mixin\MobEffectFogEnvironmentMixin.java"
  "src\main\java\dev\projectflint\mixin\BlindnessFogEnvironmentMixin.java"
  "src\main\java\dev\projectflint\mixin\FogRendererMixin.java"
  "src\main\java\dev\projectflint\mixin\ItemEntityMixin.java"
  "src\main\java\dev\projectflint\mixin\ServerPlayerMixin.java"
  "src\main\java\dev\projectflint\system\PrimitiveCooking.java"
  "APPLY_PROJECTFLINT_0_28_26_A.bat"
  "APPLY_PROJECTFLINT_0_28_25_A.bat"
  "APPLY_PROJECTFLINT_0_28_24_A.bat"
  "APPLY_PROJECTFLINT_0_28_23_A.bat"
  "APPLY_PROJECTFLINT_0_28_22_A.bat"
  "APPLY_PROJECTFLINT_0_28_21_A.bat"
  "APPLY_0_26_A_TEXTURE_DEDUP_REMOVALS_FIXED.bat"
  "APPLY_PROJECTFLINT_0_26_6_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_1_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_2_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_3_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_4_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_5_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_6_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_7_A.bat"
  "APPLY_PROJECTFLINT_0_28_7_8_A.bat"
  "DELETE_THESE_FILES.txt"
  "PATCH_MANIFEST_0.25.1.md"
  "MEAT_IMPORT_VALIDATION_v0.28.0-a.txt"
  "TEXTURE_IMPORT_VERIFICATION.txt"
  "TEXTURE_RENDER_FIX_VERIFICATION_v0.27.2-a.txt"
  "TEXTURE_SYNC_VERIFICATION.txt"
  "VALIDATION_0.27.0-a.txt"
  "VALIDATION_0.27.4-a.txt"
  "VALIDATION_0.27.5-a.txt"
  "VALIDATION_v0.28.1-a.txt"
  "src\main\resources\data\projectflint\recipe\crimson_slab_from_stripped_log.json"
  "src\main\resources\data\projectflint\recipe\crimson_stripped_log_from_slabs.json"
  "src\main\resources\data\projectflint\recipe\warped_slab_from_stripped_log.json"
  "src\main\resources\data\projectflint\recipe\warped_stripped_log_from_slabs.json"
  "src\main\resources\data\projectflint\advancement\root.json"
  "src\main\resources\data\projectflint\advancement\tool_era\root.json"
  "src\main\resources\data\projectflint\advancement\clay_era\root.json"
  "src\main\resources\data\projectflint\advancement\wood_era\root.json"
  "src\main\resources\data\projectflint\advancement\charred_twig.json"
) DO (
  IF EXIST "%%~F" DEL /F /Q "%%~F"
)

DEL /F /Q "javac.*.args" >NUL 2>&1
DEL /F /Q "javac-*.args" >NUL 2>&1

IF /I NOT "%~1"=="/quiet" ECHO Project Flint v0.28.37-a source cleanup complete.
ENDLOCAL
EXIT /B 0
