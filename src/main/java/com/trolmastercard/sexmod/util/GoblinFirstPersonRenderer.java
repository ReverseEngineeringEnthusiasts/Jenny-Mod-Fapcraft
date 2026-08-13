package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.entity.Action;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GoblinFirstPersonRenderer {
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderWorldLastEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.gameSettings.thirdPersonView == 0) {
         UUID var3 = var2.player.getPersistentID();
         BaseGirlEntity var4 = null;

         try {
            for (BaseGirlEntity var6 : BaseGirlEntity.getGirlEntityList()) {
               if (var6 != null && !var6.isDead && var6.world.isRemote && var6 instanceof IGoblin) {
                  IGoblin var7 = (IGoblin)var6;
                  if (var3.equals(var7.getOwnerUUID())) {
                     var4 = var6;
                     break;
                  }
               }
            }
         } catch (ConcurrentModificationException var8) {
         }

         if (var4 != null) {
            Render var9 = var2.getRenderManager().getEntityRenderObject(var4);
            if (var9 != null) {
               float var10 = var2.player.rotationYaw;
               GoblinRenderer.strafeRotation = (float)(var2.player.movementInput.moveStrafe * GoblinRenderer.MOVEMENT_DIR_VECTOR.x);
               GoblinRenderer.strafeRotation = GoblinRenderer.strafeRotation + -(var10 - GoblinRenderer.lastPlayerYaw) * 3.0F;
               GoblinRenderer.strafeRotation = RotationHelper.lerp(GoblinRenderer.prevStrafeRotation, GoblinRenderer.strafeRotation, 0.1F);
               float var11 = -var2.player.rotationPitch;
               GoblinRenderer.forwardRotation = (float)(
                  var2.player.movementInput.moveForward * GoblinRenderer.MOVEMENT_DIR_VECTOR.z
                     + (float)var2.player.motionY * GoblinRenderer.MOVEMENT_DIR_VECTOR.y
               );
               GoblinRenderer.forwardRotation = GoblinRenderer.forwardRotation + -(var11 - GoblinRenderer.lastPlayerPitch) * 3.0F;
               GoblinRenderer.forwardRotation = RotationHelper.lerp(GoblinRenderer.prevForwardRotation, GoblinRenderer.forwardRotation, 0.1F);
               GoblinRenderer.renderEntityInFirstPerson(var4, var1.getPartialTicks());
               GoblinRenderer.lastPlayerYaw = var10;
               GoblinRenderer.prevStrafeRotation = GoblinRenderer.strafeRotation;
               GoblinRenderer.lastPlayerPitch = var11;
               GoblinRenderer.prevForwardRotation = GoblinRenderer.forwardRotation;
               GlStateManager.enableLighting();
               GlStateManager.enableDepth();
               GlStateManager.enableAlpha();
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void b(RenderWorldLastEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player != null) {
         UUID var3 = var2.player.getPersistentID();

         try {
            for (BaseGirlEntity var5 : BaseGirlEntity.getGirlEntityList()) {
               if (var5.world.isRemote && !var5.isDead && var5 instanceof IGoblin) {
                  IGoblin var6 = (IGoblin)var5;
                  if (var5.getCurrentAction() == Action.START_THROWING) {
                     var5.setLocallyRegistered(true);
                     var2.getRenderManager().renderEntity(var5, 0.0, 0.0, 0.0, var3.equals(var6.getOwnerUUID()) ? -420.69F : 0.0F, var2.getRenderPartialTicks(), false);
                     var5.setLocallyRegistered(false);
                     return;
                  }
               }
            }
         } catch (ConcurrentModificationException var7) {
         }

         GlStateManager.enableLighting();
         GlStateManager.enableDepth();
         GlStateManager.enableAlpha();
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderHandEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      UUID var3 = var2.player.getPersistentID();

      try {
         for (BaseGirlEntity var5 : BaseGirlEntity.getGirlEntityList()) {
            if (var5 instanceof IGoblin) {
               Action var6 = var5.getCurrentAction();
               if (var6 == Action.PICK_UP || var6 == Action.START_THROWING) {
                  IGoblin var7 = (IGoblin)var5;
                  UUID var8 = var7.getOwnerUUID();
                  if (var3.equals(var8)) {
                     var1.setCanceled(true);
                     break;
                  }
               }
            }
         }
      } catch (ConcurrentModificationException var9) {
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(Pre var1) {
      UUID var2 = var1.getEntityPlayer().getPersistentID();

      try {
         for (BaseGirlEntity var4 : BaseGirlEntity.getGirlEntityList()) {
            if (var4 instanceof IGoblin) {
               IGoblin var5 = (IGoblin)var4;
               Action var6 = var4.getCurrentAction();
               if ((var6 == Action.PICK_UP || var6 == Action.START_THROWING) && var2.equals(var5.getOwnerUUID())) {
                  var1.setCanceled(true);
                  break;
               }
            }
         }
      } catch (ConcurrentModificationException var7) {
      }
   }

}
