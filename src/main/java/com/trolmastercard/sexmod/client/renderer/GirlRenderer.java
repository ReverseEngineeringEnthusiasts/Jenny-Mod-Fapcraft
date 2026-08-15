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
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
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

/**
 * Core geckolib renderer for every girl entity ({@link BaseGirlEntity} +
 * {@link IAnimatable}): Jenny, Ellie, Bee, Manglelie, and via
 * {@link GirlRendererBase} the NPC-only girls. Owns skin textures, render
 * gating, the custom-bone pass (held items, payment overlay), camera-bone
 * world positions, anchored-scene positioning and armor tinting.
 * <p>
 * <b>Anchored rendering.</b> {@link #getBoneWorldPos(BaseGirlEntity, float, double, double, double)}
 * is where the anchored-scene pose lives: an anchored girl is rendered at
 * {@code getTargetPosition()} (relative to the local player) with her yaw
 * pinned to {@code getYawRotation()} — this is what places the girl correctly
 * during sex scenes. Do NOT remove the anchored offset or yaw-pinning, and
 * keep the {@code lerpVec3dDouble} partial-tick interpolation (PROGRESS lerp,
 * correct for render code).
 * <p>
 * <b>Render gating.</b> {@link #canRenderPlayer} hides the girl when a solid
 * block occludes every corner of her bounding box from the player's eye
 * (first person) so she cannot be seen through walls; {@link #renderModel}
 * additionally skips locally registered / non-renderable entities.
 * <p>
 * <b>Custom bones.</b> {@code weapon} draws the held item
 * ({@link #renderHeldItem}, bow-pull and sword-stab poses), {@code itemRenderer}
 * draws the payment item while the action is {@code PAYMENT}
 * ({@link #renderTradeOverlay}); {@code ballL/ballR/cock} are forced opaque;
 * {@code Head2} is hidden in first person; {@code armor*} bones get armor
 * material tinting ({@link #calculateBoneArmorColor}).
 * <p>
 * <b>Scene hooks.</b> After every render {@link #applyCameraBone} publishes
 * world positions for {@link GirlModel#CAMERA_PLACEMENTS} + tracked bones
 * (read by camera code), and
 * {@link SexSceneRenderer#renderSexSceneEffects} draws scene parts.
 * <p>
 * CLIENT-side render thread only. Skin textures are fetched once per UUID via
 * {@link SkinFetcher} and cached; the {@link SexWorldClient} preload world
 * skips the custom pass entirely.
 */
public abstract class GirlRenderer<T extends BaseGirlEntity & IAnimatable> extends GeoEntityRenderer<T> implements IGirlRenderer {
   protected static final ResourceLocation LINE_TEXTURE = new ResourceLocation("sexmod", "textures/line.png");
   protected double CACHE_C;
   protected T renderEntity;
   protected static Minecraft mc;
   protected static HashMap<UUID, ResourceLocation> l = new HashMap<>();
   Color BASE_SKIN_COLOR = new Color(245, 199, 165);
   Color BLUSH_COLOR = new Color(245, 157, 169);
   boolean fallbackSkinLoaded = false;
   protected HashSet<String> activeCustomPartBones = new HashSet<>();
   float bowPullProgress = 0.0F;
   public static BufferBuilder tempBuffer;
   protected GeoBone currentRenderingBone = null;

   public GirlRenderer(RenderManager renderManager, AnimatedGeoModel<?> model, double shadowOffset) {
      super(renderManager, (AnimatedGeoModel<T>) (AnimatedGeoModel) model);
      this.CACHE_C = shadowOffset;
      mc = Minecraft.getMinecraft();
      this.shadowSize = 0.2F;
   }

   @Override
   public ResourceLocation getEntityTexture(T entity) {
      return super.getEntityTexture(entity);
   }

   /**
    * Skin texture for the girl: the interaction player's cached skin, or a
    * freshly tinted one; in the preload world / without an interaction player
    * the local player's profile id is used. Cache lives in the static map.
    */
   protected ResourceLocation getSkinTexture(T girl) {
      ResourceLocation cachedTexture;
      if (!(girl.world instanceof SexWorldClient) && girl.getInteractionPlayerUUID() != null) {
         cachedTexture = l.get(girl.getInteractionPlayerUUID());
         if (cachedTexture == null) {
            return this.getTintedSkinTexture(girl.getInteractionPlayerUUID(), girl.world);
         }
      } else {
         cachedTexture = l.get(mc.getSession().getProfile().getId());
         if (cachedTexture == null) {
            return this.getTintedSkinTexture(mc.getSession().getProfile().getId(), girl.world);
         }
      }

      return cachedTexture;
   }

   /**
    * Fetches the player skin for {@code uuid} ({@link SkinFetcher}), repaints
    * the head-top/face pixels with the mod's skin-tone colors, registers it as
    * a dynamic texture and caches it. On any fetch error falls back to the
    * bundled steve texture (or an empty 64x64 image) and still caches.
    */
   protected ResourceLocation getTintedSkinTexture(UUID uuid, World world) {
      BufferedImage skinImage;
      try {
         skinImage = SkinFetcher.fetchSkin(uuid);
         Graphics graphics = skinImage.getGraphics();
         graphics.setColor(this.BASE_SKIN_COLOR);
         graphics.fillRect(0, 0, 4, 3);
         graphics.setColor(this.BLUSH_COLOR);
         graphics.fillRect(4, 0, 3, 3);
      } catch (Exception e) {
         if (!this.fallbackSkinLoaded) {
            this.fallbackSkinLoaded = true;
         }

         try {
            skinImage = ImageIO.read(mc.getResourceManager().getResource(new ResourceLocation("sexmod", "textures/player/steve.png")).getInputStream());
         } catch (Exception e2) {
            skinImage = new BufferedImage(64, 64, 2);
         }
      }

      l.put(uuid, this.renderManager.renderEngine.getDynamicTextureLocation("player" + uuid, new DynamicTexture(skinImage)));
      return l.get(uuid);
   }

   /**
    * Yaw for rendering: the pinned scene yaw when anchored, else the
    * interpolated render-yaw-offset (partial ticks).
    */
   protected static float getInterpolatedYaw(BaseGirlEntity girl, float partialTicks) {
      return girl.isAnchored() ? girl.getYawRotation() : RotationHelper.lerp(girl.prevRenderYawOffset, girl.renderYawOffset, partialTicks);
   }

   protected void renderLeftEye() {
   }

   protected void renderRightEye() {
   }

   float rayTraceBoneDistance(World world, Vec3d eyePos, float yaw, float pitch) {
      RayTraceResult hit = this.rayTraceBlocks(eyePos, eyePos.add(VectorMath.rotateByYawPitch(new Vec3d(0.0, 0.0, -4.0), yaw, pitch)), world);
      if (hit == null) {
         return 4.0F;
      }

      Vec3d hitVec = hit.hitVec;
      return hitVec == null ? 4.0F : (float)eyePos.distanceTo(hitVec);
   }

