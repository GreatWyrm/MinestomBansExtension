package com.arcanewarrior;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

public class EventsHandler {

    private final DataManager dataManager;
    private final BanListener banListener;

    public EventsHandler(DataManager dataManager) {
        this.dataManager = dataManager;
        banListener = new BanListener();
    }

    public void registerEvents() {
        MinecraftServer.getGlobalEventHandler().addListener(banListener);
    }

    public void unregisterEvents() {
        MinecraftServer.getGlobalEventHandler().removeListener(banListener);
    }


    private class BanListener implements EventListener<AsyncPlayerPreLoginEvent> {
        @Override
        public @NotNull Class<AsyncPlayerPreLoginEvent> eventType() {
            return AsyncPlayerPreLoginEvent.class;
        }
        @Override
        public @NotNull Result run(@NotNull AsyncPlayerPreLoginEvent event) {
            if(dataManager.isUUIDBanned(event.getPlayerUuid())) {
                event.getPlayer().kick(Component.text("You have been banned from this server.\n" + dataManager.getBanReason(event.getPlayerUuid()), NamedTextColor.RED));
            }
            if(dataManager.isIPBanned(event.getPlayer().getPlayerConnection().getRemoteAddress())) {
                event.getPlayer().kick(Component.text("You have been banned from this server.\n" + dataManager.getIpBanReason(event.getPlayer().getPlayerConnection().getRemoteAddress()), NamedTextColor.RED));
            }
            return Result.SUCCESS;
        }
    }
}
