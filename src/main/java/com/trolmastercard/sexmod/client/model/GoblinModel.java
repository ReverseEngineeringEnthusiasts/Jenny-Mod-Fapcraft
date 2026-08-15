package com.trolmastercard.sexmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Vanilla body model for the goblin: a blocky body with ears, nose and a
 * 4-segment tail (used by {@code WildSlimeRenderer} as the slime's body
 * shape). Static pose — no animation hooks.
 */
public class GoblinModel extends ModelBase {
   private final ModelRenderer body;
   private final ModelRenderer rightEar;
   private final ModelRenderer leftEar;
   private final ModelRenderer nose;
   private final ModelRenderer tail;

   public GoblinModel() {
      this.textureWidth = 64;
      this.textureHeight = 32;
      this.body = new ModelRenderer(this);
      this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.body.cubeList.add(new ModelBox(this.body, 0, 16, -3.0F, 17.0F, -3.0F, 6, 6, 6, 0.0F, true));
      this.rightEar = new ModelRenderer(this);
      this.rightEar.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.rightEar.cubeList.add(new ModelBox(this.rightEar, 32, 0, 1.3F, 18.0F, -3.5F, 2, 2, 2, 0.0F, true));
      this.leftEar = new ModelRenderer(this);
      this.leftEar.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.leftEar.cubeList.add(new ModelBox(this.leftEar, 32, 4, -3.3F, 18.0F, -3.5F, 2, 2, 2, 0.0F, true));
      this.nose = new ModelRenderer(this);
      this.nose.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.nose.cubeList.add(new ModelBox(this.nose, 32, 8, -1.0F, 21.0F, -3.5F, 1, 1, 1, 0.0F, true));
      this.tail = new ModelRenderer(this);
      this.tail.setRotationPoint(-0.5F, 0.0F, 0.1F);
      ModelRenderer tailSegment1 = new ModelRenderer(this);
      tailSegment1.setRotationPoint(2.0F, 20.7406F, 4.0504F);
      this.tail.addChild(tailSegment1);
      this.setRotation(tailSegment1, 1.0908F, 0.0F, 0.0F);
      tailSegment1.cubeList.add(new ModelBox(tailSegment1, 10, 11, -2.5F, 0.0F, 0.0F, 2, 2, 1, 0.0F, false));
      ModelRenderer tailSegment2 = new ModelRenderer(this);
      tailSegment2.setRotationPoint(2.0F, 19.9214F, 3.4768F);
      this.tail.addChild(tailSegment2);
      this.setRotation(tailSegment2, 0.6109F, 0.0F, 0.0F);
      tailSegment2.cubeList.add(new ModelBox(tailSegment2, 10, 11, -3.0F, 0.0F, 0.0F, 3, 1, 1, 0.0F, false));
      ModelRenderer tailSegment3 = new ModelRenderer(this);
      tailSegment3.setRotationPoint(2.0F, 19.0074F, 3.0643F);
      this.tail.addChild(tailSegment3);
      this.setRotation(tailSegment3, 0.3491F, 0.0F, 0.0F);
      tailSegment3.cubeList.add(new ModelBox(tailSegment3, 10, 11, -4.0F, 0.0F, 0.075F, 5, 1, 1, 0.0F, false));
      ModelRenderer tailSegment4 = new ModelRenderer(this);
      tailSegment4.setRotationPoint(0.0F, 17.925F, 3.5F);
      this.tail.addChild(tailSegment4);
      this.setRotation(tailSegment4, 0.1309F, 0.0F, 0.0F);
      tailSegment4.cubeList.add(new ModelBox(tailSegment4, 10, 11, -3.0F, -1.0F, -0.5F, 7, 2, 1, 0.0F, false));
   }

   public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.body.render(scale);
      this.rightEar.render(scale);
      this.leftEar.render(scale);
      this.nose.render(scale);
      this.tail.render(scale);
   }

   public void setRotation(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
   }
}
