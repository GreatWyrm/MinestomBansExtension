package com.arcanewarrior.commands;

import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseCommand extends Command {

    public BaseCommand(String permission, @NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setCondition(((sender, commandString) -> sender.hasPermission(permission) || sender instanceof ConsoleSender));
    }
}
