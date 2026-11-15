package com.trolmastercard.sexmod.network;

import com.trolmastercard.sexmod.network.galath.GalathBackOffPacket;
import com.trolmastercard.sexmod.network.galath.GalathPouncePacket;
import com.trolmastercard.sexmod.network.galath.RequestGalathRidePacket;
import com.trolmastercard.sexmod.network.scene.ResetControllerPacket;
import com.trolmastercard.sexmod.network.scene.ResetGirlPacket;
import com.trolmastercard.sexmod.network.scene.SendGirlToBedPacket;
import com.trolmastercard.sexmod.network.scene.SetSceneCameraPacket;
import com.trolmastercard.sexmod.network.scene.LockPlayerMovementPacket;
import com.trolmastercard.sexmod.network.scene.SexPromptPacket;
import com.trolmastercard.sexmod.network.scene.StartStandingSexAnimationPacket;
import com.trolmastercard.sexmod.network.girl.OpenGirlGuiPacket;
import com.trolmastercard.sexmod.network.girl.GirlCustomizationPacket;
import com.trolmastercard.sexmod.network.scene.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.network.galath.OwnershipUpdatePacket;
import com.trolmastercard.sexmod.network.scene.SetEntityDataPacket;
import com.trolmastercard.sexmod.network.scene.SetGirlVelocityPacket;
import com.trolmastercard.sexmod.network.scene.TeleportPlayerPacket;
import com.trolmastercard.sexmod.network.girl.SyncPlayerGirlPacket;
import com.trolmastercard.sexmod.network.girl.DownloadServerModelPacket;
import com.trolmastercard.sexmod.network.girl.UploadModelStringPacket;
import com.trolmastercard.sexmod.network.girl.UpdatePlayerModelPacket;
import com.trolmastercard.sexmod.network.girl.UploadGirlInventoryPacket;
import com.trolmastercard.sexmod.network.girl.CloseGirlInventoryPacket;
import com.trolmastercard.sexmod.network.girl.RemoveItemsPacket;
import com.trolmastercard.sexmod.network.kobold.ClaimTribePacket;
import com.trolmastercard.sexmod.network.kobold.GetTribeUiValuesPacket;
import com.trolmastercard.sexmod.network.scene.StartScenePacket;
import com.trolmastercard.sexmod.network.kobold.MinePacket;
import com.trolmastercard.sexmod.network.kobold.SendEggPacket;
import com.trolmastercard.sexmod.network.kobold.SetTribeFollowModePacket;
import com.trolmastercard.sexmod.network.kobold.CancelTaskPacket;
import com.trolmastercard.sexmod.network.kobold.FallTreePacket;
import com.trolmastercard.sexmod.network.girl.BeeOpenChestPacket;
import com.trolmastercard.sexmod.network.girl.LunaActivateFishingPacket;
import com.trolmastercard.sexmod.network.girl.LunaEatingDonePacket;
import com.trolmastercard.sexmod.network.girl.LunaThrowAwayItemPacket;
import com.trolmastercard.sexmod.network.girl.SendCompanionHomePacket;
import com.trolmastercard.sexmod.network.girl.SendChatMessagePacket;
import com.trolmastercard.sexmod.network.girl.SetNewHomePacket;
import com.trolmastercard.sexmod.network.girl.MakeRichWishPacket;
import com.trolmastercard.sexmod.network.girl.SummonAlliePacket;
import com.trolmastercard.sexmod.network.kobold.TribeBlocksPacket;
import com.trolmastercard.sexmod.network.scene.SpawnParticlePacket;
import com.trolmastercard.sexmod.network.galath.GalathSummonParticlesPacket;
import com.trolmastercard.sexmod.network.galath.DragonBreathParticlesPacket;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Registers every mod packet on the {@code "sexmodchannel"} simple channel.
 * <p>
 * <b>CRITICAL:</b> packet IDs are assigned sequentially by
 * {@link #nextPacketId()} at registration time. The ID order IS the wire
 * protocol — it is shared with the built (SRG-reobfuscated) jar and any
 * saved/connected session. Never reorder, insert, or remove registrations
 * without a full client+server redeploy, and never change the channel name.
 * <p>
 * The scene-critical packets (see their class docs): {@link KoboldStatePacket}
 * (scene entry), {@link ChangeDataParameterPacket} (data-manager bridge),
 * {@link ResetGirlPacket} (scene end), {@link SendGirlToSexPacket} (bed
 * scenes), {@link TeleportPlayerPacket} (scene camera positioning),
 * {@link SetPlayerMovementPacket} (movement lock restore).
 */
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
      networkWrapper.registerMessage(LockPlayerMovementPacket.Handler.class, LockPlayerMovementPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(TeleportPlayerPacket.Handler.class, TeleportPlayerPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SendGirlToBedPacket.Handler.class, SendGirlToBedPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetPlayerForGirlPacket.Handler.class, SetPlayerForGirlPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(StartScenePacket.Handler.class, StartScenePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(ResetControllerPacket.Handler.class, ResetControllerPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ResetGirlPacket.Handler.class, ResetGirlPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetEntityDataPacket.Handler.class, SetEntityDataPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(OpenGirlGuiPacket.Handler.class, OpenGirlGuiPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SendCompanionHomePacket.Handler.class, SendCompanionHomePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetNewHomePacket.Handler.class, SetNewHomePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UploadGirlInventoryPacket.Handler.class, UploadGirlInventoryPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(RemoveItemsPacket.Handler.class, RemoveItemsPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SummonAlliePacket.Handler.class, SummonAlliePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(CloseGirlInventoryPacket.Handler.class, CloseGirlInventoryPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(MakeRichWishPacket.Handler.class, MakeRichWishPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UpdatePlayerModelPacket.Handler.class, UpdatePlayerModelPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SexPromptPacket.Handler.class, SexPromptPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(StartStandingSexAnimationPacket.Handler.class, StartStandingSexAnimationPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(BeeOpenChestPacket.Handler.class, BeeOpenChestPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(LunaActivateFishingPacket.Handler.class, LunaActivateFishingPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(LunaEatingDonePacket.Handler.class, LunaEatingDonePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(LunaThrowAwayItemPacket.Handler.class, LunaThrowAwayItemPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(ClaimTribePacket.Handler.class, ClaimTribePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GetTribeUiValuesPacket.Handler.class, GetTribeUiValuesPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SetTribeFollowModePacket.Handler.class, SetTribeFollowModePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(FallTreePacket.Handler.class, FallTreePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(TribeBlocksPacket.Handler.class, TribeBlocksPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(TribeBlocksPacket.Handler.class, TribeBlocksPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(CancelTaskPacket.Handler.class, CancelTaskPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SpawnParticlePacket.Handler.class, SpawnParticlePacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SendEggPacket.Handler.class, SendEggPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(MinePacket.Handler.class, MinePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GirlCustomizationPacket.Handler.class, GirlCustomizationPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SyncPlayerGirlPacket.Handler.class, SyncPlayerGirlPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(UploadModelStringPacket.Handler.class, UploadModelStringPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GalathPouncePacket.Handler.class, GalathPouncePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(SetGirlVelocityPacket.Handler.class, SetGirlVelocityPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(UnknownPacket.Handler.class, UnknownPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(DownloadServerModelPacket.Handler.class, DownloadServerModelPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(DragonBreathParticlesPacket.Handler.class, DragonBreathParticlesPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(GalathBackOffPacket.Handler.class, GalathBackOffPacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(OwnershipUpdatePacket.Handler.class, OwnershipUpdatePacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(RequestGalathRidePacket.Handler.class, RequestGalathRidePacket.class, nextPacketId(), Side.SERVER);
      networkWrapper.registerMessage(GalathSummonParticlesPacket.Handler.class, GalathSummonParticlesPacket.class, nextPacketId(), Side.CLIENT);
      networkWrapper.registerMessage(SetSceneCameraPacket.Handler.class, SetSceneCameraPacket.class, nextPacketId(), Side.CLIENT);
   }
}
