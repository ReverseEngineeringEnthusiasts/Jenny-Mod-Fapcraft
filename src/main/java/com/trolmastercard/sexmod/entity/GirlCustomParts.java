package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Custom body-part lists and the {@code customPartsData} table that drives the
 * sliders: id list (APPARANCE_DNA for NPC-only girls), per-part names and
 * per-part current values.
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; the entity keeps its methods
 * as delegates and its {@code customPartsData} field, which subclasses read
 * directly. {@code getBasePartIdList}/{@code getCustomPartIdList} are called
 * back through the girl so the per-girl overrides still dispatch.
 */
final class GirlCustomParts {
   private GirlCustomParts() {
   }

   static boolean isPartEnabled(BaseGirlEntity girl, int index) {
      ArrayList parts = girl.getCustomPartIdList();
      return parts.size() - 1 < index ? false : (Integer)parts.get(index) == 101;
   }

   static void setCustomPartList(BaseGirlEntity girl, List<Integer> parts) {
      if (girl instanceof AbstractNpcOnlyEntity || girl instanceof AbstractKoboldPlayerEntity) {
         StringBuilder builder = new StringBuilder();

         for (int partId : parts) {
            AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
         }

         girl.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, builder.toString());
      }
   }

   static String getCustomPartListCode(BaseGirlEntity girl) {
      return !(girl instanceof AbstractNpcOnlyEntity) && !(girl instanceof AbstractKoboldPlayerEntity)
         ? ""
         : (String)girl.entityDataManager.get(AbstractNpcOnlyEntity.APPEARANCE_DNA);
   }

   static void setCustomPartListCode(BaseGirlEntity girl, String code) {
      if (girl instanceof AbstractNpcOnlyEntity || girl instanceof AbstractKoboldPlayerEntity) {
         girl.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, code);
      }
   }

   static List<Integer> getAllPartIdsForGirl(UUID uuid) {
      BaseGirlEntity girl = Main.proxy instanceof ClientProxy ? BaseGirlEntity.getClientGirlEntity(uuid) : BaseGirlEntity.getServerGirlEntity(uuid);
      // NULL-GUARD: for a freshly constructed / unregistered entity the registry
      // lookup returns null — the caller falls back to this-girl data instead of NPE.
      if (girl == null) return new ArrayList<>();
      ArrayList parts = new ArrayList<>(girl.getBasePartIdList());
      if (girl instanceof AbstractNpcOnlyEntity || girl instanceof AbstractKoboldPlayerEntity) {
         Object dna = girl.getDataManager().get(AbstractNpcOnlyEntity.APPEARANCE_DNA);
         parts.addAll(BaseGirlEntity.decodePartIdList(dna == null ? "" : (String) dna));
      }

      return parts;
   }

   static List<Entry<BoneType, Entry<List<String>, Integer>>> buildCustomPartsData(BaseGirlEntity girl, UUID uuid) {
      if (girl.customPartsData != null) {
         return girl.customPartsData;
      }

      ArrayList parts = girl.getCustomPartIdList();
      if (parts.isEmpty()) {
         girl.customPartsData = new ArrayList<>();
         return girl.customPartsData;
      }

      // Read the current values from THIS entity (works for preview girls that are
      // not in any registry). Fall back gracefully if allParts is short.
      List allParts = currentPartValues(girl);
      ArrayList data = new ArrayList();
      for (int i = 0; i < parts.size(); i++) {
         int cur = (allParts != null && i < allParts.size() && allParts.get(i) instanceof Integer)
            ? ((Integer) allParts.get(i)).intValue()
            : 0;
         data.add(new SimpleEntry<>(BoneType.GIRL_SPECIFIC, new SimpleEntry<>(GirlPartCodec.partNames((Integer)parts.get(i)), cur)));
      }

      girl.customPartsData = data;
      return data;
   }

   /** Current part values directly from the girl's own data (no registry needed). */
   private static List<Integer> currentPartValues(BaseGirlEntity girl) {
      ArrayList<Integer> out = new ArrayList<>();
      try {
         out.addAll(girl.getBasePartIdList());
      } catch (Throwable t) { }
      try {
         if (girl instanceof AbstractNpcOnlyEntity || girl instanceof AbstractKoboldPlayerEntity) {
            Object dna = girl.getDataManager().get(AbstractNpcOnlyEntity.APPEARANCE_DNA);
            out.addAll(BaseGirlEntity.decodePartIdList(dna == null ? "" : (String) dna));
         }
      } catch (Throwable t) { }
      return out;
   }

   static void setCustomPartsData(BaseGirlEntity girl, List<Entry<BoneType, Entry<List<String>, Integer>>> data) {
      girl.customPartsData = data;
   }

   static void setCustomPartValue(BaseGirlEntity girl, int index, int value) {
      if (girl.customPartsData != null) {
         if (girl.customPartsData.size() - 1 >= index) {
            Entry entry = girl.customPartsData.get(index);
            ((Entry)entry.getValue()).setValue(value);
            girl.customPartsData.set(index, entry);
         }
      }
   }

   /**
    * Removes server-whitelisted custom parts from the girl's model code and
    * persists the filtered set (SERVER side; respects ServerWhitelistManager).
    */
   static void updateCustomModelParts(BaseGirlEntity girl) {
      if (ServerWhitelistManager.isLoaded) {
         HashSet parts = girl.getCustomPartsSet();
         NpcType type = NpcType.getNpcType(girl);
         HashSet allowed = new HashSet();
         String group = ServerWhitelistManager.getCurrentGroup();

         for (String part : (java.util.Collection<String>) (parts) ) {
            if (!"".equals(ServerWhitelistManager.getPartName(part, group))) {
               allowed.add(part);
            } else {
               HashSet allowedTypes = ServerWhitelistManager.getAllowedNpcTypes(part);
               if (allowedTypes == null) {
                  allowed.add(part);
               } else if (!allowedTypes.isEmpty() && !allowedTypes.contains(type)) {
                  allowed.add(part);
               }
            }
         }

         if (!allowed.isEmpty()) {
            parts.removeAll(allowed);
            girl.setCustomModelCode(BaseGirlEntity.encodeCustomParts(parts));
         }
      }
   }
}
