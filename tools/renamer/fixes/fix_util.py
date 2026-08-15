#!/usr/bin/env python3
"""Supplemental fixes for leftovers."""
import json, os, re, subprocess

BASE = '<REPO>/src/main/java/com/trolmastercard/sexmod/util'
MAPDIR = '/tmp/maps'

fixes = {
"DebugWindow2.java": {"methods": {}, "lambdas": {
    "DebugWindow2()#0": {"var2x": "sexmodDir", "var3x": "dontAskFile", "var5x": "ioException"},
    "DebugWindow2()#1": {"var1x": "sexmodDir", "var2x": "deleteBat", "var3x": "writer", "var4x": "ioException"}
}},
"PlayerKoboldRenderer.java": {"methods": {"renderRightEye()": {"var1": "scale", "var2": "inverseScale"}}},
"PlayerLunaRenderer.java": {"methods": {"resolveHeldItemStack(@NullableItemStack)": {"var2": "lunaStack"}}},
"DynamicTrailRenderer.java": {"methods": {"updateTrails()": {"var2": "segment"}}},
"KoboldTask.java": {"methods": {"releaseWorkers()": {"var2": "worker"}}},
"KoboldManager.java": {"methods": {"removeTask(KoboldTask)": {"var4": "master"}}},
"ServerWhitelistManager.java": {"methods": {
    "getGlobalModelOverride()": {"var0": "customModelsKey"},
    "getCustomModelsKey()": {"var0": "minecraft", "var1": "serverData", "var2": "serverIp", "var3": "portIndex"}
}},
}

fail = 0
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
    if r.returncode != 0:
        fail += 1

# direct patches for body-less declarations
def patch(path, subs):
    src = open(path).read()
    for old, new in subs:
        assert src.count(old) == 1, f"{path}: {old!r} found {src.count(old)} times"
        src = src.replace(old, new)
    open(path, 'w').write(src)

patch(os.path.join(BASE, 'IBoneRotationSupplier.java'), [('float getRotation(BaseGirlEntity var1);', 'float getRotation(BaseGirlEntity girl);')])
patch(os.path.join(BASE, 'IGalathExecute.java'), [('boolean canExecute(GalathEntity var1);', 'boolean canExecute(GalathEntity galath);')])
patch(os.path.join(BASE, 'IGalathUpdate.java'), [('boolean update(GalathEntity var1);', 'boolean update(GalathEntity galath);')])
patch(os.path.join(BASE, 'AbstractPlayerKoblinGoboldRenderer.java'), [('protected abstract Vec3i resolveBoneColor(String var1);', 'protected abstract Vec3i resolveBoneColor(String boneName);')])
patch(os.path.join(BASE, 'RibbonRenderer.java'), [('float getPoint(int var1, float var2);', 'float getPoint(int index, float time);')])
print('direct patches done')
sys.exit(1 if fail else 0)
