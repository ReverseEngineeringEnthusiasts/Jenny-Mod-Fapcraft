package com.trolmastercard.sexmod.registry;

import com.trolmastercard.sexmod.common.block.SexFireBlock;
import com.trolmastercard.sexmod.common.item.AlliesLampItem;
import com.trolmastercard.sexmod.common.item.DragonStaffItem;
import com.trolmastercard.sexmod.common.item.GalathCoinItem;
import com.trolmastercard.sexmod.common.item.KoboldEggItem;
import com.trolmastercard.sexmod.common.item.LunaRodItem;
import com.trolmastercard.sexmod.common.item.NpcEditorWandItem;
import com.trolmastercard.sexmod.common.item.TribeEggItem;
import com.trolmastercard.sexmod.common.potion.HornyPotion;

/**
 * Registers the mod's items (girl wand, dragon staff, tribe egg, ...).
 */
public class ModItems {
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
