package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to cancel a tribe mining task. Sent by the
 * dragon-staff UI when the player erases a highlighted mining target (block
 * marker) — the server responds with a {@link SendBlocksPacket} to un-highlight
 * the removed blocks on the client.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Resolves the
 * sender's tribe via {@link KoboldManager#getTribeUUID}, removes the task that
 * contained {@code taskPos}
 * ({@link KoboldManager#removeMiningTargetsFor}) and replies to the sender with
 * the removed blocks so the markers disappear ({@code SendBlocksPacket} with
 * {@code isBreaking=false}).
 */
public class CancelTaskPacket implements IMessage {
   boolean isValid = false;
   BlockPos taskPos;

   public CancelTaskPacket() {
   }

   public CancelTaskPacket(BlockPos taskPos) {
      this.taskPos = taskPos;
   }

   public void fromBytes(ByteBuf buf) {
      this.taskPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.taskPos.getX());
      buf.writeInt(this.taskPos.getY());
      buf.writeInt(this.taskPos.getZ());
   }

   public static class Handler implements IMessageHandler<CancelTaskPacket, IMessage> {
      public IMessage onMessage(CancelTaskPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID tribeUuid = KoboldManager.getTribeUUID(ctx.getServerHandler().player.getPersistentID());
               if (tribeUuid != null) {
                  HashSet targets = KoboldManager.removeMiningTargetsFor(tribeUuid, packet.taskPos);
                  if (!targets.isEmpty()) {
                     PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(targets, false), ctx.getServerHandler().player);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid Message @CancelTask :(");
            return null;
         }
      }

   }
}
