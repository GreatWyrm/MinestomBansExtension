package com.arcanewarrior.commands;

import com.arcanewarrior.BanAction;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

public class UnbanIPCommand extends BaseCommand {
    public UnbanIPCommand(String permissionName, BanAction banAction) {
        super(permissionName, "unban-ip", "pardon-ip");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /unban-ip [address]"));

        ArgumentString address = ArgumentType.String("address");

        address.setSuggestionCallback((sender, context, suggestion) -> {
            for(String name : banAction.getBannedIPs()) {
                suggestion.addEntry(new SuggestionEntry(name));
            }
        });

        addSyntax((sender, context) -> banAction.unbanIpAddress(context.get(address)), address);
    }
}
