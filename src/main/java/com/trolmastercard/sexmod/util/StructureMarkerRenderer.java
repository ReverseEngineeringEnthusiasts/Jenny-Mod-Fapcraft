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

   public static boolean isMarked(BlockPos var0) {
      return markerPositions.contains(var0);
   }

   public static void renderMarkers() {
      Tessellator var0 = Tessellator.getInstance();
      BufferBuilder var1 = var0.getBuffer();
      Vec3d var2 = RotationHelper.a(Reference.cameraPosPrevious, Reference.cameraPosCurrent, mc.getRenderPartialTicks());
      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      GlStateManager.disableDepth();
      textureManager.bindTexture(MARK_TEXTURE);
      GlStateManager.translate(-var2.x, -var2.y, -var2.z);
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

      try {
         for (BlockPos var4 : markerPositions) {
            Vec3i var5 = getBlockColor(var4);
            a(var1, var4, var5.getX(), var5.getY(), var5.getZ());
         }
      } catch (ConcurrentModificationException var6) {
      }

      var0.draw();
      GlStateManager.enableDepth();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   static Vec3i getBlockColor(BlockPos var0) {
      Block var1 = Minecraft.getMinecraft().world.getBlockState(var0).getBlock();
      if (var1 instanceof BlockBed) {
         return COLOR_BLUE;
      } else {
         return var1 instanceof BlockChest ? COLOR_GREEN : COLOR_RED;
      }
   }

   static void a(BufferBuilder var0, BlockPos var1, int var2, int var3, int var4) {
      var0.pos(var1.getX(), var1.getY() + 1, var1.getZ())
         .tex(0.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY() + 1, var1.getZ())
         .tex(1.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY(), var1.getZ())
         .tex(1.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY(), var1.getZ())
         .tex(0.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY() + 1, 1 + var1.getZ())
         .tex(0.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY() + 1, 1 + var1.getZ())
         .tex(1.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY(), 1 + var1.getZ())
         .tex(1.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY(), 1 + var1.getZ())
         .tex(0.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY() + 1, var1.getZ())
         .tex(0.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY() + 1, 1 + var1.getZ())
         .tex(1.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY(), 1 + var1.getZ())
         .tex(1.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY(), var1.getZ())
         .tex(0.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY() + 1, var1.getZ())
         .tex(0.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY() + 1, 1 + var1.getZ())
         .tex(1.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY(), 1 + var1.getZ())
         .tex(1.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY(), var1.getZ())
         .tex(0.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY(), 1 + var1.getZ())
         .tex(0.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY(), 1 + var1.getZ())
         .tex(1.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY(), var1.getZ())
         .tex(1.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY(), var1.getZ())
         .tex(0.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY() + 1, 1 + var1.getZ())
         .tex(0.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY() + 1, 1 + var1.getZ())
         .tex(1.0, 1.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(1 + var1.getX(), var1.getY() + 1, var1.getZ())
         .tex(1.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
      var0.pos(var1.getX(), var1.getY() + 1, var1.getZ())
         .tex(0.0, 0.0)
         .color(var2, var3, var4, 255)
         .endVertex();
   }

   public static void renderMarkers(HashSet<BlockPos> var0) {
      markerPositions.addAll(var0);
   }

   public static void b(HashSet<BlockPos> var0) {
      markerPositions.removeAll(var0);
   }

   @SubscribeEvent
   public void a(RenderWorldLastEvent var1) {
      GlStateManager.enableColorMaterial();
      GL11.glDisable(2896);
      ItemStack var2 = mc.player.getHeldItem(EnumHand.MAIN_HAND);
      if (var2.getItem() != DragonStaffItem.DRAGON_STAFF) {
         var2 = mc.player.getHeldItem(EnumHand.OFF_HAND);
      }

      if (var2.getItem() == DragonStaffItem.DRAGON_STAFF) {
         renderMarkers();
      }

      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GL11.glEnable(2896);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ClientTickEvent var1) {
      if (var1.phase != Phase.START) {
         EntityPlayerSP var2 = Minecraft.getMinecraft().player;
         if (var2 != null) {
            Reference.cameraPosPrevious = Reference.cameraPosCurrent;
            Reference.cameraPosCurrent = var2.getPositionVector();
         }
      }
   }

}
