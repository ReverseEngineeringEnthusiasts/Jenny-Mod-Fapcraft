package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER input for the Galath rape scene — the player chose
 * to pounce (or not) when prompted by {@code handleRapeState}.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Resolves the active
 * scene girl for the sender via
 * {@link BaseGirlEntity#getActiveSceneInfo(playerId)} and calls
 * {@link GalathEntity#handleRapeAction(boolean)} with the pounce decision; the
 * girl picks the corresponding scene branch server-side. Ordering constraint:
 * only valid while a Galath rape scene is active for that player.
 */
public class GalathRapePouncePacket implements IMessage {
   boolean isValid = false;
   boolean isPounce;

   public GalathRapePouncePacket() {
   }

   public GalathRapePouncePacket(boolean isPounce) {
      this.isPounce = isPounce;
   }

   public void fromBytes(ByteBuf buf) {
      this.isPounce = buf.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isPounce);
   }

   public static class Handler implements IMessageHandler<GalathRapePouncePacket, IMessage> {
      public IMessage onMessage(GalathRapePouncePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity girl = BaseGirlEntity.getActiveSceneInfo(ctx.getServerHandler().player.getPersistentID());
               if (girl instanceof GalathEntity) {
                  ((GalathEntity)girl).handleRapeAction(packet.isPounce);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @GalathRapePounce :(");
            return null;
         }
      }

   }
}
