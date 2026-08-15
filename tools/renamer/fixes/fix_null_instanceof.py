import os, re

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'

sites = [
    'entity/GoblinEntity.java',
    'entity/BaseGirlEntity.java',
    'networking/UnknownPacket.java',
    'networking/DownloadServerModelPacket.java',
    'util/ServerWhitelistManager.java',
]

for rel in sites:
    p = base + rel
    s = open(p).read()
    n = s.count('null instanceof ClientProxy')
    if n == 0:
        print('skip (none):', rel)
        continue
    s = s.replace('null instanceof ClientProxy', 'Main.proxy instanceof ClientProxy')
    # add Main import if missing
    if 'import com.trolmastercard.sexmod.Main;' not in s:
        lines = s.split('\n')
        # insert after the package line or first import
        for i, line in enumerate(lines):
            if line.startswith('import '):
                lines.insert(i, 'import com.trolmastercard.sexmod.Main;')
                break
        s = '\n'.join(lines)
    open(p, 'w').write(s)
    print(f'fixed {n}x:', rel)
