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

   public KoboldStatePacket(UUID var1, UUID var2, boolean var3, boolean var4) {
      this.tribeId = var1;
      this.isSneaking = var3;
      this.girlId = var2;
      this.followMode = var4;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.tribeId = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isSneaking = var1.readBoolean();
      this.followMode = var1.readBoolean();
      String var2 = ByteBufUtils.readUTF8String(var1);
      this.girlId = var2.equals("null") ? null : UUID.fromString(var2);
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.tribeId.toString());
      var1.writeBoolean(this.isSneaking);
      var1.writeBoolean(this.followMode);
      ByteBufUtils.writeUTF8String(var1, this.girlId == null ? "null" : this.girlId.toString());
   }

   public static class Handler implements IMessageHandler<KoboldStatePacket, IMessage> {
      public static void sendState(UUID var0, UUID var1, boolean var2, boolean var3) {
         SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket.sendState girl=%s player=%s isSneaking=%s followMode=%s", var0, var1, var2, var3);
         try {
            for (BaseGirlEntity var5 : BaseGirlEntity.girlList(var0)) {
               if (!var5.world.isRemote) {
                  SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: girl %s found (%s), wander=%s watch=%s", var5.getDisplayNameText(), var5.getClass().getSimpleName(), var5.wanderGoal != null, var5.watchClosestGirlGoal != null);
                  if (var5 instanceof JennyEntity || var5 instanceof EllieEntity || var5 instanceof LunaEntity) {
                     var5.tasks.removeTask(var5.watchClosestGirlGoal);
                     var5.tasks.removeTask(var5.wanderGoal);
                  }

                  var5.getNavigator().clearPath();
                  var5.motionX = 0.0;
                  var5.motionY = 0.0;
                  var5.motionZ = 0.0;
                  if (var5.getInteractionPlayerUUID() == null) {
                     var5.setInteractionPlayerUUID(var1);
                  }

                  if (var3) {
                     var5.setTargetPosition(var5.getFrontOffsetVector());
                     SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: targetPos=%s", var5.getTargetPosition());
                  }

                  var5.snapPlayerToPosition(var5.getInteractionPlayerUUID());
                  SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: after snap, girl pos=%s", var5.getPositionVector());
                  if (!var2) {
                     return;
                  }

                  if (!(var5 instanceof IEllie)) {
                     SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: %s is not IEllie, no setDismounted", var5.getClass().getSimpleName());
                     return;
                  }

                  IEllie var6 = (IEllie)var5;
                  var6.setDismounted();
                  SceneDebug.log(SceneDebug.PACKETS, "KoboldStatePacket: setDismounted() called on %s", var5.getDisplayNameText());
               }
            }
         } catch (ConcurrentModificationException var7) {
         }
      }

      public IMessage onMessage(KoboldStatePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> sendState(var1.tribeId, var1.girlId, var1.isSneaking, var1.followMode));
         }

         return null;
      }
   }
}
