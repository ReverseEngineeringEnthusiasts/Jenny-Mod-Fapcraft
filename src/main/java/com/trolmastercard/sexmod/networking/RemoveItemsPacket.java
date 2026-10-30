package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.Main;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER inventory cleanup — removes one stack of the given
 * item type from the target player's inventory. Used when a girl gives/takes
 * items and the client-side inventory preview must stay in sync with the server.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. The target is the
 * player who sent the packet ({@code ctx.getServerHandler().player}); the UUID
 * carried on the wire is kept only for compatibility with older clients and is
 * not trusted. Shrinks the first stack in the player's main inventory whose
 * item and metadata match, removing at most what the stack actually holds.
 */
public class RemoveItemsPacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   ItemStack itemStack;

   public RemoveItemsPacket() {
   }

   public RemoveItemsPacket(UUID girlUUID, ItemStack itemStack) {
      this.girlUUID = girlUUID;
      this.itemStack = itemStack;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.itemStack = ByteBufUtils.readItemStack(buf);
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeItemStack(buf, this.itemStack);
   }

   public static class Handler implements IMessageHandler<RemoveItemsPacket, IMessage> {
      public IMessage onMessage(RemoveItemsPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            net.minecraft.entity.player.EntityPlayerMP sender = ctx.getServerHandler().player;
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               ItemStack requested = packet.itemStack;
               if (requested == null || requested.isEmpty()) {
                  return;
               }

               InventoryPlayer inventory = sender.inventory;

               for (ItemStack stack : inventory.mainInventory) {
                  if (stack.getItem().equals(requested.getItem()) && stack.getMetadata() == requested.getMetadata()) {
                     stack.shrink(Math.min(requested.getCount(), stack.getCount()));
                     break;
                  }
               }
            });
            return null;
         } else {
            Main.LOGGER.info("recieved an unvalid message @RemoveItems :(");
            return null;
         }
      }

   }
}
