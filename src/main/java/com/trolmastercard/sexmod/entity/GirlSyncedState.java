package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.SceneDebug;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * Every accessor for the state {@link BaseGirlEntity} keeps in the vanilla
 * {@link net.minecraft.network.datasync.EntityDataManager}: action, outfit,
 * interaction partner, anchor target/yaw, master binding, custom name and
 * custom-model code.
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}, which keeps each method as a
 * delegate, so the public/protected API seen by subclasses is unchanged.
 * Client writes still go through {@code changeDataParameterFromClient} and
 * overridable accessors are called back through the girl instance.
 */
final class GirlSyncedState {
   private GirlSyncedState() {
   }

   static void setWalkSpeed(BaseGirlEntity girl, BaseGirlEntity.BaseGirlEntityState state) {
      girl.entityDataManager.set(BaseGirlEntity.WALK_SPEED, state.toString());
   }

   static BaseGirlEntity.BaseGirlEntityState getWalkType(BaseGirlEntity girl) {
      return BaseGirlEntity.BaseGirlEntityState.valueOf((String)girl.entityDataManager.get(BaseGirlEntity.WALK_SPEED));
   }

   static UUID getGirlId(BaseGirlEntity girl) {
      try {
         return UUID.fromString((String)girl.entityDataManager.get(BaseGirlEntity.GIRL_ID));
      } catch (Exception ex) {
         UUID id = UUID.randomUUID();
         girl.entityDataManager.set(BaseGirlEntity.GIRL_ID, id.toString());
         return id;
      }
   }

   static Action getCurrentAction(BaseGirlEntity girl) {
      return Action.valueOf((String)girl.entityDataManager.get(BaseGirlEntity.CUR_ACTION));
   }

   static void setCurrentAction(BaseGirlEntity girl, Action action) {
      Action previousAction = girl.getCurrentAction();
      SceneDebug.log(SceneDebug.ACTIONS, "setCurrentAction %s -> %s (%s, remote=%s, anchored=%s)", girl.getDisplayNameText(), previousAction, action, girl.world.isRemote, girl.isAnchored());
      if (previousAction != action) {
         if (action != Action.ATTACK || previousAction == Action.NULL) {
            action = action == null ? Action.NULL : action;
            if (girl.world.isRemote) {
               girl.changeDataParameterFromClient("currentAction", action.toString());
            } else {
               previousAction.ticksPlaying = new int[]{0, 0};
               girl.entityDataManager.set(BaseGirlEntity.CUR_ACTION, action.toString());
            }
         }
      }
   }

