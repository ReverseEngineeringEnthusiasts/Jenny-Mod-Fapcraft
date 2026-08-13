package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;







import java.lang.reflect.Field;
import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundHandler {
   public static final SoundEvent[] MISC_PLOB = new SoundEvent[1];
   public static final SoundEvent[] MISC_BELLJINGLE = new SoundEvent[1];
   public static final SoundEvent[] MISC_BEDRUSTLE = new SoundEvent[2];
   public static final SoundEvent[] MISC_SLAP = new SoundEvent[2];
   public static final SoundEvent[] MISC_TOUCH = new SoundEvent[2];
   public static final SoundEvent[] MISC_POUNDING = new SoundEvent[35];
   public static final SoundEvent[] MISC_SMALLINSERTS = new SoundEvent[5];
   public static final SoundEvent[] MISC_INSERTS = new SoundEvent[5];
   public static final SoundEvent[] MISC_CUMINFLATION = new SoundEvent[1];
   public static final SoundEvent[] MISC_SCREAM = new SoundEvent[2];
   public static final SoundEvent[] MISC_FART = new SoundEvent[3];
   public static final SoundEvent[] MISC_JUMP = new SoundEvent[1];
   public static final SoundEvent[] MISC_EAT = new SoundEvent[3];
   public static final SoundEvent[] MISC_SLIDE = new SoundEvent[7];
   public static final SoundEvent[] GIRLS_JENNY_AFTERSESSIONMOAN = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_JENNY_AHH = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_JENNY_BJMOAN = new SoundEvent[13];
   public static final SoundEvent[] GIRLS_JENNY_GIGGLE = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_JENNY_HAPPYOH = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_JENNY_HEAVYBREATHING = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_JENNY_HMPH = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_JENNY_HUH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_JENNY_LIGHTBREATHING = new SoundEvent[12];
   public static final SoundEvent[] GIRLS_JENNY_LIPSOUND = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_JENNY_MMM = new SoundEvent[9];
   public static final SoundEvent[] GIRLS_JENNY_MOAN = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_JENNY_SADOH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_JENNY_SIGH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ELLIE_AFTERSESSIONMOAN = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_ELLIE_AHH = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_ELLIE_BJMOAN = new SoundEvent[13];
   public static final SoundEvent[] GIRLS_ELLIE_GIGGLE = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_ELLIE_HAPPYOH = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_ELLIE_HEAVYBREATHING = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_ELLIE_HMPH = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_ELLIE_HUH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ELLIE_LIGHTBREATHING = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_ELLIE_LIPSOUND = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_ELLIE_MMM = new SoundEvent[9];
   public static final SoundEvent[] GIRLS_ELLIE_MOAN = new SoundEvent[9];
   public static final SoundEvent[] GIRLS_ELLIE_SADOH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ELLIE_SIGH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ELLIE_COMETOMOMMY = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ELLIE_GOODBOY = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ELLIE_MOMMYHORNY = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_BIA_AHH = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_BIA_BJMOAN = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_BIA_BREATH = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_BIA_GIGGLE = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_BIA_HEY = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_BIA_HUH = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_BIA_MMM = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_LUNA_AHH = new SoundEvent[18];
   public static final SoundEvent[] GIRLS_LUNA_CUTENYA = new SoundEvent[12];
   public static final SoundEvent[] GIRLS_LUNA_HAPPYOH = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_LUNA_HMPH = new SoundEvent[6];
   public static final SoundEvent[] GIRLS_LUNA_HORNINYA = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_LUNA_HUH = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_LUNA_LIGHTBREATHING = new SoundEvent[25];
   public static final SoundEvent[] GIRLS_LUNA_MMM = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_LUNA_MOAN = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_LUNA_SADOH = new SoundEvent[7];
   public static final SoundEvent[] GIRLS_LUNA_SIGH = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_LUNA_SINGING = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_LUNA_GIGGLE = new SoundEvent[15];
   public static final SoundEvent[] GIRLS_LUNA_OUU = new SoundEvent[13];
   public static final SoundEvent[] GIRLS_LUNA_OWO = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_ALLIE_AFTERSESSIONMOAN = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_ALLIE_AHH = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_ALLIE_BJMOAN = new SoundEvent[14];
   public static final SoundEvent[] GIRLS_ALLIE_GIGGLE = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_ALLIE_HAPPYOH = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_ALLIE_HEAVYBREATHING = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_ALLIE_HMPH = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_ALLIE_HUH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ALLIE_LIGHTBREATHING = new SoundEvent[11];
   public static final SoundEvent[] GIRLS_ALLIE_LIPSOUND = new SoundEvent[14];
   public static final SoundEvent[] GIRLS_ALLIE_MMM = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_ALLIE_MOAN = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_ALLIE_SADOH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ALLIE_SIGH = new SoundEvent[2];
   public static final SoundEvent[] GIRLS_ALLIE_SCAWY = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_KOBOLD_BJMOAN = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_KOBOLD_GIGGLE = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_KOBOLD_HAA = new SoundEvent[7];
   public static final SoundEvent[] GIRLS_KOBOLD_HEYMASTER = new SoundEvent[6];
   public static final SoundEvent[] GIRLS_KOBOLD_INTERESTED = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_KOBOLD_LIGHTBREATHING = new SoundEvent[12];
   public static final SoundEvent[] GIRLS_KOBOLD_MASTER = new SoundEvent[6];
   public static final SoundEvent[] GIRLS_KOBOLD_MOAN = new SoundEvent[10];
   public static final SoundEvent[] GIRLS_KOBOLD_ORGASM = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_KOBOLD_SAD = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_KOBOLD_YEP = new SoundEvent[7];
   public static final SoundEvent[] MISC_FLAP = new SoundEvent[4];
   public static final SoundEvent[] MISC_SHATTER = new SoundEvent[1];
   public static final SoundEvent[] MISC_WEOWEO = new SoundEvent[4];
   public static final SoundEvent[] MISC_BEEW = new SoundEvent[3];
   public static final SoundEvent[] MISC_CLAP = new SoundEvent[1];
   public static final SoundEvent[] GIRLS_GALATH_AHH = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_GALATH_BREATHING = new SoundEvent[7];
   public static final SoundEvent[] GIRLS_GALATH_DIALOG = new SoundEvent[6];
   public static final SoundEvent[] GIRLS_GALATH_GIGGLE = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_GALATH_HMPH = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_GALATH_HUH = new SoundEvent[3];
   public static final SoundEvent[] GIRLS_GALATH_LIGHTCHARGE = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_GALATH_MOAN = new SoundEvent[8];
   public static final SoundEvent[] GIRLS_GALATH_STRONGCHARGE = new SoundEvent[4];
   public static final SoundEvent[] GIRLS_GALATH_UUH = new SoundEvent[7];
   public static final SoundEvent[] GIRLS_GALATH_ORGASM = new SoundEvent[5];
   public static final SoundEvent[] GIRLS_GALATH_AAA = new SoundEvent[2];
   public static final SoundEvent[] MISC_PYRO = new SoundEvent[1];
   static HashMap<SoundEvent, Integer> lastRandomSound = new HashMap<>();

   public static void registerSounds() {
      for (Field var3 : SoundHandler.class.getDeclaredFields()) {
         Class var4 = var3.getType();
         if (var4.isArray() && var4.getComponentType() == SoundEvent.class) {
            SoundEvent[] var5;
            try {
               var5 = (SoundEvent[])var3.get(null);
            } catch (Exception var10) {
               Main.LOGGER.error("Error registering sound: " + var10.getMessage());
               continue;
            }

            String var6 = var3.getName().toLowerCase().replace("_", ".");
            String[] var7 = var6.split("\\.");
            String var8 = var7.length > 2 ? var7[2] : var7[1];

            for (int var9 = 0; var9 < var5.length; var9++) {
               var5[var9] = a_clash803(String.format("%s.%s%s", var6, var8, var9));
            }
         }
      }
   }

   public static SoundEvent a_clash803(String var0) {
      ResourceLocation var1 = new ResourceLocation("sexmod", var0);
      SoundEvent var2 = new SoundEvent(var1);
      var2.setRegistryName(var0);
      ForgeRegistries.SOUND_EVENTS.register(var2);
      return var2;
   }

   public static SoundEvent randomSound(SoundEvent[] var0) {
      lastRandomSound.putIfAbsent(var0[0], -69);
      int var2 = 0;

      int var1;
      do {
         var1 = Reference.f.nextInt(var0.length);
      } while (++var2 < 10 && var1 == lastRandomSound.get(var0[0]));

      lastRandomSound.replace(var0[0], var1);
      return var0[var1];
   }

}
