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

/**
 * Renders {@link SexSceneEntity} — the custom model parts (dildos, outfits,
 * scene props) attached to a girl's bones. Used in three places: the
 * {@link ClothingScreen} preview, the {@code ClothingScreen} customization
 * list previews, and — critically — {@link #renderSexSceneEffects}, which
 * renders the girl's active custom parts at scene render time.
 * <p>
 * <b>Attachment.</b> A part model's root bone is replaced by the girl's bone
 * matrix stack for the mapped bone ({@link #renderBoneEffect}: custom leg/arm
 * names map to the girl's vanilla bone names via {@link #initBoneMaps}), so
 * the part follows the girl's animation. Item models additionally rotate with
 * {@code ClothingScreen.currentModelYaw}.
 * <p>
 * <b>Sentinel angles.</b> The render yaw sentinels {@value #ANGLE_1_87} and
 * {@value #ANGLE_2_87} (1.876945 / 2.876945) select "render at world
 * position" (free-standing preview) instead of bone attachment; {@code isCustom}
 * flags the per-part scene-effect renders. Anchored girls render the part at
 * their target position with their yaw. Do not change these sentinels.
 * <p>
 * <b>Lighting.</b> {@link ServerWhitelistManager.ModelData} may declare
 * SEXMOD lighting (fake shading from the entity look vector) or FULLBRIGHT
 * (GL lighting off); otherwise the part is tinted by the host girl's block
 * light level.
 * <p>
 * CLIENT-side render thread only.
 */
public class SexSceneRenderer extends GeoEntityRenderer<SexSceneEntity> {
   public static final float ANGLE_1_87 = 1.876945F;
   public static final float ANGLE_2_87 = 2.876945F;
   Minecraft mc;
   SexSceneEntity sceneEntity = null;
   ServerWhitelistManager.ModelData modelData = null;
   HashMap<String, String> legBoneMap = new HashMap<>();
   HashMap<String, String> bodyBoneMap = new HashMap<>();
   HashMap<String, IBoneRotationSupplier> boneRotations = new HashMap<>();
   public static boolean isCustom = false;
   Vec3d colorScale = new Vec3d(1.0, 1.0, 1.0);
   Vec3d lightingPos;

   @Override
   public ResourceLocation getEntityTexture(SexSceneEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/egg.png");
   }
   public SexSceneRenderer(RenderManager var1, AnimatedGeoModel<?> var2) {
      super(var1, (AnimatedGeoModel<SexSceneEntity>) (AnimatedGeoModel) var2);
      this.mc = Minecraft.getMinecraft();
      this.initBoneMaps();
   }

   /**
    * Maps custom part bone names onto the girl's vanilla bone names
    * (customLegL->legL, customArmL->armL, top->upperBody, ...) and registers
    * the lower-arm rotation suppliers (the parts' forearm bends follow the
    * girl's arm angles).
    */
   void initBoneMaps() {
      this.legBoneMap.put("customLegL", "legL");
      this.legBoneMap.put("customShinL", "shinL");
      this.legBoneMap.put("customLegR", "legR");
      this.legBoneMap.put("customShinR", "shinR");
      this.bodyBoneMap.put("top", "upperBody");
      this.bodyBoneMap.put("customArmL", "armL");
      this.bodyBoneMap.put("customLowerArmL", "lowerArmL");
      this.bodyBoneMap.put("customArmR", "armR");
      this.bodyBoneMap.put("customLowerArmR", "lowerArmR");
      this.boneRotations.put("lowerArmR", var0 -> TrigMath.wrapDegrees(var0.getRightArmAngle()));
      this.boneRotations.put("lowerArmL", var0 -> TrigMath.wrapDegrees(var0.getLeftArmAngle()));
   }

