package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.AlliesLampModel;
import com.trolmastercard.sexmod.item.AlliesLampItem;







import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class AlliesLampRenderer extends GeoItemRenderer<AlliesLampItem> {
   Minecraft a = Minecraft.func_71410_x();
   static ResourceLocation b = null;

   public AlliesLampRenderer() {
      super(new AlliesLampModel());
   }

   ResourceLocation a_clash368() {
      if (b == null) {
         try {
            URL var1 = new URL(
               "https://sessionserver.mojang.com/session/minecraft/profile/"
                  + Minecraft.func_71410_x().field_71439_g.getPersistentID().toString().replace("-", "")
            );
            BufferedReader var2 = new BufferedReader(new InputStreamReader(var1.openStream()));
            String var3 = var2.lines().collect(Collectors.joining());
            int var4 = var3.indexOf("\"value\" : ");
            int var5 = var4 + 11;
            StringBuilder var6 = new StringBuilder();

            for (int var7 = 0; var3.charAt(var5 + var7) != '"'; var7++) {
               var6.append(var3.charAt(var5 + var7));
            }

            String var18 = new String(Base64.getDecoder().decode(var6.toString()));
            int var8 = var18.indexOf("\"url\" : ");
            int var9 = var8 + 9;
            StringBuilder var10 = new StringBuilder();

            for (int var11 = 0; var18.charAt(var9 + var11) != '"'; var11++) {
               var10.append(var18.charAt(var9 + var11));
            }

            URL var19 = new URL(var10.toString());
            BufferedImage var12 = ImageIO.read(var19);
            BufferedImage var13 = ImageIO.read(this.a.func_110442_L().func_110536_a(new AlliesLampModel().getTextureLocation((AlliesLampItem) null)).func_110527_b());

            for (int var14 = 0; var14 < var13.getWidth(); var14++) {
               for (int var15 = 0; var15 < var13.getHeight(); var15++) {
                  int var16 = var12.getRGB(var14, var15);
                  if (var16 != 0) {
                     var13.setRGB(var14, var15, var16);
                  }
               }
            }

            b = Minecraft.func_71410_x().func_175598_ae().field_78724_e.func_110578_a("lamptex", new DynamicTexture(var13));
         } catch (Exception var17) {
            b = new AlliesLampModel().getTextureLocation((AlliesLampItem) null);
         }
      }

      return b;
   }

   @Override
   public void render(GeoModel var1, AlliesLampItem var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.func_179129_p();
      GlStateManager.func_179091_B();
      this.renderEarly(var2, var3, var4, var5, var6, var7);
      this.renderLate(var2, var3, var4, var5, var6, var7);
      BufferBuilder var8 = Tessellator.func_178181_a().func_178180_c();
      var8.func_181668_a(7, DefaultVertexFormats.field_181712_l);

      for (GeoBone var10 : var1.topLevelBones) {
         this.a(var8, var2, var10, var4, var5, var6, var7);
      }

      Tessellator.func_178181_a().func_78381_a();
      this.renderAfter(var2, var3, var4, var5, var6, var7);
      GlStateManager.func_179101_C();
      GlStateManager.func_179089_o();
   }

   public void a(BufferBuilder var1, AlliesLampItem var2, GeoBone var3, float var4, float var5, float var6, float var7) {
      MATRIX_STACK.push();
      MATRIX_STACK.translate(var3);
      MATRIX_STACK.moveToPivot(var3);
      MATRIX_STACK.rotate(var3);
      MATRIX_STACK.scale(var3);
      MATRIX_STACK.moveBackFromPivot(var3);
      this.a.field_71446_o.func_110577_a(this.a_clash368());
      if (this.a_clash369(var3.getName())) {
         this.b(var1, var2, var3, var4, var5, var6, var7);
      }

      MATRIX_STACK.pop();
   }

   boolean a_clash369(String var1) {
      return !var1.equals("leftArm") && !var1.equals("rightArm")
         ? true
         : this.a.field_71439_g.getEntityData().func_74767_n("sexmodAllieInUse") && this.a.field_71474_y.field_74320_O == 0;
   }

   void b(BufferBuilder var1, AlliesLampItem var2, GeoBone var3, float var4, float var5, float var6, float var7) {
      if (!var3.isHidden) {
         for (GeoCube var9 : var3.childCubes) {
            MATRIX_STACK.push();
            GlStateManager.func_179094_E();
            this.renderCube(var1, var9, var4, var5, var6, var7);
            GlStateManager.func_179121_F();
            MATRIX_STACK.pop();
         }

         for (GeoBone var11 : var3.childBones) {
            this.a(var1, var2, var11, var4, var5, var6, var7);
         }
      }
   }

   private static Exception a(Exception var0) {
      return var0;
   }
}
