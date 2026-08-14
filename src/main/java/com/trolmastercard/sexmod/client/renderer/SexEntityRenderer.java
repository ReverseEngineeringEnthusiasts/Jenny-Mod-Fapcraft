package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.SexEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SexEntityRenderer extends Render<SexEntity> {
   static final double PARTICLE_OFFSET_B = 0.1896224320030116;
   static final double PARTICLE_OFFSET_D = -0.5;
   static final double PARTICLE_OFFSET_C = 0.08742380916962415;
   private static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation("textures/particle/particles.png");

   public SexEntityRenderer(RenderManager var1) {
      super(var1);
   }

   public void a(SexEntity var1, double var2, double var4, double var6, float var8, float var9) {
      LunaEntity var10 = var1.getOwnerLuna();
      if (var10 != null && !this.renderOutlines && var10.zFlag != 1.0F) {
         var10.av = var1;
         ItemStack var11 = (ItemStack)var10.getDataManager().get(LunaEntity.ag);
         if (!var11.getItem().equals(Items.AIR)) {
            float var12 = Minecraft.getDebugFPS();
            if (var12 == 0.0F) {
               var12 = 0.1F;
            }

            var10.zFlag += 60.0F / var12 * 0.01666F * 2.0F;
            var10.zFlag = Math.min(1.0F, var10.zFlag);
            EntityPlayerSP var13 = Minecraft.getMinecraft().player;
            Vec3d var14 = RotationHelper.lerpVec3dDouble(new Vec3d(var13.lastTickPosX, var13.lastTickPosY, var13.lastTickPosZ), var13.getPositionVector(), var9);
            Vec3d var15 = new Vec3d(var2, var4, var6);
            Vec3d var16 = RotationHelper.lerpVec3dDouble(
               new Vec3d(var10.lastTickPosX, var10.lastTickPosY + 0.875, var10.lastTickPosZ), var10.getPositionVector().add(0.0, 0.875, 0.0), var9
            );
            var16 = var16.subtract(var14);
            var15 = RotationHelper.lerpVec3dDouble(var15, var16, var10.zFlag);
            var2 = var15.x;
            var4 = var15.y;
            var6 = var15.z;
         } else {
            var10.zFlag = 0.0F;
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate((float)var2, (float)var4, (float)var6);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         this.bindEntityTexture(var1);
         Tessellator var45 = Tessellator.getInstance();
         BufferBuilder var46 = var45.getBuffer();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(var1));
         }

         if (!var11.getItem().equals(Items.AIR)) {
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.translate(0.0F, -0.2F, 0.0F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(var10, var11, TransformType.THIRD_PERSON_RIGHT_HAND);
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
         }

         this.bindEntityTexture(var1);
         var46.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         var46.pos(-0.5, -0.5, 0.0).tex(0.0625, 0.1875).normal(0.0F, 1.0F, 0.0F).endVertex();
         var46.pos(0.5, -0.5, 0.0).tex(0.125, 0.1875).normal(0.0F, 1.0F, 0.0F).endVertex();
         var46.pos(0.5, 0.5, 0.0).tex(0.125, 0.125).normal(0.0F, 1.0F, 0.0F).endVertex();
         var46.pos(-0.5, 0.5, 0.0).tex(0.0625, 0.125).normal(0.0F, 1.0F, 0.0F).endVertex();
         var45.draw();
         if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
         }

         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         int var47 = var10.getPrimaryHand() == EnumHandSide.RIGHT ? 1 : -1;
         ItemStack var49 = var10.getHeldItemMainhand();
         if (!(var49.getItem() instanceof ItemFishingRod)) {
            var47 = -var47;
         }

         var10.rotationYaw = var10.getYawRotation();
         var10.renderYawOffset = var10.getYawRotation();
         var10.posX = var10.getTargetPosition().x;
         var10.posY = var10.getTargetPosition().y;
         var10.posZ = var10.getTargetPosition().z;
         var10.prevPosX = var10.getTargetPosition().x;
         var10.prevPosY = var10.getTargetPosition().y;
         var10.prevPosZ = var10.getTargetPosition().z;
         float var51 = (var10.prevRenderYawOffset + (var10.renderYawOffset - var10.prevRenderYawOffset) * var9) * (float) (Math.PI / 180.0);
         double var17 = MathHelper.sin(var51);
         double var19 = MathHelper.cos(var51);
         double var21 = var47 * 0.35;
         double var23 = var10.prevPosX + (var10.posX - var10.prevPosX) * var9 - var19 * var21 - var17 * 0.8;
         double var25 = var10.prevPosY + var10.getEyeHeight() + (var10.posY - var10.prevPosY) * var9 - 0.45;
         double var27 = var10.prevPosZ + (var10.posZ - var10.prevPosZ) * var9 - var17 * var21 + var19 * 0.8;
         double var29 = var10.isSneaking() ? -0.1875 : 0.0;
         double var31 = var1.prevPosX
            + (var1.posX - var1.prevPosX) * var9
            - Math.sin((var10.getYawRotation() + 90.0F) * (Math.PI / 180.0)) * 0.1896224320030116
            - Math.sin(var10.getYawRotation().floatValue() * (Math.PI / 180.0)) * 0.08742380916962415;
         double var33 = var1.prevPosY + (var1.posY - var1.prevPosY) * var9 + 0.25 + -0.5;
         double var35 = var1.prevPosZ
            + (var1.posZ - var1.prevPosZ) * var9
            + Math.cos((var10.getYawRotation() + 90.0F) * (Math.PI / 180.0)) * 0.1896224320030116
            + Math.cos(var10.getYawRotation().floatValue() * (Math.PI / 180.0)) * 0.08742380916962415;
         double var37 = (float)(var23 - var31);
         double var39 = (float)(var25 - var33) + var29;
         double var41 = (float)(var27 - var35);
         GlStateManager.disableTexture2D();
         GlStateManager.disableLighting();
         if (var11.getItem().equals(Items.AIR)) {
            var46.begin(3, DefaultVertexFormats.POSITION_COLOR);

            for (int var43 = 0; var43 <= 16; var43++) {
               float var44 = var43 / 16.0F;
               var46.pos(var2 + var37 * var44, var4 + var39 * (var44 * var44 + var44) * 0.5 + 0.25, var6 + var41 * var44)
                  .color(0, 0, 0, 255)
                  .endVertex();
            }

            var45.draw();
         }

         GlStateManager.enableLighting();
         GlStateManager.enableTexture2D();
         super.doRender(var1, var2, var4, var6, var8, var9);
      }
   }

   @Nullable
   protected ResourceLocation getEntityTexture(SexEntity var0) {
      return PARTICLE_TEXTURE;
   }

}
