package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.util.math.Vec3d;

@FunctionalInterface
/**
 * Provides a target entity (used by scene targeting helpers).
 */
public interface ITargetProvider {
   Vec3d getTargetPosition(BaseGirlEntity girl);
}
