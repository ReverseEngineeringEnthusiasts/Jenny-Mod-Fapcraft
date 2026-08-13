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

public class RemoveItemsPacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   ItemStack itemStack;

   public RemoveItemsPacket() {
   }

   public RemoveItemsPacket(UUID var1, ItemStack var2) {
      this.girlUUID = var1;
      this.itemStack = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.itemStack = ByteBufUtils.readItemStack(var1);
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      ByteBufUtils.writeItemStack(var1, this.itemStack);
   }

   public static class Handler implements IMessageHandler<RemoveItemsPacket, IMessage> {
      public IMessage onMessage(RemoveItemsPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               InventoryPlayer var1x = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(var1.girlUUID).inventory;

               for (int var2x = 0; var2x < var1x.getSizeInventory(); var2x++) {
                  ItemStack var3 = var1x.getStackInSlot(var2x);
                  if (var3.getItem().equals(var1.itemStack.getItem())) {
                     var3.shrink(var1.itemStack.getCount());
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
