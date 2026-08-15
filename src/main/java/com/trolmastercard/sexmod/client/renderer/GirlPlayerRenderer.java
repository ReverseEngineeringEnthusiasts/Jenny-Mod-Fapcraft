package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import java.util.Objects;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

/**
 * Renderer for the horny-potion player-girls ({@link AbstractPlayerGirlEntity}):
 * a {@link GirlRenderer} whose model mirrors the transformed player — arms,
 * held items, shield blocking and bow pulling follow the owner player's state,
 * and the first-person camera rendering is supported through the
 * {@code isFirstPerson} flag.
 * <p>
 * <b>Owner sync.</b> {@link #doRenderEntity} copies the owner's held items,
 * using-item/rendering flags and armor ({@code syncArmor}) into the renderer
 * before delegating; the item bones draw the owner's actual stacks
 * ({@link #renderEquippedItem} with bow-pull progress and shield-block poses).
 * <p>
 * <b>First person.</b> {@code isFirstPerson} marks renders issued from the
 * first-person camera path (girlCam bone camera in
 * {@code GirlCameraHelper}); {@link #isGirlVisible} consumes the flag exactly
 * once, and {@link #shouldRenderFirstPersonHead} hides the head while the
 * local player's own girl is rendered in first person (except in
 * inventory/creative screens).
 * <p>
 * CLIENT-side render thread only. No shadow is drawn for player-girls.
 */
public class GirlPlayerRenderer extends GirlRenderer {
   public static boolean isFirstPerson = false;
   public ItemStack mainhandItem = ItemStack.EMPTY;
   public ItemStack offhandItem = ItemStack.EMPTY;
   public boolean isRendering = false;
   public boolean isUsingItem = false;
   protected AbstractPlayerGirlEntity playerGirl;
   protected float partialTicks;
   float bowPullProgress = 0.0F;

   public GirlPlayerRenderer(RenderManager renderManager, AnimatedGeoModel model) {
      super(renderManager, model, 0.0);
   }

