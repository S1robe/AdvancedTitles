package me.strobe.titles.config;

import me.strobe.titles.main.Titles;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.UUID;

//handles players title changes and stuff
public class Account {
    private final Titles plugin;
    private final AccountManager accountManager;
    private final UUID uuid;

    private final ConfigurationNode accountConfig;

    public Account(Titles plugin, AccountManager accountManager, UUID uuid){
        this.plugin = plugin;
        this.accountManager = accountManager;
        this.uuid = uuid;
        accountConfig = accountManager.getAccountConfig();
    }


    public String getPlayerActiveTitle(){
        return accountConfig.getNode(uuid.toString(), "activeTitle").getString();
    }

    public void setPlayerActiveTitle(String newActiveTitle){
        accountConfig.getNode(uuid.toString(), "activeTitle").setValue(newActiveTitle);
        accountManager.saveConfig();
    }

    public List<String> getPlayerUnlockedTitles(){
        return (List<String>) accountConfig.getNode(uuid.toString(), "unlockedTitles").getValue();
    }

    public void setPlayerUnlockedTitles(List<String> titles){
        accountConfig.getNode(uuid.toString(), "unlockedTitles").setValue(titles);
        accountManager.saveConfig();
    }

    public boolean unlockTitle(String title){
        if(!getPlayerUnlockedTitles().contains(title)){
            List<String> temp = getPlayerUnlockedTitles();
            temp.add(title);
            setPlayerUnlockedTitles(temp);
            return true;
        }
        return false;
    }

    public boolean lockTitle(String title){
        if(getPlayerUnlockedTitles().contains(title)){
            List<String> temp = getPlayerUnlockedTitles();
            temp.remove(title);
            setPlayerUnlockedTitles(temp);
            return true;
        }
        return false;
    }
}
