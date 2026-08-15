package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.entity.Action;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> CLIENT-side first-person rendering for the goblin transformation:
 * while the local player is the owner of a goblin girl, the girl is drawn in
 * first person (RenderWorldLast), her pick-up/throw animations replace the
 * vanilla hand and player-body render (RenderHandEvent/Pre cancellation).
 * <p>
 * <b>State.</b> {@code GoblinRenderer.strafeRotation/forwardRotation} are
 * derived from the player's movement input and smoothed with
 * {@link RotationHelper#lerp} — the smoothing must keep using prev/cur fields
 * or the goblin's arms jitter.
 * <p>
 * <b>Pitfall.</b> All list scans tolerate {@link ConcurrentModificationException}
 * and the "yaw -420.69" sentinel forces a render pass for the owner's goblin —
 * do not "clean up" either.
 */
public class GoblinFirstPersonRenderer {
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderWorldLastFirstPerson(RenderWorldLastEvent event) {
      Minecraft minecraft = Minecraft.getMinecraft();
      if (minecraft.gameSettings.thirdPersonView == 0) {
         UUID playerUuid = minecraft.player.getPersistentID();
         BaseGirlEntity goblinGirl = null;

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl != null && !girl.isDead && girl.world.isRemote && girl instanceof IGoblin) {
                  IGoblin iGoblin = (IGoblin)girl;
                  if (playerUuid.equals(iGoblin.getOwnerUUID())) {
                     goblinGirl = girl;
                     break;
                  }
               }
            }
         } catch (ConcurrentModificationException exception) {
         }

         if (goblinGirl != null) {
            Render renderer = minecraft.getRenderManager().getEntityRenderObject(goblinGirl);
            if (renderer != null) {
               float yaw = minecraft.player.rotationYaw;
               GoblinRenderer.strafeRotation = (float)(minecraft.player.movementInput.moveStrafe * GoblinRenderer.MOVEMENT_DIR_VECTOR.x);
               GoblinRenderer.strafeRotation = GoblinRenderer.strafeRotation + -(yaw - GoblinRenderer.lastPlayerYaw) * 3.0F;
               GoblinRenderer.strafeRotation = RotationHelper.lerp(GoblinRenderer.prevStrafeRotation, GoblinRenderer.strafeRotation, 0.1F);
               float pitch = -minecraft.player.rotationPitch;
               GoblinRenderer.forwardRotation = (float)(
                  minecraft.player.movementInput.moveForward * GoblinRenderer.MOVEMENT_DIR_VECTOR.z
                     + (float)minecraft.player.motionY * GoblinRenderer.MOVEMENT_DIR_VECTOR.y
               );
               GoblinRenderer.forwardRotation = GoblinRenderer.forwardRotation + -(pitch - GoblinRenderer.lastPlayerPitch) * 3.0F;
               GoblinRenderer.forwardRotation = RotationHelper.lerp(GoblinRenderer.prevForwardRotation, GoblinRenderer.forwardRotation, 0.1F);
               GoblinRenderer.renderEntityInFirstPerson(goblinGirl, event.getPartialTicks());
               GoblinRenderer.lastPlayerYaw = yaw;
               GoblinRenderer.prevStrafeRotation = GoblinRenderer.strafeRotation;
               GoblinRenderer.lastPlayerPitch = pitch;
               GoblinRenderer.prevForwardRotation = GoblinRenderer.forwardRotation;
               GlStateManager.enableLighting();
               GlStateManager.enableDepth();
               GlStateManager.enableAlpha();
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderWorldLastPlayer(RenderWorldLastEvent event) {
      Minecraft minecraft = Minecraft.getMinecraft();
      if (minecraft.player != null) {
         UUID playerUuid = minecraft.player.getPersistentID();

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl.world.isRemote && !girl.isDead && girl instanceof IGoblin) {
                  IGoblin iGoblin = (IGoblin)girl;
                  if (girl.getCurrentAction() == Action.START_THROWING) {
                     girl.setLocallyRegistered(true);
                     minecraft.getRenderManager().renderEntity(girl, 0.0, 0.0, 0.0, playerUuid.equals(iGoblin.getOwnerUUID()) ? -420.69F : 0.0F, minecraft.getRenderPartialTicks(), false);
                     girl.setLocallyRegistered(false);
                     return;
                  }
               }
            }
         } catch (ConcurrentModificationException exception) {
         }

         GlStateManager.enableLighting();
         GlStateManager.enableDepth();
         GlStateManager.enableAlpha();
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderHand(RenderHandEvent event) {
      Minecraft minecraft = Minecraft.getMinecraft();
      UUID playerUuid = minecraft.player.getPersistentID();

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl instanceof IGoblin) {
               Action action = girl.getCurrentAction();
               if (action == Action.PICK_UP || action == Action.START_THROWING) {
                  IGoblin iGoblin = (IGoblin)girl;
                  UUID ownerUuid = iGoblin.getOwnerUUID();
                  if (playerUuid.equals(ownerUuid)) {
                     event.setCanceled(true);
                     break;
                  }
               }
            }
         }
      } catch (ConcurrentModificationException exception) {
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onPre(Pre event) {
      UUID playerUuid = event.getEntityPlayer().getPersistentID();

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl instanceof IGoblin) {
               IGoblin iGoblin = (IGoblin)girl;
               Action action = girl.getCurrentAction();
               if ((action == Action.PICK_UP || action == Action.START_THROWING) && playerUuid.equals(iGoblin.getOwnerUUID())) {
                  event.setCanceled(true);
                  break;
               }
            }
         }
      } catch (ConcurrentModificationException exception) {
      }
   }

}
