package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.an;







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
   boolean c = false;
   BlockPos a;
   EnumFacing b;

   public MinePacket() {
   }

   public MinePacket(BlockPos var1, EnumFacing var2) {
      this.a = var1;
      this.b = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = new BlockPos(var1.readInt(), var1.readInt(), var1.readInt());
      this.b = EnumFacing.func_176739_a(ByteBufUtils.readUTF8String(var1));
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.a.func_177958_n());
      var1.writeInt(this.a.func_177956_o());
      var1.writeInt(this.a.func_177952_p());
      ByteBufUtils.writeUTF8String(var1, this.b.func_176610_l());
   }

   public static class Handler implements IMessageHandler<MinePacket, IMessage> {
      public IMessage onMessage(MinePacket var1, MessageContext var2) {
         if (var1.c && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .func_152344_a(
                  () -> {
                     EntityPlayerMP var3 = var2.getServerHandler().field_147369_b;
                     UUID var4 = KoboldManager.a_clash88(var3.getPersistentID());
                     if (var4 != null) {
                        int var5 = KoboldManager.h_clash81(var4);
                        int var6 = (int)Math.floor(KoboldManager.j_clash76(var4).size() / 2.0);
                        if (var5 > var6) {
                           var3.func_145747_a(
                              new TextComponentString(
                                 String.format(
                                    "sUr Tribe will only work for you, if %severyone%s of them has a %sbed",
                                    TextFormatting.RED,
                                    TextFormatting.WHITE,
                                    TextFormatting.RED
                                 )
                              )
                           );
                           var3.func_145747_a(new TextComponentString(String.format("%s%d/%d Beds", TextFormatting.YELLOW, var6, var5)));
                        } else {
                           HashSet var7 = this.a(var1.a, var1.b);
                           World var8 = var2.getServerHandler().field_147369_b.field_70170_p;

                           for (BlockPos var10 : (java.util.Collection<BlockPos>) (var7) ) {
                              IBlockState var11 = var8.func_180495_p(var10);
                              if (var11.func_177230_c().func_176195_g(var11, var8, var10) < 0.0F) {
                                 var3.func_146105_b(new TextComponentString("This area contains Bedrock and cannot be mined"), true);
                                 return;
                              }
                           }

                           KoboldTask var12 = new KoboldTask(var1.a, KoboldTask.TaskType.MINE, var7, var1.b);
                           KoboldManager.b(var4, var12);
                           PacketHandler.b.sendTo(new SendBlocksPacket(var7, true), var2.getServerHandler().field_147369_b);
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
            var3.add(var4.func_177973_b(this.a(var2)));
            var3.add(var4.func_177973_b(this.a(var2)).func_177984_a());
            var3.add(var4.func_177973_b(this.a(var2)).func_177984_a().func_177984_a());
            var3.add(var4);
            var3.add(var4.func_177984_a());
            var3.add(var4.func_177984_a().func_177984_a());
            var3.add(var4.func_177971_a(this.a(var2)));
            var3.add(var4.func_177971_a(this.a(var2)).func_177984_a());
            var3.add(var4.func_177971_a(this.a(var2)).func_177984_a().func_177984_a());
            var4 = var4.func_177971_a(var2.func_176730_m());
         }

         return var3;
      }

      BlockPos a(EnumFacing var1) {
         Vec3i var2 = var1.func_176730_m();
         return new BlockPos(var2.func_177952_p(), var2.func_177956_o(), -var2.func_177958_n());
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
