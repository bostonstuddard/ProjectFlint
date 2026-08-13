@ECHO OFF
SETLOCAL EnableExtensions
CD /D "%~dp0"

IF /I NOT "%~1"=="/quiet" ECHO Applying Project Flint v0.28.41-a source cleanup...

FOR %%F IN (
  "APPLY_PROJECTFLINT_0_28_40_A.bat"
  "APPLY_PROJECTFLINT_0_28_39_A.bat"
  "APPLY_PROJECTFLINT_0_28_38_A.bat"
  "APPLY_PROJECTFLINT_0_28_37_A.bat"
  "APPLY_PROJECTFLINT_0_28_36_A.bat"
  "APPLY_PROJECTFLINT_0_28_35_A.bat"
  "APPLY_PROJECTFLINT_0_28_34_A.bat"
  "APPLY_PROJECTFLINT_0_28_33_A.bat"
  "APPLY_PROJECTFLINT_0_28_32_A.bat"
  "APPLY_PROJECTFLINT_0_28_31_A.bat"
  "APPLY_PROJECTFLINT_0_28_30_A.bat"
  "APPLY_PROJECTFLINT_0_28_29_A.bat"
  "APPLY_PROJECTFLINT_0_28_28_A.bat"
  "APPLY_PROJECTFLINT_0_28_27_A.bat"
  "APPLY_PROJECTFLINT_0_28_26_A.bat"
  "APPLY_PROJECTFLINT_0_28_25_A.bat"
  "APPLY_PROJECTFLINT_0_28_24_A.bat"
  "APPLY_PROJECTFLINT_0_28_23_A.bat"
  "APPLY_PROJECTFLINT_0_28_22_A.bat"
  "src\main\java\dev\projectflint\registry\ModFeatures.java"
  "src\main\java\dev\projectflint\world\feature\AquiferFeature.java"
  "src\main\resources\data\projectflint\worldgen\configured_feature\aquifer.json"
  "src\main\resources\data\projectflint\worldgen\placed_feature\aquifer_humid.json"
  "src\main\resources\data\projectflint\worldgen\placed_feature\aquifer_temperate.json"
  "src\main\resources\data\projectflint\worldgen\placed_feature\aquifer_arid.json"
  "src\main\resources\data\projectflint\recipe\crimson_slab_from_stripped_log.json"
  "src\main\resources\data\projectflint\recipe\crimson_stripped_log_from_slabs.json"
  "src\main\resources\data\projectflint\recipe\warped_slab_from_stripped_log.json"
  "src\main\resources\data\projectflint\recipe\warped_stripped_log_from_slabs.json"
) DO (
  IF EXIST "%%~F" DEL /F /Q "%%~F"
)

REM v0.28.41-a preserves the unified detailed advancement tree from v0.28.40-a.
FOR %%D IN (
  "src\main\resources\data\projectflint\advancement\00_tool_era"
  "src\main\resources\data\projectflint\advancement\01_clay_era"
  "src\main\resources\data\projectflint\advancement\02_wood_era"
  "src\main\resources\data\projectflint\advancement\tool_era"
  "src\main\resources\data\projectflint\advancement\clay_era"
  "src\main\resources\data\projectflint\advancement\wood_era"
) DO (
  IF EXIST "%%~D" RMDIR /S /Q "%%~D"
)

DEL /F /Q "javac.*.args" >NUL 2>&1
DEL /F /Q "javac-*.args" >NUL 2>&1

IF /I NOT "%~1"=="/quiet" ECHO Project Flint v0.28.41-a source cleanup complete.
ENDLOCAL
EXIT /B 0
