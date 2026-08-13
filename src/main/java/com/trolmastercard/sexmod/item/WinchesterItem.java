package com.trolmastercard.sexmod.item;


import net.minecraft.util.ResourceLocation;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class WinchesterItem extends Item implements IAnimatable {
   public static final WinchesterItem b = new WinchesterItem();
   private final AnimationFactory a = new AnimationFactory(this);

   public static void register() {
      b.setRegistryName(new ResourceLocation("sexmod", "winchester"));
      b.setTranslationKey("winchester");
      MinecraftForge.EVENT_BUS.register(WinchesterItem.class);
   }

   @Override
   public void registerControllers(AnimationData var1) {
   }

   @Override
   public AnimationFactory getFactory() {
      return this.a;
   }
}
