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

/**
 * <b>Role.</b> Developer-only debug tooling, active only in a deobfuscated
 * (dev) environment — see {@link #isDeobfuscated()}. Registers client chat
 * commands ({@code set N value}, {@code get N}, {@code time}, {@code girls},
 * {@code kobs}, {@code setcumtime}, {@code resetcolor}) and prints tribe-task
 * diagnostics when a kobold is hurt. The shared dev-float array {@code b} is a
 * tuning surface for renderer constants; keep all debug logging behind the
 * deobfuscated check so obfuscated builds never touch it.
 */
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
   public void onDebugResetColor(ClientChatEvent event) {
      if (isDeobfuscated()) {
         if ("resetcolor".equalsIgnoreCase(event.getMessage())) {
            KoboldRenderer.clearBoneColors();
            PlayerKoboldRenderer.clearRenderCache();
            GoblinRenderer.clearBoneColors();
            PlayerGoblinRenderer.clearRenderCache();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugSetCommand(ClientChatEvent event) {
      if (isDeobfuscated()) {
         String message = event.getOriginalMessage();
         String[] parts = message.split(" ");
         if (parts.length == 3) {
            if ("set".equalsIgnoreCase(parts[0])) {
               int index;
               float value;
               try {
                  index = Integer.parseInt(parts[1]);
                  value = Float.parseFloat(parts[2]);
                  if (b.length - 1 < index) {
                     return;
                  }
               } catch (Exception exception) {
                  return;
               }

               Minecraft.getMinecraft()
                  .player
                  .sendMessage(new TextComponentString(String.format("%sSet dev float N.%s from %s to %s", TextFormatting.GRAY, index, b[index], value)));
               b[index] = value;
               event.setCanceled(true);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugGetCommand(ClientChatEvent event) {
      if (isDeobfuscated()) {
         String message = event.getOriginalMessage();
         String[] parts = message.split(" ");
         if (parts.length == 2) {
            if ("get".equalsIgnoreCase(parts[0])) {
               int index;
               try {
                  index = Integer.parseInt(parts[1]);
                  if (b.length - 1 < index) {
                     return;
                  }
               } catch (Exception exception) {
                  return;
               }

               Minecraft.getMinecraft()
                  .player
                  .sendMessage(new TextComponentString(String.format("%sdev float N.%s is %s", TextFormatting.YELLOW, index, b[index])));
               event.setCanceled(true);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent event) {
      if (isDeobfuscated()) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         EntityLivingBase hurtEntity = event.getEntityLiving();
         if (hurtEntity instanceof KoboldEntity) {
            KoboldEntity kobold = (KoboldEntity)hurtEntity;
            UUID tribeUuid = KoboldManager.getTribeUUID(player.getPersistentID());

            for (KoboldTask task : KoboldManager.getTribeTasks(tribeUuid)) {
               this.sendDebugMessage("task: " + task.getTaskType().name());
               this.sendDebugMessage("workers involved: ");

               for (KoboldEntity worker : task.getWorkers()) {
                  this.sendDebugMessage(worker.getDisplayNameText() + " " + worker.getGirlId());
               }
            }

            this.sendDebugMessage("tribe contains my exact reference: " + KoboldManager.getTribeMembersList(tribeUuid).contains(kobold));
            this.sendDebugMessage("tribe contains my ID: ");
            boolean memberFound = false;

            for (KoboldEntity member : KoboldManager.getTribeMembersList(tribeUuid)) {
               if (member.getGirlId().equals(kobold.getGirlId())) {
                  memberFound = true;
               }
            }

            boolean savedFound = false;

            for (Entry entry : KoboldManager.getTribeSavedPositions(tribeUuid, player.world).entrySet()) {
               if (((UUID)entry.getKey()).equals(kobold.getGirlId())) {
                  savedFound = true;
               }
            }

            this.sendDebugMessage("loaded : " + memberFound);
            this.sendDebugMessage("saved : " + savedFound);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onDebugTimeCommand(ClientChatEvent event) {
      if (isDeobfuscated()) {
         String command = event.getOriginalMessage().toLowerCase();
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         if ("time".equals(command)) {
            player.sendMessage(new TextComponentString(String.valueOf(player.world.getTotalWorldTime())));
         }

         if ("girls".equals(command)) {
            List girls = player.world.getEntities(BaseGirlEntity.class, girl -> true);
            player.sendMessage(new TextComponentString(String.valueOf(girls.size())));

            for (BaseGirlEntity girl : (java.util.Collection<BaseGirlEntity>) (girls) ) {
               System.out.printf("%s at %s %s %s\n", girl, girl.posX, girl.posY, girl.posZ);
            }
         }

         if ("kobs".equals(command)) {
            UUID tribeUuid = KoboldManager.getTribeUUID(player.getPersistentID());
            int memberCount = KoboldManager.getTribeMemberCount(tribeUuid);

            for (KoboldEntity kobold : KoboldManager.getTribeMembersList(tribeUuid)) {
               this.sendDebugMessage(
                  String.format(
                     "alive member %s at %s world.isremote? %s isdead %s girlID %s entityID %s",
                     kobold.getDisplayNameText(),
                     kobold.getPosition(),
                     kobold.world.isRemote,
                     kobold.isDead,
                     kobold.getGirlId(),
                     kobold.getEntityId()
                  )
               );
               this.sendDebugMessage(
                  player.world.getEntitiesWithinAABB(KoboldEntity.class, new AxisAlignedBB(kobold.getPosition())).isEmpty()
                     ? "couldn't be located"
                     : "appears to actually exist"
               );
            }

            HashMap savedPositions = KoboldManager.getTribeSavedPositions(tribeUuid, player.world);

            for (Entry entry : (java.util.Set<Entry>) savedPositions.entrySet()) {
               this.sendDebugMessage(String.format("saved pos of %s at %s", ((UUID)entry.getKey()).toString(), ((BlockPos)entry.getValue()).toString()));
            }

            this.sendDebugMessage("total amount members: " + memberCount);
         }

         if (command.startsWith("setcumtime ")) {
            String[] parts = command.split(" ");

            long cumTime;
            try {
               cumTime = Long.parseLong(parts[1]);
            } catch (NullPointerException exception) {
               System.out.println("long: " + parts[1]);
               exception.printStackTrace();
               return;
            }

            GirlSavedData.saveCumTime(player.getPersistentID(), cumTime);
            player.sendMessage(new TextComponentString("set to: " + cumTime));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void sendDebugMessage(String message) {
      Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
   }

}
