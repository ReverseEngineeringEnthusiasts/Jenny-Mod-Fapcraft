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

/**
 * Billboard renderer for {@link BasicGirlEntity} (the "pyrocinical" girl): a
 * flat textured quad that always faces the player, with a multi-stage texture
 * state machine — idle, walking (two-frame bob), praising/practice, and a
 * 30-tick "fat" morph animation driven by the girl's {@code lastSoundTick}.
 * <p>
 * <b>Texture selection.</b> {@link #getFatTexture} picks: the fat morph frame
 * when the girl recently made a sound, the practice texture when the player is
 * nearly stationary, idle when the girl isn't moving, or alternating walk
 * frames otherwise. Scale grows 1.4x.. and alpha fades over the morph, then
 * shrinks back after 120 ticks.
 * <p>
 * CLIENT-side render thread only. Positions are interpolated with
 * {@link RotationHelper#lerpVec3dDouble} (PROGRESS lerp — correct for render
 * interpolation, do not switch to the INT step variant).
 */
public class BasicGirlRenderer extends Render<BasicGirlEntity> {

   @Override
   protected ResourceLocation getEntityTexture(BasicGirlEntity girl) {
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

   public BasicGirlRenderer(RenderManager renderManager) {
      super(renderManager);
      this.mc = Minecraft.getMinecraft();
   }

   @Nullable
   protected ResourceLocation getGirlTexture(BasicGirlEntity girl) {
      return null;
   }

   /**
    * Renders the girl as a player-facing quad relative to the local player
    * (lerped positions, billboard rotation, scale from the morph progress,
    * alpha from the morph shrink), and fires the praising sound once per
    * minute when the practice texture first appears. Resets lighting/alpha GL
    * state afterwards.
    */
   public void doRenderBasicGirl(BasicGirlEntity girl, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glDisable(2896);
      GlStateManager.enableAlpha();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      EntityPlayerSP localPlayer = this.mc.player;
      Vec3d girlPos = RotationHelper.lerpVec3dDouble(new Vec3d(girl.lastTickPosX, girl.lastTickPosY, girl.lastTickPosZ), girl.getPositionVector(), partialTicks);
      Vec3d playerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      Vec3d offset = girlPos.subtract(playerPos);
      ResourceLocation texture = this.getFatTexture(girl, Math.abs(offset.x) + Math.abs(offset.y) + Math.abs(offset.z));
      this.mc.renderEngine.bindTexture(texture);
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, this.getFatShrink(girl, partialTicks));
      GlStateManager.translate(offset.x, offset.y + this.getFatBobOffset(texture), offset.z);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      float scale = 1.4F + this.getFatProgress(girl, partialTicks);
      GlStateManager.scale(scale, scale, scale);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      buffer.pos(-1.0, 0.0, 0.0).tex(0.0, 1.0).endVertex();
      buffer.pos(1.0, 0.0, 0.0).tex(1.0, 1.0).endVertex();
      buffer.pos(1.0, 2.0, 0.0).tex(1.0, 0.0).endVertex();
      buffer.pos(-1.0, 2.0, 0.0).tex(0.0, 0.0).endVertex();
      tessellator.draw();
      GlStateManager.popMatrix();
      GL11.glEnable(2896);
      GlStateManager.disableAlpha();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
      long now = System.currentTimeMillis();
      if (this.cachedTexture != PRACTICE_TEXTURE && texture == PRACTICE_TEXTURE && now > this.lastTextureSwitchTime + 60000L) {
         this.mc.player.playSound(SoundHandler.MISC_PYRO[0], 1.0F, 1.0F);
         this.lastTextureSwitchTime = now;
      }

      this.cachedTexture = texture;
   }

   /**
    * Texture state machine (see class javadoc): fat morph frame when
    * {@code lastSoundTick} is active, practice texture when the player is
    * nearly still (distance < 3), idle when the girl hasn't moved, otherwise
    * alternating walk frames.
    *
    * @param distance the player-relative distance to the girl
    */
   ResourceLocation getFatTexture(BasicGirlEntity girl, double distance) {
      if (girl.lastSoundTick != -1) {
         return new ResourceLocation("sexmod", String.format("%s%s.png", "textures/entity/pyrocinical/fat/", this.getFatIndex(girl)));
      } else if (distance < 3.0) {
         return PRACTICE_TEXTURE;
      } else {
         Vec3d movement = new Vec3d(girl.lastTickPosX, girl.lastTickPosY, girl.lastTickPosZ).subtract(girl.getPositionVector());
         if (Math.abs(movement.x) + Math.abs(movement.y) + Math.abs(movement.z) == 0.0) {
            return IDLE_TEXTURE;
         } else {
            return Math.sin(this.mc.player.ticksExisted * 0.75F) > 0.0 ? WALK_TEXTURE_1 : WALK_TEXTURE_2;
         }
      }
   }

   double getFatBobOffset(ResourceLocation texture) {
      return !WALK_TEXTURE_1.equals(texture) && !WALK_TEXTURE_2.equals(texture) ? 0.0 : Math.sin(this.mc.player.ticksExisted * 0.75F) * 0.1F;
   }

   /**
    * Morph frame index 1..30, clamped from the time since the girl's last
    * sound.
    */
   int getFatIndex(BasicGirlEntity girl) {
      return girl.lastSoundTick == -1 ? 0 : (int)ThreadNames.clampFloat(this.mc.player.ticksExisted - girl.lastSoundTick, 1.0F, 30.0F);
   }

   /**
    * Scale multiplier of the morph animation: 0 idle, ramps 0..1 over 30 ticks
    * after a sound, then holds.
    */
   float getFatProgress(BasicGirlEntity girl, float partialTicks) {
      if (girl.lastSoundTick == -1) {
         return 0.0F;
      }

      int index = this.getFatIndex(girl);
      return index == 30 ? 1.0F : (index + partialTicks) / 30.0F;
   }

   /**
    * Alpha multiplier for the morph: 1 normally, fades to 0 between 90 and 120
    * ticks after the sound (the girl "un-morphs" back to normal).
    */
   float getFatShrink(BasicGirlEntity girl, float partialTicks) {
      if (girl.lastSoundTick == -1) {
         return 1.0F;
      }

      if (this.mc.player.ticksExisted - girl.lastSoundTick > 120) {
         return 0.0F;
      }

      float elapsed = ThreadNames.clampFloat(this.mc.player.ticksExisted - girl.lastSoundTick, 90.0F, 120.0F) - 90.0F;
      float fadeProgress = (elapsed + partialTicks) / 30.0F;
      return 1.0F - fadeProgress;
   }

}
