package me.strobe.titles.config;

import me.strobe.titles.main.Titles;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


//Controls Accounts for accounts.conf for creating and deleteing accounts, as well as maanging their titles
public class AccountManager {
    private final Logger logger;
    private File accountsFile;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode accountConfig;

    public AccountManager(){
        logger = Titles.getInstance().getLogger();
        setupAccountCFG();
    }

    private void setupAccountCFG(){
        accountsFile = new File(Titles.getInstance().getConfigDir().toFile(), "accounts.conf");
        loader = HoconConfigurationLoader.builder().setFile(accountsFile).build();
        try{
            accountConfig = loader.load();

            if(!accountsFile.exists()){
                loader.save(accountConfig);
            }
        }
        catch(IOException e){
            logger.warn("Error with setting up Account Config");
            e.printStackTrace();
        }
    }

    public void createUserAccount(UUID uuid){
        Account account = new Account(Titles.getInstance(), this, uuid);
        if(!hasAcct(uuid)){
            account.setPlayerUnlockedTitles(new ArrayList<>());
            account.setPlayerActiveTitle("none");
        }
        if(Titles.getInstance().addAccount(uuid, account) == null)
            logger.info("Account with uuid" + uuid.toString() + "added to titles list.");
        else{
            logger.warn("Failed to add account with uuid " + uuid.toString() + " to titles list.");
        }
    }

    public boolean hasAcct(UUID uuid){
        return accountConfig.getNode(uuid.toString()).getValue() != null;
    }

    public ConfigurationNode getAccountConfig(){
        return accountConfig;
    }

    public void saveConfig(){
        try {
            loader.save(accountConfig);
        } catch (IOException e) {
            logger.warn("Error while saving an account file");
            e.printStackTrace();
        }
    }
}
