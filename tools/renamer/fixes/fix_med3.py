base = '<REPO>/src/main/java/com/trolmastercard/sexmod/'

def sub_file(path, pairs):
    s = open(path).read()
    for old, new in pairs:
        assert old in s, (path, old[:80])
        s = s.replace(old, new)
    open(path, 'w').write(s)
    print('fixed', path)

# M8: SendChatMessagePacket girlList.get(0)
sub_file(base + 'networking/SendChatMessagePacket.java', [
    ("""                  () -> {
                     Vec3d pos = BaseGirlEntity.girlList(packet.playerUUID).get(0).getPreviousPosition();
                     PacketHandler.networkWrapper
                        .sendToAllAround(
                           new SendChatMessagePacket(packet.message, packet.channel, packet.playerUUID),
                           new TargetPoint(packet.channel, pos.x, pos.y, pos.z, 40.0)
                        );""",
     """                  () -> {
                     java.util.List girls = BaseGirlEntity.girlList(packet.playerUUID);
                     if (girls.isEmpty()) {
                        return;
                     }

                     Vec3d pos = ((BaseGirlEntity)girls.get(0)).getPreviousPosition();
                     PacketHandler.networkWrapper
                        .sendToAllAround(
                           new SendChatMessagePacket(packet.message, packet.channel, packet.playerUUID),
                           new TargetPoint(packet.channel, pos.x, pos.y, pos.z, 40.0)
                        );"""),
])

# M9: UploadModelStringPacket girl null guard
sub_file(base + 'networking/UploadModelStringPacket.java', [
    ("""               BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(packet.girlUUID);
               if (packet.partIds.size() > 0) {""",
     """               BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(packet.girlUUID);
               if (girl == null) {
                  return;
               }

               if (packet.partIds.size() > 0) {"""),
])

# M10: RemoveItemsPacket player null guard
sub_file(base + 'networking/RemoveItemsPacket.java', [
    ("""            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               InventoryPlayer inventory = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(packet.girlUUID).inventory;""",
     """            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               net.minecraft.entity.player.EntityPlayerMP target = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(packet.girlUUID);
               if (target == null) {
                  return;
               }

               InventoryPlayer inventory = target.inventory;"""),
])

print('done')
