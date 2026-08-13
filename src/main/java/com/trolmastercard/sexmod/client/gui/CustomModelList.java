package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;







import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

public class CustomModelList extends GuiListExtended {
   static final int c = 3809871;
   static final List<BoneType> f = Arrays.asList(BoneType.values());
   static final String a = "MMMMMMMMMM";
   protected static int e = 200;
   private List<CustomModelList.a> b = new ArrayList<>();
   ClothingScreen d;
   boolean h = false;
   float g = 0.0F;

   public CustomModelList(Minecraft var1, ClothingScreen var2) {
      super(var1, var2.field_146294_l / 2, var2.field_146295_m, 0, var2.field_146295_m, 30);
      e = var2.field_146294_l / 2;
      this.d = var2;
   }

   public IGuiListEntry func_148180_b(int var1) {
      return this.b.get(var1);
   }

   protected int func_148127_b() {
      return this.b.size();
   }

   protected int func_148137_d() {
      return 0;
   }

   protected void drawContainerBackground(Tessellator var1) {
   }

   public void func_178039_p() {
      if (this.func_148141_e(this.field_148162_h)) {
         int var1 = Mouse.getEventDWheel();
         if (var1 != 0) {
            byte var2;
            if (var1 > 0) {
               var2 = -1;
            } else {
               var2 = 1;
            }

            this.field_148169_q = this.field_148169_q + var2 * this.field_148149_f / 2;
         }
      }
   }

   protected void func_148136_c(int var1, int var2, int var3, int var4) {
   }

   void a_clash764() {
      int var1 = this.b.size() * this.field_148149_f;
      if (var1 > this.field_148158_l) {
         this.field_148153_b = 0;
      } else {
         int var2 = this.field_148158_l - var1;
         this.field_148153_b = var2 / 2;
      }
   }

   public void func_148128_a(int var1, int var2, float var3) {
      this.b.clear();
      int var4 = 0;

      for (Entry var6 : ClothingScreen.m) {
         BoneType var7 = (BoneType)var6.getKey();
         Entry var8 = (Entry)var6.getValue();
         this.b.add(new CustomModelList.a(var7, (List<String>)var8.getKey(), (Integer)var8.getValue()));
         if (BoneType.CUSTOM_BONE.equals(var6.getKey())) {
            var4++;
         }
      }

      this.b.sort(Comparator.comparingInt(var0 -> f.indexOf(var0.d)));
      List var9 = ServerWhitelistManager.a_clash143(this.d.c).get(BoneType.CUSTOM_BONE);
      var9.add(0, "cross");
      this.b.add(new CustomModelList.a(var4 > 1));
      this.a_clash764();
      this.a(var1, var2, var3);
      if (this.h) {
         this.func_148145_f(999999);
         this.h = false;
      }
   }

