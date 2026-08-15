package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;

@FunctionalInterface
/**
 * Supplies a bone rotation value (render-time hook for bone animation).
 */
public interface IBoneRotationSupplier {
   float getRotation(BaseGirlEntity girl);
}
