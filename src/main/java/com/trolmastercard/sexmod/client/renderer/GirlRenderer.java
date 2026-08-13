package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.SkinFetcher;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.ClothingScreen;
import com.trolmastercard.sexmod.client.model.GirlModel;
import com.trolmastercard.sexmod.client.renderer.api.IGirlRenderer;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.f7;







import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.model.provider.data.EntityModelData;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.renderers.geo.RenderHurtColor;
import software.bernie.geckolib3.util.MatrixStack;
import software.bernie.shadowed.eliotlash.mclib.utils.Interpolations;

public abstract class GirlRenderer<T extends BaseGirlEntity & IAnimatable> extends GeoEntityRenderer<T> implements IGirlRenderer {
   protected static final ResourceLocation e = new ResourceLocation("sexmod", "textures/line.png");
   static final float m = 1.5F;
   protected double c;
   protected T j;
   protected static Minecraft i;
   protected static HashMap<UUID, ResourceLocation> l = new HashMap<>();
   Color f = new Color(245, 199, 165);
   Color o = new Color(245, 157, 169);
   boolean h = false;
   protected HashSet<String> p = new HashSet<>();
   Integer k = null;
   Integer b = null;
   Integer d = null;
   float a = 0.0F;
   public static BufferBuilder n;
   Matrix4f g = null;
   protected GeoBone q = null;

   public GirlRenderer(RenderManager var1, AnimatedGeoModel<?> var2, double var3) {
      super(var1, (AnimatedGeoModel<T>) (AnimatedGeoModel) var2);
      this.c = var3;
      i = Minecraft.func_71410_x();
      this.field_76989_e = 0.2F;
   }

   @Override
   public ResourceLocation func_110775_a(T var1) {
      return super.getEntityTexture(var1);
   }

   protected ResourceLocation getSkinTexture(T var1) {
      ResourceLocation var2;
      if (!(var1.field_70170_p instanceof SexWorldClient) && var1.ae_clash498() != null) {
         var2 = l.get(var1.ae_clash498());
         if (var2 == null) {
            return this.a_clash329(var1.ae_clash498(), var1.field_70170_p);
         }
      } else {
         var2 = l.get(i.func_110432_I().func_148256_e().getId());
         if (var2 == null) {
            return this.a_clash329(i.func_110432_I().func_148256_e().getId(), var1.field_70170_p);
         }
      }

      return var2;
   }

   protected ResourceLocation a_clash329(UUID var1, World var2) {
      BufferedImage var3;
      try {
         var3 = SkinFetcher.a_clash864(var1);
         Graphics var4 = var3.getGraphics();
         var4.setColor(this.f);
         var4.fillRect(0, 0, 4, 3);
         var4.setColor(this.o);
         var4.fillRect(4, 0, 3, 3);
      } catch (Exception var5) {
         if (!this.h) {
            this.h = true;
         }

         try {
            var3 = ImageIO.read(i.func_110442_L().func_110536_a(new ResourceLocation("sexmod", "textures/player/steve.png")).func_110527_b());
         } catch (Exception var6) {
            var3 = new BufferedImage(64, 64, 2);
         }
      }

      l.put(var1, this.field_76990_c.field_78724_e.func_110578_a("player" + var1, new DynamicTexture(var3)));
      return l.get(var1);
   }

   protected static float a_clash330(BaseGirlEntity var0, float var1) {
      return var0.Q_clash505() ? var0.I_clash415() : RotationHelper.a_clash25(var0.field_70760_ar, var0.field_70761_aq, var1);
   }

   protected void d_clash331() {
   }

   protected void b_clash332() {
   }

   float a(World var1, Vec3d var2, float var3, float var4) {
      RayTraceResult var5 = this.a(var2, var2.func_178787_e(ck.a(new Vec3d(0.0, 0.0, -4.0), var3, var4)), var1);
      if (var5 == null) {
         return 4.0F;
      }

      Vec3d var6 = var5.field_72307_f;
      return var6 == null ? 4.0F : (float)var2.func_72438_d(var6);
   }

   boolean a(T var1, EntityPlayer var2) {
      if (var1 instanceof AbstractPlayerGirlEntity) {
         return true;
      }

      World var3 = var1.field_70170_p;
      Vec3d var4 = var1.func_174791_d();
      float var5 = var1.field_70130_N * 1.5F;
      float var6 = var1.field_70131_O * 1.5F;
      Vec3d var7 = var2.func_174791_d().func_72441_c(0.0, var2.func_70047_e(), 0.0);
      int var8 = i.field_71474_y.field_74320_O;
      if (var8 != 0) {
         return true;
      }

      if (var8 > 0) {
         float var9 = var2.field_70177_z;
         float var10 = var2.field_70125_A;
         if (var8 == 2) {
            var10 += 180.0F;
         }

         float var11 = 4.0F;
         Vec3d var12 = var7.func_72441_c(
            MathHelper.func_76126_a(var9 * (float) (Math.PI / 180.0)) * MathHelper.func_76134_b(var10 * (float) (Math.PI / 180.0)) * var11,
            MathHelper.func_76126_a(var10 * (float) (Math.PI / 180.0)) * var11,
            -MathHelper.func_76134_b(var9 * (float) (Math.PI / 180.0)) * MathHelper.func_76134_b(var10 * (float) (Math.PI / 180.0)) * var11
         );
         BlockPos var13 = new BlockPos(var12);
         boolean var14 = var3.func_175623_d(var13);
         if (!var14) {
            var7 = var12;
         } else if (var3.func_175623_d(var13.func_177982_a(0, 1, 0))) {
            var7 = new Vec3d(var12.field_72450_a, var13.func_177956_o() + 1, var12.field_72449_c);
         }
      }

      Vec3d[] var16 = new Vec3d[]{
         var4.func_72441_c(-var5 / 2.0F, 0.0, -var5 / 2.0F),
         var4.func_72441_c(-var5 / 2.0F, 0.0, var5 / 2.0F),
         var4.func_72441_c(var5 / 2.0F, 0.0, -var5 / 2.0F),
         var4.func_72441_c(var5 / 2.0F, 0.0, var5 / 2.0F),
         var4.func_72441_c(-var5 / 2.0F, var6, -var5 / 2.0F),
         var4.func_72441_c(-var5 / 2.0F, var6, var5 / 2.0F),
         var4.func_72441_c(var5 / 2.0F, var6, -var5 / 2.0F),
         var4.func_72441_c(var5 / 2.0F, var6, var5 / 2.0F)
      };

      for (Vec3d var20 : var16) {
         RayTraceResult var21 = this.a(var7, var20, var3);
         if (var21 == null) {
            return true;
         }

         IBlockState var15 = var3.func_180495_p(var21.func_178782_a());
         if (var15.func_185895_e()) {
            return true;
         }

         if (var15.func_177230_c().func_180664_k() != BlockRenderLayer.SOLID) {
            return true;
         }
      }

      return false;
   }

