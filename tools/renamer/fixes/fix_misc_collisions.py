import re

# GirlFollowGoal: find the outer 'dist' decl and rename the second one
p = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/ai/GirlFollowGoal.java'
s = open(p).read()
lines = s.split('\n')
for i, line in enumerate(lines):
    if 'double dist =' in line and i > 130:
        old = 'dist'
        lines[i] = line.replace('dist', 'followDist', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bdist\b', 'followDist', lines[j])
            j += 1
        break
open(p, 'w').write('\n'.join(lines))
print('girlfollow done')

# DownloadServerModelPacket: loop var 'packet' collides with the outer 'packet' (handler param captured in lambda)
p = '<REPO>/src/main/java/com/trolmastercard/sexmod/networking/DownloadServerModelPacket.java'
s = open(p).read()
lines = s.split('\n')
for i, line in enumerate(lines):
    if 'for (DownloadServerModelPacket packet :' in line:
        lines[i] = line.replace('DownloadServerModelPacket packet', 'DownloadServerModelPacket chunk', 1)
        j = i + 1
        while j < len(lines) and '}' not in lines[j][:2]:
            lines[j] = re.sub(r'\bpacket\b', 'chunk', lines[j])
            j += 1
        break
open(p, 'w').write('\n'.join(lines))
print('download done')
