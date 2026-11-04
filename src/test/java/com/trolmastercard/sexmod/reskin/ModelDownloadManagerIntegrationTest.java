package com.trolmastercard.sexmod.reskin;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-end test of the resumable downloader against a local slow HTTP server
 * with Range support: full download, pause/resume from the .part offset, and
 * cancel deleting the partial file.
 */
public class ModelDownloadManagerIntegrationTest {
   private HttpServer server;
   private byte[] payload;
   private File root;

   @Before
   public void setUp() throws Exception {
      this.payload = new byte[2 * 1024 * 1024];
      for (int i = 0; i < this.payload.length; i++) {
         this.payload[i] = (byte)(i * 31 + 7);
      }

      this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      this.server.createContext("/pack.zip", new HttpHandler() {
         @Override
         public void handle(HttpExchange exchange) throws IOException {
            try {
               serve(exchange);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            } finally {
               exchange.close();
            }
         }
      });
      this.server.setExecutor(Executors.newCachedThreadPool());
      this.server.start();
      this.root = Files.createTempDirectory("sexmod-dl-it").toFile();
   }

   private void serve(HttpExchange exchange) throws IOException, InterruptedException {
      long start = 0L;
      String range = exchange.getRequestHeaders().getFirst("Range");
      if (range != null && range.startsWith("bytes=")) {
         String value = range.substring(6);
         int dash = value.indexOf('-');
         try {
            start = Long.parseLong(dash > 0 ? value.substring(0, dash) : value);
         } catch (NumberFormatException ignored) {
            start = 0L;
         }
      }

      if (start >= this.payload.length) {
         exchange.sendResponseHeaders(416, -1L);
         return;
      }

      long length = this.payload.length - start;
      if (start > 0) {
         exchange.getResponseHeaders().set("Content-Range",
            "bytes " + start + "-" + (this.payload.length - 1) + "/" + this.payload.length);
         exchange.sendResponseHeaders(206, length);
      } else {
         exchange.sendResponseHeaders(200, length);
      }

      try (OutputStream out = exchange.getResponseBody()) {
         for (long offset = start; offset < this.payload.length; offset += 8192L) {
            int chunk = (int)Math.min(8192L, this.payload.length - offset);
            out.write(this.payload, (int)offset, chunk);
            out.flush();
            Thread.sleep(15L);
         }
      } catch (IOException clientWentAway) {
         // pause/cancel closes the stream; the exchange is already done
      }
   }

   @After
   public void tearDown() {
      if (this.server != null) {
         this.server.stop(0);
      }
   }

   private String url() {
      return "http://127.0.0.1:" + this.server.getAddress().getPort() + "/pack.zip";
   }

   private static void waitStatus(ModelDownloadManager.Status wanted, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;
      while (System.currentTimeMillis() < deadline) {
         if (ModelDownloadManager.INSTANCE.snapshot().status == wanted) {
            return;
         }
         Thread.sleep(25L);
      }
      assertEquals(wanted, ModelDownloadManager.INSTANCE.snapshot().status);
   }

   @Test
   public void downloadsPauseResumesAndCancels() throws Exception {
      ArchiveIndex.Pack pack = new ArchiveIndex.Pack("Test/pack.zip", this.payload.length, this.url());

      // full download
      assertTrue(ModelDownloadManager.INSTANCE.start(Collections.singletonList(pack), this.root, null));
      waitStatus(ModelDownloadManager.Status.DONE, 30000L);
      File dest = new File(this.root, "Test/pack.zip");
      assertArrayEquals(this.payload, Files.readAllBytes(dest.toPath()));
      assertFalse(ModelDownloadManager.partOf(dest).exists());

      // pause mid-transfer, then resume from the .part offset
      ArchiveIndex.Pack pausable = new ArchiveIndex.Pack("Test/pause.zip", this.payload.length, this.url());
      assertTrue(ModelDownloadManager.INSTANCE.start(Collections.singletonList(pausable), this.root, null));
      Thread.sleep(400L);
      ModelDownloadManager.INSTANCE.pause();
      waitStatus(ModelDownloadManager.Status.PAUSED, 5000L);
      File paused = new File(this.root, "Test/pause.zip");
      File part = ModelDownloadManager.partOf(paused);
      assertTrue("partial file should exist", part.isFile());
      long partialLength = part.length();
      assertTrue("partial file should be incomplete", partialLength > 0L && partialLength < this.payload.length);

      ModelDownloadManager.INSTANCE.resume();
      waitStatus(ModelDownloadManager.Status.DONE, 30000L);
      assertArrayEquals(this.payload, Files.readAllBytes(paused.toPath()));
      assertFalse(part.exists());

      // cancel removes the unfinished .part and does not create the pack
      File cancelled = new File(this.root, "Test/cancelled.zip");
      ArchiveIndex.Pack second = new ArchiveIndex.Pack("Test/cancelled.zip", this.payload.length, this.url());
      assertTrue(ModelDownloadManager.INSTANCE.start(Collections.singletonList(second), this.root, null));
      Thread.sleep(400L);
      ModelDownloadManager.INSTANCE.cancel();
      waitStatus(ModelDownloadManager.Status.CANCELLED, 5000L);
      assertFalse(ModelDownloadManager.partOf(cancelled).exists());
      assertFalse(cancelled.exists());
   }
}
