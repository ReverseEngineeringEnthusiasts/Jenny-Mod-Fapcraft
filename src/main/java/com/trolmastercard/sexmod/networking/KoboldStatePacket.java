package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EllieEntity;
import com.trolmastercard.sexmod.entity.JennyEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.util.SceneDebug;
import io.netty.buffer.ByteBuf;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Scene-entry trigger packet, CLIENT -&gt; SERVER. Despite the name it is the
 * shared "start a scene with a girl" packet used by every NPC girl.
 * <p>
 * Constructor arg order is confusing: {@code tribeId} actually carries the
 * <b>girl's</b> UUID, {@code girlId} carries the <b>player's</b> UUID.
 * <p>
 * SERVER side, {@link Handler#sendState} does:
 * <ol>
 *   <li>finds the server girl by her UUID ({@link BaseGirlEntity#girlList});</li>
 *   <li>for Jenny/Ellie/Luna removes the wander + watch tasks;</li>
 *   <li>clears her path/motion, binds the interaction player if unset, sets
 *       {@code TARGET_POS} to the front of the player, snaps the player next
 *       to her ({@code snapPlayerToPosition});</li>
 *   <li>if {@code isSneaking} is true (always true from scene entry) and the
 *       girl implements {@link IEllie}, calls {@code setDismounted()} — which
 *       starts the girl's 40-tick lerp to her target position in
 *       {@code updateAITasks}, then {@code U()} starts the scene.</li>
 * </ol>
 * <p>
 * <b>Pitfall:</b> {@code followMode}/{@code isSneaking} are effectively always
 * {@code true} from scene entry ({@code triggerActionSync(true, true, uuid)}).
 * Do not reorder the byte-buffer fields — packet layout is shared with the
 * built (SRG-reobfuscated) jar.
 */
public class KoboldStatePacket implements IMessage {
   boolean isValid;
   UUID tribeId;
   boolean isSneaking;
   boolean followMode;
   UUID girlId = null;

   public KoboldStatePacket() {
      this.isValid = false;
   }

   public KoboldStatePacket(UUID tribeId, UUID girlId, boolean isSneaking, boolean followMode) {
      this.tribeId = tribeId;
      this.isSneaking = isSneaking;
      this.girlId = girlId;
      this.followMode = followMode;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.tribeId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isSneaking = buf.readBoolean();
      this.followMode = buf.readBoolean();
      String girlIdString = ByteBufUtils.readUTF8String(buf);
      this.girlId = girlIdString.equals("null") ? null : UUID.fromString(girlIdString);
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.tribeId.toString());
      buf.writeBoolean(this.isSneaking);
      buf.writeBoolean(this.followMode);
      ByteBufUtils.writeUTF8String(buf, this.girlId == null ? "null" : this.girlId.toString());
   }

   public static class Handler implements IMessageHandler<KoboldStatePacket, IMessage> {
      public static void sendState(UUID tribeId, UUID girlId, boolean isSneaking, boolean followMode) {
         SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket.sendState girl=%s player=%s isSneaking=%s followMode=%s", tribeId, girlId, isSneaking, followMode);
         try {
            for (BaseGirlEntity girl : BaseGirlEntity.girlList(tribeId)) {
               if (!girl.world.isRemote) {
                  SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: girl %s found (%s), wander=%s watch=%s", girl.getDisplayNameText(), girl.getClass().getSimpleName(), girl.wanderGoal != null, girl.watchClosestGirlGoal != null);
                  if (girl instanceof JennyEntity || girl instanceof EllieEntity || girl instanceof LunaEntity) {
                     girl.tasks.removeTask(girl.watchClosestGirlGoal);
                     girl.tasks.removeTask(girl.wanderGoal);
                  }

                  girl.getNavigator().clearPath();
                  girl.motionX = 0.0;
                  girl.motionY = 0.0;
                  girl.motionZ = 0.0;
                  if (girl.getInteractionPlayerUUID() == null) {
                     girl.setInteractionPlayerUUID(girlId);
                  }

                  if (followMode) {
                     girl.setTargetPosition(girl.getFrontOffsetVector());
                     SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: targetPos=%s", girl.getTargetPosition());
                  }

                  girl.snapPlayerToPosition(girl.getInteractionPlayerUUID());
                  SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: after snap, girl pos=%s", girl.getPositionVector());
                  if (!isSneaking) {
                     return;
                  }

                  if (!(girl instanceof IEllie)) {
                     SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: %s is not IEllie, no setDismounted", girl.getClass().getSimpleName());
                     return;
                  }

                  IEllie ellie = (IEllie)girl;
                  ellie.setDismounted();
                  SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: setDismounted() called on %s", girl.getDisplayNameText());
               }
            }
         } catch (ConcurrentModificationException exception) {
         }
      }

      public IMessage onMessage(KoboldStatePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> sendState(packet.tribeId, packet.girlId, packet.isSneaking, packet.followMode));
         }

         return null;
      }
   }
}
