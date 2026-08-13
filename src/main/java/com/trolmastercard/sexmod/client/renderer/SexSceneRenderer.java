package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.api.LightingType;
import com.trolmastercard.sexmod.client.gui.ClothingScreen;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadModelStringPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.GirlRegistry;
import com.trolmastercard.sexmod.util.TrigMath;
import com.trolmastercard.sexmod.util.IBoneRotationSupplier;







import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.MatrixStack;

public class SexSceneRenderer extends GeoEntityRenderer<SexSceneEntity> {
   public static final float e = 1.876945F;
   public static final float i = 2.876945F;
   Minecraft a;
   SexSceneEntity c = null;
   ServerWhitelistManager.b b = null;
   HashMap<String, String> h = new HashMap<>();
   HashMap<String, String> f = new HashMap<>();
   HashMap<String, IBoneRotationSupplier> g = new HashMap<>();
   public static boolean k = false;
   Vec3d d = new Vec3d(1.0, 1.0, 1.0);
   Vec3d j;



   @Override
   public ResourceLocation getEntityTexture(SexSceneEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/egg.png");
   }
   public SexSceneRenderer(RenderManager var1, AnimatedGeoModel<?> var2) {
      super(var1, (AnimatedGeoModel<SexSceneEntity>) (AnimatedGeoModel) var2);
      this.a = Minecraft.getMinecraft();
      this.a_clash809();
   }

   void a_clash809() {
      this.h.put("customLegL", "legL");
      this.h.put("customShinL", "shinL");
      this.h.put("customLegR", "legR");
      this.h.put("customShinR", "shinR");
      this.f.put("top", "upperBody");
      this.f.put("customArmL", "armL");
      this.f.put("customLowerArmL", "lowerArmL");
      this.f.put("customArmR", "armR");
      this.f.put("customLowerArmR", "lowerArmR");
      this.g.put("lowerArmR", var0 -> TrigMath.wrapDegrees(var0.getRightArmAngle()));
      this.g.put("lowerArmL", var0 -> TrigMath.wrapDegrees(var0.getLeftArmAngle()));
   }

   boolean d(SexSceneEntity var1) {
      String var2 = var1.a_clash343();
      if (var1.f) {
         return false;
      }

      if (ServerWhitelistManager.f_clash125(var2)) {
         return false;
      }

      if (ServerWhitelistManager.g_clash134() != null) {
         return true;
      }

      UUID var3 = var1.b_clash342();
      BaseGirlEntity var4 = BaseGirlEntity.getClientGirlEntity(var3);
      if (var4 == null) {
         return true;
      }

      HashSet var5 = var4.getCustomPartsSet();
      var5.remove(var2);
      String var6 = BaseGirlEntity.encodeCustomParts(var5);
      PacketHandler.b.sendToServer(new UploadModelStringPacket(var6, var1.b_clash342()));
      return true;
   }

   @SideOnly(Side.CLIENT)
   public static void a_clash810(BaseGirlEntity var0, float var1) {
      if (!var0.isDead) {
         if (var0.world.isRemote) {
            if (var0.hasCustomParts()) {
               RenderManager var2 = Minecraft.getMinecraft().getRenderManager();

               for (String var4 : var0.getCustomPartsSet()) {
                  SexSceneEntity var5 = new SexSceneEntity(var0.world, var0.getGirlId(), var4);
                  k = true;
                  var2.renderEntity(var5, 0.0, 0.0, 0.0, 0.0F, var1, false);
               }
            }
         }
      }
   }

   public boolean a(SexSceneEntity var1, ICamera var2, double var3, double var5, double var7) {
      return super.shouldRender(var1, var2, var3, var5, var7);
   }

   boolean a_clash811(float var1) {
      if (var1 == 2.876945F) {
         return true;
      } else if (var1 == 1.876945F) {
         return true;
      } else if (k) {
         k = false;
         return true;
      } else {
         return false;
      }
   }

   void a(ServerWhitelistManager.b var1, SexSceneEntity var2, float var3) {
      if (var1 != null && var1.i_clash894() != LightingType.DEFAULT) {
         GL11.glDisable(2896);
         this.j = var1.i_clash894() == LightingType.SEXMOD ? WorldUtils.a_clash301(var2, var3) : null;
      } else {
         this.j = null;
      }
   }

   @Override
   public void doRender(SexSceneEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.a(var1, var2, var4, var6, var8, var9);
   }

