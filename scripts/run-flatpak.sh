#!/usr/bin/env bash
# Build the mod jar and launch an existing PrismLauncher/Cleanroom instance
# through the PrismLauncher Flatpak. This skips CleanroomGradle's runClient
# download/setup and tests the same instance the players use.
#
# Usage:
#   cp .flatpak-run.conf.example .flatpak-run.conf   # once, then edit
#   scripts/run-flatpak.sh
#   scripts/run-flatpak.sh --no-build     # relaunch the current jar
#   scripts/run-flatpak.sh --no-launch    # build + install only
#
# The actual .flatpak-run.conf is gitignored; the example is committed.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

CONFIG="${FLATPAK_RUN_CONFIG:-$ROOT/.flatpak-run.conf}"
if [ ! -f "$CONFIG" ]; then
  echo "Missing $CONFIG" >&2
  echo "Copy .flatpak-run.conf.example to .flatpak-run.conf and edit it." >&2
  exit 1
fi

# shellcheck disable=SC1090
source "$CONFIG"

: "${FLATPAK_APP:?set FLATPAK_APP in $CONFIG}"
: "${INSTANCE_ID:?set INSTANCE_ID in $CONFIG}"
: "${INSTANCE_DIR:?set INSTANCE_DIR in $CONFIG}"
MODS_DIR="${MODS_DIR:-$INSTANCE_DIR/minecraft/mods}"
MOD_BACKUP_DIR="${MOD_BACKUP_DIR:-$INSTANCE_DIR/mod-backups}"
OFFLINE_NAME="${OFFLINE_NAME:-}"
CLEANROOM_VERSION="${CLEANROOM_VERSION:-}"
PRISM_ARGS="${PRISM_ARGS:-}"

BUILD=1
LAUNCH=1
for arg in "$@"; do
  case "$arg" in
    --no-build) BUILD=0 ;;
    --no-launch|--build-only) LAUNCH=0 ;;
    -h|--help)
      sed -n '2,18p' "$0"
      exit 0
      ;;
    *)
      echo "Unknown option: $arg" >&2
      exit 2
      ;;
  esac
done

if [ "$BUILD" -eq 1 ]; then
  echo "==> Building the Java 8 release jar"
  ./build.sh
fi

JAR="$(ls -t dist/fapcraft-1.12.2-*.jar 2>/dev/null | head -1 || true)"
if [ -z "$JAR" ]; then
  echo "No dist/fapcraft-1.12.2-*.jar found; run ./build.sh first." >&2
  exit 1
fi

if [ -n "$CLEANROOM_VERSION" ]; then
  python3 "$ROOT/scripts/update-cleanroom.py" \
    --instance "$INSTANCE_DIR" \
    --version "$CLEANROOM_VERSION"
fi

mkdir -p "$MODS_DIR" "$MOD_BACKUP_DIR"
for old in "$MODS_DIR"/fapcraft-*.jar; do
  [ -e "$old" ] || continue
  mv "$old" "$MOD_BACKUP_DIR/$(basename "$old").$(date +%Y%m%d-%H%M%S)"
done
cp "$JAR" "$MODS_DIR/"
echo "==> Installed $(basename "$JAR") into $MODS_DIR"

if [ "$LAUNCH" -eq 1 ]; then
  echo "==> Launching $INSTANCE_ID via $FLATPAK_APP"
  if [ -n "$OFFLINE_NAME" ]; then
    exec flatpak run "$FLATPAK_APP" --launch "$INSTANCE_ID" --offline "$OFFLINE_NAME" $PRISM_ARGS
  else
    exec flatpak run "$FLATPAK_APP" --launch "$INSTANCE_ID" $PRISM_ARGS
  fi
fi
