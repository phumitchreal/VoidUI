import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MixinSrgRenamer {

    static final Map<String, String> METHODS_BY_OWNER = new HashMap<>();   // owner + "\0" + srg + "\0" + desc -> mcp name
    static final Map<String, String> METHODS_BY_NAME_DESC = new HashMap<>(); // srg + "\0" + desc -> mcp name
    static final Map<String, String> METHODS_UNIQUE_NAME = new HashMap<>();  // srg -> mcp name (unique globally)
    static final Map<String, String> FIELDS_BY_OWNER = new HashMap<>();      // owner + "\0" + srg -> mcp name
    static final Map<String, String> FIELDS_UNIQUE_NAME = new HashMap<>();   // srg -> mcp name (unique globally)

    static boolean APPLY = false;
    static int changes = 0;

    public static void main(String[] args) throws Exception {
        Path srg = Paths.get(args[0]);
        Path inJar = Paths.get(args[1]);
        Path outJar = Paths.get(args[2]);
        APPLY = args.length > 3 && args[3].equalsIgnoreCase("apply");

        loadSrg(srg);

        Map<String, byte[]> output = new LinkedHashMap<>();
        try (JarFile jf = new JarFile(inJar.toFile())) {
            var entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                byte[] bytes = readAll(jf.getInputStream(e));
                if (e.getName().endsWith(".class")) {
                    bytes = processClass(e.getName(), bytes);
                }
                output.put(e.getName(), bytes);
            }
        }

        System.out.println("Total changes: " + changes);
        if (APPLY) {
            try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(outJar))) {
                for (Map.Entry<String, byte[]> en : output.entrySet()) {
                    JarEntry je = new JarEntry(en.getKey());
                    jos.putNextEntry(je);
                    jos.write(en.getValue());
                    jos.closeEntry();
                }
            }
            System.out.println("Wrote " + outJar);
        }
    }

    static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        in.transferTo(bos);
        return bos.toByteArray();
    }

    static String[] splitOwner(String s) {
        int i = s.lastIndexOf('/');
        return new String[]{s.substring(0, i), s.substring(i + 1)};
    }

    static void loadSrg(Path srg) throws IOException {
        List<String[]> methodRows = new ArrayList<>();
        for (String line : Files.readAllLines(srg, StandardCharsets.UTF_8)) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 5 && parts[0].startsWith("MD")) {
                String[] a = splitOwner(parts[1]);
                String[] b = splitOwner(parts[3]);
                methodRows.add(new String[]{a[0], a[1], parts[2], b[1]});
            } else if (parts.length >= 3 && parts[0].startsWith("FD")) {
                String[] a = splitOwner(parts[1]);
                String[] b = splitOwner(parts[2]);
                String key = a[0] + "\0" + a[1];
                if (!FIELDS_BY_OWNER.containsKey(key)) FIELDS_BY_OWNER.put(key, b[1]);
                if (!FIELDS_UNIQUE_NAME.containsKey(a[1])) FIELDS_UNIQUE_NAME.put(a[1], b[1]);
            }
        }
        for (String[] r : methodRows) {
            METHODS_BY_OWNER.put(r[0] + "\0" + r[1] + "\0" + r[2], r[3]);
            String nd = r[1] + "\0" + r[2];
            if (!METHODS_BY_NAME_DESC.containsKey(nd)) METHODS_BY_NAME_DESC.put(nd, r[3]);
            if (!METHODS_UNIQUE_NAME.containsKey(r[1])) METHODS_UNIQUE_NAME.put(r[1], r[3]);
        }
        System.out.println("Loaded methods: " + METHODS_BY_OWNER.size() + " fields: " + FIELDS_BY_OWNER.size());
    }

    static String remapMethod(String owner, String name, String desc) {
        if (name == null || name.isEmpty() || !name.matches("[fm]_\\d+_?")) return null;
        String v = METHODS_BY_OWNER.get(owner + "\0" + name + "\0" + desc);
        if (v == null && desc != null) v = METHODS_BY_NAME_DESC.get(name + "\0" + desc);
        return v;
    }

    static String remapField(String owner, String name) {
        if (name == null || name.isEmpty() || !name.matches("[fm]_\\d+_?")) return null;
        String v = FIELDS_BY_OWNER.get(owner + "\0" + name);
        if (v == null) v = FIELDS_UNIQUE_NAME.get(name);
        return v;
    }

    static void remapAnnotation(AnnotationNode an, String owner, String clsName) {
        if (an == null || an.values == null) return;
        for (int i = 0; i < an.values.size(); i += 2) {
            String attr = (String) an.values.get(i);
            Object v = an.values.get(i + 1);
            if (v instanceof String s) {
                String r = remapString(s, owner);
                if (r != null) { an.values.set(i + 1, r); log(clsName, "annotation " + an.desc + " " + attr + ": " + s + " -> " + r); }
            } else if (v instanceof List<?> list) {
                for (int j = 0; j < list.size(); j++) {
                    Object e = list.get(j);
                    if (e instanceof String s2) {
                        String r = remapString(s2, owner);
                        if (r != null) { ((List<Object>) list).set(j, r); log(clsName, "annotation " + an.desc + " " + attr + "[]: " + s2 + " -> " + r); }
                    } else if (e instanceof AnnotationNode an2) {
                        remapAnnotation(an2, owner, clsName);
                    }
                }
            } else if (v instanceof AnnotationNode an2) {
                remapAnnotation(an2, owner, clsName);
            }
        }
    }

    static String remapString(String s, String owner) {
        if (s == null || s.isEmpty()) return null;
        if (s.matches("[fm]_\\d+_?")) {
            String m = METHODS_UNIQUE_NAME.get(s);
            if (m != null) return m;
            String f = FIELDS_UNIQUE_NAME.get(s);
            if (f != null) return f;
            return null;
        }
        if (s.startsWith("L") && s.contains(";") && s.contains("(")) {
            int semi = s.indexOf(';');
            int paren = s.indexOf('(');
            String o = s.substring(1, semi);
            String nm = s.substring(semi + 1, paren);
            String desc = s.substring(paren);
            String mcp = remapMethod(o, nm, desc);
            if (mcp != null) return "L" + o + ";" + mcp + desc;
        }
        return null;
    }

    static Handle remapHandle(Handle h, String clsName) {
        if (h == null) return null;
        String r;
        if (h.getTag() >= Opcodes.H_GETFIELD && h.getTag() <= Opcodes.H_PUTFIELD) {
            r = remapField(h.getOwner(), h.getName());
        } else {
            r = remapMethod(h.getOwner(), h.getName(), h.getDesc());
        }
        if (r != null) { log(clsName, "handle " + h.getOwner() + "." + h.getName() + " -> " + r); return new Handle(h.getTag(), h.getOwner(), r, h.getDesc(), h.isInterface()); }
        return h;
    }

    static byte[] processClass(String name, byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        String owner = cn.name;
        String clsName = name.replace('/', '.');

        for (AnnotationNode an : allAnnotations(cn)) {
            remapAnnotation(an, owner, clsName);
        }

        for (FieldNode fn : cn.fields) {
            String r = remapField(owner, fn.name);
            if (r != null) { log(clsName, "field decl " + fn.name + " -> " + r); fn.name = r; }
            remapFieldAnnotations(fn, owner, clsName);
        }

        for (MethodNode mn : cn.methods) {
            String r = remapMethod(owner, mn.name, mn.desc);
            if (r != null) { log(clsName, "method decl " + mn.name + mn.desc + " -> " + r); mn.name = r; }
            if (mn.visibleAnnotations != null) for (AnnotationNode an : mn.visibleAnnotations) remapAnnotation(an, owner, clsName);
            if (mn.invisibleAnnotations != null) for (AnnotationNode an : mn.invisibleAnnotations) remapAnnotation(an, owner, clsName);
            if (mn.visibleTypeAnnotations != null) for (AnnotationNode an : mn.visibleTypeAnnotations) remapAnnotation(an, owner, clsName);
            if (mn.invisibleTypeAnnotations != null) for (AnnotationNode an : mn.invisibleTypeAnnotations) remapAnnotation(an, owner, clsName);

            if (mn.instructions != null) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof MethodInsnNode min) {
                        String rr = remapMethod(min.owner, min.name, min.desc);
                        if (rr != null) { min.name = rr; log(clsName, "instr " + min.owner + "." + min.name); }
                    } else if (insn instanceof FieldInsnNode fin) {
                        String rr = remapField(fin.owner, fin.name);
                        if (rr != null) { fin.name = rr; log(clsName, "field " + fin.owner + "." + fin.name); }
                    } else if (insn instanceof InvokeDynamicInsnNode idin) {
                        String inOld = idin.name;
                        String in = remapString(inOld, owner);
                        if (in != null) { idin.name = in; log(clsName, "indy callsite " + inOld + " -> " + in); }
                        for (int j = 0; j < idin.bsmArgs.length; j++) {
                            if (idin.bsmArgs[j] instanceof Handle h) {
                                idin.bsmArgs[j] = remapHandle(h, clsName);
                            } else if (idin.bsmArgs[j] instanceof String s) {
                                String bs = remapString(s, owner);
                                if (bs != null) { idin.bsmArgs[j] = bs; log(clsName, "indy bsmArg " + s + " -> " + bs); }
                            }
                        }
                    } else if (insn instanceof LdcInsnNode ldc) {
                        if (ldc.cst instanceof Handle h) ldc.cst = remapHandle(h, clsName);
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    static List<AnnotationNode> allAnnotations(ClassNode cn) {
        List<AnnotationNode> list = new ArrayList<>();
        if (cn.visibleAnnotations != null) list.addAll(cn.visibleAnnotations);
        if (cn.invisibleAnnotations != null) list.addAll(cn.invisibleAnnotations);
        if (cn.visibleTypeAnnotations != null) list.addAll(cn.visibleTypeAnnotations);
        if (cn.invisibleTypeAnnotations != null) list.addAll(cn.invisibleTypeAnnotations);
        return list;
    }

    static void remapFieldAnnotations(FieldNode fn, String owner, String clsName) {
        if (fn.visibleAnnotations != null) for (AnnotationNode an : fn.visibleAnnotations) remapAnnotation(an, owner, clsName);
        if (fn.invisibleAnnotations != null) for (AnnotationNode an : fn.invisibleAnnotations) remapAnnotation(an, owner, clsName);
    }

    static void log(String cls, String msg) {
        changes++;
        System.out.println("[CHANGE] " + cls + " :: " + msg);
    }
}
