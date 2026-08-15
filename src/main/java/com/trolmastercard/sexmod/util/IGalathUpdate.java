package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.GalathEntity;

@FunctionalInterface
/**
 * Per-tick update hook for galath AI states.
 */
public interface IGalathUpdate {
   boolean update(GalathEntity galath);
}
