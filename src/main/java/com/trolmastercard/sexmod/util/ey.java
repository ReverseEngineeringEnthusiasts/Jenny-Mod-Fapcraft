package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.by;
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

public class ey {
   static final int a = 3;

   @SubscribeEvent
   public void a(BreakEvent var1) {
      Block var2 = var1.getState().getBlock();
      if (var2 == Blocks.BED) {
         BlockPos var3 = var1.getPos();
         AxisAlignedBB var4 = new AxisAlignedBB(
            var3.getX() - 3,
            var3.getY() - 3,
            var3.getZ() - 3,
            var3.getX() + 3,
            var3.getY() + 3,
            var3.getZ() + 3
         );
         List var5 = var1.getWorld().getEntitiesWithinAABB(BaseGirlEntity.class, var4);
         boolean var6 = false;

         for (BaseGirlEntity var8 : (java.util.Collection<BaseGirlEntity>) (var5) ) {
            if (!var8.isDead && (Boolean)var8.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
               var6 = true;
               break;
            }
         }

         if (var6) {
            var1.getPlayer()
               .sendStatusMessage(new TextComponentString("this bed is currently used by a girl.. pls don't disturb okay? ... you are kinda mean rn"), true);
            var1.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void a(PlayerSPPushOutOfBlocksEvent var1) {
      if (BaseGirlEntity.getActiveSceneInfo(var1.getEntityPlayer()) != null) {
         var1.setCanceled(true);
      }
   }

}
