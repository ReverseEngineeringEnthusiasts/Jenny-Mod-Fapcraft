#!/usr/bin/env python3
import json, os, subprocess
BASE = '<REPO>/src/main/java/com/trolmastercard/sexmod/util'
MAPDIR = '/tmp/maps'
# lambda params can't shadow enclosing locals: dir/name already used -> rename to subDir/subName
mp = {"methods": {}, "lambdas": {"loadCustomModels": {"var0x": "subDir", "var1x": "subName"}}}
# key by bare method name since sig already contains renamed params
jp = os.path.join(MAPDIR, 'ServerWhitelistManager.json')
with open(jp, 'w') as f:
    json.dump(mp, f)
r = subprocess.run(['python3', '/tmp/rename_varN.py', os.path.join(BASE, 'ServerWhitelistManager.java'), jp],
                   capture_output=True, text=True)
print(r.stdout.strip())
if r.stderr:
    print('STDERR:', r.stderr.strip())
