package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.EyeColor;
import com.trolmastercard.sexmod.util.HairColor;
import com.trolmastercard.sexmod.util.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * Appearance/DNA packing for {@link GoblinEntity}: {@code APPEARANCE_DNA} is a
 * run of zero-padded part ids (model part sizes, hair/skin/eye bounds) with
 * the trailing segment (index 9) holding the model part index. Every setter
 * here rewrites the whole code and resets the renderer bone colours, because
 * the renderer caches per-bone colours keyed on that code.
 */
final class GoblinAppearance {
   private GoblinAppearance() {
   }

   static String buildModelCodeDNA(StringBuilder builder) {
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 8);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 8);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, HairColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, SkinColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, EyeColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 0);
      return builder.toString();
   }

   static ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<Integer>() {
         {
            this.add(4);
            this.add(3);
            this.add(3);
            this.add(16);
            this.add(16);
            this.add(6);
            this.add(HairColor.values().length);
            this.add(SkinColor.values().length);
            this.add(EyeColor.values().length);
         }
      };
   }

   static List<Integer> getCustomPartExtraIdList() {
      return Collections.singletonList(2);
   }

   static Point2D getModelPartByIndex(int index) {
      switch (index) {
         case 0:
            return new Point2D(40, 130);
         case 1:
            return new Point2D(60, 130);
         case 2:
            return new Point2D(80, 130);
         case 3:
            return new Point2D(100, 130);
         case 4:
            return new Point2D(120, 130);
         case 5:
            return new Point2D(140, 130);
         case 6:
            return new Point2D(160, 130);
         case 7:
            return new Point2D(180, 130);
         case 8:
            return new Point2D(200, 0);
         case 9:
            return new Point2D(200, 130);
         default:
            return Point2D.ZERO;
      }
   }

   static void setCustomPartList(GoblinEntity goblin, List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int partId : parts) {
         AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
      }

      AbstractNpcOnlyEntity.appendPaddedNumber(builder, Integer.parseInt(AbstractNpcOnlyEntity.getModelCodeParts(goblin)[9]));
      goblin.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, builder.toString());
      if (Main.proxy instanceof ClientProxy) {
         GoblinRenderer.clearBoneColors();
      }
   }

   static void updateModelCodeDNA(GoblinEntity goblin) {
      if (goblin.customPartsData != null) {
         StringBuilder builder = new StringBuilder();

         for (Entry entry : goblin.customPartsData) {
            int partId = (Integer)((Entry)entry.getValue()).getValue();
            AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
         }

         AbstractNpcOnlyEntity.appendPaddedNumber(builder, Integer.parseInt(AbstractNpcOnlyEntity.getModelCodeParts(goblin)[9]));
         goblin.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, builder.toString());
         GoblinRenderer.clearBoneColors();
      }
   }

   static String buildModelCodeDNA(StringBuilder builder, int partIndex) {
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, HairColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, partIndex);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, EyeColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 0);
      return builder.toString();
   }
}
