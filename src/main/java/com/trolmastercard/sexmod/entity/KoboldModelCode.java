package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.util.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * Custom-model code ("DNA") for {@link KoboldEntity}: builds the padded
 * letter/number segment string, maps the editor's part-id list onto the
 * data-manager keys, and answers the atlas offsets of the elephant/eye parts.
 * <p>
 * Part ids 0..2 are not DNA segments — they are body size (0), eye colour (1)
 * and main colour (2) and are written to {@code aE} / {@code CURRENT_ACTION} /
 * {@code ACTION_TARGET_POS}; every other index is appended as a padded number.
 * <b>Pitfalls.</b> The order of the first three ids is baked into saves through
 * {@code APPEARANCE_DNA}; {@code setCustomPartList} flipping {@code aA} when the
 * eye colour changes is what stops the tribe colour sync from overwriting a
 * manual edit.
 */
final class KoboldModelCode {
   private KoboldModelCode() {
   }

   static String buildModelCodeDNA(KoboldEntity entity, StringBuilder builder) {
      KoboldEntity.appendPaddedLetter(builder, 8);
      KoboldEntity.appendPaddedLetter(builder, 3);
      KoboldEntity.appendRandomGene(builder);
      KoboldEntity.appendRandomGene(builder);
      KoboldEntity.appendPaddedNumber(builder, 2);
      KoboldEntity.appendPaddedNumber(builder, 2);
      KoboldEntity.appendPaddedNumber(builder, 1);
      KoboldEntity.appendPaddedNumber(builder, 1);
      return builder.toString();
   }

   static ArrayList<Integer> getCustomPartIdList(KoboldEntity entity) {
      return new ArrayList<Integer>() {
         {
            this.add(101);
            this.add(EyeAndKoboldColor.values().length);
            this.add(EyeAndKoboldColor.values().length);
            this.add(8);
            this.add(3);
            this.add(101);
            this.add(101);
            this.add(3);
            this.add(3);
            this.add(4);
            this.add(2);
         }
      };
   }

   static ArrayList<Integer> getBasePartIdList(KoboldEntity entity) {
      ArrayList parts = new ArrayList();
      parts.add(Math.round((Float)entity.getDataManager().get(KoboldEntity.aE) * 100.0F / 0.25F));
      parts.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((String)entity.getDataManager().get(KoboldEntity.CURRENT_ACTION))));
      parts.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((Vec3i)entity.getDataManager().get(KoboldEntity.ACTION_TARGET_POS))));
      return parts;
   }

   static void setCustomPartList(KoboldEntity entity, List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < parts.size(); i++) {
         int partId = (Integer)parts.get(i);
         switch (i) {
            case 0:
               entity.getDataManager().set(KoboldEntity.aE, partId / 100.0F * 0.25F);
               break;
            case 1:
               String currentColor = (String)entity.getDataManager().get(KoboldEntity.CURRENT_ACTION);
               String newColor = EyeAndKoboldColor.values()[partId].toString();
               if (!newColor.equals(currentColor)) {
                  entity.aA = true;
               }

               entity.getDataManager().set(KoboldEntity.CURRENT_ACTION, newColor);
               break;
            case 2:
               entity.getDataManager().set(KoboldEntity.ACTION_TARGET_POS, new BlockPos(EyeAndKoboldColor.values()[partId].getMainColor()));
               break;
            default:
               KoboldEntity.appendPaddedNumber(builder, partId);
         }
      }

      entity.getDataManager().set(KoboldEntity.APPEARANCE_DNA, builder.toString());
      KoboldRenderer.clearBoneColors();
   }

   static void updateModelCodeDNA(KoboldEntity entity) {
      if (entity.customPartsData != null) {
         String prevDna = (String)entity.getDataManager().get(KoboldEntity.APPEARANCE_DNA);
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < entity.customPartsData.size(); i++) {
            Entry entry = entity.customPartsData.get(i);
            int partId = (Integer)((Entry)entry.getValue()).getValue();
            switch (i) {
               case 0:
                  entity.getDataManager().set(KoboldEntity.aE, partId / 100.0F * 0.25F);
                  break;
               case 1:
                  entity.getDataManager().set(KoboldEntity.CURRENT_ACTION, EyeAndKoboldColor.values()[partId].toString());
                  break;
               case 2:
                  entity.getDataManager().set(KoboldEntity.ACTION_TARGET_POS, new BlockPos(EyeAndKoboldColor.values()[partId].getMainColor()));
                  break;
               default:
                  KoboldEntity.appendPaddedNumber(builder, partId);
            }
         }

         entity.getDataManager().set(KoboldEntity.APPEARANCE_DNA, builder.toString());
         String newDna = (String)entity.getDataManager().get(KoboldEntity.APPEARANCE_DNA);
         if (!newDna.equals(prevDna)) {
            Main.LOGGER.info("[sexmod-pack] KoboldEntity.updateModelCodeDNA '" + prevDna + "' -> '" + newDna + "'");
         }
         KoboldRenderer.clearBoneColors();
      }
   }

   static Point2D getModelPartByIndex(KoboldEntity entity, int index) {
      switch (index) {
         case 0:
            return new Point2D(160, 0);
         case 1:
            return new Point2D(180, 0);
         case 2:
            return new Point2D(200, 0);
         case 3:
            return new Point2D(220, 0);
         case 4:
            return new Point2D(227, 20);
         case 5:
            return new Point2D(140, 40);
         case 6:
            return new Point2D(160, 40);
         case 7:
            return new Point2D(180, 40);
         case 8:
            return new Point2D(227, 40);
         case 9:
            return new Point2D(0, 130);
         case 10:
            return new Point2D(20, 130);
         default:
            return Point2D.ZERO;
      }
   }
}
