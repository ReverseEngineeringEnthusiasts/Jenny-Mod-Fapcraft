package com.trolmastercard.sexmod.reskin;

import com.trolmastercard.sexmod.Main;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <b>Role.</b> Selective, resumable downloader for {@link ArchiveIndex} packs.
 * One daemon worker downloads one pack at a time into the custom-models folder,
 * writing {@code <name>.part} until the file is complete, so closing the screen,
 * pausing, or a crash leaves a resumable partial file instead of losing the
 * transfer.
 * <p>
 * <b>Pause/resume.</b> {@link #pause()} is cooperative: the worker finishes the
 * current 64 KB chunk, closes the stream and waits. {@link #resume()} reopens
 * the request with a {@code Range: bytes=<part length>-} header; if the server
 * ignores the range (HTTP 200) the file restarts from zero.
 * <p>
 * <b>Resources.</b> Single thread, single HTTP connection, 64 KB buffer, no pack
 * contents in memory, disk-space check before every file, and the UI only reads
 * an immutable {@link Snapshot} — no Minecraft/GL calls from the worker.
 * MC-free so the range/partial/path helpers are headless-testable.
 */
public final class ModelDownloadManager {
   public static final ModelDownloadManager INSTANCE = new ModelDownloadManager();
   private static final int BUFFER = 65536;
   private static final long RESERVE_BYTES = 64L * 1024L * 1024L;

   public enum Status { IDLE, RUNNING, DOWNLOADING, PAUSED, DONE, FAILED, CANCELLED }

   public static final class Task {
      public final ArchiveIndex.Pack pack;
      public final File dest;
      volatile Status status = Status.IDLE;
      volatile long done;
      volatile String error;

      Task(ArchiveIndex.Pack pack, File dest) {
         this.pack = pack;
         this.dest = dest;
      }
   }

   /** Immutable view for the GUI. */
   public static final class Snapshot {
      public final Status status;
      public final int total;
      public final int finished;
      public final int failed;
      public final long doneBytes;
      public final long totalBytes;
      public final long currentDone;
      public final long currentTotal;
      public final long speedBps;
      public final String current;
      public final String error;

      Snapshot(Status status, int total, int finished, int failed, long doneBytes, long totalBytes,
               long currentDone, long currentTotal, long speedBps, String current, String error) {
         this.status = status;
         this.total = total;
         this.finished = finished;
         this.failed = failed;
         this.doneBytes = doneBytes;
         this.totalBytes = totalBytes;
         this.currentDone = currentDone;
         this.currentTotal = currentTotal;
         this.speedBps = speedBps;
         this.current = current;
         this.error = error;
      }
   }

   private static final class PauseSignal extends IOException {
   }

   private static final class CancelSignal extends IOException {
   }

   private final Object lock = new Object();
   private final List<Task> tasks = new ArrayList<>();
   private volatile Status status = Status.IDLE;
   private volatile boolean pauseRequested;
   private volatile boolean cancelRequested;
   private volatile long baseDone;
   private volatile long doneBytes;
   private volatile long totalBytes;
   private volatile long currentDone;
   private volatile long currentTotal;
   private volatile long speedBps;
   private volatile String current = "";
   private volatile String error = "";
   private Runnable onDone;

   private ModelDownloadManager() {
   }

   public boolean isBusy() {
      Status s = this.status;
      return s == Status.RUNNING || s == Status.PAUSED;
   }

   /** Starts a batch; returns false when a batch is already running. */
   public boolean start(List<ArchiveIndex.Pack> packs, File root, Runnable onDone) {
      if (packs == null || packs.isEmpty() || this.isBusy()) {
         return false;
      }

      List<Task> copy = new ArrayList<>();
      try {
         for (ArchiveIndex.Pack pack : packs) {
            copy.add(new Task(pack, destFor(root, pack.path)));
         }
      } catch (IOException e) {
         this.error = "Invalid destination: " + e.getMessage();
         this.status = Status.FAILED;
         return false;
      }

      synchronized (this) {
         if (this.isBusy()) {
            return false;
         }

         this.tasks.clear();
         this.tasks.addAll(copy);
         this.onDone = onDone;
         this.error = "";
         this.current = "";
         this.currentDone = 0L;
         this.currentTotal = 0L;
         this.doneBytes = 0L;
         this.speedBps = 0L;
         long total = 0L;
         for (Task task : copy) {
            total += Math.max(0L, task.pack.size);
         }
         this.totalBytes = total;
         this.baseDone = 0L;
         this.pauseRequested = false;
         this.cancelRequested = false;
         this.status = Status.RUNNING;
      }

      Thread worker = new Thread(new Runnable() {
         @Override
         public void run() {
            runBatch();
         }
      }, "sexmod-model-download");
      worker.setDaemon(true);
      worker.start();
      return true;
   }

   public void pause() {
      if (this.status == Status.RUNNING) {
         this.pauseRequested = true;
      }
   }

   public void resume() {
      synchronized (this.lock) {
         this.pauseRequested = false;
         this.lock.notifyAll();
      }
   }

   public void cancel() {
      synchronized (this.lock) {
         this.cancelRequested = true;
         this.pauseRequested = false;
         this.lock.notifyAll();
      }
   }

   public Snapshot snapshot() {
      int finished = 0;
      int failed = 0;
      for (Task task : this.tasks) {
         if (task.status == Status.DONE) {
            finished++;
         } else if (task.status == Status.FAILED) {
            failed++;
         }
      }

      return new Snapshot(this.status, this.tasks.size(), finished, failed, this.doneBytes, this.totalBytes,
         this.currentDone, this.currentTotal, this.speedBps, this.current, this.error);
   }

   private void runBatch() {
      int finished = 0;
      int failed = 0;
      try {
         for (Task task : this.tasks) {
            if (this.cancelRequested) {
               break;
            }

            this.waitWhilePaused();
            if (this.cancelRequested) {
               break;
            }

            this.current = task.pack.name;
            this.currentDone = resumeOffset(partOf(task.dest));
            this.currentTotal = Math.max(0L, task.pack.size);
            this.baseDone = this.doneBytes;
            task.status = Status.DOWNLOADING;
            try {
               this.download(task);
               task.status = Status.DONE;
               finished++;
               this.doneBytes = this.baseDone + Math.max(task.pack.size, this.currentDone);
            } catch (PauseSignal paused) {
               task.status = Status.IDLE;
               this.status = Status.PAUSED;
               this.waitWhilePaused();
               this.status = Status.RUNNING;
               task.status = Status.DOWNLOADING;
               // retry the same pack; the .part file resumes it
               try {
                  this.download(task);
                  task.status = Status.DONE;
                  finished++;
                  this.doneBytes = this.baseDone + Math.max(task.pack.size, this.currentDone);
               } catch (CancelSignal cancelled) {
                  break;
               } catch (Throwable t) {
                  task.status = Status.FAILED;
                  task.error = t.getMessage();
                  failed++;
                  this.error = task.pack.name + ": " + t.getMessage();
               }
            } catch (CancelSignal cancelled) {
               break;
            } catch (Throwable t) {
               task.status = Status.FAILED;
               task.error = t.getMessage();
               failed++;
               this.error = task.pack.name + ": " + t.getMessage();
               Main.LOGGER.warn("model download failed for {}: {}", task.pack.path, t.toString());
            }
         }
      } finally {
         this.speedBps = 0L;
         if (this.cancelRequested) {
            this.deletePartials();
            this.status = Status.CANCELLED;
            this.current = "";
         } else if (failed > 0) {
            this.status = Status.FAILED;
         } else {
            this.status = Status.DONE;
            this.current = "";
         }

         if (finished > 0 && this.onDone != null && !this.cancelRequested) {
            try {
               this.onDone.run();
            } catch (Throwable t) {
               Main.LOGGER.warn("model download completion hook failed: {}", t.toString());
            }
         }
      }
   }

   private void waitWhilePaused() {
      synchronized (this.lock) {
         if (this.pauseRequested && !this.cancelRequested) {
            this.status = Status.PAUSED;
         }
         while (this.pauseRequested && !this.cancelRequested) {
            try {
               this.lock.wait();
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return;
            }
         }
      }
   }

   private void download(Task task) throws IOException {
      File part = partOf(task.dest);
      File parent = task.dest.getParentFile();
      if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
         throw new IOException("Cannot create " + parent);
      }

      if (task.dest.isFile() && task.pack.size > 0 && task.dest.length() == task.pack.size) {
         task.done = task.pack.size;
         this.currentDone = task.pack.size;
         return;
      }

      long offset = part.isFile() ? part.length() : 0L;
      long need = Math.max(0L, task.pack.size - offset);
      if (parent != null && parent.getUsableSpace() < need + RESERVE_BYTES) {
         throw new IOException("Not enough disk space (need " + formatBytes(need + RESERVE_BYTES) + ")");
      }

      HttpURLConnection conn = (HttpURLConnection)new URL(task.pack.url).openConnection();
      conn.setConnectTimeout(15000);
      conn.setReadTimeout(30000);
      conn.setInstanceFollowRedirects(true);
      conn.setRequestProperty("User-Agent", "FapcraftMod/1.1 (sexmod model downloader)");
      if (offset > 0L) {
         conn.setRequestProperty("Range", "bytes=" + offset + "-");
      }

      int code;
      try {
         code = conn.getResponseCode();
      } catch (IOException e) {
         conn.disconnect();
         throw e;
      }

      boolean append;
      if (code == 206 && offset > 0L) {
         append = true;
      } else if (code == 200) {
         append = false;
         offset = 0L;
      } else if (code == 416 && offset > 0L && part.length() >= task.pack.size) {
         conn.disconnect();
         finishPart(part, task.dest);
         return;
      } else {
         conn.disconnect();
         throw new IOException("HTTP " + code + " for " + task.pack.name);
      }

      long written = offset;
      long speedTime = System.currentTimeMillis();
      long speedBytes = written;
      try (InputStream in = new BufferedInputStream(conn.getInputStream(), BUFFER);
           FileOutputStream out = new FileOutputStream(part, append)) {
         byte[] buffer = new byte[BUFFER];
         int n;
         while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
            written += n;
            this.currentDone = written;
            this.doneBytes = this.baseDone + written;

            long now = System.currentTimeMillis();
            if (now - speedTime >= 500L) {
               this.speedBps = Math.max(0L, (written - speedBytes) * 1000L / Math.max(1L, now - speedTime));
               speedTime = now;
               speedBytes = written;
            }

            if (this.cancelRequested) {
               throw new CancelSignal();
            }
            if (this.pauseRequested) {
               throw new PauseSignal();
            }
         }
      } finally {
         conn.disconnect();
      }

      if (task.pack.size > 0L && part.length() < task.pack.size) {
         throw new IOException("Incomplete download (" + part.length() + "/" + task.pack.size + ")");
      }

      finishPart(part, task.dest);
      task.done = task.pack.size;
   }

   private static void finishPart(File part, File dest) throws IOException {
      Files.move(part.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
   }

   /** Removes every unfinished .part file (cancel). Finished packs are kept. */
   private void deletePartials() {
      for (Task task : this.tasks) {
         if (task.status != Status.DONE) {
            partOf(task.dest).delete();
         }
      }
   }

   static File partOf(File dest) {
      return new File(dest.getPath() + ".part");
   }

   /** Maps an archive path under the target root, rejecting traversal. */
   static File destFor(File root, String archivePath) throws IOException {
      File rootCanon = root.getCanonicalFile();
      File dest = new File(rootCanon, archivePath).getCanonicalFile();
      String rootPath = rootCanon.getPath();
      String destPath = dest.getPath();
      if (!destPath.equals(rootPath) && !destPath.startsWith(rootPath + File.separator)) {
         throw new IOException("unsafe archive path: " + archivePath);
      }
      return dest;
   }

   static long resumeOffset(File part) {
      return part != null && part.isFile() ? part.length() : 0L;
   }

   public static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else if (bytes < 1024L * 1024L) {
         return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
      } else if (bytes < 1024L * 1024L * 1024L) {
         return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
      } else {
         return String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
      }
   }
}
