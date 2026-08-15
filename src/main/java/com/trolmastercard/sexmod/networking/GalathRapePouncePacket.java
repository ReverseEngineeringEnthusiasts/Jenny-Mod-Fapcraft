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

   public GalathRapePouncePacket(boolean var1) {
      this.isPounce = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.isPounce = var1.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.isPounce);
   }

   public static class Handler implements IMessageHandler<GalathRapePouncePacket, IMessage> {
      public IMessage onMessage(GalathRapePouncePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity var2x = BaseGirlEntity.getActiveSceneInfo(var2.getServerHandler().player.getPersistentID());
               if (var2x instanceof GalathEntity) {
                  ((GalathEntity)var2x).handleRapeAction(var1.isPounce);
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
