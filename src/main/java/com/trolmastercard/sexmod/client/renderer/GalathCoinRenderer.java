package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.GalathCoinModel;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

/**
 * Item renderer for the Galath coin: renders the geckolib coin model with a
 * red tint, a special "pentagram" bone, and animation states driven by the
 * player's persistent NBT timestamps.
 * <p>
 * <b>Animation.</b> While held, the coin alternates between an activation
 * spin-up and a deactivation fade over 2-second windows (timestamps
 * {@code sexmod:galath_coin_activation_time}/{@code _deactivation_time}):
 * during spin the lightmap/scale value is 240 -> 120 and the tint goes
 * bright -> dark; during fade it reverses. Idle (not held) coins bob with a
 * sine and stay bright red.
 * <p>
 * CLIENT-side render thread only. The pentagram bone is drawn in a separate
 * full-bright pass with per-vertex tint; the rest uses normal lighting.
 */
public class GalathCoinRenderer extends GeoItemRenderer<GalathCoinItem> {
   public static final Vector3fSexmodSpecial COIN_COLOR = new Vector3fSexmodSpecial(0.84705883F, 0.11764706F, 0.35686275F);
   public static final Vector3fSexmodSpecial COIN_COLOR_DARK = new Vector3fSexmodSpecial(0.44705883F, 0.44705883F, 0.44705883F);
   public static final float ROTATION_SPEED = 240.0F;
   public static final float ROTATION_AMPLITUDE = 120.0F;
   static final float BOB_SPEED = 0.05F;
   static final Minecraft mc = Minecraft.getMinecraft();
   boolean isFlipping = false;
   Vector3fSexmodSpecial currentTint;

   public GalathCoinRenderer() {
      super(new GalathCoinModel());
   }

