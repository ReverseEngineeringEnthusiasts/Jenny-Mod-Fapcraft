package com.trolmastercard.sexmod.reskin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.trolmastercard.sexmod.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <b>Role.</b> Index of the model archive on GitHub, used by the selective
 * downloader instead of pulling the whole ~560 MB {@code main.zip}.
 * <p>
 * One GitHub API call ({@code git/trees/main?recursive=1}) gives every pack file
 * with its size; the result is cached in
 * {@code sexmod/custom_models/archive_index.json} for a day so the anonymous
 * API limit (60 requests/hour) is never a problem in normal use. Refresh forces
 * a new fetch.
 * <p>
 * Every {@code .zip}/{@code .rar}/{@code .7z} blob is one pickable pack. The
 * download URL is the raw file (no API quota) and the destination keeps the
 * archive's {@code <girl>/<pack>} layout, which the reskin scanner already walks.
 * <p>
 * MC-free and headless-testable: no Minecraft imports, no GL.
 */
public final class ArchiveIndex {
   public static final String TREE_URL =
      "https://api.github.com/repos/ReverseEngineeringEnthusiasts/Jenny-Mod-Custom-Models-Archive/git/trees/main?recursive=1";
   public static final String RAW_BASE =
      "https://raw.githubusercontent.com/ReverseEngineeringEnthusiasts/Jenny-Mod-Custom-Models-Archive/main/";
   private static final File CACHE = new File("sexmod/custom_models/archive_index.json");
   private static final long CACHE_TTL_MS = 24L * 60L * 60L * 1000L;

   /** One pack file in the archive. */
   public static final class Pack {
      public final String path;
      public final String name;
      public final String group;
      public final long size;
      public final String url;

      Pack(String path, long size) {
         this(path, size, null);
      }

      Pack(String path, long size, String url) {
         this.path = path;
         this.size = size;
         int slash = path.lastIndexOf('/');
         String file = slash < 0 ? path : path.substring(slash + 1);
         int dot = file.lastIndexOf('.');
         this.name = dot > 0 ? file.substring(0, dot) : file;
         this.group = slash < 0 ? "" : path.substring(0, path.indexOf('/'));
         this.url = url != null ? url : RAW_BASE + encodePath(path);
      }

      @Override
      public String toString() {
         return this.path;
      }
   }

   /** Immutable view for the GUI. */
   public static final class State {
      public final boolean loading;
      public final boolean loaded;
      public final String error;
      public final List<Pack> packs;

      State(boolean loading, boolean loaded, String error, List<Pack> packs) {
         this.loading = loading;
         this.loaded = loaded;
         this.error = error;
         this.packs = packs;
      }
   }

   private static volatile State state = new State(false, false, null, Collections.<Pack>emptyList());
   private static volatile boolean loading;

   private ArchiveIndex() {
   }

   public static State state() {
      return state;
   }

