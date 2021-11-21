package com.arcanewarrior.storage;

import com.arcanewarrior.BanDetails;
import com.arcanewarrior.DatabaseDetails;
import com.arcanewarrior.UUIDUtils;
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
    private final String collectionName = "banlist";

    @Override
    public void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details) {
        // Connection String - create from username, password, and databaseName
        mongoConnectionString = new ConnectionString("mongodb+srv://" +
                details.username() + ":" + details.password() + "@" + details.databaseName().toLowerCase() +
                ".dtgmi.mongodb.net/" + details.databaseName() + "?retryWrites=true&w=majority");
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(collectionName);
        mongoClient.close();
    }

    @Override
    public Map<UUID, BanDetails> loadAllBansFromStorage() {
        HashMap<UUID, BanDetails> detailsMap = new HashMap<>();
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(collectionName);
        Bson projectionFields = Projections.fields(Projections.excludeId());
        for (Document next : collection.find()
                .projection(projectionFields)) {
            BanDetails details = convertFromDocument(next);
            if (details != null) {
                detailsMap.put(details.uuid(), details);
            }
        }
        mongoClient.close();
        return detailsMap;
    }

    @Override
    public void saveBannedPlayerToStorage(@NotNull BanDetails banDetails) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(collectionName);
        collection.insertOne(convertToDocument(banDetails));
        mongoClient.close();
    }

    @Override
    public void removeBannedPlayerFromStorage(@NotNull UUID id) {
        MongoClient mongoClient = createClient();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        var collection = database.getCollection(collectionName);
        Bson query = Filters.eq(uuidFieldName, UUIDUtils.stripDashesFromUUID(id));
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

    private Document convertToDocument(BanDetails details) {
        return new Document(
                uuidFieldName, UUIDUtils.stripDashesFromUUID(details.uuid())
        ).append(
                usernameFieldName, details.bannedUsername()
        ).append(
                reasonFieldName, details.banReason()
        );
    }

    private BanDetails convertFromDocument(Document document) {
        if(document.containsKey(uuidFieldName)) {
            System.out.println(UUIDUtils.makeUUIDFromStringWithoutDashes(document.getString(uuidFieldName)));
            return new BanDetails(
                    UUIDUtils.makeUUIDFromStringWithoutDashes(document.getString(uuidFieldName)),
                    document.getString(usernameFieldName),
                    document.getString(reasonFieldName)
            );
        } else {
            return null;
        }
    }
}
