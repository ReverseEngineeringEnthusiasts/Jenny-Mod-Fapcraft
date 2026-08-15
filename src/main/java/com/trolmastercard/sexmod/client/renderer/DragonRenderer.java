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

/**
 * Billboard renderer for the {@link DragonEntity} energy ball: a translucent
 * cyan/magenta quad pair (outer + inner core) that always faces the player.
 * <p>
 * <b>Color animation.</b> At full scale (SCALE_1_0 == 1) the outer/inner
 * colors continuously lerp between cyan and magenta (sine of world time);
 * while shrinking (scale < 1, e.g. when the ball is consumed) both layers fade
 * from white-tinted to cyan, tracking the scale.
 * <p>
 * CLIENT-side render thread only. Position interpolation uses
 * {@link RotationHelper#lerpVec3dDouble} (PROGRESS lerp — correct for render
 * interpolation).
 */
public class DragonRenderer extends Render<DragonEntity> {
   public static DragonRenderer instance;
   static final UnknownScreen COLOR_CYAN = new UnknownScreen(0, 255, 251, 255);
   static final UnknownScreen COLOR_MAGENTA = new UnknownScreen(255, 0, 236, 255);
   static final UnknownScreen COLOR_WHITE_ALPHA = new UnknownScreen(255, 255, 255, 0);
   Minecraft mc = Minecraft.getMinecraft();

   public DragonRenderer(RenderManager renderManager) {
      super(renderManager);
      instance = this;
   }

   @Nullable
   protected ResourceLocation getEntityTexture(DragonEntity dragon) {
      return new ResourceLocation("sexmod", "textures/entity/galath/energy_ball.png");
   }

   /**
    * Draws the energy ball relative to the local player: full-bright, 50%
    * alpha, billboarded against the player's view, two-layer quad with
    * animated colors (see class javadoc). Restores lighting/alpha state after.
    */
   @Override
   public void doRender(DragonEntity dragon, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glDisable(2896);
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      EntityPlayerSP player = this.mc.player;
      Vec3d dragonPos = RotationHelper.lerpVec3dDouble(new Vec3d(dragon.lastTickPosX, dragon.lastTickPosY, dragon.lastTickPosZ), dragon.getPositionVector(), partialTicks);
      Vec3d playerPos = RotationHelper.lerpVec3dDouble(new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ), player.getPositionVector(), partialTicks);
      Vec3d offset = dragonPos.subtract(playerPos);
      GlStateManager.pushMatrix();
      GlStateManager.translate(offset.x, offset.y, offset.z);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(dragon.SCALE_1_0, dragon.SCALE_1_0, dragon.SCALE_1_0);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      this.mc.renderEngine.bindTexture(this.getEntityTexture(dragon));
      UnknownScreen outerColor;
      UnknownScreen innerColor;
      if (dragon.SCALE_1_0 == 1.0) {
         float time = (float)this.mc.world.getTotalWorldTime() + this.mc.getRenderPartialTicks();
         double lerpProgress = 0.5 * Math.sin(time * 0.5) + 0.5;
         outerColor = RotationHelper.lerpColor(COLOR_CYAN, COLOR_MAGENTA, lerpProgress);
         innerColor = RotationHelper.lerpColor(COLOR_MAGENTA, COLOR_CYAN, lerpProgress);
      } else {
         outerColor = RotationHelper.lerpColor(COLOR_WHITE_ALPHA, COLOR_CYAN, dragon.SCALE_1_0);
         innerColor = RotationHelper.lerpColor(COLOR_WHITE_ALPHA, COLOR_CYAN, dragon.SCALE_1_0);
      }

      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.renderDragonColor(buffer, outerColor, 0.0F);
      tessellator.draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      GlStateManager.translate(0.0F, 0.075F, 0.0F);
      this.renderDragonColor(buffer, innerColor, 0.001F);
      tessellator.draw();
      GlStateManager.popMatrix();
      GlStateManager.disableAlpha();
      GL11.glEnable(2896);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
   }

   /**
    * Emits one billboard quad (0.5x0.5) with the given color and a tiny z
    * offset to avoid z-fighting between the two layers.
    */
   void renderDragonColor(BufferBuilder buffer, UnknownScreen color, float zOffset) {
      buffer.pos(-0.25, 0.0, zOffset).tex(0.0, 0.0).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(0.25, 0.0, zOffset).tex(1.0, 0.0).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(0.25, 0.5, zOffset).tex(1.0, 1.0).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(-0.25, 0.5, zOffset).tex(0.0, 1.0).color(color.red, color.green, color.blue, color.alpha).endVertex();
   }

}
