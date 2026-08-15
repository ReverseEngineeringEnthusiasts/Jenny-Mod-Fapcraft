package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.client.model.ManglelieNpcModel;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.HashSet;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

/**
 * Renderer for Manglelie (the corrupted girl). Beyond the standard
 * {@link GirlRenderer} pipeline it adds: a first-person "POV" view of the wing
 * mesh while Manglelie looks at Galath ({@link #renderMangleliePov}), a
 * hand-rendered corruption skirt/ribbon mesh built from cached skirt bone
 * offsets, held-bow rendering on the weapon/offhand bones with corruption
 * progress, and pose coupling to her Galath partner (aim yaw, ride/head
 * actions, threesome mode).
 * <p>
 * <b>Rendering gates.</b> {@link #doRenderManglelie} skips the standard render
 * while Manglelie is being looked at, riding mommy, or corrupting — those
 * poses are drawn by the POV/interaction paths instead. The shadow pass is
 * also skipped while she looks at Galath or corrupts.
 * <p>
 * <b>Threesome.</b> {@link #renderModelBuffer} switches to a two-bone pass
 * ({@code body2} + {@code steve}) instead of the normal recursion.
 * <p>
 * CLIENT-side render thread only.
 */
public class ManglelieRenderer extends GirlRenderer<ManglelieEntity> {
   static final UnknownScreen CORRUPTION_COLOR_MAIN = new UnknownScreen(115, 108, 188, 255);
   static final Vector3fSexmodSpecial OFFSET_BODY = new Vector3fSexmodSpecial(0.05F, 0.04F, 0.0F);
   static final Vector3fSexmodSpecial OFFSET_ARM = new Vector3fSexmodSpecial(0.0F, 0.065F, 0.0F);
   static final Vector3fSexmodSpecial OFFSET_LEG = new Vector3fSexmodSpecial(0.0F, 0.03F, 0.03F);
   static final UnknownScreen CORRUPTION_COLOR_DARK = new UnknownScreen(63, 59, 150, 255);
   static final UnknownScreen CORRUPTION_COLOR_LIGHT = new UnknownScreen(79, 74, 188, 255);
   public static final HashSet<String> BLACKLISTED_BONES = new HashSet<String>() {
      {
         this.add("boobs2");
         this.add("booty2");
         this.add("vagina2");
         this.add("fuckhole2");
      }
   };
   boolean initialized = false;

   public ManglelieRenderer(RenderManager renderManager, AnimatedGeoModel model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }

   /**
    * Merges the mod's custom-part bones into the static blacklist on first
    * use (single-init guard), so the corruption bones + custom parts are never
    * drawn twice.
    */
   @Override
   public HashSet<String> getBlacklistedBones() {
      if (!this.initialized) {
         BLACKLISTED_BONES.addAll(BodyParts.CUSTOM_PART_BONES);
         this.initialized = true;
      }

      return BLACKLISTED_BONES;
   }

   /**
    * Gate for the standard render: skipped while Manglelie is being looked at
    * by Galath, ready to ride mommy's head, being looked at (distance pose),
    * or already riding mommy — those cases render through the POV/interaction
    * paths (see class javadoc).
    */
   public void doRenderManglelie(ManglelieEntity manglelie, double x, double y, double z, float entityYaw, float partialTicks) {
      if (!this.isManglelieLooking(manglelie)) {
         if (!this.canRideMommy(manglelie)) {
            if (!isManglelieLooking(manglelie, 0.5F)) {
               if (!this.isRidingMommy(manglelie)) {
                  super.doRenderEntity(manglelie, x, y, z, entityYaw, partialTicks);
                  renderMangleliePov(manglelie, partialTicks);
               }
            }
         }
      }
   }

   boolean isRidingMommy(ManglelieEntity manglelie) {
      GalathEntity galath = manglelie.getGalathPartner(false);
      if (galath == null) {
         return false;
      }

      switch (galath.getCurrentAction()) {
         case CONTROLLED_FLIGHT:
         case BOOST:
            return true;
         default:
            return false;
      }
   }

   boolean canRideMommy(ManglelieEntity manglelie) {
      return manglelie.getCurrentAction() != Action.RIDE_MOMMY_HEAD ? false : manglelie.getGalathPartner(false) == null;
   }

