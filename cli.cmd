@echo off
setlocal
pushd "%~dp0"
call mvnw.cmd -q compile dependency:copy-dependencies -DincludeScope=runtime
if errorlevel 1 (
    popd
    exit /b 1
)
java -cp "target\classes;target\dependency\*" com.beamillionaire.Launcher --cli
set "cliResult=%errorlevel%"
popd
exit /b %cliResult%
