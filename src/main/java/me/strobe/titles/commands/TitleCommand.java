package me.strobe.titles.commands;

import me.strobe.titles.config.Account;
import me.strobe.titles.config.AccountManager;
import me.strobe.titles.main.Titles;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.UUID;

public class TitleCommand implements CommandExecutor {

    private final AccountManager accountManager;

    public TitleCommand(AccountManager accountManager){
        this.accountManager = accountManager;
    }

    public static CommandSpec getSpec(){
        return CommandSpec.builder()
                .description(Text.of("root command for titles"))
                .executor(new TitleCommand(Titles.getInstance().getAccountManager()))
                .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("args"))))
                .build();
    }

    @Override
    public @NonnullByDefault CommandResult execute(CommandSource src, CommandContext args){
        //src is what or who sent the command
        //args are everyting after the command separated by spaces

        //title subcommand [uuid or name] [<title>]
        String[] arguments = ((String) args.getOne(Text.of("args")).orElse("")).replaceAll("  ", " ").split(" ");
        if(src instanceof Player) {
            Player player = ((Player) src).getPlayer().get();
            UUID uuid = player.getUniqueId();
            Account account = new Account(Titles.getInstance(), accountManager, uuid);
            TitleCommandProcessor.use(uuid, arguments, account);
            return CommandResult.success();
        }
        else if(src instanceof ConsoleSource){
            //These commands are only ever used by the server, as such only UUID's are supported
            UUID receiver = Titles.getInstance().getUserFromName(arguments[1]).get().getUniqueId();
            Account account = new Account(Titles.getInstance(), accountManager, receiver);
            TitleCommandProcessor.use(Titles.serverUUID, arguments, account);
        }

        return CommandResult.empty();
    }

}
