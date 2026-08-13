package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.gm;







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
   boolean b = false;
   HashSet<BlockPos> c = new HashSet<>();
   boolean a;

   public SendBlocksPacket() {
   }

   public SendBlocksPacket(HashSet<BlockPos> var1, boolean var2) {
      this.c = var1;
      this.a = var2;
   }

   public SendBlocksPacket(BlockPos var1, boolean var2) {
      this.c.add(var1);
      this.a = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = var1.readBoolean();
      int var2 = var1.readInt();

      for (int var3 = 0; var3 < var2; var3++) {
         this.c.add(new BlockPos(var1.readInt(), var1.readInt(), var1.readInt()));
      }

      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.a);
      var1.writeInt(this.c.size());

      for (BlockPos var3 : this.c) {
         var1.writeInt(var3.func_177958_n());
         var1.writeInt(var3.func_177956_o());
         var1.writeInt(var3.func_177952_p());
      }
   }


   public static class Handler implements IMessageHandler<SendBlocksPacket, IMessage> {
      public IMessage onMessage(SendBlocksPacket var1, MessageContext var2) {
         if (!var1.b) {
            System.out.println("received an invalid Message @SendBlocks :(");
            return null;
         }

         if (var2.side.isClient()) {
            if (var1.a) {
               gm.a_clash774(var1.c);
            } else {
               gm.b(var1.c);
            }

            return null;
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .func_152344_a(
                  () -> {
                     UUID var2x = var2.getServerHandler().field_147369_b.getPersistentID();
                     UUID var3 = KoboldManager.getTribeUUID(var2x);
                     if (var3 != null) {
                        if (var1.c.size() == 1) {
                           World var4 = var2.getServerHandler().field_147369_b.field_70170_p;

                           for (BlockPos var6 : var1.c) {
                              IBlockState var7 = var4.func_180495_p(var6);
                              BlockPos var8 = null;
                              if (var7.func_177230_c() instanceof BlockBed) {
                                 var8 = cj.a(var6, var7);
                              }

                              if (var7.func_177230_c() instanceof BlockChest) {
                                 Type var9 = ((BlockChest)var7.func_177230_c()).field_149956_a;
                                 if (var4.func_180495_p(var6.func_177978_c()).func_177230_c() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.func_180495_p(var6.func_177978_c()).func_177230_c()).field_149956_a)) {
                                    var8 = var6.func_177978_c();
                                 }

                                 if (var4.func_180495_p(var6.func_177974_f()).func_177230_c() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.func_180495_p(var6.func_177974_f()).func_177230_c()).field_149956_a)) {
                                    var8 = var6.func_177974_f();
                                 }

                                 if (var4.func_180495_p(var6.func_177968_d()).func_177230_c() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.func_180495_p(var6.func_177968_d()).func_177230_c()).field_149956_a)) {
                                    var8 = var6.func_177968_d();
                                 }

                                 if (var4.func_180495_p(var6.func_177976_e()).func_177230_c() instanceof BlockChest
                                    && var9.equals(((BlockChest)var4.func_180495_p(var6.func_177976_e()).func_177230_c()).field_149956_a)) {
                                    var8 = var6.func_177976_e();
                                 }
                              }

                              if (var8 == null && var7.func_177230_c() instanceof BlockBed) {
                                 return;
                              }

                              if (var1.a) {
                                 if (var7.func_177230_c() instanceof BlockBed) {
                                    KoboldManager.a(var3, var6);
                                    KoboldManager.a(var3, var8);
                                 } else {
                                    KoboldManager.f(var3, var6);
                                    KoboldManager.f(var3, var8);
                                 }
                              } else if (var7.func_177230_c() instanceof BlockBed) {
                                 KoboldManager.e(var3, var6);
                                 KoboldManager.e(var3, var8);
                              } else {
                                 KoboldManager.d(var3, var6);
                                 KoboldManager.d(var3, var8);
                              }

                              HashSet var10 = new HashSet();
                              var10.add(var6);
                              if (var8 != null) {
                                 var10.add(var8);
                              }

                              PacketHandler.b.sendTo(new SendBlocksPacket(var10, var1.a), var2.getServerHandler().field_147369_b);
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
