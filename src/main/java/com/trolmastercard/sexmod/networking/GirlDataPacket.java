package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.GirlScreenBase;
import com.trolmastercard.sexmod.entity.NpcType;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GirlDataPacket implements IMessage {
   boolean isValid = false;
   EntityPlayer player;
   HashMap<NpcType, String> a = new HashMap<>();

   public GirlDataPacket() {
   }

   public GirlDataPacket(EntityPlayer var1) {
      this.player = var1;
   }

   public void fromBytes(ByteBuf var1) {
      int var2 = var1.readInt();

      for (int var3 = 0; var3 < var2; var3++) {
         this.a.put(NpcType.valueOf(ByteBufUtils.readUTF8String(var1)), ByteBufUtils.readUTF8String(var1));
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      for (NpcType var5 : NpcType.values()) {
         if (var5.hasSpecifics) {
            String var6 = this.player.getEntityData().getString("sexmod:GirlSpecific" + var5);
            if (!"".equals(var6)) {
               this.a.put(var5, var6);
            }
         }
      }

      var1.writeInt(this.a.size());

      for (Entry var8 : this.a.entrySet()) {
         ByteBufUtils.writeUTF8String(var1, ((NpcType)var8.getKey()).toString());
         ByteBufUtils.writeUTF8String(var1, (String)var8.getValue());
      }
   }

   public static class Handler implements IMessageHandler<GirlDataPacket, IMessage> {
      public IMessage onMessage(GirlDataPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.CLIENT) {
            this.applyGirlData(var1.a);
            return null;
         } else {
            return null;
         }
      }

      @SideOnly(Side.CLIENT)
      public void applyGirlData(HashMap<NpcType, String> var1) {
         Minecraft var2 = Minecraft.getMinecraft();
         var2.addScheduledTask(() -> var2.displayGuiScreen(new GirlScreenBase(var1)));
      }

   }
}
