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

   public GoblinRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
      mc = Minecraft.getMinecraft();
   }

   /**
    * Resolves the goblin's skin texture: the owner's cached skin (interaction
    * player, else owner) from the static cache map, or a freshly tinted skin
    * when not cached. In the preload world ({@link SexWorldClient}) or with no
    * owner it uses the local player's profile id.
    */
   protected ResourceLocation getGoblinTexture(GoblinEntity var1) {
      UUID var3 = var1.getInteractionPlayerUUID();
      if (var3 == null) {
         var3 = var1.getOwnerUUID();
      }

      ResourceLocation var2;
      if (!(var1.world instanceof SexWorldClient) && var3 != null) {
         var2 = l.get(var3);
         if (var2 == null) {
            return this.getTintedSkinTexture(var3, var1.world);
         }
      } else {
         var2 = l.get(mc.getSession().getProfile().getId());
         if (var2 == null) {
            return this.getTintedSkinTexture(mc.getSession().getProfile().getId(), var1.world);
         }
      }

      return var2;
   }

   /**
    * Renders the goblin at the origin with the first-person sentinel yaw, so
    * {@link #doRenderGoblin} treats it as a first-person pose (shoulder-idle /
    * pick-up) and applies the camera-relative transform. CLIENT render thread.
    */
   public static void renderEntityInFirstPerson(BaseGirlEntity var0, float var1) {
      mc.getRenderManager().renderEntity(var0, 0.0, 0.0, 0.0, -420.69F, var1, false);
   }

   /**
    * Applies the first-person camera bob (vanilla walk-bob formula) so the
    * goblin on the shoulder sways with the player's steps. CLIENT render
    * thread; must run before drawing the goblin quad.
    */
   public static void setFirstPersonCamera(float var0) {
      if (mc.getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer var1 = (EntityPlayer)mc.getRenderViewEntity();
         float var2 = var1.distanceWalkedModified - var1.prevDistanceWalkedModified;
         float var3 = -(var1.distanceWalkedModified + var2 * var0);
         float var4 = var1.prevCameraYaw + (var1.cameraYaw - var1.prevCameraYaw) * var0;
         float var5 = MathHelper.sin(var3 * (float) Math.PI) * var4 * 0.5F;
         GlStateManager.translate(
            Math.cos(mc.player.rotationYaw * (Math.PI / 180.0)) * var5,
            Math.abs(MathHelper.cos(var3 * (float) Math.PI) * var4),
            Math.sin(mc.player.rotationYaw * (Math.PI / 180.0)) * var5
         );
      }
   }

   public void renderModel(GeoModel var1, GoblinEntity var2, float var3, float var4, float var5, float var6, float var7) {
      super.renderModel(var1, var2, var3, var4, var5, var6, var2.ar);
   }

   /**
    * Skips the shadow/fire pass while the goblin is being picked up or riding
    * the shoulder (it would draw a shadow inside the player's view).
    */
   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!(var1 instanceof GoblinEntity)) {
         super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
      } else {
         GoblinEntity var10 = (GoblinEntity)var1;
         if (var10.getCurrentAction() != Action.PICK_UP && var10.getCurrentAction() != Action.SHOULDER_IDLE) {
            super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
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
   public static Vec3d getThrowPosition(World var0, BaseGirlEntity var1, UUID var2, double var3, double var5, double var7) {
      if (var0 == null) {
         return new Vec3d(var3, var5, var7);
      }

      if (var2 == null) {
         return new Vec3d(var3, var5, var7);
      }

      if (var1 == null) {
         return new Vec3d(var3, var5, var7);
      }

      EntityPlayer var9 = var0.getPlayerEntityByUUID(var2);
      if (var9 == null) {
         return new Vec3d(var3, var5, var7);
      }

      Vec3d var10 = var9.getPositionVector();
      Vec3d var11 = mc.player.getPositionVector();
      var1.prevRenderYawOffset = var9.prevRotationYawHead;
      var1.renderYawOffset = var9.rotationYawHead;
      var1.setCurrentAction(Action.START_THROWING);
      return var10.subtract(var11);
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
   public void doRenderGoblin(GoblinEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.renderEntity = var1;
      this.isShoulderIdle = -420.69F == var8 && var1.getCurrentAction() == Action.SHOULDER_IDLE;
      this.isBeingPickedUp = -420.69F == var8 && var1.getCurrentAction() == Action.PICK_UP;
      this.lightLevel = var1.world.getLight(var1.getPosition(), true);
      this.currentYawOffset = var9;
      currentActionValue = var8;
      Action var10 = var1.getCurrentAction();
      UUID var11 = var1.getOwnerUUID();
      if (var11 != null) {
         if (var1.isLocallyRegistered()) {
            Vec3d var19 = getThrowPosition(var1.world, var1, var11, var2, var4, var6);
            var2 = var19.x;
            var4 = var19.y;
            var6 = var19.z;
         }

         if (var10 == Action.THROWN || var10 == Action.START_THROWING) {
            if (mc.gameSettings.thirdPersonView == 0 && var8 == -420.69F && !var1.isLocallyRegistered()) {
               return;
            }

            if (!var1.isLocallyRegistered()) {
               float var20 = var1.getYawRotation();
               var1.prevRenderYawOffset = var20;
               var1.renderYawOffset = var20;
            }
         }

         if (isThrowAction(var1, var10)) {
            if (mc.player.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.renderYawOffset = mc.player.rotationYaw + 180.0F;
               var1.prevRenderYawOffset = mc.player.rotationYaw + 180.0F;
               Vec3d var21 = mc.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(var21.x, var21.y + mc.player.getEyeHeight(), var21.z);
               Vec3d var28 = GoblinEntity.rotateVectorYaw(new Vec3d(-Math.abs(mc.player.rotationPitch), 0.0, 0.0), mc.player.rotationYaw);
               GlStateManager.rotate(mc.player.rotationPitch, (float)var28.x, 0.0F, (float)var28.z);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else {
               if (!var1.isLocallyRegistered() || mc.player.getPersistentID().equals(var11)) {
                  if (!mc.player.getPersistentID().equals(var11)) {
                     EntityPlayer var22 = var1.world.getPlayerEntityByUUID(var11);
                     if (var22 != null) {
                        var1.renderYawOffset = var22.rotationYaw;
                        var1.prevRenderYawOffset = var22.rotationYaw;
                     }
                  } else {
                     var1.renderYawOffset = mc.player.rotationYaw;
                     var1.prevRenderYawOffset = mc.player.rotationYaw;
                  }
               }

               Vec3d var23 = getThrowAim(var1, var1.getOwnerUUID(), var9);
               var2 = var23.x;
               var4 = var23.y;
               var6 = var23.z;
            }
         } else if (this.isShoulderIdle) {
            setFirstPersonCamera(var9);
            Vec3d var24 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, mc.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            var24 = GoblinEntity.rotateVectorYaw(var24, mc.player.rotationYaw);
            var2 = var24.x;
            var4 = var24.y;
            var6 = var24.z;
            var1.renderYawOffset = mc.player.rotationYaw;
            var1.prevRenderYawOffset = mc.player.prevRotationYaw;
            if (mc.player.isSneaking()) {
               var4 -= 0.075;
            }
         } else if (var10 == Action.SHOULDER_IDLE) {
            if (mc.player.getPersistentID().equals(var11) && mc.gameSettings.thirdPersonView == 0) {
               return;
            }

            EntityPlayer var26 = var1.world.getPlayerEntityByUUID(var11);
            if (var26 == null) {
               return;
            }

            Vector4f var29 = getFirstPersonView(var26, var9);
            var2 = var29.x;
            var4 = var29.y;
            var6 = var29.z;
            var1.renderYawOffset = var29.w;
            if (var26.isSneaking()) {
               var4 -= 0.32;
            }
         } else if (var10 == Action.PICK_UP) {
            EntityPlayer var27 = var1.world.getPlayerEntityByUUID(var11);
            if (var27 != null) {
               var1.prevRenderYawOffset = var27.prevRotationYawHead;
               var1.renderYawOffset = var27.rotationYawHead;
            }
         }

         super.doRenderEntity(var1, var2, var4, var6, var8, var9);
         if (isThrowAction(var1, var10) && mc.gameSettings.thirdPersonView == 0 && mc.player.getPersistentID().equals(var11)) {
            GlStateManager.popMatrix();
         }
      } else {
         if (var1.isLocallyRegistered()) {
            Vec3d var12 = getThrowPosition(var1.world, var1, var11, var2, var4, var6);
            var2 = var12.x;
            var4 = var12.y;
            var6 = var12.z;
         }

         if (var10 == Action.THROWN || var10 == Action.START_THROWING) {
            if (mc.gameSettings.thirdPersonView == 0 && var8 == -420.69F && !var1.isLocallyRegistered()) {
               return;
            }

            if (!var1.isLocallyRegistered()) {
               float var14 = var1.getYawRotation();
               var1.prevRenderYawOffset = var14;
               var1.renderYawOffset = var14;
            }
         }

         if (isThrowAction(var1, var10)) {
            if (mc.player.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.renderYawOffset = mc.player.rotationYaw + 180.0F;
               var1.prevRenderYawOffset = mc.player.rotationYaw + 180.0F;
               Vec3d var15 = mc.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(var15.x, var15.y + mc.player.getEyeHeight(), var15.z);
               Vec3d var13 = GoblinEntity.rotateVectorYaw(new Vec3d(-Math.abs(mc.player.rotationPitch), 0.0, 0.0), mc.player.rotationYaw);
               GlStateManager.rotate(mc.player.rotationPitch, (float)var13.x, 0.0F, (float)var13.z);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else {
               if (var1.isLocallyRegistered()) {
               }

               var1.renderYawOffset = mc.player.rotationYaw;
               var1.prevRenderYawOffset = mc.player.rotationYaw;
               Vec3d var16 = getThrowAim(var1, var1.getOwnerUUID(), var9);
               var2 = var16.x;
               var4 = var16.y;
               var6 = var16.z;
            }
         } else if (this.isShoulderIdle) {
            setFirstPersonCamera(var9);
            Vec3d var17 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, mc.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            var17 = GoblinEntity.rotateVectorYaw(var17, mc.player.rotationYaw);
            var2 = var17.x;
            var4 = var17.y;
            var6 = var17.z;
            var1.renderYawOffset = mc.player.rotationYaw;
            var1.prevRenderYawOffset = mc.player.prevRotationYaw;
            if (mc.player.isSneaking()) {
               var4 -= 0.075;
            }
         } else {
            if (var10 == Action.SHOULDER_IDLE) {
               return;
            }

            if (var10 == Action.PICK_UP) {
            }
         }

         super.doRenderEntity(var1, var2, var4, var6, var8, var9);
         if (isThrowAction(var1, var10) && mc.gameSettings.thirdPersonView == 0 && mc.player.getPersistentID().equals(var11)) {
            GlStateManager.popMatrix();
         }
      }
   }

   /**
    * Whether the action renders the goblin in a thrown/caught/pick-up pose.
    * Start-throwing is only a render action for locally registered goblins,
    * and throw poses are never rendered in first-person view.
    */
   public static boolean isThrowAction(BaseGirlEntity var0, Action var1) {
      if (var1 == Action.START_THROWING && !var0.isLocallyRegistered()) {
         return false;
      }

      if (mc.gameSettings.thirdPersonView == 0 || var1 != Action.START_THROWING && var1 != Action.PICK_UP) {
         switch (var1) {
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
   public static Vec3d getThrowAim(BaseGirlEntity var0, UUID var1, float var2) {
      if (var1 == null) {
         return Vec3d.ZERO;
      }

      EntityPlayer var3 = var0.world.getPlayerEntityByUUID(var1);
      if (var3 == null) {
         return Vec3d.ZERO;
      }

      Vec3d var4 = RotationHelper.lerpVec3dDouble(new Vec3d(var3.prevPosX, var3.prevPosY, var3.prevPosZ), var3.getPositionVector(), var2);
      Vec3d var5 = RotationHelper.lerpVec3dDouble(
         new Vec3d(mc.player.prevPosX, mc.player.prevPosY, mc.player.prevPosZ), mc.player.getPositionVector(), var2
      );
      return var4.subtract(var5);
   }

   /**
    * First-person shoulder camera: lerped position of the owner relative to
    * the local player plus the owner's interpolated render yaw (as .w).
    */
   public static Vector4f getFirstPersonView(EntityPlayer var0, float var1) {
      EntityPlayerSP var2 = mc.player;
      float var3 = RotationHelper.lerp(var0.prevRenderYawOffset, var0.renderYawOffset, var1);
      Vec3d var4 = RotationHelper.lerpVec3dDouble(new Vec3d(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ), var0.getPositionVector(), var1);
      Vec3d var5 = RotationHelper.lerpVec3dDouble(new Vec3d(var2.lastTickPosX, var2.lastTickPosY, var2.lastTickPosZ), var2.getPositionVector(), var1);
      Vec3d var6 = var4.subtract(var5);
      return new Vector4f((float)var6.x, (float)var6.y, (float)var6.z, var3);
   }

   /**
    * Maps a bone name to its tint from the girl's model code (parts[0..9]):
    * eyes -> eye color, variant/boob/nude bones -> skin color, hair/lash ->
    * hair color, crown handled separately. White (no tint) for unknown bones
    * or short model codes.
    */
   @Override
   protected Vec3i getBoneColor(String var1) {
      String[] var2 = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
      if (var2.length < 8) {
         return DEFAULT_BONE_COLOR;
      } else if (var1.contains("band")) {
         return DEFAULT_BONE_COLOR;
      } else if (var1.contains("eyeColor") || var1.contains("eyeColor2")) {
         return getEyeColor(var2[8]);
      } else if (var1.contains("variant") || var1.contains("boob")) {
         return getSkinColor(var2[7]);
      } else if (var1.contains("hair")) {
         return getHairColor(var2[6]);
      } else if (NUDE_BONE_NAMES.contains(var1)) {
         return getSkinColor(var2[7]);
      } else {
         return LASH_BONE_NAMES.contains(var1) ? getHairColor(var2[6]) : DEFAULT_BONE_COLOR;
      }
   }

   public static Vec3i getEyeColor(String var0) {
      return EyeColor.values()[Integer.parseInt(var0)].getColor();
   }

   public static Vec3i getSkinColor(String var0) {
      return SkinColor.values()[Integer.parseInt(var0)].getColor();
   }

   public static Vec3i getHairColor(String var0) {
      return HairColor.values()[Integer.parseInt(var0)].getColor();
   }

   /**
    * Per-bone geometry tweaks from the model code (skipped in the
    * {@link SexWorldClient} preload world): ears/hair swap to the chosen part
    * models, the body pivot drops to -0.15 (plus shoulder-idle pose), leg/boob
    * bones lean with the strafe/forward rotations, and crown bones hide
    * depending on ownership state.
    */
   @Override
   protected void onBoneProcessing(BufferBuilder var1, String var2, GeoBone var3) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String[] var4 = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
         if (var4.length >= 8) {
            switch (var2) {
               case "earL":
                  applyBoneParts(var3, var4[0], var4[1], var4[3]);
                  break;
               case "earR":
                  applyBoneParts(var3, var4[0], var4[2], var4[4]);
                  break;
               case "hair":
                  applyBonePart(var3, var4[5]);
                  break;
               case "body":
                  var3.setPivotY(-0.15F);
                  applyBoneState(this.renderEntity, var3);
                  break;
               case "LegR":
                  applyBoneRot(this.isShoulderIdle, var3, 25.0F, 25.0F);
                  break;
               case "boobR":
                  applyBoneRot(this.isShoulderIdle, var3, 30.0F, 30.0F);
                  break;
               case "boobR1":
                  applyBoneRot(this.isShoulderIdle, var3, 10.0F, 15.0F);
                  break;
               case "boobR2":
                  applyBoneRot(this.isShoulderIdle, var3, 5.0F, 3.0F);
            }

            if (var2.contains("crown")) {
               applyBoneColor(this.renderEntity, var3, var4[9]);
            }
         }
      }
   }

   /**
    * Hides the crown bone by ownership: hidden for locally registered girls,
    * hidden when the model-code flag is 0 for NPC goblins, hidden when the
    * player-goblin wears no helmet.
    */
   public static void applyBoneColor(BaseGirlEntity var0, GeoBone var1, String var2) {
      if (var0.isLocallyRegistered()) {
         var1.setHidden(true);
      } else if (var0 instanceof GoblinEntity) {
         int var3 = Integer.parseInt(var2);
         var1.setHidden(var3 == 0);
      } else if (var0 instanceof GoblinPlayerEntity) {
         var1.setHidden(((ItemStack)var0.getDataManager().get(AbstractGirlNpcEntity.HELMET_SLOT)).isEmpty());
      }
   }

   /**
    * Leans the bone by the player's current strafe/forward rotation (clamped
    * to +/-the given angles) while the goblin rides the shoulder; no-op while
    * the game is paused.
    */
   public static void applyBoneRot(boolean var0, GeoBone var1, float var2, float var3) {
      if (!mc.isGamePaused()) {
         if (var0) {
            var1.setRotationX(var1.getRotationX() + TrigMath.wrapDegrees(ThreadNames.clampFloat(forwardRotation, -var2, var2)));
            var1.setRotationZ(var1.getRotationZ() + TrigMath.wrapDegrees(ThreadNames.clampFloat(strafeRotation, -var3, var3)));
         }
      }
   }

   /**
    * Body-bone pose for shoulder-idle: raises the pivot to 8 and tilts the
    * body with the camera pitch so the goblin "sits" in the player's view.
    */
   public static void applyBoneState(BaseGirlEntity var0, GeoBone var1) {
      if (currentActionValue == -420.69F && var0.getCurrentAction() == Action.SHOULDER_IDLE) {
         float var2 = -mc.getRenderManager().playerViewX;
         var1.setPivotY(8.0F);
         if (!mc.isGamePaused()) {
            var1.setRotationX(var1.getRotationX() + TrigMath.wrapDegrees(var2));
         }
      }
   }

   public static void applyBonePart(GeoBone var0, String var1) {
      int var2 = Integer.parseInt(var1);
      getChildBone(var0, var2);
   }

   static HashSet<Integer> buildColorIndexGroups(int var0, String var1) {
      int var2 = Integer.parseInt(var1);
      int var3 = var0 - 1;
      ArrayList var4 = buildColorIndexGroups(var3);

      while (var2 >= var4.size()) {
         var2 -= var4.size();
      }

      return (HashSet<Integer>)var4.get(var2);
   }

   static ArrayList<HashSet<Integer>> buildColorIndexGroups(int var0) {
      ArrayList var1 = new ArrayList();
      buildColorGroups(0, new HashSet<>(), var0, var1);
      return var1;
   }

   static void buildColorGroups(int var0, HashSet<Integer> var1, int var2, ArrayList<HashSet<Integer>> var3) {
      if (var0 > var2) {
         var3.add(var1);
      } else {
         HashSet var4 = new HashSet(var1);
         buildColorGroups(var0 + 1, var1, var2, var3);
         var4.add(var0);
         buildColorGroups(var0 + 1, var4, var2, var3);
      }
   }

   /**
    * Seeded pseudo-random color-group selection: index groups of the bone
    * variant space are deterministically chosen from the model-code seed
    * (squared percentage), so the same code always yields the same variant.
    */
   static HashSet<Integer> parseColorGroup(int var0, String var1) {
      HashSet var2 = new HashSet();
      int var3 = Integer.parseInt(var1);
      var3 = (int)(0.01F * var3 * var3);
      int var4 = Math.round(var3 / 100.0F * var0);
      Random var5 = new Random(var3);

      for (int var6 = 0; var6 < var4; var6++) {
         int var7 = var5.nextInt(var0);
         if (!var2.contains(var7)) {
            var2.add(var7);
         } else {
            var6--;
         }
      }

      return var2;
   }

   /**
    * Recursively selects a bone variant chain: hides all children of the
    * parent part bone and un-hides the chosen child index, so the model code
    * picks ear/hair variants.
    */
   public static void applyBoneParts(GeoBone var0, String var1, String var2, String var3) {
      GeoBone var4 = getChildBone(var0, Integer.parseInt(var1));
      GeoBone var5 = getChildBone(var4, Integer.parseInt(var2));
      List var6 = var5.childBones;
      int var7 = var6.size();
      HashSet<Integer> var8 = parseColorGroup(var7, var3);
      var5.childBones.forEach(var0x -> var0x.setHidden(true));
      var8.forEach(var1x -> getChildBone(var5, var1x));
   }

   /**
    * Darkens bone tints by the local light level while the goblin is on the
    * shoulder or being picked up (first-person poses ignore world lighting).
    */
   @Override
   protected Vec3i tintBoneColor(Vec3i var1) {
      if (!this.isShoulderIdle && !this.isBeingPickedUp) {
         return var1;
      }

      float var2 = ThreadNames.clampFloat(this.lightLevel, 2.0F, 15.0F) / 15.0F;
      return new Vec3i(var1.getX() * var2, var1.getY() * var2, var1.getZ() * var2);
   }

   /**
    * While running or catching, the goblin holds its own inventory item
    * ({@code GoblinEntity.a0}) instead of the rendered default stack.
    */
   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack var1) {
      Action var2 = this.renderEntity.getCurrentAction();
      return var2 != Action.RUN && var2 != Action.CATCH ? var1 : (ItemStack)this.renderEntity.getDataManager().get(GoblinEntity.a0);
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
   protected Vec3d getItemRenderOffset(ItemStack var1) {
      if (var1 == null) {
         return Vec3d.ZERO;
      } else {
         return !(var1.getItem() instanceof ItemBlock) && var1.getMaxStackSize() != 1 ? new Vec3d(180.0, 0.0, 0.0) : super.getItemRenderOffset(var1);
      }
   }

   /**
    * Skips cubes of hidden custom-part bones and hides all leg bones in the
    * shoulder-idle pose (they would clip into the player's view).
    */
   @Override
   public void renderCubeGeometry(BufferBuilder var1, GeoCube var2, GeoBone var3, float var4, float var5, float var6, float var7, double var8) {
      if (!this.isShoulderIdle || LEG_BONE_NAMES.contains(var3.getName())) {
         if (!this.activeCustomPartBones.contains(var3.getName())) {
            this.currentRenderingBone = var3;
            super.renderCubeGeometry(var1, var2, var3, var4, var5, var6, var7, var8);
         }
      }
   }

}
