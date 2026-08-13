package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BiaPlayerEntity;
import com.trolmastercard.sexmod.entity.ElliePlayerEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.item.AlliesLampItem;
import com.trolmastercard.sexmod.networking.InformOfOwnershipPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;







import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class PlayerIds {
   static final UUID b = UUID.fromString("b91e6484-8911-4def-ab04-9fa3452fca5f");
   static final UUID a = UUID.fromString("adf20149-2adc-4a9d-9af5-8e9aeda019d6");

   @SubscribeEvent
   public void a(PlayerLoggedInEvent var1) {
      EntityPlayerMP var2 = var1.player.field_70170_p.func_73046_m().func_184103_al().func_177451_a(var1.player.getPersistentID());
      var2.func_82142_c(false);
      var2.func_189654_d(false);
      var2.field_70145_X = false;
      if (!var2.field_71075_bZ.field_75098_d && var2.field_71075_bZ.field_75100_b) {
         var2.field_71075_bZ.field_75100_b = false;
      }

      PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), var2);
      PacketHandler.b.sendTo(new InformOfOwnershipPacket(GirlSavedData.c_clash849(var2.getPersistentID())), var2);

      for (ItemStack var4 : var2.field_71071_by.field_70462_a) {
         if (var4.func_77973_b() == AlliesLampItem.b && var4.func_77942_o()) {
            var4.func_77978_p().func_186854_a("user", UUID.randomUUID());
         }
      }

      UUID var7 = KoboldManager.getTribeUUID(var2.getPersistentID());
      if (var7 != null) {
         HashSet var8 = KoboldManager.d_clash90(var7);
         PacketHandler.b.sendTo(new SendBlocksPacket(var8, true), var2);
      }

      AbstractPlayerGirlEntity.rebuildPlayerGirlTable();
      AbstractPlayerGirlEntity var9 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.player.getPersistentID());
      World var5 = FMLCommonHandler.instance().getMinecraftServerInstance().func_130014_f_();
      this.a(var5, var2, var9);
      if (var9 != null) {
         var9.setAnchored(false);
         var9.b(fp.NULL);
         ResetGirlPacket.Handler.a_clash10(var9);
      }

      UUID var6 = var1.player.getPersistentID();
      if (var6.equals(b)) {
         this.a(var5, var2, var6);
      }

      if (var6.equals(a)) {
         this.b(var5, var2, var6);
      }

      GalathEntity.c_clash678(var2);
   }

   void a(World var1, EntityPlayer var2, UUID var3) {
      BiaPlayerEntity var4 = new BiaPlayerEntity(var1, var3);
      var4.func_189654_d(true);
      var4.field_70145_X = true;
      var4.field_70159_w = 0.0;
      var4.field_70181_x = 0.0;
      var4.field_70179_y = 0.0;
      var4.func_70107_b(var2.field_70165_t, var2.field_70163_u + 69.0, var2.field_70161_v);
      var1.func_72838_d(var4);
      var4.B_clash233();
   }

   void b(World var1, EntityPlayer var2, UUID var3) {
      ElliePlayerEntity var4 = new ElliePlayerEntity(var1, var3);
      var4.func_189654_d(true);
      var4.field_70145_X = true;
      var4.field_70159_w = 0.0;
      var4.field_70181_x = 0.0;
      var4.field_70179_y = 0.0;
      var4.func_70107_b(var2.field_70165_t, var2.field_70163_u + 69.0, var2.field_70161_v);
      var1.func_72838_d(var4);
      var4.B_clash233();
   }

   void a(World var1, EntityPlayer var2, AbstractPlayerGirlEntity var3) {
      Predicate var4 = var0 -> true;

      for (AbstractPlayerGirlEntity var7 : var1.func_175644_a(AbstractPlayerGirlEntity.class, var4::test)) {
         if (var7.getOwnerUserUUID().equals(var2.getPersistentID()) && (var3 == null || var7.func_145782_y() != var3.func_145782_y())) {
            var1.func_72900_e(var7);
         }
      }
   }

   @SubscribeEvent
   public void a(PlayerLoggedOutEvent var1) {
      EntityPlayer var2 = var1.player;

      try {
         for (BaseGirlEntity var4 : BaseGirlEntity.getGirlEntityList()) {
            if (var4 instanceof AbstractPlayerGirlEntity) {
               ((AbstractPlayerGirlEntity)var4).b_clash468(var2);
            }

            if (var4.getInteractionPlayerUUID() != null) {
               if (var4.getInteractionPlayerUUID().equals(var2.getPersistentID()) || var4.getInteractionPlayerUUID().equals(var2.func_110124_au())) {
                  ResetGirlPacket.Handler.a_clash10(var4);
                  var4.setAnchored(false);
                  var4.b(fp.NULL);
               }

               if (var4 instanceof AbstractPlayerGirlEntity
                  && ((AbstractPlayerGirlEntity)var4).getOwnerUserUUID().equals(var2.getPersistentID())
                  && var4.getInteractionPlayerUUID() != null) {
                  EntityPlayerMP var5 = (EntityPlayerMP)var1.player.field_70170_p.func_152378_a(var4.getInteractionPlayerUUID());
                  PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), var5);
                  ResetGirlPacket.Handler.a(var5);
                  var2.func_82142_c(false);
                  var4.setInteractionPlayerUUID(null);
               }
            }
         }
      } catch (ConcurrentModificationException var6) {
      }
   }

}
