@echo off
setlocal DisableDelayedExpansion
set "EXIT_CODE=1"
pushd "%~dp0.." >nul
if errorlevel 1 (
  >&2 echo Error: Cannot open the project folder beside this script.
  endlocal & exit /b 1
)

rem The offline markers take precedence even when Maven files are present.
if exist "lib\" (
  if not exist "sources.txt" goto incomplete_offline
  set "RUN_MODE=offline"
) else (
  if exist "sources.txt" goto incomplete_offline
  if not exist "pom.xml" goto unsupported_folder
  if not exist "mvnw.cmd" goto unsupported_folder
  set "RUN_MODE=checkout"
)
if "%RUN_MODE%"=="checkout" goto checkout_classpath
set "SOURCE_CLASSPATH=out;lib/*"
if not exist "out\com\beamillionaire\Launcher.class" goto build_first
if not exist "lib\*.jar" goto missing_dependencies
goto find_java

:checkout_classpath
set "SOURCE_CLASSPATH=target/classes;target/lib/*"
if not exist "target\classes\com\beamillionaire\Launcher.class" goto build_first
if not exist "target\lib\*.jar" goto missing_dependencies

:find_java
set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for %%I in (java.exe) do set "JAVA_EXE=%%~$PATH:I"
if not defined JAVA_EXE goto missing_java
set "TOOL_VERSION="
for /f "tokens=1,2,3" %%A in ('call "%JAVA_EXE%" -version 2^>^&1') do if "%%B"=="version" if not defined TOOL_VERSION set "TOOL_VERSION=%%~C"
call :require_java_21
if errorlevel 1 goto finish

rem Vendor runtimes may include JavaFX modules. Ordinary JDKs use the JARs.
set "JAVA_FX_MODULES="
for /f "tokens=1 delims=@" %%M in ('call "%JAVA_EXE%" --list-modules 2^>nul') do if "%%M"=="javafx.controls" set "JAVA_FX_MODULES=--add-modules javafx.controls"
"%JAVA_EXE%" %JAVA_FX_MODULES% -cp "%SOURCE_CLASSPATH%" com.beamillionaire.Launcher %*
set "EXIT_CODE=%errorlevel%"
goto finish

:incomplete_offline
>&2 echo Error: Incomplete offline source folder. Both lib and sources.txt are required.
>&2 echo Extract the complete source ZIP again, or use a checkout without either offline marker.
goto finish

:unsupported_folder
>&2 echo Error: This folder needs lib plus sources.txt, or pom.xml plus mvnw.cmd.
>&2 echo Use the complete source ZIP or a complete Git checkout.
goto finish

:build_first
>&2 echo Error: The compiled game is missing. Run scripts\build-source.cmd first, then try again.
goto finish

:missing_dependencies
if "%RUN_MODE%"=="offline" (
  >&2 echo Error: Dependency JARs are missing from lib. Extract the complete source ZIP again.
) else (
  >&2 echo Error: Dependency JARs are missing from target\lib.
)
>&2 echo Run scripts\build-source.cmd first, then try again.
goto finish

:missing_java
>&2 echo Error: Java was not found. Install Java 21 or newer.
>&2 echo Set JAVA_HOME to its folder or add its bin folder to PATH, then try again.
goto finish

:require_java_21
set "JAVA_MAJOR="
for /f "tokens=1 delims=.-+" %%V in ("%TOOL_VERSION%") do set "JAVA_MAJOR=%%V"
if not defined JAVA_MAJOR goto invalid_version
for /f "delims=0123456789" %%V in ("%JAVA_MAJOR%") do goto invalid_version
if %JAVA_MAJOR% LSS 21 (
  >&2 echo Error: Java %TOOL_VERSION% is too old. Running the game requires Java 21 or newer.
  >&2 echo Update JAVA_HOME or PATH to use that version, then try again.
  exit /b 1
)
exit /b 0

:invalid_version
>&2 echo Error: Could not read the Java version. A working Java 21 or newer is required.
>&2 echo Check JAVA_HOME and PATH, then try again.
exit /b 1

:finish
popd
endlocal & exit /b %EXIT_CODE%
