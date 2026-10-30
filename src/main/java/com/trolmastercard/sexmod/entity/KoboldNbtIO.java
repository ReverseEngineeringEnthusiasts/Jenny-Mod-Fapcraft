package com.trolmastercard.sexmod.entity;

import com.google.common.base.Optional;
import com.trolmastercard.sexmod.util.KoboldManager;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * NBT persistence for {@link KoboldEntity}: body size, eye colour, appearance
 * DNA, name, master, inventory, body colour and the tribe block (tribe id,
 * leader flag, tribe name).
 * <p>
 * <b>Pitfalls.</b> The key names are part of the save format — renaming any of
 * them silently wipes that field on existing worlds. {@link #read} keeps the
 * tribe lookup inside a {@code !isDead} guard so a despawned kobold re-inserted
 * by chunk load cannot resurrect a tribe entry for a corpse.
 */
final class KoboldNbtIO {
   private KoboldNbtIO() {
   }

   static void write(KoboldEntity entity, NBTTagCompound nbt) {
      nbt.setFloat("body_size", (Float)entity.getDataManager().get(KoboldEntity.aE));
      nbt.setInteger("eyeColorX", ((BlockPos)entity.getDataManager().get(KoboldEntity.ACTION_TARGET_POS)).getX());
      nbt.setInteger("eyeColorY", ((BlockPos)entity.getDataManager().get(KoboldEntity.ACTION_TARGET_POS)).getY());
      nbt.setInteger("eyeColorZ", ((BlockPos)entity.getDataManager().get(KoboldEntity.ACTION_TARGET_POS)).getZ());
      nbt.setString("model", (String)entity.getDataManager().get(KoboldEntity.APPEARANCE_DNA));
      nbt.setString("name", (String)entity.getDataManager().get(KoboldEntity.KOBOLD_NAME));
      nbt.setString("master", (String)entity.getDataManager().get(KoboldEntity.MASTER));
      nbt.setTag("inventory", entity.inventory.serializeNBT());
      nbt.setString("bodyColor", (String)entity.getDataManager().get(KoboldEntity.CURRENT_ACTION));
      nbt.setBoolean("editedColorManually", entity.aA);
      Optional tribeOpt = (Optional)entity.getDataManager().get(KoboldEntity.aL);
      if (tribeOpt.isPresent()) {
         nbt.setUniqueId("tribeId", (UUID)tribeOpt.get());
         nbt.setBoolean("isLeader", KoboldManager.isTribeMember((UUID)tribeOpt.get(), entity));
         nbt.setString("tribeName", (String)entity.getDataManager().get(KoboldEntity.aU));
      }
   }

   static void read(KoboldEntity entity, NBTTagCompound nbt) {
      String modelCode = nbt.getString("model");
      if (!"".equals(modelCode)) {
         entity.getDataManager().set(KoboldEntity.APPEARANCE_DNA, modelCode);
      }

      BlockPos eyeColor = new BlockPos(nbt.getInteger("eyeColorX"), nbt.getInteger("eyeColorY"), nbt.getInteger("eyeColorZ"));
      if (!BlockPos.ORIGIN.equals(eyeColor)) {
         entity.getDataManager().set(KoboldEntity.ACTION_TARGET_POS, eyeColor);
      }

      entity.getDataManager().set(KoboldEntity.aE, nbt.getFloat("body_size"));
      entity.getDataManager().set(KoboldEntity.KOBOLD_NAME, nbt.getString("name"));
      entity.getDataManager().set(KoboldEntity.MASTER, nbt.getString("master"));
      entity.inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
      String bodyColor = nbt.getString("bodyColor");
      if (!"".equals(bodyColor)) {
         entity.getDataManager().set(KoboldEntity.CURRENT_ACTION, nbt.getString("bodyColor"));
      }

      entity.aA = nbt.getBoolean("editedColorManually");
      UUID tribeId = nbt.getUniqueId("tribeId");
      if (tribeId != null && !entity.isDead) {
         entity.getDataManager().set(KoboldEntity.aL, Optional.of(tribeId));
         if (!KoboldManager.doesTribeExist(tribeId)) {
            KoboldManager.setTribeColor(tribeId, EyeAndKoboldColor.valueOf((String)entity.getDataManager().get(KoboldEntity.CURRENT_ACTION)));
         }

         KoboldManager.addTribeMember(tribeId, entity);
         if (nbt.getBoolean("isLeader")) {
            KoboldManager.isTribeMember(tribeId, entity);
         }

         entity.getDataManager().set(KoboldEntity.aU, nbt.getString("tribeName"));
      }
   }
}
