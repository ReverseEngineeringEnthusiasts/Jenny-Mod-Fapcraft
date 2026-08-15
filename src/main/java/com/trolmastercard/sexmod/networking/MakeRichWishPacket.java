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

/**
 * <b>Role.</b> CLIENT->SERVER wish item from the Allies lamp's third use —
 * spawns diamonds, emeralds and gold ingots at the requested position.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Spawns three
 * {@link EntityItem}s (1-2 of each, via {@link Reference#RANDOM}) at
 * {@code wishPos} in the sender's world. Trusted client input — the position is
 * whatever the client sent; the handler does not re-validate distance or
 * ownership.
 */
public class MakeRichWishPacket implements IMessage {
   boolean isValid;
   Vec3d wishPos;

   public MakeRichWishPacket() {
   }

   public MakeRichWishPacket(Vec3d var1) {
      this.wishPos = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.wishPos = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeDouble(this.wishPos.x);
      var1.writeDouble(this.wishPos.y);
      var1.writeDouble(this.wishPos.z);
   }

   public static class Handler implements IMessageHandler<MakeRichWishPacket, IMessage> {
      public IMessage onMessage(MakeRichWishPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     World var2x = var2.getServerHandler().player.world;
                     EntityItem var3 = new EntityItem(
                        var2x,
                        var1.wishPos.x,
                        var1.wishPos.y,
                        var1.wishPos.z,
                        new ItemStack(Items.DIAMOND, Reference.RANDOM.nextInt(2) + 1)
                     );
                     EntityItem var4 = new EntityItem(
                        var2x,
                        var1.wishPos.x,
                        var1.wishPos.y,
                        var1.wishPos.z,
                        new ItemStack(Items.EMERALD, Reference.RANDOM.nextInt(2) + 1)
                     );
                     EntityItem var5 = new EntityItem(
                        var2x,
                        var1.wishPos.x,
                        var1.wishPos.y,
                        var1.wishPos.z,
                        new ItemStack(Items.GOLD_INGOT, Reference.RANDOM.nextInt(2) + 1)
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
