package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.item.LunaRodItem;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CatActivateFishingPacket implements IMessage {
   boolean b = false;
   UUID a;

   public CatActivateFishingPacket() {
   }

   public CatActivateFishingPacket(UUID var1) {
      this.a = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
   }

   public static class Handler implements IMessageHandler<CatActivateFishingPacket, IMessage> {
      public IMessage onMessage(CatActivateFishingPacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               for (BaseGirlEntity var4 : BaseGirlEntity.g_clash524(var1.a)) {
                  if (!var4.field_70170_p.field_72995_K && var4 instanceof LunaEntity) {
                     LunaEntity var5 = (LunaEntity)var4;
                     ItemStack var6 = var5.ao;
                     LunaRodItem var7 = (LunaRodItem)var6.func_77973_b();
                     var7.a(var2.getServerHandler().field_147369_b.field_70170_p, var5, EnumHand.MAIN_HAND);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @CatActivateFishing :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
