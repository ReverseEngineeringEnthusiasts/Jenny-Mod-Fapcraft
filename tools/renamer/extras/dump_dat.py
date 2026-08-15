import os, sys, zlib, io, gzip
sys.path.insert(0, '/tmp')

def parse_nbt_buf(buf):
    import struct
    def read_name(off):
        tag = buf[off]; off += 1
        if tag == 0:
            return None, off, tag
        ln = struct.unpack('>H', buf[off:off+2])[0]; off += 2
        return buf[off:off+ln].decode('utf-8'), off+ln, tag
    def parse(off, tag):
        if tag == 1: return struct.unpack('>b', buf[off:off+1])[0], off+1
        if tag == 2: return struct.unpack('>h', buf[off:off+2])[0], off+2
        if tag == 3: return struct.unpack('>i', buf[off:off+4])[0], off+4
        if tag == 4: return struct.unpack('>q', buf[off:off+8])[0], off+8
        if tag == 5: return struct.unpack('>f', buf[off:off+4])[0], off+4
        if tag == 6: return struct.unpack('>d', buf[off:off+8])[0], off+8
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
                if t == 0: break
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
        raise ValueError(f"tag {tag}")
    tag = buf[0]
    _, off, _ = read_name(0)
    return parse(off, 10)[0]

def read_gzip_nbt(path):
    with open(path, 'rb') as f:
        raw = f.read()
    if raw[:2] == b'\x1f\x8b':
        raw = gzip.GzipFile(fileobj=io.BytesIO(raw)).read()
    return parse_nbt_buf(raw)

def pp(tag, indent=0):
    pad = '  ' * indent
    if isinstance(tag, dict):
        for k, v in tag.items():
            if isinstance(v, (dict, list)):
                print(f"{pad}{k}:")
                pp(v, indent + 1)
            else:
                s = str(v)
                print(f"{pad}{k}: {s[:120]}")
    elif isinstance(tag, list):
        for v in tag[:20]:
            if isinstance(v, (dict, list)):
                pp(v, indent + 1)
            else:
                print(f"{pad}- {str(v)[:120]}")

if __name__ == '__main__':
    path = sys.argv[1]
    nbt = read_gzip_nbt(path)
    pp(nbt)