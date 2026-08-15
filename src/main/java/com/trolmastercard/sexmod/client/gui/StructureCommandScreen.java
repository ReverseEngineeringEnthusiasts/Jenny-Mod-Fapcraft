package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.client.renderer.DragonStaffRenderer;
import com.trolmastercard.sexmod.networking.CancelTaskPacket;
import com.trolmastercard.sexmod.networking.FallTreePacket;
import com.trolmastercard.sexmod.networking.MinePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.trolmastercard.sexmod.networking.SetTribeFollowModePacket;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.StructureMarkerRenderer;
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

/**
 * Direction-pad command screen for the dragon staff: while the player holds
 * the mouse in one of four quadrants and releases (closes the screen), a
 * context-dependent command fires — bottom-left = mark/unmark the targeted
 * chest/bed ({@link SendBlocksPacket}), top-left = toggle tribe follow mode
 * ({@link SetTribeFollowModePacket}), bottom-right = toggle the staff's
 * rendering mode ({@link DragonStaffRenderer#toggleStaffRendering()}),
 * top-right = fell the targeted log / mine the targeted breakable block
 * ({@link FallTreePacket} / {@link MinePacket} / {@link CancelTaskPacket}).
 * <p>
 * Which quadrants are offered depends on the looked-at block: chest/bed
 * enable the mark command, logs and breakable materials (clay/rock/sand/
 * ground, within 3 blocks of ground support and below the player) enable the
 * mine command. Closing the screen commits the action with the highest
 * quadrant accumulation — must fire exactly once.
 * <p>
 * CLIENT-side only; any mouse release closes the screen.
 */
public class StructureCommandScreen extends GuiScreen {
   static final float SIZE_100 = 100.0F;
   static final float OFFSET_15 = 15.0F;
   static final float SCALE_0_5 = 0.5F;
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/command.png");
   static final HashSet<Material> breakableMaterials = new HashSet<>(
      Arrays.asList(Material.CLAY, Material.ROCK, Material.SAND, Material.GROUND)
   );
   public static boolean isErasing = false;
   float animProgress = 0.0F;
   float animBottomLeft = 0.0F;
   float animTopLeft = 0.0F;
   float animBottomRight = 0.0F;
   float animTopRight = 0.0F;
   IBlockState targetBlockState;
   BlockPos targetBlockPos;
   EnumFacing targetFacing;

   public StructureCommandScreen() {
      Minecraft var1 = Minecraft.getMinecraft();
      this.targetBlockPos = var1.objectMouseOver.getBlockPos();
      if (var1.objectMouseOver.sideHit == null) {
         this.targetFacing = EnumFacing.NORTH;
      } else {
         this.targetFacing = var1.objectMouseOver.sideHit.getOpposite();
      }

      if (this.targetBlockPos == null) {
         this.targetBlockPos = BlockPos.ORIGIN;
      }

      this.targetBlockState = var1.world.getBlockState(this.targetBlockPos);
   }

   /**
    * Commits the command of the most-accumulated quadrant (see class javadoc).
    * No-op when the player closed without holding a quadrant.
    */
   public void onGuiClosed() {
      super.onGuiClosed();
      List var1 = Arrays.asList(this.animBottomLeft, this.animTopLeft, this.animBottomRight, this.animTopRight);
      float var2 = (Float) Collections.max((List<Float>) (List) var1);
      if (var2 != 0.0F) {
         if (this.animBottomLeft == var2) {
            this.updateTargetState();
         }

         if (this.animTopLeft == var2) {
            this.toggleFollowMode();
         }

         if (this.animBottomRight == var2) {
            this.toggleStaffView();
         }

         if (this.animTopRight == var2) {
            this.handleLogBlock();
         }
      }
   }

   /**
    * Toggles the mark state of the targeted chest/bed: sends
    * {@link SendBlocksPacket} with the inverse of the current
    * {@link StructureMarkerRenderer} mark. Only valid for beds and chests.
    */
   void updateTargetState() {
      IBlockState var1 = this.mc.world.getBlockState(this.targetBlockPos);
      if (var1.getBlock() instanceof BlockBed || var1.getBlock() instanceof BlockChest) {
         PacketHandler.networkWrapper.sendToServer(new SendBlocksPacket(this.targetBlockPos, !StructureMarkerRenderer.isMarked(this.targetBlockPos)));
      }
   }

