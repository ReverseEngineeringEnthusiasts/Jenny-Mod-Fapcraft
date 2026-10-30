package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;

/**
 * Player placement around a girl: zeroing motion, snapping the bound player to
 * the camera spot, relative camera offsets and the "in front of the player"
 * vectors.
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; the entity keeps its methods
 * as delegates and calls {@code getVectorTowardPlayer} / {@code setYawRotation}
 * back through the girl so overrides keep dispatching.
 */
final class GirlPlayerPlacement {
   private GirlPlayerPlacement() {
   }

   static void alignPlayerToGirl(BaseGirlEntity girl, EntityPlayerMP player, boolean teleport) {
      player.motionX = 0.0;
      player.motionY = 0.0;
      player.motionZ = 0.0;
      if (teleport) {
         Vec3d offset = girl.getVectorTowardPlayer(0.35);
         player.setPositionAndUpdate(offset.x, offset.y, offset.z);
      }
   }

   static void snapPlayerToPosition(BaseGirlEntity girl, UUID playerUUID) {
      EntityPlayer player = girl.world.getPlayerEntityByUUID(playerUUID);
      player.motionX = 0.0;
      player.motionY = 0.0;
      player.motionZ = 0.0;
      Vec3d offset = girl.getVectorTowardPlayer(0.35);
      player.setPositionAndUpdate(offset.x, offset.y, offset.z);
      girl.setYawRotation(player.rotationYawHead + 180.0F);
   }

   static void positionPlayerRelative(BaseGirlEntity girl, double dx, double dy, double dz, float yaw, float pitch) {
      if (girl.getInteractionPlayerUUID() == null) {
         Main.LOGGER.info("couldnt move camera because the player isn't set");
      } else {
         EntityPlayer player = girl.world.getPlayerEntityByUUID(girl.getInteractionPlayerUUID());
         if (girl.cameraOriginPos == null) {
            girl.cameraOriginPos = player.getPositionVector();
         }

         Vec3d newPos = girl.cameraOriginPos;
         newPos = newPos.add(-Math.sin((girl.cameraYaw + 90.0F) * (Math.PI / 180.0)) * dx, 0.0, Math.cos((girl.cameraYaw + 90.0F) * (Math.PI / 180.0)) * dx);
         newPos = newPos.add(0.0, dy, 0.0);
         newPos = newPos.add(-Math.sin(girl.cameraYaw * (Math.PI / 180.0)) * dz, 0.0, Math.cos(girl.cameraYaw * (Math.PI / 180.0)) * dz);
         if (girl.world.isRemote) {
            PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), newPos, girl.cameraYaw + yaw, pitch));
         } else {
            player.setPositionAndRotation(newPos.x, newPos.y, newPos.z, girl.cameraYaw + yaw, pitch);
            player.setPositionAndUpdate(newPos.x, newPos.y, newPos.z);
            girl.motionX = 0.0;
            girl.motionY = 0.0;
            girl.motionZ = 0.0;
         }
      }
   }

   static Vec3d getFrontOffsetVector(BaseGirlEntity girl) {
      return girl.getVectorTowardPlayer(1.0);
   }

   /** @return a point {@code distance} blocks in front of the interacting player's yaw */
   static Vec3d getVectorTowardPlayer(BaseGirlEntity girl, double distance) {
      UUID uuid = girl.getInteractionPlayerUUID();
      EntityPlayer player = uuid == null ? null : girl.world.getPlayerEntityByUUID(uuid);
      if (player == null) {
         return girl.getPositionVector();
      }

      float yaw = player.rotationYaw;
      return player.getPositionVector().add(-Math.sin(yaw * (Math.PI / 180.0)) * distance, 0.0, Math.cos(yaw * (Math.PI / 180.0)) * distance);
   }

   static Vec3d getVectorTowardPlayer(BaseGirlEntity girl) {
      Vec3d girlPos = girl.getPositionVector();
      EntityPlayer player = Minecraft.getMinecraft().player;
      if (player == null) {
         return girlPos;
      }

      Vec3d playerPos = player.getPositionVector();
      Vec3d dir = girlPos.subtract(playerPos).normalize();
      return playerPos.add(dir);
   }
}
