package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.SuccubusDamageSource;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * <b>Role.</b> Galath's damage plumbing, split out of {@link GalathEntity}:
 * the fire/drown/cactus/fall immunity gate, her own hurt sound throttle, the
 * energy-ball hitbox hits and the per-tick rape damage.
 * <p>
 * <b>Pitfalls.</b> {@link #playHurtSound(GalathEntity, float)} is client-only
 * and stays quiet on a killing blow (the entity's own death sound covers it);
 * the 1000 ms throttle on {@code lastHurtSoundTime} is wall-clock, not ticks.
 */
final class GalathCombat {
   private GalathCombat() {
   }

   static boolean attackEntityFrom(GalathEntity galath, DamageSource source, float amount) {
      if (source.isFireDamage()) {
         return false;
      }

      if (DamageSource.DROWN.equals(source)) {
         return false;
      }

      if (DamageSource.CACTUS.equals(source)) {
         return false;
      }

      if (DamageSource.FALL.equals(source)) {
         return false;
      }

      if (DamageSource.FLY_INTO_WALL.equals(source)) {
         return false;
      }

      playHurtSound(galath, amount);
      return galath.attackEntityFromBase(source, amount);
   }

   static boolean attackEntityFromPart(GalathEntity galath, MultiPartEntityPart part, DamageSource source, float amount) {
      if (galath.world.isRemote) {
         return false;
      }

      if (!(source.getTrueSource() instanceof EntityPlayer)) {
         return false;
      }

      if (part == galath.energyBallHitboxRight) {
         galath.getDataManager().set(GalathEntity.RIGHT_ENERGY_BALL_ALIVE, false);
         PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(galath.energyBallHitboxRight.getPositionVector(), false), galath);
      }

      if (part == galath.energyBallHitboxLeft) {
         galath.getDataManager().set(GalathEntity.LEFT_ENERGY_BALL_ALIVE, false);
         PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(galath.energyBallHitboxLeft.getPositionVector(), false), galath);
      }

      return true;
   }

   static void playHurtSound(GalathEntity galath, float damage) {
      if (galath.world.isRemote) {
         if (!(galath.getHealth() - damage <= 0.0F)) {
            long now = System.currentTimeMillis();
            if (now >= galath.lastHurtSoundTime + 1000L) {
               galath.playRandomSound(SoundHandler.GIRLS_GALATH_UUH);
               galath.lastHurtSoundTime = now;
            }
         }
      }
   }

   /**
    * SERVER: per-tick rape damage — while RAPE_ON_GOING/RAPE_INTRO the bound
    * player takes 1 HP (non-creative, survives at 1), and Galath heals 1.5
    * per pulse when {@code applyDamage} is set.
    */
   static void handleRapeAction(GalathEntity galath, boolean applyDamage) {
      Action action = galath.getCurrentAction();
      if (action == Action.RAPE_ON_GOING || action == Action.RAPE_INTRO) {
         EntityPlayer player = galath.getPlayerEntity();
         if (player != null) {
            if (!(0.0F >= player.getHealth() - 1.0F)) {
               if (!player.capabilities.isCreativeMode) {
                  player.attackEntityFrom(new SuccubusDamageSource(galath), 1.0F);
                  if (applyDamage) {
                     galath.heal(1.5F);
                  }
               }
            }
         }
      }
   }
}