   /**
    * Sends {@link SetTribeFollowModePacket} with the inverted current erasing
    * flag ({@code isErasing}).
    */
   void toggleFollowMode() {
      PacketHandler.networkWrapper.sendToServer(new SetTribeFollowModePacket(!isErasing));
   }

   /**
    * Toggles the dragon staff's renderer mode ({@link DragonStaffRenderer}).
    */
   void toggleStaffView() {
      DragonStaffRenderer.toggleStaffRendering();
   }

   /**
    * Log command: for a targeted log, sends {@link FallTreePacket}, or
    * {@link CancelTaskPacket} when the position is already marked; same for a
    * minable material via {@link MinePacket}. Exactly one packet per close.
    */
   void handleLogBlock() {
      Block var1 = this.targetBlockState.getBlock();
      if (var1 instanceof BlockLog) {
         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            PacketHandler.networkWrapper.sendToServer(new CancelTaskPacket(this.targetBlockPos));
            return;
         }

         PacketHandler.networkWrapper.sendToServer(new FallTreePacket(this.targetBlockPos));
      }

      Object[] var2 = this.getTargetMaterial();
      if (var2 != null) {
         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            PacketHandler.networkWrapper.sendToServer(new CancelTaskPacket(this.targetBlockPos));
            return;
         }

