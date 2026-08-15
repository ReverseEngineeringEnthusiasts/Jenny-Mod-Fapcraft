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

   public void fromBytes(ByteBuf buf) {
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
   }

   public static class Handler implements IMessageHandler<SummonAlliePacket, IMessage> {
      public IMessage onMessage(SummonAlliePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     EntityPlayerMP player = ctx.getServerHandler().player;
                     Vec3d spawnPos = player.getPositionVector()
                        .add(-Math.sin(player.rotationYawHead * (Math.PI / 180.0)) * 2.0, 0.0, Math.cos(player.rotationYawHead * (Math.PI / 180.0)) * 2.0);
                     AllieEntity allie = new AllieEntity(player.world, player.getHeldItemMainhand());
                     allie.setInteractionPlayerUUID(player.getPersistentID());
                     allie.setPositionAndRotation(spawnPos.x, spawnPos.y, spawnPos.z, player.rotationYawHead + 180.0F, player.rotationPitch);
                     allie.setTargetPosition(allie.getPositionVector());
                     allie.setYawRotation(player.rotationYawHead + 180.0F);
                     allie.setNoGravity(true);
                     allie.noClip = true;
                     player.world.spawnEntity(allie);
                     BlockPos blockPos = allie.getPosition().add(0, -1, 0);
                     if (allie.world.getBlockState(blockPos).getBlock().equals(Blocks.SAND)) {
                        allie.setCurrentAction(Action.SUMMON_SAND);
                     } else {
                        allie.setCurrentAction(allie.hasLampItem() ? Action.SUMMON : Action.SUMMON_NORMAL);
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
