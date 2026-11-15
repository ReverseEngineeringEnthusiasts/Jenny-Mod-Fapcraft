package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.common.entity.galath.GalathEntity;

@FunctionalInterface
/**
 * Executable action hook for galath AI states.
 */
public interface IGalathExecute {
   boolean canExecute(GalathEntity galath);
}
