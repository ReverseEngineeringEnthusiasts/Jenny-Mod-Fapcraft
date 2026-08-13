package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.api.ITargetProvider;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

public class DynamicTrailRenderer {
   static final int c = 30;
   static final int k = 6;
   static final int f = 6;
   static final float b = 0.15F;
   List<TrailSegment> g = new ArrayList<>();
   final int a;
   final IPositionProvider i;
   final ITargetProvider d;
   public final BaseGirlEntity e;
   final float j;
   final float h;

   public DynamicTrailRenderer(int var1, IPositionProvider var2, ITargetProvider var3, BaseGirlEntity var4, float var5, float var6) {
      this.a = var1;
      this.i = var2;
      this.d = var3;
      this.e = var4;
      this.j = var5;
      this.h = var6;
   }

   public void a(Minecraft var1, Tessellator var2, BufferBuilder var3, float var4) {
      if (this.g.size() < this.a) {
         for (int var5 = 0; var5 < 6; var5++) {
            Vec3d var6 = this.i.getPosition(this.e);
            this.g
               .add(
                  new TrailSegment(
                     var1.world,
                     this.d.getTargetPosition(this.e),
                     new Vec3d(
                        var6.x + (Reference.f.nextFloat() * 2.0F - 1.0F) * this.j,
                        var6.y + (Reference.f.nextFloat() * 2.0F - 1.0F) * this.j,
                        var6.z + (Reference.f.nextFloat() * 2.0F - 1.0F) * this.j
                     )
                  )
               );
         }
      }

      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      Vec3d var10 = RotationHelper.a(
         new Vec3d(var1.player.lastTickPosX, var1.player.lastTickPosY, var1.player.lastTickPosZ),
         var1.player.getPositionVector(),
         var4
      );
      var3.begin(9, DefaultVertexFormats.POSITION_COLOR);
      this.b_clash450();
      Vec3d var11 = null;

      for (TrailSegment var8 : this.g) {
         Vec3d var9 = RotationHelper.a(var8.d, var8.f, var4);
         if (var11 == null) {
            var11 = var9;
         }

         if (var11.distanceTo(var9) > this.h) {
            var2.draw();
            var3.begin(9, DefaultVertexFormats.POSITION_COLOR);
         }

         var3.pos(var9.x - var10.x, var9.y - var10.y, var9.z - var10.z)
            .color(255, 255, 255, 255)
            .endVertex();
         var11 = var9;
      }

      var2.draw();
      GlStateManager.enableCull();
   }

   public void a_clash449() {
      for (TrailSegment var2 : this.g) {
         var2.onUpdate();
      }
   }

   void b_clash450() {
      if (!this.g.isEmpty() && this.g.size() > 1) {
         for (int var1 = 1; var1 < this.g.size(); var1++) {
            TrailSegment var2 = this.g.get(var1);
            Vec3d var3 = var2.f;

            int var4;
            for (var4 = var1 - 1; var4 >= 0 && var3.distanceTo(this.g.get(var4).f) < var3.distanceTo(this.g.get(var4 + 1).f); var4--) {
               this.g.set(var4 + 1, this.g.get(var4));
            }

            this.g.set(var4 + 1, var2);
         }
      }
   }

}
