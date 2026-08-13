package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.Reference;







import net.minecraft.entity.Entity;

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

   NpcType(Class<? extends BaseGirlEntity> var3, int var4, Class<? extends AbstractPlayerGirlEntity> var5, int var6, boolean var7) {
      this.npcID = var4;
      this.playerID = var6;
      this.npcClass = var3;
      this.playerClass = var5;
      this.isNpcOnly = false;
      this.hasSpecifics = var7;
      this.editorID = Reference.b++;
   }

   NpcType(Class<? extends BaseGirlEntity> var3, int var4, Class<? extends AbstractPlayerGirlEntity> var5, int var6) {
      this.npcID = var4;
      this.playerID = var6;
      this.npcClass = var3;
      this.playerClass = var5;
      this.isNpcOnly = false;
      this.hasSpecifics = false;
      this.editorID = Reference.b++;
   }

   NpcType(Class<? extends BaseGirlEntity> var3, int var4) {
      this.npcID = var4;
      this.npcClass = var3;
      this.isNpcOnly = true;
      this.hasSpecifics = false;
      this.editorID = Reference.b++;
      this.playerClass = null;
      this.playerID = 0;
   }

   public static NpcType a_clash750(String var0) {
      for (NpcType var4 : values()) {
         if (var4.toString().equalsIgnoreCase(var0)) {
            return var4;
         }
      }

      return JENNY;
   }

   public static NpcType a_clash751(Entity var0) {
      if (!(var0 instanceof BaseGirlEntity)) {
         return null;
      }

      BaseGirlEntity var1 = (BaseGirlEntity)var0;
      Class var2 = var1.getClass();

      for (NpcType var6 : values()) {
         if (var2.equals(var6.npcClass)) {
            return var6;
         }

         if (var2.equals(var6.playerClass)) {
            return var6;
         }
      }

      return null;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
