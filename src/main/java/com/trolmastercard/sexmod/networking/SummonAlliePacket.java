package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER "summon my Allie" request from the Allies lamp's
 * first use. Spawns an {@link AllieEntity} two blocks in front of the player,
 * facing away from him.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Creates the Allie
 * from the player's held item, sets her as the player's interaction partner,
 * disables gravity/clip (she hovers while being summoned) and picks her summon
 * action: {@code SUMMON_SAND} if standing on sand, {@code SUMMON} if she holds a
 * lamp, otherwise {@code SUMMON_NORMAL}.
 */
public class SummonAlliePacket implements IMessage {
   boolean isValid = false;

   public void fromBytes(ByteBuf var1) {
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
   }

   public static class Handler implements IMessageHandler<SummonAlliePacket, IMessage> {
      public IMessage onMessage(SummonAlliePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP var1x = var2.getServerHandler().player;
                     Vec3d var2x = var1x.getPositionVector()
                        .add(-Math.sin(var1x.rotationYawHead * (Math.PI / 180.0)) * 2.0, 0.0, Math.cos(var1x.rotationYawHead * (Math.PI / 180.0)) * 2.0);
                     AllieEntity var3 = new AllieEntity(var1x.world, var1x.getHeldItemMainhand());
                     var3.setInteractionPlayerUUID(var1x.getPersistentID());
                     var3.setPositionAndRotation(var2x.x, var2x.y, var2x.z, var1x.rotationYawHead + 180.0F, var1x.rotationPitch);
                     var3.setTargetPosition(var3.getPositionVector());
                     var3.setYawRotation(var1x.rotationYawHead + 180.0F);
                     var3.setNoGravity(true);
                     var3.noClip = true;
                     var1x.world.spawnEntity(var3);
                     BlockPos var4 = var3.getPosition().add(0, -1, 0);
                     if (var3.world.getBlockState(var4).getBlock().equals(Blocks.SAND)) {
                        var3.setCurrentAction(Action.SUMMON_SAND);
                     } else {
                        var3.setCurrentAction(var3.hasLampItem() ? Action.SUMMON : Action.SUMMON_NORMAL);
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @SummonAllie :(");
            return null;
         }
      }

   }
}
