package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.entity.fp;







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

public class am {
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
               GoblinRenderer.N = (float)(var2.player.movementInput.moveStrafe * GoblinRenderer.G.x);
               GoblinRenderer.N = GoblinRenderer.N + -(var10 - GoblinRenderer.H) * 3.0F;
               GoblinRenderer.N = RotationHelper.lerp(GoblinRenderer.I, GoblinRenderer.N, 0.1F);
               float var11 = -var2.player.rotationPitch;
               GoblinRenderer.x = (float)(
                  var2.player.movementInput.moveForward * GoblinRenderer.G.z
                     + (float)var2.player.motionY * GoblinRenderer.G.y
               );
               GoblinRenderer.x = GoblinRenderer.x + -(var11 - GoblinRenderer.t) * 3.0F;
               GoblinRenderer.x = RotationHelper.lerp(GoblinRenderer.E, GoblinRenderer.x, 0.1F);
               GoblinRenderer.a_clash398(var4, var1.getPartialTicks());
               GoblinRenderer.H = var10;
               GoblinRenderer.I = GoblinRenderer.N;
               GoblinRenderer.t = var11;
               GoblinRenderer.E = GoblinRenderer.x;
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
                  if (var5.getCurrentAction() == fp.START_THROWING) {
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
               fp var6 = var5.getCurrentAction();
               if (var6 == fp.PICK_UP || var6 == fp.START_THROWING) {
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
               fp var6 = var4.getCurrentAction();
               if ((var6 == fp.PICK_UP || var6 == fp.START_THROWING) && var2.equals(var5.getOwnerUUID())) {
                  var1.setCanceled(true);
                  break;
               }
            }
         }
      } catch (ConcurrentModificationException var7) {
      }
   }

}
