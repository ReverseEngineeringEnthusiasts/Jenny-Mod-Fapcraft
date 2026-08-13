package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClaimTribePacket implements IMessage {
   boolean c = false;
   UUID d;
   UUID a;
   String b;

   public ClaimTribePacket() {
   }

   public ClaimTribePacket(UUID var1, UUID var2, String var3) {
      this.d = var1;
      this.a = var2;
      this.b = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.d = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = ByteBufUtils.readUTF8String(var1);
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.d.toString());
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      ByteBufUtils.writeUTF8String(var1, this.b);
   }

   public static class Handler implements IMessageHandler<ClaimTribePacket, IMessage> {
      public IMessage onMessage(ClaimTribePacket var1, MessageContext var2) {
         if (var1.c && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     List var2x = KoboldManager.n_clash82(var1.d);
                     EyeAndKoboldColor var3 = null;

                     for (KoboldEntity var5 : (java.util.Collection<KoboldEntity>) (var2x) ) {
                        if (!var5.J_clash526()) {
                           EntityDataManager var6 = var5.getDataManager();
                           var6.set(BaseGirlEntity.v, var1.a.toString());
                           var6.set(KoboldEntity.aU, var1.b);
                           var3 = EyeAndKoboldColor.valueOf((String)var6.get(KoboldEntity.N));
                        }
                     }

                     if (var3 != null) {
                        PlayerList var8 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
                        String var9 = var2.getServerHandler().player.getName();

                        for (EntityPlayer var7 : var8.getPlayers()) {
                           var7.sendMessage(
                              new TextComponentString(
                                 String.format("%s formed the " + var3.getTextColor() + "%s " + TextFormatting.WHITE + "Tribe", var9, var1.b)
                              )
                           );
                        }

                        KoboldManager.a_clash87(var1.d, true);
                        KoboldManager.a(var1.d, var2.getServerHandler().player.getPersistentID());
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @ClaimTribe :(");
            return null;
         }
      }

   }
}
