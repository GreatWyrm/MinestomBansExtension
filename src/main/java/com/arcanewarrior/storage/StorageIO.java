package com.arcanewarrior.storage;

import com.arcanewarrior.BanDetails;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface StorageIO {
    /**
     * Called when the storage is first instantiated, creates any necessary files/tables if they already do not exist
     */
    void initializeIfEmpty();
    /**
     * Called when extension loads, caches all ban details from storage into the extension itself
     * @return A Map that contains the UUIDs of banned players mapped with the BanDetails record class
     */
    Map<UUID, BanDetails> loadAllBansFromStorage();

    /**
     * Saves a banned player to storage
     * @param player The player that has been banned
     * @param reason The reason for the ban
     */
    void saveBannedPlayerToStorage(@NotNull Player player, String reason);

    /**
     * Removes a banned player from storage
     * @param id The UUID of the banned player
     */
    void removeBannedPlayerFromStorage(@NotNull UUID id);
}
