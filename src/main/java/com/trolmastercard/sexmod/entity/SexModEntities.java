package com.trolmastercard.sexmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import com.trolmastercard.sexmod.Main;

/**
 * <b>Role.</b> One-shot entity registration for the whole mod: every NPC,
 * player-form, egg/projectile and filler entity plus the natural spawn rules
 * (slimes in swamps, bees in forests, pyrocinical/Manglelie in the Nether).
 * Entity ids come from {@link NpcType}; the id literals here for the
 * non-NpcType entities (kobold_egg, custom_model, friendly_slime, luna_hook,
 * energy_ball, pyrocinical) must never collide with each other.
 * <p>
 * <b>Pitfalls.</b> Registration order is irrelevant to Forge but the ids
 * must stay stable across versions (they are stored in the world/entities).
 * Player-form entities register with {@code sendVelocityUpdates = false};
 * spawn-able entities use a 50-block tracking range and egg colors.
 */
public class SexModEntities {
   public static void registerEntities() {
      registerSpawnEntity("jenny", JennyEntity.class, NpcType.JENNY.npcID, 3286592, 12655237);
      registerSpawnEntity("ellie", EllieEntity.class, NpcType.ELLIE.npcID, 1447446, 9961472);
      registerSpawnEntity("slime", SlimeEntity.class, NpcType.SLIME.npcID, 13167780, 8244330);
      registerSpawnEntity("bia", BiaEntity.class, NpcType.BIA.npcID, 7488816, 7254603);
      registerSpawnEntity("bee", BeeEntity.class, NpcType.BEE.npcID, 16701032, 4400155);
      registerSpawnEntity("luna", LunaEntity.class, NpcType.LUNA.npcID, 7881787, 7940422);
      registerEggEntity("allie", AllieEntity.class, NpcType.ALLIE.npcID);
      registerEggEntity("kobold", KoboldEntity.class, NpcType.KOBOLD.npcID);
      registerEggEntity("kobold_egg", KoboldEggEntity.class, 4674237);
      registerSpawnEntity("goblin", GoblinEntity.class, NpcType.GOBLIN.npcID, 39424, 19456);
      registerSpawnEntity("galath", GalathEntity.class, NpcType.GALATH.npcID, 16711680, 16711680);
      registerSpawnEntity("manglelie", ManglelieEntity.class, NpcType.MANGLELIE.npcID, 16382457, 8485574);
      registerEggEntity("custom_model", SexSceneEntity.class, 6281823);
      registerEntity("player_jenny", JennyPlayerEntity.class, NpcType.JENNY.playerID);
      registerEntity("player_ellie", ElliePlayerEntity.class, NpcType.ELLIE.playerID);
      registerEntity("player_slime", SlimePlayerEntity.class, NpcType.SLIME.playerID);
      registerEntity("player_bia", BiaPlayerEntity.class, NpcType.BIA.playerID);
      registerEntity("player_bee", BeePlayerEntity.class, NpcType.BEE.playerID);
      registerEntity("player_allie", AlliePlayerEntity.class, NpcType.ALLIE.playerID);
      registerEntity("player_kobold", KoboldPlayerEntity.class, NpcType.KOBOLD.playerID);
      registerEntity("player_goblin", GoblinPlayerEntity.class, NpcType.GOBLIN.playerID);
      registerEntity("player_luna", LunaPlayerEntity.class, NpcType.LUNA.playerID);
      registerEntity("player_galath", GalathPlayerEntity.class, NpcType.GALATH.playerID);
      registerEggEntity("friendly_slime", WildSlimeEntity.class, 5548484);
      registerEggEntity("luna_hook", SexEntity.class, 4768742);
      registerEggEntity("energy_ball", DragonEntity.class, 2565153);
      registerEggEntity("pyrocinical", BasicGirlEntity.class, 515153);
      EntityRegistry.addSpawn(SlimeEntity.class, 10, 1, 1, EnumCreatureType.CREATURE, new Biome[]{Biomes.SWAMPLAND, Biomes.MUTATED_SWAMPLAND});
      EntityRegistry.addSpawn(BeeEntity.class, 5, 1, 1, EnumCreatureType.CREATURE, new Biome[]{Biomes.FOREST, Biomes.FOREST_HILLS});
      EntityRegistry.addSpawn(BasicGirlEntity.class, 3, 1, 1, EnumCreatureType.AMBIENT, new Biome[]{Biomes.HELL});
      EntityRegistry.addSpawn(ManglelieEntity.class, 5, 1, 1, EnumCreatureType.AMBIENT, new Biome[]{Biomes.HELL});
   }

   private static void registerEntity(String name, Class<? extends Entity> entityClass, int id) {
      EntityRegistry.registerModEntity(new ResourceLocation("sexmod:" + name), entityClass, name, id, Main.instance, 100, 1, false);
   }

   private static void registerSpawnEntity(String name, Class<? extends Entity> entityClass, int id, int eggColor1, int eggColor2) {
      EntityRegistry.registerModEntity(new ResourceLocation("sexmod:" + name), entityClass, name, id, Main.instance, 50, 1, true, eggColor1, eggColor2);
   }

   private static void registerEggEntity(String name, Class<? extends Entity> entityClass, int id) {
      EntityRegistry.registerModEntity(new ResourceLocation("sexmod:" + name), entityClass, name, id, Main.instance, 50, 1, true);
   }
}
