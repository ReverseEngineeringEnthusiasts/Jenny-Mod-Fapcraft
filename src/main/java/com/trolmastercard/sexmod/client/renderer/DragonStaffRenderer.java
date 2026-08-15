package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.DragonStaffModel;
import com.trolmastercard.sexmod.client.model.GalathModel;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.item.DragonStaffItem;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.VectorMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

/**
 * Item renderer for the dragon staff: renders the geckolib staff model plus a
 * spinning end-crystal on the {@code staff} bone, with a bob animation, a
 * screen-space sway from the holder's movement, and an orbiting
 * colored-wool-particle ring driven by {@link KoboldEntity#aY} bone data.
 * <p>
 * <b>Dual mode.</b> When {@code isRendering} (toggled from the
 * {@code StructureCommandScreen}) the particles are spread in the player's
 * view direction (ease-in-out toward the eye); otherwise they animate along
 * the bone chain. Wool colors encode particle ids.
 * <p>
 * CLIENT-side render thread only. {@link #renderRecursively} interrupts the
 * vanilla geckolib buffer (draws pending vertices) before applying the custom
 * bone transform — do not reorder the push/draw/transform sequence.
 */
public class DragonStaffRenderer extends GeoItemRenderer<DragonStaffItem> {
   private static final ResourceLocation CRYSTAL_TEXTURE = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
   private final GalathModel crystalModel = new GalathModel();
   static final float SCALE_10 = 10.0F;
   static final float SCALE_1_5 = 1.5F;
   static final float SCALE_0_175 = 0.175F;
   static final float ROT_0_1 = 0.1F;
   static final float SPEED_0_04 = 0.04F;
   static final float SCALE_8 = 8.0F;
   static final float SCALE_6 = 6.0F;
   static final float SCALE_1_3 = 1.3F;
   static boolean isRendering = false;
   Minecraft mc;
   Vector2f screenPos;
   double animationTicks = 0.0;
   EntityPlayer player;
   ItemStack heldItem;
   static HashMap<ItemStack, Vector3f> n = new HashMap<>();

   public DragonStaffRenderer() {
      super(new DragonStaffModel());
      this.mc = Minecraft.getMinecraft();
   }

   public static boolean isRenderingStaff() {
      return isRendering;
   }

   public static void toggleStaffRendering() {
      isRendering = !isRendering;
   }

   @Override
   public void render(DragonStaffItem item, ItemStack stack) {
      this.renderStaffItem(item, stack);
   }

   /**
    * Entry point: locates the player holding this exact staff stack (main or
    * off hand), computes his screen-space movement vector, advances the
    * animation clock (paused when the game is paused) and delegates to the
    * geckolib pipeline. Stores the holder/stack for the recursive pass.
    */
   public void renderStaffItem(DragonStaffItem item, ItemStack stack) {
      EntityPlayer holder = null;

      for (EntityPlayer player : this.mc.world.playerEntities) {
         if (player.inventory.mainInventory.contains(stack)) {
            holder = player;
            break;
         }

         if (player.inventory.offHandInventory.contains(stack)) {
            holder = player;
            break;
         }
      }

      if (holder != null) {
         double deltaX = holder.posX - holder.lastTickPosX;
         double deltaZ = holder.posZ - holder.lastTickPosZ;
         double yawRadians = (Math.PI / 180.0) * holder.rotationYaw;
         this.screenPos = new Vector2f((float)(deltaX * Math.cos(yawRadians) + deltaZ * Math.sin(yawRadians)), (float)(-deltaX * Math.sin(yawRadians) + deltaZ * Math.cos(yawRadians)));
      } else {
         this.screenPos = new Vector2f(0.0F, 0.0F);
      }

      if (!Minecraft.getMinecraft().isGamePaused()) {
         this.animationTicks = Minecraft.getMinecraft().player.ticksExisted + this.mc.getRenderPartialTicks();
      }

      this.heldItem = stack;
      this.player = holder;
      super.render(item, stack);
   }