   public void a(SexSceneEntity var1, double var2, double var4, double var6, float var8, float var9) {
      if (this.a_clash811(var9)) {
         if (!ServerWhitelistManager.d) {
            if (!this.d(var1)) {
               var1.c = new MatrixStack();
               ServerWhitelistManager.b var10 = ServerWhitelistManager.b_clash142(var1.a_clash343());
               this.c = var1;
               this.b = var10;
               this.a(var10, var1, var9);
               if (var9 != 1.876945F && var9 != 2.876945F) {
                  UUID var11 = var1.b_clash342();
                  if (var11 != null) {
                     BaseGirlEntity var13 = BaseGirlEntity.getClientGirlEntity(var11);
                     if (var13 != null) {
                        if (var10 == null || var10.a_clash900() || var13.getOutfitIndex() != 0) {
                           Object var12;
                           if (!(var13 instanceof AbstractPlayerGirlEntity)) {
                              var12 = var13;
                           } else {
                              UUID var14 = ((AbstractPlayerGirlEntity)var13).getOwnerUserUUID();
                              if (var14 == null) {
                                 return;
                              }

                              EntityPlayer var15 = var1.world.getPlayerEntityByUUID(var14);
                              var12 = var15 == null ? var13 : var15;
                           }

                           Vec3d var19 = var13.renderCustomModelTransform(this.a, var1, (EntityLivingBase)var12, var9);
                           BlockPos var20 = new BlockPos(
                              Math.floor(((EntityLivingBase)var12).posX),
                              Math.floor(((EntityLivingBase)var12).posY),
                              Math.floor(((EntityLivingBase)var12).posZ)
                           );
                           int var16 = ((EntityLivingBase)var12).world.getLight(var20, true);
                           Vec3d var17 = new Vec3d(1.0, 1.0, 1.0);
                           float var18 = ThreadNames.b(var16, 10.0F, 15.0F) / 15.0F;
                           this.d = new Vec3d(var17.x * var18, var17.y * var18, var17.z * var18);
                           GlStateManager.pushMatrix();
                           GlStateManager.translate(var19.x, var19.y, var19.z);
                           if (var13.isAnchored()) {
                              GlStateManager.rotate(var13.getYawRotation(), 0.0F, 1.0F, 0.0F);
                           }

                           super.doRender(var1, 0.0, 0.0, 0.0, var8, var9);
                           GlStateManager.popMatrix();
                           GL11.glEnable(2896);
                        }
                     }
                  }
               } else {
                  this.d = new Vec3d(1.0, 1.0, 1.0);
                  super.doRender(var1, var2, var4, var6, var8, var9);
                  GL11.glEnable(2896);
               }
            }
         }
      }
   }

   public static Vec3d a(Minecraft var0, SexSceneEntity var1, EntityLivingBase var2, BaseGirlEntity var3, float var4) {
      Vec3d var5;
      if (var3.isAnchored()) {
         Vec3d var6 = var3.getTargetPosition();
         float var7 = var3.getYawRotation();
         var1.prevPosX = var6.x;
         var1.prevPosY = var6.y;
         var1.prevPosZ = var6.z;
         var1.lastTickPosX = var6.x;
         var1.lastTickPosY = var6.y;
         var1.lastTickPosZ = var6.z;
         var1.posX = var6.x;
         var1.posY = var6.y;
         var1.posZ = var6.z;
         var1.rotationYaw = var7;
         var1.prevRotationYaw = var7;
         var1.rotationYawHead = var7;
         var1.prevRotationYawHead = var7;
         var1.renderYawOffset = var7;
         var1.prevRenderYawOffset = var7;
         var1.rotationPitch = var7;
         var1.prevRotationPitch = var7;
         var5 = var6;
      } else {
         var1.rotationYaw = var2.rotationYaw;
         var1.prevRotationYaw = var2.prevRotationYaw;
         var1.rotationYawHead = var2.rotationYawHead;
         var1.prevRotationYawHead = var2.prevRotationYawHead;
         var1.renderYawOffset = var2.renderYawOffset;
         var1.prevRenderYawOffset = var2.prevRenderYawOffset;
         var1.rotationPitch = var2.rotationPitch;
         var1.prevRotationPitch = var2.prevRotationPitch;
         var1.prevPosX = var2.prevPosX;
         var1.prevPosY = var2.prevPosY;
         var1.prevPosZ = var2.prevPosZ;
         var1.lastTickPosX = var2.lastTickPosX;
         var1.lastTickPosY = var2.lastTickPosY;
         var1.lastTickPosZ = var2.lastTickPosZ;
         var1.posX = var2.posX;
         var1.posY = var2.posY;
         var1.posZ = var2.posZ;
         var5 = RotationHelper.a(new Vec3d(var2.lastTickPosX, var2.lastTickPosY, var2.lastTickPosZ), var2.getPositionVector(), var4);
      }

      EntityPlayerSP var8 = var0.player;
      Vec3d var9 = RotationHelper.a(new Vec3d(var8.lastTickPosX, var8.lastTickPosY, var8.lastTickPosZ), var8.getPositionVector(), var4);
      return var5.subtract(var9);
   }

