package com.arcanewarrior.storage;

import com.arcanewarrior.BanDetails;
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
     */
    void initializeIfEmpty(@NotNull Path rootExtensionFolder);
    /**
     * Called when extension loads, caches all ban details from storage into the extension itself
     * @return A Map that contains the UUIDs of banned players mapped with the BanDetails record class
     */
    Map<UUID, BanDetails> loadAllBansFromStorage();

    /**
     * Saves a banned player to storage
     * @param banDetails All relevant details about the ban
     */
    void saveBannedPlayerToStorage(@NotNull BanDetails banDetails);

    /**
     * Removes a banned player from storage
     * @param id The UUID of the banned player
     */
    void removeBannedPlayerFromStorage(@NotNull UUID id);
}