   /**
    * Custom pass for the {@code staff} bone: flush pending geckolib vertices,
    * apply the bone transform, then draw the bobbing crystal (rotation
    * accumulates the holder's movement per frame in the static map {@code n}
    * keyed by ItemStack) and the orbiting wool particles. Re-binds the staff
    * texture and restarts the buffer for the remaining bones.
    */
   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a) {
      if ("staff".equals(bone.getName())) {
         GlStateManager.pushMatrix();
         Tessellator.getInstance().draw();
         com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
         GlStateManager.translate(0.0, 1.5 + 0.001 * Math.sin(0.005 * this.animationTicks) + 0.001, 0.0);
         Vector3f storedSway = n.get(this.heldItem);
         GlStateManager.scale(this.getBobOffset(), this.getBobOffset(), this.getBobOffset());
         if (storedSway == null) {
            storedSway = new Vector3f(0.0F, 0.0F, 0.0F);
         }

         storedSway.add(new Vector3f(this.screenPos.x, this.player == null ? 0.0F : (float)(this.player.posY - this.player.lastTickPosY), this.screenPos.y));
         GlStateManager.rotate(storedSway.z * 10.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(storedSway.x * 10.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(-storedSway.y * 10.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate((float)(this.animationTicks * 0.1F), 1.0F, 1.0F, 1.0F);
         n.put(this.heldItem, storedSway);
         this.mc.getTextureManager().bindTexture(CRYSTAL_TEXTURE);
         this.crystalModel.render(Minecraft.getMinecraft().player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.popMatrix();
         if (this.player != null) {
            this.collectAnimationBones();
         }

         this.mc.getTextureManager().bindTexture(new DragonStaffModel().getTextureLocation((DragonStaffItem) null));
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      }

      super.renderRecursively(buffer, bone, r, g, b, a);
   }

   /**
    * Reads the particle chain from {@link KoboldEntity#aY} (id + position
    * tuples) and either spreads the particles toward the player's view
    * (rendering mode, {@link #renderParticles}) or staggers them along the
    * bone order (animation mode, {@link #animateBones}).
    */
   void collectAnimationBones() {
      ArrayList particleIds = new ArrayList();
      ArrayList particlePositions = new ArrayList();

      for (Vector4d particleData : KoboldEntity.aY) {
         particleIds.add((int)particleData.getW());
         particlePositions.add(new Vec3d(particleData.getX(), particleData.getY(), particleData.getZ()));
      }

      if (particleIds.size() != 0) {
         if (isRendering) {
            this.renderParticles(particleIds, particlePositions);
         } else {
            this.animateBones(particleIds);
         }
      }
   }

   /**
    * Rendering-mode particle placement: for each chain particle, rotates its
    * offset into the holder's head-view space and eases each axis
    * ({@code easeInOut}) so the wool cubes stream toward the player's eye,
    * scaled by 1.3.
    */
   void renderParticles(List<Integer> particleIds, List<Vec3d> particlePositions) {
      for (int i = 0; i < particleIds.size(); i++) {
         float headYaw = RotationHelper.lerp(this.player.prevRotationYawHead, this.player.rotationYawHead, this.mc.getRenderPartialTicks());
         float headPitch = RotationHelper.lerp(this.player.prevRotationPitch, this.player.rotationPitch, this.mc.getRenderPartialTicks());
         Vec3d eyePos = RotationHelper.lerpVec3dDouble(
            new Vec3d(this.player.prevPosX, this.player.prevPosY + this.player.getEyeHeight(), this.player.prevPosZ),
            this.player.getPositionVector().add(0.0, this.player.getEyeHeight(), 0.0),
            this.mc.getRenderPartialTicks()
         );
         Vec3d relative = eyePos.subtract((Vec3d)particlePositions.get(i));
         relative = VectorMath.rotateByYawPitch(relative, -headPitch, headYaw);
         double magnitude = Math.abs(relative.x) + Math.abs(relative.z) + Math.abs(relative.y);
         double easedX = -relative.x / magnitude;
         double easedY = -relative.y / magnitude;
         double easedZ = relative.z / magnitude;
         easedX = this.easeInOut(easedX);
         easedY = this.easeInOut(easedY);
         easedZ = this.easeInOut(easedZ);
         easedX *= 1.3F;
         easedY *= 1.3F;
         easedZ *= 1.3F;
         this.renderParticleFrom((Integer)particleIds.get(i), (float)easedX, (float)easedY, (float)easedZ);
      }
   }

   /**
    * Animation-mode placement: staggers the chain particles evenly along a
    * spinning arc (rotation axis per particle, speed scaled by an index
    * lerp 0.8..1.2).
    */
   void animateBones(List<Integer> particleIds) {
      float step = 1.0F / particleIds.size();
      float progress = 0.0F;

      for (int i = 0; i < particleIds.size(); i++) {
         progress += step;
         this.renderParticleAt((Integer)particleIds.get(i), 1.0F - progress, 0.0F + progress, (float)RotationHelper.lerpDouble(0.8F, 1.2F, (double)i / particleIds.size()));
      }
   }

   double easeInOut(double value) {
      return value * Math.sqrt(1.0 - value * value / 2.0);
   }

   double getBobOffset() {
      return 0.175F + 0.025 * Math.sin(0.005 * this.animationTicks) + 0.025;
   }

   void renderParticleAt(int colorId, float x, float y, float z) {
      this.renderItem(new ItemStack(Blocks.WOOL, 1, colorId), x, y, z);
   }

   void renderParticleFrom(int colorId, float x, float y, float z) {
      this.renderItemAt(new ItemStack(Blocks.WOOL, 1, colorId), x, y, z);
   }

   /**
    * Draws one wool-cube particle (color = particle id) translated by the
    * given offset from the crystal anchor. Tiny 0.04 scale, vanilla item
    * rendering.
    */
   void renderItemAt(ItemStack stack, float x, float y, float z) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0, 1.5 + 0.001 * Math.sin(0.005 * this.animationTicks) + 0.001, 0.0);
      GlStateManager.scale(0.04F, 0.04F, 0.04F);
      GlStateManager.translate(x * 6.0F, y * 6.0F, z * 6.0F);
      this.mc.getItemRenderer().renderItem(Minecraft.getMinecraft().player, stack, TransformType.NONE);
      GlStateManager.popMatrix();
   }

   /**
    * Draws one wool-cube particle rotating around the given axis with speed
    * scaled by {@code speed}, offset by 6 units along X.
    */
   void renderItem(ItemStack stack, float rotX, float rotY, float speed) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0, 1.5 + 0.001 * Math.sin(0.005 * this.animationTicks) + 0.001, 0.0);
      GlStateManager.scale(0.04F, 0.04F, 0.04F);
      GlStateManager.rotate((float)(this.animationTicks * 8.0 * speed), 0.0F, rotX, rotY);
      GlStateManager.translate(6.0F, 0.0F, 0.0F);
      this.mc.getItemRenderer().renderItem(Minecraft.getMinecraft().player, stack, TransformType.NONE);
      GlStateManager.popMatrix();
   }

}
