#!/usr/bin/env python3
"""Dump sexmod girl entities + relevant NBT from an Anvil region (1.12.2)."""
import zlib, gzip, io, sys, os, re
from nbtlib import parse_nbt

def read_chunk_nbt(data):
    length = int.from_bytes(data[:4], 'big')
    comp = data[4]
    body = data[5:5 + length]
    if comp == 1:
        return parse_nbt(gzip.GzipFile(fileobj=io.BytesIO(body)))
    elif comp == 2:
        return parse_nbt(zlib.decompress(body))
    return None

def walk_region(path):
    with open(path, 'rb') as f:
        header = f.read(4096)
        for i in range(1024):
            o = int.from_bytes(header[i * 4:i * 4 + 3], 'big') * 4096
            if o == 0:
                continue
            f.seek(o)
            sz = int.from_bytes(f.read(4), 'big')
            f.seek(o)
            data = f.read(5 + sz)
            try:
                chunk = read_chunk_nbt(data)
            except Exception:
                continue
            if chunk is not None:
                yield i, chunk

def dump_girls(path):
    m = re.search(r'r\.(-?\d+)\.(-?\d+)\.mca', path)
    rx, rz = int(m.group(1)), int(m.group(2))
    print(f"=== {os.path.basename(path)} (region {rx},{rz}) ===")
    for idx, chunk in walk_region(path):
        cx = (idx % 32) + rx * 32
        cz = (idx // 32) + rz * 32
        for e in chunk.get('Entities', []):
            eid = str(e.get('id', '?'))
            if 'sexmod' not in eid:
                continue
            pos = e.get('Pos', None)
            pos_s = f"({float(pos[0]):.1f},{float(pos[1]):.1f},{float(pos[2]):.1f})" if pos else "?"
            girlid = str(e.get('girlID', '?'))
            extra = []
            for k in ('sexmod:despawned', 'sexmod:customname'):
                if k in e:
                    extra.append(f"{k}={e[k]}")
            print(f"  [{cx},{cz}] {eid} girlID={girlid} Pos={pos_s} {' '.join(extra)}")

if __name__ == '__main__':
    base = sys.argv[1]
    for root, dirs, files in os.walk(base):
        for fn in sorted(files):
            if fn.endswith('.mca'):
                try:
                    dump_girls(os.path.join(root, fn))
                except Exception as ex:
                    print(f"  error {fn}: {ex}")