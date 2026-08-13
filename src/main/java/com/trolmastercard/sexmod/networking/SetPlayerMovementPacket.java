package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;







import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SetPlayerMovementPacket implements IMessage {
   boolean isValid;
   boolean isSprinting;

   public SetPlayerMovementPacket(boolean var1) {
      this.isSprinting = var1;
      this.isValid = true;
   }

   public SetPlayerMovementPacket() {
      this.isValid = false;
   }

   public void fromBytes(ByteBuf var1) {
      this.isSprinting = var1.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.isSprinting);
      this.isValid = true;
   }

   public static class Handler implements IMessageHandler<SetPlayerMovementPacket, IMessage> {
      public IMessage onMessage(SetPlayerMovementPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.CLIENT) {
            HandlePlayerMovement.setMovementLock(var1.isSprinting);

            try {
               Minecraft.getMinecraft().player.setVelocity(0.0, 0.0, 0.0);
            } catch (Exception var3) {
            }

            if (var1.isSprinting) {
               HornyMeterHud.hideHornyMeter();
            }

            return null;
         } else {
            System.out.println("received an invalid message @SetPlayerMovement :(");
            return null;
         }
      }

   }
}
