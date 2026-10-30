package com.trolmastercard.sexmod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * Guards the version path: pom.xml &lt;version&gt; -&gt; filtered
 * sexmod-build.properties -&gt; {@link BuildInfo#VERSION}. Also fails when
 * {@link BuildInfo#DEFAULT_VERSION} (the {@code @Mod} annotation constant and
 * IDE fallback) drifts from the pom, so a bump cannot go half-done silently.
 */
public class BuildInfoTest {

   private static final Pattern POM_VERSION =
         Pattern.compile("<version>([^<]+)</version>");

   private static String pomVersion() throws Exception {
      File pom = new File("pom.xml");
      if (!pom.isFile()) return null;
      Matcher m = POM_VERSION.matcher(new String(Files.readAllBytes(pom.toPath()), StandardCharsets.UTF_8));
      return m.find() ? m.group(1).trim() : null;
   }

   @Test
   public void versionIsFilteredFromThePom() throws Exception {
      assertFalse("unfiltered placeholder leaked: " + BuildInfo.VERSION, BuildInfo.VERSION.contains("${"));
      assertTrue("not a version: " + BuildInfo.VERSION,
            BuildInfo.VERSION.matches("\\d+\\.\\d+\\.\\d+(-[\\w.]+)?"));
      String pom = pomVersion();
      if (pom != null) assertEquals(pom, BuildInfo.VERSION);
   }

   @Test
   public void annotationConstantDoesNotDriftFromPom() throws Exception {
      String pom = pomVersion();
      if (pom != null) assertEquals("@Mod version constant is stale; bump BuildInfo.DEFAULT_VERSION",
            pom, BuildInfo.DEFAULT_VERSION);
   }
}
