package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.entity.DragonEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class DragonRenderer extends Render<DragonEntity> {
   public static DragonRenderer instance;
   static final UnknownScreen COLOR_CYAN = new UnknownScreen(0, 255, 251, 255);
   static final UnknownScreen COLOR_MAGENTA = new UnknownScreen(255, 0, 236, 255);
   static final UnknownScreen COLOR_WHITE_ALPHA = new UnknownScreen(255, 255, 255, 0);
   Minecraft mc = Minecraft.getMinecraft();

   public DragonRenderer(RenderManager var1) {
      super(var1);
      instance = this;
   }

   @Nullable
   protected ResourceLocation getEntityTexture(DragonEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/galath/energy_ball.png");
   }

   @Override
   public void doRender(DragonEntity var1, double var2, double var4, double var6, float var8, float var9) {
      GL11.glDisable(2896);
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      EntityPlayerSP var10 = this.mc.player;
      Vec3d var11 = RotationHelper.lerpVec3dDouble(new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ), var1.getPositionVector(), var9);
      Vec3d var12 = RotationHelper.lerpVec3dDouble(new Vec3d(var10.lastTickPosX, var10.lastTickPosY, var10.lastTickPosZ), var10.getPositionVector(), var9);
      Vec3d var13 = var11.subtract(var12);
      GlStateManager.pushMatrix();
      GlStateManager.translate(var13.x, var13.y, var13.z);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(var1.SCALE_1_0, var1.SCALE_1_0, var1.SCALE_1_0);
      Tessellator var14 = Tessellator.getInstance();
      BufferBuilder var15 = var14.getBuffer();
      this.mc.renderEngine.bindTexture(this.getEntityTexture(var1));
      UnknownScreen var16;
      UnknownScreen var17;
      if (var1.SCALE_1_0 == 1.0) {
         float var18 = (float)this.mc.world.getTotalWorldTime() + this.mc.getRenderPartialTicks();
         double var19 = 0.5 * Math.sin(var18 * 0.5) + 0.5;
         var16 = RotationHelper.lerpColor(COLOR_CYAN, COLOR_MAGENTA, var19);
         var17 = RotationHelper.lerpColor(COLOR_MAGENTA, COLOR_CYAN, var19);
      } else {
         var16 = RotationHelper.lerpColor(COLOR_WHITE_ALPHA, COLOR_CYAN, var1.SCALE_1_0);
         var17 = RotationHelper.lerpColor(COLOR_WHITE_ALPHA, COLOR_CYAN, var1.SCALE_1_0);
      }

      var15.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.renderDragonColor(var15, var16, 0.0F);
      var14.draw();
      var15.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      GlStateManager.translate(0.0F, 0.075F, 0.0F);
      this.renderDragonColor(var15, var17, 0.001F);
      var14.draw();
      GlStateManager.popMatrix();
      GlStateManager.disableAlpha();
      GL11.glEnable(2896);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
   }

   void renderDragonColor(BufferBuilder var1, UnknownScreen var2, float var3) {
      var1.pos(-0.25, 0.0, var3).tex(0.0, 0.0).color(var2.red, var2.green, var2.blue, var2.alpha).endVertex();
      var1.pos(0.25, 0.0, var3).tex(1.0, 0.0).color(var2.red, var2.green, var2.blue, var2.alpha).endVertex();
      var1.pos(0.25, 0.5, var3).tex(1.0, 1.0).color(var2.red, var2.green, var2.blue, var2.alpha).endVertex();
      var1.pos(-0.25, 0.5, var3).tex(0.0, 1.0).color(var2.red, var2.green, var2.blue, var2.alpha).endVertex();
   }

}
