package com.trolmastercard.sexmod.api;

/**
 * <b>Role.</b> Marker interface for girls that support the bed-scene walk-up:
 * {@code goToSexBed()} moves the girl to her bed before the scene starts.
 * Implemented by the beddable NPC girls (Jenny, Bia, ...); invoked by the
 * {@code SendGirlToSexPacket} flow. Girls that do not implement this interface
 * play their scenes in place.
 */
public interface IBeddableSexGirl {
   void goToSexBed();
}
