package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CancelTaskPacket implements IMessage {
   boolean isValid = false;
   BlockPos taskPos;

   public CancelTaskPacket() {
   }

   public CancelTaskPacket(BlockPos var1) {
      this.taskPos = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.taskPos = new BlockPos(var1.readInt(), var1.readInt(), var1.readInt());
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.taskPos.getX());
      var1.writeInt(this.taskPos.getY());
      var1.writeInt(this.taskPos.getZ());
   }

   public static class Handler implements IMessageHandler<CancelTaskPacket, IMessage> {
      public IMessage onMessage(CancelTaskPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID var2x = KoboldManager.getTribeUUID(var2.getServerHandler().player.getPersistentID());
               if (var2x != null) {
                  HashSet var3 = KoboldManager.c(var2x, var1.taskPos);
                  if (!var3.isEmpty()) {
                     PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var3, false), var2.getServerHandler().player);
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
