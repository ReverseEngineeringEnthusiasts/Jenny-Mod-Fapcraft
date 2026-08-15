package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.SexEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Renderer for {@link SexEntity} — the small "heart/particle" marker entity
 * spawned around Luna during her scenes (e.g. her fishing-rod job). Renders a
 * particle-sprite quad plus, while Luna holds a non-air item, the held item
 * itself; also draws the fishing-line/leash curve from Luna's hand to the
 * particle when her main hand is empty.
 * <p>
 * <b>Luna coupling.</b> The particle glides from its spawn position to
 * Luna's hand via a progress flag on Luna ({@code zFlag}, advanced per real
 * frame) and Luna's position/yaw fields are overwritten with her target
 * position during the render — the yaw is what the fishing line math uses.
 * The whole pass is skipped while Luna is missing or her zFlag is 1 (already
 * arrived).
 * <p>
 * CLIENT-side render thread only. Position interpolation uses
 * {@link RotationHelper#lerpVec3dDouble} (PROGRESS lerp — correct here).
 */
public class SexEntityRenderer extends Render<SexEntity> {
   static final double PARTICLE_OFFSET_B = 0.1896224320030116;
   static final double PARTICLE_OFFSET_D = -0.5;
   static final double PARTICLE_OFFSET_C = 0.08742380916962415;
   private static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation("textures/particle/particles.png");

   public SexEntityRenderer(RenderManager renderManager) {
      super(renderManager);
   }

   /**
    * Main render (see class javadoc): billboarded particle quad (with optional
    * held-item and outline-mode support), then the fishing line from Luna's
    * hand (anchored at her target position) to the particle when her hand is
    * empty. Temporarily rewrites Luna's pos/yaw fields and restores nothing —
    * the entity code tolerates the overwrite, do not "fix" it.
    */
   public void doRenderSexEntity(SexEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      LunaEntity luna = entity.getOwnerLuna();
      if (luna != null && !this.renderOutlines && luna.zFlag != 1.0F) {
         luna.av = entity;
         ItemStack heldItem = (ItemStack)luna.getDataManager().get(LunaEntity.ag);
         if (!heldItem.getItem().equals(Items.AIR)) {
            float fps = Minecraft.getDebugFPS();
            if (fps == 0.0F) {
               fps = 0.1F;
            }

            luna.zFlag += 60.0F / fps * 0.01666F * 2.0F;
            luna.zFlag = Math.min(1.0F, luna.zFlag);
            EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
            Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
            Vec3d entityPos = new Vec3d(x, y, z);
            Vec3d lunaPos = RotationHelper.lerpVec3dDouble(
               new Vec3d(luna.lastTickPosX, luna.lastTickPosY + 0.875, luna.lastTickPosZ), luna.getPositionVector().add(0.0, 0.875, 0.0), partialTicks
            );
            lunaPos = lunaPos.subtract(localPlayerPos);
            entityPos = RotationHelper.lerpVec3dDouble(entityPos, lunaPos, luna.zFlag);
            x = entityPos.x;
            y = entityPos.y;
            z = entityPos.z;
         } else {
            luna.zFlag = 0.0F;
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y, (float)z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         this.bindEntityTexture(entity);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
         }

         if (!heldItem.getItem().equals(Items.AIR)) {
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.translate(0.0F, -0.2F, 0.0F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(luna, heldItem, TransformType.THIRD_PERSON_RIGHT_HAND);
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
         }

         this.bindEntityTexture(entity);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos(-0.5, -0.5, 0.0).tex(0.0625, 0.1875).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos(0.5, -0.5, 0.0).tex(0.125, 0.1875).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos(0.5, 0.5, 0.0).tex(0.125, 0.125).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos(-0.5, 0.5, 0.0).tex(0.0625, 0.125).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
         }

         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         int handSide = luna.getPrimaryHand() == EnumHandSide.RIGHT ? 1 : -1;
         ItemStack mainhandItem = luna.getHeldItemMainhand();
         if (!(mainhandItem.getItem() instanceof ItemFishingRod)) {
            handSide = -handSide;
         }

         luna.rotationYaw = luna.getYawRotation();
         luna.renderYawOffset = luna.getYawRotation();
         luna.posX = luna.getTargetPosition().x;
         luna.posY = luna.getTargetPosition().y;
         luna.posZ = luna.getTargetPosition().z;
         luna.prevPosX = luna.getTargetPosition().x;
         luna.prevPosY = luna.getTargetPosition().y;
         luna.prevPosZ = luna.getTargetPosition().z;
         float yawRadians = (luna.prevRenderYawOffset + (luna.renderYawOffset - luna.prevRenderYawOffset) * partialTicks) * (float) (Math.PI / 180.0);
         double sinYaw = MathHelper.sin(yawRadians);
         double cosYaw = MathHelper.cos(yawRadians);
         double handOffset = handSide * 0.35;
         double handX = luna.prevPosX + (luna.posX - luna.prevPosX) * partialTicks - cosYaw * handOffset - sinYaw * 0.8;
         double handY = luna.prevPosY + luna.getEyeHeight() + (luna.posY - luna.prevPosY) * partialTicks - 0.45;
         double handZ = luna.prevPosZ + (luna.posZ - luna.prevPosZ) * partialTicks - sinYaw * handOffset + cosYaw * 0.8;
         double sneakOffset = luna.isSneaking() ? -0.1875 : 0.0;
         double particleX = entity.prevPosX
            + (entity.posX - entity.prevPosX) * partialTicks
            - Math.sin((luna.getYawRotation() + 90.0F) * (Math.PI / 180.0)) * 0.1896224320030116
            - Math.sin(luna.getYawRotation().floatValue() * (Math.PI / 180.0)) * 0.08742380916962415;
         double particleY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + 0.25 + -0.5;
         double particleZ = entity.prevPosZ
            + (entity.posZ - entity.prevPosZ) * partialTicks
            + Math.cos((luna.getYawRotation() + 90.0F) * (Math.PI / 180.0)) * 0.1896224320030116
            + Math.cos(luna.getYawRotation().floatValue() * (Math.PI / 180.0)) * 0.08742380916962415;
         double lineDx = (float)(handX - particleX);
         double lineDy = (float)(handY - particleY) + sneakOffset;
         double lineDz = (float)(handZ - particleZ);
         GlStateManager.disableTexture2D();
         GlStateManager.disableLighting();
         if (heldItem.getItem().equals(Items.AIR)) {
            buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

            for (int segment = 0; segment <= 16; segment++) {
               float progress = segment / 16.0F;
               buffer.pos(x + lineDx * progress, y + lineDy * (progress * progress + progress) * 0.5 + 0.25, z + lineDz * progress)
                  .color(0, 0, 0, 255)
                  .endVertex();
            }

            tessellator.draw();
         }

         GlStateManager.enableLighting();
         GlStateManager.enableTexture2D();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }
   }

   @Nullable
   protected ResourceLocation getEntityTexture(SexEntity entity) {
      return PARTICLE_TEXTURE;
   }

}
