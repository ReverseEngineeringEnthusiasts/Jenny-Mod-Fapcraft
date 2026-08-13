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
      i = Minecraft.getMinecraft();
      this.shadowSize = 0.2F;
   }

   @Override
   public ResourceLocation getEntityTexture(T var1) {
      return super.getEntityTexture(var1);
   }

   protected ResourceLocation getSkinTexture(T var1) {
      ResourceLocation var2;
      if (!(var1.world instanceof SexWorldClient) && var1.getInteractionPlayerUUID() != null) {
         var2 = l.get(var1.getInteractionPlayerUUID());
         if (var2 == null) {
            return this.a_clash329(var1.getInteractionPlayerUUID(), var1.world);
         }
      } else {
         var2 = l.get(i.getSession().getProfile().getId());
         if (var2 == null) {
            return this.a_clash329(i.getSession().getProfile().getId(), var1.world);
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
            var3 = ImageIO.read(i.getResourceManager().getResource(new ResourceLocation("sexmod", "textures/player/steve.png")).getInputStream());
         } catch (Exception var6) {
            var3 = new BufferedImage(64, 64, 2);
         }
      }

      l.put(var1, this.renderManager.renderEngine.getDynamicTextureLocation("player" + var1, new DynamicTexture(var3)));
      return l.get(var1);
   }

   protected static float a_clash330(BaseGirlEntity var0, float var1) {
      return var0.isAnchored() ? var0.getYawRotation() : RotationHelper.lerp(var0.prevRenderYawOffset, var0.renderYawOffset, var1);
   }

   protected void d_clash331() {
   }

   protected void b_clash332() {
   }

   float a(World var1, Vec3d var2, float var3, float var4) {
      RayTraceResult var5 = this.a(var2, var2.add(ck.a(new Vec3d(0.0, 0.0, -4.0), var3, var4)), var1);
      if (var5 == null) {
         return 4.0F;
      }

      Vec3d var6 = var5.hitVec;
      return var6 == null ? 4.0F : (float)var2.distanceTo(var6);
   }

   boolean a(T var1, EntityPlayer var2) {
      if (var1 instanceof AbstractPlayerGirlEntity) {
         return true;
      }

      World var3 = var1.world;
      Vec3d var4 = var1.getPositionVector();
      float var5 = var1.width * 1.5F;
      float var6 = var1.height * 1.5F;
      Vec3d var7 = var2.getPositionVector().add(0.0, var2.getEyeHeight(), 0.0);
      int var8 = i.gameSettings.thirdPersonView;
      if (var8 != 0) {
         return true;
      }

      if (var8 > 0) {
         float var9 = var2.rotationYaw;
         float var10 = var2.rotationPitch;
         if (var8 == 2) {
            var10 += 180.0F;
         }

         float var11 = 4.0F;
         Vec3d var12 = var7.add(
            MathHelper.sin(var9 * (float) (Math.PI / 180.0)) * MathHelper.cos(var10 * (float) (Math.PI / 180.0)) * var11,
            MathHelper.sin(var10 * (float) (Math.PI / 180.0)) * var11,
            -MathHelper.cos(var9 * (float) (Math.PI / 180.0)) * MathHelper.cos(var10 * (float) (Math.PI / 180.0)) * var11
         );
         BlockPos var13 = new BlockPos(var12);
         boolean var14 = var3.isAirBlock(var13);
         if (!var14) {
            var7 = var12;
         } else if (var3.isAirBlock(var13.add(0, 1, 0))) {
            var7 = new Vec3d(var12.x, var13.getY() + 1, var12.z);
         }
      }

      Vec3d[] var16 = new Vec3d[]{
         var4.add(-var5 / 2.0F, 0.0, -var5 / 2.0F),
         var4.add(-var5 / 2.0F, 0.0, var5 / 2.0F),
         var4.add(var5 / 2.0F, 0.0, -var5 / 2.0F),
         var4.add(var5 / 2.0F, 0.0, var5 / 2.0F),
         var4.add(-var5 / 2.0F, var6, -var5 / 2.0F),
         var4.add(-var5 / 2.0F, var6, var5 / 2.0F),
         var4.add(var5 / 2.0F, var6, -var5 / 2.0F),
         var4.add(var5 / 2.0F, var6, var5 / 2.0F)
      };

      for (Vec3d var20 : var16) {
         RayTraceResult var21 = this.a(var7, var20, var3);
         if (var21 == null) {
            return true;
         }

         IBlockState var15 = var3.getBlockState(var21.getBlockPos());
         if (var15.isTranslucent()) {
            return true;
         }

         if (var15.getBlock().getRenderLayer() != BlockRenderLayer.SOLID) {
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
         var3 = this.j.getCustomPartsSet();
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
      if (i.player == null || var2.isLocallyRegistered() || !var2.d_clash453() || this.a(var2, i.player)) {
         GlStateManager.enableRescaleNormal();
         this.a((T)var2, var3, var4, var5, var6, var7);
         this.renderLate((T)var2, var3, var4, var5, var6, var7);
         BufferBuilder var8 = Tessellator.getInstance().getBuffer();
         var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
         this.p.clear();
         this.p = this.a(var2.isLocallyRegistered(), var2.getOutfitIndex() == 0);
         this.getSkinTexture((T) this.j);
         BodyParts.a(var2.getAnimationProcessor().getModelRendererList(), this.a(), this);
         BodyParts.a_clash795(var2, var3);
         this.a(var1, var8, (T)var2, var4, var5, var6, var7, var3);
         this.renderAfter((T)var2, var3, var4, var5, var6, var7);
         GlStateManager.disableRescaleNormal();
         GlStateManager.enableCull();
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

      Tessellator.getInstance().draw();
      this.b_clash332();
      if (var9 != null) {
         var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

         Minecraft.getMinecraft().renderEngine.bindTexture(this.getSkinTexture(this.j));

         this.renderRecursively(var2, var9, var4, var5, var6, this.j.v_clash550());
         Tessellator.getInstance().draw();
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
      if (!this.j.isLocallyRegistered()) {
         if (!this.j.getCurrentAction().hideNameTag) {
            if (i.getRenderManager().renderViewEntity != null) {
               this.renderLivingLabel(this.j, this.j.ab_clash540(), var1, var3 + this.j.i_clash226(), var5, 300);
            }
         }
      }
   }

   Vec3d a_clash334(EntityPlayer var1, float var2) {
      EntityLiving var3 = (EntityLiving)var1.getRidingEntity();
      EntityPlayerSP var4 = i.player;
      Vec3d var5 = var3.getLookVec();
      Vec3d var6 = RotationHelper.a(new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ), var1.getPositionVector(), var2);
      Vec3d var7 = RotationHelper.a(new Vec3d(var4.lastTickPosX, var4.lastTickPosY, var4.lastTickPosZ), var4.getPositionVector(), var2);
      var7 = var6.subtract(var7);
      this.j.renderYawOffset = var3.renderYawOffset;
      return new Vec3d(var7.x + var5.x * -0.5, var7.y + 0.15F, var7.z + var5.z * -0.5);
   }

   protected Vec3d a(T var1, float var2, Vec3d var3) {
      return var3;
   }

   Vec3d a(T var1, float var2, double var3, double var5, double var7) {
      Vec3d var9 = new Vec3d(var3, var5, var7);
      if (var1.world instanceof SexWorldClient) {
         return var9;
      }

      if (var1.t_clash283() && (!(var1 instanceof AbstractPlayerGirlEntity) || i.gameSettings.thirdPersonView != 0)) {
         this.a_clash199(var3, var5, var7);
      }

      EntityPlayer var10 = var1.z_clash528();
      if (var10 != null && var10.isRiding() && var10.getRidingEntity() instanceof EntityHorse && ((EntityHorse)var10.getRidingEntity()).isHorseSaddled()) {
         return this.a_clash334(var10, var2);
      }

      if (!var1.isAnchored()) {
         return var9;
      }

      if (!(var1 instanceof AbstractPlayerGirlEntity) || !((AbstractPlayerGirlEntity)var1).f_clash579() || i.gameSettings.thirdPersonView == 0) {
         Vec3d var11 = RotationHelper.a(
            new Vec3d(i.player.lastTickPosX, i.player.lastTickPosY, i.player.lastTickPosZ), i.player.getPositionVector(), var2
         );
         var9 = var1.getTargetPosition().subtract(var11);
      }

      float var12 = var1.getYawRotation();
      var1.rotationYaw = var12;
      var1.prevRenderYawOffset = var12;
      var1.renderYawOffset = var12;
      var1.prevRotationYawHead = var12;
      var1.rotationYawHead = var12;
      return var9;
   }

   protected void b_clash327(T var1) {
   }

   @Override
   public void doRender(T var1, double var2, double var4, double var6, float var8, float var9) {
      this.a(var1, var2, var4, var6, var8, var9);
   }

   public void a(T var1, double var2, double var4, double var6, float var8, float var9) {
      this.j = (T)var1;
      Vec3d var10 = this.a((T)var1, var9, var2, var4, var6);
      var10 = this.a((T)var1, var9, var10);
      var2 = var10.x;
      var4 = var10.y;
      var6 = var10.z;
      this.b_clash327((T)var1);
      if (var1.getLeashed()) {
         this.a(var1, var2, var4 + this.c, var6, var9);
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(var2, var4, var6);
      GL11.glDisable(2896);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.enableNormalize();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      boolean var11 = var1.getRidingEntity() != null && var1.getRidingEntity().shouldRiderSit();
      if (var11) {
         EntityModelData var32 = new EntityModelData();
         var32.isSitting = var11;
         var32.isChild = var1.isChild();
         float var33 = Interpolations.lerpYaw(var1.prevRenderYawOffset, var1.renderYawOffset, var9);
         float var35 = Interpolations.lerpYaw(var1.prevRotationYawHead, var1.rotationYawHead, var9);
         float var36 = var35 - var33;
         if (var1.getRidingEntity() instanceof EntityLivingBase) {
            EntityLivingBase var38 = (EntityLivingBase)var1.getRidingEntity();
            var33 = Interpolations.lerpYaw(var38.prevRenderYawOffset, var38.renderYawOffset, var9);
            var36 = var35 - var33;
            float var40 = MathHelper.wrapDegrees(var36);
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

         float var39 = Interpolations.lerp(var1.prevRotationPitch, var1.rotationPitch, var9);
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
            ((IAnimatableModel)var45).setLivingAnimations(var1, var1.getUniqueID().hashCode(), var44);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.01F, 0.0F);
         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture((T)var1));
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
         GlStateManager.disableBlend();
         GlStateManager.disableNormalize();
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
         this.a_clash335((T)var1);
         SexSceneRenderer.a_clash810(var1, var9);
         f7 var52 = this.e_clash326((T)var1);
         if (var52 != null) {
            this.a(var1, var9, var52);
         }
      } else {
         EntityModelData var12 = new EntityModelData();
         var12.isSitting = var11;
         var12.isChild = var1.isChild();
         float var13 = Interpolations.lerpYaw(var1.prevRenderYawOffset, var1.renderYawOffset, var9);
         float var14 = Interpolations.lerpYaw(var1.prevRotationYawHead, var1.rotationYawHead, var9);
         float var15 = var14 - var13;
         float var16 = Interpolations.lerp(var1.prevRotationPitch, var1.rotationPitch, var9);
         float var17 = this.handleRotationFloat((T)var1, var9);
         this.b((T)var1, var17, var13, var9);
         float var18 = 0.0F;
         float var19 = 0.0F;
         if (var1.isEntityAlive()) {
            var18 = Interpolations.lerp(var1.prevLimbSwingAmount, var1.limbSwingAmount, var9);
            var19 = var1.limbSwing - var1.limbSwingAmount * (1.0F - var9);
            if (var1.isChild()) {
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
            ((IAnimatableModel)var21).setLivingAnimations(var1, var1.getUniqueID().hashCode(), var20);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.01F, 0.0F);
         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture((T)var1));
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
         GlStateManager.disableBlend();
         GlStateManager.disableNormalize();
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
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
         MatrixStack var5 = var1.a(var4, !var1.isLocallyRegistered());
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
      EntityPlayerSP var4 = i.player;
      var3 = new f7(var3.a / 255.0F, var3.c / 255.0F, var3.b / 255.0F);
      Tessellator var5 = Tessellator.getInstance();
      BufferBuilder var6 = var5.getBuffer();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0, 0.01, 0.0);
      Entity var7 = this.c_clash336(var1);
      Vec3d var8 = var1.isAnchored()
         ? var1.getTargetPosition()
         : RotationHelper.a(new Vec3d(var7.lastTickPosX, var7.lastTickPosY, var7.lastTickPosZ), var7.getPositionVector(), var2);
      Vec3d var9 = RotationHelper.a(new Vec3d(var4.lastTickPosX, var4.lastTickPosY, var4.lastTickPosZ), var4.getPositionVector(), var2);
      Vec3d var10 = var8.subtract(var9);
      GlStateManager.translate(var10.x, var10.y, var10.z);
      i.getTextureManager().bindTexture(e);
      float var11 = a(var1, var2, 1.0F, 5.0F);
      this.b(var5, var6, var1, var3, var11);
      GlStateManager.popMatrix();
   }

   protected static float a(BaseGirlEntity var0, float var1, float var2, float var3) {
      EntityPlayerSP var4 = i.player;
      Entity var5 = ((GirlRenderer)i.getRenderManager().getEntityRenderObject(var0)).c_clash336(var0);
      Vec3d var6 = var0.isAnchored()
         ? var0.getTargetPosition()
         : RotationHelper.a(new Vec3d(var5.lastTickPosX, var5.lastTickPosY, var5.lastTickPosZ), var5.getPositionVector(), var1);
      Vec3d var7 = RotationHelper.a(new Vec3d(var4.lastTickPosX, var4.lastTickPosY, var4.lastTickPosZ), var4.getPositionVector(), var1);
      Vec3d var8 = ActiveRenderInfo.getCameraPosition().add(var7);
      float var9 = (float)var8.distanceTo(var6);
      float var10 = Math.abs(var9) / 5.0F;
      return RotationHelper.lerp(var3, var2, ThreadNames.b(var10, 0.0F, 1.0F));
   }

   protected void b(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, f7 var4, float var5) {
   }

   protected static void a(BufferBuilder var0, Tessellator var1, BaseGirlEntity var2, String var3, String var4, float var5, float var6, float var7, float var8) {
      var0.begin(1, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.glLineWidth(var8);
      Vec3d var9 = var2.getCachedBoneOffset(var3);
      Vec3d var10 = var2.getCachedBoneOffset(var4);
      var0.pos(var9.x, var9.y, var9.z)
         .tex(0.0, 0.0)
         .color(var5, var6, var7, 1.0F)
         .endVertex();
      var0.pos(var10.x, var10.y, var10.z)
         .tex(0.0, 0.0)
         .color(var5, var6, var7, 1.0F)
         .endVertex();
      var1.draw();
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
         UUID var5 = ((AbstractPlayerGirlEntity)var1).getOwnerUserUUID();
         if (var5 != null) {
            EntityPlayer var6 = var1.world.getPlayerEntityByUUID(var5);
            if (var6 != null) {
               if (var6.isElytraFlying()) {
                  float var7 = var6.getTicksElytraFlying() + var4;
                  float var8 = MathHelper.clamp(var7 * var7 / 100.0F, 0.0F, 1.0F);
                  GlStateManager.rotate(var8 * (-90.0F - var6.rotationPitch), 1.0F, 0.0F, 0.0F);
                  Vec3d var9 = var6.getLook(var4);
                  double var10 = var6.motionX * var6.motionX + var6.motionZ * var6.motionZ;
                  double var12 = var9.x * var9.x + var9.z * var9.z;
                  if (var10 > 0.0 && var12 > 0.0) {
                     double var14 = (var6.motionX * var9.x + var6.motionZ * var9.z) / (Math.sqrt(var10) * Math.sqrt(var12));
                     double var16 = var6.motionX * var9.z - var6.motionZ * var9.x;
                     GlStateManager.rotate((float)(Math.signum(var16) * Math.acos(var14)) * 180.0F / (float) Math.PI, 0.0F, 1.0F, 0.0F);
                  }
               }
            }
         }
      }
   }

   protected void a(BufferBuilder var1, String var2, GeoBone var3) {
   }

   protected void a(BaseGirlEntity var1, double var2, double var4, double var6, float var8) {
      Entity var9 = var1.getLeashHolder();
      var4 -= (1.6 - var1.height) * 0.5;
      Tessellator var10 = Tessellator.getInstance();
      BufferBuilder var11 = var10.getBuffer();
      double var12 = RotationHelper.lerp(var9.prevRotationYaw, var9.rotationYaw, var8 * 0.5F) * (float) (Math.PI / 180.0);
      double var14 = RotationHelper.lerp(var9.prevRotationPitch, var9.rotationPitch, var8 * 0.5F) * (float) (Math.PI / 180.0);
      double var16 = Math.cos(var12);
      double var18 = Math.sin(var12);
      double var20 = Math.sin(var14);
      if (var9 instanceof EntityHanging) {
         var16 = 0.0;
         var18 = 0.0;
         var20 = -1.0;
      }

      double var22 = Math.cos(var14);
      double var24 = RotationHelper.b(var9.prevPosX, var9.posX, var8) - var16 * 0.7 - var18 * 0.5 * var22;
      double var26 = RotationHelper.b(var9.prevPosY + var9.getEyeHeight() * 0.7, var9.posY + var9.getEyeHeight() * 0.7, var8)
         - var20 * 0.5
         - 0.25;
      double var28 = RotationHelper.b(var9.prevPosZ, var9.posZ, var8) - var18 * 0.7 + var16 * 0.5 * var22;
      double var30 = RotationHelper.lerp(var1.prevRenderYawOffset, var1.renderYawOffset, var8) * (float) (Math.PI / 180.0) + (Math.PI / 2);
      var16 = Math.cos(var30) * var1.width * 0.4;
      var18 = Math.sin(var30) * var1.width * 0.4;
      double var32 = RotationHelper.b(var1.prevPosX, var1.posX, var8) + var16;
      double var34 = RotationHelper.b(var1.prevPosY, var1.posY, var8);
      double var36 = RotationHelper.b(var1.prevPosZ, var1.posZ, var8) + var18;
      var2 += var16;
      var6 += var18;
      double var38 = (float)(var24 - var32);
      double var40 = (float)(var26 - var34);
      double var42 = (float)(var28 - var36);
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      var11.begin(5, DefaultVertexFormats.POSITION_COLOR);

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
         var11.pos(
               var2 + var38 * var48 + 0.0, var4 + var40 * (var48 * var48 + var48) * 0.5 + ((24.0F - var44) / 18.0F + 0.125F), var6 + var42 * var48
            )
            .color(var45, var46, var47, 1.0F)
            .endVertex();
         var11.pos(
               var2 + var38 * var48 + 0.025, var4 + var40 * (var48 * var48 + var48) * 0.5 + ((24.0F - var44) / 18.0F + 0.125F) + 0.025, var6 + var42 * var48
            )
            .color(var45, var46, var47, 1.0F)
            .endVertex();
      }

      var10.draw();
      var11.begin(5, DefaultVertexFormats.POSITION_COLOR);

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
         var11.pos(
               var2 + var38 * var58 + 0.0, var4 + var40 * (var58 * var58 + var58) * 0.5 + ((24.0F - var54) / 18.0F + 0.125F) + 0.025, var6 + var42 * var58
            )
            .color(var55, var56, var57, 1.0F)
            .endVertex();
         var11.pos(
               var2 + var38 * var58 + 0.025, var4 + var40 * (var58 * var58 + var58) * 0.5 + ((24.0F - var54) / 18.0F + 0.125F), var6 + var42 * var58 + 0.025
            )
            .color(var55, var56, var57, 1.0F)
            .endVertex();
      }

      var10.draw();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.enableCull();
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      if (!(this.j.world instanceof SexWorldClient)) {
         String var7 = var2.getName();
         if (var7.equals("weapon") && this.j instanceof AbstractGirlNpcEntity) {
            this.a(var1, var2);
         }

         if (var7.equals("itemRenderer") && this.j.getCurrentAction() == fp.PAYMENT) {
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
         if ("Head2".equals(var7) && !this.shouldRenderHead2()) {
            MATRIX_STACK.pop();
         } else if (!this.isBoneAllowedForRender(var7)) {
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

   boolean isBoneAllowedForRender(String var1) {
      return !var1.startsWith("armor") ? true : this.j instanceof AbstractGirlNpcEntity;
   }

   protected Vector4f a(String var1, float var2, float var3, float var4) {
      if (!var1.startsWith("armor")) {
         return this.a_clash337(var2, var3, var4);
      }

      if (!(this.j instanceof AbstractGirlNpcEntity)) {
         return this.a_clash337(var2, var3, var4);
      }

      if ((Integer)this.j.m.get(BaseGirlEntity.D) == 0) {
         return this.a_clash337(var2, var3, var4);
      }

      GeoModelProvider var5 = this.getGeoModelProvider();
      if (!(var5 instanceof GirlModel)) {
         return this.a_clash337(var2, var3, var4);
      }

      GirlModel var6 = (GirlModel)var5;
      ItemStack var7 = var6.a_clash348(this.j, var1);
      if (!(var7.getItem() instanceof ItemArmor)) {
         return this.a_clash337(var2, var3, var4);
      }

      ItemArmor var8 = (ItemArmor)var7.getItem();
      ArmorMaterial var9 = var8.getArmorMaterial();
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
            int var11 = var8.getColor(var7);
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
      if (!(this.j.world instanceof SexWorldClient)) {
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
                  GlStateManager.pushMatrix();
                  this.q = var2;
                  this.a(var1, var11, var3, var4, var5, var6, var7);
                  GlStateManager.popMatrix();
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

   protected boolean shouldRenderHead2() {
      return !this.j.isControlledByLocalPlayer() ? true : i.gameSettings.thirdPersonView != 0;
   }

   public void a(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6, double var7) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);

      for (GeoQuad var12 : var2.quads) {
         if (var12 != null) {
            Vector3f var13 = new Vector3f(var12.normal.getX(), var12.normal.getY(), var12.normal.getZ());
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
               var1.pos(var19.getX(), var19.getY(), var19.getZ())
                  .tex(var18.textureU + var7, var18.textureV)
                  .color((float)var14.x, (float)var14.y, (float)var14.z, var6)
                  .normal(var13.getX(), var13.getY(), var13.getZ())
                  .endVertex();
            }
         }
      }
   }

   protected ItemStack a_clash340() {
      switch ((String)this.j.m.get(BaseGirlEntity.h)) {
         case "doggy":
            return new ItemStack(Items.DIAMOND, 2);
         case "blowjob":
            return new ItemStack(Items.EMERALD, 3);
         case "strip":
            return new ItemStack(Items.GOLD_INGOT, 1);
         case "boobjob":
            return new ItemStack(Items.ENDER_PEARL, 2);
         case "touch_boobs":
            return new ItemStack(Items.FISH, 2, 1);
         case "sex":
            return new ItemStack(Items.FISH, 3, 0);
         default:
            return null;
      }
   }

   protected void b(BufferBuilder var1, GeoBone var2) {
      ItemStack var3 = this.a_clash340();
      if (var3 != null) {
         ItemRenderer var4 = Minecraft.getMinecraft().getItemRenderer();

         for (int var5 = 0; var5 < var3.getCount(); var5++) {
            GlStateManager.pushMatrix();
            Tessellator.getInstance().draw();
            com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
            GL11.glEnable(2896);
            GL11.glRotated(var2.getRotationX() + 2.5, 0.0, 0.0, 1.0);
            GL11.glRotated(var2.getRotationY(), 0.0, 1.0, 0.0);
            GL11.glRotated(var2.getRotationZ(), 1.0, 0.0, 0.0);
            switch (var5) {
               case 1:
                  GL11.glRotated(-15.0, 0.0, 0.0, 1.0);
                  GlStateManager.translate(0.0, 0.0, -0.025);
                  break;
               case 2:
                  GL11.glRotated(15.0, 0.0, 0.0, 1.0);
                  GlStateManager.translate(0.0, 0.0, 0.025);
            }

            GlStateManager.scale(this.j.n, this.j.n, this.j.n);
            var4.renderItem(this.j, new ItemStack(var3.getItem(), 1), TransformType.THIRD_PERSON_RIGHT_HAND);
            this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
            var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            GL11.glDisable(2896);
            GlStateManager.popMatrix();
         }
      }
   }

   protected ItemStack a_clash341(@Nullable ItemStack var1) {
      return var1;
   }

   protected void a(BufferBuilder var1, GeoBone var2) {
      if (this.j != null) {
         if (this.j instanceof AbstractGirlNpcEntity) {
            EntityDataManager var3 = this.j.getDataManager();
            AbstractGirlNpcEntity var4 = (AbstractGirlNpcEntity)this.j;
            int var5 = (Integer)var3.get(AbstractGirlNpcEntity.M);
            if (var4.getCurrentAction() != fp.BOW) {
               this.a = 0.0F;
            }

            ItemStack var6 = null;
            if (var5 == 1) {
               var6 = (ItemStack)var3.get(AbstractGirlNpcEntity.L);
            } else if (var5 == 2) {
               var6 = (ItemStack)var3.get(AbstractGirlNpcEntity.R);
            }

            var6 = this.a_clash341(var6);
            if (var6 != null) {
               if (var6.getItem().equals(Items.BOW) && var4.getCurrentAction() == fp.BOW) {
                  this.a += 0.015F;
                  var4.d(Math.round(-this.a * 20.0F + var6.getMaxItemUseDuration()));
                  var4.a_clash517(var6);
               }

               GlStateManager.pushMatrix();
               Tessellator.getInstance().draw();
               com.trolmastercard.sexmod.MatrixHelper.a(MATRIX_STACK, var2);
               GL11.glEnable(2896);
               if (var6.getItem() instanceof ItemBow) {
                  GL11.glRotatef(var4.K, 1.0F, 0.0F, 0.0F);
               } else if (var4.getCurrentAction() == fp.ATTACK && var4.S == 0) {
                  GlStateManager.translate(var4.V.x, var4.V.y, var4.V.z);
                  GL11.glRotatef(var4.O, 1.0F, 0.0F, 0.0F);
               } else {
                  GL11.glRotatef(var4.P, 1.0F, 0.0F, 0.0F);
               }

               Minecraft.getMinecraft().getItemRenderer().renderItem(this.j, var6, TransformType.THIRD_PERSON_RIGHT_HAND);
               this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
               var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               GL11.glDisable(2896);
               GlStateManager.popMatrix();
            }
         }
      }
   }

   RayTraceResult a(Vec3d var1, Vec3d var2, World var3) {
      if (Double.isNaN(var1.x) || Double.isNaN(var1.y) || Double.isNaN(var1.z)) {
         return null;
      }

      if (!Double.isNaN(var2.x) && !Double.isNaN(var2.y) && !Double.isNaN(var2.z)) {
         int var4 = MathHelper.floor(var2.x);
         int var5 = MathHelper.floor(var2.y);
         int var6 = MathHelper.floor(var2.z);
         int var7 = MathHelper.floor(var1.x);
         int var8 = MathHelper.floor(var1.y);
         int var9 = MathHelper.floor(var1.z);
         BlockPos var10 = new BlockPos(var7, var8, var9);
         IBlockState var11 = var3.getBlockState(var10);
         if (var11.getCollisionBoundingBox(var3, var10) != Block.NULL_AABB && var11.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
            return var11.collisionRayTrace(var3, var10, var1, var2);
         }

         int var12 = 200;

         while (var12-- >= 0) {
            if (Double.isNaN(var1.x) || Double.isNaN(var1.y) || Double.isNaN(var1.z)) {
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
            double var28 = var2.x - var1.x;
            double var30 = var2.y - var1.y;
            double var32 = var2.z - var1.z;
            if (var13) {
               var22 = (var16 - var1.x) / var28;
            }

            if (var14) {
               var24 = (var18 - var1.y) / var30;
            }

            if (var15) {
               var26 = (var20 - var1.z) / var32;
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
               var1 = new Vec3d(var16, var1.y + var30 * var22, var1.z + var32 * var22);
            } else if (var24 < var26) {
               var34 = var5 > var8 ? EnumFacing.DOWN : EnumFacing.UP;
               var1 = new Vec3d(var1.x + var28 * var24, var18, var1.z + var32 * var24);
            } else {
               var34 = var6 > var9 ? EnumFacing.NORTH : EnumFacing.SOUTH;
               var1 = new Vec3d(var1.x + var28 * var26, var1.y + var30 * var26, var20);
            }

            var7 = MathHelper.floor(var1.x) - (var34 == EnumFacing.EAST ? 1 : 0);
            var8 = MathHelper.floor(var1.y) - (var34 == EnumFacing.UP ? 1 : 0);
            var9 = MathHelper.floor(var1.z) - (var34 == EnumFacing.SOUTH ? 1 : 0);
            var10 = new BlockPos(var7, var8, var9);
            IBlockState var35 = var3.getBlockState(var10);
            if ((var35.getMaterial() == Material.PORTAL || var35.getCollisionBoundingBox(var3, var10) != Block.NULL_AABB)
               && var35.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
               return var35.collisionRayTrace(var3, var10, var1, var2);
            }
         }

         return null;
      } else {
         return null;
      }
   }

}
