package com.arcanewarrior.data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public record TemporaryBanRecord(UUID uuid, String username, String banReason, String banExecutor, ZonedDateTime banTime, ZonedDateTime expiryTime) implements BanRecord {
    public TemporaryBanRecord(UUID uuid, String username, String banReason, String banExecutor, TemporalAmount banDuration) {
        this(uuid, username, banReason, banExecutor, ZonedDateTime.now(), ZonedDateTime.now().plus(banDuration));
    }


    @Override
    public boolean isPlayerBanned() {
        return LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()).isAfter(expiryTime);
    }

    @Override
    public Map<String, String> toPropertiesMap() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("uuid", uuid.toString());
        properties.put("username", username);
        properties.put("banReason", banReason);
        properties.put("banExecutor", banExecutor);
        properties.put("banTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(banTime));
        properties.put("expiryTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(expiryTime));
        return properties;
    }
}
