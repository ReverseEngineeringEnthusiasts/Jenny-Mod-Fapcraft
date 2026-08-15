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
   public void render(GeoModel model, GalathCoinItem coin, float r, float g, float b, float a, float ticks) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GeoBone pentagramBone = null;
      this.isFlipping = false;
      GeoBone topBone = model.topLevelBones.get(0);
      MATRIX_STACK.push();
      MATRIX_STACK.translate(topBone);
      MATRIX_STACK.moveToPivot(topBone);
      MATRIX_STACK.rotate(topBone);
      MATRIX_STACK.scale(topBone);
      MATRIX_STACK.moveBackFromPivot(topBone);

      for (GeoBone bone : topBone.childBones) {
         if ("pentagram".equals(bone.getName())) {
            pentagramBone = bone;
         } else {
            this.renderRecursively(buffer, bone, g, b, a, ticks);
         }
      }

      Tessellator.getInstance().draw();
      float lightmapValue = this.getCoinScale(r);
      this.currentTint = this.getCoinColor();
      if (!GirlSavedData.debugEnabled) {
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapValue, lightmapValue);
         GL11.glDisable(2896);
      }

      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.isFlipping = true;
      this.renderRecursively(buffer, pentagramBone, g, b, a, ticks);
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
   float getCoinScale(float partialTicks) {
      if (mc.player.getHeldItemMainhand() != this.currentItemStack && mc.player.getHeldItemOffhand() != this.currentItemStack) {
         return this.getCoinBob(partialTicks);
      } else {
         long now = System.currentTimeMillis();
         NBTTagCompound nbt = mc.player.getEntityData();
         long activationTime = nbt.getLong("sexmod:galath_coin_activation_time");
         long deactivationTime = nbt.getLong("sexmod:galath_coin_deactivation_time");
         if (activationTime != 0L) {
            return this.getCoinSpin(now, activationTime, partialTicks);
         } else if (deactivationTime != 0L) {
            return this.getCoinFade(now, deactivationTime, partialTicks);
         } else {
            return GirlSavedData.debugEnabled ? 120.0F : this.getCoinBob(partialTicks);
         }
      }
   }

   float getCoinFade(long now, long start, float partialTicks) {
      float elapsed = (float)(now - start);
      if (elapsed < 1000.0F) {
         return 120.0F;
      } else {
         return elapsed <= 3000.0F ? RotationHelper.lerp(120.0F, 240.0F, (elapsed - 1000.0F) / 2000.0F) : 240.0F;
      }
   }

   float getCoinSpin(long now, long start, float partialTicks) {
      float elapsed = (float)(now - start);
      if (elapsed < 1000.0F) {
         return 240.0F;
      } else {
         return elapsed <= 3000.0F ? RotationHelper.lerp(240.0F, 120.0F, (elapsed - 1000.0F) / 2000.0F) : 120.0F;
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
         long now = System.currentTimeMillis();
         NBTTagCompound nbt = mc.player.getEntityData();
         long activationTime = nbt.getLong("sexmod:galath_coin_activation_time");
         long deactivationTime = nbt.getLong("sexmod:galath_coin_deactivation_time");
         if (activationTime != 0L) {
            return this.getCoinColorDark(activationTime, now);
         } else if (deactivationTime != 0L) {
            return this.getCoinColor(deactivationTime, now);
         } else {
            return GirlSavedData.debugEnabled ? COIN_COLOR_DARK : COIN_COLOR;
         }
      }
   }

   Vector3fSexmodSpecial getCoinColor(long start, long now) {
      float elapsed = (float)(now - start);
      if (elapsed < 1000.0F) {
         return COIN_COLOR_DARK;
      } else {
         return elapsed <= 3000.0F ? RotationHelper.lerpVector3f(COIN_COLOR_DARK, COIN_COLOR, (elapsed - 1000.0F) / 2000.0F) : COIN_COLOR;
      }
   }

   Vector3fSexmodSpecial getCoinColorDark(long start, long now) {
      float elapsed = (float)(now - start);
      if (elapsed < 1000.0F) {
         return COIN_COLOR;
      } else {
         return elapsed <= 3000.0F ? RotationHelper.lerpVector3f(COIN_COLOR, COIN_COLOR_DARK, (elapsed - 1000.0F) / 2000.0F) : COIN_COLOR_DARK;
      }
   }

   float getCoinBob(float partialTicks) {
      return (float)(60.0 * Math.sin((mc.player.ticksExisted + partialTicks) * 0.05F) + 180.0);
   }

   void renderCoinQuads(BufferBuilder buffer, GeoCube cube) {
      for (GeoQuad quad : cube.quads) {
         if (quad != null) {
            for (GeoVertex vertex : quad.vertices) {
               Vector4f pos = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(pos);
               buffer.pos(pos.getX(), pos.getY(), pos.getZ())
                  .tex(vertex.textureU, vertex.textureV)
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
   public void renderCube(BufferBuilder buffer, GeoCube cube, float r, float g, float b, float alpha) {
      MATRIX_STACK.moveToPivot(cube);
      MATRIX_STACK.rotate(cube);
      MATRIX_STACK.moveBackFromPivot(cube);
      if (this.isFlipping) {
         this.renderCoinQuads(buffer, cube);
      } else {
         for (GeoQuad quad : cube.quads) {
            if (quad != null) {
               Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
               MATRIX_STACK.getNormalMatrix().transform(normal);
               if ((cube.size.y == 0.0F || cube.size.z == 0.0F) && normal.getX() < 0.0F) {
                  normal.x *= -1.0F;
               }

               if ((cube.size.x == 0.0F || cube.size.z == 0.0F) && normal.getY() < 0.0F) {
                  normal.y *= -1.0F;
               }

               if ((cube.size.x == 0.0F || cube.size.y == 0.0F) && normal.getZ() < 0.0F) {
                  normal.z *= -1.0F;
               }

               for (GeoVertex vertex : quad.vertices) {
                  Vector4f pos = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
                  MATRIX_STACK.getModelMatrix().transform(pos);
                  buffer.pos(pos.getX(), pos.getY(), pos.getZ())
                     .tex(vertex.textureU, vertex.textureV)
                     .color(r, g, b, alpha)
                     .normal(normal.getX(), normal.getY(), normal.getZ())
                     .endVertex();
               }
            }
         }
      }
   }

}
