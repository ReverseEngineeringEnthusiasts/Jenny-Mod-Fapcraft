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

   public MakeRichWishPacket(Vec3d wishPos) {
      this.wishPos = wishPos;
   }

   public void fromBytes(ByteBuf buf) {
      this.wishPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeDouble(this.wishPos.x);
      buf.writeDouble(this.wishPos.y);
      buf.writeDouble(this.wishPos.z);
   }

   public static class Handler implements IMessageHandler<MakeRichWishPacket, IMessage> {
      public IMessage onMessage(MakeRichWishPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     World world = ctx.getServerHandler().player.world;
                     EntityItem diamondItem = new EntityItem(
                        world,
                        packet.wishPos.x,
                        packet.wishPos.y,
                        packet.wishPos.z,
                        new ItemStack(Items.DIAMOND, Reference.RANDOM.nextInt(2) + 1)
                     );
                     EntityItem goldItem = new EntityItem(
                        world,
                        packet.wishPos.x,
                        packet.wishPos.y,
                        packet.wishPos.z,
                        new ItemStack(Items.EMERALD, Reference.RANDOM.nextInt(2) + 1)
                     );
                     EntityItem emeraldItem = new EntityItem(
                        world,
                        packet.wishPos.x,
                        packet.wishPos.y,
                        packet.wishPos.z,
                        new ItemStack(Items.GOLD_INGOT, Reference.RANDOM.nextInt(2) + 1)
                     );
                     world.spawnEntity(diamondItem);
                     world.spawnEntity(goldItem);
                     world.spawnEntity(emeraldItem);
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
