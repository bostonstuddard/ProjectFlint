@ECHO OFF
SETLOCAL EnableExtensions
CD /D "%~dp0"

IF /I NOT "%~1"=="/quiet" ECHO Applying Project Flint v0.28.38-a source cleanup...

FOR %%F IN (
  "APPLY_PROJECTFLINT_0_28_37_A.bat"
) DO (
  IF EXIST "%%~F" DEL /F /Q "%%~F"
)

DEL /F /Q "javac.*.args" >NUL 2>&1
DEL /F /Q "javac-*.args" >NUL 2>&1

IF /I NOT "%~1"=="/quiet" ECHO Project Flint v0.28.38-a source cleanup complete.
ENDLOCAL
EXIT /B 0
