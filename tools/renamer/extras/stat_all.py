import os, sys, re
from collections import Counter
sys.path.insert(0, '/tmp')
from dump_nbt import chunk_data, parse_nbt

base = sys.argv[1]
counts = Counter()
per_world = {}
for root, dirs, files in os.walk(base):
    for fn in sorted(files):
        if not fn.endswith('.mca'):
            continue
        path = os.path.join(root, fn)
        world = os.path.basename(os.path.dirname(os.path.dirname(path)))
        with open(path, 'rb') as f:
            header = f.read(4096)
        n = 0
        for i in range(1024):
            o = int.from_bytes(header[i*4:i*4+3], 'big') * 4096
            if o == 0:
                continue
            try:
                raw = chunk_data(path, i)
                if raw is None:
                    continue
                nbt = parse_nbt(raw)
            except Exception:
                continue
            for e in nbt.get('Entities', []):
                n += 1
                counts[e.get('id', '?')] += 1
        per_world.setdefault(world, []).append((fn, n))
for w, items in per_world.items():
    print(w, sum(x[1] for x in items), 'entities', 'in', len(items), 'regions')
print('--- by id ---')
for k, v in counts.most_common(30):
    print(f'{v:5d}  {k}')