   HashSet<String> a(Boolean var1, boolean var2) {
      if (ClientProxy.IS_PRELOADING) {
         return new HashSet<>();
      }

      HashSet var3;
      if (var1) {
         var3 = ClothingScreen.b_clash815();
      } else {
         var3 = this.j.Y_clash561();
      }

      HashSet var4 = new HashSet();

      for (String var6 : (java.util.Collection<String>) (var3) ) {
         ServerWhitelistManager.b var7 = ServerWhitelistManager.b_clash142(var6);
         if (var7 != null && (var7.a_clash900() || !var2)) {
            var4.addAll(var7.h_clash901());
         }
      }

      return var4;
   }

   public void a(GeoModel var1, T var2, float var3, float var4, float var5, float var6, float var7) {
      if (i.field_71439_g == null || var2.h_clash508() || !var2.d_clash453() || this.a(var2, i.field_71439_g)) {
         GlStateManager.func_179091_B();
         this.a((T)var2, var3, var4, var5, var6, var7);
         this.renderLate((T)var2, var3, var4, var5, var6, var7);
         BufferBuilder var8 = Tessellator.func_178181_a().func_178180_c();
         var8.func_181668_a(7, DefaultVertexFormats.field_181712_l);
         this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
         this.p.clear();
         this.p = this.a(var2.h_clash508(), var2.ah_clash493() == 0);
         this.getSkinTexture((T) this.j);
         BodyParts.a(var2.b_clash552().getModelRendererList(), this.a(), this);
         BodyParts.a_clash795(var2, var3);
         this.a(var1, var8, (T)var2, var4, var5, var6, var7, var3);
         this.renderAfter((T)var2, var3, var4, var5, var6, var7);
         GlStateManager.func_179101_C();
         GlStateManager.func_179089_o();
         GL20.glUseProgram(0);
      }
   }

   protected void a(GeoModel var1, BufferBuilder var2, T var3, float var4, float var5, float var6, float var7, float var8) {
      GeoBone var9 = null;

      for (GeoBone var11 : var1.topLevelBones) {
         if (var11.getName().equals("steve")) {
            var9 = var11;
         } else {
            this.renderRecursively(var2, var11, var4, var5, var6, var7);
         }
      }

      Tessellator.func_178181_a().func_78381_a();
      this.b_clash332();
      if (var9 != null) {
         var2.func_181668_a(7, DefaultVertexFormats.field_181712_l);

         Minecraft.func_71410_x().field_71446_o.func_110577_a(this.getSkinTexture(this.j));

         this.renderRecursively(var2, var9, var4, var5, var6, this.j.v_clash550());
         Tessellator.func_178181_a().func_78381_a();
      }
   }

   String a_clash333(String var1) {
      StringBuilder var2 = new StringBuilder();

      try {
         BufferedReader var3 = new BufferedReader(new FileReader(var1));

         String var4;
         while ((var4 = var3.readLine()) != null) {
            var2.append(var4).append("//\n");
         }

         var3.close();
      } catch (IOException var5) {
         var5.printStackTrace();
      }

      return var2.toString();
   }

   protected void a_clash199(double var1, double var3, double var5) {
      if (!this.j.h_clash508()) {
         if (!this.j.y_clash492().hideNameTag) {
            if (i.func_175598_ae().field_78734_h != null) {
               this.func_147906_a(this.j, this.j.ab_clash540(), var1, var3 + this.j.i_clash226(), var5, 300);
            }
         }
      }
   }

   Vec3d a_clash334(EntityPlayer var1, float var2) {
      EntityLiving var3 = (EntityLiving)var1.func_184187_bx();
      EntityPlayerSP var4 = i.field_71439_g;
      Vec3d var5 = var3.func_70040_Z();
      Vec3d var6 = RotationHelper.a(new Vec3d(var1.field_70142_S, var1.field_70137_T, var1.field_70136_U), var1.func_174791_d(), var2);
      Vec3d var7 = RotationHelper.a(new Vec3d(var4.field_70142_S, var4.field_70137_T, var4.field_70136_U), var4.func_174791_d(), var2);
      var7 = var6.func_178788_d(var7);
      this.j.field_70761_aq = var3.field_70761_aq;
      return new Vec3d(var7.field_72450_a + var5.field_72450_a * -0.5, var7.field_72448_b + 0.15F, var7.field_72449_c + var5.field_72449_c * -0.5);
   }

   protected Vec3d a(T var1, float var2, Vec3d var3) {
      return var3;
   }

