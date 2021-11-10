package com.arcanewarrior.commands;

import com.arcanewarrior.BansExtension;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

public class UnbanCommand extends Command {

    public UnbanCommand(String permissionName) {
        super("unban", "pardon");

        setCondition(((sender, commandString) -> sender.hasPermission(permissionName) || sender.isConsole()));
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /unban [player]"));

        // Thank you https://programming.guide/java/passing-list-to-vararg-method.html
        ArgumentWord players = ArgumentType.Word("player").from(BansExtension.getInstance().getBannedPlayerNames().toArray(new String[0]));

        // Technically could run into issues with username/UUID issues, maybe return a pair with username/uuid to solve duplicate username issue?

        addSyntax((sender, context) -> BansExtension.getInstance().unbanPlayer(context.get(players)), players);
    }
}
