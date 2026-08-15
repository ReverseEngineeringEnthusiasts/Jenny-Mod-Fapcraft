#!/usr/bin/env python3
import json, os, subprocess

BASE = '<REPO>/src/main/java/com/trolmastercard/sexmod/util'
MAPDIR = '/tmp/maps'

fixes = {
"DebugWindow2.java": {"methods": {}, "lambdas": {
    "DebugWindow2()#1": {"var5x": "ioException"}
}},
"PlayerLunaRenderer.java": {"methods": {"resolveHeldItemStack": {"var2": "lunaStack"}}},
"KoboldManager.java": {"methods": {"removeTask": {"var4": "master"}}},
}

for fname, mp in fixes.items():
    jp = os.path.join(MAPDIR, fname.replace('.java', '.json'))
    with open(jp, 'w') as f:
        json.dump(mp, f, indent=1)
    r = subprocess.run(['python3', '/tmp/rename_varN.py',
                        os.path.join(BASE, fname), jp],
                       capture_output=True, text=True)
    print(r.stdout.strip())
    if r.stderr:
        print('  STDERR:', r.stderr.strip())