   Vec3d a(T var1, float var2, double var3, double var5, double var7) {
      Vec3d var9 = new Vec3d(var3, var5, var7);
      if (var1.field_70170_p instanceof SexWorldClient) {
         return var9;
      }

      if (var1.t_clash283() && (!(var1 instanceof AbstractPlayerGirlEntity) || i.field_71474_y.field_74320_O != 0)) {
         this.a_clash199(var3, var5, var7);
      }

      EntityPlayer var10 = var1.z_clash528();
      if (var10 != null && var10.func_184218_aH() && var10.func_184187_bx() instanceof EntityHorse && ((EntityHorse)var10.func_184187_bx()).func_110257_ck()) {
         return this.a_clash334(var10, var2);
      }

      if (!var1.Q_clash505()) {
         return var9;
      }

      if (!(var1 instanceof AbstractPlayerGirlEntity) || !((AbstractPlayerGirlEntity)var1).f_clash579() || i.field_71474_y.field_74320_O == 0) {
         Vec3d var11 = RotationHelper.a(
            new Vec3d(i.field_71439_g.field_70142_S, i.field_71439_g.field_70137_T, i.field_71439_g.field_70136_U), i.field_71439_g.func_174791_d(), var2
         );
         var9 = var1.o_clash501().func_178788_d(var11);
      }

      float var12 = var1.I_clash415();
      var1.field_70177_z = var12;
      var1.field_70760_ar = var12;
      var1.field_70761_aq = var12;
      var1.field_70758_at = var12;
      var1.field_70759_as = var12;
      return var9;
   }

   protected void b_clash327(T var1) {
   }

   @Override
   public void func_76986_a(T var1, double var2, double var4, double var6, float var8, float var9) {
      this.a(var1, var2, var4, var6, var8, var9);
   }

   @Override
   public void doRender(T var1, double var2, double var4, double var6, float var8, float var9) {
      this.a(var1, var2, var4, var6, var8, var9);
   }

   public void a(T var1, double var2, double var4, double var6, float var8, float var9) {
      this.j = (T)var1;
      Vec3d var10 = this.a((T)var1, var9, var2, var4, var6);
      var10 = this.a((T)var1, var9, var10);
      var2 = var10.field_72450_a;
      var4 = var10.field_72448_b;
      var6 = var10.field_72449_c;
      this.b_clash327((T)var1);
      if (var1.func_110167_bD()) {
         this.a(var1, var2, var4 + this.c, var6, var9);
      }

      GlStateManager.func_179094_E();
      GlStateManager.func_179137_b(var2, var4, var6);
      GL11.glDisable(2896);
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.func_179108_z();
      GlStateManager.func_179147_l();
      GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      boolean var11 = var1.func_184187_bx() != null && var1.func_184187_bx().shouldRiderSit();
      if (var11) {
         EntityModelData var32 = new EntityModelData();
         var32.isSitting = var11;
         var32.isChild = var1.func_70631_g_();
         float var33 = Interpolations.lerpYaw(var1.field_70760_ar, var1.field_70761_aq, var9);
         float var35 = Interpolations.lerpYaw(var1.field_70758_at, var1.field_70759_as, var9);
         float var36 = var35 - var33;
         if (var1.func_184187_bx() instanceof EntityLivingBase) {
            EntityLivingBase var38 = (EntityLivingBase)var1.func_184187_bx();
            var33 = Interpolations.lerpYaw(var38.field_70760_ar, var38.field_70761_aq, var9);
            var36 = var35 - var33;
            float var40 = MathHelper.func_76142_g(var36);
            if (var40 < -85.0F) {
               var40 = -85.0F;
            }

            if (var40 >= 85.0F) {
               var40 = 85.0F;
            }

            var33 = var35 - var40;
            if (var40 * var40 > 2500.0F) {
               var33 += var40 * 0.2F;
            }

            var36 = var35 - var33;
         }

         float var39 = Interpolations.lerp(var1.field_70127_C, var1.field_70125_A, var9);
         float var41 = this.handleRotationFloat((T)var1, var9);
         this.b((T)var1, var41, var33, var9);
         float var42 = 0.0F;
         float var43 = 0.0F;
         var32.headPitch = -var39;
         var32.netHeadYaw = -var36;
         AnimationEvent var44 = new AnimationEvent<>(var1, var43, var42, var9, false, Collections.singletonList(var32));
         GeoModelProvider var45 = super.getGeoModelProvider();
         ResourceLocation var46 = var45.getModelLocation(var1);
         GeoModel var47 = var45.getModel(var46);
         if (var45 instanceof IAnimatableModel) {
            ((IAnimatableModel)var45).setLivingAnimations(var1, var1.func_110124_au().hashCode(), var44);
         }

         GlStateManager.func_179094_E();
         GlStateManager.func_179109_b(0.0F, 0.01F, 0.0F);
         Minecraft.func_71410_x().field_71446_o.func_110577_a(this.getEntityTexture((T)var1));
         software.bernie.geckolib3.core.util.Color var48 = this.getRenderColor((T)var1, var9);
         boolean var49 = this.setDoRenderBrightness((T)var1, var9);
         this.a(var47, (T)var1, var9, var48.getRed() / 255.0F, var48.getBlue() / 255.0F, var48.getGreen() / 255.0F, var48.getAlpha() / 255.0F);
         if (var49) {
            RenderHurtColor.unset();
         }

         for (GeoLayerRenderer var53 : this.layerRenderers) {
            var53.render((T)var1, var43, var42, var9, var43, var36, var39, var48);
         }

         GL11.glEnable(2896);
         GlStateManager.func_179084_k();
         GlStateManager.func_179133_A();
         GlStateManager.func_179121_F();
         GlStateManager.func_179121_F();
         this.a_clash335((T)var1);
         SexSceneRenderer.a_clash810(var1, var9);
         f7 var52 = this.e_clash326((T)var1);
         if (var52 != null) {
            this.a(var1, var9, var52);
         }
      } else {
         EntityModelData var12 = new EntityModelData();
         var12.isSitting = var11;
         var12.isChild = var1.func_70631_g_();
         float var13 = Interpolations.lerpYaw(var1.field_70760_ar, var1.field_70761_aq, var9);
         float var14 = Interpolations.lerpYaw(var1.field_70758_at, var1.field_70759_as, var9);
         float var15 = var14 - var13;
         float var16 = Interpolations.lerp(var1.field_70127_C, var1.field_70125_A, var9);
         float var17 = this.handleRotationFloat((T)var1, var9);
         this.b((T)var1, var17, var13, var9);
         float var18 = 0.0F;
         float var19 = 0.0F;
         if (var1.func_70089_S()) {
            var18 = Interpolations.lerp(var1.field_184618_aE, var1.field_70721_aZ, var9);
            var19 = var1.field_184619_aG - var1.field_70721_aZ * (1.0F - var9);
            if (var1.func_70631_g_()) {
               var19 *= 3.0F;
            }

            if (var18 > 1.0F) {
               var18 = 1.0F;
            }
         }

         var12.headPitch = -var16;
         var12.netHeadYaw = -var15;
         AnimationEvent var20 = new AnimationEvent<>(var1, var19, var18, var9, !(var18 > -0.15F) || !(var18 < 0.15F), Collections.singletonList(var12));
         GeoModelProvider var21 = super.getGeoModelProvider();
         ResourceLocation var22 = var21.getModelLocation(var1);
         GeoModel var23 = var21.getModel(var22);
         if (var21 instanceof IAnimatableModel) {
            ((IAnimatableModel)var21).setLivingAnimations(var1, var1.func_110124_au().hashCode(), var20);
         }

         GlStateManager.func_179094_E();
         GlStateManager.func_179109_b(0.0F, 0.01F, 0.0F);
         Minecraft.func_71410_x().field_71446_o.func_110577_a(this.getEntityTexture((T)var1));
         software.bernie.geckolib3.core.util.Color var24 = this.getRenderColor((T)var1, var9);
         boolean var25 = this.setDoRenderBrightness((T)var1, var9);
         this.a(var23, (T)var1, var9, var24.getRed() / 255.0F, var24.getBlue() / 255.0F, var24.getGreen() / 255.0F, var24.getAlpha() / 255.0F);
         if (var25) {
            RenderHurtColor.unset();
         }

         for (GeoLayerRenderer var27 : this.layerRenderers) {
            var27.render((T)var1, var19, var18, var9, var19, var15, var16, var24);
         }

         GL11.glEnable(2896);
         GlStateManager.func_179084_k();
         GlStateManager.func_179133_A();
         GlStateManager.func_179121_F();
         GlStateManager.func_179121_F();
         this.a_clash335((T)var1);
         SexSceneRenderer.a_clash810(var1, var9);
         f7 var50 = this.e_clash326((T)var1);
         if (var50 != null) {
            this.a(var1, var9, var50);
         }
      }
   }

