package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.renderer.api.IGirlRenderer;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.vecmath.Vector3f;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;

/**
 * <b>Role.</b> Static helpers for the girl renderers that compute the world
 * offset of "custom" bones (boobs/booty/vagina/fuckhole) so render layers and
 * effects can target them. Per-renderer bone classification is cached in
 * {@code d} to keep the parent-bone walk cheap.
 * <p>
 * <b>Pitfalls.</b> {@link #offsetBonePosition(Vec3d, Vector3f, Vec3d)} is
 * misnamed by the deobf — it does NOT add the offset; it progress-lerps the
 * position toward the skin-color vector ({@code SKIN_COLOR}/
 * {@code SKIN_COLOR_ALT}) by an eased dot-product factor (0.1 max). The
 * renderer's look vector must be refreshed via
 * {@link #updateBoneOffset(EntityLivingBase, float)} every frame before
 * {@link #getBoneWorldPosition(IGirlRenderer, GeoBone, Vec3d, Vector3f)}.
 */
public class BodyParts {
   public static final Vec3d SKIN_COLOR = new Vec3d(0.95, 0.65, 0.85);
   public static final Vec3d SKIN_COLOR_ALT = new Vec3d(0.0, 0.2, 0.3);
   public static final float ALPHA = 0.1F;
   public static final HashSet<String> CUSTOM_PART_BONES = new HashSet<String>() {
      {
         this.add("boobs");
         this.add("booty");
         this.add("vagina");
         this.add("fuckhole");
      }
   };
   protected static HashMap<IGirlRenderer, HashMap<String, Boolean>> d = new HashMap<>();
   public static Vec3d OFFSET_VEC;

   static boolean isCustomBone(IGirlRenderer var0, GeoBone var1) {
      HashMap var2 = d.get(var0);
      if (var2 == null) {
         var2 = new HashMap();
         boolean var6 = var0.hasParentBone(var0.getBlacklistedBones(), var1);
         var2.put(var1.getName(), var6);
         d.put(var0, var2);
         return var6;
      } else {
         Boolean var3 = (Boolean)var2.get(var1.getName());
         if (var3 == null) {
            var3 = var0.hasParentBone(var0.getBlacklistedBones(), var1);
            var2.put(var1.getName(), var3);
            d.put(var0, var2);
            return var3;
         } else {
            return var3;
         }
      }
   }

   public static Vec3d getBoneWorldPosition(IGirlRenderer var0, GeoBone var1, Vec3d var2, Vector3f var3) {
      return !isCustomBone(var0, var1) ? var2 : offsetBonePosition(var2, var3, OFFSET_VEC);
   }

   public static Vec3d offsetBonePosition(Vec3d var0, Vector3f var1, Vec3d var2) {
      double var3 = VectorMath.dotProduct(var1, var2);
      double var5 = RotationHelper.easeInOutQuad(Math.abs(var3));
      var5 *= 0.1F;
      return RotationHelper.lerpVec3dDouble(var0, var3 > 0.0 ? SKIN_COLOR : SKIN_COLOR_ALT, var5);
   }

   public static void updateBoneOffset(EntityLivingBase var0, float var1) {
      OFFSET_VEC = WorldUtils.getEntityLookVector(var0, var1);
   }

   public static void updateCustomBones(List<IBone> var0, HashSet<String> var1, IGirlRenderer var2) {
      if (d.get(var2) == null) {
         HashMap var3 = new HashMap();

         for (IBone var5 : var0) {
            var3.put(var5.getName(), var2.hasParentBone(var1, (GeoBone)var5));
         }

         d.put(var2, var3);
      }
   }

}
