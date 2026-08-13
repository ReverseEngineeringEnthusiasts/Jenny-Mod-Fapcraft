package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.SummonItemModel;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.item.WinchesterItem;
import com.trolmastercard.sexmod.util.DebugMode;







import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class SummonItemRenderer extends GeoItemRenderer<WinchesterItem> {
   static final Vec3d a = new Vec3d(0.0, 1.0, 0.0);

   public SummonItemRenderer() {
      super(new SummonItemModel());
   }

   @Override
   public void render(WinchesterItem var1, ItemStack var2) {
      this.a(var1, var2);
   }

   public void a(WinchesterItem var1, ItemStack var2) {
      if (DebugMode.b[0] == 0.0F) {
         GL11.glDisable(2896);
      }

      super.render(var1, var2);
      GL11.glEnable(2896);
   }

   @Override
   public void renderCube(BufferBuilder var1, GeoCube var2, float var3, float var4, float var5, float var6) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);

      for (GeoQuad var10 : var2.quads) {
         if (var10 != null) {
            Vector3f var11 = new Vector3f(var10.normal.getX(), var10.normal.getY(), var10.normal.getZ());
            MATRIX_STACK.getNormalMatrix().transform(var11);
            if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var11.getX() < 0.0F) {
               var11.x *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var11.getY() < 0.0F) {
               var11.y *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var11.getZ() < 0.0F) {
               var11.z *= -1.0F;
            }

            Vec3d var12 = DebugMode.b[0] == 0.0F ? BodyParts.a(new Vec3d(var3, var4, var5), var11, a) : new Vec3d(var3, var4, var5);

            for (GeoVertex var16 : var10.vertices) {
               Vector4f var17 = new Vector4f(var16.position.getX(), var16.position.getY(), var16.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var17);
               var1.pos(var17.getX(), var17.getY(), var17.getZ())
                  .tex(var16.textureU, var16.textureV)
                  .color((float)var12.x, (float)var12.y, (float)var12.z, var6)
                  .normal(var11.getX(), var11.getY(), var11.getZ())
                  .endVertex();
            }
         }
      }
   }

}
