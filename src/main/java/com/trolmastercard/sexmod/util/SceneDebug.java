package com.trolmastercard.sexmod.util;

/**
 * Special-build scene debug logging. Each subsystem has its own flag — flip to
 * false to silence it. ENABLED=false silences everything for release.
 */
/**
 * Special-build scene debug logging. Each subsystem has its own boolean flag —
 * flip to {@code false} to silence it. {@link #ENABLED} = {@code false}
 * silences everything for a release build.
 * <p>
 * The scene system's high-value diagnostics (entry transitions, resets,
 * setDead) stay ON during development; the noisy per-tick traces are OFF.
 * New debug logging added during development should go behind a named flag so
 * it can be silenced without deleting the instrumentation.
 */
public class SceneDebug {
   public static final boolean ENABLED = true;

   // Off by default: verified machinery (spams heavily)
   public static final boolean ACTIONS = false;      // every setCurrentAction call
   public static final boolean AI_TICK = false;      // per-tick updateAITasks during scenes
   public static final boolean HEARTBEAT = false;    // per-tick onUpdate during scenes
   public static final boolean PACKETS = false;      // ChangeDataParameter/KoboldStatePacket/SendGirlToSex
   public static final boolean MOVEMENT = false;     // movement lock transitions
   public static final boolean IN_HAND = false;      // InHandMapRenderer reflection (per frame)

   // On by default: scene flow still under verification
   public static final boolean SCENE_ENTRY = true;   // doAction, U(), sound-listener transitions, handleAnalState
   public static final boolean SITTING = true;       // kobold isSitting lerp
   public static final boolean RESET = true;         // ResetGirlPacket + resetCameraAndPhysics
   public static final boolean SET_DEAD = true;      // any girl setDead (vanishing safety net)
   public static final boolean RSHIFT = true;        // leave-scene keybind
   public static final boolean CLOTHING = false;     // clothing screen folder open

   public static void log(String fmt, Object... args) {
      if (ENABLED) {
         System.out.println("[sexmod-scene] " + String.format(fmt, args));
      }
   }

   public static void log(boolean flag, String fmt, Object... args) {
      if (ENABLED && flag) {
         System.out.println("[sexmod-scene] " + String.format(fmt, args));
      }
   }
}
