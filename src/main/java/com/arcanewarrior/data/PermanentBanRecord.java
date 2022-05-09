package com.arcanewarrior.data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record PermanentBanRecord(UUID uuid, String username, String banReason, String banExecutor, ZonedDateTime banTime) implements BanRecord {

    public PermanentBanRecord(UUID uuid, String username, String banReason, String banExecutor) {
        this(uuid, username, banReason, banExecutor, ZonedDateTime.now());
    }
    @Override
    public boolean isPlayerBanned() {
        return true;
    }

    @Override
    public Map<String, String> toPropertiesMap() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("uuid", uuid.toString());
        properties.put("username", username);
        properties.put("banReason", banReason);
        properties.put("banExecutor", banExecutor);
        properties.put("banTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(banTime));
        return properties;
    }
}
