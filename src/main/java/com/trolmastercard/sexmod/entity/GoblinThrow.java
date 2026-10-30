package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.util.WorldUtils;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Carry and throw kinematics for {@link GoblinEntity} (and, through the
 * {@link BaseGirlEntity} overloads, {@link GoblinPlayerEntity}).
 * <p>
 * Pickup glue: while PICK_UP with a bound owner, the goblin sticks to the
 * owner's position, counts down {@code heldPlayerDistance} (45) and reaches
 * SHOULDER_IDLE (noClip) at 0; it falls back to NULL when the owner vanishes or
 * exceeds 10 blocks. Throw flight: at tick 15 the goblin is launched from the owner's eye
 * with the owner's aim (pitch/yaw), at tick 39 she lands (THROWN) and unbinds
 * owner + interaction player.
 */
final class GoblinThrow {
   private GoblinThrow() {
   }

   static void handleGoblinOwner(GoblinEntity goblin, EntityPlayer player) {
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
      Vec3d headPos = new Vec3d(player.posX, player.posY + (playerGirl == null ? player.eyeHeight : playerGirl.getEyeHeight()), player.posZ);
      Vec3d eyePos = new Vec3d(goblin.posX, goblin.posY + goblin.getEyeHeight(), goblin.posZ);
      double dist = eyePos.distanceTo(headPos);
      double heightDiff = headPos.y - eyePos.y;
      goblin.rotationPitch = (float)(-(Math.sin(heightDiff / dist) * (180.0 / Math.PI)));
   }

   static void handleHeldState(GoblinEntity goblin) {
      if ((Boolean)goblin.entityDataManager.get(GoblinEntity.TAMED)) {
         if (goblin.getInteractionPlayerUUID() == null) {
            if (goblin.getCurrentAction() == Action.NULL) {
               EntityPlayer player = goblin.world.getClosestPlayerToEntity(goblin, 15.0);
               if (player != null && player.getDistance(goblin) < 2.0F) {
                  handleGoblinOwner(goblin, player);
                  goblin.getNavigator().clearPath();
               } else {
                  if (goblin.guardPost == null
                     || goblin.getDistance(goblin.guardPost.getX(), goblin.guardPost.getY(), goblin.guardPost.getZ()) > goblin.getThrowRange()
                     || goblin.guardPostTicks > 100) {
                     int xOffset = (goblin.getRNG().nextBoolean() ? 1 : -1) * goblin.getRNG().nextInt(5);
                     int zOffset = (goblin.getRNG().nextBoolean() ? 1 : -1) * goblin.getRNG().nextInt(5);
                     int height = WorldUtils.getHeightAt(
                        goblin.world, goblin.getPosition().getX() + xOffset, goblin.getPosition().getZ() + zOffset
                     );
                     goblin.guardPost = new BlockPos(goblin.getPosition().getX() + xOffset, height, goblin.getPosition().getZ() + zOffset);
                     goblin.guardPostTicks = 0;
                  }

                  if (Math.sqrt(goblin.guardPost.distanceSq(goblin.getPosition())) > 2.0) {
                     goblin.getNavigator().tryMoveToXYZ(goblin.guardPost.getX(), goblin.guardPost.getY(), goblin.guardPost.getZ(), 0.3F);
                     goblin.tickPathVelocity();
                  } else {
                     goblin.guardPostTicks++;
                  }
               }
            }
         }
      }
   }

   static void handleShoulderIdle(GoblinEntity goblin) {
      if (goblin.getCurrentAction() == Action.SHOULDER_IDLE) {
         UUID uuid = goblin.getOwnerUUID();
         if (uuid != null) {
            EntityPlayer player = goblin.world.getPlayerEntityByUUID(uuid);
            if (player != null) {
               goblin.setPosition(player.posX, player.posY, player.posZ);
               goblin.noClip = true;
               goblin.setNoGravity(true);
            }
         }
      }
   }

