package com.arcanewarrior;

import com.arcanewarrior.storage.StorageIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.extensions.Extension;

import java.util.List;
import java.util.UUID;

public class BansExtension extends Extension {

    private static BansExtension extensionInstance;
    
    public static BansExtension getInstance() {
        return extensionInstance;
    }

    private DataManager dataManager;
    private ConfigManager configManager;
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
        CommandsManager.registerAllCommands(configManager.loadPermissionsFromConfig());

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerPreLoginEvent.class, event -> {
           if(dataManager.isIDBanned(event.getPlayerUuid())) {
               event.getPlayer().kick(Component.text("You have been banned from this server.\n" + dataManager.getBanReason(event.getPlayerUuid()), NamedTextColor.RED));
           }
        });
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
    }
}
