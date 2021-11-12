package com.arcanewarrior.commands;

import com.arcanewarrior.BanAction;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

public class UnbanCommand extends BaseCommand {

    public UnbanCommand(String permissionName, BanAction banAction) {
        super(permissionName, "unban", "pardon");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /unban [player]"));

        // Thank you https://programming.guide/java/passing-list-to-vararg-method.html
        ArgumentWord players = ArgumentType.Word("player").from(banAction.getBannedPlayerNames().toArray(new String[0]));

        // Technically could run into issues with username/UUID issues, maybe return a pair with username/uuid to solve duplicate username issue?
        addSyntax((sender, context) -> banAction.unbanPlayer(context.get(players)), players);
    }
}
