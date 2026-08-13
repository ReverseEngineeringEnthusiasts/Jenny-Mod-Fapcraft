package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UpdatePlayerModelPacket;







import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GirlScreenBase extends GuiScreen {
   List<EntityLivingBase> a = new ArrayList<>();
   int b = 0;
   static float c = 0.0F;

   public GirlScreenBase(HashMap<NpcType, String> var1) {
      this.field_146297_k = Minecraft.func_71410_x();

      for (NpcType var5 : NpcType.values()) {
         if (!var5.isNpcOnly) {
            try {
               Constructor var6 = var5.npcClass.getConstructor(World.class);
               BaseGirlEntity var7 = (BaseGirlEntity)var6.newInstance(this.field_146297_k.field_71441_e);
               var7.b_clash507(true);
               this.a.add(var7);
               String var8 = (String)var1.get(var5);
               if (var8 != null) {
                  var7.a_clash245(BaseGirlEntity.c_clash554(var8));
               }
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }
      }

      this.a.add(this.field_146297_k.field_71439_g);
   }

   public void func_73863_a(int var1, int var2, float var3) {
      super.func_73863_a(var1, var2, var3);
      this.field_146292_n.clear();
      a(this.field_146294_l / 2, this.field_146295_m / 2 + 20, 30, this.a.get(this.b));
      this.field_146292_n.add(new GuiButton(1, this.field_146294_l / 2 + 30, this.field_146295_m / 2 - 10, 20, 20, ">"));
      this.field_146292_n.add(new GuiButton(2, this.field_146294_l / 2 - 50, this.field_146295_m / 2 - 10, 20, 20, "<"));
      this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 30, this.field_146295_m / 2 + 30, 60, 20, "pick"));
   }

   protected void func_146284_a(GuiButton var1) {
      if (">".equals(var1.field_146126_j) && ++this.b >= this.a.size()) {
         this.b = 0;
      }

      if ("<".equals(var1.field_146126_j) && --this.b < 0) {
         this.b = this.a.size() - 1;
      }

      if (var1.field_146127_k == 0) {
         PacketHandler.b.sendToServer(new UpdatePlayerModelPacket(NpcType.a_clash751((Entity)this.a.get(this.b))));
         EntityPlayerSP var2 = Minecraft.func_71410_x().field_71439_g;
         var2.func_71053_j();
         var2.eyeHeight = var2.getDefaultEyeHeight();
         if (!var2.field_71075_bZ.field_75101_c) {
            var2.field_71075_bZ.field_75101_c = var2.field_71075_bZ.field_75098_d;
         }
      }
   }

   public boolean func_73868_f() {
      return false;
   }

   public static void a(int var0, int var1, int var2, EntityLivingBase var3) {
      float var4 = var3.field_70761_aq;
      float var5 = var3.field_70177_z;
      float var6 = var3.field_70125_A;
      float var7 = var3.field_70758_at;
      float var8 = var3.field_70759_as;
      if (!(var3 instanceof EntityPlayer)) {
         var3.field_70165_t = 0.0;
         var3.field_70163_u = 0.0;
         var3.field_70161_v = 0.0;
      }

      var3.field_70761_aq = 0.0F;
      var3.field_70177_z = 0.0F;
      var3.field_70125_A = 0.0F;
      var3.field_70758_at = 0.0F;
      var3.field_70759_as = 0.0F;
      float var9 = Minecraft.func_175610_ah();
      if (var9 == 0.0F) {
         var9 = 0.1F;
      }

      c += 60.0F / var9;
      GlStateManager.func_179142_g();
      GlStateManager.func_179094_E();
      GlStateManager.func_179109_b(var0, var1, 50.0F);
      GlStateManager.func_179152_a(-var2, var2, var2);
      GlStateManager.func_179114_b(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.func_179114_b(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.func_74519_b();
      GlStateManager.func_179114_b(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(c, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179109_b(0.0F, 0.0F, 0.0F);
      RenderManager var10 = Minecraft.func_71410_x().func_175598_ae();
      var10.func_178631_a(180.0F);
      var10.func_178633_a(false);
      var10.func_188391_a(var3, 0.0, 0.0, 0.0, 0.0F, 1.2345679F, false);
      var10.func_178633_a(true);
      GlStateManager.func_179121_F();
      RenderHelper.func_74518_a();
      GlStateManager.func_179101_C();
      GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
      GlStateManager.func_179090_x();
      GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
      var3.field_70761_aq = var4;
      var3.field_70177_z = var5;
      var3.field_70125_A = var6;
      var3.field_70758_at = var7;
      var3.field_70759_as = var8;
   }

   private static Exception a(Exception var0) {
      return var0;
   }
}
