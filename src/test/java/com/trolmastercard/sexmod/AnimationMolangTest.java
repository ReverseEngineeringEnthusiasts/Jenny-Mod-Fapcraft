package com.trolmastercard.sexmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

import static org.junit.Assert.assertTrue;

/**
 * Every molang expression in the bundled animation files must parse. A stray
 * trailing operator (for example {@code "-36.4659 + size * "}) makes GeckoLib
 * throw an {@code IndexOutOfBoundsException} during the resource reload, which
 * floods the log with stack traces and aborts the rest of that animation file.
 */
public class AnimationMolangTest {
   private static final String[] SECTIONS = {"rotation", "position", "scale"};
   private static final String[] DIRECTIONS = {"pre", "post"};

   @Test
   public void everyAnimationExpressionParses() throws Exception {
      File root = new File("src/main/resources/assets/sexmod/animations");
      assertTrue("animation directory missing: " + root.getAbsolutePath(), root.isDirectory());

      List<String> failures = new ArrayList<>();
      MolangParser parser = new MolangParser();
      int[] parsed = {0};

      for (File file : animationFiles(root)) {
         JsonObject json;
         try (FileReader reader = new FileReader(file)) {
            json = new JsonParser().parse(reader).getAsJsonObject();
         }

         JsonObject animations = json.getAsJsonObject("animations");
         if (animations == null) {
            continue;
         }

         for (Map.Entry<String, JsonElement> animation : animations.entrySet()) {
            JsonElement animationValue = animation.getValue();
            if (!animationValue.isJsonObject()) {
               failures.add(file.getName() + ":" + animation.getKey() + " animation is not an object");
               continue;
            }

            JsonElement bonesValue = animationValue.getAsJsonObject().get("bones");
            if (bonesValue == null) {
               continue;
            }
            if (!bonesValue.isJsonObject()) {
               failures.add(file.getName() + ":" + animation.getKey() + " bones is not an object");
               continue;
            }

            JsonObject bones = bonesValue.getAsJsonObject();

            for (Map.Entry<String, JsonElement> bone : bones.entrySet()) {
               if (!bone.getValue().isJsonObject()) {
                  continue;
               }

               for (String section : SECTIONS) {
                  JsonElement sectionValue = bone.getValue().getAsJsonObject().get(section);
                  if (sectionValue == null || !sectionValue.isJsonObject()) {
                     continue;
                  }

                  JsonObject keyframes = sectionValue.getAsJsonObject();

                  for (Map.Entry<String, JsonElement> keyframe : keyframes.entrySet()) {
                     String path = file.getName() + ":" + animation.getKey() + "." + bone.getKey()
                        + "." + section + "." + keyframe.getKey();
                     check(parser, keyframe.getValue(), path, parsed, failures);
                  }
               }
            }
         }
      }

      assertTrue("no molang expressions were found", parsed[0] > 0);
      assertTrue("unparseable molang expressions:\n" + String.join("\n", failures),
         failures.isEmpty());
   }

   private static void check(MolangParser parser, JsonElement value, String path,
                             int[] parsed, List<String> failures) {
      if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
         parse(parser, value.getAsString(), path, parsed, failures);
         return;
      }

      if (value.isJsonArray()) {
         JsonArray array = value.getAsJsonArray();
         for (int i = 0; i < array.size(); i++) {
            check(parser, array.get(i), path + "[" + i + "]", parsed, failures);
         }
         return;
      }

      if (value.isJsonObject()) {
         JsonObject object = value.getAsJsonObject();
         JsonElement vector = object.get("vector");
         if (vector != null) {
            check(parser, vector, path + ".vector", parsed, failures);
         }
         for (String direction : DIRECTIONS) {
            JsonElement part = object.get(direction);
            if (part != null) {
               check(parser, part, path + "." + direction, parsed, failures);
            }
         }
      }
   }

   private static void parse(MolangParser parser, String expression, String path,
                             int[] parsed, List<String> failures) {
      parsed[0]++;
      try {
         parser.parseExpression(expression);
      } catch (Throwable t) {
         failures.add(path + " = \"" + expression + "\" (" + t.getClass().getSimpleName() + ")");
      }
   }

   private static List<File> animationFiles(File root) {
      List<File> files = new ArrayList<>();
      collect(root, files);
      return files;
   }

   private static void collect(File directory, List<File> files) {
      File[] children = directory.listFiles();
      if (children == null) {
         return;
      }
      for (File child : children) {
         if (child.isDirectory()) {
            collect(child, files);
         } else if (child.getName().endsWith(".animation.json")) {
            files.add(child);
         }
      }
   }
}
