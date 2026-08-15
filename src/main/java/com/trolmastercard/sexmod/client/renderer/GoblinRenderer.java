package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.EyeColor;
import com.trolmastercard.sexmod.util.HairColor;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the goblin (and goblin-player) entity. The goblin has the most
 * complex pose logic of all girls: it can ride the owner's shoulder
 * (first-person camera bob), be picked up, be thrown at players, or be caught;
 * in every state the render position/yaw is computed from the owner player's
 * view and the goblin is drawn in the correct perspective.
 * <p>
 * <b>First-person sentinel.</b> The yaw sentinel {@value #SENTINEL_VALUE}
 * (-420.69F) marks renders issued for the first-person camera
 * ({@link #renderEntityInFirstPerson}): pose selection keys on it
 * (shoulder-idle/pick-up detection in {@link #doRenderGoblin}) and it skips
 * the shadow/fire pass. Do not change this value.
 * <p>
 * <b>Customization.</b> The goblin's model is assembled from the owner's
 * model-code parts: ear/hair/body bones are swapped or hidden per part index
 * ({@link #onBoneProcessing}), bone colors come from the model code (eye,
 * skin, hair, crown), and the held item switches to the goblin's inventory
 * item while running/catching ({@link #resolveHeldItemStack}).
 * <p>
 * <b>Pitfall:</b> every pose branch skips rendering when the goblin is the
 * local player's own and would be drawn in first person — the pose is drawn
 * by the first-person camera path instead. Position interpolation uses
 * {@link RotationHelper#lerpVec3dDouble} (PROGRESS lerp — correct here).
 */
public class GoblinRenderer extends GirlRendererBase<GoblinEntity> {
   public static final Vec3i DEFAULT_BONE_COLOR = new Vec3i(255, 255, 255);
   static final float SENTINEL_VALUE = -420.69F;
   static final float RENDER_SCALE_A = 8.0F;
   static final float RENDER_SCALE_B = 3.0F;
   public static final Vec3d MOVEMENT_DIR_VECTOR = new Vec3d(10.0, -20.0, -10.0);
   static final float LERP_FACTOR = 0.1F;
   public static final HashSet<String> NUDE_BONE_NAMES = new HashSet<>(
      Arrays.asList(
         "meatTorso",
         "meatCheekR",
         "meatCheekL",
         "meatFootR",
         "meatFootL",
         "meatShinR",
         "meatShinL",
         "meatLegL",
         "meatLegR",
         "nippleR",
         "nippleL",
         "preggy",
         "shoeL",
         "shoeR",
         "frontAndInside",
         "Lside",
         "Rside",
         "cheekR",
         "cheekL",
         "fuckhole",
         "head",
         "nose",
         "neck",
         "armL",
         "lowerArmL",
         "armR",
         "lowerArmR",
         "torso",
         "LegL",
         "LegR",
         "shinL",
         "shinR"
      )
   );
   public static final HashSet<String> LASH_BONE_NAMES = new HashSet<>(Arrays.asList("lashR", "lashL", "closedR", "closedL", "browL", "browR", "closedL", "closedL"));
   static final HashSet<String> LEG_BONE_NAMES = new HashSet<>(Arrays.asList("meatLegR", "meatShinR", "meatFootR", "boobR", "boobR1", "boobR2"));
   static Minecraft mc;
   float currentYawOffset = 0.0F;
   boolean isShoulderIdle = false;
   boolean isBeingPickedUp = false;
   public static float currentActionValue = 0.0F;
   float lightLevel = 0.0F;
   public static float lastPlayerYaw = 0.0F;
   public static float lastPlayerPitch = 0.0F;
   public static float prevStrafeRotation = 0.0F;
   public static float prevForwardRotation = 0.0F;
   public static float strafeRotation = 0.0F;
   public static float forwardRotation = 0.0F;

   public GoblinRenderer(RenderManager renderManager, AnimatedGeoModel model, double shadowSize) {
      super(renderManager, model, shadowSize);
      mc = Minecraft.getMinecraft();
   }

   /**
    * Resolves the goblin's skin texture: the owner's cached skin (interaction
    * player, else owner) from the static cache map, or a freshly tinted skin
    * when not cached. In the preload world ({@link SexWorldClient}) or with no
    * owner it uses the local player's profile id.
    */
   protected ResourceLocation getGoblinTexture(GoblinEntity goblin) {
      UUID uuid = goblin.getInteractionPlayerUUID();
      if (uuid == null) {
         uuid = goblin.getOwnerUUID();
      }

      ResourceLocation cachedTexture;
      if (!(goblin.world instanceof SexWorldClient) && uuid != null) {
         cachedTexture = l.get(uuid);
         if (cachedTexture == null) {
            return this.getTintedSkinTexture(uuid, goblin.world);
         }
      } else {
         cachedTexture = l.get(mc.getSession().getProfile().getId());
         if (cachedTexture == null) {
            return this.getTintedSkinTexture(mc.getSession().getProfile().getId(), goblin.world);
         }
      }

      return cachedTexture;
   }

   /**
    * Renders the goblin at the origin with the first-person sentinel yaw, so
    * {@link #doRenderGoblin} treats it as a first-person pose (shoulder-idle /
    * pick-up) and applies the camera-relative transform. CLIENT render thread.
    */
   public static void renderEntityInFirstPerson(BaseGirlEntity girl, float partialTicks) {
      mc.getRenderManager().renderEntity(girl, 0.0, 0.0, 0.0, -420.69F, partialTicks, false);
   }

   /**
    * Applies the first-person camera bob (vanilla walk-bob formula) so the
    * goblin on the shoulder sways with the player's steps. CLIENT render
    * thread; must run before drawing the goblin quad.
    */
   public static void setFirstPersonCamera(float partialTicks) {
      if (mc.getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
         float walkDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
         float walkedDistance = -(player.distanceWalkedModified + walkDelta * partialTicks);
         float cameraYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
         float bobOffset = MathHelper.sin(walkedDistance * (float) Math.PI) * cameraYaw * 0.5F;
         GlStateManager.translate(
            Math.cos(mc.player.rotationYaw * (Math.PI / 180.0)) * bobOffset,
            Math.abs(MathHelper.cos(walkedDistance * (float) Math.PI) * cameraYaw),
            Math.sin(mc.player.rotationYaw * (Math.PI / 180.0)) * bobOffset
         );
      }
   }

   public void renderModel(GeoModel model, GoblinEntity goblin, float partialTicks, float r, float g, float b, float a) {
      super.renderModel(model, goblin, partialTicks, r, g, b, goblin.ar);
   }

   /**
    * Skips the shadow/fire pass while the goblin is being picked up or riding
    * the shoulder (it would draw a shadow inside the player's view).
    */
   public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (!(entity instanceof GoblinEntity)) {
         super.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
      } else {
         GoblinEntity goblin = (GoblinEntity)entity;
         if (goblin.getCurrentAction() != Action.PICK_UP && goblin.getCurrentAction() != Action.SHOULDER_IDLE) {
            super.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
         }
      }
   }

   /**
    * Converts the girl's world position into a player-relative throw position:
    * the owner's head yaw is copied onto the girl (aiming her at the throw
    * direction), the action is set to START_THROWING, and the result is the
    * owner's position minus the local player's. Returns the input unchanged
    * when any of world/owner/girl is missing.
    */
   public static Vec3d getThrowPosition(World world, BaseGirlEntity girl, UUID ownerUuid, double x, double y, double z) {
      if (world == null) {
         return new Vec3d(x, y, z);
      }

      if (ownerUuid == null) {
         return new Vec3d(x, y, z);
      }

      if (girl == null) {
         return new Vec3d(x, y, z);
      }

      EntityPlayer owner = world.getPlayerEntityByUUID(ownerUuid);
      if (owner == null) {
         return new Vec3d(x, y, z);
      }

      Vec3d ownerPos = owner.getPositionVector();
      Vec3d localPlayerPos = mc.player.getPositionVector();
      girl.prevRenderYawOffset = owner.prevRotationYawHead;
      girl.renderYawOffset = owner.rotationYawHead;
      girl.setCurrentAction(Action.START_THROWING);
      return ownerPos.subtract(localPlayerPos);
   }

   /**
    * Main pose dispatcher (CLIENT render thread): detects first-person
    * shoulder-idle/pick-up via the sentinel yaw, then routes by action —
    * thrown/starting (render at the owner's throw aim, skip own first-person
    * views), shoulder-idle (bob + FOV-relative offset, hide for the owner in
    * first person), pick-up (track the owner's head), otherwise default
    * render. Two symmetric branches cover owned vs unowned goblins — keep them
    * in sync.
    */
   public void doRenderGoblin(GoblinEntity goblin, double x, double y, double z, float entityYaw, float partialTicks) {
      this.renderEntity = goblin;
      this.isShoulderIdle = -420.69F == entityYaw && goblin.getCurrentAction() == Action.SHOULDER_IDLE;
      this.isBeingPickedUp = -420.69F == entityYaw && goblin.getCurrentAction() == Action.PICK_UP;
      this.lightLevel = goblin.world.getLight(goblin.getPosition(), true);
      this.currentYawOffset = partialTicks;
      currentActionValue = entityYaw;
      Action action = goblin.getCurrentAction();
      UUID ownerUuid = goblin.getOwnerUUID();
      if (ownerUuid != null) {
         if (goblin.isLocallyRegistered()) {
            Vec3d throwPos2 = getThrowPosition(goblin.world, goblin, ownerUuid, x, y, z);
            x = throwPos2.x;
            y = throwPos2.y;
            z = throwPos2.z;
         }

         if (action == Action.THROWN || action == Action.START_THROWING) {
            if (mc.gameSettings.thirdPersonView == 0 && entityYaw == -420.69F && !goblin.isLocallyRegistered()) {
               return;
            }

            if (!goblin.isLocallyRegistered()) {
               float yaw2 = goblin.getYawRotation();
               goblin.prevRenderYawOffset = yaw2;
               goblin.renderYawOffset = yaw2;
            }
         }

         if (isThrowAction(goblin, action)) {
            if (mc.player.getPersistentID().equals(ownerUuid)) {
               if (-420.69F != entityYaw) {
                  return;
               }

               goblin.renderYawOffset = mc.player.rotationYaw + 180.0F;
               goblin.prevRenderYawOffset = mc.player.rotationYaw + 180.0F;
               Vec3d lookVec2 = mc.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(lookVec2.x, lookVec2.y + mc.player.getEyeHeight(), lookVec2.z);
               Vec3d pitchVec2 = GoblinEntity.rotateVectorYaw(new Vec3d(-Math.abs(mc.player.rotationPitch), 0.0, 0.0), mc.player.rotationYaw);
               GlStateManager.rotate(mc.player.rotationPitch, (float)pitchVec2.x, 0.0F, (float)pitchVec2.z);
               x = 0.0;
               y = 0.0;
               z = 0.0;
            } else {
               if (!goblin.isLocallyRegistered() || mc.player.getPersistentID().equals(ownerUuid)) {
                  if (!mc.player.getPersistentID().equals(ownerUuid)) {
                     EntityPlayer owner = goblin.world.getPlayerEntityByUUID(ownerUuid);
                     if (owner != null) {
                        goblin.renderYawOffset = owner.rotationYaw;
                        goblin.prevRenderYawOffset = owner.rotationYaw;
                     }
                  } else {
                     goblin.renderYawOffset = mc.player.rotationYaw;
                     goblin.prevRenderYawOffset = mc.player.rotationYaw;
                  }
               }

               Vec3d throwAim2 = getThrowAim(goblin, goblin.getOwnerUUID(), partialTicks);
               x = throwAim2.x;
               y = throwAim2.y;
               z = throwAim2.z;
            }
         } else if (this.isShoulderIdle) {
            setFirstPersonCamera(partialTicks);
            Vec3d shoulderOffset2 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, mc.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            shoulderOffset2 = GoblinEntity.rotateVectorYaw(shoulderOffset2, mc.player.rotationYaw);
            x = shoulderOffset2.x;
            y = shoulderOffset2.y;
            z = shoulderOffset2.z;
            goblin.renderYawOffset = mc.player.rotationYaw;
            goblin.prevRenderYawOffset = mc.player.prevRotationYaw;
            if (mc.player.isSneaking()) {
               y -= 0.075;
            }
         } else if (action == Action.SHOULDER_IDLE) {
            if (mc.player.getPersistentID().equals(ownerUuid) && mc.gameSettings.thirdPersonView == 0) {
               return;
            }

            EntityPlayer owner2 = goblin.world.getPlayerEntityByUUID(ownerUuid);
            if (owner2 == null) {
               return;
            }

            Vector4f firstPersonView = getFirstPersonView(owner2, partialTicks);
            x = firstPersonView.x;
            y = firstPersonView.y;
            z = firstPersonView.z;
            goblin.renderYawOffset = firstPersonView.w;
            if (owner2.isSneaking()) {
               y -= 0.32;
            }
         } else if (action == Action.PICK_UP) {
            EntityPlayer pickUpOwner = goblin.world.getPlayerEntityByUUID(ownerUuid);
            if (pickUpOwner != null) {
               goblin.prevRenderYawOffset = pickUpOwner.prevRotationYawHead;
               goblin.renderYawOffset = pickUpOwner.rotationYawHead;
            }
         }

         super.doRenderEntity(goblin, x, y, z, entityYaw, partialTicks);
         if (isThrowAction(goblin, action) && mc.gameSettings.thirdPersonView == 0 && mc.player.getPersistentID().equals(ownerUuid)) {
            GlStateManager.popMatrix();
         }
      } else {
         if (goblin.isLocallyRegistered()) {
            Vec3d throwPos = getThrowPosition(goblin.world, goblin, ownerUuid, x, y, z);
            x = throwPos.x;
            y = throwPos.y;
            z = throwPos.z;
         }

         if (action == Action.THROWN || action == Action.START_THROWING) {
            if (mc.gameSettings.thirdPersonView == 0 && entityYaw == -420.69F && !goblin.isLocallyRegistered()) {
               return;
            }

            if (!goblin.isLocallyRegistered()) {
               float yaw = goblin.getYawRotation();
               goblin.prevRenderYawOffset = yaw;
               goblin.renderYawOffset = yaw;
            }
         }

         if (isThrowAction(goblin, action)) {
            if (mc.player.getPersistentID().equals(ownerUuid)) {
               if (-420.69F != entityYaw) {
                  return;
               }

               goblin.renderYawOffset = mc.player.rotationYaw + 180.0F;
               goblin.prevRenderYawOffset = mc.player.rotationYaw + 180.0F;
               Vec3d lookVec = mc.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(lookVec.x, lookVec.y + mc.player.getEyeHeight(), lookVec.z);
               Vec3d pitchVec = GoblinEntity.rotateVectorYaw(new Vec3d(-Math.abs(mc.player.rotationPitch), 0.0, 0.0), mc.player.rotationYaw);
               GlStateManager.rotate(mc.player.rotationPitch, (float)pitchVec.x, 0.0F, (float)pitchVec.z);
               x = 0.0;
               y = 0.0;
               z = 0.0;
            } else {
               if (goblin.isLocallyRegistered()) {
               }

               goblin.renderYawOffset = mc.player.rotationYaw;
               goblin.prevRenderYawOffset = mc.player.rotationYaw;
               Vec3d throwAim = getThrowAim(goblin, goblin.getOwnerUUID(), partialTicks);
               x = throwAim.x;
               y = throwAim.y;
               z = throwAim.z;
            }
         } else if (this.isShoulderIdle) {
            setFirstPersonCamera(partialTicks);
            Vec3d shoulderOffset = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, mc.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            shoulderOffset = GoblinEntity.rotateVectorYaw(shoulderOffset, mc.player.rotationYaw);
            x = shoulderOffset.x;
            y = shoulderOffset.y;
            z = shoulderOffset.z;
            goblin.renderYawOffset = mc.player.rotationYaw;
            goblin.prevRenderYawOffset = mc.player.prevRotationYaw;
            if (mc.player.isSneaking()) {
               y -= 0.075;
            }
         } else {
            if (action == Action.SHOULDER_IDLE) {
               return;
            }

            if (action == Action.PICK_UP) {
            }
         }

         super.doRenderEntity(goblin, x, y, z, entityYaw, partialTicks);
         if (isThrowAction(goblin, action) && mc.gameSettings.thirdPersonView == 0 && mc.player.getPersistentID().equals(ownerUuid)) {
            GlStateManager.popMatrix();
         }
      }
   }

   /**
    * Whether the action renders the goblin in a thrown/caught/pick-up pose.
    * Start-throwing is only a render action for locally registered goblins,
    * and throw poses are never rendered in first-person view.
    */
   public static boolean isThrowAction(BaseGirlEntity girl, Action action) {
      if (action == Action.START_THROWING && !girl.isLocallyRegistered()) {
         return false;
      }

      if (mc.gameSettings.thirdPersonView == 0 || action != Action.START_THROWING && action != Action.PICK_UP) {
         switch (action) {
            case PICK_UP:
            case CATCH:
            case CATCH_BJ:
            case CATCH_BJ_IDLE:
            case START_THROWING:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   /**
    * Lerped vector from the local player to the goblin's owner — the render
    * offset for throw/catch poses (goblin flies between the two players).
    */
   public static Vec3d getThrowAim(BaseGirlEntity girl, UUID ownerUuid, float partialTicks) {
      if (ownerUuid == null) {
         return Vec3d.ZERO;
      }

      EntityPlayer owner = girl.world.getPlayerEntityByUUID(ownerUuid);
      if (owner == null) {
         return Vec3d.ZERO;
      }

      Vec3d ownerPos = RotationHelper.lerpVec3dDouble(new Vec3d(owner.prevPosX, owner.prevPosY, owner.prevPosZ), owner.getPositionVector(), partialTicks);
      Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(
         new Vec3d(mc.player.prevPosX, mc.player.prevPosY, mc.player.prevPosZ), mc.player.getPositionVector(), partialTicks
      );
      return ownerPos.subtract(localPlayerPos);
   }

   /**
    * First-person shoulder camera: lerped position of the owner relative to
    * the local player plus the owner's interpolated render yaw (as .w).
    */
   public static Vector4f getFirstPersonView(EntityPlayer owner, float partialTicks) {
      EntityPlayerSP localPlayer = mc.player;
      float renderYaw = RotationHelper.lerp(owner.prevRenderYawOffset, owner.renderYawOffset, partialTicks);
      Vec3d ownerPos = RotationHelper.lerpVec3dDouble(new Vec3d(owner.lastTickPosX, owner.lastTickPosY, owner.lastTickPosZ), owner.getPositionVector(), partialTicks);
      Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      Vec3d offset = ownerPos.subtract(localPlayerPos);
      return new Vector4f((float)offset.x, (float)offset.y, (float)offset.z, renderYaw);
   }

   /**
    * Maps a bone name to its tint from the girl's model code (parts[0..9]):
    * eyes -> eye color, variant/boob/nude bones -> skin color, hair/lash ->
    * hair color, crown handled separately. White (no tint) for unknown bones
    * or short model codes.
    */
   @Override
   protected Vec3i getBoneColor(String boneName) {
      String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
      if (modelCodeParts.length < 8) {
         return DEFAULT_BONE_COLOR;
      } else if (boneName.contains("band")) {
         return DEFAULT_BONE_COLOR;
      } else if (boneName.contains("eyeColor") || boneName.contains("eyeColor2")) {
         return getEyeColor(modelCodeParts[8]);
      } else if (boneName.contains("variant") || boneName.contains("boob")) {
         return getSkinColor(modelCodeParts[7]);
      } else if (boneName.contains("hair")) {
         return getHairColor(modelCodeParts[6]);
      } else if (NUDE_BONE_NAMES.contains(boneName)) {
         return getSkinColor(modelCodeParts[7]);
      } else {
         return LASH_BONE_NAMES.contains(boneName) ? getHairColor(modelCodeParts[6]) : DEFAULT_BONE_COLOR;
      }
   }

   public static Vec3i getEyeColor(String code) {
      return EyeColor.values()[Integer.parseInt(code)].getColor();
   }

   public static Vec3i getSkinColor(String code) {
      return SkinColor.values()[Integer.parseInt(code)].getColor();
   }

   public static Vec3i getHairColor(String code) {
      return HairColor.values()[Integer.parseInt(code)].getColor();
   }

   /**
    * Per-bone geometry tweaks from the model code (skipped in the
    * {@link SexWorldClient} preload world): ears/hair swap to the chosen part
    * models, the body pivot drops to -0.15 (plus shoulder-idle pose), leg/boob
    * bones lean with the strafe/forward rotations, and crown bones hide
    * depending on ownership state.
    */
   @Override
   protected void onBoneProcessing(BufferBuilder buffer, String boneName, GeoBone bone) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
         if (modelCodeParts.length >= 8) {
            switch (boneName) {
               case "earL":
                  applyBoneParts(bone, modelCodeParts[0], modelCodeParts[1], modelCodeParts[3]);
                  break;
               case "earR":
                  applyBoneParts(bone, modelCodeParts[0], modelCodeParts[2], modelCodeParts[4]);
                  break;
               case "hair":
                  applyBonePart(bone, modelCodeParts[5]);
                  break;
               case "body":
                  bone.setPivotY(-0.15F);
                  applyBoneState(this.renderEntity, bone);
                  break;
               case "LegR":
                  applyBoneRot(this.isShoulderIdle, bone, 25.0F, 25.0F);
                  break;
               case "boobR":
                  applyBoneRot(this.isShoulderIdle, bone, 30.0F, 30.0F);
                  break;
               case "boobR1":
                  applyBoneRot(this.isShoulderIdle, bone, 10.0F, 15.0F);
                  break;
               case "boobR2":
                  applyBoneRot(this.isShoulderIdle, bone, 5.0F, 3.0F);
            }

            if (boneName.contains("crown")) {
               applyBoneColor(this.renderEntity, bone, modelCodeParts[9]);
            }
         }
      }
   }

   /**
    * Hides the crown bone by ownership: hidden for locally registered girls,
    * hidden when the model-code flag is 0 for NPC goblins, hidden when the
    * player-goblin wears no helmet.
    */
   public static void applyBoneColor(BaseGirlEntity girl, GeoBone bone, String crownCode) {
      if (girl.isLocallyRegistered()) {
         bone.setHidden(true);
      } else if (girl instanceof GoblinEntity) {
         int crownVariant = Integer.parseInt(crownCode);
         bone.setHidden(crownVariant == 0);
      } else if (girl instanceof GoblinPlayerEntity) {
         bone.setHidden(((ItemStack)girl.getDataManager().get(AbstractGirlNpcEntity.HELMET_SLOT)).isEmpty());
      }
   }

   /**
    * Leans the bone by the player's current strafe/forward rotation (clamped
    * to +/-the given angles) while the goblin rides the shoulder; no-op while
    * the game is paused.
    */
   public static void applyBoneRot(boolean isShoulderIdle, GeoBone bone, float maxForward, float maxStrafe) {
      if (!mc.isGamePaused()) {
         if (isShoulderIdle) {
            bone.setRotationX(bone.getRotationX() + TrigMath.wrapDegrees(ThreadNames.clampFloat(forwardRotation, -maxForward, maxForward)));
            bone.setRotationZ(bone.getRotationZ() + TrigMath.wrapDegrees(ThreadNames.clampFloat(strafeRotation, -maxStrafe, maxStrafe)));
         }
      }
   }

   /**
    * Body-bone pose for shoulder-idle: raises the pivot to 8 and tilts the
    * body with the camera pitch so the goblin "sits" in the player's view.
    */
   public static void applyBoneState(BaseGirlEntity girl, GeoBone bone) {
      if (currentActionValue == -420.69F && girl.getCurrentAction() == Action.SHOULDER_IDLE) {
         float cameraPitch = -mc.getRenderManager().playerViewX;
         bone.setPivotY(8.0F);
         if (!mc.isGamePaused()) {
            bone.setRotationX(bone.getRotationX() + TrigMath.wrapDegrees(cameraPitch));
         }
      }
   }

   public static void applyBonePart(GeoBone bone, String partCode) {
      int partIndex = Integer.parseInt(partCode);
      getChildBone(bone, partIndex);
   }

   static HashSet<Integer> buildColorIndexGroups(int groupCount, String code) {
      int groupIndex = Integer.parseInt(code);
      int lastIndex = groupCount - 1;
      ArrayList groups = buildColorIndexGroups(lastIndex);

      while (groupIndex >= groups.size()) {
         groupIndex -= groups.size();
      }

      return (HashSet<Integer>)groups.get(groupIndex);
   }

   static ArrayList<HashSet<Integer>> buildColorIndexGroups(int size) {
      ArrayList groups = new ArrayList();
      buildColorGroups(0, new HashSet<>(), size, groups);
      return groups;
   }

   static void buildColorGroups(int index, HashSet<Integer> current, int max, ArrayList<HashSet<Integer>> groups) {
      if (index > max) {
         groups.add(current);
      } else {
         HashSet next = new HashSet(current);
         buildColorGroups(index + 1, current, max, groups);
         next.add(index);
         buildColorGroups(index + 1, next, max, groups);
      }
   }

   /**
    * Seeded pseudo-random color-group selection: index groups of the bone
    * variant space are deterministically chosen from the model-code seed
    * (squared percentage), so the same code always yields the same variant.
    */
   static HashSet<Integer> parseColorGroup(int boneCount, String code) {
      HashSet chosen = new HashSet();
      int seed = Integer.parseInt(code);
      seed = (int)(0.01F * seed * seed);
      int groupCount = Math.round(seed / 100.0F * boneCount);
      Random random = new Random(seed);

      for (int i = 0; i < groupCount; i++) {
         int candidate = random.nextInt(boneCount);
         if (!chosen.contains(candidate)) {
            chosen.add(candidate);
         } else {
            i--;
         }
      }

      return chosen;
   }

   /**
    * Recursively selects a bone variant chain: hides all children of the
    * parent part bone and un-hides the chosen child index, so the model code
    * picks ear/hair variants.
    */
   public static void applyBoneParts(GeoBone parentBone, String partCode, String childCode, String colorCode) {
      GeoBone partBone = getChildBone(parentBone, Integer.parseInt(partCode));
      GeoBone variantBone = getChildBone(partBone, Integer.parseInt(childCode));
      List children = variantBone.childBones;
      int childCount = children.size();
      HashSet<Integer> colorGroup = parseColorGroup(childCount, colorCode);
      variantBone.childBones.forEach(childBone -> childBone.setHidden(true));
      colorGroup.forEach(index -> getChildBone(variantBone, index));
   }

   /**
    * Darkens bone tints by the local light level while the goblin is on the
    * shoulder or being picked up (first-person poses ignore world lighting).
    */
   @Override
   protected Vec3i tintBoneColor(Vec3i color) {
      if (!this.isShoulderIdle && !this.isBeingPickedUp) {
         return color;
      }

      float lightScale = ThreadNames.clampFloat(this.lightLevel, 2.0F, 15.0F) / 15.0F;
      return new Vec3i(color.getX() * lightScale, color.getY() * lightScale, color.getZ() * lightScale);
   }

   /**
    * While running or catching, the goblin holds its own inventory item
    * ({@code GoblinEntity.a0}) instead of the rendered default stack.
    */
   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack defaultStack) {
      Action action = this.renderEntity.getCurrentAction();
      return action != Action.RUN && action != Action.CATCH ? defaultStack : (ItemStack)this.renderEntity.getDataManager().get(GoblinEntity.a0);
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      return new HashSet<String>() {
         {
            this.add("boobs");
            this.add("booty");
            this.add("vagina");
            this.add("fuckhole");
            this.add("preggy");
            this.add("LegL");
            this.add("LegR");
            this.add("cheekR");
            this.add("cheekL");
         }
      };
   }

   @Override
   protected float getDefaultScale() {
      return this.renderEntity.getCurrentAction() == Action.CATCH ? 0.5F : 1.0F;
   }

   @Override
   protected Vec3d getItemRenderOffset(ItemStack stack) {
      if (stack == null) {
         return Vec3d.ZERO;
      } else {
         return !(stack.getItem() instanceof ItemBlock) && stack.getMaxStackSize() != 1 ? new Vec3d(180.0, 0.0, 0.0) : super.getItemRenderOffset(stack);
      }
   }

   /**
    * Skips cubes of hidden custom-part bones and hides all leg bones in the
    * shoulder-idle pose (they would clip into the player's view).
    */
   @Override
   public void renderCubeGeometry(BufferBuilder buffer, GeoCube cube, GeoBone bone, float r, float g, float b, float a, double textureVOffset) {
      if (!this.isShoulderIdle || LEG_BONE_NAMES.contains(bone.getName())) {
         if (!this.activeCustomPartBones.contains(bone.getName())) {
            this.currentRenderingBone = bone;
            super.renderCubeGeometry(buffer, cube, bone, r, g, b, a, textureVOffset);
         }
      }
   }

}
