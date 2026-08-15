package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.block.SexFireBlock;
import com.trolmastercard.sexmod.item.AlliesLampItem;
import com.trolmastercard.sexmod.item.DragonStaffItem;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import com.trolmastercard.sexmod.item.LunaRodItem;
import com.trolmastercard.sexmod.item.NpcEditorWandItem;
import com.trolmastercard.sexmod.item.TribeEggItem;
import com.trolmastercard.sexmod.potion.HornyPotion;

/**
 * Registers the mod's items (girl wand, dragon staff, tribe egg, ...).
 */
public class ItemRegistrationHandler {
   public static void registerAll() {
      HornyPotion.register();
      AlliesLampItem.register();
      DragonStaffItem.register();
      TribeEggItem.register();
      GalathCoinItem.register();
      NpcEditorWandItem.register();
      KoboldEggItem.register();
      SexFireBlock.register();
      LunaRodItem.register();
   }
}
