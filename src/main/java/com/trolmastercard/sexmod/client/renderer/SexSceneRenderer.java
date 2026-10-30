package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.Main;
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
import software.bernie.geckolib3.resource.GeckoLibCache;
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
   public ResourceLocation getEntityTexture(SexSceneEntity sceneEntity) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/egg.png");
   }
   public SexSceneRenderer(RenderManager renderManager, AnimatedGeoModel<?> model) {
      super(renderManager, (AnimatedGeoModel<SexSceneEntity>) (AnimatedGeoModel) model);
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
      this.boneRotations.put("lowerArmR", girl -> TrigMath.wrapDegrees(girl.getRightArmAngle()));
      this.boneRotations.put("lowerArmL", girl -> TrigMath.wrapDegrees(girl.getLeftArmAngle()));
   }

   /**
    * Whether the part model should be drawn at all: item models and disabled
    * models are skipped; when no server whitelist exists yet, the part is
    * removed from the girl's custom-part set (and the new set uploaded to the
    * server) so stale parts don't linger client-side.
    */
   boolean shouldRenderItemModel(SexSceneEntity sceneEntity) {
      String modelCode = sceneEntity.getModelCode();
      if (sceneEntity.isItemModel) {
         return false;
      }

      if (ServerWhitelistManager.isModelDisabled(modelCode)) {
         return false;
      }

      if (ServerWhitelistManager.getCustomModelsKey() != null) {
         return true;
      }

      UUID girlUuid = sceneEntity.getGirlIdFromCode();
      BaseGirlEntity girl = BaseGirlEntity.getClientGirlEntity(girlUuid);
      if (girl == null) {
         return true;
      }

      HashSet customParts = girl.getCustomPartsSet();
      customParts.remove(modelCode);
      String encodedParts = BaseGirlEntity.encodeCustomParts(customParts);
      PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(encodedParts, sceneEntity.getGirlIdFromCode()));
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
   public static void renderSexSceneEffects(BaseGirlEntity girl, float partialTicks) {
      if (!girl.isDead) {
         if (girl.world.isRemote) {
            if (girl.hasCustomParts()) {
               // A full-girl reskin replaces the base model; rendering legacy
               // bone-attached parts on top would double-layer the textures
               // (the "texture mashup"). Skip legacy parts for reskinned girls.
               if (com.trolmastercard.sexmod.reskin.ReskinManager.INSTANCE.isGirlReskinned(girl)) {
                  return;
               }
               RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

               for (String partCode : girl.getCustomPartsSet()) {
                  // CRASH GUARD: a legacy part whose model isn't registered in
                  // GeckoLibCache (e.g. after a resource reload wiped the cache)
                  // throws GeoModelException and kills the whole client. Skip any
                  // part whose ModelData / geo model is missing, and never let a
                  // single bad part take down the frame.
                  ServerWhitelistManager.ModelData md = ServerWhitelistManager.getModelDataForGirl(partCode);
                  if (md == null) {
                     logMissingLegacy(partCode, girl, "no ModelData");
                     clearStaleLegacy(girl, partCode);
                     continue;
                  }
                  ResourceLocation res = md.getFallbackTexture();
                  if (res == null || !GeckoLibCache.getInstance().getGeoModels().containsKey(res)) {
                     logMissingLegacy(partCode, girl, res == null ? "null geo resource" : "geo not in cache: " + res);
                     clearStaleLegacy(girl, partCode);
                     continue;
                  }
                  try {
                     SexSceneEntity scenePart = new SexSceneEntity(girl.world, girl.getGirlId(), partCode);
                     isCustom = true;
                     renderManager.renderEntity(scenePart, 0.0, 0.0, 0.0, 0.0F, partialTicks, false);
                  } catch (Throwable t) {
                     // never let a bad custom part crash rendering
                     if (!(t instanceof java.util.ConcurrentModificationException)) {
                        Main.LOGGER.warn("[sexmod] skipped malformed custom part '" + partCode + "' on " + girl.getDisplayName()
                           + ": " + t.getClass().getSimpleName() + ": " + t.getMessage());
                     }
                     clearStaleLegacy(girl, partCode);
                  }
               }
            }
         }
      }
   }

   /** Logs a missing legacy part (one line, throttled-ish) so malformed/legacy models are auditable. */
   private static void logMissingLegacy(String partCode, BaseGirlEntity girl, String why) {
      String girlName = girl.getDisplayName() == null ? "?" : girl.getDisplayName().getFormattedText();
      Main.LOGGER.warn("[sexmod] skipping missing legacy custom model '" + partCode + "' on " + girlName + " (" + why + ")");
   }

   /** Removes a stale/broken legacy part code from the girl's custom set (client-side). */
   private static void clearStaleLegacy(BaseGirlEntity girl, String partCode) {
      try {
         java.util.HashSet<String> parts = girl.getCustomPartsSet();
         if (parts == null || parts.isEmpty()) return;
         if (!parts.remove(partCode)) return;
         girl.setCustomModelCode(BaseGirlEntity.encodeCustomParts(parts));
      } catch (Throwable t) {
         // best-effort cleanup
      }
   }

   public boolean shouldRender(SexSceneEntity entity, ICamera camera, double x, double y, double z) {
      return super.shouldRender(entity, camera, x, y, z);
   }

   /**
    * The sentinel-angle gate: yaws 2.876945/1.876945 and the one-shot
    * {@code isCustom} flag (consumed on read) admit the render — all other
    * angles are ignored. See class javadoc for the sentinel semantics.
    */
   boolean isRenderAngle(float yaw) {
      if (yaw == 2.876945F) {
         return true;
      } else if (yaw == 1.876945F) {
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
   void renderModelData(ServerWhitelistManager.ModelData modelData, SexSceneEntity sceneEntity, float partialTicks) {
      if (modelData != null && modelData.getLightingType() != LightingType.DEFAULT) {
         GlStateManager.disableLighting();
         this.lightingPos = modelData.getLightingType() == LightingType.SEXMOD ? WorldUtils.getEntityLookVector(sceneEntity, partialTicks) : null;
      } else {
         this.lightingPos = null;
      }
   }

   @Override
   public void doRender(SexSceneEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      this.doRenderScene(entity, x, y, z, entityYaw, partialTicks);
   }

   /**
    * Main dispatch: gated by {@link #isRenderAngle} and the global rendering
    * switch. Bone-attached parts are rendered relative to the host girl (or
    * her owner player) with the anchored yaw rotation; free-standing parts
    * (sentinel angles) render at their world position. Tint = the host's
    * block light level (clamped 10..15).
    */
   public void doRenderScene(SexSceneEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (this.isRenderAngle(partialTicks)) {
         if (!ServerWhitelistManager.isGlobalRenderingDisabled) {
            if (!this.shouldRenderItemModel(entity)) {
               entity.matrixStack = new MatrixStack();
               ServerWhitelistManager.ModelData modelData = ServerWhitelistManager.getModelDataForGirl(entity.getModelCode());
               this.sceneEntity = entity;
               this.modelData = modelData;
               this.renderModelData(modelData, entity, partialTicks);
               if (partialTicks != 1.876945F && partialTicks != 2.876945F) {
                  UUID girlUuid = entity.getGirlIdFromCode();
                  if (girlUuid != null) {
                     BaseGirlEntity girl = BaseGirlEntity.getClientGirlEntity(girlUuid);
                     if (girl != null) {
                        if (modelData == null || modelData.isDisabled() || girl.getOutfitIndex() != 0) {
                           Object renderEntity;
                           if (!(girl instanceof AbstractPlayerGirlEntity)) {
                              renderEntity = girl;
                           } else {
                              UUID ownerUuid = ((AbstractPlayerGirlEntity)girl).getOwnerUserUUID();
                              if (ownerUuid == null) {
                                 return;
                              }

                              EntityPlayer owner = entity.world.getPlayerEntityByUUID(ownerUuid);
                              renderEntity = owner == null ? girl : owner;
                           }

                           Vec3d transformPos = girl.renderCustomModelTransform(this.mc, entity, (EntityLivingBase)renderEntity, partialTicks);
                           BlockPos lightPos = new BlockPos(
                              Math.floor(((EntityLivingBase)renderEntity).posX),
                              Math.floor(((EntityLivingBase)renderEntity).posY),
                              Math.floor(((EntityLivingBase)renderEntity).posZ)
                           );
                           int lightLevel = ((EntityLivingBase)renderEntity).world.getLight(lightPos, true);
                           Vec3d whiteColor = new Vec3d(1.0, 1.0, 1.0);
                           float lightScale = ThreadNames.clampFloat(lightLevel, 10.0F, 15.0F) / 15.0F;
                           this.colorScale = new Vec3d(whiteColor.x * lightScale, whiteColor.y * lightScale, whiteColor.z * lightScale);
                           GlStateManager.pushMatrix();
                           GlStateManager.translate(transformPos.x, transformPos.y, transformPos.z);
                           if (girl.isAnchored()) {
                              GlStateManager.rotate(girl.getYawRotation(), 0.0F, 1.0F, 0.0F);
                           }

                           super.doRender(entity, 0.0, 0.0, 0.0, entityYaw, partialTicks);
                           GlStateManager.popMatrix();
                           GlStateManager.enableLighting();
                        }
                     }
                  }
               } else {
                  this.colorScale = new Vec3d(1.0, 1.0, 1.0);
                  super.doRender(entity, x, y, z, entityYaw, partialTicks);
                  GlStateManager.enableLighting();
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
   public static Vec3d getSceneEntityPosition(Minecraft mc, SexSceneEntity sceneEntity, EntityLivingBase host, BaseGirlEntity girl, float partialTicks) {
      Vec3d result;
      if (girl.isAnchored()) {
         Vec3d targetPos = girl.getTargetPosition();
         float yaw = girl.getYawRotation();
         sceneEntity.prevPosX = targetPos.x;
         sceneEntity.prevPosY = targetPos.y;
         sceneEntity.prevPosZ = targetPos.z;
         sceneEntity.lastTickPosX = targetPos.x;
         sceneEntity.lastTickPosY = targetPos.y;
         sceneEntity.lastTickPosZ = targetPos.z;
         sceneEntity.posX = targetPos.x;
         sceneEntity.posY = targetPos.y;
         sceneEntity.posZ = targetPos.z;
         sceneEntity.rotationYaw = yaw;
         sceneEntity.prevRotationYaw = yaw;
         sceneEntity.rotationYawHead = yaw;
         sceneEntity.prevRotationYawHead = yaw;
         sceneEntity.renderYawOffset = yaw;
         sceneEntity.prevRenderYawOffset = yaw;
         sceneEntity.rotationPitch = yaw;
         sceneEntity.prevRotationPitch = yaw;
         result = targetPos;
      } else {
         sceneEntity.rotationYaw = host.rotationYaw;
         sceneEntity.prevRotationYaw = host.prevRotationYaw;
         sceneEntity.rotationYawHead = host.rotationYawHead;
         sceneEntity.prevRotationYawHead = host.prevRotationYawHead;
         sceneEntity.renderYawOffset = host.renderYawOffset;
         sceneEntity.prevRenderYawOffset = host.prevRenderYawOffset;
         sceneEntity.rotationPitch = host.rotationPitch;
         sceneEntity.prevRotationPitch = host.prevRotationPitch;
         sceneEntity.prevPosX = host.prevPosX;
         sceneEntity.prevPosY = host.prevPosY;
         sceneEntity.prevPosZ = host.prevPosZ;
         sceneEntity.lastTickPosX = host.lastTickPosX;
         sceneEntity.lastTickPosY = host.lastTickPosY;
         sceneEntity.lastTickPosZ = host.lastTickPosZ;
         sceneEntity.posX = host.posX;
         sceneEntity.posY = host.posY;
         sceneEntity.posZ = host.posZ;
         result = RotationHelper.lerpVec3dDouble(new Vec3d(host.lastTickPosX, host.lastTickPosY, host.lastTickPosZ), host.getPositionVector(), partialTicks);
      }

      EntityPlayerSP localPlayer = mc.player;
      Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      return result.subtract(localPlayerPos);
   }

   /**
    * Renders the part model: bone-attach pass (unless the yaw sentinel
    * 1.876945, which is world-space), then the normal geckolib recursion with
    * the entity's own matrix stack.
    */
   @Override
   public void render(GeoModel model, SexSceneEntity entity, float partialTicks, float r, float g, float b, float a) {
      GlStateManager.disableCull();
      GlStateManager.enableRescaleNormal();
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      for (GeoBone bone : model.topLevelBones) {
         if (partialTicks != 1.876945F) {
            this.renderBone(entity, bone, partialTicks);
         }

         entity.matrixStack.translate(-bone.getPivotX() / 16.0F, -bone.getPivotY() / 16.0F, -bone.getPivotZ() / 16.0F);
         this.renderRecursively(buffer, bone, r, g, b, a);
      }

      Tessellator.getInstance().draw();
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableCull();
   }

   /**
    * The host entity whose pose the part follows: the owning player for
    * player-girls, else the girl itself.
    */
   EntityLivingBase getRenderEntityLiving(SexSceneEntity sceneEntity) {
      BaseGirlEntity girl = this.getRenderGirl(sceneEntity);
      if (girl == null) {
         return null;
      }

      Object renderEntity;
      if (!(girl instanceof AbstractPlayerGirlEntity)) {
         renderEntity = girl;
      } else {
         EntityPlayer owner = sceneEntity.world.getPlayerEntityByUUID(((AbstractPlayerGirlEntity)girl).getOwnerUserUUID());
         renderEntity = owner == null ? girl : owner;
      }

      return (EntityLivingBase)renderEntity;
   }

   /**
    * The host girl for the part (registry lookup, fallback to the client girl
    * list).
    */
   BaseGirlEntity getRenderGirl(SexSceneEntity sceneEntity) {
      UUID girlUuid = sceneEntity.getGirlIdFromCode();
      BaseGirlEntity girl = GirlRegistry.getGirl(girlUuid);
      return girl != null ? girl : BaseGirlEntity.getClientGirlEntity(girlUuid);
   }

   void renderBone(SexSceneEntity sceneEntity, GeoBone bone, float partialTicks) {
      String boneName = this.getBoneName(sceneEntity);
      if (boneName != null) {
         this.renderBoneEffect(sceneEntity, bone, partialTicks, boneName);
      }
   }

   /**
    * Attaches the part to the girl's bone: the part's root matrix is replaced
    * by the girl's bone matrix stack for the mapped bone name; item models
    * are scaled 0.5 and rotated by {@code ClothingScreen.currentModelYaw}.
    */
   void renderBoneEffect(SexSceneEntity sceneEntity, GeoBone bone, float partialTicks, String boneName) {
      BaseGirlEntity girl = this.getRenderGirl(sceneEntity);
      this.getRenderEntityLiving(sceneEntity);
      sceneEntity.matrixStack = girl.getBoneMatrixStack(boneName, false);
      if (sceneEntity.isItemModel && partialTicks == 2.876945F) {
         sceneEntity.matrixStack.scale(0.5F, 0.5F, 0.5F);
         sceneEntity.matrixStack.rotateY((float)Math.toRadians(-ClothingScreen.currentModelYaw));
      }
   }

   String getBoneName(SexSceneEntity sceneEntity) {
      if (sceneEntity.isItemModel) {
         return sceneEntity.boneType.boneName;
      } else {
         ServerWhitelistManager.ModelData modelData = ServerWhitelistManager.getModelDataForGirl(sceneEntity.getModelCode());
         if (modelData == null) {
            return null;
         } else {
            return BoneType.CUSTOM_BONE.equals(modelData.getBoneType()) ? modelData.getModelName() : modelData.getBoneType().boneName;
         }
      }
   }

   /**
    * Bone recursion with the entity's matrix stack: transform push, cube
    * render, children (unless hidden), pop (with IllegalStateException guard).
    */
   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a) {
      this.sceneEntity.matrixStack.push();
      this.sceneEntity.matrixStack.translate(bone);
      this.sceneEntity.matrixStack.moveToPivot(bone);
      this.sceneEntity.matrixStack.rotate(bone);
      this.sceneEntity.matrixStack.scale(bone);
      this.sceneEntity.matrixStack.moveBackFromPivot(bone);
      if (!bone.isHidden()) {
         for (GeoCube cube : bone.childCubes) {
            this.sceneEntity.matrixStack.push();
            GlStateManager.pushMatrix();
            this.renderCube(buffer, cube, r, g, b, a);
            GlStateManager.popMatrix();
            this.sceneEntity.matrixStack.pop();
         }
      }

      if (!bone.childBonesAreHiddenToo()) {
         for (GeoBone childBone : bone.childBones) {
            this.renderRecursively(buffer, childBone, r, g, b, a);
         }
      }

      try {
         this.sceneEntity.matrixStack.pop();
      } catch (IllegalStateException e) {
         // ignored: popping an already-empty matrix stack is harmless
      }
   }

   /**
    * Cube pass for the part: transformed normals (mirrored on zero-size
    * faces), optional SEXMOD fake lighting applied to the tint, and vertices
    * emitted through the entity matrix stack.
    */
   @Override
   public void renderCube(BufferBuilder buffer, GeoCube cube, float r, float g, float b, float a) {
      this.sceneEntity.matrixStack.moveToPivot(cube);
      this.sceneEntity.matrixStack.rotate(cube);
      this.sceneEntity.matrixStack.moveBackFromPivot(cube);

      for (GeoQuad quad : cube.quads) {
         if (quad != null) {
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            this.sceneEntity.matrixStack.getNormalMatrix().transform(normal);
            if ((cube.size.y == 0.0F || cube.size.z == 0.0F) && normal.getX() < 0.0F) {
               normal.x *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.z == 0.0F) && normal.getY() < 0.0F) {
               normal.y *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.y == 0.0F) && normal.getZ() < 0.0F) {
               normal.z *= -1.0F;
            }

            if (this.lightingPos != null) {
               this.colorScale = BodyParts.offsetBonePosition(this.colorScale, normal, this.lightingPos);
            }

            for (GeoVertex vertex : quad.vertices) {
               Vector4f matrixPos = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
               this.sceneEntity.matrixStack.getModelMatrix().transform(matrixPos);
               buffer.pos(matrixPos.getX(), matrixPos.getY(), matrixPos.getZ())
                  .tex(vertex.textureU, vertex.textureV)
                  .color((float)this.colorScale.x, (float)this.colorScale.y, (float)this.colorScale.z, a)
                  .normal(normal.getX(), normal.getY(), normal.getZ())
                  .endVertex();
            }
         }
      }
   }

}
