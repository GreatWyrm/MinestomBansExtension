package com.arcanewarrior.data;

import com.arcanewarrior.commands.Permissions;
import com.arcanewarrior.storage.LocalStorageIO;
import com.arcanewarrior.storage.MongoDBIO;
import com.arcanewarrior.storage.SQLiteStorageIO;
import com.arcanewarrior.storage.StorageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;

public class BansConfig {

    private final Logger logger = LoggerFactory.getLogger(BansConfig.class);

    private final Path bansRootFolder;
    private final Path bansConfigPath;
    private DatabaseDetails databaseDetails;

    public BansConfig(Path rootPath) {
        bansRootFolder = rootPath;
        bansConfigPath = rootPath.resolve("config.yml");
        ensureFilesExist();
    }

    private void ensureFilesExist() {
        if(!Files.exists(bansRootFolder)) {
            logger.info("Config folder not found! Creating...");
            try {
                Files.createDirectory(bansRootFolder);
            } catch (IOException e) {
                logger.info("Failed to create root directory!");
                e.printStackTrace();
            }
        }
        if(!Files.exists(bansConfigPath)) {
            logger.info("Bans Config file not found! Creating...");
            InputStream input = BansConfig.class.getClassLoader().getResourceAsStream("config.yml");
            try {
                if (input != null) {
                    Files.copy(input, bansConfigPath);
                } else {
                    logger.warn("config.yml resource inputstream was null???");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public StorageIO getStorageIO() {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(bansConfigPath).nodeStyle(NodeStyle.BLOCK).build();
        boolean saveAndUseDefault = false;
        try {
            CommentedConfigurationNode root = loader.load();
            // Look for a database node
            CommentedConfigurationNode databaseNode = root.node("database");
            if(databaseNode.empty()) {
                logger.warn("Database field is empty!");
                saveAndUseDefault = true;
            }
            // Database Field exists and is an object, proceed with reading
            CommentedConfigurationNode typeNode = databaseNode.node("type");
            String databaseType = "";
            if(typeNode.empty()) {
                logger.warn("Type field does not exist or is empty! Fixing config and using local storage.");
                typeNode.set("local");
                saveAndUseDefault = true;
            } else {
                databaseType = typeNode.getString("local");
            }
            CommentedConfigurationNode pathNode = databaseNode.node("ban-playerBanPath");
            String databasePath = "";
            if(pathNode.empty()) {
                logger.warn("Path field does not exist or is empty! Fixing config and using local storage.");
                pathNode.set("bans");
                saveAndUseDefault = true;
            } else {
                databasePath = pathNode.getString("bans");
            }
            CommentedConfigurationNode ipPathNode = databaseNode.node("ip-ban-playerBanPath");
            String databaseIpPath = "";
            if(ipPathNode.empty()) {
                logger.warn("IP Path field does not exist or is empty! Fixing config and using local storage.");
                ipPathNode.set("ip-bans");
                saveAndUseDefault = true;
            } else {
                databaseIpPath = ipPathNode.getString("bans");
            }
            String connectionString = "";
            if(databaseNode.hasChild("connection-string")) {
                connectionString = databaseNode.node("connection-string").getString("");
            } else {
                logger.warn("Connection String field does not exist! Fixing config and using local storage.");
                CommentedConfigurationNode connectionStringNode = databaseNode.node("connection-string");
                connectionStringNode.set("");
                connectionStringNode.commentIfAbsent("Used to connect to a MongoDB database, must be fully qualified");
                saveAndUseDefault = true;
            }
            databaseDetails = new DatabaseDetails(databasePath, databaseIpPath, connectionString);
            if(saveAndUseDefault) {
                loader.save(root);
                databaseDetails = createDefaultDetails();
                return new LocalStorageIO();
            }
            switch (databaseType.toLowerCase(Locale.ROOT)) {
                case "local" -> {
                    return new LocalStorageIO();
                }
                case "sqlite" -> {
                    return new SQLiteStorageIO();
                }
                case "mongodb" -> {
                    return new MongoDBIO();
                }
                default -> {
                    logger.warn("Unknown database type " + databaseType.toLowerCase(Locale.ROOT) + ", defaulting to local storage...");
                    return new LocalStorageIO();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        databaseDetails = createDefaultDetails();
        return new LocalStorageIO();
    }

    public EnumMap<Permissions, String> loadPermissionsFromConfig() {
        EnumMap<Permissions, String> permissions = new EnumMap<>(Permissions.class);
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(bansConfigPath).build();
        boolean shouldSave = false;
        try {
            CommentedConfigurationNode rootNode = loader.load();
            for(Permissions permission : Permissions.values()) {
                String permissionName = permission.name().toLowerCase(Locale.ROOT).replace("_", "-");
                String configString = permissionName + "-permission";
                String defaultName = "minestom." + permissionName;
                CommentedConfigurationNode node = rootNode.node(configString);
                if(node.empty()) {
                    logger.warn("Either " + configString + " does not exist in the config, or it isn't a string! Correcting...");
                    node.set(defaultName);
                    permissions.put(permission, defaultName);
                    shouldSave = true;
                } else {
                    String name = node.getString(defaultName);
                    permissions.put(permission, name);
                }
            }
            if(shouldSave) {
                loader.save(rootNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public DatabaseDetails getDatabaseDetails() {
        return databaseDetails;
    }

    private DatabaseDetails createDefaultDetails() {
        return new DatabaseDetails("bans", "ip-bans", "");
    }
}
