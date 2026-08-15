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

   public GirlLayerRenderer(IGeoRenderer var1) {
      super(var1);
   }

   /**
    * Renders the elytra model over the girl when her chest slot is an elytra:
    * binds the vanilla elytra texture, pushes a 0.125 offset, applies the
    * layer's rotation angles from the owner player (or the girl itself), and
    * renders. No-op for non-girl entities or non-elytra chest items.
    */
   @Override
   public void render(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8) {
      if (var1 instanceof AbstractGirlNpcEntity) {
         AbstractGirlNpcEntity var9 = (AbstractGirlNpcEntity)var1;
         ItemStack var10 = (ItemStack)var9.getDataManager().get(AbstractGirlNpcEntity.CHEST_SLOT);
         EntityPlayer var11 = null;
         if (var9 instanceof AbstractPlayerGirlEntity) {
            UUID var12 = ((AbstractPlayerGirlEntity)var9).getOwnerUserUUID();
            if (var12 != null) {
               var11 = var1.world.getPlayerEntityByUUID(var12);
            }
         }

         if (var10.getItem() == Items.ELYTRA) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ZERO);
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(elytraTexture);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            float var21 = this.renderLayer();
            float var19 = var21;
            float var18 = var7;
            float var17 = var6;
            float var16 = var5;
            float var15 = var3;
            float var14 = var2;
            ModelElytra var13 = this.elytraModel;
            ModelElytra var10000;
            float var10001;
            float var10002;
            float var10003;
            float var10004;
            float var10005;
            float var10006;
            Object var10007;
            if (var11 == null) {
               var10000 = var13;
               var10001 = var14;
               var10002 = var15;
               var10003 = var16;
               var10004 = var17;
               var10005 = var18;
               var10006 = var19;
               var10007 = var1;
            } else {
               var10000 = var13;
               var10001 = var14;
               var10002 = var15;
               var10003 = var16;
               var10004 = var17;
               var10005 = var18;
               var10006 = var19;
               var10007 = var11;
            }

            var10000.setRotationAngles(var10001, var10002, var10003, var10004, var10005, var10006, (Entity)var10007);
            ModelElytra var20 = this.elytraModel;
            Object var23;
            if (var11 == null) {
               var10000 = var20;
               var23 = var1;
            } else {
               var10000 = var20;
               var23 = var11;
            }

            var10000.render((Entity)var23, var2, var3, var5, var6, var7, var21);
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
   public void doRenderLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
   }

   public boolean shouldCombineTextures() {
      return false;
   }

}
