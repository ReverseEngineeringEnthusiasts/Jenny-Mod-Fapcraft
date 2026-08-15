package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.StructureMarkerRenderer;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockChest.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>Role.</b> Tribe-block marker sync between the dragon-staff UI and the
 * server's {@link KoboldManager} state.
 * <p>
 * SERVER->CLIENT (usual direction): {@code isBreaking=true} adds the blocks to
 * {@link StructureMarkerRenderer#renderMarkers(HashSet)} (red/green/blue
 * highlight), {@code isBreaking=false} removes them ({@code setMarkers}). Used by
 * {@link FallTreePacket}, {@link MinePacket}, {@link CancelTaskPacket} and on
 * login ({@code PlayerIds}).
 * <p>
 * CLIENT->SERVER (dragon-staff clicks on a marked bed/chest): the SERVER-side
 * handler (scheduled on the main thread) resolves double-blocks (bed halves,
 * double chests) via {@link WorldUtils#getStatePos} and adds/removes them from
 * the tribe's bed/chest sets, echoing the resolved pair back to the client so
 * the markers match.
 * <p>
 * <b>Pitfall.</b> A single-block payload is the only accepted CLIENT->SERVER
 * form; multi-block payloads are ignored server-side (they are only valid as
 * SERVER->CLIENT echo batches).
 */
public class SendBlocksPacket implements IMessage {
   boolean isValid = false;
   HashSet<BlockPos> blockPositions = new HashSet<>();
   boolean isBreaking;

   public SendBlocksPacket() {
   }

   public SendBlocksPacket(HashSet<BlockPos> blockPositions, boolean isBreaking) {
      this.blockPositions = blockPositions;
      this.isBreaking = isBreaking;
   }

   public SendBlocksPacket(BlockPos blockPos, boolean isBreaking) {
      this.blockPositions.add(blockPos);
      this.isBreaking = isBreaking;
   }

   public void fromBytes(ByteBuf buf) {
      this.isBreaking = buf.readBoolean();
      int count = buf.readInt();

      for (int i = 0; i < count; i++) {
         this.blockPositions.add(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isBreaking);
      buf.writeInt(this.blockPositions.size());

      for (BlockPos pos : this.blockPositions) {
         buf.writeInt(pos.getX());
         buf.writeInt(pos.getY());
         buf.writeInt(pos.getZ());
      }
   }

   public static class Handler implements IMessageHandler<SendBlocksPacket, IMessage> {
      public IMessage onMessage(SendBlocksPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid Message @SendBlocks :(");
            return null;
         }

         if (ctx.side.isClient()) {
            if (packet.isBreaking) {
               StructureMarkerRenderer.renderMarkers(packet.blockPositions);
            } else {
               StructureMarkerRenderer.setMarkers(packet.blockPositions);
            }

            return null;
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     UUID playerUuid = ctx.getServerHandler().player.getPersistentID();
                     UUID tribeUuid = KoboldManager.getTribeUUID(playerUuid);
                     if (tribeUuid != null) {
                        if (packet.blockPositions.size() == 1) {
                           World world = ctx.getServerHandler().player.world;

                           for (BlockPos pos : packet.blockPositions) {
                              IBlockState state = world.getBlockState(pos);
                              BlockPos pairedPos = null;
                              if (state.getBlock() instanceof BlockBed) {
                                 pairedPos = WorldUtils.getStatePos(pos, state);
                              }

                              if (state.getBlock() instanceof BlockChest) {
                                 Type chestType = ((BlockChest)state.getBlock()).chestType;
                                 if (world.getBlockState(pos.north()).getBlock() instanceof BlockChest
                                    && chestType.equals(((BlockChest)world.getBlockState(pos.north()).getBlock()).chestType)) {
                                    pairedPos = pos.north();
                                 }

                                 if (world.getBlockState(pos.east()).getBlock() instanceof BlockChest
                                    && chestType.equals(((BlockChest)world.getBlockState(pos.east()).getBlock()).chestType)) {
                                    pairedPos = pos.east();
                                 }

                                 if (world.getBlockState(pos.south()).getBlock() instanceof BlockChest
                                    && chestType.equals(((BlockChest)world.getBlockState(pos.south()).getBlock()).chestType)) {
                                    pairedPos = pos.south();
                                 }

                                 if (world.getBlockState(pos.west()).getBlock() instanceof BlockChest
                                    && chestType.equals(((BlockChest)world.getBlockState(pos.west()).getBlock()).chestType)) {
                                    pairedPos = pos.west();
                                 }
                              }

                              if (pairedPos == null && state.getBlock() instanceof BlockBed) {
                                 return;
                              }

                              if (packet.isBreaking) {
                                 if (state.getBlock() instanceof BlockBed) {
                                    KoboldManager.addTribeBed(tribeUuid, pos);
                                    KoboldManager.addTribeBed(tribeUuid, pairedPos);
                                 } else {
                                    KoboldManager.addTribeChest(tribeUuid, pos);
                                    KoboldManager.addTribeChest(tribeUuid, pairedPos);
                                 }
                              } else if (state.getBlock() instanceof BlockBed) {
                                 KoboldManager.removeTribeChest(tribeUuid, pos);
                                 KoboldManager.removeTribeChest(tribeUuid, pairedPos);
                              } else {
                                 KoboldManager.removeMiningTargetsFor(tribeUuid, pos);
                                 KoboldManager.removeMiningTargetsFor(tribeUuid, pairedPos);
                              }

                              HashSet blocks = new HashSet();
                              blocks.add(pos);
                              if (pairedPos != null) {
                                 blocks.add(pairedPos);
                              }

                              PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, packet.isBreaking), ctx.getServerHandler().player);
                           }
                        }
                     }
                  }
               );
            return null;
         }
      }

   }
}
