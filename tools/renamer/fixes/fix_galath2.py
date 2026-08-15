import re

p = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/GalathEntity.java'
s = open(p).read()
lines = s.split('\n')

renames = [
    (2806, 'Vec3d pos = VectorMath.rotateByYaw', 'corruptPos'),
    (2865, 'Minecraft mc = Minecraft.getMinecraft();', 'flapMc'),
    (2866, 'EntityPlayerSP player = flapMc.player;', 'flapPlayer'),
    (2896, 'EntityPlayerSP player = Minecraft.getMinecraft().player;', 'coinPlayer'),
    (2897, 'float yaw = this.getYawRotation() + 180.0F;', 'coinYaw'),
]

for ln, decl_old, new_name in renames:
    idx = ln - 1
    # find the line containing the decl (may have been touched by earlier rename)
    found = None
    for i in range(idx, min(idx + 2, len(lines))):
        if decl_old.split('=')[0].strip() in lines[i]:
            found = i
            break
    assert found is not None, (ln, decl_old)
    idx = found
    m = re.search(r'\b(\w+)\s*=\s*', lines[idx].split(';')[0])
    old = m.group(1)
    lines[idx] = lines[idx].replace(old, new_name, 1)
    j = idx + 1
    while j < len(lines) and 'case "' not in lines[j]:
        lines[j] = re.sub(r'\b' + re.escape(old) + r'\b', new_name, lines[j])
        j += 1

open(p, 'w').write('\n'.join(lines))
print('galath part2 done')
