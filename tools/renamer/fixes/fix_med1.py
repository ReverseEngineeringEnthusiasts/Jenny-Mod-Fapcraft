import re

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'

def sub_file(path, pairs):
    s = open(path).read()
    for old, new in pairs:
        assert old in s, (path, old[:80])
        s = s.replace(old, new)
    open(path, 'w').write(s)
    print('fixed', path)

# M1a: KoboldEntity lerp-end deref
sub_file(base + 'entity/KoboldEntity.java', [
    ("""         EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         this.setYawRotation(player.rotationYaw + 180.0F);
         this.entityDataManager.set(IS_ANCHORED, true);
         player.noClip = true;
         player.setNoGravity(true);""",
     """         EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (player == null) {
            this.a2 = false;
            return true;
         }

         this.setYawRotation(player.rotationYaw + 180.0F);
         this.entityDataManager.set(IS_ANCHORED, true);
         player.noClip = true;
         player.setNoGravity(true);"""),
])

# M1b: BiaEntity lerp-end deref
sub_file(base + 'entity/BiaEntity.java', [
    ("""            this.yFlag = false;
            this.ag = 0;
            this.setYawRotation(this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID()).rotationYaw + 180.0F);
            this.entityDataManager.set(IS_ANCHORED, true);""",
     """            this.yFlag = false;
            this.ag = 0;
            EntityPlayer lerpPlayer = this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID());
            if (lerpPlayer != null) {
               this.setYawRotation(lerpPlayer.rotationYaw + 180.0F);
            }

            this.entityDataManager.set(IS_ANCHORED, true);"""),
])

# M1c: LunaEntity lerp-end deref
sub_file(base + 'entity/LunaEntity.java', [
    ("""            this.ac = false;
            this.aw = 0;
            this.setYawRotation(this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID()).rotationYaw + 180.0F);
            this.entityDataManager.set(IS_ANCHORED, true);""",
     """            this.ac = false;
            this.aw = 0;
            EntityPlayer lerpPlayer = this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID());
            if (lerpPlayer != null) {
               this.setYawRotation(lerpPlayer.rotationYaw + 180.0F);
            }

            this.entityDataManager.set(IS_ANCHORED, true);"""),
])

# M1d: JennyEntity lerp-end deref
sub_file(base + 'entity/JennyEntity.java', [
    ("""            this.ab = false;
            this.ac = 0;
            this.setYawRotation(this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID()).rotationYaw + 180.0F);
            this.entityDataManager.set(BaseGirlEntity.IS_ANCHORED, true);""",
     """            this.ab = false;
            this.ac = 0;
            EntityPlayer lerpPlayer = this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID());
            if (lerpPlayer != null) {
               this.setYawRotation(lerpPlayer.rotationYaw + 180.0F);
            }

            this.entityDataManager.set(BaseGirlEntity.IS_ANCHORED, true);"""),
])

# M3: JennyEntity af snap-in double lookup
sub_file(base + 'entity/JennyEntity.java', [
    ("""      if (this.af && player != null && player.getPositionVector().distanceTo(this.getPositionVector()) < 0.5) {
         this.af = false;
         this.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, this.world.getClosestPlayerToEntity(this, 15.0).getPersistentID().toString());
         EntityPlayerMP playerMP = this.getServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID());
         this.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, playerMP.getPersistentID().toString());""",
     """      if (this.af && player != null && player.getPositionVector().distanceTo(this.getPositionVector()) < 0.5) {
         this.af = false;
         EntityPlayerMP playerMP = this.getServer().getPlayerList().getPlayerByUUID(player.getPersistentID());
         if (playerMP == null) {
            return;
         }

         this.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, playerMP.getPersistentID().toString());"""),
])

print('done')
