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
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               EntityPlayerMP var1x = var2.getServerHandler().field_147369_b;
               UUID var2x = KoboldManager.a_clash88(var1x.getPersistentID());
               if (var2x != null) {
                  EyeAndKoboldColor var3 = KoboldManager.l_clash75(var2x);
                  ItemStack var4 = new ItemStack(KoboldEggItem.a, 1, var3.getWoolMeta());
                  NBTTagCompound var5 = var4.func_77978_p();
                  if (var5 == null) {
                     var5 = new NBTTagCompound();
                  }

                  var5.func_74778_a("tribeID", var2x.toString());
                  var4.func_77982_d(var5);
                  var1x.field_71071_by.func_70441_a(var4);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid Message @SendEgg :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
