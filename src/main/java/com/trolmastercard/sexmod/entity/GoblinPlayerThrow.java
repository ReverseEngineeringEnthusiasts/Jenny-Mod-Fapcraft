package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> Carry and throw glue for {@link GoblinPlayerEntity}: the
 * sneak+click pickup trigger, the head glue while carried, the throw flight
 * countdown, the thrown/stand-up recovery and the client-side owner revival.
 * Every method here is the bookkeeping half of the overrides that stay on the
 * entity ({@code onUpdate}, {@code updateAITasks}).
 * <p>
 * <b>Flow.</b> {@link #handlePlayerThrow} binds the carrier and starts
 * PICK_UP with the 45-tick hold countdown; {@link #updatePlayerThrowProgress}
 * launches the carrying player at tick 15 and unbinds owner/interaction at
 * tick 39 (THROWN); {@link #handlePlayerThrown} counts 30 grounded ticks into
 * STAND_UP; {@link #handlePlayerStandUp} runs STAND_UP for 37 ticks back to
 * NULL. {@link #handleThrowAction} is the glue that keeps the transformed
 * player 2 blocks above the carrier until the throw starts.
 */
final class GoblinPlayerThrow {
   private GoblinPlayerThrow() {
   }

   /**
    * SERVER: the pickup trigger (sneak+click by a non-transformed player on
    * the carried player's entity) — binds the carrier and starts
    * {@link Action#PICK_UP} with the 45-tick hold countdown, locking the
    * carrying player's physics.
    */
   static void handlePlayerThrow(GoblinPlayerEntity goblin, EntityPlayer player) {
      if (goblin.getCurrentAction() != Action.NULL || goblin.getOwnerUUID() != null) {
         return;
      }

      if (GoblinEntity.hasGoblinWithUUID(player.getPersistentID())) {
         player.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
         return;
      }

      goblin.setOwnerUUID(player.getPersistentID());
      goblin.setCurrentAction(Action.PICK_UP);
      goblin.setHeldPlayerDistance(45);
      EntityPlayer ownerPlayer = goblin.getOwnerPlayer();
      if (ownerPlayer == null) {
         return;
      }

      ownerPlayer.setNoGravity(true);
      ownerPlayer.noClip = true;
      if (!goblin.world.isRemote) {
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)ownerPlayer);
      }
   }

   /**
    * SERVER (mirrored on CLIENT): while carried (owner set) and not yet
    * thrown, keeps the transformed player glued 2 blocks above the carrier.
    */
   static void handleThrowAction(GoblinPlayerEntity goblin) {
      Action action = goblin.getCurrentAction();
      if (action == Action.THROWN) {
         return;
      }

      if (action == Action.START_THROWING && goblin.getThrowProgress() > 15) {
         return;
      }

      UUID uuid = goblin.getOwnerUUID();
      if (uuid == null) {
         return;
      }

      EntityPlayer owner = goblin.world.getPlayerEntityByUUID(uuid);
      if (owner == null) {
         return;
      }

      EntityPlayer player = goblin.getOwnerPlayer();
      if (player == null) {
         return;
      }

      player.noClip = true;
      player.setNoGravity(true);
      player.setPosition(owner.posX, owner.posY + 2.0, owner.posZ);
   }

   /**
    * SERVER (mirrored on CLIENT): throw flight for the transformed player —
    * at tick 15 the carrying player is launched with the carrier's aim, at
    * tick 39 the throw ends (THROWN) and the owner/interaction bindings
    * clear.
    */
   static void updatePlayerThrowProgress(GoblinPlayerEntity goblin) {
      int progress = goblin.getThrowProgress();
      if (progress == -1) {
         return;
      }

      goblin.setThrowProgress(++progress);
      EntityPlayer player = goblin.getOwnerPlayer();
      if (player == null) {
         return;
      }

      if (progress == 15) {
         GoblinEntity.getGoblinThrowPos(goblin);
         float pitch = GoblinEntity.getGoblinThrowHeight(goblin);
         float yaw = GoblinEntity.getGoblinThrowDistance(goblin);
         if (goblin.world.isRemote && goblin.hasOwnerUUID()) {
            HandlePlayerMovement.setMovementLock(true);
         }

         Vec3d vec = GoblinEntity.rotateVectorPitchYaw(new Vec3d(0.0, 0.0, 1.5), pitch, yaw);
         player.motionX = vec.x;
         player.motionY = vec.y;
         player.motionZ = vec.z;
         if (!goblin.world.isRemote) {
            goblin.setYawRotation(yaw);
         }
      }

      player.noClip = false;
      player.setNoGravity(false);
      if (progress == 39) {
         goblin.setThrowProgress(-1);
         goblin.setCurrentAction(Action.THROWN);
         goblin.setInteractionPlayerUUID(null);
         goblin.setOwnerUUID(null);
      }
   }

   static void handlePlayerStandUp(GoblinPlayerEntity goblin) {
      if (goblin.getCurrentAction() != Action.STAND_UP) {
         return;
      }

      if (++goblin.standUpTicks >= 37) {
         goblin.standUpTicks = 0;
         goblin.setCurrentAction(Action.NULL);
      }
   }

   static void handlePlayerThrown(GoblinPlayerEntity goblin) {
      if (goblin.getCurrentAction() != Action.THROWN) {
         return;
      }

      EntityPlayer player = goblin.getOwnerPlayer();
      if (player == null || !player.onGround) {
         return;
      }

      int throwTick = goblin.getThrowTickCount() + 1;
      goblin.setThrowTickCount(throwTick);
      if (throwTick >= 30) {
         goblin.setThrowTickCount(0);
         goblin.setCurrentAction(Action.STAND_UP);
      }
   }

   /**
    * CLIENT: revives the carrying player's entity while the throw is being
    * wound up, re-spawning it if the client already dropped it.
    */
   @SideOnly(Side.CLIENT)
   static void handleOwnerThrow(GoblinPlayerEntity goblin) {
      EntityPlayer player = goblin.getOwnerPlayer();
      if (player == null || goblin.getCurrentAction() != Action.START_THROWING) {
         return;
      }

      player.isDead = false;
      if (!goblin.world.loadedEntityList.contains(player)) {
         goblin.world.spawnEntity(player);
      }
   }
}
