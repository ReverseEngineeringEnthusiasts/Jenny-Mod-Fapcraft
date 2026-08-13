package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateEquipmentPacket implements IMessage {
   boolean a;
   UUID c;
   NBTTagCompound b;

   public UpdateEquipmentPacket() {
   }

   public UpdateEquipmentPacket(UUID var1, NBTTagCompound var2) {
      this.c = var1;
      this.b = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = ByteBufUtils.readTag(var1);
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      ByteBufUtils.writeTag(var1, this.b);
   }

   public static class Handler implements IMessageHandler<UpdateEquipmentPacket, IMessage> {
      public IMessage onMessage(UpdateEquipmentPacket var1, MessageContext var2) {
         if (!var1.a) {
            System.out.println("received an invalid message @UpdateEquipment :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.c)) {
                  if (var3 instanceof AbstractGirlNpcEntity) {
                     ((AbstractGirlNpcEntity)var3).Q.deserializeNBT(var1.b);
                  }
               }
            });
            return null;
         }
      }

   }
}
