package com.arcanewarrior.storage;

import com.arcanewarrior.UUIDUtils;
import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.data.DatabaseDetails;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLiteStorageIO implements StorageIO {

    private String sqLitePath;
    private final String TABLE_NAME = "BANLIST";
    private final String IP_TABLE_NAME = "IPBANLIST";
    private final String uuidFieldName = "UUID";
    private final String ipFieldName = "IP";
    private final String usernameFieldName = "username";
    private final String banReasonFieldName = "banreason";
    private final int banReasonMaxLength = 100;

    @Override
    public void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details) {
        String path = details.playerBanPath();
        if(!path.endsWith(".db")) {
            path += ".db";
        }
        try {
            Class.forName("org.sqlite.JDBC");
            sqLitePath = "jdbc:sqlite:" + rootExtensionFolder.resolve(path).toAbsolutePath();
            Connection connection = DriverManager.getConnection(sqLitePath);
            String playerTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                              "(" + uuidFieldName + " char(36) PRIMARY KEY NOT NULL," +
                              usernameFieldName + " varchar(16) NOT NULL," +
                              banReasonFieldName + " varchar(" + banReasonMaxLength + "))";
            // TODO: For backwards compatibility, add ALTER TABLE statements here if the BanDetails gets any more information
            // For example, a ban date
            String ipTableQuery = "CREATE TABLE IF NOT EXISTS " + IP_TABLE_NAME +
                    "(" + ipFieldName + " varchar(50) PRIMARY KEY NOT NULL," +
                    banReasonFieldName + " varchar(" + banReasonMaxLength + "))";
            Statement statement = connection.createStatement();
            statement.executeUpdate(playerTableQuery);
            statement.executeUpdate(ipTableQuery);
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, BanDetails> loadPlayerBansFromStorage() {
        HashMap<UUID, BanDetails> map = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String query = "SELECT * FROM " + TABLE_NAME + ";";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()) {
                String id = rs.getString(uuidFieldName);
                String username = rs.getString(usernameFieldName);
                String banReason = rs.getString(banReasonFieldName);
                UUID uuid = UUIDUtils.makeUUIDFromStringWithoutDashes(id);
                map.put(uuid, new BanDetails(uuid, username, banReason));
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }


    @Override
    public Map<String, String> loadIpBansFromStorage() {
        HashMap<String, String> map = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String query = "SELECT * FROM " + IP_TABLE_NAME + ";";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()) {
                String id = rs.getString(ipFieldName);
                String banReason = rs.getString(banReasonFieldName);
                map.put(id, banReason);
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void saveBannedPlayerToStorage(@NotNull BanDetails details) {
        String reason = details.banReason();
        if(reason.length() > banReasonMaxLength) {
            reason = reason.substring(0, banReasonMaxLength);
        }
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String queryPrepared = "INSERT INTO " + TABLE_NAME + " (" + uuidFieldName + ", " + usernameFieldName + ", " + banReasonFieldName + ") VALUES (?,?,?)";
            PreparedStatement statement = connection.prepareStatement(queryPrepared);
            statement.setString(1, UUIDUtils.stripDashesFromUUID(details.uuid())); // Statements start from 1 >:(
            statement.setString(2, details.bannedUsername());
            statement.setString(3, reason);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBannedPlayerFromStorage(@NotNull UUID id) {
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String query = "DELETE from " + TABLE_NAME + " where " + uuidFieldName + "=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, UUIDUtils.stripDashesFromUUID(id));
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveBannedIpToStorage(@NotNull String ipString, @NotNull String reasonString) {
        if(reasonString.length() > banReasonMaxLength) {
            reasonString = reasonString.substring(0, banReasonMaxLength);
        }
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String queryPrepared = "INSERT INTO " + IP_TABLE_NAME + " (" + ipFieldName + ", " + banReasonFieldName + ") VALUES (?,?)";
            PreparedStatement statement = connection.prepareStatement(queryPrepared);
            statement.setString(1, ipString); // Statements start from 1 >:(
            statement.setString(2, reasonString);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBannedIpFromStorage(@NotNull String ipString) {
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String query = "DELETE from " + IP_TABLE_NAME + " where " + ipString + "=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, ipString);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}