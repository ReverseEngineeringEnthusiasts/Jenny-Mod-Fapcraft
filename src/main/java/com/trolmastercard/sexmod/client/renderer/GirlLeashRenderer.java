package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Leash rendering for girl entities (vanilla-style two brown strip passes with
 * a sagging curve, see {@link GirlRenderer#renderLeash}).
 * <p>
 * <b>Endpoints.</b> The holder end follows the leash holder's eye height and
 * look direction (hanging entities pin it straight down); the girl end is
 * offset sideways by her yaw and width so the leash leaves her hip rather than
 * her centre. Positions are partial-tick interpolated.
 * <p>
 * <b>GL state.</b> Disables texture/lighting/cull for the strips and restores
 * all three afterwards — the caller owns everything else.
 * <p>
 * CLIENT-side render thread only.
 */
@SideOnly(Side.CLIENT)
final class GirlLeashRenderer {
   private GirlLeashRenderer() {
   }

   /**
    * Vanilla-style leash render (two brown strip passes with a sagging curve)
    * between the girl and her leash holder.
    */
   static void render(BaseGirlEntity girl, double x, double y, double z, float partialTicks) {
      Entity holder = girl.getLeashHolder();
      y -= (1.6 - girl.height) * 0.5;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      double holderYaw = RotationHelper.lerp(holder.prevRotationYaw, holder.rotationYaw, partialTicks * 0.5F) * (float) (Math.PI / 180.0);
      double holderPitch = RotationHelper.lerp(holder.prevRotationPitch, holder.rotationPitch, partialTicks * 0.5F) * (float) (Math.PI / 180.0);
      double cosYaw = Math.cos(holderYaw);
      double sinYaw = Math.sin(holderYaw);
      double sinPitch = Math.sin(holderPitch);
      if (holder instanceof EntityHanging) {
         cosYaw = 0.0;
         sinYaw = 0.0;
         sinPitch = -1.0;
      }

      double cosPitch = Math.cos(holderPitch);
      double holderX = RotationHelper.lerpDouble(holder.prevPosX, holder.posX, partialTicks) - cosYaw * 0.7 - sinYaw * 0.5 * cosPitch;
      double holderY = RotationHelper.lerpDouble(holder.prevPosY + holder.getEyeHeight() * 0.7, holder.posY + holder.getEyeHeight() * 0.7, partialTicks)
         - sinPitch * 0.5
         - 0.25;
      double holderZ = RotationHelper.lerpDouble(holder.prevPosZ, holder.posZ, partialTicks) - sinYaw * 0.7 + cosYaw * 0.5 * cosPitch;
      double girlYaw = RotationHelper.lerp(girl.prevRenderYawOffset, girl.renderYawOffset, partialTicks) * (float) (Math.PI / 180.0) + (Math.PI / 2);
      cosYaw = Math.cos(girlYaw) * girl.width * 0.4;
      sinYaw = Math.sin(girlYaw) * girl.width * 0.4;
      double girlX = RotationHelper.lerpDouble(girl.prevPosX, girl.posX, partialTicks) + cosYaw;
      double girlY = RotationHelper.lerpDouble(girl.prevPosY, girl.posY, partialTicks);
      double girlZ = RotationHelper.lerpDouble(girl.prevPosZ, girl.posZ, partialTicks) + sinYaw;
      x += cosYaw;
      z += sinYaw;
      double deltaX = (float)(holderX - girlX);
      double deltaY = (float)(holderY - girlY);
      double deltaZ = (float)(holderZ - girlZ);
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for (int segment = 0; segment <= 24; segment++) {
         float r = 0.5F;
         float g = 0.4F;
         float b = 0.3F;
         if (segment % 2 == 0) {
            r = 0.35F;
            g = 0.28F;
            b = 0.21000001F;
         }

         float progress = segment / 24.0F;
         buffer.pos(
               x + deltaX * progress + 0.0, y + deltaY * (progress * progress + progress) * 0.5 + ((24.0F - segment) / 18.0F + 0.125F), z + deltaZ * progress
            )
            .color(r, g, b, 1.0F)
            .endVertex();
         buffer.pos(
               x + deltaX * progress + 0.025, y + deltaY * (progress * progress + progress) * 0.5 + ((24.0F - segment) / 18.0F + 0.125F) + 0.025, z + deltaZ * progress
            )
            .color(r, g, b, 1.0F)
            .endVertex();
      }

      tessellator.draw();
      buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for (int segment2 = 0; segment2 <= 24; segment2++) {
         float r2 = 0.5F;
         float g2 = 0.4F;
         float b2 = 0.3F;
         if (segment2 % 2 == 0) {
            r2 = 0.35F;
            g2 = 0.28F;
            b2 = 0.21000001F;
         }

         float progress2 = segment2 / 24.0F;
         buffer.pos(
               x + deltaX * progress2 + 0.0, y + deltaY * (progress2 * progress2 + progress2) * 0.5 + ((24.0F - segment2) / 18.0F + 0.125F) + 0.025, z + deltaZ * progress2
            )
            .color(r2, g2, b2, 1.0F)
            .endVertex();
         buffer.pos(
               x + deltaX * progress2 + 0.025, y + deltaY * (progress2 * progress2 + progress2) * 0.5 + ((24.0F - segment2) / 18.0F + 0.125F), z + deltaZ * progress2 + 0.025
            )
            .color(r2, g2, b2, 1.0F)
            .endVertex();
      }

      tessellator.draw();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.enableCull();
   }
}
