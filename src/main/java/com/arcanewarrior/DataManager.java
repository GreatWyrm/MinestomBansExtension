package com.arcanewarrior;

import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public void addBannedPlayer(Player player, String reason) {
        if(!isIDBanned(player.getUuid())) {
            banList.put(player.getUuid(), new BanDetails(player.getUsername(), reason));
        }
    }

    public UUID removeBannedPlayer(String username) {
        // TODO: This could be async because we're doing a O(n) search and not interacting with API, but it probably doesn't really matter that much
        for(var entry : banList.entrySet()) {
            if(entry.getValue().bannedUsername().equals(username)) {
                banList.remove(entry.getKey());
                return entry.getKey();
            }
        }
        return null;
    }

    public List<String> getBannedUsernames() {
        return banList.values().stream().map(BanDetails::bannedUsername).collect(Collectors.toList());
    }
}
