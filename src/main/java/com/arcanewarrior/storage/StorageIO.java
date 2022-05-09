package com.arcanewarrior.storage;

import com.arcanewarrior.data.BanRecord;
import com.arcanewarrior.data.DatabaseDetails;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * An interface that defines storage IO functions for bans
 * @author ArcaneWarrior
 */
public interface StorageIO {
    /**
     * Called when the storage is first instantiated, creates any necessary files/tables if they already do not exist
     * @param rootExtensionFolder - The root folder that this extension lives in for file operations
     * @param details - The current details in the config.yml, each implementation of StorageIO can use some, all, or none of these options
     */
    void initializeIfEmpty(@NotNull Path rootExtensionFolder, DatabaseDetails details);
    /**
     * Called when the extension loads, caches all permanent ban details from storage into the extension itself
     * @return A Map that contains the UUIDs of banned players mapped with the PermanentBanRecord or TemporaryBanRecord class
     */
    Map<UUID, BanRecord> loadBans();
    /**
     * Called when the extension loads, caches all ip ban details from the storage into the extension itself
     * @return A Map that contains a string form of an ip address mapped to a string ban reason
     */
    Map<String, String> loadIpBans();

    /**
     * Adds a permanent ban to storage
     * @param banRecord The details of the ban
     */
    void saveBan(@NotNull BanRecord banRecord);

    /**
     * Removes a temporary or permanent ban for this player
      * @param id The uuid of the banned player
     */
    void removeBan(@NotNull UUID id);

    /**
     * Saves a banned ip to storage
     * @param ipString The string representation of the ip address
     * @param reasonString The ban reason string
     */
    void saveBannedIp(@NotNull String ipString, @NotNull String reasonString);

    /**
     * Removes a banned ip from storage
     * @param ipString The string representation of the ip address
     */
    void removeBannedIp(@NotNull String ipString);
}
