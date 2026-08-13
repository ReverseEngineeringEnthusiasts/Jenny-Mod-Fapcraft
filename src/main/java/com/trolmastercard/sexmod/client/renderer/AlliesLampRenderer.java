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
   Minecraft mc = Minecraft.getMinecraft();
   static ResourceLocation lampTexture = null;

   public AlliesLampRenderer() {
      super(new AlliesLampModel());
   }

   ResourceLocation getSkin() {
      if (lampTexture == null) {
         try {
            URL var1 = new URL(
               "https://sessionserver.mojang.com/session/minecraft/profile/"
                  + Minecraft.getMinecraft().player.getPersistentID().toString().replace("-", "")
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
            BufferedImage var13 = ImageIO.read(this.mc.getResourceManager().getResource(new AlliesLampModel().getTextureLocation((AlliesLampItem) null)).getInputStream());

            for (int var14 = 0; var14 < var13.getWidth(); var14++) {
               for (int var15 = 0; var15 < var13.getHeight(); var15++) {
                  int var16 = var12.getRGB(var14, var15);
                  if (var16 != 0) {
                     var13.setRGB(var14, var15, var16);
                  }
               }
            }

            lampTexture = Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation("lamptex", new DynamicTexture(var13));
         } catch (Exception var17) {
            lampTexture = new AlliesLampModel().getTextureLocation((AlliesLampItem) null);
         }
      }

      return lampTexture;
   }

   @Override
   public void render(GeoModel var1, AlliesLampItem var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      this.renderEarly(var2, var3, var4, var5, var6, var7);
      this.renderLate(var2, var3, var4, var5, var6, var7);
      BufferBuilder var8 = Tessellator.getInstance().getBuffer();
      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      for (GeoBone var10 : var1.topLevelBones) {
         this.a(var8, var2, var10, var4, var5, var6, var7);
      }

      Tessellator.getInstance().draw();
      this.renderAfter(var2, var3, var4, var5, var6, var7);
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
   }

   public void a(BufferBuilder var1, AlliesLampItem var2, GeoBone var3, float var4, float var5, float var6, float var7) {
      MATRIX_STACK.push();
      MATRIX_STACK.translate(var3);
      MATRIX_STACK.moveToPivot(var3);
      MATRIX_STACK.rotate(var3);
      MATRIX_STACK.scale(var3);
      MATRIX_STACK.moveBackFromPivot(var3);
      this.mc.renderEngine.bindTexture(this.getSkin());
      if (this.isNotArmBone(var3.getName())) {
         this.b(var1, var2, var3, var4, var5, var6, var7);
      }

      MATRIX_STACK.pop();
   }

   boolean isNotArmBone(String var1) {
      return !var1.equals("leftArm") && !var1.equals("rightArm")
         ? true
         : this.mc.player.getEntityData().getBoolean("sexmodAllieInUse") && this.mc.gameSettings.thirdPersonView == 0;
   }

   void b(BufferBuilder var1, AlliesLampItem var2, GeoBone var3, float var4, float var5, float var6, float var7) {
      if (!var3.isHidden) {
         for (GeoCube var9 : var3.childCubes) {
            MATRIX_STACK.push();
            GlStateManager.pushMatrix();
            this.renderCube(var1, var9, var4, var5, var6, var7);
            GlStateManager.popMatrix();
            MATRIX_STACK.pop();
         }

         for (GeoBone var11 : var3.childBones) {
            this.a(var1, var2, var11, var4, var5, var6, var7);
         }
      }
   }

}