   void a(int var1, int var2, float var3) {
      if (this.field_178041_q) {
         this.field_148150_g = var1;
         this.field_148162_h = var2;
         this.func_148123_a();
         int var4 = this.func_148137_d();
         int var5 = var4 + 6;
         this.func_148121_k();
         GlStateManager.func_179140_f();
         GlStateManager.func_179106_n();
         Tessellator var6 = Tessellator.func_178181_a();
         BufferBuilder var7 = var6.func_178180_c();
         this.drawContainerBackground(var6);
         int var8 = this.field_148152_e + this.field_148155_a / 2 - this.func_148139_c() / 2 + 2;
         int var9 = this.field_148153_b + 4 - (int)this.field_148169_q;
         if (this.field_148165_u) {
            this.func_148129_a(var8, var9, var6);
         }

         this.func_192638_a(var8, var9, var1, var2, var3);
         GlStateManager.func_179097_i();
         this.func_148136_c(0, this.field_148153_b, 255, 255);
         this.func_148136_c(this.field_148154_c, this.field_148158_l, 255, 255);
         GlStateManager.func_179147_l();
         GlStateManager.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
         GlStateManager.func_179118_c();
         GlStateManager.func_179103_j(7425);
         GlStateManager.func_179090_x();
         int var10 = this.func_148135_f();
         if (var10 > 0) {
            int var11 = (this.field_148154_c - this.field_148153_b) * (this.field_148154_c - this.field_148153_b) / this.func_148138_e();
            var11 = MathHelper.func_76125_a(var11, 32, this.field_148154_c - this.field_148153_b - 8);
            int var12 = (int)this.field_148169_q * (this.field_148154_c - this.field_148153_b - var11) / var10 + this.field_148153_b;
            if (var12 < this.field_148153_b) {
               var12 = this.field_148153_b;
            }

            var7.func_181668_a(7, DefaultVertexFormats.field_181709_i);
            var7.func_181662_b(var4, this.field_148154_c, 0.0).func_187315_a(0.0, 1.0).func_181669_b(0, 0, 0, 255).func_181675_d();
            var7.func_181662_b(var5, this.field_148154_c, 0.0).func_187315_a(1.0, 1.0).func_181669_b(0, 0, 0, 255).func_181675_d();
            var7.func_181662_b(var5, this.field_148153_b, 0.0).func_187315_a(1.0, 0.0).func_181669_b(0, 0, 0, 255).func_181675_d();
            var7.func_181662_b(var4, this.field_148153_b, 0.0).func_187315_a(0.0, 0.0).func_181669_b(0, 0, 0, 255).func_181675_d();
            var6.func_78381_a();
            var7.func_181668_a(7, DefaultVertexFormats.field_181709_i);
            var7.func_181662_b(var4, var12 + var11, 0.0).func_187315_a(0.0, 1.0).func_181669_b(128, 128, 128, 255).func_181675_d();
            var7.func_181662_b(var5, var12 + var11, 0.0).func_187315_a(1.0, 1.0).func_181669_b(128, 128, 128, 255).func_181675_d();
            var7.func_181662_b(var5, var12, 0.0).func_187315_a(1.0, 0.0).func_181669_b(128, 128, 128, 255).func_181675_d();
            var7.func_181662_b(var4, var12, 0.0).func_187315_a(0.0, 0.0).func_181669_b(128, 128, 128, 255).func_181675_d();
            var6.func_78381_a();
            var7.func_181668_a(7, DefaultVertexFormats.field_181709_i);
            var7.func_181662_b(var4, var12 + var11 - 1, 0.0).func_187315_a(0.0, 1.0).func_181669_b(192, 192, 192, 255).func_181675_d();
            var7.func_181662_b(var5 - 1, var12 + var11 - 1, 0.0).func_187315_a(1.0, 1.0).func_181669_b(192, 192, 192, 255).func_181675_d();
            var7.func_181662_b(var5 - 1, var12, 0.0).func_187315_a(1.0, 0.0).func_181669_b(192, 192, 192, 255).func_181675_d();
            var7.func_181662_b(var4, var12, 0.0).func_187315_a(0.0, 0.0).func_181669_b(192, 192, 192, 255).func_181675_d();
            var6.func_78381_a();
         }

         this.func_148142_b(var1, var2);
         GlStateManager.func_179098_w();
         GlStateManager.func_179103_j(7424);
         GlStateManager.func_179141_d();
         GlStateManager.func_179084_k();
      }
   }

   public boolean func_148179_a(int var1, int var2, int var3) {
      this.a(var1, var2, var3);
      return super.func_148179_a(var1, var2, var3);
   }

