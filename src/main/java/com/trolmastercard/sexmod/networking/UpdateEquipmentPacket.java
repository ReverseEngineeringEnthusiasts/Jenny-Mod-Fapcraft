package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateEquipmentPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   NBTTagCompound equipmentNbt;

   public UpdateEquipmentPacket() {
   }

   public UpdateEquipmentPacket(UUID var1, NBTTagCompound var2) {
      this.girlUUID = var1;
      this.equipmentNbt = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.equipmentNbt = ByteBufUtils.readTag(var1);
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      ByteBufUtils.writeTag(var1, this.equipmentNbt);
   }

   public static class Handler implements IMessageHandler<UpdateEquipmentPacket, IMessage> {
      public IMessage onMessage(UpdateEquipmentPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @UpdateEquipment :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.girlUUID)) {
                  if (var3 instanceof AbstractGirlNpcEntity) {
                     ((AbstractGirlNpcEntity)var3).inventory.deserializeNBT(var1.equipmentNbt);
                  }
               }
            });
            return null;
         }
      }

   }
}