   static int getOutfitIndex(BaseGirlEntity girl) {
      return (Integer)girl.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX);
   }

   static void setOutfitIndex(BaseGirlEntity girl, int index) {
      if (girl.world.isRemote) {
         girl.changeDataParameterFromClient("currentModel", "0");
      } else {
         girl.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, index);
      }
   }

   @Nullable
   static EntityPlayer getPlayerEntity(BaseGirlEntity girl) {
      UUID uuid = girl.getInteractionPlayerUUID();
      return uuid == null ? null : girl.world.getPlayerEntityByUUID(uuid);
   }

   @Nullable
   static UUID getInteractionPlayerUUID(BaseGirlEntity girl) {
      String uuidStr = (String)girl.entityDataManager.get(BaseGirlEntity.INTERACTION_PARTNER_UUID);
      if (girl.cachedInteractionPlayerRaw == null || !girl.cachedInteractionPlayerRaw.equals(uuidStr)) {
         girl.cachedInteractionPlayerUUID = uuidStr.equals("null") ? null : UUID.fromString(uuidStr);
         girl.cachedInteractionPlayerRaw = uuidStr;
      }

      return girl.cachedInteractionPlayerUUID;
   }

   static void setInteractionPlayerUUID(BaseGirlEntity girl, UUID uuid) {
      if (girl.world.isRemote) {
         if (uuid == null) {
            girl.changeDataParameterFromClient("playerSheHasSexWith", null);
         } else {
            girl.changeDataParameterFromClient("playerSheHasSexWith", uuid.toString());
         }
      } else {
         if (uuid == null) {
            girl.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, "null");
         } else {
            girl.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, uuid.toString());
         }
      }
   }

   static void setInteractionPlayer(BaseGirlEntity girl, @Nonnull EntityPlayer player) {
      girl.setInteractionPlayerUUID(player.getPersistentID());
   }

   static Vec3d getTargetPosition(BaseGirlEntity girl) {
      String raw = (String)girl.entityDataManager.get(BaseGirlEntity.TARGET_POS);
      if (girl.cachedTargetPosRaw == null || !girl.cachedTargetPosRaw.equals(raw)) {
         String[] parts = raw.split("\\|");
         girl.cachedTargetPos = new Vec3d(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
         girl.cachedTargetPosRaw = raw;
      }

      return girl.cachedTargetPos;
   }

   static void setTargetPosition(BaseGirlEntity girl, Vec3d pos) {
      if (girl.world.isRemote) {
         String formatted = pos.x + "f" + pos.y + "f" + pos.z + "f";
         girl.changeDataParameterFromClient("targetPos", formatted);
      } else {
         girl.entityDataManager.set(BaseGirlEntity.TARGET_POS, pos.x + "|" + pos.y + "|" + pos.z);
      }
   }

   static void setTargetPositionDirect(BaseGirlEntity girl, Vec3d pos) {
      girl.entityDataManager.set(BaseGirlEntity.TARGET_POS, pos.x + "|" + pos.y + "|" + pos.z);
   }

   static Float getYawRotation(BaseGirlEntity girl) {
      return (Float)girl.entityDataManager.get(BaseGirlEntity.YAW_ROTATION);
   }

   static void setYawRotation(BaseGirlEntity girl, float yaw) {
      girl.entityDataManager.set(BaseGirlEntity.YAW_ROTATION, yaw);
   }

   static void setAnchored(BaseGirlEntity girl, boolean anchored) {
      if (girl.world.isRemote) {
         girl.changeDataParameterFromClient("shouldbeattargetpos", String.valueOf(anchored));
      } else {
         girl.entityDataManager.set(BaseGirlEntity.IS_ANCHORED, anchored);
      }
   }

   static boolean isAnchored(BaseGirlEntity girl) {
      return (Boolean)girl.entityDataManager.get(BaseGirlEntity.IS_ANCHORED);
   }

   static void goHome(BaseGirlEntity girl) {
      if (girl.world.isRemote) {
         girl.changeDataParameterFromClient("master", "");
         girl.changeDataParameterFromClient("walk speed", BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      } else {
         girl.entityDataManager.set(BaseGirlEntity.MASTER, "");
         girl.entityDataManager.set(BaseGirlEntity.WALK_SPEED, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      }
   }

   static boolean hasMaster(BaseGirlEntity girl) {
      return !((String)girl.entityDataManager.get(BaseGirlEntity.MASTER)).equals("");
   }

   @Nullable
   static UUID getMasterUUID(BaseGirlEntity girl) {
      String masterUuid = (String)girl.entityDataManager.get(BaseGirlEntity.MASTER);
      if ("".equals(masterUuid)) {
         return null;
      }

      try {
         return UUID.fromString(masterUuid);
      } catch (IllegalArgumentException ex) {
         return null;
      }
   }

   @Nullable
   static EntityPlayer getMasterPlayer(BaseGirlEntity girl) {
      UUID masterUuid = girl.getMasterUUID();
      return masterUuid == null ? null : girl.world.getPlayerEntityByUUID(masterUuid);
   }

   static void setCustomNameOverride(BaseGirlEntity girl, String name) {
      girl.entityDataManager.set(BaseGirlEntity.CUSTOM_NAME, name);
   }

   static String getCustomName(BaseGirlEntity girl) {
      return (String)girl.entityDataManager.get(BaseGirlEntity.CUSTOM_NAME);
   }

   static String getEffectiveDisplayName(BaseGirlEntity girl) {
      String customName = (String)girl.entityDataManager.get(BaseGirlEntity.CUSTOM_NAME);
      return !"".equals(customName) ? customName : girl.getDisplayNameText();
   }

   static void setCustomModelCode(BaseGirlEntity girl, String modelCode) {
      girl.entityDataManager.set(BaseGirlEntity.CUSTOM_MODEL_KEY, modelCode);
   }

   static String getCustomModelCode(BaseGirlEntity girl) {
      return (String)girl.entityDataManager.get(BaseGirlEntity.CUSTOM_MODEL_KEY);
   }
}
