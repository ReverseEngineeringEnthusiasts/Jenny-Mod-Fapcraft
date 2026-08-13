package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.RemoveItemsPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;







import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GirlInventoryScreen extends GuiScreen {
   final BaseGirlEntity g;
   final EntityPlayer i;
   final String[] h;
   @Nullable
   final ItemStack[] f;
   static final ResourceLocation c = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   EntityDataManager l;
   final boolean k;
   float m = 0.0F;
   float n = 0.0F;
   String[] a = new String[]{"action.names.followme", "action.names.stopfollowme", "action.names.gohome", "action.names.setnewhome", "action.names.equipment"};
   int[] d = new int[]{0, 0, 0, 0, 0};
   int[] j = new int[]{64, 80, 47, 32, 96};
   int[] b = new int[]{4, 4, 5, 5, 4};
   int[] e = new int[]{50, 90, 50, 80, 60};

   public GirlInventoryScreen(BaseGirlEntity var1, EntityPlayer var2) {
      this.g = var1;
      this.i = var2;
      this.h = new String[0];
      this.f = new ItemStack[0];
      this.k = true;
      this.l = var1.getDataManager();
   }

   public GirlInventoryScreen(BaseGirlEntity var1, EntityPlayer var2, String[] var3, @Nullable ItemStack[] var4, boolean var5) {
      this.g = var1;
      this.i = var2;
      this.h = var3;
      this.f = var4;
      this.k = var5;
      this.l = var1.getDataManager();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void onGuiClosed() {
      super.onGuiClosed();
      this.g.ac();
   }

   protected void actionPerformed(GuiButton var1) {
      if (var1.id >= 5 && this.f != null && this.f[var1.id - 5] != null && !this.i.capabilities.isCreativeMode) {
         for (ItemStack var3 : this.i.inventory.mainInventory) {
            if (var3.getItem().equals(this.f[var1.id - 5].getItem())
               && var3.getCount() >= this.f[var1.id - 5].getCount()
               && var3.getMetadata() == this.f[var1.id - 5].getMetadata()) {
               PacketHandler.b.sendToServer(new RemoveItemsPacket(this.i.getPersistentID(), this.f[var1.id - 5]));
               this.a(var1);
               return;
            }
         }

         this.i.sendMessage(new TextComponentString("<" + this.g.getName() + "> you cannot afford that..."));
         this.g.playSound(SoundHandler.GIRLS_JENNY_SADOH[1]);
      } else {
         this.a(var1);
      }
   }

   void a(GuiButton var1) {
      String var2;
      if (var1.id < 5) {
         var2 = this.a[var1.id];
      } else {
         var2 = this.h[var1.id - 5];
      }

      this.g.doAction(var2, this.i.getPersistentID());
      Minecraft.getMinecraft().player.closeScreen();
   }

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      this.buttonList.clear();
      ScaledResolution var4 = new ScaledResolution(this.mc);
      int var5 = var4.getScaledWidth();
      int var6 = var4.getScaledHeight();
      this.m = Math.min(1.0F, this.m + this.mc.getTickLength() / 5.0F);
      if (this.m == 1.0F) {
         this.n = Math.min(1.0F, this.n + this.mc.getTickLength() / 5.0F);
      }

      int var7 = (int)RotationHelper.lerp(115.0F, 161.0F, this.n);
      int var8 = (int)RotationHelper.lerp(91.0F, 137.0F, this.n);
      int var9 = (int)RotationHelper.lerp(-30.0F, 120.0F, this.m);
      byte var10 = 70;
      byte var11 = 52;
      byte var12 = 68;

      for (int var13 = 5; var13 < this.h.length + 5; var13++) {
         if (this.n > 0.0F && this.f != null && this.f[var13 - 5] != null && this.f[var13 - 5].getCount() != 0) {
            this.zLevel = -300.0F;
            this.itemRender.zLevel = -300.0F;
            this.a(Arrays.asList(this.f[var13 - 5].getCount() + "x    "), var5 - var7, var6 - var11, this.fontRenderer);
            this.itemRender.renderItemIntoGUI(this.f[var13 - 5], var5 - var8, var6 - var12);
            this.zLevel = 0.0F;
            this.itemRender.zLevel = 0.0F;
         }

         this.buttonList.add(new GuiButton(var13, var5 - var9, var6 - var10, 100, 20, I18n.format(this.h[var13 - 5], new Object[0])));
         var10 += 30;
         var11 += 30;
         var12 += 30;
      }

      if (this.k) {
         this.a_clash826(var1, var2);
      }
   }

   void a_clash826(int var1, int var2) {
      int var3 = (int)RotationHelper.lerp(-30.0F, 120.0F, this.m);
      this.itemRender.renderItemIntoGUI((ItemStack)this.l.get(AbstractGirlNpcEntity.L), var3 - 105, 68);
      this.itemRender.renderItemIntoGUI((ItemStack)this.l.get(AbstractGirlNpcEntity.R), var3 - 105, 87);
      this.itemRender.renderItemIntoGUI((ItemStack)this.l.get(AbstractGirlNpcEntity.X), var3 - 105, 109);
      this.itemRender.renderItemIntoGUI((ItemStack)this.l.get(AbstractGirlNpcEntity.T), var3 - 105, 127);
      this.itemRender.renderItemIntoGUI((ItemStack)this.l.get(AbstractGirlNpcEntity.U), var3 - 105, 146);
      this.itemRender.renderItemIntoGUI((ItemStack)this.l.get(AbstractGirlNpcEntity.W), var3 - 105, 166);
      if (this.n != 0.0F) {
         if (!((String)this.l.get(BaseGirlEntity.MASTER)).equals("")) {
            byte var10 = 35;
            byte var11 = 70;

            for (int var12 = 0; var12 < 5; var12++) {
               if (var12 == 0) {
                  var12 = 1;
               } else if (var12 == 1) {
               }

               if (var1 >= var10 && var1 <= 58 + this.d[var12] && var2 >= var11 && var2 <= var11 + 20) {
                  this.d[var12] = Math.min(this.e[var12], this.d[var12] + 7);
               } else {
                  this.d[var12] = Math.max(0, this.d[var12] - 7);
               }

               StringBuilder var13 = new StringBuilder(I18n.format(this.a[var12], new Object[0]));

               for (int var14 = 0; var14 < this.b[var12]; var14++) {
                  var13.append(" ");
               }

               this.mc.renderEngine.bindTexture(c);
               this.drawTexturedModalRect(this.d[var12] + var10 - 18 + (int)RotationHelper.lerp(0.0F, 23.0F, this.n), var11 + 2, this.j[var12], 0, 16, 16);
               this.buttonList.add(new GuiButton(var12, 36, var11, 100, 20, var13.toString()));
               var11 += 30;
            }

            this.mc.renderEngine.bindTexture(c);
            this.drawTexturedModalRect(var3 - 113, 60, 0, 0, 32, 130);
         } else {
            byte var5 = 35;
            byte var6 = 70;

            for (int var7 = 0; var7 < 5; var7++) {
               if (var7 == 0) {
               }

               if (var7 == 1) {
                  var7 = 2;
               }

               if (var1 >= var5 && var1 <= 58 + this.d[var7] && var2 >= var6 && var2 <= var6 + 20) {
                  this.d[var7] = Math.min(this.e[var7], this.d[var7] + 7);
               } else {
                  this.d[var7] = Math.max(0, this.d[var7] - 7);
               }

               StringBuilder var8 = new StringBuilder(I18n.format(this.a[var7], new Object[0]));

               for (int var9 = 0; var9 < this.b[var7]; var9++) {
                  var8.append(" ");
               }

               this.mc.renderEngine.bindTexture(c);
               this.drawTexturedModalRect(this.d[var7] + var5 - 18 + (int)RotationHelper.lerp(0.0F, 23.0F, this.n), var6 + 2, this.j[var7], 0, 16, 16);
               this.buttonList.add(new GuiButton(var7, 36, var6, 100, 20, var8.toString()));
               var6 += 30;
            }

            this.mc.renderEngine.bindTexture(c);
            this.drawTexturedModalRect(var3 - 113, 60, 0, 0, 32, 130);
         }
      }
   }

   void a(List<String> var1, int var2, int var3, FontRenderer var4) {
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      int var5 = 0;

      for (String var7 : var1) {
         int var8 = this.fontRenderer.getStringWidth(var7);
         if (var8 > var5) {
            var5 = var8;
         }
      }

      int var11 = var2 + 12;
      int var12 = var3 - 12;
      int var13 = 8;
      if (var1.size() > 1) {
         var13 += 2 + (var1.size() - 1) * 10;
      }

      if (var11 + var5 > this.width) {
         var11 -= 28 + var5;
      }

      if (var12 + var13 + 6 > this.height) {
         var12 = this.height - var13 - 6;
      }

      this.drawGradientRect(var11 - 3, var12 - 4, var11 + var5 + 3, var12 - 3, -267386864, -267386864);
      this.drawGradientRect(var11 - 3, var12 + var13 + 3, var11 + var5 + 3, var12 + var13 + 4, -267386864, -267386864);
      this.drawGradientRect(var11 - 3, var12 - 3, var11 + var5 + 3, var12 + var13 + 3, -267386864, -267386864);
      this.drawGradientRect(var11 - 4, var12 - 3, var11 - 3, var12 + var13 + 3, -267386864, -267386864);
      this.drawGradientRect(var11 + var5 + 3, var12 - 3, var11 + var5 + 4, var12 + var13 + 3, -267386864, -267386864);
      this.drawGradientRect(var11 - 3, var12 - 3 + 1, var11 - 3 + 1, var12 + var13 + 3 - 1, 1347420415, 1344798847);
      this.drawGradientRect(var11 + var5 + 2, var12 - 3 + 1, var11 + var5 + 3, var12 + var13 + 3 - 1, 1347420415, 1344798847);
      this.drawGradientRect(var11 - 3, var12 - 3, var11 + var5 + 3, var12 - 3 + 1, 1347420415, 1347420415);
      this.drawGradientRect(var11 - 3, var12 + var13 + 2, var11 + var5 + 3, var12 + var13 + 3, 1344798847, 1344798847);

      for (int var9 = 0; var9 < var1.size(); var9++) {
         String var10 = (String)var1.get(var9);
         this.fontRenderer.drawStringWithShadow(var10, var11, var12, -1);
         if (var9 == 0) {
            var12 += 2;
         }

         var12 += 10;
      }

      GlStateManager.enableLighting();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enableRescaleNormal();
   }

}
