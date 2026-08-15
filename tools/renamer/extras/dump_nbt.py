import struct, zlib, gzip, io, os, sys, re

class NBT:
    pass

def parse_nbt(buf, off=0):
    def read(off, n):
        return buf[off:off + n], off + n
    def read_name(off):
        tag = buf[off]; off += 1
        if tag == 0:
            return None, off
        ln = struct.unpack('>H', buf[off:off+2])[0]; off += 2
        name = buf[off:off+ln].decode('utf-8'); off += ln
        return name, off, tag
    def parse(off, tag):
        if tag == 1:
            return struct.unpack('>b', buf[off:off+1])[0], off+1
        if tag == 2:
            return struct.unpack('>h', buf[off:off+2])[0], off+2
        if tag == 3:
            return struct.unpack('>i', buf[off:off+4])[0], off+4
        if tag == 4:
            return struct.unpack('>q', buf[off:off+8])[0], off+8
        if tag == 5:
            return struct.unpack('>f', buf[off:off+4])[0], off+4
        if tag == 6:
            return struct.unpack('>d', buf[off:off+8])[0], off+8
        if tag == 7:
            ln = struct.unpack('>i', buf[off:off+4])[0]; off += 4
            return list(buf[off:off+ln]), off+ln
        if tag == 8:
            ln = struct.unpack('>h', buf[off:off+2])[0]; off += 2
            return buf[off:off+ln].decode('utf-8'), off+ln
        if tag == 9:
            lt = buf[off]; off += 1
            ln = struct.unpack('>i', buf[off:off+4])[0]; off += 4
            out = []
            for _ in range(ln):
                v, off = parse(off, lt)
                out.append(v)
            return out, off
        if tag == 10:
            out = {}
            while True:
                t = buf[off]; off += 1
                if t == 0:
                    break
                ln = struct.unpack('>h', buf[off:off+2])[0]; off += 2
                name = buf[off:off+ln].decode('utf-8'); off += ln
                v, off = parse(off, t)
                out[name] = v
            return out, off
        if tag == 11:
            ln = struct.unpack('>i', buf[off:off+4])[0]; off += 4
            return list(struct.unpack('>' + 'i'*ln, buf[off:off+4*ln])), off+4*ln
        if tag == 12:
            ln = struct.unpack('>i', buf[off:off+4])[0]; off += 4
            return list(struct.unpack('>' + 'q'*ln, buf[off:off+8*ln])), off+8*ln
        raise ValueError(f"unknown tag {tag}")

    tag = buf[off]
    if tag != 10:
        raise ValueError(f"root not compound: {tag}")
    _, off, _ = read_name(off)  # skip root name
    return parse(off, 10)[0]

def chunk_data(path, idx):
    with open(path, 'rb') as f:
        header = f.read(4096)
        o = int.from_bytes(header[idx*4:idx*4+3], 'big') * 4096
        if o == 0:
            return None
        f.seek(o)
        sz = int.from_bytes(f.read(4), 'big')
        comp = f.read(1)[0]
        body = f.read(sz - 1)
        if comp == 2:
            return zlib.decompress(body)
        if comp == 1:
            return gzip.GzipFile(fileobj=io.BytesIO(body)).read()
        if comp == 4:  # lz4
            import lz4.block
            return lz4.block.decompress(body)
        return None

def main(base, want_id=None):
    for root, dirs, files in os.walk(base):
        for fn in sorted(files):
            if not fn.endswith('.mca'):
                continue
            path = os.path.join(root, fn)
            m = re.search(r'r\.(-?\d+)\.(-?\d+)\.mca', fn)
            rx, rz = int(m.group(1)), int(m.group(2))
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
                    nbt = parse_nbt(raw)
                except Exception as e:
                    continue
                cx = i % 32 + rx * 32
                cz = i // 32 + rz * 32
                for e in nbt.get('Entities', []):
                    eid = e.get('id', '?')
                    if want_id and want_id not in eid:
                        continue
                    pos = e.get('Pos')
                    pos_s = f"({pos[0]:.1f},{pos[1]:.1f},{pos[2]:.1f})" if pos else "?"
                    girlid = e.get('girlID', e.get('girlUUID', '?'))
                    extra = []
                    for k in ('sexmod:despawned', 'sexmod:customname'):
                        if k in e:
                            extra.append(f"{k}={e[k]}")
                    print(f"{fn}[{cx},{cz}] {eid} girlID={girlid} Pos={pos_s} {' '.join(extra)}")

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None)