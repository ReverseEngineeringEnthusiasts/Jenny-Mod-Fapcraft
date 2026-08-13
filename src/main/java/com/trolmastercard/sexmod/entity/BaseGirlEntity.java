package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.model.GirlModel;
import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.client.renderer.SexSceneRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.KoboldStatePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendChatMessagePacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dz;
import com.trolmastercard.sexmod.util.e1;
import com.trolmastercard.sexmod.util.fs;
import com.trolmastercard.sexmod.util.g0;







import com.mojang.realmsclient.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityEnderPearl;
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
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.MatrixStack;

public abstract class BaseGirlEntity extends EntityCreature implements IAnimatable {
   protected static final long t = 20L;
   private final AnimationFactory g = new AnimationFactory(this);
   public EntityAIWanderAvoidWater z;
   public WatchClosestGirlGoal o;
   public static HashSet<BaseGirlEntity> k = new HashSet<>();
   public Vec3d B;
   protected float r;
   public EntityDataManager m;
   public PathNavigate f;
   public Vec3d l = Vec3d.ZERO;
   public EntityEnderPearl q;
   public float n = 1.0F;
   public boolean F = false;
   private boolean i = false;
   HashMap<String, Vec3d> x = new HashMap<>();
   public static final DataParameter<String> v = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(110);
   public static final DataParameter<Boolean> G = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(109);
   public static final DataParameter<String> e = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(108);
   public static final DataParameter<Float> w = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(107);
   public static final DataParameter<String> u = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(106);
   public static final DataParameter<Integer> D = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(105);
   public static final DataParameter<String> J = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(104);
   public static final DataParameter<String> h = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(103);
   public static final DataParameter<String> y = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(102);
   public static final DataParameter<String> a = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(101);
   public static final DataParameter<String> b = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(100);
   public static final DataParameter<String> c = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(99);
   protected static final List<Item> I = Arrays.asList(Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.ENDER_PEARL);
   public AnimationController C;
   public AnimationController E;
   public AnimationController s;
   HashMap<String, Pair<Integer, Integer>> A = new HashMap<>();
   AnimationProcessor<?> H = null;
   public List<String> p = new ArrayList<>();
   protected List<Entry<BoneType, Entry<List<String>, Integer>>> d = null;

   public void a(BaseGirlEntity.BaseGirlEntityState var1) {
      this.m.set(a, var1.toString());
   }

   public BaseGirlEntity.BaseGirlEntityState q_clash489() {
      return BaseGirlEntity.BaseGirlEntityState.valueOf((String)this.m.get(a));
   }

   @SideOnly(Side.CLIENT)
   protected void changeDataParameterFromClient(String var1, String var2) {
      PacketHandler.b.sendToServer(new ChangeDataParameterPacket(this.getGirlId(), var1, var2));
   }

   public UUID getGirlId() {
      try {
         return UUID.fromString((String)this.m.get(u));
      } catch (Exception var3) {
         UUID var2 = UUID.randomUUID();
         this.m.set(u, var2.toString());
         return var2;
      }
   }

