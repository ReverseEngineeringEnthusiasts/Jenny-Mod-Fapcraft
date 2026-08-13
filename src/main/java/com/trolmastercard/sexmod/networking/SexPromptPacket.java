package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SexPromptPacket implements IMessage {
   boolean e = false;
   String c;
   UUID b;
   UUID a;
   boolean d;

   public SexPromptPacket() {
   }

   public SexPromptPacket(String var1, UUID var2, UUID var3, boolean var4) {
      this.c = var1;
      this.b = var2;
      this.a = var3;
      this.d = var4;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = ByteBufUtils.readUTF8String(var1);
      this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.d = var1.readBoolean();
      this.e = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c);
      ByteBufUtils.writeUTF8String(var1, this.b.toString());
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      var1.writeBoolean(this.d);
   }

   public static class Handler implements IMessageHandler<SexPromptPacket, IMessage> {
      public IMessage onMessage(SexPromptPacket var1, MessageContext var2) {
         if (!var1.e) {
            System.out.println("received an invalid message @SexPrompt :(");
            return null;
         } else if (var2.side.equals(Side.CLIENT)) {
            GenderSwapScreen.a.a(new GenderSwapScreen.a(var1.c, var1.b, var1.a, var1.d));
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               World var2x = var2.getServerHandler().player.world;
               EntityPlayer var3 = var2x.getPlayerEntityByUUID(var1.a);
               EntityPlayer var4 = var2x.getPlayerEntityByUUID(var1.b);
               if (var3 == null) {
                  System.out.println("Sex prompt invalid -> female player not found");
               } else if (var4 == null) {
                  System.out.println("Sex prompt invalid -> male player not found");
               } else {
                  PacketHandler.b.sendTo(new SexPromptPacket(var1.c, var1.b, var1.a, var1.d), (EntityPlayerMP)(var1.d ? var3 : var4));
               }
            });
            return null;
         }
      }

   }
}
