package com.trolmastercard.sexmod.api;

import com.trolmastercard.sexmod.entity.GalathEntity;

@FunctionalInterface
/**
 * Callback fired when a galath scene/fight finishes.
 */
public interface IGalathFinish {
   void finish(GalathEntity var1);
}
