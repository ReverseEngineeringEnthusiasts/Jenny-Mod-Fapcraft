package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER "mine this area" command from the dragon-staff UI.
 * Sent when the player selects a block and a facing; the tribe kobolds dig a
 * 30-block corridor through it.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Validates the bed
 * requirement (see {@link FallTreePacket}); computes the 30x3 tunnel blocks with
 * {@link #getMineableBlocks(BlockPos, EnumFacing)}, rejects the request if any of
 * them is unbreakable (bedrock), then registers a {@code MINE} {@link KoboldTask}
 * and echoes the target blocks to the client as a
 * {@link SendBlocksPacket} (markers).
 */
public class MinePacket implements IMessage {
   boolean isValid = false;
   BlockPos targetPos;
   EnumFacing facing;

   public MinePacket() {
   }

   public MinePacket(BlockPos targetPos, EnumFacing facing) {
      this.targetPos = targetPos;
      this.facing = facing;
   }

   public void fromBytes(ByteBuf buf) {
      this.targetPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
      this.facing = EnumFacing.byName(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.targetPos.getX());
      buf.writeInt(this.targetPos.getY());
      buf.writeInt(this.targetPos.getZ());
      ByteBufUtils.writeUTF8String(buf, this.facing.getName());
   }

   public static class Handler implements IMessageHandler<MinePacket, IMessage> {
      public IMessage onMessage(MinePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP player = ctx.getServerHandler().player;
                     UUID tribeUuid = KoboldManager.getTribeUUID(player.getPersistentID());
                     if (tribeUuid != null) {
                        int memberCount = KoboldManager.getTribeMemberCount(tribeUuid);
                        int bedCount = (int)Math.floor(KoboldManager.getTribeBeds(tribeUuid).size() / 2.0);
                        if (memberCount > bedCount) {
                           player.sendMessage(
                              new TextComponentString(
                                 String.format(
                                    "sUr Tribe will only work for you, if %severyone%s of them has a %sbed",
                                    TextFormatting.RED,
                                    TextFormatting.WHITE,
                                    TextFormatting.RED
                                 )
                              )
                           );
                           player.sendMessage(new TextComponentString(String.format("%s%d/%d Beds", TextFormatting.YELLOW, bedCount, memberCount)));
                        } else {
                           HashSet blocks = this.getMineableBlocks(packet.targetPos, packet.facing);
                           World world = ctx.getServerHandler().player.world;

                           for (BlockPos pos : (java.util.Collection<BlockPos>) (blocks) ) {
                              IBlockState state = world.getBlockState(pos);
                              if (state.getBlock().getBlockHardness(state, world, pos) < 0.0F) {
                                 player.sendStatusMessage(new TextComponentString("This area contains Bedrock and cannot be mined"), true);
                                 return;
                              }
                           }

                           KoboldTask task = new KoboldTask(packet.targetPos, KoboldTask.TaskType.MINE, blocks, packet.facing);
                           KoboldManager.addTask(tribeUuid, task);
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, true), ctx.getServerHandler().player);
                        }
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid Message @Mine :(");
            return null;
         }
      }

      /**
    * Computes the 3-block-high, 30-block-long tunnel cross-section starting at
    * {@code startPos} extending along {@code facing}. SERVER-side helper for the mine
    * command.
    *
    * @return all {@link BlockPos} that the kobolds will have to break
    */
   HashSet<BlockPos> getMineableBlocks(BlockPos startPos, EnumFacing facing) {
         HashSet positions = new HashSet();
         BlockPos pos = startPos;

         for (int i = 0; i < 30; i++) {
            positions.add(pos.subtract(this.getNextBlock(facing)));
            positions.add(pos.subtract(this.getNextBlock(facing)).up());
            positions.add(pos.subtract(this.getNextBlock(facing)).up().up());
            positions.add(pos);
            positions.add(pos.up());
            positions.add(pos.up().up());
            positions.add(pos.add(this.getNextBlock(facing)));
            positions.add(pos.add(this.getNextBlock(facing)).up());
            positions.add(pos.add(this.getNextBlock(facing)).up().up());
            pos = pos.add(facing.getDirectionVec());
         }

         return positions;
      }

      /**
    * Lateral offset of the tunnel cross-section: the vector perpendicular to the
    * facing direction (rotated 90 degrees in the horizontal plane).
    */
   BlockPos getNextBlock(EnumFacing facing) {
         Vec3i dirVec = facing.getDirectionVec();
         return new BlockPos(dirVec.getZ(), dirVec.getY(), -dirVec.getX());
      }

   }
}
