import os

ROOT = '<REPO>/src/main/java/com/trolmastercard/sexmod'

def sub_file(path, pairs):
    with open(path) as f:
        s = f.read()
    for old, new in pairs:
        assert old in s, f"{path}: NOT FOUND: {old[:80]}"
        s = s.replace(old, new)
    with open(path, 'w') as f:
        f.write(s)
    print("ok", path)

sub_file(os.path.join(ROOT, 'networking/KoboldStatePacket.java'), [
    ('SceneDebug.log("KoboldStatePacket.sendState', 'SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket.sendState'),
    ('SceneDebug.log("KoboldStatePacket: girl %s found', 'SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: girl %s found'),
    ('SceneDebug.log("KoboldStatePacket: targetPos', 'SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: targetPos'),
    ('SceneDebug.log("KoboldStatePacket: after snap', 'SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: after snap'),
    ('SceneDebug.log("KoboldStatePacket: %s is not IEllie', 'SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: %s is not IEllie'),
    ('SceneDebug.log("KoboldStatePacket: setDismounted', 'SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: setDismounted'),
])

sub_file(os.path.join(ROOT, 'networking/ChangeDataParameterPacket.java'), [
    ('SceneDebug.log("ChangeDataParameter:', 'SceneDebug.log(SceneDebug.PACKETS, "ChangeDataParameter:'),
])

sub_file(os.path.join(ROOT, 'networking/ResetGirlPacket.java'), [
    ('SceneDebug.log("ResetGirlPacket.resetGirl', 'SceneDebug.log(SceneDebug.RESET, "ResetGirlPacket.resetGirl'),
    ('SceneDebug.log("ResetGirlPacket.onMessage', 'SceneDebug.log(SceneDebug.RESET, "ResetGirlPacket.onMessage'),
])

sub_file(os.path.join(ROOT, 'networking/SendGirlToSexPacket.java'), [
    ('SceneDebug.log("SendGirlToSexPacket:', 'SceneDebug.log(SceneDebug.PACKETS, "SendGirlToSexPacket:'),
])

sub_file(os.path.join(ROOT, 'util/HandlePlayerMovement.java'), [
    ('SceneDebug.log("setMovementLock(', 'SceneDebug.log(SceneDebug.MOVEMENT, "setMovementLock('),
])

sub_file(os.path.join(ROOT, 'util/InHandMapRenderer.java'), [
    ('SceneDebug.log("InHandMapRenderer:', 'SceneDebug.log(SceneDebug.IN_HAND, "InHandMapRenderer:'),
])

sub_file(os.path.join(ROOT, 'client/gui/ClothingScreen.java'), [
    ('SceneDebug.log("ClothingScreen:', 'SceneDebug.log(SceneDebug.CLOTHING, "ClothingScreen:'),
])

sub_file(os.path.join(ROOT, 'client/SexSceneKeyHandler.java'), [
    ('SceneDebug.log("R-SHIFT pressed', 'SceneDebug.log(SceneDebug.RSHIFT, "R-SHIFT pressed'),
])