   @Override
   public void render(GeoModel var1, SexSceneEntity var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      BufferBuilder var8 = Tessellator.getInstance().getBuffer();
      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      for (GeoBone var10 : var1.topLevelBones) {
         if (var3 != 1.876945F) {
            this.a(var2, var10, var3);
         }

         var2.c.translate(-var10.getPivotX() / 16.0F, -var10.getPivotY() / 16.0F, -var10.getPivotZ() / 16.0F);
         this.renderRecursively(var8, var10, var4, var5, var6, var7);
      }

      Tessellator.getInstance().draw();
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
   }

   EntityLivingBase c_clash812(SexSceneEntity var1) {
      BaseGirlEntity var3 = this.b_clash813(var1);
      if (var3 == null) {
         return null;
      }

      Object var2;
      if (!(var3 instanceof AbstractPlayerGirlEntity)) {
         var2 = var3;
      } else {
         EntityPlayer var4 = var1.world.getPlayerEntityByUUID(((AbstractPlayerGirlEntity)var3).getOwnerUserUUID());
         var2 = var4 == null ? var3 : var4;
      }

      return (EntityLivingBase)var2;
   }

   BaseGirlEntity b_clash813(SexSceneEntity var1) {
      UUID var2 = var1.b_clash342();
      BaseGirlEntity var3 = GirlRegistry.getGirl(var2);
      return var3 != null ? var3 : BaseGirlEntity.getClientGirlEntity(var2);
   }

   void a(SexSceneEntity var1, GeoBone var2, float var3) {
      String var4 = this.a_clash814(var1);
      if (var4 != null) {
         this.a(var1, var2, var3, var4);
      }
   }

   void a(SexSceneEntity var1, GeoBone var2, float var3, String var4) {
      BaseGirlEntity var5 = this.b_clash813(var1);
      this.c_clash812(var1);
      var1.c = var5.getBoneMatrixStack(var4, false);
      if (var1.f && var3 == 2.876945F) {
         var1.c.scale(0.5F, 0.5F, 0.5F);
         var1.c.rotateY((float)Math.toRadians(-ClothingScreen.b));
      }
   }

   String a_clash814(SexSceneEntity var1) {
      if (var1.f) {
         return var1.d.boneName;
      } else {
         ServerWhitelistManager.b var2 = ServerWhitelistManager.b_clash142(var1.a_clash343());
         if (var2 == null) {
            return null;
         } else {
            return BoneType.CUSTOM_BONE.equals(var2.j_clash897()) ? var2.b_clash893() : var2.j_clash897().boneName;
         }
      }
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      this.c.c.push();
      this.c.c.translate(var2);
      this.c.c.moveToPivot(var2);
      this.c.c.rotate(var2);
      this.c.c.scale(var2);
      this.c.c.moveBackFromPivot(var2);
      if (!var2.isHidden()) {
         for (GeoCube var8 : var2.childCubes) {
            this.c.c.push();
            GlStateManager.pushMatrix();
            this.renderCube(var1, var8, var3, var4, var5, var6);
            GlStateManager.popMatrix();
            this.c.c.pop();
         }
      }

      if (!var2.childBonesAreHiddenToo()) {
         for (GeoBone var11 : var2.childBones) {
            this.renderRecursively(var1, var11, var3, var4, var5, var6);
         }
      }

      try {
         this.c.c.pop();
      } catch (IllegalStateException var9) {
      }
   }

   @Override
   public void renderCube(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6) {
      this.c.c.moveToPivot(var2);
      this.c.c.rotate(var2);
      this.c.c.moveBackFromPivot(var2);

      for (GeoQuad var10 : var2.quads) {
         if (var10 != null) {
            Vector3f var11 = new Vector3f(var10.normal.getX(), var10.normal.getY(), var10.normal.getZ());
            this.c.c.getNormalMatrix().transform(var11);
            if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var11.getX() < 0.0F) {
               var11.x *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var11.getY() < 0.0F) {
               var11.y *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var11.getZ() < 0.0F) {
               var11.z *= -1.0F;
            }

            if (this.j != null) {
               this.d = BodyParts.a(this.d, var11, this.j);
            }

            for (GeoVertex var15 : var10.vertices) {
               Vector4f var16 = new Vector4f(var15.position.getX(), var15.position.getY(), var15.position.getZ(), 1.0F);
               this.c.c.getModelMatrix().transform(var16);
               var1.pos(var16.getX(), var16.getY(), var16.getZ())
                  .tex(var15.textureU, var15.textureV)
                  .color((float)this.d.x, (float)this.d.y, (float)this.d.z, var6)
                  .normal(var11.getX(), var11.getY(), var11.getZ())
                  .endVertex();
            }
         }
      }
   }

}