   /**
    * Custom render pass: draws all bones except the pentagram with normal
    * lighting, then draws the pentagram bone full-bright in a separate tinted
    * pass (lightmap + color from {@link #getCoinColor()}), driven by the
    * current spin/fade state. Resets culling/lighting state afterwards.
    */
   @Override
   public void render(GeoModel var1, GalathCoinItem var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      BufferBuilder var8 = Tessellator.getInstance().getBuffer();
      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GeoBone var9 = null;
      this.isFlipping = false;
      GeoBone var10 = var1.topLevelBones.get(0);
      MATRIX_STACK.push();
      MATRIX_STACK.translate(var10);
      MATRIX_STACK.moveToPivot(var10);
      MATRIX_STACK.rotate(var10);
      MATRIX_STACK.scale(var10);
      MATRIX_STACK.moveBackFromPivot(var10);

      for (GeoBone var12 : var10.childBones) {
         if ("pentagram".equals(var12.getName())) {
            var9 = var12;
         } else {
            this.renderRecursively(var8, var12, var4, var5, var6, var7);
         }
      }

      Tessellator.getInstance().draw();
      float var13 = this.getCoinScale(var3);
      this.currentTint = this.getCoinColor();
      if (!GirlSavedData.debugEnabled) {
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var13, var13);
         GL11.glDisable(2896);
      }

      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.isFlipping = true;
      this.renderRecursively(var8, var9, var4, var5, var6, var7);
      Tessellator.getInstance().draw();
      GL11.glEnable(2896);
      MATRIX_STACK.pop();
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
      GlStateManager.resetColor();
   }

   /**
    * Resolves the coin's scale/brightness value: 120 (full-bright) while
    * spinning up, 240 (dim) while fading, else a sine bob for idle coins; in
    * debug mode it is pinned to 120.
    */
   float getCoinScale(float var1) {
      if (mc.player.getHeldItemMainhand() != this.currentItemStack && mc.player.getHeldItemOffhand() != this.currentItemStack) {
         return this.getCoinBob(var1);
      } else {
         long var2 = System.currentTimeMillis();
         NBTTagCompound var4 = mc.player.getEntityData();
         long var5 = var4.getLong("sexmod:galath_coin_activation_time");
         long var7 = var4.getLong("sexmod:galath_coin_deactivation_time");
         if (var5 != 0L) {
            return this.getCoinSpin(var2, var5, var1);
         } else if (var7 != 0L) {
            return this.getCoinFade(var2, var7, var1);
         } else {
            return GirlSavedData.debugEnabled ? 120.0F : this.getCoinBob(var1);
         }
      }
   }

   float getCoinFade(long var1, long var3, float var5) {
      float var6 = (float)(var1 - var3);
      if (var6 < 1000.0F) {
         return 120.0F;
      } else {
         return var6 <= 3000.0F ? RotationHelper.lerp(120.0F, 240.0F, (var6 - 1000.0F) / 2000.0F) : 240.0F;
      }
   }

   float getCoinSpin(long var1, long var3, float var5) {
      float var6 = (float)(var1 - var3);
      if (var6 < 1000.0F) {
         return 240.0F;
      } else {
         return var6 <= 3000.0F ? RotationHelper.lerp(240.0F, 120.0F, (var6 - 1000.0F) / 2000.0F) : 120.0F;
      }
   }

   /**
    * Resolves the coin's tint color: dark while spinning up (lerping back to
    * bright over 2s), bright while fading, static bright when idle.
    */
   Vector3fSexmodSpecial getCoinColor() {
      if (mc.player.getHeldItemMainhand() != this.currentItemStack && mc.player.getHeldItemOffhand() != this.currentItemStack) {
         return COIN_COLOR;
      } else {
         long var1 = System.currentTimeMillis();
         NBTTagCompound var3 = mc.player.getEntityData();
         long var4 = var3.getLong("sexmod:galath_coin_activation_time");
         long var6 = var3.getLong("sexmod:galath_coin_deactivation_time");
         if (var4 != 0L) {
            return this.getCoinColorDark(var4, var1);
         } else if (var6 != 0L) {
            return this.getCoinColor(var6, var1);
         } else {
            return GirlSavedData.debugEnabled ? COIN_COLOR_DARK : COIN_COLOR;
         }
      }
   }

   Vector3fSexmodSpecial getCoinColor(long var1, long var3) {
      float var5 = (float)(var3 - var1);
      if (var5 < 1000.0F) {
         return COIN_COLOR_DARK;
      } else {
         return var5 <= 3000.0F ? RotationHelper.lerpVector3f(COIN_COLOR_DARK, COIN_COLOR, (var5 - 1000.0F) / 2000.0F) : COIN_COLOR;
      }
   }

   Vector3fSexmodSpecial getCoinColorDark(long var1, long var3) {
      float var5 = (float)(var3 - var1);
      if (var5 < 1000.0F) {
         return COIN_COLOR;
      } else {
         return var5 <= 3000.0F ? RotationHelper.lerpVector3f(COIN_COLOR, COIN_COLOR_DARK, (var5 - 1000.0F) / 2000.0F) : COIN_COLOR_DARK;
      }
   }

   float getCoinBob(float var1) {
      return (float)(60.0 * Math.sin((mc.player.ticksExisted + var1) * 0.05F) + 180.0);
   }

   void renderCoinQuads(BufferBuilder var1, GeoCube var2) {
      for (GeoQuad var6 : var2.quads) {
         if (var6 != null) {
            for (GeoVertex var10 : var6.vertices) {
               Vector4f var11 = new Vector4f(var10.position.getX(), var10.position.getY(), var10.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var11);
               var1.pos(var11.getX(), var11.getY(), var11.getZ())
                  .tex(var10.textureU, var10.textureV)
                  .color(this.currentTint.x, this.currentTint.y, this.currentTint.z, 1.0F)
                  .endVertex();
            }
         }
      }
   }

   /**
    * Cube pass: transforms vertices by the model matrix and emits them. In
    * flipping mode (pentagram pass) it emits tinted vertices without normals;
    * otherwise it computes and mirrors normals for zero-size cube faces (the
    * coin's flat quads) and emits lit vertices.
    */
   @Override
   public void renderCube(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);
      if (this.isFlipping) {
         this.renderCoinQuads(var1, var2);
      } else {
         for (GeoQuad var10 : var2.quads) {
            if (var10 != null) {
               Vector3f var11 = new Vector3f(var10.normal.getX(), var10.normal.getY(), var10.normal.getZ());
               MATRIX_STACK.getNormalMatrix().transform(var11);
               if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var11.getX() < 0.0F) {
                  var11.x *= -1.0F;
               }

               if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var11.getY() < 0.0F) {
                  var11.y *= -1.0F;
               }

               if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var11.getZ() < 0.0F) {
                  var11.z *= -1.0F;
               }

               for (GeoVertex var15 : var10.vertices) {
                  Vector4f var16 = new Vector4f(var15.position.getX(), var15.position.getY(), var15.position.getZ(), 1.0F);
                  MATRIX_STACK.getModelMatrix().transform(var16);
                  var1.pos(var16.getX(), var16.getY(), var16.getZ())
                     .tex(var15.textureU, var15.textureV)
                     .color(var3, var4, var5, var6)
                     .normal(var11.getX(), var11.getY(), var11.getZ())
                     .endVertex();
               }
            }
         }
      }
   }

}
