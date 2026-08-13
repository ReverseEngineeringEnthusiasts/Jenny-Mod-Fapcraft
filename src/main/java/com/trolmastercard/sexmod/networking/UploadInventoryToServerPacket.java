package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntityBase;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UploadInventoryToServerPacket implements IMessage {
   boolean b = false;
   ItemStack[] d;
   UUID a;
   UUID c;

   public UploadInventoryToServerPacket() {
   }

   public UploadInventoryToServerPacket(UUID var1, UUID var2, ItemStack[] var3) {
      this.a = var1;
      this.d = var3;
      this.c = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      int var2 = var1.readInt();
      this.d = new ItemStack[var2];

      for (int var3 = 0; var3 < var2; var3++) {
         this.d[var3] = ByteBufUtils.readItemStack(var1);
      }

      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      var1.writeInt(this.d.length);

      for (ItemStack var5 : this.d) {
         ByteBufUtils.writeItemStack(var1, var5);
      }
   }


   public static class Handler implements IMessageHandler<UploadInventoryToServerPacket, IMessage> {
      public IMessage onMessage(UploadInventoryToServerPacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.a)) {
                  if (!var3.world.isRemote) {
                     EntityPlayer var4 = var3.world.getPlayerEntityByUUID(var1.c);
                     if (var4 == null) {
                        return;
                     }

                     InventoryPlayer var5 = var4.inventory;

                     for (int var6 = 0; var6 < 36; var6++) {
                        var5.setInventorySlotContents(var6, var1.d[var6]);
                     }

                     if (var3 instanceof LunaEntity) {
                        AbstractGirlNpcEntity var8 = (AbstractGirlNpcEntity)var3;
                        var8.Q.setStackInSlot(0, var1.d[36]);
                        var8.Q.setStackInSlot(1, var1.d[37]);
                        var8.Q.setStackInSlot(2, var1.d[38]);
                        var8.Q.setStackInSlot(3, var1.d[39]);
                        var8.Q.setStackInSlot(4, var1.d[40]);
                        var8.Q.setStackInSlot(5, var1.d[41]);
                        var8.Q.setStackInSlot(6, var1.d[42]);
                     } else if (var3 instanceof AbstractGirlNpcEntity) {
                        AbstractGirlNpcEntity var9 = (AbstractGirlNpcEntity)var3;
                        var9.Q.setStackInSlot(0, var1.d[36]);
                        var9.Q.setStackInSlot(1, var1.d[37]);
                        var9.Q.setStackInSlot(2, var1.d[38]);
                        var9.Q.setStackInSlot(3, var1.d[39]);
                        var9.Q.setStackInSlot(4, var1.d[40]);
                        var9.Q.setStackInSlot(5, var1.d[41]);
                     }

                     if (var3 instanceof BeeEntityBase) {
                        BeeEntityBase var10 = (BeeEntityBase)var3;

                        for (int var7 = 0; var7 < 27; var7++) {
                           var10.L.setStackInSlot(var7, var1.d[var7 + 36]);
                        }
                     }
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadInventoryToServer :(");
            return null;
         }
      }

   }
}
