package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Right-click-on-bed interaction hook used by bed scenes (girls walking a
 * player to bed / waking up).
 */
public class GirlBedInteraction {

   @SubscribeEvent
   public void onBlockBreak(BreakEvent event) {
      Block block = event.getState().getBlock();
      if (block == Blocks.BED) {
         BlockPos pos = event.getPos();
         AxisAlignedBB aabb = new AxisAlignedBB(
            pos.getX() - 3,
            pos.getY() - 3,
            pos.getZ() - 3,
            pos.getX() + 3,
            pos.getY() + 3,
            pos.getZ() + 3
         );
         List girls = event.getWorld().getEntitiesWithinAABB(BaseGirlEntity.class, aabb);
         boolean found = false;

         for (BaseGirlEntity girl : (java.util.Collection<BaseGirlEntity>) (girls) ) {
            if (!girl.isDead && (Boolean)girl.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
               found = true;
               break;
            }
         }

         if (found) {
            event.getPlayer()
               .sendStatusMessage(new TextComponentString("this bed is currently used by a girl.. pls don't disturb okay? ... you are kinda mean rn"), true);
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {
      if (BaseGirlEntity.getActiveSceneInfo(event.getEntityPlayer()) != null) {
         event.setCanceled(true);
      }
   }

}