   /**
    * Whether the part model should be drawn at all: item models and disabled
    * models are skipped; when no server whitelist exists yet, the part is
    * removed from the girl's custom-part set (and the new set uploaded to the
    * server) so stale parts don't linger client-side.
    */
   boolean shouldRenderItemModel(SexSceneEntity var1) {
      String var2 = var1.getModelCode();
      if (var1.isItemModel) {
         return false;
      }

      if (ServerWhitelistManager.isModelDisabled(var2)) {
         return false;
      }

      if (ServerWhitelistManager.getCustomModelsKey() != null) {
         return true;
      }

      UUID var3 = var1.getGirlIdFromCode();
      BaseGirlEntity var4 = BaseGirlEntity.getClientGirlEntity(var3);
      if (var4 == null) {
         return true;
      }

      HashSet var5 = var4.getCustomPartsSet();
      var5.remove(var2);
      String var6 = BaseGirlEntity.encodeCustomParts(var5);
      PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(var6, var1.getGirlIdFromCode()));
      return true;
   }

   /**
    * Static hook called from {@link GirlRenderer#doRenderEntity} after each
    * girl render: spawns a temporary {@link SexSceneEntity} per active custom
    * part and renders it at the origin (bone attachment happens via the
    * sentinel-angle path). The temporary entities must never be added to the
    * world — they exist only for the render call. CLIENT-side.
    */
   @SideOnly(Side.CLIENT)
   public static void renderSexSceneEffects(BaseGirlEntity var0, float var1) {
      if (!var0.isDead) {
         if (var0.world.isRemote) {
            if (var0.hasCustomParts()) {
               RenderManager var2 = Minecraft.getMinecraft().getRenderManager();

               for (String var4 : var0.getCustomPartsSet()) {
                  SexSceneEntity var5 = new SexSceneEntity(var0.world, var0.getGirlId(), var4);
                  isCustom = true;
                  var2.renderEntity(var5, 0.0, 0.0, 0.0, 0.0F, var1, false);
               }
            }
         }
      }
   }

   public boolean shouldRender(SexSceneEntity var1, ICamera var2, double var3, double var5, double var7) {
      return super.shouldRender(var1, var2, var3, var5, var7);
   }

   /**
    * The sentinel-angle gate: yaws 2.876945/1.876945 and the one-shot
    * {@code isCustom} flag (consumed on read) admit the render — all other
    * angles are ignored. See class javadoc for the sentinel semantics.
    */
   boolean isRenderAngle(float var1) {
      if (var1 == 2.876945F) {
         return true;
      } else if (var1 == 1.876945F) {
         return true;
      } else if (isCustom) {
         isCustom = false;
         return true;
      } else {
         return false;
      }
   }

   /**
    * Resolves the part's lighting mode from its {@link LightingType}:
    * SEXMOD lights the part from the entity's look vector (fake shading),
    * FULLBRIGHT disables GL lighting, DEFAULT keeps world lighting.
    */
   void renderModelData(ServerWhitelistManager.ModelData var1, SexSceneEntity var2, float var3) {
      if (var1 != null && var1.getLightingType() != LightingType.DEFAULT) {
         GL11.glDisable(2896);
         this.lightingPos = var1.getLightingType() == LightingType.SEXMOD ? WorldUtils.getEntityLookVector(var2, var3) : null;
      } else {
         this.lightingPos = null;
      }
   }

   @Override
   public void doRender(SexSceneEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.doRenderScene(var1, var2, var4, var6, var8, var9);
   }

   /**
    * Main dispatch: gated by {@link #isRenderAngle} and the global rendering
    * switch. Bone-attached parts are rendered relative to the host girl (or
    * her owner player) with the anchored yaw rotation; free-standing parts
    * (sentinel angles) render at their world position. Tint = the host's
    * block light level (clamped 10..15).
    */
   public void doRenderScene(SexSceneEntity var1, double var2, double var4, double var6, float var8, float var9) {
      if (this.isRenderAngle(var9)) {
         if (!ServerWhitelistManager.isGlobalRenderingDisabled) {
            if (!this.shouldRenderItemModel(var1)) {
               var1.matrixStack = new MatrixStack();
               ServerWhitelistManager.ModelData var10 = ServerWhitelistManager.getModelDataForGirl(var1.getModelCode());
               this.sceneEntity = var1;
               this.modelData = var10;
               this.renderModelData(var10, var1, var9);
               if (var9 != 1.876945F && var9 != 2.876945F) {
                  UUID var11 = var1.getGirlIdFromCode();
                  if (var11 != null) {
                     BaseGirlEntity var13 = BaseGirlEntity.getClientGirlEntity(var11);
                     if (var13 != null) {
                        if (var10 == null || var10.isDisabled() || var13.getOutfitIndex() != 0) {
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

                           Vec3d var19 = var13.renderCustomModelTransform(this.mc, var1, (EntityLivingBase)var12, var9);
                           BlockPos var20 = new BlockPos(
                              Math.floor(((EntityLivingBase)var12).posX),
                              Math.floor(((EntityLivingBase)var12).posY),
                              Math.floor(((EntityLivingBase)var12).posZ)
                           );
                           int var16 = ((EntityLivingBase)var12).world.getLight(var20, true);
                           Vec3d var17 = new Vec3d(1.0, 1.0, 1.0);
                           float var18 = ThreadNames.clampFloat(var16, 10.0F, 15.0F) / 15.0F;
                           this.colorScale = new Vec3d(var17.x * var18, var17.y * var18, var17.z * var18);
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
                  this.colorScale = new Vec3d(1.0, 1.0, 1.0);
                  super.doRender(var1, var2, var4, var6, var8, var9);
                  GL11.glEnable(2896);
               }
            }
         }
      }
   }

   /**
    * Computes the part's render position relative to the local player:
    * anchored girls pin the part to their target position + yaw (all pos/yaw
    * fields of the temp entity are overwritten — the entity code tolerates
    * this); otherwise the host entity's lerped position is used.
    */
   public static Vec3d getSceneEntityPosition(Minecraft var0, SexSceneEntity var1, EntityLivingBase var2, BaseGirlEntity var3, float var4) {
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
         var5 = RotationHelper.lerpVec3dDouble(new Vec3d(var2.lastTickPosX, var2.lastTickPosY, var2.lastTickPosZ), var2.getPositionVector(), var4);
      }

      EntityPlayerSP var8 = var0.player;
      Vec3d var9 = RotationHelper.lerpVec3dDouble(new Vec3d(var8.lastTickPosX, var8.lastTickPosY, var8.lastTickPosZ), var8.getPositionVector(), var4);
      return var5.subtract(var9);
   }

   /**
    * Renders the part model: bone-attach pass (unless the yaw sentinel
    * 1.876945, which is world-space), then the normal geckolib recursion with
    * the entity's own matrix stack.
    */
   @Override
   public void render(GeoModel var1, SexSceneEntity var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      BufferBuilder var8 = Tessellator.getInstance().getBuffer();
      var8.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      for (GeoBone var10 : var1.topLevelBones) {
         if (var3 != 1.876945F) {
            this.renderBone(var2, var10, var3);
         }

         var2.matrixStack.translate(-var10.getPivotX() / 16.0F, -var10.getPivotY() / 16.0F, -var10.getPivotZ() / 16.0F);
         this.renderRecursively(var8, var10, var4, var5, var6, var7);
      }

      Tessellator.getInstance().draw();
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
   }

   /**
    * The host entity whose pose the part follows: the owning player for
    * player-girls, else the girl itself.
    */
   EntityLivingBase getRenderEntityLiving(SexSceneEntity var1) {
      BaseGirlEntity var3 = this.getRenderGirl(var1);
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

   /**
    * The host girl for the part (registry lookup, fallback to the client girl
    * list).
    */
   BaseGirlEntity getRenderGirl(SexSceneEntity var1) {
      UUID var2 = var1.getGirlIdFromCode();
      BaseGirlEntity var3 = GirlRegistry.getGirl(var2);
      return var3 != null ? var3 : BaseGirlEntity.getClientGirlEntity(var2);
   }

   void renderBone(SexSceneEntity var1, GeoBone var2, float var3) {
      String var4 = this.getBoneName(var1);
      if (var4 != null) {
         this.renderBoneEffect(var1, var2, var3, var4);
      }
   }

   /**
    * Attaches the part to the girl's bone: the part's root matrix is replaced
    * by the girl's bone matrix stack for the mapped bone name; item models
    * are scaled 0.5 and rotated by {@code ClothingScreen.currentModelYaw}.
    */
   void renderBoneEffect(SexSceneEntity var1, GeoBone var2, float var3, String var4) {
      BaseGirlEntity var5 = this.getRenderGirl(var1);
      this.getRenderEntityLiving(var1);
      var1.matrixStack = var5.getBoneMatrixStack(var4, false);
      if (var1.isItemModel && var3 == 2.876945F) {
         var1.matrixStack.scale(0.5F, 0.5F, 0.5F);
         var1.matrixStack.rotateY((float)Math.toRadians(-ClothingScreen.currentModelYaw));
      }
   }

   String getBoneName(SexSceneEntity var1) {
      if (var1.isItemModel) {
         return var1.boneType.boneName;
      } else {
         ServerWhitelistManager.ModelData var2 = ServerWhitelistManager.getModelDataForGirl(var1.getModelCode());
         if (var2 == null) {
            return null;
         } else {
            return BoneType.CUSTOM_BONE.equals(var2.getBoneType()) ? var2.getModelName() : var2.getBoneType().boneName;
         }
      }
   }

   /**
    * Bone recursion with the entity's matrix stack: transform push, cube
    * render, children (unless hidden), pop (with IllegalStateException guard).
    */
   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      this.sceneEntity.matrixStack.push();
      this.sceneEntity.matrixStack.translate(var2);
      this.sceneEntity.matrixStack.moveToPivot(var2);
      this.sceneEntity.matrixStack.rotate(var2);
      this.sceneEntity.matrixStack.scale(var2);
      this.sceneEntity.matrixStack.moveBackFromPivot(var2);
      if (!var2.isHidden()) {
         for (GeoCube var8 : var2.childCubes) {
            this.sceneEntity.matrixStack.push();
            GlStateManager.pushMatrix();
            this.renderCube(var1, var8, var3, var4, var5, var6);
            GlStateManager.popMatrix();
            this.sceneEntity.matrixStack.pop();
         }
      }

      if (!var2.childBonesAreHiddenToo()) {
         for (GeoBone var11 : var2.childBones) {
            this.renderRecursively(var1, var11, var3, var4, var5, var6);
         }
      }

      try {
         this.sceneEntity.matrixStack.pop();
      } catch (IllegalStateException var9) {
      }
   }

   /**
    * Cube pass for the part: transformed normals (mirrored on zero-size
    * faces), optional SEXMOD fake lighting applied to the tint, and vertices
    * emitted through the entity matrix stack.
    */
   @Override
   public void renderCube(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6) {
      this.sceneEntity.matrixStack.moveToPivot(var2);
      this.sceneEntity.matrixStack.rotate(var2);
      this.sceneEntity.matrixStack.moveBackFromPivot(var2);

      for (GeoQuad var10 : var2.quads) {
         if (var10 != null) {
            Vector3f var11 = new Vector3f(var10.normal.getX(), var10.normal.getY(), var10.normal.getZ());
            this.sceneEntity.matrixStack.getNormalMatrix().transform(var11);
            if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var11.getX() < 0.0F) {
               var11.x *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var11.getY() < 0.0F) {
               var11.y *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var11.getZ() < 0.0F) {
               var11.z *= -1.0F;
            }

            if (this.lightingPos != null) {
               this.colorScale = BodyParts.offsetBonePosition(this.colorScale, var11, this.lightingPos);
            }

            for (GeoVertex var15 : var10.vertices) {
               Vector4f var16 = new Vector4f(var15.position.getX(), var15.position.getY(), var15.position.getZ(), 1.0F);
               this.sceneEntity.matrixStack.getModelMatrix().transform(var16);
               var1.pos(var16.getX(), var16.getY(), var16.getZ())
                  .tex(var15.textureU, var15.textureV)
                  .color((float)this.colorScale.x, (float)this.colorScale.y, (float)this.colorScale.z, var6)
                  .normal(var11.getX(), var11.getY(), var11.getZ())
                  .endVertex();
            }
         }
      }
   }

}
