package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.SceneDebug;
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

/**
 * Scene-end packet, CLIENT -&gt; SERVER.
 * <p>
 * <b>The {@code resetPose} flag is INVERTED relative to intuition</b>
 * (jar-verified against the original bytecode):
 * <ul>
 *   <li><b>single-arg ctor</b> {@code ResetGirlPacket(uuid)} = resetPose
 *       {@code false} = <b>FULL scene-end reset</b>: restores the interacting
 *       player's physics ({@link Handler#resetGirls}) AND releases the girl
 *       ({@link Handler#resetGirl} — re-adds AI tasks, un-anchors, clears the
 *       interaction partner, restores gravity/noClip, teleports her to air).</li>
 *   <li><b>two-arg ctor</b> {@code ResetGirlPacket(uuid, true)} = resetPose
 *       {@code true} = <b>player-only reset</b> used by strip/doggy
 *       transitions where the girl keeps her pose.</li>
 * </ul>
 * <p>
 * The natural scene end sends the single-arg form: the cum animation's
 * {@code xxx_cumDone} sound keyframe calls
 * {@link BaseGirlEntity#resetCameraAndPhysics()} -&gt;
 * {@code resetLocalPlayerClientState()} which sends this packet with the
 * girl's UUID. The R-Shift keybind ({@code SexSceneKeyHandler}) sends it too.
 * <p>
 * <b>Pitfall:</b> this boolean was once inverted in the remap — the full
 * reset only ran on {@code true}, so every natural scene end left the girl
 * anchored/noGravity/still-interacting. Never "fix" the branch condition
 * without re-verifying against the original jar.
 */
public class ResetGirlPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   boolean resetPose;

   public ResetGirlPacket() {
      this.isValid = false;
   }

   public ResetGirlPacket(UUID girlUUID) {
      this.girlUUID = girlUUID;
      this.resetPose = false;
      this.isValid = true;
   }

   public ResetGirlPacket(UUID girlUUID, boolean resetPose) {
      this.girlUUID = girlUUID;
      this.resetPose = resetPose;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.resetPose = buf.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      buf.writeBoolean(this.resetPose);
      this.isValid = true;
   }

   public static class Handler implements IMessageHandler<ResetGirlPacket, IMessage> {
      public static void resetGirl(BaseGirlEntity girl) {
         SceneDebug.log(SceneDebug.RESET, "ResetGirlPacket.resetGirl %s remote=%s anchored=%s action=%s interact=%s", girl.getDisplayNameText(), girl.world.isRemote, girl.isAnchored(), girl.getCurrentAction(), girl.getInteractionPlayerUUID());
         girl.reinitTasks();
         if (girl instanceof AbstractPlayerGirlEntity && girl.world.getPlayerEntityByUUID(((AbstractPlayerGirlEntity)girl).getOwnerUserUUID()) != null) {
            PacketHandler.networkWrapper
               .sendTo(
                  new SetPlayerMovementPacket(true),
                  (EntityPlayerMP)FMLCommonHandler.instance()
                     .getMinecraftServerInstance()
                     .getWorld(girl.dimension)
                     .getPlayerEntityByUUID(((AbstractPlayerGirlEntity)girl).getOwnerUserUUID())
               );
            girl.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, 1);
            EntityPlayer owner = girl.world.getPlayerEntityByUUID(((AbstractPlayerGirlEntity)girl).getOwnerUserUUID());
            owner.capabilities.isFlying = false;
            owner.setNoGravity(false);
            owner.noClip = false;
            girl.setAnchored(false);
            girl.setCurrentAction(Action.NULL);

            // Jar-faithful: the interacting player's physics are restored here,
            // inside the APGE branch only. For NPC girl scenes the player is
            // restored by resetGirls() in onMessage, which runs whenever the
            // girl has an interaction partner (before the resetPose check).
            if (girl.getInteractionPlayerUUID() != null) {
               EntityPlayer interactionPlayer = girl.world.getPlayerEntityByUUID(girl.getInteractionPlayerUUID());
               if (interactionPlayer != null) {
                  interactionPlayer.capabilities.isFlying = false;
                  interactionPlayer.setNoGravity(false);
                  interactionPlayer.noClip = false;
               }
            }
         }

         girl.setAnchored(false);
         girl.setInteractionPlayerUUID(null);
         girl.cameraOriginPos = null;
         girl.setNoGravity(false);
         girl.noClip = false;
         World world = girl.world;
         Vec3d pos = girl.getPositionVector();

         while (world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() != Blocks.AIR) {
            pos = pos.add(0.0, 1.0, 0.0);
         }

         girl.setPositionAndUpdate(pos.x, pos.y, pos.z);
      }

      public static void resetGirls(EntityPlayerMP player) {
         if (player != null) {
            World world = player.world;
            Vec3d pos = player.getPositionVector();

            while (world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() != Blocks.AIR) {
               pos = pos.add(0.0, 1.0, 0.0);
            }

            player.setPositionAndUpdate(pos.x, pos.y, pos.z);
            player.setInvisible(false);
            player.noClip = false;
            player.setNoGravity(false);
            player.capabilities.isFlying = false;
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), player);
         }
      }

      public IMessage onMessage(ResetGirlPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                  if (!girl.world.isRemote) {
                     SceneDebug.log(SceneDebug.RESET, "ResetGirlPacket.onMessage girl=%s resetPose=%s action=%s anchored=%s interact=%s", girl.getDisplayNameText(), packet.resetPose, girl.getCurrentAction(), girl.isAnchored(), girl.getInteractionPlayerUUID());
                     if (girl.getInteractionPlayerUUID() != null) {
                        resetGirls(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(girl.getInteractionPlayerUUID()));
                     }

                     // Jar-faithful (verified against Fapcraft.1.12.2.v1.1.jar
                     // bytecode): resetGirl() runs when resetPose == FALSE. The
                     // single-arg packet is the full scene-end reset (player
                     // physics via resetGirls + girl release via resetGirl); the
                     // two-arg TRUE packet is the player-only reset used by
                     // strip/doggy transitions where the girl keeps her pose.
                     if (!packet.resetPose) {
                        resetGirl(girl);
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
