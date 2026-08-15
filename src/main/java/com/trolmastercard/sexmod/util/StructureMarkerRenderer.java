package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.item.DragonStaffItem;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
/**
 * <b>Role.</b> CLIENT-only overlay markers for tribe-managed blocks: the
 * dragon-staff UI and the {@link SendBlocksPacket} flow add/remove {@link BlockPos}s
 * from {@code markerPositions}; while the player holds the dragon staff, every
 * marked position gets a colored marker quad drawn in world space (blue = bed,
 * green = chest, red = mine target).
 * <p>
 * <b>State.</b> {@link Reference#cameraPosPrevious/Current} are advanced in
 * {@link #onClientTick} and consumed for render interpolation —
 * {@code StructureMarkerRenderer} is the only writer of those fields.
 * <p>
 * <b>Pitfall.</b> {@link #renderMarkers()} iterates the shared set during
 * world render; the concurrent-modification guard is required because
 * {@link SendBlocksPacket} handlers can mutate the set from the netty thread.
 */
public class StructureMarkerRenderer {
   static final Vec3i COLOR_RED = new Vec3i(255, 0, 0);
   static final Vec3i COLOR_GREEN = new Vec3i(0, 255, 0);
   static final Vec3i COLOR_BLUE = new Vec3i(0, 0, 255);
   static final ResourceLocation MARK_TEXTURE = new ResourceLocation("sexmod", "textures/mark.png");
   static HashSet<BlockPos> markerPositions = new HashSet<>();
   static Minecraft mc = Minecraft.getMinecraft();
   static TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

   public static void clearMarkers() {
      markerPositions.clear();
   }

   public static boolean isMarked(BlockPos pos) {
      return markerPositions.contains(pos);
   }

   public static void renderMarkers() {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      Vec3d cameraPos = RotationHelper.lerpVec3dDouble(Reference.cameraPosPrevious, Reference.cameraPosCurrent, mc.getRenderPartialTicks());
      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      GlStateManager.disableDepth();
      textureManager.bindTexture(MARK_TEXTURE);
      GlStateManager.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

      try {
         for (BlockPos pos : markerPositions) {
            Vec3i color = getBlockColor(pos);
            drawMarkerFace(buffer, pos, color.getX(), color.getY(), color.getZ());
         }
      } catch (ConcurrentModificationException exception) {
      }

      tessellator.draw();
      GlStateManager.enableDepth();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   static Vec3i getBlockColor(BlockPos pos) {
      Block block = Minecraft.getMinecraft().world.getBlockState(pos).getBlock();
      if (block instanceof BlockBed) {
         return COLOR_BLUE;
      } else {
         return block instanceof BlockChest ? COLOR_GREEN : COLOR_RED;
      }
   }

   static void drawMarkerFace(BufferBuilder buffer, BlockPos pos, int red, int green, int blue) {
      buffer.pos(pos.getX(), pos.getY() + 1, pos.getZ())
         .tex(0.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY() + 1, pos.getZ())
         .tex(1.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY(), pos.getZ())
         .tex(1.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY(), pos.getZ())
         .tex(0.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY() + 1, 1 + pos.getZ())
         .tex(0.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY() + 1, 1 + pos.getZ())
         .tex(1.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY(), 1 + pos.getZ())
         .tex(1.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY(), 1 + pos.getZ())
         .tex(0.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY() + 1, pos.getZ())
         .tex(0.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY() + 1, 1 + pos.getZ())
         .tex(1.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY(), 1 + pos.getZ())
         .tex(1.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY(), pos.getZ())
         .tex(0.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY() + 1, pos.getZ())
         .tex(0.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY() + 1, 1 + pos.getZ())
         .tex(1.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY(), 1 + pos.getZ())
         .tex(1.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY(), pos.getZ())
         .tex(0.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY(), 1 + pos.getZ())
         .tex(0.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY(), 1 + pos.getZ())
         .tex(1.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY(), pos.getZ())
         .tex(1.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY(), pos.getZ())
         .tex(0.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY() + 1, 1 + pos.getZ())
         .tex(0.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY() + 1, 1 + pos.getZ())
         .tex(1.0, 1.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(1 + pos.getX(), pos.getY() + 1, pos.getZ())
         .tex(1.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
      buffer.pos(pos.getX(), pos.getY() + 1, pos.getZ())
         .tex(0.0, 0.0)
         .color(red, green, blue, 255)
         .endVertex();
   }

   public static void renderMarkers(HashSet<BlockPos> markers) {
      markerPositions.addAll(markers);
   }

   public static void setMarkers(HashSet<BlockPos> markers) {
      markerPositions.removeAll(markers);
   }

   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      GlStateManager.enableColorMaterial();
      GL11.glDisable(2896);
      ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
      if (stack.getItem() != DragonStaffItem.DRAGON_STAFF) {
         stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
      }

      if (stack.getItem() == DragonStaffItem.DRAGON_STAFF) {
         renderMarkers();
      }

      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GL11.glEnable(2896);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (event.phase != Phase.START) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         if (player != null) {
            Reference.cameraPosPrevious = Reference.cameraPosCurrent;
            Reference.cameraPosCurrent = player.getPositionVector();
         }
      }
   }

}
