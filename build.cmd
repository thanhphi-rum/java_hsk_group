@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
set "JDK_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JDK_BIN=%JAVA_HOME%\bin\"
if not defined JDK_BIN if exist "%USERPROFILE%\.jdks\openjdk-23.0.1\bin\javac.exe" set "JDK_BIN=%USERPROFILE%\.jdks\openjdk-23.0.1\bin\"
if not defined JDK_BIN where javac >nul 2>&1
if errorlevel 1 (
  echo JDK 17 or newer is required. Set JAVA_HOME to your JDK folder.
  exit /b 1
)
if not exist bin mkdir bin
(for /r src %%f in (*.java) do (
  set "SOURCE_FILE=%%f"
  echo "!SOURCE_FILE:\=/!"
)) > build-sources.txt
"%JDK_BIN%javac.exe" --release 17 -encoding UTF-8 -cp "lib/*" -d bin @build-sources.txt
exit /b %errorlevel%
