package com.arcanewarrior;

import com.arcanewarrior.data.PermanentBanRecord;
import com.arcanewarrior.data.TemporaryBanRecord;
import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.entity.Player;

import java.net.SocketAddress;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.UUID;

public class BanAction {

    private final StorageIO storageIO;
    private final DataManager dataManager;

    public BanAction(StorageIO storageIO, DataManager dataManager) {
        this.storageIO = storageIO;
        this.dataManager = dataManager;
    }

    public boolean permanentBanPlayer(Player player, String reason, String banExecutor) {
        return permanentBanPlayer(player.getUuid(), player.getUsername(), reason, banExecutor);
    }
    public boolean permanentBanPlayer(UUID id, String username, String reason, String banExecutor) {
        PermanentBanRecord record = dataManager.addPermanentPlayerBan(id, username, reason, banExecutor);
        if(record != null) {
            storageIO.saveBan(record);
            return true;
        }
        return false;
    }
    public boolean temporaryBanPlayer(Player player, String reason, String banExecutor, TemporalAmount duration) {
        return temporaryBanPlayer(player.getUuid(), player.getUsername(), reason, banExecutor, duration);
    }

    public boolean temporaryBanPlayer(UUID id, String username, String reason, String banExecutor, TemporalAmount duration) {
        TemporaryBanRecord record = dataManager.addTemporaryPlayerBan(id, username, reason, banExecutor, duration);
        if(record != null) {
            storageIO.saveBan(record);
            return true;
        }
        return false;
    }

    public void unbanPlayer(String username) {
        UUID id = dataManager.removeBannedPlayer(username);
        if(id != null) {
            storageIO.removeBan(id);
        }
    }

    public void addBannedIP(SocketAddress address, String reason) {
        String ipStringAddress = dataManager.addBannedIP(address, reason);
        if(ipStringAddress != null) {
            storageIO.saveBannedIp(ipStringAddress, reason);
        }
    }

    public void unbanIpAddress(String address) {
        String ipAddress = dataManager.removeBannedIP(address);
        if(ipAddress != null) {
            storageIO.removeBannedIp(address);
        }
    }

    public List<String> getBannedIPs() {
        return dataManager.getBannedIps();
    }

    public List<String> getBannedPlayerNames() {
        return dataManager.getBannedUsernames();
    }
}
