package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UpdatePlayerModelPacket;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Girl-model selection screen (the "pick a girl" GUI): cycles through previews
 * of every non-NPC girl type plus the player's own model, and sends the chosen
 * type to the server via {@link UpdatePlayerModelPacket} — this is the
 * horny-potion girl transformation picker.
 * <p>
 * <b>Preview construction.</b> For each {@link NpcType} (skipping
 * {@code isNpcOnly}) a {@link BaseGirlEntity} is constructed via reflection
 * into the client world, marked locally registered, and given the player's
 * custom part list; previews rotate in a static spin animation.
 * <p>
 * CLIENT-side only. On "pick", the eye height and flying capability of the
 * local player are restored to defaults before the screen closes.
 */
public class GirlScreenBase extends GuiScreen {
   List<EntityLivingBase> nearbyEntities = new ArrayList<>();
   int renderIndex = 0;
   static float progress = 0.0F;

   public GirlScreenBase(HashMap<NpcType, String> modelCodes) {
      this.mc = Minecraft.getMinecraft();

      for (NpcType npcType : NpcType.values()) {
         if (!npcType.isNpcOnly) {
            try {
               Constructor constructor = npcType.npcClass.getConstructor(World.class);
               BaseGirlEntity entity = (BaseGirlEntity)constructor.newInstance(this.mc.world);
               entity.setLocallyRegistered(true);
               this.nearbyEntities.add(entity);
               String parts = (String)modelCodes.get(npcType);
               if (parts != null) {
                  entity.setCustomPartList(BaseGirlEntity.decodePartIdList(parts));
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      this.nearbyEntities.add(this.mc.player);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.buttonList.clear();
      renderEntityPreview(this.width / 2, this.height / 2 + 20, 30, this.nearbyEntities.get(this.renderIndex));
      this.buttonList.add(new GuiButton(1, this.width / 2 + 30, this.height / 2 - 10, 20, 20, ">"));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 50, this.height / 2 - 10, 20, 20, "<"));
      this.buttonList.add(new GuiButton(0, this.width / 2 - 30, this.height / 2 + 30, 60, 20, "pick"));
   }

   /**
    * "Pick" commits the currently previewed type: sends
    * {@link UpdatePlayerModelPacket} to the server and restores the player's
    * eye height and flight capability. "<"/">" cycle the preview index
    * (wrapping).
    */
   protected void actionPerformed(GuiButton button) {
      if (">".equals(button.displayString) && ++this.renderIndex >= this.nearbyEntities.size()) {
         this.renderIndex = 0;
      }

      if ("<".equals(button.displayString) && --this.renderIndex < 0) {
         this.renderIndex = this.nearbyEntities.size() - 1;
      }

      if (button.id == 0) {
         PacketHandler.networkWrapper.sendToServer(new UpdatePlayerModelPacket(NpcType.getNpcType((Entity)this.nearbyEntities.get(this.renderIndex))));
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         player.closeScreen();
         player.eyeHeight = player.getDefaultEyeHeight();
         if (!player.capabilities.allowFlying) {
            player.capabilities.allowFlying = player.capabilities.isCreativeMode;
         }
      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   /**
    * Renders an entity as a rotating inventory-style preview at the given
    * screen position. Temporarily overrides the entity's yaw/pitch/head
    * rotation and (for non-players) its position, renders via the render
    * manager with the spin angle accumulated from real elapsed time, then
    * restores every overridden field. The static {@code progress} makes all
    * previews rotate in sync.
    */
   public static void renderEntityPreview(int x, int y, int scale, EntityLivingBase entity) {
      float renderYawOffset = entity.renderYawOffset;
      float rotationYaw = entity.rotationYaw;
      float rotationPitch = entity.rotationPitch;
      float prevRotationYawHead = entity.prevRotationYawHead;
      float rotationYawHead = entity.rotationYawHead;
      if (!(entity instanceof EntityPlayer)) {
         entity.posX = 0.0;
         entity.posY = 0.0;
         entity.posZ = 0.0;
      }

      entity.renderYawOffset = 0.0F;
      entity.rotationYaw = 0.0F;
      entity.rotationPitch = 0.0F;
      entity.prevRotationYawHead = 0.0F;
      entity.rotationYawHead = 0.0F;
      float fps = Minecraft.getDebugFPS();
      if (fps == 0.0F) {
         fps = 0.1F;
      }

      progress += 60.0F / fps;
      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 50.0F);
      GlStateManager.scale(-scale, scale, scale);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(progress, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, 0.0F);
      RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
      renderManager.setPlayerViewY(180.0F);
      renderManager.setRenderShadow(false);
      renderManager.renderEntity(entity, 0.0, 0.0, 0.0, 0.0F, 1.2345679F, false);
      renderManager.setRenderShadow(true);
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      entity.renderYawOffset = renderYawOffset;
      entity.rotationYaw = rotationYaw;
      entity.rotationPitch = rotationPitch;
      entity.prevRotationYawHead = prevRotationYawHead;
      entity.rotationYawHead = rotationYawHead;
   }

}
