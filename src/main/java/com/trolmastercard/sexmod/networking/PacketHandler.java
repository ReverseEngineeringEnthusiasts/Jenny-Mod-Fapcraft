package com.trolmastercard.sexmod.networking;


import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
   public static SimpleNetworkWrapper b;
   private static int a = 0;

   private static int nextPacketId() {
      return a++;
   }

   public static void register() {
      b = NetworkRegistry.INSTANCE.newSimpleChannel("sexmodchannel");
      b.registerMessage(SendChatMessagePacket.Handler.class, SendChatMessagePacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(SendChatMessagePacket.Handler.class, SendChatMessagePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SetPlayerMovementPacket.Handler.class, SetPlayerMovementPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(TeleportPlayerPacket.Handler.class, TeleportPlayerPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SendGirlToSexPacket.Handler.class, SendGirlToSexPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SetPlayerForGirlPacket.Handler.class, SetPlayerForGirlPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(KoboldStatePacket.Handler.class, KoboldStatePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(ResetGirlPacket.Handler.class, ResetGirlPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(ChangeDataParameterPacket.Handler.class, ChangeDataParameterPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(PlayerActionPacket.Handler.class, PlayerActionPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SendCompanionHomePacket.Handler.class, SendCompanionHomePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SetNewHomePacket.Handler.class, SetNewHomePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(UploadInventoryToServerPacket.Handler.class, UploadInventoryToServerPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(RemoveItemsPacket.Handler.class, RemoveItemsPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SummonAlliePacket.Handler.class, SummonAlliePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(UploadInventoryToServerPacket2.Handler.class, UploadInventoryToServerPacket2.class, nextPacketId(), Side.SERVER);
      b.registerMessage(MakeRichWishPacket.Handler.class, MakeRichWishPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(UpdatePlayerModelPacket.Handler.class, UpdatePlayerModelPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(StartStandingSexAnimationPacket.Handler.class, StartStandingSexAnimationPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(BeeOpenChestPacket.Handler.class, BeeOpenChestPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(CatActivateFishingPacket.Handler.class, CatActivateFishingPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(CatEatingDonePacket.Handler.class, CatEatingDonePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(CatThrowAwayItemPacket.Handler.class, CatThrowAwayItemPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(ClaimTribePacket.Handler.class, ClaimTribePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(SetTribeFollowModePacket.Handler.class, SetTribeFollowModePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(FallTreePacket.Handler.class, FallTreePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SendBlocksPacket.Handler.class, SendBlocksPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(SendBlocksPacket.Handler.class, SendBlocksPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(CancelTaskPacket.Handler.class, CancelTaskPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SpawnParticlePacket.Handler.class, SpawnParticlePacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(SendEggPacket.Handler.class, SendEggPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(MinePacket.Handler.class, MinePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(GirlDataPacket.Handler.class, GirlDataPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(ForcePlayerGirlUpdatePacket.Handler.class, ForcePlayerGirlUpdatePacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(UploadModelStringPacket.Handler.class, UploadModelStringPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(GalathRapePouncePacket.Handler.class, GalathRapePouncePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(UpdateVelocityPacket.Handler.class, UpdateVelocityPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(SpawnEnergyBallParticlesPacket2.Handler.class, SpawnEnergyBallParticlesPacket2.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(GalathBackOffRapePacket.Handler.class, GalathBackOffRapePacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(InformOfOwnershipPacket.Handler.class, InformOfOwnershipPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(RequestRidingPacket.Handler.class, RequestRidingPacket.class, nextPacketId(), Side.SERVER);
      b.registerMessage(SpawnEnergyBallParticlesPacket.Handler.class, SpawnEnergyBallParticlesPacket.class, nextPacketId(), Side.CLIENT);
      b.registerMessage(SetPlayerCamPacket.Handler.class, SetPlayerCamPacket.class, nextPacketId(), Side.CLIENT);
   }
}
