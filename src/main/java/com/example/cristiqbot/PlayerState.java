package com.example.cristiqbot;

import org.bukkit.entity.Player;
import java.util.UUID;
import java.time.Instant;

public class PlayerState {
    private final UUID playerId;
    private boolean chatEnabled;
    private Instant lastChatTime;
    private String lastWorld; // For per-world personality

    public PlayerState(Player player) {
        this.playerId = player.getUniqueId();
        this.chatEnabled = CristiqBot.getInstance().getPluginConfig().getBoolean("chat.enabled_by_default", false);
        this.lastChatTime = Instant.now();
        this.lastWorld = player.getWorld().getKey().getKey();
    }

    // Getters/Setters
    public UUID getPlayerId() { return playerId; }
    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean enabled) { chatEnabled = enabled; }
    public Instant getLastChatTime() { return lastChatTime; }
    public void setLastChatTime(Instant time) { lastChatTime = time; }
    public String getLastWorld() { return lastWorld; }
    public void setLastWorld(String world) { lastWorld = world; }

    public boolean canChatNow() {
        return Instant.now().minusSeconds(2).isAfter(lastChatTime);
    }
}
