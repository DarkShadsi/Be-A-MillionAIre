@echo off
setlocal DisableDelayedExpansion
set "EXIT_CODE=1"
pushd "%~dp0.." >nul
if errorlevel 1 (
  >&2 echo Error: Cannot open the project folder beside this script.
  endlocal & exit /b 1
)

rem The offline source ZIP also contains pom.xml and the Maven wrapper.
rem Its two markers therefore take precedence over checkout detection.
if exist "lib\" (
  if not exist "sources.txt" goto incomplete_offline
  set "BUILD_MODE=offline"
) else (
  if exist "sources.txt" goto incomplete_offline
  if not exist "pom.xml" goto unsupported_folder
  if not exist "mvnw.cmd" goto unsupported_folder
  set "BUILD_MODE=checkout"
)
if "%BUILD_MODE%"=="offline" if not exist "lib\*.jar" goto missing_dependencies

set "JAVA_EXE="
set "JAVAC_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" if exist "%JAVA_HOME%\bin\javac.exe" (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
  set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
)
if defined JAVA_EXE goto check_versions
rem Do not pass a missing or incomplete JAVA_HOME to the Maven wrapper.
set "JAVA_HOME="
for %%I in (java.exe) do set "JAVA_EXE=%%~$PATH:I"
for %%I in (javac.exe) do set "JAVAC_EXE=%%~$PATH:I"
if not defined JAVA_EXE goto missing_java
if not defined JAVAC_EXE goto missing_javac

:check_versions
set "TOOL_VERSION="
for /f "tokens=1,2,3" %%A in ('call "%JAVA_EXE%" -version 2^>^&1') do if "%%B"=="version" if not defined TOOL_VERSION set "TOOL_VERSION=%%~C"
call :require_java_21 Java
if errorlevel 1 goto finish
set "TOOL_VERSION="
for /f "tokens=1,2" %%A in ('call "%JAVAC_EXE%" -version 2^>^&1') do if "%%A"=="javac" if not defined TOOL_VERSION set "TOOL_VERSION=%%B"
call :require_java_21 javac
if errorlevel 1 goto finish

if "%BUILD_MODE%"=="checkout" goto build_checkout
if not exist "out\" mkdir "out"
if not exist "out\" goto output_failure
echo Compiling the offline source folder with JDK %TOOL_VERSION%...
"%JAVAC_EXE%" --release 21 -encoding UTF-8 -cp "lib/*" -d "out" @sources.txt
set "EXIT_CODE=%errorlevel%"
if not "%EXIT_CODE%"=="0" goto finish
if not exist "src\main\resources\" goto missing_resources
dir /b /s /a-d "src\main\resources\*" >nul 2>&1
if errorlevel 1 (
  >&2 echo Error: Resource copying failed. No files were found in src\main\resources.
  set "EXIT_CODE=1"
  goto finish
)
xcopy "src\main\resources\*" "out\" /E /I /Y >nul
set "EXIT_CODE=%errorlevel%"
if not "%EXIT_CODE%"=="0" (
  >&2 echo Error: Resource copying failed. Check src\main\resources and the out folder.
  goto finish
)
echo Source compilation complete. Run scripts\run-source.cmd to start the game.
goto finish

:build_checkout
echo Building the checkout with the Maven wrapper...
call ".\mvnw.cmd" -B -ntp verify
set "EXIT_CODE=%errorlevel%"
if "%EXIT_CODE%"=="0" echo Build complete. Run scripts\run-source.cmd to start the game.
goto finish

:incomplete_offline
>&2 echo Error: Incomplete offline source folder. Both lib and sources.txt are required.
>&2 echo Extract the complete source ZIP again, or use a checkout without either offline marker.
goto finish

:unsupported_folder
>&2 echo Error: This folder needs lib plus sources.txt, or pom.xml plus mvnw.cmd.
>&2 echo Use the complete source ZIP or a complete Git checkout.
goto finish

:missing_dependencies
>&2 echo Error: No dependency JARs were found in lib.
>&2 echo Extract the complete source ZIP again, then run scripts\build-source.cmd.
goto finish

:missing_java
>&2 echo Error: Java was not found. Install JDK 21 or newer.
>&2 echo Set JAVA_HOME to its JDK folder or add its bin folder to PATH, then try again.
goto finish

:missing_javac
>&2 echo Error: javac was not found. Building requires JDK 21 or newer, including its compiler.
>&2 echo Set JAVA_HOME to its JDK folder or add its bin folder to PATH, then try again.
goto finish

:output_failure
>&2 echo Error: Cannot create the out folder. Check that this source folder is writable.
goto finish

:missing_resources
>&2 echo Error: src\main\resources is missing. Extract the complete source ZIP again.
set "EXIT_CODE=1"
goto finish

:require_java_21
set "JAVA_MAJOR="
for /f "tokens=1 delims=.-+" %%V in ("%TOOL_VERSION%") do set "JAVA_MAJOR=%%V"
if not defined JAVA_MAJOR goto invalid_version
for /f "delims=0123456789" %%V in ("%JAVA_MAJOR%") do goto invalid_version
if %JAVA_MAJOR% LSS 21 (
  >&2 echo Error: %~1 %TOOL_VERSION% is too old. Building requires JDK 21 or newer.
  >&2 echo Update JAVA_HOME or PATH to use that JDK, then try again.
  exit /b 1
)
exit /b 0

:invalid_version
>&2 echo Error: Could not read the %~1 version. Building requires a working JDK 21 or newer.
>&2 echo Check JAVA_HOME and PATH, then try again.
exit /b 1

:finish
popd
endlocal & exit /b %EXIT_CODE%