   /** Loads the cached index when fresh, otherwise fetches it on a worker. */
   public static void loadAsync(final boolean force) {
      synchronized (ArchiveIndex.class) {
         if (loading) {
            return;
         }

         if (!force) {
            List<Pack> cached = readCache(false);
            if (cached != null) {
               state = new State(false, true, null, cached);
               return;
            }
         }

         loading = true;
         state = new State(true, false, null, state.packs);
      }

      Thread thread = new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               List<Pack> packs = fetchTree();
               writeCache(packs);
               state = new State(false, true, null, packs);
            } catch (Throwable t) {
               List<Pack> cached = readCache(true);
               if (cached != null) {
                  state = new State(false, true, "Could not refresh the archive list: " + t.getMessage(), cached);
               } else {
                  state = new State(false, false, "Could not load the archive list: " + t.getMessage(),
                     Collections.<Pack>emptyList());
               }
               Main.LOGGER.warn("model archive index unavailable: {}", t.toString());
            } finally {
               synchronized (ArchiveIndex.class) {
                  loading = false;
               }
            }
         }
      }, "sexmod-model-index");
      thread.setDaemon(true);
      thread.start();
   }

   static List<Pack> fetchTree() throws IOException {
      HttpURLConnection conn = (HttpURLConnection)new URL(TREE_URL).openConnection();
      conn.setConnectTimeout(15000);
      conn.setReadTimeout(30000);
      conn.setRequestProperty("User-Agent", "FapcraftMod/1.1 (model archive index)");
      conn.setRequestProperty("Accept", "application/vnd.github+json");
      int code = conn.getResponseCode();
      if (code != 200) {
         throw new IOException("HTTP " + code + " from the archive API");
      }

      StringBuilder sb = new StringBuilder(1024 * 1024);
      try (InputStream in = conn.getInputStream();
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
         char[] buf = new char[65536];
         int n;
         while ((n = reader.read(buf)) > 0) {
            sb.append(buf, 0, n);
         }
      } finally {
         conn.disconnect();
      }

      return parseTree(sb.toString());
   }

   /** Parses the GitHub tree JSON. Package-private so it can be unit tested. */
   static List<Pack> parseTree(String json) {
      List<Pack> packs = new ArrayList<>();
      JsonObject root = new JsonParser().parse(json).getAsJsonObject();
      JsonArray tree = root.has("tree") && root.get("tree").isJsonArray()
         ? root.getAsJsonArray("tree") : new JsonArray();
      for (JsonElement element : tree) {
         if (!element.isJsonObject()) {
            continue;
         }

         JsonObject blob = element.getAsJsonObject();
         if (!blob.has("type") || !"blob".equals(blob.get("type").getAsString()) || !blob.has("path")) {
            continue;
         }

         String path = blob.get("path").getAsString();
         if (!isPackFile(path)) {
            continue;
         }

         long size = blob.has("size") ? blob.get("size").getAsLong() : 0L;
         packs.add(new Pack(path, size));
      }

      Collections.sort(packs, new Comparator<Pack>() {
         @Override
         public int compare(Pack a, Pack b) {
            int group = a.group.compareToIgnoreCase(b.group);
            return group != 0 ? group : a.name.compareToIgnoreCase(b.name);
         }
      });
      return packs;
   }

   static boolean isPackFile(String path) {
      String lower = path.toLowerCase(java.util.Locale.ROOT);
      return lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z");
   }

   /** Percent-encodes every path segment, keeping the slashes. */
   static String encodePath(String path) {
      StringBuilder out = new StringBuilder(path.length() + 16);
      String[] parts = path.split("/", -1);
      for (int i = 0; i < parts.length; i++) {
         if (i > 0) {
            out.append('/');
         }
         try {
            out.append(URLEncoder.encode(parts[i], "UTF-8").replace("+", "%20"));
         } catch (java.io.UnsupportedEncodingException e) {
            out.append(parts[i]);
         }
      }
      return out.toString();
   }

   private static List<Pack> readCache(boolean allowStale) {
      if (!CACHE.isFile()) {
         return null;
      }

      try (FileInputStream in = new FileInputStream(CACHE);
           InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
         JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
         long fetched = root.has("fetched") ? root.get("fetched").getAsLong() : 0L;
         if (!allowStale && System.currentTimeMillis() - fetched > CACHE_TTL_MS) {
            return null;
         }

         JsonArray array = root.has("packs") ? root.getAsJsonArray("packs") : new JsonArray();
         List<Pack> packs = new ArrayList<>();
         for (JsonElement element : array) {
            if (!element.isJsonObject()) {
               continue;
            }

            JsonObject obj = element.getAsJsonObject();
            if (!obj.has("p")) {
               continue;
            }

            packs.add(new Pack(obj.get("p").getAsString(), obj.has("s") ? obj.get("s").getAsLong() : 0L));
         }
         return packs.isEmpty() ? null : packs;
      } catch (Throwable t) {
         return null;
      }
   }

   private static void writeCache(List<Pack> packs) {
      try {
         File parent = CACHE.getParentFile();
         if (parent != null) {
            parent.mkdirs();
         }

         JsonObject root = new JsonObject();
         root.addProperty("fetched", System.currentTimeMillis());
         JsonArray array = new JsonArray();
         for (Pack pack : packs) {
            JsonObject obj = new JsonObject();
            obj.addProperty("p", pack.path);
            obj.addProperty("s", pack.size);
            array.add(obj);
         }
         root.add("packs", array);
         try (FileOutputStream out = new FileOutputStream(CACHE);
              Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write(root.toString());
         }
      } catch (Throwable t) {
         Main.LOGGER.warn("could not cache the model archive index: {}", t.toString());
      }
   }
}
