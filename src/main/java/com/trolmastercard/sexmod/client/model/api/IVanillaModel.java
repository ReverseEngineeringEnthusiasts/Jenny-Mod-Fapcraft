package com.trolmastercard.sexmod.client.model.api;

import net.minecraft.client.model.ModelRenderer;

/**
 * Minimal contract for classes exposing a vanilla {@link ModelRenderer} (used
 * by the vanilla-model rendering paths for NPCs like the slime).
 */
public interface IVanillaModel {
   ModelRenderer getModel();
}
