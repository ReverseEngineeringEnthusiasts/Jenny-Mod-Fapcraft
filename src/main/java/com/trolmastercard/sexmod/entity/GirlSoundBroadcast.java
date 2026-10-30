package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

/**
 * Server-side sound broadcast helpers: sends a sound packet to every player
 * tracking the girl, optionally pinning it one block in front of each player.
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; the entity keeps the same
 * public static methods as delegates.
 */
final class GirlSoundBroadcast {
   private GirlSoundBroadcast() {
   }

   static void girlPlaySound(BaseGirlEntity girl, SoundEvent sound, boolean atPlayer) {
      Vec3d pos = girl.getPositionVector();

      for (EntityPlayer player : WorldUtils.getNearbyPlayers(girl)) {
         Vec3d soundPos;
         if (!atPlayer) {
            soundPos = pos;
         } else {
            Vec3d playerPos = player.getPositionVector();
            Vec3d dir = pos.subtract(playerPos).normalize();
            soundPos = playerPos.add(dir);
         }

         ((EntityPlayerMP)player)
            .connection
            .sendPacket(new SPacketSoundEffect(sound, SoundCategory.AMBIENT, soundPos.x, soundPos.y, soundPos.z, 1.0F, 1.0F));
      }
   }

   static void girlPlaySound(BaseGirlEntity girl, SoundEvent sound) {
      girlPlaySound(girl, sound, false);
   }

   static void playRandomSound(BaseGirlEntity girl, SoundEvent[] sounds) {
      girlPlaySound(girl, SoundHandler.randomSound(sounds));
   }

   static void playRandomSound(BaseGirlEntity girl, SoundEvent[] sounds, boolean atPlayer) {
      girlPlaySound(girl, SoundHandler.randomSound(sounds), atPlayer);
   }
}
