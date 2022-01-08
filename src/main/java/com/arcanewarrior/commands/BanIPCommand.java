package com.arcanewarrior.commands;

import com.arcanewarrior.BanAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import java.util.List;

public class BanIPCommand extends BaseCommand {
    public BanIPCommand(String permissionName, BanAction banAction) {
        super(permissionName, "ban-ip");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /ban-ip [player] [reason]"));

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true);
        Argument<String[]> reason = ArgumentType.StringArray("reason").setDefaultValue(new String[]{"The Ban Hammer has spoken!"});

        addSyntax((sender, context) -> {
            // Create ban reason by concatenating the string array with spaces
            StringBuilder banReason = new StringBuilder();
            for(String s : context.get(reason)) {
                banReason.append(s);
                banReason.append(" ");
            }
            // Remove last space
            if(!banReason.isEmpty())
                banReason.deleteCharAt(banReason.length() - 1);
            List<Entity> entityList =  context.get(players).find(sender);
            if(entityList.size() > 0) {
                for(Entity entity : entityList) {
                    // Should only be players, but hey, cast just to be safe
                    if(entity instanceof Player player) {
                        player.kick(Component.text(banReason.toString(), NamedTextColor.RED));
                        banAction.addBannedIP(player.getPlayerConnection().getRemoteAddress(), banReason.toString());
                    }
                }
            }
        }, players, reason);
    }
}
