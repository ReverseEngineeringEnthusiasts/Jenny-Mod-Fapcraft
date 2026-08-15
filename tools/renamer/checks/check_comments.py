import os, re

files = [
    'entity/Action.java', 'util/RotationHelper.java', 'util/SceneDebug.java',
    'util/HandlePlayerMovement.java', 'client/SexSceneKeyHandler.java',
    'networking/ChangeDataParameterPacket.java', 'networking/KoboldStatePacket.java',
    'networking/ResetGirlPacket.java', 'networking/SendGirlToSexPacket.java',
    'networking/PacketHandler.java', 'entity/BaseGirlEntity.java',
    'entity/JennyEntity.java', 'entity/BiaEntity.java', 'entity/LunaEntity.java',
    'entity/KoboldEntity.java', 'client/gui/HornyMeterHud.java',
    'client/GirlCameraHelper.java', 'client/gui/ClothingScreen.java',
    'util/InHandMapRenderer.java',
]
root = '<REPO>/src/main/java/com/trolmastercard/sexmod'
ok = True
for f in files:
    p = os.path.join(root, f)
    with open(p) as fh:
        s = fh.read()
    opens = s.count('/*')
    closes = s.count('*/')
    jdoc = s.count('/**')
    status = 'OK' if opens == closes and jdoc > 0 else 'PROBLEM'
    if status != 'OK':
        ok = False
    print(f"{status}  {f}  (/* = {opens}, */ = {closes}, /** = {jdoc})")
print("ALL OK" if ok else "FIX NEEDED")
