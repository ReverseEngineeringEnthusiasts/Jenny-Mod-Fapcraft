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
            URL profileUrl = new URL(
               "https://sessionserver.mojang.com/session/minecraft/profile/"
                  + Minecraft.getMinecraft().player.getPersistentID().toString().replace("-", "")
            );
            BufferedReader reader = new BufferedReader(new InputStreamReader(profileUrl.openStream()));
            String profileJson = reader.lines().collect(Collectors.joining());
            int valueStart = profileJson.indexOf("\"value\" : ");
            int valueContentStart = valueStart + 11;
            StringBuilder valueBuilder = new StringBuilder();

            for (int i = 0; profileJson.charAt(valueContentStart + i) != '"'; i++) {
               valueBuilder.append(profileJson.charAt(valueContentStart + i));
            }

            String decodedValue = new String(Base64.getDecoder().decode(valueBuilder.toString()));
            int urlStart = decodedValue.indexOf("\"url\" : ");
            int urlContentStart = urlStart + 9;
            StringBuilder urlBuilder = new StringBuilder();

            for (int j = 0; decodedValue.charAt(urlContentStart + j) != '"'; j++) {
               urlBuilder.append(decodedValue.charAt(urlContentStart + j));
            }

            URL skinUrl = new URL(urlBuilder.toString());
            BufferedImage playerSkin = ImageIO.read(skinUrl);
            BufferedImage baseTexture = ImageIO.read(this.mc.getResourceManager().getResource(new AlliesLampModel().getTextureLocation((AlliesLampItem) null)).getInputStream());

            for (int x = 0; x < baseTexture.getWidth(); x++) {
               for (int y = 0; y < baseTexture.getHeight(); y++) {
                  int rgb = playerSkin.getRGB(x, y);
                  if (rgb != 0) {
                     baseTexture.setRGB(x, y, rgb);
                  }
               }
            }

            lampTexture = Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation("lamptex", new DynamicTexture(baseTexture));
         } catch (Exception e) {
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
   public void render(GeoModel model, AlliesLampItem lamp, float r, float g, float b, float a, float ticks) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      this.renderEarly(lamp, r, g, b, a, ticks);
      this.renderLate(lamp, r, g, b, a, ticks);
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      for (GeoBone bone : model.topLevelBones) {
         this.renderLampBone(buffer, lamp, bone, g, b, a, ticks);
      }

      Tessellator.getInstance().draw();
      this.renderAfter(lamp, r, g, b, a, ticks);
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
   }

   /**
    * Pushes the bone's transform onto the geckolib matrix stack and renders
    * its cubes with the skin texture, skipping the arm bones in first person
    * while the lamp is in use ({@code sexmodAllieInUse}).
    */
   public void renderLampBone(BufferBuilder buffer, AlliesLampItem lamp, GeoBone bone, float r, float g, float b, float a) {
      MATRIX_STACK.push();
      MATRIX_STACK.translate(bone);
      MATRIX_STACK.moveToPivot(bone);
      MATRIX_STACK.rotate(bone);
      MATRIX_STACK.scale(bone);
      MATRIX_STACK.moveBackFromPivot(bone);
      this.mc.renderEngine.bindTexture(this.getSkin());
      if (this.isNotArmBone(bone.getName())) {
         this.renderLampEffect(buffer, lamp, bone, r, g, b, a);
      }

      MATRIX_STACK.pop();
   }

   /**
    * Whether this bone may be drawn: arm bones are only drawn in third person
    * or when the lamp is not currently in use.
    */
   boolean isNotArmBone(String boneName) {
      return !boneName.equals("leftArm") && !boneName.equals("rightArm")
         ? true
         : this.mc.player.getEntityData().getBoolean("sexmodAllieInUse") && this.mc.gameSettings.thirdPersonView == 0;
   }

   /**
    * Renders a bone's cubes and recurses into its child bones, unless the
    * bone is hidden.
    */
   void renderLampEffect(BufferBuilder buffer, AlliesLampItem lamp, GeoBone bone, float r, float g, float b, float a) {
      if (!bone.isHidden) {
         for (GeoCube cube : bone.childCubes) {
            MATRIX_STACK.push();
            GlStateManager.pushMatrix();
            this.renderCube(buffer, cube, r, g, b, a);
            GlStateManager.popMatrix();
            MATRIX_STACK.pop();
         }

         for (GeoBone childBone : bone.childBones) {
            this.renderLampBone(buffer, lamp, childBone, r, g, b, a);
         }
      }
   }

}