   boolean isManglelieLooking(ManglelieEntity manglelie) {
      GalathEntity galath = manglelie.getGalathPartner(false);
      if (galath == null) {
         return false;
      } else if (galath.isDead) {
         manglelie.setGalathPartnerUUID(null);
         return false;
      } else {
         return galath.isHuggingManglelie();
      }
   }

   /**
    * Skips the shadow/fire pass while Manglelie looks at Galath or corrupts
    * (the POV render draws her differently).
    */
   public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (!(entity instanceof ManglelieEntity)) {
         super.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
      } else {
         ManglelieEntity manglelie = (ManglelieEntity)entity;
         if (!this.isManglelieLooking(manglelie)) {
            if (!manglelie.isCorrupting()) {
               super.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
            }
         }
      }
   }

   /**
    * Whether the girl is close enough to her Galath partner for the
    * look/interact pose (partner's proximity value below {@code threshold}).
    */
   static boolean isManglelieLooking(BaseGirlEntity girl, float threshold) {
      if (!(girl instanceof ManglelieEntity)) {
         return false;
      }

      GalathEntity galath = ((ManglelieEntity)girl).getGalathPartner(false);
      return galath == null ? false : galath.bm < threshold;
   }

   /**
    * First-person POV pass: draws the Galath geometry and the corruption
    * ribbon at the girl's position (or a small offset for locally registered
    * previews), then the wing mesh, with culling/lighting disabled and
    * re-enabled around the pass. Only when the girl is NOT in the close-look
    * pose and the local player exists.
    */
   public static void renderMangleliePov(BaseGirlEntity girl, float partialTicks) {
      EntityPlayerSP player = mc.player;
      if (player != null) {
         if (!isManglelieLooking(girl, 0.5F)) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            GlStateManager.pushMatrix();
            if (girl.isLocallyRegistered()) {
               GlStateManager.translate(0.0, 0.01, 0.0);
            } else {
               GalathGeometryRender.renderGalathGeometry(mc, girl, partialTicks);
               renderManglelieRibbon(girl, partialTicks);
            }

            mc.getTextureManager().bindTexture(LINE_TEXTURE);
            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GalathRenderer.renderWingMesh(girl, buffer, tessellator);
            GlStateManager.popMatrix();
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
         }
      }
   }

   /**
    * Counter-rotates the corruption ribbon by Manglelie's interpolated yaw so
    * it stays screen-aligned while she corrupts (skipped in threesome mode).
    */
   static void renderManglelieRibbon(BaseGirlEntity girl, float partialTicks) {
      if (girl instanceof ManglelieEntity) {
         ManglelieEntity manglelie = (ManglelieEntity)girl;
         if (manglelie.isCorrupting()) {
            if (!ManglelieNpcModel.isInThreesome(manglelie)) {
               GalathEntity galath = manglelie.getGalathPartner(false);
               if (galath != null) {
                  GlStateManager.rotate(-RotationHelper.lerpFloat(girl.prevRenderYawOffset, girl.renderYawOffset, partialTicks), 0.0F, 1.0F, 0.0F);
               }
            }
         }
      }
   }

   /**
    * True while the girl (Galath or her Manglelie partner) is NOT in a
    * threesome action — i.e. the look/interact poses are active.
    */
   public static boolean isGalathLooking(BaseGirlEntity girl) {
      if (girl instanceof GalathEntity) {
         girl = ((GalathEntity)girl).getMangleliePartner(false);
      }

      return girl == null ? false : !Action.isAnyAction(girl, Action.THREESOME_SLOW, Action.THREESOME_FAST, Action.THREESOME_CUM);
   }

   /**
    * Builds the 40-strip skirt mesh (dark/light alternating colors) from the
    * girl's cached {@code skirt_<i>_<j>} bone offsets; only drawn while the
    * look pose is active.
    */
   static void renderManglelieMesh(BaseGirlEntity girl, BufferBuilder buffer, Tessellator tessellator) {
      if (isGalathLooking(girl)) {
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

         for (int i = 0; i < 39; i++) {
            renderManglelieStrip(girl, buffer, i, i + 1);
         }

         renderManglelieStrip(girl, buffer, 39, 0);
         tessellator.draw();
      }
   }

   /**
    * Emits one 8-vertex double quad (outer + inner skirt face) between the two
    * strip indices, tinted with the corruption color for the strip parity.
    */
   static void renderManglelieStrip(BaseGirlEntity girl, BufferBuilder buffer, int startStrip, int endStrip) {
      Vec3d startOffset0 = girl.getCachedBoneOffset("skirt_" + startStrip + "_0");
      Vec3d startOffset1 = girl.getCachedBoneOffset("skirt_" + startStrip + "_1");
      Vec3d startOffset2 = girl.getCachedBoneOffset("skirt_" + startStrip + "_2");
      Vec3d endOffset0 = girl.getCachedBoneOffset("skirt_" + endStrip + "_0");
      Vec3d endOffset1 = girl.getCachedBoneOffset("skirt_" + endStrip + "_1");
      Vec3d endOffset2 = girl.getCachedBoneOffset("skirt_" + endStrip + "_2");
      UnknownScreen stripColor = startStrip % 2 == 0 ? CORRUPTION_COLOR_LIGHT : CORRUPTION_COLOR_DARK;
      buffer.pos(startOffset0.x, startOffset0.y, startOffset0.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(startOffset1.x, startOffset1.y, startOffset1.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(endOffset1.x, endOffset1.y, endOffset1.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(endOffset0.x, endOffset0.y, endOffset0.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(startOffset1.x, startOffset1.y, startOffset1.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(endOffset1.x, endOffset1.y, endOffset1.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(endOffset2.x, endOffset2.y, endOffset2.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
      buffer.pos(startOffset2.x, startOffset2.y, startOffset2.z).color(stripColor.red, stripColor.green, stripColor.blue, stripColor.alpha).endVertex();
   }

   /**
    * Per-bone processing: applies the skirt-follow transform (see
    * {@link #applyBoneTransform}) and, while the girl looks at her corrupt
    * entity, swaps the held-bow rendering between the weapon bone (looking)
    * and the offhand bone (not looking).
    */
   @Override
   protected void onBoneProcessing(BufferBuilder buffer, String boneName, GeoBone bone) {
      applyBoneTransform(this.renderEntity, boneName, bone, false);
      Entity corruptEntity = this.renderEntity.getCorruptEntity();
      if (corruptEntity != null) {
         if ("weapon".equals(boneName) && this.renderEntity.isLookingAtGalathEntity(corruptEntity, mc.getRenderPartialTicks())) {
            this.renderEquippedItem(buffer, bone, true);
         }

         if ("offhand".equals(boneName) && !this.renderEntity.isLookingAtGalathEntity(corruptEntity, mc.getRenderPartialTicks())) {
            this.renderEquippedItem(buffer, bone, false);
         }
      }
   }

   /**
    * Renders the corruption bow at the given bone: applies the bone transform
    * (flushing pending vertices first), poses the bow, and drives the entity's
    * hand-active state from the corruption progress — charging the bow (11
    * item-use ticks, ease-in-out) while corrupting below 1.0, otherwise
    * clearing the hand. Re-binds the entity texture and restarts the buffer
    * afterwards.
    */
   public void renderEquippedItem(BufferBuilder buffer, GeoBone bone, boolean isOffhand) {
      ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
      GL11.glEnable(2896);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      if (isOffhand) {
         GlStateManager.translate(-0.01, 0.0, 0.0);
         GlStateManager.rotate(120.0F, 1.0F, 0.0F, 0.0F);
      } else {
         GlStateManager.translate(0.15, 0.0, -0.05);
         GlStateManager.rotate(-140.0F, 1.0F, 0.0F, 0.0F);
      }

      GlStateManager.scale(0.7, 0.7, 0.7);
      ItemStack bowStack = new ItemStack(Items.BOW);
      float corruptProgress = this.renderEntity.getCorruptProgress(mc.getRenderPartialTicks());
      if (corruptProgress < 1.0F) {
         float easedProgress = (float)RotationHelper.easeInOutQuad(corruptProgress);
         this.renderEntity.setItemUseCount((int)(11.0F * (1.0F - easedProgress) + 71980.0F));
         this.renderEntity.setHeldItemOverride(bowStack);
         this.renderEntity.setActiveHand(EnumHand.MAIN_HAND);
         this.renderEntity.setHandActiveState();
      } else {
         this.renderEntity.setHeldItemOverride(ItemStack.EMPTY);
         this.renderEntity.clearHandActiveState();
      }

      itemRenderer.renderItem(this.renderEntity, bowStack, TransformType.THIRD_PERSON_RIGHT_HAND);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   /**
    * Skirt-follow bone logic: skirt bones 17..35 track the cheek rotation
    * (raised positionY by the cheek's rotation), skirt bones 1..11 (suffix
    * "1") copy the leg rotationX into rotationX/positionY — so the skirt
    * segments follow Manglelie's legs/cheeks while animating. No-op when the
    * game is paused or the driver bone's rotation is negative.
    */
   public static void applyBoneTransform(BaseGirlEntity girl, String boneName, GeoBone bone, boolean isSecondary) {
      if (boneName.contains("skirt_")) {
         int boneIndex = parseBoneIndex(boneName);
         if (ThreadNames.isBetween(boneIndex, 17.0, 35.0)) {
            if (mc.isGamePaused()) {
               return;
            }

            String driverBone = boneIndex < 26 ? "cheekL" : "cheekR";
            if (isSecondary) {
               driverBone = driverBone + "2";
            }

            float driverRotation = TrigMath.toDegrees(girl.getAnimationProcessor().getBone(driverBone).getRotationX());
            if (driverRotation < 0.0F) {
               return;
            }

            bone.setPositionY(bone.getPositionY() + driverRotation * 0.01F);
         }

         if (ThreadNames.isBetween(boneIndex, 1.0, 11.0)) {
            if (!boneName.endsWith("1")) {
               return;
            }

            String legBone = boneIndex < 6 ? "legR" : "legL";
            if (isSecondary) {
               legBone = legBone + "2";
            }

            float legRotation = TrigMath.toDegrees(girl.getAnimationProcessor().getBone(legBone).getRotationX());
            if (legRotation < 0.0F) {
               return;
            }

            bone.setRotationX(TrigMath.wrapDegrees(legRotation));
            bone.setPositionY(TrigMath.wrapDegrees(legRotation * 0.03F));
         }
      }
   }

   static int parseBoneIndex(String boneName) {
      int firstUnderscore = boneName.indexOf(95);
      int secondUnderscore = boneName.indexOf(95, firstUnderscore + 1);
      if (firstUnderscore != -1 && secondUnderscore != -1) {
         String indexString = boneName.substring(firstUnderscore + 1, secondUnderscore);

         try {
            return Integer.parseInt(indexString);
         } catch (NumberFormatException e) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   /**
    * Threesome render pass: instead of the normal recursion, only the
    * {@code body2} bone is drawn normally and the {@code steve} bone is drawn
    * with the girl's render-scale factor, each in its own buffer flush.
    */
   protected void renderModelBuffer(GeoModel model, BufferBuilder buffer, ManglelieEntity manglelie, float r, float g, float b, float a, float scale) {
      if (!ManglelieNpcModel.isInThreesome(manglelie)) {
         super.renderModelBuffer(model, buffer, manglelie, r, g, b, a, scale);
      } else {
         GeoBone topBone = model.topLevelBones.get(0);
         GeoBone body2Bone = null;
         GeoBone steveBone = null;

         for (GeoBone bone : topBone.childBones) {
            switch (bone.getName()) {
               case "steve":
                  steveBone = bone;
                  break;
               case "body2":
                  body2Bone = bone;
            }
         }

         MATRIX_STACK.push();
         MATRIX_STACK.translate(topBone);
         MATRIX_STACK.moveToPivot(topBone);
         MATRIX_STACK.rotate(topBone);
         MATRIX_STACK.scale(topBone);
         MATRIX_STACK.moveBackFromPivot(topBone);
         this.renderRecursively(buffer, body2Bone, r, g, b, a);
         Tessellator.getInstance().draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.renderEntity));

         this.renderRecursively(buffer, steveBone, r, g, b, this.renderEntity.getRenderScaleFactor());
         Tessellator.getInstance().draw();
         MATRIX_STACK.pop();
      }
   }

   /**
    * Renders the corruption ribbon body mesh (three cloth-body segments
    * between the cached cloth bones) in the main corruption color.
    */
   static void renderManglelieRibbonMesh(BaseGirlEntity girl, BufferBuilder buffer, Tessellator tessellator, float partialTicks) {
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      Vec3d[][] leftMesh = GalathGeometryRender.buildBodyMesh(girl, partialTicks, "clothBoobLconStart", "clothBoobLconEnd", OFFSET_BODY, OFFSET_ARM);
      Vec3d[][] rightMesh = GalathGeometryRender.buildBodyMesh(girl, partialTicks, "clothBoobRconStart", "clothBoobRconEnd", OFFSET_BODY, OFFSET_ARM);
      Vec3d[][] midMesh = GalathGeometryRender.buildBodyMesh(girl, partialTicks, "clothBoobMidconStart", "clothBoobMidconEnd", OFFSET_LEG, OFFSET_LEG);
      GalathGeometryRender.renderMesh(buffer, leftMesh, CORRUPTION_COLOR_MAIN);
      GalathGeometryRender.renderMesh(buffer, rightMesh, CORRUPTION_COLOR_MAIN);
      GalathGeometryRender.renderMesh(buffer, midMesh, CORRUPTION_COLOR_MAIN);
      tessellator.draw();
   }

   /**
    * Parent-chain rule for the corruption cloth bones: bones under
    * {@code clothBoob*} are always renderable (they are part of the custom
    * mesh), everything else follows {@link IGirlRenderer#hasParentBone}.
    */
   @Override
   public boolean hasParentBone(HashSet blacklistedBones, GeoBone bone) {
      while (bone.parent != null) {
         String boneName = bone.getName();
         if (boneName.contains("clothBoob")) {
            return true;
         }

         if (blacklistedBones.contains(boneName)) {
            return false;
         }

         if (boneName.startsWith("armor")) {
            return false;
         }

         bone = bone.parent;
      }

      return true;
   }

   /**
    * Render-position override: while running, pins the girl's yaw to her
    * movement yaw; while corrupting (not in threesome), aims her at the Galath
    * partner and returns the partner-relative look position (the corruption
    * pose renders at Galath's location).
    */
   protected Vec3d getBoneWorldPosManglelie(ManglelieEntity manglelie, float partialTicks, Vec3d pos) {
      if (manglelie.getCurrentAction() == Action.RUN) {
         float yaw = manglelie.getYawRotation();
         manglelie.rotationYaw = yaw;
         manglelie.prevRenderYawOffset = yaw;
         manglelie.renderYawOffset = yaw;
         manglelie.prevRotationYawHead = yaw;
         manglelie.rotationYawHead = yaw;
         return pos;
      }

      if (isCorrupting(manglelie)) {
         GalathEntity galath = manglelie.getGalathPartner(false);
         if (galath != null) {
            renderGalathInteract(galath, partialTicks, manglelie);
            return getLookVector(galath, partialTicks);
         }
      }

      return pos;
   }

   /**
    * Points the girl's head/body yaw at Galath (anchored: aim yaw; else the
    * head yaw + aim), so the corruption look-pose faces her correctly.
    */
   public static void renderGalathInteract(GalathEntity galath, float partialTicks, EntityLivingBase target) {
      if (galath.isAnchored()) {
         float yaw = galath.getYawRotation();
         float prevYaw = galath.getYawRotation();
         Float aimYaw2 = GalathEntity.getAimYaw(galath, partialTicks);
         if (aimYaw2 != null) {
            yaw = aimYaw2;
            prevYaw = aimYaw2;
         }

         target.rotationYaw = yaw;
         target.prevRenderYawOffset = prevYaw;
         target.renderYawOffset = yaw;
         target.prevRotationYawHead = prevYaw;
         target.rotationYawHead = yaw;
      } else {
         float headYaw = galath.rotationYawHead;
         float prevHeadYaw = galath.prevRotationYawHead;
         Float aimYaw = GalathEntity.getAimYaw(galath, partialTicks);
         if (aimYaw != null) {
            headYaw = aimYaw;
            prevHeadYaw = aimYaw;
         }

         target.rotationYaw = headYaw;
         target.prevRenderYawOffset = prevHeadYaw;
         target.renderYawOffset = headYaw;
         target.prevRotationYawHead = prevHeadYaw;
         target.rotationYawHead = headYaw;
      }
   }

   /**
    * Whether Manglelie is in her corruption state and NOT in a threesome (the
    * only state where the corruption render paths apply).
    */
   public static boolean isCorrupting(ManglelieEntity manglelie) {
      return manglelie.isCorrupting() && !ManglelieNpcModel.isInThreesome(manglelie);
   }

   public static Vec3d getLookVector(GalathEntity galath, float partialTicks) {
      return EntityLookVectorHelper.getAimVector(galath, mc.player, partialTicks).add(galath.getCachedBoneOffset("mangPos"));
   }

   public static Vec3d getEntityLookVector(GalathEntity galath, float partialTicks) {
      return EntityLookVectorHelper.getEntityLookVector(galath, partialTicks).add(galath.getCachedBoneOffset("mangPos"));
   }

}
