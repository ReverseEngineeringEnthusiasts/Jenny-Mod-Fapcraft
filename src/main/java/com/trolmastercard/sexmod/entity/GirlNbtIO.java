package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

/**
 * NBT (de)serialisation for the state {@link BaseGirlEntity} owns itself:
 * home position, persistent girl id, custom name and custom-model code.
 * <p>
 * Extracted verbatim from the entity's {@code writeEntityToNBT} /
 * {@code readEntityFromNBT} overrides. The vanilla {@code super} calls stay in
 * the entity so read order (super first) and write order (fields then super)
 * are unchanged.
 */
final class GirlNbtIO {
   private GirlNbtIO() {
   }

   static void write(BaseGirlEntity girl, NBTTagCompound nbt) {
      nbt.setDouble("homeX", girl.homePos.x);
      nbt.setDouble("homeY", girl.homePos.y);
      nbt.setDouble("homeZ", girl.homePos.z);
      nbt.setString("girlID", (String)girl.entityDataManager.get(BaseGirlEntity.GIRL_ID));
      String customName = girl.getCustomName();
      if (!"".equals(customName)) {
         nbt.setString("sexmod:customname", customName);
      }

      if (girl.supportsCustomModels()) {
         nbt.setString("sexmod:customModel", girl.getCustomModelCode());
      }
   }

   static void read(BaseGirlEntity girl, NBTTagCompound nbt) {
      girl.homePos = new Vec3d(nbt.getDouble("homeX"), nbt.getDouble("homeY"), nbt.getDouble("homeZ"));
      String customName = nbt.getString("sexmod:customname");
      if (!"".equals(customName)) {
         girl.setCustomNameOverride(customName);
      }

      String girlId = nbt.getString("girlID");
      if (!"".equals(girlId)) {
         UUID uuid = UUID.fromString(girlId);
         boolean duped = false;

         for (BaseGirlEntity other : BaseGirlEntity.girlList(uuid)) {
            if (!other.world.isRemote && other != girl && !other.isDead && other.isAddedToWorld()) {
               duped = true;
               break;
            }
         }

         if (duped) {
            Main.LOGGER.log(Level.WARN, String.format("got a duped %s with id '%s'. Deleted her", girl.getDisplayNameText(), uuid));
            girl.world.removeEntity(girl);
         } else {
            girl.entityDataManager.set(BaseGirlEntity.GIRL_ID, uuid.toString());
            if (girl.supportsCustomModels()) {
               girl.setCustomModelCode(nbt.getString("sexmod:customModel"));
            }
         }
      }
   }
}
