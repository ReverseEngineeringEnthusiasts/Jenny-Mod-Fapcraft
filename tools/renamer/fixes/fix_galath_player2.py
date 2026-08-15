import re

p = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/GalathPlayerEntity.java'
s = open(p).read()
lines = s.split('\n')
# flapControlled case: rename mc/player to flapMc/flapPlayer
for i, line in enumerate(lines):
    if 'Minecraft mc = Minecraft.getMinecraft();' in line:
        lines[i] = line.replace('mc', 'flapMc', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bmc\b', 'flapMc', lines[j])
            j += 1
        break
for i, line in enumerate(lines):
    if 'EntityPlayerSP player = flapMc.player;' in line:
        lines[i] = line.replace('player', 'flapPlayer', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bplayer\b', 'flapPlayer', lines[j])
            j += 1
        break
open(p, 'w').write('\n'.join(lines))
print('done')
