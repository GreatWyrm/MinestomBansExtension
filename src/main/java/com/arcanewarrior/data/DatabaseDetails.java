package com.arcanewarrior.data;

// Holds all the information contained in the parameters section of the database part of the config.yml
public record DatabaseDetails(
        // Local Path on filesystem
        String path,
        // Used to connect to a remote database
        String connectionString
) {
}
