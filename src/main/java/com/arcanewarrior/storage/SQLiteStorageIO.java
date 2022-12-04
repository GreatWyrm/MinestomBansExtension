package com.arcanewarrior.storage;

import com.arcanewarrior.UUIDUtils;
import com.arcanewarrior.data.BanRecord;
import com.arcanewarrior.data.DatabaseDetails;
import com.arcanewarrior.data.PermanentBanRecord;
import com.arcanewarrior.data.TemporaryBanRecord;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.*;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private final String banExecutorFieldName = "executor";
    private final String banTimeFieldName = "banTime";
    private final String expiryTimeFieldName = "expiryTime";
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
                              banExecutorFieldName + " varchar(30) NOT NULL," +
                              banTimeFieldName + " varchar(50) NOT NULL, " +
                              expiryTimeFieldName + " varchar(50), " +
                              banReasonFieldName + " varchar(" + banReasonMaxLength + "))";
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
    public Map<UUID, BanRecord> loadBans() {
        HashMap<UUID, BanRecord> map = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String query = "SELECT * FROM " + TABLE_NAME + ";";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()) {
                String id = rs.getString(uuidFieldName);
                String username = rs.getString(usernameFieldName);
                String banReason = rs.getString(banReasonFieldName);
                String banExecutor = rs.getString(banExecutorFieldName);
                String banTime = rs.getString(banTimeFieldName);
                String banExpiry = rs.getString(expiryTimeFieldName);
                UUID uuid = UUIDUtils.makeUUIDFromStringWithoutDashes(id);
                if (banExpiry == null) {
                    map.put(uuid, new PermanentBanRecord(uuid, username, banReason, banExecutor, ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(banTime))));
                } else {
                    map.put(uuid, new TemporaryBanRecord(uuid, username, banReason, banExecutor, ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(banTime)), ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(banExpiry))));
                }
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException | DateTimeException e) {
            e.printStackTrace();
        }
        return map;
    }


    @Override
    public Map<String, String> loadIpBans() {
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
    public void saveBan(@NotNull BanRecord details) {
        String reason = details.banReason();
        if(reason.length() > banReasonMaxLength) {
            reason = reason.substring(0, banReasonMaxLength);
        }
        try {
            Connection connection = DriverManager.getConnection(sqLitePath);
            String queryPrepared = "INSERT INTO " + TABLE_NAME + " (" + uuidFieldName + ", " + usernameFieldName + ", " + banExecutorFieldName + ", " + banTimeFieldName + ", " + expiryTimeFieldName + ", " + banReasonFieldName + ") VALUES (?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(queryPrepared);
            if(details instanceof TemporaryBanRecord temporaryBanRecord) {
                statement.setString(1, UUIDUtils.stripDashesFromUUID(details.uuid())); // Statements start from 1 >:(
                statement.setString(2, details.username());
                statement.setString(3, temporaryBanRecord.banExecutor());
                statement.setString(4, DateTimeFormatter.ISO_ZONED_DATE_TIME.format(temporaryBanRecord.banTime()));
                statement.setString(5, DateTimeFormatter.ISO_ZONED_DATE_TIME.format(temporaryBanRecord.expiryTime()));
                statement.setString(6, reason);
            } else if(details instanceof PermanentBanRecord permanentBanRecord) {
                statement.setString(1, UUIDUtils.stripDashesFromUUID(details.uuid())); // Statements start from 1 >:(
                statement.setString(2, details.username());
                statement.setString(3, permanentBanRecord.banExecutor());
                statement.setString(4, DateTimeFormatter.ISO_ZONED_DATE_TIME.format(permanentBanRecord.banTime()));
                statement.setString(5, null);
                statement.setString(6, reason);
                statement.executeUpdate();
                statement.close();
            } else {
                return;
            }
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBan(@NotNull UUID id) {
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
    public void saveBannedIp(@NotNull String ipString, @NotNull String reasonString) {
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
    public void removeBannedIp(@NotNull String ipString) {
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