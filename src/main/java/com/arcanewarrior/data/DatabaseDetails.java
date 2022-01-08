package com.arcanewarrior.data;

// Holds all the information contained in the parameters section of the database part of the config.yml
public record DatabaseDetails(
        // Local Path on filesystem for player bans
        String playerBanPath,
        // Local Path on filesystem for ip bans
        String ipBanPath,
        // Used to connect to a remote database
        String connectionString
) {
}
