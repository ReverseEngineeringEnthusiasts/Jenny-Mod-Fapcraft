package com.trolmastercard.sexmod.client.render.base;

import com.trolmastercard.sexmod.client.render.entity.AllieRenderer;
import com.trolmastercard.sexmod.client.render.entity.BiaRenderer;
import com.trolmastercard.sexmod.client.render.entity.LunaRenderer;
import com.trolmastercard.sexmod.client.render.entity.SlimeRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerAllieRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerBeeRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerBiaRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerEllieRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerGalathRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerGoblinRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerJennyRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerKoboldRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerLunaRenderer;
import com.trolmastercard.sexmod.client.render.player.PlayerSlimeRenderer;

import com.trolmastercard.sexmod.client.model.entity.AllieNpcModel;
import com.trolmastercard.sexmod.client.model.entity.BeeNpcModel;
import com.trolmastercard.sexmod.client.model.entity.BiaNpcModel;
import com.trolmastercard.sexmod.client.model.entity.CatNpcModel;
import com.trolmastercard.sexmod.client.model.entity.EllieNpcModel;
import com.trolmastercard.sexmod.client.model.entity.GalathNpcModel;
import com.trolmastercard.sexmod.client.model.entity.GoblinNpcModel;
import com.trolmastercard.sexmod.client.model.entity.JennyNpcModel;
import com.trolmastercard.sexmod.client.model.entity.KoboldEggModel;
import com.trolmastercard.sexmod.client.model.entity.KoboldNpcModel;
import com.trolmastercard.sexmod.client.model.entity.ManglelieNpcModel;
import com.trolmastercard.sexmod.client.model.entity.ScenePartModel;
import com.trolmastercard.sexmod.client.model.entity.SlimeNpcModel;
import com.trolmastercard.sexmod.client.render.entity.PyrocynicalRenderer;
import com.trolmastercard.sexmod.client.render.entity.BeeRenderer;
import com.trolmastercard.sexmod.client.render.entity.DragonChargeRenderer;
import com.trolmastercard.sexmod.client.render.entity.EllieRenderer;
import com.trolmastercard.sexmod.client.render.entity.GalathRenderer;
import com.trolmastercard.sexmod.client.render.base.GirlRenderer;
import com.trolmastercard.sexmod.client.render.entity.GoblinRenderer;
import com.trolmastercard.sexmod.client.render.entity.JennyRenderer;
import com.trolmastercard.sexmod.client.render.entity.KoboldEggRenderer;
import com.trolmastercard.sexmod.client.render.entity.KoboldRenderer;
import com.trolmastercard.sexmod.client.render.entity.ManglelieRenderer;
import com.trolmastercard.sexmod.client.render.entity.LunaHookRenderer;
import com.trolmastercard.sexmod.client.render.entity.ScenePartRenderer;
import com.trolmastercard.sexmod.client.render.entity.WildSlimeRenderer;
import com.trolmastercard.sexmod.common.entity.girl.AllieEntity;
import com.trolmastercard.sexmod.common.entity.player.AlliePlayerEntity;
import com.trolmastercard.sexmod.common.entity.girl.PyrocynicalEntity;
import com.trolmastercard.sexmod.common.entity.girl.BeeEntity;
import com.trolmastercard.sexmod.common.entity.player.BeePlayerEntity;
import com.trolmastercard.sexmod.common.entity.girl.BiaEntity;
import com.trolmastercard.sexmod.common.entity.player.BiaPlayerEntity;
import com.trolmastercard.sexmod.common.entity.galath.DragonChargeEntity;
import com.trolmastercard.sexmod.common.entity.girl.EllieEntity;
import com.trolmastercard.sexmod.common.entity.player.ElliePlayerEntity;
import com.trolmastercard.sexmod.common.entity.galath.GalathEntity;
import com.trolmastercard.sexmod.common.entity.galath.GalathPlayerEntity;
import com.trolmastercard.sexmod.common.entity.goblin.GoblinEntity;
import com.trolmastercard.sexmod.common.entity.goblin.GoblinPlayerEntity;
import com.trolmastercard.sexmod.common.entity.girl.JennyEntity;
import com.trolmastercard.sexmod.common.entity.player.JennyPlayerEntity;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldEggEntity;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldEntity;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldPlayerEntity;
import com.trolmastercard.sexmod.common.entity.girl.LunaEntity;
import com.trolmastercard.sexmod.common.entity.player.LunaPlayerEntity;
import com.trolmastercard.sexmod.common.entity.girl.ManglelieEntity;
import com.trolmastercard.sexmod.common.entity.girl.LunaHookEntity;
import com.trolmastercard.sexmod.common.entity.base.SceneModelAnchorEntity;
import com.trolmastercard.sexmod.common.entity.girl.SlimeEntity;
import com.trolmastercard.sexmod.common.entity.player.SlimePlayerEntity;
import com.trolmastercard.sexmod.common.entity.girl.WildSlimeEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

/**
 * Registers all entity renderers with the RenderManager (CLIENT).
 */
public class RenderHandler {
   public static void register() {
      // skin cache must not survive a session change (players can change skins)
      MinecraftForge.EVENT_BUS.register(GirlRenderer.SkinCacheInvalidator.INSTANCE);
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
      RenderingRegistry.registerEntityRenderingHandler(LunaHookEntity.class, LunaHookRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(SceneModelAnchorEntity.class, entity -> new ScenePartRenderer(entity, new ScenePartModel()));
      RenderingRegistry.registerEntityRenderingHandler(DragonChargeEntity.class, DragonChargeRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(PyrocynicalEntity.class, PyrocynicalRenderer::new);
   }
}
