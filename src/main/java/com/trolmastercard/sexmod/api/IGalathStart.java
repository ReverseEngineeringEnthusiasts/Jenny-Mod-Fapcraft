package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.common.entity.galath.GalathEntity;

@FunctionalInterface
/**
 * Callback fired when a galath scene/fight starts.
 */
public interface IGalathStart {
   void start(GalathEntity galath);
}
