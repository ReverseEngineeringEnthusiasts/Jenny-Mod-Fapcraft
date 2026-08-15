import re

def fix_case_collisions(path, renames):
    s = open(path).read()
    lines = s.split('\n')
    for ln, needle, new_name in renames:
        idx = ln - 1
        found = None
        for i in range(max(0, idx - 1), min(idx + 2, len(lines))):
            if needle in lines[i]:
                found = i
                break
        assert found is not None, (path, ln, needle)
        idx = found
        m = re.search(r'\b(\w+)\s*=\s*', lines[idx].split(';')[0])
        old = m.group(1)
        lines[idx] = lines[idx].replace(old, new_name, 1)
        j = idx + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\b' + re.escape(old) + r'\b', new_name, lines[j])
            j += 1
    open(path, 'w').write('\n'.join(lines))
    print('fixed', path)

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/'

fix_case_collisions(base + 'GalathPlayerEntity.java', [
    (433, 'EntityPlayerSP player = Minecraft.getMinecraft().player;', 'corruptPlayer'),
    (434, 'float yaw = this.getYawRotation() + 220.0F;', 'corruptYaw'),
    (435, 'Vec3d pos = VectorMath.rotateByYaw', 'corruptPos'),
    (472, 'Minecraft mc = Minecraft.getMinecraft();', 'flapMc'),
    (473, 'EntityPlayerSP player = mc.player;', 'flapPlayer'),
])
