package com.trolmastercard.sexmod.client.particle;


import net.minecraft.client.particle.ParticleDragonBreath;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DragonBreathParticle extends ParticleDragonBreath {
   public static final float motionX = 0.2F;
   public static final float motionY = 0.5F;
   public static float BREATH_SCALE = 0.2F;

   public DragonBreathParticle(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6, 0.0, 0.0, 0.0);
   }

   public void renderParticle(BufferBuilder var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.particleScale = BREATH_SCALE;
      float var9 = this.particleTextureIndexX / 16.0F;
      float var10 = var9 + 0.0624375F;
      float var11 = this.particleTextureIndexY / 16.0F;
      float var12 = var11 + 0.0624375F;
      float var13 = 0.1F * this.particleScale;
      if (this.particleTexture != null) {
         var9 = this.particleTexture.getMinU();
         var10 = this.particleTexture.getMaxU();
         var11 = this.particleTexture.getMinV();
         var12 = this.particleTexture.getMaxV();
      }

      float var14 = (float)(this.prevPosX + (this.posX - this.prevPosX) * var3 - interpPosX);
      float var15 = (float)(this.prevPosY + (this.posY - this.prevPosY) * var3 - interpPosY);
      float var16 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * var3 - interpPosZ);
      int var17 = this.getBrightnessForRender(var3);
      int var18 = var17 >> 16 & 65535;
      int var19 = var17 & 65535;
      Vec3d[] var20 = new Vec3d[]{
         new Vec3d(-var4 * var13 - var7 * var13, -var5 * var13, -var6 * var13 - var8 * var13),
         new Vec3d(-var4 * var13 + var7 * var13, var5 * var13, -var6 * var13 + var8 * var13),
         new Vec3d(var4 * var13 + var7 * var13, var5 * var13, var6 * var13 + var8 * var13),
         new Vec3d(var4 * var13 - var7 * var13, -var5 * var13, var6 * var13 - var8 * var13)
      };
      if (this.particleAngle != 0.0F) {
         float var21 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * var3;
         float var22 = MathHelper.cos(var21 * 0.5F);
         float var23 = MathHelper.sin(var21 * 0.5F) * (float)cameraViewDir.x;
         float var24 = MathHelper.sin(var21 * 0.5F) * (float)cameraViewDir.y;
         float var25 = MathHelper.sin(var21 * 0.5F) * (float)cameraViewDir.z;
         Vec3d var26 = new Vec3d(var23, var24, var25);

         for (int var27 = 0; var27 < 4; var27++) {
            var20[var27] = var26.scale(2.0 * var20[var27].dotProduct(var26))
               .add(var20[var27].scale(var22 * var22 - var26.dotProduct(var26)))
               .add(var26.crossProduct(var20[var27]).scale(2.0F * var22));
         }
      }

      var1.pos(var14 + var20[0].x, var15 + var20[0].y, var16 + var20[0].z)
         .tex(var10, var12)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(var18, var19)
         .endVertex();
      var1.pos(var14 + var20[1].x, var15 + var20[1].y, var16 + var20[1].z)
         .tex(var10, var11)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(var18, var19)
         .endVertex();
      var1.pos(var14 + var20[2].x, var15 + var20[2].y, var16 + var20[2].z)
         .tex(var9, var11)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(var18, var19)
         .endVertex();
      var1.pos(var14 + var20[3].x, var15 + var20[3].y, var16 + var20[3].z)
         .tex(var9, var12)
         .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
         .lightmap(var18, var19)
         .endVertex();
   }

}
