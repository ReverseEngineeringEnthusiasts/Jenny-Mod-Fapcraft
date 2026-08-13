package com.trolmastercard.sexmod.entity;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import com.trolmastercard.sexmod.Main;

public class SexModEntities {
   public static void registerEntities() {
      a("jenny", JennyEntity.class, NpcType.JENNY.npcID, 3286592, 12655237);
      a("ellie", EllieEntity.class, NpcType.ELLIE.npcID, 1447446, 9961472);
      a("slime", SlimeEntity.class, NpcType.SLIME.npcID, 13167780, 8244330);
      a("bia", BiaEntity.class, NpcType.BIA.npcID, 7488816, 7254603);
      a("bee", BeeEntity.class, NpcType.BEE.npcID, 16701032, 4400155);
      a("luna", LunaEntity.class, NpcType.LUNA.npcID, 7881787, 7940422);
      a("allie", AllieEntity.class, NpcType.ALLIE.npcID);
      a("kobold", KoboldEntity.class, NpcType.KOBOLD.npcID);
      a("kobold_egg", KoboldEggEntity.class, 4674237);
      a("goblin", GoblinEntity.class, NpcType.GOBLIN.npcID, 39424, 19456);
      a("galath", GalathEntity.class, NpcType.GALATH.npcID, 16711680, 16711680);
      a("manglelie", ManglelieEntity.class, NpcType.MANGLELIE.npcID, 16382457, 8485574);
      a("custom_model", SexSceneEntity.class, 6281823);
      b("player_jenny", JennyPlayerEntity.class, NpcType.JENNY.playerID);
      b("player_ellie", ElliePlayerEntity.class, NpcType.ELLIE.playerID);
      b("player_slime", SlimePlayerEntity.class, NpcType.SLIME.playerID);
      b("player_bia", BiaPlayerEntity.class, NpcType.BIA.playerID);
      b("player_bee", BeePlayerEntity.class, NpcType.BEE.playerID);
      b("player_allie", AlliePlayerEntity.class, NpcType.ALLIE.playerID);
      b("player_kobold", KoboldPlayerEntity.class, NpcType.KOBOLD.playerID);
      b("player_goblin", GoblinPlayerEntity.class, NpcType.GOBLIN.playerID);
      b("player_luna", LunaPlayerEntity.class, NpcType.LUNA.playerID);
      b("player_galath", GalathPlayerEntity.class, NpcType.GALATH.playerID);
      a("friendly_slime", WildSlimeEntity.class, 5548484);
      a("luna_hook", SexEntity.class, 4768742);
      a("energy_ball", DragonEntity.class, 2565153);
      a("pyrocinical", BasicGirlEntity.class, 515153);
      EntityRegistry.addSpawn(SlimeEntity.class, 10, 1, 1, EnumCreatureType.CREATURE, new Biome[]{Biomes.SWAMPLAND, Biomes.MUTATED_SWAMPLAND});
      EntityRegistry.addSpawn(BeeEntity.class, 5, 1, 1, EnumCreatureType.CREATURE, new Biome[]{Biomes.FOREST, Biomes.FOREST_HILLS});
      EntityRegistry.addSpawn(BasicGirlEntity.class, 3, 1, 1, EnumCreatureType.AMBIENT, new Biome[]{Biomes.HELL});
      EntityRegistry.addSpawn(ManglelieEntity.class, 5, 1, 1, EnumCreatureType.AMBIENT, new Biome[]{Biomes.HELL});
   }

   private static void b(String var0, Class<? extends Entity> var1, int var2) {
      EntityRegistry.registerModEntity(new ResourceLocation("sexmod:" + var0), var1, var0, var2, Main.instance, 100, 1, false);
   }

   private static void a(String var0, Class<? extends Entity> var1, int var2, int var3, int var4) {
      EntityRegistry.registerModEntity(new ResourceLocation("sexmod:" + var0), var1, var0, var2, Main.instance, 50, 1, true, var3, var4);
   }

   private static void a(String var0, Class<? extends Entity> var1, int var2) {
      EntityRegistry.registerModEntity(new ResourceLocation("sexmod:" + var0), var1, var0, var2, Main.instance, 50, 1, true);
   }
}
