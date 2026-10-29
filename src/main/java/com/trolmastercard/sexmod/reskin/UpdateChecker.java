package com.trolmastercard.sexmod.reskin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

/**
 * <b>Role.</b> MC-free update checker for the mod.
 * <p>
 * On the FIRST world join of a session it queries the GitHub releases API and
 * compares the newest release tag to the locally installed version. If a newer
 * release exists, a single chat message tells the user the mod is outdated.
 * <p>
 * To avoid pestering, it writes a small state file
 * ({@code sexmod/.update_check_<version>_<sha-of-latest>}) after a successful
 * check; it only re-checks once per local version + once per latest release
 * SHA. Network failures are silent (offline play) — no chat spam.
 * <p>
 * Version parsing: accepts {@code v1.2.3}, {@code 1.2.3}, {@code 1.2.3-SNAPSHOT},
 * {@code 1.2 (1)} (the local jar layout) by extracting digit triples; compares
 * numerically.
 */
public class UpdateChecker {

   public static final String REPO = "ReverseEngineeringEnthusiasts/Jenny-Mod-Fapcraft";
   public static final String RELEASES_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";

   /** @return newest release tag (e.g. "1.2.4") or null on any failure/offline. */
   public static String fetchLatestTag() {
      JsonObject rel = fetchLatestRelease();
      if (rel == null) return null;
      String tag = rel.has("tag_name") ? rel.get("tag_name").getAsString() : null;
      if (tag == null && rel.has("name")) tag = rel.get("name").getAsString();
      return tag;
   }

   /** @return newest release body (changelog) or null. */
   public static String fetchLatestBody() {
      JsonObject rel = fetchLatestRelease();
      if (rel == null || !rel.has("body")) return null;
      String body = rel.get("body").getAsString();
      return body == null ? null : body.trim();
   }

   private static JsonObject fetchLatestRelease() {
      try {
         HttpURLConnection conn = (HttpURLConnection) new URL(RELEASES_URL).openConnection();
         conn.setConnectTimeout(8000);
         conn.setReadTimeout(12000);
         conn.setInstanceFollowRedirects(true);
         conn.setRequestProperty("Accept", "application/vnd.github+json");
         conn.setRequestProperty("User-Agent", "Fapcraft-1.12.2/1.1.1 (update checker)");
         int code = conn.getResponseCode();
         if (code != 200) return null;
         try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return new Gson().fromJson(sb.toString(), JsonObject.class);
         }
      } catch (Throwable t) {
         return null;
      }
   }

   /** Numeric version parse: [major, minor, patch] from common tag formats. */
   public static int[] parseVersion(String v) {
      if (v == null) return new int[]{0, 0, 0};
      String s = v.trim().replaceFirst("^[vV]", "").toLowerCase(java.util.Locale.ROOT);
      // strip -SNAPSHOT / -beta etc.
      int dash = s.indexOf('-');
      if (dash >= 0) s = s.substring(0, dash);
      StringBuilder digits = new StringBuilder();
      java.util.List<Integer> nums = new java.util.ArrayList<>();
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (Character.isDigit(c)) digits.append(c);
         else if (digits.length() > 0) {
            nums.add(Integer.parseInt(digits.toString()));
            digits.setLength(0);
         }
      }
      if (digits.length() > 0) nums.add(Integer.parseInt(digits.toString()));
      while (nums.size() < 3) nums.add(0);
      return new int[]{nums.get(0), nums.get(1), nums.get(2)};
   }

   public static int compare(int[] a, int[] b) {
      for (int i = 0; i < 3; i++) {
         if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
      }
      return 0;
   }

   /** Returns a short plain-text changelog snippet (first few meaningful lines). */
   public static String summarizeBody(String body, int maxLines) {
      if (body == null || body.trim().isEmpty()) return null;
      StringBuilder sb = new StringBuilder();
      int lines = 0;
      for (String rawLine : body.split("\r?\n")) {
         String line = rawLine.trim();
         if (line.isEmpty()) continue;
         // strip markdown bullets/headers/links roughly
         line = line.replaceAll("^#{1,6}\\s+", "");
         line = line.replaceAll("^[-*+]\\s+", "• ");
         line = line.replaceAll("\\[(.+?)\\]\\([^)]+\\)", "$1");
         line = line.replaceAll("[*_`~]", "");
         if (line.isEmpty()) continue;
         if (line.length() > 110) line = line.substring(0, 110) + "…";
         sb.append("  ").append(line).append("\n");
         if (++lines >= maxLines) break;
      }
      String out = sb.toString().trim();
      return out.isEmpty() ? null : out;
   }

   /** Whether a newer release is available than {@code localVersion}. */
   public static boolean isOutdated(String localVersion, String latestTag) {
      if (localVersion == null || latestTag == null) return false;
      return compare(parseVersion(localVersion), parseVersion(latestTag)) < 0;
   }

   /** Build-aware: if the latest tag is "vN", compare the local build counter to N. */
   public static boolean isOutdatedBuild(int localBuild, String latestTag) {
      if (latestTag == null) return false;
      String s = latestTag.trim().toLowerCase(java.util.Locale.ROOT);
      // match v7, build 7, 7
      java.util.regex.Matcher m = java.util.regex.Pattern.compile("^v?(\\d+)$").matcher(s);
      if (m.find()) {
         try {
            int latest = Integer.parseInt(m.group(1));
            return localBuild < latest;
         } catch (NumberFormatException e) {
            return false;
         }
      }
      // not build-shaped: fall back to semver-ish, treat localBuild as major
      return isOutdated(String.valueOf(localBuild), latestTag);
   }

   /* ---------------- notify-once state ---------------- */

   private static File stateFile() {
      return new File("sexmod/.update_check_state");
   }

   /** True if we already notified for this local version + latest SHA (or latest is unknown). */
   public static boolean alreadyNotified(String localVersion, String latestTag) {
      try {
         File f = stateFile();
         if (!f.isFile()) return false;
         BufferedReader r = new BufferedReader(new FileReader(f));
         String line;
         while ((line = r.readLine()) != null) {
            if (line.trim().equals(localVersion + "|" + latestTag)) return true;
         }
         r.close();
      } catch (Throwable t) { }
      return false;
   }

   public static void markNotified(String localVersion, String latestTag) {
      try {
         File f = stateFile();
         if (f.getParentFile() != null) f.getParentFile().mkdirs();
         java.util.List<String> lines = new java.util.ArrayList<>();
         if (f.isFile()) {
            BufferedReader r = new BufferedReader(new FileReader(f));
            String l;
            while ((l = r.readLine()) != null) if (!l.trim().isEmpty()) lines.add(l.trim());
            r.close();
         }
         String entry = localVersion + "|" + (latestTag == null ? "" : latestTag);
         if (!lines.contains(entry)) lines.add(entry);
         Files.write(f.toPath(), (String.join("\n", lines) + "\n").getBytes(StandardCharsets.UTF_8));
      } catch (Throwable t) { }
   }
}