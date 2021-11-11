package com.arcanewarrior;

import com.arcanewarrior.storage.StorageIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BansExtension extends Extension {

    private static BansExtension extensionInstance;
    
    public static BansExtension getInstance() {
        return extensionInstance;
    }

    private DataManager dataManager;
    private ConfigManager configManager;
    private CommandsManager commandsManager;
    private BanListener banListener;
    private StorageIO storageIO;

    public BansExtension() {
        extensionInstance = this;
    }

    @Override
    public void initialize() {
        getLogger().info("Initializing Bans Extension...");

        configManager = new ConfigManager();
        storageIO = configManager.getStorageIO();
        storageIO.initializeIfEmpty();
        dataManager = new DataManager(storageIO);
        commandsManager = new CommandsManager();
        commandsManager.registerAllCommands(configManager.loadPermissionsFromConfig());

        banListener = new BanListener();
        MinecraftServer.getGlobalEventHandler().addListener(banListener);
    }

    public void addBannedPlayer(Player player, String reason) {
        dataManager.addBannedPlayer(player, reason);
        storageIO.saveBannedPlayerToStorage(player, reason);
    }

    public void unbanPlayer(String username) {
        UUID id = dataManager.removeBannedPlayer(username);
        if(id != null) {
            storageIO.removeBannedPlayerFromStorage(id);
        }
    }

    public List<String> getBannedPlayerNames() {
        return dataManager.getBannedUsernames();
    }

    @Override
    public void terminate() {
        getLogger().info("Terminating Bans Extension...");
        commandsManager.unregisterAllCommands();
        MinecraftServer.getGlobalEventHandler().removeListener(banListener);
    }

    private class BanListener implements EventListener<AsyncPlayerPreLoginEvent> {
        @Override
        public @NotNull Class<AsyncPlayerPreLoginEvent> eventType() {
            return AsyncPlayerPreLoginEvent.class;
        }
        @Override
        public @NotNull Result run(@NotNull AsyncPlayerPreLoginEvent event) {
            if(dataManager.isIDBanned(event.getPlayerUuid())) {
                event.getPlayer().kick(Component.text("You have been banned from this server.\n" + dataManager.getBanReason(event.getPlayerUuid()), NamedTextColor.RED));
            }
            return Result.SUCCESS;
        }
    }
}
