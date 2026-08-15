package com.trolmastercard.sexmod.client.renderer.api;

import com.trolmastercard.sexmod.entity.BodyParts;
import java.util.HashSet;
import software.bernie.geckolib3.geo.render.built.GeoBone;

/**
 * Contract implemented by girl renderers for geckolib bone visibility control.
 * Custom parts (dildos, etc.) are attached to {@link BodyParts#CUSTOM_PART_BONES};
 * the renderer must not render a bone (nor its children) when that bone — or an
 * {@code armor*} ancestor — is blacklisted.
 * <p>
 * CLIENT-side render thread only.
 */
public interface IGirlRenderer {
   /**
    * The set of bones that must never be rendered (custom part anchor bones).
    */
   default HashSet<String> getBlacklistedBones() {
      return BodyParts.CUSTOM_PART_BONES;
   }

   /**
    * Walks {@code bone}'s parent chain up to the root.
    *
    * @return {@code false} if any ancestor is a blacklisted custom-part bone or
    *         an armor bone; {@code true} otherwise (bone may be rendered)
    */
   default boolean hasParentBone(HashSet<String> blacklistedBones, GeoBone bone) {
      while (bone.parent != null) {
         String boneName = bone.getName();
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
}
