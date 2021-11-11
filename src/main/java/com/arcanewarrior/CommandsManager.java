package com.arcanewarrior;

import com.arcanewarrior.commands.BanCommand;
import com.arcanewarrior.commands.KickCommand;
import com.arcanewarrior.commands.Permissions;
import com.arcanewarrior.commands.UnbanCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class CommandsManager {

    private final Set<Command> commands = new HashSet<>();

    public void registerAllCommands(EnumMap<Permissions, String> permissionMap) {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        commands.add(new KickCommand(permissionMap.get(Permissions.KICK)));
        commands.add(new BanCommand(permissionMap.get(Permissions.BAN)));
        commands.add(new UnbanCommand(permissionMap.get(Permissions.UNBAN)));

        commands.forEach(commandManager::register);
    }

    public void unregisterAllCommands() {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commands.forEach(commandManager::unregister);
    }
}
