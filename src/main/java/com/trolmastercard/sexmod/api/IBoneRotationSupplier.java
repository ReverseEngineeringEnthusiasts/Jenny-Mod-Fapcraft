package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;

@FunctionalInterface
/**
 * Supplies a bone rotation value (render-time hook for bone animation).
 */
public interface IBoneRotationSupplier {
   float getRotation(BaseGirlEntity girl);
}
