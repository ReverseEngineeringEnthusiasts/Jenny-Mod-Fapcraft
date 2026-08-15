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
   public void onPre(Pre event) {
      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (!girl.isDead && girl.getInteractionPlayerUUID() != null && girl.getCurrentAction() != Action.NULL) {
               EntityPlayer player = event.getEntityPlayer();
               if (girl.getCurrentAction().hasPlayer && (girl.getInteractionPlayerUUID().equals(player.getPersistentID()) || girl.getInteractionPlayerUUID().equals(player.getUniqueID()))) {
                  event.setCanceled(true);
                  return;
               }
            }
         }
      } catch (ConcurrentModificationException cme) {
      }
   }

   /**
    * Hides the first-person hand/held item while the local player is the
    * interaction partner of an active girl action, or while the local player
    * itself is an anchored {@link AbstractPlayerGirlEntity} (potion-transformed
    * girl). Without this the player's own arm would clip through the scene.
    */
   @SubscribeEvent
   public void onRenderHand(RenderHandEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      EntityPlayerSP player = mc.player;
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player);
      if (playerGirl != null && playerGirl.isAnchored()) {
         event.setCanceled(true);
      } else {
         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               UUID partnerUuid = girl.getInteractionPlayerUUID();
               Action action = girl.getCurrentAction();
               if (!girl.isDead
                  && partnerUuid != null
                  && action != null
                  && action.hasPlayer
                  && (partnerUuid.equals(player.getUniqueID()) || partnerUuid.equals(player.getPersistentID()))) {
                  event.setCanceled(true);
                  return;
               }
            }
         } catch (ConcurrentModificationException cme) {
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
   public void onRenderTick(RenderTickEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null) {
         if (event.phase == Phase.END) {
            if (this.position != null) {
               mc.player.setPosition(this.position.x, this.position.y, this.position.z);
               mc.player.lastTickPosX = this.rotation.x;
               mc.player.lastTickPosY = this.rotation.y;
               mc.player.lastTickPosZ = this.rotation.z;
               this.position = null;
               this.rotation = null;
            }
         } else if (mc.gameSettings.thirdPersonView == 0) {
            BaseGirlEntity girl = BaseGirlEntity.getGirlByUUID(mc.player.getPersistentID(), Boolean.valueOf(false));
            if (girl != null) {
               if (girl.getCurrentAction().useBoyCam) {
                  if (!girl.isCustomType()) {
                     this.position = mc.player.getPositionVector();
                     this.rotation = new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ);
                     Vec3d bonePos = girl.isAnchored()
                        ? girl.getCachedBoneOffset("boyCam").add(girl.getTargetPosition())
                        : girl.getCachedBoneOffset("boyCam")
                           .add(
                              RotationHelper.lerpVec3dDouble(new Vec3d(girl.lastTickPosX, girl.lastTickPosY, girl.lastTickPosZ), girl.getPositionVector(), event.renderTickTime)
                           );
                     mc.player.posX = bonePos.x;
                     mc.player.posY = bonePos.y - mc.player.getEyeHeight();
                     mc.player.posZ = bonePos.z;
                     mc.player.lastTickPosX = bonePos.x;
                     mc.player.lastTickPosY = bonePos.y - mc.player.getEyeHeight();
                     mc.player.lastTickPosZ = bonePos.z;
                  }
               }
            }
         }
      }
   }

}
