package com.trolmastercard.sexmod.util;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * <b>Role.</b> The in-game guide: a vanilla written book handed to a player on
 * the first join of a save (tracked per world by {@link GuideBookData}). It
 * covers the courting loop and payment, the horny potion, the
 * {@code sexmod/player_skin/} override, the girl wand, custom models and the
 * dragon staff.
 * <p>
 * A plain {@code minecraft:written_book} is deliberate: no item registration,
 * texture, model or lang key, and it works in every 1.12.2 instance.
 * <p>
 * <b>Page layout.</b> Each {@link #pages()} entry is one book page. The vanilla
 * book renders about 19 characters per line and 13 lines per page, so keep
 * pages near 250 characters and use explicit newlines. Keep the text ASCII and
 * free of em dashes.
 * <p>
 * SERVER-side for {@link #giveOnce(EntityPlayer)}; {@link #create()} is safe on
 * both sides.
 */
public final class GuideBook {
   private static final String TITLE = "Fapcraft Guide";
   private static final String AUTHOR = "Fapcraft";

   private GuideBook() {
   }

   /**
    * Hands the guide to {@code player} once per save. Does nothing on the
    * client or when this player already received it.
    */
   public static void giveOnce(EntityPlayer player) {
      if (player == null || player.world == null || player.world.isRemote) {
         return;
      }

      UUID playerId = player.getPersistentID();
      GuideBookData data = GuideBookData.get(player.world);
      if (data.hasReceived(playerId)) {
         return;
      }

      data.markReceived(playerId);
      ItemStack book = create();
      if (!player.inventory.addItemStackToInventory(book)) {
         player.dropItem(book, false);
      }
   }

   /** The written book stack, with one NBT page per {@link #pages()} entry. */
   public static ItemStack create() {
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NBTTagCompound tag = new NBTTagCompound();
      tag.setString("title", TITLE);
      tag.setString("author", AUTHOR);

      NBTTagList pageList = new NBTTagList();
      for (String page : pages()) {
         pageList.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(new TextComponentString(page))));
      }

      tag.setTag("pages", pageList);
      book.setTagCompound(tag);
      return book;
   }

   /** Book pages, in order. One string per page. */
   public static String[] pages() {
      return new String[]{
         "FAPCRAFT GUIDE\n\nRight-click a girl to open her menu.\n\nPick an action and pay the price on the button.\n\nPress RShift to leave a scene.",
         "COURTING\n\nJenny takes emeralds, pearls, diamonds or gold.\n\nKeep a stack of each and you can court any girl.\n\nSome girls have their own menu.",
         "HORNY POTION\n\nMundane potion plus a poppy.\n\nGirls skip the payment while it lasts.\n\nFive seconds later the girl picker opens.",
         "CUSTOM SKIN\nPut a square PNG in .minecraft/sexmod/player_skin/\nsteve.png = wide arms\nalex.png = slim arms\n64x64 or bigger. Rejoin after adding it.",
         "GIRL WAND\nRight-click a girl to open her editor.\nChange parts, clothes and colors.\nLeft-click a girl to copy her model code.\nThe folder button opens the model folder.",
         "CUSTOM MODELS\nModels live in .minecraft/sexmod/custom_models/singleplayer/\nOpen the girl wand with no models and press Download.\nOr drop a pack in that folder and reopen the wand.",
         "DRAGON STAFF\nRight-click opens tribe commands.\nNeeds a kobold tribe.\nMark beds and chests for your kobolds.\nThe staff never places blocks.",
         "OTHER ITEMS\nAllies Lamp: summon Allie, three wishes.\nLuna Rod: fish with Luna.\nGalath Coin: summon your succubus.\nAll are in the creative Tools tab."
      };
   }
}