   public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
   }

   /**
    * Whether the girl should be drawn: locally registered previews always
    * render; otherwise the {@code isFirstPerson} flag is consumed (reset to
    * {@code false}) and returned — the first-person camera path is the only
    * one that may draw the local player's own girl.
    */
   boolean isGirlVisible(BaseGirlEntity girl) {
      if (girl.isLocallyRegistered()) {
         return true;
      }

      boolean visible = isFirstPerson;
      isFirstPerson = false;
      return visible;
   }

   /**
    * Player-girl render entry: gated by {@link #isGirlVisible}; syncs the
    * owner's items/flags/armor, draws the name label for other players, then
    * delegates to the normal girl pipeline.
    */
   @Override
   public void doRenderEntity(BaseGirlEntity girl, double x, double y, double z, float entityYaw, float partialTicks) {
      if (this.isGirlVisible(girl)) {
         AbstractPlayerGirlEntity playerGirl = (AbstractPlayerGirlEntity)girl;
         if (playerGirl.getOwnerUserUUID() != null) {
            EntityPlayer owner = Minecraft.getMinecraft().player.world.getPlayerEntityByUUID(playerGirl.getOwnerUserUUID());
            if (owner != null) {
               this.mainhandItem = owner.getHeldItemMainhand();
               this.offhandItem = owner.getHeldItemOffhand();
               this.isUsingItem = playerGirl.ah;
               this.isRendering = playerGirl.ad;
               this.playerGirl = (AbstractPlayerGirlEntity)girl;
               this.partialTicks = partialTicks;
               playerGirl.syncArmor(owner);
               if (this.isOwnPlayer(owner, girl)) {
                  this.renderLivingLabel(girl, owner.getName(), x, y + playerGirl.getScaleFactor(), z, 300);
               }

               super.doRenderEntity(girl, x, y, z, entityYaw, partialTicks);
            }
         }
      }
   }

   /**
    * Render entity used for position interpolation: the owner player when
    * present (so the girl's pose tracks the owner's movement), else the girl
    * itself.
    */
   @Override
   public Entity getRenderEntity(BaseGirlEntity girl) {
      if (!(girl instanceof AbstractPlayerGirlEntity)) {
         return girl;
      }

      AbstractPlayerGirlEntity playerGirl = (AbstractPlayerGirlEntity)girl;
      EntityPlayer owner = playerGirl.getOwnerPlayer();
      return (Entity)(owner == null ? girl : owner);
   }

   /**
    * Name-label visibility: never for the local player; for others, hidden
    * while the current action hides name tags.
    */
   boolean isOwnPlayer(EntityPlayer player, BaseGirlEntity girl) {
      if (player.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
         return false;
      }

      Action action = girl.getCurrentAction();
      return action == null ? true : !action.hideNameTag;
   }

   protected void onBoneRenderStart(String boneName, GeoBone bone) {
   }

   protected void onBoneRenderingLayer(String boneName, GeoBone bone, AbstractPlayerGirlEntity playerGirl, BufferBuilder buffer) {
   }

   /**
    * Bone recursion for player-girls: adds scene/using-item pose overrides
    * (upperBody/head tilt while {@code isRendering}, bow-draw arm pitch, shield
    * blocking arm poses), renders the head overlay (elytra layer), the held
    * main/offhand items on their bones, and the armor-tinted cube pass (see
    * {@link GirlRenderer#renderRecursively} for the shared rules).
    */
   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a) {
      String boneName = bone.getName();
      if (this.isRendering) {
         if (boneName.equals("upperBody")) {
            bone.setRotationX(bone.getRotationX() - 0.5F);
         }

         if (boneName.equals("head")) {
            bone.setRotationX(bone.getRotationX() + 0.5F);
         }
      }

      if (boneName.equals("head")) {
         this.renderOverlay(buffer, bone, Color.ofRGB(r, g, b));
      }

      this.onBoneRenderStart(boneName, bone);
      this.onBoneRenderingLayer(boneName, bone, this.playerGirl, buffer);
      if (this.isUsingItem && (this.mainhandItem.getItem() instanceof ItemBow || this.offhandItem.getItem() instanceof ItemBow)) {
         if (boneName.equals("armR")) {
            bone.setRotationX(bone.getRotationX() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (boneName.equals("armL")) {
            bone.setRotationY(bone.getRotationY() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (this.offhandItem.getItem() instanceof ItemBow) {
            ItemStack tempStack = this.offhandItem;
            this.offhandItem = this.mainhandItem;
            this.mainhandItem = tempStack;
         }
      }

      if (this.isUsingItem && this.mainhandItem.getItem() instanceof ItemShield) {
         if (boneName.equals("armR")) {
            bone.setRotationZ(0.0F);
            bone.setRotationX(0.5F);
         } else if (this.offhandItem.getItem() instanceof ItemShield && boneName.equals("armL")) {
            bone.setRotationZ(0.0F);
            bone.setRotationX(0.5F);
         }
      }

      if (boneName.equals("weapon") && !this.mainhandItem.isEmpty()) {
         this.renderEquippedItem(buffer, bone, false);
      }

      if (boneName.equals("offhand") && !this.offhandItem.isEmpty()) {
         this.renderEquippedItem(buffer, bone, true);
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(bone);
      MATRIX_STACK.moveToPivot(bone);
      MATRIX_STACK.rotate(bone);
      MATRIX_STACK.scale(bone);
      MATRIX_STACK.moveBackFromPivot(bone);
      if ("Head2".equals(boneName) && !this.shouldRenderHead2()) {
         MATRIX_STACK.pop();
      } else if (("neck".equals(boneName) || "head".equals(boneName)) && !this.shouldRenderFirstPersonHead()) {
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
                  GlStateManager.pushMatrix();
                  this.currentRenderingBone = bone;
                  this.renderCubeGeometry(buffer, cube, r, g, b, a, (double)armorAlpha);
                  GlStateManager.popMatrix();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone childBone : bone.childBones) {
               if (armorAlpha == 0.0) {
                  this.renderRecursively(buffer, childBone, r, g, b, a);
               } else {
                  this.renderCustomBones(buffer, childBone, r, g, b, a, (double)armorAlpha);
               }
            }
         }

         try {
            MATRIX_STACK.pop();
         } catch (IllegalStateException e) {
         }
      }
   }

   /**
    * Whether the head bone may be rendered: always for girls without an owner
    * UUID or in third person; in first person only while an inventory screen
    * is open (the vanilla head would otherwise occlude the first-person
    * camera).
    */
   public boolean shouldRenderFirstPersonHead() {
      if (!((AbstractPlayerGirlEntity)this.playerGirl).hasOwnerUUID()) {
         return true;
      } else {
         return mc.gameSettings.thirdPersonView != 0 ? true : mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative;
      }
   }

   /**
    * Renders the {@link GirlLayerRenderer} (elytra) at the head bone with the
    * bone transform applied; restores the entity texture and vertex buffer
    * afterwards. This is what draws armor/elytra layers on player-girls.
    */
   public void renderOverlay(BufferBuilder buffer, GeoBone bone, Color color) {
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
      GL11.glEnable(2896);
      this.preRenderCallback();
      new GirlLayerRenderer(this).render(this.playerGirl, this.playerGirl.limbSwing, this.playerGirl.limbSwingAmount, this.partialTicks, 0.0F, 0.0F, 0.0F, color);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.playerGirl)));
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
   }

   protected void preRenderCallback() {
   }

   /**
    * Renders the owner's held item on the weapon/offhand bone: applies the
    * bow-pull or shield-block pose, tracks bow draw progress onto the girl
    * (item-use count / active hand / held-item override), then renders the
    * third-person item and restores buffer + texture.
    */
   public void renderEquippedItem(BufferBuilder buffer, GeoBone bone, boolean isOffhand) {
      ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
      GL11.glEnable(2896);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      ItemStack stack = isOffhand ? this.offhandItem : this.mainhandItem;
      switch (stack.getItem().getItemUseAction(stack)) {
         case BOW:
            this.applyBowRotation(isOffhand);
            break;
         case BLOCK:
            this.applyShieldBlockingTransform(isOffhand, this.isUsingItem);
      }

      if (this.isUsingItem && !isOffhand && stack.getItem() instanceof ItemBow) {
         this.bowPullProgress += 0.015F;
         this.playerGirl.setItemUseCount(Math.round(-this.bowPullProgress * 20.0F + stack.getMaxItemUseDuration()));
         this.playerGirl.setHeldItemOverride(stack);
         this.playerGirl.setActiveHand(EnumHand.MAIN_HAND);
         this.playerGirl.setHandActiveState();
      } else {
         this.bowPullProgress = 0.0F;
         this.playerGirl.setItemUseCount(0);
         this.playerGirl.setHeldItemOverride(ItemStack.EMPTY);
         this.playerGirl.setHandActiveState();
      }

      this.applyItemPostRotation(isOffhand, stack);
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      itemRenderer.renderItem(this.playerGirl, stack, TransformType.THIRD_PERSON_RIGHT_HAND);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.playerGirl)));
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   /**
    * Base item rotation: 90 degrees for main hand, 200 for offhand.
    */
   protected void applyItemPostRotation(boolean isOffhand, ItemStack stack) {
      GlStateManager.rotate(isOffhand ? 200.0F : 90.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void applyBowRotation(boolean isOffhand) {
      GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
   }

   /**
    * Shield-blocking pose: mirrors the shield across the body, and while
    * actively blocking (isUsingItem) raises it in front of the face (with
    * per-hand offsets).
    */
   protected void applyShieldBlockingTransform(boolean isOffhand, boolean isBlocking) {
      if (isOffhand) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         if (isBlocking) {
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(35.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0F, 0.228F);
         }
      } else if (isBlocking) {
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0F, 0.165F, 0.0F);
      }
   }

}
