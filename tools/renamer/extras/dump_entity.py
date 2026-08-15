import os, sys, re
sys.path.insert(0, '/tmp')
from dump_nbt import chunk_data, parse_nbt

def get_level(nbt):
    return nbt.get('Level', nbt)

base = sys.argv[1]
want_id = sys.argv[2] if len(sys.argv) > 2 else 'sexmod:bia'
for root, dirs, files in os.walk(base):
    for fn in sorted(files):
        if not fn.endswith('.mca'):
            continue
        path = os.path.join(root, fn)
        world = os.path.basename(os.path.dirname(os.path.dirname(path)))
        with open(path, 'rb') as f:
            header = f.read(4096)
        for i in range(1024):
            o = int.from_bytes(header[i*4:i*4+3], 'big') * 4096
            if o == 0:
                continue
            try:
                raw = chunk_data(path, i)
                if raw is None:
                    continue
                lvl = get_level(parse_nbt(raw))
            except Exception:
                continue
            for e in lvl.get('Entities', []):
                eid = e.get('id', '?')
                if eid != want_id:
                    continue
                print(f"=== [{world}] {fn} chunk idx {i} {eid} ===")
                for k, v in sorted(e.items()):
                    if isinstance(v, list):
                        print(f"  {k}: {v}")
                    else:
                        print(f"  {k}: {str(v)[:200]}")
                print()