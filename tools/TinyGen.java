import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Generates a Tiny v2 mapping (MCP <-> SRG) from the SRG Minecraft jar + MCP stable CSVs.
 * usage: TinyGen <srg.jar> <methods.csv> <fields.csv> <out.tiny>
 */
public class TinyGen {
    static Map<String, String> methods = new HashMap<>();
    static Map<String, String> fields = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String srgJar = args[0];
        loadCsv(args[1], methods);
        loadCsv(args[2], fields);
        PrintWriter out = new PrintWriter(new FileWriter(args[3]));
        out.println("tiny\t2\t0\tmcp\tsrg");

        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(srgJar)));
        ZipEntry e;
        while ((e = zin.getNextEntry()) != null) {
            String n = e.getName();
            if (!n.endsWith(".class")) { zin.closeEntry(); continue; }
            if (!n.startsWith("net/minecraft/")) { zin.closeEntry(); continue; }
            byte[] data = readAll(zin);
            zin.closeEntry();
            final String className = n.substring(0, n.length() - 6);
            final List<String> lines = new ArrayList<>();
            ClassReader cr = new ClassReader(data);
            cr.accept(new ClassVisitor(Opcodes.ASM7) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    String mcp = methods.get(name);
                    if (mcp != null && !mcp.equals(name)) {
                        lines.add("\tm\t" + mcp + "\t" + desc + "\t" + name + "\t" + desc);
                    }
                    return null;
                }
                @Override
                public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
                    String mcp = fields.get(name);
                    if (mcp != null && !mcp.equals(name)) {
                        lines.add("\tf\t" + mcp + "\t" + desc + "\t" + name + "\t" + desc);
                    }
                    return null;
                }
            }, 0);
            if (!lines.isEmpty()) {
                out.println("c\t" + className + "\t" + className);
                for (String l : lines) out.println(l);
            }
        }
        zin.close();
        out.close();
        System.out.println("tiny written");
    }

    static void loadCsv(String path, Map<String, String> map) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        String line;
        while ((line = r.readLine()) != null) {
            int c1 = line.indexOf(',');
            if (c1 <= 0) continue;
            String a = line.substring(0, c1).trim();
            int c2 = line.indexOf(',', c1 + 1);
            String b = (c2 < 0 ? line.substring(c1 + 1) : line.substring(c1 + 1, c2)).trim();
            if (a.isEmpty() || b.isEmpty()) continue;
            map.put(a, b);
        }
        r.close();
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        return b.toByteArray();
    }
}
