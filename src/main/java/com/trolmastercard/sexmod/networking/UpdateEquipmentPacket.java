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

/**
 * SERVER-bound packet: updates a girl's equipment/held items (used by the
 * girl inventory screens). See GirlInventoryContainer* for the UI side.
 */
public class UpdateEquipmentPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   NBTTagCompound equipmentNbt;

   public UpdateEquipmentPacket() {
   }

   public UpdateEquipmentPacket(UUID girlUUID, NBTTagCompound equipmentNbt) {
      this.girlUUID = girlUUID;
      this.equipmentNbt = equipmentNbt;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.equipmentNbt = ByteBufUtils.readTag(buf);
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeTag(buf, this.equipmentNbt);
   }

   public static class Handler implements IMessageHandler<UpdateEquipmentPacket, IMessage> {
      public IMessage onMessage(UpdateEquipmentPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid message @UpdateEquipment :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                  if (girl instanceof AbstractGirlNpcEntity) {
                     ((AbstractGirlNpcEntity)girl).inventory.deserializeNBT(packet.equipmentNbt);
                  }
               }
            });
            return null;
         }
      }

   }
}
