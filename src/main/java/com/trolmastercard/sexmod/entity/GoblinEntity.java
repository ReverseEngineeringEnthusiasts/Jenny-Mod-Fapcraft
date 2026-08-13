package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GalathScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.e1;
import com.trolmastercard.sexmod.util.eh;
import com.trolmastercard.sexmod.util.g5;







import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class GoblinEntity extends AbstractNpcOnlyEntity implements IGoblin {
   public static final by ax = by.DARK_GREEN;
   public static final Vec3i ah = new Vec3i(11, 6, 11);
   public static final Vec3d aB = new Vec3d(5.0, 1.0, 9.0);
   public static final Vec3d af = new Vec3d(3.0, -1.0, 6.0);
   public static final Vec3d ao = new Vec3d(1.0, 1.0, 5.0);
   public static final Vec3d au = new Vec3d(-6.0, -1.0, 3.0);
   public static final Vec3d aM = new Vec3d(5.0, 1.0, 1.0);
   public static final Vec3d W = new Vec3d(-3.0, -1.0, -6.0);
   public static final Vec3d U = new Vec3d(9.0, 1.0, 5.0);
   public static final Vec3d as = new Vec3d(0.0, -1.0, -4.0);
   public static final Vec3d aT = new Vec3d(1.0, -1.0, -3.0);
   public static final Vec3d ap = new Vec3d(-1.0, -1.0, -3.0);
   public static final Vec3d at = new Vec3d(6.0, -1.0, -3.0);
   public static final int aj = 39;
   public static final int ae = 15;
   public static final int aE = 8400;
   static final int aH = 45;
   static final int ad = 32000;
   static final int aw = 26;
   static final int V = 205;
   static final int aL = 100;
   static final int aA = 1200;
   static final int ak = 30;
   static final int aW = 37;
   static final float aU = 2.0F;
   static final int aI = 5;
   static final int S = 100;
   static final int aq = 20;
   static final float aG = 0.825F;
   static final Vector2f aS = new Vector2f(0.5F, 0.99F);
   static final HashSet<Item> ag = new HashSet<>(
      Arrays.asList(
         Items.GOLDEN_HOE,
         Items.GOLDEN_HORSE_ARMOR,
         Items.GOLD_INGOT,
         Items.GOLDEN_APPLE,
         Items.GOLDEN_AXE,
         Items.GOLDEN_SHOVEL,
         Items.GOLDEN_PICKAXE,
         Items.GOLDEN_SWORD,
         Items.GOLDEN_CARROT,
         Items.GOLDEN_HELMET,
         Items.GOLDEN_BOOTS,
         Items.GOLDEN_CHESTPLATE,
         Items.GOLDEN_LEGGINGS,
         Items.GOLD_INGOT,
         Items.GOLD_NUGGET,
         Item.getItemFromBlock(Blocks.GOLD_BLOCK),
         Item.getItemFromBlock(Blocks.GOLD_ORE)
      )
   );
   public static final DataParameter<String> Q = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(122);
   public static final DataParameter<String> aK = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(123);
   public static final DataParameter<ItemStack> a0 = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(124);
   public static final DataParameter<Boolean> aC = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(125);
   public static final DataParameter<Boolean> aV = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(126);
   public boolean aX = false;
   public float ac = 0.0F;
   public long av = -1L;
   public Vec3d al = Vec3d.ZERO;
   List<UUID> T = new ArrayList<>();
   int aO = 31520;
   int aQ = -1;
   public int aR = -1;
   boolean aZ = false;
   BlockPos R = null;
   int Y = 0;
   int aa = 0;
   int aJ = 0;
   int an = -1;
   int am = 0;
   long ai = 0L;
   List<GoblinEntity> ab = new ArrayList<>();
   int aY = -1;
   int az = -1;
   fp aN = null;
   public float ar = 1.0F;
   int Z = -1;
   boolean aD = true;
   boolean aF = true;
   boolean X = false;
   String aP = "";
   boolean ay = false;

   public GoblinEntity(World var1) {
      super(var1);
      this.setSize(aS.x, aS.y);
   }

   public GoblinEntity(World var1, @Nonnull String var2, int var3) {
      this(var1);
      this.entityDataManager.set(aK, var2);
      this.entityDataManager.set(M, this.a_clash247(new StringBuilder(), var3));
   }

   public GoblinEntity(World var1, boolean var2, float var3, Vec3d var4) {
      this(var1);
      if (var2) {
         this.entityDataManager.set(M, this.b_clash242(new StringBuilder()));
         this.ac = var3;
         this.al = var4;
         this.aX = true;
         this.setTargetPosition(var4);
         this.setYawRotation(var3);
         this.setCurrentAction(fp.SIT);
         this.setAnchored(true);
         this.setPosition(var4.x, var4.y, var4.z);
      }
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.setOwnerUUID(null);
      this.noClip = false;
      this.setNoGravity(false);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      eh var1 = eh.values()[this.getRNG().nextInt(eh.values().length)];
      this.entityDataManager.register(K, new BlockPos(var1.a_clash565()));
      this.entityDataManager.register(N, ax.name());
      this.entityDataManager.register(Q, "");
      this.entityDataManager.register(aK, "");
      this.entityDataManager.register(a0, ItemStack.EMPTY);
      this.entityDataManager.register(aC, false);
      this.entityDataManager.register(aV, false);
   }

   @Override
   protected void a_clash222() {
      GoblinRenderer.clearBoneColors();
   }

   public void setDead() {
      super.setDead();
      this.setOwnerUUID(null);
      if (!this.world.isRemote) {
         ItemStack var1 = (ItemStack)this.entityDataManager.get(a0);
         if (var1 != ItemStack.EMPTY) {
            EntityItem var2 = new EntityItem(this.world, this.posX, this.posY, this.posZ, var1);
            this.world.spawnEntity(var2);
         }
      }
   }

   @Override
   public void doAction(String var1, UUID var2) {
      if ("take ur stuff back".equals(var1)) {
         this.setCurrentAction(fp.START_THROWING);
      }

      if ("use her".equals(var1)) {
         this.c_clash239(var2);
      }
   }

   public void c_clash239(UUID var1) {
      this.aY = 0;
      BeeScreen.enableInteraction();
      d3.setMovementLock(false);
      this.setInteractionPlayerUUID(var1);
   }

   public void b_clash240(UUID var1) {
      this.az = 0;
      BeeScreen.enableInteraction();
      d3.setMovementLock(false);
      this.setInteractionPlayerUUID(var1);
   }

   @Override
   public String getDisplayNameText() {
      return "Goblin";
   }

   public float getEyeHeight() {
      return 0.75F;
   }

   @Override
   public float i_clash226() {
      return 0.1F;
   }

   @Override
   public void setOwnerUUID(UUID var1) {
      if (var1 == null) {
         this.entityDataManager.set(Q, "");
      } else {
         this.entityDataManager.set(Q, var1.toString());
      }
   }

   @Nullable
   @Override
   public UUID getOwnerUUID() {
      String var1 = (String)this.entityDataManager.get(Q);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.entityDataManager.get(Q));
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public int c_clash56() {
      return this.aQ;
   }

   @Override
   public void b_clash63(int var1) {
      this.aQ = var1;
   }

   protected String b_clash242(StringBuilder var1) {
      appendPaddedNumber(var1, 3);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 2);
      c(var1, 7);
      c(var1, 7);
      appendPaddedNumber(var1, 5);
      appendPaddedNumber(var1, g5.values().length - 1);
      appendPaddedNumber(var1, by.values().length - 1);
      appendPaddedNumber(var1, eh.values().length - 1);
      c(var1, 1);
      return var1.toString();
   }

   @Override
   protected String a(StringBuilder var1) {
      appendPaddedNumber(var1, 3);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 8);
      appendPaddedNumber(var1, 8);
      appendPaddedNumber(var1, 5);
      appendPaddedNumber(var1, g5.values().length - 1);
      appendPaddedNumber(var1, by.values().length - 1);
      appendPaddedNumber(var1, eh.values().length - 1);
      c(var1, 0);
      return var1.toString();
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<Integer>() {
         {
            this.add(4);
            this.add(3);
            this.add(3);
            this.add(16);
            this.add(16);
            this.add(6);
            this.add(g5.values().length);
            this.add(by.values().length);
            this.add(eh.values().length);
         }
      };
   }

   @Override
   public List<Integer> getCustomPartExtraIdList() {
      return Collections.singletonList(2);
   }

   @Override
   public e1 g(int var1) {
      switch (var1) {
         case 0:
            return new e1(40, 130);
         case 1:
            return new e1(60, 130);
         case 2:
            return new e1(80, 130);
         case 3:
            return new e1(100, 130);
         case 4:
            return new e1(120, 130);
         case 5:
            return new e1(140, 130);
         case 6:
            return new e1(160, 130);
         case 7:
            return new e1(180, 130);
         case 8:
            return new e1(200, 0);
         case 9:
            return new e1(200, 130);
         default:
            return e1.a;
      }
   }

   @Override
   public void setCustomPartList(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var4 : var1) {
         c(var2, var4);
      }

      c(var2, Integer.parseInt(getModelCodeParts(this)[9]));
      this.entityDataManager.set(M, var2.toString());
      if (null instanceof ClientProxy) {
         GoblinRenderer.clearBoneColors();
      }
   }

   void i_clash246() {
      if (this.customPartsData != null) {
         StringBuilder var1 = new StringBuilder();

         for (Entry var3 : this.customPartsData) {
            int var4 = (Integer)((Entry)var3.getValue()).getValue();
            c(var1, var4);
         }

         c(var1, Integer.parseInt(getModelCodeParts(this)[9]));
         this.entityDataManager.set(M, var1.toString());
         GoblinRenderer.clearBoneColors();
      }
   }

   protected String a_clash247(StringBuilder var1, int var2) {
      appendPaddedNumber(var1, 3);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 2);
      appendPaddedNumber(var1, 7);
      appendPaddedNumber(var1, 7);
      appendPaddedNumber(var1, 5);
      appendPaddedNumber(var1, g5.values().length - 1);
      c(var1, var2);
      appendPaddedNumber(var1, eh.values().length - 1);
      c(var1, 0);
      return var1.toString();
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setString("bodyColor", (String)this.entityDataManager.get(N));
      var1.setInteger("eyeColorX", ((BlockPos)this.entityDataManager.get(K)).getX());
      var1.setInteger("eyeColorY", ((BlockPos)this.entityDataManager.get(K)).getY());
      var1.setInteger("eyeColorZ", ((BlockPos)this.entityDataManager.get(K)).getZ());
      var1.setString("model", (String)this.entityDataManager.get(M));
      var1.setString("girlID", (String)this.entityDataManager.get(GIRL_ID));
      var1.setString("queen", (String)this.entityDataManager.get(aK));
      var1.setBoolean("isQueen", this.aX);
      var1.setBoolean("isTamed", (Boolean)this.entityDataManager.get(aC));
      var1.setInteger("robTicks", this.aO);
      if (this.aX) {
         var1.setBoolean("preggo", (Boolean)this.entityDataManager.get(aV));
         var1.setFloat("throneRot", this.ac);
         var1.setDouble("thronePosX", this.al.x);
         var1.setDouble("thronePosY", this.al.y);
         var1.setDouble("thronePosZ", this.al.z);
         var1.setLong("impregnationTick", this.av);

         for (int var2 = 0; var2 < this.T.size(); var2++) {
            var1.setString("guard" + var2, this.T.get(var2).toString());
         }
      }
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.aX = var1.getBoolean("isQueen");
      this.entityDataManager.set(M, var1.getString("model"));
      this.entityDataManager.set(N, var1.getString("bodyColor"));
      String[] var2 = getModelCodeParts(this);
      if (Integer.parseInt(var2[3]) > 7 || Integer.parseInt(var2[4]) > 7) {
         this.entityDataManager.set(M, this.a_clash247(new StringBuilder(), this.k_clash270()));
         Main.LOGGER.log(Level.INFO, "updated an old Goblin");
      }

      this.entityDataManager.set(K, new BlockPos(var1.getInteger("eyeColorX"), var1.getInteger("eyeColorY"), var1.getInteger("eyeColorZ")));
      this.entityDataManager.set(GIRL_ID, var1.getString("girlID"));
      this.entityDataManager.set(aK, var1.getString("queen"));
      this.entityDataManager.set(aC, var1.getBoolean("isTamed"));
      this.aO = var1.getInteger("robTicks");
      if (this.aX) {
         this.ac = var1.getFloat("throneRot");
         this.al = new Vec3d(var1.getDouble("thronePosX"), var1.getDouble("thronePosY"), var1.getDouble("thronePosZ"));

         for (int var3 = 0; !"".equals(var1.getString("guard" + var3)); var3++) {
            this.T.add(UUID.fromString(var1.getString("guard" + var3)));
         }

         this.entityDataManager.set(aV, var1.getBoolean("preggo"));
         this.av = var1.getLong("impregnationTick");
      }
   }

   protected boolean processInteract(EntityPlayer var1, EnumHand var2) {
      if (this.world.isRemote) {
         return true;
      }

      if (this.aX) {
         return true;
      }

      if (this.getCurrentAction() == fp.RUN) {
         if (this.getDistance(var1) > 3.5) {
            var1.sendStatusMessage(new TextComponentString("get a bit closer..."), true);
         } else {
            this.setTargetPosition(var1.getPositionVector());
            this.setYawRotation(var1.rotationYaw);
            this.setCurrentAction(fp.CATCH);
            this.entityDataManager.set(GIRL_HAND_STATES, "bj");
            this.setOwnerUUID(var1.getPersistentID());
            this.setInteractionPlayerUUID(var1.getPersistentID());
            this.getNavigator().clearPath();
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         }

         return true;
      } else {
         if (d_clash248(var1.getPersistentID())) {
            var1.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
         } else {
            this.setOwnerUUID(var1.getPersistentID());
            this.setCurrentAction(fp.PICK_UP);
            this.aQ = 45;
            this.setAnchored(false);
            this.entityDataManager.set(aC, true);
            this.getNavigator().clearPath();
         }

         return true;
      }
   }

   public static boolean d_clash248(UUID var0) {
      if (var0 == null) {
         return false;
      }

      try {
         for (BaseGirlEntity var2 : BaseGirlEntity.getGirlEntityList()) {
            if (var2 instanceof IGoblin && !var2.world.isRemote && !var2.isDead) {
               UUID var3 = ((IGoblin)var2).getOwnerUUID();
               if (var0.equals(var3)) {
                  return true;
               }
            }
         }
      } catch (ConcurrentModificationException var4) {
      }

      return false;
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 2.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.watchClosestGirlGoal);
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.f_clash262();
      a_clash282(this);
      this.m_clash272();
      this.B_clash269();
      this.J_clash267();
      this.E_clash260();
      this.t_clash258();
      this.w_clash257();
      this.b_clash256();
      this.d_clash255();
      this.h_clash254();
      this.o_clash253();
      this.u_clash252();
      this.n_clash250();
   }

   public boolean canBeCollidedWith() {
      fp var1 = this.getCurrentAction();
      if (var1 == fp.THROWN) {
         return false;
      } else if (var1 == fp.RUN) {
         return super.canBeCollidedWith();
      } else if (var1 == fp.AWAIT_PICK_UP) {
         return super.canBeCollidedWith();
      } else if (this.getOwnerUUID() != null) {
         return false;
      } else {
         return var1 != fp.NULL ? false : super.canBeCollidedWith();
      }
   }

   void b_clash249(EntityPlayer var1) {
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getPersistentID());
      Vec3d var3 = new Vec3d(var1.posX, var1.posY + (var2 == null ? var1.eyeHeight : var2.getEyeHeight()), var1.posZ);
      Vec3d var4 = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
      double var5 = var4.distanceTo(var3);
      double var7 = var3.y - var4.y;
      this.rotationPitch = (float)(-(Math.sin(var7 / var5) * (180.0 / Math.PI)));
   }

   void n_clash250() {
      if ((Boolean)this.entityDataManager.get(aC)) {
         if (this.getInteractionPlayerUUID() == null) {
            if (this.getCurrentAction() == fp.NULL) {
               EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 15.0);
               if (var1 != null && var1.getDistance(this) < 2.0F) {
                  this.b_clash249(var1);
                  this.getNavigator().clearPath();
               } else {
                  if (this.R == null
                     || this.getDistance(this.R.getX(), this.R.getY(), this.R.getZ()) > this.l_clash251()
                     || this.Y > 100) {
                     int var2 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
                     int var3 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(5);
                     int var4 = cj.a(this.world, this.getPosition().getX() + var2, this.getPosition().getZ() + var3);
                     this.R = new BlockPos(this.getPosition().getX() + var2, var4, this.getPosition().getZ() + var3);
                     this.Y = 0;
                  }

                  if (Math.sqrt(this.R.distanceSq(this.getPosition())) > 2.0) {
                     this.getNavigator().tryMoveToXYZ(this.R.getX(), this.R.getY(), this.R.getZ(), 0.3F);
                     this.tickPathVelocity();
                  } else {
                     this.Y++;
                  }
               }
            }
         }
      }
   }

   double l_clash251() {
      return Math.sqrt(800.0);
   }

   void u_clash252() {
      if (this.getCurrentAction() == fp.STAND_UP) {
         if (++this.aa >= 37) {
            this.aa = 0;
            this.setCurrentAction(fp.NULL);
         }
      }
   }

   @Override
   public void a_clash59(int var1) {
      this.aJ = var1;
   }

   @Override
   public int d_clash60() {
      return this.aJ;
   }

   void o_clash253() {
      if (this.getCurrentAction() == fp.THROWN) {
         if (this.onGround) {
            int var1 = this.d_clash60() + 1;
            this.a_clash59(var1);
            if (var1 >= 30) {
               this.a_clash59(0);
               this.setCurrentAction(fp.STAND_UP);
            }
         }
      }
   }

   void h_clash254() {
      if (this.aX) {
         if ((Boolean)this.entityDataManager.get(aV)) {
            if (this.av + 8400L < this.world.getTotalWorldTime()) {
               this.entityDataManager.set(aV, false);
            }
         }
      }
   }

   void d_clash255() {
      if (this.aX) {
         if (!this.ab.isEmpty()) {
            boolean var1 = false;

            for (GoblinEntity var3 : this.ab) {
               if ((Boolean)var3.getDataManager().get(aC)) {
                  var1 = true;
               }
            }

            if (var1) {
               this.sendGirlChatMessage("Farewell my knight. You are welcome once I am breedable again.");

               for (GoblinEntity var5 : this.ab) {
                  if (!(Boolean)var5.getDataManager().get(aC)) {
                     var5.setCurrentAction(fp.VANISH);
                  }
               }

               this.ab.clear();
               this.setInteractionPlayerUUID(null);
            }
         }
      }
   }

   void b_clash256() {
      if (this.aX) {
         if (this.Z != -1) {
            if (++this.Z >= 100) {
               this.Z = -1;
               UUID var1 = this.getInteractionPlayerUUID();
               if (var1 == null) {
                  this.resetCameraAndPhysics();
               } else {
                  EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
                  if (var2 == null) {
                     this.resetCameraAndPhysics();
                  } else {
                     this.setInteractionPlayerUUID(null);

                     for (GoblinEntity var4 : this.ab) {
                        var4.setInteractionPlayerUUID(null);
                     }

                     List var10 = this.I_clash261();
                     float var11 = this.ac + 180.0F;
                     Vec3d var5 = this.al.add(b(aT, var11));
                     Vec3d var6 = this.al.add(b(ap, var11));
                     Vec3d var7 = this.al.add(b(as, var11));
                     GoblinEntity var8 = (GoblinEntity)var10.get(0);
                     GoblinEntity var9 = (GoblinEntity)var10.get(1);
                     var8.setTargetPosition(var5);
                     var9.setTargetPosition(var6);
                     var8.setYawRotation(0.0F);
                     var9.setYawRotation(0.0F);
                     var8.setAnchored(true);
                     var9.setAnchored(true);
                     var8.setCurrentAction(fp.AWAIT_PICK_UP);
                     var9.setCurrentAction(fp.AWAIT_PICK_UP);
                     var8.setNoGravity(false);
                     var9.setNoGravity(false);
                     var2.setNoGravity(false);
                     var8.noClip = false;
                     var9.noClip = false;
                     var2.noClip = false;
                     var2.rotationYaw = var11;
                     var2.rotationPitch = 30.0F;
                     var2.setPositionAndUpdate(var7.x, var7.y, var7.z);
                     PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var2);
                     this.sendGirlChatMessage(
                        "Thanks to you, my clan is soon going to get a few new members! In return I will bear of one of my guards to serve as your personal Onahole. Choose wisely~"
                     );
                  }
               }
            }
         }
      }
   }

   void w_clash257() {
      if (this.aX) {
         if (this.an != -1) {
            if (++this.an >= 205) {
               this.an = -1;
               UUID var1 = this.getInteractionPlayerUUID();
               if (var1 != null) {
                  EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
                  if (var2 != null) {
                     Vec3d var3 = b(new Vec3d(0.0, 0.15625 - var2.getEyeHeight(), -0.8859375), this.ac - 180.0F);
                     var3 = var3.add(this.getTargetPosition());
                     var2.setPositionAndUpdate(var3.x, var3.y, var3.z);
                  }
               }
            }
         }
      }
   }

   public static Vec3d b(Vec3d var0, float var1) {
      return a(var0, 0.0F, var1);
   }

   public static Vec3d a(Vec3d var0, float var1, float var2) {
      Vec3d var3 = new Vec3d(
         var0.x,
         var0.y * Math.cos(var1 * (Math.PI / 180.0)) - var0.z * Math.sin(var1 * (Math.PI / 180.0)),
         var0.y * Math.sin(var1 * (Math.PI / 180.0)) + var0.z * Math.cos(var1 * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((var2 + 90.0F) * (Math.PI / 180.0)) * var3.x - Math.sin(var2 * (Math.PI / 180.0)) * var3.z,
         var3.y,
         Math.cos((var2 + 90.0F) * (Math.PI / 180.0)) * var3.x + Math.cos(var2 * (Math.PI / 180.0)) * var3.z
      );
   }

   void t_clash258() {
      if (this.aX) {
         if (this.getCurrentAction() == fp.JUMP_0) {
            if (++this.am >= 26) {
               this.am = 0;
               Vec3d var1;
               switch ((int)this.ac) {
                  case -90:
                     var1 = this.al.add(at);
                     break;
                  case 90:
                     var1 = this.al.add(au);
                     break;
                  case 180:
                     var1 = this.al.add(W);
                     break;
                  default:
                     var1 = this.al.add(af);
               }

               UUID var2 = this.getInteractionPlayerUUID();
               if (var2 != null) {
                  EntityPlayer var3 = this.world.getPlayerEntityByUUID(var2);
                  if (var3 != null) {
                     this.setTargetPosition(var1);
                     this.setYawRotation(this.ac);
                     this.setCurrentAction(fp.BREEDING_INTRO_0);
                     this.noClip = true;
                     this.setNoGravity(true);
                     Vec3d var4 = b(new Vec3d(0.0, 0.44375 - var3.eyeHeight, -0.7875), this.ac - 180.0F);
                     var3.noClip = true;
                     var3.setNoGravity(true);
                     var3.setPositionAndUpdate(
                        var4.x + var1.x, var4.y + var1.y, var4.z + var1.z
                     );
                     List var5 = this.I_clash261();
                     if (var5.size() >= 1) {
                        GoblinEntity var6 = (GoblinEntity)var5.get(0);
                        var6.setTargetPosition(var1);
                        var6.setYawRotation(this.ac);
                        var6.setCurrentAction(fp.BREEDING_INTRO_1);
                        var6.noClip = true;
                        var6.setNoGravity(true);
                     }

                     if (var5.size() >= 2) {
                        GoblinEntity var7 = (GoblinEntity)var5.get(1);
                        var7.setTargetPosition(var1);
                        var7.setYawRotation(this.ac);
                        var7.setCurrentAction(fp.BREEDING_INTRO_2);
                        var7.noClip = true;
                        var7.setNoGravity(true);
                     }

                     this.an = 0;
                  }
               }
            }
         }
      }
   }

   AxisAlignedBB a_clash259(Vec3d var1, Vec3d var2) {
      return new AxisAlignedBB(var1.x, var1.y, var1.z, var2.x, var2.y, var2.z);
   }

   void E_clash260() {
      if (this.aX) {
         if (this.getInteractionPlayerUUID() == null) {
            Vec3d var1 = null;
            switch ((int)this.ac) {
               case -90:
                  var1 = ao;
                  break;
               case 0:
                  var1 = aM;
                  break;
               case 90:
                  var1 = U;
                  break;
               case 180:
                  var1 = aB;
            }

            if (var1 != null) {
               Vec3d var2 = this.al.subtract(0.5, 0.0, 0.5).subtract(var1);
               AxisAlignedBB var3 = this.a_clash259(var2, var2.add(ah.getX(), ah.getY(), ah.getZ()));
               List var4 = this.world.getEntitiesWithinAABB(EntityPlayer.class, var3);
               if (!var4.isEmpty()) {
                  EntityPlayer var5 = (EntityPlayer)var4.get(0);
                  if (var5.onGround) {
                     if ((Boolean)this.entityDataManager.get(aV)) {
                        if (this.ai + 1200L < this.world.getTotalWorldTime()) {
                           var5.sendStatusMessage(new TextComponentString("The Queen is still pregnant - so no breeding for you uwu"), true);
                           this.ai = this.world.getTotalWorldTime();
                        }
                     } else {
                        UUID var6 = var5.getPersistentID();
                        Vec3d var7 = var5.getPositionVector();
                        float var8 = var5.rotationYaw + 180.0F;
                        PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var5);
                        this.setInteractionPlayerUUID(var6);
                        this.setCurrentAction(fp.JUMP_0);
                        this.setTargetPosition(var7);
                        this.setYawRotation(var8);
                        this.setAnchored(true);
                        List var9 = this.I_clash261();
                        if (var9.size() > 0) {
                           GoblinEntity var10 = (GoblinEntity)var9.get(0);
                           var10.setInteractionPlayerUUID(var6);
                           var10.setCurrentAction(fp.JUMP_1);
                           var10.setTargetPosition(var7);
                           var10.setYawRotation(var8);
                           var10.setAnchored(true);
                           if (var9.size() > 1) {
                              GoblinEntity var11 = (GoblinEntity)var9.get(1);
                              var11.setInteractionPlayerUUID(var6);
                              var11.setCurrentAction(fp.JUMP_2);
                              var11.setTargetPosition(var7);
                              var11.setYawRotation(var8);
                              var11.setAnchored(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   List<GoblinEntity> I_clash261() {
      if (this.ab.size() > 1) {
         return this.ab;
      }

      for (GoblinEntity var2 : this.ab) {
         this.world.removeEntity(var2);
      }

      this.ab.clear();
      GoblinEntity var3 = new GoblinEntity(this.world, this.getGirlId().toString(), this.k_clash270());
      var3.setPosition(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(var3);
      this.ab.add(var3);
      GoblinEntity var4 = new GoblinEntity(this.world, this.getGirlId().toString(), this.k_clash270());
      var4.setPosition(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(var4);
      this.ab.add(var4);
      return this.ab;
   }

   void f_clash262() {
      if (!this.aZ) {
         this.noClip = false;
         this.setNoGravity(false);
         if (!this.aX && !(Boolean)this.entityDataManager.get(aC) && !((String)this.entityDataManager.get(aK)).equals("") && this.getCurrentAction() == fp.NULL) {
            this.world.removeEntity(this);
         }

         this.aZ = true;
      }
   }

   void e_clash263() {
      GoblinEntity var1 = this;
      int var2 = var1.a_clash58();
      if (var2 != -1) {
         var1.c_clash57(++var2);
         if (var2 == 15) {
            Vec3d var3 = b_clash264(this);
            float var4 = d_clash266(this);
            float var5 = c_clash265(this);
            this.setPositionAndUpdate(var3.x, var3.y, var3.z);
            Vec3d var6 = a(new Vec3d(0.0, 0.0, 1.5), var4, var5);
            this.motionX = var6.x;
            this.motionY = var6.y;
            this.motionZ = var6.z;
            if (!this.world.isRemote) {
               this.setYawRotation(var5);
            }
         }

         this.noClip = false;
         this.setNoGravity(false);
         if (var2 == 39) {
            this.c_clash57(-1);
            this.setCurrentAction(fp.THROWN);
            this.setInteractionPlayerUUID(null);
            this.setOwnerUUID(null);
         }
      }
   }

   public static Vec3d b_clash264(BaseGirlEntity var0) {
      IGoblin var1 = (IGoblin)var0;
      UUID var2 = var1.getOwnerUUID();
      if (var2 == null) {
         return var0.getPositionVector();
      }

      EntityPlayer var3 = var0.world.getPlayerEntityByUUID(var2);
      return var3 == null
         ? var0.getPositionVector()
         : var3.getPositionVector().add(0.0, var3.getEyeHeight(), 0.0).add(a(new Vec3d(0.4F, 0.0, 0.0), d_clash266(var0), c_clash265(var0)));
   }

   public static float c_clash265(BaseGirlEntity var0) {
      IGoblin var1 = (IGoblin)var0;
      UUID var2 = var1.getOwnerUUID();
      if (var2 == null) {
         return 0.0F;
      }

      EntityPlayer var3 = var0.world.getPlayerEntityByUUID(var2);
      return var3 == null ? 0.0F : var3.rotationYawHead;
   }

   public static float d_clash266(BaseGirlEntity var0) {
      IGoblin var1 = (IGoblin)var0;
      UUID var2 = var1.getOwnerUUID();
      if (var2 == null) {
         return 0.0F;
      }

      EntityPlayer var3 = var0.world.getPlayerEntityByUUID(var2);
      return var3 == null ? 0.0F : var3.rotationPitch;
   }

   void J_clash267() {
      if (this.onGround) {
         if (this.getCurrentAction() == fp.RUN) {
            EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 100.0);
            if (var1 != null) {
               double var2 = 20.0;

               while (!(var2 <= 0.0)) {
                  Vec3d var5 = this.getPositionVector().subtract(var1.getPositionVector());
                  Vec3d var6 = new Vec3d(Math.abs(var5.x), Math.abs(var5.y), Math.abs(var5.z));
                  double var7 = var6.x / (var6.x + var6.z);
                  double var9 = var6.z / (var6.x + var6.z);
                  Vec3d var11 = this.getPositionVector()
                     .add(new Vec3d((var5.x > 0.0 ? 1 : -1) * var7 * var2, 0.0, (var5.z > 0.0 ? 1 : -1) * var9 * var2));
                  PathNavigate var12 = this.getNavigator();
                  var12.clearPath();
                  boolean var4 = var12.tryMoveToXYZ(var11.x, var11.y, var11.z, 0.825F);
                  var2--;
                  if (var4) {
                     return;
                  }
               }
            }
         }
      }
   }

   protected void jump() {
      if (this.getCurrentAction() != fp.RUN || this.j_clash268()) {
         super.jump();
      }
   }

   boolean j_clash268() {
      PathNavigate var1 = this.getNavigator();
      Path var2 = var1.getPath();
      if (var2 == null) {
         return true;
      } else {
         int var3 = var2.getCurrentPathIndex();
         int var4 = var2.getCurrentPathLength();
         if (var4 != var3 && var4 - 1 != var3) {
            PathPoint var5 = var2.getPathPointFromIndex(var3);
            PathPoint var6 = var2.getPathPointFromIndex(var3 + 1);
            return var6.y - var5.y == 1;
         } else {
            return true;
         }
      }
   }

   void B_clash269() {
      if (this.aX) {
         if (!(Boolean)this.entityDataManager.get(aC)) {
            if (!(Boolean)this.entityDataManager.get(aV)) {
               if (this.getCurrentAction() == fp.SIT) {
                  if (++this.aO >= 32000) {
                     EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 3000.0);
                     if (var1 != null) {
                        if (var1.onGround) {
                           if (!var1.isAirBorne) {
                              Integer var2 = this.c_clash271(var1);
                              if (var2 != null) {
                                 Vec3d var3 = var1.getPositionVector();
                                 Vec3d var4 = this.getPositionVector();
                                 Vec3d var5 = var3.subtract(var4);
                                 double var6 = Math.sqrt(var5.x * var5.x + var5.z * var5.z);
                                 if (!(var6 > 100.0)) {
                                    ItemStack var8 = var1.inventory.getStackInSlot(var2).copy();
                                    GoblinEntity var9 = new GoblinEntity(this.world, this.getGirlId().toString(), this.k_clash270());
                                    Vec3d var10 = b(new Vec3d(0.0, 0.0, -0.2F), var1.rotationYawHead);
                                    var9.setPosition(var1.posX + var10.x, var1.posY, var1.posZ + var10.z);
                                    var9.setCurrentAction(fp.RUN);
                                    this.world.spawnEntity(var9);
                                    var9.entityDataManager.set(a0, var8);
                                    var1.sendMessage(
                                       new TextComponentString(String.format("<%s> I got your %s hehe~", var9.getDisplayNameText(), var8.getDisplayName()))
                                    );
                                    var1.inventory.removeStackFromSlot(var2);
                                    this.aO = 0;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   int k_clash270() {
      return Integer.parseInt(getModelCodeParts(this)[7]);
   }

   @Nullable
   Integer c_clash271(EntityPlayer var1) {
      NonNullList var2 = var1.inventory.mainInventory;
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var2.size(); var4++) {
         ItemStack var5 = (ItemStack)var2.get(var4);
         if (var5 != ItemStack.EMPTY && ag.contains(var5.getItem())) {
            var3.add(var4);
         }
      }

      return var3.isEmpty() ? null : (Integer)var3.get(this.getRNG().nextInt(var3.size()));
   }

   void m_clash272() {
      if (this.aX) {
         if (this.getInteractionPlayerUUID() == null) {
            this.setTargetPosition(this.al);
            this.setYawRotation(this.ac);
            this.setAnchored(true);
            this.setNoGravity(true);
            this.setCurrentAction(fp.SIT);
         }
      }
   }

   @Override
   public void onUpdate() {
      this.i_clash246();
      e_clash273(this);
      this.e_clash263();
      if (this.getOwnerUUID() != null) {
         this.inPortal = false;
      }

      super.onUpdate();
      this.y_clash284();
      this.H_clash275();
      this.F_clash274();
      if (this.world.isRemote) {
         this.v_clash276();
         this.A_clash277();
         if (this.getOwnerUUID() != null) {
            this.noClip = true;
         }
      }
   }

   @Override
   public fp b_clash62() {
      return this.aN;
   }

   @Override
   public void a_clash61(fp var1) {
      this.aN = var1;
   }

   @Override
   public void c_clash57(int var1) {
      this.aR = var1;
   }

   @Override
   public int a_clash58() {
      return this.aR;
   }

   public static void e_clash273(BaseGirlEntity var0) {
      fp var1 = var0.getCurrentAction();
      IGoblin var2 = (IGoblin)var0;
      if (var2.b_clash62() != fp.START_THROWING && var1 == fp.START_THROWING) {
         var2.c_clash57(0);
      }

      var2.a_clash61(var1);
   }

   public void setFire(int var1) {
      if (this.getOwnerUUID() == null) {
         super.setFire(var1);
      }
   }

   void F_clash274() {
      if (this.getCurrentAction() == fp.VANISH) {
         this.ar -= 0.05F;
         if (!(this.ar > 0.0F)) {
            this.world.removeEntity(this);
         }
      }
   }

   void H_clash275() {
      if (!(Boolean)this.entityDataManager.get(aC)) {
         if (this.getCurrentAction() == fp.THROWN) {
            if (this.onGround || this.isInWater()) {
               this.ar = (float)(this.ar - 0.05);
               if (!(this.ar > 0.0F)) {
                  if (!this.world.isRemote) {
                     this.setCurrentAction(fp.NULL);
                     this.setInteractionPlayerUUID(null);
                     this.setOwnerUUID(null);
                     this.world.removeEntity(this);
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void v_clash276() {
      if (this.aY != -1) {
         if (++this.aY == 15) {
            this.aY = -1;
            this.setCurrentAction(fp.PAIZURI_START);
            Minecraft.getMinecraft().player.closeScreen();
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void A_clash277() {
      if (this.az != -1) {
         if (++this.az == 15) {
            this.az = -1;
            this.setCurrentAction(fp.NELSON_INTRO);
            Minecraft var1 = Minecraft.getMinecraft();
            var1.player.closeScreen();
            var1.gameSettings.thirdPersonView = 2;
         }
      }
   }

   @Override
   public void setCurrentAction(fp action) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.PAIZURI_CUM || action != fp.PAIZURI_SLOW && action != fp.PAIZURI_FAST) {
         if (var2 != fp.NELSON_CUM || action != fp.NELSON_SLOW && action != fp.NELSON_FAST) {
            if (var2 != fp.BREEDING_CUM_0 || action != fp.BREEDING_SLOW_0 && action != fp.BREEDING_FAST_0) {
               if (action == fp.START_THROWING && !this.world.isRemote) {
                  this.setInteractionPlayerUUID(this.getOwnerUUID());
                  this.L_clash281();
               }

               if (action == fp.PAIZURI_START && !this.world.isRemote) {
                  this.z_clash280();
               }

               if (action == fp.NELSON_INTRO && !this.world.isRemote) {
                  this.q_clash279();
               }

               if (this.getCurrentAction() == fp.PAIZURI_CUM && action == fp.NULL && !this.world.isRemote) {
                  this.D_clash278();
               }

               if (action == fp.BREEDING_CUM_0) {
                  this.entityDataManager.set(aV, true);
                  this.av = this.world.getTotalWorldTime();
                  this.ai = this.world.getTotalWorldTime();
               }

               if (action == fp.BREEDING_CUM_0) {
                  this.Z = 0;
               }

               if (action == fp.NELSON_CUM) {
                  this.entityDataManager.set(aV, true);
               }

               if (var2 == fp.NELSON_CUM && action != fp.NELSON_CUM) {
                  this.entityDataManager.set(aV, false);
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   void D_clash278() {
      EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (var1 != null) {
         ResetGirlPacket.Handler.a((EntityPlayerMP)var1);
      }

      this.setInteractionPlayerUUID(null);
      this.setAnchored(false);
      this.noClip = false;
      this.setNoGravity(false);
      this.entityDataManager.set(a0, ItemStack.EMPTY);
      if (!(Boolean)this.entityDataManager.get(aC)) {
         this.setPositionAndUpdate(this.homePos.x, this.homePos.y, this.homePos.z);
         this.world.removeEntity(this);
      }
   }

   void q_clash279() {
      EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (var1 != null) {
         this.setOwnerUUID(null);
         this.setTargetPosition(var1.getPositionVector());
         this.setYawRotation(var1.rotationYaw);
         this.setAnchored(true);
         this.noClip = true;
         this.setNoGravity(true);
         var1.setNoGravity(true);
         var1.noClip = true;
         this.setInteractionPlayerUUID(var1.getPersistentID());
      }
   }

   void z_clash280() {
      EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (var1 != null) {
         this.setOwnerUUID(null);
         this.setTargetPosition(var1.getPositionVector());
         this.setYawRotation(var1.rotationYaw + 180.0F);
         this.setAnchored(true);
         this.noClip = true;
         this.setNoGravity(true);
         var1.setNoGravity(true);
         var1.noClip = true;
         this.setInteractionPlayerUUID(var1.getPersistentID());
         var1.setPositionAndUpdate(var1.posX, var1.posY - 0.5, var1.posZ);
         var1.rotationPitch = 70.0F;
         var1.prevRotationPitch = 70.0F;
      }
   }

   void L_clash281() {
      ItemStack var1 = (ItemStack)this.entityDataManager.get(a0);
      if (var1 != ItemStack.EMPTY) {
         EntityPlayer var2 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (var2 != null) {
            var2.inventory.addItemStackToInventory(var1.copy());
            this.entityDataManager.set(a0, ItemStack.EMPTY);
         }
      }
   }

   public static void a_clash282(BaseGirlEntity var0) {
      if (var0.getCurrentAction() == fp.PICK_UP) {
         IGoblin var1 = (IGoblin)var0;
         UUID var2 = var1.getOwnerUUID();
         if (var2 == null) {
            var1.b_clash63(-1);
            var0.setCurrentAction(fp.NULL);
            var1.setOwnerUUID(null);
         } else {
            EntityPlayer var3 = var0.world.getPlayerEntityByUUID(var2);
            if (var3 == null) {
               var1.b_clash63(-1);
               var0.setCurrentAction(fp.NULL);
               var1.setOwnerUUID(null);
            } else {
               var0.setPosition(var3.posX, var3.posY, var3.posZ);
               if (var0.getPositionVector().distanceTo(var3.getPositionVector()) > 10.0) {
                  var1.b_clash63(-1);
                  var0.setCurrentAction(fp.NULL);
                  var1.setOwnerUUID(null);
               } else {
                  int var4 = var1.c_clash56() - 1;
                  var1.b_clash63(var4);
                  if (var4 == 0) {
                     var0.setCurrentAction(fp.SHOULDER_IDLE);
                     var0.noClip = true;
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean shouldRenderNameTag() {
      if (this.getCurrentAction() != fp.NULL) {
         return false;
      } else if (this.getOwnerUUID() != null) {
         return false;
      } else {
         return !this.entityDataManager.get(aC) && !Minecraft.getMinecraft().player.canEntityBeSeen(this) ? false : this.getOwnerUUID() == null;
      }
   }

   void y_clash284() {
      if (this.getCurrentAction() == fp.SHOULDER_IDLE) {
         UUID var1 = this.getOwnerUUID();
         if (var1 != null) {
            EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
            if (var2 != null) {
               this.setPosition(var2.posX, var2.posY, var2.posZ);
               this.noClip = true;
               this.setNoGravity(true);
            }
         }
      }
   }

   @Override
   protected fp getNextAction(fp var1) {
      switch (var1) {
         case PAIZURI_IDLE:
         case PAIZURI_SLOW:
            return fp.PAIZURI_FAST;
         case BREEDING_SLOW_0:
            return fp.BREEDING_FAST_0;
         case BREEDING_SLOW_2:
            return fp.BREEDING_FAST_2;
         case NELSON_SLOW:
            return fp.NELSON_FAST;
         default:
            return null;
      }
   }

   @Override
   protected fp getCumAction(fp var1) {
      switch (var1) {
         case PAIZURI_SLOW:
         case PAIZURI_FAST:
         case PAIZURI_FAST_CONTINUES:
            return fp.PAIZURI_CUM;
         case BREEDING_SLOW_0:
         case BREEDING_FAST_0:
            for (GoblinEntity var3 : this.ab) {
               var3.getCumAction(var1);
            }

            return fp.BREEDING_CUM_0;
         case BREEDING_SLOW_2:
         case BREEDING_FAST_2:
            return fp.BREEDING_CUM_2;
         case NELSON_SLOW:
         case NELSON_FAST:
            return fp.NELSON_CUM;
         case BREEDING_1:
            return fp.BREEDING_CUM_1;
         default:
            return null;
      }
   }

   public boolean C_clash285() {
      Block var1 = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !var1.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   public void fall(float var1, float var2) {
      fp var3 = this.getCurrentAction();
      if (var3 != fp.THROWN && var3 != fp.START_THROWING) {
         super.fall(var1, var2);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != fp.NULL) {
               this.createAnimation("animation.goblin.null", true, var1);
            } else {
               this.createAnimation("animation.goblin.blink", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != fp.NULL) {
               this.createAnimation("animation.goblin.null", true, var1);
            } else {
               double var4 = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (!(Boolean)this.entityDataManager.get(IS_ANCHORED) && var4 > 0.0) {
                  if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                     if (var4 > 0.2F) {
                        this.createAnimation("animation.goblin.walk", true, var1);
                     } else {
                        this.createAnimation("animation.goblin.walk", true, var1);
                     }

                     this.rotationYaw = this.rotationYawHead;
                  } else {
                     this.createAnimation("animation.goblin.fly", true, var1);
                  }
               } else {
                  this.createAnimation("animation.goblin.idle", true, var1);
               }
            }
            break;
         case "action":
            Minecraft var6 = Minecraft.getMinecraft();
            String var7 = var6.player.getPersistentID().equals(this.getOwnerUUID()) && var6.gameSettings.thirdPersonView == 0 ? "1" : "3";
            switch (this.getCurrentAction()) {
               case PAIZURI_IDLE:
                  this.createAnimation("animation.goblin.paizuri_idle", true, var1);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.goblin.paizuri_slow" + this.aP, true, var1);
                  break;
               case BREEDING_SLOW_0:
                  this.createAnimation("animation.goblin.breeding_slow_1" + (this.aD ? "l" : "r"), true, var1);
                  break;
               case BREEDING_SLOW_2:
                  this.createAnimation("animation.goblin.breeding_slow_3", true, var1);
                  break;
               case NELSON_SLOW:
                  this.createAnimation("animation.goblin.nelson_slow" + (this.aF ? "" : "2"), true, var1);
                  break;
               case PAIZURI_FAST:
                  this.createAnimation("animation.goblin.paizuri_fast", true, var1);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  this.createAnimation("animation.goblin.paizuri_fast_countinues", true, var1);
                  break;
               case BREEDING_1:
                  this.createAnimation("animation.goblin.breeding_2", true, var1);
                  break;
               case BREEDING_FAST_2:
                  this.createAnimation("animation.goblin.breeding_fast_3", true, var1);
                  break;
               case NELSON_FAST:
                  this.createAnimation("animation.goblin.nelson_fast" + (this.X ? "c" : "s"), true, var1);
                  break;
               case BREEDING_FAST_0:
                  this.createAnimation("animation.goblin.breeding_fast_1" + (this.ay ? "c" : "s"), true, var1);
                  break;
               case NULL:
                  this.createAnimation("animation.goblin.null", true, var1);
                  break;
               case SHOULDER_IDLE:
                  this.createAnimation("animation.goblin.shoulder_idle", true, var1);
                  break;
               case PICK_UP:
                  this.createAnimation(String.format("animation.goblin.pick_up_%sperson", var7), true, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.goblin.sit", true, var1);
                  break;
               case RUN:
                  if (this.onGround) {
                     this.createAnimation("animation.goblin.running", true, var1);
                  } else {
                     this.createAnimation("animation.goblin.fly", true, var1);
                  }
                  break;
               case CATCH:
                  this.createAnimation(String.format("animation.goblin.catch_%sperson", var7), true, var1);
                  break;
               case CATCH_BJ:
                  this.createAnimation(String.format("animation.goblin.catch_%spersonBj", var7), true, var1);
                  break;
               case CATCH_BJ_IDLE:
                  this.createAnimation(String.format("animation.goblin.catch_%spersonBj_idle", var7), true, var1);
                  break;
               case START_THROWING:
                  this.createAnimation(String.format("animation.goblin.throw_%sperson", var7), true, var1);
                  break;
               case THROWN:
                  this.createAnimation("animation.goblin.thrown", true, var1);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.goblin.paizuri_start", true, var1);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.goblin.paizuri_cum", true, var1);
                  break;
               case JUMP_0:
                  this.createAnimation("animation.goblin.jump_1", true, var1);
                  break;
               case JUMP_1:
                  this.createAnimation("animation.goblin.jump_2", true, var1);
                  break;
               case JUMP_2:
                  this.createAnimation("animation.goblin.jump_3", true, var1);
                  break;
               case BREEDING_INTRO_0:
                  this.createAnimation("animation.goblin.breeding_intro_1", true, var1);
                  break;
               case BREEDING_INTRO_1:
                  this.createAnimation("animation.goblin.breeding_intro_2", true, var1);
                  break;
               case BREEDING_INTRO_2:
                  this.createAnimation("animation.goblin.breeding_intro_3", true, var1);
                  break;
               case BREEDING_CUM_0:
                  this.createAnimation("animation.goblin.breeding_cum_1", true, var1);
                  break;
               case BREEDING_CUM_1:
                  this.createAnimation("animation.goblin.breeding_cum_2", true, var1);
                  break;
               case BREEDING_CUM_2:
                  this.createAnimation("animation.goblin.breeding_cum_3", true, var1);
                  break;
               case VANISH:
               case AWAIT_PICK_UP:
                  this.createAnimation("animation.goblin.await_pick_up", true, var1);
                  break;
               case STAND_UP:
                  this.createAnimation("animation.goblin.stand_up", false, var1);
                  break;
               case NELSON_INTRO:
                  this.createAnimation("animation.goblin.nelson_intro", true, var1);
                  break;
               case NELSON_CUM:
                  this.createAnimation("animation.goblin.nelson_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "catchEh":
               this.sendChatMessage("ehh..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchAkward":
               this.sendChatMessage("awkward..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchWell":
               this.sendChatMessage("well...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchRather":
               this.sendChatMessage("would you rather have this stupid... thing?");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchMe":
               this.sendChatMessage("...or use me?~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchDone":
               if ("bj".equals(this.entityDataManager.get(GIRL_HAND_STATES))) {
                  this.setCurrentAction(fp.CATCH_BJ);
               }
               break;
            case "catchBjDone":
               this.setCurrentAction(fp.CATCH_BJ_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var6 = Minecraft.getMinecraft().player;
                  openInventoryGui(var6, this, new String[]{"use her", "take ur stuff back"}, null, false);
               }
               break;
            case "paizuriChoice":
               this.sendChatMessage("good choice!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriBoth":
               this.sendChatMessage("...for both of us!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizruiUse":
               this.sendChatMessage("now use me like a fuck toy!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriSwitch":
               if (!this.getRNG().nextBoolean()) {
                  this.aP = "".equals(this.aP) ? "2" : "";
               }
               break;
            case "touch":
               this.playRandomSoundAtVolume(SoundHandler.MISC_TOUCH, 3.0F);
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "paizuri_startDone":
               this.setCurrentAction(fp.PAIZURI_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastDone":
               this.setCurrentAction(fp.PAIZURI_SLOW);
               break;
            case "paizuriFastReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.setCurrentAction(fp.PAIZURI_FAST_CONTINUES);
               }
               break;
            case "paizuriFastContinuesReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "smallPound":
               this.playRandomSoundAtVolume(SoundHandler.MISC_POUNDING, 0.25F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "paizruiCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var4 = Minecraft.getMinecraft().player;
                  var4.rotationPitch = 70.0F;
                  var4.prevRotationPitch = 70.0F;
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "paizuriCumDone":
               this.setCurrentAction(fp.NULL);
               break;
            case "cumSound":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "jumpCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var9 = Minecraft.getMinecraft();
                  var9.player.rotationYaw = this.getYawRotation() + 170.0F;
                  var9.player.rotationPitch = -20.0F;
                  var9.player.rotationYawHead = var9.player.rotationYaw;
                  var9.gameSettings.thirdPersonView = 2;
               }
               break;
            case "breedingHmm":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var8 = Minecraft.getMinecraft();
                  var8.player.rotationYaw = this.getYawRotation() + 180.0F;
                  var8.player.rotationPitch = -15.0F;
                  var8.player.rotationYawHead = var8.player.rotationYaw;
                  var8.gameSettings.thirdPersonView = 0;
               }

               this.sendChatMessage("hmm...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingFound":
               this.sendChatMessage("guess we found a worthy breeding partner!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingEnough":
               this.sendChatMessage("Eh.. go pin him down, before he runs off!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingCam2":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var7 = Minecraft.getMinecraft();
                  var7.gameSettings.thirdPersonView = 2;
                  var7.player.rotationYaw = this.getYawRotation() - 120.0F;
                  var7.player.rotationPitch = -30.0F;
               }
            case "breedingIntroDone":
               this.setCurrentAction(fp.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "breeding_slow1Done":
               if (this.getRNG().nextBoolean()) {
                  this.aD = !this.aD;
               }

               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.setCurrentAction(fp.BREEDING_FAST_0);
                  this.ay = false;
               }
               break;
            case "breeding_fast1Done":
               this.setCurrentAction(fp.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  this.ay = false;
               }
               break;
            case "breeding_fast1Ready":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.ay = true;
                  this.resetAnimationControllerOffset();
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "breeding_intro_3Done":
               this.setCurrentAction(fp.BREEDING_SLOW_2);
               break;
            case "breeding_3_wiggle":
               if (this.getRNG().nextBoolean()) {
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "breeding_fast_3Done":
               if (this.isControlledByLocalPlayer() && !d3.d) {
                  this.setCurrentAction(fp.BREEDING_SLOW_2);
               }
               break;
            case "breeding_intro_2Done":
               this.setCurrentAction(fp.BREEDING_1);
               break;
            case "breeding_cumCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft var5 = Minecraft.getMinecraft();
                  var5.gameSettings.thirdPersonView = 0;
                  var5.player.rotationYaw = this.getYawRotation() + 180.0F;
                  var5.player.rotationPitch = -15.0F;
                  var5.player.rotationYawHead = var5.player.rotationYaw;
                  var5.gameSettings.thirdPersonView = 0;
               }
               break;
            case "neslon_introDone":
               this.setCurrentAction(fp.NELSON_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "nelson_slowDone":
               if (this.getRNG().nextBoolean()) {
                  this.aF = !this.aF;
               }
               break;
            case "neslon_fastSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.X = true;
                  return;
               }

               if (d3.d) {
                  this.X = true;
               }
               break;
            case "neslon_fastBackSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.actionController.tickOffset = 0.0;
               } else if (d3.d) {
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "nelsonFastDone":
               this.X = false;
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(fp.NELSON_SLOW);
               }
               break;
            case "nelson_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  this.setCurrentAction(fp.NULL);
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      this.movementController.transitionLengthTicks = 10.0;
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

   public static class c {
      static Minecraft a = null;

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(ClientTickEvent var1) {
         if (var1.phase != Phase.START) {
            ArrayList var2 = new ArrayList();

            try {
               for (BaseGirlEntity var4 : BaseGirlEntity.getGirlEntityList()) {
                  if (var4.world.isRemote && var4 instanceof GoblinEntity) {
                     GoblinEntity var5 = (GoblinEntity)var4;
                     UUID var6 = var5.getOwnerUUID();
                     if (var6 != null) {
                        EntityPlayer var7 = var5.world.getPlayerEntityByUUID(var6);
                        if (var7 != null && var7.dimension != var5.dimension) {
                           var2.add(var5);
                        }
                     }
                  }
               }
            } catch (ConcurrentModificationException var8) {
            }

            for (GoblinEntity var10 : (java.util.Collection<GoblinEntity>) (var2) ) {
               var10.setOwnerUUID(null);
               var10.setInteractionPlayerUUID(null);
               var10.setDead();
            }
         }
      }

      @SubscribeEvent
      public void a(PlayerChangedDimensionEvent var1) {
         EntityPlayer var2 = var1.player;
         UUID var3 = var2.getPersistentID();
         int var4 = var1.toDim;
         World var5 = var2.world;
         GoblinEntity var6 = null;

         try {
            for (BaseGirlEntity var8 : BaseGirlEntity.getGirlEntityList()) {
               if (!var8.world.isRemote && var8 instanceof GoblinEntity) {
                  GoblinEntity var9 = (GoblinEntity)var8;
                  if (var3.equals(var9.getOwnerUUID())) {
                     String var10 = var9.getCustomModelCode();
                     String var11 = var9.getCustomPartListCode();
                     var6 = var9;
                     var6.setOwnerUUID(null);
                     var6.setInteractionPlayerUUID(null);
                     var6.setCurrentAction(fp.NULL);
                     GoblinEntity var12 = new GoblinEntity(var5);
                     var12.dimension = var4;
                     var12.forceSpawn = true;
                     var12.setCustomModelCode(var10);
                     var12.setCustomPartListCode(var11);
                     var12.entityDataManager.set(GoblinEntity.aC, true);
                     var5.spawnEntity(var12);
                     var12.setPositionAndUpdate(var2.posX, var2.posY, var2.posZ);
                     var12.setOwnerUUID(var3);
                     var12.setCurrentAction(fp.SHOULDER_IDLE);
                     break;
                  }
               }
            }
         } catch (ConcurrentModificationException var13) {
         }

         if (var6 != null) {
            var5.removeEntity(var6);
            BaseGirlEntity.getGirlEntityList().remove(var6);
         }
      }

      @SubscribeEvent
      public void a(LivingAttackEvent var1) {
         if (var1.getSource() != DamageSource.OUT_OF_WORLD) {
            EntityLivingBase var2 = var1.getEntityLiving();
            if (var2 instanceof GoblinEntity) {
               GoblinEntity var3 = (GoblinEntity)var2;
               if (var3.getOwnerUUID() != null) {
                  var1.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void a(KeyInputEvent var1) {
         if (a == null) {
            a = Minecraft.getMinecraft();
         }

         if (!(a.currentScreen instanceof GalathScreen)) {
            if (ClientProxy.keyBindings[0].isPressed()) {
               BaseGirlEntity var2 = null;
               UUID var3 = Minecraft.getMinecraft().player.getPersistentID();

               try {
                  for (BaseGirlEntity var5 : BaseGirlEntity.getGirlEntityList()) {
                     if (var5.world.isRemote && var5 instanceof IGoblin) {
                        IGoblin var6 = (IGoblin)var5;
                        if (var3.equals(var6.getOwnerUUID())) {
                           var2 = var5;
                           break;
                        }
                     }
                  }
               } catch (ConcurrentModificationException var7) {
               }

               if (var2 != null) {
                  if (var2.getCurrentAction() == fp.SHOULDER_IDLE) {
                     Minecraft.getMinecraft().displayGuiScreen(new GalathScreen(var2));
                  }
               }
            }
         }
      }

   }
}
