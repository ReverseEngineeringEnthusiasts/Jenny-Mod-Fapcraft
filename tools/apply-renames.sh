#!/usr/bin/env bash
# apply-renames.sh — apply the api/util/entity class renames + method token renames.
# Idempotent: safe to re-run after reverting affected dirs (git checkout).
# Usage: ./apply-renames.sh [src-root]   (default src/main/java)
set -e
ROOT="${1:-src/main/java}"

API_PAIRS="ao IGalathFinish
ar IPositionProvider
b2 IGalathStart
b8 ITargetProvider
ba KoboldNames
by SkinColor
c8 LightingType"

UTIL_PAIRS="fg IBeddableSexGirl
fm TribeState
gr EscapeDirectionKey
eh EyeColor
g5 HairColor
gt IBoneRotationSupplier
h_ IGalathUpdate
g1 IGalathExecute"

echo "== api classes =="
echo "$API_PAIRS" | while read -r old new; do
  python3 tools/rename_class.py "$ROOT" com/trolmastercard/sexmod/api "$old" "$new" 2>&1 | tail -1
done
echo "== util classes =="
echo "$UTIL_PAIRS" | while read -r old new; do
  python3 tools/rename_class.py "$ROOT" com/trolmastercard/sexmod/util "$old" "$new" 2>&1 | tail -1
done
echo "== entity fp -> Action =="
python3 tools/rename_class.py "$ROOT" com/trolmastercard/sexmod/entity fp Action 2>&1 | tail -1

echo "== method token renames =="
for m in iface_methods fg_method gfd_methods color_methods; do
  python3 tools/rename_methods.py "$ROOT" "/tmp/opencode/$m.json" 2>&1 | tail -1
done
echo "== done =="
