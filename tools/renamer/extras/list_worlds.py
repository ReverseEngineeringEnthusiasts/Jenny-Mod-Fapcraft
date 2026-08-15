import os, glob, sys
from nbtlib import load

base = sys.argv[1]
for d in sorted(os.listdir(base)):
    p = os.path.join(base, d)
    if not os.path.isdir(p):
        continue
    try:
        nbt = load(os.path.join(p, 'level.dat'))
        print(d, '->', repr(str(nbt['Data']['LevelName'])))
    except Exception as e:
        print(d, 'ERR', e)