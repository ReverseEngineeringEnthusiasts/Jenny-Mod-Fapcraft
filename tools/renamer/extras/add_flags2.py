import os

ROOT = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity'

def sub_file(path, pairs):
    with open(path) as f:
        s = f.read()
    for old, new in pairs:
        assert old in s, f"{path}: NOT FOUND: {old[:80]}"
        s = s.replace(old, new)
    with open(path, 'w') as f:
        f.write(s)
    print("ok", path)

# BiaEntity -> SCENE_ENTRY
sub_file(os.path.join(ROOT, 'BiaEntity.java'), [
    ('SceneDebug.log("Bia.doAction', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.doAction'),
    ('SceneDebug.log("Bia.U()', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.U()'),
    ('SceneDebug.log("Bia.sound', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.sound'),
    ('SceneDebug.log("Bia.handleAnalState', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.handleAnalState'),
])

# LunaEntity -> SCENE_ENTRY
sub_file(os.path.join(ROOT, 'LunaEntity.java'), [
    ('SceneDebug.log("Luna.doAction', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.doAction'),
    ('SceneDebug.log("Luna.U()', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.U()'),
    ('SceneDebug.log("Luna.sound', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.sound'),
    ('SceneDebug.log("Luna.handleNearbyPlayer', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna.handleNearbyPlayer'),
    ('SceneDebug.log("Luna: walking to bed', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna: walking to bed'),
    ('SceneDebug.log("Luna: arrived at bed', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Luna: arrived at bed'),
])

# KoboldEntity -> SITTING / SCENE_ENTRY
sub_file(os.path.join(ROOT, 'KoboldEntity.java'), [
    ('SceneDebug.log("Kobold.isSitting', 'SceneDebug.log(SceneDebug.SITTING, "Kobold.isSitting'),
    ('SceneDebug.log("Kobold.U()', 'SceneDebug.log(SceneDebug.SCENE_ENTRY, "Kobold.U()'),
])
