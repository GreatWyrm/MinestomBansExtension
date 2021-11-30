package com.arcanewarrior.data;

import java.util.UUID;

public record BanDetails(UUID uuid, String bannedUsername, String banReason) {
}
