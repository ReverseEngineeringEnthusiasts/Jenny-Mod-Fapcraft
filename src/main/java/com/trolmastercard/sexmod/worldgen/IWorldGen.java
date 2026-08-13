package com.trolmastercard.sexmod.worldgen;


import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IWorldGen {
   WorldServer b = FMLCommonHandler.instance().getMinecraftServerInstance().func_71218_a(0);
   PlacementSettings a = new PlacementSettings().func_186218_a(null).func_186222_a(false).func_186214_a(Mirror.NONE).func_186220_a(Rotation.NONE);
}
