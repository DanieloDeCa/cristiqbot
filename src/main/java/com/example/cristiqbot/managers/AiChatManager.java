package com.example.cristiqbot.managers;

import com.example.cristiqbot.AiClient;
import com.example.cristiqbot.CristiqBot;
import com.example.cristiqbot.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AiChatManager implements Listener {
    private final CristiqBot plugin;
    private final AiClient aiClient;
    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final String mentionPrefix;

    public AiChatManager(CristiqBot plugin) {
        this.plugin = plugin;
        this.aiClient = new AiClient(plugin);
        this.mentionPrefix = plugin.getPluginConfig().getString("chat.mention_prefix", "CristiqBot");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("AiChatManager initialized.");
    }

    public void toggleChat(Player player, boolean enable) {
        PlayerState state = getPlayerState(player);
        state.setChatEnabled(enable);
        if (enable) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage("CristiqBot: Hello, I'm CristiqBot - your AI companion in this world!"));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        if (!message.startsWith(mentionPrefix + " ")) return;

        event.setCancelled(true); // Handle privately or broadcast response

        PlayerState state = getPlayerState(player);
        if (!state.isChatEnabled() || !state.canChatNow()) return;

        String query = message.substring(mentionPrefix.length() + 1);
        state.setLastChatTime(Instant.now());
        state.setLastWorld(player.getWorld().getKey().getKey());

        String personality = plugin.getPluginConfig().getString("chat.personality", "Friendly, witty, and helpful");
        aiClient.generateResponse(query, personality, state.getLastWorld())
                .thenAccept(response -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        // Broadcast to nearby players or just sender
                        player.sendMessage("CristiqBot: " + response);
                    });
                });
    }

    private PlayerState getPlayerState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), k -> new PlayerState(player));
    }

    public void shutdown() {
        playerStates.clear();
    }
}
