package com.trolmastercard.sexmod.entity;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> Scene entry and per-tick action handling for
 * {@link GoblinPlayerEntity}: the paizuri/nelson player placement the state
 * machine fires from {@code setCurrentAction}, and the client-side camera
 * switch plus the nelson pose-flag diff that runs every tick.
 * <p>
 * <b>Flow.</b> {@link #handlePlayerInteract} is the PAIZURI_START placement
 * (yaw + 180, player dropped in front, pitch 70); {@link #handlePlayerLook}
 * is the NELSON_INTRO placement (yaw match, player pulled one block back);
 * {@link #handleLocalAction} forces third person for the nelson loop while
 * the local player is the interaction target; {@link #handleNelsonAction}
 * clears the fast "c" pose whenever the action leaves NELSON_FAST.
 * <p>
 * <b>Pitfalls.</b> Both placement helpers run on the SERVER only
 * ({@code setCurrentAction} guards on {@code !world.isRemote}); they also
 * disable gravity/noClip on the player and the girl, which the scene teardown
 * is responsible for restoring.
 */
final class GoblinPlayerScene {
   private GoblinPlayerScene() {
   }

   static void handlePlayerLook(GoblinPlayerEntity goblin) {
      EntityPlayer player = goblin.world.getPlayerEntityByUUID(goblin.getInteractionPlayerUUID());
      if (player == null) {
         return;
      }

      goblin.setYawRotation(player.rotationYaw);
      goblin.noClip = true;
      goblin.setNoGravity(true);
      player.setNoGravity(true);
      player.noClip = true;
      player.setPositionAndUpdate(player.posX, player.posY, player.posZ - 1.0);
   }

   static void handlePlayerInteract(GoblinPlayerEntity goblin) {
      EntityPlayer player = goblin.world.getPlayerEntityByUUID(goblin.getInteractionPlayerUUID());
      if (player == null) {
         return;
      }

      goblin.setYawRotation(player.rotationYaw + 180.0F);
      goblin.noClip = true;
      goblin.setNoGravity(true);
      player.setNoGravity(true);
      player.noClip = true;
      player.setPositionAndUpdate(player.posX, player.posY - 0.5, player.posZ - 0.6F);
      player.rotationPitch = 70.0F;
      player.prevRotationPitch = 70.0F;
   }

   /**
    * CLIENT: forces third person for the nelson loop when the local player is
    * the interaction target and is still in first person.
    */
   @SideOnly(Side.CLIENT)
   static void handleLocalAction(GoblinPlayerEntity goblin, Action action) {
      Minecraft mc = Minecraft.getMinecraft();
      UUID interactionUuid = goblin.getInteractionPlayerUUID();
      if (!mc.player.getPersistentID().equals(interactionUuid)) {
         return;
      }

      if (mc.gameSettings.thirdPersonView != 0) {
         return;
      }

      switch (action) {
         case NELSON_CUM:
         case NELSON_FAST:
         case NELSON_INTRO:
         case NELSON_SLOW:
            mc.gameSettings.thirdPersonView = 2;
      }
   }

   @SideOnly(Side.CLIENT)
   static void handleNelsonAction(GoblinPlayerEntity goblin, Action action) {
      if (action == Action.NELSON_FAST && goblin.getPreviousAction() != Action.NELSON_FAST) {
         goblin.aF = false;
      }
   }
}