   void a_clash335(T var1) {
      ArrayList var2 = new ArrayList<>(GirlModel.e);
      var2.addAll(var1.p);

      for (String var4 : (java.util.Collection<String>) (var2) ) {
         MatrixStack var5 = var1.a(var4, !var1.h_clash508());
         Matrix4f var6 = var5.getModelMatrix();
         Vec3d var7 = new Vec3d(-var6.m03, var6.m13, -var6.m23);
         var1.a(var4, var7);
      }
   }

   @Nullable
   protected f7 e_clash326(T var1) {
      return null;
   }

   public Entity c_clash336(BaseGirlEntity var1) {
      return var1;
   }

   void a(BaseGirlEntity var1, float var2, f7 var3) {
      EntityPlayerSP var4 = i.field_71439_g;
      var3 = new f7(var3.a / 255.0F, var3.c / 255.0F, var3.b / 255.0F);
      Tessellator var5 = Tessellator.func_178181_a();
      BufferBuilder var6 = var5.func_178180_c();
      GlStateManager.func_179094_E();
      GlStateManager.func_179137_b(0.0, 0.01, 0.0);
      Entity var7 = this.c_clash336(var1);
      Vec3d var8 = var1.Q_clash505()
         ? var1.o_clash501()
         : RotationHelper.a(new Vec3d(var7.field_70142_S, var7.field_70137_T, var7.field_70136_U), var7.func_174791_d(), var2);
      Vec3d var9 = RotationHelper.a(new Vec3d(var4.field_70142_S, var4.field_70137_T, var4.field_70136_U), var4.func_174791_d(), var2);
      Vec3d var10 = var8.func_178788_d(var9);
      GlStateManager.func_179137_b(var10.field_72450_a, var10.field_72448_b, var10.field_72449_c);
      i.func_110434_K().func_110577_a(e);
      float var11 = a(var1, var2, 1.0F, 5.0F);
      this.b(var5, var6, var1, var3, var11);
      GlStateManager.func_179121_F();
   }

   protected static float a(BaseGirlEntity var0, float var1, float var2, float var3) {
      EntityPlayerSP var4 = i.field_71439_g;
      Entity var5 = ((GirlRenderer)i.func_175598_ae().func_78713_a(var0)).c_clash336(var0);
      Vec3d var6 = var0.Q_clash505()
         ? var0.o_clash501()
         : RotationHelper.a(new Vec3d(var5.field_70142_S, var5.field_70137_T, var5.field_70136_U), var5.func_174791_d(), var1);
      Vec3d var7 = RotationHelper.a(new Vec3d(var4.field_70142_S, var4.field_70137_T, var4.field_70136_U), var4.func_174791_d(), var1);
      Vec3d var8 = ActiveRenderInfo.getCameraPosition().func_178787_e(var7);
      float var9 = (float)var8.func_72438_d(var6);
      float var10 = Math.abs(var9) / 5.0F;
      return RotationHelper.a_clash25(var3, var2, ThreadNames.b(var10, 0.0F, 1.0F));
   }

   protected void b(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, f7 var4, float var5) {
   }

