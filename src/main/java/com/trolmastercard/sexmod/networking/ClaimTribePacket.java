package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to claim (form) a kobold tribe. Sent from
 * the dragon-staff UI when a player who found an unclaimed tribe enters a name.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. For every kobold of
 * the tribe that has no master yet it writes {@code MASTER} (player UUID) and the
 * tribe name ({@code aU}) into the data manager; then it announces the tribe
 * formation in chat to all players and enables follow mode +
 * {@link KoboldManager#assignMaster}. {@code CURRENT_ACTION} of the leader
 * (parsed as {@link EyeAndKoboldColor}) determines the chat color.
 * <p>
 * <b>Ordering.</b> Must be sent only for a tribe that exists on the server
 * (i.e. after the tribe was spawned and synced); the handler no-ops on unknown
 * tribes because {@link KoboldManager} lookups return empty data.
 */
public class ClaimTribePacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   UUID playerUUID;
   String tribeName;

   public ClaimTribePacket() {
   }

   public ClaimTribePacket(UUID girlUUID, UUID playerUUID, String tribeName) {
      this.girlUUID = girlUUID;
      this.playerUUID = playerUUID;
      this.tribeName = tribeName;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.tribeName = ByteBufUtils.readUTF8String(buf);
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.tribeName);
   }

   public static class Handler implements IMessageHandler<ClaimTribePacket, IMessage> {
      public IMessage onMessage(ClaimTribePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     List members = KoboldManager.getTribeMembersList(packet.girlUUID);
                     EyeAndKoboldColor color = null;

                     for (KoboldEntity kobold : (java.util.Collection<KoboldEntity>) (members) ) {
                        if (!kobold.hasMaster()) {
                           EntityDataManager dataManager = kobold.getDataManager();
                           dataManager.set(BaseGirlEntity.MASTER, packet.playerUUID.toString());
                           dataManager.set(KoboldEntity.aU, packet.tribeName);
                           color = EyeAndKoboldColor.valueOf((String)dataManager.get(KoboldEntity.CURRENT_ACTION));
                        }
                     }

                     if (color != null) {
                        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
                        String playerName = ctx.getServerHandler().player.getName();

                        for (EntityPlayer player : playerList.getPlayers()) {
                           player.sendMessage(
                              new TextComponentString(
                                 String.format("%s formed the " + color.getTextColor() + "%s " + TextFormatting.WHITE + "Tribe", playerName, packet.tribeName)
                              )
                           );
                        }

                        KoboldManager.setTribeFollowMode(packet.girlUUID, true);
                        KoboldManager.assignMaster(packet.girlUUID, ctx.getServerHandler().player.getPersistentID());
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @ClaimTribe :(");
            return null;
         }
      }

   }
}
