package com.arcanewarrior;

import com.arcanewarrior.commands.Permissions;
import com.arcanewarrior.storage.LocalStorageIO;
import com.arcanewarrior.storage.MongoDBIO;
import com.arcanewarrior.storage.SQLiteStorageIO;
import com.arcanewarrior.storage.StorageIO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;

public class BansConfig {

    private final Logger logger = LoggerFactory.getLogger(BansConfig.class);

    private final Path bansRootFolder;
    private final Path bansConfigPath;
    private DatabaseDetails databaseDetails;

    public BansConfig(Path rootPath) {
        bansRootFolder = rootPath;
        bansConfigPath = rootPath.resolve("config.json");
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
            InputStream input = BansConfig.class.getClassLoader().getResourceAsStream("config.json");
            try {
                if (input != null) {
                    Files.copy(input, bansConfigPath);
                } else {
                    logger.warn("config.json resource inputstream was null???");
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
            JsonNode rootNode = mapper.readTree(Files.newBufferedReader(bansConfigPath));
            // Look for a database json object
            JsonNode databaseNode = rootNode.get("database");
            if(databaseNode == null || !databaseNode.isObject()) {
                logger.warn("Either database field does not exist or is not an object! Fixing config and using local storage.");
                if(rootNode instanceof ObjectNode objectNode) {
                    ObjectNode newNode = mapper.createObjectNode();
                    newNode.put("type", "local");
                    objectNode.set("database", newNode);
                    mapper.writeValue(Files.newBufferedWriter(bansConfigPath), rootNode);
                } else {
                    logger.warn("Failed to correct database field!");
                }
                databaseDetails = createDefaultDetails();
                return new LocalStorageIO();
            }
            // Database Field exists and is an object, proceed with reading
            JsonNode typeField = databaseNode.get("type");
            if(typeField == null || !typeField.isTextual()) {
                logger.warn("Either type field does not exist or is not an string! Fixing config and using local storage.");
                if(databaseNode instanceof ObjectNode objectNode && rootNode instanceof ObjectNode rootObject) {
                    objectNode.put("type", "local");
                    rootObject.replace("database", objectNode);
                    mapper.writeValue(Files.newBufferedWriter(bansConfigPath), rootNode);
                } else {
                    logger.warn("Failed to correct type field!");
                }
                databaseDetails = createDefaultDetails();
                return new LocalStorageIO();
            }
            JsonNode parametersField = databaseNode.get("parameters");
            if(parametersField == null || !parametersField.isObject()) {
                logger.warn("Either parameters field does not exist or is not an POJO! Fixing config.");
                databaseDetails = createDefaultDetails();
            } else {
                databaseDetails = mapper.reader().readValue(parametersField, DatabaseDetails.class);
            }
            // Always write out value to update config in case of changes
            if(databaseNode instanceof ObjectNode objectNode && rootNode instanceof ObjectNode rootObject) {
                objectNode.putPOJO("parameters", databaseDetails);
                rootObject.replace("database", objectNode);
                mapper.writeValue(Files.newBufferedWriter(bansConfigPath), rootNode);
            } else {
                logger.warn("Failed to update parameters field!");
            }
            switch (typeField.asText().toLowerCase()) {
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
                    logger.warn("Unknown database type " + typeField.asText() + " defaulting to local storage...");
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
        ObjectMapper mapper = new ObjectMapper();
        boolean shouldFixConfig = false;
        try {
            JsonNode rootNode = mapper.readTree(Files.newBufferedReader(bansConfigPath));
            for(Permissions permission : Permissions.values()) {
                String configString = permission.name().toLowerCase() + "-permission";
                String defaultName = "minestom." + permission.name().toLowerCase();
                JsonNode node = rootNode.get(configString);
                if(node == null || !node.isTextual()) {
                    logger.warn("Either " + configString + " does not exist in the config, or it isn't a string! Correcting...");
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
                mapper.writeValue(Files.newBufferedWriter(bansConfigPath), rootNode);
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
        return new DatabaseDetails("bans", "", "", "");
    }
}
