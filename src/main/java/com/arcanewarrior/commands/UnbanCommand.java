package com.arcanewarrior.commands;

import com.arcanewarrior.BanAction;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

public class UnbanCommand extends BaseCommand {

    public UnbanCommand(String permissionName, BanAction banAction) {
        super(permissionName, "unban", "pardon");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /unban [player]"));

        ArgumentString player = ArgumentType.String("player");

        player.setSuggestionCallback((sender, context, suggestion) -> {
           for(String name : banAction.getBannedPlayerNames()) {
               suggestion.addEntry(new SuggestionEntry(name));
           }
        });

        // Technically could run into issues with username/UUID issues, maybe return a pair with username/uuid to solve duplicate username issue?
        addSyntax((sender, context) -> banAction.unbanPlayer(context.get(player)), player);
    }
}
