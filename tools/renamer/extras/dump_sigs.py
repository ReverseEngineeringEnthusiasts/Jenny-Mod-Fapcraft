import sys, glob, os
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'core'))
from rename_varN import lex, scope_scan
for path in sorted(glob.glob(sys.argv[1] + '/*.java')):
    src = open(path).read()
    toks = lex(src)
    scopes, errors = scope_scan(toks)
    print('==', path.split('/')[-1])
    for sc in scopes:
        kind = sc['kind']
        if kind == 'method':
            print(f"  {sc['sig']} params={sc['params']}")
        elif kind == 'lambda':
            p = sc['parent']
            pk = p['key'] if p and 'key' in p else '?'
            print(f"  LAMBDA parent={pk} params={sc['params']}")
    if errors:
        print('  ERRORS:', errors)
