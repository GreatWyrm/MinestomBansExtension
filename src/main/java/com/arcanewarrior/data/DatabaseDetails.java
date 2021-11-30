package com.arcanewarrior.data;

// Holds all the information contained in the parameters section of the database part of the config.json
public record DatabaseDetails(
        // Local Path on filesystem
        String path,
        // Username for an online system
        String username,
        // Password for an online system
        String password,
        // Name of database/cluster for online system
        String databaseName
) {
}
