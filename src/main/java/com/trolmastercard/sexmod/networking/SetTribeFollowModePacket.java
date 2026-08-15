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

   public SetTribeFollowModePacket(boolean var1) {
      this.followMode = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.followMode = var1.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.followMode);
   }

   public static class Handler implements IMessageHandler<SetTribeFollowModePacket, IMessage> {
      public IMessage onMessage(SetTribeFollowModePacket var1, MessageContext var2) {
         if (var1.isValid && !var2.side.isClient()) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID var2x = KoboldManager.getTribeUUID(var2.getServerHandler().player.getPersistentID());
               if (var2x != null) {
                  KoboldManager.setTribeFollowMode(var2x, var1.followMode);
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
