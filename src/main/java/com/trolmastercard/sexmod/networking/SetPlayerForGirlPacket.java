package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.JennyEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER "start a scene with this girl" request. Sent from
 * the interaction menu when the player picks an action; binds the player to the
 * girl as her interaction partner.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. For Jenny
 * additionally sets her {@code af} flag (scene-ready). Sets the girl's
 * interaction-player UUID — the data-manager sync then makes the girl follow
 * that player; the actual scene entry is triggered by the follow-up packets
 * ({@code KoboldStatePacket}/{@code ChangeDataParameterPacket} flow).
 */
public class SetPlayerForGirlPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   UUID playerUUID;

   public SetPlayerForGirlPacket() {
      this.isValid = false;
   }

   public SetPlayerForGirlPacket(UUID girlUUID, UUID playerUUID) {
      this.girlUUID = girlUUID;
      this.playerUUID = playerUUID;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<SetPlayerForGirlPacket, IMessage> {
      public IMessage onMessage(SetPlayerForGirlPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                  PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

                  try {
                     playerList.getPlayerByUUID(packet.playerUUID).getName();
                  } catch (NullPointerException exception) {
                     System.out.println("couldn't find player with UUID: " + packet.playerUUID);
                     System.out.println("could only find players with thsese UUID's:");

                     for (EntityPlayerMP player : playerList.getPlayers()) {
                        System.out.println(player.getName() + " " + player.getUniqueID());
                     }
                     continue;
                  }

                  if (girl instanceof JennyEntity) {
                     ((JennyEntity)girl).af = true;
                  }

                  girl.setInteractionPlayerUUID(packet.playerUUID);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetPlayerForGirl :(");
            return null;
         }
      }

   }
}
