package com.trolmastercard.sexmod;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;







import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PositionData {
   Vec3d b = null;
   Vec3d a = null;

   @SubscribeEvent
   public void a(Pre var1) {
      try {
         for (BaseGirlEntity var3 : BaseGirlEntity.getGirlEntityList()) {
            if (!var3.isDead && var3.getInteractionPlayerUUID() != null && var3.getCurrentAction() != fp.NULL) {
               EntityPlayer var4 = var1.getEntityPlayer();
               if (var3.getCurrentAction().hasPlayer && (var3.getInteractionPlayerUUID().equals(var4.getPersistentID()) || var3.getInteractionPlayerUUID().equals(var4.getUniqueID()))) {
                  var1.setCanceled(true);
                  return;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   @SubscribeEvent
   public void a(RenderHandEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      EntityPlayerSP var3 = var2.player;
      AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.g(var3);
      if (var4 != null && var4.isAnchored()) {
         var1.setCanceled(true);
      } else {
         try {
            for (BaseGirlEntity var6 : BaseGirlEntity.getGirlEntityList()) {
               UUID var7 = var6.getInteractionPlayerUUID();
               fp var8 = var6.getCurrentAction();
               if (!var6.isDead
                  && var7 != null
                  && var8 != null
                  && var8.hasPlayer
                  && (var7.equals(var3.getUniqueID()) || var7.equals(var3.getPersistentID()))) {
                  var1.setCanceled(true);
                  return;
               }
            }
         } catch (ConcurrentModificationException var9) {
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player != null) {
         if (var1.phase == Phase.END) {
            if (this.b != null) {
               var2.player.setPosition(this.b.x, this.b.y, this.b.z);
               var2.player.lastTickPosX = this.a.x;
               var2.player.lastTickPosY = this.a.y;
               var2.player.lastTickPosZ = this.a.z;
               this.b = null;
               this.a = null;
            }
         } else if (var2.gameSettings.thirdPersonView == 0) {
            BaseGirlEntity var3 = BaseGirlEntity.a(var2.player.getPersistentID(), Boolean.valueOf(false));
            if (var3 != null) {
               if (var3.getCurrentAction().useBoyCam) {
                  if (!var3.m_clash494()) {
                     this.b = var2.player.getPositionVector();
                     this.a = new Vec3d(var2.player.lastTickPosX, var2.player.lastTickPosY, var2.player.lastTickPosZ);
                     Vec3d var4 = var3.isAnchored()
                        ? var3.getCachedBoneOffset("boyCam").add(var3.getTargetPosition())
                        : var3.getCachedBoneOffset("boyCam")
                           .add(
                              RotationHelper.a(new Vec3d(var3.lastTickPosX, var3.lastTickPosY, var3.lastTickPosZ), var3.getPositionVector(), var1.renderTickTime)
                           );
                     var2.player.posX = var4.x;
                     var2.player.posY = var4.y - var2.player.getEyeHeight();
                     var2.player.posZ = var4.z;
                     var2.player.lastTickPosX = var4.x;
                     var2.player.lastTickPosY = var4.y - var2.player.getEyeHeight();
                     var2.player.lastTickPosZ = var4.z;
                  }
               }
            }
         }
      }
   }

}
