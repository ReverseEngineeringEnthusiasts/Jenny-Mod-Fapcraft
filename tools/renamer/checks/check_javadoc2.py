import glob, sys, re

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
    lines = src.split('\n')
    # locate all comment blocks: for each '/**', find the first '*/' that ends a line
    i = 0
    blocks = 0
    while True:
        s = src.find('/**', i)
        if s < 0:
            break
        # find closing '*/' at end of line
        e = -1
        j = s + 3
        while True:
            k = src.find('*/', j)
            if k < 0:
                break
            # check char after k is newline/EOF
            after = src[k+2] if k+2 < len(src) else '\n'
            if after in '\n\r':
                e = k
                break
            j = k + 2
        if e < 0:
            print(f"NO EOL CLOSE: {f} block at line {src.count(chr(10),0,s)+1}")
            ok = False
            break
        inner = src[s+3:e]
        if '*/' in inner:
            print(f"EMBEDDED */ INSIDE: {f} block at line {src.count(chr(10),0,s)+1}")
            ok = False
        # also make sure no stray '/' '*' imbalance after stripping comments: count * on lines
        blocks += 1
        i = e + 2
    if blocks == 0:
        print(f"NO JAVADOC: {f}")
        ok = False
    # after removing all comment blocks (and single-line // comments), check the rest has balanced /* */
    stripped = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    stripped = re.sub(r'//[^\n]*', '', stripped)
    if stripped.count('/*') != stripped.count('*/'):
        print(f"IMBALANCE AFTER STRIP: {f}")
        ok = False
    count += 1
print(f"checked {count} files, {sum(1 for _ in [])} blocks")
print("ALL OK" if ok else "PROBLEMS FOUND")
sys.exit(0 if ok else 1)
