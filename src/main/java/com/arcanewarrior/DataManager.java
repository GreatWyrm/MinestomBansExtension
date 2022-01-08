package com.arcanewarrior;

import com.arcanewarrior.data.BanDetails;
import com.arcanewarrior.storage.StorageIO;
import net.minestom.server.entity.Player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Map<UUID, BanDetails> banList = new HashMap<>();
    // Format is key - string form of ip address, value - ban reason
    private final Map<String, String> ipBanList = new HashMap<>();

    public DataManager(StorageIO storage) {
        banList.putAll(storage.loadPlayerBansFromStorage());
        ipBanList.putAll(storage.loadIpBansFromStorage());
    }

    public boolean isIDBanned(UUID id) {
        return banList.containsKey(id);
    }

    public boolean isIPBanned(SocketAddress address) {
        if(address instanceof InetSocketAddress inetAddress) {
            return ipBanList.containsKey(formatIpAddress(inetAddress));
        }
        return false;
    }

    public String getBanReason(UUID id) {
        if(isIDBanned(id)) {
            return banList.get(id).banReason();
        } else {
            return null;
        }
    }

    public String getIpBanReason(SocketAddress address) {
        if(address instanceof InetSocketAddress inetAddress) {
            return ipBanList.get(formatIpAddress(inetAddress));
        }
        return "";
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

    public String addBannedIP(SocketAddress address, String reason) {
        if(address instanceof InetSocketAddress inetAddress && !isIPBanned(inetAddress)) {
            ipBanList.put(formatIpAddress(inetAddress), reason);
            return formatIpAddress(inetAddress);
        }
        return null;
    }

    public String removeBannedIP(String address) {
        if(ipBanList.containsKey(address)) {
            ipBanList.remove(address);
            return address;
        } else {
            return null;
        }

    }

    public List<String> getBannedUsernames() {
        return banList.values().stream().map(BanDetails::bannedUsername).toList();
    }

    public List<String> getBannedIps() {
        return ipBanList.keySet().stream().toList();
    }

    /**
     * Formats the InetSocketAddress into a comparable and storable string
     * @param address The address to convert
     * @return The formatted string
     */
    private String formatIpAddress(InetSocketAddress address) {
        String ipAddress = address.getAddress().toString();
        if(ipAddress.startsWith("/")) {
            ipAddress = ipAddress.substring(1);
        }
        return ipAddress;
    }
}
