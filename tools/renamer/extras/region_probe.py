import zlib, gzip, io, sys
from nbtlib import parse_nbt

path = sys.argv[1]
with open(path, 'rb') as f:
    header = f.read(4096)
    for i in range(1024):
        o = int.from_bytes(header[i * 4:i * 4 + 3], 'big') * 4096
        if o == 0:
            continue
        f.seek(o)
        sz = int.from_bytes(f.read(4), 'big')
        comp = f.read(1)[0]
        body = f.read(sz - 1)
        print(f"chunk {i}: size={sz} comp={comp} first={body[:8].hex()}")
        if comp == 2:
            raw = zlib.decompress(body)
        elif comp == 1:
            raw = gzip.GzipFile(fileobj=io.BytesIO(body)).read()
        else:
            print("  unknown compression")
            continue
        print(f"  decompressed {len(raw)} bytes, first={raw[:8].hex()}")
        try:
            nbt = parse_nbt(raw)
            ents = nbt.get('Entities', [])
            print(f"  parsed OK, entities={len(ents)}")
            for e in ents:
                print("   ", e.get('id'))
        except Exception as e:
            print(f"  parse error: {e!r}")
        break