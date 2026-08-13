package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.d3;







import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SetPlayerMovementPacket implements IMessage {
   boolean a;
   boolean b;

   public SetPlayerMovementPacket(boolean var1) {
      this.b = var1;
      this.a = true;
   }

   public SetPlayerMovementPacket() {
      this.a = false;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = var1.readBoolean();
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.b);
      this.a = true;
   }

   public static class Handler implements IMessageHandler<SetPlayerMovementPacket, IMessage> {
      public IMessage onMessage(SetPlayerMovementPacket var1, MessageContext var2) {
         if (var1.a && var2.side == Side.CLIENT) {
            d3.setMovementLock(var1.b);

            try {
               Minecraft.func_71410_x().field_71439_g.func_70016_h(0.0, 0.0, 0.0);
            } catch (Exception var3) {
            }

            if (var1.b) {
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
