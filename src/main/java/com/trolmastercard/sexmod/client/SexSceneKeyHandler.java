package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.SceneDebug;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * CLIENT keybind handler for the "Leave sex scene" key (R-Shift,
 * {@code ClientProxy.keyBindings[2]}).
 * <p>
 * <b>Behavior (reworked):</b> instead of snapping the player out, the key
 * first tries to PROGRESS the scene to its natural ending:
 * <ol>
 *   <li>{@link BaseGirlEntity#triggerCumAction(UUID)} — jumps to the cum
 *       action if the current action has one; the cum animation plays, the
 *       meter fills, and the normal ending machinery
 *       ({@code xxx_cumDone} -&gt; {@code resetCameraAndPhysics} -&gt;
 *       single-arg {@code ResetGirlPacket}) releases the player cleanly.</li>
 *   <li>If the action did not change, walks the chain forward with
 *       {@link BaseGirlEntity#triggerFastSexAction(UUID)}
 *       ({@code getNextAction}).</li>
 *   <li>If the action still did not change (scene stuck at PAYMENT/NULL with
 *       nowhere to advance), falls back to the full client exit
 *       {@link BaseGirlEntity#resetCameraAndPhysics()} — which clears the
 *       scene camera, unlocks movement, un-hides the player and sends the
 *       full ResetGirlPacket. This guarantees the player is never left stuck
 *       in the scene view.</li>
 * </ol>
 * The player-form girl ({@link AbstractPlayerGirlEntity}) gets the same
 * treatment.
 * <p>
 * <b>History:</b> the progression behavior (49e6f87) was originally reverted
 * (9fd5d34) because scenes were permanently stuck at PAYMENT/NULL — that was
 * caused by the dismount-lerp bug ({@code RotationHelper.lerpVec3dDouble}
 * misuse) which made scenes unable to advance at all. With that fixed, the
 * progression path works and the reset is only the fallback.
 */
public class SexSceneKeyHandler {
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onKeyInput(KeyInputEvent event) {
      if (ClientProxy.keyBindings[2].isPressed()) {
         SceneDebug.log(SceneDebug.RSHIFT, "R-SHIFT pressed (Leave sex scene)");
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.player == null) {
            return;
         }

         UUID playerUuid = mc.player.getPersistentID();

         // "Leave sex scene": PROGRESS the scene to its ending when possible —
         // triggerCumAction jumps to the cum/ending animation (fills the meter,
         // plays the ending, then the normal scene-end machinery releases the
         // player through cumDone -> resetCameraAndPhysics). When the current
         // action has no cum, walk the action chain forward with
         // triggerFastSexAction. If neither advances (scene stuck at
         // PAYMENT/NULL), fall back to a full client-side exit
         // (resetCameraAndPhysics -> ResetGirlPacket) so the player is never
         // left stuck. The scene-progression path only works because the
         // dismount lerp is fixed; the old revert (9fd5d34) predates that fix.
         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (!girl.isDead && girl.world.isRemote && girl.getInteractionPlayerUUID() != null) {
                  UUID girlUuid = girl.getInteractionPlayerUUID();
                  if (playerUuid.equals(girlUuid) || mc.player.getUniqueID().equals(girlUuid)) {
                     Action before = girl.getCurrentAction();
                     BaseGirlEntity.triggerCumAction(playerUuid);
                     if (girl.getCurrentAction() == before) {
                        for (int i = 0; i < 32; i++) {
                           Action prev = girl.getCurrentAction();
                           BaseGirlEntity.triggerFastSexAction(playerUuid);
                           if (girl.getCurrentAction() == prev) {
                              break; // chain end reached
                           }
                        }
                     }

                     if (girl.getCurrentAction() == before) {
                        SceneDebug.log(SceneDebug.RSHIFT, "R-SHIFT: %s stuck at %s, full exit", girl.getDisplayNameText(), before);
                        girl.resetCameraAndPhysics();
                     } else {
                        SceneDebug.log(SceneDebug.RSHIFT, "R-SHIFT: %s progressed %s -> %s", girl.getDisplayNameText(), before, girl.getCurrentAction());
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException cme) {
         }

         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(playerUuid);
         if (playerGirl != null && playerGirl.getCurrentAction() != Action.NULL) {
            Action before = playerGirl.getCurrentAction();
            BaseGirlEntity.triggerCumAction(playerUuid);
            if (playerGirl.getCurrentAction() == before) {
               for (int i = 0; i < 32; i++) {
                  Action prev = playerGirl.getCurrentAction();
                  BaseGirlEntity.triggerFastSexAction(playerUuid);
                  if (playerGirl.getCurrentAction() == prev) {
                     break;
                  }
               }
            }

            if (playerGirl.getCurrentAction() == before) {
               playerGirl.resetCameraAndPhysics();
            }
         }
      }
   }

}
