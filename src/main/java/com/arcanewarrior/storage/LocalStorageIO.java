package com.arcanewarrior.storage;

import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.data.DatabaseDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
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
                // Write out empty node to file
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                mapper.writeValue(Files.newBufferedWriter(bansDataFile), node);
            } catch (IOException e) {
                logger.info("Failed to create banlist file!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<UUID, BanDetails> loadAllBansFromStorage() {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<UUID, BanDetails> list = new HashMap<>();
        try {
            JsonNode banList = mapper.readTree(Files.newBufferedReader(bansDataFile));
            for (Iterator<Map.Entry<String, JsonNode>> iterator = banList.fields(); iterator.hasNext(); ) {
                Map.Entry<String, JsonNode> node = iterator.next();
                // String should be UUID, JsonNode should contain ban reason and username
                UUID id = UUID.fromString(node.getKey());
                String banReason = node.getValue().get("reason").asText("The Ban Hammer has spoken!");
                String username = node.getValue().get("username").asText("Error: Username not Found!");
                list.put(id, new BanDetails(id, username, banReason));
            }
        } catch (IOException e) {
            logger.warn("Failed to load in ban list file!");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void saveBannedPlayerToStorage(@NotNull BanDetails details) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode parentNode = mapper.createObjectNode();
        ObjectNode childNode = mapper.createObjectNode();
        childNode.put("reason", details.banReason());
        childNode.put("username", details.bannedUsername());
        parentNode.set(details.uuid().toString(), childNode);

        ObjectReader reader = mapper.readerForUpdating(parentNode);
        try {
            JsonNode entireFile = reader.readTree(Files.newBufferedReader(bansDataFile));
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(Files.newBufferedWriter(bansDataFile), entireFile);
        } catch (IOException e) {
            logger.warn("Failed to add player " + details.bannedUsername() + " to the ban list file.");
            e.printStackTrace();
        }
    }

    @Override
    public void removeBannedPlayerFromStorage(@NotNull UUID id) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode nodes = mapper.readTree(Files.newBufferedReader(bansDataFile));
            if(nodes instanceof ObjectNode objectNode) {
                objectNode.remove(id.toString());
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(Files.newBufferedWriter(bansDataFile), nodes);
            } else {
                logger.warn("Could not modify bans list file, as it was not an instance of ObjectNode!");
            }
        } catch (IOException e) {
            logger.warn("Failed to remove UUID " + id + " to the ban list file.");
            e.printStackTrace();
        }
    }
}
