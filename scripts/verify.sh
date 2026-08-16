#!/usr/bin/env bash
# Loop-engineering style verification loop for StaffMate.
#
# Cycle: check -> (if failing) fix -> push -> re-check -> repeat, up to a
# fixed budget. This script performs the *check* stage only (fast, local,
# no external services); the fix/push/re-check stages are done by Claude
# using GitHub Actions (build.yml, deploy-pwa.yml) as the verification
# oracle, since real compilation/deployment requires their environments.
#
# Exit code 0 = all local checks passed. Non-zero = something needs a fix.
set -e
cd "$(dirname "$0")/.."

echo "== [1/2] PWA: JS syntax check =="
fail=0
for f in $(find pwa/js -name "*.js"); do
  cp "$f" "${f%.js}.mjs.tmp" && mv "${f%.js}.mjs.tmp" "${f%.js}.mjs"
done
for f in $(find pwa/js -name "*.mjs"); do
  node --check "$f" || { echo "SYNTAX FAIL: $f"; fail=1; }
done
find pwa/js -name "*.mjs" -delete
[ "$fail" -eq 0 ] && echo "OK"

echo "== [2/2] Android: brace balance check =="
for f in $(find app/src -name "*.kt"); do
  o=$(tr -cd '{' < "$f" | wc -c)
  c=$(tr -cd '}' < "$f" | wc -c)
  if [ "$o" -ne "$c" ]; then echo "BRACE MISMATCH: $f (open=$o close=$c)"; fail=1; fi
done
[ "$fail" -eq 0 ] && echo "OK"

if [ "$fail" -ne 0 ]; then
  echo ""
  echo "Local checks FAILED — fix before pushing."
  exit 1
fi

echo ""
echo "All local checks passed. Push and let build.yml / deploy-pwa.yml verify the real build."
