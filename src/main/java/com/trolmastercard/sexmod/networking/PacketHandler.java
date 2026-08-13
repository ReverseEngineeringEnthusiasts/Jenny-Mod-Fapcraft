package com.trolmastercard.sexmod.networking;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
   public static SimpleNetworkWrapper networkWrapper;
   private static int nextId = 0;

   private static int nextPacketId() {
      return nextId++;
   }

   public static void register() {
      networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("sexmodchannel");
      networkWrapper.registerMessage(SendChatMessagePacket.Handler.class, SendChatMessagePacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SendChatMessagePacket.Handler.class, SendChatMessagePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetPlayerMovementPacket.Handler.class, SetPlayerMovementPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(TeleportPlayerPacket.Handler.class, TeleportPlayerPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SendGirlToSexPacket.Handler.class, SendGirlToSexPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetPlayerForGirlPacket.Handler.class, SetPlayerForGirlPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(KoboldStatePacket.Handler.class, KoboldStatePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ResetGirlPacket.Handler.class, ResetGirlPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ChangeDataParameterPacket.Handler.class, ChangeDataParameterPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(PlayerActionPacket.Handler.class, PlayerActionPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SendCompanionHomePacket.Handler.class, SendCompanionHomePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetNewHomePacket.Handler.class, SetNewHomePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UploadInventoryToServerPacket.Handler.class, UploadInventoryToServerPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(RemoveItemsPacket.Handler.class, RemoveItemsPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SummonAlliePacket.Handler.class, SummonAlliePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UploadInventoryToServerPacket2.Handler.class, UploadInventoryToServerPacket2.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(MakeRichWishPacket.Handler.class, MakeRichWishPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UpdatePlayerModelPacket.Handler.class, UpdatePlayerModelPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(StartStandingSexAnimationPacket.Handler.class, StartStandingSexAnimationPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(BeeOpenChestPacket.Handler.class, BeeOpenChestPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(CatActivateFishingPacket.Handler.class, CatActivateFishingPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(CatEatingDonePacket.Handler.class, CatEatingDonePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(CatThrowAwayItemPacket.Handler.class, CatThrowAwayItemPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ClaimTribePacket.Handler.class, ClaimTribePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SetTribeFollowModePacket.Handler.class, SetTribeFollowModePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(FallTreePacket.Handler.class, FallTreePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SendBlocksPacket.Handler.class, SendBlocksPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SendBlocksPacket.Handler.class, SendBlocksPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(CancelTaskPacket.Handler.class, CancelTaskPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SpawnParticlePacket.Handler.class, SpawnParticlePacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SendEggPacket.Handler.class, SendEggPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(MinePacket.Handler.class, MinePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GirlDataPacket.Handler.class, GirlDataPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(ForcePlayerGirlUpdatePacket.Handler.class, ForcePlayerGirlUpdatePacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(UploadModelStringPacket.Handler.class, UploadModelStringPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GalathRapePouncePacket.Handler.class, GalathRapePouncePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UpdateVelocityPacket.Handler.class, UpdateVelocityPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SpawnEnergyBallParticlesPacket2.Handler.class, SpawnEnergyBallParticlesPacket2.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(GalathBackOffRapePacket.Handler.class, GalathBackOffRapePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(InformOfOwnershipPacket.Handler.class, InformOfOwnershipPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(RequestRidingPacket.Handler.class, RequestRidingPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SpawnEnergyBallParticlesPacket.Handler.class, SpawnEnergyBallParticlesPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SetPlayerCamPacket.Handler.class, SetPlayerCamPacket.class, nextPacketId(), Side.CLIENT);
   }
}