   /**
    * First-person occlusion gate: the girl is only rendered when at least one
    * corner of her bounding box has an unobstructed (or translucent/non-solid)
    * ray from the player's eye. Player-girls always render. In third person
    * the camera position is moved behind a solid block when needed.
    *
    * @return {@code true} if the girl may be drawn
    */
   boolean canRenderPlayer(T girl, EntityPlayer player) {
      if (girl instanceof AbstractPlayerGirlEntity) {
         return true;
      }

      World world = girl.world;
      Vec3d girlPos = girl.getPositionVector();
      float halfWidth = girl.width * 1.5F;
      float height = girl.height * 1.5F;
      Vec3d eyePos = player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0);
      int thirdPersonView = mc.gameSettings.thirdPersonView;
      if (thirdPersonView != 0) {
         return true;
      }

      if (thirdPersonView > 0) {
         float yaw = player.rotationYaw;
         float pitch = player.rotationPitch;
         if (thirdPersonView == 2) {
            pitch += 180.0F;
         }

         float distance = 4.0F;
         Vec3d cameraPos = eyePos.add(
            MathHelper.sin(yaw * (float) (Math.PI / 180.0)) * MathHelper.cos(pitch * (float) (Math.PI / 180.0)) * distance,
            MathHelper.sin(pitch * (float) (Math.PI / 180.0)) * distance,
            -MathHelper.cos(yaw * (float) (Math.PI / 180.0)) * MathHelper.cos(pitch * (float) (Math.PI / 180.0)) * distance
         );
         BlockPos cameraBlock = new BlockPos(cameraPos);
         boolean isAir = world.isAirBlock(cameraBlock);
         if (!isAir) {
            eyePos = cameraPos;
         } else if (world.isAirBlock(cameraBlock.add(0, 1, 0))) {
            eyePos = new Vec3d(cameraPos.x, cameraBlock.getY() + 1, cameraPos.z);
         }
      }

      Vec3d[] corners = new Vec3d[]{
         girlPos.add(-halfWidth / 2.0F, 0.0, -halfWidth / 2.0F),
         girlPos.add(-halfWidth / 2.0F, 0.0, halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, 0.0, -halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, 0.0, halfWidth / 2.0F),
         girlPos.add(-halfWidth / 2.0F, height, -halfWidth / 2.0F),
         girlPos.add(-halfWidth / 2.0F, height, halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, height, -halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, height, halfWidth / 2.0F)
      };

      for (Vec3d corner : corners) {
         RayTraceResult hit = this.rayTraceBlocks(eyePos, corner, world);
         if (hit == null) {
            return true;
         }

         IBlockState blockState = world.getBlockState(hit.getBlockPos());
         if (blockState.isTranslucent()) {
            return true;
         }

         if (blockState.getBlock().getRenderLayer() != BlockRenderLayer.SOLID) {
            return true;
         }
      }

