package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SexSceneKeyHandler {
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onKeyInput(KeyInputEvent var1) {
      if (ClientProxy.keyBindings[2].isPressed()) {
         Minecraft var2 = Minecraft.getMinecraft();
         if (var2.player == null) {
            return;
         }

         UUID var3 = var2.player.getPersistentID();

         // "Leave sex scene" should PROGRESS the scene to its natural end, not
         // snap the player out. The original mod has no exit key: scenes end when
         // the action chain completes. triggerCumAction jumps to the ending (cum)
         // animation if the current action has one; triggerFastSexAction advances
         // the chain one step (getNextAction). When no cum action exists we walk
         // the chain to its end, after which the girl releases the player through
         // the normal machinery. Never resetCameraAndPhysics here: on an anchored
         // girl the server refuses the NULL action ("prevented a potential
         // animation break"), which left girls stuck standing with the player
         // already snapped out.
         try {
            for (BaseGirlEntity var5 : BaseGirlEntity.getGirlEntityList()) {
               if (!var5.isDead && var5.world.isRemote && var5.getInteractionPlayerUUID() != null) {
                  UUID var6 = var5.getInteractionPlayerUUID();
                  if (var3.equals(var6) || var2.player.getUniqueID().equals(var6)) {
                     Action before = var5.getCurrentAction();
                     BaseGirlEntity.triggerCumAction(var3);
                     // if the cum action did not apply (no cum for this action),
                     // walk the chain to its end
                     if (var5.getCurrentAction() == before) {
                        for (int i = 0; i < 32; i++) {
                           Action prev = var5.getCurrentAction();
                           BaseGirlEntity.triggerFastSexAction(var3);
                           if (var5.getCurrentAction() == prev) {
                              break; // chain end reached
                           }
                        }
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException var7) {
         }

         AbstractPlayerGirlEntity var8 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var3);
         if (var8 != null && var8.getCurrentAction() != Action.NULL) {
            Action before = var8.getCurrentAction();
            BaseGirlEntity.triggerCumAction(var3);
            if (var8.getCurrentAction() == before) {
               for (int i = 0; i < 32; i++) {
                  Action prev = var8.getCurrentAction();
                  BaseGirlEntity.triggerFastSexAction(var3);
                  if (var8.getCurrentAction() == prev) {
                     break;
                  }
               }
            }
         }
      }
   }

}
