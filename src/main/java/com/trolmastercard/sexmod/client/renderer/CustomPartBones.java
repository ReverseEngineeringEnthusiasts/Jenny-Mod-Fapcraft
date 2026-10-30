package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.ClothingScreen;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import java.util.HashSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Resolves which bones are currently provided by custom model parts, i.e. the
 * bones the stock model must not draw (see
 * {@link GirlRenderer#getActiveBones(Boolean, boolean)}).
 * <p>
 * <b>Selection.</b> Bone names come from the {@link ClothingScreen} preview
 * (locally registered girls) or from the girl's own custom-part set; a part
 * contributes its bones when its model is disabled or the girl is dressed.
 * Empty in the {@link SexWorldClient} preload world.
 * <p>
 * CLIENT-side render thread only (reads client GUI / whitelist state).
 */
@SideOnly(Side.CLIENT)
final class CustomPartBones {
   private CustomPartBones() {
   }

   /**
    * The set of bones currently replaced by custom parts.
    *
    * @param isPreview   choose bone names from the {@link ClothingScreen} preview
    * @param isNudeOutfit whether the girl is nude (parts still hide their bones)
    */
   static HashSet<String> resolve(BaseGirlEntity girl, Boolean isPreview, boolean isNudeOutfit) {
      if (ClientProxy.IS_PRELOADING) {
         return new HashSet<>();
      }

      HashSet boneNameSet;
      if (isPreview) {
         boneNameSet = ClothingScreen.getCustomBoneNames();
      } else {
         boneNameSet = girl.getCustomPartsSet();
      }

      HashSet resultSet = new HashSet();

      for (String boneName : (java.util.Collection<String>) (boneNameSet) ) {
         ServerWhitelistManager.ModelData modelData = ServerWhitelistManager.getModelDataForGirl(boneName);
         if (modelData != null && (modelData.isDisabled() || !isNudeOutfit)) {
            resultSet.addAll(modelData.getCustomPartBones());
         }
      }

      return resultSet;
   }
}
