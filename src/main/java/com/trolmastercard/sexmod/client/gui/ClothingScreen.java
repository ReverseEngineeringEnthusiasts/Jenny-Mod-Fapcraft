package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadModelStringPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SceneDebug;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.Point2D;
import java.io.IOException;
import java.awt.Desktop;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The girl wand's customization GUI — cycles the target girl's custom model
 * parts (bone toggles with {@code <}/{@code >} and {@code +}/{@code -}),
 * rotates the preview model, and hosts the custom-model folder/tutorial
 * buttons. Opened via {@link #openClothingScreen(BaseGirlEntity)} (the K key
 * while transformed, or right-clicking a girl with the wand).
 * <p>
 * <b>Pitfall (crash fix):</b> the folder button MUST check
 * {@code exists()}/{@code mkdirs()} before {@code java.awt.Desktop.open()} —
 * opening the not-yet-existing {@code sexmod/custom_models/singleplayer}
 * directory throws {@code IllegalArgumentException} (NOT IOException) and
 * crashes the whole client.
 */
public class ClothingScreen extends GuiScreen {
   public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/clothing_icons.png");
   static final int SCROLLBAR_SIZE = 20;
   static final float SCROLL_SPEED = 0.25F;
   int guiX = 0;
   int guiY = 0;
   float modelRotation = 0.0F;
   public static float currentModelYaw = 0.0F;
   protected static List<Integer> selectedPartIds = new ArrayList<>();
   protected static int scrollOffset = 0;
   protected static int targetScrollOffset = 0;
   BaseGirlEntity previewGirl;
   boolean isRendering = false;
   CustomModelList modelList;
   public static List<Entry<BoneType, Entry<List<String>, Integer>>> m = new ArrayList<>();
   final UUID girlId;
   int partsCount;
   int lastMouseX;
   public boolean isEditing = false;
   int scrollVelocity = 0;
   int scrollDirection = 1;

   public ClothingScreen(@Nonnull BaseGirlEntity girl) {
      this.mc = Minecraft.getMinecraft();
      this.girlId = girl.getGirlId();
      NpcType npcType = NpcType.getNpcType(girl);
      if (npcType == null) {
         npcType = NpcType.JENNY;
      }

      try {
         Constructor constructor = npcType.npcClass.getConstructor(World.class);
         this.previewGirl = (BaseGirlEntity)constructor.newInstance(this.mc.world);
         this.previewGirl.setLocallyRegistered(true);
      } catch (Exception e) {
         e.printStackTrace();
      }

      this.refreshCustomParts();
      String modelCode = girl.getCustomModelCode();
      this.previewGirl.getDataManager().set(BaseGirlEntity.CUSTOM_MODEL_KEY, modelCode);
      int customBoneCount = 0;

      for (String partName : this.previewGirl.getCustomPartsSet()) {
         BoneType boneType = ServerWhitelistManager.getBoneType(partName);
         if (BoneType.CUSTOM_BONE.equals(boneType)) {
            customBoneCount++;
         }

         Entry entry = null;
         if (BoneType.CUSTOM_BONE.equals(boneType) && customBoneCount > 1) {
            entry = getCustomPartData(this.previewGirl);
         } else {
            for (Entry existingEntry : m) {
               if (((BoneType)existingEntry.getKey()).equals(boneType)) {
                  entry = existingEntry;
               }
            }
         }

         if (entry != null) {
            m.remove(entry);
            int index = ((List)((Entry)entry.getValue()).getKey()).indexOf(partName);
            if (index == -1) {
               index = 0;
            }

            ((Entry)entry.getValue()).setValue(index);
            m.add(entry);
         }
      }
   }

   public void handleMouseInput() {
      super.handleMouseInput();
      this.modelList.handleMouseInput();
   }

   public static HashSet<String> getCustomBoneNames() {
      HashSet names = new HashSet();

      for (Entry entry : m) {
         if (((List)((Entry)entry.getValue()).getKey()).size() != 1) {
            Entry data = (Entry)entry.getValue();
            List parts = (List)data.getKey();
            Integer index = (Integer)data.getValue();
            names.add(parts.get(index));
         }
      }

      return names;
   }

   public static Entry<BoneType, Entry<List<String>, Integer>> getCustomPartData(BaseGirlEntity girl) {
      ArrayList parts = new ArrayList();
      parts.add("cross");
      parts.addAll(ServerWhitelistManager.getModelParts(girl).get(BoneType.CUSTOM_BONE));
      return new SimpleEntry<>(BoneType.CUSTOM_BONE, new SimpleEntry<>(parts, 0));
   }

