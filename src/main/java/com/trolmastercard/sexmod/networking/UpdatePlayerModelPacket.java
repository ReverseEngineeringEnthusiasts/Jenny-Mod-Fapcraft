package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.util.TrailSegment;







import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UpdatePlayerModelPacket implements IMessage {
   boolean isValid = false;
   NpcType npcType;

   public UpdatePlayerModelPacket() {
   }

   public UpdatePlayerModelPacket(NpcType var1) {
      this.npcType = var1;
   }

   public void fromBytes(ByteBuf var1) {
      String var2 = ByteBufUtils.readUTF8String(var1);
      if ("player".equals(var2)) {
         this.npcType = null;
      } else {
         this.npcType = NpcType.valueOf(var2);
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      if (this.npcType == null) {
         ByteBufUtils.writeUTF8String(var1, "player");
      } else {
         ByteBufUtils.writeUTF8String(var1, this.npcType.toString());
      }
   }


   public static class Handler implements IMessageHandler<UpdatePlayerModelPacket, IMessage> {
      public IMessage onMessage(UpdatePlayerModelPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               EntityPlayerMP var2x = var2.getServerHandler().player;
               World var3 = var2x.world;
               UUID var4 = var2.getServerHandler().player.getPersistentID();
               AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var4);
               if (var5 != null) {
                  try {
                     for (BaseGirlEntity var7 : BaseGirlEntity.getGirlEntityList()) {
                        if (!var7.world.isRemote && var7.getGirlId().equals(var5.getGirlId())) {
                           var3.removeEntity(var7);
                        }
                     }
                  } catch (ConcurrentModificationException var10) {
                  }

                  var5.y_clash234();
                  AbstractPlayerGirlEntity.al.remove(var4);
                  BaseGirlEntity.getGirlEntityList().remove(var5);
                  var5.a(Optional.absent());
               }

               NpcType var12 = var1.npcType;
               if (var12 != null) {
                  AbstractPlayerGirlEntity var11;
                  try {
                     Constructor var8 = var12.playerClass.getConstructor(World.class, UUID.class);
                     var11 = (AbstractPlayerGirlEntity)var8.newInstance(var3, var2.getServerHandler().player.getPersistentID());
                  } catch (Exception var9) {
                     var9.printStackTrace();
                     return;
                  }

                  var11.setNoGravity(true);
                  var11.noClip = true;
                  var11.motionX = 0.0;
                  var11.motionY = 0.0;
                  var11.motionZ = 0.0;
                  var11.setPosition(var2x.posX, var2x.posY + 69.0, var2x.posZ);
                  var3.spawnEntity(var11);
                  var11.B_clash233();
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UpdatePlayerModel :(");
            return null;
         }
      }

   }
}
