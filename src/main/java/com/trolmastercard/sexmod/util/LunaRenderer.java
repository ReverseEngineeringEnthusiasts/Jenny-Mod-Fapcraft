package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;







import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class LunaRenderer extends GirlRenderer {
   float r;

   public LunaRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack var1) {
      switch (this.j.getCurrentAction()) {
         case FISHING_IDLE:
         case FISHING_START:
            ItemStack var2 = ((LunaEntity)this.j).ao;
            ItemStack var3 = (ItemStack)this.j.getDataManager().get(LunaEntity.az);
            if (var3.equals(ItemStack.EMPTY)) {
               return var2;
            }

            Map var4 = EnchantmentHelper.getEnchantments(var3);
            EnchantmentHelper.setEnchantments(var4, var2);
            this.j.setHeldItem(EnumHand.MAIN_HAND, var2);
            return var2;
         default:
            return var1;
      }
   }

   boolean a_clash364() {
      return (Boolean)this.j.getDataManager().get(BaseGirlEntity.IS_ANCHORED);
   }

   @Override
   protected void onBoneProcessing(BufferBuilder var1, String var2, GeoBone var3) {
      if (!Minecraft.getMinecraft().isGamePaused()) {
         switch (var2) {
            case "head":
               this.r = var3.getRotationX();
               break;
            case "backHair":
               if (!this.a_clash364()) {
                  double var11 = this.r / TrigMath.wrapDegrees(45.0F);
                  float var12 = (float)RotationHelper.b(0.0, 0.75, var11);
                  var3.setPositionZ(var12);
                  var3.setPositionY(var12);
                  var3.setRotationX(-this.r);
               }
               break;
            case "sideHairR":
            case "sideHairL":
               if (this.a_clash364()) {
                  break;
               }

               double var6 = this.r / TrigMath.wrapDegrees(45.0F);
               float var8 = (float)RotationHelper.b(0.0, 1.3F, var6);
               var3.setPositionZ(-var8);
               var3.setPositionY(var8);
            case "frontHairL":
            case "frontHairR":
               if (!this.a_clash364()) {
                  var3.setRotationX(-this.r);
               }
               break;
            case "offhand":
               LunaEntity var9 = (LunaEntity)this.j;
               ItemStack var10 = (ItemStack)this.j.getDataManager().get(LunaEntity.ag);
               if (!var10.equals(ItemStack.EMPTY) && var9.Z == 1.0F) {
                  GlStateManager.pushMatrix();
                  Tessellator.getInstance().draw();
                  com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var3);
                  GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                  GlStateManager.scale(var9.aa, var9.aa, var9.aa);
                  Minecraft.getMinecraft().getItemRenderer().renderItem(this.j, var10, TransformType.THIRD_PERSON_RIGHT_HAND);
                  GirlRenderer.n.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                  this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
                  GlStateManager.popMatrix();
               }
         }
      }
   }

}
