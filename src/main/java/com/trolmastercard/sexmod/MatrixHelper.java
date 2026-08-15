package com.trolmastercard.sexmod;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.MatrixStack;

/**
 * Matrix utilities for geckolib bone rendering. Provides the bridge between
 * geckolib's {@link MatrixStack}/{@link GeoBone} transforms and vanilla OpenGL
 * state, used by renderers that must place vanilla-rendered parts (or custom
 * geometry) at a bone's exact pose.
 * <p>
 * <b>CLIENT-side only.</b> The shared static buffer {@code b}/{@code floatBuffer}
 * are overwritten on every call; callers must consume the GL state immediately and
 * must not hold references across renders.
 */
public class MatrixHelper {
   public static final float[] b = new float[16];
   public static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
   private static final Matrix4f matrix = new Matrix4f();

   /**
    * Applies a bone's model-space transform onto the current OpenGL matrix:
    * the {@link MatrixStack}'s model matrix is transposed, uploaded via
    * {@link GlStateManager#multMatrix}, then translated to the bone's rotation
    * point (pixels converted to blocks, /16).
    * <p>
    * Must be called on the render thread between matrix pushes; the caller is
    * responsible for {@code glPushMatrix}/{@code glPopMatrix} around it. Side
    * effect: clobbers the shared static {@code b} and {@code floatBuffer}.
    */
   public static void applyBoneTransform(MatrixStack var0, GeoBone var1) {
      matrix.set(var0.getModelMatrix());
      matrix.transpose();
      toFloatArray(b, matrix);
      ((Buffer)floatBuffer).clear();
      floatBuffer.put(b);
      ((Buffer)floatBuffer).flip();
      GlStateManager.multMatrix(floatBuffer);
      GlStateManager.translate(var1.rotationPointX / 16.0F, var1.rotationPointY / 16.0F, var1.rotationPointZ / 16.0F);
   }

   /**
    * Flattens a {@link Matrix4f} into a 16-element column-major float array.
    * {@code target} must have length >= 16.
    */
   public static void toFloatArray(float[] var0, Matrix4f var1) {
      var0[0] = var1.m00;
      var0[1] = var1.m01;
      var0[2] = var1.m02;
      var0[3] = var1.m03;
      var0[4] = var1.m10;
      var0[5] = var1.m11;
      var0[6] = var1.m12;
      var0[7] = var1.m13;
      var0[8] = var1.m20;
      var0[9] = var1.m21;
      var0[10] = var1.m22;
      var0[11] = var1.m23;
      var0[12] = var1.m30;
      var0[13] = var1.m31;
      var0[14] = var1.m32;
      var0[15] = var1.m33;
   }

   public static Matrix4f multiply(Matrix4f var0, Matrix4f var1) {
      Matrix4f var2 = (Matrix4f)var1.clone();
      var2.mul(var0);
      return var2;
   }
}