   public fp getCurrentAction() {
      return fp.valueOf((String)this.m.get(J));
   }

   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != var1) {
         if (var1 != fp.ATTACK || var2 == fp.NULL) {
            var1 = var1 == null ? fp.NULL : var1;
            if (this.world.isRemote) {
               this.changeDataParameterFromClient("currentAction", var1.toString());
            } else {
               var2.ticksPlaying = new int[]{0, 0};
               this.m.set(J, var1.toString());
            }
         }
      }
   }

   public int getOutfitIndex() {
      return (Integer)this.m.get(D);
   }

   public void f(int var1) {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("currentModel", "0");
      } else {
         this.m.set(D, var1);
      }
   }

   public boolean m_clash494() {
      return false;
   }

   @Nullable
   public EntityPlayer S_clash495() {
      UUID var1 = this.getInteractionPlayerUUID();
      return var1 == null ? null : this.world.getPlayerEntityByUUID(var1);
   }

   public static void a_clash496(BaseGirlEntity var0, String var1) {
      for (EntityPlayer var3 : cj.a_clash303(var0)) {
         var3.sendMessage(new TextComponentString(var1));
      }
   }

   public static void a(BaseGirlEntity var0, SoundEvent var1, boolean var2) {
      Vec3d var3 = var0.getPositionVector();

      for (EntityPlayer var5 : cj.a_clash303(var0)) {
         Vec3d var6;
         if (!var2) {
            var6 = var3;
         } else {
            Vec3d var7 = var5.getPositionVector();
            Vec3d var8 = var3.subtract(var7).normalize();
            var6 = var7.add(var8);
         }

         ((EntityPlayerMP)var5)
            .connection
            .sendPacket(new SPacketSoundEffect(var1, SoundCategory.AMBIENT, var6.x, var6.y, var6.z, 1.0F, 1.0F));
      }
   }

   public static void a(BaseGirlEntity var0, SoundEvent var1) {
      a(var0, var1, false);
   }

   public static void a(BaseGirlEntity var0, SoundEvent[] var1) {
      a(var0, SoundHandler.randomSound(var1));
   }

   public static void a(BaseGirlEntity var0, SoundEvent[] var1, boolean var2) {
      a(var0, SoundHandler.randomSound(var1), var2);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d A_clash497() {
      Vec3d var1 = Minecraft.getMinecraft().player.getPositionVector();
      Vec3d var2 = this.getPositionVector();
      Vec3d var3 = var2.subtract(var1).normalize();
      return var1.add(var3);
   }

   @Nullable
   public UUID getInteractionPlayerUUID() {
      String var1 = (String)this.m.get(y);
      return var1.equals("null") ? null : UUID.fromString(var1);
   }

   public void setInteractionPlayerUUID(UUID var1) {
      if (this.world.isRemote) {
         if (var1 == null) {
            this.changeDataParameterFromClient("playerSheHasSexWith", null);
         } else {
            this.changeDataParameterFromClient("playerSheHasSexWith", var1.toString());
         }
      } else {
         if (var1 == null) {
            this.m.set(y, "null");
         } else {
            this.m.set(y, var1.toString());
         }
      }
   }

   public void setInteractionPlayer(@Nonnull EntityPlayer var1) {
      this.setInteractionPlayerUUID(var1.getPersistentID());
   }

   public Vec3d getTargetPosition() {
      String[] var1 = ((String)this.m.get(e)).split("\\|");
      return new Vec3d(Double.parseDouble(var1[0]), Double.parseDouble(var1[1]), Double.parseDouble(var1[2]));
   }

   public void setTargetPosition(Vec3d var1) {
      if (this.world.isRemote) {
         String var2 = var1.x + "f" + var1.y + "f" + var1.z + "f";
         this.changeDataParameterFromClient("targetPos", var2);
      } else {
         this.m.set(e, var1.x + "|" + var1.y + "|" + var1.z);
      }
   }

   public void setTargetPositionDirect(Vec3d var1) {
      this.m.set(e, var1.x + "|" + var1.y + "|" + var1.z);
   }

   public Float getYawRotation() {
      return (Float)this.m.get(w);
   }

   public void setYawRotation(float var1) {
      this.m.set(w, var1);
   }

   public void setAnchored(boolean var1) {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("shouldbeattargetpos", String.valueOf(var1));
      } else {
         this.m.set(G, var1);
      }
   }

   public boolean isAnchored() {
      return (Boolean)this.m.get(G);
   }

   protected boolean canDespawn() {
      return false;
   }

   protected BaseGirlEntity(World var1) {
      super(var1);
      if (var1.isRemote) {
         this.p_clash506();
      }

      if (!var1.isRemote || !(var1 instanceof SexWorldClient)) {
         PathNavigate var2 = this.getNavigator();
         if (var2 instanceof PathNavigateGround) {
            ((PathNavigateGround)var2).setBreakDoors(true);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void p_clash506() {
      this.C = new AnimationController<>(this, "action", 0.0F, this::a);
      this.E = new AnimationController<>(this, "movement", 5.0F, this::a);
      this.s = new AnimationController<>(this, "eyes", 10.0F, this::a);
   }

   protected void entityInit() {
      super.entityInit();
      this.f = this.getNavigator();
      this.m = this.getDataManager();
      this.m.register(u, UUID.randomUUID().toString());
      this.m.register(D, 1);
      this.m.register(J, fp.NULL.toString());
      this.m.register(h, "");
      this.m.register(y, "null");
      this.m.register(G, false);
      this.m.register(w, 0.0F);
      this.m.register(e, "0|0|0");
      this.m.register(v, "");
      this.m.register(a, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      this.m.register(b, "");
      this.m.register(c, "");
   }

   public void setLocallyRegistered(boolean var1) {
      this.i = var1;
      if (var1) {
         fs.b_clash710(this);
      } else {
         fs.a_clash711(this);
      }
   }

   public boolean isLocallyRegistered() {
      return this.i;
   }

   public static List<BaseGirlEntity> getGirlEntityList() {
      if (!g0.a_clash472()) {
         return Z_clash510();
      }

      WorldServer[] var0 = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
      if (var0.length == 0) {
         return new ArrayList<>();
      }

      ArrayList var1 = new ArrayList();

      for (WorldServer var5 : var0) {
         var1.addAll(var5.getEntities(BaseGirlEntity.class, var0x -> true));
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   private static List<BaseGirlEntity> Z_clash510() {
      WorldClient var0 = Minecraft.getMinecraft().world;
      return var0 == null ? new ArrayList<>() : var0.getEntities(BaseGirlEntity.class, var0x -> true);
   }

   public boolean B_clash511() {
      return true;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
   }

   protected void initEntityAI() {
      this.z = new EntityAIWanderAvoidWater(this, 0.35);
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAITempt(this, 0.4, false, new HashSet<>(I)));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.o);
      this.tasks.addTask(5, this.z);
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setDouble("homeX", this.l.x);
      var1.setDouble("homeY", this.l.y);
      var1.setDouble("homeZ", this.l.z);
      var1.setString("girlID", (String)this.m.get(u));
      String var2 = this.w_clash539();
      if (!"".equals(var2)) {
         var1.setString("sexmod:customname", var2);
      }

      if (this.X_clash438()) {
         var1.setString("sexmod:customModel", this.getCustomModelCode());
      }

      super.writeEntityToNBT(var1);
   }

   protected boolean X_clash438() {
      return a_clash542(this);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.l = new Vec3d(var1.getDouble("homeX"), var1.getDouble("homeY"), var1.getDouble("homeZ"));
      String var2 = var1.getString("sexmod:customname");
      if (!"".equals(var2)) {
         this.g_clash538(var2);
      }

      String var3 = var1.getString("girlID");
      if (!"".equals(var3)) {
         UUID var4 = UUID.fromString(var3);
         boolean var5 = false;

         for (BaseGirlEntity var7 : girlList(var4)) {
            if (!var7.world.isRemote && var7 != this && !var7.isDead && var7.isAddedToWorld()) {
               var5 = true;
               break;
            }
         }

         if (var5) {
            Main.LOGGER.log(Level.WARN, String.format("got a duped %s with id '%s'. Deleted her", this.getDisplayNameText(), var4));
            this.world.removeEntity(this);
         } else {
            this.m.set(u, var4.toString());
            if (this.X_clash438()) {
               this.setCustomModelCode(var1.getString("sexmod:customModel"));
            }
         }
      }
   }

   public boolean d_clash453() {
      return true;
   }

   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
   }

   public void setVelocity(Vec3d var1) {
      this.motionX = var1.x;
      this.motionY = var1.y;
      this.motionZ = var1.z;
   }

   public Vec3d getLastTickPosVector() {
      return new Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ);
   }

   public void updateAITasks() {
      if ((Boolean)this.m.get(G)) {
         this.setRotationYawHead(this.getYawRotation());
         this.setPositionAndRotation(this.getTargetPosition().x, this.getTargetPosition().y, this.getTargetPosition().z, this.getYawRotation(), 0.0F);
         this.setRotation(this.getYawRotation(), this.rotationPitch);
      }

      if (this.l.equals(Vec3d.ZERO)) {
         this.l = new Vec3d(this.getPosition());
      }

      this.G();
   }

   public void onUpdate() {
      super.onUpdate();
      this.tickFollowUpTransitions();
   }

   protected void G() {
      if (ServerWhitelistManager.e) {
         HashSet var1 = this.getCustomPartsSet();
         NpcType var2 = NpcType.getNpcType(this);
         HashSet var3 = new HashSet();
         String var4 = ServerWhitelistManager.h_clash132();

         for (String var6 : (java.util.Collection<String>) (var1) ) {
            if (!"".equals(ServerWhitelistManager.a_clash136(var6, var4))) {
               var3.add(var6);
            } else {
               HashSet var7 = ServerWhitelistManager.a_clash139(var6);
               if (var7 == null) {
                  var3.add(var6);
               } else if (!var7.isEmpty() && !var7.contains(var2)) {
                  var3.add(var6);
               }
            }
         }

         if (!var3.isEmpty()) {
            var1.removeAll(var3);
            this.setCustomModelCode(encodeCustomParts(var1));
         }
      }
   }

   protected void tickFollowUpTransitions() {
      fp var1 = this.getCurrentAction();
      if (++var1.ticksPlaying[this.world.isRemote ? 1 : 0] >= var1.length) {
         if (var1.followUp != null && !this.world.isRemote) {
            this.b(var1.followUp);
         }
      }
   }

   protected void tickPathVelocity() {
      Path var1 = this.getNavigator().getPath();
      if (var1 != null) {
         if (!this.onGround && !this.isInWater()) {
            int var2 = var1.getCurrentPathIndex();
            int var3 = var1.getCurrentPathLength();
            if (var3 != var2 && var3 - 1 != var2) {
               PathPoint var4 = var1.getPathPointFromIndex(var2);
               PathPoint var5 = var1.getPathPointFromIndex(var2 + 1);
               Vec3d var6 = new Vec3d(var5.x - var4.x, var5.y - var4.y, var5.z - var4.z);
               this.motionX = var6.x / 7.0;
               this.motionZ = var6.z / 7.0;
            }
         }
      }
   }

   public void reinitTasks() {
   }

   @SideOnly(Side.CLIENT)
   public boolean openInteractionMenu(EntityPlayer var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected static void a_clash516(EntityPlayer var0, BaseGirlEntity var1) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0));
   }

   @SideOnly(Side.CLIENT)
   protected static void a(EntityPlayer var0, BaseGirlEntity var1, String[] var2, ItemStack[] var3, boolean var4) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0, var2, var3, var4));
   }

   @SideOnly(Side.CLIENT)
   protected static void a(EntityPlayer var0, BaseGirlEntity var1, String[] var2, boolean var3) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0, var2, null, var3));
   }

   public void a_clash517(ItemStack var1) {
      this.activeItemStack = var1;
   }

   public void d(int var1) {
      this.activeItemStackUseCount = var1;
   }

   public Vec3d M_clash518() {
      return new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
   }

   protected static Vec3d a_clash519(BaseGirlEntity var0) {
      return new Vec3d(var0.prevPosX, var0.prevPosY, var0.prevPosZ);
   }

   public BaseGirlEntity af_clash520() {
      return this;
   }

   public void x_clash475() {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("master", "");
         this.changeDataParameterFromClient("walk speed", BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      } else {
         this.m.set(v, "");
         this.m.set(a, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      }
   }

   protected void a(EntityPlayerMP var1, boolean var2) {
      var1.motionX = 0.0;
      var1.motionY = 0.0;
      var1.motionZ = 0.0;
      if (var2) {
         Vec3d var3 = this.getVectorTowardPlayer(0.35);
         var1.setPositionAndUpdate(var3.x, var3.y, var3.z);
      }
   }

   public void j_clash521(UUID var1) {
      EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
      var2.motionX = 0.0;
      var2.motionY = 0.0;
      var2.motionZ = 0.0;
      Vec3d var3 = this.getVectorTowardPlayer(0.35);
      var2.setPositionAndUpdate(var3.x, var3.y, var3.z);
      this.setYawRotation(var2.rotationYawHead + 180.0F);
   }

   protected void a(boolean var1, boolean var2, UUID var3) {
      if (this.world.isRemote) {
         PacketHandler.b.sendToServer(new KoboldStatePacket(this.getGirlId(), var3, var1, var2));
      } else {
         KoboldStatePacket.Handler.a(this.getGirlId(), var3, var1, var2);
      }
   }

   public static BaseGirlEntity getClientGirlEntity(UUID var0) {
      if (var0 == null) {
         return null;
      }

      for (BaseGirlEntity var2 : girlList(var0)) {
         if (var2.world.isRemote) {
            return var2;
         }
      }

      return null;
   }

   public static BaseGirlEntity getServerGirlEntity(UUID var0) {
      if (var0 == null) {
         return null;
      }

      for (BaseGirlEntity var2 : girlList(var0)) {
         if (!var2.world.isRemote) {
            return var2;
         }
      }

      return null;
   }

   public static ArrayList<BaseGirlEntity> girlList(UUID var0) {
      ArrayList var1 = new ArrayList();

      try {
         for (BaseGirlEntity var3 : getGirlEntityList()) {
            if (var3 != null && var3.getGirlId().equals(var0)) {
               var1.add(var3);
            }
         }
      } catch (ConcurrentModificationException var4) {
         System.out.println("had a ConcurrentModificationException while cycling through the girl list... hopefully nothin borke owo");
         var4.printStackTrace();
      }

      return var1;
   }

   protected BlockPos a_clash525(BlockPos var1) {
      return this.a(var1, 1);
   }

   public BlockPos a(BlockPos var1, int var2) {
      return this.a(var1, var2, Blocks.BED, 22, 3, null);
   }

   public void W() {
      this.m.set(HAND_STATES, Byte.valueOf("1"));
   }

   public void K() {
      this.m.set(HAND_STATES, Byte.valueOf("0"));
   }

   public BlockPos a(BlockPos var1, int var2, Block var3, int var4, int var5, @Nullable HashSet<Biome> var6) {
      int var7 = 1;
      byte var8 = -1;
      BlockPos var9 = var1;
      int var10 = 0;

      while (var7 < var4) {
         for (int var11 = 0; var11 < 2; var11++) {
            var8 *= -1;

            for (int var12 = 0; var12 < var7; var12++) {
               var9 = var9.add(0, 0, var8);

               for (int var13 = -var5; var13 < var5 + 1; var13++) {
                  if (this.world.getBlockState(var9.add(0, var13, var8)).getBlock() == var3) {
                     var10++;
                     if (var10 >= var2 && (var6 == null || var6.contains(this.world.getBiome(var9.add(var8, var13, 0))))) {
                        return var9.add(0, var13, var8);
                     }
                  }
               }
            }

            for (int var14 = 0; var14 < var7; var14++) {
               var9 = var9.add(var8, 0, 0);

               for (int var15 = -var5; var15 < var5 + 1; var15++) {
                  if (this.world.getBlockState(var9.add(var8, var15, 0)).getBlock() == var3) {
                     var10++;
                     if (var10 >= var2 && (var6 == null || var6.contains(this.world.getBiome(var9.add(var8, var15, 0))))) {
                        return var9.add(var8, var15, 0);
                     }
                  }
               }
            }

            var7++;
         }
      }

      return null;
   }

   protected List<BlockPos> a(BlockPos var1, Class var2, int var3, int var4, @Nullable HashSet<Biome> var5) {
      int var6 = 1;
      byte var7 = -1;
      BlockPos var8 = var1;
      ArrayList var9 = new ArrayList();

      while (var6 < var3) {
         for (int var10 = 0; var10 < 2; var10++) {
            var7 *= -1;

            for (int var11 = 0; var11 < var6; var11++) {
               var8 = var8.add(0, 0, var7);

               for (int var12 = -var4; var12 < var4 + 1; var12++) {
                  if (var2.isInstance(this.world.getBlockState(var8.add(0, var12, var7)).getBlock())
                     && (var5 == null || var5.contains(this.world.getBiome(var8.add(var7, var12, 0))))) {
                     var9.add(var8.add(0, var12, var7));
                  }
               }
            }

            for (int var13 = 0; var13 < var6; var13++) {
               var8 = var8.add(var7, 0, 0);

               for (int var14 = -var4; var14 < var4 + 1; var14++) {
                  if (var2.isInstance(this.world.getBlockState(var8.add(var7, var14, 0)).getBlock())
                     && (var5 == null || var5.contains(this.world.getBiome(var8.add(var7, var14, 0))))) {
                     var9.add(var8.add(var7, var14, 0));
                  }
               }
            }

            var6++;
         }
      }

      return var9;
   }

   public boolean J_clash526() {
      return !((String)this.m.get(v)).equals("");
   }

   @Nullable
   public UUID O_clash527() {
      String var1 = (String)this.m.get(v);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString(var1);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   @Nullable
   public EntityPlayer z_clash528() {
      UUID var1 = this.O_clash527();
      return var1 == null ? null : this.world.getPlayerEntityByUUID(var1);
   }

   protected ResourceLocation getLootTable() {
      return dz.d;
   }

   @SideOnly(Side.CLIENT)
   public void a(String var1, UUID var2) {
   }

   @SideOnly(Side.CLIENT)
   protected abstract <E extends IAnimatable> PlayState a(AnimationEvent<E> var1);

   @SideOnly(Side.CLIENT)
   protected boolean a(fp var1, String var2, boolean var3, AnimationEvent var4) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, boolean var2, AnimationEvent var3, boolean var4) {
      if (var4 || !fp.b_clash719(this, var3.getPartialTick()) || !this.a(this.getCurrentAction(), var1, d3.d, var3)) {
         ILoopType.EDefaultLoopTypes var5 = var2 ? ILoopType.EDefaultLoopTypes.LOOP : ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
         var3.getController().setAnimation(new AnimationBuilder().addAnimation(var1, var5));
         var3.getController().transitionLengthTicks = 0.0;
      }
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, boolean var2, AnimationEvent var3) {
      this.a(var1, var2, var3, false);
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, int var2, float var3, AnimationEvent var4, boolean var5) {
      if (var5 || !fp.b_clash719(this, var4.getPartialTick()) || !this.a(this.getCurrentAction(), var1, d3.d, var4)) {
         AnimationController var6 = var4.getController();
         Pair var7 = this.A.get(var1);
         if (var7 == null) {
            var7 = Pair.of(0, 0);
         }

         int var8 = (Integer)var7.first();
         int var9 = (Integer)var7.second();
         if (!fp.b_clash719(this, var4.getPartialTick())) {
            var4.getController().setAnimation(new AnimationBuilder().addAnimation(var8 == 0 ? var1 : var1 + var8, ILoopType.EDefaultLoopTypes.LOOP));
            var4.getController().transitionLengthTicks = 0.0;
         } else {
            int var10 = this.a(var8, var9, var2, var3);
            AnimationBuilder var12 = new AnimationBuilder();
            AnimationController var11 = var6;
            AnimationController var10000;
            AnimationBuilder var10001;
            String var10002;
            if (var10 == 0) {
               var10000 = var11;
               var10001 = var12;
               var10002 = var1;
            } else {
               var10000 = var11;
               var10001 = var12;
               var10002 = var1 + var10;
            }

            var10000.setAnimation(var10001.addAnimation(var10002, ILoopType.EDefaultLoopTypes.LOOP));
            var6.transitionLengthTicks = 0.0;
            HashMap var16 = this.A;
            Integer var15 = var10;
            String var14 = var1;
            HashMap var13 = var16;
            HashMap var17;
            String var18;
            Integer var19;
            int var10003;
            if (var10 == 0) {
               var17 = var13;
               var18 = var14;
               var19 = var15;
               var10003 = var9;
            } else {
               var17 = var13;
               var18 = var14;
               var19 = var15;
               var10003 = var10;
            }

            var17.put(var18, Pair.of(var19, var10003));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void a(String var1, int var2, float var3, AnimationEvent var4) {
      this.a(var1, var2, var3, var4, false);
   }

   int a(int var1, int var2, int var3, float var4) {
      if (var1 != 0) {
         return 0;
      }

      Random var5 = this.getRNG();
      if (var5.nextFloat() > var4) {
         return 0;
      }

      int var6;
      do {
         var6 = var5.nextInt(var3);
      } while ((var6 == var2 || var6 == 0) && var3 > 2);

      return var6;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public abstract void registerControllers(AnimationData var1);

   protected void s() {
      if (this.world.isRemote && this.isControlledByLocalPlayer()) {
         this.B = null;
         PacketHandler.b.sendToServer(new ResetGirlPacket(this.getGirlId(), true));
      } else if (!this.world.isRemote) {
         ResetGirlPacket.Handler.a((EntityPlayerMP)this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID()));
      }
   }

   public static BaseGirlEntity c_clash529(EntityPlayer var0) {
      return var0 == null ? null : i_clash530(var0.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public Vec3d a(Minecraft var1, SexSceneEntity var2, EntityLivingBase var3, float var4) {
      return SexSceneRenderer.a(var1, var2, var3, this, var4);
   }

   public static BaseGirlEntity i_clash530(@Nonnull UUID var0) {
      return a(var0, (Boolean)null);
   }

   public static BaseGirlEntity a(@Nonnull UUID var0, Boolean var1) {
      try {
         for (BaseGirlEntity var3 : getGirlEntityList()) {
            if (!var3.isDead && var0.equals(var3.getInteractionPlayerUUID())) {
               if (var1 == null) {
                  return var3;
               }

               boolean var4 = var3.world.isRemote;
               if (var4 && !var1) {
                  return var3;
               }

               if (!var4 && var1) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      return null;
   }

   @Nullable
   public static BaseGirlEntity c_clash531(@Nonnull UUID var0) {
      boolean var1 = FMLCommonHandler.instance().getMinecraftServerInstance() == null;

      try {
         for (BaseGirlEntity var3 : getGirlEntityList()) {
            if (!var3.isDead) {
               boolean var4 = var3.world.isRemote;
               if (var4 == var1 && var0.equals(var3.getInteractionPlayerUUID())) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      return null;
   }

   public static BaseGirlEntity d_clash532(@Nonnull EntityPlayer var0) {
      return c_clash531(var0.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public void ac() {
   }

   public void r_clash533() {
      this.B = null;
      this.setNoGravity(false);
      this.b((fp)null);
      if (this.world.isRemote) {
         this.V();
      }
   }

   @SideOnly(Side.CLIENT)
   protected void V() {
      if (this.isControlledByLocalPlayer()) {
         d3.setMovementLock(true);
         Minecraft.getMinecraft().player.setInvisible(false);
         PacketHandler.b.sendToServer(new ResetGirlPacket(this.getGirlId()));
      }
   }

   @SideOnly(Side.CLIENT)
   public static void k(UUID var0) {
      try {
         for (BaseGirlEntity var2 : getGirlEntityList()) {
            UUID var3 = var2.getInteractionPlayerUUID();
            if (var3 != null && var3.equals(var0)) {
               fp var4 = var2.getNextAction(var2.getCurrentAction());
               if (var4 == null) {
                  return;
               }

               var2.b(var4);
               return;
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   @SideOnly(Side.CLIENT)
   public static void f_clash534(UUID var0) {
      try {
         for (BaseGirlEntity var2 : getGirlEntityList()) {
            if (!var2.isDead && var2.world.isRemote) {
               UUID var3 = var2.getInteractionPlayerUUID();
               if (var3 != null && var3.equals(var0)) {
                  fp var4 = var2.getCumAction(var2.getCurrentAction());
                  if (var4 != null) {
                     var2.b(var4);
                  }
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   public void N() {
      this.ag();
      PacketHandler.b.sendToServer(new ResetControllerPacket(this.getGirlId()));
   }

   @SideOnly(Side.CLIENT)
   public void ag() {
      this.C.tickOffset = 0.0;
   }

   @SideOnly(Side.CLIENT)
   @Nullable
   protected abstract fp getNextAction(fp var1);

   @SideOnly(Side.CLIENT)
   protected abstract fp getCumAction(fp var1);

   public TargetPoint getTargetNetworkPoint() {
      return new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 50.0);
   }

   protected void positionPlayerRelative(double var1, double var3, double var5, float var7, float var8) {
      if (this.getInteractionPlayerUUID() == null) {
         System.out.println("couldnt move camera because the player isn't set");
      } else {
         EntityPlayer var9 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (this.B == null) {
            this.B = var9.getPositionVector();
         }

         Vec3d var10 = this.B;
         var10 = var10.add(-Math.sin((this.r + 90.0F) * (Math.PI / 180.0)) * var1, 0.0, Math.cos((this.r + 90.0F) * (Math.PI / 180.0)) * var1);
         var10 = var10.add(0.0, var3, 0.0);
         var10 = var10.add(-Math.sin(this.r * (Math.PI / 180.0)) * var5, 0.0, Math.cos(this.r * (Math.PI / 180.0)) * var5);
         if (this.world.isRemote) {
            PacketHandler.b.sendToServer(new TeleportPlayerPacket(var9.getPersistentID().toString(), var10, this.r + var7, var8));
         } else {
            var9.setPositionAndRotation(var10.x, var10.y, var10.z, this.r + var7, var8);
            var9.setPositionAndUpdate(var10.x, var10.y, var10.z);
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean isControlledByLocalPlayer() {
      if (!this.world.isRemote) {
         return false;
      }

      EntityPlayerSP var1 = Minecraft.getMinecraft().player;
      return var1.getPersistentID().equals(this.getInteractionPlayerUUID()) || var1.getUniqueID().equals(this.getInteractionPlayerUUID());
   }

   protected void U() {
   }

   public void g_clash538(String var1) {
      this.m.set(c, var1);
   }

   public String w_clash539() {
      return (String)this.m.get(c);
   }

   public abstract String getDisplayNameText();

   public String ab_clash540() {
      String var1 = (String)this.m.get(c);
      return !"".equals(var1) ? var1 : this.getDisplayNameText();
   }

   public abstract float i_clash226();

   @SideOnly(Side.CLIENT)
   public boolean t_clash283() {
      return true;
   }

   public void h(String var1) {
      if (!this.world.isRemote) {
         PacketHandler.b
            .sendToAllAround(
               new SendChatMessagePacket(String.format("<%s> %s", this.ab_clash540(), var1), this.dimension, this.getGirlId()),
               new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 40.0)
            );
      } else if (this.isControlledByLocalPlayer()) {
         PacketHandler.b.sendToServer(new SendChatMessagePacket(String.format("<%s> %s", this.ab_clash540(), var1), this.dimension, this.getGirlId()));
      }
   }

   protected void b(String var1, boolean var2) {
      if (!var2) {
         this.h(var1);
      }

      if (!this.world.isRemote) {
         PacketHandler.b
            .sendToAllAround(
               new SendChatMessagePacket(var1, this.dimension, this.getGirlId()),
               new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 40.0)
            );
      } else {
         if (this.isControlledByLocalPlayer()) {
            PacketHandler.b.sendToServer(new SendChatMessagePacket(var1, this.dimension, this.getGirlId()));
         }
      }
   }

   protected void sendChatMessage(String var1) {
      if (this.world.isRemote) {
         Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format("<%s> %s", this.ab_clash540(), var1)));
      }
   }

   protected void a(UUID var1, String var2) {
      EntityPlayer var3 = this.world.getPlayerEntityByUUID(var1);
      if (var3 == null) {
         System.out.println("Player with UUID " + var1.toString() + " not found");
      } else {
         if (this.world.isRemote) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("<" + var3.getName() + "> " + var2));
         }
      }
   }

   public void a(SoundEvent var1, float var2, float var3) {
      this.world
         .playSound(
            this.getPosition().getX(),
            this.getPosition().getY(),
            this.getPosition().getZ(),
            var1,
            SoundCategory.NEUTRAL,
            var2,
            var3,
            false
         );
   }

   public void a(SoundEvent var1) {
      this.a(var1, 1.0F, 1.0F);
   }

   public void a(SoundEvent[] var1, int... var2) {
      if (var2.length == 0) {
         this.a(var1[this.getRNG().nextInt(var1.length)]);
      } else {
         this.a(var1[var2[this.getRNG().nextInt(var2.length)]], 1.0F, 1.0F);
      }
   }

   public void a(SoundEvent[] var1, float var2) {
      this.a(var1[this.getRNG().nextInt(var1.length)], var2, 1.0F);
   }

   public void a(SoundEvent var1, float var2) {
      this.a(var1, var2, 1.0F);
   }

   public static boolean a_clash542(Entity var0) {
      if (var0 == null) {
         return false;
      } else {
         return !(var0 instanceof BaseGirlEntity) ? false : !(var0 instanceof AbstractPlayerGirlEntity);
      }
   }

   @SideOnly(Side.CLIENT)
   public BaseGirlEntity E_clash543() {
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean isLocalPlayerNearby() {
      EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 50.0);
      return var1 == null ? false : var1.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID());
   }

   public Vec3d aa_clash545() {
      return this.getVectorTowardPlayer(1.0);
   }

   public Vec3d getVectorTowardPlayer(double var1) {
      EntityPlayer var3 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      float var4 = var3.rotationYaw;
      return var3.getPositionVector().add(-Math.sin(var4 * (Math.PI / 180.0)) * var1, 0.0, Math.cos(var4 * (Math.PI / 180.0)) * var1);
   }

   public Vec3d a_clash432(Vec3d var1, float var2) {
      return var1;
   }

   public static void a(EnumParticleTypes var0, BaseGirlEntity var1) {
      double var2 = Reference.f.nextGaussian() * 0.02;
      double var4 = Reference.f.nextGaussian() * 0.02;
      double var6 = Reference.f.nextGaussian() * 0.02;
      var1.world
         .spawnParticle(
            var0,
            var1.posX + Reference.f.nextFloat() * var1.width * 2.0F - var1.width,
            var1.posY + 0.5 + Reference.f.nextFloat() * var1.height,
            var1.posZ + Reference.f.nextFloat() * var1.width * 2.0F - var1.width,
            var2,
            var4,
            var6,
            new int[0]
         );
   }

   public static void a(EnumParticleTypes var0, BaseGirlEntity var1, int var2) {
      for (int var3 = 0; var3 < var2; var3++) {
         a(var0, var1);
      }
   }

   @Override
   public AnimationFactory getFactory() {
      return this.g;
   }

   public boolean canBePushed() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected SoundEvent getAmbientSound() {
      if (this.getRNG().nextInt(10000) == 0) {
         if (this.world.isRemote && Minecraft.getMinecraft().player.getPositionVector().distanceTo(this.getPositionVector()) < 10.0) {
            this.sendChatMessage("whopa");
         }

         return SoundHandler.randomSound(SoundHandler.MISC_FART);
      } else {
         return null;
      }
   }

   public float T_clash293() {
      return 0.0F;
   }

   public float ai_clash294() {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public MatrixStack a(String var1, boolean var2) {
      if (this.H == null) {
         this.H = this.getAnimationProcessor();
      }

      IBone var3 = this.H.getBone(var1);
      if (var3 == null) {
         if (!GirlModel.e.contains(var1)) {
            Main.LOGGER.log(Level.WARN, String.format("The bone '%s' does not exist on %s. Bone model matrix couldn't be calculated", var1, this.getDisplayNameText()));
            this.p.remove(var1);
         }

         return new MatrixStack();
      } else {
         GeoBone var4 = (GeoBone)var3;
         ArrayList var5 = new ArrayList();
         GeoBone var6 = var4;

         while (var6.parent != null) {
            GeoBone var7 = var6.parent;
            var5.add(var7);
            var6 = var7;
         }

         Collections.reverse(var5);
         MatrixStack var9 = new MatrixStack();
         if (this.isAnchored()) {
            var9.rotateY((float)(-Math.toRadians(this.getYawRotation().floatValue())));
         } else if (var2) {
            var9.rotateY(
               (float)(-Math.toRadians(RotationHelper.lerp(this.prevRenderYawOffset, this.renderYawOffset, Minecraft.getMinecraft().getRenderPartialTicks())))
            );
         }

         for (GeoBone var8 : (java.util.Collection<GeoBone>) (var5) ) {
            var9.translate(var8);
            var9.moveToPivot(var8);
            var9.rotate(var8);
            var9.scale(var8);
            var9.moveBackFromPivot(var8);
         }

         var9.translate(var4);
         var9.moveToPivot(var4);
         var9.rotate(var4);
         var9.scale(var4);
         return this.a(var9);
      }
   }

   protected MatrixStack a(MatrixStack var1) {
      return var1;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getCachedBoneOffset(String var1) {
      Vec3d var2 = this.x.get(var1);
      if (var2 != null) {
         return var2;
      }

      if (!this.p.contains(var1)) {
         this.p.add(var1);
      }

      return Vec3d.ZERO;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d d_clash548(String var1) {
      return this.getCachedBoneOffset(var1).add(this.getPositionVector());
   }

   public void a(String var1, Vec3d var2) {
      this.x.put(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public float getCameraBoneHeight() {
      AnimationProcessor var1 = this.getAnimationProcessor();
      IBone var2 = var1.getBone("girlCam");
      if (var2 == null) {
         return 0.0F;
      }

      float var3 = var2.getPivotY();
      var3 = this.a_clash356(var3);
      return var3 / 16.0F;
   }

   @SideOnly(Side.CLIENT)
   public float v_clash550() {
      return 1.0F;
   }

   protected float a_clash356(float var1) {
      return var1;
   }

   public AnimatedGeoModel<? extends BaseGirlEntity> a_clash551() {
      Minecraft var1 = Minecraft.getMinecraft();
      Render var2 = var1.getRenderManager().getEntityRenderObject(this);
      if (var2 == null) {
         return null;
      } else if (!(var2 instanceof GirlRenderer)) {
         return null;
      } else {
         GeoEntityRenderer var3 = (GeoEntityRenderer)var2;
         GeoModelProvider var4 = var3.getGeoModelProvider();
         if (var4 == null) {
            return null;
         } else {
            return !(var4 instanceof AnimatedGeoModel) ? null : (AnimatedGeoModel)var4;
         }
      }
   }

   public AnimationProcessor<?> getAnimationProcessor() {
      return this.a_clash551().getAnimationProcessor();
   }

   public boolean h(int var1) {
      ArrayList var2 = this.D_clash243();
      return var2.size() - 1 < var1 ? false : (Integer)var2.get(var1) == 101;
   }

   public e1 g(int var1) {
      return e1.a;
   }

   public void a_clash245(List<Integer> var1) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         StringBuilder var2 = new StringBuilder();

         for (int var4 : var1) {
            AbstractNpcOnlyEntity.c(var2, var4);
         }

         this.m.set(AbstractNpcOnlyEntity.M, var2.toString());
      }
   }

   public String F_clash553() {
      return !(this instanceof AbstractNpcOnlyEntity) && !(this instanceof AbstractKoboldPlayerEntity)
         ? ""
         : (String)this.m.get(AbstractNpcOnlyEntity.M);
   }

   public static String c(List<Integer> var0) {
      StringBuilder var1 = new StringBuilder();

      for (int var3 : var0) {
         var1.append(var3);
         var1.append("-");
      }

      return var1.toString();
   }

   public static List<Integer> c_clash554(String var0) {
      ArrayList var1 = new ArrayList();
      String[] var2 = var0.split("-");

      for (String var6 : var2) {
         var1.add(Integer.parseInt(var6));
      }

      return var1;
   }

   public static List<Integer> h_clash555(UUID var0) {
      BaseGirlEntity var1 = null instanceof ClientProxy ? getClientGirlEntity(var0) : getServerGirlEntity(var0);
      ArrayList var2 = new ArrayList<>(var1.L_clash353());
      if (var1 instanceof AbstractNpcOnlyEntity || var1 instanceof AbstractKoboldPlayerEntity) {
         var2.addAll(c_clash554((String)var1.getDataManager().get(AbstractNpcOnlyEntity.M)));
      }

      return var2;
   }

   public ArrayList<Integer> L_clash353() {
      return new ArrayList<>();
   }

   public List<Entry<BoneType, Entry<List<String>, Integer>>> d_clash556(UUID var1) {
      if (this.d != null) {
         return this.d;
      }

      ArrayList var2 = this.D_clash243();
      if (var2.isEmpty()) {
         this.d = new ArrayList<>();
         return this.d;
      }

      ArrayList var3 = new ArrayList();
      List var4 = h_clash555(var1);

      for (int var5 = 0; var5 < var2.size(); var5++) {
         var3.add(new SimpleEntry<>(BoneType.GIRL_SPECIFIC, new SimpleEntry<>(this.e((Integer)var2.get(var5)), var4.get(var5))));
      }

      this.d = var3;
      return var3;
   }

   public void b(List<Entry<BoneType, Entry<List<String>, Integer>>> var1) {
      this.d = var1;
   }

   public void a_clash557(int var1, int var2) {
      if (this.d != null) {
         if (this.d.size() - 1 >= var1) {
            Entry var3 = this.d.get(var1);
            ((Entry)var3.getValue()).setValue(var2);
            this.d.set(var1, var3);
         }
      }
   }

   public void e_clash558(String var1) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         this.m.set(AbstractNpcOnlyEntity.M, var1);
      }
   }

   private List<String> e(int var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = 0; var3 < var1; var3++) {
         var2.add("");
      }

      return var2;
   }

   public ArrayList<Integer> D_clash243() {
      return new ArrayList<>();
   }

   public List<Integer> u_clash244() {
      return new ArrayList<>();
   }

   public void setCustomModelCode(String var1) {
      this.m.set(b, var1);
   }

   public String getCustomModelCode() {
      return (String)this.m.get(b);
   }

   public static String encodeCustomParts(HashSet<String> var0) {
      if (var0 == null) {
         return "";
      }

      if (var0.isEmpty()) {
         return "";
      }

      StringBuilder var1 = new StringBuilder();

      for (String var3 : var0) {
         var1.append(var3);
         var1.append("#");
      }

      return var1.toString();
   }

   public HashSet<String> getCustomPartsSet() {
      String var1 = this.getCustomModelCode();
      String[] var2 = var1.split("#");
      HashSet var3 = new HashSet();

      for (String var7 : var2) {
         if (!"".equals(var7) && !"cross".equals(var7)) {
            var3.add(var7);
         }
      }

      return var3;
   }

   @SideOnly(Side.CLIENT)
   public boolean H_clash562() {
      return true;
   }

   public enum BaseGirlEntityState {
      WALK,
      FAST_WALK,
      RUN;
   }
}
