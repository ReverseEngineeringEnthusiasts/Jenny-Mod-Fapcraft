package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.ClientServerCheck;
import com.trolmastercard.sexmod.util.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> Galath's riding and camera helpers, split out of
 * {@link GalathEntity}: gravity while somebody sits on her, the render yaw she
 * copies from her rider, the boost impulse, the master-facing pitch and the
 * teleport that puts her under a player.
 * <p>
 * <b>Flow.</b> {@link GalathEntity#onUpdate()} calls
 * {@link #updateRidingGravity(GalathEntity)}, {@code tickTamedSystems} the
 * rest; the tamed AI calls {@link #handlePlayerRide(GalathEntity, EntityPlayer)}
 * when the owner is too far away to walk to.
 */
final class GalathRiding {
   private GalathRiding() {
   }

   static void updateRidingGravity(GalathEntity galath) {
      galath.setNoGravity(galath.getRidingPlayer() != null);
   }

   static void syncRenderYawToRider(GalathEntity galath) {
      EntityPlayer rider = galath.getRidingPlayer();
      if (rider != null) {
         galath.prevRenderYawOffset = rider.prevRotationYawHead;
         galath.renderYawOffset = rider.rotationYawHead;
      }
   }

   static void tickBoostFlight(GalathEntity galath) {
      EntityPlayer player = galath.getRidingPlayer();
      Action action = galath.getCurrentAction();
      if (player != null) {
         if (action == Action.BOOST) {
            int sideIndex = ClientServerCheck.getInstance() ? 0 : 1;
            if (action.ticksPlaying[sideIndex] >= 13) {
               if (action.ticksPlaying[sideIndex] == 13) {
                  galath.boostSpeed = 6.0F;
               }

               Vec3d look = player.getLook(0.0F).normalize();
               galath.motionX = look.x * galath.boostSpeed;
               galath.motionY = look.y * galath.boostSpeed;
               galath.motionZ = look.z * galath.boostSpeed;
               galath.boostSpeed *= 0.94F;
            }
         }
      }
   }

   static void updateMasterLookPitch(GalathEntity galath) {
      if (galath.world.isRemote) {
         if (galath.getRidingPlayer() == null) {
            EntityPlayer master = galath.getMasterPlayer();
            if (master != null) {
               handleGalathPlayer(galath, master);
            }
         }
      }
   }

   static void handleGalathPlayer(GalathEntity galath, EntityPlayer player) {
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
      Vec3d headPos = new Vec3d(player.posX, player.posY + (playerGirl == null ? player.eyeHeight : playerGirl.getEyeHeight()), player.posZ);
      Vec3d eyePos = new Vec3d(galath.posX, galath.posY + galath.getEyeHeight(), galath.posZ);
      double dist = eyePos.distanceTo(headPos);
      double heightDiff = headPos.y - eyePos.y;
      galath.rotationPitch = (float)(-(Math.sin(heightDiff / dist) * (180.0 / Math.PI)));
   }

   static void handlePlayerRide(GalathEntity galath, EntityPlayer player) {
      int attempts = 0;

      BlockPos teleportPos;
      do {
         teleportPos = player.getPosition().add(Reference.RANDOM.nextInt(4), 0, Reference.RANDOM.nextInt(4));
      } while (++attempts < 20 && !galath.attemptTeleport(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ()));

      if (attempts >= 20) {
         galath.setPosition(player.posX, player.posY, player.posZ);
      }

      galath.motionX = 0.0;
      galath.motionY = 0.0;
      galath.motionZ = 0.0;
   }
}
