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

   public SpawnEnergyBallParticlesPacket(UUID girlUUID, UUID playerUUID) {
      this.girlUUID = girlUUID;
      this.playerUUID = playerUUID;
   }

   public void fromBytes(ByteBuf buf) {
      try {
         this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      } catch (Exception exception) {
         this.girlUUID = null;
      }

      try {
         this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      } catch (Exception exception2) {
         this.playerUUID = null;
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID == null ? "trol was here" : this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.playerUUID == null ? "trol was here" : this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<SpawnEnergyBallParticlesPacket, IMessage> {
      public IMessage onMessage(SpawnEnergyBallParticlesPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.CLIENT)) {
            BaseGirlEntity girl = BaseGirlEntity.getClientGirlEntity(packet.girlUUID);
            if (!(girl instanceof GalathEntity)) {
               System.out.println("doesnt exit");
               return null;
            } else {
               GalathCoinItem.summonGalathFor(packet.playerUUID, (GalathEntity)girl);
               return null;
            }
         } else {
            System.out.println("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

   }
}
