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
   boolean a = false;
   BlockPos b;

   public CancelTaskPacket() {
   }

   public CancelTaskPacket(BlockPos var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = new BlockPos(var1.readInt(), var1.readInt(), var1.readInt());
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.b.getX());
      var1.writeInt(this.b.getY());
      var1.writeInt(this.b.getZ());
   }

   public static class Handler implements IMessageHandler<CancelTaskPacket, IMessage> {
      public IMessage onMessage(CancelTaskPacket var1, MessageContext var2) {
         if (var1.a && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID var2x = KoboldManager.getTribeUUID(var2.getServerHandler().player.getPersistentID());
               if (var2x != null) {
                  HashSet var3 = KoboldManager.c(var2x, var1.b);
                  if (!var3.isEmpty()) {
                     PacketHandler.b.sendTo(new SendBlocksPacket(var3, false), var2.getServerHandler().player);
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
