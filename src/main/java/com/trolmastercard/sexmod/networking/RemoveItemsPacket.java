package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.an;







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
   boolean c = false;
   UUID a;
   ItemStack b;

   public RemoveItemsPacket() {
   }

   public RemoveItemsPacket(UUID var1, ItemStack var2) {
      this.a = var1;
      this.b = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = ByteBufUtils.readItemStack(var1);
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      ByteBufUtils.writeItemStack(var1, this.b);
   }

   public static class Handler implements IMessageHandler<RemoveItemsPacket, IMessage> {
      public IMessage onMessage(RemoveItemsPacket var1, MessageContext var2) {
         if (var1.c && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               InventoryPlayer var1x = FMLCommonHandler.instance().getMinecraftServerInstance().func_184103_al().func_177451_a(var1.a).field_71071_by;

               for (int var2x = 0; var2x < var1x.func_70302_i_(); var2x++) {
                  ItemStack var3 = var1x.func_70301_a(var2x);
                  if (var3.func_77973_b().equals(var1.b.func_77973_b())) {
                     var3.func_190918_g(var1.b.func_190916_E());
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
