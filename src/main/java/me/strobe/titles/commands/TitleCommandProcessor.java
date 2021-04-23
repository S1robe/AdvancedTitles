package me.strobe.titles.commands;

import me.strobe.titles.config.Account;
import me.strobe.titles.main.Titles;
import me.strobe.titles.utils.Utils;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TitleCommandProcessor {

    public static String usage = "&c/titles, /title equip <title>, /title off";
    public static String adminUsage = "&c/title create <title>, /title delete <title>, /title reload";
    public static String noPerm = "&cYou do not have permission to use that command!";
    public static String notUnlocked = "&cYou do not have permission to use that Title!";
    public static String title = "&8[&5Titles&8]&r";
    public static String header = "&aYour Titles";


    //main sub command, delegates where o do things
    public static void use(UUID sender, String[] args, Account account) {

        if (args.length == 0) {
            //List current unlocked titles
            list(sender, account);
        } else if(args.length < 4){
            String sub = args[0];
            args = Arrays.copyOfRange(args, 1, args.length-1);
            //title equip <title> length 2
            if(sub.equalsIgnoreCase("equip")){
                equip(sender, args[1], account);
            }
            //Title off length 1
            else if(sub.equalsIgnoreCase("off")){
                remove(sender, account);
            }
            else if(sender.equals(Titles.serverUUID) || Titles.getInstance().hasPerm(sender, "titles.admin")) {
                try {
                    //title create <name> <formatted title> 3
                    if (sub.equalsIgnoreCase("create")) {
                        create(sender, args);
                        //title delete  <name> 2
                    } else if (sub.equalsIgnoreCase("delete")) {
                        delete(sender, args);
                        //title reload  1
                    } else if (sub.equalsIgnoreCase("reload")) {
                        reload(sender);


                        //default action is give which enable the title for the player
                        //title give <name> <title> 3
                    } else if (sub.equalsIgnoreCase("give")) {
                        if (args.length == 3) {
                            giveToPlayer(sender, args[1], args[2], false);
                        } else
                            giveSelf(sender, args[1], false);
                        //title giveItem <name> <title>
                    } else if (sub.equalsIgnoreCase("giveItem")) {
                        if (args.length == 3) {
                            giveToPlayer(sender, args[1], args[2], true);
                        } else
                            giveSelf(sender, args[1], true);


                    } else {
                        Titles.getInstance().sendMessage(sender, adminUsage);
                    }
                }
                catch (NullPointerException e){
                    Titles.getInstance().getLogger().error("Account Error, the server UUID had a method called on it, Check your code idiot.");
                }
            }
        }
        else
            Titles.getInstance().sendMessage(sender, usage);

    }

   /* public static void serverUse(String[] args, Account acct){
        String sub = args[0];
        args = Arrays.copyOfRange(args, 1, args.length-1);
        equipFor(args[1], args[2], acct);


    }*/
    public static void list(UUID uuid, Account acct){
        Titles.getInstance().sendTitlesListPage(uuid, createChatPage(title, header, acct.getPlayerUnlockedTitles()));
    }

    public static void equip(UUID uuid, String title, Account account){
        if(account.getPlayerUnlockedTitles().contains(title)) {
            account.setPlayerActiveTitle(title);
            Titles.getInstance().sendMessage(uuid, "&7Your title is now &a" + title + " &7("+Titles.getInstance().getGlobalTitleList().get(title)+"&7)! ");
        }
        else{
            Titles.getInstance().sendMessage(uuid, notUnlocked);
        }
    }

    /*public static void equipFor(String name, String title, Account account){
        UUID receiver = Titles.getInstance().getServer().getPlayer(name).get().getUniqueId();

    }*/

    public static void remove(UUID uuid, Account account){
        account.setPlayerActiveTitle("none");
        Titles.getInstance().sendMessage(uuid, "&7You have removed your title.");
    }



    public static void create(UUID uuid, String[] args){
        if (Titles.getInstance().addTitle(args[1], args[2]) != null) {
            Titles.getInstance().sendMessage(uuid, "&cThe title " + args[1] + "&c(" + args[2] + "&c) already exists!");
            return;
        }
        Titles.getInstance().sendMessage(uuid, "&aThe title " + args[1] + "&a(" + args[2] + "&a) was successfully created!");
    }

    public static void delete(UUID uuid, String[] args){
        if (Titles.getInstance().removeTitle(args[1]) != null) {
            Titles.getInstance().sendMessage(uuid, "&cThe title " + args[1] + "&c(" + args[2] + "&c) does not exist!");
            Titles.getInstance().validateTitles();
            return;
        }
        Titles.getInstance().sendMessage(uuid, "&aThe title " + args[1] + "&a(" + args[2] + "&a) was successfully removed!");
    }

    public static void giveToPlayer(UUID sender, String name, String title, boolean item){
        UUID receiver = Titles.getInstance().getUserFromName(name).get().getUniqueId();
        if(item) {
            if (Titles.getInstance().sendItem(receiver, title)) {
                Titles.getInstance().sendMessage(sender, "The player with uuid " + receiver + " was unable to receive their title item at this time.");
                return;
            }
            Titles.getInstance().sendMessage(sender, "The player with uuid " + receiver + "  received their title.");
            Titles.getInstance().sendMessage(receiver, "You have been given the " + title + " title item!");
            return;
        }
        Titles.getInstance().getAccount(receiver).unlockTitle(title);
        Titles.getInstance().sendMessage(sender, "The player with uuid " + receiver + " has had the title " + title + " unlocked");
        Titles.getInstance().sendMessage(receiver, "You have unlocked the " + title + " title!");
    }

    public static void giveSelf(UUID senderAndReciever, String title, boolean item){
        if(item) {
            if (Titles.getInstance().sendItem(senderAndReciever, title)) {
                Titles.getInstance().sendMessage(senderAndReciever, "You cant fit this in your inventory right now!");
            }
            Titles.getInstance().sendMessage(senderAndReciever, "You have been given the " + title + " title item!");
            return;
        }
        Titles.getInstance().getAccount(senderAndReciever).unlockTitle(title);
        Titles.getInstance().sendMessage(senderAndReciever, "You have unlocked the " + title + " title!");

    }

    public static void reload(UUID uuid) {
        if (uuid.equals(Titles.serverUUID) || Titles.getInstance().hasPerm(uuid, "titles.admin.reload")) {
            Titles.getInstance().save();
            Titles.getInstance().load();
            Titles.getInstance().sendMessage(uuid, "&2Reloaded Title config.");
            return;
        }
        Titles.getInstance().sendMessage(uuid, noPerm);
    }

    private static PaginationList createChatPage(String title, String header, List<String> titles){
        Text[] formattedTitles = new Text[titles.size()];
        List<String> gTitleList = new ArrayList<>(Titles.getInstance().getGlobalTitleList().keySet());
        for(int i = 0; i < titles.size(); i++) {
            String ttl = titles.get(i);

            formattedTitles[i] = Utils.colorizer(gTitleList.get(i))
                    .toBuilder()
                    .onClick(TextActions.runCommand("titles equip " + ttl))
                    .build();
        }
        return PaginationList.builder()
                .title(Utils.colorizer(title))
                .header(Utils.colorizer(header))
                .padding(Utils.colorizer("-"))
                .contents(formattedTitles)
                .linesPerPage(7)
                .build();
    }
}
