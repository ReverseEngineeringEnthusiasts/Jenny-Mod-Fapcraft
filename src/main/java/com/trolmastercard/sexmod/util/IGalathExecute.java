package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.GalathEntity;

@FunctionalInterface
/**
 * Executable action hook for galath AI states.
 */
public interface IGalathExecute {
   boolean canExecute(GalathEntity var1);
}
