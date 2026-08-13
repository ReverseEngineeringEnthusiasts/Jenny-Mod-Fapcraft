package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;







import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GirlCameraHelper {
   public static final float c = 1.2345679F;
   Vec3d b = null;
   Vec3d d = null;
   AbstractPlayerGirlEntity a = null;
   boolean e = false;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(Pre var1) {
      if (var1.getPartialRenderTick() != 1.2345679F) {
         AbstractPlayerGirlEntity.rebuildPlayerGirlTable();
         AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getEntityPlayer().getPersistentID());
         if (var2 != null) {
            var1.setCanceled(true);
            a(var2, var1.getEntityPlayer(), var1.getX(), var1.getY(), var1.getZ(), var1.getPartialRenderTick());
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static void a(AbstractPlayerGirlEntity var0, EntityPlayer var1, double var2, double var4, double var6, float var8) {
      Minecraft var9 = Minecraft.getMinecraft();
      var1 = var0.resolvePlayerEntity(var1);
      if (!var1.isInvisibleToPlayer(var9.player) || var0.E_clash458()) {
         RenderManager var10 = var9.getRenderManager();
         var0.rotationYaw = var1.rotationYaw;
         var0.prevRotationYawHead = var1.prevRotationYawHead;
         var0.rotationYawHead = var1.rotationYawHead;
         var0.prevRotationPitch = var1.prevRotationPitch;
         var0.rotationPitch = var1.rotationPitch;
         var0.prevRotationYaw = var1.prevRotationYaw;
         var0.prevPosX = var1.prevPosX;
         var0.prevPosY = var1.prevPosY;
         var0.prevPosZ = var1.prevPosZ;
         var0.lastTickPosX = var1.lastTickPosX;
         var0.lastTickPosY = var1.lastTickPosY;
         var0.lastTickPosZ = var1.lastTickPosZ;
         var0.renderYawOffset = var1.renderYawOffset;
         var0.prevRenderYawOffset = var1.prevRenderYawOffset;
         var0.ad = var1.isSneaking();
         var0.aj = var1.isSprinting();
         var0.ak = var1.isRiding();
         var0.af = var1.onGround;
         var0.ah = var1.getItemInUseCount() != 0;
         double var11 = var1.lastTickPosX - var1.posX;
         double var13 = var1.posZ - var1.lastTickPosZ;
         double var15 = (Math.PI / 180.0) * var1.rotationYaw;
         var0.ao = new Vector2f((float)(var11 * Math.cos(var15) + var13 * Math.sin(var15)), (float)(var11 * Math.sin(var15) + var13 * Math.cos(var15)));
         float var17 = var0.z_clash454() ? a(var0, var1) : 0.0F;
         GirlPlayerRenderer.v = true;
         var10.renderEntity(var0, var2, var4 + var17, var6, 90.0F, var8, false);
      }
   }

   static float a(AbstractPlayerGirlEntity var0, EntityPlayer var1) {
      if ((Boolean)var0.getDataManager().get(BaseGirlEntity.G)) {
         return 0.0F;
      }

      if ((var1.getHeldItemMainhand().getItem() instanceof ItemBow || var1.getHeldItemOffhand().getItem() instanceof ItemBow) && var0.ah) {
         var0.b(fp.BOW);
      }

      if (var0.getCurrentAction() == fp.BOW && !var0.ah) {
         var0.b(fp.NULL);
      }

      if (var0.getCurrentAction() == fp.BOW) {
         var0.rotationYaw = var0.rotationYawHead;
         var0.renderYawOffset = var0.rotationYawHead;
         var0.prevRenderYawOffset = var0.prevRotationYawHead;
      }

      if (var0.ak) {
         return var1.getRidingEntity() instanceof EntityBoat ? 0.4F : 0.2F;
      } else {
         return 0.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player != null) {
         if (var1.phase == Phase.END) {
            if (this.b != null) {
               var2.player.setPosition(this.b.x, this.b.y, this.b.z);
               var2.player.lastTickPosX = this.d.x;
               var2.player.lastTickPosY = this.d.y;
               var2.player.lastTickPosZ = this.d.z;
               this.b = null;
               this.d = null;
            }
         } else if (var2.gameSettings.thirdPersonView == 0) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.player.getPersistentID());
            if (var3 != null) {
               if (var3.o_clash456()) {
                  this.b = var2.player.getPositionVector();
                  this.d = new Vec3d(var2.player.lastTickPosX, var2.player.lastTickPosY, var2.player.lastTickPosZ);
                  Vec3d var4 = var3.getCachedBoneOffset("girlCam");
                  var4 = var3.b(var4, var1.renderTickTime);
                  var4 = var4.add(RotationHelper.a(this.d, this.b, var1.renderTickTime));
                  var2.player.posX = var4.x;
                  var2.player.posY = var4.y - var2.player.getEyeHeight();
                  var2.player.posZ = var4.z;
                  var2.player.lastTickPosX = var4.x;
                  var2.player.lastTickPosY = var4.y - var2.player.getEyeHeight();
                  var2.player.lastTickPosZ = var4.z;
                  fp var5 = var3.getCurrentAction();
                  float var6 = var3.getYawRotation();
                  if (!var3.a(var5, var2.player)) {
                     if (var5.flipGirlYaw) {
                        var6 += 180.0F;
                     }

                     if (var2.player.rotationPitch > var5.maxGirlPitch) {
                        var2.player.rotationPitch = var5.maxGirlPitch;
                        var2.player.prevRotationPitch = var5.maxGirlPitch;
                     }

                     if (var2.player.rotationPitch < var5.minGirlPitch) {
                        var2.player.rotationPitch = var5.minGirlPitch;
                        var2.player.prevRotationPitch = var5.minGirlPitch;
                     }

                     if (var2.player.rotationYaw > var6 + 90.0F) {
                        var2.player.rotationYaw = var6 + 90.0F;
                        var2.player.prevRotationYaw = var6 + 90.0F;
                     }

                     if (var2.player.rotationYaw < var6 - 90.0F) {
                        var2.player.rotationYaw = var6 - 90.0F;
                        var2.player.prevRotationYaw = var6 - 90.0F;
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(CameraSetup var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player != null) {
         AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.player.getPersistentID());
         if (var3 != null) {
            if (var3.F_clash231()) {
               if (var3.isAnchored()) {
                  var1.setRoll(180.0F);
                  var1.setPitch(-var1.getPitch());
                  var1.setYaw(-var1.getYaw());
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderWorldLastEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (this.b != null) {
         if (var2.gameSettings.thirdPersonView == 0) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.player.getPersistentID());
            if (var3 != null) {
               Vec3d var4 = var2.player.getPositionVector();
               Vec3d var5 = RotationHelper.a(this.d, this.b, var1.getPartialTicks());
               Vec3d var6 = var5.subtract(var4);
               a(var3, var2.player, var6.x, var6.y, var6.z, var1.getPartialTicks());
               GlStateManager.enableLighting();
               GlStateManager.enableDepth();
               GlStateManager.enableAlpha();
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void b(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.getMinecraft();
      if (var2.player != null) {
         if (var1.phase != Phase.END) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var2.player.getPersistentID());
            if (var3 == null) {
               if (this.e) {
                  this.e = false;
                  var2.player.eyeHeight = var2.player.getDefaultEyeHeight();
               }
            } else if (var3.isAnchored()) {
               if (this.e) {
                  this.e = false;
                  var2.player.eyeHeight = var2.player.getDefaultEyeHeight();
               }
            } else {
               if (this.a != var3) {
                  a(var3, var2.player, 0.0, 500.0, 0.0, var1.renderTickTime);
                  this.a = var3;
               }

               var2.player.eyeHeight = var3.getCameraBoneHeight();
               this.e = true;
            }
         }
      }
   }

}
