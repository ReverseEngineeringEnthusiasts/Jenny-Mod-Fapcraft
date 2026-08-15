package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER owner command for a transformed player-girl —
 * starts a standing sex animation between the girl's owner and the given player.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Resolves the
 * {@link AbstractPlayerGirlEntity} by the owner's UUID (on an integrated server
 * it re-scans the girl list because the owner girl may not be the first match),
 * then calls {@code handleOwnerCommand(animation, playerUUID)} which runs the
 * scene server-side. Ordering: the player must be registered as interaction
 * partner first (see {@link SetPlayerForGirlPacket}).
 */
public class StartStandingSexAnimationPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   UUID playerUUID;
   String animation;

   public StartStandingSexAnimationPacket() {
   }

   public StartStandingSexAnimationPacket(UUID girlUUID, UUID playerUUID, String animation) {
      this.girlUUID = girlUUID;
      this.playerUUID = playerUUID;
      this.animation = animation;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.animation = ByteBufUtils.readUTF8String(buf);
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.animation);
   }

   public static class Handler implements IMessageHandler<StartStandingSexAnimationPacket, IMessage> {
      public IMessage onMessage(StartStandingSexAnimationPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(packet.girlUUID);
               if (playerGirl != null) {
                  if (!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
                     try {
                        for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                           if (girl instanceof AbstractPlayerGirlEntity) {
                              playerGirl = (AbstractPlayerGirlEntity)girl;
                              if (!playerGirl.world.isRemote && playerGirl.getOwnerUserUUID().equals(packet.girlUUID)) {
                                 break;
                              }
                           }
                        }
                     } catch (Exception exception) {
                     }
                  }

                  playerGirl.handleOwnerCommand(packet.animation, packet.playerUUID);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @StartStandingSexAnimation :(");
            return null;
         }
      }

   }
}
