package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ResetGirlPacket implements IMessage {
   boolean b;
   UUID c;
   boolean a;

   public ResetGirlPacket() {
      this.b = false;
   }

   public ResetGirlPacket(UUID var1) {
      this.c = var1;
      this.a = false;
      this.b = true;
   }

   public ResetGirlPacket(UUID var1, boolean var2) {
      this.c = var1;
      this.a = var2;
      this.b = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = var1.readBoolean();
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      var1.writeBoolean(this.a);
      this.b = true;
   }

   public static class Handler implements IMessageHandler<ResetGirlPacket, IMessage> {
      public static void a_clash10(BaseGirlEntity var0) {
         var0.reinitTasks();
         if (var0 instanceof AbstractPlayerGirlEntity && var0.field_70170_p.func_152378_a(((AbstractPlayerGirlEntity)var0).getOwnerUserUUID()) != null) {
            PacketHandler.b
               .sendTo(
                  new SetPlayerMovementPacket(true),
                  (EntityPlayerMP)FMLCommonHandler.instance()
                     .getMinecraftServerInstance()
                     .func_71218_a(var0.field_71093_bK)
                     .func_152378_a(((AbstractPlayerGirlEntity)var0).getOwnerUserUUID())
               );
            var0.func_184212_Q().func_187227_b(BaseGirlEntity.D, 1);
            EntityPlayer var1 = var0.field_70170_p.func_152378_a(((AbstractPlayerGirlEntity)var0).getOwnerUserUUID());
            var1.field_71075_bZ.field_75100_b = false;
            var1.func_189654_d(false);
            var1.field_70145_X = false;
            var0.setAnchored(false);
            var0.b(fp.NULL);
            if (var0.getInteractionPlayerUUID() != null) {
               EntityPlayer var2 = var0.field_70170_p.func_152378_a(var0.getInteractionPlayerUUID());
               if (var2 != null) {
                  var2.field_71075_bZ.field_75100_b = false;
                  var2.func_189654_d(false);
                  var2.field_70145_X = false;
               }
            }
         }

         var0.setAnchored(false);
         var0.setInteractionPlayerUUID(null);
         var0.B = null;
         var0.func_189654_d(false);
         var0.field_70145_X = false;
         World var3 = var0.field_70170_p;
         Vec3d var4 = var0.func_174791_d();

         while (var3.func_180495_p(new BlockPos(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c)).func_177230_c() != Blocks.field_150350_a) {
            var4 = var4.func_72441_c(0.0, 1.0, 0.0);
         }

         var0.func_70634_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
      }

      public static void a(EntityPlayerMP var0) {
         if (var0 != null) {
            World var1 = var0.field_70170_p;
            Vec3d var2 = var0.func_174791_d();

            while (var1.func_180495_p(new BlockPos(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c)).func_177230_c() != Blocks.field_150350_a) {
               var2 = var2.func_72441_c(0.0, 1.0, 0.0);
            }

            var0.func_70634_a(var2.field_72450_a, var2.field_72448_b, var2.field_72449_c);
            var0.func_82142_c(false);
            var0.field_70145_X = false;
            var0.func_189654_d(false);
            var0.field_71075_bZ.field_75100_b = false;
            PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), var0);
         }
      }

      public IMessage onMessage(ResetGirlPacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.c)) {
                  if (!var3.field_70170_p.field_72995_K) {
                     if (var3.getInteractionPlayerUUID() != null) {
                        a(FMLCommonHandler.instance().getMinecraftServerInstance().func_184103_al().func_177451_a(var3.getInteractionPlayerUUID()));
                     }

                     if (var1.a) {
                        a_clash10(var3);
                     }
                  }
               }
            });
            return null;
         } else {
            System.out.println("recieved an unvalid message @ResetGirl :(");
            return null;
         }
      }

   }
}
