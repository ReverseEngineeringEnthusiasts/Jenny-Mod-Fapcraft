package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadModelStringPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.e1;







import java.io.IOException;

import java.awt.Desktop;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClothingScreen extends GuiScreen {
   public static final ResourceLocation k = new ResourceLocation("sexmod", "textures/gui/clothing_icons.png");
   static final int r = 20;
   static final float j = 0.25F;
   int n = 0;
   int l = 0;
   float o = 0.0F;
   public static float b = 0.0F;
   protected static List<Integer> a = new ArrayList<>();
   protected static int s = 0;
   protected static int h = 0;
   BaseGirlEntity c;
   boolean p = false;
   CustomModelList q;
   public static List<Entry<BoneType, Entry<List<String>, Integer>>> m = new ArrayList<>();
   final UUID g;
   int i;
   int t;
   public boolean f = false;
   int d = 0;
   int e = 1;

   public ClothingScreen(@Nonnull BaseGirlEntity var1) {
      this.field_146297_k = Minecraft.func_71410_x();
      this.g = var1.getGirlId();
      NpcType var2 = NpcType.getNpcType(var1);
      if (var2 == null) {
         var2 = NpcType.JENNY;
      }

      try {
         Constructor var3 = var2.npcClass.getConstructor(World.class);
         this.c = (BaseGirlEntity)var3.newInstance(this.field_146297_k.field_71441_e);
         this.c.setLocallyRegistered(true);
      } catch (Exception var11) {
         var11.printStackTrace();
      }

      this.e_clash817();
      String var12 = var1.getCustomModelCode();
      this.c.func_184212_Q().func_187227_b(BaseGirlEntity.b, var12);
      int var4 = 0;

      for (String var6 : this.c.getCustomPartsSet()) {
         BoneType var7 = ServerWhitelistManager.e_clash138(var6);
         if (BoneType.CUSTOM_BONE.equals(var7)) {
            var4++;
         }

         Entry var8 = null;
         if (BoneType.CUSTOM_BONE.equals(var7) && var4 > 1) {
            var8 = b_clash816(this.c);
         } else {
            for (Entry var10 : m) {
               if (((BoneType)var10.getKey()).equals(var7)) {
                  var8 = var10;
               }
            }
         }

         if (var8 != null) {
            m.remove(var8);
            int var13 = ((List)((Entry)var8.getValue()).getKey()).indexOf(var6);
            if (var13 == -1) {
               var13 = 0;
            }

            ((Entry)var8.getValue()).setValue(var13);
            m.add(var8);
         }
      }
   }

   public void func_146274_d() {
      super.func_146274_d();
      this.q.func_178039_p();
   }

   public static HashSet<String> b_clash815() {
      HashSet var0 = new HashSet();

      for (Entry var2 : m) {
         if (((List)((Entry)var2.getValue()).getKey()).size() != 1) {
            Entry var3 = (Entry)var2.getValue();
            List var4 = (List)var3.getKey();
            Integer var5 = (Integer)var3.getValue();
            var0.add(var4.get(var5));
         }
      }

      return var0;
   }

   public static Entry<BoneType, Entry<List<String>, Integer>> b_clash816(BaseGirlEntity var0) {
      ArrayList var1 = new ArrayList();
      var1.add("cross");
      var1.addAll(ServerWhitelistManager.a_clash143(var0).get(BoneType.CUSTOM_BONE));
      return new SimpleEntry<>(BoneType.CUSTOM_BONE, new SimpleEntry<>(var1, 0));
   }

   void e_clash817() {
      m.clear();
      List var1 = this.c.d_clash556(this.g);
      this.i = var1.size();
      m.addAll(var1);

      for (BoneType var5 : BoneType.values()) {
         if (var5 != BoneType.GIRL_SPECIFIC) {
            ArrayList var6 = new ArrayList();
            var6.add("cross");
            m.add(new SimpleEntry<>(var5, new SimpleEntry<>(var6, 0)));
         }
      }

      for (Entry var8 : ServerWhitelistManager.a_clash143(this.c).entrySet()) {
         Entry var9 = null;

         for (Entry var12 : m) {
            if (((BoneType)var8.getKey()).equals(var12.getKey())) {
               var9 = var12;
            }
         }

         if (var9 != null) {
            int var11 = m.indexOf(var9);
            m.remove(var9);
            ((List)((Entry)var9.getValue()).getKey()).addAll((Collection)var8.getValue());
            m.add(var11, var9);
         }
      }
   }

   public void func_73866_w_() {
      this.q = new CustomModelList(this.field_146297_k, this);
   }

   public void func_146280_a(Minecraft var1, int var2, int var3) {
      super.func_146280_a(var1, var2, var3);
      this.n = this.a_clash821(76.0F);
      this.l = this.b_clash822(89.0F);
      this.o = 90.0F;
   }

   boolean a_clash818(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var1 < var3) {
         return false;
      } else if (var1 > var5) {
         return false;
      } else {
         return var2 < var4 ? false : var2 <= var6;
      }
   }

   public void func_73863_a(int var1, int var2, float var3) {
      super.func_73863_a(var1, var2, var3);
      if (this.p) {
         b = b + RotationHelper.lerp(h, s, var3);
      }

      this.a_clash824();
      this.field_146297_k.field_71446_o.func_110577_a(k);
      int var4 = this.n - this.a_clash821(15.0F);
      int var5 = this.l - 20;
      this.func_73729_b(var4, var5, 100, this.a_clash818(var1, var2, var4, var5, var4 + 20, var5 + 20) ? 40 : 20, 20, 20);
      if (ServerWhitelistManager.g_clash134() == null) {
         this.b(var4, var1, var2);
      }

      this.a(this.n, this.l, this.o, this.c, 1.2345679F);
      this.c.func_70071_h_();
      this.q.func_148128_a(var1, var2, var3);
   }

   void b(int var1, int var2, int var3) {
      int var4 = this.l - 40;
      this.func_73729_b(var1, var4, 120, this.a_clash818(var2, var3, var1, var4, var1 + 20, var4 + 20) ? 40 : 20, 20, 20);
      var4 -= 20;
      this.func_73729_b(var1, var4, 20, this.a_clash818(var2, var3, var1, var4, var1 + 20, var4 + 20) ? 170 : 150, 20, 20);
      var4 -= 20;
      this.func_73729_b(var1, var4, 0, this.a_clash818(var2, var3, var1, var4, var1 + 20, var4 + 20) ? 170 : 150, 20, 20);
   }

   public boolean func_73868_f() {
      return false;
   }

   void c_clash819() {
      this.field_146297_k.func_147118_V().func_147682_a(PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F));
      HashSet var1 = new HashSet();
      ArrayList var2 = new ArrayList();

      for (Entry var4 : m) {
         if (var4.getKey() == BoneType.GIRL_SPECIFIC) {
            var2.add(((Entry)var4.getValue()).getValue());
         } else {
            Entry var5 = (Entry)var4.getValue();
            Integer var6 = (Integer)var5.getValue();
            if (var6 != 0) {
               String var7 = (String)((List)var5.getKey()).get(var6);
               var1.add(var7);
            }
         }
      }

      PacketHandler.b.sendToServer(new UploadModelStringPacket(BaseGirlEntity.encodeCustomParts(var1), this.g, var2));
      this.field_146297_k.field_71439_g.func_71053_j();
   }

   public void a(BoneType var1, boolean var2, int var3) {
      this.field_146297_k.func_147118_V().func_147682_a(PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F));
      ArrayList var4 = new ArrayList();
      ArrayList var5 = new ArrayList();
      int var6 = 0;

      for (Entry var8 : m) {
         if (((BoneType)var8.getKey()).equals(var1)) {
            var4.add(var8);
            var5.add(var6);
         }

         var6++;
      }

      if (var4.size() != 0) {
         Entry var15;
         int var16;
         if (var4.size() == 1) {
            var15 = (Entry)var4.get(0);
            var16 = (Integer)var5.get(0);
         } else {
            int var9;
            if (this.i != 0 && var3 <= this.i - 1 + BoneType.a_clash759()) {
               var9 = var3;
            } else {
               var9 = var3 - (this.i + BoneType.a_clash759());
            }

            var15 = (Entry)var4.get(var9);
            var16 = (Integer)var5.get(var9);
         }

         if (var15 != null) {
            Entry var17 = (Entry)var15.getValue();
            int var10 = (Integer)var17.getValue();
            int var11 = ((List)var17.getKey()).size();
            if (var2) {
               if (++var10 >= var11) {
                  var10 = 0;
               }
            } else if (--var10 < 0) {
               var10 = var11 - 1;
            }

            m.set(var16, new SimpleEntry<>((BoneType)var15.getKey(), new SimpleEntry<>((List<String>)((Entry)var15.getValue()).getKey(), var10)));
            ArrayList var12 = new ArrayList();

            for (Entry var14 : m) {
               if (var14.getKey() == BoneType.GIRL_SPECIFIC) {
                  var12.add(var14);
               }
            }

            this.c.b(var12);
         }
      }
   }

   public void a(int var1, int var2, float var3, SexSceneEntity var4) {
      this.a(var1, var2, var3, var4, 1.876945F);
   }

   public void a_clash820(SexSceneEntity var1) {
      this.a(this.n, this.l, this.o, var1, 2.876945F, var1.f ? 1 : 0);
   }

   public void a(String var1, int var2, int var3) {
      this.func_146279_a(var1, var2, var3);
   }

   protected void func_146273_a(int var1, int var2, int var3, long var4) {
      super.func_146273_a(var1, var2, var3, var4);
      if (var3 == 0) {
         if (var1 >= this.field_146294_l / 2) {
            int var6 = var1 - this.t;
            a.add(var6);
            this.t = var1;
         }
      }
   }

   protected void func_73864_a(int var1, int var2, int var3) {
      super.func_73864_a(var1, var2, var3);
      this.q.func_148179_a(var1, var2, var3);
      if (var3 == 0) {
         this.f = true;
         this.p = true;
         this.t = var1;
         int var4 = this.n - this.a_clash821(15.0F);
         int var5 = this.l - 20;
         if (this.a_clash818(var1, var2, var4, var5, var4 + 20, var5 + 20)) {
            this.c_clash819();
         }

         if (ServerWhitelistManager.g_clash134() == null) {
            var5 = this.l - 40;
            if (this.a_clash818(var1, var2, var4, var5, var4 + 20, var5 + 20)) {
               this.field_146297_k.func_147118_V().func_147682_a(PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F));
               this.field_146297_k.field_71439_g.func_71053_j();
               int var6 = ServerWhitelistManager.b_clash126(true);
               if (var6 != 0) {
                  ServerWhitelistManager.d = true;
               } else {
                  BaseGirlEntity var7 = BaseGirlEntity.getClientGirlEntity(this.g);
                  if (var7 != null) {
                     a_clash825(var7);
                  }
               }
            } else {
               var5 -= 20;
               if (this.a_clash818(var1, var2, var4, var5, var4 + 20, var5 + 20)) {
                  try { Desktop.getDesktop().open(new File(ServerWhitelistManager.d_clash133())); } catch (IOException var9) { }
               } else {
                  var5 -= 20;
                  if (this.a_clash818(var1, var2, var4, var5, var4 + 20, var5 + 20)) {
                     try {
                        Desktop.getDesktop().browse(new URI("http://fapcraft.org/assets/video/tutorial/girl_wand.mp4"));
                     } catch (URISyntaxException var8) {
                        throw new RuntimeException(var8);
                     } catch (IOException var9) {
                        throw new RuntimeException(var9);
                     }
                  }
               }
            }
         }
      }
   }

   protected void func_146286_b(int var1, int var2, int var3) {
      super.func_146286_b(var1, var2, var3);
      if (var3 == 0) {
         this.p = false;
         this.f = false;
      }

      this.d = h;
   }

   int a_clash821(float var1) {
      return Math.round(this.field_146294_l * (var1 / 100.0F));
   }

   int b_clash822(float var1) {
      return Math.round(this.field_146295_m * (var1 / 100.0F));
   }

   public void func_146281_b() {
      super.func_146281_b();
      this.c.field_70170_p.func_72973_f(this.c);
      a.clear();
      m.clear();
   }

   public BaseGirlEntity d_clash823() {
      return this.c;
   }

   public void a(int var1, int var2, int var3, int var4) {
      this.field_146297_k.field_71446_o.func_110577_a(k);
      this.func_73729_b(var1, var2, var3, var4, 20, 20);
   }

   public void a(int var1, int var2, int var3) {
      this.a(var1, var2, var3, 0);
   }

   public void a(int var1, int var2, e1 var3) {
      this.a(var1, var2, var3.c, var3.b);
   }

   void a(int var1, int var2, float var3, EntityLivingBase var4, float var5) {
      this.a(var1, var2, var3, var4, var5, 0);
   }

   void a(int var1, int var2, float var3, EntityLivingBase var4, float var5, int var6) {
      float var7 = var4.field_70761_aq;
      float var8 = var4.field_70177_z;
      float var9 = var4.field_70125_A;
      float var10 = var4.field_70758_at;
      float var11 = var4.field_70759_as;
      var4.field_70761_aq = 0.0F;
      var4.field_70177_z = 0.0F;
      var4.field_70125_A = 0.0F;
      var4.field_70758_at = 0.0F;
      var4.field_70759_as = 0.0F;
      GlStateManager.func_179142_g();
      GlStateManager.func_179094_E();
      GlStateManager.func_179109_b(var1, var2, 50.0F);
      GlStateManager.func_179152_a(-var3, var3, var3);
      GlStateManager.func_179114_b(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.func_179114_b(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.func_74519_b();
      GlStateManager.func_179114_b(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179109_b(0.0F, 0.0F, var6);
      GlStateManager.func_179114_b(b, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(0.25F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179109_b(0.0F, 0.0F, 0.0F);
      RenderManager var12 = Minecraft.func_71410_x().func_175598_ae();
      var12.func_178631_a(180.0F);
      var12.func_178633_a(false);
      var12.func_188391_a(var4, 0.0, 0.0, 0.0, 0.0F, var5, false);
      var12.func_178633_a(true);
      GlStateManager.func_179121_F();
      RenderHelper.func_74518_a();
      GlStateManager.func_179101_C();
      GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
      GlStateManager.func_179090_x();
      GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
      var4.field_70761_aq = var7;
      var4.field_70177_z = var8;
      var4.field_70125_A = var9;
      var4.field_70758_at = var10;
      var4.field_70759_as = var11;
   }

   void a_clash824() {
      if (!this.p) {
         float var1 = Minecraft.func_175610_ah();
         if (var1 == 0.0F) {
            var1 = 0.1F;
         }

         if (this.d == 0) {
            b = b + this.e * 10 / var1;
         } else {
            b = b + this.d / var1;
            this.d = (int)(this.d * (1.0F - 0.25F / var1));
            if (Math.abs(this.d) <= 10) {
               this.e = this.d > 0 ? 1 : -1;
               this.d = 0;
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static void a_clash825(@Nonnull BaseGirlEntity var0) {
      Minecraft var1 = Minecraft.func_71410_x();
      if (!(var1.field_71462_r instanceof ClothingScreen)) {
         boolean var2 = ServerWhitelistManager.g_clash134() == null || ServerWhitelistManager.b_clash129();
         if (!var2) {
            var1.field_71439_g
               .func_146105_b(
                  new TextComponentString("You have to whitelist the server to use its custom models. " + TextFormatting.YELLOW + "/whitelistserver"), true
               );
         } else {
            var1.func_152344_a(() -> var1.func_147108_a(new ClothingScreen(var0)));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static class b {
      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void a(KeyInputEvent var1) {
         if (ClientProxy.keyBindings[1].func_151468_f()) {
            if (ServerWhitelistManager.d) {
               ServerWhitelistManager.d = 0 != ServerWhitelistManager.b_clash126(true);
               if (ServerWhitelistManager.d) {
                  return;
               }
            }

            Minecraft var2 = Minecraft.func_71410_x();
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.field_71439_g.getPersistentID());
            if (var3 == null) {
               var2.field_71439_g.func_146105_b(new TextComponentString("You have to turn into the girl you want to customize"), true);
            } else {
               ClothingScreen.a_clash825(var3);
            }
         }
      }

      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void a(ClientTickEvent var1) {
         ClothingScreen.h = ClothingScreen.s;
         ClothingScreen.s = 0;

         for (Integer var3 : ClothingScreen.a) {
            ClothingScreen.s = ClothingScreen.s + var3;
         }

         ClothingScreen.a.clear();
      }

   }
}
