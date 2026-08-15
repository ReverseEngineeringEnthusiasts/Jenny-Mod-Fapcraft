package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.KoboldPlayerEntity;
import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the kobold NPC (and kobold-player): a {@link GirlRendererBase}
 * whose bone colors encode the kobold's color scheme and whose held items
 * follow the current action.
 * <p>
 * <b>Coloring.</b> {@link #getBoneColor} maps bones from the static
 * {@link #hideBones} (main color) / {@link #showBones} (secondary color) sets
 * to the kobold's {@link EyeAndKoboldColor} palette; the iris bones use the
 * action target position as an RGB color. The color cache is invalidated on
 * action changes ({@link #doRenderKobold} watches the action string).
 * <p>
 * <b>Held items.</b> {@link #resolveHeldItemStack} switches the held item by
 * action: iron axe/pickaxe while mining, sword while attacking or when the
 * guard flag is set. Payment items (pickaxe / 3 gold) come from the hand
 * states during the blowjob/anal trade actions.
 * <p>
 * CLIENT-side render thread only. In the {@link SexWorldClient} preload world
 * the custom-bone pass is skipped.
 */
public class KoboldRenderer extends GirlRendererBase<KoboldEntity> {
   public static final HashSet<String> hideBones = new HashSet<>(
      Arrays.asList(
         "colorSpots",
         "neck",
         "head",
         "snout",
         "midSectionR",
         "midSectionL",
         "innerCheekLR",
         "innerCheekRR",
         "gayL",
         "gayR",
         "legR",
         "legL",
         "shinL",
         "toesL",
         "kneeL",
         "curvesL",
         "shinR",
         "toesR",
         "kneeR",
         "curvesR",
         "sideL",
         "sideR",
         "hip",
         "torsoL",
         "torsoR",
         "armR",
         "lowerArmR",
         "ellbowR",
         "armL",
         "lowerArmL",
         "ellbowL",
         "hornUL",
         "hornUR",
         "tail",
         "tail2",
         "tail3",
         "tail4",
         "tail5",
         "hornDL2",
         "hornDR2",
         "hornDR3M",
         "hornDL3M",
         "frecklesAL1",
         "frecklesAL2",
         "frecklesAR1",
         "frecklesAR2",
         "frecklesHL1",
         "frecklesHL2",
         "frecklesHR1",
         "frecklesHR2"
      )
   );
   public static final HashSet<String> showBones = new HashSet<>(
      Arrays.asList(
         "boobR",
         "boobL",
         "frontNeck",
         "Rside",
         "Lside",
         "frontAndInside",
         "innerCheekLL",
         "innerCheekRL",
         "layer",
         "layer2",
         "down",
         "down2",
         "down3",
         "down4",
         "down5",
         "fuckhole",
         "hornDR3S",
         "hornDL3S",
         "assholeCoverUp",
         "assholeCoverUp2"
      )
   );
   Minecraft mc = Minecraft.getMinecraft();

   public KoboldRenderer(RenderManager renderManager, AnimatedGeoModel model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }

   /**
    * Bone tint by name: hide-list bones use the kobold color's main color,
    * show-list bones its secondary color, iris bones encode the action-target
    * position as RGB; everything else stays white.
    */
   @Override
   protected Vec3i getBoneColor(String boneName) {
      EntityDataManager dataManager = this.renderEntity.getDataManager();
      EyeAndKoboldColor koboldColor = EyeAndKoboldColor.valueOf((String)dataManager.get(KoboldEntity.CURRENT_ACTION));
      BlockPos targetPos = (BlockPos)dataManager.get(KoboldEntity.ACTION_TARGET_POS);
      if (hideBones.contains(boneName)) {
         return koboldColor.getMainColor();
      } else if (showBones.contains(boneName)) {
         return koboldColor.getSecondaryColor();
      } else {
         return (Vec3i)(!"irisR".equals(boneName) && !"irisL".equals(boneName) ? defaultColor : targetPos);
      }
   }

   /**
    * Held item by action: axe or pickaxe while mining (flag chooses), sword
    * while attacking or when the guard flag is set; otherwise the default
    * stack.
    */
   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack defaultStack) {
      switch (this.renderEntity.getCurrentAction()) {
         case MINE:
            if ((Boolean)this.renderEntity.getDataManager().get(KoboldEntity.at)) {
               return new ItemStack(Items.IRON_AXE);
            }

            return new ItemStack(Items.IRON_PICKAXE);
         case NULL:
            if ((Boolean)this.renderEntity.getDataManager().get(KoboldEntity.aC)) {
               return new ItemStack(Items.IRON_SWORD);
            }
         default:
            return defaultStack;
         case ATTACK:
            return new ItemStack(Items.IRON_SWORD);
      }
   }

   /**
    * Custom-bone pass tweaks: the {@code blowOpening} bone disables the
    * texture V offset; the {@code mouth} bone shifts V by -1/128 when the
    * model code selects the open-mouth variant. Otherwise delegates to
    * {@link GirlRendererBase#renderCustomBones}.
    */
   @Override
   public void renderCustomBones(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a, double overlayAlpha) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String boneName = bone.getName();
         if ("blowOpening".equals(boneName)) {
            overlayAlpha = 0.0;
         }

         if ("mouth".equals(boneName)) {
            String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
            int mouthVariant = Integer.parseInt(modelCodeParts[7]);
            if (mouthVariant == 1) {
               overlayAlpha = -0.078125;
            }
         }

         super.renderCustomBones(buffer, bone, r, g, b, a, overlayAlpha);
      }
   }

   /**
    * Eye-squint pair: the left eye shrinks by the squint value (from the
    * player-kobold's data manager) — the right eye compensates with the
    * inverse scale so the face stays closed/open correctly.
    */
   @Override
   protected void renderLeftEye() {
      float squint = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      GlStateManager.scale(1.0F - squint, 1.0F - squint, 1.0F - squint);
   }

   @Override
   protected void renderRightEye() {
      float squint = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      double inverseScale = 1.0 / (1.0 - squint);
      GlStateManager.scale(inverseScale, inverseScale, inverseScale);
   }

   @Override
   protected ItemStack getPaymentItemStack() {
      String handState = (String)this.renderEntity.getDataManager().get(BaseGirlEntity.GIRL_HAND_STATES);
      if ("STARTBLOWJOB".equals(handState)) {
         return new ItemStack(Items.IRON_PICKAXE);
      } else {
         return "ANAL_START".equals(handState) ? new ItemStack(Items.GOLD_INGOT, 3) : null;
      }
   }

   /**
    * Main render: detects action changes and clears the cached bone colors
    * (colors depend on the action), stores the render offset for the eye
    * scaling, then delegates to the normal pipeline.
    */
   public void doRenderKobold(KoboldEntity kobold, double x, double y, double z, float entityYaw, float partialTicks) {
      String actionString = (String)kobold.getDataManager().get(AbstractNpcOnlyEntity.CURRENT_ACTION);
      if (kobold.as == null) {
         kobold.as = actionString;
      }

      if (!kobold.as.equals(actionString)) {
         clearBoneColors();
         kobold.as = actionString;
      }

      super.doRenderEntity(kobold, x, y, z, entityYaw, partialTicks);
   }

   /**
    * Name tag: tribed kobolds (custom name tag, not "null") show the tribe
    * name in the kobold color's text color below their display name.
    */
   @Override
   protected void renderNameTag(double x, double y, double z) {
      EntityDataManager dataManager = this.renderEntity.getDataManager();
      String tribeName = (String)dataManager.get(KoboldEntity.aU);
      if ("null".equals(tribeName)) {
         super.renderNameTag(x, y, z);
      } else {
         EyeAndKoboldColor koboldColor = EyeAndKoboldColor.valueOf((String)dataManager.get(KoboldEntity.CURRENT_ACTION));
         tribeName = koboldColor.getTextColor() + " -" + tribeName + "-";
         this.renderLivingLabel(this.renderEntity, this.renderEntity.getEffectiveDisplayName() + tribeName, x, y + this.renderEntity.getScaleFactor(), z, 300);
      }
   }

}
