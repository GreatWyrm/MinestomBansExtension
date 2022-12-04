package com.arcanewarrior;

import com.arcanewarrior.data.BanRecord;
import com.arcanewarrior.data.PermanentBanRecord;
import com.arcanewarrior.data.TemporaryBanRecord;
import com.arcanewarrior.storage.StorageIO;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final Map<UUID, BanRecord> uuidBanList = new HashMap<>();
    // Format is key - string form of ip address, value - ban reason
    private final Map<String, String> ipBanList = new HashMap<>();

    public DataManager(StorageIO storage) {
        uuidBanList.putAll(storage.loadBans());
        ipBanList.putAll(storage.loadIpBans());
    }

    public boolean isUUIDBanned(UUID id) {
        return uuidBanList.containsKey(id) && uuidBanList.get(id).isPlayerBanned();
    }

    public boolean isIPBanned(SocketAddress address) {
        if(address instanceof InetSocketAddress inetAddress) {
            return ipBanList.containsKey(formatIpAddress(inetAddress));
        }
        return false;
    }

    public String getBanReason(UUID id) {
        if(uuidBanList.containsKey(id)) {
            return uuidBanList.get(id).banReason();
        }
        return null;
    }

    public String getIpBanReason(SocketAddress address) {
        if(address instanceof InetSocketAddress inetAddress) {
            return ipBanList.get(formatIpAddress(inetAddress));
        }
        return null;
    }

    public PermanentBanRecord addPermanentPlayerBan(UUID id, String username, String reason, String executor) {
        if(!uuidBanList.containsKey(id)) {
            PermanentBanRecord banRecord = new PermanentBanRecord(id, username, reason, executor);
            uuidBanList.put(id, banRecord);
            return banRecord;
        }
        return null;
    }
    public TemporaryBanRecord addTemporaryPlayerBan(UUID id, String username, String reason, String executor, TemporalAmount duration) {
        if(!uuidBanList.containsKey(id)) {
            TemporaryBanRecord banRecord = new TemporaryBanRecord(id, username, reason, executor, duration);
            uuidBanList.put(id, banRecord);
            return banRecord;
        }
        return null;
    }

    public UUID removeBannedPlayer(String username) {
        // TODO: This could be async because we're doing a O(n) search and not interacting with API, but it probably doesn't really matter that much
        // Or - create a map of UUID to username, and update that instead when removing a banned player
        for(var entry : uuidBanList.entrySet()) {
            if(entry.getValue().username().equals(username)) {
                uuidBanList.remove(entry.getKey());
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
        return uuidBanList.values().stream().map(BanRecord::username).toList();
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
