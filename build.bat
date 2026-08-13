@echo off
setlocal EnableExtensions
cd /d "%~dp0"

REM ============================================================
REM Project Flint v0.28.41-a builder
REM - keeps woven chest/dispenser-style storage aligned
REM - syncs placed/crafting sprites before each build
REM - mirrors raw leather block texture for backpack rendering
REM - fixes Wooden Grinder dual-input recipe preview and atomic ingredient consumption
REM - preserves grinder recipe selection after successful crafts
REM - keeps hardcore night darkness lightmap-based so distant block lights remain visible
REM - keeps worldgen Loose Rock rendering independent from held Rock items
REM - preserves v0.28.7.4-a forage/berry ecology and distinct bear model layers
REM - preserves v0.28.7.5-a stack-safe knapping and crude plant torches
REM - removes the artificial night fog so distant block-light landmarks remain visible
REM - keeps carried crude plant torches lit under cover; sky access is only required after placement
REM - preserves the four-block Wattle-and-Daub structural construction tier
REM - Project Flint v0.28.19-a adds Wattled Daub flooring/window/door construction and placement fixes
REM - Project Flint v0.28.21-a adds the reusable Flint Torch wall-light progression and strict Wattled Daub mounting safety
REM - Project Flint v0.28.25-a keeps the guide station-accurate, fits the Woven Chest cleanly, and uses a proper inset book control
REM - Project Flint v0.28.26-a fixes horse resources, vision bypass, advancement guidance, and grinder wood filtering
REM - Project Flint v0.28.27-a replaces physical aquifer worldgen with a seed-deterministic virtual groundwater field
REM - Project Flint v0.28.29-a restores granular era advancement guidance and stable Tool -> Clay -> Wood tab ordering
REM - Project Flint v0.28.30-a begins Wood -> Water progression with the Sanding Station and Lumber Processor
REM - Project Flint v0.28.31-a fixes staged-log Lumber Processor mining and output selection
REM - Project Flint v0.28.32-a fixes client/server Lumber Processor chop prediction so full Water Pots no longer block mining
REM - Project Flint v0.28.33-a adds full plant/foliage coverage to Flint Plant Cutters
REM - Project Flint v0.28.34-a corrects Plant Cutters to Advanced Flint Multitool plant-tier speed/yields while preserving Plant Tool Bindings
REM - Project Flint v0.28.35-a rebuilds the Sanding Station GUI around a clean segmented sanding-reserve display
REM - Project Flint v0.28.36-a keeps Sugarcane Rolls exclusive to Flint Plant Cutters; Advanced Flint Multitools harvest normal cane
REM - Project Flint v0.28.37-a applies owner-supplied Sanding Station, Water Pot, and Lumber Processor textures
REM - Project Flint v0.28.39-a restores one detailed advancement tree and softens opening RNG/darkness timing
REM - Project Flint v0.28.41-a restores Crude Multitool leaf harvesting so the branch/twig progression path cannot dead-end
REM ============================================================

IF EXIST "APPLY_PROJECTFLINT_0_28_41_A.bat" CALL "APPLY_PROJECTFLINT_0_28_41_A.bat" /quiet

if not exist "src\main\resources\assets\projectflint\textures\block\placed" mkdir "src\main\resources\assets\projectflint\textures\block\placed"
copy /y "src\main\resources\assets\projectflint\textures\item\branch.png" "src\main\resources\assets\projectflint\textures\block\placed\branch.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\burnt_branch.png" "src\main\resources\assets\projectflint\textures\block\placed\burnt_branch.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\burnt_log.png" "src\main\resources\assets\projectflint\textures\block\placed\burnt_log.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\burnt_twig.png" "src\main\resources\assets\projectflint\textures\block\placed\burnt_twig.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\crude_plant_string.png" "src\main\resources\assets\projectflint\textures\block\placed\crude_plant_string.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\crude_reed.png" "src\main\resources\assets\projectflint\textures\block\placed\crude_reed.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\filed_bone.png" "src\main\resources\assets\projectflint\textures\block\placed\filed_bone.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\flint_axe_pieces.png" "src\main\resources\assets\projectflint\textures\block\placed\flint_axe_pieces.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\flint_cutter_piece.png" "src\main\resources\assets\projectflint\textures\block\placed\flint_cutter_piece.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\lightly_burnt_twig.png" "src\main\resources\assets\projectflint\textures\block\placed\lightly_burnt_twig.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\plant_fiber.png" "src\main\resources\assets\projectflint\textures\block\placed\plant_fiber.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\plant_tool_binding.png" "src\main\resources\assets\projectflint\textures\block\placed\plant_tool_binding.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\refined_flint_multitool_piece.png" "src\main\resources\assets\projectflint\textures\block\placed\refined_flint_multitool_piece.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\rock.png" "src\main\resources\assets\projectflint\textures\block\placed\rock.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\twig.png" "src\main\resources\assets\projectflint\textures\block\placed\twig.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\very_refined_stone.png" "src\main\resources\assets\projectflint\textures\block\placed\very_refined_stone.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\woven_fan.png" "src\main\resources\assets\projectflint\textures\block\placed\woven_fan.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\woven_wool.png" "src\main\resources\assets\projectflint\textures\block\placed\woven_wool.png" >nul
copy /y "src\main\resources\assets\projectflint\textures\item\raw_leather.png" "src\main\resources\assets\projectflint\textures\block\raw_leather.png" >nul

