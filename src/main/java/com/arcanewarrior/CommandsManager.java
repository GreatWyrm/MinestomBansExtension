package com.arcanewarrior;

import com.arcanewarrior.commands.BanCommand;
import com.arcanewarrior.commands.KickCommand;
import com.arcanewarrior.commands.Permissions;
import com.arcanewarrior.commands.UnbanCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;

import java.util.EnumMap;

public class CommandsManager {

    private static final EnumMap<Permissions, String> permissionMap = new EnumMap<>(Permissions.class);

    public static void registerAllCommands() {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        permissionMap.put(Permissions.KICK, "minestom.kick");
        permissionMap.put(Permissions.BAN, "minestom.ban");
        permissionMap.put(Permissions.UNBAN, "minestom.unban");

        commandManager.register(new KickCommand(permissionMap.get(Permissions.KICK)));
        commandManager.register(new BanCommand(permissionMap.get(Permissions.BAN)));
        commandManager.register(new UnbanCommand(permissionMap.get(Permissions.UNBAN)));
    }
}
