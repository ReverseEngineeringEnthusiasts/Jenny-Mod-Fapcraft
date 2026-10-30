package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * <b>Role.</b> Galath's action state machine and scene start/teardown hooks,
 * split out of {@link GalathEntity}: the {@code setCurrentAction} gate, the
 * coin grant, the rape/hug/morning-blowjob enders, the Manglelie partner spawn
 * and the repositioning that puts the bound player into a scene.
 * <p>
 * <b>Flow.</b> Scenes enter through the packet and command entry points
 * ({@link #handleRapeState(GalathEntity)}, {@link #startPussyLicking(GalathEntity)},
 * {@link #startMasterbateSitting(GalathEntity)}); every transition runs the
 * {@link #setCurrentAction(GalathEntity, Action)} gate, which then fires the
 * GIVE_COIN / HUG_MANG / MORNING_BLOWJOB_CUM enders.
 * <p>
 * <b>Pitfalls.</b> The gate refuses to leave GALATH_DE_SUMMON and blocks the
 * cum loops from re-entering their own scene, so an action that fails to
 * advance is usually this gate, not the ticker. All teardown hooks are
 * SERVER-only; a client-side call would desync the player.
 */
final class GalathActions {
   private GalathActions() {
   }

   /**
    * SERVER: the state-machine gate — guards the cum loops and
    * GALATH_DE_SUMMON, persists the cum time via {@link GirlSavedData} for
    * the tamed cum scenes, and fires the GIVE_COIN
    * ({@link #grantCoinAndBindMaster(GalathEntity)}), HUG_MANG
    * ({@link #endHugScene(GalathEntity)}) and MORNING_BLOWJOB_CUM
    * ({@link #endMorningBlowjobScene(GalathEntity)}) teardown hooks.
    */
   static void setCurrentAction(GalathEntity galath, Action action) {
      Action currentAction = galath.getCurrentAction();
      if (currentAction == Action.GALATH_DE_SUMMON) {
         return;
      }

      if (currentAction == Action.CORRUPT_CUM && (action == Action.CORRUPT_FAST || action == Action.CORRUPT_SLOW)) {
         return;
      }

      if (currentAction == Action.RAPE_CUM && action == Action.RAPE_ON_GOING) {
         return;
      }

      if (currentAction == Action.MORNING_BLOWJOB_CUM && (action == Action.MORNING_BLOWJOB_SLOW || action == Action.MORNING_BLOWJOB_FAST)) {
         return;
      }

      if (!galath.world.isRemote && Action.isAny(currentAction, Action.CORRUPT_CUM, Action.RAPE_CUM, Action.MORNING_BLOWJOB_CUM)) {
         GirlSavedData.saveCumTime(galath.getInteractionPlayerUUID(), galath.world.getTotalWorldTime());
      }

      if (action == Action.CORRUPT_SLOW) {
         galath.corruptHard = false;
         if (currentAction == Action.CORRUPT_INTRO) {
            setFlying(galath, false);
         }

         if (galath.hasMaster() && currentAction == Action.NULL) {
            setFlying(galath, true);
         }
      }

      if (currentAction == Action.GIVE_COIN && action == Action.NULL && !galath.world.isRemote) {
         grantCoinAndBindMaster(galath);
      }

      if (currentAction == Action.HUG_MANG && action == Action.NULL) {
         endHugScene(galath);
      }

      if (currentAction == Action.MORNING_BLOWJOB_CUM && action == Action.NULL) {
         endMorningBlowjobScene(galath);
      }

      galath.setCurrentActionBase(action);
   }

   /**
    * SERVER: ends the rape scene — once the RAPE_ON_GOING loop leaves for
    * RAPE_CUM she switches to flight repositioning (CHANGE_POSITION) and
    * releases the player.
    */
   static void handleRapeState(GalathEntity galath) {
      Action action = galath.getCurrentAction();
      if (action == Action.RAPE_ON_GOING) {
         galath.activeFlight = GalathFlightData.CHANGE_POSITION;
         galath.activeFlight.executeStart(galath);
         galath.setAnchored(false);
         galath.setCurrentAction(Action.FLY);
         EntityPlayer player = galath.getPlayerEntity();
         galath.setInteractionPlayerUUID(null);
         if (player != null) {
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
         }

         BaseGirlEntity.girlPlaySound(galath, SoundHandler.GIRLS_GALATH_DIALOG[0]);
      }
   }

   /**
    * SERVER: the morning-blowjob teardown — resets the bound player and the
    * girl after MORNING_BLOWJOB_CUM.
    */
   static void endMorningBlowjobScene(GalathEntity galath) {
      EntityPlayer player = galath.getPlayerEntity();
      if (player != null) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)player);
      }

      ResetGirlPacket.Handler.resetGirl(galath);
   }

   static void endHugScene(GalathEntity galath) {
      galath.setAnchored(false);
      ManglelieEntity manglelie = galath.getMangleliePartner(true);
      if (manglelie != null) {
         manglelie.setCorrupting(true);
      }
   }

   /**
    * SERVER: the coin grant — swaps the player's main hand for the
    * {@link GalathCoinItem}, unbinds the scene, binds her to the player as
    * master and explains the coin mechanic in chat.
    */
   static void grantCoinAndBindMaster(GalathEntity galath) {
      EntityPlayer player = galath.getPlayerEntity();
      if (player != null) {
         ItemStack stack = player.getHeldItemMainhand();
         player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(GalathCoinItem.GALATH_COIN));
         if (!stack.isEmpty()) {
            player.inventory.addItemStackToInventory(stack);
         }

         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
         galath.setInteractionPlayerUUID(null);
         galath.setTargetEntity(null);
         player.sendMessage(
            new TextComponentString(
               TextFormatting.GRAY
                  + "Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin"
            )
         );
         GirlSavedData.updateMangleliePartner(galath);
         player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
      }
   }

   static void setFlying(GalathEntity galath, boolean flying) {
      EntityPlayer player = galath.getPlayerEntity();
      if (player != null) {
         Vec3d pos;
         if (flying) {
            pos = new Vec3d(-0.5, 0.5F - player.getEyeHeight(), 0.4F).add(galath.getTargetPosition());
         } else {
            pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - player.getEyeHeight(), 0.4F), galath.getYawRotation()).add(galath.getTargetPosition());
         }

         player.setPositionAndUpdate(pos.x, pos.y, pos.z);
      }
   }

   static boolean canStartPussyLicking(GalathEntity galath) {
      if (galath.getMangleliePartner(true) != null) {
         return false;
      }

      ManglelieEntity manglelie = new ManglelieEntity(galath.world);
      galath.setMangleliePartnerUUID(manglelie.getGirlId());
      manglelie.setGalathPartnerUUID(galath.getGirlId());
      manglelie.setCorrupting(true);
      manglelie.setCurrentAction(Action.RIDE_MOMMY_HEAD);
      manglelie.setPositionAndUpdate(galath.posX, galath.posY, galath.posZ);
      galath.world.spawnEntity(manglelie);
      return true;
   }

   static void startMasterbateSitting(GalathEntity galath) {
      if (galath.getCurrentAction() != Action.MASTERBATE_SITTING) {
         galath.masterbateSittingPending = true;
         galath.setCurrentAction(Action.MASTERBATE_SITTING);
      }
   }

   static void startPussyLicking(GalathEntity galath) {
      galath.a5 = true;
      galath.setCurrentAction(Action.PUSSY_LICKING);
   }

   static void resetInteractionState(GalathEntity galath) {
      galath.setInteractionPlayerUUID(null);
      galath.setCurrentAction((Action)null);
   }
}
