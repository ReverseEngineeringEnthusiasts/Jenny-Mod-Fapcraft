package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

/**
 * Geckolib layer that renders a vanilla {@link ModelElytra} on girl entities
 * ({@link AbstractGirlNpcEntity} incl. player-girls) when their chest slot
 * holds an elytra.
 * <p>
 * <b>Animation source.</b> The elytra pose is driven by
 * {@code setRotationAngles} using the owning player's limb/head angles when
 * the girl is a player-girl with a known owner ({@code getOwnerUserUUID}),
 * otherwise the girl's own angles — so a girl wearing her owner's elytra
 * flaps in sync with him.
 * <p>
 * CLIENT-side render thread only; attached to the girl renderer via geckolib's
 * layer system.
 */
public class GirlLayerRenderer extends GeoLayerRenderer {
   private static final ResourceLocation elytraTexture = new ResourceLocation("textures/entity/elytra.png");
   private final ModelElytra elytraModel = new ModelElytra();

   public GirlLayerRenderer(IGeoRenderer renderer) {
      super(renderer);
   }

   /**
    * Renders the elytra model over the girl when her chest slot is an elytra:
    * binds the vanilla elytra texture, pushes a 0.125 offset, applies the
    * layer's rotation angles from the owner player (or the girl itself), and
    * renders. No-op for non-girl entities or non-elytra chest items.
    */
   @Override
   public void render(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Color color) {
      if (entity instanceof AbstractGirlNpcEntity) {
         AbstractGirlNpcEntity girl = (AbstractGirlNpcEntity)entity;
         ItemStack chestStack = (ItemStack)girl.getDataManager().get(AbstractGirlNpcEntity.CHEST_SLOT);
         EntityPlayer owner = null;
         if (girl instanceof AbstractPlayerGirlEntity) {
            UUID ownerUuid = ((AbstractPlayerGirlEntity)girl).getOwnerUserUUID();
            if (ownerUuid != null) {
               owner = entity.world.getPlayerEntityByUUID(ownerUuid);
            }
         }

         if (chestStack.getItem() == Items.ELYTRA) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ZERO);
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(elytraTexture);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            float layerScale = this.renderLayer();
            float modelScale = layerScale;
            float scale2 = scale;
            float headPitch2 = headPitch;
            float netHeadYaw2 = netHeadYaw;
            float limbSwingAmount2 = limbSwingAmount;
            float limbSwing2 = limbSwing;
            ModelElytra elytraModel = this.elytraModel;
            ModelElytra modelTarget;
            float limbSwingArg;
            float limbSwingAmountArg;
            float netHeadYawArg;
            float headPitchArg;
            float scaleArg;
            float modelScaleArg;
            Object poseEntity;
            if (owner == null) {
               modelTarget = elytraModel;
               limbSwingArg = limbSwing2;
               limbSwingAmountArg = limbSwingAmount2;
               netHeadYawArg = netHeadYaw2;
               headPitchArg = headPitch2;
               scaleArg = scale2;
               modelScaleArg = modelScale;
               poseEntity = entity;
            } else {
               modelTarget = elytraModel;
               limbSwingArg = limbSwing2;
               limbSwingAmountArg = limbSwingAmount2;
               netHeadYawArg = netHeadYaw2;
               headPitchArg = headPitch2;
               scaleArg = scale2;
               modelScaleArg = modelScale;
               poseEntity = owner;
            }

            modelTarget.setRotationAngles(limbSwingArg, limbSwingAmountArg, netHeadYawArg, headPitchArg, scaleArg, modelScaleArg, (Entity)poseEntity);
            ModelElytra renderModel = this.elytraModel;
            Object renderEntity;
            if (owner == null) {
               modelTarget = renderModel;
               renderEntity = entity;
            } else {
               modelTarget = renderModel;
               renderEntity = owner;
            }

            modelTarget.render((Entity)renderEntity, limbSwing, limbSwingAmount, netHeadYaw, headPitch, scale, layerScale);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }
      }
   }

   /**
    * Prepares the vanilla layer transform (rescale-normal on, mirrored scale,
    * -1.501 translate) and returns the standard 1/16 model scale.
    */
   public float renderLayer() {
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
      GlStateManager.translate(0.0F, -1.501F, 0.0F);
      return 0.0625F;
   }

   @Override
   public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, float renderScale) {
   }

   public boolean shouldCombineTextures() {
      return false;
   }

}
