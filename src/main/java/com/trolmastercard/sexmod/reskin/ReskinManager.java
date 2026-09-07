package com.trolmastercard.sexmod.reskin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.json.JsonAnimationUtils;

/**
 * <b>Role.</b> CLIENT-side manager for lazy reskin-pack import.
 * <p>
 * Discovers packs at ANY nesting depth under the current custom-models group
 * ({@link ServerWhitelistManager#getGlobalModelOverride()}) using only header
 * metadata ({@link ReskinScanner}); decodes geo/texture/animation bytes ONLY
 * when the user clicks Apply ({@link ReskinReader} + {@link ReskinApplier}).
 * <p>
 * <b>How a reskin works.</b> The pack's entries live at
 * {@code assets/sexmod/geo|textures|animations/...} — exactly the resource
 * locations the girl models already ask for. Apply parses the archive content
 * and OVERRIDES those {@code sexmod:} locations in-memory:
 * <ul>
 *   <li>geo → {@code Converter} → {@code RawGeometryTree} →
 *       {@code GeoBuilder} → {@link GeckoLibCache#getGeoModels()}</li>
 *   <li>texture → {@code DynamicTexture} →
 *       {@code Minecraft.getMinecraft().renderEngine.loadTexture(...)}</li>
 *   <li>animation → {@link JsonAnimationUtils} → {@code AnimationFile} →
 *       {@link GeckoLibCache#getAnimations()}</li>
 * </ul>
 * No file in the user's folders is ever moved or rewritten, and no resource
 * pack is touched: this is a pure render-time override that takes effect next
 * frame (GeckoLib re-registers bones when the cache model instance changes).
 * <p>
 * <b>Persistence.</b> A {@link IResourceManagerReloadListener} hook re-applies
 * every active reskin after F3+T / resource reloads wipe the caches.
 * <p>
 * <b>SRG note.</b> The MC calls used here ({@code getDynamicTextureLocation},
 * {@code loadTexture}, {@code deleteTexture}, {@code refreshResources},
 * {@code getResourceManager}, {@code registerReloadListener},
 * {@code onResourceManagerReload}) all resolve 1:1 in methods.csv, and the
 * RegisterReloadListener hook follows the exact pattern the shipped
 * GeckoLibCache already uses.
 */
@SideOnly(Side.CLIENT)
public class ReskinManager {
   public static final ReskinManager INSTANCE = new ReskinManager();
   private static final int MAX_PREVIEW_DIM = 64;

