import re

p = '<REPO>/src/main/java/com/trolmastercard/sexmod/client/SexSceneKeyHandler.java'
s = open(p).read()
# method-local renames: event, mc, playerUuid, girl, girlUuid, (player-girl block below)
lines = s.split('\n')
for i, line in enumerate(lines):
    line = line.replace('onKeyInput(KeyInputEvent var1)', 'onKeyInput(KeyInputEvent event)')
    line = line.replace('Minecraft var2 = Minecraft.getMinecraft();', 'Minecraft mc = Minecraft.getMinecraft();')
    line = line.replace('UUID var3 = var2.player.getPersistentID();', 'UUID playerUuid = mc.player.getPersistentID();')
    line = line.replace('for (BaseGirlEntity var5 : BaseGirlEntity.getGirlEntityList())', 'for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList())')
    line = line.replace('UUID var6 = var5.getInteractionPlayerUUID();', 'UUID girlUuid = girl.getInteractionPlayerUUID();')
    lines[i] = line
s = '\n'.join(lines)
# remaining var5/var6/var2/var3 uses
s = s.replace('var5.', 'girl.')
s = s.replace('var6', 'girlUuid')
s = s.replace('var2.', 'mc.')
s = s.replace('var3', 'playerUuid')
# player-girl tail block (var8)
s = s.replace('AbstractPlayerGirlEntity var8 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(playerUuid);', 'AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(playerUuid);')
s = s.replace('var8.', 'playerGirl.')
open(p, 'w').write(s)
print('keyhandler done')

# DownloadServerModelPacket: rename all varN per context
p = '<REPO>/src/main/java/com/trolmastercard/sexmod/networking/DownloadServerModelPacket.java'
s = open(p).read()
s = s.replace('DownloadServerModelPacket(boolean var1)', 'DownloadServerModelPacket(boolean isValid)')
s = s.replace('this.isValid = var1', 'this.isValid = isValid')
s = s.replace('var2x', 'ctx')
s = s.replace('var1x', 'packet')
s = s.replace('var3x', 'type')
s = s.replace('var4x', 'packets')
s = s.replace('var5x', 'typeName')
s = s.replace('var6x', 'typeEnding')
s = s.replace('var7x', 'serverDir')
open(p, 'w').write(s)
print('download part1 done')
