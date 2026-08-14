#!/usr/bin/env bash
# ============================================================
#  Fapcraft 1.12.2 — commit + push helper
#  Usage: ./push.sh ["commit message"]
#         (default message: Build: <date>)
# ============================================================
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

MSG="${1:-Build: $(date +%F)}"

git add -A
if git diff --cached --quiet; then
  echo "[push] Nothing to commit."
  exit 0
fi
git commit -q -m "$MSG"
git push origin HEAD
echo "[push] Pushed."
