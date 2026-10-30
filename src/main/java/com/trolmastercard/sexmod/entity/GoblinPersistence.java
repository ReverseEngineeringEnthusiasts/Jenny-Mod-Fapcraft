package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

/**
 * NBT persistence for {@link GoblinEntity}. The goblin-specific half of
 * {@code writeEntityToNBT}/{@code readEntityFromNBT}; the entity keeps the
 * {@code super} calls and delegates here.
 * <p>
 * Queen-only fields are written only when {@code isQueen} is set, and guards
 * are stored as {@code guard0}, {@code guard1}, ... read back until the first
 * empty entry.
 */
final class GoblinPersistence {
   private GoblinPersistence() {
   }

   static void write(GoblinEntity goblin, NBTTagCompound nbt) {
      nbt.setString("bodyColor", (String)goblin.entityDataManager.get(AbstractNpcOnlyEntity.CURRENT_ACTION));
      nbt.setInteger("eyeColorX", ((BlockPos)goblin.entityDataManager.get(AbstractNpcOnlyEntity.ACTION_TARGET_POS)).getX());
      nbt.setInteger("eyeColorY", ((BlockPos)goblin.entityDataManager.get(AbstractNpcOnlyEntity.ACTION_TARGET_POS)).getY());
      nbt.setInteger("eyeColorZ", ((BlockPos)goblin.entityDataManager.get(AbstractNpcOnlyEntity.ACTION_TARGET_POS)).getZ());
      nbt.setString("model", (String)goblin.entityDataManager.get(AbstractNpcOnlyEntity.APPEARANCE_DNA));
      nbt.setString("girlID", (String)goblin.entityDataManager.get(AbstractNpcOnlyEntity.GIRL_ID));
      nbt.setString("queen", (String)goblin.entityDataManager.get(GoblinEntity.aK));
      nbt.setBoolean("isQueen", goblin.isQueen);
      nbt.setBoolean("isTamed", (Boolean)goblin.entityDataManager.get(GoblinEntity.aC));
      nbt.setInteger("robTicks", goblin.robTicks);
      if (goblin.isQueen) {
         nbt.setBoolean("preggo", (Boolean)goblin.entityDataManager.get(GoblinEntity.aV));
         nbt.setFloat("throneRot", goblin.throneYaw);
         nbt.setDouble("thronePosX", goblin.thronePos.x);
         nbt.setDouble("thronePosY", goblin.thronePos.y);
         nbt.setDouble("thronePosZ", goblin.thronePos.z);
         nbt.setLong("impregnationTick", goblin.impregnationTick);

         for (int i = 0; i < goblin.guardUUIDs.size(); i++) {
            nbt.setString("guard" + i, goblin.guardUUIDs.get(i).toString());
         }
      }
   }

   static void read(GoblinEntity goblin, NBTTagCompound nbt) {
      goblin.isQueen = nbt.getBoolean("isQueen");
      goblin.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, nbt.getString("model"));
      goblin.entityDataManager.set(AbstractNpcOnlyEntity.CURRENT_ACTION, nbt.getString("bodyColor"));
      String[] parts = AbstractNpcOnlyEntity.getModelCodeParts(goblin);
      if (Integer.parseInt(parts[3]) > 7 || Integer.parseInt(parts[4]) > 7) {
         goblin.entityDataManager.set(
            AbstractNpcOnlyEntity.APPEARANCE_DNA, goblin.buildModelCodeDNA(new StringBuilder(), goblin.getModelPartIndex())
         );
         Main.LOGGER.log(Level.INFO, "updated an old Goblin");
      }

      goblin.entityDataManager.set(
         AbstractNpcOnlyEntity.ACTION_TARGET_POS,
         new BlockPos(nbt.getInteger("eyeColorX"), nbt.getInteger("eyeColorY"), nbt.getInteger("eyeColorZ"))
      );
      goblin.entityDataManager.set(AbstractNpcOnlyEntity.GIRL_ID, nbt.getString("girlID"));
      goblin.entityDataManager.set(GoblinEntity.aK, nbt.getString("queen"));
      goblin.entityDataManager.set(GoblinEntity.aC, nbt.getBoolean("isTamed"));
      goblin.robTicks = nbt.getInteger("robTicks");
      if (goblin.isQueen) {
         goblin.throneYaw = nbt.getFloat("throneRot");
         goblin.thronePos = new Vec3d(nbt.getDouble("thronePosX"), nbt.getDouble("thronePosY"), nbt.getDouble("thronePosZ"));

         for (int i = 0; !"".equals(nbt.getString("guard" + i)); i++) {
            goblin.guardUUIDs.add(UUID.fromString(nbt.getString("guard" + i)));
         }

         goblin.entityDataManager.set(GoblinEntity.aV, nbt.getBoolean("preggo"));
         goblin.impregnationTick = nbt.getLong("impregnationTick");
      }
   }
}
