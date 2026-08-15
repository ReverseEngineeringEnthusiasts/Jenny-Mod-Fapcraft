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

/**
 * Item renderer for the Allies Lamp: renders the geckolib lamp model but
 * replaces the lamp texture with the local player's own Minecraft skin,
 * blended pixel-wise over the base texture (the skin's opaque pixels win).
 * <p>
 * <b>Skin fetch.</b> {@link #getSkin()} downloads the skin from the Mojang
 * session server once (synchronous HTTP — do not call from the render thread
 * during frame setup on a fresh player) and caches it as a dynamic texture;
 * on any failure it falls back to the plain model texture. The player's arms
 * are hidden while he holds the lamp in first person (skin face would clip).
 * <p>
 * CLIENT-side render thread only.
 */
public class AlliesLampRenderer extends GeoItemRenderer<AlliesLampItem> {
   Minecraft mc = Minecraft.getMinecraft();
   static ResourceLocation lampTexture = null;

   public AlliesLampRenderer() {
      super(new AlliesLampModel());
   }

   /**
    * Lazily fetches the local player's skin, overlays it onto the lamp's base
    * texture (non-transparent skin pixels replace base pixels) and caches the
    * result as a dynamic texture. Falls back to the plain model texture on any
    * error. Cache is static — persists across item renders.
    */
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

   /**
    * Full custom render pass (bypasses most of geckolib's default item
    * rendering): disables culling, renders early/late hooks, then walks the
    * top-level bones with a fresh vertex buffer and the skin texture bound.
    */
   @Override
   public void render(GeoModel var1, AlliesLampItem var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      this.renderEarly(var2, var3, var4, var5, var6, var7);
      this.renderLate(var2, var3, var4, var5, var6, var7);
      BufferBuilder var8 = Tessellator.getInstance().getBuffer();
      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      for (GeoBone var10 : var1.topLevelBones) {
         this.renderLampBone(var8, var2, var10, var4, var5, var6, var7);
      }

      Tessellator.getInstance().draw();
      this.renderAfter(var2, var3, var4, var5, var6, var7);
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
   }

   /**
    * Pushes the bone's transform onto the geckolib matrix stack and renders
    * its cubes with the skin texture, skipping the arm bones in first person
    * while the lamp is in use ({@code sexmodAllieInUse}).
    */
   public void renderLampBone(BufferBuilder var1, AlliesLampItem var2, GeoBone var3, float var4, float var5, float var6, float var7) {
      MATRIX_STACK.push();
      MATRIX_STACK.translate(var3);
      MATRIX_STACK.moveToPivot(var3);
      MATRIX_STACK.rotate(var3);
      MATRIX_STACK.scale(var3);
      MATRIX_STACK.moveBackFromPivot(var3);
      this.mc.renderEngine.bindTexture(this.getSkin());
      if (this.isNotArmBone(var3.getName())) {
         this.renderLampEffect(var1, var2, var3, var4, var5, var6, var7);
      }

      MATRIX_STACK.pop();
   }

   /**
    * Whether this bone may be drawn: arm bones are only drawn in third person
    * or when the lamp is not currently in use.
    */
   boolean isNotArmBone(String var1) {
      return !var1.equals("leftArm") && !var1.equals("rightArm")
         ? true
         : this.mc.player.getEntityData().getBoolean("sexmodAllieInUse") && this.mc.gameSettings.thirdPersonView == 0;
   }

   /**
    * Renders a bone's cubes and recurses into its child bones, unless the
    * bone is hidden.
    */
   void renderLampEffect(BufferBuilder var1, AlliesLampItem var2, GeoBone var3, float var4, float var5, float var6, float var7) {
      if (!var3.isHidden) {
         for (GeoCube var9 : var3.childCubes) {
            MATRIX_STACK.push();
            GlStateManager.pushMatrix();
            this.renderCube(var1, var9, var4, var5, var6, var7);
            GlStateManager.popMatrix();
            MATRIX_STACK.pop();
         }

         for (GeoBone var11 : var3.childBones) {
            this.renderLampBone(var1, var2, var11, var4, var5, var6, var7);
         }
      }
   }

}
