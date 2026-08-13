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
   public static ShaderGroup b;
   static final ResourceLocation a = new ResourceLocation("sexmod", "shaders/post/outline.json");
   static Framebuffer c;

   public static void a_clash66() {
      Minecraft var0 = Minecraft.func_71410_x();
      if (!OpenGlHelper.field_148824_g) {
         Main.LOGGER.warn("Shaders not supported");
      } else {
         if (ShaderLinkHelper.func_148074_b() == null) {
            ShaderLinkHelper.func_148076_a();
         }

         try {
            b = new ShaderGroup(var0.func_110434_K(), var0.func_110442_L(), var0.func_147110_a(), a);
            b.func_148026_a(var0.field_71443_c, var0.field_71440_d);
            c = b.func_177066_a("final");
            ClientRegistry.registerEntityShader(BaseGirlEntity.class, a);
            System.out.println("succ registered the outline shader :)");
         } catch (IOException var2) {
            Main.LOGGER.warn("Failed to load shader: {}", a, var2);
         } catch (JsonSyntaxException var3) {
            Main.LOGGER.warn("Failed to load shader: {}", a, var3);
         }
      }
   }

}
