base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'

def sub_file(path, pairs):
    s = open(path).read()
    for old, new in pairs:
        assert old in s, (path, old[:80])
        s = s.replace(old, new)
    open(path, 'w').write(s)
    print('fixed', path)

# M2a: SlimePlayerEntity owner deref
sub_file(base + 'entity/SlimePlayerEntity.java', [
    ("""               EntityPlayer ownerPlayer = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
               ownerPlayer.setNoGravity(true);
               player.noClip = true;
               player.capabilities.isFlying = true;
               ownerPlayer.capabilities.isFlying = true;""",
     """               EntityPlayer ownerPlayer = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
               if (ownerPlayer != null) {
                  ownerPlayer.setNoGravity(true);
                  ownerPlayer.capabilities.isFlying = true;
               }

               player.noClip = true;
               player.capabilities.isFlying = true;"""),
])

# M2b: JennyPlayerEntity owner deref
sub_file(base + 'entity/JennyPlayerEntity.java', [
    ("""            player.capabilities.isFlying = true;
            this.world.getPlayerEntityByUUID(this.getOwnerUserUUID()).capabilities.isFlying = true;
            this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);""",
     """            player.capabilities.isFlying = true;
            EntityPlayer ownerPlayer = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
            if (ownerPlayer != null) {
               ownerPlayer.capabilities.isFlying = true;
            }

            this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);"""),
])

# M2c: ElliePlayerEntity owner deref (ai Optional.get)
sub_file(base + 'entity/ElliePlayerEntity.java', [
    ("""         EntityPlayerMP playerMP = (EntityPlayerMP)this.world.getPlayerEntityByUUID((UUID)((Optional)this.entityDataManager.get(ai)).get());
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), playerMP);
         player.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
         playerMP.capabilities.isFlying = true;
         player.capabilities.isFlying = true;""",
     """         Optional ownerOpt = (Optional)this.entityDataManager.get(ai);
         EntityPlayerMP playerMP = ownerOpt.isPresent() ? (EntityPlayerMP)this.world.getPlayerEntityByUUID((UUID)ownerOpt.get()) : null;
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
         if (playerMP != null) {
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), playerMP);
            playerMP.capabilities.isFlying = true;
         }

         player.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
         player.capabilities.isFlying = true;"""),
])

print('done')
