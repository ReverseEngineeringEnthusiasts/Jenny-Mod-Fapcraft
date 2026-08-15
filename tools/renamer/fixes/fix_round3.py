import re

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'

def apply(path, edits):
    s = open(path).read()
    for old, new in edits:
        assert old in s, (path, old[:70])
        s = s.replace(old, new)
    open(path, 'w').write(s)
    print('fixed', path)

# GalathEntity 978: rename the second rightPos decl (the mirror section) — it's the
# declaration AFTER the tempPos use. Rename it to rightPos2? Better: the second block
# is the LEFT hand's mirror; rename its decl+uses to mirrorRightPos.
p = base + 'entity/GalathEntity.java'
s = open(p).read()
lines = s.split('\n')
# find the declaration line "Vec3d rightPos = tempPos.add(...)" (the second occurrence)
decl_idx = None
count = 0
for i, line in enumerate(lines):
    if line.startswith('            Vec3d rightPos = tempPos.add('):
        count += 1
        if count == 2:
            decl_idx = i
            break
assert decl_idx is not None
lines[decl_idx] = lines[decl_idx].replace('Vec3d rightPos', 'Vec3d mirrorRightPos', 1)
for i in range(decl_idx + 1, len(lines)):
    if 'case "' in lines[i] or lines[i].strip().startswith('void ') or lines[i].strip().startswith('@Override'):
        break
    lines[i] = re.sub(r'\brightPos\b', 'mirrorRightPos', lines[i])
open(p, 'w').write('\n'.join(lines))
print('galath rightPos done')

# GalathPlayerEntity 433-435: corrupt-case player/yaw/pos
apply(base + 'entity/GalathPlayerEntity.java', [
    ('EntityPlayerSP player = Minecraft.getMinecraft().player;\n                     float yaw = this.getYawRotation() + 220.0F;\n                     Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - player.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());',
     'EntityPlayerSP corruptPlayer = Minecraft.getMinecraft().player;\n                     float corruptYaw = this.getYawRotation() + 220.0F;\n                     Vec3d corruptPos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - corruptPlayer.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());'),
    ('sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, yaw, 15.0F));',
     'sendToServer(new TeleportPlayerPacket(corruptPlayer.getPersistentID().toString(), corruptPos, corruptYaw, 15.0F));'),
])

# ElliePlayerEntity 397: 'dashDone' player (EntityPlayer) vs 406 'hugDone' player (EntityPlayerSP) — same lambda scope
apply(base + 'entity/ElliePlayerEntity.java', [
    ('case "dashDone":\n               this.setCurrentAction(Action.HUG);\n               EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);',
     'case "dashDone":\n               this.setCurrentAction(Action.HUG);\n               EntityPlayer closestPlayer = this.world.getClosestPlayerToEntity(this, 15.0);'),
])
# rename uses of that dashDone player within its case: yaw = player.rotationYaw; this.rotationYaw = yaw; ... until next case
s = open(base + 'entity/ElliePlayerEntity.java').read()
lines = s.split('\n')
for i, line in enumerate(lines):
    if 'EntityPlayer closestPlayer = this.world.getClosestPlayerToEntity' in line:
        j = i + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\bplayer\b', 'closestPlayer', lines[j])
            j += 1
        break
open(base + 'entity/ElliePlayerEntity.java', 'w').write('\n'.join(lines))
print('ellie done')
