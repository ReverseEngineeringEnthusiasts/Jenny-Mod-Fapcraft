#!/usr/bin/env bash
# ============================================================
#  Fapcraft 1.12.2 — universal build script (Linux/macOS/WSL)
#  Usage:
#    ./build.sh                 build only (jar -> dist/)
#    ./build.sh push "message"  build, then commit + push to origin
#    JAVA_HOME=/path ./build.sh force a specific JDK
# ============================================================
set -euo pipefail

BANNER="Reverse engineered in Kurdistan <3"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

say()  { printf '\033[1;36m[build]\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[build]\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31m[build]\033[0m %s\n' "$*" >&2; exit 1; }

# ---------------- Java discovery ----------------
# Order: $JAVA_HOME (if valid) -> PATH -> SDKMAN -> common system dirs.
# The MCRepack/SRG step (ASM 7.1 fork) requires a MODERN JDK; this project is
# built and verified on JDK 17/21/22+. Prefer the newest available, accept 8+.
# Override: JAVA_HOME=... ./build.sh  or  PREFER_JAVA=8 ./build.sh
declare -a JAVA_CANDIDATES=()

try_java_home() {
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then JAVA_CANDIDATES+=("$JAVA_HOME/bin/java"); fi
}

try_path() {
  local j; j="$(command -v java 2>/dev/null || true)"
  if [ -n "$j" ]; then JAVA_CANDIDATES+=("$j"); fi
}

try_sdkman() {
  local dir base="$HOME/.sdkman/candidates/java"
  [ -d "$base" ] || return 0
  for dir in "$base"/*/; do
    [ -x "$dir/bin/java" ] || continue
    JAVA_CANDIDATES+=("$dir/bin/java")
  done
}

try_system_dirs() {
  local dir
  for dir in /usr/lib/jvm/*/bin/java /usr/java/*/bin/java /opt/java/*/bin/java \
             /opt/*jdk*/bin/java /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java \
             /opt/homebrew/opt/openjdk@*/bin/java /opt/homebrew/opt/openjdk/bin/java; do
    if [ -x "$dir" ]; then JAVA_CANDIDATES+=("$dir"); fi
  done
}

java_version() { # <java-bin> -> major version number (8 for 1.8.0_xxx, 17 for 17.0.x)
  "$1" -version 2>&1 | sed -nE 's/.*version "([0-9]+)(\.([0-9]+))?.*/\1.\3/p' | head -1 | awk -F. '{print ($1==1 && $2>=1) ? $2 : $1}'
}

pick_java() {
  try_java_home; try_path; try_sdkman; try_system_dirs
  local seen="" best="" best_score=-1 j ver score
  for j in "${JAVA_CANDIDATES[@]}"; do
    case " $seen " in *" $j "*) continue;; esac
    seen="$seen $j"
    ver="$(java_version "$j" || true)"
    [ -n "$ver" ] || continue
    [ "$ver" -ge 8 ] || continue
    case "${PREFER_JAVA:-}" in
      8)  case "$ver" in 8) score=100 ;; 11|17) score=60 ;; *) score=40 ;; esac ;;
      *)  # default: newest wins (build needs modern JDK for MCRepack)
          case "$ver" in
            8)  score=30 ;;
            11) score=50 ;;
            17) score=70 ;;
            21) score=90 ;;
            *)  score=100 ;;
          esac ;;
    esac
    if [ "$score" -gt "$best_score" ]; then best="$j"; best_score="$score"; fi
  done
  [ -n "$best" ] || die "No usable JDK (>= 8) found. Install one or set JAVA_HOME."
  JAVA_BIN="$best"
  JAVA_HOME="$(cd "$(dirname "$best")/.." && pwd)"
}

# ---------------- Maven discovery ----------------
pick_mvn() {
  local m
  if m="$(command -v mvn 2>/dev/null || true)" && [ -n "$m" ]; then MVN="$m"; return 0; fi
  for m in "$HOME/.sdkman/candidates/maven/current/bin/mvn" \
           "${MAVEN_HOME:-}/bin/mvn" /opt/maven/bin/mvn /usr/share/maven/bin/mvn \
           /opt/apache-maven*/bin/mvn; do
    [ -x "$m" ] && { MVN="$m"; return 0; }
  done
  die "Maven not found. Install it (sdk install maven) or put mvn on PATH."
}

# ---------------- Build ----------------
pick_java
pick_mvn
say "Java:    $JAVA_HOME  ($("$JAVA_BIN" -version 2>&1 | head -1))"
say "Maven:   $MVN"
export JAVA_HOME

say "Building..."
"$MVN" -q clean package || die "Build failed."

JAR="$(ls -1 target/*.jar 2>/dev/null | grep -v original | head -1 || true)"
[ -n "$JAR" ] || die "No jar produced (check the build log)."
mkdir -p dist
cp "$JAR" dist/
BASE="$(basename "$JAR")"

echo
printf '\033[1;35m==============================================\033[0m\n'
printf '\033[1;35m  %s\033[0m\n' "$BANNER"
printf '\033[1;35m==============================================\033[0m\n'
echo
say "Artifact:  $PROJECT_DIR/dist/$BASE"
say "Size:      $(du -h "$JAR" | cut -f1)"
say "SRG-reobfuscated + shaded. Drop it into the mods/ folder of a 1.12.2 instance."

# ---------------- Optional push ----------------
if [ "${1:-}" = "push" ]; then
  shift
  MSG="${1:-Build: $(date +%F)}"
  say "Committing + pushing: $MSG"
  git add -A
  git commit -q -m "$MSG" || warn "Nothing to commit."
  git push origin HEAD || die "Push failed."
  say "Pushed."
fi
