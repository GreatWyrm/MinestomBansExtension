package com.arcanewarrior.storage;

import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.data.DatabaseDetails;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocalStorageIO implements StorageIO {

    private final Logger logger = LoggerFactory.getLogger(LocalStorageIO.class);

    private Path bansDataFile;

    @Override
    public void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details) {
        String path = details.path();
        if(!path.endsWith(".json")) {
            path += ".json";
        }
        bansDataFile = rootExtensionFolder.resolve(path);
        if(!Files.exists(bansDataFile)) {
            logger.info("Banlist file not found! Creating...");
            try {
                Files.createFile(bansDataFile);
            } catch (IOException e) {
                logger.info("Failed to create banlist file!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<UUID, BanDetails> loadAllBansFromStorage() {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(bansDataFile).build();
        HashMap<UUID, BanDetails> list = new HashMap<>();
        boolean shouldSave = false;
        try {
            BasicConfigurationNode root = loader.load();
            for(var entry : root.childrenMap().entrySet()) {
                try {
                    UUID id = UUID.fromString(entry.getKey().toString());
                    BasicConfigurationNode node = entry.getValue();
                    String reason = node.node("reason").getString();
                    String username = node.node("username").getString();
                    BanDetails details = new BanDetails(id, username, reason);
                    list.put(id, details);
                } catch (IllegalArgumentException e) {
                    shouldSave = true;
                    root.removeChild(entry.getKey());
                    logger.warn("Invalid UUID detected when loading bans");
                }
            }
            if(shouldSave) {
                loader.save(root);
            }
        } catch (ConfigurateException e) {
            logger.warn("Failed to load in ban list file!");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void saveBannedPlayerToStorage(@NotNull BanDetails details) {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(bansDataFile).build();
        try {
            BasicConfigurationNode rootNode = loader.load();
            BasicConfigurationNode node = loader.createNode();
            node.node("reason").set(details.banReason());
            node.node("username").set(details.bannedUsername());
            rootNode.node(details.uuid().toString()).set(node);
            loader.save(rootNode);

        } catch (ConfigurateException e) {
            logger.warn("Failed to add player " + details.bannedUsername() + " to the ban list file.");
            e.printStackTrace();
        }
    }

    @Override
    public void removeBannedPlayerFromStorage(@NotNull UUID id) {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(bansDataFile).build();
        try {
            BasicConfigurationNode root = loader.load();
            root.removeChild(id.toString());
            loader.save(root);
        } catch (ConfigurateException e) {
            logger.warn("Failed to remove UUID " + id + " to the ban list file.");
            e.printStackTrace();
        }
    }
}
