package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.util.math.Vec3d;

@FunctionalInterface
public interface IPositionProvider {
   Vec3d getPosition(BaseGirlEntity var1);
}
