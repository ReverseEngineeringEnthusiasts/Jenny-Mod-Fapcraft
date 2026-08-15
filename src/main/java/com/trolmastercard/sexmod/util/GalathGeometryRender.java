package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * <b>Role.</b> CLIENT-side geometry construction for the Galath model's custom
 * translucent overlays (body/wing meshes drawn in the editor and in-game).
 * Builds quad meshes from bone offsets and rotation parameters: {@link #buildBodyBoneMesh}
 * = wing mesh + body mesh; {@link #buildBodyMesh} = body line + wing mesh.
 * <p>
 * <b>Pitfalls.</b> The 8/12-point vertex layouts are indexed by the mesh
 * builders — reordering a vertex breaks the quads. Bones are resolved via
 * {@link BaseGirlEntity#getAnimationProcessor()} and
 * {@code getCachedBoneOffset}; a missing bone yields zeroed vertices
 * (tolerated, renders nothing).
 */
public class GalathGeometryRender {
   public static Vec3d[][] buildBodyBoneMesh(
      BaseGirlEntity girl, float partialTicks, String leftBoneName, String rightBoneName, String midBoneName, float innerRadius, float outerRadius, float innerLength, float outerLength, String wingBoneName
   ) {
      Vec3d[] mesh = buildWingBoneMesh(girl, partialTicks, leftBoneName, rightBoneName, midBoneName, innerRadius, outerRadius, innerLength, outerLength, wingBoneName);
      return buildBodyMesh(mesh);
   }

   public static Vec3d[][] buildBodyMesh(BaseGirlEntity girl, float partialTicks, String leftBoneName, String rightBoneName, Vector3fSexmodSpecial leftOffset, Vector3fSexmodSpecial rightOffset) {
      Vec3d[] line = buildBodyMeshLine(girl, partialTicks, leftBoneName, rightBoneName, leftOffset, rightOffset);
      return buildWingMesh(line);
   }

   static Vec3d[] buildBodyMeshLine(BaseGirlEntity girl, float partialTicks, String leftBoneName, String rightBoneName, Vector3fSexmodSpecial leftOffset, Vector3fSexmodSpecial rightOffset) {
      Vec3d leftPos = girl.getCachedBoneOffset(leftBoneName);
      Vec3d rightPos = girl.getCachedBoneOffset(rightBoneName);
      Vec3d[] corners = new Vec3d[8];
      if (leftOffset.x == 0.0F && rightOffset.x == 0.0F) {
         corners[0] = new Vec3d(0.0, leftOffset.y, leftOffset.z);
         corners[1] = new Vec3d(0.0, -leftOffset.y, leftOffset.z);
         corners[2] = new Vec3d(0.0, -leftOffset.y, -leftOffset.z);
         corners[3] = new Vec3d(0.0, leftOffset.y, -leftOffset.z);
         corners[4] = new Vec3d(0.0, rightOffset.y, rightOffset.z);
         corners[5] = new Vec3d(0.0, -rightOffset.y, rightOffset.z);
         corners[6] = new Vec3d(0.0, -rightOffset.y, -rightOffset.z);
         corners[7] = new Vec3d(0.0, rightOffset.y, -rightOffset.z);
      } else {
         corners[0] = new Vec3d(leftOffset.x, leftOffset.y, 0.0);
         corners[1] = new Vec3d(-leftOffset.x, leftOffset.y, 0.0);
         corners[2] = new Vec3d(-leftOffset.x, -leftOffset.y, 0.0);
         corners[3] = new Vec3d(leftOffset.x, -leftOffset.y, 0.0);
         corners[4] = new Vec3d(rightOffset.x, rightOffset.y, 0.0);
         corners[5] = new Vec3d(-rightOffset.x, rightOffset.y, 0.0);
         corners[6] = new Vec3d(-rightOffset.x, -rightOffset.y, 0.0);
         corners[7] = new Vec3d(rightOffset.x, -rightOffset.y, 0.0);
      }

      for (int i = 0; i < corners.length; i++) {
         corners[i] = VectorMath.rotateByYaw(corners[i], partialTicks);
      }

      for (int i2 = 0; i2 < 4; i2++) {
         corners[i2] = corners[i2].add(leftPos);
      }

      for (int i3 = 4; i3 < 8; i3++) {
         corners[i3] = corners[i3].add(rightPos);
      }

      return corners;
   }

   static Vec3d[][] buildWingMesh(Vec3d[] bodyCorners) {
      Vec3d[][] wingMesh = new Vec3d[6][4];
      wingMesh[0][0] = bodyCorners[0];
      wingMesh[0][1] = bodyCorners[1];
      wingMesh[0][2] = bodyCorners[2];
      wingMesh[0][3] = bodyCorners[3];
      wingMesh[1][0] = bodyCorners[4];
      wingMesh[1][1] = bodyCorners[5];
      wingMesh[1][2] = bodyCorners[6];
      wingMesh[1][3] = bodyCorners[7];
      wingMesh[2][0] = bodyCorners[1];
      wingMesh[2][1] = bodyCorners[2];
      wingMesh[2][2] = bodyCorners[6];
      wingMesh[2][3] = bodyCorners[5];
      wingMesh[3][0] = bodyCorners[3];
      wingMesh[3][1] = bodyCorners[7];
      wingMesh[3][2] = bodyCorners[4];
      wingMesh[3][3] = bodyCorners[0];
      wingMesh[4][0] = bodyCorners[1];
      wingMesh[4][1] = bodyCorners[0];
      wingMesh[4][2] = bodyCorners[4];
      wingMesh[4][3] = bodyCorners[5];
      wingMesh[5][0] = bodyCorners[2];
      wingMesh[5][1] = bodyCorners[3];
      wingMesh[5][2] = bodyCorners[7];
      wingMesh[5][3] = bodyCorners[6];
      return wingMesh;
   }

   static Vec3d[] buildWingBoneMesh(BaseGirlEntity girl, float partialTicks, String leftBoneName, String rightBoneName, String midBoneName, float innerRadius, float outerRadius, float innerLength, float outerLength, String boneName) {
      IBone bone = girl.getAnimationProcessor().getBone(boneName);
      if (bone == null) {
         Vec3d[] zeros = new Vec3d[12];
         Arrays.fill(zeros, Vec3d.ZERO);
         return zeros;
      }

      float rotY = TrigMath.toDegrees(bone.getRotationY());
      float rotZ = TrigMath.toDegrees(bone.getRotationZ());
      Vec3d leftPos = girl.getCachedBoneOffset(leftBoneName);
      Vec3d rightPos = girl.getCachedBoneOffset(rightBoneName);
      Vec3d midPos = girl.getCachedBoneOffset(midBoneName);
      Vec3d[] corners = new Vec3d[]{
         new Vec3d(innerRadius, 0.0, -outerRadius),
         new Vec3d(-innerRadius, 0.0, -outerRadius),
         new Vec3d(-innerRadius, 0.0, outerRadius),
         new Vec3d(innerRadius, 0.0, outerRadius),
         new Vec3d(innerRadius, outerRadius, 0.0),
         new Vec3d(-innerRadius, outerRadius, 0.0),
         new Vec3d(-innerRadius, -outerRadius, 0.0),
         new Vec3d(innerRadius, -outerRadius, 0.0),
         new Vec3d(innerLength, 0.0, -outerLength),
         new Vec3d(-innerLength, 0.0, -outerLength),
         new Vec3d(-innerLength, 0.0, outerLength),
         new Vec3d(innerLength, 0.0, outerLength)
      };

      for (int i = 0; i < corners.length; i++) {
         corners[i] = VectorMath.rotateByYaw(corners[i], partialTicks);
      }

      for (int i2 = 0; i2 < 4; i2++) {
         corners[i2] = VectorMath.rotateByEuler(corners[i2], 0.0F, rotY, rotZ);
      }

      for (int i3 = 0; i3 < 4; i3++) {
         corners[i3] = corners[i3].add(leftPos);
      }

      for (int i4 = 4; i4 < 8; i4++) {
         corners[i4] = corners[i4].add(rightPos);
      }

      for (int i5 = 8; i5 < 12; i5++) {
         corners[i5] = corners[i5].add(midPos);
      }

      return corners;
   }

   static Vec3d[][] buildBodyMesh(Vec3d[] points) {
      Vec3d[][] bodyMesh = new Vec3d[10][4];
      bodyMesh[0][0] = points[0];
      bodyMesh[0][1] = points[1];
      bodyMesh[0][2] = points[5];
      bodyMesh[0][3] = points[4];
      bodyMesh[1][0] = points[1];
      bodyMesh[1][1] = points[2];
      bodyMesh[1][2] = points[6];
      bodyMesh[1][3] = points[5];
      bodyMesh[2][0] = points[3];
      bodyMesh[2][1] = points[2];
      bodyMesh[2][2] = points[6];
      bodyMesh[2][3] = points[7];
      bodyMesh[3][0] = points[0];
      bodyMesh[3][1] = points[4];
      bodyMesh[3][2] = points[7];
      bodyMesh[3][3] = points[3];
      bodyMesh[4][0] = points[0];
      bodyMesh[4][1] = points[1];
      bodyMesh[4][2] = points[2];
      bodyMesh[4][3] = points[3];
      bodyMesh[5][0] = points[4];
      bodyMesh[5][1] = points[5];
      bodyMesh[5][2] = points[9];
      bodyMesh[5][3] = points[8];
      bodyMesh[6][0] = points[9];
      bodyMesh[6][1] = points[10];
      bodyMesh[6][2] = points[6];
      bodyMesh[6][3] = points[5];
      bodyMesh[7][0] = points[10];
      bodyMesh[7][1] = points[11];
      bodyMesh[7][2] = points[7];
      bodyMesh[7][3] = points[6];
      bodyMesh[8][0] = points[4];
      bodyMesh[8][1] = points[7];
      bodyMesh[8][2] = points[11];
      bodyMesh[8][3] = points[8];
      bodyMesh[9][0] = points[8];
      bodyMesh[9][1] = points[9];
      bodyMesh[9][2] = points[10];
      bodyMesh[9][3] = points[11];
      return bodyMesh;
   }

   public static void renderMesh(BufferBuilder buffer, Vec3d[][] mesh, UnknownScreen color) {
      for (Vec3d[] row : mesh) {
         for (Vec3d point : row) {
            buffer.pos(point.x, point.y, point.z)
               .tex(0.0, 0.0)
               .color(color.red, color.green, color.blue, color.alpha)
               .endVertex();
         }
      }
   }

   public static void renderGalathGeometry(Minecraft minecraft, BaseGirlEntity girl, float partialTicks) {
      EntityPlayerSP player = minecraft.player;
      if (player != null) {
         GlStateManager.translate(0.0, 0.01, 0.0);
         Entity renderEntity = ((GirlRenderer)minecraft.getRenderManager().getEntityRenderObject(girl)).getRenderEntity(girl);
         Vec3d girlPos = girl.isAnchored()
            ? girl.getTargetPosition()
            : RotationHelper.lerpVec3dDouble(new Vec3d(renderEntity.lastTickPosX, renderEntity.lastTickPosY, renderEntity.lastTickPosZ), renderEntity.getPositionVector(), partialTicks);
         Vec3d cameraPos = RotationHelper.lerpVec3dDouble(new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ), player.getPositionVector(), partialTicks);
         Vec3d offset = girlPos.subtract(cameraPos);
         offset = girl.transformRenderOffset(offset, partialTicks);
         GlStateManager.translate(offset.x, offset.y, offset.z);
      }
   }

}
