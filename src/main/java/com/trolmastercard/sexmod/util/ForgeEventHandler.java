package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.PositionData;
import com.trolmastercard.sexmod.block.SexFireBlock;
import com.trolmastercard.sexmod.client.GirlCameraHelper;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.ClothingScreen;
import com.trolmastercard.sexmod.client.gui.EscapeMinigameHud;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.CummyEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.KoboldEggProjectileEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.entity.ai.GirlFollowGoal;
import com.trolmastercard.sexmod.item.AlliesLampItem;
import com.trolmastercard.sexmod.item.DragonStaffItem;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import com.trolmastercard.sexmod.item.LunaRodItem;
import com.trolmastercard.sexmod.item.NpcEditorWandItem;
import com.trolmastercard.sexmod.item.WinchesterItem;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.worldgen.ConfigWorldGenHandler;
import java.io.File;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ForgeEventHandler {
   public static void registerB(boolean var0) {
      MinecraftForge.EVENT_BUS.register(new GirlCombatProtection());
      MinecraftForge.EVENT_BUS.register(new LivingDeathHandler());
      MinecraftForge.EVENT_BUS.register(new PlayerIds());
      MinecraftForge.EVENT_BUS.register(new HornyPotion());
      MinecraftForge.EVENT_BUS.register(new DamageCalculation());
      MinecraftForge.EVENT_BUS.register(new KoboldEggProjectileEntity.a());
      MinecraftForge.EVENT_BUS.register(new GirlFollowGoal.a());
      MinecraftForge.EVENT_BUS.register(AlliesLampItem.ALLIES_LAMP);
      MinecraftForge.EVENT_BUS.register(DragonStaffItem.DRAGON_STAFF);
      MinecraftForge.EVENT_BUS.register(NpcEditorWandItem.EDITOR_WAND);
      MinecraftForge.EVENT_BUS.register(new LunaRodItem());
      MinecraftForge.EVENT_BUS.register(new PlayerGirlEvents());
      MinecraftForge.EVENT_BUS.register(new LunaEntity.a());
      MinecraftForge.EVENT_BUS.register(new GirlBedInteraction());
      MinecraftForge.EVENT_BUS.register(SexFireBlock.FIRE);
      MinecraftForge.EVENT_BUS.register(new KoboldEntity.c());
      MinecraftForge.EVENT_BUS.register(new DragonStaffItem.a());
      MinecraftForge.EVENT_BUS.register(new KoboldManager.b("tribes"));
      MinecraftForge.EVENT_BUS.register(new KoboldEggItem());
      MinecraftForge.EVENT_BUS.register(new GoblinFirstPersonRenderer());
      MinecraftForge.EVENT_BUS.register(new GoblinEntity.c());
      MinecraftForge.EVENT_BUS.register(new GoblinPlayerEntity.a());
      MinecraftForge.EVENT_BUS.register(new AlliesLampItem.a());
      MinecraftForge.EVENT_BUS.register(new DebugMode());
      MinecraftForge.EVENT_BUS.register(new GalathEntity.a());
      MinecraftForge.EVENT_BUS.register(new GirlSavedData());
      MinecraftForge.EVENT_BUS.register(GalathCoinItem.GALATH_COIN);
      MinecraftForge.EVENT_BUS.register(WinchesterItem.WINCHESTER_ITEM);
      MinecraftForge.EVENT_BUS.register(new BeeWorldData());
      MinecraftForge.EVENT_BUS.register(new AllieWorldData());
      MinecraftForge.EVENT_BUS.register(new GirlWorldData());
      MinecraftForge.EVENT_BUS.register(ConfigWorldGenHandler.getInstance());
      MinecraftForge.EVENT_BUS.register(new ManglelieEntity.b());
      MinecraftForge.EVENT_BUS.register(new NameTagInteractHandler());
      if (var0) {
         registerDebugWindow();
      }
   }

   @SideOnly(Side.CLIENT)
   static void registerDebugWindow() {
      if (shouldShowDebugWindow()) {
         MinecraftForge.EVENT_BUS.register(new DebugWindow());
      } else {
         DebugWindow2.isVisible = false;
      }

      MinecraftForge.EVENT_BUS.register(new HornyMeterHud());
      MinecraftForge.EVENT_BUS.register(new BeeScreen());
      MinecraftForge.EVENT_BUS.register(new HandlePlayerMovement());
      MinecraftForge.EVENT_BUS.register(new PositionData());
      MinecraftForge.EVENT_BUS.register(new GuiOpenHandler());
      MinecraftForge.EVENT_BUS.register(new InHandMapRenderer());
      MinecraftForge.EVENT_BUS.register(new GirlCameraHelper());
      MinecraftForge.EVENT_BUS.register(new com.trolmastercard.sexmod.client.SexSceneKeyHandler());
      MinecraftForge.EVENT_BUS.register(new GenderSwapScreen());
      MinecraftForge.EVENT_BUS.register(new PlayerAllieRenderer.a());
      MinecraftForge.EVENT_BUS.register(new StructureMarkerRenderer());
      MinecraftForge.EVENT_BUS.register(new DeprecatedCheckForUpdates());
      MinecraftForge.EVENT_BUS.register(new ClothingScreen.b());
      MinecraftForge.EVENT_BUS.register(new ServerWhitelistManager.a());
      MinecraftForge.EVENT_BUS.register(new EscapeMinigameHud());
      MinecraftForge.EVENT_BUS.register(new CummyEntity());
      MinecraftForge.EVENT_BUS.register(new GalathFlightHud());
   }

   static boolean shouldShowDebugWindow() {
      File var0 = new File("sexmod/dontAskAgain");
      var0.getParentFile().mkdirs();
      return !var0.exists();
   }

}
