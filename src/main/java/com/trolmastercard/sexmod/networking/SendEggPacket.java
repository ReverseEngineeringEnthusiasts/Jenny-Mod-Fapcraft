package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SendEggPacket implements IMessage {
   boolean a;

   public void fromBytes(ByteBuf var1) {
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
   }

   public static class Handler implements IMessageHandler<SendEggPacket, IMessage> {
      public IMessage onMessage(SendEggPacket var1, MessageContext var2) {
         if (var1.a && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               EntityPlayerMP var1x = var2.getServerHandler().player;
               UUID var2x = KoboldManager.getTribeUUID(var1x.getPersistentID());
               if (var2x != null) {
                  EyeAndKoboldColor var3 = KoboldManager.l_clash75(var2x);
                  ItemStack var4 = new ItemStack(KoboldEggItem.a, 1, var3.getWoolMeta());
                  NBTTagCompound var5 = var4.getTagCompound();
                  if (var5 == null) {
                     var5 = new NBTTagCompound();
                  }

                  var5.setString("tribeID", var2x.toString());
                  var4.setTagCompound(var5);
                  var1x.inventory.addItemStackToInventory(var4);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid Message @SendEgg :(");
            return null;
         }
      }

   }
}
