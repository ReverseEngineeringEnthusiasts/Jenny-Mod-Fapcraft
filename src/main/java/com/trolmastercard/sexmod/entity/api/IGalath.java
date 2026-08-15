package com.trolmastercard.sexmod.entity.api;

import com.trolmastercard.sexmod.util.Vector4d;

/**
 * <b>Role.</b> The Galath render/shared contract, implemented by
 * {@link GalathEntity} and {@link GalathPlayerEntity}:
 * {@link #getFlightData()} feeds the flight-HUD/wing animation tilt, the
 * wing-animation flags decide whether the wing bones animate (dressed/action
 * dependent), and {@link #isHuggingManglelie()} marks the hug actions so the
 * renderer hides/positions her correctly.
 */
public interface IGalath {
   Vector4d getFlightData();

   boolean isWingsAnimated();

   boolean areWingsAnimated();

   boolean isHuggingManglelie();
}
