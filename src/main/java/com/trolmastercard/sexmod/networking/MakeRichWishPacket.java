package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MakeRichWishPacket implements IMessage {
   boolean b;
   Vec3d a;

   public MakeRichWishPacket() {
   }

   public MakeRichWishPacket(Vec3d var1) {
      this.a = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeDouble(this.a.x);
      var1.writeDouble(this.a.y);
      var1.writeDouble(this.a.z);
   }

   public static class Handler implements IMessageHandler<MakeRichWishPacket, IMessage> {
      public IMessage onMessage(MakeRichWishPacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     World var2x = var2.getServerHandler().player.world;
                     EntityItem var3 = new EntityItem(
                        var2x,
                        var1.a.x,
                        var1.a.y,
                        var1.a.z,
                        new ItemStack(Items.DIAMOND, Reference.f.nextInt(2) + 1)
                     );
                     EntityItem var4 = new EntityItem(
                        var2x,
                        var1.a.x,
                        var1.a.y,
                        var1.a.z,
                        new ItemStack(Items.EMERALD, Reference.f.nextInt(2) + 1)
                     );
                     EntityItem var5 = new EntityItem(
                        var2x,
                        var1.a.x,
                        var1.a.y,
                        var1.a.z,
                        new ItemStack(Items.GOLD_INGOT, Reference.f.nextInt(2) + 1)
                     );
                     var2x.spawnEntity(var3);
                     var2x.spawnEntity(var4);
                     var2x.spawnEntity(var5);
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @MakeRichWish :(");
            return null;
         }
      }

   }
}