   protected static void a(BufferBuilder var0, Tessellator var1, BaseGirlEntity var2, String var3, String var4, float var5, float var6, float var7, float var8) {
      var0.func_181668_a(1, DefaultVertexFormats.field_181709_i);
      GlStateManager.func_187441_d(var8);
      Vec3d var9 = var2.b_clash547(var3);
      Vec3d var10 = var2.b_clash547(var4);
      var0.func_181662_b(var9.field_72450_a, var9.field_72448_b, var9.field_72449_c)
         .func_187315_a(0.0, 0.0)
         .func_181666_a(var5, var6, var7, 1.0F)
         .func_181675_d();
      var0.func_181662_b(var10.field_72450_a, var10.field_72448_b, var10.field_72449_c)
         .func_187315_a(0.0, 0.0)
         .func_181666_a(var5, var6, var7, 1.0F)
         .func_181675_d();
      var1.func_78381_a();
   }

   protected static void a(Tessellator var0, BufferBuilder var1, BaseGirlEntity var2, f7 var3, float var4) {
      a(var1, var0, var2, "braStringMidStartR", "braStringMidMid1R", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidMid1R", "braStringMidMid2R", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidMid2R", "braStringMidMid3R", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidMid3R", "braStringMidEndR", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidEndR", "braStringBackR", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringBackR", "braStringRightEndR", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringRightEndR", "braStringRightStartR", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringRightR", "braStringRightL", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidStartL", "braStringMidMid1L", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidMid1L", "braStringMidMid2L", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidMid2L", "braStringMidMid3L", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidMid3L", "braStringMidEndL", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringMidEndL", "braStringBackL", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringBackL", "braStringLeftEndL", var3.a, var3.c, var3.b, var4);
      a(var1, var0, var2, "braStringLeftEndL", "braStringLeftStartL", var3.a, var3.c, var3.b, var4);
   }

   protected void b(T var1, float var2, float var3, float var4) {
      super.applyRotations((T)var1, var2, var3, var4);
      if (var1 instanceof AbstractPlayerGirlEntity) {
         UUID var5 = ((AbstractPlayerGirlEntity)var1).m_clash583();
         if (var5 != null) {
            EntityPlayer var6 = var1.field_70170_p.func_152378_a(var5);
            if (var6 != null) {
               if (var6.func_184613_cA()) {
                  float var7 = var6.func_184599_cB() + var4;
                  float var8 = MathHelper.func_76131_a(var7 * var7 / 100.0F, 0.0F, 1.0F);
                  GlStateManager.func_179114_b(var8 * (-90.0F - var6.field_70125_A), 1.0F, 0.0F, 0.0F);
                  Vec3d var9 = var6.func_70676_i(var4);
                  double var10 = var6.field_70159_w * var6.field_70159_w + var6.field_70179_y * var6.field_70179_y;
                  double var12 = var9.field_72450_a * var9.field_72450_a + var9.field_72449_c * var9.field_72449_c;
                  if (var10 > 0.0 && var12 > 0.0) {
                     double var14 = (var6.field_70159_w * var9.field_72450_a + var6.field_70179_y * var9.field_72449_c) / (Math.sqrt(var10) * Math.sqrt(var12));
                     double var16 = var6.field_70159_w * var9.field_72449_c - var6.field_70179_y * var9.field_72450_a;
                     GlStateManager.func_179114_b((float)(Math.signum(var16) * Math.acos(var14)) * 180.0F / (float) Math.PI, 0.0F, 1.0F, 0.0F);
                  }
               }
            }
         }
      }
   }

   protected void a(BufferBuilder var1, String var2, GeoBone var3) {
   }

   protected void a(BaseGirlEntity var1, double var2, double var4, double var6, float var8) {
      Entity var9 = var1.func_110166_bE();
      var4 -= (1.6 - var1.field_70131_O) * 0.5;
      Tessellator var10 = Tessellator.func_178181_a();
      BufferBuilder var11 = var10.func_178180_c();
      double var12 = RotationHelper.a_clash25(var9.field_70126_B, var9.field_70177_z, var8 * 0.5F) * (float) (Math.PI / 180.0);
      double var14 = RotationHelper.a_clash25(var9.field_70127_C, var9.field_70125_A, var8 * 0.5F) * (float) (Math.PI / 180.0);
      double var16 = Math.cos(var12);
      double var18 = Math.sin(var12);
      double var20 = Math.sin(var14);
      if (var9 instanceof EntityHanging) {
         var16 = 0.0;
         var18 = 0.0;
         var20 = -1.0;
      }

      double var22 = Math.cos(var14);
      double var24 = RotationHelper.b(var9.field_70169_q, var9.field_70165_t, var8) - var16 * 0.7 - var18 * 0.5 * var22;
      double var26 = RotationHelper.b(var9.field_70167_r + var9.func_70047_e() * 0.7, var9.field_70163_u + var9.func_70047_e() * 0.7, var8)
         - var20 * 0.5
         - 0.25;
      double var28 = RotationHelper.b(var9.field_70166_s, var9.field_70161_v, var8) - var18 * 0.7 + var16 * 0.5 * var22;
      double var30 = RotationHelper.a_clash25(var1.field_70760_ar, var1.field_70761_aq, var8) * (float) (Math.PI / 180.0) + (Math.PI / 2);
      var16 = Math.cos(var30) * var1.field_70130_N * 0.4;
      var18 = Math.sin(var30) * var1.field_70130_N * 0.4;
      double var32 = RotationHelper.b(var1.field_70169_q, var1.field_70165_t, var8) + var16;
      double var34 = RotationHelper.b(var1.field_70167_r, var1.field_70163_u, var8);
      double var36 = RotationHelper.b(var1.field_70166_s, var1.field_70161_v, var8) + var18;
      var2 += var16;
      var6 += var18;
      double var38 = (float)(var24 - var32);
      double var40 = (float)(var26 - var34);
      double var42 = (float)(var28 - var36);
      GlStateManager.func_179090_x();
      GlStateManager.func_179140_f();
      GlStateManager.func_179129_p();
      var11.func_181668_a(5, DefaultVertexFormats.field_181706_f);

      for (int var44 = 0; var44 <= 24; var44++) {
         float var45 = 0.5F;
         float var46 = 0.4F;
         float var47 = 0.3F;
         if (var44 % 2 == 0) {
            var45 = 0.35F;
            var46 = 0.28F;
            var47 = 0.21000001F;
         }

         float var48 = var44 / 24.0F;
         var11.func_181662_b(
               var2 + var38 * var48 + 0.0, var4 + var40 * (var48 * var48 + var48) * 0.5 + ((24.0F - var44) / 18.0F + 0.125F), var6 + var42 * var48
            )
            .func_181666_a(var45, var46, var47, 1.0F)
            .func_181675_d();
         var11.func_181662_b(
               var2 + var38 * var48 + 0.025, var4 + var40 * (var48 * var48 + var48) * 0.5 + ((24.0F - var44) / 18.0F + 0.125F) + 0.025, var6 + var42 * var48
            )
            .func_181666_a(var45, var46, var47, 1.0F)
            .func_181675_d();
      }

      var10.func_78381_a();
      var11.func_181668_a(5, DefaultVertexFormats.field_181706_f);

      for (int var54 = 0; var54 <= 24; var54++) {
         float var55 = 0.5F;
         float var56 = 0.4F;
         float var57 = 0.3F;
         if (var54 % 2 == 0) {
            var55 = 0.35F;
            var56 = 0.28F;
            var57 = 0.21000001F;
         }

         float var58 = var54 / 24.0F;
         var11.func_181662_b(
               var2 + var38 * var58 + 0.0, var4 + var40 * (var58 * var58 + var58) * 0.5 + ((24.0F - var54) / 18.0F + 0.125F) + 0.025, var6 + var42 * var58
            )
            .func_181666_a(var55, var56, var57, 1.0F)
            .func_181675_d();
         var11.func_181662_b(
               var2 + var38 * var58 + 0.025, var4 + var40 * (var58 * var58 + var58) * 0.5 + ((24.0F - var54) / 18.0F + 0.125F), var6 + var42 * var58 + 0.025
            )
            .func_181666_a(var55, var56, var57, 1.0F)
            .func_181675_d();
      }

      var10.func_78381_a();
      GlStateManager.func_179145_e();
      GlStateManager.func_179098_w();
      GlStateManager.func_179089_o();
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      if (!(this.j.field_70170_p instanceof SexWorldClient)) {
         String var7 = var2.getName();
         if (var7.equals("weapon") && this.j instanceof AbstractGirlNpcEntity) {
            this.a(var1, var2);
         }

         if (var7.equals("itemRenderer") && this.j.y_clash492() == fp.PAYMENT) {
            this.b(var1, var2);
         }

         if (var7.equals("ballL") || var7.equals("ballR") || var7.equals("cock")) {
            var6 = 1.0F;
         }

         n = var1;
         this.a(var1, var7, var2);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if ("Head2".equals(var7) && !this.c_clash339()) {
            MATRIX_STACK.pop();
         } else if (!this.b_clash338(var7)) {
            MATRIX_STACK.pop();
         } else {
            if (!var2.isHidden) {
               Vector4f var8 = this.a(var7, var3, var4, var5);
               var3 = var8.x;
               var4 = var8.y;
               var5 = var8.z;
               double var9 = var8.w;
               if (!this.p.contains(var7)) {
                  for (GeoCube var12 : var2.childCubes) {
                     MATRIX_STACK.push();
                     this.q = var2;
                     this.a(var1, var12, var3, var4, var5, var6, var9);
                     MATRIX_STACK.pop();
                  }
               }

               for (GeoBone var18 : var2.childBones) {
                  if (var9 == 0.0) {
                     this.renderRecursively(var1, var18, var3, var4, var5, var6);
                  } else {
                     this.a(var1, var18, var3, var4, var5, var6, var9);
                  }
               }
            }

            try {
               MATRIX_STACK.pop();
            } catch (IllegalStateException var13) {
            }
         }
      }
   }

   protected Vector4f a_clash337(float var1, float var2, float var3) {
      return new Vector4f(var1, var2, var3, 0.0F);
   }

   boolean b_clash338(String var1) {
      return !var1.startsWith("armor") ? true : this.j instanceof AbstractGirlNpcEntity;
   }

   protected Vector4f a(String var1, float var2, float var3, float var4) {
      if (!var1.startsWith("armor")) {
         return this.a_clash337(var2, var3, var4);
      }

      if (!(this.j instanceof AbstractGirlNpcEntity)) {
         return this.a_clash337(var2, var3, var4);
      }

      if ((Integer)this.j.m.func_187225_a(BaseGirlEntity.D) == 0) {
         return this.a_clash337(var2, var3, var4);
      }

      GeoModelProvider var5 = this.getGeoModelProvider();
      if (!(var5 instanceof GirlModel)) {
         return this.a_clash337(var2, var3, var4);
      }

      GirlModel var6 = (GirlModel)var5;
      ItemStack var7 = var6.a_clash348(this.j, var1);
      if (!(var7.func_77973_b() instanceof ItemArmor)) {
         return this.a_clash337(var2, var3, var4);
      }

      ItemArmor var8 = (ItemArmor)var7.func_77973_b();
      ArmorMaterial var9 = var8.func_82812_d();
      float var10 = 0.0F;
      switch (var9) {
         case GOLD:
            var10 = 1.0F;
            break;
         case CHAIN:
         case IRON:
            var10 = 2.0F;
            break;
         case LEATHER:
            var10 = 4.0F;
            int var11 = var8.func_82814_b(var7);
            float var12 = (var11 >> 16 & 0xFF) / 255.0F;
            float var13 = (var11 >> 8 & 0xFF) / 255.0F;
            float var14 = (var11 & 0xFF) / 255.0F;
            var2 *= var12;
            var3 *= var13;
            var4 *= var14;
      }

      return new Vector4f(var2, var3, var4, 72.0F * var10 / 4096.0F);
   }

   public void a(T var1, float var2, float var3, float var4, float var5, float var6) {
      this.g = (Matrix4f)MATRIX_STACK.getModelMatrix().clone();
   }

   public void a(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6, double var7) {
      if (!(this.j.field_70170_p instanceof SexWorldClient)) {
         String var9 = var2.getName();
         if (var9.equals("weapon")) {
            this.a(var1, var2);
         }

         if (var9.equals("ballL") || var9.equals("ballR") || var9.equals("cock")) {
            var6 = 1.0F;
         }

         this.a(var1, var2.getName(), var2);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if (!var2.isHidden) {
            if (!this.p.contains(var9)) {
               for (GeoCube var11 : var2.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.func_179094_E();
                  this.q = var2;
                  this.a(var1, var11, var3, var4, var5, var6, var7);
                  GlStateManager.func_179121_F();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone var13 : var2.childBones) {
               this.a(var1, var13, var3, var4, var5, var6, var7);
            }
         }

         MATRIX_STACK.pop();
      }
   }

   protected boolean c_clash339() {
      return !this.j.n_clash537() ? true : i.field_71474_y.field_74320_O != 0;
   }

   public void a(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6, double var7) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);

      for (GeoQuad var12 : var2.quads) {
         if (var12 != null) {
            Vector3f var13 = new Vector3f(var12.normal.func_177958_n(), var12.normal.func_177956_o(), var12.normal.func_177952_p());
            MATRIX_STACK.getNormalMatrix().transform(var13);
            if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var13.getX() < 0.0F) {
               var13.x *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var13.getY() < 0.0F) {
               var13.y *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var13.getZ() < 0.0F) {
               var13.z *= -1.0F;
            }

            Vec3d var14 = BodyParts.a(this, this.q, new Vec3d(var3, var4, var5), var13);

            for (GeoVertex var18 : var12.vertices) {
               Vector4f var19 = new Vector4f(var18.position.getX(), var18.position.getY(), var18.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var19);
               var1.func_181662_b(var19.getX(), var19.getY(), var19.getZ())
                  .func_187315_a(var18.textureU + var7, var18.textureV)
                  .func_181666_a((float)var14.field_72450_a, (float)var14.field_72448_b, (float)var14.field_72449_c, var6)
                  .func_181663_c(var13.getX(), var13.getY(), var13.getZ())
                  .func_181675_d();
            }
         }
      }
   }

   protected ItemStack a_clash340() {
      switch ((String)this.j.m.func_187225_a(BaseGirlEntity.h)) {
         case "doggy":
            return new ItemStack(Items.field_151045_i, 2);
         case "blowjob":
            return new ItemStack(Items.field_151166_bC, 3);
         case "strip":
            return new ItemStack(Items.field_151043_k, 1);
         case "boobjob":
            return new ItemStack(Items.field_151079_bi, 2);
         case "touch_boobs":
            return new ItemStack(Items.field_151115_aP, 2, 1);
         case "sex":
            return new ItemStack(Items.field_151115_aP, 3, 0);
         default:
            return null;
      }
   }

   protected void b(BufferBuilder var1, GeoBone var2) {
      ItemStack var3 = this.a_clash340();
      if (var3 != null) {
         ItemRenderer var4 = Minecraft.func_71410_x().func_175597_ag();

         for (int var5 = 0; var5 < var3.func_190916_E(); var5++) {
            GlStateManager.func_179094_E();
            Tessellator.func_178181_a().func_78381_a();
            com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
            GL11.glEnable(2896);
            GL11.glRotated(var2.getRotationX() + 2.5, 0.0, 0.0, 1.0);
            GL11.glRotated(var2.getRotationY(), 0.0, 1.0, 0.0);
            GL11.glRotated(var2.getRotationZ(), 1.0, 0.0, 0.0);
            switch (var5) {
               case 1:
                  GL11.glRotated(-15.0, 0.0, 0.0, 1.0);
                  GlStateManager.func_179137_b(0.0, 0.0, -0.025);
                  break;
               case 2:
                  GL11.glRotated(15.0, 0.0, 0.0, 1.0);
                  GlStateManager.func_179137_b(0.0, 0.0, 0.025);
            }

            GlStateManager.func_179152_a(this.j.n, this.j.n, this.j.n);
            var4.func_178099_a(this.j, new ItemStack(var3.func_77973_b(), 1), TransformType.THIRD_PERSON_RIGHT_HAND);
            this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
            var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
            GL11.glDisable(2896);
            GlStateManager.func_179121_F();
         }
      }
   }

   protected ItemStack a_clash341(@Nullable ItemStack var1) {
      return var1;
   }

   protected void a(BufferBuilder var1, GeoBone var2) {
      if (this.j != null) {
         if (this.j instanceof AbstractGirlNpcEntity) {
            EntityDataManager var3 = this.j.func_184212_Q();
            AbstractGirlNpcEntity var4 = (AbstractGirlNpcEntity)this.j;
            int var5 = (Integer)var3.func_187225_a(AbstractGirlNpcEntity.M);
            if (var4.y_clash492() != fp.BOW) {
               this.a = 0.0F;
            }

            ItemStack var6 = null;
            if (var5 == 1) {
               var6 = (ItemStack)var3.func_187225_a(AbstractGirlNpcEntity.L);
            } else if (var5 == 2) {
               var6 = (ItemStack)var3.func_187225_a(AbstractGirlNpcEntity.R);
            }

            var6 = this.a_clash341(var6);
            if (var6 != null) {
               if (var6.func_77973_b().equals(Items.field_151031_f) && var4.y_clash492() == fp.BOW) {
                  this.a += 0.015F;
                  var4.d(Math.round(-this.a * 20.0F + var6.func_77988_m()));
                  var4.a_clash517(var6);
               }

               GlStateManager.func_179094_E();
               Tessellator.func_178181_a().func_78381_a();
               com.trolmastercard.sexmod.MatrixHelper.a(MATRIX_STACK, var2);
               GL11.glEnable(2896);
               if (var6.func_77973_b() instanceof ItemBow) {
                  GL11.glRotatef(var4.K, 1.0F, 0.0F, 0.0F);
               } else if (var4.y_clash492() == fp.ATTACK && var4.S == 0) {
                  GlStateManager.func_179137_b(var4.V.field_72450_a, var4.V.field_72448_b, var4.V.field_72449_c);
                  GL11.glRotatef(var4.O, 1.0F, 0.0F, 0.0F);
               } else {
                  GL11.glRotatef(var4.P, 1.0F, 0.0F, 0.0F);
               }

               Minecraft.func_71410_x().func_175597_ag().func_178099_a(this.j, var6, TransformType.THIRD_PERSON_RIGHT_HAND);
               this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
               var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
               GL11.glDisable(2896);
               GlStateManager.func_179121_F();
            }
         }
      }
   }

   RayTraceResult a(Vec3d var1, Vec3d var2, World var3) {
      if (Double.isNaN(var1.field_72450_a) || Double.isNaN(var1.field_72448_b) || Double.isNaN(var1.field_72449_c)) {
         return null;
      }

      if (!Double.isNaN(var2.field_72450_a) && !Double.isNaN(var2.field_72448_b) && !Double.isNaN(var2.field_72449_c)) {
         int var4 = MathHelper.func_76128_c(var2.field_72450_a);
         int var5 = MathHelper.func_76128_c(var2.field_72448_b);
         int var6 = MathHelper.func_76128_c(var2.field_72449_c);
         int var7 = MathHelper.func_76128_c(var1.field_72450_a);
         int var8 = MathHelper.func_76128_c(var1.field_72448_b);
         int var9 = MathHelper.func_76128_c(var1.field_72449_c);
         BlockPos var10 = new BlockPos(var7, var8, var9);
         IBlockState var11 = var3.func_180495_p(var10);
         if (var11.func_185890_d(var3, var10) != Block.field_185506_k && var11.func_177230_c().func_180664_k() == BlockRenderLayer.SOLID) {
            return var11.func_185910_a(var3, var10, var1, var2);
         }

         int var12 = 200;

         while (var12-- >= 0) {
            if (Double.isNaN(var1.field_72450_a) || Double.isNaN(var1.field_72448_b) || Double.isNaN(var1.field_72449_c)) {
               return null;
            }

            if (var7 == var4 && var8 == var5 && var9 == var6) {
               return null;
            }

            boolean var13 = true;
            boolean var14 = true;
            boolean var15 = true;
            double var16 = 999.0;
            double var18 = 999.0;
            double var20 = 999.0;
            if (var4 > var7) {
               var16 = var7 + 1.0;
            } else if (var4 < var7) {
               var16 = var7 + 0.0;
            } else {
               var13 = false;
            }

            if (var5 > var8) {
               var18 = var8 + 1.0;
            } else if (var5 < var8) {
               var18 = var8 + 0.0;
            } else {
               var14 = false;
            }

            if (var6 > var9) {
               var20 = var9 + 1.0;
            } else if (var6 < var9) {
               var20 = var9 + 0.0;
            } else {
               var15 = false;
            }

            double var22 = 999.0;
            double var24 = 999.0;
            double var26 = 999.0;
            double var28 = var2.field_72450_a - var1.field_72450_a;
            double var30 = var2.field_72448_b - var1.field_72448_b;
            double var32 = var2.field_72449_c - var1.field_72449_c;
            if (var13) {
               var22 = (var16 - var1.field_72450_a) / var28;
            }

            if (var14) {
               var24 = (var18 - var1.field_72448_b) / var30;
            }

            if (var15) {
               var26 = (var20 - var1.field_72449_c) / var32;
            }

            if (var22 == -0.0) {
               var22 = -1.0E-4;
            }

            if (var24 == -0.0) {
               var24 = -1.0E-4;
            }

            if (var26 == -0.0) {
               var26 = -1.0E-4;
            }

            EnumFacing var34;
            if (var22 < var24 && var22 < var26) {
               var34 = var4 > var7 ? EnumFacing.WEST : EnumFacing.EAST;
               var1 = new Vec3d(var16, var1.field_72448_b + var30 * var22, var1.field_72449_c + var32 * var22);
            } else if (var24 < var26) {
               var34 = var5 > var8 ? EnumFacing.DOWN : EnumFacing.UP;
               var1 = new Vec3d(var1.field_72450_a + var28 * var24, var18, var1.field_72449_c + var32 * var24);
            } else {
               var34 = var6 > var9 ? EnumFacing.NORTH : EnumFacing.SOUTH;
               var1 = new Vec3d(var1.field_72450_a + var28 * var26, var1.field_72448_b + var30 * var26, var20);
            }

            var7 = MathHelper.func_76128_c(var1.field_72450_a) - (var34 == EnumFacing.EAST ? 1 : 0);
            var8 = MathHelper.func_76128_c(var1.field_72448_b) - (var34 == EnumFacing.UP ? 1 : 0);
            var9 = MathHelper.func_76128_c(var1.field_72449_c) - (var34 == EnumFacing.SOUTH ? 1 : 0);
            var10 = new BlockPos(var7, var8, var9);
            IBlockState var35 = var3.func_180495_p(var10);
            if ((var35.func_185904_a() == Material.field_151567_E || var35.func_185890_d(var3, var10) != Block.field_185506_k)
               && var35.func_177230_c().func_180664_k() == BlockRenderLayer.SOLID) {
               return var35.func_185910_a(var3, var10, var1, var2);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Exception b(Exception var0) {
      return var0;
   }
}
