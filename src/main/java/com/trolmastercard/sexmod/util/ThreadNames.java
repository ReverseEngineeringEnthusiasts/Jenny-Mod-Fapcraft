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
 * {@code step} toward the target), not progress lerp — call sites rely on the
 * stepping semantics.
 */
public class ThreadNames {
   public static float wrapAngle(double angle, double target) {
      angle = (angle + (Math.PI * 2)) % (Math.PI * 2);
      target = (target + (Math.PI * 2)) % (Math.PI * 2);
      double diff = target - angle;

      while (diff < -Math.PI) {
         diff += Math.PI * 2;
      }

      while (diff >= Math.PI) {
         diff -= Math.PI * 2;
      }

      return (float)diff;
   }

   public static Vector2f getLookAngles(Vec3d from, Vec3d to) {
      Vec3d delta = to.subtract(from).normalize();
      return new Vector2f(
         (float)Math.atan2(delta.x, delta.z),
         (float)Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))
      );
   }

   public static void copyToClipboard(String text) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection selection = new StringSelection(text);
      clipboard.setContents(selection, null);
   }

   public static String capitalizeFirst(String text) {
      return text != null && !text.isEmpty() ? Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase() : text;
   }

   public static boolean isBetween(double value, double min, double max) {
      return value < min ? false : !(value >= max);
   }

   public static int weightedRandomIndex(int maxIndex) {
      if (maxIndex <= 0) {
         return maxIndex;
      }

      Random random = new Random();
      int totalWeight = 0;

      for (int i = 0; i <= maxIndex; i++) {
         totalWeight += i;
      }

      int roll = random.nextInt(totalWeight) + 1;
      int cumulative = 0;

      for (int i2 = 0; i2 <= maxIndex; i2++) {
         cumulative += i2;
         if (cumulative >= roll) {
            return i2;
         }
      }

      return maxIndex;
   }

   public static int randomSign() {
      return Reference.RANDOM.nextBoolean() ? 1 : -1;
   }

   public static float clampFloat(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   public static double clampDouble(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public static float randomSignedFloat(float magnitude, boolean allowNegative) {
      Random random = new Random();
      return random.nextFloat() * magnitude * (allowNegative && random.nextBoolean() ? -1 : 1);
   }

   public static float moveToward(float current, float target, float step) {
      if (Math.abs(current - target) <= step) {
         return current;
      } else if (Math.abs(current) < Math.abs(target)) {
         return target > 0.0F ? target - step : target + step;
      } else {
         return current > 0.0F ? current - step : current + step;
      }
   }

   public static int roundToInt(double value) {
      return Math.round((float)value);
   }

   public static void createDaemonThread(int priority, Runnable runnable) {
      String threadName = UUID.randomUUID().toString();
      new Thread(() -> {
         try {
            Thread.sleep(priority);
         } catch (Exception exception) {
            exception.printStackTrace();
         }

         runnable.run();
      }, (ClientServerCheck.getInstance() ? "server sexmod thread " : "client sexmod thread ") + threadName).start();
   }

}
