package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.RequestRidingPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> Galath's right-click entry points, split out of
 * {@link GalathEntity}: the master (tamed) action menu, the client action
 * dispatch that arms the scenes, and the corruption trigger on a knocked-out
 * wild girl.
 * <p>
 * <b>Flow.</b> {@link #processInteract(GalathEntity, EntityPlayer, EnumHand)}
 * routes by master; {@link #doAction(GalathEntity, String, UUID)} is called
 * back from the open {@code BeeScreen} menu and runs its 1200 ms arming thread.
 * <p>
 * <b>Pitfalls.</b> The arming threads are 1200 ms delayed and read
 * {@code Minecraft.player} when they fire, not the click-time player.
 */
final class GalathInteractions {
   private GalathInteractions() {
   }

   static boolean processInteract(GalathEntity galath, EntityPlayer player, EnumHand hand) {
      return galath.hasMaster() ? processMasterInteract(galath, player, hand) : processGirlInteract(galath, player, hand);
   }

   /**
    * SERVER: tamed interaction — only the master may interact; the action
    * menu depends on held items (no coin) and context: ride while airborne,
    * cowgirl/anal (+ threesome with a Manglelie) on the ground.
    */
   static boolean processMasterInteract(GalathEntity galath, EntityPlayer player, EnumHand hand) {
      if (!player.getPersistentID().equals(galath.getMasterUUID())) {
         return false;
      }

      if (Action.isAnyAction(galath, Action.HUG_MANG, Action.RUN, Action.GALATH_SUMMON, Action.GALATH_DE_SUMMON, Action.MASTERBATE)) {
         return false;
      }

      if (!GalathCoinItem.GALATH_COIN.equals(player.getHeldItem(EnumHand.OFF_HAND).getItem())
         && !GalathCoinItem.GALATH_COIN.equals(player.getHeldItem(EnumHand.MAIN_HAND).getItem())) {
         galath.playRandomSound(SoundHandler.GIRLS_GALATH_HUH);
         String[] options;
         if (!player.onGround) {
            options = new String[]{"ride"};
         } else if (galath.getMangleliePartner(false) == null) {
            options = new String[]{"cowgirl", "anal", "ride"};
         } else {
            options = new String[]{"cowgirl", "anal", "threesome", "ride"};
         }

         if (galath.world.isRemote) {
            BaseGirlEntity.openInventoryGui(player, galath.getSelf(), options, false);
         }

         return true;
      } else {
         return false;
      }
   }

   /**
    * CLIENT: master-action dispatch — {@code ride} shows the flight HUD and
    * requests riding; {@code anal}/{@code cowgirl}/{@code threesome} arm the
    * scene after a 1200 ms delay thread (anchor + start the action + bind
    * the player; the threesome also starts Manglelie on
    * {@link Action#THREESOME_SLOW} + {@link Action#PUSSY_LICKING}).
    */
   @SideOnly(Side.CLIENT)
   static void doAction(GalathEntity galath, String action, UUID uuid) {
      if ("ride".equals(action)) {
         GalathFlightHud.showHud();
         PacketHandler.networkWrapper.sendToServer(new RequestRidingPacket());
      } else if ("anal".equals(action)) {
         BeeScreen.enableInteraction();
         HandlePlayerMovement.setMovementLock(false);
         ThreadNames.createDaemonThread(1200, () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            galath.setTargetPosition(player.getPositionVector());
            galath.setYawRotation(0.0F);
            galath.setInteractionPlayerUUID(player.getPersistentID());
            galath.setAnchored(true);
            galath.setCurrentAction(Action.CORRUPT_SLOW);
         });
      } else if ("cowgirl".equals(action)) {
         BeeScreen.enableInteraction();
         HandlePlayerMovement.setMovementLock(false);
         ThreadNames.createDaemonThread(1200, () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            galath.setTargetPosition(player.getPositionVector());
            galath.setYawRotation(player.rotationYaw + 180.0F);
            galath.setCurrentAction(Action.RAPE_INTRO);
            galath.setInteractionPlayerUUID(player.getPersistentID());
            galath.setAnchored(true);
         });
      } else {
         if ("threesome".equals(action)) {
            ManglelieEntity manglelie = galath.getMangleliePartner(false);
            if (manglelie == null) {
               return;
            }

            BeeScreen.enableInteraction();
            HandlePlayerMovement.setMovementLock(false);
            ThreadNames.createDaemonThread(1200, () -> {
               Minecraft mc = Minecraft.getMinecraft();
               EntityPlayerSP player = mc.player;
               mc.gameSettings.thirdPersonView = 1;
               manglelie.setTargetPosition(player.getPositionVector());
               galath.setTargetPosition(player.getPositionVector());
               manglelie.setYawRotation(player.rotationYaw + 180.0F);
               galath.setYawRotation(player.rotationYaw);
               manglelie.setCurrentAction(Action.THREESOME_SLOW);
               galath.setCurrentAction(Action.PUSSY_LICKING);
               manglelie.setInteractionPlayerUUID(player.getPersistentID());
               galath.setInteractionPlayerUUID(player.getPersistentID());
               manglelie.setAnchored(true);
               galath.setAnchored(true);
            });
         }
      }
   }

   /**
    * SERVER: the corruption trigger — a knocked-out (paralyzed) Galath being
    * right-clicked by a non-owner player starts {@link Action#CORRUPT_INTRO}
    * anchored and locks the player in.
    */
   static boolean processGirlInteract(GalathEntity galath, EntityPlayer player, EnumHand hand) {
      if (!(Boolean)galath.getDataManager().get(GalathEntity.bP)) {
         return galath.processInteractBase(player, hand);
      } else if (galath.getCurrentAction() != Action.KNOCK_OUT_GROUND) {
         return galath.processInteractBase(player, hand);
      } else if (galath.world.isRemote) {
         player.rotationYaw -= -128.0F;
         player.rotationPitch = 19.0F;
         return true;
      } else {
         galath.setCurrentAction(Action.CORRUPT_INTRO);
         galath.setInteractionPlayerUUID(player.getPersistentID());
         galath.setAnchored(true);
         galath.setTargetPosition(galath.getPositionVector());
         galath.setYawRotation(player.rotationYaw);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
         player.setPositionAndUpdate(galath.posX, galath.posY, galath.posZ);
         return true;
      }
   }
}
