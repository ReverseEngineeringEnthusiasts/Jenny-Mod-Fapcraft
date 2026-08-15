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
      Minecraft mc = Minecraft.getMinecraft();
      this.targetBlockPos = mc.objectMouseOver.getBlockPos();
      if (mc.objectMouseOver.sideHit == null) {
         this.targetFacing = EnumFacing.NORTH;
      } else {
         this.targetFacing = mc.objectMouseOver.sideHit.getOpposite();
      }

      if (this.targetBlockPos == null) {
         this.targetBlockPos = BlockPos.ORIGIN;
      }

      this.targetBlockState = mc.world.getBlockState(this.targetBlockPos);
   }

   /**
    * Commits the command of the most-accumulated quadrant (see class javadoc).
    * No-op when the player closed without holding a quadrant.
    */
   public void onGuiClosed() {
      super.onGuiClosed();
      List animations = Arrays.asList(this.animBottomLeft, this.animTopLeft, this.animBottomRight, this.animTopRight);
      float maxAnim = (Float) Collections.max((List<Float>) (List) animations);
      if (maxAnim != 0.0F) {
         if (this.animBottomLeft == maxAnim) {
            this.updateTargetState();
         }

         if (this.animTopLeft == maxAnim) {
            this.toggleFollowMode();
         }

         if (this.animBottomRight == maxAnim) {
            this.toggleStaffView();
         }

         if (this.animTopRight == maxAnim) {
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
      IBlockState state = this.mc.world.getBlockState(this.targetBlockPos);
      if (state.getBlock() instanceof BlockBed || state.getBlock() instanceof BlockChest) {
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
      Block block = this.targetBlockState.getBlock();
      if (block instanceof BlockLog) {
         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            PacketHandler.networkWrapper.sendToServer(new CancelTaskPacket(this.targetBlockPos));
            return;
         }

         PacketHandler.networkWrapper.sendToServer(new FallTreePacket(this.targetBlockPos));
      }

      Object[] materialTarget = this.getTargetMaterial();
      if (materialTarget != null) {
         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            PacketHandler.networkWrapper.sendToServer(new CancelTaskPacket(this.targetBlockPos));
            return;
         }

         PacketHandler.networkWrapper.sendToServer(new MinePacket((BlockPos)materialTarget[0], (EnumFacing)materialTarget[1]));
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
      Material material = this.mc.world.getBlockState(this.targetBlockPos).getMaterial();
      EntityPlayerSP player = this.mc.player;
      if (!breakableMaterials.contains(material)) {
         return null;
      }

      if (player.getPosition().getY() > this.targetBlockPos.getY()) {
         return null;
      }

      BlockPos groundPos = this.targetBlockPos;

      while (
         this.mc.world.getBlockState(groundPos.down().add(this.targetFacing.getOpposite().getDirectionVec())).getBlock()
            == Blocks.AIR
      ) {
         groundPos = groundPos.down();
      }

      return this.targetBlockPos.getY() - groundPos.getY() > 3 ? null : new Object[]{groundPos, this.targetFacing};
   }

   /**
    * Draws the four quadrant buttons with entrance animation and per-quadrant
    * hover accumulation (the values {@link #onGuiClosed()} reads). Only the
    * quadrants valid for the targeted block are offered, and active states
    * (erasing/staff rendering/marked) overlay a highlight icon.
    */
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      GL11.glEnable(3042);
      OpenGlHelper.glBlendFunc(770, 771, 1, 0);
      GL11.glBlendFunc(770, 771);

      try {
         this.animProgress = Math.min(1.0F, this.animProgress + this.mc.getTickLength() / 5.0F);
      } catch (NullPointerException npe) {
      }

      float scale = (float)this.easeOutBack(this.animProgress);
      float offset = (1.0F - scale) * 100.0F;
      this.animBottomLeft = this.animBottomLeft + (mouseX < this.width / 2 && mouseY > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animTopLeft = this.animTopLeft + (mouseX < this.width / 2 && mouseY < this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animBottomRight = this.animBottomRight + (mouseX > this.width / 2 && mouseY > this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animTopRight = this.animTopRight + (mouseX > this.width / 2 && mouseY < this.height / 2 ? 1 : -1) * this.mc.getTickLength();
      this.animBottomLeft = ThreadNames.clampFloat(this.animBottomLeft, 0.0F, 1.0F);
      this.animTopLeft = ThreadNames.clampFloat(this.animTopLeft, 0.0F, 1.0F);
      this.animBottomRight = ThreadNames.clampFloat(this.animBottomRight, 0.0F, 1.0F);
      this.animTopRight = ThreadNames.clampFloat(this.animTopRight, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
      GlStateManager.scale(scale, scale, scale);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.animTopLeft * 0.5F, 1.0F + this.animTopLeft * 0.5F, 1.0F);
      this.drawTexturedModalRect(-62.0F + offset - this.animTopLeft * 15.0F, -62.0F + offset - this.animTopLeft * 15.0F, 0, 0, 64, 64);
      this.drawTopLeft(offset);
      if (isErasing) {
         this.drawTexturedModalRect(-62.0F + offset - this.animTopLeft * 15.0F, -62.0F + offset - this.animTopLeft * 15.0F, 128, 64, 64, 64);
      }

      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F + this.animBottomRight * 0.5F, 1.0F + this.animBottomRight * 0.5F, 1.0F);
      this.drawTexturedModalRect(-2.0F - offset + this.animBottomRight * 15.0F, -2.0F - offset + this.animBottomRight * 15.0F, 0, 0, 64, 64);
      this.drawBottomRight(offset);
      if (DragonStaffRenderer.isRenderingStaff()) {
         this.drawTexturedModalRect(-2.0F - offset + this.animBottomRight * 15.0F, -2.0F - offset + this.animBottomRight * 15.0F, 128, 64, 64, 64);
      }

      GlStateManager.popMatrix();
      Block block = this.targetBlockState.getBlock();
      boolean isChest = block instanceof BlockChest;
      boolean isBed = block instanceof BlockBed;
      if (isChest || isBed) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F + this.animBottomLeft * 0.5F, 1.0F + this.animBottomLeft * 0.5F, 1.0F);
         this.drawTexturedModalRect(-62.0F + offset - this.animBottomLeft * 15.0F, -2.0F - offset + this.animBottomLeft * 15.0F, 0, 0, 64, 64);
         if (isChest) {
            this.drawBottomLeft(offset);
         }

         if (isBed) {
            this.setBuildProgress(offset);
         }

         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            this.drawTexturedModalRect(-62.0F + offset - this.animBottomLeft * 15.0F, -2.0F - offset + this.animBottomLeft * 15.0F, 128, 64, 64, 64);
         }

         GlStateManager.popMatrix();
      }

      boolean isLog = block instanceof BlockLog;
      boolean hasMaterialTarget = this.getTargetMaterial() != null;
      if (isLog || hasMaterialTarget) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F + this.animTopRight * 0.5F, 1.0F + this.animTopRight * 0.5F, 1.0F);
         this.drawTexturedModalRect(-2.0F - offset + this.animTopRight * 15.0F, -62.0F + offset - this.animTopRight * 15.0F, 0, 0, 64, 64);
         if (isLog) {
            this.setEraseProgress(offset);
         }

         if (hasMaterialTarget) {
            this.drawTopRight(offset);
         }

         if (StructureMarkerRenderer.isMarked(this.targetBlockPos)) {
            this.drawTexturedModalRect(-2.0F - offset + this.animTopRight * 15.0F, -62.0F + offset - this.animTopRight * 15.0F, 128, 64, 64, 64);
         }

         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GL11.glDisable(3042);
   }

   void drawBottomRight(float offset) {
      this.drawTexturedModalRect(-2.0F - offset + this.animBottomRight * 15.0F, -2.0F - offset + this.animBottomRight * 15.0F, 192, 64, 64, 64);
   }

   void drawTopLeft(float offset) {
      this.drawTexturedModalRect(-62.0F + offset - this.animTopLeft * 15.0F, -62.0F + offset - this.animTopLeft * 15.0F, 64, 64, 64, 64);
   }

   void setEraseProgress(float offset) {
      this.drawTexturedModalRect(-2.0F - offset + this.animTopRight * 15.0F, -62.0F + offset - this.animTopRight * 15.0F, 64, 0, 64, 64);
   }

   void drawTopRight(float offset) {
      this.drawTexturedModalRect(-2.0F - offset + this.animTopRight * 15.0F, -62.0F + offset - this.animTopRight * 15.0F, 128, 0, 64, 64);
   }

   void setBuildProgress(float offset) {
      this.drawTexturedModalRect(-62.0F + offset - this.animBottomLeft * 15.0F, -2.0F - offset + this.animBottomLeft * 15.0F, 0, 64, 64, 64);
   }

   void drawBottomLeft(float offset) {
      this.drawTexturedModalRect(-62.0F + offset - this.animBottomLeft * 15.0F, -2.0F - offset + this.animBottomLeft * 15.0F, 192, 0, 64, 64);
   }

   double easeOutBack(double t) {
      double c1 = 1.70158;
      double c3 = 2.70158;
      return 1.0 + c3 * Math.pow(t - 1.0, 3.0) + c1 * Math.pow(t - 1.0, 2.0);
   }

   /**
    * Any mouse release closes the screen, committing the held quadrant's
    * command ({@link #onGuiClosed()}).
    */
   protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      this.mc.player.closeScreen();
      super.mouseReleased(mouseX, mouseY, mouseButton);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

}
