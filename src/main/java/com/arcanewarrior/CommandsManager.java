package com.arcanewarrior;

import com.arcanewarrior.commands.BanCommand;
import com.arcanewarrior.commands.KickCommand;
import com.arcanewarrior.commands.Permissions;
import com.arcanewarrior.commands.UnbanCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;

import java.util.EnumMap;

public class CommandsManager {

    public static void registerAllCommands(EnumMap<Permissions, String> permissionMap) {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        commandManager.register(new KickCommand(permissionMap.get(Permissions.KICK)));
        commandManager.register(new BanCommand(permissionMap.get(Permissions.BAN)));
        commandManager.register(new UnbanCommand(permissionMap.get(Permissions.UNBAN)));
    }
}
