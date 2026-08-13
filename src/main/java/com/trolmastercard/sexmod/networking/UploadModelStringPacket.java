package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UploadModelStringPacket implements IMessage {
   boolean isValid = false;
   String modelCode;
   List<Integer> partIds = new ArrayList<>();
   UUID girlUUID;

   public UploadModelStringPacket() {
   }

   public UploadModelStringPacket(String var1, UUID var2) {
      this.modelCode = var1;
      this.girlUUID = var2;
   }

   public UploadModelStringPacket(String var1, UUID var2, List<Integer> var3) {
      this.modelCode = var1;
      this.girlUUID = var2;
      this.partIds = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.modelCode = ByteBufUtils.readUTF8String(var1);
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      int var2 = var1.readInt();

      for (int var3 = 0; var3 < var2; var3++) {
         this.partIds.add(var1.readInt());
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.modelCode);
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      var1.writeInt(this.partIds.size());

      for (int var3 : this.partIds) {
         var1.writeInt(var3);
      }
   }

   public static class Handler implements IMessageHandler<UploadModelStringPacket, IMessage> {
      public IMessage onMessage(UploadModelStringPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity var3 = BaseGirlEntity.getServerGirlEntity(var1.girlUUID);
               if (var1.partIds.size() > 0) {
                  boolean var5 = this.a(var3, var1.partIds);
                  if (var5) {
                     var3.setCustomPartList(var1.partIds);
                  }

                  if (!(var3 instanceof AbstractPlayerGirlEntity)) {
                     var3.setCustomModelCode(var1.modelCode);
                  } else {
                     EntityPlayerMP var10 = var2.getServerHandler().player;
                     NBTTagCompound var11 = var10.getEntityData();
                     AbstractPlayerGirlEntity var12 = AbstractPlayerGirlEntity.g(var10);
                     if (var12 != null) {
                        NpcType var13 = NpcType.getNpcType(var12);
                        var11.setString("sexmod:CustomModel" + var13.toString(), var1.modelCode);
                        if (var5) {
                           var11.setString("sexmod:GirlSpecific" + var13.toString(), BaseGirlEntity.encodePartIdList(var1.partIds));
                        }
                     }
                  }
               } else if (!(var3 instanceof AbstractPlayerGirlEntity)) {
                  var3.setCustomModelCode(var1.modelCode);
               } else {
                  EntityPlayerMP var6 = var2.getServerHandler().player;
                  NBTTagCompound var7 = var6.getEntityData();
                  AbstractPlayerGirlEntity var8 = AbstractPlayerGirlEntity.g(var6);
                  if (var8 != null) {
                     NpcType var9 = NpcType.getNpcType(var8);
                     var7.setString("sexmod:CustomModel" + var9.toString(), var1.modelCode);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadModelString :(");
            return null;
         }
      }

      boolean a(BaseGirlEntity var1, List<Integer> var2) {
         ArrayList var3 = var1.getCustomPartIdList();

         try {
            for (int var4 = 0; var4 < var3.size(); var4++) {
               if ((Integer)var3.get(var4) <= (Integer)var2.get(var4)) {
                  return false;
               }
            }

            return true;
         } catch (IndexOutOfBoundsException var5) {
            return false;
         }
      }

   }
}
