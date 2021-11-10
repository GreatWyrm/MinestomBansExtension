package com.arcanewarrior;

import com.arcanewarrior.commands.Permissions;
import com.arcanewarrior.storage.LocalStorageIO;
import com.arcanewarrior.storage.StorageIO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;

public class ConfigManager {


    private static final Path BANS_ROOT_FOLDER = BansExtension.getInstance().getDataDirectory();
    private static final Path BANS_CONFIG_FILE = BANS_ROOT_FOLDER.resolve("config.json");

    public ConfigManager() {
        ensureFilesExist();
    }

    private void ensureFilesExist() {
        if(!Files.exists(BANS_ROOT_FOLDER)) {
            BansExtension.getInstance().getLogger().info("Config folder not found! Creating...");
            try {
                Files.createDirectory(BANS_ROOT_FOLDER);
            } catch (IOException e) {
                BansExtension.getInstance().getLogger().info("Failed to create root directory!");
                e.printStackTrace();
            }
        }
        if(!Files.exists(BANS_CONFIG_FILE)) {
            BansExtension.getInstance().getLogger().info("Bans Config file not found! Creating...");
            InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.json");
            try {
                if (input != null) {
                    Files.copy(input, BANS_CONFIG_FILE);
                } else {
                    BansExtension.getInstance().getLogger().warn("config.json resource inputstream was null???");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public StorageIO getStorageIO() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            JsonNode rootNode = mapper.readTree(Files.newBufferedReader(BANS_CONFIG_FILE));
            // Look for a database json object
            JsonNode databaseNode = rootNode.get("database");
            if(databaseNode == null || !databaseNode.isObject()) {
                BansExtension.getInstance().getLogger().warn("Either database field does not exist or is not an object! Fixing config and using local storage.");
                if(rootNode instanceof ObjectNode objectNode) {
                    ObjectNode newNode = mapper.createObjectNode();
                    newNode.put("type", "local");
                    objectNode.set("database", newNode);
                    mapper.writeValue(Files.newBufferedWriter(BANS_CONFIG_FILE), rootNode);
                } else {
                    BansExtension.getInstance().getLogger().warn("Failed to correct database field!");
                }
                return new LocalStorageIO();
            }
            // Database Field exists and is an object, proceed with reading
            JsonNode typeField = databaseNode.get("type");
            if(typeField == null || !typeField.isTextual()) {
                BansExtension.getInstance().getLogger().warn("Either type field does not exist or is not an string! Fixing config and using local storage.");
                if(databaseNode instanceof ObjectNode objectNode && rootNode instanceof ObjectNode rootObject) {
                    objectNode.put("type", "local");
                    rootObject.replace("database", objectNode);
                    mapper.writeValue(Files.newBufferedWriter(BANS_CONFIG_FILE), rootNode);
                } else {
                    BansExtension.getInstance().getLogger().warn("Failed to correct database field!");
                }
                return new LocalStorageIO();
            }
            switch (typeField.asText().toLowerCase()) {
                case "local" -> {
                    return new LocalStorageIO();
                }
                default -> {
                    BansExtension.getInstance().getLogger().warn("Unknown database type " + typeField.asText() + " defaulting to local storage...");
                    return new LocalStorageIO();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LocalStorageIO();
    }

    public EnumMap<Permissions, String> loadPermissionsFromConfig() {
        EnumMap<Permissions, String> permissions = new EnumMap<>(Permissions.class);
        ObjectMapper mapper = new ObjectMapper();
        boolean shouldFixConfig = false;
        try {
            JsonNode rootNode = mapper.readTree(Files.newBufferedReader(BANS_CONFIG_FILE));
            for(Permissions permission : Permissions.values()) {
                String configString = permission.name().toLowerCase() + "-permission";
                String defaultName = "minestom." + permission.name().toLowerCase();
                JsonNode node = rootNode.get(configString);
                if(node == null || !node.isTextual()) {
                    BansExtension.getInstance().getLogger().warn("Either " + configString + " does not exist in the config, or it isn't a string! Correcting...");
                    permissions.put(permission, defaultName);
                    shouldFixConfig = true;
                    if(rootNode instanceof ObjectNode objectNode) {
                        objectNode.put(configString, defaultName);
                    }
                    continue;
                }
                permissions.put(permission, node.asText(defaultName));
            }
            if(shouldFixConfig) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(Files.newBufferedWriter(BANS_CONFIG_FILE), rootNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return permissions;
    }
}
