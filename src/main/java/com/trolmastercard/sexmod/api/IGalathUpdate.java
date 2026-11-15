package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.common.entity.galath.GalathEntity;

@FunctionalInterface
/**
 * Per-tick update hook for galath AI states.
 */
public interface IGalathUpdate {
   boolean update(GalathEntity galath);
}
