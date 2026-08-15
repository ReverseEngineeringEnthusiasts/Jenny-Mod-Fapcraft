package com.trolmastercard.sexmod.client.model.api;
/**
 * Model metadata contract for girl models: named bone lists for the outfit
 * pieces (head armor, attachments, top/bottom armor, shoes, ...). Defaults to
 * empty; models that support the customization/outfit system override the
 * relevant accessors with the actual bone names.
 */
public interface IGirlModelInfo {
   default String[] HeadArmor() {
      return new String[0];
   }

   default String[] Attachments() {
      return new String[0];
   }

   default String[] TopArmor() {
      return new String[0];
   }

   default String[] Top() {
      return new String[0];
   }

   default String[] BottomArmor() {
      return new String[0];
   }

   default String[] Bottom() {
      return new String[0];
   }

   default String[] ShoesArmor() {
      return new String[0];
   }

   default String[] Shoes() {
      return new String[0];
   }
}
