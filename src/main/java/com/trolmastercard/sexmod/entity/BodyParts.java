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

   static boolean isCustomBone(IGirlRenderer renderer, GeoBone bone) {
      HashMap cache = d.get(renderer);
      if (cache == null) {
         cache = new HashMap();
         boolean custom = renderer.hasParentBone(renderer.getBlacklistedBones(), bone);
         cache.put(bone.getName(), custom);
         d.put(renderer, cache);
         return custom;
      } else {
         Boolean cached = (Boolean)cache.get(bone.getName());
         if (cached == null) {
            cached = renderer.hasParentBone(renderer.getBlacklistedBones(), bone);
            cache.put(bone.getName(), cached);
            d.put(renderer, cache);
            return cached;
         } else {
            return cached;
         }
      }
   }

   public static Vec3d getBoneWorldPosition(IGirlRenderer renderer, GeoBone bone, Vec3d worldPos, Vector3f rotation) {
      return !isCustomBone(renderer, bone) ? worldPos : offsetBonePosition(worldPos, rotation, OFFSET_VEC);
   }

   public static Vec3d offsetBonePosition(Vec3d worldPos, Vector3f rotation, Vec3d offset) {
      double dot = VectorMath.dotProduct(rotation, offset);
      double fade = RotationHelper.easeInOutQuad(Math.abs(dot));
      fade *= 0.1F;
      return RotationHelper.lerpVec3dDouble(worldPos, dot > 0.0 ? SKIN_COLOR : SKIN_COLOR_ALT, fade);
   }

   public static void updateBoneOffset(EntityLivingBase entity, float partialTicks) {
      OFFSET_VEC = WorldUtils.getEntityLookVector(entity, partialTicks);
   }

   public static void updateCustomBones(List<IBone> bones, HashSet<String> blacklisted, IGirlRenderer renderer) {
      if (d.get(renderer) == null) {
         HashMap customBones = new HashMap();

         for (IBone bone : bones) {
            customBones.put(bone.getName(), renderer.hasParentBone(blacklisted, (GeoBone)bone));
         }

         d.put(renderer, customBones);
      }
   }

}
