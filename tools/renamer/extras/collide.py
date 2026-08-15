import sys, re
sys.path.insert(0, '/tmp')
import vartool
src = open(sys.argv[1]).read()
sig = sys.argv[2]
renames = dict(x.split('=') for x in sys.argv[3].split(','))
spec = {"scopes": [{"sig": sig, "renames": renames}]}
root = vartool.scan(src)
for sc in vartool.iter_scopes(root):
    if sc['kind'] not in ('method', 'lambda', 'init'):
        continue
    region = re.sub(r'\s+', ' ', src[sc['sig_start']:sc['end']]).strip()
    if re.search(sig, region):
        start = sc['sig_start']
        excl = []
        vartool.descendant_spans(sc, excl)
        ranges = []
        for (a, b) in vartool.code_ranges(src, start, sc['end']):
            if any(x <= a and b <= y for (x, y) in excl):
                continue
            ranges.append((a, b))
        idents = set()
        for (a, b) in ranges:
            idents.update(re.findall(r'[A-Za-z_$][\w$]*', src[a:b]))
        print("IDENT SET:", sorted(idents))
        for old, new in renames.items():
            print(f"  {old} -> {new}: {'COLLIDES' if new in idents else 'ok'}")
        break
