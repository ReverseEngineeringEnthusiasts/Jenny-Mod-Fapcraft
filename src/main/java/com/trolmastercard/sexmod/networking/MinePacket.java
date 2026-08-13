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

public class MinePacket implements IMessage {
   boolean isValid = false;
   BlockPos targetPos;
   EnumFacing facing;

   public MinePacket() {
   }

   public MinePacket(BlockPos var1, EnumFacing var2) {
      this.targetPos = var1;
      this.facing = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.targetPos = new BlockPos(var1.readInt(), var1.readInt(), var1.readInt());
      this.facing = EnumFacing.byName(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.targetPos.getX());
      var1.writeInt(this.targetPos.getY());
      var1.writeInt(this.targetPos.getZ());
      ByteBufUtils.writeUTF8String(var1, this.facing.getName());
   }

   public static class Handler implements IMessageHandler<MinePacket, IMessage> {
      public IMessage onMessage(MinePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP var3 = var2.getServerHandler().player;
                     UUID var4 = KoboldManager.getTribeUUID(var3.getPersistentID());
                     if (var4 != null) {
                        int var5 = KoboldManager.getTribeMemberCount(var4);
                        int var6 = (int)Math.floor(KoboldManager.getTribeBeds(var4).size() / 2.0);
                        if (var5 > var6) {
                           var3.sendMessage(
                              new TextComponentString(
                                 String.format(
                                    "sUr Tribe will only work for you, if %severyone%s of them has a %sbed",
                                    TextFormatting.RED,
                                    TextFormatting.WHITE,
                                    TextFormatting.RED
                                 )
                              )
                           );
                           var3.sendMessage(new TextComponentString(String.format("%s%d/%d Beds", TextFormatting.YELLOW, var6, var5)));
                        } else {
                           HashSet var7 = this.a(var1.targetPos, var1.facing);
                           World var8 = var2.getServerHandler().player.world;

                           for (BlockPos var10 : (java.util.Collection<BlockPos>) (var7) ) {
                              IBlockState var11 = var8.getBlockState(var10);
                              if (var11.getBlock().getBlockHardness(var11, var8, var10) < 0.0F) {
                                 var3.sendStatusMessage(new TextComponentString("This area contains Bedrock and cannot be mined"), true);
                                 return;
                              }
                           }

                           KoboldTask var12 = new KoboldTask(var1.targetPos, KoboldTask.TaskType.MINE, var7, var1.facing);
                           KoboldManager.b(var4, var12);
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var7, true), var2.getServerHandler().player);
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

      HashSet<BlockPos> a(BlockPos var1, EnumFacing var2) {
         HashSet var3 = new HashSet();
         BlockPos var4 = var1;

         for (int var5 = 0; var5 < 30; var5++) {
            var3.add(var4.subtract(this.a(var2)));
            var3.add(var4.subtract(this.a(var2)).up());
            var3.add(var4.subtract(this.a(var2)).up().up());
            var3.add(var4);
            var3.add(var4.up());
            var3.add(var4.up().up());
            var3.add(var4.add(this.a(var2)));
            var3.add(var4.add(this.a(var2)).up());
            var3.add(var4.add(this.a(var2)).up().up());
            var4 = var4.add(var2.getDirectionVec());
         }

         return var3;
      }

      BlockPos a(EnumFacing var1) {
         Vec3i var2 = var1.getDirectionVec();
         return new BlockPos(var2.getZ(), var2.getY(), -var2.getX());
      }

   }
}
