@echo off
setlocal EnableExtensions
cd /d "%~dp0"

REM ============================================================
REM Project Flint v0.31.67-b builder
REM v0.31.67-b: Remove unintended stripped-log sheet shortcut; keep Wooden Grinder progression.
REM v0.31.66-b: Fix packed mud crash, restore intended plank tools, add selective weak dynamite and carbon refining.
REM v0.31.65-b: Forge auto log batches, mud masonry and personal/table recipe yield balance.
REM v0.31.64-b: Restore personal Wooden Tools, Oak woodworking, vest bushes and valid Forge relighting.
REM v0.31.63-b: Report successful custom block breaks to Fabric/Flint Quests.
REM v0.31.63-b: Restore primitive Stone hammering without loosening ore/rock mining tiers.
REM v0.31.61-b: Mojang-mapped transparent glass and Hardwood-only PMI Sheets.
REM v0.31.60-b: Light-passing Mica Glass, PMI Sheets, daemon-free Gradle and dist JAR.
REM v0.31.58-b: Manual Forge fuel, 50-minute Fat Torches, Basin odds, cups, fertilizer.
REM v0.31.52-b: Fix Minecraft 1.21.11 RecipeDisplay import in Hardwood crafting recipe.
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
REM - Project Flint v0.31.0-b expands Wood/Water storage, brushing, beams, Bookshelf, and fixes water/leaf/processor integration
REM - Project Flint v0.31.1-b fixes Flint Plant Cutters harvesting full Sugar Cane columns into Sugarcane Rolls
REM - Project Flint v0.31.2-b fixes Wood/Water workstation UI, assets, recipes, names, and Flint Axe dismantling
REM - Project Flint v0.31.3-b adds Flint File woodworking, leaf tuning, washed-beam visuals, armor recipes, and Bookshelf cleanup
REM - Project Flint v0.31.4-b restores responsive Bookshelf crafting and adds the first full-shelf recipe, chest frames, and Sanding Station dye processing
REM - Project Flint v0.31.6-b fixes cabinet rendering/storage, Stove fuel/food/speed, names, and advanced-tool recovery for the v0.31.5-b content
REM - Project Flint v0.31.7-b adds persistent placed Crushed Stone recovery, Flax/String progression, research parchments, and Primitive Oven cooking
REM - Project Flint v0.31.8-b fixes Recipe Parchment name-component compilation on Minecraft 1.21.11
REM - Project Flint v0.31.9-b fixes single-cup milking, ambiguous Wooden Grinder recipes, and Bookshelf book-count recovery
REM - Project Flint v0.31.10-b restores blank parchment cabinet storage, adds bound-parchment visuals, staged Rope, and three researched Oven foods
REM - Project Flint v0.31.11-b fixes the stale 2-binding Crude Stone Cutters hammer-completion gate
REM - Project Flint v0.31.12-b adds Project Flint title branding, panorama, and custom splash text
REM - Project Flint v0.31.14-b fixes the scaled Project Flint logo render path
REM - Project Flint v0.31.15-b removes Realms title UI and disables background Realms availability/network polling
REM - Project Flint v0.31.17-b delays Realms-row removal until first render so Mod Menu can finish its title-screen injection
REM - Project Flint v0.31.18-b fixes Loom PF-EMI jar-in-jar resolution using a real external module dependency
REM - Project Flint v0.31.19-b adds stable independent PF-EMI promotion via filename + SHA sidecar
REM - Project Flint v0.31.20-b retires advancements, fixes Grinder twig ratios, adds the chest-backed Leather Backpack, and fixes dropped crude torches
REM - Project Flint v0.31.22-b adds nest-gated chicken eggs, repairs the Advanced Kiln clay input, and cleans the pause menu
REM - Project Flint v0.31.23-b makes PF-EMI an external standalone runtime mod and forbids nested EMI packaging
REM - Project Flint v0.31.24-b adds the aquifer Well, survival screwed beams/Dowsing Rod, Brick Shingles, and incomplete parchment art
REM - Project Flint v0.31.25-b adds the Brick Furnace/Blast Furnace shell and retextures the Advanced Kiln with mortarized smoker art
REM - Project Flint v0.31.26-b fixes Well UI/progress, dismantling rules, and the compact Well upper frame
REM - Project Flint v0.31.27-b relaxes Well interior validation and fixes Creative slowdown/darkness/lighting behavior
REM - Project Flint v0.31.28-b removes ProjectFlint-owned EMI registration and the local PF-EMI compile bridge
REM - Project Flint v0.31.29-b adds the Chicken Nest inventory/occupant UI and fired Clay Bucket water progression
REM - Project Flint v0.31.30-b adds Strawberries with cutter harvesting and temperate woodland/meadow worldgen
REM - Project Flint v0.31.31-b restores placed String pickup, adds seeds/jam/pie and brick chain, tunes aquifers
REM - Project Flint v0.31.32-b adds food/farming/fireclay progression, fuel hints, and night wolf encounters
REM - Project Flint v0.31.34-b refines nest/bucket/pulley crafting, fixes workstation models, and adds Creative-only Fireclay Forge charcoal processing
REM - Project Flint v0.31.35-b corrects Forge tiers/fuel, prototype collisions, and scrolling recipe-guide output names
REM - Project Flint v0.31.36-b adds Tanned Leather armor/Utility Vest, restores controlled personal crafting, and changes Brick Furnace fuel to charcoal
REM - Project Flint v0.31.37-b fixes four-log Forge fuel consumption and presents concise, multiline workstation hints
REM - v0.31.39-b adds consolidated recipe guide, interactive Basin and practical wood/vest harvesting fixes
REM - v0.31.40-b fixes wooden pickaxe masonry access/stone timing and forage bush pickup
REM - v0.31.44-b fixes Wooden Pickaxe tier bypass, Fat-Burning Torch support, and restores Wooden Sheet art
REM - v0.31.45-b introduces mining protection, Fat Torch fixes and Hardwood tool progression
REM - v0.31.46-b repairs the fish-bucket food component removal call for the 1.21.11 API
REM - v0.31.47-b balances Hardwood Hammer/Pickaxe, fixes Axe stripping and adds the G-key Field Guide
REM - v0.31.48-b fixes Field Guide crash caused by redundant screen blurring
REM - v0.31.49-b repairs Hardwood recipes, Fat Torch fuel, and player guide accuracy
REM - v0.31.50-b replaces the Hardwood Table ghost-result menu with server timed crafting
REM ============================================================

