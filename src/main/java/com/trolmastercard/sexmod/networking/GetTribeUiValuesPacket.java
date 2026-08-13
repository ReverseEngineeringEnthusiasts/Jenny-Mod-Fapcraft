package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.StructureCommandScreen;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.vecmath.Vector4d;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GetTribeUiValuesPacket implements IMessage {
   boolean a = false;
   boolean b;
   List<Vector4d> c;

   public GetTribeUiValuesPacket() {
      this.b = false;
      this.c = new ArrayList<>();
   }

   public GetTribeUiValuesPacket(boolean var1, List<Vector4d> var2) {
      this.b = var1;
      this.c = var2;
   }

   static GetTribeUiValuesPacket a_clash29() {
      return new GetTribeUiValuesPacket(false, new ArrayList<>());
   }

   public void fromBytes(ByteBuf var1) {
      this.b = var1.readBoolean();
      int var2 = var1.readInt();

      for (int var3 = 0; var3 < var2; var3++) {
         this.c.add(new Vector4d(var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt()));
      }

      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.b);
      var1.writeInt(this.c.size());

      for (Vector4d var3 : this.c) {
         var1.writeInt((int)var3.getX());
         var1.writeInt((int)var3.getY());
         var1.writeInt((int)var3.getZ());
         var1.writeInt((int)var3.getW());
      }
   }


   public static class Handler implements IMessageHandler<GetTribeUiValuesPacket, IMessage> {
      public IMessage onMessage(GetTribeUiValuesPacket var1, MessageContext var2) {
         if (!var1.a) {
            System.out.println("received an invalid message @GetTribeUIValues :(");
            return null;
         } else if (var2.side.isClient()) {
            StructureCommandScreen.d = var1.b;
            KoboldEntity.aY = var1.c;
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID var1x = KoboldManager.getTribeUUID(var2.getServerHandler().player.getPersistentID());
               if (var1x == null) {
                  PacketHandler.b.sendTo(GetTribeUiValuesPacket.a_clash29(), var2.getServerHandler().player);
               } else {
                  boolean var2x = KoboldManager.c_clash86(var1x);
                  EntityPlayerMP var3 = var2.getServerHandler().player;
                  HashMap var4 = KoboldManager.getTribeSavedPositions(var1x, var3.world);
                  List var5 = KoboldManager.n_clash82(var1x);
                  ArrayList var6 = new ArrayList();
                  int var7 = KoboldManager.l_clash75(var1x).getWoolMeta();
                  HashSet var8 = new HashSet();

                  for (KoboldEntity var10 : (java.util.Collection<KoboldEntity>) (var5) ) {
                     if (!var10.isDead) {
                        UUID var11 = var10.getGirlId();
                        if (!var8.contains(var11)) {
                           if (var10.aA) {
                              var7 = EyeAndKoboldColor.safeValueOf((String)var10.getDataManager().get(AbstractNpcOnlyEntity.N)).getWoolMeta();
                           }

                           var6.add(new Vector4d(var10.posX, var10.posY, var10.posZ, var7));
                           var8.add(var11);
                        }
                     }
                  }

                  for (Entry var13 : (java.util.Set<Entry>) var4.entrySet()) {
                     if (!var8.contains(var13.getKey())) {
                        BlockPos var14 = (BlockPos)var13.getValue();
                        var6.add(new Vector4d(var14.getX(), var14.getY(), var14.getZ(), var7));
                     }
                  }

                  PacketHandler.b.sendTo(new GetTribeUiValuesPacket(var2x, var6), var3);
               }
            });
            return null;
         }
      }

   }
}
