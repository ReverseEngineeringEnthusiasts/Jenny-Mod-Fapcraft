package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.TrailSegment;
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
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Finds the player by
 * the "girl UUID" (here used as the player UUID of the transformed girl) and
 * shrinks the first matching stack by the sent count.
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
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               InventoryPlayer inventory = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(packet.girlUUID).inventory;

               for (int i = 0; i < inventory.getSizeInventory(); i++) {
                  ItemStack stack = inventory.getStackInSlot(i);
                  if (stack.getItem().equals(packet.itemStack.getItem())) {
                     stack.shrink(packet.itemStack.getCount());
                     break;
                  }
               }
            });
            return null;
         } else {
            System.out.println("recieved an unvalid message @RemoveItems :(");
            return null;
         }
      }

   }
}
