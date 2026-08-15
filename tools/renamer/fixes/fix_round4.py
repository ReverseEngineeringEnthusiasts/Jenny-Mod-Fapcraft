import re

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'

# GalathEntity: the rightPos decl that collides is the tempPos.add one
p = base + 'entity/GalathEntity.java'
s = open(p).read()
lines = s.split('\n')
decl_idx = None
for i, line in enumerate(lines):
    if 'Vec3d rightPos = tempPos.add(' in line:
        decl_idx = i
        break
assert decl_idx is not None, 'rightPos decl not found'
lines[decl_idx] = lines[decl_idx].replace('Vec3d rightPos', 'Vec3d mirrorRightPos', 1)
for i in range(decl_idx + 1, len(lines)):
    if lines[i].strip().startswith('}') or 'case "' in lines[i]:
        break
    lines[i] = re.sub(r'\brightPos\b', 'mirrorRightPos', lines[i])
open(p, 'w').write('\n'.join(lines))
print('galath rightPos done')

# GalathPlayerEntity corrupt case (already has corruptPlayer? check), fix remaining player/yaw/pos uses in that case
p = base + 'entity/GalathPlayerEntity.java'
s = open(p).read()
lines = s.split('\n')
for i, line in enumerate(lines):
    if 'EntityPlayerSP player = Minecraft.getMinecraft().player;' in line:
        lines[i] = line.replace('player', 'corruptPlayer', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bplayer\b', 'corruptPlayer', lines[j])
            j += 1
        break
for i, line in enumerate(lines):
    if 'float yaw = this.getYawRotation() + 220.0F;' in line:
        lines[i] = line.replace('yaw', 'corruptYaw', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\byaw\b', 'corruptYaw', lines[j])
            j += 1
        break
for i, line in enumerate(lines):
    if 'Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - corruptPlayer.getEyeHeight(), 0.4F)' in line:
        lines[i] = line.replace('Vec3d pos', 'Vec3d corruptPos', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bpos\b', 'corruptPos', lines[j])
            j += 1
        break
open(p, 'w').write('\n'.join(lines))
print('galathplayer corrupt done')

# ElliePlayerEntity dashDone case
p = base + 'entity/ElliePlayerEntity.java'
s = open(p).read()
lines = s.split('\n')
for i, line in enumerate(lines):
    if 'EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);' in line:
        lines[i] = line.replace('EntityPlayer player', 'EntityPlayer closestPlayer', 1)
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bplayer\b', 'closestPlayer', lines[j])
            j += 1
        break
open(p, 'w').write('\n'.join(lines))
print('ellie done')

# DownloadServerModelPacket: precise loop-var rename only
p = base + 'networking/DownloadServerModelPacket.java'
s = open(p).read()
lines = s.split('\n')
for i, line in enumerate(lines):
    if 'for (DownloadServerModelPacket var18 :' in line:
        lines[i] = line.replace('var18', 'chunkPacket', 1)
        # rename uses ONLY inside this loop (until the closing }); of the lambda at same indent)
        j = i + 1
        while j < len(lines):
            if lines[j].strip() == '});':
                break
            if 'var18' in lines[j]:
                lines[j] = lines[j].replace('var18', 'chunkPacket')
            j += 1
        break
open(p, 'w').write('\n'.join(lines))
print('download done')

# GirlInventorySlot 99: int -> String
p = base + 'client/gui/GirlInventorySlot.java'
s = open(p).read()
lines = s.split('\n')
print('slot line 99:', lines[98])
