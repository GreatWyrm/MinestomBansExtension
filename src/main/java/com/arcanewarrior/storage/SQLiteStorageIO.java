package com.arcanewarrior.storage;

import com.arcanewarrior.BanDetails;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLiteStorageIO implements StorageIO {

    private final String SQLitePath = "jdbc:sqlite:test.db";
    private final String TABLE_NAME = "BANLIST";
    private final int banReasonMaxLength = 100;

    @Override
    public void initializeIfEmpty() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(SQLitePath);
            String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + """
                              (UUID char(36) PRIMARY KEY NOT NULL,
                              username varchar(16) NOT NULL,
                              banreason varchar(""" + banReasonMaxLength + "))";
            //String query = "CREATE TABLE IF NOT EXISTS BANLIST (UUID char(36) PRIMARY KEY NOT NULL, username varchar(16) NOT NULL, banreason varchar(100))";
            // TODO: For backwards compatibility, add ALTER TABLE statements here if the BanDetails gets any more information
            // For example, a ban date
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, BanDetails> loadAllBansFromStorage() {
        HashMap<UUID, BanDetails> map = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection(SQLitePath);
            String query = "SELECT * FROM " + TABLE_NAME + ";";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()) {
                String id = rs.getString("UUID");
                String username = rs.getString("username");
                String banReason = rs.getString("banreason");
                // Much thanks https://stackoverflow.com/questions/18986712/creating-a-uuid-from-a-string-with-no-dashes
                UUID uuid = UUID.fromString(id
                        .replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                        ));
                map.put(uuid, new BanDetails(username, banReason));
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
    public void saveBannedPlayerToStorage(@NotNull Player player, String reason) {
        if(reason.length() > banReasonMaxLength) {
            reason = reason.substring(0, banReasonMaxLength);
        }
        try {
            Connection connection = DriverManager.getConnection(SQLitePath);
            String query = "INSERT INTO " + TABLE_NAME + " (UUID, username, banreason) " +
                    "VALUES ('" + stripUUID(player.getUuid()) + "','" + player.getUsername() + "','" + reason + "');";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBannedPlayerFromStorage(@NotNull UUID id) {
        try {
            Connection connection = DriverManager.getConnection(SQLitePath);
            String query = "DELETE from " + TABLE_NAME + " where UUID='" + stripUUID(id) + "';";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String stripUUID(UUID id) {
        return id.toString().replaceAll("-", "");
    }
}
