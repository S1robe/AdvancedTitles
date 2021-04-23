package me.strobe.titles.utils;

import me.strobe.titles.config.Account;
import me.strobe.titles.main.Titles;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static ItemStack titleItem(String name, String data){
        List<Text> lore = Collections.singletonList(Text.of(TextColors.DARK_GRAY, "Title: " + data));
        return ItemStack.builder()
                .itemType(ItemTypes.BOOK)
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_PURPLE, name))
                .keyValue(Keys.ITEM_LORE, lore)
                .build();
    }

    public ItemStack fillerItem(String name, String data){
        return ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_GRAY, "???"))
                .build();
    }
    public ItemStack backItem(){
        return ItemStack.builder()
                .itemType(ItemTypes.STICK)
                .keyValue(Keys.DISPLAY_NAME,Text.of(
                        TextColors.DARK_GRAY, "Previous Page"))
                .build();
    }
    public ItemStack forwardItem(){
        return ItemStack.builder()
                .itemType(ItemTypes.BONE)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.GREEN, "Next Page"))
                .build();
    }

    public void fillOpenSlots(ItemStack item, Inventory inv){
        inv.query(new SlotPos(1, 1)).set(item);
    }

   public Inventory createGUI(Account acct){
       Inventory x = Inventory.builder()
               .property(
                       "title", new InventoryTitle(
                               Text.of("Your Titles")))
               .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, 3))
               .build(Titles.getInstance());
       List<String> playerTitles = acct.getPlayerUnlockedTitles();
       ArrayList<ItemStack> formattedItems = new ArrayList<>();
       playerTitles.forEach(s -> {
           titleItem(s, Titles.getInstance().getGlobalTitleList().get(s));
       });
   }

    public static Text colorizer(String message){
        return Text.of(message.replaceAll("&([0-9a-fA-Frolmnk]){1}", "§$1"));
    }


}
