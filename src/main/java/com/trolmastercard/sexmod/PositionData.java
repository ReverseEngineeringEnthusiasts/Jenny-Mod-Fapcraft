package com.trolmastercard.sexmod;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * CLIENT-side scene camera manager. Drives the first-person "boyCam" camera
 * while the local player is involved in a girl's action: it cancels the vanilla
 * player render (the girl scene is drawn instead), cancels the first-person hand
 * model, and on each render tick repositions the client player at the girl's
 * {@code boyCam} bone so the scene is viewed from the girl's perspective.
 * <p>
 * <b>State flow.</b> On a render tick with {@code thirdPersonView == 0} and an
 * active action with {@code useBoyCam}, the player's position and last-tick
 * position are captured (fields {@code position}/{@code rotation}) and the
 * player is teleported to the bone offset. On the matching {@code Phase.END}
 * tick the captured position is written back, restoring the player to where he
 * was before the camera snap — so world physics/packets keep seeing a stable
 * position.
 * <p>
 * <b>Pitfalls.</b> When the girl is anchored, the bone offset is taken from
 * {@code getCachedBoneOffset("boyCam")} relative to her target position;
 * otherwise it is interpolated from last-tick to current position with
 * {@link RotationHelper#lerpVec3dDouble} (PROGRESS lerp — correct here, render
 * interpolation must NOT use the INT step variant). Both {@code onPre} and
 * {@code onRenderHand} iterate {@link BaseGirlEntity#getGirlEntityList()} and
 * swallow {@link ConcurrentModificationException} — do not remove that guard.
 */
public class PositionData {
   Vec3d position = null;
   Vec3d rotation = null;

   /**
    * Cancels the vanilla player-body render when the rendered player is the
    * interaction partner of an active girl action that has a player, so the
    * girl scene renderer is the only thing drawn for that player.
    * CLIENT-side render event; iterates the shared girl list, so concurrent
    * modification is tolerated (see class javadoc).
    */
   @SubscribeEvent
   public void onPre(Pre var1) {
      try {
         for (BaseGirlEntity var3 : BaseGirlEntity.getGirlEntityList()) {
            if (!var3.isDead && var3.getInteractionPlayerUUID() != null && var3.getCurrentAction() != Action.NULL) {
               EntityPlayer var4 = var1.getEntityPlayer();
               if (var3.getCurrentAction().hasPlayer && (var3.getInteractionPlayerUUID().equals(var4.getPersistentID()) || var3.getInteractionPlayerUUID().equals(var4.getUniqueID()))) {
                  var1.setCanceled(true);
                  return;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   /**
    * Hides the first-person hand/held item while the local player is the
    * interaction partner of an active girl action, or while the local player
    * itself is an anchored {@link AbstractPlayerGirlEntity} (potion-transformed
    * girl). Without this the player's own arm would clip through the scene.
    */
   @SubscribeEvent
   public void onRenderHand(RenderHandEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      EntityPlayerSP var3 = var2.player;
      AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var3);
      if (var4 != null && var4.isAnchored()) {
         var1.setCanceled(true);
      } else {
         try {
            for (BaseGirlEntity var6 : BaseGirlEntity.getGirlEntityList()) {
               UUID var7 = var6.getInteractionPlayerUUID();
               Action var8 = var6.getCurrentAction();
               if (!var6.isDead
                  && var7 != null
                  && var8 != null
                  && var8.hasPlayer
                  && (var7.equals(var3.getUniqueID()) || var7.equals(var3.getPersistentID()))) {
                  var1.setCanceled(true);
                  return;
               }
            }
         } catch (ConcurrentModificationException var9) {
         }
      }
   }

   /**
    * Render-tick camera hook (CLIENT-side).
    * <p>
    * <b>Phase.BEGIN:</b> if the local player is the boy of an active
    * {@code useBoyCam} action with a non-custom girl, capture the player's
    * position + last-tick position, then snap the player to the girl's
    * {@code boyCam} bone (anchored: relative to {@code getTargetPosition()};
    * otherwise lerped with partial ticks via {@code lerpVec3dDouble}).
    * <p>
    * <b>Phase.END:</b> write the captured position back (position and
    * lastTickPos) and clear the capture. Ordering matters — END restores what
    * BEGIN saved, and the two fields must be nulled exactly once per pair or
    * the player gets stuck at the girl's position.
    */
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderTick(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player != null) {
         if (var1.phase == Phase.END) {
            if (this.position != null) {
               var2.player.setPosition(this.position.x, this.position.y, this.position.z);
               var2.player.lastTickPosX = this.rotation.x;
               var2.player.lastTickPosY = this.rotation.y;
               var2.player.lastTickPosZ = this.rotation.z;
               this.position = null;
               this.rotation = null;
            }
         } else if (var2.gameSettings.thirdPersonView == 0) {
            BaseGirlEntity var3 = BaseGirlEntity.getGirlByUUID(var2.player.getPersistentID(), Boolean.valueOf(false));
            if (var3 != null) {
               if (var3.getCurrentAction().useBoyCam) {
                  if (!var3.isCustomType()) {
                     this.position = var2.player.getPositionVector();
                     this.rotation = new Vec3d(var2.player.lastTickPosX, var2.player.lastTickPosY, var2.player.lastTickPosZ);
                     Vec3d var4 = var3.isAnchored()
                        ? var3.getCachedBoneOffset("boyCam").add(var3.getTargetPosition())
                        : var3.getCachedBoneOffset("boyCam")
                           .add(
                              RotationHelper.lerpVec3dDouble(new Vec3d(var3.lastTickPosX, var3.lastTickPosY, var3.lastTickPosZ), var3.getPositionVector(), var1.renderTickTime)
                           );
                     var2.player.posX = var4.x;
                     var2.player.posY = var4.y - var2.player.getEyeHeight();
                     var2.player.posZ = var4.z;
                     var2.player.lastTickPosX = var4.x;
                     var2.player.lastTickPosY = var4.y - var2.player.getEyeHeight();
                     var2.player.lastTickPosZ = var4.z;
                  }
               }
            }
         }
      }
   }

}
