package com.trolmastercard.sexmod.networking;


import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
   public static SimpleNetworkWrapper b;
   private static int a = 0;

   private static int b_clash733() {
      return a++;
   }

   public static void register() {
      b = NetworkRegistry.INSTANCE.newSimpleChannel("sexmodchannel");
      b.registerMessage(SendChatMessagePacket.Handler.class, SendChatMessagePacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(SendChatMessagePacket.Handler.class, SendChatMessagePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SetPlayerMovementPacket.Handler.class, SetPlayerMovementPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(TeleportPlayerPacket.Handler.class, TeleportPlayerPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SendGirlToSexPacket.Handler.class, SendGirlToSexPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SetPlayerForGirlPacket.Handler.class, SetPlayerForGirlPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(KoboldStatePacket.Handler.class, KoboldStatePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(ResetGirlPacket.Handler.class, ResetGirlPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(ChangeDataParameterPacket.Handler.class, ChangeDataParameterPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(PlayerActionPacket.Handler.class, PlayerActionPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SendCompanionHomePacket.Handler.class, SendCompanionHomePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SetNewHomePacket.Handler.class, SetNewHomePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(UploadInventoryToServerPacket.Handler.class, UploadInventoryToServerPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(RemoveItemsPacket.Handler.class, RemoveItemsPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SummonAlliePacket.Handler.class, SummonAlliePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(UploadInventoryToServerPacket2.Handler.class, UploadInventoryToServerPacket2.class, b_clash733(), Side.SERVER);
      b.registerMessage(MakeRichWishPacket.Handler.class, MakeRichWishPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(UpdatePlayerModelPacket.Handler.class, UpdatePlayerModelPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(StartStandingSexAnimationPacket.Handler.class, StartStandingSexAnimationPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(BeeOpenChestPacket.Handler.class, BeeOpenChestPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(CatActivateFishingPacket.Handler.class, CatActivateFishingPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(CatEatingDonePacket.Handler.class, CatEatingDonePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(CatThrowAwayItemPacket.Handler.class, CatThrowAwayItemPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(ClaimTribePacket.Handler.class, ClaimTribePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(SetTribeFollowModePacket.Handler.class, SetTribeFollowModePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(FallTreePacket.Handler.class, FallTreePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SendBlocksPacket.Handler.class, SendBlocksPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(SendBlocksPacket.Handler.class, SendBlocksPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(CancelTaskPacket.Handler.class, CancelTaskPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SpawnParticlePacket.Handler.class, SpawnParticlePacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(SendEggPacket.Handler.class, SendEggPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(MinePacket.Handler.class, MinePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(GirlDataPacket.Handler.class, GirlDataPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(ForcePlayerGirlUpdatePacket.Handler.class, ForcePlayerGirlUpdatePacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(UploadModelStringPacket.Handler.class, UploadModelStringPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(GalathRapePouncePacket.Handler.class, GalathRapePouncePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(UpdateVelocityPacket.Handler.class, UpdateVelocityPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(SpawnEnergyBallParticlesPacket2.Handler.class, SpawnEnergyBallParticlesPacket2.class, b_clash733(), Side.CLIENT);
      b.registerMessage(GalathBackOffRapePacket.Handler.class, GalathBackOffRapePacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(InformOfOwnershipPacket.Handler.class, InformOfOwnershipPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(RequestRidingPacket.Handler.class, RequestRidingPacket.class, b_clash733(), Side.SERVER);
      b.registerMessage(SpawnEnergyBallParticlesPacket.Handler.class, SpawnEnergyBallParticlesPacket.class, b_clash733(), Side.CLIENT);
      b.registerMessage(SetPlayerCamPacket.Handler.class, SetPlayerCamPacket.class, b_clash733(), Side.CLIENT);
   }
}
