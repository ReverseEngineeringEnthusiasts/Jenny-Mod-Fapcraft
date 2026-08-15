p = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/GalathEntity.java'
s = open(p).read()
lines = s.split('\n')

# 1) lightcharge + flap: per-case Vec3d vec
fixes = [
    (2646, 'Vec3d vec = this.getVectorTowardPlayer();', 'Vec3d aimVec = this.getVectorTowardPlayer();'),
    (2678, 'Vec3d vec = this.getVectorTowardPlayer();', 'Vec3d flapVec = this.getVectorTowardPlayer();'),
]
for ln, decl_old, decl_new in fixes:
    idx = ln - 1
    assert decl_old in lines[idx], (ln, lines[idx])
    lines[idx] = decl_new
    old = decl_old.split(' ')[1]
    new = decl_new.split(' ')[1]
    j = idx + 1
    while j < len(lines) and 'case "' not in lines[j]:
        if old in lines[j]:
            lines[j] = lines[j].replace(old, new)
        j += 1

open(p, 'w').write('\n'.join(lines))
print('part1 done')
