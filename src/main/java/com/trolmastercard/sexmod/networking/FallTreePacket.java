package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to fell a tree with the tribe. Sent from the
 * dragon-staff UI when the player marks a log ("cut tree").
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Validates the tribe
 * bed requirement (members may not exceed {@code beds/2}); if it holds, walks
 * down the log with {@link #findGroundPos(World, BlockPos)} to the ground block,
 * computes the connected log blocks via
 * {@link KoboldTask#findConnectedBlocks(World, BlockPos, UUID)} (which also
 * creates the {@code FALL_TREE} task) and echoes the blocks back to the client as
 * a {@link SendBlocksPacket} for marker highlighting.
 */
public class FallTreePacket implements IMessage {
   Boolean isValid = false;
   BlockPos treePos;

   public FallTreePacket() {
   }

   public FallTreePacket(BlockPos treePos) {
      this.treePos = treePos;
   }

   public void fromBytes(ByteBuf buf) {
      this.treePos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.treePos.getX());
      buf.writeInt(this.treePos.getY());
      buf.writeInt(this.treePos.getZ());
   }

   public static class Handler implements IMessageHandler<FallTreePacket, IMessage> {
      public IMessage onMessage(FallTreePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP player = ctx.getServerHandler().player;
                     UUID tribeUuid = KoboldManager.getTribeUUID(player.getPersistentID());
                     if (tribeUuid == null) {
                        System.out.println("not tribe for player");
                     } else {
                        int memberCount = KoboldManager.getTribeMemberCount(tribeUuid);
                        int bedCount = (int)Math.floor(KoboldManager.getTribeBeds(tribeUuid).size() / 2.0);
                        if (memberCount > bedCount) {
                           player.sendMessage(
                              new TextComponentString(
                                 String.format(
                                    "Ur Tribe will only work for you, if %severyone%s of them has a %sbed",
                                    TextFormatting.RED,
                                    TextFormatting.WHITE,
                                    TextFormatting.RED
                                 )
                              )
                           );
                           player.sendMessage(new TextComponentString(String.format("%s%d/%d Beds", TextFormatting.YELLOW, bedCount, memberCount)));
                        } else {
                           World world = player.world;
                           BlockPos groundPos = this.findGroundPos(world, packet.treePos);
                           HashSet blocks = KoboldTask.findConnectedBlocks(world, groundPos, tribeUuid);
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, true), ctx.getServerHandler().player);
                        }
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid Message @FallTree :(");
            return null;
         }
      }

      /**
    * Recursively follows {@link BlockLog} blocks downwards (including diagonals)
    * to find the lowest ground block of the tree. SERVER-side helper; recursion
    * terminates because the tree has finite height.
    *
    * @return the {@link BlockPos} just above the first non-log block
    */
   BlockPos findGroundPos(World world, BlockPos pos) {
         if (world.getBlockState(pos.add(0, -1, 0)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(0, -1, 0));
         } else if (world.getBlockState(pos.add(1, -1, 0)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(1, -1, 0));
         } else if (world.getBlockState(pos.add(-1, -1, 0)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(-1, -1, 0));
         } else if (world.getBlockState(pos.add(0, -1, 1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(0, -1, 1));
         } else if (world.getBlockState(pos.add(0, -1, -1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(0, -1, -1));
         } else if (world.getBlockState(pos.add(-1, -1, -1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(-1, -1, -1));
         } else if (world.getBlockState(pos.add(1, -1, 1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(1, -1, 1));
         } else if (world.getBlockState(pos.add(-1, -1, 1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(world, pos.add(-1, -1, 1));
         } else {
            return world.getBlockState(pos.add(1, -1, -1)).getBlock() instanceof BlockLog
               ? this.findGroundPos(world, pos.add(1, -1, -1))
               : pos;
         }
      }

   }
}
