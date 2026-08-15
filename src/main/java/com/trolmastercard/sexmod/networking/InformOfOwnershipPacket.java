package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.GirlSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> SERVER->CLIENT ownership-status notification for Galath/Manglelie.
 * Sent whenever the server-side ownership state of the player changes (owner
 * granted, removed, girl despawned, dimension change) so the client can mirror
 * it in {@link GirlSavedData#debugEnabled} — which gates the Galath coin UI and
 * "already owned" logic.
 * <p>
 * <b>Handler.</b> CLIENT-side; writes {@link GirlSavedData#debugEnabled} directly
 * (static field, thread-safe enough for the client thread).
 */
public class InformOfOwnershipPacket implements IMessage {
   boolean isValid = false;
   boolean isOwned;

   public InformOfOwnershipPacket() {
   }

   public InformOfOwnershipPacket(boolean isOwned) {
      this.isOwned = isOwned;
   }

   public void fromBytes(ByteBuf buf) {
      this.isOwned = buf.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isOwned);
   }

   public static class Handler implements IMessageHandler<InformOfOwnershipPacket, IMessage> {
      public IMessage onMessage(InformOfOwnershipPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.CLIENT)) {
            GirlSavedData.debugEnabled = packet.isOwned;
            return null;
         } else {
            System.out.println("received an invalid message @InformOfOwnership :(");
            return null;
         }
      }

   }
}
