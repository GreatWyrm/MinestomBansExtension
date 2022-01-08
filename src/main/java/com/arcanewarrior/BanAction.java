package com.arcanewarrior;

import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.entity.Player;

import java.net.SocketAddress;
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
        BanDetails details = dataManager.addBannedPlayer(player, reason);
        if(details != null) {
            storageIO.saveBannedPlayerToStorage(details);
        }
    }

    public void addBannedPlayer(UUID id, String username, String reason) {
        BanDetails details = dataManager.addBannedPlayer(id, username, reason);
        if(details != null) {
            storageIO.saveBannedPlayerToStorage(details);
        }
    }

    public void unbanPlayer(String username) {
        UUID id = dataManager.removeBannedPlayer(username);
        if(id != null) {
            storageIO.removeBannedPlayerFromStorage(id);
        }
    }

    public void addBannedIP(SocketAddress address, String reason) {
        String ipStringAddress = dataManager.addBannedIP(address, reason);
        if(ipStringAddress != null) {
            storageIO.saveBannedIpToStorage(ipStringAddress, reason);
        }
    }

    public void unbanIpAddress(String address) {
        String ipAddress = dataManager.removeBannedIP(address);
        if(ipAddress != null) {
            storageIO.removeBannedIpFromStorage(address);
        }
    }

    public List<String> getBannedIPs() {
        return dataManager.getBannedIps();
    }

    public List<String> getBannedPlayerNames() {
        return dataManager.getBannedUsernames();
    }
}
