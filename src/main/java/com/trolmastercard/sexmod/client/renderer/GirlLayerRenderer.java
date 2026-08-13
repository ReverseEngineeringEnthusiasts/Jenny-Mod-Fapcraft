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

public class GirlLayerRenderer extends GeoLayerRenderer {
   private static final ResourceLocation b = new ResourceLocation("textures/entity/elytra.png");
   private final ModelElytra a = new ModelElytra();

   public GirlLayerRenderer(IGeoRenderer var1) {
      super(var1);
   }

   @Override
   public void render(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8) {
      if (var1 instanceof AbstractGirlNpcEntity) {
         AbstractGirlNpcEntity var9 = (AbstractGirlNpcEntity)var1;
         ItemStack var10 = (ItemStack)var9.func_184212_Q().func_187225_a(AbstractGirlNpcEntity.T);
         EntityPlayer var11 = null;
         if (var9 instanceof AbstractPlayerGirlEntity) {
            UUID var12 = ((AbstractPlayerGirlEntity)var9).getOwnerUserUUID();
            if (var12 != null) {
               var11 = var1.field_70170_p.func_152378_a(var12);
            }
         }

         if (var10.func_77973_b() == Items.field_185160_cR) {
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.func_179147_l();
            GlStateManager.func_187401_a(SourceFactor.ONE, DestFactor.ZERO);
            Minecraft.func_71410_x().func_175598_ae().field_78724_e.func_110577_a(b);
            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b(0.0F, 0.0F, 0.125F);
            float var21 = this.a_clash198();
            float var19 = var21;
            float var18 = var7;
            float var17 = var6;
            float var16 = var5;
            float var15 = var3;
            float var14 = var2;
            ModelElytra var13 = this.a;
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

            var10000.func_78087_a(var10001, var10002, var10003, var10004, var10005, var10006, (Entity)var10007);
            ModelElytra var20 = this.a;
            Object var23;
            if (var11 == null) {
               var10000 = var20;
               var23 = var1;
            } else {
               var10000 = var20;
               var23 = var11;
            }

            var10000.func_78088_a((Entity)var23, var2, var3, var5, var6, var7, var21);
            GlStateManager.func_179084_k();
            GlStateManager.func_179121_F();
         }
      }
   }

   public float a_clash198() {
      GlStateManager.func_179091_B();
      GlStateManager.func_179152_a(-1.0F, -1.0F, 1.0F);
      GlStateManager.func_179109_b(0.0F, -1.501F, 0.0F);
      return 0.0625F;
   }

   @Override
   public void func_177141_a(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
   }

   public boolean func_177142_b() {
      return false;
   }

}
