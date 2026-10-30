package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.LightingType;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.NpcType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * <b>Role.</b> Parser for one custom model's {@code .cfg} file: reads the
 * properties into a {@link ServerWhitelistManager.ModelData} (wear type, allowed
 * girls, lighting, author/model-code, hidden bones, nude flag, GUI scale and
 * position) and records the first validation problem in
 * {@link ServerWhitelistManager.ModelData#errorMessage}.
 * <p>
 * <b>Pitfalls.</b> The property names are the user-facing mod format — renaming
 * one breaks every existing custom model. Parsing stops at the first illegal
 * value for the enum/name fields, but numeric fields only record the error and
 * keep parsing. The property stream is never closed (matches the original).
 */
final class ModelCfgParser {
   private ModelCfgParser() {
   }

   static void parse(File file, String modelName, ServerWhitelistManager.ModelData model) {
      if (modelName.contains(" ") || modelName.contains("#") || modelName.contains("$")) {
         model.errorMessage = String.format("You cannot call your custom model '%s'. '#', '$' and spaces are illegal characters", modelName);
      } else if ("cross".equalsIgnoreCase(modelName)) {
         model.errorMessage = "You cannot call your custom model 'cross'. Im sorry, but I need that specific name for internal stuff";
      } else {
         Properties properties = new Properties();

         FileInputStream inputStream;
         try {
            inputStream = new FileInputStream(file);
         } catch (FileNotFoundException notFoundExc) {
            model.errorMessage = String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", modelName, file.getAbsolutePath());
            return;
         }

         try {
            properties.load(inputStream);
         } catch (IOException ioExc) {
            model.errorMessage = String.format(
               "couldn't read the cfg File for '%s' at '%s'. It appears to be corrupted. Try making a new one", modelName, file.getAbsolutePath()
            );
            return;
         }

         String wearType = properties.getProperty("wear_type");
         if (wearType == null) {
            model.errorMessage = String.format(
               "The cfg File for the model '%s' at '%s' is missing the 'wear_type'. Go to the bottom of the cfg File and write 'wear_type=HEAD'. Check the cfg files of my examples to see what values for 'wear_type' are possible",
               modelName,
               file.getAbsolutePath()
            );
         } else {
            try {
               wearType = wearType.replace(" ", "");
               model.boneType = BoneType.valueOf(wearType);
            } catch (IllegalArgumentException illegalArg) {
               model.errorMessage = String.format(
                  "you entered '%s' into the 'wear_type' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'wear_type",
                  wearType,
                  modelName,
                  file.getAbsolutePath()
               );
               return;
            }

            if (BoneType.CUSTOM_BONE.equals(model.boneType)) {
               model.modelName = properties.getProperty("custom_bone");
               if ("".equals(model.modelName)) {
                  model.errorMessage = String.format(
                     "You selected CUSTOM_BONE as the 'wear_type' in the cfg file for '%s' at '%s', yet you left the 'custom_bone' field right underneath it empty. If you want ur model to be parented to a specific bone, you have to enter the name of that bone at the field 'custom_bone'.",
                     modelName,
                     file.getAbsolutePath()
                  );
                  return;
               }
            }

            String girlsStr = properties.getProperty("which_girls");
            if (girlsStr == null) {
               girlsStr = "";
            }

            girlsStr = girlsStr.replace(" ", "");
            String[] girlNames = girlsStr.split(",");

            for (String girlName : girlNames) {
               try {
                  if (!"".equals(girlName)) {
                     model.allowedNpcTypes.add(NpcType.valueOf(girlName));
                  }
               } catch (IllegalArgumentException illegalArg2) {
                  model.errorMessage = String.format(
                     "you entered '%s' as one of the girls, you put into the 'which_girls' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'which_girls'.",
                     girlName,
                     modelName,
                     file.getAbsolutePath()
                  );
                  return;
               }
            }

            String lightingStr = properties.getProperty("which_lighting");
            if (lightingStr == null) {
               model.errorMessage = String.format(
                  "The %s's cfg file at '%s' doesn't contain the field 'which_lighting'. Go to the bottom of the cfg file and write either 'which_lighting=DEFAULT', 'which_lighting=SEXMOD', or 'which_lighting=NONE'.",
                  modelName,
                  file.getAbsolutePath()
               );
            } else {
               lightingStr = lightingStr.replace(" ", "");

               try {
                  model.lightingType = LightingType.valueOf(lightingStr);
               } catch (IllegalArgumentException illegalArg3) {
                  model.errorMessage = String.format(
                     "you entered '%s' into the 'which_lighting' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'which_lighting'.",
                     lightingStr,
                     modelName,
                     file.getAbsolutePath()
                  );
               }

               String author = properties.getProperty("author");
               if (author != null && !"".equals(author)) {
                  model.modelCode = author;
               } else {
                  model.modelCode = "anon";
               }

               String hideBonesStr = properties.getProperty("bones_to_hide");
               if (hideBonesStr != null && !"".equals(hideBonesStr)) {
                  hideBonesStr = hideBonesStr.replace(" ", "");
                  String[] boneNames = hideBonesStr.split(",");
                  model.customPartBones.addAll(Arrays.asList(boneNames));
               }

               String nudeStr = properties.getProperty("enable_when_nude");
               if (nudeStr == null) {
                  model.disabled = false;
               } else {
                  nudeStr = nudeStr.replace(" ", "");
                  model.disabled = nudeStr.equalsIgnoreCase("yes");
               }

               String scaleStr = properties.getProperty("gui_size_factor");
               if (scaleStr != null && !"".equals(scaleStr)) {
                  scaleStr = scaleStr.replace(" ", "");
                  scaleStr = scaleStr.replace(",", ".");

                  try {
                     model.scale = Float.parseFloat(scaleStr);
                  } catch (NumberFormatException numberExc) {
                     model.errorMessage = String.format(
                        "you entered '%s' into the 'gui_size_factor' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'gui_size_factor'.",
                        scaleStr,
                        modelName,
                        file.getAbsolutePath()
                     );
                  }
               }

               String posStr = properties.getProperty("gui_vertical_positioning");
               if (posStr != null && !"".equals(posStr)) {
                  posStr = posStr.replace(" ", "");
                  posStr = posStr.replace(",", ".");

                  try {
                     model.xOffset = Float.parseFloat(posStr);
                  } catch (NumberFormatException numberExc2) {
                     model.errorMessage = String.format(
                        "you entered '%s' into the 'gui_vertical_positioning' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'gui_vertical_positioning'.",
                        posStr,
                        modelName,
                        file.getAbsolutePath()
                     );
                  }
               }

               String versionStr = properties.getProperty("version");
               versionStr = versionStr.replace(" ", "");
               versionStr = versionStr.replace(",", ".");

               try {
                  model.zOffset = Float.parseFloat(versionStr);
               } catch (NumberFormatException numberExc3) {
                  model.errorMessage = String.format(
                     "you entered '%s' into the 'versionString' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'versionString'.",
                     versionStr,
                     modelName,
                     file.getAbsolutePath()
                  );
               }
            }
         }
      }
   }
}
