package com.trolmastercard.sexmod;


import java.nio.Buffer;
import java.nio.FloatBuffer;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.MatrixStack;

public class MatrixHelper {
   public static final float[] b = new float[16];
   public static final FloatBuffer c = BufferUtils.createFloatBuffer(16);
   private static final Matrix4f a = new Matrix4f();

   public static void a(MatrixStack var0, GeoBone var1) {
      a.set(var0.getModelMatrix());
      a.transpose();
      a(b, a);
      ((Buffer)c).clear();
      c.put(b);
      ((Buffer)c).flip();
      GlStateManager.func_179110_a(c);
      GlStateManager.func_179109_b(var1.rotationPointX / 16.0F, var1.rotationPointY / 16.0F, var1.rotationPointZ / 16.0F);
   }

   public static void a(float[] var0, Matrix4f var1) {
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

   public static Matrix4f a(Matrix4f var0, Matrix4f var1) {
      Matrix4f var2 = (Matrix4f)var1.clone();
      var2.mul(var0);
      return var2;
   }
}
