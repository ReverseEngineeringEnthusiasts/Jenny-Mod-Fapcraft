package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
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

         // "Leave sex scene": send a full reset (single-arg ResetGirlPacket =
         // resetPose FALSE) for every girl this player is currently interacting
         // with. The server's ResetGirlPacket handler runs resetGirls(player)
         // (restores the player's physics) AND resetGirl(girl) (releases the
         // girl: un-anchors, clears the interaction partner, restores
         // gravity/noClip and re-adds her AI tasks). This matches the original
         // jar semantics and the original d99f7fb keybind; the scene progression
         // rework (49e6f87) and the wrong-UUID safety net (6e489ab) are what
         // left girls stuck mid-scene with nothing responding.
         try {
            for (BaseGirlEntity var5 : BaseGirlEntity.getGirlEntityList()) {
               if (!var5.isDead && var5.world.isRemote && var5.getInteractionPlayerUUID() != null) {
                  UUID var6 = var5.getInteractionPlayerUUID();
                  if (var3.equals(var6) || var2.player.getUniqueID().equals(var6)) {
                     PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(var5.getGirlId()));
                  }
               }
            }
         } catch (ConcurrentModificationException var7) {
         }

         AbstractPlayerGirlEntity var8 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var3);
         if (var8 != null && var8.getCurrentAction() != Action.NULL) {
            PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(var8.getGirlId()));
         }
      }
   }

}
