package com.trolmastercard.sexmod.common.event;

import com.trolmastercard.sexmod.common.entity.base.GirlBedInteraction;

import com.trolmastercard.sexmod.common.world.data.AllieWorldData;
import com.trolmastercard.sexmod.common.world.data.BeeWorldData;
import com.trolmastercard.sexmod.common.world.data.OwnershipSavedData;
import com.trolmastercard.sexmod.common.world.data.CustomModelCodeData;
import com.trolmastercard.sexmod.common.entity.player.PlayerGirlLoginHandler;
import com.trolmastercard.sexmod.common.model.ServerWhitelistManager;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldManager;
import com.trolmastercard.sexmod.common.combat.DamageCalculation;
import com.trolmastercard.sexmod.common.combat.GirlCombatProtection;
import com.trolmastercard.sexmod.client.event.PlayerMovementController;
import com.trolmastercard.sexmod.client.debug.DebugMode;
import com.trolmastercard.sexmod.client.debug.AgeWarningTrigger;
import com.trolmastercard.sexmod.client.debug.AgeWarningWindow;
import com.trolmastercard.sexmod.common.update.DeprecatedCheckForUpdates;
import com.trolmastercard.sexmod.client.event.GoblinFirstPersonHandler;
import com.trolmastercard.sexmod.client.render.player.PlayerAllieRenderer;
import com.trolmastercard.sexmod.client.render.effects.InHandMapRenderer;
import com.trolmastercard.sexmod.client.render.effects.StructureMarkerRenderer;
import com.trolmastercard.sexmod.client.gui.GuiOpenHandler;

import com.trolmastercard.sexmod.client.event.SexSceneKeyHandler;

import com.trolmastercard.sexmod.client.reskin.ReskinManager;

import com.trolmastercard.sexmod.client.render.effects.SceneCameraManager;
import com.trolmastercard.sexmod.common.block.SexFireBlock;
import com.trolmastercard.sexmod.client.render.effects.GirlCameraHelper;
import com.trolmastercard.sexmod.client.gui.screen.SceneFadeScreen;
import com.trolmastercard.sexmod.client.gui.screen.ClothingScreen;
import com.trolmastercard.sexmod.client.gui.hud.EscapeMinigameHud;
import com.trolmastercard.sexmod.client.gui.hud.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.screen.GenderSwapScreen;
import com.trolmastercard.sexmod.client.gui.hud.HornyMeterHud;
import com.trolmastercard.sexmod.client.render.effects.CumTrailOverlay;
import com.trolmastercard.sexmod.common.entity.galath.GalathEntity;
import com.trolmastercard.sexmod.common.entity.goblin.GoblinEventHandler;
import com.trolmastercard.sexmod.common.entity.goblin.GoblinPlayerEntity;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldEggProjectileEntity;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldEntity;
import com.trolmastercard.sexmod.common.entity.girl.LunaEntity;
import com.trolmastercard.sexmod.common.entity.girl.ManglelieEntity;
import com.trolmastercard.sexmod.common.entity.ai.GirlFollowGoal;
import com.trolmastercard.sexmod.common.item.AlliesLampItem;
import com.trolmastercard.sexmod.common.item.DragonStaffItem;
import com.trolmastercard.sexmod.common.item.GalathCoinItem;
import com.trolmastercard.sexmod.common.item.KoboldEggItem;
import com.trolmastercard.sexmod.common.item.LunaRodItem;
import com.trolmastercard.sexmod.common.item.NpcEditorWandItem;
import com.trolmastercard.sexmod.common.item.WinchesterItem;
import com.trolmastercard.sexmod.common.potion.HornyPotion;
import com.trolmastercard.sexmod.common.world.gen.ConfigWorldGenHandler;
import java.io.File;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Central Forge event registration hub (entity spawn, ticks, interactions).
 * RegisterB(true/false) sets up the event bus subscriptions; the mod's
 * handlers (HandlePlayerMovement, GirlCameraHelper, HornyMeterHud,
 * GirlSavedData, PlayerIds, ...) are registered here.
 */
