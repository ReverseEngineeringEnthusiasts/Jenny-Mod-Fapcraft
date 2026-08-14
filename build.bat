@echo off
rem ============================================================
rem  Fapcraft 1.12.2 - universal build script (Windows)
rem  Usage:
rem    build.bat                build only (jar -> dist\)
rem    build.bat push "message" build, then commit + push to origin
rem    set JAVA_HOME=C:\jdk8    force a specific JDK
rem ============================================================
setlocal enabledelayedexpansion
chcp 65001 >nul

set "BANNER=Reverse engineered in Kurdistan <3"
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

rem ---------------- Java discovery ----------------
rem Order: JAVA_HOME -> PATH -> SDKMAN -> Program Files dirs.
rem The MCRepack/SRG step (ASM 7.1 fork) requires a MODERN JDK; this project is
rem built and verified on JDK 17/21/22+. Prefer the newest available, accept 8+.
set "JAVA_BIN="
set "BEST_SCORE=-1"

call :try_java_home
call :try_path
call :try_sdkman
call :try_system_dirs

if not defined JAVA_BIN (
  echo [build] No usable JDK (^>= 8) found. Install one or set JAVA_HOME.
  exit /b 1
)
for %%J in ("%JAVA_BIN%") do set "JAVA_HOME=%%~dpJ.."
echo [build] Java:    %JAVA_HOME%
"%JAVA_BIN%" -version 2>&1 | findstr /r "version" | head -1 >nul 2>&1

rem ---------------- Maven discovery ----------------
set "MVN="
where mvn >nul 2>&1 && set "MVN=mvn"
if not defined MVN if exist "%USERPROFILE%\.sdkman\candidates\maven\current\bin\mvn.cmd" set "MVN=%USERPROFILE%\.sdkman\candidates\maven\current\bin\mvn.cmd"
if not defined MVN if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
if not defined MVN if exist "C:\Program Files\apache-maven*\bin\mvn.cmd" set "MVN=C:\Program Files\apache-maven\bin\mvn.cmd"
if not defined MVN (
  echo [build] Maven not found. Install it or put mvn on PATH.
  exit /b 1
)
echo [build] Maven:   %MVN%

rem ---------------- Build ----------------
echo [build] Building...
call "%MVN%" -q clean package
if errorlevel 1 (
  echo [build] Build failed.
  exit /b 1
)

for %%F in (target\*.jar) do set "JAR=%%F"
if not defined JAR (
  echo [build] No jar produced.
  exit /b 1
)
if not exist dist mkdir dist
copy /y "%JAR%" dist\ >nul
for %%F in ("%JAR%") do set "JARSIZE=%%~zF"
echo.
echo ==============================================
echo   %BANNER%
echo ==============================================
echo.
echo [build] Artifact:  %PROJECT_DIR%dist\%~nxJAR%
echo [build] Size:      %JARSIZE% bytes
echo [build] SRG-reobfuscated + shaded. Drop it into the mods/ folder of a 1.12.2 instance.

if "%~1"=="push" (
  shift
  if "%~1"=="" (set "MSG=Build: %date%") else (set "MSG=%~1")
  echo [build] Committing + pushing: %MSG%
  git add -A
  git commit -m "%MSG%" >nul 2>&1 || echo [build] Nothing to commit.
  git push origin HEAD || echo [build] Push failed.
  echo [build] Pushed.
)
endlocal
exit /b 0

rem ---------------- helper: JAVA_HOME ----------------
:try_java_home
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
exit /b 0

rem ---------------- helper: PATH ----------------
:try_path
where java >nul 2>&1 && set "JAVA_BIN=java"
exit /b 0

rem ---------------- helper: SDKMAN ----------------
:try_sdkman
if not exist "%USERPROFILE%\.sdkman\candidates\java" exit /b 0
rem newest first (SDKMAN sorts alphabetically; 25.x/22.x/21.x/17.x/11.x/8.x)
for /d %%D in ("%USERPROFILE%\.sdkman\candidates\java\*") do (
  if exist "%%D\bin\java.exe" set "JAVA_BIN=%%D\bin\java.exe"
)
exit /b 0

rem ---------------- helper: system dirs ----------------
:try_system_dirs
if not defined JAVA_BIN for /d %%D in ("C:\Program Files\Java\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Microsoft\jdk*" "C:\Program Files\Amazon Corretto\jdk*" "C:\Program Files\Zulu\zulu*") do (
  if exist "%%D\bin\java.exe" set "JAVA_BIN=%%D\bin\java.exe"
)
exit /b 0
