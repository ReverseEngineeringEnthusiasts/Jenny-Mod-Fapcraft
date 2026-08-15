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
import javax.vecmath.Vector3f;
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
   Vector3f renderOffset;

   public KoboldRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   /**
    * Bone tint by name: hide-list bones use the kobold color's main color,
    * show-list bones its secondary color, iris bones encode the action-target
    * position as RGB; everything else stays white.
    */
   @Override
   protected Vec3i getBoneColor(String var1) {
      EntityDataManager var2 = this.renderEntity.getDataManager();
      EyeAndKoboldColor var3 = EyeAndKoboldColor.valueOf((String)var2.get(KoboldEntity.CURRENT_ACTION));
      BlockPos var4 = (BlockPos)var2.get(KoboldEntity.ACTION_TARGET_POS);
      if (hideBones.contains(var1)) {
         return var3.getMainColor();
      } else if (showBones.contains(var1)) {
         return var3.getSecondaryColor();
      } else {
         return (Vec3i)(!"irisR".equals(var1) && !"irisL".equals(var1) ? defaultColor : var4);
      }
   }

   /**
    * Held item by action: axe or pickaxe while mining (flag chooses), sword
    * while attacking or when the guard flag is set; otherwise the default
    * stack.
    */
   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack var1) {
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
            return var1;
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
   public void renderCustomBones(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6, double var7) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String var9 = var2.getName();
         if ("blowOpening".equals(var9)) {
            var7 = 0.0;
         }

         if ("mouth".equals(var9)) {
            String[] var10 = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
            int var11 = Integer.parseInt(var10[7]);
            if (var11 == 1) {
               var7 = -0.078125;
            }
         }

         super.renderCustomBones(var1, var2, var3, var4, var5, var6, var7);
      }
   }

   /**
    * Eye-squint pair: the left eye shrinks by the squint value (from the
    * player-kobold's data manager) — the right eye compensates with the
    * inverse scale so the face stays closed/open correctly.
    */
   @Override
   protected void renderLeftEye() {
      float var1 = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      GlStateManager.scale(1.0F - var1, 1.0F - var1, 1.0F - var1);
   }

   @Override
   protected void renderRightEye() {
      float var1 = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      double var2 = 1.0 / (1.0 - var1);
      GlStateManager.scale(var2, var2, var2);
   }

   @Override
   protected ItemStack getPaymentItemStack() {
      String var1 = (String)this.renderEntity.getDataManager().get(BaseGirlEntity.GIRL_HAND_STATES);
      if ("STARTBLOWJOB".equals(var1)) {
         return new ItemStack(Items.IRON_PICKAXE);
      } else {
         return "ANAL_START".equals(var1) ? new ItemStack(Items.GOLD_INGOT, 3) : null;
      }
   }

   /**
    * Main render: detects action changes and clears the cached bone colors
    * (colors depend on the action), stores the render offset for the eye
    * scaling, then delegates to the normal pipeline.
    */
   public void doRenderKobold(KoboldEntity var1, double var2, double var4, double var6, float var8, float var9) {
      String var10 = (String)var1.getDataManager().get(AbstractNpcOnlyEntity.CURRENT_ACTION);
      if (var1.as == null) {
         var1.as = var10;
      }

      if (!var1.as.equals(var10)) {
         clearBoneColors();
         var1.as = var10;
      }

      this.renderOffset = new Vector3f((float)var2, (float)var4, (float)var6);
      super.doRenderEntity(var1, var2, var4, var6, var8, var9);
   }

   /**
    * Name tag: tribed kobolds (custom name tag, not "null") show the tribe
    * name in the kobold color's text color below their display name.
    */
   @Override
   protected void renderNameTag(double var1, double var3, double var5) {
      EntityDataManager var7 = this.renderEntity.getDataManager();
      String var8 = (String)var7.get(KoboldEntity.aU);
      if ("null".equals(var8)) {
         super.renderNameTag(var1, var3, var5);
      } else {
         EyeAndKoboldColor var9 = EyeAndKoboldColor.valueOf((String)var7.get(KoboldEntity.CURRENT_ACTION));
         var8 = var9.getTextColor() + " -" + var8 + "-";
         this.renderLivingLabel(this.renderEntity, this.renderEntity.getEffectiveDisplayName() + var8, var1, var3 + this.renderEntity.getScaleFactor(), var5, 300);
      }
   }

}
