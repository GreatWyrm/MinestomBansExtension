package com.arcanewarrior.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public class KickCommand extends BaseCommand {

    // Maybe figure out how to display this message if permission fail, or should this just be removed?
    //private final Component NO_PERMISSION = Component.text("You do not have permission to use /kick.", NamedTextColor.RED);

    public KickCommand(String permissionName) {
        super(permissionName, "kick");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /kick [player] [reason]"));

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true);
        Argument<String[]> reason = ArgumentType.StringArray("reason").setDefaultValue(new String[]{"Kicked from the server"});

        addSyntax((sender, context) -> {
            String kickReason = String.join(" ", context.get(reason));
            for(Entity entity : context.get(players).find(sender)) {
                // Should only be players, but hey, cast just to be safe
                if(entity instanceof Player player) {
                    player.kick(Component.text(kickReason, NamedTextColor.RED));
                }
            }
        }, players, reason);
    }
}
