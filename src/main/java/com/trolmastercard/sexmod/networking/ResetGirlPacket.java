package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.TrailSegment;







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
   boolean isValid;
   UUID girlUUID;
   boolean resetPose;

   public ResetGirlPacket() {
      this.isValid = false;
   }

   public ResetGirlPacket(UUID var1) {
      this.girlUUID = var1;
      this.resetPose = false;
      this.isValid = true;
   }

   public ResetGirlPacket(UUID var1, boolean var2) {
      this.girlUUID = var1;
      this.resetPose = var2;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.resetPose = var1.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      var1.writeBoolean(this.resetPose);
      this.isValid = true;
   }

   public static class Handler implements IMessageHandler<ResetGirlPacket, IMessage> {
      public static void a_clash10(BaseGirlEntity var0) {
         var0.reinitTasks();
         if (var0 instanceof AbstractPlayerGirlEntity && var0.world.getPlayerEntityByUUID(((AbstractPlayerGirlEntity)var0).getOwnerUserUUID()) != null) {
            PacketHandler.networkWrapper
               .sendTo(
                  new SetPlayerMovementPacket(true),
                  (EntityPlayerMP)FMLCommonHandler.instance()
                     .getMinecraftServerInstance()
                     .getWorld(var0.dimension)
                     .getPlayerEntityByUUID(((AbstractPlayerGirlEntity)var0).getOwnerUserUUID())
               );
            var0.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, 1);
            EntityPlayer var1 = var0.world.getPlayerEntityByUUID(((AbstractPlayerGirlEntity)var0).getOwnerUserUUID());
            var1.capabilities.isFlying = false;
            var1.setNoGravity(false);
            var1.noClip = false;
            var0.setAnchored(false);
            var0.setCurrentAction(Action.NULL);
            if (var0.getInteractionPlayerUUID() != null) {
               EntityPlayer var2 = var0.world.getPlayerEntityByUUID(var0.getInteractionPlayerUUID());
               if (var2 != null) {
                  var2.capabilities.isFlying = false;
                  var2.setNoGravity(false);
                  var2.noClip = false;
               }
            }
         }

         var0.setAnchored(false);
         var0.setInteractionPlayerUUID(null);
         var0.cameraOriginPos = null;
         var0.setNoGravity(false);
         var0.noClip = false;
         World var3 = var0.world;
         Vec3d var4 = var0.getPositionVector();

         while (var3.getBlockState(new BlockPos(var4.x, var4.y, var4.z)).getBlock() != Blocks.AIR) {
            var4 = var4.add(0.0, 1.0, 0.0);
         }

         var0.setPositionAndUpdate(var4.x, var4.y, var4.z);
      }

      public static void a(EntityPlayerMP var0) {
         if (var0 != null) {
            World var1 = var0.world;
            Vec3d var2 = var0.getPositionVector();

            while (var1.getBlockState(new BlockPos(var2.x, var2.y, var2.z)).getBlock() != Blocks.AIR) {
               var2 = var2.add(0.0, 1.0, 0.0);
            }

            var0.setPositionAndUpdate(var2.x, var2.y, var2.z);
            var0.setInvisible(false);
            var0.noClip = false;
            var0.setNoGravity(false);
            var0.capabilities.isFlying = false;
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), var0);
         }
      }

      public IMessage onMessage(ResetGirlPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.girlUUID)) {
                  if (!var3.world.isRemote) {
                     if (var3.getInteractionPlayerUUID() != null) {
                        a(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(var3.getInteractionPlayerUUID()));
                     }

                     if (var1.resetPose) {
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