   static void updateThrowProgress(GoblinEntity goblin) {
      int progress = goblin.getThrowProgress();
      if (progress != -1) {
         goblin.setThrowProgress(++progress);
         if (progress == 15) {
            Vec3d pos = getGoblinThrowPos(goblin);
            float pitch = getGoblinThrowHeight(goblin);
            float yaw = getGoblinThrowDistance(goblin);
            goblin.setPositionAndUpdate(pos.x, pos.y, pos.z);
            Vec3d vec = rotateVectorPitchYaw(new Vec3d(0.0, 0.0, 1.5), pitch, yaw);
            goblin.motionX = vec.x;
            goblin.motionY = vec.y;
            goblin.motionZ = vec.z;
            if (!goblin.world.isRemote) {
               goblin.setYawRotation(yaw);
            }
         }

         goblin.noClip = false;
         goblin.setNoGravity(false);
         if (progress == 39) {
            goblin.setThrowProgress(-1);
            goblin.setCurrentAction(Action.THROWN);
            goblin.setInteractionPlayerUUID(null);
            goblin.setOwnerUUID(null);
         }
      }
   }

   static void handleGoblinThrowAction(BaseGirlEntity girl) {
      Action action = girl.getCurrentAction();
      IGoblin goblin = (IGoblin)girl;
      if (goblin.getPreviousAction() != Action.START_THROWING && action == Action.START_THROWING) {
         goblin.setThrowProgress(0);
      }

      goblin.setPreviousAction(action);
   }

   static Vec3d getGoblinThrowPos(BaseGirlEntity girl) {
      IGoblin goblin = (IGoblin)girl;
      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return girl.getPositionVector();
      }

      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      return player == null
         ? girl.getPositionVector()
         : player.getPositionVector()
            .add(0.0, player.getEyeHeight(), 0.0)
            .add(rotateVectorPitchYaw(new Vec3d(0.4F, 0.0, 0.0), getGoblinThrowHeight(girl), getGoblinThrowDistance(girl)));
   }

   static float getGoblinThrowDistance(BaseGirlEntity girl) {
      IGoblin goblin = (IGoblin)girl;
      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return 0.0F;
      }

      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      return player == null ? 0.0F : player.rotationYawHead;
   }

   static float getGoblinThrowHeight(BaseGirlEntity girl) {
      IGoblin goblin = (IGoblin)girl;
      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return 0.0F;
      }

      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      return player == null ? 0.0F : player.rotationPitch;
   }

   static Vec3d rotateVectorYaw(Vec3d vec, float yaw) {
      return rotateVectorPitchYaw(vec, 0.0F, yaw);
   }

   static Vec3d rotateVectorPitchYaw(Vec3d vec, float pitch, float yaw) {
      Vec3d rotated = new Vec3d(
         vec.x,
         vec.y * Math.cos(pitch * (Math.PI / 180.0)) - vec.z * Math.sin(pitch * (Math.PI / 180.0)),
         vec.y * Math.sin(pitch * (Math.PI / 180.0)) + vec.z * Math.cos(pitch * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((yaw + 90.0F) * (Math.PI / 180.0)) * rotated.x - Math.sin(yaw * (Math.PI / 180.0)) * rotated.z,
         rotated.y,
         Math.cos((yaw + 90.0F) * (Math.PI / 180.0)) * rotated.x + Math.cos(yaw * (Math.PI / 180.0)) * rotated.z
      );
   }

   static void handlePickUpState(BaseGirlEntity girl) {
      if (girl.getCurrentAction() == Action.PICK_UP) {
         IGoblin goblin = (IGoblin)girl;
         UUID uuid = goblin.getOwnerUUID();
         if (uuid == null) {
            goblin.setHeldPlayerDistance(-1);
            girl.setCurrentAction(Action.NULL);
            goblin.setOwnerUUID(null);
         } else {
            EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
            if (player == null) {
               goblin.setHeldPlayerDistance(-1);
               girl.setCurrentAction(Action.NULL);
               goblin.setOwnerUUID(null);
            } else {
               girl.setPosition(player.posX, player.posY, player.posZ);
               if (girl.getPositionVector().distanceTo(player.getPositionVector()) > 10.0) {
                  goblin.setHeldPlayerDistance(-1);
                  girl.setCurrentAction(Action.NULL);
                  goblin.setOwnerUUID(null);
               } else {
                  int distance = goblin.getHeldPlayerDistance() - 1;
                  goblin.setHeldPlayerDistance(distance);
                  if (distance == 0) {
                     girl.setCurrentAction(Action.SHOULDER_IDLE);
                     girl.noClip = true;
                  }
               }
            }
         }
      }
   }
}
