package com.trolmastercard.sexmod.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * String codecs for the girl part-id lists and custom-model part sets.
 * <p>
 * Extracted from {@link BaseGirlEntity}; every method here is pure (no entity
 * state). The entity keeps its public static methods as delegates so callers
 * in subclasses and renderers are unaffected.
 */
final class GirlPartCodec {
   private GirlPartCodec() {
   }

   /** Joins part ids as {@code "3-4-5-"} (trailing separator, as before). */
   static String encodePartIdList(List<Integer> parts) {
      StringBuilder sb = new StringBuilder();

      for (int part : parts) {
         sb.append(part);
         sb.append("-");
      }

      return sb.toString();
   }

   static List<Integer> decodePartIdList(String code) {
      ArrayList ids = new ArrayList();
      String[] tokens = code.split("-");

      for (String token : tokens) {
         ids.add(Integer.parseInt(token));
      }

      return ids;
   }

   /**
    * Splits a custom-model code on {@code '#'}, dropping empty tokens and the
    * legacy {@code "cross"} marker.
    */
   static HashSet<String> parseCustomParts(String code) {
      String[] parts = code.split("#");
      HashSet set = new HashSet();

      for (String part : parts) {
         if (!"".equals(part) && !"cross".equals(part)) {
            set.add(part);
         }
      }

      return set;
   }

   /** @return {@code count} empty part names */
   static List<String> partNames(int count) {
      ArrayList names = new ArrayList();

      for (int i = 0; i < count; i++) {
         names.add("");
      }

      return names;
   }

   /** Joins a part set as {@code "name1#name2#"}; null/empty become "". */
   static String encodeCustomParts(HashSet<String> parts) {
      if (parts == null) {
         return "";
      }

      if (parts.isEmpty()) {
         return "";
      }

      StringBuilder builder = new StringBuilder();

      for (String part : parts) {
         builder.append(part);
         builder.append("#");
      }

      return builder.toString();
   }
}
