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

public class SendBlocksPacket implements IMessage {
   boolean isValid = false;
   HashSet<BlockPos> blockPositions = new HashSet<>();
   boolean isBreaking;

   public SendBlocksPacket() {
   }

   public SendBlocksPacket(HashSet<BlockPos> var1, boolean var2) {
      this.blockPositions = var1;
      this.isBreaking = var2;
   }

   public SendBlocksPacket(BlockPos var1, boolean var2) {
      this.blockPositions.add(var1);
      this.isBreaking = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.isBreaking = var1.readBoolean();
      int var2 = var1.readInt();

      for (int var3 = 0; var3 < var2; var3++) {
         this.blockPositions.add(new BlockPos(var1.readInt(), var1.readInt(), var1.readInt()));
      }

      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.isBreaking);
      var1.writeInt(this.blockPositions.size());

      for (BlockPos var3 : this.blockPositions) {
         var1.writeInt(var3.getX());
         var1.writeInt(var3.getY());
         var1.writeInt(var3.getZ());
      }
   }

   public static class Handler implements IMessageHandler<SendBlocksPacket, IMessage> {
      public IMessage onMessage(SendBlocksPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid Message @SendBlocks :(");
            return null;
         }

         if (var2.side.isClient()) {
            if (var1.isBreaking) {
               StructureMarkerRenderer.renderMarkers(var1.blockPositions);
            } else {
               StructureMarkerRenderer.b(var1.blockPositions);
            }

            return null;
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     UUID var2x = var2.getServerHandler().player.getPersistentID();
                     UUID var3 = KoboldManager.getTribeUUID(var2x);
                     if (var3 != null) {
                        if (var1.blockPositions.size() == 1) {
                           World var4 = var2.getServerHandler().player.world;

                           for (BlockPos var6 : var1.blockPositions) {
                              IBlockState var7 = var4.getBlockState(var6);
                              BlockPos var8 = null;
                              if (var7.getBlock() instanceof BlockBed) {
                                 var8 = WorldUtils.a(var6, var7);
                              }

                              if (var7.getBlock() instanceof BlockChest) {
                                 Type var9 = ((BlockChest)var7.getBlock()).chestType;
                                 if (var4.getBlockState(var6.north()).getBlock() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.getBlockState(var6.north()).getBlock()).chestType)) {
                                    var8 = var6.north();
                                 }

                                 if (var4.getBlockState(var6.east()).getBlock() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.getBlockState(var6.east()).getBlock()).chestType)) {
                                    var8 = var6.east();
                                 }

                                 if (var4.getBlockState(var6.south()).getBlock() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.getBlockState(var6.south()).getBlock()).chestType)) {
                                    var8 = var6.south();
                                 }

                                 if (var4.getBlockState(var6.west()).getBlock() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.getBlockState(var6.west()).getBlock()).chestType)) {
                                    var8 = var6.west();
                                 }
                              }

                              if (var8 == null && var7.getBlock() instanceof BlockBed) {
                                 return;
                              }

                              if (var1.isBreaking) {
                                 if (var7.getBlock() instanceof BlockBed) {
                                    KoboldManager.addTribeBed(var3, var6);
                                    KoboldManager.addTribeBed(var3, var8);
                                 } else {
                                    KoboldManager.addTribeChest(var3, var6);
                                    KoboldManager.addTribeChest(var3, var8);
                                 }
                              } else if (var7.getBlock() instanceof BlockBed) {
                                 KoboldManager.removeTribeChest(var3, var6);
                                 KoboldManager.removeTribeChest(var3, var8);
                              } else {
                                 KoboldManager.removeMiningTargetsFor(var3, var6);
                                 KoboldManager.removeMiningTargetsFor(var3, var8);
                              }

                              HashSet var10 = new HashSet();
                              var10.add(var6);
                              if (var8 != null) {
                                 var10.add(var8);
                              }

                              PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var10, var1.isBreaking), var2.getServerHandler().player);
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
