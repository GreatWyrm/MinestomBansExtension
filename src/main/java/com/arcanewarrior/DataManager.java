package com.arcanewarrior;

import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Map<UUID, BanDetails> banList = new HashMap<>();

    public DataManager(StorageIO storage) {
        banList.putAll(storage.loadAllBansFromStorage());
    }

    public boolean isIDBanned(UUID id) {
        return banList.containsKey(id);
    }

    public String getBanReason(UUID id) {
        if(isIDBanned(id)) {
            return banList.get(id).banReason();
        } else {
            return null;
        }
    }

    public BanDetails addBannedPlayer(Player player, String reason) {
        return addBannedPlayer(player.getUuid(), player.getUsername(), reason);
    }

    public BanDetails addBannedPlayer(UUID id, String username, String reason) {
        if(!isIDBanned(id)) {
            BanDetails details = new BanDetails(id, username, reason);
            banList.put(id, details);
            return details;
        }
        return null;
    }

    public UUID removeBannedPlayer(String username) {
        // TODO: This could be async because we're doing a O(n) search and not interacting with API, but it probably doesn't really matter that much
        // Or - create a map of UUID to username, and update that instead when removing a banned player
        for(var entry : banList.entrySet()) {
            if(entry.getValue().bannedUsername().equals(username)) {
                banList.remove(entry.getKey());
                return entry.getKey();
            }
        }
        return null;
    }

    public List<String> getBannedUsernames() {
        return banList.values().stream().map(BanDetails::bannedUsername).toList();
    }
}
