package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * CLIENT scene camera for the horny-potion player-girls
 * ({@link AbstractPlayerGirlEntity}) — NOT the NPC scenes.
 * <p>
 * While the transformed player-girl is in a scene (non-anchored), the camera
 * is attached to the girl model's {@code girlCam} bone: the player's
 * position is overridden on the render tick start ({@link #onRenderTickStart},
 * {@link #onRenderTickEnd} save/restore the real position) and the girl's
 * body is rendered first-person via {@link #applyCameraTransform}. When the
 * girl is anchored the camera rolls 180&deg; ({@link #onCameraSetup}).
 * <p>
 * <b>Pitfall:</b> the camera only detaches when the girl's scene state clears
 * (action/interaction reset). The NPC-scene R-Shift exit therefore must go
 * through {@code resetCameraAndPhysics()} on the client (clears
 * {@code cameraOriginPos}) — sending only the server {@code ResetGirlPacket}
 * leaves the camera attached and the player model visible in the scene view.
 */
public class GirlCameraHelper {
   public static final float CAMERA_SCALE = 1.2345679F;
   Vec3d playerPos = null;
   Vec3d playerLastPos = null;
   AbstractPlayerGirlEntity currentGirl = null;
   boolean isSmoothing = false;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onPre(Pre event) {
      if (event.getPartialRenderTick() != 1.2345679F) {
         AbstractPlayerGirlEntity.rebuildPlayerGirlTableFromWorld();
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(event.getEntityPlayer().getPersistentID());
         if (playerGirl != null) {
            event.setCanceled(true);
            applyCameraTransform(playerGirl, event.getEntityPlayer(), event.getX(), event.getY(), event.getZ(), event.getPartialRenderTick());
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static void applyCameraTransform(AbstractPlayerGirlEntity girl, EntityPlayer player, double x, double y, double z, float partialTicks) {
      Minecraft mc = Minecraft.getMinecraft();
      player = girl.resolvePlayerEntity(player);
      if (!player.isInvisibleToPlayer(mc.player) || girl.E_clash458()) {
         RenderManager renderManager = mc.getRenderManager();
         girl.rotationYaw = player.rotationYaw;
         girl.prevRotationYawHead = player.prevRotationYawHead;
         girl.rotationYawHead = player.rotationYawHead;
         girl.prevRotationPitch = player.prevRotationPitch;
         girl.rotationPitch = player.rotationPitch;
         girl.prevRotationYaw = player.prevRotationYaw;
         girl.prevPosX = player.prevPosX;
         girl.prevPosY = player.prevPosY;
         girl.prevPosZ = player.prevPosZ;
         girl.lastTickPosX = player.lastTickPosX;
         girl.lastTickPosY = player.lastTickPosY;
         girl.lastTickPosZ = player.lastTickPosZ;
         girl.renderYawOffset = player.renderYawOffset;
         girl.prevRenderYawOffset = player.prevRenderYawOffset;
         girl.ad = player.isSneaking();
         girl.aj = player.isSprinting();
         girl.ak = player.isRiding();
         girl.af = player.onGround;
         girl.ah = player.getItemInUseCount() != 0;
         double dX = player.lastTickPosX - player.posX;
         double dZ = player.posZ - player.lastTickPosZ;
         double yawRad = (Math.PI / 180.0) * player.rotationYaw;
         girl.ao = new Vector2f((float)(dX * Math.cos(yawRad) + dZ * Math.sin(yawRad)), (float)(dX * Math.sin(yawRad) + dZ * Math.cos(yawRad)));
         float cameraOffset = girl.isRidingSomething() ? getCameraOffset(girl, player) : 0.0F;
         GirlPlayerRenderer.isFirstPerson = true;
         renderManager.renderEntity(girl, x, y + cameraOffset, z, 90.0F, partialTicks, false);
      }
   }

   static float getCameraOffset(AbstractPlayerGirlEntity girl, EntityPlayer player) {
      if ((Boolean)girl.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
         return 0.0F;
      }

      if ((player.getHeldItemMainhand().getItem() instanceof ItemBow || player.getHeldItemOffhand().getItem() instanceof ItemBow) && girl.ah) {
         girl.setCurrentAction(Action.BOW);
      }

      if (girl.getCurrentAction() == Action.BOW && !girl.ah) {
         girl.setCurrentAction(Action.NULL);
      }

      if (girl.getCurrentAction() == Action.BOW) {
         girl.rotationYaw = girl.rotationYawHead;
         girl.renderYawOffset = girl.rotationYawHead;
         girl.prevRenderYawOffset = girl.prevRotationYawHead;
      }

      if (girl.ak) {
         return player.getRidingEntity() instanceof EntityBoat ? 0.4F : 0.2F;
      } else {
         return 0.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderTickEnd(RenderTickEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null) {
         if (event.phase == Phase.END) {
            if (this.playerPos != null) {
               mc.player.setPosition(this.playerPos.x, this.playerPos.y, this.playerPos.z);
               mc.player.lastTickPosX = this.playerLastPos.x;
               mc.player.lastTickPosY = this.playerLastPos.y;
               mc.player.lastTickPosZ = this.playerLastPos.z;
               this.playerPos = null;
               this.playerLastPos = null;
            }
         } else if (mc.gameSettings.thirdPersonView == 0) {
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(mc.player.getPersistentID());
            if (playerGirl != null) {
               if (playerGirl.isSceneActive()) {
                  this.playerPos = mc.player.getPositionVector();
                  this.playerLastPos = new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ);
                  Vec3d camPos = playerGirl.getCachedBoneOffset("girlCam");
                  camPos = playerGirl.getOwnerAimVector(camPos, event.renderTickTime);
                  camPos = camPos.add(RotationHelper.lerpVec3dDouble(this.playerLastPos, this.playerPos, event.renderTickTime));
                  mc.player.posX = camPos.x;
                  mc.player.posY = camPos.y - mc.player.getEyeHeight();
                  mc.player.posZ = camPos.z;
                  mc.player.lastTickPosX = camPos.x;
                  mc.player.lastTickPosY = camPos.y - mc.player.getEyeHeight();
                  mc.player.lastTickPosZ = camPos.z;
                  Action action = playerGirl.getCurrentAction();
                  float yaw = playerGirl.getYawRotation();
                  if (!playerGirl.canPerformAction(action, mc.player)) {
                     if (action.flipGirlYaw) {
                        yaw += 180.0F;
                     }

                     if (mc.player.rotationPitch > action.maxGirlPitch) {
                        mc.player.rotationPitch = action.maxGirlPitch;
                        mc.player.prevRotationPitch = action.maxGirlPitch;
                     }

                     if (mc.player.rotationPitch < action.minGirlPitch) {
                        mc.player.rotationPitch = action.minGirlPitch;
                        mc.player.prevRotationPitch = action.minGirlPitch;
                     }

                     if (mc.player.rotationYaw > yaw + 90.0F) {
                        mc.player.rotationYaw = yaw + 90.0F;
                        mc.player.prevRotationYaw = yaw + 90.0F;
                     }

                     if (mc.player.rotationYaw < yaw - 90.0F) {
                        mc.player.rotationYaw = yaw - 90.0F;
                        mc.player.prevRotationYaw = yaw - 90.0F;
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onCameraSetup(CameraSetup event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null) {
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(mc.player.getPersistentID());
         if (playerGirl != null) {
            if (playerGirl.F_clash231()) {
               if (playerGirl.isAnchored()) {
                  event.setRoll(180.0F);
                  event.setPitch(-event.getPitch());
                  event.setYaw(-event.getYaw());
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (this.playerPos != null) {
         if (mc.gameSettings.thirdPersonView == 0) {
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(mc.player.getPersistentID());
            if (playerGirl != null) {
               Vec3d playerPos = mc.player.getPositionVector();
               Vec3d lerpedPos = RotationHelper.lerpVec3dDouble(this.playerLastPos, this.playerPos, event.getPartialTicks());
               Vec3d offset = lerpedPos.subtract(playerPos);
               applyCameraTransform(playerGirl, mc.player, offset.x, offset.y, offset.z, event.getPartialTicks());
               GlStateManager.enableLighting();
               GlStateManager.enableDepth();
               GlStateManager.enableAlpha();
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderTickStart(RenderTickEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null) {
         if (event.phase != Phase.END) {
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(mc.player.getPersistentID());
            if (playerGirl == null) {
               if (this.isSmoothing) {
                  this.isSmoothing = false;
                  mc.player.eyeHeight = mc.player.getDefaultEyeHeight();
               }
            } else if (playerGirl.isAnchored()) {
               if (this.isSmoothing) {
                  this.isSmoothing = false;
                  mc.player.eyeHeight = mc.player.getDefaultEyeHeight();
               }
            } else {
               if (this.currentGirl != playerGirl) {
                  applyCameraTransform(playerGirl, mc.player, 0.0, 500.0, 0.0, event.renderTickTime);
                  this.currentGirl = playerGirl;
               }

               mc.player.eyeHeight = playerGirl.getCameraBoneHeight();
               this.isSmoothing = true;
            }
         }
      }
   }

}
