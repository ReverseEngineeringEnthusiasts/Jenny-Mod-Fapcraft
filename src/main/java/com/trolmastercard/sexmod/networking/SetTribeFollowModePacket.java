package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>Role.</b> CLIENT->SERVER toggle of the tribe "follow/alerted" mode from the
 * dragon-staff UI.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Resolves the
 * sender's tribe and flips
 * {@link KoboldManager#setTribeFollowMode(UUID, boolean)}; the flag is read by
 * {@link KoboldManager#isTribeAlerted} and the kobold AI (follow the master vs.
 * idle). No tribe -> no-op.
 */
public class SetTribeFollowModePacket implements IMessage {
   boolean isValid = false;
   boolean followMode;

   public SetTribeFollowModePacket() {
   }

   public SetTribeFollowModePacket(boolean followMode) {
      this.followMode = followMode;
   }

   public void fromBytes(ByteBuf buf) {
      this.followMode = buf.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.followMode);
   }

   public static class Handler implements IMessageHandler<SetTribeFollowModePacket, IMessage> {
      public IMessage onMessage(SetTribeFollowModePacket packet, MessageContext ctx) {
         if (packet.isValid && !ctx.side.isClient()) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID tribeUuid = KoboldManager.getTribeUUID(ctx.getServerHandler().player.getPersistentID());
               if (tribeUuid != null) {
                  KoboldManager.setTribeFollowMode(tribeUuid, packet.followMode);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetTribeFollowMode :(");
            return null;
         }
      }

   }
}
