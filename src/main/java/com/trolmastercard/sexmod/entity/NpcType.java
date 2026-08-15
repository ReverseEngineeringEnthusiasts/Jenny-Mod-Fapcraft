package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.Reference;
import net.minecraft.entity.Entity;

/**
 * <b>Role.</b> The canonical registry mapping every girl type to its NPC and
 * player-form classes plus their entity ids. {@link #getNpcType(Entity)}
 * resolves the type of any girl entity; the npc/player id pairs are used by
 * {@link SexModEntities} and by the "sexmod:GirlSpecific" / "sexmod:CustomModel"
 * per-player data keys (suffixed with the npc type name).
 * <p>
 * <b>Pitfalls.</b> The {@code npcID}/{@code playerID} values are persisted
 * entity ids — do not renumber. {@link #getNpcTypeByName(String)} falls back
 * to {@link #JENNY} for unknown names. {@link #MANGLELIE} is NPC-only
 * ({@code isNpcOnly}); KOBOLD and GOBLIN set {@code hasSpecifics}.
 */
public enum NpcType {
   JENNY(JennyEntity.class, 177013, JennyPlayerEntity.class, 12388645),
   ELLIE(EllieEntity.class, 228922, ElliePlayerEntity.class, 46348348),
   BIA(BiaEntity.class, 230053, BiaPlayerEntity.class, 65456415),
   SLIME(SlimeEntity.class, 168597, SlimePlayerEntity.class, 54816432),
   BEE(BeeEntity.class, 4663354, BeePlayerEntity.class, 48648638),
   ALLIE(AllieEntity.class, 5614613, AlliePlayerEntity.class, 64867483),
   LUNA(LunaEntity.class, 6816463, LunaPlayerEntity.class, 81234824),
   KOBOLD(KoboldEntity.class, 5648456, KoboldPlayerEntity.class, 62484851, true),
   GOBLIN(GoblinEntity.class, 4567275, GoblinPlayerEntity.class, 6584344, true),
   GALATH(GalathEntity.class, 314351, GalathPlayerEntity.class, 652535516),
   MANGLELIE(ManglelieEntity.class, 618151);

   public final int npcID;
   public final int playerID;
   public final Class<? extends BaseGirlEntity> npcClass;
   public final Class<? extends AbstractPlayerGirlEntity> playerClass;
   public final boolean isNpcOnly;
   public final int editorID;
   public final boolean hasSpecifics;

   NpcType(Class<? extends BaseGirlEntity> npcClass, int npcID, Class<? extends AbstractPlayerGirlEntity> playerClass, int playerID, boolean hasSpecifics) {
      this.npcID = npcID;
      this.playerID = playerID;
      this.npcClass = npcClass;
      this.playerClass = playerClass;
      this.isNpcOnly = false;
      this.hasSpecifics = hasSpecifics;
      this.editorID = Reference.EDITOR_ID_COUNTER++;
   }

   NpcType(Class<? extends BaseGirlEntity> npcClass, int npcID, Class<? extends AbstractPlayerGirlEntity> playerClass, int playerID) {
      this.npcID = npcID;
      this.playerID = playerID;
      this.npcClass = npcClass;
      this.playerClass = playerClass;
      this.isNpcOnly = false;
      this.hasSpecifics = false;
      this.editorID = Reference.EDITOR_ID_COUNTER++;
   }

   NpcType(Class<? extends BaseGirlEntity> npcClass, int npcID) {
      this.npcID = npcID;
      this.npcClass = npcClass;
      this.isNpcOnly = true;
      this.hasSpecifics = false;
      this.editorID = Reference.EDITOR_ID_COUNTER++;
      this.playerClass = null;
      this.playerID = 0;
   }

   public static NpcType getNpcTypeByName(String name) {
      for (NpcType type : values()) {
         if (type.toString().equalsIgnoreCase(name)) {
            return type;
         }
      }

      return JENNY;
   }

   public static NpcType getNpcType(Entity entity) {
      if (!(entity instanceof BaseGirlEntity)) {
         return null;
      }

      BaseGirlEntity girl = (BaseGirlEntity)entity;
      Class clazz = girl.getClass();

      for (NpcType type : values()) {
         if (clazz.equals(type.npcClass)) {
            return type;
         }

         if (clazz.equals(type.playerClass)) {
            return type;
         }
      }

      return null;
   }

}
