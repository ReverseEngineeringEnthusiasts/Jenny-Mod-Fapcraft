package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.particle.DragonBreathParticle;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SuccubusDamageSource;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> Galath's per-tick scene dispatcher, split out of
 * {@link GalathEntity}: the scene enders (RAPE_CUM / CORRUPT_CUM teardown, the
 * wild rape kill), the wild-system tick chain, and the client-side visual
 * helpers that ride along with it (coin scene, corrupt-intro camera, sword
 * particles, wing flag). Every method is stateless bookkeeping over the
 * entity's package-private state and data keys.
 * <p>
 * <b>Flow.</b> {@link GalathEntity#onUpdate()} calls the wild chain
 * ({@link #tickWildSystems(GalathEntity)}) or the tamed chain
 * ({@link #handleRapeCum(GalathEntity)}, {@link #endTamedCorruptScene(GalathEntity)});
 * {@link #tickGiveCoinScene(GalathEntity)} runs on the client every tick.
 * <p>
 * <b>Pitfalls.</b> The enders only act on the server and only once the action's
 * {@code ticksPlaying} passes the threshold — they also restore player
 * movement, so they must not be called from the client. {@code HIDE_EFFECTS_FLAG}
 * pins the wings animated regardless of the current action.
 */
final class GalathSceneTicks {
   private GalathSceneTicks() {
   }

   /**
    * CLIENT: the GIVE_COIN scene — at tick 95 summons the coin for the local
    * player, and during ticks 25-38 spawns the dragon-breath particles
    * streaming between the two hands (progress lerp between the weapon and
    * offhand bones).
    */
   @SideOnly(Side.CLIENT)
   static void tickGiveCoinScene(GalathEntity galath) {
      if (galath.getCurrentAction() == Action.GIVE_COIN) {
         int coinTick = Action.GIVE_COIN.ticksPlaying[1];
         if (coinTick == 95) {
            GalathCoinItem.summonForPlayer(Minecraft.getMinecraft().player, galath);
         }

         if (coinTick > 25 && coinTick < 38) {
            Vec3d pos = galath.getPositionVector();
            Vec3d weaponPos = galath.getCachedBoneOffset("weapon").add(pos);
            Vec3d offhandPos = galath.getCachedBoneOffset("offhand").add(pos);
            DragonBreathParticle.BREATH_SCALE = 0.5F;

            for (float t = 0.0F; t < 1.0F; t += 0.2F) {
               Vec3d lerped = RotationHelper.lerpVec3dDouble(weaponPos, offhandPos, t);
               Minecraft.getMinecraft().effectRenderer.addEffect(new DragonBreathParticle(galath.world, lerped.x, lerped.y, lerped.z));
            }
         }
      }
   }

   /**
    * SERVER: ends the rape scene — 28 ticks into RAPE_CUM she un-anchors,
    * resets to NULL and releases the player (positioned on solid ground with
    * movement restored).
    */
   static void handleRapeCum(GalathEntity galath) {
      if (!galath.world.isRemote) {
         if (galath.getCurrentAction() == Action.RAPE_CUM) {
            if (Action.RAPE_CUM.ticksPlaying[0] >= 28) {
               galath.setAnchored(false);
               galath.setCurrentAction(Action.NULL);
               EntityPlayer player = galath.getPlayerEntity();
               galath.setInteractionPlayerUUID(null);
               if (player != null) {
                  player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
                  PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
               }
            }
         }
      }
   }

   static void endTamedCorruptScene(GalathEntity galath) {
      if (!galath.world.isRemote) {
         if (galath.getCurrentAction() == Action.CORRUPT_CUM) {
            if (Action.CORRUPT_CUM.ticksPlaying[0] >= 30) {
               galath.setAnchored(false);
               galath.setCurrentAction(Action.NULL);
               EntityPlayer player = galath.getPlayerEntity();
               galath.setInteractionPlayerUUID(null);
               if (player != null) {
                  player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
                  PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
               }
            }
         }
      }
   }

   static void tickWildSystems(GalathEntity galath) {
      GalathEntity.getAimYaw(galath, 0.0F);
      GalathEnergyBalls.resetEnergyBalls(galath);
      galath.updateFlightRenderAngles();
      spawnSwordAttackParticles(galath);
      clearSwordRenderFlags(galath);
      restoreStarsVisible(galath);
      updateWingsAnimated(galath);
      handleCorruptCum(galath);
      if (galath.world.isRemote) {
         tickCorruptIntroCamera(galath);
      }
   }

   /**
    * SERVER: the corrupt-scene ender — 30 ticks into CORRUPT_CUM advances to
    * the coin-give scene ({@link Action#GIVE_COIN}).
    */
   static void handleCorruptCum(GalathEntity galath) {
      if (!galath.world.isRemote) {
         if (galath.getCurrentAction() == Action.CORRUPT_CUM) {
            if (Action.CORRUPT_CUM.ticksPlaying[0] >= 30) {
               galath.setCurrentAction(Action.GIVE_COIN);
            }
         }
      }
   }

   static void updateWingsAnimated(GalathEntity galath) {
      if ((Boolean)galath.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
         galath.wingsAnimated = true;
      } else {
         switch (galath.getCurrentAction()) {
            case RAPE_INTRO:
            case RAPE_ON_GOING:
            case RAPE_CUM:
            case RAPE_CHARGE:
            case RAPE_CUM_IDLE:
            case CORRUPT_SLOW:
            case CORRUPT_FAST:
            case CORRUPT_CUM:
            case MASTERBATE:
               galath.wingsAnimated = true;
            case RAPE_PREPARE:
               return;
            default:
               galath.wingsAnimated = false;
         }
      }
   }

   /**
    * CLIENT: re-enables the knock-out star overlay every tick except during
    * {@link Action#KNOCK_OUT_STAND_UP} — the {@code dontDrawStars} animation
    * key clears {@link GalathEntity#drawStars}, this puts it back.
    */
   static void restoreStarsVisible(GalathEntity galath) {
      if (galath.world.isRemote) {
         if (galath.getCurrentAction() != Action.KNOCK_OUT_STAND_UP) {
            galath.drawStars = true;
         }
      }
   }

   static void updateHealthBar(GalathEntity galath) {
      galath.bossBar.setPercent(galath.getHealth() / galath.getMaxHealth());
   }

   static void updateGravity(GalathEntity galath) {
      if (!(Boolean)galath.getDataManager().get(GalathEntity.PARALYZED)) {
         galath.setNoGravity(galath.getTargetEntity() != null);
      }
   }

   static void clearSwordRenderFlags(GalathEntity galath) {
      if (galath.getCurrentAction() != Action.ATTACK_SWORD) {
         galath.renderSword = false;
         galath.spawnSwordParticles = false;
      }
   }

   /**
    * CLIENT: tracks the energy-ball attack — spawns dragon-breath particles
    * along the weapon bone while the sword is drawn ({@code spawnSwordParticles}).
    */
   static void spawnSwordAttackParticles(GalathEntity galath) {
      if (galath.world.isRemote) {
         if (galath.spawnSwordParticles) {
            Vec3d pos = galath.getPositionVector();
            Vec3d startPos = galath.getCachedBoneOffset("weaponStart").add(pos);
            Vec3d endPos = galath.getCachedBoneOffset("weaponEnd").add(pos);
            float step = 0.1F;
            Random random = galath.getRNG();

            for (float t = 0.0F; t < 1.0F; t += step) {
               Vec3d lerped = RotationHelper.lerpVec3dDouble(startPos, endPos, t);

               for (int i = 0; i < 3; i++) {
                  galath.world
                     .spawnParticle(
                        EnumParticleTypes.DRAGON_BREATH,
                        lerped.x + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                        lerped.y + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                        lerped.z + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                        0.0,
                        0.0,
                        0.0,
                        new int[0]
                     );
               }
            }

            for (int i = 0; i < 3; i++) {
               galath.world
                  .spawnParticle(
                     EnumParticleTypes.DRAGON_BREATH,
                     endPos.x + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1) * (random.nextBoolean() ? 1 : -1),
                     endPos.y + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                     endPos.z + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                     0.0,
                     0.0,
                     0.0,
                     new int[0]
                  );
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   static void tickCorruptIntroCamera(GalathEntity galath) {
      Action action = galath.getCurrentAction();
      if (galath.previousAction != Action.CORRUPT_INTRO && action == Action.CORRUPT_INTRO) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         if (!player.getPersistentID().equals(galath.getInteractionPlayerUUID())) {
            galath.previousAction = action;
         } else {
            float yaw = galath.hasMaster() ? 0.0F : galath.getYawRotation() + 180.0F;
            player.rotationYaw = yaw;
            player.prevRotationYaw = yaw;
            player.rotationPitch = 80.0F;
            player.prevRotationPitch = 80.0F;
            galath.previousAction = action;
         }
      } else {
         galath.previousAction = action;
      }
   }

   static void tickWildRapeCum(GalathEntity galath) {
      if (!galath.hasMaster()) {
         Action action = galath.getCurrentAction();
         if (action != Action.RAPE_CUM) {
            galath.rapeCumTicks = 0;
         } else {
            EntityPlayer player = galath.getPlayerEntity();
            if (player == null) {
               galath.rapeCumTicks = 0;
            } else if (++galath.rapeCumTicks == 15) {
               player.attackEntityFrom(new SuccubusDamageSource(galath), 2.1474836E9F);
            }
         }
      }
   }
}