public class ForgeEventHandler {
   public static void registerAll(boolean showDebug) {
      if (showDebug) {
         // client-only reskin manager (lazy pack import + reload persistence)
         MinecraftForge.EVENT_BUS.register(com.trolmastercard.sexmod.client.reskin.ReskinManager.INSTANCE);
      }
      MinecraftForge.EVENT_BUS.register(new GirlCombatProtection());
      MinecraftForge.EVENT_BUS.register(new LivingDeathHandler());
      MinecraftForge.EVENT_BUS.register(new PlayerGirlLoginHandler());
      MinecraftForge.EVENT_BUS.register(new HornyPotion());
      MinecraftForge.EVENT_BUS.register(new DamageCalculation());
      MinecraftForge.EVENT_BUS.register(new KoboldEggProjectileEntity.EnderTeleportHandler());
      MinecraftForge.EVENT_BUS.register(new GirlFollowGoal.DownedGirlEvents());
      MinecraftForge.EVENT_BUS.register(AlliesLampItem.ALLIES_LAMP);
      MinecraftForge.EVENT_BUS.register(DragonStaffItem.DRAGON_STAFF);
      MinecraftForge.EVENT_BUS.register(NpcEditorWandItem.EDITOR_WAND);
      MinecraftForge.EVENT_BUS.register(new LunaRodItem());
      MinecraftForge.EVENT_BUS.register(new PlayerGirlEvents());
      MinecraftForge.EVENT_BUS.register(new LunaEntity.CreeperAvoidanceHandler());
      MinecraftForge.EVENT_BUS.register(new GirlBedInteraction());
      MinecraftForge.EVENT_BUS.register(SexFireBlock.FIRE);
      MinecraftForge.EVENT_BUS.register(new KoboldEntity.KoboldTribeEvents());
      MinecraftForge.EVENT_BUS.register(new DragonStaffItem.RightClickHandler());
      MinecraftForge.EVENT_BUS.register(new KoboldManager.TribeWorldSavedData("tribes"));
      MinecraftForge.EVENT_BUS.register(new KoboldEggItem());
      MinecraftForge.EVENT_BUS.register(new GoblinFirstPersonHandler());
      MinecraftForge.EVENT_BUS.register(new GoblinEventHandler());
      MinecraftForge.EVENT_BUS.register(new GoblinPlayerEntity.GoblinCarryEvents());
      MinecraftForge.EVENT_BUS.register(new AlliesLampItem.InteractionHandler());
      MinecraftForge.EVENT_BUS.register(new DebugMode());
      MinecraftForge.EVENT_BUS.register(new GalathEntity.GalathLifecycleEvents());
      MinecraftForge.EVENT_BUS.register(new OwnershipSavedData());
      MinecraftForge.EVENT_BUS.register(GalathCoinItem.GALATH_COIN);
      MinecraftForge.EVENT_BUS.register(WinchesterItem.WINCHESTER_ITEM);
      MinecraftForge.EVENT_BUS.register(new BeeWorldData());
      MinecraftForge.EVENT_BUS.register(new AllieWorldData());
      MinecraftForge.EVENT_BUS.register(new CustomModelCodeData());
      MinecraftForge.EVENT_BUS.register(ConfigWorldGenHandler.getInstance());
      MinecraftForge.EVENT_BUS.register(new ManglelieEntity.ArrowImpactHandler());
      MinecraftForge.EVENT_BUS.register(new NameTagInteractHandler());
      if (showDebug) {
         registerDebugWindow();
      }
   }

   @SideOnly(Side.CLIENT)
   static void registerDebugWindow() {
      if (shouldShowDebugWindow()) {
         MinecraftForge.EVENT_BUS.register(new AgeWarningTrigger());
      } else {
         AgeWarningWindow.isVisible = false;
      }

      MinecraftForge.EVENT_BUS.register(new HornyMeterHud());
      MinecraftForge.EVENT_BUS.register(new SceneFadeScreen());
      MinecraftForge.EVENT_BUS.register(new PlayerMovementController());
      MinecraftForge.EVENT_BUS.register(new SceneCameraManager());
      MinecraftForge.EVENT_BUS.register(new GuiOpenHandler());
      MinecraftForge.EVENT_BUS.register(new InHandMapRenderer());
      MinecraftForge.EVENT_BUS.register(new GirlCameraHelper());
      MinecraftForge.EVENT_BUS.register(new com.trolmastercard.sexmod.client.event.SexSceneKeyHandler());
      MinecraftForge.EVENT_BUS.register(new GenderSwapScreen());
      MinecraftForge.EVENT_BUS.register(new PlayerAllieRenderer.ClientTickHandler());
      MinecraftForge.EVENT_BUS.register(new StructureMarkerRenderer());
      MinecraftForge.EVENT_BUS.register(new DeprecatedCheckForUpdates());
      MinecraftForge.EVENT_BUS.register(new ClothingScreen.KeyAndScrollHandler());
      MinecraftForge.EVENT_BUS.register(new ServerWhitelistManager.ChatHandler());
      MinecraftForge.EVENT_BUS.register(new EscapeMinigameHud());
      MinecraftForge.EVENT_BUS.register(new CumTrailOverlay());
      MinecraftForge.EVENT_BUS.register(new GalathFlightHud());
   }

   static boolean shouldShowDebugWindow() {
      File dontAskAgainFile = new File("sexmod/dontAskAgain");
      dontAskAgainFile.getParentFile().mkdirs();
      return !dontAskAgainFile.exists();
   }

}