      return false;
   }

   /**
    * The set of bones currently replaced by custom parts: from the
    * {@link ClothingScreen} preview (locally registered) or the girl's own
    * custom-part set; disabled models' bones are added regardless of
    * {@code isNudeOutfit}. Empty while {@code ClientProxy.IS_PRELOADING}.
    */
   HashSet<String> getActiveBones(Boolean isPreview, boolean isNudeOutfit) {
      if (ClientProxy.IS_PRELOADING) {
         return new HashSet<>();
      }

      HashSet boneNameSet;
      if (isPreview) {
         boneNameSet = ClothingScreen.getCustomBoneNames();
      } else {
         boneNameSet = this.renderEntity.getCustomPartsSet();
      }

      HashSet resultSet = new HashSet();

      for (String boneName : (java.util.Collection<String>) (boneNameSet) ) {
         ServerWhitelistManager.ModelData modelData = ServerWhitelistManager.getModelDataForGirl(boneName);
         if (modelData != null && (modelData.isDisabled() || !isNudeOutfit)) {
            resultSet.addAll(modelData.getCustomPartBones());
         }
      }

      return resultSet;
   }

   /**
    * Model render entry: gated by {@link #canRenderPlayer} / local-registration
    * / {@code shouldRenderModel()}, then captures the global matrix, binds the
    * entity texture, resolves active custom-part bones, updates custom bones +
    * bone offsets ({@code BodyParts.updateCustomBones/updateBoneOffset}) and
    * renders the buffer with early/late hooks.
    */
   public void renderModel(GeoModel model, T entity, float partialTicks, float r, float g, float b, float a) {
      if (mc.player == null || entity.isLocallyRegistered() || !entity.shouldRenderModel() || this.canRenderPlayer(entity, mc.player)) {
         GlStateManager.enableRescaleNormal();
         this.captureGlobalMatrix((T)entity, partialTicks, r, g, b, a);
         this.renderLate((T)entity, partialTicks, r, g, b, a);
         BufferBuilder buffer = Tessellator.getInstance().getBuffer();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
         this.activeCustomPartBones.clear();
         this.activeCustomPartBones = this.getActiveBones(entity.isLocallyRegistered(), entity.getOutfitIndex() == 0);
         this.getSkinTexture((T) this.renderEntity);
         BodyParts.updateCustomBones(entity.getAnimationProcessor().getModelRendererList(), this.getBlacklistedBones(), this);
         BodyParts.updateBoneOffset(entity, partialTicks);
         this.renderModelBuffer(model, buffer, (T)entity, r, g, b, a, partialTicks);
         this.renderAfter((T)entity, partialTicks, r, g, b, a);
         GlStateManager.disableRescaleNormal();
         GlStateManager.enableCull();
         GL20.glUseProgram(0);
      }
   }

   /**
    * Renders all top-level bones except the {@code steve} bone (the player
    * skin body), flushes, then renders the steve bone in a second pass with
    * the girl's skin texture and {@code getRenderScaleFactor()}.
    */
   protected void renderModelBuffer(GeoModel model, BufferBuilder buffer, T entity, float r, float g, float b, float a, float scale) {
      GeoBone steveBone = null;

      for (GeoBone bone : model.topLevelBones) {
         if (bone.getName().equals("steve")) {
            steveBone = bone;
         } else {
            this.renderRecursively(buffer, bone, r, g, b, a);
         }
      }

      Tessellator.getInstance().draw();
      this.renderRightEye();
      if (steveBone != null) {
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

         Minecraft.getMinecraft().renderEngine.bindTexture(this.getSkinTexture(this.renderEntity));

         this.renderRecursively(buffer, steveBone, r, g, b, this.renderEntity.getRenderScaleFactor());
         Tessellator.getInstance().draw();
      }
   }

   String buildModelCode(String path) {
      StringBuilder builder = new StringBuilder();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(path));

         String line;
         while ((line = reader.readLine()) != null) {
            builder.append(line).append("//\n");
         }

         reader.close();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return builder.toString();
   }

   protected void renderNameTag(double x, double y, double z) {
      if (!this.renderEntity.isLocallyRegistered()) {
         if (!this.renderEntity.getCurrentAction().hideNameTag) {
            if (mc.getRenderManager().renderViewEntity != null) {
               this.renderLivingLabel(this.renderEntity, this.renderEntity.getEffectiveDisplayName(), x, y + this.renderEntity.getScaleFactor(), z, 300);
            }
         }
      }
   }

   /**
    * Render offset while the girl's master rides a saddled horse: positions
    * the girl behind the horse's head (relative to the local player) and
    * copies the ridden entity's yaw onto her.
    */
   Vec3d getRidingOffset(EntityPlayer player, float partialTicks) {
      EntityLiving ridingEntity = (EntityLiving)player.getRidingEntity();
      EntityPlayerSP localPlayer = mc.player;
      Vec3d lookVec = ridingEntity.getLookVec();
      Vec3d playerPos = RotationHelper.lerpVec3dDouble(new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ), player.getPositionVector(), partialTicks);
      Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      localPlayerPos = playerPos.subtract(localPlayerPos);
      this.renderEntity.renderYawOffset = ridingEntity.renderYawOffset;
      return new Vec3d(localPlayerPos.x + lookVec.x * -0.5, localPlayerPos.y + 0.15F, localPlayerPos.z + lookVec.z * -0.5);
   }

   /**
    * Hook for subclasses to override the final bone world position.
    */
   protected Vec3d getBoneWorldPos(T entity, float partialTicks, Vec3d pos) {
      return pos;
   }

   /**
    * Computes the render position for the girl (CLIENT-side).
    * <p>
    * <b>Anchored girls render at {@code getTargetPosition()},</b> relative to
    * the lerped local player position, with every yaw field pinned to
    * {@code getYawRotation()} — this is the scene pose; player-girls owned by
    * another player only do this in third person. Unanchored girls render at
    * their world position. Also: name-tag rendering, horse-riding offset, and
    * a no-op for the {@link SexWorldClient} preload world.
    * <p>
    * <b>Pitfall:</b> the partial-tick lerp here uses
    * {@link RotationHelper#lerpVec3dDouble} (PROGRESS) — correct for render
    * interpolation; do not switch to the INT step variant.
    */
   Vec3d getBoneWorldPos(T entity, float partialTicks, double x, double y, double z) {
      Vec3d pos = new Vec3d(x, y, z);
      if (entity.world instanceof SexWorldClient) {
         return pos;
      }

      if (entity.shouldRenderNameTag() && (!(entity instanceof AbstractPlayerGirlEntity) || mc.gameSettings.thirdPersonView != 0)) {
         this.renderNameTag(x, y, z);
      }

      EntityPlayer master = entity.getMasterPlayer();
      if (master != null && master.isRiding() && master.getRidingEntity() instanceof EntityHorse && ((EntityHorse)master.getRidingEntity()).isHorseSaddled()) {
         return this.getRidingOffset(master, partialTicks);
      }

      if (!entity.isAnchored()) {
         return pos;
      }

      if (!(entity instanceof AbstractPlayerGirlEntity) || !((AbstractPlayerGirlEntity)entity).hasOwnerUUID() || mc.gameSettings.thirdPersonView == 0) {
         Vec3d lerpedPlayerPos = RotationHelper.lerpVec3dDouble(
            new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), mc.player.getPositionVector(), partialTicks
         );
         pos = entity.getTargetPosition().subtract(lerpedPlayerPos);
      }

      float yaw = entity.getYawRotation();
      entity.rotationYaw = yaw;
      entity.prevRenderYawOffset = yaw;
      entity.renderYawOffset = yaw;
      entity.prevRotationYawHead = yaw;
      entity.rotationYawHead = yaw;
      return pos;
   }

   protected void onBoneRenderStart(T entity) {
   }

   @Override
   public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
      this.doRenderEntity(entity, x, y, z, entityYaw, partialTicks);
   }

   /**
    * The full girl render pipeline (CLIENT-side): resolves the bone world
    * position (anchored/target-position logic, see {@link #getBoneWorldPos}),
    * draws the leash, then — with lighting off, alpha 0.5, blend on — builds
    * the geckolib {@link AnimationEvent} (head yaw/pitch, limb swing, sitting
    * pose with vanilla riding clamps), renders the model, layers, camera-bone
    * positions, scene effects ({@link SexSceneRenderer#renderSexSceneEffects})
    * and the optional girl overlay color. Restores lighting/blend state.
    * <p>
    * <b>Ordering matters:</b> model -> layers -> camera bone -> scene effects.
    * The camera bone pass must run after the model is drawn so
    * {@code GirlModel.CAMERA_PLACEMENTS} reflect this frame's pose.
    */
   public void doRenderEntity(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
      this.renderEntity = (T)entity;
      Vec3d boneWorldPos = this.getBoneWorldPos((T)entity, partialTicks, x, y, z);
      boneWorldPos = this.getBoneWorldPos((T)entity, partialTicks, boneWorldPos);
      x = boneWorldPos.x;
      y = boneWorldPos.y;
      z = boneWorldPos.z;
      this.onBoneRenderStart((T)entity);
      if (entity.getLeashed()) {
         this.renderLeash(entity, x, y + this.CACHE_C, z, partialTicks);
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, z);
      GL11.glDisable(2896);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.enableNormalize();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      boolean isSitting = entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit();
      if (isSitting) {
         EntityModelData ridingModelData = new EntityModelData();
         ridingModelData.isSitting = isSitting;
         ridingModelData.isChild = entity.isChild();
         float ridingRenderYawOffset = Interpolations.lerpYaw(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
         float ridingRotationYawHead = Interpolations.lerpYaw(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
         float ridingHeadYawDelta = ridingRotationYawHead - ridingRenderYawOffset;
         if (entity.getRidingEntity() instanceof EntityLivingBase) {
            EntityLivingBase ridingEntity = (EntityLivingBase)entity.getRidingEntity();
            ridingRenderYawOffset = Interpolations.lerpYaw(ridingEntity.prevRenderYawOffset, ridingEntity.renderYawOffset, partialTicks);
            ridingHeadYawDelta = ridingRotationYawHead - ridingRenderYawOffset;
            float clampedHeadYawDelta = MathHelper.wrapDegrees(ridingHeadYawDelta);
            if (clampedHeadYawDelta < -85.0F) {
               clampedHeadYawDelta = -85.0F;
            }

            if (clampedHeadYawDelta >= 85.0F) {
               clampedHeadYawDelta = 85.0F;
            }

            ridingRenderYawOffset = ridingRotationYawHead - clampedHeadYawDelta;
            if (clampedHeadYawDelta * clampedHeadYawDelta > 2500.0F) {
               ridingRenderYawOffset += clampedHeadYawDelta * 0.2F;
            }

            ridingHeadYawDelta = ridingRotationYawHead - ridingRenderYawOffset;
         }

         float ridingPitch = Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTicks);
         float ridingRotationFloat = this.handleRotationFloat((T)entity, partialTicks);
         this.applyRotations((T)entity, ridingRotationFloat, ridingRenderYawOffset, partialTicks);
         float ridingLimbSwingAmount = 0.0F;
         float ridingLimbSwing = 0.0F;
         ridingModelData.headPitch = -ridingPitch;
         ridingModelData.netHeadYaw = -ridingHeadYawDelta;
         AnimationEvent ridingAnimationEvent = new AnimationEvent<>(entity, ridingLimbSwing, ridingLimbSwingAmount, partialTicks, false, Collections.singletonList(ridingModelData));
         GeoModelProvider ridingModelProvider = super.getGeoModelProvider();
         ResourceLocation ridingModelLocation = ridingModelProvider.getModelLocation(entity);
         GeoModel ridingModel = ridingModelProvider.getModel(ridingModelLocation);
         if (ridingModelProvider instanceof IAnimatableModel) {
            ((IAnimatableModel)ridingModelProvider).setLivingAnimations(entity, entity.getUniqueID().hashCode(), ridingAnimationEvent);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.01F, 0.0F);
         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture((T)entity));
         software.bernie.geckolib3.core.util.Color ridingRenderColor = this.getRenderColor((T)entity, partialTicks);
         boolean ridingHurtColorSet = this.setDoRenderBrightness((T)entity, partialTicks);
         this.renderModel(ridingModel, (T)entity, partialTicks, ridingRenderColor.getRed() / 255.0F, ridingRenderColor.getBlue() / 255.0F, ridingRenderColor.getGreen() / 255.0F, ridingRenderColor.getAlpha() / 255.0F);
         if (ridingHurtColorSet) {
            RenderHurtColor.unset();
         }

         for (GeoLayerRenderer ridingLayerRenderer : this.layerRenderers) {
            ridingLayerRenderer.render((T)entity, ridingLimbSwing, ridingLimbSwingAmount, partialTicks, ridingLimbSwing, ridingHeadYawDelta, ridingPitch, ridingRenderColor);
         }

         GL11.glEnable(2896);
         GlStateManager.disableBlend();
         GlStateManager.disableNormalize();
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
         this.applyCameraBone((T)entity);
         SexSceneRenderer.renderSexSceneEffects(entity, partialTicks);
         Vector3fSexmodSpecial ridingOverlayColor = this.getAdditionalOverlayColor((T)entity);
         if (ridingOverlayColor != null) {
            this.renderGirlColor(entity, partialTicks, ridingOverlayColor);
         }
      } else {
         EntityModelData modelData = new EntityModelData();
         modelData.isSitting = isSitting;
         modelData.isChild = entity.isChild();
         float renderYawOffset = Interpolations.lerpYaw(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
         float rotationYawHead = Interpolations.lerpYaw(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
         float headYawDelta = rotationYawHead - renderYawOffset;
         float pitch = Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTicks);
         float rotationFloat = this.handleRotationFloat((T)entity, partialTicks);
         this.applyRotations((T)entity, rotationFloat, renderYawOffset, partialTicks);
         float limbSwingAmount = 0.0F;
         float limbSwing = 0.0F;
         if (entity.isEntityAlive()) {
            limbSwingAmount = Interpolations.lerp(entity.prevLimbSwingAmount, entity.limbSwingAmount, partialTicks);
            limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
            if (entity.isChild()) {
               limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
               limbSwingAmount = 1.0F;
            }
         }

         modelData.headPitch = -pitch;
         modelData.netHeadYaw = -headYawDelta;
         AnimationEvent animationEvent = new AnimationEvent<>(entity, limbSwing, limbSwingAmount, partialTicks, !(limbSwingAmount > -0.15F) || !(limbSwingAmount < 0.15F), Collections.singletonList(modelData));
         GeoModelProvider modelProvider = super.getGeoModelProvider();
         ResourceLocation modelLocation = modelProvider.getModelLocation(entity);
         GeoModel model = modelProvider.getModel(modelLocation);
         if (modelProvider instanceof IAnimatableModel) {
            ((IAnimatableModel)modelProvider).setLivingAnimations(entity, entity.getUniqueID().hashCode(), animationEvent);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.01F, 0.0F);
         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture((T)entity));
         software.bernie.geckolib3.core.util.Color renderColor = this.getRenderColor((T)entity, partialTicks);
         boolean hurtColorSet = this.setDoRenderBrightness((T)entity, partialTicks);
         this.renderModel(model, (T)entity, partialTicks, renderColor.getRed() / 255.0F, renderColor.getBlue() / 255.0F, renderColor.getGreen() / 255.0F, renderColor.getAlpha() / 255.0F);
         if (hurtColorSet) {
            RenderHurtColor.unset();
         }

         for (GeoLayerRenderer layerRenderer : this.layerRenderers) {
            layerRenderer.render((T)entity, limbSwing, limbSwingAmount, partialTicks, limbSwing, headYawDelta, pitch, renderColor);
         }

         GL11.glEnable(2896);
         GlStateManager.disableBlend();
         GlStateManager.disableNormalize();
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
         this.applyCameraBone((T)entity);
         SexSceneRenderer.renderSexSceneEffects(entity, partialTicks);
         Vector3fSexmodSpecial overlayColor = this.getAdditionalOverlayColor((T)entity);
         if (overlayColor != null) {
            this.renderGirlColor(entity, partialTicks, overlayColor);
         }
      }
   }

   /**
    * Publishes the world positions of every camera/attachment bone
    * ({@link GirlModel#CAMERA_PLACEMENTS} + the girl's own tracking list) from
    * the model matrices into the girl's bone-position cache — consumed by the
    * scene camera and effect renderers. Must run after the model render.
    */
   void applyCameraBone(T entity) {
      ArrayList boneNames = new ArrayList<>(GirlModel.CAMERA_PLACEMENTS);
      boneNames.addAll(entity.boneTrackingList);

      for (String boneName : (java.util.Collection<String>) (boneNames) ) {
         MatrixStack matrixStack = entity.getBoneMatrixStack(boneName, !entity.isLocallyRegistered());
         Matrix4f modelMatrix = matrixStack.getModelMatrix();
         Vec3d worldPos = new Vec3d(-modelMatrix.m03, modelMatrix.m13, -modelMatrix.m23);
         entity.setBoneWorldPosition(boneName, worldPos);
      }
   }

   @Nullable
   protected Vector3fSexmodSpecial getAdditionalOverlayColor(T entity) {
      return null;
   }

   public Entity getRenderEntity(BaseGirlEntity girl) {
      return girl;
   }

   /**
    * Draws the colored line overlay (e.g. corrupted girl tint) anchored to the
    * girl (target position when anchored, else lerped) relative to the local
    * player, with line width from {@link #getRenderOffset}.
    */
   void renderGirlColor(BaseGirlEntity girl, float partialTicks, Vector3fSexmodSpecial overlayColor) {
      EntityPlayerSP localPlayer = mc.player;
      overlayColor = new Vector3fSexmodSpecial(overlayColor.x / 255.0F, overlayColor.y / 255.0F, overlayColor.z / 255.0F);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0, 0.01, 0.0);
      Entity renderEntity = this.getRenderEntity(girl);
      Vec3d girlPos = girl.isAnchored()
         ? girl.getTargetPosition()
         : RotationHelper.lerpVec3dDouble(new Vec3d(renderEntity.lastTickPosX, renderEntity.lastTickPosY, renderEntity.lastTickPosZ), renderEntity.getPositionVector(), partialTicks);
      Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      Vec3d offset = girlPos.subtract(localPlayerPos);
      GlStateManager.translate(offset.x, offset.y, offset.z);
      mc.getTextureManager().bindTexture(LINE_TEXTURE);
      float lineWidth = getRenderOffset(girl, partialTicks, 1.0F, 5.0F);
      this.drawOverlayLines(tessellator, buffer, girl, overlayColor, lineWidth);
      GlStateManager.popMatrix();
   }

   /**
    * Line width scaled by the girl's distance from the camera (5 blocks = max
    * width, closer = thinner), clamped 0..1 progress.
    */
   protected static float getRenderOffset(BaseGirlEntity girl, float partialTicks, float minWidth, float maxWidth) {
      EntityPlayerSP localPlayer = mc.player;
      Entity renderEntity = ((GirlRenderer)mc.getRenderManager().getEntityRenderObject(girl)).getRenderEntity(girl);
      Vec3d girlPos = girl.isAnchored()
         ? girl.getTargetPosition()
         : RotationHelper.lerpVec3dDouble(new Vec3d(renderEntity.lastTickPosX, renderEntity.lastTickPosY, renderEntity.lastTickPosZ), renderEntity.getPositionVector(), partialTicks);
      Vec3d playerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      Vec3d cameraPos = ActiveRenderInfo.getCameraPosition().add(playerPos);
      float distance = (float)cameraPos.distanceTo(girlPos);
      float clampedDistance = Math.abs(distance) / 5.0F;
      return RotationHelper.lerp(maxWidth, minWidth, ThreadNames.clampFloat(clampedDistance, 0.0F, 1.0F));
   }

   protected void drawOverlayLines(Tessellator tessellator, BufferBuilder buffer, BaseGirlEntity girl, Vector3fSexmodSpecial color, float lineWidth) {
   }

   /**
    * Draws a single colored line segment between two cached bone offsets.
    */
   protected static void renderNameLabel(BufferBuilder buffer, Tessellator tessellator, BaseGirlEntity girl, String startBone, String endBone, float r, float g, float b, float width) {
      buffer.begin(1, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.glLineWidth(width);
      Vec3d startPos = girl.getCachedBoneOffset(startBone);
      Vec3d endPos = girl.getCachedBoneOffset(endBone);
      buffer.pos(startPos.x, startPos.y, startPos.z)
         .tex(0.0, 0.0)
         .color(r, g, b, 1.0F)
         .endVertex();
      buffer.pos(endPos.x, endPos.y, endPos.z)
         .tex(0.0, 0.0)
         .color(r, g, b, 1.0F)
         .endVertex();
      tessellator.draw();
   }

   /**
    * Draws the bra-string tint chain (all {@code braString*} segments) in the
    * given color — used by the corruption/buff overlay renderers.
    */
   protected static void renderGirlTint(Tessellator tessellator, BufferBuilder buffer, BaseGirlEntity girl, Vector3fSexmodSpecial color, float width) {
      renderNameLabel(buffer, tessellator, girl, "braStringMidStartR", "braStringMidMid1R", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid1R", "braStringMidMid2R", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid2R", "braStringMidMid3R", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid3R", "braStringMidEndR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidEndR", "braStringBackR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringBackR", "braStringRightEndR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringRightEndR", "braStringRightStartR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringRightR", "braStringRightL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidStartL", "braStringMidMid1L", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid1L", "braStringMidMid2L", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid2L", "braStringMidMid3L", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid3L", "braStringMidEndL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidEndL", "braStringBackL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringBackL", "braStringLeftEndL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringLeftEndL", "braStringLeftStartL", color.x, color.y, color.z, width);
   }

   /**
    * Rotation overrides for player-girls: while the owner elytra-flies, the
    * girl pitches nose-down (clamped by flight time) and banks into the turn
    * like the owner.
    */
   protected void applyRotations(T entity, float rotationFloat, float renderYawOffset, float partialTicks) {
      super.applyRotations((T)entity, rotationFloat, renderYawOffset, partialTicks);
      if (entity instanceof AbstractPlayerGirlEntity) {
         UUID ownerUuid = ((AbstractPlayerGirlEntity)entity).getOwnerUserUUID();
         if (ownerUuid != null) {
            EntityPlayer owner = entity.world.getPlayerEntityByUUID(ownerUuid);
            if (owner != null) {
               if (owner.isElytraFlying()) {
                  float elytraTicks = owner.getTicksElytraFlying() + partialTicks;
                  float flightProgress = MathHelper.clamp(elytraTicks * elytraTicks / 100.0F, 0.0F, 1.0F);
                  GlStateManager.rotate(flightProgress * (-90.0F - owner.rotationPitch), 1.0F, 0.0F, 0.0F);
                  Vec3d lookVec = owner.getLook(partialTicks);
                  double motionSq = owner.motionX * owner.motionX + owner.motionZ * owner.motionZ;
                  double lookSq = lookVec.x * lookVec.x + lookVec.z * lookVec.z;
                  if (motionSq > 0.0 && lookSq > 0.0) {
                     double cosAngle = (owner.motionX * lookVec.x + owner.motionZ * lookVec.z) / (Math.sqrt(motionSq) * Math.sqrt(lookSq));
                     double crossZ = owner.motionX * lookVec.z - owner.motionZ * lookVec.x;
                     GlStateManager.rotate((float)(Math.signum(crossZ) * Math.acos(cosAngle)) * 180.0F / (float) Math.PI, 0.0F, 1.0F, 0.0F);
                  }
               }
            }
         }
      }
   }

   protected void onBoneProcessing(BufferBuilder buffer, String boneName, GeoBone bone) {
   }

   /**
    * Vanilla-style leash render (two brown strip passes with a sagging curve)
    * between the girl and her leash holder.
    */
   protected void renderLeash(BaseGirlEntity girl, double x, double y, double z, float partialTicks) {
      Entity holder = girl.getLeashHolder();
      y -= (1.6 - girl.height) * 0.5;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      double holderYaw = RotationHelper.lerp(holder.prevRotationYaw, holder.rotationYaw, partialTicks * 0.5F) * (float) (Math.PI / 180.0);
      double holderPitch = RotationHelper.lerp(holder.prevRotationPitch, holder.rotationPitch, partialTicks * 0.5F) * (float) (Math.PI / 180.0);
      double cosYaw = Math.cos(holderYaw);
      double sinYaw = Math.sin(holderYaw);
      double sinPitch = Math.sin(holderPitch);
      if (holder instanceof EntityHanging) {
         cosYaw = 0.0;
         sinYaw = 0.0;
         sinPitch = -1.0;
      }

      double cosPitch = Math.cos(holderPitch);
      double holderX = RotationHelper.lerpDouble(holder.prevPosX, holder.posX, partialTicks) - cosYaw * 0.7 - sinYaw * 0.5 * cosPitch;
      double holderY = RotationHelper.lerpDouble(holder.prevPosY + holder.getEyeHeight() * 0.7, holder.posY + holder.getEyeHeight() * 0.7, partialTicks)
         - sinPitch * 0.5
         - 0.25;
      double holderZ = RotationHelper.lerpDouble(holder.prevPosZ, holder.posZ, partialTicks) - sinYaw * 0.7 + cosYaw * 0.5 * cosPitch;
      double girlYaw = RotationHelper.lerp(girl.prevRenderYawOffset, girl.renderYawOffset, partialTicks) * (float) (Math.PI / 180.0) + (Math.PI / 2);
      cosYaw = Math.cos(girlYaw) * girl.width * 0.4;
      sinYaw = Math.sin(girlYaw) * girl.width * 0.4;
      double girlX = RotationHelper.lerpDouble(girl.prevPosX, girl.posX, partialTicks) + cosYaw;
      double girlY = RotationHelper.lerpDouble(girl.prevPosY, girl.posY, partialTicks);
      double girlZ = RotationHelper.lerpDouble(girl.prevPosZ, girl.posZ, partialTicks) + sinYaw;
      x += cosYaw;
      z += sinYaw;
      double deltaX = (float)(holderX - girlX);
      double deltaY = (float)(holderY - girlY);
      double deltaZ = (float)(holderZ - girlZ);
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for (int segment = 0; segment <= 24; segment++) {
         float r = 0.5F;
         float g = 0.4F;
         float b = 0.3F;
         if (segment % 2 == 0) {
            r = 0.35F;
            g = 0.28F;
            b = 0.21000001F;
         }

         float progress = segment / 24.0F;
         buffer.pos(
               x + deltaX * progress + 0.0, y + deltaY * (progress * progress + progress) * 0.5 + ((24.0F - segment) / 18.0F + 0.125F), z + deltaZ * progress
            )
            .color(r, g, b, 1.0F)
            .endVertex();
         buffer.pos(
               x + deltaX * progress + 0.025, y + deltaY * (progress * progress + progress) * 0.5 + ((24.0F - segment) / 18.0F + 0.125F) + 0.025, z + deltaZ * progress
            )
            .color(r, g, b, 1.0F)
            .endVertex();
      }

      tessellator.draw();
      buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for (int segment2 = 0; segment2 <= 24; segment2++) {
         float r2 = 0.5F;
         float g2 = 0.4F;
         float b2 = 0.3F;
         if (segment2 % 2 == 0) {
            r2 = 0.35F;
            g2 = 0.28F;
            b2 = 0.21000001F;
         }

         float progress2 = segment2 / 24.0F;
         buffer.pos(
               x + deltaX * progress2 + 0.0, y + deltaY * (progress2 * progress2 + progress2) * 0.5 + ((24.0F - segment2) / 18.0F + 0.125F) + 0.025, z + deltaZ * progress2
            )
            .color(r2, g2, b2, 1.0F)
            .endVertex();
         buffer.pos(
               x + deltaX * progress2 + 0.025, y + deltaY * (progress2 * progress2 + progress2) * 0.5 + ((24.0F - segment2) / 18.0F + 0.125F), z + deltaZ * progress2 + 0.025
            )
            .color(r2, g2, b2, 1.0F)
            .endVertex();
      }

      tessellator.draw();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.enableCull();
   }

   /**
    * Recursive bone render: per bone — held-item / trade-overlay hooks,
    * forced opacity for {@code ballL/ballR/cock}, {@link #onBoneProcessing},
    * armor tinting, and skipping (Head2 in first person, disallowed bones,
    * custom-part-replaced bones). Children switch to {@link #renderCustomBones}
    * when the bone carries an armor overlay (nonzero overlay alpha).
    */
   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String boneName = bone.getName();
         if (boneName.equals("weapon") && this.renderEntity instanceof AbstractGirlNpcEntity) {
            this.renderHeldItem(buffer, bone);
         }

         if (boneName.equals("itemRenderer") && this.renderEntity.getCurrentAction() == Action.PAYMENT) {
            this.renderTradeOverlay(buffer, bone);
         }

         if (boneName.equals("ballL") || boneName.equals("ballR") || boneName.equals("cock")) {
            a = 1.0F;
         }

         tempBuffer = buffer;
         this.onBoneProcessing(buffer, boneName, bone);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(bone);
         MATRIX_STACK.moveToPivot(bone);
         MATRIX_STACK.rotate(bone);
         MATRIX_STACK.scale(bone);
         MATRIX_STACK.moveBackFromPivot(bone);
         if ("Head2".equals(boneName) && !this.shouldRenderHead2()) {
            MATRIX_STACK.pop();
         } else if (!this.isBoneAllowedForRender(boneName)) {
            MATRIX_STACK.pop();
         } else {
            if (!bone.isHidden) {
               Vector4f armorColor = this.calculateBoneArmorColor(boneName, r, g, b);
               r = armorColor.x;
               g = armorColor.y;
               b = armorColor.z;
               double armorAlpha = armorColor.w;
               if (!this.activeCustomPartBones.contains(boneName)) {
                  for (GeoCube cube : bone.childCubes) {
                     MATRIX_STACK.push();
                     this.currentRenderingBone = bone;
                     this.renderCubeGeometry(buffer, cube, r, g, b, a, armorAlpha);
                     MATRIX_STACK.pop();
                  }
               }

               for (GeoBone childBone : bone.childBones) {
                  if (armorAlpha == 0.0) {
                     this.renderRecursively(buffer, childBone, r, g, b, a);
                  } else {
                     this.renderCustomBones(buffer, childBone, r, g, b, a, armorAlpha);
                  }
               }
            }

            try {
               MATRIX_STACK.pop();
            } catch (IllegalStateException e) {
            }
         }
      }
   }

   protected Vector4f createOverlayColor(float r, float g, float b) {
      return new Vector4f(r, g, b, 0.0F);
   }

   /**
    * Armor bones only render on NPC girls (the player's own armor is drawn by
    * the vanilla player renderer).
    */
   boolean isBoneAllowedForRender(String boneName) {
      return !boneName.startsWith("armor") ? true : this.renderEntity instanceof AbstractGirlNpcEntity;
   }

   /**
    * Armor tint for {@code armor*} bones of NPC girls with an outfit: gold
    * (alpha 72/4096), iron/chain (144/4096), or leather tinted by the armor's
    * dye color. Returns the unchanged base color otherwise. The alpha encodes
    * the armor overlay strength.
    */
   protected Vector4f calculateBoneArmorColor(String boneName, float r, float g, float b) {
      if (!boneName.startsWith("armor")) {
         return this.createOverlayColor(r, g, b);
      }

      if (!(this.renderEntity instanceof AbstractGirlNpcEntity)) {
         return this.createOverlayColor(r, g, b);
      }

      if ((Integer)this.renderEntity.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) == 0) {
         return this.createOverlayColor(r, g, b);
      }

      GeoModelProvider modelProvider = this.getGeoModelProvider();
      if (!(modelProvider instanceof GirlModel)) {
         return this.createOverlayColor(r, g, b);
      }

      GirlModel girlModel = (GirlModel)modelProvider;
      ItemStack armorStack = girlModel.getItemStackForBone(this.renderEntity, boneName);
      if (!(armorStack.getItem() instanceof ItemArmor)) {
         return this.createOverlayColor(r, g, b);
      }

      ItemArmor armorItem = (ItemArmor)armorStack.getItem();
      ArmorMaterial armorMaterial = armorItem.getArmorMaterial();
      float overlayStrength = 0.0F;
      switch (armorMaterial) {
         case GOLD:
            overlayStrength = 1.0F;
            break;
         case CHAIN:
         case IRON:
            overlayStrength = 2.0F;
            break;
         case LEATHER:
            overlayStrength = 4.0F;
            int dyeColor = armorItem.getColor(armorStack);
            float rTint = (dyeColor >> 16 & 0xFF) / 255.0F;
            float gTint = (dyeColor >> 8 & 0xFF) / 255.0F;
            float bTint = (dyeColor & 0xFF) / 255.0F;
            r *= rTint;
            g *= gTint;
            b *= bTint;
      }

      return new Vector4f(r, g, b, 72.0F * overlayStrength / 4096.0F);
   }

   /**
    * Captures the current model matrix for custom-part world-position
    * computation later in the frame.
    */
   public void captureGlobalMatrix(T entity, float partialTicks, float r, float g, float b, float a) {
   }

   /**
    * Custom-bone recursion (used for armor-overlay children and by
    * {@link GirlRendererBase}): like {@link #renderRecursively} minus the
    * armor tinting, with per-cube GL pushes and forced opacity on the genital
    * bones. Skipped in the {@link SexWorldClient} preload world.
    */
   public void renderCustomBones(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a, double overlayAlpha) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String boneName = bone.getName();
         if (boneName.equals("weapon")) {
            this.renderHeldItem(buffer, bone);
         }

         if (boneName.equals("ballL") || boneName.equals("ballR") || boneName.equals("cock")) {
            a = 1.0F;
         }

         this.onBoneProcessing(buffer, bone.getName(), bone);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(bone);
         MATRIX_STACK.moveToPivot(bone);
         MATRIX_STACK.rotate(bone);
         MATRIX_STACK.scale(bone);
         MATRIX_STACK.moveBackFromPivot(bone);
         if (!bone.isHidden) {
            if (!this.activeCustomPartBones.contains(boneName)) {
               for (GeoCube cube : bone.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.pushMatrix();
                  this.currentRenderingBone = bone;
                  this.renderCubeGeometry(buffer, cube, r, g, b, a, overlayAlpha);
                  GlStateManager.popMatrix();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone childBone : bone.childBones) {
               this.renderCustomBones(buffer, childBone, r, g, b, a, overlayAlpha);
            }
         }

         MATRIX_STACK.pop();
      }
   }

   /**
    * The {@code Head2} bone is hidden in first person while the girl is
    * controlled by the local player (it would clip into the view).
    */
   protected boolean shouldRenderHead2() {
      return !this.renderEntity.isControlledByLocalPlayer() ? true : mc.gameSettings.thirdPersonView != 0;
   }

   public void renderCubeGeometry(BufferBuilder buffer, GeoCube cube, float r, float g, float b, float a, double textureVOffset) {
      MATRIX_STACK.moveToPivot(cube);
      MATRIX_STACK.rotate(cube);
      MATRIX_STACK.moveBackFromPivot(cube);

      for (GeoQuad quad : cube.quads) {
         if (quad != null) {
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            MATRIX_STACK.getNormalMatrix().transform(normal);
            if ((cube.size.y == 0.0F || cube.size.z == 0.0F) && normal.getX() < 0.0F) {
               normal.x *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.z == 0.0F) && normal.getY() < 0.0F) {
               normal.y *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.y == 0.0F) && normal.getZ() < 0.0F) {
               normal.z *= -1.0F;
            }

            Vec3d worldColor = BodyParts.getBoneWorldPosition(this, this.currentRenderingBone, new Vec3d(r, g, b), normal);

            for (GeoVertex vertex : quad.vertices) {
               Vector4f matrixPos = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(matrixPos);
               buffer.pos(matrixPos.getX(), matrixPos.getY(), matrixPos.getZ())
                  .tex(vertex.textureU + textureVOffset, vertex.textureV)
                  .color((float)worldColor.x, (float)worldColor.y, (float)worldColor.z, a)
                  .normal(normal.getX(), normal.getY(), normal.getZ())
                  .endVertex();
            }
         }
      }
   }

   /**
    * Payment item shown on the {@code itemRenderer} bone during a
    * {@code PAYMENT} action, keyed by the girl's current hand state:
    * doggy=2 diamonds, blowjob=3 emeralds, strip=1 gold ingot,
    * boobjob=2 ender pearls, touch_boobs=2 fish(1), sex=3 fish, else none.
    */
   protected ItemStack getPaymentItemStack() {
      switch ((String)this.renderEntity.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES)) {
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

   /**
    * Renders the payment items fanned in front of the girl on the
    * {@code itemRenderer} bone: per item, flush pending vertices, apply the
    * bone transform + rotations, fan 2nd/3rd items sideways, then re-bind the
    * entity texture and restart the buffer.
    */
   protected void renderTradeOverlay(BufferBuilder buffer, GeoBone bone) {
      ItemStack paymentStack = this.getPaymentItemStack();
      if (paymentStack != null) {
         ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();

         for (int i = 0; i < paymentStack.getCount(); i++) {
            GlStateManager.pushMatrix();
            Tessellator.getInstance().draw();
            com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
            GL11.glEnable(2896);
            GL11.glRotated(bone.getRotationX() + 2.5, 0.0, 0.0, 1.0);
            GL11.glRotated(bone.getRotationY(), 0.0, 1.0, 0.0);
            GL11.glRotated(bone.getRotationZ(), 1.0, 0.0, 0.0);
            switch (i) {
               case 1:
                  GL11.glRotated(-15.0, 0.0, 0.0, 1.0);
                  GlStateManager.translate(0.0, 0.0, -0.025);
                  break;
               case 2:
                  GL11.glRotated(15.0, 0.0, 0.0, 1.0);
                  GlStateManager.translate(0.0, 0.0, 0.025);
            }

            GlStateManager.scale(this.renderEntity.scaleFactor, this.renderEntity.scaleFactor, this.renderEntity.scaleFactor);
            itemRenderer.renderItem(this.renderEntity, new ItemStack(paymentStack.getItem(), 1), TransformType.THIRD_PERSON_RIGHT_HAND);
            this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            GL11.glDisable(2896);
            GlStateManager.popMatrix();
         }
      }
   }

   protected ItemStack resolveHeldItemStack(@Nullable ItemStack stack) {
      return stack;
   }

   /**
    * Renders the held weapon/bow on the {@code weapon} bone for NPC girls:
    * weapon (attack mode 1) or bow (mode 2); a drawn bow gets an increasing
    * pull progress (item-use count) and bow-hold rotation, attacks get stab
    * (thrust offset + rotation) or slash rotations from the entity's swing
    * state. Resets the buffer + texture binding afterwards.
    */
   protected void renderHeldItem(BufferBuilder buffer, GeoBone bone) {
      if (this.renderEntity != null) {
         if (this.renderEntity instanceof AbstractGirlNpcEntity) {
            EntityDataManager dataManager = this.renderEntity.getDataManager();
            AbstractGirlNpcEntity girl = (AbstractGirlNpcEntity)this.renderEntity;
            int attackMode = (Integer)dataManager.get(AbstractGirlNpcEntity.ATTACK_MODE);
            if (girl.getCurrentAction() != Action.BOW) {
               this.bowPullProgress = 0.0F;
            }

            ItemStack heldStack = null;
            if (attackMode == 1) {
               heldStack = (ItemStack)dataManager.get(AbstractGirlNpcEntity.WEAPON);
            } else if (attackMode == 2) {
               heldStack = (ItemStack)dataManager.get(AbstractGirlNpcEntity.BOW);
            }

            heldStack = this.resolveHeldItemStack(heldStack);
            if (heldStack != null) {
               if (heldStack.getItem().equals(Items.BOW) && girl.getCurrentAction() == Action.BOW) {
                  this.bowPullProgress += 0.015F;
                  girl.setItemUseCount(Math.round(-this.bowPullProgress * 20.0F + heldStack.getMaxItemUseDuration()));
                  girl.setHeldItemOverride(heldStack);
               }

               GlStateManager.pushMatrix();
               Tessellator.getInstance().draw();
               com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(MATRIX_STACK, bone);
               GL11.glEnable(2896);
               if (heldStack.getItem() instanceof ItemBow) {
                  GL11.glRotatef(girl.holdBowRot, 1.0F, 0.0F, 0.0F);
               } else if (girl.getCurrentAction() == Action.ATTACK && girl.nextAttack == 0) {
                  GlStateManager.translate(girl.swordOffsetStab.x, girl.swordOffsetStab.y, girl.swordOffsetStab.z);
                  GL11.glRotatef(girl.stabSwordRot, 1.0F, 0.0F, 0.0F);
               } else {
                  GL11.glRotatef(girl.slashSwordRot, 1.0F, 0.0F, 0.0F);
               }

               Minecraft.getMinecraft().getItemRenderer().renderItem(this.renderEntity, heldStack, TransformType.THIRD_PERSON_RIGHT_HAND);
               this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
               buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               GL11.glDisable(2896);
               GlStateManager.popMatrix();
            }
         }
      }
   }

   /**
    * Block ray-trace (voxel DDA, mirrors vanilla semantics) with NaN guards —
    * used for the occlusion check. Returns the first solid block hit or
    * {@code null} (NaN inputs or no hit within 200 steps).
    */
   RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, World world) {
      if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
         return null;
      }

      if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {
         int endX = MathHelper.floor(end.x);
         int endY = MathHelper.floor(end.y);
         int endZ = MathHelper.floor(end.z);
         int currentX = MathHelper.floor(start.x);
         int currentY = MathHelper.floor(start.y);
         int currentZ = MathHelper.floor(start.z);
         BlockPos currentPos = new BlockPos(currentX, currentY, currentZ);
         IBlockState blockState = world.getBlockState(currentPos);
         if (blockState.getCollisionBoundingBox(world, currentPos) != Block.NULL_AABB && blockState.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
            return blockState.collisionRayTrace(world, currentPos, start, end);
         }

         int steps = 200;

         while (steps-- >= 0) {
            if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
               return null;
            }

            if (currentX == endX && currentY == endY && currentZ == endZ) {
               return null;
            }

            boolean stepX = true;
            boolean stepY = true;
            boolean stepZ = true;
            double nextX = 999.0;
            double nextY = 999.0;
            double nextZ = 999.0;
            if (endX > currentX) {
               nextX = currentX + 1.0;
            } else if (endX < currentX) {
               nextX = currentX + 0.0;
            } else {
               stepX = false;
            }

            if (endY > currentY) {
               nextY = currentY + 1.0;
            } else if (endY < currentY) {
               nextY = currentY + 0.0;
            } else {
               stepY = false;
            }

            if (endZ > currentZ) {
               nextZ = currentZ + 1.0;
            } else if (endZ < currentZ) {
               nextZ = currentZ + 0.0;
            } else {
               stepZ = false;
            }

            double tMaxX = 999.0;
            double tMaxY = 999.0;
            double tMaxZ = 999.0;
            double deltaX = end.x - start.x;
            double deltaY = end.y - start.y;
            double deltaZ = end.z - start.z;
            if (stepX) {
               tMaxX = (nextX - start.x) / deltaX;
            }

            if (stepY) {
               tMaxY = (nextY - start.y) / deltaY;
            }

            if (stepZ) {
               tMaxZ = (nextZ - start.z) / deltaZ;
            }

            if (tMaxX == -0.0) {
               tMaxX = -1.0E-4;
            }

            if (tMaxY == -0.0) {
               tMaxY = -1.0E-4;
            }

            if (tMaxZ == -0.0) {
               tMaxZ = -1.0E-4;
            }

            EnumFacing facing;
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
               facing = endX > currentX ? EnumFacing.WEST : EnumFacing.EAST;
               start = new Vec3d(nextX, start.y + deltaY * tMaxX, start.z + deltaZ * tMaxX);
            } else if (tMaxY < tMaxZ) {
               facing = endY > currentY ? EnumFacing.DOWN : EnumFacing.UP;
               start = new Vec3d(start.x + deltaX * tMaxY, nextY, start.z + deltaZ * tMaxY);
            } else {
               facing = endZ > currentZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
               start = new Vec3d(start.x + deltaX * tMaxZ, start.y + deltaY * tMaxZ, nextZ);
            }

            currentX = MathHelper.floor(start.x) - (facing == EnumFacing.EAST ? 1 : 0);
            currentY = MathHelper.floor(start.y) - (facing == EnumFacing.UP ? 1 : 0);
            currentZ = MathHelper.floor(start.z) - (facing == EnumFacing.SOUTH ? 1 : 0);
            currentPos = new BlockPos(currentX, currentY, currentZ);
            IBlockState hitState = world.getBlockState(currentPos);
            if ((hitState.getMaterial() == Material.PORTAL || hitState.getCollisionBoundingBox(world, currentPos) != Block.NULL_AABB)
               && hitState.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
               return hitState.collisionRayTrace(world, currentPos, start, end);
            }
         }

         return null;
      } else {
         return null;
      }
   }

}
