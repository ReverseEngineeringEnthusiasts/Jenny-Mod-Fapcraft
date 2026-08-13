package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.client.renderer.DragonStaffRenderer;
import com.trolmastercard.sexmod.networking.CancelTaskPacket;
import com.trolmastercard.sexmod.networking.FallTreePacket;
import com.trolmastercard.sexmod.networking.MinePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.trolmastercard.sexmod.networking.SetTribeFollowModePacket;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.gm;







import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class StructureCommandScreen extends GuiScreen {
   static final float f = 100.0F;
   static final float g = 15.0F;
   static final float j = 0.5F;
   static final ResourceLocation h = new ResourceLocation("sexmod", "textures/gui/command.png");
   static final HashSet<Material> l = new HashSet<>(
      Arrays.asList(Material.CLAY, Material.ROCK, Material.SAND, Material.GROUND)
   );
   public static boolean d = false;
   float m = 0.0F;
   float a = 0.0F;
   float k = 0.0F;
   float n = 0.0F;
   float i = 0.0F;
   IBlockState e;
   BlockPos c;
   EnumFacing b;

   public StructureCommandScreen() {
      Minecraft var1 = Minecraft.getMinecraft();
      this.c = var1.objectMouseOver.getBlockPos();
      if (var1.objectMouseOver.sideHit == null) {
         this.b = EnumFacing.NORTH;
      } else {
         this.b = var1.objectMouseOver.sideHit.getOpposite();
      }

      if (this.c == null) {
         this.c = BlockPos.ORIGIN;
      }

      this.e = var1.world.getBlockState(this.c);
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      List var1 = Arrays.asList(this.a, this.k, this.n, this.i);
      float var2 = (Float) Collections.max((List<Float>) (List) var1);
      if (var2 != 0.0F) {
         if (this.a == var2) {
            this.b_clash832();
         }

         if (this.k == var2) {
            this.d_clash833();
         }

         if (this.n == var2) {
            this.c_clash834();
         }

         if (this.i == var2) {
            this.a_clash835();
         }
      }
   }

   void b_clash832() {
      IBlockState var1 = this.mc.world.getBlockState(this.c);
      if (var1.getBlock() instanceof BlockBed || var1.getBlock() instanceof BlockChest) {
         PacketHandler.b.sendToServer(new SendBlocksPacket(this.c, !gm.a_clash771(this.c)));
      }
   }

   void d_clash833() {
      PacketHandler.b.sendToServer(new SetTribeFollowModePacket(!d));
   }

   void c_clash834() {
      DragonStaffRenderer.a_clash632();
   }

   void a_clash835() {
      Block var1 = this.e.getBlock();
      if (var1 instanceof BlockLog) {
         if (gm.a_clash771(this.c)) {
            PacketHandler.b.sendToServer(new CancelTaskPacket(this.c));
            return;
         }

         PacketHandler.b.sendToServer(new FallTreePacket(this.c));
      }

      Object[] var2 = this.e_clash836();
      if (var2 != null) {
         if (gm.a_clash771(this.c)) {
            PacketHandler.b.sendToServer(new CancelTaskPacket(this.c));
            return;
         }

         PacketHandler.b.sendToServer(new MinePacket((BlockPos)var2[0], (EnumFacing)var2[1]));
      }
   }

   @Nullable
   Object[] e_clash836() {
      Material var1 = this.mc.world.getBlockState(this.c).getMaterial();
      EntityPlayerSP var2 = this.mc.player;
      if (!l.contains(var1)) {
         return null;
      }

      if (var2.getPosition().getY() > this.c.getY()) {
         return null;
      }

      BlockPos var3 = this.c;

      while (
         this.mc.world.getBlockState(var3.down().add(this.b.getOpposite().getDirectionVec())).getBlock()
            == Blocks.AIR
      ) {
         var3 = var3.down();
      }

      return this.c.getY() - var3.getY() > 3 ? null : new Object[]{var3, this.b};
   }

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      GL11.glEnable(3042);
      OpenGlHelper.glBlendFunc(770, 771, 1, 0);
      GL11.glBlendFunc(770, 771);

      try {
         this.m = Math.min(1.0F, this.m + this.mc.getTickLength() / 5.0F);
      } catch (NullPointerException var11) {
      }

      float var4 = (float)this.a_clash841(this.m);
      float var5 = (1.0F - var4) * 100.0F;
      this.a = this.a + (var1 < this.width / 2 && var2 > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.k = this.k + (var1 < this.width / 2 && var2 < this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.n = this.n + (var1 > this.width / 2 && var2 > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.i = this.i + (var1 > this.width / 2 && var2 < this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.a = ThreadNames.b(this.a, 0.0F, 1.0F);
      this.k = ThreadNames.b(this.k, 0.0F, 1.0F);
      this.n = ThreadNames.b(this.n, 0.0F, 1.0F);
      this.i = ThreadNames.b(this.i, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
      GlStateManager.scale(var4, var4, var4);
      this.mc.renderEngine.bindTexture(h);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.k * 0.5F, 1.0F + this.k * 0.5F, 1.0F);
      this.drawTexturedModalRect(-62.0F + var5 - this.k * 15.0F, -62.0F + var5 - this.k * 15.0F, 0, 0, 64, 64);
      this.c_clash838(var5);
      if (d) {
         this.drawTexturedModalRect(-62.0F + var5 - this.k * 15.0F, -62.0F + var5 - this.k * 15.0F, 128, 64, 64, 64);
      }

      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.n * 0.5F, 1.0F + this.n * 0.5F, 1.0F);
      this.drawTexturedModalRect(-2.0F - var5 + this.n * 15.0F, -2.0F - var5 + this.n * 15.0F, 0, 0, 64, 64);
      this.a_clash837(var5);
      if (DragonStaffRenderer.b_clash631()) {
         this.drawTexturedModalRect(-2.0F - var5 + this.n * 15.0F, -2.0F - var5 + this.n * 15.0F, 128, 64, 64, 64);
      }

      GlStateManager.popMatrix();
      Block var6 = this.e.getBlock();
      boolean var7 = var6 instanceof BlockChest;
      boolean var8 = var6 instanceof BlockBed;
      if (var7 || var8) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F + this.a * 0.5F, 1.0F + this.a * 0.5F, 1.0F);
         this.drawTexturedModalRect(-62.0F + var5 - this.a * 15.0F, -2.0F - var5 + this.a * 15.0F, 0, 0, 64, 64);
         if (var7) {
            this.d_clash840(var5);
         }

         if (var8) {
            this.f(var5);
         }

         if (gm.a_clash771(this.c)) {
            this.drawTexturedModalRect(-62.0F + var5 - this.a * 15.0F, -2.0F - var5 + this.a * 15.0F, 128, 64, 64, 64);
         }

         GlStateManager.popMatrix();
      }

      boolean var9 = var6 instanceof BlockLog;
      boolean var10 = this.e_clash836() != null;
      if (var9 || var10) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F + this.i * 0.5F, 1.0F + this.i * 0.5F, 1.0F);
         this.drawTexturedModalRect(-2.0F - var5 + this.i * 15.0F, -62.0F + var5 - this.i * 15.0F, 0, 0, 64, 64);
         if (var9) {
            this.e(var5);
         }

         if (var10) {
            this.b_clash839(var5);
         }

         if (gm.a_clash771(this.c)) {
            this.drawTexturedModalRect(-2.0F - var5 + this.i * 15.0F, -62.0F + var5 - this.i * 15.0F, 128, 64, 64, 64);
         }

         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GL11.glDisable(3042);
   }

   void a_clash837(float var1) {
      this.drawTexturedModalRect(-2.0F - var1 + this.n * 15.0F, -2.0F - var1 + this.n * 15.0F, 192, 64, 64, 64);
   }

   void c_clash838(float var1) {
      this.drawTexturedModalRect(-62.0F + var1 - this.k * 15.0F, -62.0F + var1 - this.k * 15.0F, 64, 64, 64, 64);
   }

   void e(float var1) {
      this.drawTexturedModalRect(-2.0F - var1 + this.i * 15.0F, -62.0F + var1 - this.i * 15.0F, 64, 0, 64, 64);
   }

   void b_clash839(float var1) {
      this.drawTexturedModalRect(-2.0F - var1 + this.i * 15.0F, -62.0F + var1 - this.i * 15.0F, 128, 0, 64, 64);
   }

   void f(float var1) {
      this.drawTexturedModalRect(-62.0F + var1 - this.a * 15.0F, -2.0F - var1 + this.a * 15.0F, 0, 64, 64, 64);
   }

   void d_clash840(float var1) {
      this.drawTexturedModalRect(-62.0F + var1 - this.a * 15.0F, -2.0F - var1 + this.a * 15.0F, 192, 0, 64, 64);
   }

   double a_clash841(double var1) {
      double var3 = 1.70158;
      double var5 = 2.70158;
      return 1.0 + var5 * Math.pow(var1 - 1.0, 3.0) + var3 * Math.pow(var1 - 1.0, 2.0);
   }

   protected void mouseReleased(int var1, int var2, int var3) {
      this.mc.player.closeScreen();
      super.mouseReleased(var1, var2, var3);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

}
