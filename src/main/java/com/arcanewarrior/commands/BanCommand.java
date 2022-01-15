package com.arcanewarrior.commands;

import com.arcanewarrior.BanAction;
import com.arcanewarrior.UUIDUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.mojang.MojangUtils;

import java.util.List;

public class BanCommand extends BaseCommand {

    public BanCommand(String permissionName, BanAction banAction) {
        super(permissionName, "ban");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /ban [player] [reason]"));

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true);
        Argument<String[]> reason = ArgumentType.StringArray("reason").setDefaultValue(new String[]{"The Ban Hammer has spoken!"});

        addSyntax((sender, context) -> {
            // Create ban reason by concatenating the string array with spaces
            String banReason = String.join(" ", context.get(reason));
            List<Entity> entityList =  context.get(players).find(sender);
            if(entityList.size() > 0) {
                for(Entity entity : entityList) {
                    // Should only be players, but hey, cast just to be safe
                    if(entity instanceof Player player) {
                        player.kick(Component.text(banReason, NamedTextColor.RED));
                        banAction.addBannedPlayer(player, banReason);
                    }
                }
            } else {
                String offlinePlayer = context.getRaw(players);
                var output = MojangUtils.fromUsername(offlinePlayer);
                // Output is of format {"name":Player Username,"id":UUID of Player (without dashes)}
                if(output != null) {
                    String uuid = output.get("id").getAsString();
                    banAction.addBannedPlayer(UUIDUtils.makeUUIDFromStringWithoutDashes(uuid), output.get("name").getAsString(), banReason);
                } else {
                    sender.sendMessage("Error: Could not find offline player with name " + offlinePlayer);
                }
            }
        }, players, reason);
    }
}