   void refreshCustomParts() {
      m.clear();
      List customParts = this.previewGirl.buildCustomPartsData(this.girlId);
      this.partsCount = customParts.size();
      m.addAll(customParts);

      for (BoneType boneType : BoneType.values()) {
         if (boneType != BoneType.GIRL_SPECIFIC) {
            ArrayList parts = new ArrayList();
            parts.add("cross");
            m.add(new SimpleEntry<>(boneType, new SimpleEntry<>(parts, 0)));
         }
      }

      for (Entry modelParts : ServerWhitelistManager.getModelParts(this.previewGirl).entrySet()) {
         Entry entry = null;

         for (Entry existingEntry : m) {
            if (((BoneType)modelParts.getKey()).equals(existingEntry.getKey())) {
               entry = existingEntry;
            }
         }

         if (entry != null) {
            int index = m.indexOf(entry);
            m.remove(entry);
            ((List)((Entry)entry.getValue()).getKey()).addAll((Collection)modelParts.getValue());
            m.add(index, entry);
         }
      }
   }

   public void initGui() {
      this.modelList = new CustomModelList(this.mc, this);
   }

   public void setWorldAndResolution(Minecraft mc, int width, int height) {
      super.setWorldAndResolution(mc, width, height);
      this.guiX = this.screenX(76.0F);
      this.guiY = this.screenY(89.0F);
      this.modelRotation = 90.0F;
   }

   boolean isMouseOverPart(int mouseX, int mouseY, int left, int top, int right, int bottom) {
      if (mouseX < left) {
         return false;
      } else if (mouseX > right) {
         return false;
      } else {
         return mouseY < top ? false : mouseY <= bottom;
      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      if (this.isRendering) {
         currentModelYaw = currentModelYaw + RotationHelper.lerp(targetScrollOffset, scrollOffset, partialTicks);
      }

      this.startRendering();
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      int x = this.guiX - this.screenX(15.0F);
      int y = this.guiY - 20;
      this.drawTexturedModalRect(x, y, 100, this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20) ? 40 : 20, 20, 20);
      if (ServerWhitelistManager.getCustomModelsKey() == null) {
         this.drawBackground(x, mouseX, mouseY);
      }

      this.drawPartRotated(this.guiX, this.guiY, this.modelRotation, this.previewGirl, 1.2345679F);
      this.previewGirl.onUpdate();
      this.modelList.drawScreen(mouseX, mouseY, partialTicks);
   }

