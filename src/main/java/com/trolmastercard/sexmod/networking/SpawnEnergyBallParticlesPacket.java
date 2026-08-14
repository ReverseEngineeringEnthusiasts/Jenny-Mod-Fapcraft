package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SpawnEnergyBallParticlesPacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   UUID playerUUID;

   public SpawnEnergyBallParticlesPacket() {
   }

   public SpawnEnergyBallParticlesPacket(UUID var1, UUID var2) {
      this.girlUUID = var1;
      this.playerUUID = var2;
   }

   public void fromBytes(ByteBuf var1) {
      try {
         this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      } catch (Exception var3) {
         this.girlUUID = null;
      }

      try {
         this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      } catch (Exception var2) {
         this.playerUUID = null;
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID == null ? "trol was here" : this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.playerUUID == null ? "trol was here" : this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<SpawnEnergyBallParticlesPacket, IMessage> {
      public IMessage onMessage(SpawnEnergyBallParticlesPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.CLIENT)) {
            BaseGirlEntity var3 = BaseGirlEntity.getClientGirlEntity(var1.girlUUID);
            if (!(var3 instanceof GalathEntity)) {
               System.out.println("doesnt exit");
               return null;
            } else {
               GalathCoinItem.summonGalathFor(var1.playerUUID, (GalathEntity)var3);
               return null;
            }
         } else {
            System.out.println("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

   }
}
