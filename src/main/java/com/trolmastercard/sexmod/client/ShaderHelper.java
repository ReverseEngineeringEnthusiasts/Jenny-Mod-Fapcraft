package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ShaderHelper {
   public static ShaderGroup outlineShaderGroup;
   static final ResourceLocation entityShader = new ResourceLocation("sexmod", "shaders/post/outline.json");
   static Framebuffer outlineFramebuffer;

   public static void initOutlineShader() {
      Minecraft var0 = Minecraft.getMinecraft();
      if (!OpenGlHelper.shadersSupported) {
         Main.LOGGER.warn("Shaders not supported");
      } else {
         if (ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
         }

         try {
            outlineShaderGroup = new ShaderGroup(var0.getTextureManager(), var0.getResourceManager(), var0.getFramebuffer(), entityShader);
            outlineShaderGroup.createBindFramebuffers(var0.displayWidth, var0.displayHeight);
            outlineFramebuffer = outlineShaderGroup.getFramebufferRaw("final");
            ClientRegistry.registerEntityShader(BaseGirlEntity.class, entityShader);
            System.out.println("succ registered the outline shader :)");
         } catch (IOException var2) {
            Main.LOGGER.warn("Failed to load shader: {}", entityShader, var2);
         } catch (JsonSyntaxException var3) {
            Main.LOGGER.warn("Failed to load shader: {}", entityShader, var3);
         }
      }
   }

}
