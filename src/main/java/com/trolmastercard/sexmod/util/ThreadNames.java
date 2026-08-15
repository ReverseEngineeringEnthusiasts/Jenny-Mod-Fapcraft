package com.trolmastercard.sexmod.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Random;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> General math/utility helpers despite the name — angle wrapping,
 * look-angle computation, clipboard, weighted random indices, clamps,
 * sign/float helpers and {@link #createDaemonThread(int, Runnable)} for
 * delayed background work (e.g. particle/cleanup timers) named after the side
 * it runs on (see {@link ClientServerCheck}).
 * <p>
 * <b>Pitfall.</b> {@link #moveToward} implements *step* movement (one step of
 * {@code var2} toward the target), not progress lerp — call sites rely on the
 * stepping semantics.
 */
public class ThreadNames {
   public static float wrapAngle(double var0, double var2) {
      var0 = (var0 + (Math.PI * 2)) % (Math.PI * 2);
      var2 = (var2 + (Math.PI * 2)) % (Math.PI * 2);
      double var4 = var2 - var0;

      while (var4 < -Math.PI) {
         var4 += Math.PI * 2;
      }

      while (var4 >= Math.PI) {
         var4 -= Math.PI * 2;
      }

      return (float)var4;
   }

   public static Vector2f getLookAngles(Vec3d var0, Vec3d var1) {
      Vec3d var2 = var1.subtract(var0).normalize();
      return new Vector2f(
         (float)Math.atan2(var2.x, var2.z),
         (float)Math.atan2(var2.y, Math.sqrt(var2.x * var2.x + var2.z * var2.z))
      );
   }

   public static void copyToClipboard(String var0) {
      Clipboard var1 = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection var2 = new StringSelection(var0);
      var1.setContents(var2, null);
   }

   public static String capitalizeFirst(String var0) {
      return var0 != null && !var0.isEmpty() ? Character.toUpperCase(var0.charAt(0)) + var0.substring(1).toLowerCase() : var0;
   }

   public static boolean isBetween(double var0, double var2, double var4) {
      return var0 < var2 ? false : !(var0 >= var4);
   }

   public static int weightedRandomIndex(int var0) {
      if (var0 <= 0) {
         return var0;
      }

      Random var1 = new Random();
      int var2 = 0;

      for (int var3 = 0; var3 <= var0; var3++) {
         var2 += var3;
      }

      int var6 = var1.nextInt(var2) + 1;
      int var4 = 0;

      for (int var5 = 0; var5 <= var0; var5++) {
         var4 += var5;
         if (var4 >= var6) {
            return var5;
         }
      }

      return var0;
   }

   public static int randomSign() {
      return Reference.RANDOM.nextBoolean() ? 1 : -1;
   }

   public static float clampFloat(float var0, float var1, float var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   public static double clampDouble(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }

   public static float randomSignedFloat(float var0, boolean var1) {
      Random var2 = new Random();
      return var2.nextFloat() * var0 * (var1 && var2.nextBoolean() ? -1 : 1);
   }

   public static float moveToward(float var0, float var1, float var2) {
      if (Math.abs(var0 - var1) <= var2) {
         return var0;
      } else if (Math.abs(var0) < Math.abs(var1)) {
         return var1 > 0.0F ? var1 - var2 : var1 + var2;
      } else {
         return var0 > 0.0F ? var0 - var2 : var0 + var2;
      }
   }

   public static int roundToInt(double var0) {
      return Math.round((float)var0);
   }

   public static void createDaemonThread(int var0, Runnable var1) {
      String var2 = UUID.randomUUID().toString();
      new Thread(() -> {
         try {
            Thread.sleep(var0);
         } catch (Exception var3) {
            var3.printStackTrace();
         }

         var1.run();
      }, (ClientServerCheck.getInstance() ? "server sexmod thread " : "client sexmod thread ") + var2).start();
   }

}
