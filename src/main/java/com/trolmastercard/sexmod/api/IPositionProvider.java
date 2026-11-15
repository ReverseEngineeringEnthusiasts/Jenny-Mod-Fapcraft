package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;
import net.minecraft.util.math.Vec3d;

@FunctionalInterface
/**
 * Provides a Vec3d position (used by scene positioning helpers).
 */
public interface IPositionProvider {
   Vec3d getPosition(BaseGirlEntity girl);
}
