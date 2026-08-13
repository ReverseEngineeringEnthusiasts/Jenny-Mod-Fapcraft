package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.model.AllieNpcModel;
import com.trolmastercard.sexmod.client.model.BeeNpcModel;
import com.trolmastercard.sexmod.client.model.BiaNpcModel;
import com.trolmastercard.sexmod.client.model.CatNpcModel;
import com.trolmastercard.sexmod.client.model.EllieNpcModel;
import com.trolmastercard.sexmod.client.model.GalathNpcModel;
import com.trolmastercard.sexmod.client.model.GoblinNpcModel;
import com.trolmastercard.sexmod.client.model.JennyNpcModel;
import com.trolmastercard.sexmod.client.model.KoboldEggModel;
import com.trolmastercard.sexmod.client.model.KoboldNpcModel;
import com.trolmastercard.sexmod.client.model.ManglelieNpcModel;
import com.trolmastercard.sexmod.client.model.SexSceneModel;
import com.trolmastercard.sexmod.client.model.SlimeNpcModel;
import com.trolmastercard.sexmod.client.renderer.BasicGirlRenderer;
import com.trolmastercard.sexmod.client.renderer.BeeRenderer;
import com.trolmastercard.sexmod.client.renderer.DragonRenderer;
import com.trolmastercard.sexmod.client.renderer.EllieRenderer;
import com.trolmastercard.sexmod.client.renderer.GalathRenderer;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.client.renderer.JennyRenderer;
import com.trolmastercard.sexmod.client.renderer.KoboldEggRenderer;
import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.client.renderer.ManglelieRenderer;
import com.trolmastercard.sexmod.client.renderer.SexEntityRenderer;
import com.trolmastercard.sexmod.client.renderer.SexSceneRenderer;
import com.trolmastercard.sexmod.client.renderer.WildSlimeRenderer;
import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.AlliePlayerEntity;
import com.trolmastercard.sexmod.entity.BasicGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntity;
import com.trolmastercard.sexmod.entity.BeePlayerEntity;
import com.trolmastercard.sexmod.entity.BiaEntity;
import com.trolmastercard.sexmod.entity.BiaPlayerEntity;
import com.trolmastercard.sexmod.entity.DragonEntity;
import com.trolmastercard.sexmod.entity.EllieEntity;
import com.trolmastercard.sexmod.entity.ElliePlayerEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.GalathPlayerEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.JennyEntity;
import com.trolmastercard.sexmod.entity.JennyPlayerEntity;
import com.trolmastercard.sexmod.entity.KoboldEggEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.KoboldPlayerEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.LunaPlayerEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.entity.SexEntity;
import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.entity.SlimeEntity;
import com.trolmastercard.sexmod.entity.SlimePlayerEntity;
import com.trolmastercard.sexmod.entity.WildSlimeEntity;







import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class fk {
   public static void register() {
      RenderingRegistry.registerEntityRenderingHandler(KoboldEntity.class, var0 -> new KoboldRenderer(var0, new KoboldNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(JennyEntity.class, var0 -> new JennyRenderer(var0, new JennyNpcModel(), -0.15));
      RenderingRegistry.registerEntityRenderingHandler(EllieEntity.class, var0 -> new EllieRenderer(var0, new EllieNpcModel(), 0.05));
      RenderingRegistry.registerEntityRenderingHandler(SlimeEntity.class, var0 -> new d1(var0, new SlimeNpcModel(), -0.2));
      RenderingRegistry.registerEntityRenderingHandler(BiaEntity.class, var0 -> new dt(var0, new BiaNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(AllieEntity.class, var0 -> new d8(var0, new AllieNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(BeeEntity.class, var0 -> new BeeRenderer(var0, new BeeNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(WildSlimeEntity.class, WildSlimeRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(LunaEntity.class, var0 -> new dp(var0, new CatNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(GoblinEntity.class, var0 -> new GoblinRenderer(var0, new GoblinNpcModel(), -0.6));
      RenderingRegistry.registerEntityRenderingHandler(GalathEntity.class, var0 -> new GalathRenderer(var0, new GalathNpcModel(), -0.05));
      RenderingRegistry.registerEntityRenderingHandler(KoboldEggEntity.class, var0 -> new KoboldEggRenderer(var0, new KoboldEggModel()));
      RenderingRegistry.registerEntityRenderingHandler(ManglelieEntity.class, var0 -> new ManglelieRenderer(var0, new ManglelieNpcModel(), -0.05));
      RenderingRegistry.registerEntityRenderingHandler(BiaPlayerEntity.class, var0 -> new d0(var0, new BiaNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(JennyPlayerEntity.class, var0 -> new db(var0, new JennyNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(ElliePlayerEntity.class, var0 -> new dl(var0, new EllieNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(SlimePlayerEntity.class, var0 -> new d5(var0, new SlimeNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(AlliePlayerEntity.class, var0 -> new dv(var0, new AllieNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(BeePlayerEntity.class, var0 -> new d2(var0, new BeeNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(LunaPlayerEntity.class, var0 -> new di(var0, new CatNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(KoboldPlayerEntity.class, var0 -> new de(var0, new KoboldNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(GoblinPlayerEntity.class, var0 -> new dg(var0, new GoblinNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(GalathPlayerEntity.class, var0 -> new dx(var0, new GalathNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(SexEntity.class, SexEntityRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(SexSceneEntity.class, var0 -> new SexSceneRenderer(var0, new SexSceneModel()));
      RenderingRegistry.registerEntityRenderingHandler(DragonEntity.class, DragonRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(BasicGirlEntity.class, BasicGirlRenderer::new);
   }
}
