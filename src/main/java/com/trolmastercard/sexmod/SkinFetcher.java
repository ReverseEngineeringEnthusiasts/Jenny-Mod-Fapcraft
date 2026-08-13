package com.trolmastercard.sexmod;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SkinFetcher {
   public static final int a = 3;

   @SideOnly(Side.CLIENT)
   public static BufferedImage a_clash864(UUID var0) {
      try {
         URL var1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + var0.toString().replace("-", ""));
         BufferedReader var2 = new BufferedReader(new InputStreamReader(var1.openStream()));
         String var3 = var2.lines().collect(Collectors.joining());
         int var4 = var3.indexOf("\"value\" : ");
         int var5 = var4 + 11;
         StringBuilder var6 = new StringBuilder();

         for (int var7 = 0; var3.charAt(var5 + var7) != '"'; var7++) {
            var6.append(var3.charAt(var5 + var7));
         }

         String var13 = new String(Base64.getDecoder().decode(var6.toString()));
         int var8 = var13.indexOf("\"url\" : ");
         int var9 = var8 + 9;
         StringBuilder var10 = new StringBuilder();

         for (int var11 = 0; var13.charAt(var9 + var11) != '"'; var11++) {
            var10.append(var13.charAt(var9 + var11));
         }

         URL var14 = new URL(var10.toString());
         return ImageIO.read(var14);
      } catch (Exception var12) {
         try {
            return ImageIO.read(
               Minecraft.func_71410_x().func_110442_L().func_110536_a(new ResourceLocation("sexmod", "textures/player/steve.png")).func_110527_b()
            );
         } catch (java.io.IOException var13) {
            return null;
         }
      }
   }

}
