@echo off
setlocal
cd /d "%~dp0"
call build.cmd
if errorlevel 1 (
  pause
  exit /b 1
)
set "JAVA_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVA_BIN=%JAVA_HOME%\bin\"
if not defined JAVA_BIN if exist "%USERPROFILE%\.jdks\openjdk-23.0.1\bin\javaw.exe" set "JAVA_BIN=%USERPROFILE%\.jdks\openjdk-23.0.1\bin\"
start "Cinema" "%JAVA_BIN%javaw.exe" -cp "bin;lib/*" gui.Main_GUI
