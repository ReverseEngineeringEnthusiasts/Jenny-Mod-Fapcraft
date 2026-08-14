@echo off
rem ============================================================
rem  Fapcraft 1.12.2 - commit + push helper (Windows)
rem  Usage: push.bat ["commit message"]
rem ============================================================
setlocal
chcp 65001 >nul
cd /d "%~dp0"

if "%~1"=="" (set "MSG=Build: %date%") else (set "MSG=%~1")

git add -A
git diff --cached --quiet
if not errorlevel 1 (
  echo [push] Nothing to commit.
  exit /b 0
)
git commit -m "%MSG%"
git push origin HEAD
echo [push] Pushed.
endlocal
