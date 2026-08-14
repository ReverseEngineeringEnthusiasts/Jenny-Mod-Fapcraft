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
    static Map<String, String> methodOwnerMap = new HashMap<>(); // "mcp\0desc" -> "owner:srgName" (for ambiguous dupes)
    static Map<String, String> srgMethodOwner = new HashMap<>(); // "func_XXX\0desc" -> first declaring class
    static Map<String, String> srgFieldOwner = new HashMap<>();  // "field_XXX\0desc" -> first declaring class
    static Map<String, java.util.Set<String>> srgMethodAllOwners = new HashMap<>(); // "func_XXX\0desc" -> all declaring classes
    static Map<String, java.util.Set<String>> srgFieldAllOwners = new HashMap<>();  // "field_XXX\0desc" -> all declaring classes
    static Map<String, String> classParents = new HashMap<>();   // class internal name -> superName
    static Map<String, Map<String, String>> methodOwnerCandidates = new HashMap<>(); // "mcp\0desc" -> declaringClass -> srgName
    static Map<String, Map<String, String>> fieldOwnerCandidates = new HashMap<>();  // "mcp\0desc" -> declaringClass -> srgName
    static boolean reobf;

    // classes whose methods/fields ARE SRG-renamed at runtime: Minecraft itself,
    // the mod, Forge's MC-facing classes (net/minecraftforge/...), and GeckoLib's
    // own API classes. Bundled third-party libs under software/bernie/shadowed
    // (jackson etc.) must NEVER be remapped.
    static boolean isSrgOwner(String owner) {
        return owner != null
                && (owner.startsWith("net/minecraft/")
                    || owner.startsWith("net/minecraftforge/")
                    || owner.startsWith("com/trolmastercard/")
                    || owner.startsWith("software/bernie/geckolib3"));
    }

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
            // collect ALL declaring classes for this (mcp, desc) so we can resolve
            // by the callsite owner's class hierarchy (handles stale duplicates like
            // getResourceDomains: func_110587_b on FileResourcePack vs func_135055_a
            // on IResourceManager).
            java.util.Set<String> owners = srgMethodAllOwners.get(key);
            if (owners != null) {
                Map<String, String> byClass = methodOwnerCandidates.computeIfAbsent(mapKey, k -> new java.util.HashMap<>());
                for (String o : owners) byClass.put(o, srgName);
            }
        }
        for (Map.Entry<String, String> e : srgField.entrySet()) {
            String key = e.getKey();
            String srgName = e.getValue();
            String mcp = fieldMcp.get(srgName);
            if (mcp == null || mcp.equals(srgName)) continue;
            String mapKey = (reobf ? mcp : srgName) + "\u0000" + key.substring(key.indexOf('\u0000') + 1);
            fieldMap.put(mapKey, reobf ? srgName : mcp);
            java.util.Set<String> owners = srgFieldAllOwners.get(key);
            if (owners != null) {
                Map<String, String> byClass = fieldOwnerCandidates.computeIfAbsent(mapKey, k -> new java.util.HashMap<>());
                for (String o : owners) byClass.put(o, srgName);
            }
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
            classParents.put(cn.name, cn.superName);
            for (org.objectweb.asm.tree.MethodNode m : cn.methods) {
                if (m.name.startsWith("func_")) {
                    String key = m.name + "\u0000" + m.desc;
                    methods.put(key, m.name);
                    srgMethodOwner.merge(key, cn.name, (a, b) -> a); // first declaring class wins (used for canonical owner)
                    srgMethodAllOwners.computeIfAbsent(key, k -> new java.util.LinkedHashSet<>()).add(cn.name);
                }
            }
            for (org.objectweb.asm.tree.FieldNode f : cn.fields) {
                if (f.name.startsWith("field_")) {
                    String key = f.name + "\u0000" + f.desc;
                    fields.put(key, f.name);
                    srgFieldOwner.merge(key, cn.name, (a, b) -> a);
                    srgFieldAllOwners.computeIfAbsent(key, k -> new java.util.LinkedHashSet<>()).add(cn.name);
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
        // Walk the callsite owner up its superclass chain looking for a class that
        // declares a candidate SRG name for this (mcp, desc). Falls back to the
        // generic map. Never remaps non-MC owners (java.* etc.) — those classes
        // are not SRG-renamed at runtime (e.g. BufferedReader.close()).
        private String resolveByOwner(Map<String, String> byClass, String owner, String name, String fallback) {
            if (byClass == null || byClass.isEmpty()) return name;
            if (!isSrgOwner(owner)) return name; // JDK/third-party: never remap
            String cur = owner;
            int depth = 0;
            while (cur != null && depth++ < 64) {
                String srg = byClass.get(cur);
                if (srg != null) return srg;
                cur = classParents.get(cur);
            }
            // no class in the chain declares this (mcp, desc) -> it is a mod/other
            // declared member that only collides by name; keep the original name
            return name;
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String key = name + "\u0000" + desc;
            String n = methodMap.get(key);
            if (n == null) return name;
            if (reobf) {
                Map<String, String> cands = methodOwnerCandidates.get(key);
                if (cands != null && cands.size() > 1) {
                    return resolveByOwner(cands, owner, name, n);
                }
                // even with a single candidate, refuse to remap JDK/third-party owners
                if (!isSrgOwner(owner)) return name;
            }
            return n;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String key = name + "\u0000" + desc;
            String n = fieldMap.get(key);
            if (n == null) return name;
            if (reobf) {
                if (!isSrgOwner(owner)) return name;
                Map<String, String> cands = fieldOwnerCandidates.get(key);
                if (cands != null && cands.size() > 1) {
                    return resolveByOwner(cands, owner, name, n);
                }
                // The callsite owner is an MC/mod/forge/geckolib class. Only remap
                // MC-declared (inherited) fields: walk the FULL superclass chain;
                // a cands hit means some class in the chain declares this SRG field.
                // If NO class in the chain declares it (e.g. a mod-declared field
                // like TrailSegment.world that merely shares an MCP name+desc with
                // an MC field), keep the original name.
                String cur = owner;
                int depth = 0;
                while (cur != null && depth++ < 64) {
                    String srg = cands != null ? cands.get(cur) : null;
                    if (srg != null) return srg; // declared by this class -> remap
                    cur = classParents.get(cur);
                }
                return name;
            }
            return n;
        }
    }

    static void rezip(File in, File out) throws Exception {
        // pass 1: collect class hierarchy of the INPUT jar too, so owner resolution
        // can walk from mod subclasses up to their MC superclasses (BaseGirlEntity
        // -> ... -> Entity), which are the classes that declare the SRG names.
        ZipInputStream scan = new ZipInputStream(new BufferedInputStream(new FileInputStream(in)));
        ZipEntry se;
        while ((se = scan.getNextEntry()) != null) {
            String n = se.getName();
            if (n.endsWith(".class")) {
                byte[] data = readAll(scan);
                try {
                    org.objectweb.asm.tree.ClassNode cn = new org.objectweb.asm.tree.ClassNode();
                    new ClassReader(data).accept(cn, 0);
                    classParents.putIfAbsent(cn.name, cn.superName);
                } catch (Exception ignore) { }
            }
            scan.closeEntry();
        }
        scan.close();

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