   void drawBackground(int x, int mouseX, int mouseY) {
      int y = this.guiY - 40;
      this.drawTexturedModalRect(x, y, 120, this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20) ? 40 : 20, 20, 20);
      y -= 20;
      this.drawTexturedModalRect(x, y, 20, this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20) ? 170 : 150, 20, 20);
      y -= 20;
      this.drawTexturedModalRect(x, y, 0, this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20) ? 170 : 150, 20, 20);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   void playClickSound() {
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      HashSet selectedParts = new HashSet();
      ArrayList girlSpecific = new ArrayList();

      for (Entry entry : m) {
         if (entry.getKey() == BoneType.GIRL_SPECIFIC) {
            girlSpecific.add(((Entry)entry.getValue()).getValue());
         } else {
            Entry data = (Entry)entry.getValue();
            Integer index = (Integer)data.getValue();
            if (index != 0) {
               String partName = (String)((List)data.getKey()).get(index);
               selectedParts.add(partName);
            }
         }
      }

      PacketHandler.networkWrapper.sendToServer(new UploadModelStringPacket(BaseGirlEntity.encodeCustomParts(selectedParts), this.girlId, girlSpecific));
      this.mc.player.closeScreen();
   }

   public void onBoneTypeToggle(BoneType boneType, boolean forward, int clickIndex) {
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      ArrayList matches = new ArrayList();
      ArrayList indices = new ArrayList();
      int i = 0;

      for (Entry entry : m) {
         if (((BoneType)entry.getKey()).equals(boneType)) {
            matches.add(entry);
            indices.add(i);
         }

         i++;
      }

      if (matches.size() != 0) {
         Entry entry;
         int index;
         if (matches.size() == 1) {
            entry = (Entry)matches.get(0);
            index = (Integer)indices.get(0);
         } else {
            int partIndex;
            if (this.partsCount != 0 && clickIndex <= this.partsCount - 1 + BoneType.getCustomBoneCount()) {
               partIndex = clickIndex;
            } else {
               partIndex = clickIndex - (this.partsCount + BoneType.getCustomBoneCount());
            }

            // guard against out-of-range click indices (multi-custom-bone lists)
            partIndex = Math.max(0, Math.min(partIndex, matches.size() - 1));
            entry = (Entry)matches.get(partIndex);
            index = (Integer)indices.get(partIndex);
         }

         if (entry != null) {
            Entry data = (Entry)entry.getValue();
            int currentIndex = (Integer)data.getValue();
            int partCount = ((List)data.getKey()).size();
            if (forward) {
               if (++currentIndex >= partCount) {
                  currentIndex = 0;
               }
            } else if (--currentIndex < 0) {
               currentIndex = partCount - 1;
            }

            m.set(index, new SimpleEntry<>((BoneType)entry.getKey(), new SimpleEntry<>((List<String>)((Entry)entry.getValue()).getKey(), currentIndex)));
            ArrayList girlSpecificEntries = new ArrayList();

            for (Entry girlSpecificEntry : m) {
               if (girlSpecificEntry.getKey() == BoneType.GIRL_SPECIFIC) {
                  girlSpecificEntries.add(girlSpecificEntry);
               }
            }

            this.previewGirl.setCustomPartsData(girlSpecificEntries);
         }
      }
   }

   public void drawPart(int x, int y, float rotation, SexSceneEntity entity) {
      this.drawPartRotated(x, y, rotation, entity, 1.876945F);
   }

   public void drawPreviewModel(SexSceneEntity entity) {
      this.drawPartRotatedScaled(this.guiX, this.guiY, this.modelRotation, entity, 2.876945F, entity.isItemModel ? 1 : 0);
   }

   public void drawHoverText(String text, int x, int y) {
      this.drawHoveringText(text, x, y);
   }

   protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
      super.mouseClickMove(mouseX, mouseY, mouseButton, time);
      if (mouseButton == 0) {
         if (mouseX >= this.width / 2) {
            int deltaX = mouseX - this.lastMouseX;
            selectedPartIds.add(deltaX);
            this.lastMouseX = mouseX;
         }
      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.modelList.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton == 0) {
         this.isEditing = true;
         this.isRendering = true;
         this.lastMouseX = mouseX;
         int x = this.guiX - this.screenX(15.0F);
         int y = this.guiY - 20;
         if (this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20)) {
            this.playClickSound();
         }

         if (ServerWhitelistManager.getCustomModelsKey() == null) {
            y = this.guiY - 40;
            if (this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20)) {
               this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               this.mc.player.closeScreen();
               int modelCount = ServerWhitelistManager.getModelCount(true);
               if (modelCount != 0) {
                  ServerWhitelistManager.isGlobalRenderingDisabled = true;
               } else {
                  BaseGirlEntity girl = BaseGirlEntity.getClientGirlEntity(this.girlId);
                  if (girl != null) {
                     openClothingScreen(girl);
                  }
               }
            } else {
               y -= 20;
               if (this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20)) {
                  File folder = new File(ServerWhitelistManager.getGlobalModelOverride());
                  SceneDebug.log(SceneDebug.CLOTHING, "ClothingScreen: opening folder %s (exists=%s)", folder.getAbsolutePath(), folder.exists());
                  if (folder.exists() || folder.mkdirs()) {
                     if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                        try { Desktop.getDesktop().open(folder); } catch (IOException ioe) { }
                     }
                  }
               } else {
                  y -= 20;
                  if (this.isMouseOverPart(mouseX, mouseY, x, y, x + 20, y + 20)) {
                     if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                           Desktop.getDesktop().browse(new URI("https://web.archive.org/web/20241120012617/http://fapcraft.org/assets/video/tutorial/girl_wand.mp4"));
                        } catch (URISyntaxException urie) {
                           throw new RuntimeException(urie);
                        } catch (IOException ioe) {
                           throw new RuntimeException(ioe);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      super.mouseReleased(mouseX, mouseY, mouseButton);
      if (mouseButton == 0) {
         this.isRendering = false;
         this.isEditing = false;
      }

      this.scrollVelocity = targetScrollOffset;
   }

   int screenX(float percent) {
      return Math.round(this.width * (percent / 100.0F));
   }

   int screenY(float percent) {
      return Math.round(this.height * (percent / 100.0F));
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      this.previewGirl.world.removeEntityDangerously(this.previewGirl);
      selectedPartIds.clear();
      m.clear();
   }

   public BaseGirlEntity getPreviewGirl() {
      return this.previewGirl;
   }

   public void drawPartBackground(int x, int y, int u, int v) {
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      this.drawTexturedModalRect(x, y, u, v, 20, 20);
   }

   public void drawPartIcon(int x, int y, int u) {
      this.drawPartBackground(x, y, u, 0);
   }

   public void drawPartAt(int x, int y, Point2D pos) {
      this.drawPartBackground(x, y, pos.x, pos.y);
   }

   void drawPartRotated(int x, int y, float rotation, EntityLivingBase entity, float scale) {
      this.drawPartRotatedScaled(x, y, rotation, entity, scale, 0);
   }

   void drawPartRotatedScaled(int x, int y, float rotation, EntityLivingBase entity, float scale, int zOffset) {
      float renderYawOffset = entity.renderYawOffset;
      float rotationYaw = entity.rotationYaw;
      float rotationPitch = entity.rotationPitch;
      float prevRotationYawHead = entity.prevRotationYawHead;
      float rotationYawHead = entity.rotationYawHead;
      entity.renderYawOffset = 0.0F;
      entity.rotationYaw = 0.0F;
      entity.rotationPitch = 0.0F;
      entity.prevRotationYawHead = 0.0F;
      entity.rotationYawHead = 0.0F;
      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 50.0F);
      GlStateManager.scale(-rotation, rotation, rotation);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, zOffset);
      GlStateManager.rotate(currentModelYaw, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(0.25F, 1.0F, 0.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, 0.0F);
      RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
      renderManager.setPlayerViewY(180.0F);
      renderManager.setRenderShadow(false);
      renderManager.renderEntity(entity, 0.0, 0.0, 0.0, 0.0F, scale, false);
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

   void startRendering() {
      if (!this.isRendering) {
         float fps = Minecraft.getDebugFPS();
         if (fps == 0.0F) {
            fps = 0.1F;
         }

         if (this.scrollVelocity == 0) {
            currentModelYaw = currentModelYaw + this.scrollDirection * 10 / fps;
         } else {
            currentModelYaw = currentModelYaw + this.scrollVelocity / fps;
            this.scrollVelocity = (int)(this.scrollVelocity * (1.0F - 0.25F / fps));
            if (Math.abs(this.scrollVelocity) <= 10) {
               this.scrollDirection = this.scrollVelocity > 0 ? 1 : -1;
               this.scrollVelocity = 0;
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static void openClothingScreen(@Nonnull BaseGirlEntity girl) {
      Minecraft mc = Minecraft.getMinecraft();
      if (!(mc.currentScreen instanceof ClothingScreen)) {
         boolean canUseCustomModels = ServerWhitelistManager.getCustomModelsKey() == null || ServerWhitelistManager.isGlobalRenderingDisabled();
         if (!canUseCustomModels) {
            mc.player
               .sendStatusMessage(
                  new TextComponentString("You have to whitelist the server to use its custom models. " + TextFormatting.YELLOW + "/whitelistserver"), true
               );
         } else {
            mc.addScheduledTask(() -> mc.displayGuiScreen(new ClothingScreen(girl)));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static class b {
      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void onKeyInput(KeyInputEvent event) {
         if (ClientProxy.keyBindings[1].isPressed()) {
            if (ServerWhitelistManager.isGlobalRenderingDisabled) {
               ServerWhitelistManager.isGlobalRenderingDisabled = 0 != ServerWhitelistManager.getModelCount(true);
               if (ServerWhitelistManager.isGlobalRenderingDisabled) {
                  return;
               }
            }

            Minecraft mc = Minecraft.getMinecraft();
            AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(mc.player.getPersistentID());
            if (playerGirl == null) {
               mc.player.sendStatusMessage(new TextComponentString("You have to turn into the girl you want to customize"), true);
            } else {
               ClothingScreen.openClothingScreen(playerGirl);
            }
         }
      }

      @SubscribeEvent
      @SideOnly(Side.CLIENT)
      public void onClientTick(ClientTickEvent event) {
         ClothingScreen.targetScrollOffset = ClothingScreen.scrollOffset;
         ClothingScreen.scrollOffset = 0;

         for (Integer id : ClothingScreen.selectedPartIds) {
            ClothingScreen.scrollOffset = ClothingScreen.scrollOffset + id;
         }

         ClothingScreen.selectedPartIds.clear();
      }

   }
}
