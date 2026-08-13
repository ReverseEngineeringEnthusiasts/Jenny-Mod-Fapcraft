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

public class GalathCoinRenderer extends GeoItemRenderer<GalathCoinItem> {
   public static final Vector3fSexmodSpecial e = new Vector3fSexmodSpecial(0.84705883F, 0.11764706F, 0.35686275F);
   public static final Vector3fSexmodSpecial f = new Vector3fSexmodSpecial(0.44705883F, 0.44705883F, 0.44705883F);
   public static final float b = 240.0F;
   public static final float g = 120.0F;
   static final float h = 0.05F;
   static final Minecraft a = Minecraft.getMinecraft();
   boolean c = false;
   Vector3fSexmodSpecial d;

   public GalathCoinRenderer() {
      super(new GalathCoinModel());
   }

   @Override
   public void render(GeoModel var1, GalathCoinItem var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      BufferBuilder var8 = Tessellator.getInstance().getBuffer();
      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GeoBone var9 = null;
      this.c = false;
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
      float var13 = this.a_clash107(var3);
      this.d = this.a_clash108();
      if (!GirlSavedData.f) {
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var13, var13);
         GL11.glDisable(2896);
      }

      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.c = true;
      this.renderRecursively(var8, var9, var4, var5, var6, var7);
      Tessellator.getInstance().draw();
      GL11.glEnable(2896);
      MATRIX_STACK.pop();
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
      GlStateManager.resetColor();
   }

   float a_clash107(float var1) {
      if (a.player.getHeldItemMainhand() != this.currentItemStack && a.player.getHeldItemOffhand() != this.currentItemStack) {
         return this.b_clash109(var1);
      } else {
         long var2 = System.currentTimeMillis();
         NBTTagCompound var4 = a.player.getEntityData();
         long var5 = var4.getLong("sexmod:galath_coin_activation_time");
         long var7 = var4.getLong("sexmod:galath_coin_deactivation_time");
         if (var5 != 0L) {
            return this.a(var2, var5, var1);
         } else if (var7 != 0L) {
            return this.b(var2, var7, var1);
         } else {
            return GirlSavedData.f ? 120.0F : this.b_clash109(var1);
         }
      }
   }

   float b(long var1, long var3, float var5) {
      float var6 = (float)(var1 - var3);
      if (var6 < 1000.0F) {
         return 120.0F;
      } else {
         return var6 <= 3000.0F ? RotationHelper.lerp(120.0F, 240.0F, (var6 - 1000.0F) / 2000.0F) : 240.0F;
      }
   }

   float a(long var1, long var3, float var5) {
      float var6 = (float)(var1 - var3);
      if (var6 < 1000.0F) {
         return 240.0F;
      } else {
         return var6 <= 3000.0F ? RotationHelper.lerp(240.0F, 120.0F, (var6 - 1000.0F) / 2000.0F) : 120.0F;
      }
   }

   Vector3fSexmodSpecial a_clash108() {
      if (a.player.getHeldItemMainhand() != this.currentItemStack && a.player.getHeldItemOffhand() != this.currentItemStack) {
         return e;
      } else {
         long var1 = System.currentTimeMillis();
         NBTTagCompound var3 = a.player.getEntityData();
         long var4 = var3.getLong("sexmod:galath_coin_activation_time");
         long var6 = var3.getLong("sexmod:galath_coin_deactivation_time");
         if (var4 != 0L) {
            return this.b(var4, var1);
         } else if (var6 != 0L) {
            return this.a(var6, var1);
         } else {
            return GirlSavedData.f ? f : e;
         }
      }
   }

   Vector3fSexmodSpecial a(long var1, long var3) {
      float var5 = (float)(var3 - var1);
      if (var5 < 1000.0F) {
         return f;
      } else {
         return var5 <= 3000.0F ? RotationHelper.a(f, e, (var5 - 1000.0F) / 2000.0F) : e;
      }
   }

   Vector3fSexmodSpecial b(long var1, long var3) {
      float var5 = (float)(var3 - var1);
      if (var5 < 1000.0F) {
         return e;
      } else {
         return var5 <= 3000.0F ? RotationHelper.a(e, f, (var5 - 1000.0F) / 2000.0F) : f;
      }
   }

   float b_clash109(float var1) {
      return (float)(60.0 * Math.sin((a.player.ticksExisted + var1) * 0.05F) + 180.0);
   }

   void a(BufferBuilder var1, GeoCube var2) {
      for (GeoQuad var6 : var2.quads) {
         if (var6 != null) {
            for (GeoVertex var10 : var6.vertices) {
               Vector4f var11 = new Vector4f(var10.position.getX(), var10.position.getY(), var10.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var11);
               var1.pos(var11.getX(), var11.getY(), var11.getZ())
                  .tex(var10.textureU, var10.textureV)
                  .color(this.d.a, this.d.c, this.d.b, 1.0F)
                  .endVertex();
            }
         }
      }
   }

   @Override
   public void renderCube(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);
      if (this.c) {
         this.a(var1, var2);
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
