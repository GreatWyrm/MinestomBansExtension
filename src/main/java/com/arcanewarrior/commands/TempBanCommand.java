package com.arcanewarrior.commands;

import com.arcanewarrior.BanAction;
import com.arcanewarrior.UUIDUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.mojang.MojangUtils;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;

public class TempBanCommand extends BaseCommand {
    public TempBanCommand(String permission, BanAction banAction) {

        super(permission, "temp-ban", "tempban");

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /temp-ban [player] [duration] [reason]"));

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true);
        Argument<String> duration = ArgumentType.String("duration");
        Argument<String[]> reason = ArgumentType.StringArray("reason").setDefaultValue(new String[]{"The Ban Hammer has spoken!"});

        addSyntax((sender, context) -> {
            // Create ban reason by concatenating the string array with spaces
            String banReason = String.join(" ", context.get(reason));
            // Calculate duration, and stop if we've reached and invalid amount
            TemporalAmount banDuration = calculateDuration(sender, context.get(duration));
            if((banDuration instanceof Duration && banDuration.get(ChronoUnit.SECONDS) == 0)
            || banDuration instanceof Period && banDuration.get(ChronoUnit.DAYS) == 0) {
                sender.sendMessage(Component.text("Invalid Ban Duration, ban not issued.", NamedTextColor.RED));
                return;
            }
            String banExecutor;
            if(sender instanceof Player player) {
                banExecutor = player.getUsername();
            } else {
                banExecutor = "Console";
            }
            List<Entity> entityList =  context.get(players).find(sender);
            if(entityList.size() > 0) {
                for(Entity entity : entityList) {
                    // Should only be players, but hey, cast just to be safe
                    if(entity instanceof Player player) {
                        player.kick(Component.text(banReason, NamedTextColor.RED));
                        boolean success = banAction.temporaryBanPlayer(player, banReason, banExecutor, banDuration);
                        if(!success) {
                            sender.sendMessage(Component.text("Failed to ban " + player.getUsername() + ", most likely they are already banned.", NamedTextColor.RED));
                        }
                    }
                }
            } else {
                String offlinePlayer = context.getRaw(players);
                var output = MojangUtils.fromUsername(offlinePlayer);
                // Output is of format {"name":Player Username,"id":UUID of Player (without dashes)}
                if(output != null) {
                    String uuid = output.get("id").getAsString();
                    String username = output.get("name").getAsString();
                    boolean success = banAction.temporaryBanPlayer(UUIDUtils.makeUUIDFromStringWithoutDashes(uuid), username, banReason, banExecutor, banDuration);
                    if(!success) {
                        sender.sendMessage(Component.text("Failed to ban " + username + ", most likely they are already banned.", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage("Error: Could not find offline player with name " + offlinePlayer);
                }
            }
        }, players, duration, reason);
    }
    private TemporalAmount calculateDuration(CommandSender sender, String durationString) {
        Duration duration = Duration.ZERO;
        Period period = Period.ZERO;
        int previousNumberIndex = 0;
        for(int i = 0; i < durationString.length(); i++) {
            char current = durationString.charAt(i);
            if(Character.isAlphabetic(current)) {
                // Calculate from previousNumberIndex to current
                int count = Integer.parseInt(durationString.substring(previousNumberIndex, i));
                switch (current) {
                    case 'y', 'Y' -> period = period.plusYears(count);
                    case 'M' -> period = period.plusMonths(count);
                    case 'w', 'W' -> period = period.plusDays(7L * count);
                    case 'd', 'D' -> duration = duration.plusDays(count);
                    case 'h', 'H' -> duration = duration.plusHours(count);
                    case 'm' -> duration = duration.plusMinutes(count);
                    default -> {
                        sender.sendMessage(Component.text("Unknown unit value: ", NamedTextColor.RED).append(Component.text(current, NamedTextColor.AQUA)));
                        return Duration.ZERO;
                    }
                }
            }
        }
        if(!period.isZero()) {
            return period;
        } else {
            return duration;
        }
    }
}
