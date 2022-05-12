package com.arcanewarrior.storage;

import com.arcanewarrior.data.BanRecord;
import com.arcanewarrior.data.DatabaseDetails;
import com.arcanewarrior.data.PermanentBanRecord;
import com.arcanewarrior.data.TemporaryBanRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocalStorageIO implements StorageIO {

    private final Logger logger = LoggerFactory.getLogger(LocalStorageIO.class);
    private Path uuidBansPath;
    private Path ipBansPath;

    @Override
    public void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details) {
        String playerPath = details.playerBanPath();
        if(!playerPath.endsWith(".json")) {
            playerPath += ".json";
        }
        uuidBansPath = rootExtensionFolder.resolve(playerPath);
        String ipPath = details.ipBanPath();
        if(!ipPath.endsWith(".json")) {
            ipPath += ".json";
        }
        ipBansPath = rootExtensionFolder.resolve(ipPath);
        if(!Files.exists(uuidBansPath)) {
            logger.info("UUID Ban list file not found! Creating...");
            try {
                Files.createFile(uuidBansPath);
            } catch (IOException e) {
                logger.info("Failed to create uuid banlist file!");
                e.printStackTrace();
            }
        }
        if(!Files.exists(ipBansPath)) {
            logger.info("IP Ban list file not found! Creating...");
            try {
                Files.createFile(ipBansPath);
            } catch (IOException e) {
                logger.info("Failed to create ip banlist file!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<UUID, BanRecord> loadBans() {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(uuidBansPath).build();
        HashMap<UUID, BanRecord> list = new HashMap<>();
        boolean shouldSave = false;
        try {
            BasicConfigurationNode root = loader.load();
            for(var entry : root.childrenMap().entrySet()) {
                try {
                    UUID id = UUID.fromString(entry.getKey().toString());
                    BasicConfigurationNode node = entry.getValue();
                    String banTimeString = node.node("banTime").getString();
                    ZonedDateTime banTime = ZonedDateTime.now();
                    if(banTimeString != null) {
                        banTime = ZonedDateTime.parse(banTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    }
                    if(node.hasChild("expiryTime")) {
                        // Working with temp ban, load accordingly
                        String expireTimeString = node.node("expiryTime").getString();
                        ZonedDateTime expireTime = ZonedDateTime.now();
                        if(expireTimeString != null) {
                            expireTime = ZonedDateTime.parse(expireTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                        }
                        TemporaryBanRecord record = new TemporaryBanRecord(
                            id, node.node("username").getString("Unknown"),
                                node.node("banReason").getString("The Ban Hammer has spoken!"),
                                node.node("banExecutor").getString("Unknown"),
                                banTime,
                                expireTime
                        );
                        // Quick check to not load expired bans
                        if(record.isPlayerBanned()) {
                            list.put(id, record);
                        }
                    } else {
                        PermanentBanRecord record = new PermanentBanRecord(
                                id, node.node("username").getString("Unknown"),
                                node.node("banReason").getString("The Ban Hammer has spoken!"),
                                node.node("banExecutor").getString("Unknown"),
                                banTime
                        );
                        list.put(id, record);
                    }
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
            logger.warn("Failed to load in player ban list file!");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Map<String, String> loadIpBans() {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(ipBansPath).build();
        HashMap<String, String> list = new HashMap<>();
        try {
            BasicConfigurationNode root = loader.load();
            for(var entry : root.childrenMap().entrySet()) {
                String address = entry.getKey().toString();
                BasicConfigurationNode node = entry.getValue();
                String reason = node.node("reason").getString();
                list.put(address, reason);
            }
        } catch (ConfigurateException e) {
            logger.warn("Failed to load in ip ban list file!");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void saveBan(@NotNull BanRecord banRecord) {
        // Don't save ban if it's not an actual ban
        if(!banRecord.isPlayerBanned()) {
            return;
        }
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(uuidBansPath).build();
        try {
            BasicConfigurationNode rootNode = loader.load();
            BasicConfigurationNode node = loader.createNode();
            var propertiesMap = banRecord.toPropertiesMap();
            for(String key : propertiesMap.keySet()) {
                node.node(key).set(propertiesMap.get(key));
            }
            rootNode.node(banRecord.uuid().toString()).set(node);
            loader.save(rootNode);

        } catch (ConfigurateException e) {
            logger.warn("Failed to add player " + banRecord.username() + " to the ban list file.");
            e.printStackTrace();
        }
    }

    @Override
    public void removeBan(@NotNull UUID id) {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(uuidBansPath).build();
        try {
            BasicConfigurationNode root = loader.load();
            root.removeChild(id.toString());
            loader.save(root);
        } catch (ConfigurateException e) {
            logger.warn("Failed to remove UUID " + id + " to the ban list file.");
            e.printStackTrace();
        }
    }

    @Override
    public void saveBannedIp(@NotNull String ipString, @NotNull String reasonString) {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(ipBansPath).build();
        try {
            BasicConfigurationNode rootNode = loader.load();
            BasicConfigurationNode node = loader.createNode();
            node.node("reason").set(reasonString);
            rootNode.node(ipString).set(node);
            loader.save(rootNode);
        } catch (ConfigurateException e) {
            logger.warn("Failed to add ip " + ipString + " to the ban list file.");
            e.printStackTrace();
        }
    }

    @Override
    public void removeBannedIp(@NotNull String ipString) {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(ipBansPath).build();
        try {
            BasicConfigurationNode root = loader.load();
            root.removeChild(ipString);
            loader.save(root);
        } catch (ConfigurateException e) {
            logger.warn("Failed to remove UUID " + ipString + " to the ban list file.");
            e.printStackTrace();
        }
    }
}
