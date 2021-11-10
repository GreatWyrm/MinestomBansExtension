package com.arcanewarrior.commands;

import com.arcanewarrior.BansExtension;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public class BanCommand extends Command {

    public BanCommand(String permissionName) {
        super("ban");

        setCondition(((sender, commandString) -> sender.hasPermission(permissionName) || sender.isConsole()));
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /ban [player] [reason]"));

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true);
        Argument<String> reason = ArgumentType.String("reason").setDefaultValue("The Ban Hammer has spoken!");

        addSyntax((sender, context) -> {
            String banReason = context.get(reason);
            for(Entity entity : context.get(players).find(sender)) {
                // Should only be players, but hey, cast just to be safe
                if(entity instanceof Player player) {
                    player.kick(Component.text(banReason, NamedTextColor.RED));
                    BansExtension.getInstance().addBannedPlayer(player, banReason);
                }
            }
        }, players, reason);
    }
}
