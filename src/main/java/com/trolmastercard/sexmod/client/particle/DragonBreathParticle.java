package com.trolmastercard.sexmod.client.particle;

import net.minecraft.client.particle.ParticleDragonBreath;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Dragon-breath particle for the dragon staff's breath effect: a
 * {@link ParticleDragonBreath} whose scale is pinned to the static
 * {@link #BREATH_SCALE} (so callers can tune the breath size globally) and
 * whose quad is rendered with the standard billboard math.
 * <p>
 * CLIENT-side render thread only (vanilla particle system).
 */
public class DragonBreathParticle extends ParticleDragonBreath {
   public static float BREATH_SCALE = 0.2F;

   public DragonBreathParticle(World world, double x, double y, double z) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
   }

   /**
    * Renders the camera-facing quad at the interpolated position with the
    * current particle texture frame (copied from vanilla so the breath scale
    * override stays applied). CLIENT-side render thread.
    */
   public void renderParticle(BufferBuilder builder, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
      this.particleScale = BREATH_SCALE;
      float minU = this.particleTextureIndexX / 16.0F;
      float maxU = minU + 0.0624375F;
      float minV = this.particleTextureIndexY / 16.0F;
      float maxV = minV + 0.0624375F;
      float scale = 0.1F * this.particleScale;
      if (this.particleTexture != null) {
         minU = this.particleTexture.getMinU();
         maxU = this.particleTexture.getMaxU();
         minV = this.particleTexture.getMinV();
         maxV = this.particleTexture.getMaxV();
      }

      float posX = (float)(this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
      float posY = (float)(this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
      float posZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
      int brightness = this.getBrightnessForRender(partialTicks);
      int brightnessX = brightness >> 16 & 65535;
      int brightnessZ = brightness & 65535;
      Vec3d[] quadVecs = new Vec3d[]{
         new Vec3d(-rotationX * scale - rotationXY * scale, -rotationZ * scale, -rotationYZ * scale - rotationXZ * scale),
         new Vec3d(-rotationX * scale + rotationXY * scale, rotationZ * scale, -rotationYZ * scale + rotationXZ * scale),
         new Vec3d(rotationX * scale + rotationXY * scale, rotationZ * scale, rotationYZ * scale + rotationXZ * scale),
         new Vec3d(rotationX * scale - rotationXY * scale, -rotationZ * scale, rotationYZ * scale - rotationXZ * scale)
      };
      if (this.particleAngle != 0.0F) {
         float rotAngle = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
         float cosAngle = MathHelper.cos(rotAngle * 0.5F);
         float sinX = MathHelper.sin(rotAngle * 0.5F) * (float)cameraViewDir.x;
         float sinY = MathHelper.sin(rotAngle * 0.5F) * (float)cameraViewDir.y;
         float sinZ = MathHelper.sin(rotAngle * 0.5F) * (float)cameraViewDir.z;
         Vec3d axis = new Vec3d(sinX, sinY, sinZ);

         for (int i = 0; i < 4; i++) {
            quadVecs[i] = axis.scale(2.0 * quadVecs[i].dotProduct(axis))
               .add(quadVecs[i].scale(cosAngle * cosAngle - axis.dotProduct(axis)))
               .add(axis.crossProduct(quadVecs[i]).scale(2.0F * cosAngle));
         }
      }

      builder.pos(posX + quadVecs[0].x, posY + quadVecs[0].y, posZ + quadVecs[0].z)
         .tex(maxU, maxV)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(brightnessX, brightnessZ)
         .endVertex();
      builder.pos(posX + quadVecs[1].x, posY + quadVecs[1].y, posZ + quadVecs[1].z)
         .tex(maxU, minV)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(brightnessX, brightnessZ)
         .endVertex();
      builder.pos(posX + quadVecs[2].x, posY + quadVecs[2].y, posZ + quadVecs[2].z)
         .tex(minU, minV)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(brightnessX, brightnessZ)
         .endVertex();
      builder.pos(posX + quadVecs[3].x, posY + quadVecs[3].y, posZ + quadVecs[3].z)
         .tex(minU, maxV)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(brightnessX, brightnessZ)
         .endVertex();
   }

}
