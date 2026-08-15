import os, struct, glob

def scan(path):
    with open(path, 'rb') as f:
        data = f.read()
    if len(data) < 8 or data[:4] != b'\xca\xfe\xba\xbe':
        return None
    pos = 8
    cp_count = struct.unpack('>H', data[pos:pos+2])[0]
    pos += 2
    cp = [None] * cp_count
    i = 1
    while i < cp_count:
        tag = data[pos]
        if tag == 1:
            ln = struct.unpack('>H', data[pos+1:pos+3])[0]
            cp[i] = data[pos+3:pos+3+ln].decode('utf-8', 'replace')
            pos += 3 + ln
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            pos += 5
        elif tag in (5, 6):
            pos += 9
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            pos += 3
        elif tag == 15:
            pos += 4
        else:
            return None
        i += 1
    pos += 6
    ifaces = struct.unpack('>H', data[pos:pos+2])[0]
    pos += 2 + 2 * ifaces
    fcount = struct.unpack('>H', data[pos:pos+2])[0]
    pos += 2
    fields = []
    for _ in range(fcount):
        acc = struct.unpack('>H', data[pos:pos+2])[0]
        name_idx = struct.unpack('>H', data[pos+2:pos+4])[0]
        desc_idx = struct.unpack('>H', data[pos+4:pos+6])[0]
        name = cp[name_idx] if name_idx < cp_count else None
        desc = cp[desc_idx] if desc_idx < cp_count else None
        pos += 6
        acount = struct.unpack('>H', data[pos:pos+2])[0]
        pos += 2
        for _ in range(acount):
            atag = data[pos]
            if atag == 1:
                ln = struct.unpack('>H', data[pos+1:pos+3])[0]
                pos += 3 + ln
            elif atag in (3, 4, 9, 10, 11, 12, 17, 18):
                pos += 5
            elif atag in (5, 6):
                pos += 9
            elif atag in (7, 8, 16, 19, 20):
                pos += 3
            elif atag == 15:
                pos += 4
            else:
                return None
        fields.append((acc, name, desc))
    return fields

base = '/tmp/orig/jar/com/trolmastercard/sexmod'
for p in sorted(glob.glob(base + '/*.class')):
    try:
        fields = scan(p)
    except Exception:
        continue
    if not fields:
        continue
    for acc, name, desc in fields:
        if name == 'j' and (acc & 0x0008) and desc in ('I', 'Z', 'F'):
            print(os.path.basename(p), 'static', name, desc)