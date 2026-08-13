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

public class dp extends GirlRenderer {
   float r;

   public dp(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   protected ItemStack a_clash341(@Nullable ItemStack var1) {
      switch (this.j.y_clash492()) {
         case FISHING_IDLE:
         case FISHING_START:
            ItemStack var2 = ((LunaEntity)this.j).ao;
            ItemStack var3 = (ItemStack)this.j.func_184212_Q().func_187225_a(LunaEntity.az);
            if (var3.equals(ItemStack.field_190927_a)) {
               return var2;
            }

            Map var4 = EnchantmentHelper.func_82781_a(var3);
            EnchantmentHelper.func_82782_a(var4, var2);
            this.j.func_184611_a(EnumHand.MAIN_HAND, var2);
            return var2;
         default:
            return var1;
      }
   }

   boolean a_clash364() {
      return (Boolean)this.j.func_184212_Q().func_187225_a(BaseGirlEntity.G);
   }

   @Override
   protected void a(BufferBuilder var1, String var2, GeoBone var3) {
      if (!Minecraft.func_71410_x().func_147113_T()) {
         switch (var2) {
            case "head":
               this.r = var3.getRotationX();
               break;
            case "backHair":
               if (!this.a_clash364()) {
                  double var11 = this.r / gc.c_clash744(45.0F);
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

               double var6 = this.r / gc.c_clash744(45.0F);
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
               ItemStack var10 = (ItemStack)this.j.func_184212_Q().func_187225_a(LunaEntity.ag);
               if (!var10.equals(ItemStack.field_190927_a) && var9.Z == 1.0F) {
                  GlStateManager.func_179094_E();
                  Tessellator.func_178181_a().func_78381_a();
                  com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var3);
                  GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
                  GlStateManager.func_179152_a(var9.aa, var9.aa, var9.aa);
                  Minecraft.func_71410_x().func_175597_ag().func_178099_a(this.j, var10, TransformType.THIRD_PERSON_RIGHT_HAND);
                  GirlRenderer.n.func_181668_a(7, DefaultVertexFormats.field_181712_l);
                  this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
                  GlStateManager.func_179121_F();
               }
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
