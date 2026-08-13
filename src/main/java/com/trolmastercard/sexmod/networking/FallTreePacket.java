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

public class FallTreePacket implements IMessage {
   Boolean b = false;
   BlockPos a;

   public FallTreePacket() {
   }

   public FallTreePacket(BlockPos var1) {
      this.a = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = new BlockPos(var1.readInt(), var1.readInt(), var1.readInt());
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.a.getX());
      var1.writeInt(this.a.getY());
      var1.writeInt(this.a.getZ());
   }

   public static class Handler implements IMessageHandler<FallTreePacket, IMessage> {
      public IMessage onMessage(FallTreePacket var1, MessageContext var2) {
         if (var1.b && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP var3 = var2.getServerHandler().player;
                     UUID var4 = KoboldManager.getTribeUUID(var3.getPersistentID());
                     if (var4 == null) {
                        System.out.println("not tribe for player");
                     } else {
                        int var5 = KoboldManager.h_clash81(var4);
                        int var6 = (int)Math.floor(KoboldManager.j_clash76(var4).size() / 2.0);
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
                           BlockPos var8 = this.a_clash16(var7, var1.a);
                           HashSet var9 = KoboldTask.a(var7, var8, var4);
                           PacketHandler.b.sendTo(new SendBlocksPacket(var9, true), var2.getServerHandler().player);
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

      BlockPos a_clash16(World var1, BlockPos var2) {
         if (var1.getBlockState(var2.add(0, -1, 0)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(0, -1, 0));
         } else if (var1.getBlockState(var2.add(1, -1, 0)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(1, -1, 0));
         } else if (var1.getBlockState(var2.add(-1, -1, 0)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(-1, -1, 0));
         } else if (var1.getBlockState(var2.add(0, -1, 1)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(0, -1, 1));
         } else if (var1.getBlockState(var2.add(0, -1, -1)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(0, -1, -1));
         } else if (var1.getBlockState(var2.add(-1, -1, -1)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(-1, -1, -1));
         } else if (var1.getBlockState(var2.add(1, -1, 1)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(1, -1, 1));
         } else if (var1.getBlockState(var2.add(-1, -1, 1)).getBlock() instanceof BlockLog) {
            return this.a_clash16(var1, var2.add(-1, -1, 1));
         } else {
            return var1.getBlockState(var2.add(1, -1, -1)).getBlock() instanceof BlockLog
               ? this.a_clash16(var1, var2.add(1, -1, -1))
               : var2;
         }
      }

   }
}
