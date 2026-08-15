package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER input for the Galath rape scene — the player pressed
 * the "back off" key during the pounce/dash phase.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Resolves the girl
 * currently in a scene with the sending player
 * ({@link BaseGirlEntity#getGirlByUUID(playerId, true)}) and, if she is a
 * {@link GalathEntity}, calls {@code handleRapeState()} to abort the pounce and
 * transition the scene state. No girl in scene -> no-op.
 */
public class GalathBackOffRapePacket implements IMessage {
   boolean isValid = false;

   public void fromBytes(ByteBuf buf) {
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
   }

   public static class Handler implements IMessageHandler<GalathBackOffRapePacket, IMessage> {
      public IMessage onMessage(GalathBackOffRapePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity girl = BaseGirlEntity.getGirlByUUID(ctx.getServerHandler().player.getPersistentID(), Boolean.valueOf(true));
               if (girl instanceof GalathEntity) {
                  ((GalathEntity)girl).handleRapeState();
               }
            });
            return null;
         } else {
            System.out.println("received an invalid Message @GalathBackOffRape :(");
            return null;
         }
      }

   }
}
