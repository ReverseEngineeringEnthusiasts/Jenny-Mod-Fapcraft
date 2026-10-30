package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.item.DragonStaffItem;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 * world render while {@link SendBlocksPacket} handlers can mutate it from the
 * netty thread — the set is a {@link ConcurrentHashMap} key set so iteration is
 * safe; the concurrent-modification guard stays as a belt-and-braces.
 */
public class StructureMarkerRenderer {
   static final Vec3i COLOR_RED = new Vec3i(255, 0, 0);
   static final Vec3i COLOR_GREEN = new Vec3i(0, 255, 0);
   static final Vec3i COLOR_BLUE = new Vec3i(0, 0, 255);
   static final ResourceLocation MARK_TEXTURE = new ResourceLocation("sexmod", "textures/mark.png");
   static Set<BlockPos> markerPositions = ConcurrentHashMap.newKeySet();
   /** Per-marker colour memo, dropped whenever the marker set changes. */
   static final Map<BlockPos, Vec3i> markerColors = new ConcurrentHashMap<>();
   static Minecraft mc = Minecraft.getMinecraft();
   static TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

   public static void clearMarkers() {
      markerPositions.clear();
      markerColors.clear();
   }

   public static boolean isMarked(BlockPos pos) {
      return markerPositions.contains(pos);
   }

   public static void renderMarkers() {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      Vec3d cameraPos = RotationHelper.lerpVec3dDouble(Reference.cameraPosPrevious, Reference.cameraPosCurrent, mc.getRenderPartialTicks());
      boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
      boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      GlStateManager.pushMatrix();
      try {
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
            // ignored: marker list mutated during the frame; markers are redrawn next frame
         }

         tessellator.draw();
      } finally {
         restore(depth, GlStateManager::enableDepth, GlStateManager::disableDepth);
         restore(cull, GlStateManager::enableCull, GlStateManager::disableCull);
         GlStateManager.popMatrix();
      }
   }

   static Vec3i getBlockColor(BlockPos pos) {
      Vec3i cached = markerColors.get(pos);
      if (cached != null) {
         return cached;
      }

      Block block = Minecraft.getMinecraft().world.getBlockState(pos).getBlock();
      Vec3i color;
      if (block instanceof BlockBed) {
         color = COLOR_BLUE;
      } else {
         color = block instanceof BlockChest ? COLOR_GREEN : COLOR_RED;
      }

      markerColors.put(pos, color);
      return color;
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
      markerColors.clear();
   }

   public static void setMarkers(HashSet<BlockPos> markers) {
      markerPositions.removeAll(markers);
      markerColors.clear();
   }

   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
      if (stack.getItem() != DragonStaffItem.DRAGON_STAFF) {
         stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
      }

      if (stack.getItem() != DragonStaffItem.DRAGON_STAFF) {
         return;
      }

      // Snapshot every flag this pass touches so it can be put back exactly as found.
      boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
      boolean colorMaterial = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
      boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
      try {
         GlStateManager.enableColorMaterial();
         GlStateManager.disableLighting();
         renderMarkers();
      } finally {
         restore(lighting, GlStateManager::enableLighting, GlStateManager::disableLighting);
         restore(colorMaterial, GlStateManager::enableColorMaterial, GlStateManager::disableColorMaterial);
         restore(depth, GlStateManager::enableDepth, GlStateManager::disableDepth);
         restore(alpha, GlStateManager::enableAlpha, GlStateManager::disableAlpha);
      }
   }

   private static void restore(boolean enabled, Runnable enable, Runnable disable) {
      if (enabled) {
         enable.run();
      } else {
         disable.run();
      }
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
