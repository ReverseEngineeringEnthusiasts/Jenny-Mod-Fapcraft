package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook.EnumFlags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class TeleportPlayerPacket implements IMessage {
   boolean b;
   String c;
   Vec3d e;
   float a;
   float d;

   public TeleportPlayerPacket() {
      this.b = false;
   }

   public TeleportPlayerPacket(String var1, Vec3d var2) {
      this.c = var1;
      this.e = var2;
      this.a = 0.0F;
      this.d = 0.0F;
      this.b = true;
   }

   public TeleportPlayerPacket(String var1, Vec3d var2, float var3, float var4) {
      this.c = var1;
      this.e = var2;
      this.a = var3;
      this.d = var4;
      this.b = true;
   }

   public TeleportPlayerPacket(String var1, double var2, double var4, double var6, float var8, float var9) {
      this.c = var1;
      this.e = new Vec3d(var2, var4, var6);
      this.a = var8;
      this.d = var9;
      this.b = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = ByteBufUtils.readUTF8String(var1);
      this.e = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.a = var1.readFloat();
      this.d = var1.readFloat();
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c);
      var1.writeDouble(this.e.field_72450_a);
      var1.writeDouble(this.e.field_72448_b);
      var1.writeDouble(this.e.field_72449_c);
      var1.writeFloat(this.a);
      var1.writeFloat(this.d);
      this.b = true;
   }

   public static class Handler implements IMessageHandler<TeleportPlayerPacket, IMessage> {
      public IMessage onMessage(TeleportPlayerPacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .func_152344_a(
                  () -> {
                     try {
                        System.out.println("teleporting player " + var1.c + " to " + var1.e);
                        EntityPlayerMP var1x = FMLCommonHandler.instance().getMinecraftServerInstance().func_184103_al().func_177451_a(UUID.fromString(var1.c));
                        var1.a = MathHelper.func_76142_g(var1.a);
                        var1.d = MathHelper.func_76142_g(var1.d);
                        var1x.func_70012_b(var1.e.field_72450_a, var1.e.field_72448_b, var1.e.field_72449_c, var1.a, var1.d);
                        var1x.func_70034_d(var1.a);
                        var1x.field_70159_w = 0.0;
                        var1x.field_70181_x = 0.0;
                        var1x.field_70179_y = 0.0;
                        var1x.field_71135_a
                           .func_175089_a(var1.e.field_72450_a, var1.e.field_72448_b, var1.e.field_72449_c, var1.a, var1.d, EnumSet.noneOf(EnumFlags.class));
                     } catch (Exception var2x) {
                        System.out.println("couldn't find player with UUID: " + var1.c);
                        System.out.println("could only find the following players:");
                        System.out.println(FMLCommonHandler.instance().getMinecraftServerInstance().func_184103_al().func_181058_b(true));
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @TeleportPlayer :(");
            return null;
         }
      }

   }
}
