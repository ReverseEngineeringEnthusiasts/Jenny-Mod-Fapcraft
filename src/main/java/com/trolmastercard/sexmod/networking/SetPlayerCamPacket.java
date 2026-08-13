package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SetPlayerCamPacket implements IMessage {
   boolean d = false;
   float a;
   float b;
   int c;

   public SetPlayerCamPacket() {
   }

   public SetPlayerCamPacket(float var1, float var2, int var3) {
      this.a = var1;
      this.b = var2;
      this.c = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = var1.readFloat();
      this.b = var1.readFloat();
      this.c = var1.readInt();
      this.d = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeFloat(this.a);
      var1.writeFloat(this.b);
      var1.writeInt(this.c);
   }

   public static class Handler implements IMessageHandler<SetPlayerCamPacket, IMessage> {
      public IMessage onMessage(SetPlayerCamPacket var1, MessageContext var2) {
         if (var1.d && var2.side == Side.CLIENT) {
            System.out.println(Thread.currentThread().getName());
            Minecraft var3 = Minecraft.getMinecraft();
            var3.addScheduledTask(() -> {
               var3.gameSettings.thirdPersonView = var1.c;
               EntityPlayerSP var2x = var3.player;
               var2x.rotationYaw = var1.b;
               var2x.prevRotationYaw = var1.b;
               var2x.prevRotationYawHead = var1.b;
               var2x.rotationYawHead = var1.b;
               var2x.renderYawOffset = var1.b;
               var2x.rotationPitch = var1.a;
               var2x.prevRotationPitch = var1.a;
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetPlayerCam :(");
            return null;
         }
      }

   }
}
