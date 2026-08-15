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

   public FallTreePacket(BlockPos var1) {
      this.treePos = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.treePos = new BlockPos(var1.readInt(), var1.readInt(), var1.readInt());
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.treePos.getX());
      var1.writeInt(this.treePos.getY());
      var1.writeInt(this.treePos.getZ());
   }

   public static class Handler implements IMessageHandler<FallTreePacket, IMessage> {
      public IMessage onMessage(FallTreePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP var3 = var2.getServerHandler().player;
                     UUID var4 = KoboldManager.getTribeUUID(var3.getPersistentID());
                     if (var4 == null) {
                        System.out.println("not tribe for player");
                     } else {
                        int var5 = KoboldManager.getTribeMemberCount(var4);
                        int var6 = (int)Math.floor(KoboldManager.getTribeBeds(var4).size() / 2.0);
                        if (var5 > var6) {
                           var3.sendMessage(
                              new TextComponentString(
                                 String.format(
                                    "Ur Tribe will only work for you, if %severyone%s of them has a %sbed",
                                    TextFormatting.RED,
                                    TextFormatting.WHITE,
                                    TextFormatting.RED
                                 )
                              )
                           );
                           var3.sendMessage(new TextComponentString(String.format("%s%d/%d Beds", TextFormatting.YELLOW, var6, var5)));
                        } else {
                           World var7 = var3.world;
                           BlockPos var8 = this.findGroundPos(var7, var1.treePos);
                           HashSet var9 = KoboldTask.findConnectedBlocks(var7, var8, var4);
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var9, true), var2.getServerHandler().player);
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
   BlockPos findGroundPos(World var1, BlockPos var2) {
         if (var1.getBlockState(var2.add(0, -1, 0)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(0, -1, 0));
         } else if (var1.getBlockState(var2.add(1, -1, 0)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(1, -1, 0));
         } else if (var1.getBlockState(var2.add(-1, -1, 0)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(-1, -1, 0));
         } else if (var1.getBlockState(var2.add(0, -1, 1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(0, -1, 1));
         } else if (var1.getBlockState(var2.add(0, -1, -1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(0, -1, -1));
         } else if (var1.getBlockState(var2.add(-1, -1, -1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(-1, -1, -1));
         } else if (var1.getBlockState(var2.add(1, -1, 1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(1, -1, 1));
         } else if (var1.getBlockState(var2.add(-1, -1, 1)).getBlock() instanceof BlockLog) {
            return this.findGroundPos(var1, var2.add(-1, -1, 1));
         } else {
            return var1.getBlockState(var2.add(1, -1, -1)).getBlock() instanceof BlockLog
               ? this.findGroundPos(var1, var2.add(1, -1, -1))
               : var2;
         }
      }

   }
}
