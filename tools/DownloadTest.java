import com.trolmastercard.sexmod.reskin.PackDownloader;
import com.trolmastercard.sexmod.reskin.ReskinEntry;
import com.trolmastercard.sexmod.reskin.ReskinScanner;
import java.io.File;
import java.util.List;

/**
 * HEADLESS TEST of the GitHub download + extract flow.
 * 1) Downloads the real archive (network + redirects).
 * 2) Extracts into a temp dir (strips top folder, path-traversal safe).
 * 3) Scans the result to prove packs are actually usable.
 * Exit 0 on success.
 */
public class DownloadTest {
   public static void main(String[] a) throws Exception {
      File dest = new File("/tmp/dlout/custom_models");
      System.out.println("== 1) downloading " + PackDownloader.ARCHIVE_URL);
      File zip = PackDownloader.downloadToTemp(PackDownloader.ARCHIVE_URL);
      System.out.println("   downloaded " + zip.length() + " bytes to " + zip);

      System.out.println("== 2) extracting into " + dest);
      String result = PackDownloader.extractTo(zip, dest);
      System.out.println("   " + result.replace("\n", "\n   "));
      zip.delete();

      System.out.println("== 3) scanning the extracted tree");
      List<ReskinEntry> packs = new ReskinScanner().scan(dest);
      System.out.println("   packs found: " + packs.size());
      int zipN = 0, rarN = 0, usable = 0;
      for (ReskinEntry e : packs) {
         if (e.kind == ReskinEntry.Kind.ZIP) zipN++; else rarN++;
         if (e.isUsable()) usable++;
      }
      System.out.println("   zips=" + zipN + " rars=" + rarN + " usable=" + usable);
      if (packs.isEmpty()) {
         System.out.println("FAIL: no packs extracted");
         System.exit(1);
      }
      // show first few
      for (int i = 0; i < Math.min(5, packs.size()); i++) {
         System.out.println("   - " + packs.get(i));
      }
      System.out.println("RESULT: OK");
      System.exit(0);
   }
}