package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SetPlayerCamPacket implements IMessage {
   boolean isValid = false;
   float camX;
   float camY;
   int camMode;

   public SetPlayerCamPacket() {
   }

   public SetPlayerCamPacket(float var1, float var2, int var3) {
      this.camX = var1;
      this.camY = var2;
      this.camMode = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.camX = var1.readFloat();
      this.camY = var1.readFloat();
      this.camMode = var1.readInt();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeFloat(this.camX);
      var1.writeFloat(this.camY);
      var1.writeInt(this.camMode);
   }

   public static class Handler implements IMessageHandler<SetPlayerCamPacket, IMessage> {
      public IMessage onMessage(SetPlayerCamPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.CLIENT) {
            System.out.println(Thread.currentThread().getName());
            Minecraft var3 = Minecraft.getMinecraft();
            var3.addScheduledTask(() -> {
               var3.gameSettings.thirdPersonView = var1.camMode;
               EntityPlayerSP var2x = var3.player;
               var2x.rotationYaw = var1.camY;
               var2x.prevRotationYaw = var1.camY;
               var2x.prevRotationYawHead = var1.camY;
               var2x.rotationYawHead = var1.camY;
               var2x.renderYawOffset = var1.camY;
               var2x.rotationPitch = var1.camX;
               var2x.prevRotationPitch = var1.camX;
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetPlayerCam :(");
            return null;
         }
      }

   }
}