IF EXIST "APPLY_PROJECTFLINT_0_31_67_B.bat" CALL "APPLY_PROJECTFLINT_0_31_66_B.bat" /quiet

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

if not defined MOD_VERSION set "MOD_VERSION=0.31.67-b"
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
    call gradlew.bat clean build --no-daemon --stacktrace
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
call "%GRADLE_HOME%\bin\gradle.bat" clean build --no-daemon --stacktrace

:check_build
if errorlevel 1 goto :build_failed

if not exist "build\libs\%EXPECTED_JAR%" (
    echo [ERROR] Expected output JAR build\libs\%EXPECTED_JAR% was not generated.
    goto :build_failed
)

jar tf "build\libs\%EXPECTED_JAR%" | findstr /i /c:"PF-EMI-1.21.11.jar" >nul
if not errorlevel 1 (
    echo [ERROR] Produced Project Flint JAR includes nested PF-EMI; PF-EMI must remain separate.
    goto :build_failed
)

REM Publish only the finished mod JAR, not sources/dev/snapshot JARs.
if not exist "dist" mkdir "dist"
if not exist "dist" (
    echo [ERROR] Could not create the dist folder.
    goto :build_failed
)
copy /Y "build\libs\%EXPECTED_JAR%" "dist\%EXPECTED_JAR%" >nul
if errorlevel 1 (
    echo [ERROR] Failed to copy the finished JAR to dist.
    goto :build_failed
)

REM Keep only the most recently built Project Flint release in dist.
REM Unrelated JARs with different prefixes are intentionally left alone.
for %%J in ("dist\%ARCHIVES_BASE_NAME%-*.jar") do (
    if /I not "%%~nxJ"=="%EXPECTED_JAR%" del /q "%%~fJ" >nul 2>nul
)

echo.
echo ============================================================
echo                     BUILD SUCCESSFUL
echo ============================================================
echo [OK] Finished mod JAR:
echo   %CD%\dist\%EXPECTED_JAR%
echo [INFO] Gradle ran with --no-daemon, so its build Java process exits at completion.
echo.
start "" explorer.exe /select,"%CD%\dist\%EXPECTED_JAR%"
ENDLOCAL
EXIT /B 0

:build_failed
echo.
echo ============================================================
echo                        BUILD FAILED
echo ============================================================
echo [INFO] No new distribution JAR was published.
pause
ENDLOCAL
EXIT /B 1