         PacketHandler.networkWrapper.sendToServer(new MinePacket((BlockPos)var2[0], (EnumFacing)var2[1]));
      }
   }

   /**
    * Resolves the mining target for the looked-at block.
    *
    * @return {@code [groundPos, facing]} if the target material is breakable,
    *         the player is at/below the target, and the ground support is at
    *         most 3 blocks below; otherwise {@code null}
    */
   @Nullable
   Object[] getTargetMaterial() {
      Material var1 = this.mc.world.getBlockState(this.targetBlockPos).getMaterial();
      EntityPlayerSP var2 = this.mc.player;
      if (!breakableMaterials.contains(var1)) {
         return null;
      }

      if (var2.getPosition().getY() > this.targetBlockPos.getY()) {
         return null;
      }

      BlockPos var3 = this.targetBlockPos;

      while (
         this.mc.world.getBlockState(var3.down().add(this.targetFacing.getOpposite().getDirectionVec())).getBlock()
            == Blocks.AIR
      ) {
         var3 = var3.down();
      }

      return this.targetBlockPos.getY() - var3.getY() > 3 ? null : new Object[]{var3, this.targetFacing};
   }

   /**
    * Draws the four quadrant buttons with entrance animation and per-quadrant
    * hover accumulation (the values {@link #onGuiClosed()} reads). Only the
    * quadrants valid for the targeted block are offered, and active states
    * (erasing/staff rendering/marked) overlay a highlight icon.
    */
   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      GL11.glEnable(3042);
      OpenGlHelper.glBlendFunc(770, 771, 1, 0);
      GL11.glBlendFunc(770, 771);

      try {
         this.animProgress = Math.min(1.0F, this.animProgress + this.mc.getTickLength() / 5.0F);
      } catch (NullPointerException var11) {
      }

      float var4 = (float)this.easeOutBack(this.animProgress);
      float var5 = (1.0F - var4) * 100.0F;
      this.animBottomLeft = this.animBottomLeft + (var1 < this.width / 2 && var2 > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animTopLeft = this.animTopLeft + (var1 < this.width / 2 && var2 < this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animBottomRight = this.animBottomRight + (var1 > this.width / 2 && var2 > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animTopRight = this.animTopRight + (var1 > this.width / 2 && var2 < this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animBottomLeft = ThreadNames.clampFloat(this.animBottomLeft, 0.0F, 1.0F);
      this.animTopLeft = ThreadNames.clampFloat(this.animTopLeft, 0.0F, 1.0F);
      this.animBottomRight = ThreadNames.clampFloat(this.animBottomRight, 0.0F, 1.0F);
      this.animTopRight = ThreadNames.clampFloat(this.animTopRight, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
      GlStateManager.scale(var4, var4, var4);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.animTopLeft * 0.5F, 1.0F + this.animTopLeft * 0.5F, 1.0F);
      this.drawTexturedModalRect(-62.0F + var5 - this.animTopLeft * 15.0F, -62.0F + var5 - this.animTopLeft * 15.0F, 0, 0, 64, 64);
      this.drawTopLeft(var5);
      if (isErasing) {
         this.drawTexturedModalRect(-62.0F + var5 - this.animTopLeft * 15.0F, -62.0F + var5 - this.animTopLeft * 15.0F, 128, 64, 64, 64);
      }

      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.animBottomRight * 0.5F, 1.0F + this.animBottomRight * 0.5F, 1.0F);
      this.drawTexturedModalRect(-2.0F - var5 + this.animBottomRight * 15.0F, -2.0F - var5 + this.animBottomRight * 15.0F, 0, 0, 64, 64);
      this.drawBottomRight(var5);
      if (DragonStaffRenderer.isRenderingStaff()) {
         this.drawTexturedModalRect(-2.0F - var5 + this.animBottomRight * 15.0F, -2.0F - var5 + this.animBottomRight * 15.0F, 128, 64, 64, 64);
      }

      GlStateManager.popMatrix();
      Block var6 = this.targetBlockState.getBlock();
      boolean var7 = var6 instanceof BlockChest;
      boolean var8 = var6 instanceof BlockBed;
      if (var7 || var8) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F + this.animBottomLeft * 0.5F, 1.0F + this.animBottomLeft * 0.5F, 1.0F);
         this.drawTexturedModalRect(-62.0F + var5 - this.animBottomLeft * 15.0F, -2.0F - var5 + this.animBottomLeft * 15.0F, 0, 0, 64, 64);
         if (var7) {
            this.drawBottomLeft(var5);
         }

         if (var8) {
            this.setBuildProgress(var5);
         }

         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            this.drawTexturedModalRect(-62.0F + var5 - this.animBottomLeft * 15.0F, -2.0F - var5 + this.animBottomLeft * 15.0F, 128, 64, 64, 64);
         }

         GlStateManager.popMatrix();
      }

      boolean var9 = var6 instanceof BlockLog;
      boolean var10 = this.getTargetMaterial() != null;
      if (var9 || var10) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F + this.animTopRight * 0.5F, 1.0F + this.animTopRight * 0.5F, 1.0F);
         this.drawTexturedModalRect(-2.0F - var5 + this.animTopRight * 15.0F, -62.0F + var5 - this.animTopRight * 15.0F, 0, 0, 64, 64);
         if (var9) {
            this.setEraseProgress(var5);
         }

         if (var10) {
            this.drawTopRight(var5);
         }

         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            this.drawTexturedModalRect(-2.0F - var5 + this.animTopRight * 15.0F, -62.0F + var5 - this.animTopRight * 15.0F, 128, 64, 64, 64);
         }

         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GL11.glDisable(3042);
   }

   void drawBottomRight(float var1) {
      this.drawTexturedModalRect(-2.0F - var1 + this.animBottomRight * 15.0F, -2.0F - var1 + this.animBottomRight * 15.0F, 192, 64, 64, 64);
   }

   void drawTopLeft(float var1) {
      this.drawTexturedModalRect(-62.0F + var1 - this.animTopLeft * 15.0F, -62.0F + var1 - this.animTopLeft * 15.0F, 64, 64, 64, 64);
   }

   void setEraseProgress(float var1) {
      this.drawTexturedModalRect(-2.0F - var1 + this.animTopRight * 15.0F, -62.0F + var1 - this.animTopRight * 15.0F, 64, 0, 64, 64);
   }

   void drawTopRight(float var1) {
      this.drawTexturedModalRect(-2.0F - var1 + this.animTopRight * 15.0F, -62.0F + var1 - this.animTopRight * 15.0F, 128, 0, 64, 64);
   }

   void setBuildProgress(float var1) {
      this.drawTexturedModalRect(-62.0F + var1 - this.animBottomLeft * 15.0F, -2.0F - var1 + this.animBottomLeft * 15.0F, 0, 64, 64, 64);
   }

   void drawBottomLeft(float var1) {
      this.drawTexturedModalRect(-62.0F + var1 - this.animBottomLeft * 15.0F, -2.0F - var1 + this.animBottomLeft * 15.0F, 192, 0, 64, 64);
   }

   double easeOutBack(double var1) {
      double var3 = 1.70158;
      double var5 = 2.70158;
      return 1.0 + var5 * Math.pow(var1 - 1.0, 3.0) + var3 * Math.pow(var1 - 1.0, 2.0);
   }

   /**
    * Any mouse release closes the screen, committing the held quadrant's
    * command ({@link #onGuiClosed()}).
    */
   protected void mouseReleased(int var1, int var2, int var3) {
      this.mc.player.closeScreen();
      super.mouseReleased(var1, var2, var3);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

}
