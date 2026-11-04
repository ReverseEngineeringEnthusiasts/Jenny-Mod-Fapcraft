package com.trolmastercard.sexmod;

import java.io.InputStream;
import java.util.Properties;

/**
 * <b>Role.</b> Runtime view of the mod version. {@code pom.xml}'s {@code <version>}
 * is filtered into {@code sexmod-build.properties} on the classpath (see the
 * {@code <resources>} block in the pom) and read once here, so a version bump is
 * one edit in {@code pom.xml}.
 * <p>
 * <b>Pitfall.</b> {@link #DEFAULT_VERSION} must stay a compile-time constant:
 * {@link Main}'s {@code @Mod(version = ...)} annotation takes nothing else. So it
 * is the one literal left — bump it together with the pom. It is also the fallback
 * when the resource is unfiltered (IDE run, plain {@code javac}), which is why a
 * value still containing {@code ${} is treated as missing.
 */
public final class BuildInfo {
   /** Fallback version for unfiltered builds; keep in step with pom.xml. */
   public static final String DEFAULT_VERSION = "1.1.2";

   /** Effective mod version: filtered pom version, else {@link #DEFAULT_VERSION}. */
   public static final String VERSION = load();

   private static final String RESOURCE = "/sexmod-build.properties";

   private BuildInfo() {}

   private static String load() {
      try (InputStream in = BuildInfo.class.getResourceAsStream(RESOURCE)) {
         if (in == null) return DEFAULT_VERSION;
         Properties props = new Properties();
         props.load(in);
         String v = props.getProperty("version");
         if (v == null) return DEFAULT_VERSION;
         v = v.trim();
         // unfiltered resource still carries the placeholder
         if (v.isEmpty() || v.contains("${")) return DEFAULT_VERSION;
         return v;
      } catch (Throwable t) {
         return DEFAULT_VERSION;
      }
   }
}
