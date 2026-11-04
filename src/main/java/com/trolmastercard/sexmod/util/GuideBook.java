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
   /** Bump when the page set changes so existing saves get the expanded book once. */
   private static final int VERSION = 2;
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
      if (data.hasReceived(playerId) && data.version() >= VERSION) {
         return;
      }

      data.markReceived(playerId, VERSION);
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
         "FAPCRAFT GUIDE\nRight-click a girl to open her menu.\nPick an action and pay the price on the button.\nG goblin, L customise, RShift leave a scene.",
         "YOUR GIRL\nFollow me makes her yours.\nShe follows and fights nearby mobs.\nStop following sends her home.\nGo home teleports her home.\nSet new home moves it here.",
         "EQUIPMENT\nOpen her menu, then Equipment.\nWeapon, bow, helmet, chestplate, leggings, boots.\nShe uses the weapon up close and the bow at range.\nArmour shows on her body.",
         "COURTING\nJenny takes emeralds, pearls, diamonds or gold.\nKeep a stack of each and you can court any girl.\nSome girls have their own menu.",
         "HORNY POTION\nMundane potion plus a poppy.\nGirls skip the payment while it lasts.\nFive seconds later the girl picker opens.\nPick a body there to play as a girl.",
         "CUSTOM SKIN\nPut a square PNG in .minecraft/sexmod/player_skin/\nsteve.png = wide arms\nalex.png = slim arms\n64x64 or bigger. Rejoin after adding it.",
         "GIRL WAND\nRight-click a girl to open her editor.\nChange parts, clothes and colors.\nLeft-click a girl to copy her model code.\nThe folder button opens the model folder.",
         "WARDROBE\nIn the girl wand press K for reskin packs.\nPick a pack and apply it to the girl.\nLegacy opens the old wardrobe with sliders.\nPacks are remembered per world.",
         "CUSTOM MODELS\nModels live in .minecraft/sexmod/custom_models/singleplayer/\nOpen the girl wand with no models and press Download.\nOr drop a pack in that folder and reopen the wand.",
         "KOBOLDS\nTribes spawn with the world. Find one.\nRight-click with the dragon staff to claim it.\nClaimed kobolds follow and work for you.\nMark beds and chests for them with the staff.",
         "DRAGON STAFF\nRight-click opens the command pad.\nLeft: mark beds and chests.\nTop left: follow mode.\nTop right: fell a tree or mine a block.\nIt also hands out tribe eggs.",
         "GOBLINS\nRight-click a goblin to interact.\nPress G to open the goblin pad when one rides you.\nRShift opens the same pad.\n/locatenearestgoblinlair finds a queen lair.",
         "OTHER ITEMS\nAllies Lamp: summon Allie, three wishes.\nLuna Rod: fish with Luna.\nGalath Coin: beat the succubus, corrupt her, then summon her.\nRight-click her with the coin to send her back.",
         "TUNING\n/futa true or false toggles the Galath futa parts.\n/whitelistserver lets a server push models.\nconfig/sexmod.json: worldgen, model cache, flying.\n/reloadcustommodels (op) reloads server models."
      };
   }
}
