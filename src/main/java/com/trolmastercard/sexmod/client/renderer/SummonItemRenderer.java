package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.SummonItemModel;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.item.WinchesterItem;
import com.trolmastercard.sexmod.util.DebugMode;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

/**
 * Item renderer for the Winchester summoning staff ({@link WinchesterItem}):
 * geckolib model with a custom cube pass that (in non-debug mode) offsets each
 * cube's tint by its normal direction via
 * {@link BodyParts#offsetBonePosition}, creating a fake "depth shading" on the
 * model, and disables GL lighting so the shading reads as the only lighting.
 * <p>
 * CLIENT-side render thread only. Lighting is disabled per-render and
 * re-enabled afterwards — the GL light state must not leak into other
 * renders.
 */
public class SummonItemRenderer extends GeoItemRenderer<WinchesterItem> {
   static final Vec3d offsetVec = new Vec3d(0.0, 1.0, 0.0);

   public SummonItemRenderer() {
      super(new SummonItemModel());
   }

   @Override
   public void render(WinchesterItem item, ItemStack stack) {
      this.renderSummonItem(item, stack);
   }

   public void renderSummonItem(WinchesterItem item, ItemStack stack) {
      if (DebugMode.b[0] == 0.0F) {
         GL11.glDisable(2896);
      }

      super.render(item, stack);
      GL11.glEnable(2896);
   }

   /**
    * Cube pass: transforms normals (mirroring them for zero-size cube faces)
    * and emits tinted vertices. Tint = the render color offset by the normal
    * (fake lighting) when {@code DebugMode.b[0] == 0}, else the plain render
    * color.
    */
   @Override
   public void renderCube(BufferBuilder buffer, GeoCube cube, float r, float g, float b, float alpha) {
      MATRIX_STACK.moveToPivot(cube);
      MATRIX_STACK.rotate(cube);
      MATRIX_STACK.moveBackFromPivot(cube);

      for (GeoQuad quad : cube.quads) {
         if (quad != null) {
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            MATRIX_STACK.getNormalMatrix().transform(normal);
            if ((cube.size.y == 0.0F || cube.size.z == 0.0F) && normal.getX() < 0.0F) {
               normal.x *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.z == 0.0F) && normal.getY() < 0.0F) {
               normal.y *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.y == 0.0F) && normal.getZ() < 0.0F) {
               normal.z *= -1.0F;
            }

            Vec3d tint = DebugMode.b[0] == 0.0F ? BodyParts.offsetBonePosition(new Vec3d(r, g, b), normal, offsetVec) : new Vec3d(r, g, b);

            for (GeoVertex vertex : quad.vertices) {
               Vector4f matrixPos = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(matrixPos);
               buffer.pos(matrixPos.getX(), matrixPos.getY(), matrixPos.getZ())
                  .tex(vertex.textureU, vertex.textureV)
                  .color((float)tint.x, (float)tint.y, (float)tint.z, alpha)
                  .normal(normal.getX(), normal.getY(), normal.getZ())
                  .endVertex();
            }
         }
      }
   }

}
