import glob, sys

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'
files = (glob.glob(base + '*.java') + glob.glob(base + 'client/*.java') + glob.glob(base + 'client/gui/*.java') +
         glob.glob(base + 'client/renderer/*.java') + glob.glob(base + 'client/renderer/api/*.java') +
         glob.glob(base + 'client/model/*.java') + glob.glob(base + 'client/model/api/*.java') +
         glob.glob(base + 'client/particle/*.java'))
skip = {base + 'Main.java',
        base + 'client/gui/ClothingScreen.java',
        base + 'client/gui/HornyMeterHud.java',
        base + 'client/GirlCameraHelper.java',
        base + 'client/SexSceneKeyHandler.java'}
ok = True
count = 0
for f in sorted(files):
    if f in skip:
        continue
    src = open(f).read()
    n_open = src.count('/*')
    n_close = src.count('*/')
    if n_open != n_close:
        print(f"UNBALANCED: {f} open={n_open} close={n_close}")
        ok = False
    i = 0
    blocks = 0
    while True:
        s = src.find('/**', i)
        if s < 0:
            break
        e = src.find('*/', s + 3)
        if e < 0:
            print(f"UNTERMINATED: {f} at {s}")
            ok = False
            break
        inner = src[s + 3:e]
        if '*/' in inner:
            print(f"STRAY */ INSIDE: {f} at {s}")
            ok = False
        blocks += 1
        i = e + 2
    if blocks == 0:
        print(f"NO JAVADOC: {f}")
        ok = False
    count += 1
print(f"checked {count} files")
print("ALL OK" if ok else "PROBLEMS FOUND")
sys.exit(0 if ok else 1)
