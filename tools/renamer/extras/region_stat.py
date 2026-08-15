import zlib, gzip, io, sys, re
from nbtlib import parse_nbt, String

def read_chunk_nbt(data):
    length = int.from_bytes(data[:4], 'big')
    comp = data[4]
    body = data[5:5 + length]
    if comp == 1:
        return parse_nbt(gzip.GzipFile(fileobj=io.BytesIO(body)))
    elif comp == 2:
        return parse_nbt(zlib.decompress(body))
    return None

path = sys.argv[1]
reg = sys.argv[2] if len(sys.argv) > 2 else 0
with open(path, 'rb') as f:
    header = f.read(4096)
    total_chunks = 0
    total_ents = 0
    ids = {}
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
        except Exception as e:
            print(f"chunk {i}: parse error {e}")
            continue
        if chunk is None:
            continue
        total_chunks += 1
        for e in chunk.get('Entities', []):
            total_ents += 1
            eid = str(e.get('id', '?'))
            ids[eid] = ids.get(eid, 0) + 1
    print(f"chunks parsed: {total_chunks}, entities: {total_ents}")
    for k, v in sorted(ids.items()):
        print(f"  {v:4d}  {k}")