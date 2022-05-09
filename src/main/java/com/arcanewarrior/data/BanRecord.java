package com.arcanewarrior.data;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a record of a Ban
 */
public interface BanRecord {

    /**
     * Gets the UUID associated with this ban record
     * @return The UUID of the banned player
     */
    UUID uuid();

    /**
     * Gets the username associated with the ban record
     * @return The username of the banned player
     */
    String username();

    /**
     * Describes whether the player should be considered banned by this ban record. Will be always true for permanent bans, and false for temporary bans once they have expired
     * @return Whether the player is considered banned
     */
    boolean isPlayerBanned();

    /**
     * Gets a list of the properties of this record, arranged as property name mapped to string value
     * @return A list of the property names mapped to their values
     */
    Map<String, String> toPropertiesMap();
}
