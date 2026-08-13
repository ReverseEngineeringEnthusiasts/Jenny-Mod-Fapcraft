package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.fp;







import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public abstract class GirlRendererBase<G extends AbstractNpcOnlyEntity> extends GirlRenderer<G> {
   protected static final Vec3i r = new Vec3i(255, 255, 255);
   static HashMap<Integer, Vec3i> s = new HashMap<>();

   public GirlRendererBase(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   public static void c_clash214() {
      s.clear();
   }

   protected Vec3i a_clash215(GeoBone var1) {
      String var2 = var1.getName();
      int var3 = var2.hashCode() + this.j.getPersistentID().hashCode();
      Vec3i var4 = s.get(var3);
      if (var4 != null) {
         return var4;
      }

      var4 = this.a_clash216(var2);
      s.put(var3, var4);
      return var4;
   }

   protected abstract Vec3i a_clash216(String var1);

   protected static void b(GeoBone var0, int var1) {
      List var2 = var0.childBones;

      for (int var4 = 0; var4 < var2.size(); var4++) {
         GeoBone var5 = (GeoBone)var2.get(var4);
         if (var1 == var4) {
            GeoBone var3 = var5;
            var3.setHidden(false);
            return;
         }
      }
   }

   @Override
   protected void a(BufferBuilder var1, GeoBone var2) {
      ItemStack var3 = this.a_clash341(null);
      float var4 = this.a_clash217();
      Vec3d var5 = this.a_clash218(var3);
      if (var3 != null) {
         GlStateManager.func_179094_E();
         Tessellator.func_178181_a().func_78381_a();
         com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
         GL11.glEnable(2896);
         GlStateManager.func_179152_a(var4, var4, var4);
         GlStateManager.func_179114_b((float)var5.field_72450_a, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179114_b((float)var5.field_72448_b, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b((float)var5.field_72449_c, 0.0F, 0.0F, 1.0F);
         Minecraft.func_71410_x().func_175597_ag().func_178099_a(this.j, var3, TransformType.THIRD_PERSON_RIGHT_HAND);
         this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
         var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
         GL11.glDisable(2896);
         GlStateManager.func_179121_F();
      }
   }

   protected float a_clash217() {
      return 1.0F;
   }

   protected Vec3d a_clash218(ItemStack var1) {
      return new Vec3d(-90.0, 0.0, 0.0);
   }

   protected static GeoBone a(GeoBone var0, int var1) {
      List var2 = var0.childBones;
      GeoBone var3 = null;
      var2.sort(Comparator.comparingDouble(GeoBone::getPivotY));

      for (int var4 = 0; var4 < var2.size(); var4++) {
         GeoBone var5 = (GeoBone)var2.get(var4);
         if (var1 == var4) {
            var3 = var5;
            var3.setHidden(false);
         } else {
            var5.setHidden(true);
         }
      }

      return var3;
   }

   protected Vec3i a_clash219(Vec3i var1) {
      return var1;
   }

   @Override
   public void a(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6, double var7) {
      if (!(this.j.field_70170_p instanceof SexWorldClient)) {
         String var9 = var2.getName();
         if (var9.equals("weapon")) {
            this.a(var1, var2);
         }

         if (var9.equals("itemRenderer") && this.j.y_clash492() == fp.PAYMENT) {
            this.b(var1, var2);
         }

         this.a(var1, var2.getName(), var2);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if (!var2.isHidden) {
            for (GeoCube var11 : var2.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.func_179094_E();
               this.q = var2;
               this.a(var1, var11, var2, var3, var4, var5, var6, var7);
               GlStateManager.func_179121_F();
               MATRIX_STACK.pop();
            }

            for (GeoBone var13 : var2.childBones) {
               this.a(var1, var13, var3, var4, var5, var6, var7);
            }
         }

         MATRIX_STACK.pop();
      }
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      this.a(var1, var2, var3, var4, var5, var6, 0.0);
   }

   public void a(BufferBuilder var1, GeoCube var2, GeoBone var3, float var4, float var5, float var6, float var7, double var8) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);

      for (GeoQuad var13 : var2.quads) {
         if (var13 != null) {
            Vector3f var14 = new Vector3f(var13.normal.func_177958_n(), var13.normal.func_177956_o(), var13.normal.func_177952_p());
            MATRIX_STACK.getNormalMatrix().transform(var14);
            if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var14.getX() < 0.0F) {
               var14.x *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var14.getY() < 0.0F) {
               var14.y *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var14.getZ() < 0.0F) {
               var14.z *= -1.0F;
            }

            Vec3i var15 = this.a_clash215(var3);
            var15 = this.a_clash219(var15);
            Vec3d var16 = BodyParts.a(
               this, var3, new Vec3d(var15.func_177958_n() / 255.0F, var15.func_177956_o() / 255.0F, var15.func_177952_p() / 255.0F), var14
            );

            for (GeoVertex var20 : var13.vertices) {
               Vector4f var21 = new Vector4f(var20.position.getX(), var20.position.getY(), var20.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var21);
               var1.func_181662_b(var21.getX(), var21.getY(), var21.getZ())
                  .func_187315_a(var20.textureU + var8, var20.textureV)
                  .func_181666_a((float)var16.field_72450_a, (float)var16.field_72448_b, (float)var16.field_72449_c, var7)
                  .func_181663_c(var14.getX(), var14.getY(), var14.getZ())
                  .func_181675_d();
            }
         }
      }
   }

   private static RuntimeException c(RuntimeException var0) {
      return var0;
   }
}