   /** Registered on the forge bus from ClientProxy's ForgeEventHandler.registerB(true). */
   private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "sexmod-reskin");
      t.setDaemon(true);
      return t;
   });

   private volatile List<ReskinEntry> entries;
   private final Map<String, BufferedImage> previews = new ConcurrentHashMap<>();
   /** Shared GL thumbnails across all GUI instances (keyed by absolute path). */
   private static final Map<String, ResourceLocation> GL_THUMBS = new ConcurrentHashMap<>();
   private static final Map<String, DynamicTexture> GL_THUMB_TEX = new ConcurrentHashMap<>();
   private final Set<ReskinEntry> applied = java.util.Collections.synchronizedSet(new LinkedHashSet<>());
   private final Map<String, ITextureObject> appliedTextures = new ConcurrentHashMap<>();
   private final Map<String, DynamicTexture> retainedTextures = new ConcurrentHashMap<>();
   private final Map<String, String> applyErrors = new ConcurrentHashMap<>();
   private final ReloadWatcher watcher = new ReloadWatcher();
   private long lastTick;

   /** Currently-active world key (single-<folder> / multi-<ip>), null when no world is open. */
   private String activeWorldKey = null;
   /** Packs that were applied previously but whose archive file is now missing (path -> display name). */
   private final Map<String, String> missingPacks = new ConcurrentHashMap<>();

   private ReskinManager() {
   }

   /** Watches resource reloads (F3+T etc.) and marks reskins as needing re-apply. */
   private static class ReloadWatcher implements IResourceManagerReloadListener {
      volatile boolean dirty = false;

      @Override
      public void onResourceManagerReload(IResourceManager resourceManager) {
         this.dirty = true;
      }
   }

   /** Returns a cached GL texture for a pack thumbnail, or null (upload later on render thread). */
   public ResourceLocation getGlThumbnail(String path) {
      return GL_THUMBS.get(path);
   }

   /** Uploads a decoded preview to GL and caches it for every future GUI open. */
   public ResourceLocation uploadThumbnail(String path, BufferedImage img) {
      ResourceLocation existing = GL_THUMBS.get(path);
      if (existing != null) return existing;
      try {
         DynamicTexture dt = new DynamicTexture(img);
         ResourceLocation rl = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("reskin_thumb_" + (GL_THUMBS.size() % 100000), dt);
         GL_THUMBS.put(path, rl);
         GL_THUMB_TEX.put(path, dt);
         return rl;
      } catch (Throwable t) {
         return null;
      }
   }

   /* ------------------------------------------------------------------ */
   /* Discovery                                                          */
   /* ------------------------------------------------------------------ */

   /** Lazily scans the custom-models tree (metadata only — fast). */
   public List<ReskinEntry> scan() {
      List<ReskinEntry> local = entries;
      if (local != null) return local;
      synchronized (this) {
         if (entries == null) {
            try {
               File root = new File(ServerWhitelistManager.getGlobalModelOverride());
               entries = new ReskinScanner().scan(root);
            } catch (Throwable t) {
               entries = new ArrayList<>();
               t.printStackTrace();
            }
         }
         return entries;
      }
   }

   /** Re-scan (e.g. after the user drops new archives into the folder). */
   public synchronized void rescan() {
      entries = null;
      scan();
   }

   public List<ReskinEntry> appliedCopy() {
      synchronized (applied) {
         return new ArrayList<>(applied);
      }
   }

   public boolean isApplied(ReskinEntry e) {
      return applied.contains(e);
   }

   /** True if any applied reskin targets the given lowercase girl name (e.g. "jenny"). */
   public boolean isReskinActiveFor(String girlLower) {
      if (girlLower == null) return false;
      synchronized (applied) {
         for (ReskinEntry e : applied) {
            for (String g : e.girls) {
               if (girlLower.equalsIgnoreCase(g)) return true;
            }
         }
      }
      return false;
   }

   /** True if the girl (by NPC type) currently has an applied reskin. */
   public boolean isGirlReskinned(com.trolmastercard.sexmod.entity.BaseGirlEntity girl) {
      if (girl == null) return false;
      com.trolmastercard.sexmod.entity.NpcType type = com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl);
      return type != null && isReskinActiveFor(type.name().toLowerCase(java.util.Locale.ROOT));
   }

   /**
    * Clears the legacy bone-part custom models from every client girl whose
    * NPC type matches this pack. A full-girl reskin replaces the girl entirely;
    * leaving the old custom part set would render the pack as a bone-attached
    * part on top of the reskinned body (the "double layer / texture mashup").
    */
   private void clearLegacyPartsFor(ReskinEntry e) {
      try {
         for (com.trolmastercard.sexmod.entity.BaseGirlEntity girl : com.trolmastercard.sexmod.entity.BaseGirlEntity.getGirlEntityList()) {
            if (girl == null) continue;
            com.trolmastercard.sexmod.entity.NpcType type = com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl);
            if (type == null) continue;
            String tn = type.name().toLowerCase(java.util.Locale.ROOT);
            for (String g : e.girls) {
               if (tn.equalsIgnoreCase(g)) {
                  girl.setCustomModelCode("");
                  break;
               }
            }
         }
      } catch (Throwable t) {
         // never crash the apply for cleanup
      }
   }

   /** Last apply error text for an entry (or null). */
   @Nullable
   public String applyError(ReskinEntry e) {
      return applyErrors.get(key(e));
   }

   /* ------------------------------------------------------------------ */
   /* Previews                                                           */
   /* ------------------------------------------------------------------ */

   /**
    * Decoded + downscaled preview of the pack's PNG (pack.png preferred).
    * Safe to call from any thread; the result is cached. Returns null if the
    * pack has no usable PNG.
    */
   @Nullable
   public BufferedImage preview(ReskinEntry e) {
      if (e == null) return null;
      String k = key(e);
      BufferedImage cached = previews.get(k);
      if (cached != null) return cached;
      // try the preferred preview, then fall back to other PNGs in the pack
      // (some packs ship a corrupt pack.png but a usable texture)
      List<String> candidates = new ArrayList<>();
      if (e.previewName != null) candidates.add(e.previewName);
      for (ReskinEntry.FileInfo fi : e.files) {
         String low = fi.name.toLowerCase(Locale.ROOT);
         if ((low.endsWith(".png") || low.endsWith(".jpg")) && !fi.name.equals(e.previewName)) {
            candidates.add(fi.name);
            if (candidates.size() >= 4) break;
         }
      }
      for (String cand : candidates) {
         try {
            byte[] bytes = ReskinReader.read(e, cand);
            if (bytes == null) continue;
            BufferedImage img = ReskinReader.decodeImage(bytes);
            if (img == null) continue;
            BufferedImage small = downscale(img, MAX_PREVIEW_DIM);
            previews.put(k, small);
            return small;
         } catch (IOException | RuntimeException ignored) {
            // try next candidate
         }
      }
      previews.put(k, null);
      return null;
   }

   /** Decodes + downscales off-thread, then the GUI turns it into a GL texture. */
   public void requestPreview(ReskinEntry e) {
      final ReskinEntry entry = e;
      worker.execute(() -> preview(entry));
   }

   static BufferedImage downscale(BufferedImage src, int maxDim) {
      int w = src.getWidth();
      int h = src.getHeight();
      if (w <= 0 || h <= 0) return src;
      double scale = Math.min(1.0, (double) maxDim / Math.max(w, h));
      if (scale >= 1.0) return src;
      int nw = Math.max(1, (int) Math.round(w * scale));
      int nh = Math.max(1, (int) Math.round(h * scale));
      BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = out.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.drawImage(src, 0, 0, nw, nh, null);
      g.dispose();
      return out;
   }

   /* ------------------------------------------------------------------ */
   /* Apply / Revert                                                     */
   /* ------------------------------------------------------------------ */

   /**
    * Applies a pack (decodes geo/texture/animation and overrides the
    * {@code sexmod:} resource locations). Must be called on the client (render)
    * thread — GeckoLib caches are plain HashMaps. Returns a user-facing status
    * string; re-applying an already-applied pack just re-overrides.
    */
   public synchronized String apply(ReskinEntry e) {
      registerWatcherOnce();
      List<ReskinApplier.Op> ops = ReskinApplier.plan(e);
      if (ops.isEmpty()) {
         String msg = "No usable reskin content found in '" + e.displayName + "' (needs assets/sexmod/geo|textures|animations).";
         applyErrors.put(key(e), msg);
         return msg;
      }
      int ok = 0;
      List<String> problems = new ArrayList<>();
      for (ReskinApplier.Op op : ops) {
         try {
            byte[] bytes = ReskinReader.read(e, op.relName);
            if (bytes == null) {
               problems.add("missing " + op.relName);
               continue;
            }
            switch (op.kind) {
               case GEO:
                  applyGeo(e, op, bytes);
                  break;
               case TEXTURE:
                  applyTexture(e, op, bytes);
                  break;
               case ANIMATION:
                  applyAnimation(e, op, bytes);
                  break;
            }
            ok++;
         } catch (Throwable t) {
            problems.add(op.relName + " (" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
         }
      }
      applyErrors.remove(key(e));
      applied.add(e);
      clearLegacyPartsFor(e);
      // clean audit: written to sexmod/reskin_audit.log so the user can see
      // exactly which packs are applied and which were replaced.
      writeAudit();
      saveAppliedForWorld();
      String status = "Applied '" + e.displayName + "' (" + ok + "/" + ops.size() + " files)";
      if (!problems.isEmpty()) status += " — problems: " + String.join("; ", problems);
      System.out.println("[reskin] " + status);
      return status;
   }

   public static String auditLogPath() {
      return "sexmod/reskin_audit.log";
   }

   /** Writes the currently-applied reskin list to {@code sexmod/reskin_audit.log}. */
   public synchronized void writeAudit() {
      try {
         File f = new File(auditLogPath());
         f.getParentFile().mkdirs();
         StringBuilder sb = new StringBuilder();
         sb.append("# reskin audit ").append(new java.util.Date()).append("\n");
         synchronized (applied) {
            for (ReskinEntry e : applied) {
               sb.append("APPLIED  ").append(e.displayName).append("  girls=").append(e.girlSummary()).append("\n");
            }
         }
         for (Map.Entry<String, String> en : missingPacks.entrySet()) {
            sb.append("MISSING  ").append(en.getValue()).append("  (").append(en.getKey()).append(")\n");
         }
         sb.append("# end\n");
         java.nio.file.Files.write(f.toPath(), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
      } catch (Throwable t) {
         // audit is best-effort
      }
   }

   /* ------------------------------------------------------------------ */
   /* Per-world persistence                                              */
   /* ------------------------------------------------------------------ */

   /**
    * Stable key for the currently open world: {@code single-<save folder>} for
    * singleplayer, {@code multi-<server ip>} for multiplayer, else null when no
    * world is loaded (title screen).
    */
   @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
   private static String currentWorldKey() {
      try {
         net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
         if (mc == null) return null;
         net.minecraft.server.integrated.IntegratedServer is = mc.getIntegratedServer();
         if (is != null) {
            String folder = is.getFolderName();
            if (folder != null && !folder.isEmpty()) return "single-" + folder;
         }
         net.minecraft.client.multiplayer.ServerData sd = mc.getCurrentServerData();
         if (sd != null && sd.serverIP != null && !sd.serverIP.isEmpty()) return "multi-" + sd.serverIP;
      } catch (Throwable t) {
         // headless / not in a world
      }
      return null;
   }

   private static String sanitizeWorldKey(String key) {
      return key.replaceAll("[^A-Za-z0-9._-]", "_");
   }

   /** Config file storing the applied-pack list for one world. */
   private static File appliedConfigFile(String worldKey) {
      return new File("sexmod/reskins/" + sanitizeWorldKey(worldKey) + ".json");
   }

   /** Saves the applied pack paths for the current world (no-op outside a world). */
   public synchronized void saveAppliedForWorld() {
      saveAppliedForWorld(currentWorldKey());
   }

   private synchronized void saveAppliedForWorld(String worldKey) {
      if (worldKey == null) return;
      try {
         File f = appliedConfigFile(worldKey);
         f.getParentFile().mkdirs();
         com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
         synchronized (applied) {
            for (ReskinEntry e : applied) {
               arr.add(e.source.getAbsolutePath());
            }
         }
         com.google.gson.JsonObject root = new com.google.gson.JsonObject();
         root.addProperty("world", worldKey);
         root.add("applied", arr);
         java.nio.file.Files.write(f.toPath(), new Gson().toJson(root).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      } catch (Throwable t) {
         // persistence is best-effort; never crash apply/revert for it
      }
   }

   /**
    * Re-applies every pack saved for the given world. Packs whose archive file
    * no longer exists are skipped and recorded as missing (no crash). Must be
    * called on the client thread (applies touch GeckoLib caches).
    */
   public synchronized void loadAppliedForWorld(String worldKey) {
      try {
         File f = appliedConfigFile(worldKey);
         if (!f.isFile()) return;
         com.google.gson.JsonObject root = new Gson().fromJson(
            new String(java.nio.file.Files.readAllBytes(f.toPath()), java.nio.charset.StandardCharsets.UTF_8),
            com.google.gson.JsonObject.class
         );
         if (root == null || !root.has("applied")) return;
         com.google.gson.JsonArray arr = root.getAsJsonArray("applied");
         // map current scan by absolute path so applied entries are the live objects
         List<ReskinEntry> all = scan();
         Map<String, ReskinEntry> byPath = new java.util.HashMap<>();
         if (all != null) {
            for (ReskinEntry e : all) byPath.put(e.source.getAbsolutePath(), e);
         }
         missingPacks.clear();
         int appliedOk = 0, missing = 0;
         for (com.google.gson.JsonElement el : arr) {
            String path = el.getAsString();
            ReskinEntry e = byPath.get(path);
            File src = new File(path);
            if (e == null || !src.isFile()) {
               missingPacks.put(path, e == null ? new File(path).getName() : e.displayName);
               System.out.println("[reskin] missing pack, skipping: " + path);
               missing++;
               continue;
            }
            try {
               apply(e);
               appliedOk++;
            } catch (Throwable t) {
               System.out.println("[reskin] failed to re-apply " + e.displayName + ": " + t.getMessage());
            }
         }
         if (missing > 0) {
            System.out.println("[reskin] world '" + worldKey + "': re-applied " + appliedOk + " pack(s), " + missing + " missing");
         }
         writeAudit();
      } catch (Throwable t) {
         // never crash world join on a bad config
      }
   }

   /** Paths of previously-applied packs whose archive is now missing (path -> display name). */
   public Map<String, String> missingCopy() {
      return new java.util.HashMap<>(missingPacks);
   }

   private void applyGeo(ReskinEntry e, ReskinApplier.Op op, byte[] bytes) throws Exception {
      String json = ReskinApplier.prepareGeoJson(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
      if (json == null) return; // decoy / misplaced animation file — skip
      ResourceLocation rl = new ResourceLocation(op.rlNs, op.rlPath);
      RawGeoModel raw = Converter.fromJsonString(json);
      RawGeometryTree tree = RawGeometryTree.parseHierarchy(raw, rl);
      GeoModel model = GeoBuilder.getGeoBuilder(rl.getNamespace()).constructGeoModel(tree);
      GeckoLibCache.getInstance().getGeoModels().put(rl, model);
   }

   private void applyTexture(ReskinEntry e, ReskinApplier.Op op, byte[] bytes) throws IOException {
      BufferedImage img = ReskinReader.decodeImage(bytes);
      if (img == null) return; // undecodable/decoy image — keep default texture
      ResourceLocation rl = new ResourceLocation(op.rlNs, op.rlPath);
      DynamicTexture dt = new DynamicTexture(img);
      Minecraft.getMinecraft().renderEngine.loadTexture(rl, dt);
      appliedTextures.put(rl.toString(), dt);
      retainedTextures.put(rl.toString(), dt);
   }

   private void applyAnimation(ReskinEntry e, ReskinApplier.Op op, byte[] bytes) throws Exception {
      String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
      if (!ReskinApplier.isAnimationJson(json)) return; // decoy or not an animation — skip
      JsonObject root = new Gson().fromJson(json, JsonObject.class);
      if (root == null || !root.has("animations")) return; // empty animation file is fine
      ResourceLocation rl = new ResourceLocation(op.rlNs, op.rlPath);
      AnimationFile af = new AnimationFile();
      for (Map.Entry<String, com.google.gson.JsonElement> en : JsonAnimationUtils.getAnimations(root)) {
         Animation anim = JsonAnimationUtils.deserializeJsonToAnimation(
            JsonAnimationUtils.getAnimation(root, en.getKey()), GeckoLibCache.getInstance().parser);
         af.putAnimation(en.getKey(), anim);
      }
      GeckoLibCache.getInstance().getAnimations().put(rl, af);
   }

   /**
    * Removes the overrides for one pack and restores the defaults (geos/
    * animations/textures come back from the resource packs via a resource
    * reload). Other applied packs are re-applied right after.
    */
   /**
    * Reorders an applied pack within the stack. {@code delta} is in DISPLAYED
    * stack terms: -1 = move UP (higher priority/overrides), +1 = move DOWN
    * (lower priority/fallback). Internal {@code applied} is ordered oldest-first
    * (the display reverses it), so display-up == internal-toward-end.
    */
   public synchronized String move(ReskinEntry e, int delta) {
      if (e == null) return "No pack.";
      List<ReskinEntry> copy = appliedCopy();
      int idx = copy.indexOf(e);
      if (idx < 0) return "'" + e.displayName + "' is not applied.";
      int displayIdx = copy.size() - 1 - idx;             // top-first index
      int newDisplay = displayIdx + delta;
      if (newDisplay < 0) return "'" + e.displayName + "' is already at the top.";
      if (newDisplay >= copy.size()) return "'" + e.displayName + "' is already at the bottom.";
      copy.remove(idx);
      // After removal the list is one shorter. newDisplay is in 0..size-1 (original).
      // Correct internal insert position (bottom-first) = newSize - newDisplay.
      // Example: internal [A,B,C] => display [C,B,A]; move B to display 0:
      //   remove B -> [A,C] (size 2), newDisplay 0 -> insert at 2 -> [A,C,B]
      //   = display [B,C,A], correct. (Old code used size-1-newDisplay which
      //   produced -1 for a 1-element stack -> IndexOutOfBoundsException.)
      int newInternal = copy.size() - newDisplay;
      newInternal = Math.max(0, Math.min(newInternal, copy.size()));
      try {
         copy.add(newInternal, e);
      } catch (Throwable t) {
         return "Could not move (internal error).";
      }
      applied.clear();
      applied.addAll(copy);
      // Re-apply bottom->top so the new top (highest priority) is applied LAST and
      // therefore overrides shared resource keys — vanilla texture-pack semantics.
      for (ReskinEntry re : copy) {
         try { applySilent(re); } catch (Throwable t) { /* keep going */ }
      }
      writeAudit();
      saveAppliedForWorld();
      return "Moved '" + e.displayName + "' to priority " + (newDisplay + 1) + ".";
   }

   /** Add a pack to the applied set WITHOUT loading (for the debounce queue) —
    * shows it on the right immediately; flushing with apply() loads it for real. */
   public synchronized void addToAppliedOnly(ReskinEntry e) {
      if (e == null) return;
      applied.add(e);
   }

   /** Applies without writing chat/status (used for stack re-application). */
   private synchronized void applySilent(ReskinEntry e) {
      try { apply(e); } catch (Throwable t) { t.printStackTrace(); }
   }

   /**
    * Reverts a pack; {@link #applied} is a LinkedHashSet so order is preserved. */
   public synchronized String revert(ReskinEntry e) {
      List<ReskinApplier.Op> ops = ReskinApplier.plan(e);
      for (ReskinApplier.Op op : ops) {
         ResourceLocation rl = new ResourceLocation(op.rlNs, op.rlPath);
         try {
            if (op.kind == ReskinApplier.Kind.GEO) {
               GeckoLibCache.getInstance().getGeoModels().remove(rl);
            } else if (op.kind == ReskinApplier.Kind.ANIMATION) {
               GeckoLibCache.getInstance().getAnimations().remove(rl);
            } else if (op.kind == ReskinApplier.Kind.TEXTURE) {
               Minecraft.getMinecraft().renderEngine.deleteTexture(rl);
               appliedTextures.remove(rl.toString());
               retainedTextures.remove(rl.toString());
            }
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }
      applied.remove(e);
      applyErrors.remove(key(e));
      restoreDefaults();
      saveAppliedForWorld();
      return "Reverted '" + e.displayName + "'";
   }

   /** Removes ALL applied reskins and restores defaults. */
   public synchronized String revertAll() {
      List<ReskinEntry> copy = appliedCopy();
      for (ReskinEntry e : copy) revert(e);
      if (copy.isEmpty()) restoreDefaults();
      return copy.isEmpty() ? "No reskins applied." : "Reverted " + copy.size() + " reskin(s).";
   }

   /**
    * Full resource reload: GeckoLibCache.onResourceManagerReload repopulates
    * geo + animations from the resource packs (this is what restores the
    * defaults), and our ReloadWatcher fires so pending re-applies happen.
    */
   private void restoreDefaults() {
      try {
         Minecraft.getMinecraft().refreshResources();
      } catch (Throwable t) {
         t.printStackTrace();
      }
      // re-apply the still-active reskins (refreshResources wiped them too)
      List<ReskinEntry> still = appliedCopy();
      for (ReskinEntry e : still) {
         try { apply(e); } catch (Throwable t) { t.printStackTrace(); }
      }
      watcher.dirty = false;
   }

   /** Hands over the RenderThread job to the generic client tick handler. */
   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (event.phase != Phase.END) return;
      long now = System.currentTimeMillis();
      if (now - lastTick < 500) return;
      lastTick = now;

      // Per-world persistence: detect world enter / leave / switch.
      // Saving the OLD key first is important: currentWorldKey() has already
      // changed by the time we detect the transition.
      String wk = currentWorldKey();
      if (!java.util.Objects.equals(wk, this.activeWorldKey)) {
         if (this.activeWorldKey != null) {
            saveAppliedForWorld(this.activeWorldKey);
         }
         this.activeWorldKey = wk;
         if (wk != null) {
            loadAppliedForWorld(wk);
         }
      }

      if (watcher.dirty) {
         watcher.dirty = false;
         List<ReskinEntry> still = appliedCopy();
         for (ReskinEntry e : still) {
            try { apply(e); } catch (Throwable t) { t.printStackTrace(); }
         }
      }
   }

   private boolean watcherRegistered = false;

   private synchronized void registerWatcherOnce() {
      if (watcherRegistered) return;
      try {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(this.watcher);
            this.watcher.dirty = false; // registration invokes the listener once
            watcherRegistered = true;
         }
      } catch (Throwable t) {
         t.printStackTrace();
      }
   }

   /** Background executor exposed for one-off image tasks (daemon). */
   public void submit(Runnable r) {
      worker.execute(r);
   }

   private static String key(ReskinEntry e) {
      return e.source.getAbsolutePath();
   }

   /** Debug helper: prints the planned override table for a pack. */
   public static void debugPlan(ReskinEntry e) {
      for (ReskinApplier.Op op : ReskinApplier.plan(e)) {
         System.out.println("  [reskin] " + op);
      }
   }
}