package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
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

   public StartStandingSexAnimationPacket(UUID var1, UUID var2, String var3) {
      this.girlUUID = var1;
      this.playerUUID = var2;
      this.animation = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.animation = ByteBufUtils.readUTF8String(var1);
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.playerUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.animation);
   }

   public static class Handler implements IMessageHandler<StartStandingSexAnimationPacket, IMessage> {
      public IMessage onMessage(StartStandingSexAnimationPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               AbstractPlayerGirlEntity var1x = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.girlUUID);
               if (var1x != null) {
                  if (!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
                     try {
                        for (BaseGirlEntity var3 : BaseGirlEntity.getGirlEntityList()) {
                           if (var3 instanceof AbstractPlayerGirlEntity) {
                              var1x = (AbstractPlayerGirlEntity)var3;
                              if (!var1x.world.isRemote && var1x.getOwnerUserUUID().equals(var1.girlUUID)) {
                                 break;
                              }
                           }
                        }
                     } catch (Exception var4) {
                     }
                  }

                  var1x.handleOwnerCommand(var1.animation, var1.playerUUID);
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
