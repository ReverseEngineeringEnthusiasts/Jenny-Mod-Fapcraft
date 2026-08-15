package com.trolmastercard.sexmod.util;

import java.util.Random;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> Global mod constants: ids, proxy class names, entity ids,
 * {@link #RANDOM}, the shared camera-position pair used by
 * {@link StructureMarkerRenderer} and render interpolation. Static holder.
 * <p>
 * <b>Pitfall.</b> {@code cameraPosCurrent/Previous} are written by
 * {@link StructureMarkerRenderer#onClientTick} and read by renderers — the
 * tick-order (previous = current, then current = player pos) must not be
 * reversed or the interpolation jitters.
 */
public class Reference {
   public static final String MOD_ID = "sexmod";
   public static final String MOD_NAME = "Fapcraft";
   public static final String MOD_VERSION = "1.1.0";
   public static final String CLIENT_PROXY = "com.trolmastercard.sexmod.ClientProxy";
   public static final String COMMON_PROXY = "com.trolmastercard.sexmod.CommonProxy";
   public static final Random RANDOM = new Random();
   public static int EDITOR_ID_COUNTER = 0;
   public static int BUTTON_ID_COUNTER = 0;
   public static final int KOBOLD_EGG_ENTITY_ID = 4674237;
   public static final int CUSTOM_MODEL_ENTITY_ID = 6281823;
   public static Vec3d cameraPosCurrent = Vec3d.ZERO;
   public static Vec3d cameraPosPrevious = Vec3d.ZERO;
}
