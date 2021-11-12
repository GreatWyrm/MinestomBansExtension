package com.arcanewarrior;

import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.entity.Player;

import java.util.List;
import java.util.UUID;

public class BanAction {

    private final StorageIO storageIO;
    private final DataManager dataManager;

    public BanAction(StorageIO storageIO, DataManager dataManager) {
        this.storageIO = storageIO;
        this.dataManager = dataManager;
    }

    public void addBannedPlayer(Player player, String reason) {
        dataManager.addBannedPlayer(player, reason);
        storageIO.saveBannedPlayerToStorage(player, reason);
    }

    public void unbanPlayer(String username) {
        UUID id = dataManager.removeBannedPlayer(username);
        if(id != null) {
            storageIO.removeBannedPlayerFromStorage(id);
        }
    }

    public List<String> getBannedPlayerNames() {
        return dataManager.getBannedUsernames();
    }
}
