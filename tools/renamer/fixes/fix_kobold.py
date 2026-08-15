p = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/KoboldEntity.java'
s = open(p).read()
lines = s.split('\n')
for ln in (1645, 1674, 1724):
    idx = ln - 1
    assert 'facing' in lines[idx], lines[idx]
    lines[idx] = lines[idx].replace('facing', 'taskFacing')
for i in range(1762, 1806):
    if 'minePos' in lines[i]:
        lines[i] = lines[i].replace('minePos', 'workerMinePos')
open(p, 'w').write('\n'.join(lines))
print("done")
