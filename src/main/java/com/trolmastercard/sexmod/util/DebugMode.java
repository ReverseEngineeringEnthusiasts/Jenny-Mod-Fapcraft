package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DebugMode {
   static final int debugFlag = 60;
   public static float[] b;

   public DebugMode() {
      if (isDeobfuscated()) {
         b = new float[60];
      }
   }

   public static boolean isDeobfuscated() {
      return (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugResetColor(ClientChatEvent var1) {
      if (isDeobfuscated()) {
         if ("resetcolor".equalsIgnoreCase(var1.getMessage())) {
            KoboldRenderer.clearBoneColors();
            PlayerKoboldRenderer.clearRenderCache();
            GoblinRenderer.clearBoneColors();
            PlayerGoblinRenderer.clearRenderCache();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugSetCommand(ClientChatEvent var1) {
      if (isDeobfuscated()) {
         String var2 = var1.getOriginalMessage();
         String[] var3 = var2.split(" ");
         if (var3.length == 3) {
            if ("set".equalsIgnoreCase(var3[0])) {
               int var4;
               float var5;
               try {
                  var4 = Integer.parseInt(var3[1]);
                  var5 = Float.parseFloat(var3[2]);
                  if (b.length - 1 < var4) {
                     return;
                  }
               } catch (Exception var6) {
                  return;
               }

               Minecraft.getMinecraft()
                  .player
                  .sendMessage(new TextComponentString(String.format("%sSet dev float N.%s from %s to %s", TextFormatting.GRAY, var4, b[var4], var5)));
               b[var4] = var5;
               var1.setCanceled(true);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugGetCommand(ClientChatEvent var1) {
      if (isDeobfuscated()) {
         String var2 = var1.getOriginalMessage();
         String[] var3 = var2.split(" ");
         if (var3.length == 2) {
            if ("get".equalsIgnoreCase(var3[0])) {
               int var4;
               try {
                  var4 = Integer.parseInt(var3[1]);
                  if (b.length - 1 < var4) {
                     return;
                  }
               } catch (Exception var5) {
                  return;
               }

               Minecraft.getMinecraft()
                  .player
                  .sendMessage(new TextComponentString(String.format("%sdev float N.%s is %s", TextFormatting.YELLOW, var4, b[var4])));
               var1.setCanceled(true);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent var1) {
      if (isDeobfuscated()) {
         EntityPlayerSP var2 = Minecraft.getMinecraft().player;
         EntityLivingBase var3 = var1.getEntityLiving();
         if (var3 instanceof KoboldEntity) {
            KoboldEntity var4 = (KoboldEntity)var3;
            UUID var5 = KoboldManager.getTribeUUID(var2.getPersistentID());

            for (KoboldTask var8 : KoboldManager.getTribeTasks(var5)) {
               this.sendDebugMessage("task: " + var8.getTaskType().name());
               this.sendDebugMessage("workers involved: ");

               for (KoboldEntity var10 : var8.getWorkers()) {
                  this.sendDebugMessage(var10.getDisplayNameText() + " " + var10.getGirlId());
               }
            }

            this.sendDebugMessage("tribe contains my exact reference: " + KoboldManager.getTribeMembersList(var5).contains(var4));
            this.sendDebugMessage("tribe contains my ID: ");
            boolean var11 = false;

            for (KoboldEntity var14 : KoboldManager.getTribeMembersList(var5)) {
               if (var14.getGirlId().equals(var4.getGirlId())) {
                  var11 = true;
               }
            }

            boolean var13 = false;

            for (Entry var16 : KoboldManager.getTribeSavedPositions(var5, var2.world).entrySet()) {
               if (((UUID)var16.getKey()).equals(var4.getGirlId())) {
                  var13 = true;
               }
            }

            this.sendDebugMessage("loaded : " + var11);
            this.sendDebugMessage("saved : " + var13);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugTimeCommand(ClientChatEvent var1) {
      if (isDeobfuscated()) {
         String var2 = var1.getOriginalMessage().toLowerCase();
         EntityPlayerSP var3 = Minecraft.getMinecraft().player;
         if ("time".equals(var2)) {
            var3.sendMessage(new TextComponentString(String.valueOf(var3.world.getTotalWorldTime())));
         }

         if ("girls".equals(var2)) {
            List var4 = var3.world.getEntities(BaseGirlEntity.class, var0 -> true);
            var3.sendMessage(new TextComponentString(String.valueOf(var4.size())));

            for (BaseGirlEntity var6 : (java.util.Collection<BaseGirlEntity>) (var4) ) {
               System.out.printf("%s at %s %s %s\n", var6, var6.posX, var6.posY, var6.posZ);
            }
         }

         if ("kobs".equals(var2)) {
            UUID var11 = KoboldManager.getTribeUUID(var3.getPersistentID());
            int var13 = KoboldManager.getTribeMemberCount(var11);

            for (KoboldEntity var8 : KoboldManager.getTribeMembersList(var11)) {
               this.sendDebugMessage(
                  String.format(
                     "alive member %s at %s world.isremote? %s isdead %s girlID %s entityID %s",
                     var8.getDisplayNameText(),
                     var8.getPosition(),
                     var8.world.isRemote,
                     var8.isDead,
                     var8.getGirlId(),
                     var8.getEntityId()
                  )
               );
               this.sendDebugMessage(
                  var3.world.getEntitiesWithinAABB(KoboldEntity.class, new AxisAlignedBB(var8.getPosition())).isEmpty()
                     ? "couldn't be located"
                     : "appears to actually exist"
               );
            }

            HashMap var16 = KoboldManager.getTribeSavedPositions(var11, var3.world);

            for (Entry var9 : (java.util.Set<Entry>) var16.entrySet()) {
               this.sendDebugMessage(String.format("saved pos of %s at %s", ((UUID)var9.getKey()).toString(), ((BlockPos)var9.getValue()).toString()));
            }

            this.sendDebugMessage("total amount members: " + var13);
         }

         if (var2.startsWith("setcumtime ")) {
            String[] var12 = var2.split(" ");

            long var14;
            try {
               var14 = Long.parseLong(var12[1]);
            } catch (NullPointerException var10) {
               System.out.println("long: " + var12[1]);
               var10.printStackTrace();
               return;
            }

            GirlSavedData.a(var3.getPersistentID(), var14);
            var3.sendMessage(new TextComponentString("set to: " + var14));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void sendDebugMessage(String var1) {
      Minecraft.getMinecraft().player.sendMessage(new TextComponentString(var1));
   }

}
