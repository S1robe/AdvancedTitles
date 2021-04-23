package me.strobe.titles.main;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.text.event.NucleusTextTemplateEvent;
import me.strobe.titles.commands.TitleCommand;
import me.strobe.titles.config.Account;
import me.strobe.titles.config.AccountManager;
import me.strobe.titles.utils.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Plugin(
        id = "titles",
        name = "Titles",
        version = "1.0-SNAPSHOT"
)
public class Titles {

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path titleConfg;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    private Server server;

    private static Titles instance;

    private ConfigurationNode titleConfig;
    private AccountManager accountManager;
    private static final HashMap<String, String> titleList = new HashMap<>();
    private static final HashMap<UUID, Account> accounts = new HashMap<>();
    private static final String titleToken = "{{title}}";
    public static UUID serverUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");


    @Listener
    public void preInit(GamePreInitializationEvent event){
        instance = this;
        accountManager = new AccountManager();
        load();
        logger.info("Titles and Accounts Loaded!");
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        Sponge.getCommandManager().register(this, TitleCommand.getSpec(), "titles", "title");
        logger.info("Titles Commands Loaded!");
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event){
        server = game.getServer();
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        save();
        logger.info("Titles is Stopping");
    }






    @Listener
    public void onJoin(ClientConnectionEvent.Join event){
        UUID pUUID = event.getTargetEntity().getUniqueId();
        accountManager.createUserAccount(pUUID);
    }

    @Listener
    public void onLeave(ClientConnectionEvent.Disconnect event){
        accountManager.saveConfig();
        accounts.remove(event.getTargetEntity().getUniqueId());
    }

    @Listener
    public void onChat(NucleusTextTemplateEvent event){
        //if the message sent contains replacable title token
        if(event.getOriginalMessage().containsTokens()) {
            //gets player uuid?
            UUID sender = event.getCause().first(Player.class).get().getUniqueId();
            //construct their title
            Text title = Utils.colorizer(getAccount(sender).getPlayerActiveTitle());
            //Smack our title on after all tokens added by nucleus

            event.setMessage(event.getMessage().getPrefix().get().replace(titleToken, title).toString());
        }
    }

    public boolean hasPerm(UUID uuid, String perm){
        return game.getServer().getPlayer(uuid).get().hasPermission(perm);
    }

    public void sendMessage(UUID uuid, String message){
        game.getServer().getPlayer(uuid).get().sendMessage(Utils.colorizer(message));
    }

    public void sendTitlesListPage(UUID uuid, PaginationList pageList){
        pageList.sendTo(game.getServer().getPlayer(uuid).get());
    }

    public boolean sendItem(UUID uuid, String title){
        return game.getServer().getPlayer(uuid).get()
                .getInventory().offer(Utils.titleItem(title, titleList.get(title)))
                               .getType().equals(InventoryTransactionResult.Type.SUCCESS);
    }

    public void save(){
        validateTitles();
        try {
            loader.save(titleConfig);
        } catch (IOException e) {
            logger.warn("Error saving titles configuraiton");
            e.printStackTrace();
        }
    }

    public void load(){
        try {
            titleConfig = loader.load();
            if(!titleConfg.toFile().exists()){
                titleConfg.toFile().mkdir();
            }
            else{
                titleConfig.getChildrenList().forEach(node ->{
                    titleList.putIfAbsent(node.toString(), node.getValue().toString());
                });
            }

            validateTitles();
            loader.save(titleConfig);
        } catch (IOException e) {
            logger.warn("Error loading titles configuraiton");
            e.printStackTrace();
        }
    }

    public Object addAccount(UUID uuid, Account acct){
        return accounts.putIfAbsent(uuid, acct);
    }

    public Object addTitle(String name, String data){
        return titleList.putIfAbsent(name, data);
    }

    public Object removeTitle(String name){
        return titleList.remove(name);
    }

    /*public void validateTitles(){
        accounts.forEach(((uuid, account) -> {
            if(!titleList.containsKey(account.getPlayerActiveTitle()))
                account.setPlayerActiveTitle("none");
            account.getPlayerUnlockedTitles().forEach(title -> {
                if(!titleList.containsKey(title))
                    account.lockTitle(title);
            });
        }));
        accountManager.saveConfig();
    }*/

    //check all titles at startup and at delete, dont need to any other time!
    public void validateTitles(){
        List<ConfigurationNode> accounts = (List<ConfigurationNode>) accountManager.getAccountConfig().getChildrenList();
        accounts.forEach(acct -> {
            if(!titleList.containsKey(acct.getNode("activeTitle").getValue())){
                acct.getNode("activeTitle").setValue("none");
            }
            List<String> titles = ((List<String>)acct.getNode("unlockedTitles").getValue());
            titles.removeIf(t -> !titles.contains(t));
            acct.getNode("unlockedTitles").setValue(titles);
        });
    }



    public static Titles getInstance() {
        return instance;
    }

    public Logger getLogger(){
        return logger;
    }

    public Path getConfigDir(){
        return configDir;
    }

    public AccountManager getAccountManager(){
        return accountManager;
    }

    public HashMap<String, String> getGlobalTitleList(){
        return titleList;
    }

    public Account getAccount(UUID uuid){
        return accounts.get(uuid);
    }

    public Optional<User> getUserFromName(String lastknownname){
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(lastknownname);
    }

    //Really only used for online players or API's that have a storage for uuid's
    public Optional<User> getUserFromUUID(UUID uuid){
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid);
    }

    public Game getGame(){
        return game;
    }

    public Server getServer(){
        return server;
    }
}
