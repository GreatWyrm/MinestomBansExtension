package com.arcanewarrior;

import com.arcanewarrior.commands.Permissions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {


    private static final Path BANS_ROOT_FOLDER = BansExtension.getInstance().getDataDirectory();
    private static final Path BANS_DATA_FILE = BANS_ROOT_FOLDER.resolve("bans.json");
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
        if(!Files.exists(BANS_DATA_FILE)) {
            BansExtension.getInstance().getLogger().info("Banlist file not found! Creating...");
            try {
                Files.createFile(BANS_DATA_FILE);
                // Write out empty node to file
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                mapper.writeValue(Files.newBufferedWriter(BANS_DATA_FILE), node);
            } catch (IOException e) {
                BansExtension.getInstance().getLogger().info("Failed to create banlist file!");
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

    public Map<UUID, BanDetails> loadBanListFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<UUID, BanDetails> list = new HashMap<>();
        try {
            JsonNode banList = mapper.readTree(Files.newBufferedReader(BANS_DATA_FILE));
            for (Iterator<Map.Entry<String, JsonNode>> iterator = banList.fields(); iterator.hasNext(); ) {
                Map.Entry<String, JsonNode> node = iterator.next();
                // String should be UUID, JsonNode should contain ban reason and username
                UUID id = UUID.fromString(node.getKey());
                String banReason = node.getValue().get("reason").asText("The Ban Hammer has spoken!");
                String username = node.getValue().get("username").asText("Error: Username not Found!");
                list.put(id, new BanDetails(username, banReason));
            }
        } catch (IOException e) {
            BansExtension.getInstance().getLogger().warn("Failed to load in ban list file!");
            e.printStackTrace();
        }
        return list;
    }

    public void addBannedPlayerToConfig(@NotNull Player player, String reason) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode parentNode = mapper.createObjectNode();
        ObjectNode childNode = mapper.createObjectNode();
        childNode.put("reason", reason);
        childNode.put("username", player.getUsername());
        parentNode.set(player.getUuid().toString(), childNode);

        ObjectReader reader = mapper.readerForUpdating(parentNode);
        try {
            JsonNode entireFile = reader.readTree(Files.newBufferedReader(BANS_DATA_FILE));
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(Files.newBufferedWriter(BANS_DATA_FILE), entireFile);
        } catch (IOException e) {
            BansExtension.getInstance().getLogger().warn("Failed to add player " + player.getUsername() + " to the ban list file.");
            e.printStackTrace();
        }
    }

    public void removeBannedPlayerFromConfig(@NotNull UUID id) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode nodes = mapper.readTree(Files.newBufferedReader(BANS_DATA_FILE));
            if(nodes instanceof ObjectNode objectNode) {
                objectNode.remove(id.toString());
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(Files.newBufferedWriter(BANS_DATA_FILE), nodes);
            } else {
                BansExtension.getInstance().getLogger().warn("Could not modify bans list file, as it was not an instance of ObjectNode!");
            }
        } catch (IOException e) {
            BansExtension.getInstance().getLogger().warn("Failed to remove UUID " + id + " to the ban list file.");
            e.printStackTrace();
        }
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
