package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Player-facing interactions for {@link GoblinEntity}: the GUI actions
 * ({@code take ur stuff back}, {@code use her}, nelson pickup), the
 * click-to-pickup handler, the scene-entry/teardown hooks the state machine
 * fires ({@link #handlePlayerInteract}, {@link #handlePlayerLook},
 * {@link #returnStolenItem}, {@link #endPaizuriScene}) and the client-only
 * scene timers that launch paizuri/nelson ({@link #tickPaizuriStartTimer},
 * {@link #tickNelsonStartTimer}).
 * <p>
 * Also holds {@link #hasGoblinWithUUID} (one carried goblin per player) and
 * the shoulder gesture-pad opener shared by the G and RShift keybinds.
 */
final class GoblinInteractions {
   private GoblinInteractions() {
   }

   /**
    * CLIENT: action dispatch from the goblin's GUI — {@code take ur stuff
    * back} starts the throw, {@code use her} binds the acting player as the
    * throw/pickup target.
    */
   static void doAction(GoblinEntity goblin, String action, UUID uuid) {
      if ("take ur stuff back".equals(action)) {
         goblin.setCurrentAction(Action.START_THROWING);
      }

      if ("use her".equals(action)) {
         goblin.setThrowTarget(uuid);
      }
   }

   /**
    * CLIENT: arms the "use her" timer ({@code paizuriStartTicks} = 0) — after 15
    * ticks the paizuri scene starts (see {@link #tickPaizuriStartTimer()}).
    */
   static void setThrowTarget(GoblinEntity goblin, UUID uuid) {
      goblin.paizuriStartTicks = 0;
      BeeScreen.enableInteraction();
      HandlePlayerMovement.setMovementLock(false);
      goblin.setInteractionPlayerUUID(uuid);
   }

   /**
    * CLIENT: arms the nelson "pickup" timer ({@code nelsonStartTicks} = 0) — after
    * 15 ticks the nelson scene starts (see {@link #tickNelsonStartTimer()}).
    */
   static void setPickupTarget(GoblinEntity goblin, UUID uuid) {
      goblin.nelsonStartTicks = 0;
      BeeScreen.enableInteraction();
      HandlePlayerMovement.setMovementLock(false);
      goblin.setInteractionPlayerUUID(uuid);
   }

   /**
    * CLIENT: opens the goblin throw/pickup gesture pad ({@code GalathScreen})
    * when the local player owns a goblin that is riding their shoulder
    * (SHOULDER_IDLE). Returns true if a pad was opened. Used by both the G
    * keybind and the RShift (Leave-sex-scene) dual keybind.
    */
   @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
   static boolean openGoblinPadForPlayer(net.minecraft.client.Minecraft mc, UUID playerUuid) {
      if (mc == null || playerUuid == null) return false;
      if (mc.currentScreen instanceof com.trolmastercard.sexmod.client.gui.GalathScreen) return false;
      BaseGirlEntity padGirl = null;
      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl.world.isRemote && girl instanceof IGoblin) {
               IGoblin goblin = (IGoblin)girl;
               if (playerUuid.equals(goblin.getOwnerUUID())) {
                  padGirl = girl;
                  break;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         return false;
      }
      if (padGirl != null && padGirl.getCurrentAction() == Action.SHOULDER_IDLE) {
         mc.displayGuiScreen(new com.trolmastercard.sexmod.client.gui.GalathScreen(padGirl));
         return true;
      }
      return false;
   }

   static boolean processInteract(GoblinEntity goblin, EntityPlayer player, EnumHand hand) {
      if (goblin.world.isRemote) {
         return true;
      }

      if (goblin.isQueen) {
         return true;
      }

      if (goblin.getCurrentAction() == Action.RUN) {
         if (goblin.getDistance(player) > 3.5) {
            player.sendStatusMessage(new TextComponentString("get a bit closer..."), true);
         } else {
            goblin.setTargetPosition(player.getPositionVector());
            goblin.setYawRotation(player.rotationYaw);
            goblin.setCurrentAction(Action.CATCH);
            goblin.entityDataManager.set(BaseGirlEntity.GIRL_HAND_STATES, "bj");
            goblin.setOwnerUUID(player.getPersistentID());
            goblin.setInteractionPlayerUUID(player.getPersistentID());
            goblin.getNavigator().clearPath();
            goblin.motionX = 0.0;
            goblin.motionY = 0.0;
            goblin.motionZ = 0.0;
         }

         return true;
      } else {
         if (hasGoblinWithUUID(player.getPersistentID())) {
            player.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
         } else {
            goblin.setOwnerUUID(player.getPersistentID());
            goblin.setCurrentAction(Action.PICK_UP);
            goblin.heldPlayerDistance = 45;
            goblin.setAnchored(false);
            goblin.entityDataManager.set(GoblinEntity.TAMED, true);
            goblin.getNavigator().clearPath();
         }

         return true;
      }
   }

   static boolean hasGoblinWithUUID(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl instanceof IGoblin && !girl.world.isRemote && !girl.isDead) {
               UUID candidateUuid = ((IGoblin)girl).getOwnerUUID();
               // WILD/unowned goblins have a null owner; the QUERY uuid is the
               // receiver so a null candidate just fails the match (no NPE).
               // (Original bytecode: var0.equals(var3) — query first. The rename
               // swapped the operands, NPEing on every unowned goblin.)
               if (uuid.equals(candidateUuid)) {
                  return true;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         // ignored: entity list mutated during iteration; retried on the next tick
      }

      return false;
   }

   /**
    * SERVER: paizuri scene teardown — resets the bound player, unbinds,
    * un-anchors, restores physics, drops the stolen item and (untamed) sends
    * her home and despawns her.
    */
   static void endPaizuriScene(GoblinEntity goblin) {
      EntityPlayer player = goblin.world.getPlayerEntityByUUID(goblin.getInteractionPlayerUUID());
      if (player != null) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)player);
      }

      goblin.setInteractionPlayerUUID(null);
      goblin.setAnchored(false);
      goblin.noClip = false;
      goblin.setNoGravity(false);
      goblin.entityDataManager.set(GoblinEntity.STOLEN_ITEM, ItemStack.EMPTY);
      if (!(Boolean)goblin.entityDataManager.get(GoblinEntity.TAMED)) {
         goblin.setPositionAndUpdate(goblin.homePos.x, goblin.homePos.y, goblin.homePos.z);
         goblin.world.removeEntity(goblin);
      }
   }

   static void handlePlayerLook(GoblinEntity goblin) {
      EntityPlayer player = goblin.world.getPlayerEntityByUUID(goblin.getInteractionPlayerUUID());
      if (player != null) {
         goblin.setOwnerUUID(null);
         goblin.setTargetPosition(player.getPositionVector());
         goblin.setYawRotation(player.rotationYaw);
         goblin.setAnchored(true);
         goblin.noClip = true;
         goblin.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         goblin.setInteractionPlayerUUID(player.getPersistentID());
      }
   }

   static void handlePlayerInteract(GoblinEntity goblin) {
      EntityPlayer player = goblin.world.getPlayerEntityByUUID(goblin.getInteractionPlayerUUID());
      if (player != null) {
         goblin.setOwnerUUID(null);
         goblin.setTargetPosition(player.getPositionVector());
         goblin.setYawRotation(player.rotationYaw + 180.0F);
         goblin.setAnchored(true);
         goblin.noClip = true;
         goblin.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         goblin.setInteractionPlayerUUID(player.getPersistentID());
         player.setPositionAndUpdate(player.posX, player.posY - 0.5, player.posZ);
         player.rotationPitch = 70.0F;
         player.prevRotationPitch = 70.0F;
      }
   }

   /**
    * SERVER: returns the stolen item stack to the interaction player's
    * inventory and clears the stolen-item key.
    */
   static void returnStolenItem(GoblinEntity goblin) {
      ItemStack heldItem = (ItemStack)goblin.entityDataManager.get(GoblinEntity.STOLEN_ITEM);
      if (heldItem != ItemStack.EMPTY) {
         EntityPlayer player = goblin.world.getPlayerEntityByUUID(goblin.getInteractionPlayerUUID());
         if (player != null) {
            player.inventory.addItemStackToInventory(heldItem.copy());
            goblin.entityDataManager.set(GoblinEntity.STOLEN_ITEM, ItemStack.EMPTY);
         }
      }
   }

   static void tickPaizuriStartTimer(GoblinEntity goblin) {
      if (goblin.paizuriStartTicks != -1) {
         if (++goblin.paizuriStartTicks == 15) {
            goblin.paizuriStartTicks = -1;
            goblin.setCurrentAction(Action.PAIZURI_START);
            Minecraft.getMinecraft().player.closeScreen();
         }
      }
   }

   static void tickNelsonStartTimer(GoblinEntity goblin) {
      if (goblin.nelsonStartTicks != -1) {
         if (++goblin.nelsonStartTicks == 15) {
            goblin.nelsonStartTicks = -1;
            goblin.setCurrentAction(Action.NELSON_INTRO);
            Minecraft mc = Minecraft.getMinecraft();
            mc.player.closeScreen();
            mc.gameSettings.thirdPersonView = 2;
         }
      }
   }
}
