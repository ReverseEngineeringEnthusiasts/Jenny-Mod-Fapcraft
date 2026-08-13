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
      this.mc = Minecraft.getMinecraft();
      this.g = var1.getGirlId();
      NpcType var2 = NpcType.getNpcType(var1);
      if (var2 == null) {
         var2 = NpcType.JENNY;
      }

      try {
         Constructor var3 = var2.npcClass.getConstructor(World.class);
         this.c = (BaseGirlEntity)var3.newInstance(this.mc.world);
         this.c.setLocallyRegistered(true);
      } catch (Exception var11) {
         var11.printStackTrace();
      }

      this.e_clash817();
      String var12 = var1.getCustomModelCode();
      this.c.getDataManager().set(BaseGirlEntity.CUSTOM_MODEL_KEY, var12);
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

   public void handleMouseInput() {
      super.handleMouseInput();
      this.q.handleMouseInput();
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
      List var1 = this.c.buildCustomPartsData(this.g);
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

   public void initGui() {
      this.q = new CustomModelList(this.mc, this);
   }

   public void setWorldAndResolution(Minecraft var1, int var2, int var3) {
      super.setWorldAndResolution(var1, var2, var3);
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

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      if (this.p) {
         b = b + RotationHelper.lerp(h, s, var3);
      }

      this.a_clash824();
      this.mc.renderEngine.bindTexture(k);
      int var4 = this.n - this.a_clash821(15.0F);
      int var5 = this.l - 20;
      this.drawTexturedModalRect(var4, var5, 100, this.a_clash818(var1, var2, var4, var5, var4 + 20, var5 + 20) ? 40 : 20, 20, 20);
      if (ServerWhitelistManager.g_clash134() == null) {
         this.b(var4, var1, var2);
      }

      this.a(this.n, this.l, this.o, this.c, 1.2345679F);
      this.c.onUpdate();
      this.q.drawScreen(var1, var2, var3);
   }

   void b(int var1, int var2, int var3) {
      int var4 = this.l - 40;
      this.drawTexturedModalRect(var1, var4, 120, this.a_clash818(var2, var3, var1, var4, var1 + 20, var4 + 20) ? 40 : 20, 20, 20);
      var4 -= 20;
      this.drawTexturedModalRect(var1, var4, 20, this.a_clash818(var2, var3, var1, var4, var1 + 20, var4 + 20) ? 170 : 150, 20, 20);
      var4 -= 20;
      this.drawTexturedModalRect(var1, var4, 0, this.a_clash818(var2, var3, var1, var4, var1 + 20, var4 + 20) ? 170 : 150, 20, 20);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   void c_clash819() {
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
      this.mc.player.closeScreen();
   }

   public void a(BoneType var1, boolean var2, int var3) {
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
      this.drawHoveringText(var1, var2, var3);
   }

   protected void mouseClickMove(int var1, int var2, int var3, long var4) {
      super.mouseClickMove(var1, var2, var3, var4);
      if (var3 == 0) {
         if (var1 >= this.width / 2) {
            int var6 = var1 - this.t;
            a.add(var6);
            this.t = var1;
         }
      }
   }

   protected void mouseClicked(int var1, int var2, int var3) {
      super.mouseClicked(var1, var2, var3);
      this.q.mouseClicked(var1, var2, var3);
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
               this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               this.mc.player.closeScreen();
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

   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
      if (var3 == 0) {
         this.p = false;
         this.f = false;
      }

      this.d = h;
   }

   int a_clash821(float var1) {
      return Math.round(this.width * (var1 / 100.0F));
   }

   int b_clash822(float var1) {
      return Math.round(this.height * (var1 / 100.0F));
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      this.c.world.removeEntityDangerously(this.c);
      a.clear();
      m.clear();
   }

   public BaseGirlEntity d_clash823() {
      return this.c;
   }

   public void a(int var1, int var2, int var3, int var4) {
      this.mc.renderEngine.bindTexture(k);
      this.drawTexturedModalRect(var1, var2, var3, var4, 20, 20);
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
      float var7 = var4.renderYawOffset;
      float var8 = var4.rotationYaw;
      float var9 = var4.rotationPitch;
      float var10 = var4.prevRotationYawHead;
      float var11 = var4.rotationYawHead;
      var4.renderYawOffset = 0.0F;
      var4.rotationYaw = 0.0F;
      var4.rotationPitch = 0.0F;
      var4.prevRotationYawHead = 0.0F;
      var4.rotationYawHead = 0.0F;
      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate(var1, var2, 50.0F);
      GlStateManager.scale(-var3, var3, var3);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, var6);
      GlStateManager.rotate(b, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(0.25F, 1.0F, 0.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, 0.0F);
      RenderManager var12 = Minecraft.getMinecraft().getRenderManager();
      var12.setPlayerViewY(180.0F);
      var12.setRenderShadow(false);
      var12.renderEntity(var4, 0.0, 0.0, 0.0, 0.0F, var5, false);
      var12.setRenderShadow(true);
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      var4.renderYawOffset = var7;
      var4.rotationYaw = var8;
      var4.rotationPitch = var9;
      var4.prevRotationYawHead = var10;
      var4.rotationYawHead = var11;
   }

   void a_clash824() {
      if (!this.p) {
         float var1 = Minecraft.getDebugFPS();
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
      Minecraft var1 = Minecraft.getMinecraft();
      if (!(var1.currentScreen instanceof ClothingScreen)) {
         boolean var2 = ServerWhitelistManager.g_clash134() == null || ServerWhitelistManager.b_clash129();
         if (!var2) {
            var1.player
               .sendStatusMessage(
                  new TextComponentString("You have to whitelist the server to use its custom models. " + TextFormatting.YELLOW + "/whitelistserver"), true
               );
         } else {
            var1.addScheduledTask(() -> var1.displayGuiScreen(new ClothingScreen(var0)));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static class b {
      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void a(KeyInputEvent var1) {
         if (ClientProxy.keyBindings[1].isPressed()) {
            if (ServerWhitelistManager.d) {
               ServerWhitelistManager.d = 0 != ServerWhitelistManager.b_clash126(true);
               if (ServerWhitelistManager.d) {
                  return;
               }
            }

            Minecraft var2 = Minecraft.getMinecraft();
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.player.getPersistentID());
            if (var3 == null) {
               var2.player.sendStatusMessage(new TextComponentString("You have to turn into the girl you want to customize"), true);
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
