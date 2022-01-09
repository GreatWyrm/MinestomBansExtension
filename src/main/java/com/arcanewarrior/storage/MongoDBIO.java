package com.arcanewarrior.storage;

import com.arcanewarrior.UUIDUtils;
import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.data.DatabaseDetails;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MongoDBIO implements StorageIO {

    private ConnectionString mongoConnectionString;

    private final String databaseName = "bans";
    private final String playerCollectionName = "banlist";
    private final String ipCollectionName = "ipbanlist";

    @Override
    public void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details) {
        mongoConnectionString = new ConnectionString(details.connectionString());
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        // Get collections to ensure they exist
        var collection = database.getCollection(playerCollectionName);
        var ipCollection = database.getCollection(ipCollectionName);
        mongoClient.close();
    }

    @Override
    public Map<UUID, BanDetails> loadPlayerBansFromStorage() {
        HashMap<UUID, BanDetails> detailsMap = new HashMap<>();
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(playerCollectionName);
        Bson projectionFields = Projections.fields(Projections.excludeId());
        for (Document next : collection.find()
                .projection(projectionFields)) {
            BanDetails details = convertPlayerBanFromDocument(next);
            if (details != null) {
                detailsMap.put(details.uuid(), details);
            }
        }
        mongoClient.close();
        return detailsMap;
    }


    @Override
    public Map<String, String> loadIpBansFromStorage() {
        HashMap<String, String> detailsMap = new HashMap<>();
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(ipCollectionName);
        Bson projectionFields = Projections.fields(Projections.excludeId());
        for (Document next : collection.find()
                .projection(projectionFields)) {
            StringPair details = convertIpBanFromDocument(next);
            if (details != null) {
                detailsMap.put(details.ip(), details.reason());
            }
        }
        mongoClient.close();
        return detailsMap;
    }

    @Override
    public void saveBannedPlayerToStorage(@NotNull BanDetails banDetails) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(playerCollectionName);
        collection.insertOne(convertPlayerBanToDocument(banDetails));
        mongoClient.close();
    }

    @Override
    public void removeBannedPlayerFromStorage(@NotNull UUID id) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(playerCollectionName);
        Bson query = Filters.eq(uuidFieldName, UUIDUtils.stripDashesFromUUID(id));
        try {
            collection.deleteOne(query);
        } catch (MongoException me) {
            me.printStackTrace();
        }
        mongoClient.close();
    }

    @Override
    public void saveBannedIpToStorage(@NotNull String ipString, @NotNull String reasonString) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(ipCollectionName);
        collection.insertOne(convertIpBanToDocument(ipString, reasonString));
        mongoClient.close();
    }

    @Override
    public void removeBannedIpFromStorage(@NotNull String ipString) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(ipCollectionName);
        Bson query = Filters.eq(ipFieldName, ipString);
        try {
            collection.deleteOne(query);
        } catch (MongoException me) {
            me.printStackTrace();
        }
        mongoClient.close();
    }

    private MongoClient createClient() {
        return MongoClients.create(mongoConnectionString);
    }


    private final String uuidFieldName = "uuid";
    private final String usernameFieldName = "username";
    private final String reasonFieldName = "reason";
    private final String ipFieldName = "ip";

    private Document convertPlayerBanToDocument(BanDetails details) {
        return new Document(
                uuidFieldName, UUIDUtils.stripDashesFromUUID(details.uuid())
        ).append(
                usernameFieldName, details.bannedUsername()
        ).append(
                reasonFieldName, details.banReason()
        );
    }

    private BanDetails convertPlayerBanFromDocument(Document document) {
        if(document.containsKey(uuidFieldName)) {
            return new BanDetails(
                    UUIDUtils.makeUUIDFromStringWithoutDashes(document.getString(uuidFieldName)),
                    document.getString(usernameFieldName),
                    document.getString(reasonFieldName)
            );
        } else {
            return null;
        }
    }

    private Document convertIpBanToDocument(String ip, String reason) {
        return new Document(
                ipFieldName, ip
        ).append(
                reasonFieldName, reason
        );
    }

    private StringPair convertIpBanFromDocument(Document document) {
        if(document.containsKey(ipFieldName)) {
            return new StringPair(
                   document.getString(ipFieldName),
                    document.getString(reasonFieldName)
            );
        } else {
            return null;
        }
    }

    private record StringPair(String ip, String reason) {}
}
