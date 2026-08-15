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

/**
 * Owns the outline shader for girl entities. Holds the loaded
 * {@link ShaderGroup} and the {@code final} framebuffer so the mod's renderers
 * can draw girls with the outline post-process applied.
 * <p>
 * CLIENT-side only. See {@link #initOutlineShader()} for failure semantics —
 * consumers must treat a {@code null} group as "shader unavailable".
 */
public class ShaderHelper {
   public static ShaderGroup outlineShaderGroup;
   static final ResourceLocation entityShader = new ResourceLocation("sexmod", "shaders/post/outline.json");
   static Framebuffer outlineFramebuffer;

   /**
    * Loads and registers the outline post-processing shader
    * ({@code shaders/post/outline.json}) for {@link BaseGirlEntity}, so girls
    * render with the outline effect, and caches the shader group + "final"
    * framebuffer.
    * <p>
    * CLIENT-side, called once at init. Safe to fail: when shaders are
    * unsupported or the JSON is malformed it only logs a warning and leaves
    * {@code outlineShaderGroup} {@code null} — render code must null-check
    * before using it. Requires a GL context (do not call before the display is
    * created).
    */
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
