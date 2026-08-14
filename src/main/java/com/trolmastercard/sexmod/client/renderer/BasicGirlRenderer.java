package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.BasicGirlEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class BasicGirlRenderer extends Render<BasicGirlEntity> {

   @Override
   protected ResourceLocation getEntityTexture(BasicGirlEntity var1) {
      return this.IDLE_TEXTURE;
   }

   static final ResourceLocation IDLE_TEXTURE = new ResourceLocation("sexmod", "textures/entity/pyrocinical/standing.png");
   static final ResourceLocation PRACTICE_TEXTURE = new ResourceLocation("sexmod", "textures/entity/pyrocinical/praising.png");
   static final ResourceLocation WALK_TEXTURE_1 = new ResourceLocation("sexmod", "textures/entity/pyrocinical/walking1.png");
   static final ResourceLocation WALK_TEXTURE_2 = new ResourceLocation("sexmod", "textures/entity/pyrocinical/walking2.png");
   static final String FAT_TEXTURE_PATH = "textures/entity/pyrocinical/fat/";
   static final int FAT_MAX_TICKS = 30;
   static final float SCALE_1_4 = 1.4F;
   static final float SCALE_0_75 = 0.75F;
   Minecraft mc;
   ResourceLocation cachedTexture = null;
   long lastTextureSwitchTime = 0L;

   public BasicGirlRenderer(RenderManager var1) {
      super(var1);
      this.mc = Minecraft.getMinecraft();
   }

   @Nullable
   protected ResourceLocation a(BasicGirlEntity var1) {
      return null;
   }

   public void a(BasicGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      GL11.glDisable(2896);
      GlStateManager.enableAlpha();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      EntityPlayerSP var10 = this.mc.player;
      Vec3d var11 = RotationHelper.lerpVec3dDouble(new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ), var1.getPositionVector(), var9);
      Vec3d var12 = RotationHelper.lerpVec3dDouble(new Vec3d(var10.lastTickPosX, var10.lastTickPosY, var10.lastTickPosZ), var10.getPositionVector(), var9);
      Vec3d var13 = var11.subtract(var12);
      ResourceLocation var14 = this.getFatTexture(var1, Math.abs(var13.x) + Math.abs(var13.y) + Math.abs(var13.z));
      this.mc.renderEngine.bindTexture(var14);
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, this.getFatShrink(var1, var9));
      GlStateManager.translate(var13.x, var13.y + this.getFatBobOffset(var14), var13.z);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      float var15 = 1.4F + this.getFatProgress(var1, var9);
      GlStateManager.scale(var15, var15, var15);
      Tessellator var16 = Tessellator.getInstance();
      BufferBuilder var17 = var16.getBuffer();
      var17.begin(7, DefaultVertexFormats.POSITION_TEX);
      var17.pos(-1.0, 0.0, 0.0).tex(0.0, 1.0).endVertex();
      var17.pos(1.0, 0.0, 0.0).tex(1.0, 1.0).endVertex();
      var17.pos(1.0, 2.0, 0.0).tex(1.0, 0.0).endVertex();
      var17.pos(-1.0, 2.0, 0.0).tex(0.0, 0.0).endVertex();
      var16.draw();
      GlStateManager.popMatrix();
      GL11.glEnable(2896);
      GlStateManager.disableAlpha();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
      long var18 = System.currentTimeMillis();
      if (this.cachedTexture != PRACTICE_TEXTURE && var14 == PRACTICE_TEXTURE && var18 > this.lastTextureSwitchTime + 60000L) {
         this.mc.player.playSound(SoundHandler.MISC_PYRO[0], 1.0F, 1.0F);
         this.lastTextureSwitchTime = var18;
      }

      this.cachedTexture = var14;
   }

   ResourceLocation getFatTexture(BasicGirlEntity var1, double var2) {
      if (var1.lastSoundTick != -1) {
         return new ResourceLocation("sexmod", String.format("%s%s.png", "textures/entity/pyrocinical/fat/", this.getFatIndex(var1)));
      } else if (var2 < 3.0) {
         return PRACTICE_TEXTURE;
      } else {
         Vec3d var4 = new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ).subtract(var1.getPositionVector());
         if (Math.abs(var4.x) + Math.abs(var4.y) + Math.abs(var4.z) == 0.0) {
            return IDLE_TEXTURE;
         } else {
            return Math.sin(this.mc.player.ticksExisted * 0.75F) > 0.0 ? WALK_TEXTURE_1 : WALK_TEXTURE_2;
         }
      }
   }

   double getFatBobOffset(ResourceLocation var1) {
      return !WALK_TEXTURE_1.equals(var1) && !WALK_TEXTURE_2.equals(var1) ? 0.0 : Math.sin(this.mc.player.ticksExisted * 0.75F) * 0.1F;
   }

   int getFatIndex(BasicGirlEntity var1) {
      return var1.lastSoundTick == -1 ? 0 : (int)ThreadNames.clampFloat(this.mc.player.ticksExisted - var1.lastSoundTick, 1.0F, 30.0F);
   }

   float getFatProgress(BasicGirlEntity var1, float var2) {
      if (var1.lastSoundTick == -1) {
         return 0.0F;
      }

      int var3 = this.getFatIndex(var1);
      return var3 == 30 ? 1.0F : (var3 + var2) / 30.0F;
   }

   float getFatShrink(BasicGirlEntity var1, float var2) {
      if (var1.lastSoundTick == -1) {
         return 1.0F;
      }

      if (this.mc.player.ticksExisted - var1.lastSoundTick > 120) {
         return 0.0F;
      }

      float var4 = ThreadNames.clampFloat(this.mc.player.ticksExisted - var1.lastSoundTick, 90.0F, 120.0F) - 90.0F;
      float var5 = (var4 + var2) / 30.0F;
      return 1.0F - var5;
   }

}
