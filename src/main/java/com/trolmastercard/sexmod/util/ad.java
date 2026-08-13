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

public class ad {
   static final int a = 60;
   public static float[] b;

   public ad() {
      if (a_clash64()) {
         b = new float[60];
      }
   }

   public static boolean a_clash64() {
      return (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ClientChatEvent var1) {
      if (a_clash64()) {
         if ("resetcolor".equalsIgnoreCase(var1.getMessage())) {
            KoboldRenderer.clearBoneColors();
            de.e_clash190();
            GoblinRenderer.clearBoneColors();
            dg.e_clash190();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void c(ClientChatEvent var1) {
      if (a_clash64()) {
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

               Minecraft.func_71410_x()
                  .field_71439_g
                  .func_145747_a(new TextComponentString(String.format("%sSet dev float N.%s from %s to %s", TextFormatting.GRAY, var4, b[var4], var5)));
               b[var4] = var5;
               var1.setCanceled(true);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void b(ClientChatEvent var1) {
      if (a_clash64()) {
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

               Minecraft.func_71410_x()
                  .field_71439_g
                  .func_145747_a(new TextComponentString(String.format("%sdev float N.%s is %s", TextFormatting.YELLOW, var4, b[var4])));
               var1.setCanceled(true);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(LivingHurtEvent var1) {
      if (a_clash64()) {
         EntityPlayerSP var2 = Minecraft.func_71410_x().field_71439_g;
         EntityLivingBase var3 = var1.getEntityLiving();
         if (var3 instanceof KoboldEntity) {
            KoboldEntity var4 = (KoboldEntity)var3;
            UUID var5 = KoboldManager.getTribeUUID(var2.getPersistentID());

            for (KoboldTask var8 : KoboldManager.p_clash79(var5)) {
               this.a_clash65("task: " + var8.d_clash202().name());
               this.a_clash65("workers involved: ");

               for (KoboldEntity var10 : var8.c_clash209()) {
                  this.a_clash65(var10.getDisplayNameText() + " " + var10.getGirlId());
               }
            }

            this.a_clash65("tribe contains my exact reference: " + KoboldManager.n_clash82(var5).contains(var4));
            this.a_clash65("tribe contains my ID: ");
            boolean var11 = false;

            for (KoboldEntity var14 : KoboldManager.n_clash82(var5)) {
               if (var14.getGirlId().equals(var4.getGirlId())) {
                  var11 = true;
               }
            }

            boolean var13 = false;

            for (Entry var16 : KoboldManager.a_clash91(var5, var2.field_70170_p).entrySet()) {
               if (((UUID)var16.getKey()).equals(var4.getGirlId())) {
                  var13 = true;
               }
            }

            this.a_clash65("loaded : " + var11);
            this.a_clash65("saved : " + var13);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void d(ClientChatEvent var1) {
      if (a_clash64()) {
         String var2 = var1.getOriginalMessage().toLowerCase();
         EntityPlayerSP var3 = Minecraft.func_71410_x().field_71439_g;
         if ("time".equals(var2)) {
            var3.func_145747_a(new TextComponentString(String.valueOf(var3.field_70170_p.func_82737_E())));
         }

         if ("girls".equals(var2)) {
            List var4 = var3.field_70170_p.func_175644_a(BaseGirlEntity.class, var0 -> true);
            var3.func_145747_a(new TextComponentString(String.valueOf(var4.size())));

            for (BaseGirlEntity var6 : (java.util.Collection<BaseGirlEntity>) (var4) ) {
               System.out.printf("%s at %s %s %s\n", var6, var6.field_70165_t, var6.field_70163_u, var6.field_70161_v);
            }
         }

         if ("kobs".equals(var2)) {
            UUID var11 = KoboldManager.getTribeUUID(var3.getPersistentID());
            int var13 = KoboldManager.h_clash81(var11);

            for (KoboldEntity var8 : KoboldManager.n_clash82(var11)) {
               this.a_clash65(
                  String.format(
                     "alive member %s at %s world.isremote? %s isdead %s girlID %s entityID %s",
                     var8.getDisplayNameText(),
                     var8.func_180425_c(),
                     var8.field_70170_p.field_72995_K,
                     var8.field_70128_L,
                     var8.getGirlId(),
                     var8.func_145782_y()
                  )
               );
               this.a_clash65(
                  var3.field_70170_p.func_72872_a(KoboldEntity.class, new AxisAlignedBB(var8.func_180425_c())).isEmpty()
                     ? "couldn't be located"
                     : "appears to actually exist"
               );
            }

            HashMap var16 = KoboldManager.a_clash91(var11, var3.field_70170_p);

            for (Entry var9 : (java.util.Set<Entry>) var16.entrySet()) {
               this.a_clash65(String.format("saved pos of %s at %s", ((UUID)var9.getKey()).toString(), ((BlockPos)var9.getValue()).toString()));
            }

            this.a_clash65("total amount members: " + var13);
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
            var3.func_145747_a(new TextComponentString("set to: " + var14));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void a_clash65(String var1) {
      Minecraft.func_71410_x().field_71439_g.func_145747_a(new TextComponentString(var1));
   }

}
