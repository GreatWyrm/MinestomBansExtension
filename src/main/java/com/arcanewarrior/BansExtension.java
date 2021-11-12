package com.arcanewarrior;

import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.extensions.Extension;

public class BansExtension extends Extension {

    private DataManager dataManager;
    private BansConfig config;
    private CommandsManager commandsManager;
    private EventsHandler eventsHandler;
    private StorageIO storageIO;

    @Override
    public void initialize() {
        getLogger().info("Initializing Bans Extension...");

        config = new BansConfig(getDataDirectory());
        storageIO = config.getStorageIO();
        storageIO.initializeIfEmpty(getDataDirectory());

        dataManager = new DataManager(storageIO);
        commandsManager = new CommandsManager(new BanAction(storageIO, dataManager));
        commandsManager.registerAllCommands(config.loadPermissionsFromConfig());

        eventsHandler = new EventsHandler(dataManager);
        eventsHandler.registerEvents();
    }

    @Override
    public void terminate() {
        getLogger().info("Terminating Bans Extension...");
        commandsManager.unregisterAllCommands();
        eventsHandler.unregisterEvents();
    }


}