for /f "tokens=1,* delims==" %%A in ('findstr /b /c:"mod_version=" "gradle.properties"') do set "MOD_VERSION=%%B"
for /f "tokens=1,* delims==" %%A in ('findstr /b /c:"archives_base_name=" "gradle.properties"') do set "ARCHIVES_BASE_NAME=%%B"
for /f "tokens=1,* delims==" %%A in ('findstr /b /c:"minecraft_version=" "gradle.properties"') do set "MINECRAFT_VERSION=%%B"

if not defined MOD_VERSION set "MOD_VERSION=0.28.41-a"
if not defined ARCHIVES_BASE_NAME set "ARCHIVES_BASE_NAME=projectflint"
if not defined MINECRAFT_VERSION set "MINECRAFT_VERSION=1.21.11"

set "EXPECTED_JAR=%ARCHIVES_BASE_NAME%-%MOD_VERSION%.jar"
set "GRADLE_VERSION=9.2.1"
set "BOOTSTRAP_DIR=%CD%\.gradle-bootstrap"
set "GRADLE_HOME=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%"
set "GRADLE_ZIP=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_SHA=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%-bin.zip.sha256"
set "GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_SHA_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip.sha256"

title Project Flint v%MOD_VERSION% Builder

echo ============================================================
echo                  PROJECT FLINT BUILDER
echo ============================================================
echo   Release:   v%MOD_VERSION%
echo   Minecraft: %MINECRAFT_VERSION%
echo   Output:    %EXPECTED_JAR%
echo ============================================================
echo.

if not exist "build.gradle" (
    echo [ERROR] build.gradle was not found.
    pause
    exit /b 1
)
if not exist "gradle.properties" (
    echo [ERROR] gradle.properties was not found.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found in PATH.
    echo Project Flint requires Java 21.
    pause
    exit /b 1
)

echo [INFO] Java runtime:
java -version
if errorlevel 1 (
    echo [ERROR] Java could not be started.
    pause
    exit /b 1
)
echo.

if exist "gradlew.bat" (
    echo [INFO] Gradle wrapper found.
    echo [INFO] Building Project Flint v%MOD_VERSION%...
    call gradlew.bat clean build --stacktrace
    goto :check_build
)

if exist "%GRADLE_HOME%\bin\gradle.bat" goto :run_bootstrap

if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"
where powershell >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Windows PowerShell was not found.
    pause
    exit /b 1
)

echo [1/3] Downloading Gradle %GRADLE_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
if errorlevel 1 (
    echo [ERROR] Gradle download failed.
    pause
    exit /b 1
)

echo [2/3] Downloading official SHA-256 checksum...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%GRADLE_SHA_URL%' -OutFile '%GRADLE_SHA%'"
if errorlevel 1 (
    echo [ERROR] Could not download the Gradle checksum.
    pause
    exit /b 1
)

echo [3/3] Verifying download...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$actual=(Get-FileHash -Algorithm SHA256 -LiteralPath '%GRADLE_ZIP%').Hash.ToLower(); $expected=((Get-Content -LiteralPath '%GRADLE_SHA%' -Raw).Trim().Split()[0]).ToLower(); if ($actual -ne $expected) { Write-Host '[ERROR] SHA-256 mismatch.'; exit 1 } else { Write-Host '[OK] SHA-256 verified.' }"
if errorlevel 1 (
    del /q "%GRADLE_ZIP%" >nul 2>nul
    del /q "%GRADLE_SHA%" >nul 2>nul
    pause
    exit /b 1
)

echo [INFO] Extracting Gradle...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -LiteralPath '%GRADLE_ZIP%' -DestinationPath '%BOOTSTRAP_DIR%' -Force"
if errorlevel 1 (
    echo [ERROR] Gradle extraction failed.
    pause
    exit /b 1
)

:run_bootstrap
echo [INFO] Building Project Flint v%MOD_VERSION% with Gradle %GRADLE_VERSION%...
call "%GRADLE_HOME%\bin\gradle.bat" clean build --stacktrace

:check_build
if errorlevel 1 (
    echo.
    echo ============================================================
    echo                        BUILD FAILED
    echo ============================================================
    pause
    exit /b 1
)

echo.
echo ============================================================
echo                     BUILD SUCCESSFUL
echo ============================================================
if exist "build\libs\%EXPECTED_JAR%" (
    echo [OK] Main mod JAR:
    echo   %CD%\build\libs\%EXPECTED_JAR%
) else (
    echo [WARNING] Expected JAR was not found.
    dir /b "build\libs\*.jar" 2>nul
)
echo.
if exist "build\libs\%EXPECTED_JAR%" (
    echo [INFO] Opening the built JAR location...
    start "" explorer.exe /select,"%CD%\build\libs\%EXPECTED_JAR%"
) else (
    echo [INFO] Opening build\libs...
    start "" explorer.exe "%CD%\build\libs"
)
ENDLOCAL
EXIT /B 0
