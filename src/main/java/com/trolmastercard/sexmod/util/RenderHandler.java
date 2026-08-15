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

/**
 * Registers all entity renderers with the RenderManager (CLIENT).
 */
public class RenderHandler {
   public static void register() {
      RenderingRegistry.registerEntityRenderingHandler(KoboldEntity.class, entity -> new KoboldRenderer(entity, new KoboldNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(JennyEntity.class, entity -> new JennyRenderer(entity, new JennyNpcModel(), -0.15));
      RenderingRegistry.registerEntityRenderingHandler(EllieEntity.class, entity -> new EllieRenderer(entity, new EllieNpcModel(), 0.05));
      RenderingRegistry.registerEntityRenderingHandler(SlimeEntity.class, entity -> new SlimeRenderer(entity, new SlimeNpcModel(), -0.2));
      RenderingRegistry.registerEntityRenderingHandler(BiaEntity.class, entity -> new BiaRenderer(entity, new BiaNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(AllieEntity.class, entity -> new AllieRenderer(entity, new AllieNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(BeeEntity.class, entity -> new BeeRenderer(entity, new BeeNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(WildSlimeEntity.class, WildSlimeRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(LunaEntity.class, entity -> new LunaRenderer(entity, new CatNpcModel(), -0.4));
      RenderingRegistry.registerEntityRenderingHandler(GoblinEntity.class, entity -> new GoblinRenderer(entity, new GoblinNpcModel(), -0.6));
      RenderingRegistry.registerEntityRenderingHandler(GalathEntity.class, entity -> new GalathRenderer(entity, new GalathNpcModel(), -0.05));
      RenderingRegistry.registerEntityRenderingHandler(KoboldEggEntity.class, entity -> new KoboldEggRenderer(entity, new KoboldEggModel()));
      RenderingRegistry.registerEntityRenderingHandler(ManglelieEntity.class, entity -> new ManglelieRenderer(entity, new ManglelieNpcModel(), -0.05));
      RenderingRegistry.registerEntityRenderingHandler(BiaPlayerEntity.class, entity -> new PlayerBiaRenderer(entity, new BiaNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(JennyPlayerEntity.class, entity -> new PlayerJennyRenderer(entity, new JennyNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(ElliePlayerEntity.class, entity -> new PlayerEllieRenderer(entity, new EllieNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(SlimePlayerEntity.class, entity -> new PlayerSlimeRenderer(entity, new SlimeNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(AlliePlayerEntity.class, entity -> new PlayerAllieRenderer(entity, new AllieNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(BeePlayerEntity.class, entity -> new PlayerBeeRenderer(entity, new BeeNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(LunaPlayerEntity.class, entity -> new PlayerLunaRenderer(entity, new CatNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(KoboldPlayerEntity.class, entity -> new PlayerKoboldRenderer(entity, new KoboldNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(GoblinPlayerEntity.class, entity -> new PlayerGoblinRenderer(entity, new GoblinNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(GalathPlayerEntity.class, entity -> new PlayerGalathRenderer(entity, new GalathNpcModel()));
      RenderingRegistry.registerEntityRenderingHandler(SexEntity.class, SexEntityRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(SexSceneEntity.class, entity -> new SexSceneRenderer(entity, new SexSceneModel()));
      RenderingRegistry.registerEntityRenderingHandler(DragonEntity.class, DragonRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(BasicGirlEntity.class, BasicGirlRenderer::new);
   }
}
