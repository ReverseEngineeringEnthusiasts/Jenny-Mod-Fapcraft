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

/**
 * <b>Role.</b> SERVER->CLIENT Galath summon trigger — makes the client render the
 * Galath's summon particle burst and switches the ownership debug flag.
 * <p>
 * <b>Handler.</b> CLIENT-side. Looks up the client-side {@link GalathEntity} and
 * calls {@link GalathCoinItem#summonGalathFor(UUID, GalathEntity)} which spawns
 * the energy-ball particles; if the UUID is the local player's, ownership debug
 * is turned off so the summoned girl behaves as owned.
 * <p>
 * Either UUID may be {@code null} (wire-sent as the sentinel string
 * {@code "trol was here"}); the handler tolerates that.
 */
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
