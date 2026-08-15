package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.SceneDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * CLIENT-side player input interceptor for scenes (horny-potion player-girls
 * and NPC scenes alike).
 * <p>
 * <b>Movement lock.</b> {@link #setMovementLock(boolean)} toggles the mod's
 * input lock (static {@code isSneaking} flag, confusingly named). While
 * unlocked (in a scene), {@code onInputUpdate} zeroes all movement and maps
 * the keys to scene controls:
 * <ul>
 *   <li><b>sneak (shift)</b> — {@code triggerFastSexAction}: advance the
 *       action chain one step ({@code getNextAction}); also exposes
 *       {@link #isJumping} (yes, it mirrors sneak).</li>
 *   <li><b>jump (space)</b> — {@code resetPlayerGirlCamera} (player-girls), or
 *       {@code triggerCumAction} when the cum meter
 *       ({@link HornyMeterHud#meterValue}) is full — the standard scene
 *       ending input. Exposed as {@link #isInAction}.</li>
 * </ul>
 * While locked, the mouse is captured ({@link #onMouse}) so the player cannot
 * click away from the scene.
 * <p>
 * <b>Pitfall:</b> the lock must be released (true) at scene end — the natural
 * end does this via {@code resetCameraAndPhysics} ->
 * {@code resetLocalPlayerClientState}; the R-Shift keybind
 * ({@code SexSceneKeyHandler}) does it through the same client exit path when
 * the scene cannot progress.
 */
public class HandlePlayerMovement {
   /** True while the mod's input lock is engaged (idle); false during scenes. */
   private static boolean isSneaking = true;

   /** Mirrors the sneak key while in a scene (used by action "Done" checks). */
   public static boolean isJumping = false;

   /** Mirrors the jump key while in a scene (cum-meter expansion check). */
   public static boolean isInAction = false;

   /** The latest MovementInput from {@link #onInputUpdate}. */
   public static MovementInput input;

   /**
    * CLIENT input hook: captures movement while a scene is active and maps
    * sneak/jump to the scene controls (see class doc).
    */
   @SubscribeEvent
   public void onInputUpdate(InputUpdateEvent var1) {
      input = var1.getMovementInput();
      isJumping = input.sneak;
      isInAction = input.jump;
      if (!isSneaking) {
         if (input.jump) {
            AbstractPlayerGirlEntity.resetPlayerGirlCamera();
         }

         if (input.sneak) {
            BaseGirlEntity.triggerFastSexAction(Minecraft.getMinecraft().player.getPersistentID());
         }

         if (input.jump && HornyMeterHud.meterValue >= 1.0) {
            BaseGirlEntity.triggerCumAction(Minecraft.getMinecraft().player.getPersistentID());
         }

         input.backKeyDown = false;
         input.forwardKeyDown = false;
         input.leftKeyDown = false;
         input.rightKeyDown = false;
         input.sneak = false;
         input.jump = false;
         input.moveForward = 0.0F;
         input.moveStrafe = 0.0F;
         Minecraft.getMinecraft().player.setVelocity(0.0, 0.0, 0.0);
      }
   }

   public static boolean isSneakingState() {
      return isSneaking;
   }

   public static void setMovementLock(boolean var0) {
      SceneDebug.log(SceneDebug.MOVEMENT, "setMovementLock(%s) (was %s)", var0, isSneaking);
      isSneaking = var0;
      if (!var0) {
         handlePlayerMovementTick();
      }
   }

   @SideOnly(Side.CLIENT)
   static void handlePlayerMovementTick() {
      EntityPlayerSP var0 = Minecraft.getMinecraft().player;
      if (AbstractPlayerGirlEntity.isOwnerPlayer(var0)) {
         var0.sendStatusMessage(new TextComponentString("Jump to get out of the animation"), true);
      }
   }

   @SubscribeEvent
   public void onMouse(MouseEvent var1) {
      if (!isSneaking && var1.isButtonstate()) {
         var1.setCanceled(true);
      }
   }

}
