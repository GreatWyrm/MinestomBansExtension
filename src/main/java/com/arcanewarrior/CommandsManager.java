package com.arcanewarrior;

import com.arcanewarrior.commands.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class CommandsManager {

    private final Set<Command> commands = new HashSet<>();
    private final BanAction banAction;

    public CommandsManager(BanAction banAction) {
        this.banAction = banAction;
    }

    public void registerAllCommands(EnumMap<Permissions, String> permissionMap) {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        commands.add(new KickCommand(permissionMap.get(Permissions.KICK)));
        commands.add(new BanCommand(permissionMap.get(Permissions.BAN), banAction));
        commands.add(new UnbanCommand(permissionMap.get(Permissions.UNBAN), banAction));
        commands.add(new BanIPCommand(permissionMap.get(Permissions.BAN_IP), banAction));
        commands.add(new UnbanIPCommand(permissionMap.get(Permissions.UNBAN_IP), banAction));

        commands.forEach(commandManager::register);
    }

    public void unregisterAllCommands() {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commands.forEach(commandManager::unregister);
    }
}
