package com.trolmastercard.sexmod.reskin;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Checks the animation-JSON repair applied before GeckoLib's molang parse. */
public class AnimationJsonSanitizerTest {
   @Test
   public void repairsExpressionTyposFromTheOriginalJar() {
      String json = "{\"animations\":{\"a\":{\"bones\":{\"b\":{\"rotation\":{\"0.2\":{"
         + "\"vector\":[\"`-11\",\"-36.4659 + size * \",\"NaN-1*\"]}}}}}}}";

      String fixed = ReskinApplier.prepareAnimationJson(json);

      assertTrue(fixed.contains("\"-11\""));
      assertTrue(fixed.contains("\"-36.4659 + size * 180\""));
      assertTrue(fixed.contains("\"NaN-1\""));
      assertFalse(fixed.contains("`"));
      assertFalse(fixed.contains("NaN-1*"));
   }

   @Test
   public void rejectsDecoysAndNonAnimationJson() {
      assertNull(ReskinApplier.prepareAnimationJson(null));
      assertNull(ReskinApplier.prepareAnimationJson("{\"minecraft:geometry\":{}}"));
      assertNull(ReskinApplier.prepareAnimationJson("{\"animations\":{},\"_pad\":\"fake\"}"));
   }
}
