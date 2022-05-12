package com.arcanewarrior.storage;

import com.arcanewarrior.UUIDUtils;
import com.arcanewarrior.data.BanRecord;
import com.arcanewarrior.data.DatabaseDetails;
import com.arcanewarrior.data.PermanentBanRecord;
import com.arcanewarrior.data.TemporaryBanRecord;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MongoDBIO implements StorageIO {

    private ConnectionString mongoConnectionString;

    private final String databaseName = "bans";
    private final String bansCollectionName = "banlist";
    private final String ipCollectionName = "ipbanlist";

    @Override
    public void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details) {
        mongoConnectionString = new ConnectionString(details.connectionString());
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        // Get collections to ensure they exist
        var collection = database.getCollection(bansCollectionName);
        var ipCollection = database.getCollection(ipCollectionName);
        mongoClient.close();
    }

    @Override
    public Map<UUID, BanRecord> loadBans() {
        HashMap<UUID, BanRecord> detailsMap = new HashMap<>();
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(bansCollectionName);
        Bson projectionFields = Projections.fields(Projections.excludeId());
        for (Document next : collection.find()
                .projection(projectionFields)) {
            BanRecord details = convertPlayerBanFromDocument(next);
            if (details != null) {
                detailsMap.put(details.uuid(), details);
            }
        }
        mongoClient.close();
        return detailsMap;
    }


    @Override
    public Map<String, String> loadIpBans() {
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
    public void saveBan(@NotNull BanRecord banRecord) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(bansCollectionName);
        collection.insertOne(convertPlayerBanToDocument(banRecord));
        mongoClient.close();
    }

    @Override
    public void removeBan(@NotNull UUID id) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(bansCollectionName);
        Bson query = Filters.eq(uuidFieldName, UUIDUtils.stripDashesFromUUID(id));
        try {
            collection.deleteOne(query);
        } catch (MongoException me) {
            me.printStackTrace();
        }
        mongoClient.close();
    }

    @Override
    public void saveBannedIp(@NotNull String ipString, @NotNull String reasonString) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(ipCollectionName);
        collection.insertOne(convertIpBanToDocument(ipString, reasonString));
        mongoClient.close();
    }

    @Override
    public void removeBannedIp(@NotNull String ipString) {
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
    private final String reasonFieldName = "banReason";
    private final String executorFieldName = "banExecutor";
    private final String banTimeFieldName = "banTime";
    private final String expiryTimeFieldName = "expiryTime";
    private final String ipFieldName = "ip";

    private Document convertPlayerBanToDocument(BanRecord details) {
        Document document = new Document();
        for(var test : details.toPropertiesMap().entrySet()) {
            document.append(test.getKey(), test.getValue());
        }
        return document;
    }

    private BanRecord convertPlayerBanFromDocument(Document document) {
        if(document.containsKey(uuidFieldName)) {
            UUID id = UUIDUtils.makeUUIDFromStringWithoutDashes(document.getString(uuidFieldName));
            String username = document.containsKey(usernameFieldName) ? document.getString(usernameFieldName) : "Unknown";
            String executor = document.containsKey(executorFieldName) ? document.getString(executorFieldName) : "Unknown";
            String reason = document.containsKey(reasonFieldName) ? document.getString(reasonFieldName) : "The Ban Hammer has Spoken!";
            ZonedDateTime banTime = document.containsKey(banTimeFieldName) ? ZonedDateTime.parse(document.getString(banTimeFieldName), DateTimeFormatter.ISO_ZONED_DATE_TIME) : ZonedDateTime.now();
            if(document.containsKey(expiryTimeFieldName)) {
                ZonedDateTime expireTime = document.containsKey(expiryTimeFieldName) ? ZonedDateTime.parse(document.getString(expiryTimeFieldName), DateTimeFormatter.ISO_ZONED_DATE_TIME) : ZonedDateTime.now();
                return new TemporaryBanRecord(
                        id,
                        username,
                        reason,
                        executor,
                        banTime,
                        expireTime
                );
            } else {
                return new PermanentBanRecord(
                       id,
                       username,
                       reason,
                       executor,
                       banTime
                );
            }
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
