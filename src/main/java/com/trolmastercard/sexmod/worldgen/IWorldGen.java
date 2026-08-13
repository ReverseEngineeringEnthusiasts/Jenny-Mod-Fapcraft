package com.trolmastercard.sexmod.worldgen;


import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IWorldGen {
   WorldServer b = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
   PlacementSettings a = new PlacementSettings().setChunk(null).setIgnoreEntities(false).setMirror(Mirror.NONE).setRotation(Rotation.NONE);
}
