import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class MCRepack {
    // Builds the mapping from the SRG MC jar + MCP CSVs, keyed by (name, desc).
    // mode: srg2mcp -> rename func_XXX/field_XXX to MCP names (compile jar)
    //       mcp2srg -> rename MCP names back to func_XXX/field_XXX (reobfuscate built mod jar)
    static Map<String, String> methodMap = new HashMap<>();   // "name\0desc" -> mappedName
    static Map<String, String> fieldMap = new HashMap<>();    // "name\0desc" -> mappedName
    static boolean reobf;

    public static void main(String[] args) throws Exception {
        String srgJar = args[0];
        String methodCsv = args[1];
        String fieldCsv = args[2];
        String mode = args[3];
        String in = args[4];
        String out = args[5];
        reobf = mode.equals("mcp2srg");

        // 1. scan the SRG jar for method/field (name, desc)
        Map<String, String> srgMethod = new HashMap<>();  // "func_XXX\0desc" -> "func_XXX"
        Map<String, String> srgField = new HashMap<>();   // "field_XXX\0desc" -> "field_XXX"
        scanJar(srgJar, srgMethod, srgField);

        // 2. build MCP<->SRG map keyed by (name, desc)
        Map<String, String> mcpName = loadCsv(methodCsv); // func_XXX -> MCP
        Map<String, String> fieldMcp = loadCsv(fieldCsv); // field_XXX -> MCP
        for (Map.Entry<String, String> e : srgMethod.entrySet()) {
            String key = e.getKey(); // "func_XXX\0desc"
            String srgName = e.getValue();
            String mcp = mcpName.get(srgName);
            if (mcp == null || mcp.equals(srgName)) continue;
            String mapKey = (reobf ? mcp : srgName) + "\u0000" + key.substring(key.indexOf('\u0000') + 1);
            String existing = methodMap.get(mapKey);
            if (existing == null || betterSide(srgName, existing)) {
                methodMap.put(mapKey, reobf ? srgName : mcp);
            }
        }
        for (Map.Entry<String, String> e : srgField.entrySet()) {
            String key = e.getKey();
            String srgName = e.getValue();
            String mcp = fieldMcp.get(srgName);
            if (mcp == null || mcp.equals(srgName)) continue;
            fieldMap.put((reobf ? mcp : srgName) + "\u0000" + key.substring(key.indexOf('\u0000') + 1), reobf ? srgName : mcp);
        }
        System.out.println("method entries: " + methodMap.size() + " field entries: " + fieldMap.size());

        File outFile = new File(out);
        File tmp = new File(out + ".tmp");
        rezip(new File(in), tmp);
        if (outFile.exists()) outFile.delete();
        java.nio.file.Files.move(tmp.toPath(), outFile.toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("done: " + out);
    }

    static Map<String, String> mcpSide = new HashMap<>(); // func_XXX -> side

    static Map<String, String> loadCsv(String path) throws Exception {
        Map<String, String> map = new HashMap<>();
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
            int c3 = line.indexOf(',', c2 + 1);
            String side = (c3 < 0 ? "2" : line.substring(c2 + 1, c3).trim());
            if (side.isEmpty()) side = "2";
            mcpSide.put(a, side);
        }
        r.close();
        return map;
    }

    static void scanJar(String jar, Map<String, String> methods, Map<String, String> fields) throws Exception {
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(jar)));
        ZipEntry e;
        while ((e = zin.getNextEntry()) != null) {
            String n = e.getName();
            if (!n.endsWith(".class")) { zin.closeEntry(); continue; }
            byte[] data = readAll(zin);
            zin.closeEntry();
            org.objectweb.asm.tree.ClassNode cn = new org.objectweb.asm.tree.ClassNode();
            try { new ClassReader(data).accept(cn, 0); } catch (Exception ex) { continue; }
            for (org.objectweb.asm.tree.MethodNode m : cn.methods) {
                if (m.name.startsWith("func_")) {
                    methods.put(m.name + "\u0000" + m.desc, m.name);
                }
            }
            for (org.objectweb.asm.tree.FieldNode f : cn.fields) {
                if (f.name.startsWith("field_")) {
                    fields.put(f.name + "\u0000" + f.desc, f.name);
                }
            }
        }
        zin.close();
    }

    // prefer side-2 (both client+server) entries when (name, desc) is ambiguous
    static boolean betterSide(String candidate, String existing) {
        String cs = mcpSide.get(candidate);
        String es = mcpSide.get(existing);
        if (cs == null) cs = "2";
        if (es == null) es = "2";
        return cs.equals("2") && !es.equals("2");
    }

    static class MyRemapper extends Remapper {
        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String n = methodMap.get(name + "\u0000" + desc);
            return n == null ? name : n;
        }
        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String n = fieldMap.get(name + "\u0000" + desc);
            if (n == null) return name;
            if (reobf && !owner.startsWith("net/minecraft/")) return name; // fields: scope to MC owners
            return n;
        }
    }

    static void rezip(File in, File out) throws Exception {
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(in)));
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
        ZipEntry e;
        while ((e = zin.getNextEntry()) != null) {
            byte[] bytes = readAll(zin);
            if (e.getName().endsWith(".class")) {
                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(0);
                ClassRemapper crr = new ClassRemapper(cw, new MyRemapper());
                cr.accept(crr, 0);
                ZipEntry ne = new ZipEntry(e.getName());
                zout.putNextEntry(ne);
                zout.write(cw.toByteArray());
            } else {
                ZipEntry ne = new ZipEntry(e.getName());
                zout.putNextEntry(ne);
                zout.write(bytes);
            }
            zout.closeEntry();
            zin.closeEntry();
        }
        zout.close();
        zin.close();
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        return b.toByteArray();
    }
}