   void a(int var1, int var2, int var3) {
      if (var1 <= this.field_148155_a) {
         int var4 = this.func_148148_g();
         float var5 = var4 + var2 - 5 - this.field_148153_b;
         int var6 = Math.round((float)Math.floor(var5 / this.field_148149_f));
         int var7 = (int)Math.round((var5 / this.field_148149_f - Math.floor(var5 / this.field_148149_f)) * this.field_148149_f);
         if (var6 >= 0) {
            if (var6 < this.b.size()) {
               this.b.get(var6).a(var1, var7, var3, var6);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public class a implements IGuiListEntry {
      static final int g = 4;
      public BoneType d;
      public List<String> b;
      public int f;
      FontRenderer c;
      boolean a = false;
      boolean e = false;

      public a(BoneType var2, List<String> var3, int var4) {
         this.d = var2;
         this.b = var3;
         this.f = var4;
         this.c = CustomModelList.this.field_148161_k.field_71466_p;
      }

      public a(boolean var2) {
         this.e = var2;
         this.a = true;
      }

      boolean b(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (var1 < var3) {
            return false;
         } else if (var1 > var5) {
            return false;
         } else {
            return var2 < var4 ? false : var2 <= var6;
         }
      }

      void b(int var1, int var2, int var3) {
         int var4 = 30;
         var1 += 5;
         CustomModelList.this.field_148161_k.field_71446_o.func_110577_a(ClothingScreen.k);
         CustomModelList.this.d.func_73729_b(var4, var1, 40, this.b(var2, var3, var4, var1, 50, var1 + 20) ? 40 : 20, 20, 20);
         var4 += 40;
         CustomModelList.this.d.func_73729_b(var4, var1, this.e ? 60 : 80, this.e && this.b(var2, var3, var4, var1, var4 + 20, var1 + 20) ? 40 : 20, 20, 20);
      }

      void a(int var1, int var2, int var3) {
         CustomModelList.this.field_148161_k.field_71446_o.func_110577_a(ClothingScreen.k);
         CustomModelList.this.d.func_73729_b(5, var1, 0, 60, this.f == 0 ? 119 : 256, 30);
         int var4 = 15;
         var1 += 5;
         CustomModelList.this.d.a(var4, var1, this.d.iconXPos);
         var4 += 25;
         var4 = this.c(var4, var1, var2, var3);
         BaseGirlEntity var5 = CustomModelList.this.d.d_clash823();
         SexSceneEntity var6;
         if (this.f == 0) {
            var6 = SexSceneEntity.a(CustomModelList.this.field_148161_k.field_71441_e, var5.getGirlId(), this.d);
         } else {
            var6 = new SexSceneEntity(var5.field_70170_p, var5.getGirlId(), this.b.get(this.f));
         }

         ServerWhitelistManager.b var7 = ServerWhitelistManager.b_clash142(var6.a_clash343());
         if (var7 != null) {
            float var8 = !var6.f ? var7.d_clash896() : 1.0F;
            int var27 = (int)(-var7.g_clash895());
            CustomModelList.this.d.a(var4, var1 + 10 + (var6.f ? 0 : 6) + var27, 30.0F * var8, var6);
            if (this.f != 0) {
               CustomModelList.this.d.a_clash820(var6);
            }

            CustomModelList.this.field_148161_k.field_71441_e.func_72973_f(var6);
            var4 = (int)(var4 + 30.0F);
            if (this.f != 0) {
               int var28 = var4;
               String var29 = this.b.get(this.f);
               String var30 = var29.length() > 10 ? var29.substring(0, 7) + "..." : var29;
               this.a(var30, var4, var1 + 10);
               var4 += this.c.func_78256_a("MMMMMMMMMM");
               int var31 = var4;
               int var32 = var4;
               String var33 = ServerWhitelistManager.d_clash141(var29);
               String var34 = var33.length() > 10 ? var33.substring(0, 7) + "..." : var33;
               this.a(var34, var4, var1 + 10);
               var4 += this.c.func_78256_a("MMMMMMMMMM");
               int var35 = var4;
               if (this.b(var2, var3, var28, var1 + 10, var31, var1 + 10 + this.c.field_78288_b)) {
                  CustomModelList.this.d.a(var29, var2, var3);
               }

               if (this.b(var2, var3, var32, var1 + 10, var35, var1 + 10 + this.c.field_78288_b)) {
                  CustomModelList.this.d.a(var33, var2, var3);
               }

               GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.func_179131_c(255.0F, 255.0F, 255.0F, 255.0F);
            }
         } else {
            if (!var6.f) {
            }

            byte var9 = 0;
            CustomModelList.this.d.a(var4, var1 + 10 + (var6.f ? 0 : 6) + var9, 30.0F, var6);
            if (this.f != 0) {
               CustomModelList.this.d.a_clash820(var6);
            }

            CustomModelList.this.field_148161_k.field_71441_e.func_72973_f(var6);
            var4 = (int)(var4 + 30.0F);
            if (this.f != 0) {
               int var10 = var4;
               String var11 = this.b.get(this.f);
               String var12 = var11.length() > 10 ? var11.substring(0, 7) + "..." : var11;
               this.a(var12, var4, var1 + 10);
               var4 += this.c.func_78256_a("MMMMMMMMMM");
               int var13 = var4;
               int var14 = var4;
               String var15 = ServerWhitelistManager.d_clash141(var11);
               String var16 = var15.length() > 10 ? var15.substring(0, 7) + "..." : var15;
               this.a(var16, var4, var1 + 10);
               var4 += this.c.func_78256_a("MMMMMMMMMM");
               int var17 = var4;
               if (this.b(var2, var3, var10, var1 + 10, var13, var1 + 10 + this.c.field_78288_b)) {
                  CustomModelList.this.d.a(var11, var2, var3);
               }

               if (this.b(var2, var3, var14, var1 + 10, var17, var1 + 10 + this.c.field_78288_b)) {
                  CustomModelList.this.d.a(var15, var2, var3);
               }

               GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.func_179131_c(255.0F, 255.0F, 255.0F, 255.0F);
            }
         }
      }

      int c(int var1, int var2, int var3, int var4) {
         CustomModelList.this.d.a(var1, var2, 0, 20 * (this.b(var3, var4, var1, var2, var1 + 20, var2 + 20) ? 2 : 1));
         var1 += 20;
         CustomModelList.this.d.a(var1, var2, 20, 20 * (this.b(var3, var4, var1, var2, var1 + 20, var2 + 20) ? 2 : 1));
         return var1 + 40;
      }

      void a(int var1, int var2, int var3, int var4, int var5) {
         CustomModelList.this.d.func_73729_b(var1, var2, 140, 20, 79, 20);
         var1 += 4;
         int var6 = var1;
         int var7 = var1 + 71 - 4;
         float var8 = this.a_clash868(var2, var6, var7, var3, var4, var5);
         int var9 = (int)RotationHelper.lerp(var6, var7, var8);
         CustomModelList.this.d.func_73729_b(var9, var2, this.b(var3, var4, var9, var2, var9 + 4, var2 + 20) ? 223 : 219, 20, 4, 20);
         CustomModelList.this.d.c.a_clash557(var5, (int)(var8 * 100.0F));
      }

      float a_clash868(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (!CustomModelList.this.d.f) {
            return this.a_clash869(var6);
         }

         if (var4 > 0.33333334F * CustomModelList.this.d.field_146294_l) {
            return this.a_clash869(var6);
         }

         if (var5 < var1 || var5 > var1 + 20) {
            return this.a_clash869(var6);
         }

         if (var4 < var2) {
            return 0.0F;
         }

         if (var4 > var3) {
            return 1.0F;
         }

         var3 -= var2;
         var4 -= var2;
         return (float)var4 / var3;
      }

      float a_clash869(int var1) {
         Entry var2 = CustomModelList.this.d.c.d_clash556(CustomModelList.this.d.g).get(var1);
         return ((Integer)((Entry)var2.getValue()).getValue()).intValue() / 100.0F;
      }

      void b(int var1, int var2, int var3, int var4) {
         if (CustomModelList.this.d.c.h(var4)) {
            CustomModelList.this.field_148161_k.field_71446_o.func_110577_a(ClothingScreen.k);
            CustomModelList.this.d.func_73729_b(5, var1, 0, 60, 119, 30);
            int var10 = 15;
            var1 += 5;
            CustomModelList.this.d.a(var10, var1, CustomModelList.this.d.c.g(var4));
            var10 += 25;
            this.a(var10, var1, var2, var3, var4);
         } else {
            CustomModelList.this.field_148161_k.field_71446_o.func_110577_a(ClothingScreen.k);
            CustomModelList.this.d.func_73729_b(5, var1, 0, 90, 95, 30);
            int var6 = 15;
            var1 += 5;
            CustomModelList.this.d.a(var6, var1, CustomModelList.this.d.c.g(var4));
            var6 += 25;
            this.c(var6, var1, var2, var3);
         }
      }

      public void func_192634_a(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, float var9) {
         if (this.a) {
            this.b(var3, var6, var7);
         } else if (this.d == BoneType.GIRL_SPECIFIC) {
            this.b(var3, var6, var7, var1);
         } else {
            this.a(var3, var6, var7);
         }
      }

      void a(String var1, int var2, int var3) {
         this.c.func_78276_b(var1, var2, var3, 3809871);
         GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
      }

      void b(int var1, int var2) {
         int var3 = 30;
         if (var1 > var3 && var1 < 50) {
            CustomModelList.this.h = true;
            CustomModelList.this.field_148161_k.func_147118_V().func_147682_a(PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F));
            ArrayList var4 = new ArrayList();
            var4.add("cross");
            var4.addAll(ServerWhitelistManager.a_clash143(CustomModelList.this.d.c).get(BoneType.CUSTOM_BONE));
            ClothingScreen.m.add(ClothingScreen.b_clash816(CustomModelList.this.d.c));
         }

         if (this.e) {
            var3 += 40;
            if (var1 > var3 && var1 < var3 + 20) {
               CustomModelList.this.field_148161_k.func_147118_V().func_147682_a(PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F));
               ClothingScreen.m.remove(ClothingScreen.m.size() - 1);
            }
         }
      }

      void a_clash870(int var1, int var2) {
         if (var1 > 40 && var1 < 60) {
            CustomModelList.this.d.a(this.d, false, var2);
         }

         if (var1 > 60 && var1 < 80) {
            CustomModelList.this.d.a(this.d, true, var2);
         }
      }

      void c(int var1, int var2) {
         if (!CustomModelList.this.d.c.h(var2)) {
            this.a_clash870(var1, var2);
         }
      }

      public void a(int var1, int var2, int var3, int var4) {
         if (var3 == 0) {
            if (var2 >= 5) {
               if (var2 <= 25) {
                  if (this.a) {
                     this.b(var1, var2);
                  } else if (this.d == BoneType.GIRL_SPECIFIC) {
                     this.c(var1, var4);
                  } else {
                     this.a_clash870(var1, var4);
                  }
               }
            }
         }
      }

      public void func_192633_a(int var1, int var2, int var3, float var4) {
      }

      public boolean func_148278_a(int var1, int var2, int var3, int var4, int var5, int var6) {
         return false;
      }

      public void func_148277_b(int var1, int var2, int var3, int var4, int var5, int var6) {
      }
   }
}
