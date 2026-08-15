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
   public static void applyBoneTransform(MatrixStack stack, GeoBone bone) {
      matrix.set(stack.getModelMatrix());
      matrix.transpose();
      toFloatArray(b, matrix);
      ((Buffer)floatBuffer).clear();
      floatBuffer.put(b);
      ((Buffer)floatBuffer).flip();
      GlStateManager.multMatrix(floatBuffer);
      GlStateManager.translate(bone.rotationPointX / 16.0F, bone.rotationPointY / 16.0F, bone.rotationPointZ / 16.0F);
   }

   /**
    * Flattens a {@link Matrix4f} into a 16-element column-major float array.
    * {@code target} must have length >= 16.
    */
   public static void toFloatArray(float[] target, Matrix4f matrix) {
      target[0] = matrix.m00;
      target[1] = matrix.m01;
      target[2] = matrix.m02;
      target[3] = matrix.m03;
      target[4] = matrix.m10;
      target[5] = matrix.m11;
      target[6] = matrix.m12;
      target[7] = matrix.m13;
      target[8] = matrix.m20;
      target[9] = matrix.m21;
      target[10] = matrix.m22;
      target[11] = matrix.m23;
      target[12] = matrix.m30;
      target[13] = matrix.m31;
      target[14] = matrix.m32;
      target[15] = matrix.m33;
   }

   public static Matrix4f multiply(Matrix4f left, Matrix4f right) {
      Matrix4f copy = (Matrix4f)right.clone();
      copy.mul(left);
      return copy;
   }
}
